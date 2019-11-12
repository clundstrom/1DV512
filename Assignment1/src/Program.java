import java.util.ArrayList;


/**
 * @author: Christoffer Lundstrom
 * @date: 09/11/2019
 * <p>
 * Description: Test class for the Gant & Table printing.
 */
public class Program {


    static ArrayList<Process> listOfProcesses;

    public static void main(String[] args) {
        listOfProcesses = new ArrayList<>();

        listOfProcesses.add(new Process(1, 0, 18));
        listOfProcesses.add(new Process(2, 3, 2));
        listOfProcesses.add(new Process(3, 25, 5));
        listOfProcesses.add(new Process(4, 29, 2));
        listOfProcesses.add(new Process(5, 33, 7));


//        listOfProcesses.add(new Process(1, 0, 2));
//        listOfProcesses.add(new Process(2, 3, 1));
//        listOfProcesses.add(new Process(3, 5, 6));

        FCFS f = new FCFS(listOfProcesses);

        f.run();

        f.printTable();
        f.printGanttChart();

    }


}
