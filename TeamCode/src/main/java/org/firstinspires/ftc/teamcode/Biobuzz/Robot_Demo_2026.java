package org.firstinspires.ftc.teamcode.Biobuzz;

import static org.firstinspires.ftc.teamcode.Decode.Emergency.Storage_5384_2025.EjectorPosition;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Decode.DecodeCamera;
import org.firstinspires.ftc.teamcode.Decode.Emergency.Intake_5384_2025;
import org.firstinspires.ftc.teamcode.Decode.Emergency.Shooter_5384_2025;
import org.firstinspires.ftc.teamcode.Decode.Emergency.Storage_5384_2025;
import org.firstinspires.ftc.teamcode.DriveTrain;

public class Robot_Demo_2026 {

    public DriveTrain driveTrain;
    public DecodeCamera camera;
    Telemetry telemetry;

    public void initializeRobot(HardwareMap hardwareMap, Telemetry _telemetry) {

        telemetry = _telemetry;

        driveTrain = new DriveTrain(hardwareMap, telemetry);
        camera = new DecodeCamera(hardwareMap, telemetry,"Webcam 1");
    }

    public void autoLoop(){
        camera.doLoop();
        telemetry.update();
    }

}
