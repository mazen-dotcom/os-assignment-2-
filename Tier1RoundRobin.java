
import java.util.*;

public class Tier1RoundRobin {

    static int endTime = 0;
    static Process lastProcess = null;

    // Result of Tier 1.
    public static class RunResult {
        public final List<Process> demoted;
        public final int endTime;
        public final Process lastProcess;

        public RunResult(List<Process> demoted,
                         int endTime,
                         Process lastProcess) {

            this.demoted = demoted;
            this.endTime = endTime;
            this.lastProcess = lastProcess;
        }
    }

    // Old method kept for compatibility.
    static List<Process> run(
            List<Process> processes, int quantum, int cs) {

        return runWithOutput(
                processes, quantum, cs,
                new ArrayList<>()
        ).demoted;
    }

    // New method with snapshot recording.
    public static RunResult runWithOutput(
            List<Process> processes,
            int quantum,
            int cs,
            List<ACSOutput.Snapshot> snapshots) {

        if (quantum <= 0 || cs < 0) {
            throw new IllegalArgumentException(
                    "Quantum must be positive and CS cannot be negative."
            );
        }

        List<Process> sorted = new ArrayList<>(processes);

        sorted.sort(Comparator.comparingInt(p -> p.arrivalTime));

        Queue<Process> queue = new LinkedList<>();

        List<Process> demoted = new ArrayList<>();

        int time = 0;
        int next = 0;

        Process last = null;

        while (true) {

            // Add all processes that have arrived.
            while (next < sorted.size()
                    && sorted.get(next).arrivalTime <= time) {

                queue.add(sorted.get(next));
                next++;
            }

            // Handle CPU idle time.
            if (queue.isEmpty()) {

                if (next >= sorted.size()) {
                    break;
                }

                last = null;

                int nextArrival = sorted.get(next).arrivalTime;

                while (time < nextArrival) {

                    ACSOutput.record(
                            snapshots, time, "IDLE",
                            new ArrayList<>(queue), demoted
                    );

                    time++;
                }

                continue;
            }

            // Select the next process.
            Process p = queue.poll();

            // Context switching only when changing processes.
            if (last != null && last != p) {

                for (int i = 0; i < cs; i++) {

                    ACSOutput.record(
                            snapshots, time, "CS",
                            new ArrayList<>(queue), demoted
                    );

                    time++;

                    // New arrivals during CS enter Tier 1.
                    while (next < sorted.size()
                            && sorted.get(next).arrivalTime <= time) {

                        queue.add(sorted.get(next));
                        next++;
                    }
                }
            }

            // Set first response time.
            if (p.firstStartTime == -1) {
                p.firstStartTime = time;
            }

            // Run for at most one quantum.
            int slice = Math.min(quantum, p.remainingBurst);

            for (int i = 0; i < slice; i++) {

                // Save the current system state.
                ACSOutput.record(
                        snapshots, time, p.id,
                        new ArrayList<>(queue), demoted
                );

                p.remainingBurst--;
                time++;

                // Add newly arrived processes.
                while (next < sorted.size()
                        && sorted.get(next).arrivalTime <= time) {

                    queue.add(sorted.get(next));
                    next++;
                }

                // Process completed.
                if (p.remainingBurst == 0) {

                    p.completionTime = time;
                    p.calculateMetrics();

                    break;
                }
            }

            // Process did not finish within its quantum.
            if (p.remainingBurst > 0) {

                p.waitingSince = time;

                demoted.add(p);
            }

            last = p;
        }

        endTime = time;
        lastProcess = last;

        return new RunResult(demoted, time, last);
    }
}
