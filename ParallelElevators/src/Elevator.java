public class Elevator extends Provided.AbstractElevator {

	/**
	 * Constructor for an elevator. <br> 
	 * Passes along the ElevatorController, which all elevators share.
	 */

	public Elevator(Provided.AbstractElevatorController control) {
		super(control);
		// TODO Auto-generated constructor stub
	}

	/**
	 * This hail() method allows the elevator to know a passenger has hailed it. <br> 
	 * First, it locks the elevator so that no other passengers can modify it.<br>
	 * Then, if the elevator is available, the passenger is assigned to the elevator
	 * and the passenger's destination set to the elevator's target floor.
	 * Then, the elevator is unlocked again.
	 */

	@Override
	public void hail(int floor, Provided.Person p) throws Provided.OccupiedException {
		// TODO Auto-generated method stub
		try {
			lock.lock();
			if(isAvailable()) {
				passenger = p;
				this.targetFloor = floor;
			}
			else
				throw new Provided.OccupiedException("Elevator is occupied!");
		}
		finally {
			lock.unlock();
		}

	}

	/**
	 * The start() method creates and runs a new thread associated with the elevator.
	 */

	@Override
	public void start() {
		super.thread = new Thread(this);
		super.thread.start();
		// TODO Auto-generated method stub

	}

	/**
	 * A boolean method that returns the availability of the elevator, given that it currently
	 * does not have a passenger and is not carrying anything.
	 */

	@Override
	public boolean isAvailable() {
		return (passenger == null && !carrying);
	}

	/**
	 * This method allows the passenger to board the elevator. <br>
	 * The elevator is passed to the passenger's board method, 
	 * the elevator's carrying field is set to true, and
	 * targetFloor of elevator is set to passenger's destination.
	 */

	@Override
	protected void pickUp() {
		passenger.board(this);
		carrying = true;
		targetFloor = passenger.getDestination();
		// TODO Auto-generated method stub

	}

	/**
	 * This boolean method checks if the elevator should pick up the passenger. <br>
	 * It should pick up the passenger if passenger is defined, 
	 * the elevator is not currently carrying anything,
	 * and the elevator is not moving.
	 */

	@Override
	protected boolean shouldPickUp() {
		if(passenger !=null && carrying == false && !isMoving())
			return true;
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * This method drops off the passenger at the current floor.<br>
	 * First, the elevatorController is locked so no other threads can modify it.
	 * Then, the passenger exits, and the elevator carrying field is set to false and it's
	 * current passenger set to null. Then, it sends a signal to the controller that the elevator
	 * is finished. 
	 * Finally, the elevatorController is unlocked again. 
	 */

	@Override
	protected void offload() {
		try {
			control.lock.lock();
			passenger.exit();
			carrying = false;
			passenger = null;
			control.elevatorFinished.signalAll();

		}
		finally {
			control.lock.unlock();
		}
		// TODO Auto-generated method stub

	}

	/**
	 * This boolean method checks if the elevator should off load the passenger. <br>
	 * It should off load if the elevator is currently carrying a passenger 
	 * and it is not moving.
	 */
	@Override
	protected boolean shouldOffload() {
		// TODO Auto-generated method stub

		if(!isMoving() && carrying)
			return true;
		return false;
	}
}

