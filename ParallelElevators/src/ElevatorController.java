import java.util.concurrent.TimeUnit;

public class ElevatorController extends Provided.AbstractElevatorController {

	@Override
	/**
	 * Wait for pressed buttons and available elevators by calling 'await'
	 * on the two corresponding Condition objects. When possible, assign an 
	 * Elevator to a waiting person by calling Elevator's 'hail' method. <br><br>
	 * 
	 * You should not just periodically check for pushed buttons and available
	 * elevators; you must use the conditions' await methods.
	 */

	/**
	 * While the thread is still running,
	 * lock the controller so other elevators cannot modify it.
	 * Then, wait for the floorQueue to have something in it and for an elevator to be available.
	 * If someone is waiting for an elevator while there are available elevators, 
	 * hail the next available elevator. 
	 */

	public synchronized void run() {

		while(!Provided.TERMINATE) {
			try {
				this.lock.lock();

				if (floorQueue.isEmpty())
					buttonPressed.await();

				if (nextAvailable() == null)
					elevatorFinished.await();

				for(Provided.AbstractElevator e : elevators) {
					if(e.isAvailable() && !personQueue.isEmpty() && !floorQueue.isEmpty()) {
						e.hail(floorQueue.remove(), personQueue.remove());
					}
				}

			} catch (InterruptedException | Provided.OccupiedException e) {
				// TODO Auto-generated catch block
				System.out.println("Controller interrupted");
			}
			finally {
				lock.unlock();
			}

			// TODO Auto-generated method stub

		}

	}

	/**
	 * This helper method returns the next available elevator from the set of elevators.
	 */

	public Provided.AbstractElevator nextAvailable() {
		for(Provided.AbstractElevator e : elevators) {
			if(e.isAvailable())
				return e;
		}
		return null;
	}
}