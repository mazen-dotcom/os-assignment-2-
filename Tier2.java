
import java.util.ArrayList;
import java.util.List;

public class Tier2 {

    public static class Result {
        public final List<SJF.Segment> timeline = new ArrayList<>();
        public final List<String> log = new ArrayList<>();
        public int endTime;
    }

    // Old method kept for compatibility.
    public static Result run(
            List<Process> demoted,
            int cs,
            PriorityScheduler.AgingPolicy aging) {

        return run(
                demoted,
                Tier1RoundRobin.endTime,
                Tier1RoundRobin.lastProcess,
                cs,
                aging,
                new ArrayList<>()
        );
    }

    // New method with snapshot recording.
    public static Result run(
            List<Process> demoted,
            int startTime,
            Process lastRan,
            int cs,
            PriorityScheduler.AgingPolicy aging,
            List<ACSOutput.Snapshot> snapshots) {

        if (cs < 0) {
            throw new IllegalArgumentException(
                    "Context switch time cannot be negative."
            );
        }

        PriorityScheduler scheduler = new PriorityScheduler();

        scheduler.setAgingPolicy(aging);

        // Add all demoted processes to Tier 2.
        for (Process p : demoted) {
            scheduler.enqueue(p);
        }

        Result result = new Result();

        int time = startTime;

        Process last = lastRan;

        while (!scheduler.isEmpty()) {

            // Apply aging and select the highest-priority process.
            Process p = scheduler.selectNext(time);

            result.log.add(
                    "T=" + time
                    + " selected "
                    + PriorityScheduler.describe(p)
            );

            // Context switching.
            if (last != null && last != p && cs > 0) {

                for (int i = 0; i < cs; i++) {

                    ACSOutput.record(
                            snapshots,
                            time,
                            "CS",
                            new ArrayList<>(),
                            scheduler.waiting()
                    );

                    result.timeline.add(
                            new SJF.Segment(
                                    "CS", time, time + 1
                            )
                    );

                    time++;
                }
            }

            if (p.firstStartTime == -1) {
                p.firstStartTime = time;
            }

            // Non-preemptive execution:
            // The process runs until it finishes.
            int executionTime = p.remainingBurst;

            for (int i = 0; i < executionTime; i++) {

                ACSOutput.record(
                        snapshots,
                        time,
                        p.id,
                        new ArrayList<>(),
                        scheduler.waiting()
                );

                time++;
            }

            result.timeline.add(
                    new SJF.Segment(
                            p.id,
                            time - executionTime,
                            time
                    )
            );

            p.remainingBurst = 0;

            p.completionTime = time;

            p.calculateMetrics();

            result.log.add(
                    "T=" + time + " " + p.id + " finished"
            );

            last = p;
        }

        result.log.add(
                "T=" + time + " Tier 1: [Empty] Tier 2: [Empty]"
        );

        result.endTime = time;

        return result;
    }
}
