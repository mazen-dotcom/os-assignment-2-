import java.util.ArrayList;
import java.util.List;

public class Tier2 {

    public static class Result {
        public final List<SJF.Segment> timeline = new ArrayList<>();
        public final List<String> log = new ArrayList<>();
        public int endTime;
    }

    public static Result run(List<Process> demoted, int cs, PriorityScheduler.AgingPolicy aging) {
        return run(demoted, Tier1RoundRobin.endTime, Tier1RoundRobin.lastProcess, cs, aging);
    }

    /**
     * @param demoted   processes coming from Tier 1 (remainingBurst > 0, waitingSince set)
     * @param startTime time at which Tier 1 finished
     * @param lastRan   last process that held the CPU in Tier 1 (null if none)
     * @param cs        context-switch time
     * @param aging     Tier 3 aging hook (null = no aging)
     */
    public static Result run(List<Process> demoted, int startTime, Process lastRan,
                             int cs, PriorityScheduler.AgingPolicy aging) {
        if (cs < 0) {
            throw new IllegalArgumentException("Context switch time cannot be negative.");
        }

        PriorityScheduler scheduler = new PriorityScheduler();
        scheduler.setAgingPolicy(aging);
        for (Process p : demoted) {
            scheduler.enqueue(p);
        }

        Result result = new Result();
        int time = startTime;
        Process last = lastRan;

        while (!scheduler.isEmpty()) {
            Process p = scheduler.selectNext(time);   // aging is applied inside
            result.log.add("T=" + time + "  selected " + PriorityScheduler.describe(p)
                    + "  | Tier 2 after pick: " + scheduler.describeQueue());

            // Load the chosen process: pay the context switch if the CPU changes hands.
            if (last != null && last != p && cs > 0) {
                addSegment(result.timeline, "CS", time, time + cs);
                result.log.add("T=" + time + "  context switch " + last.id + " -> " + p.id);
                time += cs;
            }

            if (p.firstStartTime == -1) {
                p.firstStartTime = time;
            }

            addSegment(result.timeline, p.id, time, time + p.remainingBurst);
            time += p.remainingBurst;
            p.remainingBurst = 0;
            p.completionTime = time;
            p.calculateMetrics();
            result.log.add("T=" + time + "  " + p.id + " finished");

            last = p;
        }

        result.log.add("T=" + time + "  Tier 1: [Empty]  Tier 2: [Empty]");
        result.endTime = time;
        return result;
    }

    private static void addSegment(List<SJF.Segment> timeline, String label, int start, int end) {
        if (start == end) {
            return;
        }
        timeline.add(new SJF.Segment(label, start, end));
    }
}
