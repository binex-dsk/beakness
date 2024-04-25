// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.lib.beaklib.motor.configs;

/** Configuration for closed loop wrapping & feedback. */
public class BeakClosedLoopConfigs {
    /**
     * Whether or not the PID error should wrap.
     */
    public boolean Wrap = false;

    public enum FeedbackSensor {
        BuiltIn,
        /** Only supported by TalonFX */
        FusedCANCoder,
        /** Only supported by TalonFX */
        RemoteCANCoder,

        ConnectedAbsolute,
        ConnectedRelative
    }

    /**
     * The source of the PID Controller's feedback.
     */
    public FeedbackSensor FeedbackSource = FeedbackSensor.BuiltIn;

    /**
     * The ID of the remote/fused sensor, if applicable.
     */
    public int RemoteSensorID = 0;

    public BeakClosedLoopConfigs() {
    }

    /**
     * Method-chaining API for this config.
     * 
     * @param wrap Whether or not to wrap the sensor error.
     * @return Itself, with this parameter changed.
     */
    public BeakClosedLoopConfigs withWrap(boolean wrap) {
        Wrap = wrap;
        return this;
    }

    /**
     * Method-chaining API for this config.
     * 
     * @param feedbackSource The source of PID feedback.
     * @return Itself, with this parameter changed.
     */
    public BeakClosedLoopConfigs withFeedbackSource(FeedbackSensor feedbackSource) {
        FeedbackSource = feedbackSource;
        return this;
    }

    /**
     * Method-chaining API for this config.
     * 
     * @param remoteSensorID The ID of the remote sensor, I/A.
     * @return Itself, with this parameter changed.
     */
    public BeakClosedLoopConfigs withRemoteSensorID(int remoteSensorID) {
        RemoteSensorID = remoteSensorID;
        return this;
    }
}
