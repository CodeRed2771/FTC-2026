package org.firstinspires.ftc.teamcode.Biobuzz;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Biobuzz_Shooter_5384 {

    private final Telemetry _telemetry;

    private DcMotorEx _shooter_motor1 = null;
    private DcMotorEx _shooter_motor2 = null;

    private final double MOTOR_POWER = 1.0;
    private final double TARGET_TOLERANCE = 10;
    private final double MAX_MOTOR_VEL = 1800;

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
    private final double ticksPerRev;
    private double lastCommandedTicks = 0;

    public enum Defaults {
        REVERSE_SLOW(-50.0),
        STOP(0.0),
        EJECT(50.0),
        SHOOT(180.0);

        private final double val;
        Defaults (double new_value)
        {
            val = new_value;
        }
        public double value(){
            return val;
        }
    }

    public Biobuzz_Shooter_5384 (HardwareMap hardwareMap, Telemetry telemetry){

        _telemetry = telemetry;

        _shooter_motor1 = hardwareMap.get(DcMotorEx.class, "shooter_right");
        _shooter_motor2 = hardwareMap.get(DcMotorEx.class, "shooter_left");

        _shooter_motor1.setDirection(DcMotor.Direction.REVERSE);

        // Preserve full RPM capability (some SDK defaults reduce it)
        setAchievableMaxRpmFraction(_shooter_motor1, 1.0);
        setAchievableMaxRpmFraction(_shooter_motor2, 1.0);

        MotorConfigurationType mtype = _shooter_motor1.getMotorType();
        ticksPerRev = mtype.getTicksPerRev();

        reset();
    }

    public void reset(){
        _shooter_motor1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        _shooter_motor2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        _shooter_motor1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        _shooter_motor2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        _shooter_motor1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        _shooter_motor2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        defaultPIDF();

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

    public void setPowerHive() {
        setShooterSpeed(Defaults.SHOOT);
    }

    public void eject () {
        setShooterSpeed(Defaults.EJECT);
    }

    public void stop() {
        setShooterSpeed(Defaults.STOP);
    }

    public void setShooterSpeed(Defaults preset) {
        setShooterSpeed(preset.value());
    }

    public void setShooterSpeed(double rpm) {
        double leftTps  = rpmToTicksPerSec(rpm);
        double rightTps = rpmToTicksPerSec(rpm);
        lastCommandedTicks = leftTps;
        _shooter_motor1.setVelocity(leftTps);
        _shooter_motor2.setVelocity(rightTps);
    }

    public boolean atTargetSpeed(){
        double cur_speed = ticksPerSecToRpm(_shooter_motor1.getVelocity());
        double target_speed = ticksPerSecToRpm(lastCommandedTicks);
        double error = target_speed - cur_speed;
        return Math.abs(error) < TARGET_TOLERANCE;
    }

    public void loop(){
        setShooterSpeed(lastCommandedTicks);
    }

    public void telemetry(int page){
        PIDFCoefficients coefficients;
        switch(page) {
            case 1:
                _telemetry.addLine("Flower Velocity Tuning");
                coefficients = _shooter_motor1.getPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER);
                _telemetry.addData("Vel P", coefficients.p);
                _telemetry.addData("Vel I", coefficients.i);
                _telemetry.addData("Vel D", coefficients.d);
                _telemetry.addData("Vel F", coefficients.f);
                _telemetry.addData("Power", _shooter_motor1.getPower());
                _telemetry.addData("Overall", _vel_gain);
                break;
            case 2:
                _telemetry.addLine("Flower Position Tuning");
                coefficients = _shooter_motor1.getPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION);
                _telemetry.addData("Pos P", coefficients.p);
                _telemetry.addData("Pos I", coefficients.i);
                _telemetry.addData("Pos D", coefficients.d);
                _telemetry.addData("Pos F", coefficients.f);
                _telemetry.addData("Overall", _pos_gain);
                break;
            default:
                _telemetry.addData("shoot speed", lastCommandedTicks);
                _telemetry.addData("at target", atTargetSpeed());
                _telemetry.addData("shooter1 velocity", _shooter_motor1.getVelocity());
                _telemetry.addData("shooter2 velocity", _shooter_motor2.getVelocity());
                break;
        }
    }

    private double _vel_gain = 1.0;
    private double _pos_gain = 1.0;
    public void setAutoTunePIDF(double vel_gain, double pos_gain){
        _vel_gain = vel_gain;
        _pos_gain = pos_gain;
        double K_ff = 32767 / MAX_MOTOR_VEL * _vel_gain;
        PIDFCoefficients coefficients = new PIDFCoefficients();
        coefficients.p = 0.1 * K_ff;
        coefficients.i = 0.1 * coefficients.p;
        coefficients.d = 0.0;
        coefficients.f = K_ff;
        _shooter_motor1.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, coefficients);
        _shooter_motor1.setPositionPIDFCoefficients(5.0 * _pos_gain);
        _shooter_motor2.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, coefficients);
        _shooter_motor2.setPositionPIDFCoefficients(5.0 * _pos_gain);
    }

    public void defaultPIDF(){
        // These constants were observed from sampling from the HD Hex motor 11/5/2026
        PIDFCoefficients coefficients = new PIDFCoefficients();
        coefficients.p = 10;
        coefficients.i = 3;
        coefficients.d = 0.0;
        coefficients.f = 0.0;
        _shooter_motor1.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, coefficients);
        _shooter_motor1.setPositionPIDFCoefficients(5);
        _shooter_motor2.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, coefficients);
        _shooter_motor2.setPositionPIDFCoefficients(5);
    }
}
