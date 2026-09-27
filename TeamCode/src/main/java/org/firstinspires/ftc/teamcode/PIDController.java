package org.firstinspires.ftc.teamcode;

public class PIDController {
    protected double target = 0.0, rate_limited_target = 0.0, rate_lim_target_prev =0.0;
    protected double command = 0.0, Cmd_ff = 0, Cmd_p = 0.0, Cmd_i= 0.0, Cmd_d = 0.0;
    public double slew_UL = 0, slew_LL = 0;
    protected double error_UL = 0, error_LL = 0;
    protected double target_UL = 0, target_LL = 0;
    protected double integral_UL = 0, integral_LL = 0;
    protected double command_UL = 0, command_LL = 0;
    public double Kp=0, Ki=0, Kd=0, Kff = 0;
    protected double error_prev;
    protected double last_update = 0.0;

    public boolean slew_rate_enabled = false;
    protected boolean target_limit_enabled = false;
    protected boolean integral_limit_enabled = false;
    protected boolean command_limit_enabled = false;
    protected boolean error_limit_enabled = false;

    public PIDController()
    {
        ;
    }

    public PIDController(double P, double I,double D)
    {
        setGains(P,I,D);
    }

    public void setTarget(double tgt)
    {
        if (target_limit_enabled && tgt > target_UL) tgt = target_UL;
        if (target_limit_enabled && tgt < target_LL) tgt = target_LL;

        target = tgt;
    }

    public void setFF(double gain){
        Kff = gain;
    }
    public void setP(double gain){
        Kp = gain;
    }
    public void setI(double gain){
        Ki = gain;
    }
    public void setD(double gain){
        Kd = gain;
    }
    
    public void setGains(double FF, double P, double I, double D){
        setFF(FF);
        setP(P);
        setI(I);
        setD(D);
    }

    public void setGains(double P, double I, double D)
    {
        setP(P);
        setI(I);
        setD(D);
    }

    public void resetTargetLimits(){target_limit_enabled = false;}
    public void setTargetLimits(double lower, double upper)
    {
        if ( upper < lower ) {
            target_UL = lower;
            target_LL = upper;
            target_limit_enabled = true;
        }
        else {
            target_UL = upper;
            target_LL = lower;
            target_limit_enabled = true;
        }
    }

    public void resetIntegralLimits(){integral_limit_enabled = false;}
    public void setIntegralLimits(double lower, double upper)
    {
        if ( upper < lower ) {
            integral_UL = lower;
            integral_LL = upper;
            integral_limit_enabled = true;
        }
        else {
            integral_UL = upper;
            integral_LL = lower;
            integral_limit_enabled = true;
        }
    }

    public void resetCommandLimits(){command_limit_enabled= false;}
    public void setCommandLimits(double lower, double upper)
    {
        if ( upper < lower ) {
            command_UL = lower;
            command_LL = upper;
            command_limit_enabled = true;
        }
        else {
            command_UL = upper;
            command_LL = lower;
            command_limit_enabled = true;
        }
    }

    public void resetErrorLimits(){error_limit_enabled= false;}
    public void setErrorLimits(double lower, double upper)
    {
        if ( upper < lower ) {
            error_UL = lower;
            error_LL = upper;
            error_limit_enabled = true;
        }
        else {
            error_UL = upper;
            error_LL = lower;
            error_limit_enabled = true;
        }
    }

    public void resetRateLimits(){slew_rate_enabled= false;}
    public void setRateLimits(double lower, double upper)
    {
        if ( upper < lower ) {
            slew_UL = lower;
            slew_LL = upper;
            slew_rate_enabled = true;
        }
        else {
            slew_UL = upper;
            slew_LL = lower;
            slew_rate_enabled = true;
        }
        resetSlew();
    }
    public double getCommand(){
        return command;
    }

    public double getTarget()
    {
        return target;
    }
    public double getRateLimitedTarget()
    {
        return rate_limited_target;
    }

    public double update(double feedback)
    {
        double now = System.currentTimeMillis();
        double time_step = (now - last_update) / 1000.0;

        double local_max = rate_limited_target + slew_UL * time_step;
        double local_min = rate_limited_target + slew_LL * time_step;

        if(slew_rate_enabled){
            rate_limited_target = Math.min(target, local_max);
            rate_limited_target = Math.max(rate_limited_target, local_min);
                
        } else {
            rate_limited_target = target;
        }
        
        double error = rate_limited_target - feedback;

        if (error_limit_enabled && (error > error_UL)) error = error_UL;
        if (error_limit_enabled && (error < error_LL)) error = error_LL;


        Cmd_ff = Kff * rate_limited_target;

        Cmd_p = Kp *  error;

        Cmd_i = Cmd_i + Ki * time_step * error;
        if (integral_limit_enabled && (Cmd_i > integral_UL)) Cmd_i = integral_UL;
        if (integral_limit_enabled && (Cmd_i < integral_LL)) Cmd_i = integral_LL;

        Cmd_d = Kd * time_step * (error - error_prev);
        error_prev = error;

        command = Cmd_p + Cmd_i + Cmd_d + Cmd_ff;
        if (command_limit_enabled && (command > command_UL)) command = command_UL;
        if (command_limit_enabled && (command < command_LL)) command = command_LL;

        last_update = now;
        return command;
    }
    
    void resetSlew(){
        last_update = System.currentTimeMillis();
    }

    void Initialize(double tgt, double feedback, double output)
    {
        setTarget(tgt);
        rate_limited_target = target;
        last_update = System.currentTimeMillis();
        
        error_prev = (target-feedback);
        if (error_limit_enabled && error_prev > error_UL) error_prev = error_UL;
        if (error_limit_enabled && error_prev < error_LL) error_prev = error_LL;

        Cmd_p = Kp *  error_prev;

        Cmd_i = 0;

        Cmd_d = 0;
    }
}
