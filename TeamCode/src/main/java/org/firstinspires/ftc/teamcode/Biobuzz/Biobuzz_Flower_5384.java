package org.firstinspires.ftc.teamcode.Biobuzz;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.DCMotor_Mechanism;
import org.firstinspires.ftc.teamcode.PageDisplay;

public class Biobuzz_Flower_5384 extends DCMotor_Mechanism {

    private final DigitalChannel _deploy_limit_switch_1;
    private final DigitalChannel _deploy_limit_switch_2;

    private final double DEPLOY_POWER = 0.4;
    private final double DEPLOY_TOLERANCE = 5;

    private Presets _current_mode = Presets.STOW;

    public enum Presets {
        DEPLOY_LIMIT(-5),
        DEPLOY(0),
        STOW(105),
        STOW_LIMIT(122);

        private final double val;
        Presets (double new_value)
        {
            val = new_value;
        }
        public double value(){
            return val;
        }
    }

    public Biobuzz_Flower_5384 (HardwareMap hardwareMap) {
        super(
                new DCMotor_Config( "Flower",
                                    new String[] { "deploy" },
                                    new DcMotor.Direction[] { DcMotor.Direction.FORWARD },
                                    640,
                                    DcMotor.ZeroPowerBehavior.BRAKE),
                hardwareMap
        );
        setAutoTunePIDF(1.0,2.0);

        _deploy_limit_switch_1 = hardwareMap.tryGet(DigitalChannel.class, "deploy_limit1");
        _deploy_limit_switch_2 = hardwareMap.tryGet(DigitalChannel.class, "deploy_limit2");

        if (_deploy_limit_switch_1 != null)
            _deploy_limit_switch_1.setMode(DigitalChannel.Mode.INPUT);
        if (_deploy_limit_switch_2 != null)
            _deploy_limit_switch_2.setMode(DigitalChannel.Mode.INPUT);

        setUnitPerRev(getTicksPerRev());
        set_position_tolerance(DEPLOY_TOLERANCE);
    }

    public void reset (){
        super.reset();

        if ( atDeployLimit() ) {
            _current_mode = Presets.DEPLOY;
            setCurrentPosition(Presets.DEPLOY.value());
        } else {
            _current_mode = Presets.STOW;
            setCurrentPosition(Presets.STOW_LIMIT.value());
        }

    }

    public void hold() { gotoPosition(getPosition()); }

    public void deploy(){
        set(Presets.DEPLOY);
    }

    public void stow(){
        set(Presets.STOW);
    }

    public Presets getMode() {
        return _current_mode;
    }

    public void set(Presets new_mode){
        _current_mode = new_mode;
        gotoPosition(_current_mode.value());
    }

    public void gotoPosition(double target){
        double limited_target = target;
        // Ensure target won't crash motors
        limited_target = Math.min(limited_target, Presets.STOW_LIMIT.value());
        limited_target = Math.max(limited_target, Presets.DEPLOY_LIMIT.value());

        // Drive the motor to the target position
        super.gotoPosition(limited_target,DEPLOY_POWER);
    }

    public void setCurrentPosition (double position){
        position = Math.max(position, Presets.DEPLOY_LIMIT.value());
        position = Math.min(position, Presets.STOW_LIMIT.value());
        super.setCurrentPosition(position);
    }

    public boolean isStowed() {
        return _current_mode == Presets.STOW && atTargetPosition();
    }

    public boolean isDeployed() {
        return _current_mode == Presets.DEPLOY && atTargetPosition();
    }

    public boolean atDeployLimit (){
        boolean at_limit = false;
        // switches are "true" while not in contact.
        // while in contact, switches are "false".
        if ( _deploy_limit_switch_1 != null &&
             _deploy_limit_switch_1.getState() == false) {
            at_limit = true;
        }

        if ( _deploy_limit_switch_2 != null &&
             _deploy_limit_switch_2.getState() == false) {
            at_limit = true;
        }

        return at_limit;
    }

    public void loop(){
        super.loop();

        if ( atDeployLimit() ) {
            setCurrentPosition(Presets.DEPLOY.value());
        }
    }

    public void registerPages(PageDisplay display){
        display.addPage(getName(), "State Info", 0, this::printTelemetry);
        super.registerPages(display);
    }

    public void printTelemetry(Telemetry telemetry, int page){
        switch(page){
            default:
                telemetry.addData("State", _current_mode.toString());
                telemetry.addData("At Limit", atDeployLimit());
                if(_deploy_limit_switch_1 != null)
                    telemetry.addData("Switch 1", _deploy_limit_switch_1.getState());
                else
                    telemetry.addData("Switch 1","NOT INSTALLED");

                if(_deploy_limit_switch_2 != null)
                    telemetry.addData("Switch 2", _deploy_limit_switch_2.getState());
                else
                    telemetry.addData("Switch 2","NOT INSTALLED");
                break;
        }
    }
}
