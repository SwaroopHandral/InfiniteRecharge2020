package frc.robot;

import edu.wpi.first.wpilibj.CounterBase.EncodingType;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.CANEncoder;



/**
 * Handles actions related to robot motion.
 * Includes motor controller, encoder, and gyro instances.
 * Created January 2018
 * 
 * @author Brewer FIRST Robotics Team 4564
 * @author Evan McCoy
 * @author Brent Roberts
 */
public class DriveTrain extends DifferentialDrive {
	private static DriveTrain instance;
	
	public static double DRIVEACCEL = 0.05;

	public static final double TURNACCEL = .06;

	public static final double TURNMAX = 0.5, DRIVE_MAX = 1.0;
	
	private static final double HIGH_DISTANCE_CONVERSION_FACTOR = 2.02; /* was 222/106.180550*/// low appears to be 37.452019/(12*Math.PI);	
	/*
		Changed neos to public for testing, change back soon
	*/
	public static final CANSparkMax
			frontL = new CANSparkMax(Constants.DRIVE_FL, CANSparkMax.MotorType.kBrushless),
			frontR = new CANSparkMax(Constants.DRIVE_FR, CANSparkMax.MotorType.kBrushless),
			middleL = new CANSparkMax(Constants.DRIVE_ML, CANSparkMax.MotorType.kBrushless),
			middleR = new CANSparkMax(Constants.DRIVE_MR, CANSparkMax.MotorType.kBrushless),
			backL = new CANSparkMax(Constants.DRIVE_BL, CANSparkMax.MotorType.kBrushless),
			backR = new CANSparkMax(Constants.DRIVE_BR, CANSparkMax.MotorType.kBrushless); 
	private static final SpeedControllerGroup left = new SpeedControllerGroup(frontL, middleL, backL);
	private static final SpeedControllerGroup right = new SpeedControllerGroup(frontR, middleR, backR); 
	
	private double P = 0, I = 0, D = 0;
	private double driveSpeed = 0, turnSpeed = 0;
	
	private CANEncoder encoderL, encoderR;
	//private double IPC_HIGH = 1, IPC_LOW = 1;
	private PID drivePID;
	//private Heading heading;
	//private Solenoid shifter;

	private boolean driveComp = true;
	
	/**
	 * Creates an instance of DriveTrain.
	 * Motor controller and encoder channels are determined in Constants.
	 */
	public DriveTrain() {
		super(left, right);
		
		initMotors();
		encoderL = new CANEncoder(frontL);
		encoderR =  new CANEncoder(frontR);
		encoderL.setPositionConversionFactor(this.HIGH_DISTANCE_CONVERSION_FACTOR);
		encoderR.setPositionConversionFactor(this.HIGH_DISTANCE_CONVERSION_FACTOR);
		Common.dashNum("conversion factor", encoderL.getPositionConversionFactor());
		//heading = new Heading();
		//shifter = new Solenoid(Constants.PCM_CAN_ID, Constants.Sol_SHIFTER);
		
		//pidL = new PID(0.005, 0, 0, false, true, "velL");
		//pidR = new PID(0.005, 0, 0, false, true, "velR");
		drivePID = new PID(P, I, D, false, false, "DrivePID");
		drivePID.setOutputLimits(-.4, 0.4);
		drivePID.setMinMagnitude(0.0);

		
		instance = this;
	}

	/**
	 * A function that initiates the drivetrain.
	 */
	public void init() {
		drivePID.reset();
	}

	
	/**
	 * Function to init Motors
	 */
	private void initMotors() {
		frontL.restoreFactoryDefaults();
		frontR.restoreFactoryDefaults();
		middleL.restoreFactoryDefaults();
		middleR.restoreFactoryDefaults();
		backL.restoreFactoryDefaults();
		backR.restoreFactoryDefaults();

		frontL.setSmartCurrentLimit(40);
		frontR.setSmartCurrentLimit(40);
		middleL.setSmartCurrentLimit(40);
		middleR.setSmartCurrentLimit(40);
		backL.setSmartCurrentLimit(40);
		backR.setSmartCurrentLimit(40);
	}
	
	/**
	 * Shifts the drivetrain gearbox to high gear.
	 */
	/*public void shiftHigh() {
		shifter.set(false);
	}
	
	/**
	 * Shifts the drivetrain gearbox to low gear.
	 */
	/*public void shiftLow() {
		shifter.set(true);
	}
	
	/**
	 * Whether or not the drivetrain is in low gear.
	 * 
	 * @return - is low
	 */
	/*public boolean isShiftedLow() {
		return shifter.get();
	}
	
	/**
	 * Resets the counts of the left and right encoders.
	 */
	public void resetEncoders() {
		encoderL.setPosition(0);
		encoderR.setPosition(0);
		drivePID.reset();
	}
	
	
	/**
	 * Returns an instance of DriveTrain which is bound to the motor controllers.
	 * Only this instance will be functional.
	 * 
	 * @return the DriveTrain instance.
	 */
	public static DriveTrain instance() {
		return instance;
	}
	
	/**
	 * Get raw counts for the left encoder.
	 * 
	 * @return the counts
	 */
	/*public int getLeftCounts() {
		return encoderL.get();
	}*/
	
	/**
	 * Get the scaled distance of the left encoder.
	 * 
	 * @return the distance in inches
	 */
	public double getLeftDist() {
		return encoderL.getPosition();
	}
	
	/**
	 * Get the scaled velocity of the left encoder.
	 * 
	 * @return the velocity in inches/second
	 */
	public double getLeftVelocity() {
		return encoderL.getVelocity();
	}
	
	/**
	 * Get raw counts for the right encoder.
	 * 
	 * @return the counts
	 */
	/*public int getRightCounts() {
		return encoderR.get();
	}*/
	
	/**
	 * Get the scaled distance of the right encoder.
	 * 
	 * @return double - the distance in inches
	 */
	public double getRightDist() {
		return encoderR.getPosition();
	}
	
	/**
	 * Get the scaled velocity of the right encoder.
	 * 
	 * @return double - the velocity in inches/second
	 */
	public double getRightVelocity() {
		return encoderR.getVelocity();
	}
	
	/**
	 * Get the averaged counts between the two encoders.
	 * 
	 * @return int - the average counts
	 */
	/*public int getAverageCounts() {
		return (encoderL.get() + encoderR.get()) / 2;
	}*/
	
	/**
	 * Get the averaged scaled distance between the two encoders.
	 * Inverts the right side to get a robot average.
	 * 
	 * @return double - the average distance in inches
	 */
	public double getAverageDist() {
		return (this.getLeftDist() + -this.getRightDist()) / 2;
	}
	
	/**
	 * Get the averaged scaled velocity between the two encoders.
	 * Inverts the right side to get a robot average.
	 * 
	 * @return double - the average velocity in inches/second
	 */
	public double getAverageVelocity() {
		return (-this.getRightVelocity() + this.getLeftVelocity()) / 2;
	}
	
	/**
	 * An instance of Heading, a gyro utility and PID controller.
	 * 
	 * @return Heading - the heading instance.
	 */
	/*public Heading getHeading() {
		return this.heading;
	}*/
	
	/**
	 * Gets the drive acceleration value based on the elevator height and gear.
	 * 
	 * @return - the drive acceleration value
	 */
	/*public double getDriveAccel() {
		Elevator e = Robot.getElevator();
		double percentHeight = e.getInches() / e.ELEVATOR_HEIGHT;
		
		if (isShiftedLow()) {
			Common.dashStr("Gear", "Low");
			Common.dashNum("Calculated Acceleration", (1.0 - percentHeight) * (ACCEL_LG_LE - ACCEL_LG_HE) + ACCEL_LG_HE);
			return (1.0 - percentHeight) * (ACCEL_LG_LE - ACCEL_LG_HE) + ACCEL_LG_HE;
		}
		else {
			Common.dashStr("Gear", "High");
			Common.dashNum("Calculated Acceleration", (1.0 - percentHeight) * (ACCEL_HG_LE - ACCEL_HG_HE) + ACCEL_HG_HE);
			return (1.0 - percentHeight) * (ACCEL_HG_LE - ACCEL_HG_HE) + ACCEL_HG_HE;
		}
	}*/
	
	/**
	 * Gradually accelerate to a specified drive value.
	 * Uses DRIVEACCEL as chaneg value and DRIVE_MAX as max speed allowed.
	 * 
	 * @param target - the target drive value from -1 to 1
	 * @return double - the allowed drive value for this cycle.
	 */
	public double driveAccelCurve(double target) {

		if (Math.abs(target) > DRIVE_MAX) {
			if (target > 0 ) {
				target = DRIVE_MAX;
			} else {
				target = -DRIVE_MAX;
			}
		}

		//If the magnitude of current is greater than the minimum
		//If the difference is greater than the allowed acceleration
		if (Math.abs(driveSpeed - target) > DRIVEACCEL) {
			//Accelerate in the correct direction
            if (driveSpeed > target) {
                driveSpeed = driveSpeed - DRIVEACCEL;
            } else {
                driveSpeed = driveSpeed + DRIVEACCEL;
            }
		}
		

		//If the difference is less than the allowed acceleration, reach target
		else {
            driveSpeed = target;
        }
		return driveSpeed;
	 }
	 
	 /**
	  * Gradually accelerate to a specified turn value.
	  * 
	  * @param target - the target turn value from -1 to 1
	  * @return double - the allowed turn value at this cycle.
	  */
	 public double turnAccelCurve(double target) {
		 if (Math.abs(turnSpeed - target) > TURNACCEL) {
	    		if (turnSpeed > target) {
	    			turnSpeed = turnSpeed - TURNACCEL;
	    		} else {
	    			turnSpeed = turnSpeed + TURNACCEL;
	    		}
	    	} else {
	    		turnSpeed = target;
	    	}
		 if (turnSpeed >= 0) {
			 turnSpeed = Math.min(TURNMAX, turnSpeed);
		 } else {
			 turnSpeed = Math.max(-TURNMAX, turnSpeed);
		 }
	    return turnSpeed;
	}
	
	/**
	 * Arcade drive with an acceleration curve.
	 * 
	 * @param drive - the forward/backward value from -1 to 1.
	 * @param turn - the turn value from -1 to 1.
	 */
	public void accelDrive(double drive, double turn) {
		drive = driveAccelCurve(drive);
		turn = turnAccelCurve(turn);
		Common.dashNum("Drive output", drive);
		Common.dashNum("Turn output", turn);
		arcadeDrive(drive, -turn);
	}
	
	
	/**
	 * Acceleration control for tank drive. Does not set motors.
	 * 
	 * @param left - the target left power.
	 * @param right - the target right power.
	 */
	/*public void accelTankDrive(double left, double right) {
		tankLeft = accelSide(tankLeft, left);
		tankRight = accelSide(tankRight, right);
		
		//System.out.println(tankLeft + ":" + tankRight);
	}*/
	
	/**
	 * Applies the current DriveTrain tankLeft and tankRight motor powers.
	 * Uses heading hold PID if heading hold is enabled with {@link Heading#setHeadingHold(boolean)}
	 */
	/*public void applyTankDrive() {
		if (heading.isHeadingHold()) {
			double turn = heading.turnRate() / 2;
			super.tankDrive(tankLeft + turn, tankRight - turn);
		}
		else {
			super.tankDrive(tankLeft, tankRight);
		}
	}*/
	
	/**
	 * A utility method that determines the change in current power, given a desired target and allowed power curve.
	 * 
	 * @param current the current motor power.
	 * @param target the target motor power.
	 * @return the new motor power.
	 */
	/*private double accelSide(double current, double target) {
		double TANKACCEL = getDriveAccel();
		//If the magnitude of current is less than the minimum
		if (Math.abs(current) < TANKMIN) {
			//Move to the lesser value of the minimum or the target, including desired direction.
			if (target > 0) {
				current = Math.min(TANKMIN, target);
			}
			else {
				current = Math.max(-TANKMIN, target);
			}
		}
		//If the magnitude of current is greater than the minimum
		//If the difference is greater than the allowed acceleration
		if (Math.abs(current - target) > TANKACCEL) {
			//Accelerate in the correct direction
            if (current > target) {
                current = current - TANKACCEL;
            } else {
                current = current + TANKACCEL;
            }
        }
		//If the difference is less than the allowed acceleration, reach target
		else {
            current = target;
        }
		return current;
	}*/

	public void drivebyPID()
    {
    	double outputDrive = calcDrive(), outputTurn = 0;// outputTurn = -heading.turnRate();
    	Common.dashNum("drivePIDOUT", outputDrive);
    	Common.dashNum("TurnPIDOUT ", outputTurn);
    	/*if(turning){
    		drivePID.setTarget(encoder.getDistance());
    		outputDrive = 0;
    	}*/
    	
    	arcadeDrive(outputDrive, outputTurn);
	}
	
	public void driveDistance(double distance) {
    	drivePID.setTarget(this.getAverageDist() + distance);
    	driveComp = Math.abs(getAverageDist() - drivePID.getTarget()) <= 2.0;
	}
	
	public double calcDrive() {
    	//Common.debug("DT:CalcDrive");
    	return drivePID.calc(getAverageDist());
    }

	/**
	 * Added an update for the Drive PID.
	 */
	public void update() {
		drivePID.update();
	}


}
