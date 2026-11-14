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
@Autonomous(name = "2026:5384:Emergency Just Drive Left", group = "5384")
public class Auto_Just_Drive_Left_5384 extends AutoBaseClass_5384 {

    // Starts with robot facing the wall in the Launch Zone
    private int MAX_RUNTIME = 30;
    @Override
    public void runOpMode() {

        initializeRobot();
        configureRobot();
        fieldPosition = StartPosition.LEFT;

        while (!isStarted()) {

            setupDelayedStart();
            setupAllianceAndPosition();
            updateTelemetry();
            idle();
        }
        if(!opModeIsActive()) return;
        autonomous_step_name = "RUNNING";
        delay(delayedStart * 1000);

        // #1.  Drive forward to get Leave points
        driveTrain.driveDistance(14.0, .6, 3);
        if(!opModeIsActive()) return;

        // #2.0  Look for the Hive to be tipped how we want.
        // Wait up to X seconds leaving enough time for parking
        delay(500);

        driveTrain.slideDistance(5.0*12, 0.8, 2);
        delay(500);
        driveTrain.slideDistance(0.5*12, 0.4, 1);
        if(!opModeIsActive()) return;
        driveTrain.slideDistance(-8, 0.6, 1);
        if(!opModeIsActive()) return;

        driveTrain.driveDistance(12, 0.6, 2);
        if(!opModeIsActive()) return;

    }
}
