package org.firstinspires.ftc.teamcode.Biobuzz;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.DCMotor_Mechanism;
import org.firstinspires.ftc.teamcode.PageDisplay;


public class Biobuzz_Intake_5383 extends DCMotor_Mechanism {

    public enum Presets {
        FORWARD(0.7),
        STOP(0.0),
        REVERSE(-0.5),
        CUSTOM(0.0);

        private final double val;
        private Presets (double new_value)
        {
            val = new_value;
        }
        public double value(){
            return val;
        }
    }

    private Presets _intake_mode = Presets.CUSTOM;
    private double last_cmd;

    public Biobuzz_Intake_5383 (HardwareMap hardwareMap) {
        super(new DCMotor_Config( "Intake",
                                  new String[] { "intake" },
                                  new DcMotor.Direction[] { DcMotor.Direction.FORWARD },
                                  2200,
                                  DcMotor.ZeroPowerBehavior.FLOAT),
              hardwareMap);
        setAutoTunePIDF(1.0,2.0);

    }

    public void reset(){
        super.reset();
        set(Presets.STOP);
    }

    public void startIntake() {
        set(Presets.FORWARD);
    }

    public void stopIntake() {
        set(Presets.STOP);
    }

    public void ejectIntake() {
        set(Presets.REVERSE);
    }

    public void set (Presets new_mode) {
        _intake_mode = new_mode;
        set(_intake_mode.value());
    }

    public void set(double cmd){
        _intake_mode = Presets.CUSTOM;
        last_cmd = cmd;
        setPower(cmd);
    }

    public double get(){
        return getPower();
    }

    public void registerPages(PageDisplay display){
        display.addPage(getName(), "State Info", 0, this::printTelemetry);
        super.registerPages(display);
    }

    public void printTelemetry(Telemetry telemetry, int page){
        switch(page){
            default:
                telemetry.addData("State", _intake_mode.toString());
                telemetry.addData("Intake Command", last_cmd);
                break;
        }
    }
}
