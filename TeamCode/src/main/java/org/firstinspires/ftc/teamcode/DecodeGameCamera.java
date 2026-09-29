package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;

public class DecodeGameCamera extends RobotCamera {
    private final int red_id = 24;
    private final int blue_id = 20;
    private int obelisk_id = 0;
    private int found_id = 0;

    private double autoaim_distance = 0.0;
    private double autoaim_bearing = 0.0;


    public DecodeGameCamera(HardwareMap hardwareMap, Telemetry telemetry, String deviceName) {
        super(hardwareMap, telemetry, deviceName);
    }

    @Override
    public void doLoop() {
        super.doLoop();

        captureTargets();
    }

    @Override
    public void telemetry(int page) {
        switch (page) {
            case 0:
                super.telemetry(page);
                break;
            case 1:
                telemetry.addData("obelisk", obelisk_id);
                telemetry.addData("ID", found_id);
                telemetry.addData("Bearing", autoaim_bearing);
                telemetry.addData("Distance", autoaim_distance);
                break;
            default:
        }

    }

    public double getAutoaim_bearing() {
        return autoaim_bearing;
    }

    public double getAutoaim_distance() {
        return autoaim_distance;
    }

    public boolean isObeliskIDValid() {
        return (obelisk_id != 0);
    }

    public int getObeliskID() {
        return obelisk_id;
    }

    public boolean redGoalFound() {
        return (found_id == red_id);
    }

    public boolean blueGoalFound() {
        return (found_id == blue_id);
    }

    public boolean goalFound() {
        return blueGoalFound() || redGoalFound();
    }

    protected void captureTargets() {
        found_id = 0;
        AprilTagSingleDetection singleDet = getFirstAprilTag();
        if (singleDet != null) {
            switch (singleDet.id) {
                case red_id:
                case blue_id:
                    found_id = singleDet.id;
                    autoaim_bearing = singleDet.ftcPose.bearing;
                    autoaim_distance = singleDet.ftcPose.range;
                    break;
                case 21:
                case 22:
                case 23:
                    obelisk_id = singleDet.id;
                    break;
            }
        }
        if (found_id == 0)
            autoaim_bearing = 0.0;
    }
}
