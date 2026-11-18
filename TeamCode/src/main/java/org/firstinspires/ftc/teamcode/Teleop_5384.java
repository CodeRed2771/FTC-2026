package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.Biobuzz.Biobuzz_Flower_5384;
import org.firstinspires.ftc.teamcode.Biobuzz.Biobuzz_Intake_5384;
import org.firstinspires.ftc.teamcode.Biobuzz.Biobuzz_Shooter_5384;
import org.firstinspires.ftc.teamcode.Decode.DecodeCamera;
import org.firstinspires.ftc.teamcode.Decode.Emergency.Lift_5384_2025;
import org.firstinspires.ftc.teamcode.Decode.Emergency.Storage_5384_2025;

@TeleOp(name = "2026:5384:Emergency Drive", group = "Iterative Opmode")
public class Teleop_5384 extends OpMode {

    Orientation angles;
    IMU imu;
    Acceleration gravity;
    
    RevBlinkinLedDriver led;
    RevBlinkinLedDriver.BlinkinPattern led_pattern;

    DriveTrain driveTrain;
    Biobuzz_Intake_5384 intake;
    Biobuzz_Flower_5384 flower;
    Biobuzz_Shooter_5384 shooter;
    Storage_5384_2025 storage;
    DecodeCamera camera;
    Lift_5384_2025 lift;

    private PageDisplay display;

    boolean red_goal_found = false;
    boolean blue_goal_found = false;
    

    final double AIM_LED_TOLERANCE = 1.0;
    final double aim_bias = 2.0;

    boolean aPressed = false;
    int artifactCount = 3;
    private final double autoAim_limit = 0.5;
    private final double autoAim_kP = autoAim_limit / 25.0; // max command / Max offangle
    private double autoaim_distance = 0.0;
    private double autoaim_bearing = 0.0;
    private boolean autoaim = false;
    private boolean LED_TROUBLESHOOTING = false;
    private boolean DISABLE_LIFT = true;

    private final int MAX_PAGES = 20;

    private String alliance;

    @Override
    public void init() {
        display = new PageDisplay();

        driveTrain = new DriveTrain(hardwareMap);
        restoreAutoState();

        intake = new Biobuzz_Intake_5384(hardwareMap);
        flower = new Biobuzz_Flower_5384(hardwareMap);
//        storage = new Storage_5384_2025(hardwareMap);
        shooter = new Biobuzz_Shooter_5384(hardwareMap);
//        if (!DISABLE_LIFT) {
//            lift = new Lift_5384_2025(hardwareMap);
//        }
        camera = new DecodeCamera(hardwareMap, "Webcam 1");

        display.addPage("Main", "Game Info",1, this::printTelemetry);
        driveTrain.registerPages(display);
        intake.registerPages(display);
        shooter.registerPages(display);
        flower.registerPages(display);

        intake.reset();
        shooter.reset();
        flower.reset();

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        display.gotoPage("Shooter", "State Info");

        driveTrain.setFieldCentricState(false);
    }

    protected void restoreAutoState(){
        alliance = (String) blackboard.getOrDefault("Alliance","UNKNOWN");

        if("RED".equals(alliance))
            driveTrain.setFCAngle(AngleUnit.DEGREES, 90.0);
        else if("BLUE".equals(alliance))
            driveTrain.setFCAngle(AngleUnit.DEGREES, -90.0);


        Object x = blackboard.getOrDefault("auto_x",0.0);
        Object y = blackboard.getOrDefault("auto_y",0.0);
        Object hdg = blackboard.getOrDefault("auto_hdg",0.0);

        if(x == null || y == null || hdg == null)
            return;

        Pose2D position = new Pose2D(DistanceUnit.INCH, (double) x, (double) y, AngleUnit.DEGREES, (double) hdg);
        driveTrain.setCurrentPosition(position);
    }

    public void printTelemetry(Telemetry telemetry, int page){
        switch(page){
            default:
                telemetry.addData("Alliance", alliance);
                break;
        }
    }
    
    @Override
    public void loop() {

//        doCamera();

        doDrive();
        doShooter();
        doIntake();
//        doStorage();
//        doLift();

        camera.doLoop();
        driveTrain.loop();
        intake.loop();
        flower.loop();
        shooter.loop();
//        storage.loop();
        
        doTelemetry();
    }
    
    private void doTelemetry(){
        if(gamepad1.backWasPressed())
            display.nextGroup();
        if(gamepad2.backWasPressed())
            display.nextPage();

        display.print(telemetry);
        telemetry.update();
    }
    
    private void doCamera() {
        red_goal_found = camera.redGoalFound();
        blue_goal_found = camera.blueGoalFound();
        
        if(camera.goalFound()){
            autoaim_bearing = camera.getAutoaim_bearing();
            autoaim_distance = camera.getAutoaim_distance();
            double aim_range_adj = (115-autoaim_distance)/(115-50)*3.0;
            if (!LED_TROUBLESHOOTING) {
                if (Double.isNaN(autoaim_bearing)) {
                    led.setPattern(RevBlinkinLedDriver.BlinkinPattern.RED);
                } else if (autoaim_bearing - aim_bias < -1 * (AIM_LED_TOLERANCE + aim_range_adj) ) {
                    led.setPattern(RevBlinkinLedDriver.BlinkinPattern.YELLOW);
                } else if (autoaim_bearing - aim_bias > (AIM_LED_TOLERANCE + aim_range_adj)) {
                    led.setPattern(RevBlinkinLedDriver.BlinkinPattern.BLUE);
                } else {
                    led.setPattern(RevBlinkinLedDriver.BlinkinPattern.GREEN);
                }
            }
        } else {
            led.setPattern(RevBlinkinLedDriver.BlinkinPattern.VIOLET);
            autoaim_bearing = 0.0;   
        }
    }

    double steerToTag = 0.0;
    private void doDrive(){
        driveTrain.speedAdjust(gamepad1.startWasPressed());

        if (false && gamepad1.rightBumperWasPressed())
            driveTrain.resetFCAngle();

        if (false && gamepad1.leftBumperWasPressed())
            driveTrain.toggleFieldCentricState();

        double slide = -gamepad1.left_stick_x;
        double turn = -gamepad1.right_stick_x;
        double forward = -gamepad1.left_stick_y;
        driveTrain.runDrive_fieldCentric(forward, slide, turn+steerToTag);
    }
    
    private boolean wasShooting = false;
    private void doShooter() {

        if(gamepad2.leftBumperWasPressed())
            shooter.setAdjustment(shooter.getAdjustment() - 0.05);
        if(gamepad2.rightBumperWasPressed())
            shooter.setAdjustment(shooter.getAdjustment() + 0.05);

        if (gamepad2.aWasPressed()) {
            autoaim = false;
            shooter.setPowerHive();
            intake.fireAll();
        }
        else if (gamepad2.x) {
            autoaim = false;
            shooter.stop();
            intake.stop();
        }
        else if (gamepad1.dpad_down) {
            autoaim = false;
            shooter.eject();
            intake.eject();
        }
        
        if (autoaim)
        {
            shooter.setPowerHive();
        }
    }

    private void doIntake() {
        if (gamepad2.dpad_up) {
            intake.startIntake();
        }
        else if (gamepad2.dpad_left)  {
            intake.stop();
        }
        else if (gamepad2.dpad_down){
            intake.eject();
        }

        if (gamepad2.bWasPressed()){
            if (flower.getMode() == Biobuzz_Flower_5384.Presets.DEPLOY)
                flower.stow();
            else
                flower.deploy();
        }
    }
    
}