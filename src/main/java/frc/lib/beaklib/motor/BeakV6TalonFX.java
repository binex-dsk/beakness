// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.lib.beaklib.motor;

import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.RotationsPerSecond;
import static edu.wpi.first.units.Units.Second;

import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.HardwareLimitSwitchConfigs;
import com.ctre.phoenix6.configs.MotionMagicConfigs;
import com.ctre.phoenix6.configs.MotorOutputConfigs;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.Slot1Configs;
import com.ctre.phoenix6.configs.Slot2Configs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.configs.TalonFXConfigurator;
import com.ctre.phoenix6.controls.DutyCycleOut;
// import com.ctre.phoenix6.controls.DutyCycleOut;
import com.ctre.phoenix6.controls.MotionMagicDutyCycle;
import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.controls.PositionDutyCycle;
import com.ctre.phoenix6.controls.PositionVoltage;
import com.ctre.phoenix6.controls.VelocityDutyCycle;
import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.controls.VoltageOut;
// import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.FeedbackSensorSourceValue;
import com.ctre.phoenix6.signals.ForwardLimitSourceValue;
import com.ctre.phoenix6.signals.ForwardLimitTypeValue;
import com.ctre.phoenix6.signals.ForwardLimitValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.ctre.phoenix6.signals.ReverseLimitSourceValue;
import com.ctre.phoenix6.signals.ReverseLimitTypeValue;
import com.ctre.phoenix6.signals.ReverseLimitValue;

import edu.wpi.first.units.Distance;
import edu.wpi.first.units.Measure;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStation;
import frc.lib.beaklib.motor.configs.BeakClosedLoopConfigs;
import frc.lib.beaklib.motor.configs.BeakCurrentLimitConfigs;
import frc.lib.beaklib.motor.configs.BeakDutyCycleConfigs;
import frc.lib.beaklib.motor.configs.BeakHardwareLimitSwitchConfigs;
import frc.lib.beaklib.motor.configs.BeakMotionProfileConfigs;
import frc.lib.beaklib.motor.configs.BeakVoltageConfigs;
import frc.lib.beaklib.motor.configs.BeakHardwareLimitSwitchConfigs.BeakLimitSwitchSource;
import frc.lib.beaklib.pid.BeakPIDConstants;

/** Add your docs here. */
public class BeakV6TalonFX extends TalonFX implements BeakMotorController {
    private TalonFXConfigurator m_configurator;
    private TalonFXConfiguration m_config = new TalonFXConfiguration();

    private DutyCycleOut m_dutyCycleOut = new DutyCycleOut(0.);
    private VoltageOut m_voltageOut = new VoltageOut(0.);
    private VelocityDutyCycle m_velocityOut = new VelocityDutyCycle(0.);
    private VelocityVoltage m_velocityVoltage = new VelocityVoltage(0.);
    private PositionDutyCycle m_positionOut = new PositionDutyCycle(0.);
    private PositionVoltage m_positionVoltage = new PositionVoltage(0.);
    private MotionMagicDutyCycle m_motionMagicOut = new MotionMagicDutyCycle(0.);
    private MotionMagicVoltage m_motionMagicVoltage = new MotionMagicVoltage(0.);

    private double m_velocityConversionConstant = 1. / 60.;
    private double m_positionConversionConstant = 1.;
    private double m_gearRatio = 1.;
    private Measure<Distance> m_wheelDiameter = Inches.of(4.);

    private boolean m_voltageCompEnabled = false;

    private int m_slot = 0;
    private double m_arbFeedforward = 0.;
    private boolean m_useFoc = false;

    private DigitalInput m_dioRevLimitSwitch = null;
    private DigitalInput m_dioFwdLimitSwitch = null;

    private BeakLimitSwitchSource m_forwardSource = BeakLimitSwitchSource.None;
    private BeakLimitSwitchSource m_reverseSource = BeakLimitSwitchSource.None;

    public BeakV6TalonFX(int port, String canBus) {
        super(port, canBus);
        m_configurator = super.getConfigurator();
        m_configurator.refresh(m_config);
    }

    public BeakV6TalonFX(int port) {
        this(port, "");
    }

    @Override
    public void setVoltage(double volts) {
        super.setControl(m_voltageOut.withEnableFOC(m_useFoc).withOutput(volts));
    }

    @Override
    public void set(double output) {
        super.setControl(m_dutyCycleOut.withEnableFOC(m_useFoc).withOutput(output));
    }

    @Override
    public void setBrake(boolean brake) {
        // v6 is funky
        NeutralModeValue neutralMode = brake ? NeutralModeValue.Brake : NeutralModeValue.Coast;

        MotorOutputConfigs config = new MotorOutputConfigs();
        m_configurator.refresh(config);

        config.NeutralMode = neutralMode;

        m_configurator.apply(config, 0.1);
    }

    @Override
    public void setVelocityNU(double nu) {
        if (m_voltageCompEnabled) {
            super.setControl(m_velocityVoltage
                    .withFeedForward(m_arbFeedforward)
                    .withSlot(m_slot)
                    .withVelocity(nu)
                    .withEnableFOC(m_useFoc));
        } else {
            super.setControl(m_velocityOut
                    .withFeedForward(m_arbFeedforward)
                    .withSlot(m_slot)
                    .withVelocity(nu)
                    .withEnableFOC(m_useFoc));
        }
    }

    @Override
    public void setPositionNU(double nu) {
        if (m_voltageCompEnabled) {
            super.setControl(m_positionVoltage
                    .withFeedForward(m_arbFeedforward)
                    .withSlot(m_slot)
                    .withPosition(nu)
                    .withEnableFOC(m_useFoc));
        } else {
            super.setControl(m_positionOut
                    .withFeedForward(m_arbFeedforward)
                    .withSlot(m_slot)
                    .withPosition(nu)
                    .withEnableFOC(m_useFoc));
        }
    }

    @Override
    public void setEncoderPositionNU(double nu) {
        super.setPosition(nu, 0.1);
    }

    @Override
    public void setMotionProfileNU(double nu) {
        if (m_voltageCompEnabled) {
            super.setControl(m_motionMagicVoltage
                    .withFeedForward(m_arbFeedforward)
                    .withSlot(m_slot)
                    .withPosition(nu)
                    .withEnableFOC(m_useFoc));
        } else {
            super.setControl(m_motionMagicOut
                    .withFeedForward(m_arbFeedforward)
                    .withSlot(m_slot)
                    .withPosition(nu)
                    .withEnableFOC(m_useFoc));
        }
    }

    @Override
    public DataSignal<Double> getVelocityNU() {
        return new DataSignal<Double>(super.getVelocity());
    }

    // TODO: Latency compensation!!!
    @Override
    public DataSignal<Double> getPositionNU(boolean latencyCompensated) {
        return new DataSignal<Double>(super.getPosition());
    }

    @Override
    public DataSignal<Double> getSuppliedVoltage() {
        return new DataSignal<Double>(super.getSupplyVoltage());
    }

    @Override
    public void setPID(BeakPIDConstants constants) {
        // The v6 slot API is wacky
        switch (m_slot) {
            case 0:
                Slot0Configs slot0Config = new Slot0Configs();
                slot0Config.kP = constants.kP;
                slot0Config.kI = constants.kI;
                slot0Config.kD = constants.kD;
                slot0Config.kV = constants.kV;
                slot0Config.kS = constants.kS;
                m_configurator.apply(slot0Config);
                break;
            case 1:
                Slot1Configs slot1Config = new Slot1Configs();
                slot1Config.kP = constants.kP;
                slot1Config.kI = constants.kI;
                slot1Config.kD = constants.kD;
                slot1Config.kV = constants.kV;
                slot1Config.kS = constants.kS;
                m_configurator.apply(slot1Config);
                break;
            case 2:
                Slot2Configs slot2Config = new Slot2Configs();
                slot2Config.kP = constants.kP;
                slot2Config.kI = constants.kI;
                slot2Config.kD = constants.kD;
                slot2Config.kV = constants.kV;
                slot2Config.kS = constants.kS;
                m_configurator.apply(slot2Config);
                break;
            default:
                DriverStation.reportWarning(
                        "v6 TalonFX only supports slots 0, 1, and 2. Not applying PID configuration.", false);
                break;
        }
    }

    @Override
    public BeakPIDConstants getPID() {
        // The v6 slot API is wacky
        BeakPIDConstants constants = new BeakPIDConstants();
        m_configurator.refresh(m_config);
        switch (m_slot) {
            case 0:
                Slot0Configs slot0Config = m_config.Slot0;
                constants.kP = slot0Config.kP;
                constants.kI = slot0Config.kI;
                constants.kD = slot0Config.kD;
                constants.kV = slot0Config.kV;
                constants.kS = slot0Config.kS;
                break;
            case 1:
                Slot1Configs slot1Config = m_config.Slot1;
                constants.kP = slot1Config.kP;
                constants.kI = slot1Config.kI;
                constants.kD = slot1Config.kD;
                constants.kV = slot1Config.kV;
                constants.kS = slot1Config.kS;
                break;
            case 2:
                Slot2Configs slot2Config = m_config.Slot2;
                constants.kP = slot2Config.kP;
                constants.kI = slot2Config.kI;
                constants.kD = slot2Config.kD;
                constants.kV = slot2Config.kV;
                constants.kS = slot2Config.kS;
                break;
            default:
                DriverStation.reportWarning(
                        "v6 TalonFX only supports slots 0, 1, and 2. Returning blank PID configuration.", false);
                break;
        }
        return constants;
    }

    @Override
    public void setVelocityConversionConstant(double constant) {
        m_velocityConversionConstant = constant;
    }

    @Override
    public double getVelocityConversionConstant() {
        return m_velocityConversionConstant;
    }

    @Override
    public void setPositionConversionConstant(double constant) {
        m_positionConversionConstant = constant;
    }

    @Override
    public double getPositionConversionConstant() {
        return m_positionConversionConstant;
    }

    @Override
    public void setEncoderGearRatio(double ratio) {
        m_gearRatio = ratio;
    }

    @Override
    public double getEncoderGearRatio() {
        return m_gearRatio;
    }

    @Override
    public void setWheelDiameter(Measure<Distance> diameter) {
        m_wheelDiameter = diameter;
    }

    @Override
    public Measure<Distance> getWheelDiameter() {
        return m_wheelDiameter;
    }

    @Override
    public void setNextArbFeedforward(double arbFeedforward) {
        m_arbFeedforward = arbFeedforward;
    }

    @Override
    public void setSlot(int slot) {
        m_slot = slot;
    }

    @Override
    public void useFOC(boolean useFoc) {
        m_useFoc = useFoc;
    }

    @Override
    public void applyConfig(BeakClosedLoopConfigs config) {
        TalonFXConfiguration configs = new TalonFXConfiguration();
        m_configurator.refresh(configs);

        configs.ClosedLoopGeneral.ContinuousWrap = config.Wrap;
        configs.Feedback.FeedbackRemoteSensorID = config.RemoteSensorID;

        FeedbackSensorSourceValue source;
        switch (config.FeedbackSource) {
            case FusedCANCoder:
                source = FeedbackSensorSourceValue.FusedCANcoder;
                break;
            case RemoteCANCoder:
                source = FeedbackSensorSourceValue.RemoteCANcoder;
                break;
            case BuiltIn:
            default:
                source = FeedbackSensorSourceValue.FusedCANcoder;
                break;
        }

        configs.Feedback.FeedbackSensorSource = source;

        m_configurator.apply(configs);
    }

    @Override
    public void applyConfig(BeakCurrentLimitConfigs config) {
        CurrentLimitsConfigs configs = new CurrentLimitsConfigs();
        m_configurator.refresh(configs);

        configs.StatorCurrentLimit = config.StatorCurrentLimit;
        configs.StatorCurrentLimitEnable = config.StatorCurrentLimit > 0.0;

        // TODO: Peak/reverse torque current

        configs.SupplyCurrentLimit = config.SupplyCurrentLimit;
        configs.SupplyCurrentLimitEnable = config.SupplyCurrentLimit > 0.0;
        configs.SupplyCurrentThreshold = config.SupplyCurrentThreshold;
        configs.SupplyTimeThreshold = config.SupplyTimeThreshold;

        m_configurator.apply(configs);
    }

    @Override
    public void applyConfig(BeakDutyCycleConfigs config) {
        TalonFXConfiguration configs = new TalonFXConfiguration();
        m_configurator.refresh(configs);

        configs.ClosedLoopRamps.DutyCycleClosedLoopRampPeriod = config.ClosedRampPeriod;
        configs.OpenLoopRamps.DutyCycleOpenLoopRampPeriod = config.OpenRampPeriod;

        configs.MotorOutput.DutyCycleNeutralDeadband = config.NeutralDeadband;
        configs.MotorOutput.PeakForwardDutyCycle = config.PeakForwardOutput;
        configs.MotorOutput.PeakReverseDutyCycle = config.PeakReverseOutput;

        m_configurator.apply(configs);
    }

    @Override
    public void applyConfig(BeakHardwareLimitSwitchConfigs config) {
        HardwareLimitSwitchConfigs configs = new HardwareLimitSwitchConfigs();
        m_configurator.refresh(configs);

        configs.ForwardLimitEnable = config.ForwardSource != BeakLimitSwitchSource.None;
        configs.ForwardLimitType = config.ForwardNormallyClosed ? ForwardLimitTypeValue.NormallyClosed
                : ForwardLimitTypeValue.NormallyOpen;

        switch (config.ForwardSource) {
            case Connected:
                configs.ForwardLimitSource = ForwardLimitSourceValue.LimitSwitchPin;
                configs.ForwardLimitRemoteSensorID = config.ForwardLimitSwitchID;
                m_forwardSource = BeakLimitSwitchSource.Connected;
                break;
            case DIO:
                if (m_dioFwdLimitSwitch != null) {
                    m_dioFwdLimitSwitch.close();
                }

                m_dioFwdLimitSwitch = new DigitalInput(config.ForwardLimitSwitchID);
                m_forwardSource = BeakLimitSwitchSource.DIO;
                break;
            default:
                m_forwardSource = BeakLimitSwitchSource.None;
        }

        configs.ReverseLimitEnable = config.ReverseSource != BeakLimitSwitchSource.None;
        configs.ReverseLimitType = config.ReverseNormallyClosed ? ReverseLimitTypeValue.NormallyClosed
                : ReverseLimitTypeValue.NormallyOpen;

        switch (config.ReverseSource) {
            case Connected:
                configs.ReverseLimitSource = ReverseLimitSourceValue.LimitSwitchPin;
                configs.ReverseLimitRemoteSensorID = config.ReverseLimitSwitchID;
                m_reverseSource = BeakLimitSwitchSource.Connected;
                break;
            case DIO:
                if (m_dioRevLimitSwitch != null) {
                    m_dioRevLimitSwitch.close();
                }

                m_dioRevLimitSwitch = new DigitalInput(config.ReverseLimitSwitchID);
                m_reverseSource = BeakLimitSwitchSource.DIO;
                break;
            default:
                m_reverseSource = BeakLimitSwitchSource.None;
        }

        m_configurator.apply(configs);
    }

    @Override
    public void applyConfig(BeakMotionProfileConfigs config) {
        MotionMagicConfigs configs = new MotionMagicConfigs();
        m_configurator.refresh(configs);

        configs.MotionMagicCruiseVelocity = config.Velocity.in(RotationsPerSecond);
        configs.MotionMagicAcceleration = config.Acceleration.in(RotationsPerSecond.per(Second));
        configs.MotionMagicJerk = config.Jerk.in(RotationsPerSecond.per(Second).per(Second));

        m_configurator.apply(configs);
    }

    @Override
    public void applyConfig(BeakVoltageConfigs config) {
        TalonFXConfiguration configs = new TalonFXConfiguration();
        m_configurator.refresh(configs);

        configs.ClosedLoopRamps.VoltageClosedLoopRampPeriod = config.ClosedRampPeriod;
        configs.OpenLoopRamps.VoltageOpenLoopRampPeriod = config.OpenRampPeriod;

        configs.Voltage.PeakForwardVoltage = config.PeakForwardOutput;
        configs.Voltage.PeakReverseVoltage = config.PeakReverseOutput;

        m_configurator.apply(configs);
    }

    @Override
    public boolean getForwardLimitSwitch() {
        return m_forwardSource == BeakLimitSwitchSource.Connected ? super.getForwardLimit().getValue() == ForwardLimitValue.ClosedToGround
                : m_dioFwdLimitSwitch.get();
    }

    @Override
    public boolean getReverseLimitSwitch() {
        return m_reverseSource == BeakLimitSwitchSource.Connected ? super.getReverseLimit().getValue() == ReverseLimitValue.ClosedToGround
                : m_dioRevLimitSwitch.get();
    }
}
