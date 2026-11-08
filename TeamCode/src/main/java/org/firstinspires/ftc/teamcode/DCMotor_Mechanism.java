package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class DCMotor_Mechanism {

    public static class DCMotor_Config {
        String name;
        String[] motor_names;
        DcMotor.Direction[] direction;
        double MAX_MOTOR_TICKS_PER_SEC;
        DcMotor.ZeroPowerBehavior zero_power_behavior;

        public DCMotor_Config (String name,
                               String[] motor_names,
                               DcMotor.Direction[] direction,
                               double MAX_MOTOR_TICKS_PER_SEC,
                               DcMotor.ZeroPowerBehavior zero_power_behavior) {
            this.motor_names = motor_names;
            this.direction = direction;
            this.name = name;
            this.MAX_MOTOR_TICKS_PER_SEC = MAX_MOTOR_TICKS_PER_SEC;
            this.zero_power_behavior = zero_power_behavior;
        }
    }

    protected final Telemetry _telemetry;
    private final DcMotorEx[] _motors;
    private final DCMotor_Config _config;
    private final double _ticksPerRev;
    private double _posPerRev = 1.0;
    private double[] _last_cmd;
    private double _power_level;
    private DcMotor.RunMode _run_mode;
    private double _speed_tolerance = 1.0;
    private double _position_tolerance = 1.0;
    /******** Motor Tuning Functions **********************/
    private double _vel_gain = 1.0;
    private double _pos_gain = 1.0;

    /***************  Creation, Initialization, and Configuration ****************/
    public DCMotor_Mechanism (DCMotor_Config config, HardwareMap map, Telemetry telemetry) {
        _telemetry = telemetry;
        _config = config;

        _motors = new DcMotorEx[_config.motor_names.length];
        for (int i = 0; i < _config.motor_names.length; i++) {
            _motors[i] = map.get(DcMotorEx.class, _config.motor_names[i]);
            _motors[i].setDirection(_config.direction[i]);
            _motors[i].setZeroPowerBehavior(_config.zero_power_behavior);
        }

        MotorConfigurationType mtype = _motors[0].getMotorType().clone();
        _ticksPerRev = mtype.getTicksPerRev();

        _last_cmd = new double[_config.motor_names.length];

        reset();
    }

    public void reset () {
        setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    }

    private void setMode (DcMotor.RunMode mode) {
        if (_run_mode != mode) {
            _run_mode = mode;
            for (DcMotorEx motor : _motors) {
                motor.setMode(_run_mode);
            }
        }
    }

    public void loop () {
        applyMixedCmd();
    }

    protected void clearCmd () {
        setUniformCmd(0.0);
    }

    protected void setUniformCmd (double val) {

        for (int i = 0; i < _motors.length; i++) {
            _last_cmd[i] = val;
        }
    }

    protected void addMixedCmd (double[] val) {
        if (val.length != _motors.length) return;

        for (int i = 0; i < _motors.length; i++) {
            _last_cmd[i] += val[i];
        }
    }

    protected void applyMixedCmd () {
        // testing shows that continuously applying the command to the motors helps them achieve their targets
        switch (_run_mode) {
            case RUN_TO_POSITION:
                applyPositionCmd(_last_cmd);
                break;
            case RUN_USING_ENCODER:
                applySpeedCmd(_last_cmd);
                break;
            case RUN_WITHOUT_ENCODER:
                applyPower(_last_cmd);
                break;
        }
    }

    /********** Motor Power/Voltage Control Functions ***************/
    public void stopPower () {
        setPower(0.0);
    }

    public void setPower (double power) {
        _power_level = power;
        setUniformCmd(power);
        setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    public double getPower () {
        return _power_level;
    }

    // Applies Power in any mode of operation
    // In Position/Speed control, this limits motion power.
    // In Power mode, this is a direct power command.
    private void applyPower (double[] power) {
        if (power.length != _motors.length) return;

        for (int i = 0; i < _motors.length; i++) {
            _motors[i].setPower(power[i]);
        }
    }

    /************** Motor Speed Control Functions ***************/
    public void stopSpeed () {
        setSpeed(0.0);
    }

    public void setSpeed (double speed) {
        setUniformCmd(speed);
        setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    public double getSpeed(){
        return ticksPerSecToRpm(_motors[0].getVelocity());
    }

    public boolean atTargetSpeed () {
        return atTargetSpeed(_speed_tolerance);
    }

    public boolean atTargetSpeed (double tolerance) {
        if (_run_mode != DcMotor.RunMode.RUN_USING_ENCODER) return false;

        double cur_speed = getSpeed();
        double target_speed = _last_cmd[0];
        double error = target_speed - cur_speed;
        return Math.abs(error) < tolerance;
    }

    private void applySpeedCmd (double[] cmd) {
        if (cmd.length != _motors.length) return;

        for (int i = 0; i < _motors.length; i++) {
            _motors[i].setVelocity(rpmToTicksPerSec(cmd[i]));
        }
    }

    private double rpmToTicksPerSec (double rpm) {
        return (rpm * _ticksPerRev) / 60.0;
    }

    private double ticksPerSecToRpm (double tps) {
        return (tps * 60.0) / _ticksPerRev;
    }

    public double get_speed_tolerance () {
        return _speed_tolerance;
    }

    public void set_speed_tolerance (double tolerance) {
        if (tolerance <= 0) return;
        _speed_tolerance = tolerance;
    }

    public void setSpeedPower (double power) {
        double[] power_cmd = new double[_motors.length];
        _power_level = power;
        for (int i = 0; i < _motors.length; i++) {
            power_cmd[i] = _power_level;
        }
        applyPower(power_cmd);
    }

    /********** Motor Position Control Functions ***************/
    public void gotoPosition (double position) {
        setUniformCmd(position);
        setMode(DcMotor.RunMode.RUN_TO_POSITION);
    }

    public double getPosition(){
        return ticksToPosition(_motors[0].getCurrentPosition());
    }

    private void applyPositionCmd (double[] cmd) {
        if (cmd.length != _motors.length) return;

        for (int i = 0; i < _motors.length; i++) {
            _motors[i].setTargetPosition(positionToTicks(cmd[i]));
        }
    }

    private double ticksToPosition (int ticks) {
        return ticks / _ticksPerRev * _posPerRev;
    }

    private int positionToTicks (double position) {
        return (int) (position / _posPerRev * _ticksPerRev);
    }

    public void setUnitPerRev (double unit_per_rev) {
        if (unit_per_rev <= 0) return;
        _posPerRev = unit_per_rev;
    }

    public boolean atTargetPosition () {
        return atTargetPosition(_position_tolerance);
    }

    public boolean atTargetPosition (double tolerance) {
        double cur_pos = getPosition();
        double target_pos = _last_cmd[0];
        double error = target_pos - cur_pos;
        return Math.abs(error) < tolerance;
    }

    public double get_position_tolerance () {
        return _position_tolerance;
    }

    public void set_position_tolerance (double tolerance) {
        if (tolerance <= 0) return;
        _position_tolerance = tolerance;
    }

    public void setPositionPower (double power) {
        double[] power_cmd = new double[_motors.length];
        for (int i = 0; i < _motors.length; i++) {
            power_cmd[i] = power;
        }
        applyPower(power_cmd);
    }

    /********** Telemetry Handling functions ***************/
    public void telemetry (int page) {
        PIDFCoefficients coefficients;
        switch (page) {
            case 1:
                printPageTitle(page, "Motor Config");
                _telemetry.addData("Ticks Per Rev", _ticksPerRev);
                _telemetry.addData("Unit per Rev", _posPerRev);
                _telemetry.addData("Max Ticks Per Sec", _config.MAX_MOTOR_TICKS_PER_SEC);
                for (int i = 0; i < _config.motor_names.length; i++) {
                    _telemetry.addData(_config.motor_names[i], _config.direction[i].toString());
                }
                break;
            case 2:
                printPageTitle(page,"Vel Tuning");
                coefficients = _motors[0].getPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER);
                _telemetry.addData("Vel P", coefficients.p);
                _telemetry.addData("Vel I", coefficients.i);
                _telemetry.addData("Vel D", coefficients.d);
                _telemetry.addData("Vel F", coefficients.f);
                _telemetry.addData("Power", _motors[0].getPower());
                _telemetry.addData("Overall", _vel_gain);
                break;
            case 3:
                printPageTitle(page, "Pos Tuning");
                coefficients = _motors[0].getPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION);
                _telemetry.addData("Pos P", coefficients.p);
                _telemetry.addData("Pos I", coefficients.i);
                _telemetry.addData("Pos D", coefficients.d);
                _telemetry.addData("Pos F", coefficients.f);
                _telemetry.addData("Overall", _pos_gain);
                break;
            default:
                printPageTitle(page,"Motor Cmds");
                _telemetry.addData("Mode", _run_mode.toString());
                _telemetry.addData("Power Level", _power_level);
                switch (_run_mode) {
                    case RUN_TO_POSITION:
                        _telemetry.addData("Tolerance", _position_tolerance);
                        _telemetry.addData("at Target", atTargetSpeed());
                        _telemetry.addData(_config.motor_names[0] + " pos", _motors[0].getCurrentPosition());
                        break;
                    case RUN_USING_ENCODER:
                        _telemetry.addData("Tolerance", _speed_tolerance);
                        _telemetry.addData("at target", atTargetPosition());
                        _telemetry.addData(_config.motor_names[0] + " vel", _motors[0].getVelocity());
                        break;
                    case RUN_WITHOUT_ENCODER:
                        _telemetry.addData("Power", _motors[0].getPower());
                        break;
                }
                break;
        }
    }

    public int numTelemetryPages () {
        return 4;
    }

    protected void printPageTitle (int page, String title) {
        String page_id = _config.name + " (" + page + "/" + numTelemetryPages() + ")";
        _telemetry.addLine(page_id + " : " + title);
    }

    public void setAutoTunePIDF (double vel_gain, double pos_gain) {
        _vel_gain = vel_gain;
        _pos_gain = pos_gain;

        double K_ff = 32767.0 / _config.MAX_MOTOR_TICKS_PER_SEC * _vel_gain;
        PIDFCoefficients coefficients = new PIDFCoefficients();
        coefficients.p = 0.1 * K_ff;
        coefficients.i = 0.1 * coefficients.p;
        coefficients.d = 0.0;
        coefficients.f = K_ff;

        setVelocityPIDF(coefficients);
        setPositionPIDF(5.0 * _pos_gain);
    }

    public void defaultPIDF () {
        // These constants were observed from sampling from the HD Hex motor 11/5/2026

        PIDFCoefficients coefficients = new PIDFCoefficients();
        coefficients.p = 10.0;
        coefficients.i = 3.0;
        coefficients.d = 0.0;
        coefficients.f = 0.0;

        setVelocityPIDF(coefficients);
        setPositionPIDF(5.0);
    }

    public void setVelocityPIDF (PIDFCoefficients coeffs) {
        for (DcMotorEx motor : _motors) {
            motor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, coeffs);
        }
    }

    public void setPositonPIDF (PIDFCoefficients coeffs) {
        for (DcMotorEx motor : _motors) {
            motor.setPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION, coeffs);
        }
    }

    public void setPositionPIDF (double gain) {
        for (DcMotorEx motor : _motors) {
            motor.setPositionPIDFCoefficients(gain);
        }
    }
}
