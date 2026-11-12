package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.teamcode.Biobuzz.Biobuzz_Flower_5384;
import org.firstinspires.ftc.teamcode.Biobuzz.Biobuzz_Intake_5384;
import org.firstinspires.ftc.teamcode.Biobuzz.Biobuzz_Shooter_5384;
import org.firstinspires.ftc.teamcode.Decode.Artifact;
import org.firstinspires.ftc.teamcode.Decode.DecodeCamera;

import org.firstinspires.ftc.teamcode.Decode.Emergency.Lift_5384_2025;
import org.firstinspires.ftc.teamcode.Decode.Emergency.Storage_5384_2025;
import org.firstinspires.ftc.teamcode.Decode.Emergency.Storage_5384_2025.WiperPosition;

@TeleOp(name = "2026:5384:Emergency Drive", group = "Iterative Opmode")
public class Teleop extends OpMode {

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

        alliance = (String) blackboard.getOrDefault("Alliance","UNKNOWN");
//        led = hardwareMap.get(RevBlinkinLedDriver.class,"led");
        
        driveTrain = new DriveTrain(hardwareMap, telemetry);
        intake = new Biobuzz_Intake_5384(hardwareMap);
        flower = new Biobuzz_Flower_5384(hardwareMap);
//        storage = new Storage_5384_2025(hardwareMap, telemetry);
        shooter = new Biobuzz_Shooter_5384(hardwareMap);
//        if (!DISABLE_LIFT) {
//            lift = new Lift_5384_2025(hardwareMap, telemetry);
//        }
        camera = new DecodeCamera(hardwareMap, telemetry,"Webcam 1");

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

        display.gotoPage("Intake", "State Info");
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
        if((gamepad1.backWasPressed() || gamepad2.backWasPressed()))
            display.nextPage();

        display.print(telemetry);
        telemetry.update();
    }
    
    private void doLift() {
        if (lift == null) {
            return;
        }
        if (gamepad1.right_trigger > 0.5 && gamepad1.left_trigger > 0.5) {
            
        }    
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
        double slide = -gamepad1.left_stick_x;
        double turn = -gamepad1.right_stick_x;
        double forward = -gamepad1.left_stick_y;

        driveTrain.runDrive_fieldCentric(gamepad1, forward, slide, turn+steerToTag);
    }
    
    private boolean wasShooting = false;
    private void doShooter() {

        if (gamepad1.dpad_up) {
            autoaim = false;
            shooter.setPowerHive();
        }
        else if (gamepad1.dpad_left || gamepad1.dpad_right) {
            autoaim = false;
            shooter.stop();
        }
        else if (gamepad1.dpad_down) {
            autoaim = false;
            shooter.eject();
        }
        
        if (autoaim)
        {
            shooter.setPowerHive();
        }
    }

    private void doIntake() {
        if (gamepad1.a) {
            intake.startIntake();
        }
        else if (gamepad1.x)  {
            intake.stopIntake();
        }
        else if (gamepad1.y){
            intake.ejectIntake();
        }

        if (gamepad1.bWasPressed()){
            if (flower.getMode() == Biobuzz_Flower_5384.Presets.DEPLOY)
                flower.stow();
            else
                flower.deploy();
        }

//        intake.setMaxLoaderCommand(1.0 - gamepad2.right_trigger);
    }
    
    static boolean pressed = false;
    private void doStorage() {
        if(gamepad2.right_bumper)
            storage.ejectorStow();

            
        if(gamepad2.start)
            storage.setWiperPosition(WiperPosition.LEFT, WiperPosition.RIGHT);
        else if(gamepad2.y && !pressed) // top
        {
            pressed = true;
            storage.captureArtifact();
        }
        else if(gamepad2.a && !pressed)
        {
            pressed = true;
            storage.select(Artifact.UNKNOWN);
        }
        else if(gamepad2.b && !pressed)
        {
            pressed = true;
            storage.ejectorStow();
            storage.setWiperPosition(WiperPosition.RIGHT, WiperPosition.RIGHT);
        }
        else if(gamepad2.x && !pressed)
        {
            pressed = true;
            storage.ejectorStow();
            storage.setWiperPosition(WiperPosition.LEFT, WiperPosition.LEFT);
        }
        else{
            pressed = false;
        }

   }
    
}