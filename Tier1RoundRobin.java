import java.util.*;

public class Tier1RoundRobin {

    // Filled in by run(): the time Tier 1 finished and the last process it ran.
    // Tier 2 starts from here.
    static int endTime = 0;
    static Process lastProcess = null;

    // Runs Tier 1 (Round Robin). Finished processes get completionTime, firstStartTime
    // and their metrics calculated. Returns the processes that used a full quantum
    // and must move to Tier 2.
    static List<Process> run(List<Process> processes, int quantum, int cs) {
        List<Process> sorted = new ArrayList<>(processes);
        sorted.sort(Comparator.comparingInt(p -> p.arrivalTime));

        Queue<Process> queue = new LinkedList<>();
        List<Process> demoted = new ArrayList<>();
        int time = 0;
        int next = 0;
        Process last = null;

        while (true) {
            while (next < sorted.size() && sorted.get(next).arrivalTime <= time) {
                queue.add(sorted.get(next));
                next++;
            }

            if (queue.isEmpty()) {
                if (next >= sorted.size()) break;
                time = sorted.get(next).arrivalTime;
                continue;
            }

            Process p = queue.poll();

            if (last != null) {
                time += cs;
            }
            if (p.firstStartTime == -1) {
                p.firstStartTime = time;
            }

            int slice = Math.min(quantum, p.remainingBurst);
            time += slice;
            p.remainingBurst -= slice;

            if (p.remainingBurst == 0) {
                p.completionTime = time;
                p.calculateMetrics();
            } else {
                p.waitingSince = time;   // moment of demotion = start of waiting in Tier 2
                demoted.add(p);
            }
            last = p;
        }

        endTime = time;
        lastProcess = last;
        return demoted;
    }
}