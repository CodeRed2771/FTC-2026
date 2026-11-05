package org.firstinspires.ftc.teamcode.Biobuzz;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Biobuzz_Flower_5384 {
    private final DcMotor _deploy_motor;
    private final Telemetry _telemetry;

    private final DigitalChannel _deploy_limit_switch_1;
    private final DigitalChannel _deploy_limit_switch_2;
    private int deploy_position = 0;

    private final int init_position = 122;

    private final double deploy_power = 0.5;

    public enum DeployMode {
        DEPLOY(0),
        STOW(115);

        private final int val;
        private DeployMode(int new_value)
        {
            val = new_value;
        }
        public int value(){
            return val;
        }
    }

    public DeployMode getMode () {
        return mode;
    }

    private DeployMode mode = DeployMode.STOW;

    public Biobuzz_Flower_5384 (HardwareMap hardwareMap, Telemetry telemetry) {
        _telemetry = telemetry;
        _deploy_motor = hardwareMap.dcMotor.get("deploy");
        _deploy_limit_switch_1 = hardwareMap.tryGet(DigitalChannel.class, "deploy_limit1");
        _deploy_limit_switch_2 = hardwareMap.tryGet(DigitalChannel.class, "deploy_limit2");

        _deploy_limit_switch_1.setMode(DigitalChannel.Mode.INPUT);
        _deploy_limit_switch_2.setMode(DigitalChannel.Mode.INPUT);
        init();
    }

    public void init(){
        deploy_position = _deploy_motor.getCurrentPosition() - init_position;
    }

    public void deploy(){
        mode = DeployMode.DEPLOY;
        gotoPosition(mode.value());
    }

    public void stow(){
        mode = DeployMode.STOW;
        gotoPosition(mode.value());
    }

    private void gotoPosition(int target){
        _deploy_motor.setTargetPosition(mode.value() + deploy_position);
        _deploy_motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        _deploy_motor.setPower(deploy_power);
    }

    private int getPosition(){
        return _deploy_motor.getCurrentPosition() + deploy_position;
    }
    private void setPosition(){
        setPosition(_deploy_motor.getCurrentPosition());
    }

    private void setPosition(int position){
        int raw_pos = _deploy_motor.getCurrentPosition();

        position = Math.max(position,DeployMode.DEPLOY.value());
        position = Math.min(position,DeployMode.STOW.value());

        deploy_position = position - raw_pos;
    }

    public void loop(){

        if ( _deploy_limit_switch_1 != null ) {
            if (! _deploy_limit_switch_1.getState()) {
                setPosition(DeployMode.DEPLOY.value());
            }
        }
        gotoPosition(mode.value());

    }

    public void telemetry(int page){
        switch(page)
        {
            default:
                _telemetry.addData("Deploy",mode.toString());
                _telemetry.addData("Deploy Pos",_deploy_motor.getCurrentPosition());
                _telemetry.addData("Deploy Cmd",_deploy_motor.getTargetPosition());

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

}
