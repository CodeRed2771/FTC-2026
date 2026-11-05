package org.firstinspires.ftc.teamcode.Biobuzz;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Biobuzz_Shooter_5384 {

    private Telemetry _telemetry;

    private DcMotorEx shooter1 = null;
    private DcMotorEx shooter2 = null;

    private final double max_shoot_speed = 180;
    private final double eject_shoot_speed = 50;

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

    // --- Internal state ---
    private double ticksPerRev;
    private double lastCommandedTicks = 0;


    public Biobuzz_Shooter_5384 (HardwareMap hardwareMap, Telemetry telemetry){

        _telemetry = telemetry;

        shooter1 = hardwareMap.get(DcMotorEx.class, "shooter_right");
        shooter2 = hardwareMap.get(DcMotorEx.class, "shooter_left");

        shooter1.setDirection(DcMotor.Direction.REVERSE);

        shooter1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        shooter2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        shooter1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        shooter2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        shooter1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooter2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        // Preserve full RPM capability (some SDK defaults reduce it)
        setAchievableMaxRpmFraction(shooter1, 1.0);
        setAchievableMaxRpmFraction(shooter2, 1.0);

        MotorConfigurationType mtype = shooter1.getMotorType();
        ticksPerRev = mtype.getTicksPerRev();
    }

    private void setAchievableMaxRpmFraction(DcMotorEx m, double frac) {
        MotorConfigurationType cfg = m.getMotorType().clone();
        cfg.setAchieveableMaxRPMFraction(frac);
        m.setMotorType(cfg);
    }

    private double rpmToTicksPerSec(double rpm) {
        return (rpm * ticksPerRev) / 60.0;
    }

    public void setPowerHive(){
        setShooterSpeed(max_shoot_speed);
    }

    public void setPowerEject(){
        setShooterSpeed(eject_shoot_speed);
    }

    public void stop(){
        setShooterSpeed(0);
    }

    public void setShooterSpeed(double rpm) {
        double leftTps  = rpmToTicksPerSec(rpm);
        double rightTps = rpmToTicksPerSec(rpm);
        lastCommandedTicks = leftTps;
        if ( shooter1 != null)
            shooter1.setVelocity(leftTps);
        if ( shooter2 != null)
            shooter2.setVelocity(rightTps);
    }

    public void telemetry(int page){
        switch(page)
        {
            default:
                _telemetry.addData("shoot speed", lastCommandedTicks);
                if ( shooter1 != null)
                    _telemetry.addData("shooter1 velocity", shooter1.getVelocity());
                if ( shooter2 != null)
                    _telemetry.addData("shooter2 velocity", shooter2.getVelocity());
        }

    }

    public void loop(){
        ;
    }
}
