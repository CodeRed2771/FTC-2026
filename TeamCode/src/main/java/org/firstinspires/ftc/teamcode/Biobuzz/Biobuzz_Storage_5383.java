package org.firstinspires.ftc.teamcode.Biobuzz;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.DCMotor_Mechanism;
import org.firstinspires.ftc.teamcode.PageDisplay;


public class Biobuzz_Storage_5383 extends DCMotor_Mechanism {

    public enum Presets {
        DOWN(0.0),
        UP(28),
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

    private final double INITIAL_POSITION = 33.0;
    private Presets _preset_mode = Presets.CUSTOM;
    private final double DEPLOY_POWER = 0.5;

    public Biobuzz_Storage_5383 (HardwareMap hardwareMap) {
        super(new DCMotor_Config( "Storage",
                                  new String[] { "tipper" },
                                  new DcMotor.Direction[] { DcMotor.Direction.FORWARD },
                                  2200,
                                  DcMotor.ZeroPowerBehavior.BRAKE),
              hardwareMap);
        setGearRatio(5.0*5.0);
        setAutoTunePIDF(1.0,1.0);

        setCurrentPosition(INITIAL_POSITION);

    }

    public void set (Presets new_mode) {
        if(new_mode == Presets.CUSTOM) return;

        set(_preset_mode.value());
        _preset_mode = new_mode;
    }

    public void set(double cmd){
        _preset_mode = Presets.CUSTOM;
        gotoPosition(cmd,DEPLOY_POWER);
    }

    public double get(){
        return getPosition();
    }

    public void registerPages(PageDisplay display){
        display.addPage(getName(), "State Info", 0, this::printTelemetry);
        super.registerPages(display);
    }

    public void printTelemetry(Telemetry telemetry, int page){
        switch(page){
            default:
                telemetry.addData("State", _preset_mode.toString());
                telemetry.addData("Position", getPosition());
                break;
        }
    }
}
