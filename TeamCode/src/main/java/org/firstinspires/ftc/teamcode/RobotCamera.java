package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.hardware.camera.BuiltinCameraDirection;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.vision.VisionPortal;
import java.util.List;


public class RobotCamera {
    private static final boolean USE_WEBCAM = true; 
    private AprilTagProcessor aprilTag;
    private VisionPortal visionPortal;
    private HardwareMap hardwareMap;
    private Telemetry telemetry;
    
    private List<AprilTagDetection> currentDetections;
    private List<AprilTagDetection> attemptedDetections;
    private long lastDetectionTime;
    private final long refresh_period = 1;
    
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

    public AprilTagDetection findAprilTag(int tagID) {
        for (AprilTagDetection detection : currentDetections) {
            if (detection.id == tagID) {
                return detection;
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
        CaptureTagets();
    }
    
    private void CaptureTagets(){
        found_id = 0;
        if(currentDetections != null){
            for (AprilTagDetection detection : currentDetections) {
                switch(detection.id){
                    case red_id:
                    case blue_id:
                        found_id = detection.id;
                        autoaim_bearing = detection.ftcPose.bearing;
                        autoaim_distance = detection.ftcPose.range;
                        break;
                    case 21:
                    case 22:
                    case 23:
                        obelisk_id = detection.id;
                        break;
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
        if (currentDetections.size() > 0) {
            result = currentDetections.get(0).id;
        }
        return result;
    }
    
    public AprilTagDetection getDetection(){
        AprilTagDetection result = null;
        if (currentDetections != null && currentDetections.size() > 0) {
            result = currentDetections.get(0);
        }
        return result;
    }
    
    public void telemetryAprilTag() {

//        List<AprilTagDetection> currentDetections = aprilTag.getDetections();
        telemetry.addData("# AprilTags Detected", currentDetections.size());
    
        // Step through the list of detections and display info for each one.
        for (AprilTagDetection detection : currentDetections) {
            if (detection.metadata != null) {
                double offsetDegrees = detection.ftcPose.bearing;
                telemetry.addLine(String.format("\n==== (ID %d) %s", detection.id, detection.metadata.name));
                telemetry.addLine(String.format("XYZ %6.1f %6.1f %6.1f  (inch)", detection.ftcPose.x, detection.ftcPose.y, detection.ftcPose.z));
                telemetry.addLine(String.format("PRY %6.1f %6.1f %6.1f  (deg)", detection.ftcPose.pitch, detection.ftcPose.roll, detection.ftcPose.yaw));
                telemetry.addLine(String.format("RBE %6.1f %6.1f %6.1f  (inch, deg, deg)", detection.ftcPose.range, detection.ftcPose.bearing, detection.ftcPose.elevation));
                telemetry.addData("Degrees off", offsetDegrees);
            } else {
                telemetry.addLine(String.format("\n==== (ID %d) Unknown", detection.id));
                telemetry.addLine(String.format("Center %6.0f %6.0f   (pixels)", detection.center.x, detection.center.y));
            }
        }   // end for() loop

        // Add "key" information to telemetry
        telemetry.addLine("\nkey:\nXYZ = X (Right), Y (Forward), Z (Up) dist.");
        telemetry.addLine("PRY = Pitch, Roll & Yaw (XYZ Rotation)");
        telemetry.addLine("RBE = Range, Bearing & Elevation");

    } 
}