package org.firstinspires.ftc.teamcode.Decode.Emergency;

import static org.firstinspires.ftc.teamcode.Decode.Emergency.Storage_5384_2025.*;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Decode.DecodeCamera;
import org.firstinspires.ftc.teamcode.DriveTrain;

public class Robot_5384_2025 {

    public DriveTrain driveTrain;
    public Intake_5384_2025 intake;
    public Shooter_5384_2025 shooter;
    public Storage_5384_2025 storage;
    public DecodeCamera camera;
    Telemetry telemetry;

    public void initializeRobot(HardwareMap hardwareMap, Telemetry _telemetry) {

        telemetry = _telemetry;

        driveTrain = new DriveTrain(hardwareMap, telemetry);
        intake = new Intake_5384_2025(hardwareMap, telemetry);
        shooter = new Shooter_5384_2025(hardwareMap, telemetry);
        storage = new Storage_5384_2025(hardwareMap, telemetry);
        camera = new DecodeCamera(hardwareMap, telemetry,"Webcam 1");
    }

    public void autoLoop(){
        camera.doLoop();
        intake.loop();
        shooter.loop();
        storage.loop();
        storage.telemetry(3);
        telemetry.update();
    }
    public void autoShoot(){
        ;
        double giveUpTime = 0;

        giveUpTime = System.currentTimeMillis()+1000;
        while(!shooter.atShootingSpeed() && System.currentTimeMillis() < giveUpTime ){
            autoLoop();
        }

        giveUpTime = System.currentTimeMillis()+1000;
        storage.ejectorLaunch();
        while(storage.getEjector() != EjectorPosition.UP && System.currentTimeMillis() < giveUpTime){
            autoLoop();
        }

        giveUpTime = System.currentTimeMillis()+1000;
        storage.ejectorStow();
        while(storage.getEjector() != EjectorPosition.DOWN && System.currentTimeMillis() < giveUpTime){
            autoLoop();
        }
    }
}
