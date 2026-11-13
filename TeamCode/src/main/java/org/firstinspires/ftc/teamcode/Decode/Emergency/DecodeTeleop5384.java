package org.firstinspires.ftc.teamcode.Decode.Emergency;

import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.teamcode.Decode.Artifact;
import org.firstinspires.ftc.teamcode.Decode.DecodeCamera;
import org.firstinspires.ftc.teamcode.Decode.Emergency.Storage_5384_2025.EjectorPosition;
import org.firstinspires.ftc.teamcode.Decode.Emergency.Storage_5384_2025.WiperPosition;
import org.firstinspires.ftc.teamcode.DriveTrain;
import org.firstinspires.ftc.teamcode.PageDisplay;

@TeleOp(name = "Decode - 5384 Drive 2025", group = "Iterative Opmode")

public class DecodeTeleop5384 extends OpMode {

    Orientation angles;
    IMU imu;
    Acceleration gravity;
    
    RevBlinkinLedDriver led;
    RevBlinkinLedDriver.BlinkinPattern led_pattern;

    DriveTrain driveTrain;
    Intake_5384_2025 intake;
    Shooter_5384_2025 shooter;
    Storage_5384_2025 storage;
    DecodeCamera camera;
    Lift_5384_2025 lift;
    PageDisplay display = new PageDisplay();

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

    
    @Override
    public void init() {
        led = hardwareMap.get(RevBlinkinLedDriver.class,"led");

        display.addPage("Robot", "Main", 0, this::robotTelemetry);


        driveTrain = new DriveTrain(hardwareMap);
        driveTrain.registerPages(display);

        intake = new Intake_5384_2025(hardwareMap);
        intake.registerPages(display);

        storage = new Storage_5384_2025(hardwareMap);
        storage.registerPages(display);

        shooter = new Shooter_5384_2025(hardwareMap);
        shooter.registerPages(display);

        if (!DISABLE_LIFT) {
            lift = new Lift_5384_2025(hardwareMap);
            lift.registerPages(display);
        }

        camera = new DecodeCamera(hardwareMap,"Webcam 1");
        camera.registerPages(display);

        telemetry.addData("Status", "Initialized");
        telemetry.update();
    }
    
    @Override
    public void loop() {

        doCamera();

        doDrive();
        doShooter();
        doIntake();
        doStorage();
        doLift();

        camera.doLoop();
        driveTrain.loop();
        intake.loop();
        shooter.loop();
        storage.loop();
        
        doTelemetry();
    }
    
    boolean back_prev = false;
    int page = 0;
    private void doTelemetry() {
        if (gamepad1.backWasPressed())
            display.nextGroup();
        if (gamepad2.backWasPressed())
            display.nextPage();

        display.print(telemetry);
        telemetry.update();
    }

    public void robotTelemetry(Telemetry telemetry,int page){
        switch(page){
            case 0:
                if (LED_TROUBLESHOOTING) {
                    led.setPattern(RevBlinkinLedDriver.BlinkinPattern.STROBE_RED);
                }
                telemetry.addData("Red Found", red_goal_found);
                telemetry.addData("Blue Found", blue_goal_found);
                telemetry.addData("Autoaim", autoaim);
                telemetry.addData("autoaim_distance", autoaim_distance);
                telemetry.addData("autoaim_bearing", autoaim_bearing);
                break;
            default:
                if (LED_TROUBLESHOOTING) {
                    led.setPattern(RevBlinkinLedDriver.BlinkinPattern.BLACK);
                }
                break;
        }
        
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
        double aim_command = autoAim_kP * autoaim_bearing;
    
        if (gamepad2.left_trigger > .5) {
            autoaim = true;

            if (aim_command >= autoAim_limit) {
                aim_command = autoAim_limit;
            } else if (aim_command <= -autoAim_limit) {
                aim_command = -autoAim_limit;
            } else {
                aim_command = aim_command;
            }
            steerToTag = aim_command;

        }
        else
            steerToTag = 0.0;

        if (gamepad2.dpad_up) {
            autoaim = false;
            shooter.setPowerFar();
        }
        else if (gamepad2.dpad_left || gamepad2.dpad_right) {
            autoaim = false;
            shooter.setPowerMid();
        }
        else if (gamepad2.dpad_down) {
            autoaim = false;
            shooter.setPowerNear();
        }
        
        if (autoaim)
        {
            shooter.setShooterDistance(autoaim_distance);
        }
    
        if (false) {
            // Continuous Shot Mode
            if (gamepad2.right_trigger > .5) {
                EjectorPosition ejectorPosition = storage.getEjector();
                // If we've finished shooting, prepare to shoot again.
                if (wasShooting && (ejectorPosition == EjectorPosition.UP)) {
                    storage.ejectorStow();
                    wasShooting = false;
                    storage.select(Artifact.UNKNOWN);
                }
                // Check to see if we are ready to shoot again.
                if (!wasShooting
                    // && storage.getStoredArtifactCount() > 0
                    // && shooter.readyToShoot()
                    // && shooter.atShootingSpeed()
                    && ejectorPosition == EjectorPosition.DOWN) {
                    wasShooting = true;
                    storage.ejectorLaunch();
                }
            }
        } else {
            // Single Shot mode
            if (gamepad2.right_trigger > .5) {

                shooter.setShooterSpeed(5000.0);

                wasShooting = true;
                storage.ejectorLaunch();
            }
            else if (wasShooting && (storage.getEjector() == EjectorPosition.UP))
            {
                storage.ejectorStow();
                wasShooting = false;
            }
        }
        
        if(gamepad2.right_bumper)
        {
            autoaim = false;
            shooter.setShooterSpeed(0.0);
        }
    }
    
    private void doIntake() {
        if (gamepad1.a) {
            intake.startIntake();
            shooter.stop();
            storage.ejectorStow();

        }
        else if (gamepad1.x)  {
            intake.stopIntake();
        }
        if (gamepad1.y){
            intake.ejectIntake();
        }
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