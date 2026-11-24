
import java.util.ArrayList;
import java.util.List;

public class ACS {

    public static ACSOutput.Result run(
            List<Process> inputProcesses,
            int quantum,
            int cs) {

        if (inputProcesses == null) {
            throw new IllegalArgumentException(
                    "Process list cannot be null."
            );
        }

        if (quantum <= 0 || cs < 0) {
            throw new IllegalArgumentException(
                    "Invalid Quantum or Context Switch."
            );
        }

        // Work on copies to preserve original input.
        List<Process> processes = new ArrayList<>();

        for (Process p : inputProcesses) {
            processes.add(p.copy());
        }

        // Stores all system snapshots.
        List<ACSOutput.Snapshot> snapshots = new ArrayList<>();

        // Run Tier 1: Round Robin.
        Tier1RoundRobin.RunResult tier1Result =
                Tier1RoundRobin.runWithOutput(
                        processes,
                        quantum,
                        cs,
                        snapshots
                );

        // Run Tier 2: Non-Preemptive Priority + Aging.
        Tier2.Result tier2Result =
                Tier2.run(
                        tier1Result.demoted,
                        tier1Result.endTime,
                        tier1Result.lastProcess,
                        cs,
                        new Aging(),
                        snapshots
                );

        // Final completion time.
        int completionTime = tier2Result.endTime;

        // Build the final result.
        return new ACSOutput.Result(
                processes,
                snapshots,
                completionTime
        );
    }
}
