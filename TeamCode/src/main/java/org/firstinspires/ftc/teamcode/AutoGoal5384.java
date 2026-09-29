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

import org.firstinspires.ftc.teamcode.Storage_5384_2025.WiperPosition;
/**
 * This file contains a minimal example of a Linear "OpMode". An OpMode is a 'program' that runs
 * in either the autonomous or the TeleOp period of an FTC match. The names of OpModes appear on
 * the menu of the FTC Driver Station. When an selection is made from the menu, the corresponding
 * OpMode class is instantiated on the Robot Controller and executed.
 * <p>
 * Remove the @Disabled annotation on the next line or two (if present) to add this OpMode to the
 * Driver Station OpMode list, or add a @Disabled annotation to prevent this OpMode from being
 * added to the Driver Station.
 */
@Autonomous(name = "Auto Goal", group = "5384")

public class AutoGoal5384 extends AutoBaseClass_5384_2025 {

    // Starts with robot shooter facing the wall of the goal

    @Override
    public void runOpMode() {
        int obeliskValue = 0;
        double giveUpTime = 0;
        boolean getSecondArtifacts = true;
        double delayedStart = 0.0;
        int up_deadband = 0;
        int down_deadband = 0;
        Artifact[] fireOrder = {Artifact.UNKNOWN, Artifact.UNKNOWN, Artifact.UNKNOWN};
        
        initializeRobot();
        
        telemetry.addData("Status", "Initialized");
        telemetry.update();
        // Wait for the game to start (driver presses PLAY)
        
        storage.ejectorStow();
        while (!isStarted()) {
              
            if(gamepad1.y || gamepad2.y)
                getSecondArtifacts = true;
            if(gamepad1.a || gamepad2.a)
                getSecondArtifacts = false;
                
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
            delayedStart = Math.min(delayedStart,15);
            telemetry.addData("DELAY", delayedStart);
            telemetry.addData("SHOOT EXTRA", getSecondArtifacts);

            setupAllianceAndPosition();

            idle();
        }

        storage.ejectorStow();
        storage.setWiperPosition(WiperPosition.LEFT, WiperPosition.RIGHT);
        storage.preload(Artifact.PURPLE, Artifact.PURPLE, Artifact.GREEN);

        giveUpTime = System.currentTimeMillis()+delayedStart*1000;
        while (opModeIsActive() && System.currentTimeMillis() < giveUpTime) {
            idle();
        }

        // sleep(5);
        driveTrain.driveDistance(40,0.8,6); 
           
        // turn to see obelisk
        if (isBlueAlliance()) {
            driveTrain.turn(60,0.4,3);
        } else
            driveTrain.turn(-60,0.4,3);
        giveUpTime = System.currentTimeMillis()+3000;   
        while (camera.getObeliskID() == 0 && opModeIsActive() && System.currentTimeMillis() < giveUpTime) {
            autoLoop();
            camera.telemetry(0);
            telemetry.update();
            idle();
            
        }
        sleep(500);
        shooter.setPowerMid();
        
        if (isBlueAlliance()) {
            driveTrain.turn(-55,0.6,2);
        } else
            driveTrain.turn(55,0.6,2);
        autoLoop();
            
        obeliskValue = camera.getObeliskID();
        if (obeliskValue==21) { // GPP
            fireOrder[0] = Artifact.GREEN;
            fireOrder[1] = Artifact.PURPLE;
            fireOrder[2] = Artifact.PURPLE;
        } else if (obeliskValue==22) { // PGP
            fireOrder[0] = Artifact.PURPLE;
            fireOrder[1] = Artifact.GREEN;
            fireOrder[2] = Artifact.PURPLE;
        } else if (obeliskValue==23) { // PPG
            fireOrder[0] = Artifact.PURPLE;
            fireOrder[1] = Artifact.PURPLE;
            fireOrder[2] = Artifact.GREEN;
        } 
        
        giveUpTime = System.currentTimeMillis()+500;   
        while(!camera.goalFound() && opModeIsActive() && System.currentTimeMillis() < giveUpTime){
            autoLoop();
            idle();
        }
        shooter.setShooterDistance(camera.getAutoaim_distance()-5.0);

        autoShoot();
        storage.select(fireOrder[1]);
        
        giveUpTime = System.currentTimeMillis()+750;   
        while(storage.isWiping() && opModeIsActive() && System.currentTimeMillis() < giveUpTime){
            autoLoop();
            idle();
        }
        // storage.setWiperPosition(WiperPosition.RIGHT, WiperPosition.RIGHT);
        autoShoot();
        storage.select(fireOrder[2]);

        giveUpTime = System.currentTimeMillis()+750;   
        while(storage.isWiping() && opModeIsActive() && System.currentTimeMillis() < giveUpTime){
            autoLoop();
            idle();
        }

        autoShoot();
        shooter.stop();
        if(getSecondArtifacts){
            // turn and head to back wall to get out of the way
            if (isBlueAlliance()) {
                driveTrain.turn(45,0.6,3);
            } else
                driveTrain.turn(-51,0.6,3);
            sleep(250);
              
            driveTrain.driveDistance(18.0, 0.8,2);
            sleep(250);
            if (isBlueAlliance()) {
                driveTrain.turn(80,0.6,3);
            } else
                driveTrain.turn(-80,0.6,3);
            sleep(500);
            intake.startIntake();
    
            driveTrain.driveDistance(12,0.8,2.0);
            
            driveTrain.startDriving(7,0.15,1.0);
            giveUpTime = System.currentTimeMillis()+1500;   
            driveTrain.autoDriveStep();
            while(!driveTrain.completedMovement() && opModeIsActive() && System.currentTimeMillis() < giveUpTime){
                autoLoop();
                driveTrain.autoDriveStep();
                idle();
                if(storage.isCenterLoaded())
                    storage.captureArtifact();
            }
    
            giveUpTime = System.currentTimeMillis()+1000;   
            while(storage.getStoredArtifactCount() < 1
                   && opModeIsActive() && System.currentTimeMillis() < giveUpTime){
                autoLoop();
                idle();
                if(storage.isCenterLoaded())
                    storage.captureArtifact();
            }
    
    
            driveTrain.startDriving(6,0.15,3);
            giveUpTime = System.currentTimeMillis()+1000;   
            driveTrain.autoDriveStep();
            while(!driveTrain.completedMovement() && opModeIsActive() && System.currentTimeMillis() < giveUpTime){
                autoLoop();
                driveTrain.autoDriveStep();
                idle();
                if(storage.isCenterLoaded())
                    storage.captureArtifact();
            }
            giveUpTime = System.currentTimeMillis()+500;   
            while(storage.getStoredArtifactCount() < 2
                   && opModeIsActive() && System.currentTimeMillis() < giveUpTime){
                autoLoop();
                idle();
                if(storage.isCenterLoaded())
                    storage.captureArtifact();
            }
    
    
            driveTrain.startDriving(18,0.15,3);
            giveUpTime = System.currentTimeMillis()+3000;   
            driveTrain.autoDriveStep();
            while(!driveTrain.completedMovement() && opModeIsActive() && System.currentTimeMillis() < giveUpTime){
                autoLoop();
                driveTrain.autoDriveStep();
                idle();
                if(storage.isCenterLoaded())
                    storage.captureArtifact();
            }
            giveUpTime = System.currentTimeMillis()+500;   
            while(!(storage.isRightLoaded() && storage.isLeftLoaded() && storage.isCenterLoaded())
                   && opModeIsActive() && System.currentTimeMillis() < giveUpTime){
                autoLoop();
                idle();
                if(storage.isCenterLoaded())
                    storage.captureArtifact();
            }
    
            autoLoop();
            driveTrain.driveDistance(-49.5,1.0,3);
            autoLoop();
            intake.ejectIntake();
            autoLoop();
            shooter.setPowerMid();
            if (isBlueAlliance()) {
                driveTrain.turn(-45-90.0,0.8,3);
            } else
                driveTrain.turn(45+90.0,0.8,3);
            autoLoop();
            driveTrain.driveDistance(-4,1.0,1.0);
            autoLoop();
    
            giveUpTime = System.currentTimeMillis()+250;   
            while(!camera.goalFound() && opModeIsActive() && System.currentTimeMillis() < giveUpTime){
                autoLoop();
                idle();
            }
            if(false && camera.goalFound())
                shooter.setShooterDistance(camera.getAutoaim_distance());
                
            if(camera.goalFound()&& Math.abs(camera.getAutoaim_bearing())>5.0){
                driveTrain.turn(camera.getAutoaim_bearing(),0.8,0.5);
            } 
            // if(!storage.isCenterLoaded())
            //     storage.select(fireOrder[0]);
            autoShoot();
    
            storage.select(fireOrder[1]);
            giveUpTime = System.currentTimeMillis()+750;
            while(storage.isWiping() && opModeIsActive() && System.currentTimeMillis() < giveUpTime){
                autoLoop();
                idle();
            }
            
            autoShoot();
            giveUpTime = System.currentTimeMillis()+500;
            while(storage.isWiping() && opModeIsActive() && System.currentTimeMillis() < giveUpTime){
                autoLoop();
                idle();
            }
    
            shooter.stop();
            if (isBlueAlliance())
                driveTrain.slideDistance(18.0,0.8,3);
            else
                driveTrain.slideDistance(-18.0,0.8,3);
        } else {

            if (isBlueAlliance())
                driveTrain.slideDistance(-18.0,0.8,3);
            else
                driveTrain.slideDistance(18.0,0.8,3);
        }
    }
}
