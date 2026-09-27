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

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotorController;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This file contains a minimal example of an iterative (Non-Linear) "OpMode". An OpMode is a
 * 'program' that runs in either the autonomous or the TeleOp period of an FTC match. The names
 * of OpModes appear on the menu of the FTC Driver Station. When an selection is made from the
 * menu, the corresponding OpMode class is instantiated on the Robot Controller and executed.
 *
 * Remove the @Disabled annotation on the next line or two (if present) to add this OpMode to the
 * Driver Station OpMode list, or add a @Disabled annotation to prevent this OpMode from being
 * added to the Driver Station.
 */


public class Shooter_5384_2025 {

    private DcMotorEx shooter1 = null;
    private DcMotorEx shooter2 = null;
    private DcMotor shoot = null;
    private boolean isShooting = false;
    private double stopShootingTime = 0;
    private double selectedSpeed = 0;
    private double shootStartTime = 0;
    private Telemetry telemetry;
    
    // --- Targets ---
    private double targetRpm = 150;    // starting preset (change to your wheel/gearbox)
    private boolean shooterOn = false;
    private final double shooter_tolerance = 30.0;
    
    private final double RPMs[] = {64,68,92};
    private final double Ranges[] = {40,55,115};
    
    private final double near_scale = (RPMs[1] - RPMs[0])/(Ranges[1]-Ranges[0]);
    private final double far_scale  = (RPMs[2] - RPMs[1])/(Ranges[2]-Ranges[1]);
    
    // --- Tuning knobs (start here) ---
    // 1) Set your PIDF for RUN_USING_ENCODER velocity loop.
    //    You can tune live in Driver Station if desired.
    private static final double kP = 200.0;
    private static final double kI = 0.10;
    private static final double kD = 0.00;
    // kF is feedforward in ticks/s per applied power unit; SDK expects "countsPerSec per power".
    // Rule of thumb: kF ≈ (maxTicksPerSec at 12V) / 12, then invert for SDK units if needed.
    // Using SDK's VelocityPIDFCoefficients signature is simpler via setVelocityPIDFCoefficients.
    private static final double kF = 10.0;  // start at 0; use built-in motor model first

    // 2) Limit how fast we change speed to avoid brownouts & big current spikes.
    private static final double MAX_RPM_SLEW_PER_SEC = 4000; // rpm per second

    // 3) Left/right sync helper: if one side is lagging, add a small split correction.
    private static final double SYNC_KP = 0.02;  // rpm error → rpm correction (small!)

    // --- Internal state ---
    private double ticksPerRev;
//    private double lastCommandedRpm = 0;
    private double lastCommandedTicks;
    private final ElapsedTime loopTimer = new ElapsedTime();

    public Shooter_5384_2025 (HardwareMap hardwareMap, Telemetry telemetry){
        this.telemetry = telemetry;
        
        shooter1 = hardwareMap.get(DcMotorEx.class, "shooter_right");
        shooter2 = hardwareMap.get(DcMotorEx.class, "shooter_left");
        shooter2.setDirection(DcMotor.Direction.REVERSE);
        
        shooter1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        shooter2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        shooter1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        shooter2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // Let flywheels coast down naturally when toggled off
        shooter1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooter2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        // Preserve full RPM capability (some SDK defaults reduce it)
        setAchievableMaxRpmFraction(shooter1, 1.0);
        setAchievableMaxRpmFraction(shooter2, 1.0);

        // (Optional) Set custom PIDF if you want to override stock motor model
        shooter1.setVelocityPIDFCoefficients(kP, kI, kD, kF);
        shooter2.setVelocityPIDFCoefficients(kP, kI, kD, kF);

        // Determine ticks/rev from motor config (works for most goBILDA/REV motors)
        MotorConfigurationType mtype = shooter1.getMotorType();
        ticksPerRev = mtype.getTicksPerRev();

        loopTimer.reset();

    }
    
    public void telemetry(int page){
        switch(page)
        {
            default:
                telemetry.addData("shoot speed", lastCommandedTicks);
                telemetry.addData("shooter1 velocity", shooter1.getVelocity());
                telemetry.addData("shooter2 velocity", shooter2.getVelocity());
        }

    }
    
    public void shoot() {
        if (!isShooting && selectedSpeed > 0) {
            // shoot.setPosition(44);
            // shoot.setPower(1);
            isShooting = true;
            stopShootingTime = System.currentTimeMillis()+1000;
        }
    }
    
    public void setStartShootingTime(double shootStartTime) {
        this.shootStartTime = shootStartTime;
    }
    
    public void loop() {
        if (!isShooting && shootStartTime > 0 && shootStartTime < System.currentTimeMillis())
            ;
            // shoot();
                
        if (isShooting) {
            if (System.currentTimeMillis() > stopShootingTime) {
                isShooting = false;
                // shoot.setTargetPosition(0);
                // shoot.setPower(1);
                shootStartTime = 0;
            }
        }
        
        shooter1.setVelocity(lastCommandedTicks);
        shooter2.setVelocity(lastCommandedTicks);
        
    }
    
    public boolean readyToShoot() {
        double shooter1Difference = (lastCommandedTicks - shooter1.getVelocity()) / lastCommandedTicks;
        double shooter2Difference = (lastCommandedTicks - shooter2.getVelocity()) / lastCommandedTicks;
        
        double tolerance = 0.10;
        if (Math.abs(shooter1Difference) < tolerance) {
            return false;
        }
        if (Math.abs(shooter2Difference) < tolerance) {
            return false;
        }
        return true;
    }
    
    public boolean isShooting() {
        return isShooting;
    }
    
    public void setPowerNear() {
        selectedSpeed = RPMs[0];
        setShooterSpeed(selectedSpeed);
    }
    
    public void setPowerMid() {
        selectedSpeed = RPMs[1];
        setShooterSpeed(selectedSpeed);
    }

    public void setPowerFar() {
        selectedSpeed = RPMs[2];
        setShooterSpeed(selectedSpeed);
    }
    
    public boolean atShootingSpeed(){
        return (Math.abs(shooter1.getVelocity() - lastCommandedTicks) < shooter_tolerance && 
                Math.abs(shooter2.getVelocity() - lastCommandedTicks) < shooter_tolerance);
    }
    
    public void setShooterSpeed(double rpm) {
        double leftTps  = rpmToTicksPerSec(rpm);
        double rightTps = rpmToTicksPerSec(rpm);
        lastCommandedTicks = leftTps;

        shooter1.setVelocity(leftTps);
        shooter2.setVelocity(rightTps);
        
    }
    
    public void setShooterDistance(double range){
        double command = RPMs[1];
        if(range < Ranges[1]){
            command = RPMs[0] + (range - Ranges[0])*near_scale;
        }
        else
        {
            command = RPMs[1] + (range - Ranges[1])*far_scale;
        }   
        setShooterSpeed(command);
    }
    
    public void stop() {
        setShooterSpeed(0);
        // selectedSpeed = 0;
        // shooter1.setPower(0);
        // shooter2.setPower(0);
    }

    private void setAchievableMaxRpmFraction(DcMotorEx m, double frac) {
        MotorConfigurationType cfg = m.getMotorType().clone();
        cfg.setAchieveableMaxRPMFraction(frac);
        m.setMotorType(cfg);
    }

    private double rpmToTicksPerSec(double rpm) {
        return (rpm * ticksPerRev) / 60.0;
    }

    private double ticksPerSecToRpm(double tps) {
        return (tps * 60.0) / ticksPerRev;
    }

    private double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
