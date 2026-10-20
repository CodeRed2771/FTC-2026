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
@Autonomous(name = "Auto Launch Zone Right", group = "5384")

public class AutoLaunchZone5384_Right extends AutoBaseClass_5384_2025 {

    // Starts with robot facing the wall in the Launch Zone

    @Override
    public void runOpMode() {
        int obeliskValue = 0;
        double giveUpTime = 0;
        double delayedStart = 0.0;
        int up_deadband = 0;
        int down_deadband = 0;

        
        initializeRobot();
        
        telemetry.addData("Status", "Initialized");
        telemetry.update();
        // Wait for the game to start (driver presses PLAY)
       
        // driveTrain.setRunToPosition();
        storage.ejectorStow();
        

        opModeIsActive();
         delay(delayedStart * 1000);

        driveTrain.driveDistance(6,0.6,3);
        driveTrain.slideDistance(12,1,3);
        shooter.setPowerMid();
        delay(1500);
        camera.loop();
        if(camera.goalFound()){
            double aim = camera.getBearingToAprilTag();
        }




        // step one : move and shoot the preloaded pollen
            //1.1: move forward .5 feet
            //1.2: ready shooter
            //1.3 look for april tag
            //1.4 adjust aiming( use april tag)
            //1.5 if not seeing april tag, skip to step 2
            //1.6 if seen, fire all pollen

        // step two: slide over to runway

        // step 3 : ride the wall into the parking zone


    }


}
