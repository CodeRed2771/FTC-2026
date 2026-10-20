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
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.teamcode.Artifact;

import org.firstinspires.ftc.teamcode.Storage_5384_2025.WiperPosition;
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
@Autonomous(name = "Auto Launch Zone Leave", group = "5384")

public class AutoLaunchZone5384_Leave extends AutoBaseClass_5384_2025 {

    // Starts with robot facing the wall in the Launch Zone

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
        

        opModeIsActive();


        delay(delayedStart * 1000);

        // sleep(5);
        driveTrain.driveDistance(12.0, 1, 3);
        // 2. look for the hive ti be tipped how we want
        // wait up to X seconds leaving enough time to park
        delay(2000);
// 2. shoot the pollen
        //2.1 start spinning the motors
        shooter.setPowerMid();
        //2.2 get pollen from storage ready to go
        //storage.select();
        //2.3 set the speed based on distance - may only need one or two speeds
        shooter.setPowerMid();
        // wait till motors are ready - wait fixed amount of time
        delay(1500);
        // - wait until motor stops accelerating +- 0 acellearation
        //wait till motor is at target speed
        // 2.5 transfer from storage to shooter
        storage.ejectorLaunch();

        // 3 park in the parking zone
        // 4 move right 4 feet
        driveTrain.slideDistance(-60,1.0,10.0);




    }

}