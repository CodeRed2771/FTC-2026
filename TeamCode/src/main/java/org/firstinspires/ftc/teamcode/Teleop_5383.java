package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.teamcode.Biobuzz.Biobuzz_Intake_5383;
import org.firstinspires.ftc.teamcode.Biobuzz.Biobuzz_Shooter_5383;
import org.firstinspires.ftc.teamcode.Biobuzz.Biobuzz_Storage_5383;
import org.firstinspires.ftc.teamcode.Biobuzz.DriveTrain_5383;

@TeleOp(name = "2026:5383:Neutrinos Drive", group = "Iterative Opmode")
public class Teleop_5383 extends OpMode {

    Orientation angles;
    Acceleration gravity;
    
    DriveTrain driveTrain;
    Biobuzz_Intake_5383 intake;
    Biobuzz_Shooter_5383 shooter;
    Biobuzz_Storage_5383 storage;
    RobotCamera camera;

    private PageDisplay display;

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

        driveTrain = new DriveTrain_5383(hardwareMap, telemetry);
        intake = new Biobuzz_Intake_5383(hardwareMap);
        shooter = new Biobuzz_Shooter_5383(hardwareMap);
        storage = new Biobuzz_Storage_5383(hardwareMap);

        camera = new RobotCamera(hardwareMap, telemetry,"Webcam 1");

        intake.reset();
        shooter.reset();

        display.addPage("Main", "Game Info",1, this::printTelemetry);
        display.addPage("Camera","April Tags", 0, camera::telemetryAprilTag);
        driveTrain.registerPages(display);
        intake.registerPages(display);
        shooter.registerPages(display);
        storage.registerPages(display);

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        display.gotoPage("Storage", "Motor Cmds");

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

        camera.doLoop();
        driveTrain.loop();
        intake.loop();
        shooter.loop();
        storage.loop();

        doTelemetry();
    }
    
    private void doTelemetry(){
        if((gamepad1.backWasPressed() || gamepad2.backWasPressed()))
            display.nextPage();

        display.print(telemetry);
        telemetry.update();
    }

    
    private void doCamera() {
        
//        if(camera.goalFound()){
//            autoaim_bearing = camera.getAutoaim_bearing();
//            autoaim_distance = camera.getAutoaim_distance();
//            double aim_range_adj = (115-autoaim_distance)/(115-50)*3.0;
//            if (!LED_TROUBLESHOOTING) {
//                if (Double.isNaN(autoaim_bearing)) {
//                    led.setPattern(RevBlinkinLedDriver.BlinkinPattern.RED);
//                } else if (autoaim_bearing - aim_bias < -1 * (AIM_LED_TOLERANCE + aim_range_adj) ) {
//                    led.setPattern(RevBlinkinLedDriver.BlinkinPattern.YELLOW);
//                } else if (autoaim_bearing - aim_bias > (AIM_LED_TOLERANCE + aim_range_adj)) {
//                    led.setPattern(RevBlinkinLedDriver.BlinkinPattern.BLUE);
//                } else {
//                    led.setPattern(RevBlinkinLedDriver.BlinkinPattern.GREEN);
//                }
//            }
//        } else {
//            led.setPattern(RevBlinkinLedDriver.BlinkinPattern.VIOLET);
//            autoaim_bearing = 0.0;
//        }
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

        if (gamepad2.bWasPressed())
            storage.set(Biobuzz_Storage_5383.Presets.UP);

        if (gamepad2.aWasPressed())
            storage.set(Biobuzz_Storage_5383.Presets.DOWN);

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

    }
    
}