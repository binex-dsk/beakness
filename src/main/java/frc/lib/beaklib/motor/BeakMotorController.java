// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.lib.beaklib.motor;

import static edu.wpi.first.units.Units.*;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.units.*;
import edu.wpi.first.wpilibj.motorcontrol.MotorController;
import frc.lib.beaklib.motor.requests.BeakControlRequest;
import frc.lib.beaklib.pid.BeakPIDConstants;

/** Common interface for all motor controllers. */
public interface BeakMotorController extends MotorController {
    /**
     * Set the motor to be on brake or coast mode.
     * 
     * @param brake
     *              True = brake, False = coast
     */
    public void setBrake(boolean brake);

    /**
     * Run the motor in velocity mode.
     * </p>
     * 
     * To run in native units, use {@link setVelocityNU}.
     * 
     * @param velocity
     *                 Velocity to run.
     * 
     */
    default void setVelocity(Measure<Velocity<Distance>> velocity) {
        setVelocityNU(
                (velocity.in(MetersPerSecond) / (getWheelDiameter().in(Meters) * Math.PI) * 60.) // rpm
                        * getEncoderGearRatio() * getVelocityConversionConstant());
    }

    /**
     * Run the motor in velocity mode, based on an angular velocity target.
     * </p>
     * 
     * To run in native units, use {@link setVelocityNU}.
     * 
     * @param velocity Angular velocity to run.
     */
    default void setAngularVelocity(Measure<Velocity<Angle>> velocity) {
        setVelocityNU(velocity.in(RPM) * getVelocityConversionConstant() * getEncoderGearRatio());
    }

    /**
     * Run the motor in velocity mode, in NU.
     * </p>
     * NU/100ms for Talons, RPM for SparkMAX.
     * 
     * @param nu
     *           NU to run.
     */
    public void setVelocityNU(double nu);

    /**
     * Run the motor in position mode.
     * </p>
     * 
     * To run in native units, use {@link setPositionNU}.
     * 
     * @param distance
     *                 Distance to run.
     */
    default void setPosition(Measure<Distance> distance) {
        setPositionNU(
                (distance.in(Meters) * getPositionConversionConstant() * getEncoderGearRatio()) //
                        / (getWheelDiameter().in(Meters) * Math.PI));
    }

    /**
     * Run the motor to a specified angle.
     * </p>
     * 
     * To run in native units, use {@link setPositionNU}.
     * 
     * @param angle
     *              Angle to run to.
     */
    default void setAngle(Rotation2d angle) {
        setPositionNU(angle.getRotations() * getPositionConversionConstant() * getEncoderGearRatio());
    }

    /**
     * Run the motor in position mode, in NU.
     * </p>
     * 2048 NU per rotation for TalonFX, 4096 for TalonSRX, and usually 1 for
     * SparkMAX.
     * 
     * @param nu
     *           NU to run.
     */
    public void setPositionNU(double nu);

    /**
     * Sets the encoder's position.
     * </p>
     * 
     * To set in native units, use {@link setEncoderPositionNU}.
     * 
     * @param distance
     *                 Distance to set the encoder to.
     */
    default void setEncoderPosition(Measure<Distance> distance) {
        setEncoderPositionNU(
                (distance.in(Meters) * getPositionConversionConstant() * getEncoderGearRatio()) //
                        / (getWheelDiameter().in(Meters) * Math.PI));
    }

    /**
     * Sets the encoder's position, in motor rotations.
     * </p>
     * 
     * To set in native units, use {@link setEncoderPositionNU}.
     * 
     * @param rotations
     *                  Rotations to set the encoder to.
     */
    default void setEncoderPositionMotorRotations(double rotations) {
        setEncoderPositionNU(rotations * getPositionConversionConstant() * getEncoderGearRatio());
    }

    /**
     * Sets the encoder's position, in NU.
     * </p>
     * 2048 NU per rotation for TalonFX, 4096 for TalonSRX, and usually 1 for
     * SparkMAX.
     * 
     * @param nu
     *           NU to set the encoder to.
     */
    public void setEncoderPositionNU(double nu);

    /**
     * Resets the encoder position to 0.
     */
    default void resetEncoder() {
        setEncoderPositionNU(0.);
    }

    /**
     * Run the motor in motion magic mode.
     * </p>
     * 
     * To run in native units, use {@link setMotionMagicNU}.
     * 
     * @param distance
     *                 Distance to run.
     */
    default void setMotionMagic(Measure<Distance> distance) {
        setMotionMagicNU(
                (distance.in(Meters) * getPositionConversionConstant() * getEncoderGearRatio()) //
                        / (getWheelDiameter().in(Meters) * Math.PI));
    }

    /**
     * Runs the motor to a specified angle in motion magic mode.
     * </p>
     * 
     * To run in native units, use {@link setMotionMagicNU}.
     * 
     * @param angle
     *              Angle to run to.
     */
    default void setMotionMagicAngle(Rotation2d angle) {
        setMotionMagicNU(angle.getRotations() * getPositionConversionConstant() * getEncoderGearRatio());
    }

    /**
     * Runs the motor in motion magic mode, in NU.
     * </p>
     * 2048 NU per rotation for TalonFX, 4096 for TalonSRX, and usually 1 for
     * SparkMAX.
     * 
     * @param nu
     *           NU to run.
     */
    public void setMotionMagicNU(double nu);

    /**
     * Set the arbitrary feedforward to pass to the next PID command.
     * 
     * @param arbFeedforward
     *                       The feedforward, in volts.
     */
    public void setNextArbFeedforward(double arbFeedforward);

    /**
     * Set the slot to use with PID.
     * 
     * @param slot
     *             The next slot to use with PID. This applies to both setting
     *             constants and using them.
     */
    public void setSlot(int slot);

    /**
     * Enable or disable FOC control.
     * @param useFoc Whether or not to use FOC (if supported by the motor)
     */
    public void useFOC(boolean useFoc);

    /**
     * Run the motor using the specified request.
     * @param request The request to apply.
     */
    default void setControl(BeakControlRequest request) {
        request.apply(this);
    }

    /**
     * Get the motor velocity.
     * 
     * @return Velocity combined with the timestamp of the received data.
     */
    default DataSignal<Measure<Velocity<Distance>>> getSpeed() {
        DataSignal<Double> velocity = getVelocityNU();

        Measure<Velocity<Distance>> motorVelocity = MetersPerSecond
                .of(velocity.Value * (getWheelDiameter().in(Meters) * Math.PI)
                        / getVelocityConversionConstant() / getEncoderGearRatio() / 60.);

        return new DataSignal<Measure<Velocity<Distance>>>(motorVelocity);
    }

    /**
     * Get the motor angular velocity.
     * 
     * @return Velocity combined with the timestamp of the received data.
     */
    default DataSignal<Measure<Velocity<Angle>>> getAngularVelocity() {
        DataSignal<Double> velocity = getVelocityNU();
        var angularVelocity = RPM.of(velocity.Value / getVelocityConversionConstant() / getEncoderGearRatio());

        return new DataSignal<Measure<Velocity<Angle>>>(angularVelocity);
    }

    /**
     * Get the motor velocity, in NU.
     * </p>
     * NU/100ms for Talons, RPM for SparkMAX.
     * 
     * @return Velocity in NU combined with the timestamp of the received data.
     */
    public DataSignal<Double> getVelocityNU();

    /**
     * Get the motor distance.
     * 
     * @param latencyCompensated
     *                           Whether or not to attempt latency compensation.
     * 
     * @return Distance combined with the timestamp of the received data.
     */
    default DataSignal<Measure<Distance>> getDistance(boolean latencyCompensated) {
        DataSignal<Double> position = getPositionNU(latencyCompensated);

        Measure<Distance> motorDistance = Meters.of(position.Value * (getWheelDiameter().in(Meters) * Math.PI)
                / getPositionConversionConstant() / getEncoderGearRatio());

        return new DataSignal<Measure<Distance>>(motorDistance, position.Timestamp);
    }

    /**
     * Get the motor position, in motor rotations.
     * 
     * @param latencyCompensated
     *                           Whether or not to attempt latency compensation.
     * 
     * @return Position in motor rotations.
     */
    default DataSignal<Rotation2d> getAngle(boolean latencyCompensated) {
        DataSignal<Double> position = getPositionNU(latencyCompensated);

        return new DataSignal<Rotation2d>(
                Rotation2d.fromRotations(position.Value / getPositionConversionConstant() / getEncoderGearRatio()),
                position.Timestamp);
    }

    /**
     * Get the motor position, in NU.
     * 2048 NU per rotation for TalonFX, 4096 for TalonSRX, and usually 1 for
     * SparkMAX.
     * 
     * @param latencyCompensated
     *                           Whether or not to attempt latency compensation.
     * 
     * @return Position in NU combined with the timestamp of the received data.
     */
    public DataSignal<Double> getPositionNU(boolean latencyCompensated);

    /**
     * Stop the motor.
     */
    default void stop() {
        set(0.);
    }

    @Override
    default void disable() {
        stop();
    }

    @Override
    default void stopMotor() {
        stop();
    }

    /**
     * Get the voltage currently being run to the motor controller, with the
     * timestamp of the received data.
     */
    public DataSignal<Double> getSuppliedVoltage();

    /**
     * Get the current applied voltage to the motor controller.
     * 
     * @return Applied voltage.
     */
    default DataSignal<Double> getOutputVoltage() {
        DataSignal<Double> voltage = getSuppliedVoltage();
        voltage.Value *= get();
        return voltage;
    }

    /**
     * Set PIDF gains.
     * 
     * @param constants
     *                  PIDF Constants.
     */
    public void setPID(BeakPIDConstants constants);

    /**
     * Get PIDF gains.
     */
    public BeakPIDConstants getPID();

    /**
     * Set the reverse limit switch's default state
     * 
     * @param normallyClosed
     *                       True if its normal state is "closed", false if its
     *                       normal state is "open"
     */
    public void setReverseLimitSwitchNormallyClosed(boolean normallyClosed);

    /**
     * Set the forward limit switch's default state
     * 
     * @param normallyClosed
     *                       True if its normal state is "closed", false if its
     *                       normal state is "open"
     */
    public void setForwardLimitSwitchNormallyClosed(boolean normallyClosed);

    /**
     * Set the position at which the encoder will be reset to once the reverse limit
     * switch is hit.
     * </p>
     * 
     * Only applies to v6 Talon FX. This can probably be faked though :)
     * 
     * @param nu
     *           Position in NU (shaft rotations) to set the encoder to when
     *           hitting the reverse limit switch.
     */
    default void setReverseExtremePosition(double nu) {
    }

    /**
     * Set the position at which the encoder will be reset to once the forward limit
     * switch is hit.
     * </p>
     * 
     * Only applies to v6 Talon FX.
     * 
     * @param nu
     *           Position in NU (shaft rotations) to set the encoder to when
     *           hitting the forward limit switch.
     */
    default void setForwardExtremePosition(double nu) {
    }

    /**
     * Whether or not the limit switch is closed. This is independent of the
     * polarity (normally-closed) option on
     * CTRE devices, but on Spark MAXes, it is dependent--i.e. returning true if the
     * limit switch is not pressed,
     * when it's configured to be normally closed.
     * </p>
     * Also returns the timestamp of the received data.
     */
    public DataSignal<Boolean> getReverseLimitSwitch();

    /**
     * Whether or not the limit switch is closed. This is independent of the
     * polarity (normally-closed) option on
     * CTRE devices, but on Spark MAXes, it is dependent--i.e. returning true if the
     * limit switch is not pressed,
     * when it's configured to be normally closed.
     * </p>
     * Also returns the timestamp of the received data.
     */
    public DataSignal<Boolean> getForwardLimitSwitch();

    /**
     * Set the supply (PDH to controller) current limit.
     * </p>
     * 
     * For Talons, the "tripping" point is set to this plus 5, and the time to trip
     * back to the limit is set to 0.1 seconds.
     * 
     * @param amps
     *             The maximum amps to allow the motor controller to receive.
     */
    public void setSupplyCurrentLimit(int amps);

    /**
     * Set the stator (controller to motor) current limit.
     * </p>
     * 
     * Only supported on TalonFX.
     * </p>
     * 
     * The "tripping" point is set to this plus 5, and the time to trip back to the
     * limit is set to 0.1 seconds.
     * 
     * @param amps
     *             The maximum amps to allow the motor controller to send.
     */
    public void setStatorCurrentLimit(int amps);

    /**
     * Restore the motor controller's factory default settings.
     */
    public void restoreFactoryDefault();

    /**
     * Set the deadband, in NU, where PID control will no longer attempt to respond
     * to an error.
     * 
     * @param error
     *              Error deadband.
     */
    public void setAllowedClosedLoopError(double error);

    /**
     * Set the voltage compensation saturation for the motor controller.
     * </p>
     * 
     * See CTRE's docs for more info on voltage compensation.
     * </p>
     * 
     * Note: due to the closed-source nature of the motor controller's
     * implementations (JNI), the exact way this works on Spark MAXes and
     * Talons may be inconsistent. For more consistent behavior, use
     * <code>setVoltage</code> instead. This will directly account for
     * voltage drops, with a standardized compensation value on Talons,
     * or directly on the motor controller with PID on the Spark MAX.
     * </p>
     * 
     * For any motor controller, set this to anything greater than 0 to enable it.
     * 
     * @param saturation
     *                   Saturation.
     */
    public void setNominalVoltage(double saturation);

    /**
     * Set the Motion Magic cruise velocity.
     * </p>
     * 
     * See CTRE's Motion Magic documentation, or REV's Smart Motion example
     * to see what this means.
     * 
     * @param velocity
     *                 Cruise velocity, in NU.
     */
    public void setMotionMagicCruiseVelocity(double velocity);

    /**
     * Set the Motion Magic acceleration.
     * </p>
     * 
     * See CTRE's Motion Magic documentation, or REV's Smart Motion example
     * to see what this means.
     * 
     * @param accel
     *              Acceleration, in NU per second.
     */
    public void setMotionMagicAcceleration(double accel);

    /* CONVERSION API */

    /**
     * Set the velocity conversion constant for this motor.
     * </p>
     * 
     * The velocity conversion constant is a factor that, when dividing native
     * velocity units by the constant, outputs rotations per minute.
     * </p>
     * 
     * Default values:
     * <ul>
     * <li>v6 Talon FX, Spark MAX: 1 (NU are RPM)</li>
     * <li>v5 Talon FX: 600 / 2048 (NU/100ms -> RPM).</li>
     * <li>Talon SRX: 600 / 4096 (NU/100ms -> RPM)</li>
     * </ul>
     * 
     * @param constant
     *                 Conversion constant. Units: <code>NU/rev/min</code>
     */
    public void setVelocityConversionConstant(double constant);

    /**
     * Get the velocity conversion constant for this motor.
     * </p>
     * 
     * This is used by the RPM and m/s getter/setter methods. Divide the native
     * velocity units by this constant to output rotations per minute.
     * 
     * @return Conversion constant. Units: <code>NU/rev/min</code>
     */
    public double getVelocityConversionConstant();

    /**
     * Set the position conversion constant for this motor.
     * </p>
     * 
     * The position conversion constant is a factor that, when dividing native
     * position units by the constant, outputs rotations.
     * </p>
     * 
     * Default values:
     * <ul>
     * <li>v6 Talon FX, Spark MAX: 1 (NU are rotations)</li>
     * <li>v5 Talon FX: 2048 (NU -> rotations).</li>
     * <li>Talon SRX: 4096 (NU -> rotations)</li>
     * </ul>
     * 
     * @param constant
     *                 Conversion constant. Units: <code>NU/rev</code>
     */
    public void setPositionConversionConstant(double constant);

    /**
     * Get the position conversion constant for this motor.
     * </p>
     * 
     * This is used by the rotationsd and meters getter/setter methods. Divide the
     * native
     * position units by this constant to output rotations.
     * 
     * @return Conversion constant. Units: <code>NU/rev</code>
     */
    public double getPositionConversionConstant();

    /**
     * Set the gear ratio between the encoder and output shaft.
     * </p>
     * 
     * This number represents the number of rotations of the motor shaft per
     * rotation of the output shaft. Therefore, if a motor has a 16:1 gearbox
     * attached, this value should be 16.
     * </p>
     * 
     * For motors with integrated encoders, this will generally be greater than 1 if
     * the motor has a gearbox. However, if a non-integrated encoder is mounted
     * after the gearbox, this will be 1.
     * 
     * @param ratio
     *              Gear ratio. Units: coefficient
     */
    public void setEncoderGearRatio(double ratio);

    /**
     * Get the gear ratio between the encoder and output shaft.
     * </p>
     * 
     * This number represents the number of rotations of the motor shaft per
     * rotation of the output shaft. Therefore, if a motor has a 16:1 gearbox
     * attached, this value should be 16.
     * </p>
     * 
     * Divide the motor rotations or RPM by this number to get the actual rotations
     * or RPM of the final output shaft. Multiply rotations of the output shaft by
     * this number to get the number of motor rotations.
     * 
     * @return Gear ratio. Units: coefficient
     */
    public double getEncoderGearRatio();

    /**
     * Set the diameter of the wheel driven by this motor.
     * </p>
     * 
     * If the motor does not drive a traditional wheel but instead operates a linear
     * actuation mechanism, set this to the diameter of whatever circular object it
     * is rotating.
     * 
     * @param diameter
     *                 Diameter of the wheel. Units: distance
     */
    public void setWheelDiameter(Measure<Distance> diameter);

    /**
     * Get the diameter of the wheel driven by this motor.
     * </p>
     * 
     * Multiply the number of motor rotations or RPM by this number to get the
     * distance travelled by this motor, or the linear speed of the wheel attached
     * to it. Divide the speed or distance by this number to get output shaft
     * rotations.
     * </p>
     * 
     * Note that multiplying RPM by this will net meters per minute, so to get
     * meters per second, you need to divide by 60.
     * 
     * @return Diameter of the wheel. Units: distance
     */
    public Measure<Distance> getWheelDiameter();
}
