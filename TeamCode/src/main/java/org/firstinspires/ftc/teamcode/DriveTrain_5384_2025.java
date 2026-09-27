package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import java.util.Locale;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import com.qualcomm.robotcore.hardware.Gamepad;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotor;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import com.qualcomm.robotcore.hardware.IMU;


public class DriveTrain_5384_2025 {
    private int[] fullPowerCallCounts = {0,0,0};
    private Boolean slowToggle = false;
    private DcMotor frontRight = null;     //frontleft
    private DcMotor frontLeft = null;   //frontRight
    private DcMotor backRight = null;   //backright
    private DcMotor backLeft = null;    //backleft

    double driveTicksPerInch = 50.6 * 24.0 / 36.8 * 24.0 / 23.0; 
    double slideTicksPerInch = 61.6 * 24.0 / 34.0 * 24.0 / 25.0;
    double turnTicksPerDegree = 13.55 * 180.0 / 270.0 * 180.0 / 200.0 * 180.0 / 181.5;
    double slideSkew = 0.0; //-0.04;
    double slideBias = 0.0; //6/36

    double globalAngle;
    double lastHeading;
    
    double drivePriority = 65.0;
    
    protected ElapsedTime runtime = new ElapsedTime();
    double timerSetPoint = 999;
    
    public double currentPower = 0;
    
    public PIDController vel_pid;
    public PIDController power_pid;
    
    double leftbias =0.0/100.0;
    double fwdbias =0.0/100.0;
    double fr_cal =(1-leftbias)*(1+fwdbias);
    double fl_cal =(1+leftbias)*(1+fwdbias);
    double bl_cal =(1+leftbias)*(1-fwdbias);
    double br_cal =(1-leftbias)*(1-fwdbias);
    Orientation angles;
    double init_angle;
    long powerSelectHigh = 0;
    long powerSelectThreshold = 1;
    long fcSelectThreshold = 1;
    long fcSelectHigh = 0;
    
    private final double min_pwr = 0.2;
    int leftFrontStartPos;
    
    boolean fieldCentricEnabled = false;
    
    private final double turn_slew_limit = 1.0 / 0.5; // 360 deg in 1 sec
    private final double drive_slew_limit = 1.0 / 0.3; // full speed in 1 sec
    private double prev_drive_time = 0.0;
    private double prev_turn = 0;
    private double prev_forward = 0.0;
    private double prev_slide = 0.0;
    
    IMU imu;
    Telemetry telemetry;

    public DriveTrain_5384_2025(HardwareMap hardwareMap, Telemetry _telemetry) {
        telemetry = _telemetry;
        frontRight = hardwareMap.dcMotor.get("frontRight");
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        backRight = hardwareMap.dcMotor.get("backRight");
        backLeft = hardwareMap.dcMotor.get("backLeft");

        frontRight.setDirection(DcMotor.Direction.REVERSE);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setDirection(DcMotor.Direction.REVERSE);
        backRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        IMU.Parameters parameters = new IMU.Parameters(
            new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.DOWN,
                RevHubOrientationOnRobot.UsbFacingDirection.BACKWARD
                )
            );
            
        imu = hardwareMap.get(IMU.class, "imu");
        imu.initialize(parameters);
        
        resetGyro();
 
        vel_pid = new PIDController();
        vel_pid.setP( 1.0 / 36.0); // 100% if target 12 inches away

        double pwr_rate_limit = (1.0 / 0.75); // (100% in 1.0 second)
        power_pid = new PIDController();
        power_pid.setRateLimits(-pwr_rate_limit,pwr_rate_limit);
        power_pid.setCommandLimits(min_pwr,1.0);
        power_pid.setFF(1.0/1.0); // 1.0 Power for 100% pwr

    }
    
    public void telemetry(int page){
        switch(page)
        {
            case 1:
                telemetry.addData("Target", vel_pid.getTarget());
                telemetry.addData("Vel Cmd", vel_pid.getCommand());
                telemetry.addData("Power Tgt", power_pid.getTarget());
                telemetry.addData("Power Limited Tgt", power_pid.getRateLimitedTarget());
                telemetry.addData("Power Cmd", power_pid.getCommand());
                break;
            default:
                telemetry.addData("Field Centric",fieldCentricEnabled);
                telemetry.addData("Anchor Angle", init_angle);
                telemetry.addData("Heading", rot_angle);
                telemetry.addData("Power", powerLevel);
                telemetry.addData("dT", delta_t);
                break;
        }     
    }

    public void runDrive_dumb(Gamepad gp) {

        double slide = -gp.left_stick_x;
        double turn = -gp.right_stick_x;
        double forward = -gp.left_stick_y;

        speedAdjust(gp);
        
        double power_fr = (forward+slide+turn)*powerLevel;
        double power_br = (forward-slide+turn)*powerLevel;
        double power_fl = (forward-slide-turn)*powerLevel;
        double power_bl = (forward+slide-turn)*powerLevel;
        
        frontRight.setPower(power_fr*fr_cal);
        backRight.setPower(power_br*br_cal);
        frontLeft.setPower(power_fl*fl_cal);
        backLeft.setPower(power_bl*bl_cal);
        
   }
   
   public void loop()
   {
       ; // do nothing
   }

   private double powerLevel = 0.8;
   public void speedAdjust(Gamepad gp){
        boolean powerSelect = gp.right_bumper;
        if (powerSelect) {
            powerSelectHigh += 1;
        } else {
            if (powerSelectHigh >= powerSelectThreshold) {
                if (slowToggle) {
                    slowToggle = false;
                } else {
                    slowToggle = true;
                }
            }
            powerSelectHigh = 0;
        }
        
        if (slowToggle) {
            powerLevel = 0.3;
        } else {
            powerLevel = 0.8;   
        }
       
   }
        
    private boolean last_fc_toggle = false;
    private double rot_angle = 0.0;
    private double delta_t = 0.0;
    public void runDrive_fieldCentric(Gamepad gp, double forward, double slide, double turn){
        double new_forward = forward;
        double new_slide = slide;
        angles = imu.getRobotOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.RADIANS);        
        rot_angle = -(angles.firstAngle-init_angle);
        boolean reset_init_angle = gp.b;
        
        speedAdjust(gp);
        
        boolean fc_toggle = gp.left_bumper;
        if (fc_toggle) {
            fcSelectHigh += 1;
        } else {
            if (fcSelectHigh >= fcSelectThreshold) {
                if (fieldCentricEnabled) {
                    fieldCentricEnabled = false;
                } else {
                    fieldCentricEnabled = true;
                }
            }
            fcSelectHigh = 0;
        }
        
        if (reset_init_angle) init_angle = angles.firstAngle;

        if(fieldCentricEnabled){
            new_forward = forward * Math.cos(rot_angle) - slide * Math.sin(rot_angle);
            new_slide = forward * Math.sin(rot_angle) + slide * Math.cos(rot_angle);
        }
        forward = new_forward;
        slide = new_slide;

        double cur_drive_time = System.currentTimeMillis();  
        delta_t = (cur_drive_time - prev_drive_time)/1000.0;
        // Apply slew rate limit to avoid spinning tires
        forward = Math.max(forward,prev_forward - drive_slew_limit * delta_t);
        forward = Math.min(forward,prev_forward + drive_slew_limit * delta_t);
        slide = Math.max(slide,prev_slide - drive_slew_limit * delta_t);
        slide = Math.min(slide,prev_slide + drive_slew_limit * delta_t);
        prev_forward = forward;
        prev_slide = slide;

        
        turn = Math.max(turn,prev_turn - turn_slew_limit * delta_t);
        turn = Math.min(turn,prev_turn + turn_slew_limit * delta_t);
        prev_turn = turn;
        prev_drive_time = cur_drive_time;

        double scale = powerLevel * 1.0;
        
        double power_fr = (forward+slide+turn)*scale;
        double power_br = (forward-slide+turn)*scale;
        double power_fl = (forward-slide-turn)*scale;
        double power_bl = (forward+slide-turn)*scale;
        
        frontRight.setPower(1.0*power_fr);
        backRight.setPower(1.0*power_br);
        frontLeft.setPower(1.0*power_fl);
        backLeft.setPower(1.0*power_bl);
    }

    private double turnAdjust(double fwd, double slide, double turn, double max) {
        double driveMag = Math.abs(fwd) + Math.abs(slide);
        double turnMag = Math.abs(turn);
        double mag = driveMag + turnMag;
        
        double priority = 1-(drivePriority/100.0);
        double scale = 1.0;
        if(mag > max){
            double ratio = turnMag / mag;
            
            scale = priority / ratio;
        }
        return 1.0;
        // return scale;
    }

    
    private double driveAdjust(double fwd, double slide, double turn, double max) {
        double magnitude = fwd + slide + turn;
        double priority = (drivePriority/100.0);
        double scale = 1.0;
        if(magnitude > max){
            scale = max/magnitude;
        }
        return 1.0;
        // return scale;
    }
    
    public void driveDistance(double distance, double pwr, double maxSeconds) {
        // currentPower = 0;
        startDriving(distance, pwr, maxSeconds);
        waitForDriveComplete();
    }
    
    public void startDriving(double distance, double pwr, double maxSeconds) {
        setTimer(maxSeconds);

        int targetTicks = calculateDistanceTicks(distance);
        power_pid.setCommandLimits(min_pwr,pwr);
        power_pid.resetSlew();

        frontLeft.setTargetPosition(frontLeft.getCurrentPosition() + targetTicks);
        frontRight.setTargetPosition(frontRight.getCurrentPosition() + targetTicks);
        backLeft.setTargetPosition(backLeft.getCurrentPosition() + targetTicks);
        backRight.setTargetPosition(backRight.getCurrentPosition() + targetTicks);

        setRunToPosition();


        leftFrontStartPos = frontLeft.getCurrentPosition();
        vel_pid.setTarget(distance);
    }
    
    public void setRunToPosition() {
        frontLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        backLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        frontRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        backRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    }
    
    public void slideDistance(double distance, double pwr, double maxSeconds) {
        // currentPower = 0;
        power_pid.setCommandLimits(-pwr, pwr);
        startSliding(distance, pwr, maxSeconds);

        waitForDriveComplete();

    }
    
    public void startSliding(double distance, double pwr, double maxSeconds) {

        setTimer(maxSeconds);

        int targetTicks = calculateSlideDistanceTicks(distance);
        int slideBiasTicks = calculateDistanceTicks(distance * slideBias);
        power_pid.setCommandLimits(min_pwr,pwr);
        power_pid.resetSlew();

        leftFrontStartPos = frontLeft.getCurrentPosition();
        vel_pid.setTarget(TicksToDistance(targetTicks));

        frontLeft.setTargetPosition(frontLeft.getCurrentPosition() + targetTicks + slideBiasTicks);
        frontRight.setTargetPosition(frontRight.getCurrentPosition() - targetTicks + slideBiasTicks);
        backLeft.setTargetPosition(backLeft.getCurrentPosition() - targetTicks + slideBiasTicks);
        backRight.setTargetPosition(backRight.getCurrentPosition() + targetTicks + slideBiasTicks);
        setRunToPosition();

      //  setMotorPower(pwr);
    }

    public void autoDriveStep(){
        double CurPos = 0;
        double velTarget = 0;

        // Target was set by Start function call.
        CurPos = TicksToDistance(frontLeft.getCurrentPosition()-leftFrontStartPos);
        velTarget = vel_pid.update(CurPos); // Feed in current position in Inches

        // Convert Velocity into Power
        power_pid.setTarget(Math.abs(velTarget));
        currentPower = power_pid.update(0.0);
        setMotorPower(currentPower);
        
        telemetry(1);
        telemetry.update();
    }

    public void waitForDriveComplete() {
        double now = System.currentTimeMillis() / 1000.0;
        double startTime = now;
        double nextRunTime = now + 0.05;
        double velTarget = 0;
        double CurPos = 0;
        while (!completedMovement() && !timeExpired()) {

            autoDriveStep();

            while(now < nextRunTime) {
                now = System.currentTimeMillis() / 1000.0;
            }
            nextRunTime+=0.05;
        }
    }
    
    boolean timeExpired() {
        return runtime.seconds() >= timerSetPoint;
    }
   public void setMotorPower(double pwr) {
        frontLeft.setPower(pwr * (1.0+slideSkew));
        frontRight.setPower(pwr * (1.0+slideSkew));
        backLeft.setPower(pwr * (1.0-slideSkew));
        backRight.setPower(pwr * (1.0-slideSkew));
    }
    
    public void setTimer(double secs) {
        timerSetPoint = runtime.seconds() + secs;
        
    }
    
    public void startTurning(double degrees, double pwr, double maxSeconds) {
        setTimer(maxSeconds);

        int targetTicks = calculateTurnTicks(degrees);
        leftFrontStartPos = frontLeft.getCurrentPosition();
        vel_pid.setTarget(TicksToDistance(-targetTicks));
        power_pid.setCommandLimits(min_pwr,pwr);
        power_pid.resetSlew();

        frontLeft.setTargetPosition(frontLeft.getCurrentPosition() - targetTicks );
        frontRight.setTargetPosition(frontRight.getCurrentPosition() + targetTicks );
        backLeft.setTargetPosition(backLeft.getCurrentPosition() - targetTicks );
        backRight.setTargetPosition(backRight.getCurrentPosition() + targetTicks );

        setRunToPosition();

    }

    public void turn(double degrees, double pwr, double maxSeconds) {
        // currentPower = 0;
        // power_pid.setCommandLimits(-pwr, pwr);
        startTurning(-degrees, pwr, maxSeconds);

        waitForDriveComplete();
    }

    public void turnTo(double degrees, double pwr, double maxSeconds) {
        turn(getTurnAmount(degrees), pwr, maxSeconds);
    }

    public void gyroTurn(double degrees, double maxPwr, double maxSeconds) {
        double targetDegree = 0;
        double P = .03;
        double pwr = 0;
        double minPwr = .18; // was .05 juse before first states match

        setRunWithoutEncoders();
        setTimer(maxSeconds);

        targetDegree = getHeading() + degrees;

        while (!timeExpired() && Math.abs(targetDegree - getHeading()) > 1.0) {
            pwr = (targetDegree - getHeading()) * P;

            if (pwr > maxPwr) {
                pwr = maxPwr;
            } else if (pwr < -maxPwr) {
                pwr = -maxPwr;
            }

            if (pwr > 0 && pwr < minPwr) {
                pwr = minPwr;
            } else if (pwr < 0 && pwr > -minPwr) {
                pwr = - minPwr;
            }

            frontLeft.setPower(pwr);
            backLeft.setPower(pwr);
            frontRight.setPower(-pwr);
            backRight.setPower(-pwr);
        }
        //stopDriveMotors();
    }

    private double getTurnAmount(double degrees) {

        return degrees - getHeading();
    }

    public boolean turnCompleted() {
        return completedMovement();
    }
    
    boolean completedMovement() {
        int diff = 0;
        diff = Math.abs(frontLeft.getCurrentPosition() - frontLeft.getTargetPosition());
        //telemetry.addData("completed diff", diff);
        //telemetry.update();
        return diff < 20;
        //return !(frontLeft.isBusy() || frontRight.isBusy());
    }
       
   public void gyroStart() {
        // Start the logging of measured acceleration
        //imu.startAccelerationIntegration(new Position(), new Velocity(), 1000);
    }

    /**
     * Resets the cumulative angle tracking to zero.
     */
    public void resetGyro() {
        imu.resetYaw();
        lastHeading = getHeadingSingleRotation();

        globalAngle = 0;
    }

    public double getHeadingSingleRotation() {
       
        YawPitchRollAngles robotOrientation;
        robotOrientation = imu.getRobotYawPitchRollAngles();
        
        return - robotOrientation.getYaw(AngleUnit.DEGREES);
    }

    /**
     * Get current cumulative angle rotation from last reset.
     *
     * @return Angle in degrees. + = left, - = right.
     */
    public double getHeading() {
         // We have to process the angle because the imu works in euler angles so the Z axis is
        // returned as 0 to +180 or 0 to -180 rolling back to -179 or +179 when rotation passes
        // 180 degrees. We detect this transition and track the total cumulative angle of rotation.

        YawPitchRollAngles robotOrientation;
        robotOrientation = imu.getRobotYawPitchRollAngles();
        
        double Yaw = robotOrientation.getYaw(AngleUnit.DEGREES);
        
        double deltaAngle = Yaw - lastHeading;

        if (deltaAngle < -180)
            deltaAngle += 360;
        else if (deltaAngle > 180)
            deltaAngle -= 360;

        globalAngle += deltaAngle;

        lastHeading = Yaw;

        return -globalAngle;
    }

    public void setRunWithoutEncoders() {
        frontLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    public double getHeadingFormatted() {
        return Math.round(getHeading());    
    }
    
    public int calculateDistanceTicks(double dist) {
        return (int) (dist * driveTicksPerInch);
    }
    public double TicksToDistance(int ticks) {return ((double) ticks)/driveTicksPerInch;}

    public int calculateSlideDistanceTicks(double dist) {
        return (int) (dist * slideTicksPerInch);
    }
    public double TicksToSlideDistance(int ticks) {return ((double) ticks)/slideTicksPerInch;}

    public int calculateTurnTicks(double degrees) {
        return (int) (degrees * turnTicksPerDegree);
    }
    

}
