/*
Copyright 2025 FIRST Tech Challenge Team 5383

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute,
sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial
portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Storage_5384_2025.EjectorPosition;
/**
 * This file contains a minimal example of a Linear "OpMode". An OpMode is a 'program' that runs
 * in either the autonomous or the TeleOp period of an FTC match. The names of OpModes appear on
 * the menu of the FTC Driver Station. When an selection is made from the menu, the corresponding
 * OpMode class is instantiated on the Robot Controller and executed.
 *
 * Remove the @Disabled annotation on the next line or two (if present) to add this OpMode to the
 * Driver Station OpMode list, or add a @Disabled annotation to prevent this OpMode from being
 * added to the Driver Station.
 */

public class AutoBaseClass_5384_2025 extends LinearOpMode {
    protected DriveTrain_5384_2025 driveTrain;
    protected Intake_5384_2025 intake;
    protected Shooter_5384_2025 shooter;
    protected Storage_5384_2025 storage;
    protected DecodeGameCamera camera;
    
    protected ElapsedTime runtime = new ElapsedTime();
    double timerSetPoint = 999;


    public enum Alliance {
        AMBER,
        BLUE,
        RED
    };

    public enum StartPosition {
        LEFT,
        RIGHT
    }
    protected Alliance alliance;
    protected StartPosition fieldPosition;

    public void initializeRobot() {
        driveTrain = new DriveTrain_5384_2025(hardwareMap, telemetry);
        intake = new Intake_5384_2025(hardwareMap, telemetry);
        shooter = new Shooter_5384_2025(hardwareMap, telemetry);
        storage = new Storage_5384_2025(hardwareMap, telemetry);
        camera = new DecodeGameCamera(hardwareMap, telemetry,"Webcam 1");
    }
    public void delay(double milliseconds) {

        double giveUpTime = System.currentTimeMillis() + milliseconds;
        while (camera.getObeliskID() == 0 && opModeIsActive() && System.currentTimeMillis() < giveUpTime) {
            camera.doLoop();
            camera.telemetry(0);
            telemetry.update();

            idle();
        }
    }

    
    // Assumes caller has set speed to match shooting position already.
    protected void autoShoot(){
        double giveUpTime = 0;

        giveUpTime = System.currentTimeMillis()+1000;
        while(!shooter.atShootingSpeed() && System.currentTimeMillis() < giveUpTime ){
            autoLoop();
        }

        giveUpTime = System.currentTimeMillis()+1000;
        storage.ejectorLaunch();
        while(storage.getEjector() != EjectorPosition.UP && System.currentTimeMillis() < giveUpTime){
            autoLoop();
        }

        giveUpTime = System.currentTimeMillis()+1000;
        storage.ejectorStow();
        while(storage.getEjector() != EjectorPosition.DOWN && System.currentTimeMillis() < giveUpTime){
            autoLoop();
        }
    }
    
    @Override
    public void runOpMode() {
    }
    
    public void autoLoop(){
        camera.doLoop();
        intake.loop();
        shooter.loop();
        storage.loop();
        storage.telemetry(3);
        telemetry.update();
    }
    
    boolean timeExpired() {
        return runtime.seconds() >= timerSetPoint;
    }

    public void setTimer(double secs) {
        timerSetPoint = runtime.seconds() + secs;
    }

    public boolean isBlueAlliance() {
        return alliance == Alliance.BLUE;
    }

    public boolean isRedAlliance() {
        return alliance == Alliance.RED;
    }


    public boolean inStartingPositionL() {
        return fieldPosition == StartPosition.LEFT;
    }

    public boolean inStartingPositionR() {
        return fieldPosition == StartPosition.RIGHT;
    }

    public void setupAllianceAndPosition() {

        if (gamepad1.x || gamepad2.x) {
            alliance = Alliance.BLUE;
        }
        if (gamepad1.b || gamepad2.b) {
            alliance = Alliance.RED;
        }
     
        if (isBlueAlliance()) {
            // setGreenLED();
            telemetry.addData("ALLIANCE", "Alliance: BLUE");
        }
        else if (isRedAlliance()) {
            // setRedLED();
            telemetry.addData("ALLIANCE", "Alliance: RED");
        }
        else {
            telemetry.addData("ALLIANCE", "Alliance: Undefined");
        }

        storage.telemetry(3);

        telemetry.update();
    }
    
    public void gyroStart() {
        // Start the logging of measured acceleration
        //imu.startAccelerationIntegration(new Position(), new Velocity(), 1000);
    }

  
}
