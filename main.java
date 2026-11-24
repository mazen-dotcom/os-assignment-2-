
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        List<Process> processes = new ArrayList<>();

        // Test Case 1
        processes.add(new Process("P1", 0, 4, 2));
        processes.add(new Process("P2", 1, 7, 3));
        processes.add(new Process("P3", 3, 2, 1));

        int quantum = 3;
        int cs = 0;

        // Run SJF.
        System.out.println("\n\n******** PREEMPTIVE SJF ********");

        SJF.Result sjfResult = SJF.run(processes, cs);

        printSJF(sjfResult);

        // Run ACS.
        System.out.println("\n\n******** ADAPTIVE CONTAINER SCHEDULER ********");

        ACSOutput.Result acsResult =
                ACS.run(processes, quantum, cs);

        ACSOutput.printAll(acsResult);
    }

    public static void printSJF(SJF.Result result) {

        System.out.println("\nGantt Chart:");

        for (SJF.Segment s : result.timeline) {

            System.out.println(
                    s.label + " : " +
                    s.startTime + " -> " + s.endTime
            );
        }

        System.out.println("\nScheduling Analysis:");

        System.out.printf(
                "%-10s %-10s %-10s %-10s %-10s %-10s%n",
                "Process", "Arrival", "Finish",
                "WT", "TAT", "RT"
        );

        for (Process p : result.processes) {

            System.out.printf(
                    "%-10s %-10d %-10d %-10d %-10d %-10d%n",
                    p.id,
                    p.arrivalTime,
                    p.completionTime,
                    p.waitingTime,
                    p.turnaroundTime,
                    p.responseTime
            );
        }

        System.out.printf(
                "\nAverage Waiting Time: %.2f%n",
                result.averageWaitingTime
        );

        System.out.printf(
                "Average Turnaround Time: %.2f%n",
                result.averageTurnaroundTime
        );

        System.out.printf(
                "Average Response Time: %.2f%n",
                result.averageResponseTime
        );
    }
}
