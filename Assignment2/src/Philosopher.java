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

    private Random randomGenerator = new Random();

    private int numberOfEatingTurns = 0;
    private int numberOfThinkingTurns = 0;
    private int numberOfHungryTurns = 0;

    private double thinkingTime = 0;
    private double eatingTime = 0;
    private double hungryTime = 0;
    private boolean canEat;

    public Philosopher(int id, Chopstick leftChopstick, Chopstick rightChopstick, int seed, boolean debug) {
        this.id = id;
        this.leftChopstick = leftChopstick;
        this.rightChopstick = rightChopstick;
        this.running = true;
        this.DEBUG = debug;
        currentState = State.Thinking;
        canEat = false;

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
        return hungryTime / numberOfHungryTurns;
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


    /**
     *
     */
    private void think() {
        numberOfThinkingTurns++;
        currentState = State.Thinking;
        long waitTime = randomGenerator.nextInt(1000);
        printState(currentState, waitTime);
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
        }
        thinkingTime += waitTime;
    }


    /**
     * Sets current state to hungry and increments hungryturns.
     */
    private void hungry() {
        numberOfHungryTurns++;
        currentState = State.Hungry;
        if(DEBUG){
            System.out.println("Philosopher " + getId() + " is " + currentState.name());
        }
    }

    /**
     * Increments the number of turns eating and sleeps for a specified amount of time.
     */
    private void eat() {
        numberOfEatingTurns++;
        currentState = State.Eating;
        long waitTime = randomGenerator.nextInt(1000);
        printState(currentState, waitTime);
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
        }
        eatingTime += waitTime;
    }

    /**
     * Prints the current state
     * @param state
     * @param time
     */
    public void printState(State state, long time) {
        if (DEBUG) {
            System.out.println("Philosopher " + getId() + " is " + state.name() + " for " + time);
        }
    }


    /**
     *
     *
     */
    @Override
    public void run() {
        while (running) {
            think();
            long startHungry = System.currentTimeMillis();
            hungry();
            this.hungryTime += System.currentTimeMillis()-startHungry;
            getChopSticks();
        }
    }

    /**
     * Fetches the chopsticks by continuously picking up the first, then trying for the second.
     * If the second is not available it returns the left and tries again.
     */
    private void getChopSticks() {
        while (!canEat) {
            if (leftChopstick.getLock().tryLock()) {

                if (rightChopstick.getLock().tryLock()) {
                    canEat = true;

                    if(DEBUG) {
                        System.out.println("Philosopher " + getId() + " picked up chopstick " + leftChopstick.getId());
                        System.out.println("Philosopher " + getId() + " picked up chopstick " + rightChopstick.getId());
                    }
                    eat();
                    // Unlock right and left after eating.
                    rightChopstick.getLock().unlock();
                    leftChopstick.getLock().unlock();
                } else {
                    // Unlock left if right not available.
                    leftChopstick.getLock().unlock();
                }
                //try again.
            }
        }
        canEat = false;
        // back to thinking
    }
}
