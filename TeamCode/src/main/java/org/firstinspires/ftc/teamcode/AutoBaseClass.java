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

import org.firstinspires.ftc.teamcode.Decode.DecodeCamera;
import org.firstinspires.ftc.teamcode.Decode.Emergency.Intake_5384_2025;
import org.firstinspires.ftc.teamcode.Decode.Emergency.Shooter_5384_2025;
import org.firstinspires.ftc.teamcode.Decode.Emergency.Storage_5384_2025;

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

public class AutoBaseClass extends LinearOpMode {

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

    protected double delayedStart = 0.0;

    public void initializeRobot() {
        driveTrain = new DriveTrain(hardwareMap, telemetry);
//        intake = new Intake_5384_2025(hardwareMap, telemetry);
//        shooter = new Shooter_5384_2025(hardwareMap, telemetry);
//        storage = new Storage_5384_2025(hardwareMap, telemetry);
        camera = new DecodeCamera(hardwareMap, telemetry,"Webcam 1");
    }

    public void delay(double milliseconds){
        double giveUpTime = 0;

        giveUpTime = System.currentTimeMillis()+milliseconds;
        while (opModeIsActive() && System.currentTimeMillis() < giveUpTime) {
            autoLoop();
            idle();
        }
    }

    // Assumes caller has set speed to match shooting position already.

    
    @Override
    public void runOpMode() {
    }
    
    public void autoLoop(){
        camera.doLoop();
//        intake.loop();
//        shooter.loop();
//        storage.loop();
//        storage.telemetry(3);
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

    int up_deadband = 0;
    int down_deadband = 0;
    public void setupAutoDelayedStart(){
        // Use deadband variabls to detect when DPAD is pressed.
        // ignore anything after the rising edge of the press.
        if(gamepad1.dpad_up || gamepad2.dpad_up){
            up_deadband += 1;
        } else {
            if (up_deadband > 0)
                delayedStart += 1.0;
            up_deadband = 0;
        }
        if(gamepad1.dpad_down || gamepad2.dpad_down)
        {
            down_deadband += 1;
        } else {
            if (down_deadband > 0)
                delayedStart -= 1.0;
            down_deadband = 0;
        }

        // Use min/max to prevent driver from getting absurd delays.
        delayedStart = Math.max(delayedStart,0);
        delayedStart = Math.min(delayedStart,30);

        telemetry.addData("DELAY", delayedStart);
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

        telemetry.update();
    }
}
