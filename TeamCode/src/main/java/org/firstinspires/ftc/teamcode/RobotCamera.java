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


public class RobotCamera {
    private static final boolean USE_WEBCAM = true; 
    private AprilTagProcessor aprilTag;
    private VisionPortal visionPortal;
    private final HardwareMap hardwareMap;
    private final Telemetry telemetry;
    
    private List<AprilTagDetection> currentDetections;
    private List<AprilTagDetection> attemptedDetections;
    private long lastDetectionTime;

    private int obelisk_id = 0;
    
    private final int red_id = 24;
    private final int blue_id = 20;
    private int found_id = 0;
    private double autoaim_distance = 0.0;
    private double autoaim_bearing = 0.0;

    public RobotCamera (HardwareMap hardwareMap, Telemetry telemetry){
        this.telemetry = telemetry;
        this.hardwareMap = hardwareMap;
        
        initAprilTag();
    }
    
    public void loop() {
        doDetection();


                //Save CPU resources; can resume streaming when needed.
        // if (gamepad1.dpad_down) {
        //     visionPortal.stopStreaming();
        // } 
        // else if (gamepad1.dpad_up) {
        //     visionPortal.resumeStreaming();
        // }
        // visionPortal.close();
    }
    
    public void telemetry(int page)
    {
        switch(page)
        {
            case 0:
                telemetryAprilTag();
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

    public boolean isObeliskIDValid()
    {
        return (obelisk_id != 0);
    }

    public int getObeliskID(){
        return obelisk_id;
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
    
    public boolean redGoalFound(){
        return (found_id == red_id);
    }

    public boolean blueGoalFound(){
        return (found_id == blue_id);
    }

    public boolean goalFound(){
        return blueGoalFound() || redGoalFound();
    }
    
    public double getBearingToAprilTag() {
        return autoaim_bearing;
    }
    
    public double getDistanceToAprilTag() {
        return autoaim_distance;
    }

    private void doDetection() {
        currentDetections = aprilTag.getDetections();
        lastDetectionTime = System.currentTimeMillis();
        CaptureTargets();
    }

    private void CaptureTargets(){
        found_id = 0;
        if(currentDetections != null){
            for (AprilTagDetection detection : currentDetections) {
                if (detection instanceof AprilTagSingleDetection) {
                    AprilTagSingleDetection singleDet = (AprilTagSingleDetection) detection;
                    // Do single Tag processing here
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
            }
        }
        if(found_id == 0)
            autoaim_bearing = 0.0;
    }
    
    public void initAprilTag() {

        // Create the AprilTag processor the easy way.
        aprilTag = AprilTagProcessor.easyCreateWithDefaults();

        //Create the vision portal the easy way.
        if (USE_WEBCAM) {
            visionPortal = VisionPortal.easyCreateWithDefaults(
            hardwareMap.get(WebcamName.class, "Webcam 1"), aprilTag);
        }
    }
    
    public int getAprilTag() {
        return getFirstAprilTag();
    }
    
    public int getFirstAprilTag() {
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
    
    public AprilTagSingleDetection getDetection(){
        AprilTagSingleDetection result = null;
        if(currentDetections != null){
            for (AprilTagDetection detection : currentDetections) {
                if (detection instanceof AprilTagSingleDetection) {
                    result = (AprilTagSingleDetection) detection;
                    break;
                }
            }
        }
        return result;
    }
    
    @SuppressLint("DefaultLocale")
    public void telemetryAprilTag() {

//        List<AprilTagDetection> currentDetections = aprilTag.getDetections();
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