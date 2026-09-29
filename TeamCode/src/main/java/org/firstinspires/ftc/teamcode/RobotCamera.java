package org.firstinspires.ftc.teamcode;

import android.annotation.SuppressLint;

import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;

import java.util.List;


public abstract class RobotCamera {
    private AprilTagProcessor aprilTag;
    private VisionPortal visionPortal;
    private final HardwareMap hardwareMap;
    protected final Telemetry telemetry;
    
    private List<AprilTagDetection> currentDetections;
    private long lastDetectionTime;

    public RobotCamera (HardwareMap hardwareMap, Telemetry telemetry, String deviceName){
        this.telemetry = telemetry;
        this.hardwareMap = hardwareMap;
        
        initAprilTagProcessor(deviceName);
    }
    
    public void doLoop() {
        doDetection();
    }

    public void startStream(){
        visionPortal.resumeStreaming();
    }

    public void stopStream(){
        visionPortal.stopStreaming();
    }

    public void telemetry(int page){
        switch (page) {
            case 0:
                telemetryAprilTag();
                break;
            default:
        }
    }

    private void doDetection() {
        currentDetections = aprilTag.getDetections();
        lastDetectionTime = System.currentTimeMillis();
    }

    private void initAprilTagProcessor(String deviceName) {
        WebcamName installedCamera = hardwareMap.tryGet(WebcamName.class, deviceName);

        // Create the AprilTag processor the easy way.
        aprilTag = AprilTagProcessor.easyCreateWithDefaults();

        //Create the vision portal the easy way.
        if (installedCamera != null) {
            visionPortal = VisionPortal.easyCreateWithDefaults(installedCamera, aprilTag);
        }
    }
    
    public int getFirstAprilTagID() {
        int result = 0;
        if(currentDetections != null){
            for (AprilTagDetection detection : currentDetections) {
                if (detection instanceof AprilTagSingleDetection) {
                    AprilTagSingleDetection singleDet = (AprilTagSingleDetection) detection;
                    result = singleDet.id;
                    break;
                }
            }
        }
        return result;
    }
    
    public AprilTagSingleDetection getFirstAprilTag(){
        if(currentDetections != null){
            for (AprilTagDetection detection : currentDetections) {
                if (detection instanceof AprilTagSingleDetection) {
                    return (AprilTagSingleDetection) detection;
                }
            }
        }
        return null;
    }

    public AprilTagSingleDetection findAprilTag(int tagID) {
        if (currentDetections != null) {
            for (AprilTagDetection detection : currentDetections) {
                if (detection instanceof AprilTagSingleDetection) {
                    AprilTagSingleDetection singleDet = (AprilTagSingleDetection) detection;
                    // Do single Tag processing here
                    if (singleDet.id == tagID)
                        return singleDet;
                }
            }
        }
        return null;
    }

    @SuppressLint("DefaultLocale")
    public void telemetryAprilTag() {
        if (currentDetections != null) {
            telemetry.addData("# AprilTags Detected", currentDetections.size());
        
            // Step through the list of detections and display info for each one.
            for (AprilTagDetection detection : currentDetections) {
                if (detection instanceof AprilTagSingleDetection) {
                    AprilTagSingleDetection singleDet = (AprilTagSingleDetection) detection;
                    if (singleDet.metadata != null) {
                        double offsetDegrees = singleDet.ftcPose.bearing;
                        telemetry.addLine(String.format("\n==== (ID %d) %s", singleDet.id, singleDet.metadata.name));
                        telemetry.addLine(String.format("XYZ %6.1f %6.1f %6.1f  (inch)", singleDet.ftcPose.x, singleDet.ftcPose.y, singleDet.ftcPose.z));
                        telemetry.addLine(String.format("PRY %6.1f %6.1f %6.1f  (deg)", singleDet.ftcPose.pitch, singleDet.ftcPose.roll, singleDet.ftcPose.yaw));
                        telemetry.addLine(String.format("RBE %6.1f %6.1f %6.1f  (inch, deg, deg)", singleDet.ftcPose.range, singleDet.ftcPose.bearing, singleDet.ftcPose.elevation));
                        telemetry.addData("Degrees off", offsetDegrees);
                    } else {
                        telemetry.addLine(String.format("\n==== (ID %d) Unknown", singleDet.id));
                        telemetry.addLine(String.format("Center %6.0f %6.0f   (pixels)", singleDet.center.x, singleDet.center.y));
                    }
                }
            }   // end for() loop
        }

        // Add "key" information to telemetry
        telemetry.addLine("\nkey:\nXYZ = X (Right), Y (Forward), Z (Up) dist.");
        telemetry.addLine("PRY = Pitch, Roll & Yaw (XYZ Rotation)");
        telemetry.addLine("RBE = Range, Bearing & Elevation");

    } 
}