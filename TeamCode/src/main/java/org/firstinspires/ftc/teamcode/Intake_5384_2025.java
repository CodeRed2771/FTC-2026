package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;


public class Intake_5384_2025 {
    private final DcMotor _intake_motor;
    private final Telemetry _telemetry;
    
    public enum IntakeMode {
        RUN(0.8),
        STOP(0.0),
        REVERSE(-1.0);

        private final double val;
        private IntakeMode(double new_value)
        {
            val = new_value;
        }
        public double value(){
            return val;
        }
    }
    private IntakeMode mode = IntakeMode.STOP;
    
    public Intake_5384_2025 (HardwareMap hardwareMap, Telemetry telemetry) {
        _telemetry = telemetry;
        _intake_motor = hardwareMap.dcMotor.get("intake");
    }
    
    public void startIntake() {
        mode = IntakeMode.RUN;
        _intake_motor.setPower(mode.value());
    }

    public void stopIntake() {
        mode = IntakeMode.STOP;
        _intake_motor.setPower(mode.value());
        
    }

    public void ejectIntake() {
        mode = IntakeMode.REVERSE;
        _intake_motor.setPower(mode.value());
        
    }
    
    public void loop(){
        _intake_motor.setPower(mode.value());
    }
    
    public void telemetry(int page){
        switch(page)
        {
            default:
                _telemetry.addData("Intake",mode.toString());
        }
    }

}
