// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.lib.beaklib.motor.requests.motionmagic;

import frc.lib.beaklib.motor.BeakMotorController;
import frc.lib.beaklib.motor.requests.BeakControlRequest;

/** Drive to a specified position using smart motion / motion magic. */
public class BeakMotionMagicNU extends BeakControlRequest {
    /**
     * Position setpoint in NU.
     */
    public double Position = 0.0;

    /**
     * Whether or not to use FOC commutation if supported.
     */
    public boolean UseFOC = false;

    /**
     * Output value (current, voltage, duty cycle)
     */
    public OutputType Output = OutputType.Voltage;

    public BeakMotionMagicNU() {
    }

    public void apply(BeakMotorController controller) {
        controller.setNextOutputType(Output);
        controller.useFOC(UseFOC);
        controller.setMotionProfileNU(Position);
    }

    /**
     * Method-chaining API for this request.
     * 
     * @param position The new position to use, in NU.
     * @return Itself, with this parameter changed.
     */
    public BeakMotionMagicNU withPosition(double position) {
        this.Position = position;
        return this;
    }

    /**
     * Method-chaining API for this request.
     * 
     * @param useFOC Whether or not to use FOC.
     * @return Itself, with this parameter changed.
     */
    public BeakMotionMagicNU withUseFOC(boolean useFOC) {
        this.UseFOC = useFOC;
        return this;
    }

    /**
     * Method-chaining API for this request.
     * 
     * @param type What output type to use: voltage, duty cycle, current
     * @return Itself, with this parameter changed.
     */
    public BeakMotionMagicNU withOutputType(OutputType type) {
        this.Output = type;
        return this;
    }

}
