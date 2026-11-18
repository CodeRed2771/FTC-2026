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

import org.firstinspires.ftc.teamcode.Biobuzz.Biobuzz_Storage_5383;

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
@Autonomous(name = "2026:5383:Just Drive Right", group = "5383")

public class Auto_Just_Drive_Right_5383 extends AutoBaseClass_5383 {

    // Starts with robot facing the wall in the Launch Zone
    private int Max_RUNTIME = 30;
    @Override
    public void runOpMode() {
        double delayedStart = 0.0;
        int up_deadband = 0;
        int down_deadband = 0;

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

        // step 1: Move and shoot the preloaded pollen
        // 1.1 : move forward 0.5ft
        driveTrain.driveDistance(12.0, 0.6, 1);
        delay(250);
        storage.set(Biobuzz_Storage_5383.Presets.DOWN);

        // 1.2 : ready the shooter (spin it up)

        // 1.4 : adjust aiming (use april tag)

        // 1.5 : If not seeing april tag, keep pollen and skip to step 2

        // 1.6 : Fire all pole


        // step 2: Slide over to the runway
        driveTrain.slideDistance(-32,0.6,2);
        delay(500);
        driveTrain.turn(45,0.3,2);
        shooter.eject();
        delay(500);
        shooter.setPowerHive();
        storage.set(Biobuzz_Storage_5383.Presets.UP);
        intake.startIntake();
        delay(4000);
        //shooter.setPowerMid();

        driveTrain.turn(-45,0.3,2);
        shooter.stop();


        driveTrain.driveDistance(8*12,0.6,4);
        intake.stopIntake();

        // step 3: Ride the wall into the parking zone
        driveTrain.slideDistance(-18, 0.6, 2);
        driveTrain.slideDistance(-12, 0.3, 2);
        driveTrain.slideDistance(8, 0.8, 1);
        driveTrain.driveDistance(12, 0.8, 2);
    }
}