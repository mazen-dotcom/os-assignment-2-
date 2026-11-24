
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ACSOutput {

    // One snapshot represents the system state at a time step.
    public static class Snapshot {
        public final int time;
        public final String running;
        public final String tier1;
        public final String tier2;

        public Snapshot(int time, String running,
                        String tier1, String tier2) {
            this.time = time;
            this.running = running;
            this.tier1 = tier1;
            this.tier2 = tier2;
        }
    }

    // Stores all final ACS results.
    public static class Result {

        public final List<Process> processes;
        public final List<Snapshot> snapshots;
        public final int completionTime;

        public final double averageWaitingTime;
        public final double averageTurnaroundTime;
        public final double averageResponseTime;

        public Result(List<Process> processes,
                      List<Snapshot> snapshots,
                      int completionTime) {

            this.processes = processes;
            this.snapshots = snapshots;
            this.completionTime = completionTime;

            double totalWT = 0;
            double totalTAT = 0;
            double totalRT = 0;

            for (Process p : processes) {
                totalWT += p.waitingTime;
                totalTAT += p.turnaroundTime;
                totalRT += p.responseTime;
            }

            int n = processes.size();

            averageWaitingTime =
                    n == 0 ? 0 : totalWT / n;

            averageTurnaroundTime =
                    n == 0 ? 0 : totalTAT / n;

            averageResponseTime =
                    n == 0 ? 0 : totalRT / n;
        }
    }

    // Creates a readable description of a process.
    public static String describe(Process p) {

        return p.id
                + " (remaining: " + p.remainingBurst
                + ", Priority: " + p.effectivePriority + ")";
    }

    // Converts a process queue into readable text.
    public static String describeQueue(List<Process> queue) {

        if (queue.isEmpty()) {
            return "[Empty]";
        }

        StringBuilder sb = new StringBuilder("[");

        for (int i = 0; i < queue.size(); i++) {

            if (i > 0) {
                sb.append(", ");
            }

            sb.append(describe(queue.get(i)));
        }

        return sb.append("]").toString();
    }

    // Records the state of both tiers at a specific time.
    public static void record(
            List<Snapshot> snapshots,
            int time,
            String running,
            List<Process> tier1,
            List<Process> tier2) {

        snapshots.add(new Snapshot(
                time,
                running,
                describeQueue(tier1),
                describeQueue(tier2)
        ));
    }

    // Prints the Demotion Matrix.
    public static void printMatrix(Result result) {

        System.out.println("\n========== ACS DEMOTION MATRIX ==========");

        System.out.printf(
                "%-8s %-12s %-45s %-45s%n",
                "Time", "Running", "Tier 1", "Tier 2"
        );

        for (Snapshot s : result.snapshots) {

            System.out.printf(
                    "%-8d %-12s %-45s %-45s%n",
                    s.time,
                    s.running,
                    s.tier1,
                    s.tier2
            );
        }
    }

    // Prints the scheduling metrics for every process.
    public static void printMetrics(Result result) {

        System.out.println("\n========== SCHEDULING ANALYSIS ==========");

        List<Process> sorted = new ArrayList<>(result.processes);

        sorted.sort(Comparator.comparing(p -> p.id));

        System.out.printf(
                "%-10s %-10s %-10s %-10s %-10s %-10s%n",
                "Process", "Arrival", "Finish",
                "WT", "TAT", "RT"
        );

        for (Process p : sorted) {

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

        System.out.println("-----------------------------------------------");

        System.out.printf(
                "Average Waiting Time: %.2f%n",
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

    // Prints the Gantt chart using consecutive snapshots.
    public static void printGanttChart(Result result) {

        System.out.println("\n========== ACS GANTT CHART ==========");

        List<Snapshot> rows = result.snapshots;

        if (rows.isEmpty()) {
            System.out.println("No execution.");
            return;
        }

        int start = rows.get(0).time;
        String current = rows.get(0).running;

        for (int i = 1; i < rows.size(); i++) {

            Snapshot s = rows.get(i);

            if (!s.running.equals(current)) {

                System.out.println(
                        current + " : " + start + " -> " + s.time
                );

                current = s.running;
                start = s.time;
            }
        }

        System.out.println(
                current + " : " + start + " -> "
                + result.completionTime
        );
    }

    // Prints all ACS outputs.
    public static void printAll(Result result) {

        printGanttChart(result);
        printMatrix(result);
        printMetrics(result);
    }
}
