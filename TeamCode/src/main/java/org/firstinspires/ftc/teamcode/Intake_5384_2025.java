package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;


public class Intake_5384_2025 {
    private DcMotor intake;
    private Telemetry telemetry;
    
    public enum IntakeMode {
        IN(0.8),
        STOP(0.0),
        EJECT(-1.0);

        private double val;
        private IntakeMode(double new_value)
        {
            val = new_value;
        }
        public double value(){
            return val;
        }
    }
    private IntakeMode mode = IntakeMode.STOP;
    
    public Intake_5384_2025 (HardwareMap hardwareMap, Telemetry _telemetry) {
        telemetry = _telemetry;
        intake = hardwareMap.dcMotor.get("intake");
    }
    
    public void startIntake() {
        mode = IntakeMode.IN;
        intake.setPower(mode.value());
    }

    public void stopIntake() {
        mode = IntakeMode.STOP;
        intake.setPower(mode.value());
        
    }

    public void ejectIntake() {
        mode = IntakeMode.EJECT;
        intake.setPower(mode.value());
        
    }
    
    public void loop(){
        intake.setPower(mode.value());
    }
    
    public void telemetry(int page){
        switch(page)
        {
            default:
                telemetry.addData("Intake",mode.toString());
        }
    }

}
