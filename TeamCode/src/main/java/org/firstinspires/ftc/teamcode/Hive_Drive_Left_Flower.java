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
@Autonomous(name = "Hive Drive Left Flower", group = "5384", preselectTeleOp = "2026:5384:Emergency Drive")
public class Hive_Drive_Left_Flower extends AutoBaseClass_5384 {

    // Starts with robot facing the wall in the Launch Zone
    private int MAX_RUNTIME = 30;
    private final int TIME_TO_PARK = 5;
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
        double startTime = System.currentTimeMillis();
        delay(delayedStart * 1000);

        // #1.  Drive forward to get Leave points
        driveTrain.driveDistance(12.0, .5, 2);

        // #2.0  Look for the Hive to be tipped how we want.
        // Wait up to X seconds leaving enough time for parking

        // #2 Shoot the pollen
        // 2.1 Start spinning the motors on the shooter
        shooter.setPowerHive();
        intake.fireAll();
        delay(3500);
        shooter.stop();
        intake.stop();

        // 2.2 get a pollen from storage ready to go
        flower.deploy();

        // 2.3 get pollen from flower
        driveTrain.driveDistance(8, 0.6, 2);
        driveTrain.slideDistance(15, 0.6, 3);
        driveTrain.turn(180,0.6,3);
        intake.startIntake();
        driveTrain.driveDistance(13,0.6, 3);
        delay(1000);
        intake.stop();
        driveTrain.driveDistance(-5,0.6, 2);
        driveTrain.turn(180,0.6,3);
        driveTrain.slideDistance(15, 0.6, 2);

        // #3 Park in the parking zone
        // #3.1 Move right 5 ft
        driveTrain.slideDistance(4.5*12, 0.8, 3);
        driveTrain.slideDistance(-6, 0.8, 1);

        driveTrain.driveDistance(4, 0.4, 2);

    }
}
