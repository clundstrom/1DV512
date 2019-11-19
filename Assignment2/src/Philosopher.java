/*
 * File:	Philosopher.java
 * Course: 	Operating Systems
 * Code: 	1DV512
 * Author: 	Suejb Memeti (modified by Kostiantyn Kucher)
 * Date: 	November 2019
 */

import java.util.Random;

public class Philosopher implements Runnable {

    /*
     * Controls whether logs should be shown on the console or not.
     * Logs should print events such as: state of the philosopher, and state of the chopstick
     * 		for example: philosopher # is eating;
     * 		philosopher # picked up the left chopstick (chopstick #)
     */
    public boolean DEBUG = true;

    private int id;

    private final Chopstick leftChopstick;
    private final Chopstick rightChopstick;
    private volatile boolean running;
    private State currentState;
    boolean hasLeftChopstick;
    boolean hasRightChopstick;

    private Random randomGenerator = new Random();

    private int numberOfEatingTurns = 0;
    private int numberOfThinkingTurns = 0;
    private int numberOfHungryTurns = 0;

    private double thinkingTime = 0;
    private double eatingTime = 0;
    private double hungryTime = 0;

    public Philosopher(int id, Chopstick leftChopstick, Chopstick rightChopstick, int seed, boolean debug) {
        this.id = id;
        this.leftChopstick = leftChopstick;
        this.rightChopstick = rightChopstick;
        this.running = true;
        this.DEBUG = debug;
        currentState = State.Thinking;
        hasLeftChopstick = false;
        hasRightChopstick = false;

        /*
         * set the seed for this philosopher. To differentiate the seed from the other philosophers, we add the philosopher id to the seed.
         * the seed makes sure that the random numbers are the same every time the application is executed
         * the random number is not the same between multiple calls within the same program execution

         * NOTE
         * In order to get the same average values use the seed 100, and set the id of the philosopher starting from 0 to 4 (0,1,2,3,4).
         * Each philosopher sets the seed to the random number generator as seed+id.
         * The seed for each philosopher is as follows:
         * 	 	P0.seed = 100 + P0.id = 100 + 0 = 100
         * 		P1.seed = 100 + P1.id = 100 + 1 = 101
         * 		P2.seed = 100 + P2.id = 100 + 2 = 102
         * 		P3.seed = 100 + P3.id = 100 + 3 = 103
         * 		P4.seed = 100 + P4.id = 100 + 4 = 104
         * Therefore, if the ids of the philosophers are not 0,1,2,3,4 then different random numbers will be generated.
         */

        randomGenerator.setSeed(id + seed);
    }

    public int getId() {
        return id;
    }

    public double getAverageThinkingTime() {

        return thinkingTime / numberOfThinkingTurns;
    }

    public double getAverageEatingTime() {
        return eatingTime / numberOfEatingTurns;
    }

    public double getAverageHungryTime() {
        return  hungryTime / numberOfHungryTurns;
    }

    public int getNumberOfThinkingTurns() {
        return numberOfThinkingTurns;
    }

    public int getNumberOfEatingTurns() {
        return numberOfEatingTurns;
    }

    public int getNumberOfHungryTurns() {
        return numberOfHungryTurns;
    }

    public double getTotalThinkingTime() {
        return thinkingTime;
    }

    public double getTotalEatingTime() {
        return eatingTime;
    }

    public double getTotalHungryTime() {
        return hungryTime;
    }


    public void interrupt() {
        running = false;
    }


    private void think() {
        numberOfThinkingTurns++;
        currentState = State.Thinking;
        long waitTime = randomGenerator.nextInt(1000);
        printState(currentState, waitTime);
        thinkingTime += waitTime;
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void hungry() {
        numberOfHungryTurns++;
        currentState = State.Hungry;
        System.out.println("Philosopher " + getId() + " is " + currentState.name());
    }

    private void eat() {
        pickupChopsticks();
        numberOfEatingTurns++;
        currentState = State.Eating;
        long waitTime = randomGenerator.nextInt(1000);
        eatingTime += waitTime;
        printState(currentState, waitTime);
        releaseChopsticks();

    }

    public void printState(State state, long time){
        if (DEBUG) {
            System.out.println("Philosopher " + getId() + " is " + state.name() + " for " + time);
        }
    }


    @Override
    public void run() {
        while (running) {
            think();
            long hungry = System.currentTimeMillis();
            hungry();
            eat();
            hungryTime += System.currentTimeMillis() - hungry;
        }

        currentState = State.Finished;
        printState(State.Finished, 0);
        /* TODO
         * (Initialize some additional variables, if necessary)
         *
         * Think,
         * Get hungry,
         * Pick up the left and then the right chopstick,
         * Eat,
         * Release the chopsticks.
         * ^^^ Repeat until the thread is interrupted
         *
         * Increment the thinking/hungry/eating turns counter *when each turn starts*.
         *
         * Update the duration of each turn *after the turn is completely finished*.
         * Keep track of total hungry turn durations by getting the current timestamp with System.currentTimeMillis()
         * when the turn starts, then another System.currentTimeMillis() after the turn has finished, and subtracting these.
         * For thinking and eating turns, use the duration generated with randomGenerator.nextInt(1000).
         *
         * If DEBUG is true, print the log messages for each event.
         * Additionally, you might want to print a message such as "philosopher X has finished" when the thread terminates
         * (for debugging purposes).
         *
         *
         * Add comprehensive comments to explain your implementation, including deadlock prevention/detection.
         * You should start with a straightforward implementation, but you will eventually have to make it more sophisticated
         * w.r.t the order (and conditions) of the actions and the threads synchronization in order to pass the tests with the expected results!
         */

    }

    private void releaseChopsticks() {
        synchronized (leftChopstick.getLock()) {
            if (hasLeftChopstick) {
                leftChopstick.getLock().unlock();
                System.out.println("Philosopher " + getId() + " released chopstick " + leftChopstick.getId());
                hasLeftChopstick = false;
            }


        }
        synchronized (rightChopstick.getLock()) {
            if (hasRightChopstick) {
                rightChopstick.getLock().unlock();
                System.out.println("Philosopher " + getId() + " released chopstick " + rightChopstick.getId());
                hasRightChopstick = false;
            }
        }

    }

    private void pickupChopsticks() {

        synchronized (leftChopstick.getLock()){
            // Only one thread can run this code
            leftChopstick.getLock().lock();
            System.out.println("Philosopher " + getId() + " picked up chopstick " + leftChopstick.getId());
            hasLeftChopstick = true;


            rightChopstick.getLock().lock();
            System.out.println("Philosopher " + getId() + " picked up chopstick " + rightChopstick.getId());
            hasRightChopstick = true;
        }

    }
}
