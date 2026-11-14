/*
Copyright 2025 FIRST Tech Challenge Team 5383

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute,
sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial
portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.teamcode.Biobuzz.Biobuzz_Intake_5383;
import org.firstinspires.ftc.teamcode.Biobuzz.Biobuzz_Shooter_5383;
import org.firstinspires.ftc.teamcode.Biobuzz.Biobuzz_Storage_5383;
import org.firstinspires.ftc.teamcode.Biobuzz.DriveTrain_5383;

/**
 * This file contains a minimal example of a Linear "OpMode". An OpMode is a 'program' that runs
 * in either the autonomous or the TeleOp period of an FTC match. The names of OpModes appear on
 * the menu of the FTC Driver Station. When an selection is made from the menu, the corresponding
 * OpMode class is instantiated on the Robot Controller and executed.
 *
 * Remove the @Disabled annotation on the next line or two (if present) to add this OpMode to the
 * Driver Station OpMode list, or add a @Disabled annotation to prevent this OpMode from being
 * added to the Driver Station.
 */

public class AutoBaseClass_5383 extends AutoBaseClass {
    protected DriveTrain_5383 driveTrain;
    protected Biobuzz_Intake_5383 intake;
    protected Biobuzz_Shooter_5383 shooter;
    protected Biobuzz_Storage_5383 storage;

    @Override
    public void initializeRobot() {
        super.initializeRobot();
        driveTrain = new DriveTrain_5383(hardwareMap);
        intake = new Biobuzz_Intake_5383(hardwareMap);
        shooter = new Biobuzz_Shooter_5383(hardwareMap);
        storage = new Biobuzz_Storage_5383(hardwareMap);
    }

    @Override
    public void configureRobot(){
        driveTrain.registerPages(display);
        intake.registerPages(display);
        shooter.registerPages(display);
        storage.registerPages(display);
    }

    @Override
    public void autoLoop(){
        super.autoLoop();

        driveTrain.loop();
        intake.loop();
        shooter.loop();
        storage.loop();

        saveLastAutoPosition(driveTrain.getCurrentPosition());

    }
}
