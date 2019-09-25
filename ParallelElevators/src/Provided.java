import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * CS 2110 Concurrency Assignment <br> <br>
 * 
 * Defined below are several abstract classes that define the backbone of an
 * elevator system. You are to extend these abstract classes, implementing 
 * the abstract methods such that all the Persons are delivered safely to their
 * destination floors.<br> <br>
 * 
 * Start by making classes that will extend the abstract classes here. Your classes
 * should be called: <br>
 *  - Elevator <br>
 *  - Button <br>
 *  - ElevatorController <br>
 * Add method stubs in your concrete classes for each of the abstract methods, and
 * go from there. Note that you should not need to import 'Provided'. Eclipse 
 * sometimes automatically imports it. Instead, when you refer to classes in Provided,
 * reference them as "Provided._____".<br> <br>

 * Read the comments in this file for instructions, advice, and hints on specific
 * classes and methods. <br> <br>
 * 
 * We strongly recommend that you implement the Elevator 'start()' method first! 
 * Try running the main method in this file after you do. You should see helpful
 * debug information in your output console. We recommend that you then proceed to 
 * complete the Button class, then the Elevator class, and finally the ElevatorController
 * class last. <br><br>
 * 
 * The abstract classes below include several public and protected fields. You may 
 * introduce more fields in your concrete implementations if you like, but you should
 * not need to; the provided fields are sufficient. Neglecting the provided fields
 * may cause tests to fail and/or debug information to be incorrect. <br><br>
 * 
 * We also recommend that you test your
 * implementation with various experimental parameters. Make sure your implementation
 * is thread-safe by trying high-speed, stressful parameters, like 'DELAY' = 5 and
 * 'TEST_DURATION' = 1. Make sure your implementation works with any number of elevators
 * and floors. You must utilize every elevator. <br> <br>
 * 
 * You should not make any changes to this file except for testing purposes. We 
 * will test your submission by running your classes with our version of this
 * file, so any changes you make will not be preserved during grading. Your
 * implementation must work with the provided code. <br><br>
 * 
 * This assignment assumes a simplified elevator system in which each floor only 
 * has a single button (not 'going-up' and 'going-down' buttons), and for testing 
 * purposes, each floor starts with only has a single person on it. Elevators
 * can only carry one person at a time. We do not expect you to implement the most
 * efficient solution, but your system should respond immediately to requests, and
 * every request must be fulfilled. 
 * 
 * @author Maxwell Patek
 * @version 1.2
 *
 */
public final class Provided {
	
	private static final Random random = new Random();
	private static boolean passed = true;
	/**
	 * Signal that is set to 'true' if the simulation should halt. All threads'
	 * run() methods should respect this signal by returning if set to 'true'.
	 */
	protected static boolean TERMINATE = false;
	
	
	
	
	
	
	// ----- Experimental Parameters ----- \\
	/*
	 * Change these fields to adjust the parameters of the experiment. You might
	 * want to stress test your implementation by setting TEST_LENGTH to 1.
	 */
	/**
	 * Number of floors in the building. Floors are 0-indexed.
	 */
	public static final int FLOORS = 25;
	/**
	 * Number of Elevators. Elevator 'id's go 'A', 'B', etc.
	 */
	public static final int ELEVATORS = 5;
	/**
	 * The number of seconds during which buttons are randomly pressed.
	 * After this time, no buttons will be pressed, and the elevators only
	 * have to clear the queue.
	 */
	public static final int TEST_LENGTH = 30; // seconds
	/**
	 * Essentially the inverse of the simulation speed. A delay of 1000 ms
	 * means that elevator floors will update once a second (1000 ms). 
	 */
	public static final int DELAY = 500; // milliseconds

	
	
	
	
	
	
	// ----- Exception Classes ----- \\
	
	/**
	 * This Exception should be thrown if a busy elevator is hailed. <br><br>
	 * 
	 * Note that it extends Exception, which means it is checked.
	 */
	public static class OccupiedException extends Exception {
		private static final long serialVersionUID = -3388816891645794348L;

		public OccupiedException(String id) {
			super(id);
		}
	}
	/**
	 * A TestFailure will be thrown if an elevator or the elevator control
	 * attempts an illegal operation or fails to meet requirements. <br><br>
	 * 
	 * Note that TestFailure extends RuntimeException, so it is unchecked.
	 */
	public static final class TestFailure extends RuntimeException {
		private static final long serialVersionUID = 7177872724706310332L;

		public TestFailure(String id) {
			super(id);
			TERMINATE = true;
			passed = false;
		}
	}
	
	
	
	
	
	
	
	
	// ----- Abstract Classes ----- \\
	
	/**
	 * An abstract class defining the backbone of any Elevator.
	 * You should extend this class. Make sure to use the provided fields. <br><br>
	 * 
	 * run() is implemented for you, and it will call the methods that you
	 * will implement. <br>
	 * Lock provided
	 * 
	 */
	public static abstract class AbstractElevator implements Runnable {
		
		private static char ID = 'A';
		
		protected ReentrantLock lock = new ReentrantLock();
		protected Thread thread;
		protected AbstractElevatorController control;
		
		private int floor;
		/**
		 * The elevator will move towards this floor, so make sure to set it appropriately
		 * in your concrete methods.
		 */
		protected int targetFloor;
		public final char id;
		/**
		 * The passenger field should be set upon being hailed. Make the passenger
		 * board the elevator when appropriate by calling 'board' on the field.
		 * Make the passenger get off the elevator by calling 'exit', then forget about
		 * the passenger by setting the field to null.
		 */
		protected Person passenger;
		/**
		 * Field to allow the elevator to know whether it has picked up its passenger yet.
		 * The field is used in the provided toString method, which is in turn used in
		 * the printState method, so we recommend that you set this field appropriately.
		 */
		protected boolean carrying;
	
		/**
		 * Note that when extending this class, you will have to wrap this constructor.<br>
		 * Something like this:	
		 * <pre>
		 * 	public Elevator(Provided.AbstractElevatorController c) {
		 *		super(c);
		 *	}
		 * </pre>
		 * Same goes for the other classes you are extending.
		 * 
		 * @param control the controller for this elevator
		 * 
		 */
		public AbstractElevator(AbstractElevatorController control) {
			this.control = control;
			this.targetFloor = this.floor = 0;
			carrying = false;
			passenger = null;
			id = ID;
			ID++;
		}
		
		public final int getFloor() {
			return floor;
		}
		public final boolean isMoving() {
			return floor != targetFloor;
		}
		
		/**
		 * This run() method will:<br>
		 *  1. move the elevator up or down towards the targetFloor. <br>
		 *  2. check if shouldPickUp()<br>
		 *  	if yes, it will call pickUp()<br>
		 *  3. check if shouldOffload()<br>
		 *  	if yes, it will call offload()<br>
		 *  4. repeat
		 */
		@Override
		public final void run() {
			int oldFloor = floor;
			while (!TERMINATE) {
				try {
					lock.lock();
					validateFloor(oldFloor);
					if (id == 'A')
						control.printState();

					if (floor != targetFloor) { // move the elevator 1 floor
						floor += (floor < targetFloor) ? 1 : -1;
					}

					if (shouldPickUp())
						pickUp();
					else if (shouldOffload())
						offload();

					oldFloor = floor;
				} finally {
					lock.unlock();
				}
				delay();
			}
		}
		
		private final void validateFloor(int oldFloor) {
			if (floor != oldFloor) {
				throw new TestFailure("Elevators should "
						+ "only be moved by the provided code. "
						+ "Do not change 'floor'.");
			}
		}
		
		private final void delay() {
			try {
				Thread.sleep(DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public final String toString() {
			return id + "(" + (carrying ? passenger : "") + ")->"+targetFloor;
		}
		
		/**
		 * Should start the elevator moving in the direction of 'floor'.
		 * The Person that pressed the button and hailed the elevator is passed
		 * in as Person 'p' and should be saved in the passenger field.
		 * 
		 * @param floor where the elevator should go
		 * @param p the person that hailed the elevator
		 * @throws OccupiedException if hailed but not available
		 * 
		 */
		public abstract void hail(int floor, Person p) throws OccupiedException;
		
		/**
		 * Construct and start the thread running this elevator. <br>
		 * You must the provided 'thread' field. <br> <br>
		 * 
		 * Note: <br>
		 * We recommend you write this method first, since it will make 
		 * the other methods easier to visualize.
		 */
		public abstract void start();
		
		/**
		 * @return true if the elevator is available to be hailed
		 */
		public abstract boolean isAvailable();
		
		/**
		 * Make sure to call 'p's board method.
		 * Elevator should start moving towards passenger's destination.
		 */
		protected abstract void pickUp();
		
		/**
		 * @return true if the elevator should open its doors and pick up 'passenger'
		 */
		protected abstract boolean shouldPickUp();
		
		/**
		 * Make sure to call the passenger's exit method
		 */
		protected abstract void offload();
		
		/**
		 * @return true if the elevator can offload its passenger
		 */
		protected abstract boolean shouldOffload();
		
	}
	
	/**
	 * An abstract class that defines the backbone of an Elevator Controller.
	 * You should extend this class. <br><br>
	 * 
	 * The run() method, which you will implement, should wait for button presses 
	 * and available elevators, dispatching elevators (by calling AbstractElevator's
	 *  'hail' method) when possible. <br><br>
	 *  
	 *  Lock and Conditions are provided.
	 * 
	 *
	 */
	public static abstract class AbstractElevatorController implements Runnable {

		/**
		 * The elevators in the building.
		 */
		protected Set <AbstractElevator> elevators;
		/**
		 * Holds the floors whose buttons have been pressed.
		 */
		protected Queue <Integer> floorQueue;
		/**
		 * Holds the people who have pressed the buttons.
		 */
		protected Queue <Person> personQueue;
		
		protected Thread thread;
		/**
		 * Used to wait for button presses.
		 */
		protected Condition buttonPressed;
		/**
		 * Used to wait for available elevators
		 */
		protected Condition elevatorFinished;
		protected ReentrantLock lock;
		
		/**
		 * This is private because the ElevatorController implementation shouldn't
		 * need to store all the buttons, the buttons should notify the controller
		 * independently. This field is only included for the printState method, which
		 * you do not need to worry about.
		 */
		private AbstractButton[] buttons = new AbstractButton[FLOORS];
		
		/**
		 * Because this constructor does not take in any parameters, it is considered
		 * the default constructor, so your implementation does not need to wrap it.
		 */
		public AbstractElevatorController() {
			floorQueue = new LinkedBlockingQueue <Integer> ();
			personQueue = new LinkedBlockingQueue <Person> ();
			lock = new ReentrantLock();
			buttonPressed = lock.newCondition();
			elevatorFinished = lock.newCondition();
		}
		
		public final void setElevators(Set <AbstractElevator> elevators) {
			this.elevators = elevators;
		}
		
		public final void start() {
			thread = new Thread(this);
			for (AbstractElevator e : elevators)
				e.start();
			thread.start();
		}
		
		/**
		 * Should be called by the Buttons. It is up for the controller's 
		 * run() method to dequeue the floor and person queues. <br> <br>
		 * 
		 * This method is non-final, so you may override it if you like.
		 * 
		 * @param floor the floor that requested the elevator
		 * @param p the person that requested the elevator
		 */
		public void request(int floor, Person p) {
			floorQueue.add(floor);
			personQueue.add(p);

		}
		
		/**
		 * Used to number the printState outputs.
		 */
		private int count = 0;
		/**
		 * Prints the state of the elevator system for debugging. You do not
		 * need to worry about this code
		 */
		public final void printState() {
			
			@SuppressWarnings("unchecked")
			LinkedList<AbstractElevator>[] array = new LinkedList [FLOORS];
			for (int i = 0; i < FLOORS; i++)
				array[i]  = new LinkedList<AbstractElevator>();
			
			int longest = 0;
			for (AbstractElevator e : elevators) {
				array[e.getFloor()].add(e);
				if (array[e.getFloor()].size() > array[longest].size())
					longest = e.getFloor();
			}
			int size = array[longest].toString().length();
			size = size < 25 ? 25 : size;
			
			int width = String.format("| floor %2s: %7s\t%"+size+"s |\n", 
					0+"", 
					buttons[0], 
					array[0]
				).length();
			String bottom = "+";
			for (int i = 0; i < width +1; i++)
				bottom += "-";
			bottom += "+";
			String top = "+"+count;
			for (int i = 0; i < width +1 - (""+count).length(); i++)
				top += "-";
			top += "+";
			
			//System.out.println(++count);
			System.out.println(top);
			for (int i = FLOORS-1; i >= 0; i--) 
				System.out.printf("| floor %2s: %7s\t%"+size+"s |\n", 
						i+"", 
						buttons[i], 
						array[i]
					);
			System.out.println(bottom + "\n");
			count++;
		}
		
		
		/**
		 * Wait for pressed buttons and available elevators by calling 'await'
		 * on the two corresponding Condition objects. When possible, assign an 
		 * Elevator to a waiting person by calling Elevator's 'hail' method. <br><br>
		 * 
		 * You should not just periodically check for pushed buttons and available
		 * elevators; you must use the conditions' await methods.
		 */
		@Override
		public abstract void run();
		
	}
	
	/**
	 * This abstract class mostly defines the button that is on each floor. 
	 * There is only a single abstract method, 'press()' which is called when
	 * the person on the floor with this button presses the button. <br><br>
	 * 
	 * You should call AbstractElevator's 'request' method.
	 *
	 */
	public static abstract class AbstractButton implements Runnable {
		
		public final int floor;
		private Thread thread;
		protected AbstractElevatorController control;
		private Person p;
		private boolean pressed = false;
		
		/**
		 * Note that when extending this class, you will have to wrap this constructor.<br>
		 * 
		 * Something like this:
		 * <pre>
		 * 	public Button(int floor, Provided.AbstractElevatorController control) {
		 *		super(floor, control);
		 *	}
		 *</pre>
		 *
		 * @param floor the floor the button is on
		 * @param control the controller for this button
		 * 
		 */
		public AbstractButton(int floor, AbstractElevatorController control) {
			this.floor = floor;
			this.control = control;
			control.buttons[floor] = this;
			p = new Provided.Person(floor);
		}
		public final void start() {
			thread = new Thread(this);
			thread.start();
		}

		@Override
		public final void run() {
			try {
				Thread.sleep(Provided.random.nextInt(TEST_LENGTH*1000));
			} catch (InterruptedException e) {
				return;
			}
			press(p);
			pressed = true;
		}
		
		@Override
		public final String toString() {
			return pressed && p.elevator == null ? "waiting" : "";
		}
		
		/**
		 * Called when the button is pressed. Make sure to notify the controller
		 * by calling request() <br><br>
		 * 
		 * The Person parameter is constructed and passed in by the provided code
		 * and is used to verify that your system works, so you must make sure not
		 * to loose the reference to it. It should be passed to the controller through
		 * the request method, and it should then be passed to the Elevator through the
		 * hail method. This way, the Elevators can call 'board' and 'exit' on the 
		 * provided Person objects, thereby passing the tests.
		 * 
		 * @param p, the person that pressed the button.
		 */
		protected abstract void press(Person p);
		
		
	}
	
	
	
	
	
	
	
	
	// ----- The Person Class used for Testing ----- \\
	
	/**
	 * Class used for testing effectiveness of Elevators.
	 * You do not need to worry about this code. However, you do need to call
	 * 'board()', 'exit()', and 'getDestination()' in Elevator. You must
	 * also pass the Person objects from the Button 'press' method to the controller
	 * through the request method, and finally to the elevator through the 'hail' 
	 * method. This is the only way to pass the tests.
	 */
	public static final class Person {
		private int startingFloor;
		private int destination;
		private AbstractElevator elevator;
		private boolean done = false;
		
		
		public Person(int startingFloor) {
			this.startingFloor = startingFloor;
			this.destination = Provided.random.nextInt(FLOORS);
		}
		/**
		 * @return the floor that this person wants to go to.
		 */
		public int getDestination() {
			return destination;
		}
		/**
		 * Call this method to make the person get on the elevator.
		 * 
		 * @param e the Elevator that this Person is boarding.
		 */
		public void board(AbstractElevator e) {
			if (elevator != null) 
				throw new TestFailure("Passenger " + startingFloor + " is Already "
						+ "on an Elevator");
			if (e.getFloor() != startingFloor)
				throw new TestFailure("Cannot Pick Up Passenger " + startingFloor 
						+" from Floor " + e.getFloor() +".");
			elevator = e;
		}
		/**
		 * Call this method to make the person get off the elevator
		 */
		public void exit() {
			//System.out.println(startingFloor +" dropped off at " + elevator.getFloor());
			if (elevator == null) 
				throw new TestFailure("Passenger " + startingFloor + " has not Boarded "
						+ "and Cannot Exit.");
			if (elevator.getFloor() != destination)
				throw new TestFailure("Passenger " + startingFloor + "'s Destination is "
						+destination+". He/She Cannot be Dropped Off at " 
						+ elevator.getFloor() +".");
			done = true;
		}
		@Override
		public String toString() {
			return "" + startingFloor;
		}
	}
	
	
	
	
	
	
	
	// ----- Main Method Testing ----- \\
	
	
	/**
	 * Run this to test your code.<br>
	 * However, we encourage you to write your own tests as well.
	 */
	public static void main(String[] args) {
		// ---- Initialize Test ---- //
		ElevatorController c = new ElevatorController();
		HashSet <AbstractElevator> e = new HashSet<AbstractElevator>();
		for (int i = 0; i < ELEVATORS; i++)
			e.add(new Elevator(c));
		HashSet <AbstractButton> b = new HashSet<AbstractButton>();
		for (int i = 0; i < FLOORS; i++)
			b.add(new Button(i, c));
		c.setElevators(e);


		// ---- Start Test ---- //
		c.start(); // controller starts the elevators
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		if (!c.thread.isAlive()) // Test that Controller started properly
			throw new TestFailure("Controller Terminated Early.");
		if (!c.thread.getState().equals(Thread.State.WAITING))
			throw new TestFailure("Controller Needs to Call Await on the Conditions.");
		for (AbstractButton i : b)
			i.start();

		// ---- Sleep While Buttons are Pressing ---- //
		try {
			Thread.sleep(TEST_LENGTH*1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		

		// ---- Check if Test is Complete ---- //
		while (!TERMINATE) {
			try {
				Thread.sleep(DELAY*4); // check every fourth cycle
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			boolean stillGoing = false;
			for (AbstractElevator i : e) 
				if (i.isMoving()) // if an elevator is moving, stop
					stillGoing = true;
			if (!stillGoing) {
				for (AbstractButton i: b)
					if (!i.p.done) // if stopping, everyone should be delivered
						throw new TestFailure("Did Not Drop Everyone Off!");
				if (!c.thread.getState().equals(Thread.State.WAITING))
					throw new TestFailure("Test Ended with Controller in "
							+ "Non-Awaiting State");
				TERMINATE = true; // Tell all threads to stop
				c.thread.interrupt(); 
			}
		}
		if (passed)
			System.out.println("All tests passed!");
	}
}