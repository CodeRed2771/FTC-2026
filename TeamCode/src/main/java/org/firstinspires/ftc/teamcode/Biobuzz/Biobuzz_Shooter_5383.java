package org.firstinspires.ftc.teamcode.Biobuzz;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.DCMotor_Mechanism;
import org.firstinspires.ftc.teamcode.PageDisplay;

public class Biobuzz_Shooter_5383 extends DCMotor_Mechanism {

    public enum Presets {
        REVERSE_SLOW(-50.0),
        STOP(0.0),
        EJECT(-50.0),
        SHOOT(150.0),
        CUSTOM(0.0);

        private final double val;
        Presets (double new_value)
        {
            val = new_value;
        }
        public double value(){
            return val;
        }
    }
    private Presets _shooter_mode = Presets.CUSTOM;

    private final double MOTOR_POWER = 1.0;

    public Biobuzz_Shooter_5383 (HardwareMap hardwareMap){
        super(new DCMotor_Config( "Shooter",
                        new String[] { "shooter" },
                        new DcMotor.Direction[] { DcMotor.Direction.FORWARD },
                        1800.0,
                        DcMotor.ZeroPowerBehavior.FLOAT),
                hardwareMap);

        set_speed_tolerance(20.0);
    }

    public void reset(){
        super.reset();
        set(Presets.STOP);
    }

    public void setPowerHive() {
        set(Presets.SHOOT);
    }

    public void eject () {
        set(Presets.EJECT);
    }

    public void stop() {
        set(Presets.STOP);
    }

    public void set (Presets new_mode) {
        _shooter_mode = new_mode;
        set(_shooter_mode.value());
    }

    public void set(double cmd){
        _shooter_mode = Presets.CUSTOM;
        setSpeed(cmd);
    }

    public double get(){
        return getSpeed();
    }

    public void registerPages(PageDisplay display){
        display.addPage(getName(), "State Info", 0, this::printTelemetry);
        super.registerPages(display);
    }

    public void printTelemetry(Telemetry telemetry, int page){
        switch(page){
            default:
                telemetry.addData("State", _shooter_mode.toString());
                break;
        }
    }
}
