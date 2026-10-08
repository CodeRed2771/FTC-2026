# Teleop Plan

## Shooter control

On press and hold of trigger on manipulator gamepad:
1. Spin up shooter wheel
    - Does shooter need fixed speed or distance based ramp up.
2. Once at speed and aimed at the target, start pushing elements into shooter.
    - Shoot all element in the robot.  Empty it.

### Example code we want to be able to write
For settings speed:
`shooter.set(1.0); // Full power`
Similar: Shooter_5384_2025.setShooterSpeed()

For checking 'Once at speed':
`shooter.onTarget(); // True if near set command` <- use this
Similar: Shooter_5384_2025.readyToShoot()

`shooter.get(); // readback what speed of the motor +/-1.0`

## Aiming control

On press of manipulator gamepad A?, lock turret to fixed position ?straight?
`shooter.aim(0.5);  //turns turret to angle from robot`

On press of other trigger on manipulator gamepad, point turret at hive April tag.


# Laptop assignment
- Willem : 1
- Liam P : 6
- Cade : ?
- Wes : 3 + 4
- Liam J : 9
- Asmitha : 2
- Buyaung : 7