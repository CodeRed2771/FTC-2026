/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.robotcore.external.Telemetry;
/*
 * This OpMode scans a single servo back and forward until Stop is pressed.
 * The code is structured as a LinearOpMode
 * INCREMENT sets how much to increase/decrease the servo position each cycle
 * CYCLE_MS sets the update period.
 *
 * This code assumes a Servo configured with the name "left_hand" as is found on a Robot.
 *
 * NOTE: When any servo position is set, ALL attached servos are activated, so ensure that any other
 * connected servos are able to move freely before running this test.
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list
 */

public class Storage_5384_2025 {
 
    Servo   servo_right;
    Servo   servo_left;
    private Telemetry telemetry;
    private DistanceSensor sensorDistanceRight;
    private DistanceSensor sensorDistanceLeft;
    long lastLeftMoveTime = 0;
    long lastRightMoveTime = 0;
    long lastEjectionTime = 0;

    Servo ejector1;
    Servo ejector2;
    
    private final double dist_max_limit = 20.0;
    private final double dist_min_limit = 14.5;
    private final double wipe_time = 300;
    private final double eject_move_time = 750;
    
    private final double[] ejector_mins = {0, -1};
    private final double[] ejector_maxs = {1, 0};

    public enum EjectorPosition {
        UP(1),
        GOING_UP(1),
        DOWN(0),
        GOING_DOWN(0);
       
        double val;
        private EjectorPosition(double input){
            val = input;
        }
        public double value(){
            return val;
        }
    }
    EjectorPosition curEjectorPosition;
    
    public enum WiperPosition {
        LEFT(0.01),
        MOVING_LEFT(0.01),
        RIGHT(0.99),
        MOVING_RIGHT(0.99);
        
        double val;
        private WiperPosition(double input){
            val = input;
        }
        public double value(){
            return val;
        }
    }
    private WiperPosition currentLeftWiper;
    private WiperPosition currentRightWiper;
    
    int ArtifactCount = 0;
    public enum StorageLocation {
        LEFT(1),
        CENTER(0),
        RIGHT(2);

        int val;
        private StorageLocation(int input){
            val = input;
        }
        public int value(){
            return val;
        }
        
    }
    
    private Artifact[] storageSlots = {
        Artifact.NONE, Artifact.NONE, Artifact.NONE
    };
    
    double rightDistanceCm = 0.0;
    double leftDistanceCm = 0.0;

    public Storage_5384_2025(HardwareMap hardwareMap, Telemetry _telemetry) {
        telemetry = _telemetry;
        servo_right = hardwareMap.servo.get("wiper_right");
        servo_left = hardwareMap.servo.get("wiper_left");
        sensorDistanceRight = hardwareMap.get(DistanceSensor.class, "distance_right");
        sensorDistanceLeft = hardwareMap.get(DistanceSensor.class, "distance_left");

        ejector1 = hardwareMap.servo.get("ejector");
        ejector2 = hardwareMap.servo.get("ejector2");

        curEjectorPosition = EjectorPosition.DOWN;
        currentRightWiper = WiperPosition.LEFT;
        currentLeftWiper = WiperPosition.LEFT;

    }

    public void preload(Artifact left, Artifact center, Artifact right){
        storageSlots[StorageLocation.LEFT.value()] = left;
        storageSlots[StorageLocation.CENTER.value()] = center;
        storageSlots[StorageLocation.RIGHT.value()] = right;
    }

    public int getStoredArtifactCount() {
        int counted = 0;
        for(int i = 0; i < storageSlots.length; i++) {
            if (storageSlots[i] != Artifact.NONE) {
                counted++;
            }
        }
        return counted;
    }

    public EjectorPosition getEjector(){
        return curEjectorPosition;
    }
    public WiperPosition getRightWiper(){
        return currentRightWiper;
    }
    public WiperPosition getLeftWiper(){
        return currentLeftWiper;
    }
    
    public boolean isWiping(){
        return (getRightWiper() == WiperPosition.MOVING_LEFT ||
                getRightWiper() == WiperPosition.MOVING_RIGHT ||
                getLeftWiper() == WiperPosition.MOVING_LEFT ||
                getLeftWiper() == WiperPosition.MOVING_RIGHT
                );
    }

    private double convertEjectorPosition(int ejector, EjectorPosition desiredPosition) {
        final double[] ejector_mins = {0, -1};
        final double[] ejector_maxs = {1, 0};
        if (ejector_mins.length < ejector || ejector_maxs.length < ejector) {
            return Double.NaN;
        }
        double min = ejector_mins[ejector - 1];
        double max = ejector_maxs[ejector - 1];
        double range = max - min;
        
        // If min = 0 & max = 1, range = 1; 0 + 0 * 1 = 0, and 0 + 1 * 1 = 1
        // If min = -1 & max = 0, range = 1; -1 + 0 * 1 = -1 and -1 + 1 * 1 = 0
        
        return Math.abs(min + desiredPosition.value() * range);
    }
    
    public void ejectorStow(){
        if(curEjectorPosition != EjectorPosition.DOWN && 
           curEjectorPosition != EjectorPosition.GOING_DOWN)
        {
            lastEjectionTime = System.currentTimeMillis();
            curEjectorPosition = EjectorPosition.GOING_DOWN;
        }
        //ejector1.setPosition(curEjectorPosition.value());
        ejector1.setPosition(convertEjectorPosition(1, curEjectorPosition));
        if (ejector2 != null) {
            //ejector2.setPosition(curEjectorPosition.value());
            ejector2.setPosition(convertEjectorPosition(2, curEjectorPosition));
        }
    }

    public void ejectorLaunch(){
        if(curEjectorPosition != EjectorPosition.UP && 
           curEjectorPosition != EjectorPosition.GOING_UP)
        {
            storageSlots[StorageLocation.CENTER.value()] = Artifact.NONE;
            lastEjectionTime = System.currentTimeMillis();
            curEjectorPosition = EjectorPosition.GOING_UP;
        }
        //ejector1.setPosition(curEjectorPosition.value());
        ejector1.setPosition(convertEjectorPosition(1, curEjectorPosition));
        if (ejector2 != null) {
            //ejector2.setPosition(curEjectorPosition.value());
            ejector2.setPosition(convertEjectorPosition(2, curEjectorPosition));
        }
    }
 
    public StorageLocation findArtifact(Artifact goal) {
        StorageLocation result = StorageLocation.CENTER;
        boolean found = false;

        if(isCenterLoaded() && getLeftWiper() == WiperPosition.LEFT && getRightWiper() == WiperPosition.RIGHT) {
                found = true;
                result = StorageLocation.CENTER;
        }
        else if(goal != Artifact.UNKNOWN){
            if(getLoadedArtifact(StorageLocation.CENTER) == goal){
                found = true;
                result = StorageLocation.CENTER;
            } else if (getLoadedArtifact(StorageLocation.RIGHT) == goal) {
                found = true;
                result = StorageLocation.RIGHT;
            } else if (getLoadedArtifact(StorageLocation.LEFT) == goal) {
                found = true;
                result = StorageLocation.LEFT;
            }
        }
        if(!found || goal == Artifact.UNKNOWN){
            found = true;
            if(isCenterLoaded()){
                result = StorageLocation.CENTER;
            } else if (isRightLoaded()) {
                result = StorageLocation.RIGHT;
            } else if (isLeftLoaded()) {
                result = StorageLocation.LEFT;
            }
        }

        return result;
    }

    public Artifact getLoadedArtifact(StorageLocation spot) {
        return storageSlots[spot.value()];
    }

    public boolean isCenterLoaded(){
        return getLoadedArtifact(StorageLocation.CENTER) != Artifact.NONE;
    }
    
    public boolean isRightLoaded(){
        return getLoadedArtifact(StorageLocation.RIGHT) != Artifact.NONE;
    }
    
    public boolean isLeftLoaded(){
        return getLoadedArtifact(StorageLocation.LEFT) != Artifact.NONE;
    }
    
    public void setLeftWiperPosition(WiperPosition target) {
        if(curEjectorPosition == EjectorPosition.DOWN){
            if(target == WiperPosition.LEFT
               || target == WiperPosition.MOVING_LEFT)
            {
                if(currentLeftWiper == WiperPosition.RIGHT
                   || currentLeftWiper == WiperPosition.MOVING_RIGHT){

                    currentLeftWiper = WiperPosition.MOVING_LEFT;
                    lastLeftMoveTime = System.currentTimeMillis();
                    storageSlots[StorageLocation.LEFT.value()] = getLoadedArtifact(StorageLocation.CENTER);
                    storageSlots[StorageLocation.CENTER.value()] = Artifact.NONE;
                }
            }
            else if (target == WiperPosition.RIGHT 
                     || target == WiperPosition.MOVING_RIGHT)
            {
                setRightWiperPosition(WiperPosition.RIGHT);
                if(currentLeftWiper == WiperPosition.LEFT
                   || currentLeftWiper == WiperPosition.MOVING_LEFT){

                    currentLeftWiper = WiperPosition.MOVING_RIGHT;
                    lastLeftMoveTime = System.currentTimeMillis();
                    storageSlots[StorageLocation.CENTER.value()] = getLoadedArtifact(StorageLocation.LEFT);
                    storageSlots[StorageLocation.LEFT.value()] = Artifact.NONE;
                }
            }
            servo_left.setPosition(currentLeftWiper.value());
        }
    }
    
    public void setRightWiperPosition(WiperPosition target) {
        if(curEjectorPosition == EjectorPosition.DOWN){
            if(target == WiperPosition.RIGHT
               || target == WiperPosition.MOVING_RIGHT)
            {
                if(currentRightWiper == WiperPosition.LEFT
                   || currentRightWiper == WiperPosition.MOVING_LEFT){

                    currentRightWiper = WiperPosition.MOVING_RIGHT;
                    lastRightMoveTime = System.currentTimeMillis();
                    storageSlots[StorageLocation.RIGHT.value()] = getLoadedArtifact(StorageLocation.CENTER);
                    storageSlots[StorageLocation.CENTER.value()] = Artifact.NONE;
                }
            }
            else if (target == WiperPosition.LEFT 
                     || target == WiperPosition.MOVING_LEFT)
            {
                setLeftWiperPosition(WiperPosition.LEFT);
                if(currentRightWiper == WiperPosition.RIGHT
                   || currentRightWiper == WiperPosition.MOVING_RIGHT){

                    currentRightWiper = WiperPosition.MOVING_LEFT;
                    lastRightMoveTime = System.currentTimeMillis();
                    storageSlots[StorageLocation.CENTER.value()] = getLoadedArtifact(StorageLocation.RIGHT);
                    storageSlots[StorageLocation.RIGHT.value()] = Artifact.NONE;
                }
            }
            servo_right.setPosition(currentRightWiper.value());
        }
    }
    
    public void setWiperPosition(WiperPosition left, WiperPosition right) {
        setRightWiperPosition(right);
        setLeftWiperPosition(left);
    }

    public void select(Artifact goal) {
        StorageLocation targetLocation = findArtifact(goal);

        if(targetLocation == StorageLocation.LEFT)
            setLeftWiperPosition(WiperPosition.RIGHT);
        else if(targetLocation == StorageLocation.RIGHT)
            setRightWiperPosition(WiperPosition.LEFT);
    }
    
    public boolean isLeftWiperMoving(){
        return (System.currentTimeMillis() - lastLeftMoveTime) < wipe_time;
    }

    public boolean isRightWiperMoving(){
        return (System.currentTimeMillis() - lastRightMoveTime) < wipe_time;
    }

    public boolean isEjectorMoving(){
        return (System.currentTimeMillis() - lastEjectionTime) < eject_move_time;
    }

    public void captureArtifact() {
        if(isCenterLoaded()){
            if ( currentRightWiper == WiperPosition.LEFT) {
                setRightWiperPosition(WiperPosition.RIGHT);
   
                if(!isLeftLoaded()) {
                   setLeftWiperPosition(WiperPosition.RIGHT);
                }
            } else if( currentLeftWiper == WiperPosition.RIGHT ) {
                setLeftWiperPosition(WiperPosition.LEFT);
        
                if(!isRightLoaded()) {
                    setRightWiperPosition(WiperPosition.LEFT);
                }
            }
        }
    }

    public void telemetry(int page){
        switch(page)
        {
            case 0:
                telemetry.addData("Ejector",curEjectorPosition.toString());
                telemetry.addData("Left Wiper", currentLeftWiper.toString());
                telemetry.addData("Right Wiper", currentRightWiper.toString());
                break;
            case 1:
                telemetry.addData("right_range", String.format("%.01f cm", rightDistanceCm));
                telemetry.addData("left_range", String.format("%.01f cm", leftDistanceCm));
                telemetry.addData(StorageLocation.CENTER.toString(), getLoadedArtifact(StorageLocation.CENTER).toString());
                break;
            default:
                telemetry.addData(StorageLocation.LEFT.toString(), getLoadedArtifact(StorageLocation.LEFT).toString());
                telemetry.addData(StorageLocation.CENTER.toString(), getLoadedArtifact(StorageLocation.CENTER).toString());
                telemetry.addData(StorageLocation.RIGHT.toString(), getLoadedArtifact(StorageLocation.RIGHT).toString());
        }     
    }

    private void updateRightWiperState(){
        if(currentRightWiper == WiperPosition.MOVING_RIGHT && !isRightWiperMoving())
            currentRightWiper = WiperPosition.RIGHT;
        else if(currentRightWiper == WiperPosition.MOVING_LEFT && !isRightWiperMoving())
            currentRightWiper = WiperPosition.LEFT;
    }
    
    private void updateLeftWiperState(){
        if(currentLeftWiper == WiperPosition.MOVING_RIGHT && !isLeftWiperMoving())
            currentLeftWiper = WiperPosition.RIGHT;
        else if(currentLeftWiper == WiperPosition.MOVING_LEFT && !isLeftWiperMoving())
            currentLeftWiper = WiperPosition.LEFT;
    }
    
    private void updateEjectorState(){
        if(curEjectorPosition == EjectorPosition.GOING_UP && !isEjectorMoving())
            curEjectorPosition = EjectorPosition.UP;
        else if(curEjectorPosition == EjectorPosition.GOING_DOWN && !isEjectorMoving())
            curEjectorPosition = EjectorPosition.DOWN;
    }

    private int timeInCenter = 0;

    private void updateCenterArtifact(){
        rightDistanceCm = sensorDistanceRight.getDistance(DistanceUnit.CM);
        leftDistanceCm = sensorDistanceLeft.getDistance(DistanceUnit.CM);
        
        
        
        if (!isLeftWiperMoving() && !isRightWiperMoving() && curEjectorPosition == EjectorPosition.DOWN) {
           if(rightDistanceCm > dist_max_limit || leftDistanceCm > dist_max_limit) {
               storageSlots[StorageLocation.CENTER.value()] = Artifact.NONE;
               timeInCenter = 0;
           }
           else if(rightDistanceCm < dist_min_limit || leftDistanceCm < dist_min_limit) {
               storageSlots[StorageLocation.CENTER.value()] = Artifact.NONE;
               timeInCenter = 0;
           }
           else {
               timeInCenter += 1;
               if( timeInCenter > 5)
                   storageSlots[StorageLocation.CENTER.value()] = Artifact.UNKNOWN;
           }

        }
    }

    public void loop() {

        updateRightWiperState();
        updateLeftWiperState();
        updateEjectorState();

        // Command outputs
        //ejector1.setPosition(curEjectorPosition.value());
        ejector1.setPosition(convertEjectorPosition(1, curEjectorPosition));
        if (ejector2 != null) {
            //ejector2.setPosition(curEjectorPosition.value());
            ejector2.setPosition(convertEjectorPosition(2, curEjectorPosition));
        }
        servo_right.setPosition(currentRightWiper.value());
        servo_left.setPosition(currentLeftWiper.value());

        updateCenterArtifact();
    }
}
 
