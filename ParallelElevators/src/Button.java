public class Button extends Provided.AbstractButton {

	/**
	 * Constructor for the button that passes along the shared ElevatorController
	 * as well as the floor that the button is pressed for.
	 */

	public Button(int floor, Provided.AbstractElevatorController control) {
		super(floor, control);
	}

	/**
	 * First, lock the controller so that no other modifications can be made to the thread. 
	 * Alerts the controller that a button has been pressed, with the person who pressed it
	 * and the floor the person wants to go to. 
	 * Finally, unlocks controller again.
	 */

	@Override
	protected void press(Provided.Person p) {
		try {
			control.lock.lock();
			control.request(floor, p);
			control.buttonPressed.signalAll();
		}
		finally {
			control.lock.unlock();
		}
	}
}