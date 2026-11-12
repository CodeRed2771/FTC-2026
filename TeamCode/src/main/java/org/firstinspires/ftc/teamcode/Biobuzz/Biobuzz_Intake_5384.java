package org.firstinspires.ftc.teamcode.Biobuzz;

import androidx.core.math.MathUtils;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.DCMotor_Mechanism;
import org.firstinspires.ftc.teamcode.PageDisplay;


public class Biobuzz_Intake_5384 extends DCMotor_Mechanism {

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
    CRServo _loader_motor;
    private final double CR_MOTOR_CMD_LIMIT = 0.92;
    private double max_loader_command = CR_MOTOR_CMD_LIMIT;

    public Biobuzz_Intake_5384 (HardwareMap hardwareMap) {
        super(new DCMotor_Config( "Intake",
                                  new String[] { "intake" },
                                  new DcMotor.Direction[] { DcMotor.Direction.FORWARD },
                                  2200,
                                  DcMotor.ZeroPowerBehavior.FLOAT),
              hardwareMap);
        setAutoTunePIDF(1.0,2.0);

        _loader_motor = hardwareMap.get(CRServo.class, "loader");
        _loader_motor.setDirection(DcMotorSimple.Direction.REVERSE);

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

    public double getMaxLoaderCommand () {
        return max_loader_command;
    }

    public void setMaxLoaderCommand (double max_loader_command) {
        this.max_loader_command = MathUtils.clamp(max_loader_command,0.0,CR_MOTOR_CMD_LIMIT);
    }

    public void set(double cmd){
        _intake_mode = Presets.CUSTOM;
        last_cmd = cmd;
        setPower(cmd);
    }

    @Override
    public void loop () {
        super.loop();

        if(Math.abs(last_cmd) < 0.005)
            _loader_motor.setPower(0.0);
        else
            _loader_motor.setPower(Math.signum(last_cmd) * max_loader_command);

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
                telemetry.addData("Loader Command", last_cmd);
                telemetry.addData("Loader Power", _loader_motor.getPower());
                break;
        }
    }
}
