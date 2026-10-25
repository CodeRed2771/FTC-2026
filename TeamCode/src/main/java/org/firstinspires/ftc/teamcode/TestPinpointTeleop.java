package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Pinpoint Test", group = "Iterative Opmode")

public class TestPinpointTeleop extends OpMode {

    SensorGoBildaPinpoint pinpoint;

    @Override
    public void init() {

        // initialize the pinpoint module
        pinpoint = new SensorGoBildaPinpoint(hardwareMap, telemetry);
        pinpoint.init();

        telemetry.addData("Status", "Initialized");
        telemetry.addData("My Name", "Rajkumar");
        telemetry.update();
    }

    @Override
    public void loop() {

        pinpoint.loop();

        doTelemetry();
    }

    boolean back_prev = false;
    int page = 0;
    private void doTelemetry(){
        boolean back = (gamepad1.back || gamepad2.back);
        if(!back_prev && back)
            page = (page + 1) % 10;
        back_prev = back;

        telemetry.addData("Page", page);
        telemetry.update();
    }
}