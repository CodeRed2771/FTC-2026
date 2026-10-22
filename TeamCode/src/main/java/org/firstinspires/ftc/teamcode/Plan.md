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


```java
 boolean isGood; // Declare memory space
    public void main(){
        int counter = 1;
        
        isGood = true;
        toggleGood();
        
        if(isGood)
            isGood=false;
        
        if( ! isGood ) {
            toggleGood();
        }

        if( ! getState() ){
            toggleGood();
        } else {
            isGood = ! getState();
        }
        
    }
    
    public void toggleGood(){
        int counter = 0;
        isGood = ! isGood;
    }
    
    public boolean getState(){
        return isGood;
    }
    
```

# Laptop assignment
- Willem : 1
- Liam P : 6
- Cade : 5 - GH set
- Wes : 3
- Zeke : 4
- Asmitha : 2
- Liam J : 9
- FRC  : 7




# Auto Storyboard
- [ ] Have 2 automonomous modes: one for each starting location.
- [X] We need input for delayed start to match with partners.
- [X] We selector for Red vs Blue alliance for nectar recognition.

0. Start in line with Hive opening for shooting.

1. Leave starting point - Break contact with outer wall by driving forward.

2. shoot all 4 pollen.
- Only if hive is tipped towards us.
- Can we use camara to see April tags for tip position?
  - Tip camera so that we only see when hive is tipped good.
  - OR: Get center of apriltag in pixel coordinates and check whether Y is big or small.
  - 

3. Park in parkzone, just overlap a little.
- Drive left/right to the lane.
- Drive forward to parking distance.
- Drive left/right into parkzone.
- For the long drive, make sure to come in early to get into the top of the parking zone.











