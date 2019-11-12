/*
 * File:	Process.java
 * Course: 	Operating Systems
 * Code: 	1DV512
 * Author: 	Suejb Memeti
 * Date: 	November, 2018
 */

import java.util.ArrayList;
import java.util.Comparator;

public class FCFS {

    // The list of processes to be scheduled
    public ArrayList<Process> processes;

    // Class constructor
    public FCFS(ArrayList<Process> processes) {
        this.processes = processes;
    }

    public void run() {

        this.processes.sort(Comparator.comparingInt(Process::getArrivalTime));

        int previousBurst = 0;
        int previousCompleted = 0;

        for (int i = 0; i < processes.size(); i++) {

            // SET COMPLETED TIME OF FIRST PROCESS
            Process current = processes.get(i);

            if (current.getArrivalTime() > previousBurst && current.getArrivalTime() < previousCompleted) {
                current.setCompletedTime(previousCompleted + current.getBurstTime());
            } else if (current.getArrivalTime() > previousBurst) {
                current.setCompletedTime(current.getArrivalTime() + current.getBurstTime());
            } else {
                current.setCompletedTime(previousBurst + current.getBurstTime());
            }

            current.setTurnaroundTime(current.getCompletedTime() - current.getArrivalTime());

            current.setWaitingTime(current.getTurnaroundTime() - current.getBurstTime());

            previousBurst += current.getBurstTime();
            previousCompleted = current.getCompletedTime();
        }


    }

    public void printTable() {
        System.out.println("------------------------------------");
        System.out.println("PID\tAT\tBT\tCT\tTAT\tWT");
        for (Process p : processes) {
            System.out.println(p.processId + "\t" + p.getArrivalTime() + "\t" + p.getBurstTime() + "\t" + p.getCompletedTime() + "\t" + p.getTurnaroundTime() + "\t" + p.getWaitingTime());
        }
        System.out.println("------------------------------------");
    }

    public void printGanttChart() {

        int totalSpace = 0;
        int[] space = calcProcessSpace();
        totalSpace += calcCPUWaitTime();
        totalSpace += calcTotalSpace(space);


        printSymbolAmount(totalSpace, '='); // TOP BORDER

        System.out.println();

        for (int i = 0; i < processes.size(); i++) {
            /////////////////////////////////////////////////////////////
            ///// START OF CENTER BLOCK

            if (i == 0) {
                printWaitSymbol(processes.get(i), null, '*');
            } else {
                printWaitSymbol(processes.get(i), processes.get(i - 1), '*');
            }

            System.out.print("|");

            printSymbolAmount((space[i]), ' '); // SPACE BETWEEN PROCESS ID

            System.out.print("P" + processes.get(i).getProcessId());

            printSymbolAmount((space[i]), ' '); // SPACE BETWEEN PROCESS ID

            System.out.print("|");
            ////// END OF CENTER BLOCK
            /////////////////////////////////////////////////////////////
        }

        System.out.println();

        printSymbolAmount(totalSpace, '='); // BOTTOM BORDER
        System.out.println();

        //////////////////////////////////////////////////////////
        // TIME BLOCK/////////////
        Process previous = null;
        int end = 0;
        int previousEnd = 0;
        for (int i = 0; i < processes.size(); i++) {

            printWaitSymbol(processes.get(i), previous, ' ');
            int start = calcProcessStart(previous, processes.get(i));
            end = processes.get(i).getCompletedTime();


            if (previousEnd == start && previousEnd != 0) {
                printSymbolAmount((space[i] * 2) + 1, ' ');
                System.out.print(end);
            } else {
                System.out.print(start);
                printSymbolAmount((space[i] * 2) + 1, ' ');
                System.out.print(end);
            }

            previous = processes.get(i);
            previousEnd = end;
        }

        /// END OF TIME BLOCK
        //////////////////////////////////////////////////////////
        System.out.println("\n");
    }


    /**
     * Prints specified symbol if there is wait-time in between processes.
     * @param
     */
    private void printWaitSymbol(Process current, Process previousProcess, char symbol) {
        if (previousProcess == null) {
            return; // No wait time if no previous process.
        }

        if (current.getArrivalTime() > previousProcess.getCompletedTime()) {
            int cpuWait = current.getArrivalTime() - previousProcess.getCompletedTime();
            for (int i = 0; i < cpuWait; i++) {
                System.out.print(symbol);
            }
        }
    }


    /**
     * Prints symbol x, y times.
     * @param amount
     * @param symbol
     */
    private void printSymbolAmount(int amount, char symbol) {
        for (int i = 0; i < amount; i++) {
            System.out.print(symbol);
        }
    }

    /**
     * @return Space needed for all the processes.
     */
    private int[] calcProcessSpace() {
        Process previous = null;

        int[] space = new int[processes.size()];

        for (int i = 0; i < processes.size(); i++) {
            if (previous != null) {
                space[i] = processes.get(i).getCompletedTime() - previous.getCompletedTime();
            } else {
                space[i] = processes.get(i).getCompletedTime();
            }
            previous = processes.get(i);
        }

        return space;
    }


    /**
     * Calculates total space needed for the chart.
     * @param additionalSpace Takes previously calculated process spaces and accounts for that.
     * @return Total space.
     */
    private int calcTotalSpace(int[] additionalSpace) {
        int totalSpace = 0;
        for (int i = 0; i < additionalSpace.length; i++) {
            totalSpace += ((additionalSpace[i] * 2) + 4); // magic numbers are for characters not accounted for in process body.
        }
        return totalSpace;
    }

    /**
     * @return Amount of Time/Spaces the CPU has to wait in total.
     */
    private int calcCPUWaitTime() {
        Process previous = null;
        int cpuWait = 0;

        for (Process current : processes) {

            if (previous != null) {
                if (current.getArrivalTime() > previous.getCompletedTime()) {
                    cpuWait += current.getArrivalTime() - previous.getCompletedTime();
                }
            }

            previous = current;
        }
        return cpuWait;
    }


    // AT <= PREVIOUS COMPLETED -> START = Previous completed.
    // AT > PREVIOUS COMPLETED -> START = AT

    /**
     * Calculates the starting time of a process which will differ depending on arrival time
     * and previously completed Processes.
     *
     * @param first  Process for comparison.
     * @param second Process for comparison.
     *               <p>
     *               AT <= PREVIOUS COMPLETED -> START = Previous completed.
     *               AT > PREVIOUS COMPLETED -> START = AT
     * @return
     */
    private int calcProcessStart(Process first, Process second) {
        if (first == null && second.getArrivalTime() > 0) {
            return second.getArrivalTime();
        }
        if (first == null) return 0;

        if (second.getArrivalTime() <= first.getCompletedTime()) {
            return first.getCompletedTime();
        } else if (second.getArrivalTime() > first.getCompletedTime()) {
            return second.getArrivalTime();
        }

        return 0;
    }
}
