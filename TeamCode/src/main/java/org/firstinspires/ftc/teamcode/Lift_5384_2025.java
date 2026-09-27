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


public class Lift_5384_2025 {

    private DcMotorEx lift1 = null;
    private boolean isLifting = false;
    private Telemetry telemetry;
    
    // --- Targets ---
    private double targetRpm = 150;    // starting preset (change to your wheel/gearbox)

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
    private double lastCommandedTicks;
    private final ElapsedTime loopTimer = new ElapsedTime();

    public Lift_5384_2025 (HardwareMap hardwareMap, Telemetry telemetry){
        this.telemetry = telemetry;
        
        lift1 = hardwareMap.get(DcMotorEx.class, "lift1");
        // lift1.setDirection(DcMotor.Direction.REVERSE);
        
        // lift1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        // lift1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // Let flywheels coast down naturally when toggled off
        //lift1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        // lift1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Preserve full RPM capability (some SDK defaults reduce it)
        // setAchievableMaxRpmFraction(lift1, 1.0);

        // (Optional) Set custom PIDF if you want to override stock motor model
        // lift1.setVelocityPIDFCoefficients(kP, kI, kD, kF);

        // Determine ticks/rev from motor config (works for most goBILDA/REV motors)
        MotorConfigurationType mtype = lift1.getMotorType();
        ticksPerRev = mtype.getTicksPerRev();

        loopTimer.reset();
    }
    
    public void telemetry(int page){
        switch(page)
        {
            default:
                telemetry.addData("lift speed", lastCommandedTicks);
                telemetry.addData("lift1 velocity", lift1.getVelocity());
                telemetry.addData("lift1 current position", lift1.getCurrentPosition());

                MotorConfigurationType mtype = lift1.getMotorType();
                telemetry.addData("lift1 ticks per rev", mtype.getTicksPerRev());
                telemetry.addData("lift1 orientation", mtype.getOrientation());
        }

    }

    public void setLiftSpeed(double rpm) {
        double liftTps  = rpmToTicksPerSec(rpm);
        lastCommandedTicks = liftTps;

        lift1.setVelocity(liftTps);
    }

    public void stop() {
        setLiftSpeed(0);
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
}
