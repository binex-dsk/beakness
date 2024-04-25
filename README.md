# Beaking time

TODO:::::::::

- [x] work
- [x] support FoC
- [x] create motor control requests
- [ ] motor config requests/classes
    - [x] ClosedLoop: wrap
    - [x] Current Limits
    - [x] Fused CANCoder of some sort
    - [x] Limit switch configs
        - [x] Support external (DIO) pins
    - [x] Motion Magic
    - [x] Duty Cycle: deadband, peak forward/reverse, open/closed loop ramps
    - [x] Same for voltage
    - [ ] Same for torque
    - [ ] Soft Limits
    - [ ] Similar thing for encoders/gyros
    - [ ] Nominal voltage
- [x] swerve control requests
    - [ ] Facing Angle
    - [ ] X-Drive
    - [ ] Robot Centric
    - [ ] SysId control of some sort
- [ ] look forward to prevent slips and such
- [ ] Collision detection; ignore odometry
- [ ] Slip detection
- [ ] Real feedforward
- [ ] Standardized Simulation
- [ ] Differential drivetrain requests
- [ ] TorqueCurrent output
- [ ] More clean API for duty vs voltage vs torque
- [ ] General cleanup of old stuff
- [ ] 250Hz odometry
    - depends on status signals
- [ ] PWM controller w/ DIO encoder?
- [ ] Fused CANCoder

- [ ] General DataSignal improvements
    - [ ] getter for value/timestamp
    - [ ] Update frequencies
    - [ ] Refresh
    - [ ] RefreshAll
    - [ ] SignalStore, perhaps?

- [ ] LogStore & DashboardStore