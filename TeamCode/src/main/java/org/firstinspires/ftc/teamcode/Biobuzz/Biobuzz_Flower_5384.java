package org.firstinspires.ftc.teamcode.Biobuzz;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Biobuzz_Flower_5384 {
    private final DcMotorEx _deploy_motor;
    private final Telemetry _telemetry;

    private final DigitalChannel _deploy_limit_switch_1;
    private final DigitalChannel _deploy_limit_switch_2;
    private int zero_position_offset = 0;

    private final double DEPLOY_POWER = 1.0;
    private final double DEPLOY_TOLERANCE = 5;
    private final double MAX_DEPLOY_MOTOR_VEL = 1800;

    private Defaults _current_mode = Defaults.STOW;

    public enum Defaults {
        DEPLOY_LIMIT(-5),
        DEPLOY(0),
        STOW(115),
        STOW_LIMIT(122);

        private final int val;
        Defaults (int new_value)
        {
            val = new_value;
        }
        public int value(){
            return val;
        }
    }

    public Biobuzz_Flower_5384 (HardwareMap hardwareMap, Telemetry telemetry) {
        _telemetry = telemetry;
        _deploy_motor = hardwareMap.get(DcMotorEx.class, "deploy");
        _deploy_limit_switch_1 = hardwareMap.tryGet(DigitalChannel.class, "deploy_limit1");
        _deploy_limit_switch_2 = hardwareMap.tryGet(DigitalChannel.class, "deploy_limit2");

        if (_deploy_limit_switch_1 != null)
            _deploy_limit_switch_1.setMode(DigitalChannel.Mode.INPUT);
        if (_deploy_limit_switch_2 != null)
            _deploy_limit_switch_2.setMode(DigitalChannel.Mode.INPUT);

        reset();
    }


    public void reset (){
        defaultPIDF();

        _deploy_motor.setTargetPosition(_deploy_motor.getCurrentPosition());
        _deploy_motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        if ( atDeployLimit() ) {
            _current_mode = Defaults.DEPLOY;
            setPosition(Defaults.DEPLOY.value());
        } else {
            _current_mode = Defaults.STOW;
            setPosition(Defaults.STOW_LIMIT.value());
        }
    }

    public void deploy(){
        set(Defaults.DEPLOY);
    }

    public void stow(){
        set(Defaults.STOW);
    }

    public Defaults getMode() {
        return _current_mode;
    }

    public void set(Defaults new_mode){
        _current_mode = new_mode;
        set(_current_mode.value());
    }

    public void set(int target){
        int limited_target = target;
        // Ensure target won't crash motors
        limited_target = Math.min(limited_target, Defaults.STOW_LIMIT.value());
        limited_target = Math.max(limited_target, Defaults.DEPLOY_LIMIT.value());

        // Drive the motor to the target position
        _deploy_motor.setTargetPosition(posToTicks(limited_target));
        _deploy_motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        _deploy_motor.setPower(DEPLOY_POWER);
    }

    public double get (){
        return ticksToPosition(_deploy_motor.getCurrentPosition());
    }

    private int ticksToPosition(int ticks) {
        return ticks - zero_position_offset;
    }

    private int posToTicks(int position) {
        return position + zero_position_offset;
    }

    private void setPosition(int position){
        int raw_pos = _deploy_motor.getCurrentPosition();

        position = Math.max(position, Defaults.DEPLOY_LIMIT.value());
        position = Math.min(position, Defaults.STOW_LIMIT.value());

        zero_position_offset = -position + raw_pos;
    }

    public boolean atTargetPosition(){
        double cur_pos = ticksToPosition(_deploy_motor.getCurrentPosition());
        double target_pos = ticksToPosition(_deploy_motor.getTargetPosition());
        double error = target_pos - cur_pos;
        return Math.abs(error) < DEPLOY_TOLERANCE;
    }

    public boolean isStowed() {
        return _current_mode == Defaults.STOW && atTargetPosition();
    }

    public boolean isDeployed() {
        return _current_mode == Defaults.DEPLOY && atTargetPosition();
    }

    public boolean atDeployLimit (){
        boolean not_at_limit = true;
        // switches are "true" while not in contact.
        // while in contact, switches are "false".
        if ( _deploy_limit_switch_1 != null ) {
            not_at_limit = not_at_limit && _deploy_limit_switch_1.getState();
        }
        if ( _deploy_limit_switch_2 != null ) {
            not_at_limit = not_at_limit && _deploy_limit_switch_2.getState();
        }
        return ! not_at_limit;
    }

    public void loop(){

        if ( atDeployLimit() ) {
            setPosition(Defaults.DEPLOY.value());
        }

        if (! atTargetPosition()) {
            set(_current_mode.value());
        }
    }

    public void telemetry(int page){
        PIDFCoefficients coefficients;
        switch(page)
        {
            case 1:
                _telemetry.addLine("Flower Velocity Tuning");
                coefficients = _deploy_motor.getPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER);
                _telemetry.addData("Vel P", coefficients.p);
                _telemetry.addData("Vel I", coefficients.i);
                _telemetry.addData("Vel D", coefficients.d);
                _telemetry.addData("Vel F", coefficients.f);
                _telemetry.addData("Power",_deploy_motor.getPower());
                _telemetry.addData("Overall", _vel_gain);
                break;
            case 2:
                _telemetry.addLine("Flower Position Tuning");
                coefficients = _deploy_motor.getPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION);
                _telemetry.addData("Pos P", coefficients.p);
                _telemetry.addData("Pos I", coefficients.i);
                _telemetry.addData("Pos D", coefficients.d);
                _telemetry.addData("Pos F", coefficients.f);
                _telemetry.addData("Overall", _pos_gain);
                break;
            default:
                _telemetry.addLine("Flower State");
                _telemetry.addData("Deploy", _current_mode.toString());
                _telemetry.addData("Deploy Pos", get());
                _telemetry.addData("On Target", atTargetPosition());
                _telemetry.addData("Raw Pos",_deploy_motor.getCurrentPosition());
                _telemetry.addData("Raw Cmd",_deploy_motor.getTargetPosition());

                if(_deploy_limit_switch_1 != null)
                    _telemetry.addData("Deploy Limit", _deploy_limit_switch_1.getState());
                else
                    _telemetry.addData("Deploy Limit","NOT INSTALLED");

                if(_deploy_limit_switch_2 != null)
                    _telemetry.addData("Deploy Limit", _deploy_limit_switch_2.getState());
                else
                    _telemetry.addData("Deploy Limit","NOT INSTALLED");
        }
    }

    private double _vel_gain = 1.0;
    private double _pos_gain = 1.0;
    public void setAutoTunePIDF(double vel_gain, double pos_gain){
        _vel_gain = vel_gain;
        _pos_gain = pos_gain;
        double K_ff = 32767 / MAX_DEPLOY_MOTOR_VEL * _vel_gain;
        PIDFCoefficients coefficients = new PIDFCoefficients();
        coefficients.p = 0.1 * K_ff;
        coefficients.i = 0.1 * coefficients.p;
        coefficients.d = 0.0;
        coefficients.f = K_ff;
        _deploy_motor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, coefficients);
        _deploy_motor.setPositionPIDFCoefficients(5.0 * _pos_gain);
    }

    public void defaultPIDF(){
        // These constants were observed from sampling from the HD Hex motor 11/5/2026
        PIDFCoefficients coefficients = new PIDFCoefficients();
        coefficients.p = 10;
        coefficients.i = 3;
        coefficients.d = 0.0;
        coefficients.f = 0.0;
        _deploy_motor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, coefficients);
        _deploy_motor.setPositionPIDFCoefficients(5);
    }

}
