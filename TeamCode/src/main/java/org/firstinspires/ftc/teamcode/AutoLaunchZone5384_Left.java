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

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

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
@Autonomous(name = "Auto Launch Zone Leave", group = "5384")

public class AutoLaunchZone5384_Left extends AutoBaseClass_5384_2025 {

    // Starts with robot facing the wall in the Launch Zone
    private int Max_RUNTIME = 30;
    @Override
    public void runOpMode() {
        double delayedStart = 0.0;
        int up_deadband = 0;
        int down_deadband = 0;

        initializeRobot();
        
        telemetry.addData("Status", "Initialized");
        telemetry.update();
        // Wait for the game to start (driver presses PLAY)
        // driveTrain.setRunToPosition();
        storage.ejectorStow();
        
        while (!isStarted()) {

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
            delayedStart = Math.max(delayedStart,0);
            delayedStart = Math.min(delayedStart,30);
            telemetry.addData("DELAY", delayedStart);

            setupAllianceAndPosition();
            idle();
        }
        opModeIsActive();

        delay(delayedStart * 1000);

        // #1 Drive forward to get leave points
        driveTrain.driveDistance(12.0, 1, 3);

        // #2.0 look for the Hive to be tipped how we want.
        // Wait up to X seconds leaving enough time for parking
        delay(1000);

        // #2 Shoot the Pollen
        // 2.1 Start spinning the motors on the shooter
        shooter.setPowerMid();

        // 2.2 get a pollen from storage ready to go
        // storage.select();

        // 2.3 set the speed based on distance - may only need 1 or 2 speeds to select
        shooter.setPowerMid();

        // 2.4 wait until the motors are ready
        delay(1000);
        //  - wait fixed amount of time
        //  - wait until it stops accelerating < +/- X of 0 acceleration
        //  - wait until motor is at speed target < +/- Y of target
        // DRY: Don't Repeat Yourself
        delay(1000);


        // 2.5 transfer from storage to the shooter
        storage.ejectorLaunch();
        // Decide if we wait until motors get back up to speed or just shoot all elements


        // #3 Park in the parking zone
        driveTrain.slideDistance(5*12.0, 1.0,10.0);

        // #3.1 Move right 5 ft
    }
}