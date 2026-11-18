package org.firstinspires.ftc.teamcode.Biobuzz;
import androidx.core.math.MathUtils;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.DCMotor_Mechanism;
import org.firstinspires.ftc.teamcode.PageDisplay;


public class Biobuzz_Storage_5383 extends DCMotor_Mechanism {

    public enum Presets {
        DOWN(0.0),
        UP(33),
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

    private final double INITIAL_POSITION = 35.0;
    private Presets _preset_mode = Presets.CUSTOM;
    private final double DEPLOY_POWER = 0.8;
    NormalizedColorSensor colorSensor;
    private double color_gain = 6.2;
    private final float RAMP_COLOR_THRESHOLD = 0.297f;

    public Biobuzz_Storage_5383 (HardwareMap hardwareMap) {
        super(new DCMotor_Config( "Storage",
                                  new String[] { "tipper" },
                                  new DcMotor.Direction[] { DcMotor.Direction.FORWARD },
                                  2200,
                                  DcMotor.ZeroPowerBehavior.BRAKE),
              hardwareMap);
        setGearRatio(5.0*5.0);
        setAutoTunePIDF(1.0,1.0);

        colorSensor = hardwareMap.get(NormalizedColorSensor.class, "ramp_color");
        setColorGain(color_gain);

        if(isRampUp())
            setCurrentPosition(INITIAL_POSITION);
        else
            setCurrentPosition(Presets.DOWN.value());


    }

    @Override
    public void loop(){
        super.loop();

        if((_preset_mode == Presets.UP) && isRampUp()){
            setCurrentPosition(Presets.UP.value());
        }
//        if((_preset_mode == Presets.UP) && isBound()){
//            setCurrentPosition(Presets.UP.value());
//        }

        if((_preset_mode == Presets.DOWN) && isBound())
            setCurrentPosition(Presets.DOWN.value());

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

    public double getColorGain () {
        return color_gain;
    }

    public void setColorGain (double gain) {
        color_gain = MathUtils.clamp(gain,1.0,10.0);
        colorSensor.setGain((float) color_gain);
    }

    public void incrementColorGain (double delta) {
        setColorGain(getColorGain() + delta);
    }

    public boolean isRampUp(){
        return colorSensor.getNormalizedColors().red >= RAMP_COLOR_THRESHOLD;
    }

    public void registerPages(PageDisplay display){
        display.addPage(getName(), "Main", 0, this::printTelemetry);
        super.registerPages(display);
    }

    public void printTelemetry(Telemetry telemetry, int page){
        switch(page){
            default:
                telemetry.addData("State", _preset_mode.toString());
                telemetry.addData("Position", getPosition());
                telemetry.addData("Color Gain", color_gain);
                telemetry.addData("Color", colorSensor.getNormalizedColors().red);
                break;
        }
    }
}
