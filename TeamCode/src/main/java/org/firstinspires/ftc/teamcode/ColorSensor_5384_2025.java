package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.opencv.ImageRegion;
import org.firstinspires.ftc.vision.opencv.PredominantColorProcessor;
import android.util.Size;



public class ColorSensor_5384_2025{
    private WebcamName camera;
    private Telemetry telemetry;
    private VisionPortal visionPortal;
    private PredominantColorProcessor colorSensor;
    
    public ColorSensor_5384_2025(WebcamName _camera, Telemetry _telemetry) {
        camera = _camera;
        telemetry = _telemetry;

        /* Build a "Color Sensor" vision processor based on the PredominantColorProcessor class.
         *
         * - Focus the color sensor by defining a RegionOfInterest (ROI) which you want to inspect.
         *     This can be the entire frame, or a sub-region defined using:
         *     1) standard image coordinates or 2) a normalized +/- 1.0 coordinate system.
         *     Use one form of the ImageRegion class to define the ROI.
         *         ImageRegion.entireFrame()
         *         ImageRegion.asImageCoordinates(50, 50,  150, 150)  100x100 pixel square near the upper left corner
         *         ImageRegion.asUnityCenterCoordinates(-0.1, 0.1, 0.1, -0.1)  10% width/height square centered on screen
         *
         * - Set the list of "acceptable" color swatches (matches).
         *     Only colors that you assign here will be returned.
         *     If you know the sensor will be pointing to one of a few specific colors, enter them here.
         *     Or, if the sensor may be pointed randomly, provide some additional colors that may match the surrounding.
         *     Possible choices are:
         *         RED, ORANGE, YELLOW, GREEN, CYAN, BLUE, PURPLE, MAGENTA, BLACK, WHITE;
         *
         *     Note that in the example shown below, only some of the available colors are included.
         *     This will force any other colored region into one of these colors.
         *     eg: Green may be reported as YELLOW, as this may be the "closest" match.
         */
        colorSensor = new PredominantColorProcessor.Builder()
                .setRoi(ImageRegion.asUnityCenterCoordinates(-0.1, 0.1, 0.1, -0.1))
                .setSwatches(
                    PredominantColorProcessor.Swatch.PURPLE,
                    PredominantColorProcessor.Swatch.GREEN,
                    PredominantColorProcessor.Swatch.BLACK)
                .build();

        /*
         * Build a vision portal to run the Color Sensor process.
         *
         *  - Add the colorSensor process created above.
         *  - Set the desired video resolution.
         *      Since a high resolution will not improve this process, choose a lower resolution that is
         *      supported by your camera.  This will improve overall performance and reduce latency.
         *  - Choose your video source.  This may be
         *      .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))  .....   for a webcam
         *  or
         *      .setCamera(BuiltinCameraDirection.BACK)    ... for a Phone Camera
         */
        VisionPortal portal = new VisionPortal.Builder()
                .addProcessor(colorSensor)
                .setCameraResolution(new Size(640, 480))
                .setCamera(camera)
                .build();
    }

    public Artifact detectColor(){
        PredominantColorProcessor.Result result = colorSensor.getAnalysis();
        switch(result.closestSwatch) {
            case PURPLE:
                return Artifact.PURPLE;
            case GREEN:
                return Artifact.GREEN;
            default:
                return Artifact.NONE;
        }
    }
}
