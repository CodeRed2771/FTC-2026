package org.firstinspires.ftc.teamcode;

import java.util.List;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;


@Autonomous(name = "_Calibrate", group = "5383")
public class Calibrate extends AutoBaseClass_5384_2025 {

    // todo: write your code here
    @Override
    public void runOpMode() {
        initializeRobot();

        
        telemetry.addData("Status", "Initialized");
        telemetry.update();
     
        waitForStart();

        // driveTrain.driveDistance(24,0.3,5);
        // sleep(5000);
        // driveTrain.slideDistance(24,0.3,5);
        // sleep(5000);
        // driveTrain.turn(360,0.6,10);
    
    }

}