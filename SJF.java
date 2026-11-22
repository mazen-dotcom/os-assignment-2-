import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SJF {

    public static class Segment {
        public final String label;
        public final int startTime;
        public final int endTime;

        public Segment(String label, int startTime, int endTime) {
            this.label = label;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    public static class Result {
        public final List<Process> processes;
        public final List<Segment> timeline;
        public final double averageWaitingTime;
        public final double averageTurnaroundTime;
        public final double averageResponseTime;

        public Result(List<Process> processes, List<Segment> timeline) {
            this.processes = processes;
            this.timeline = timeline;

            double totalWaiting = 0;
            double totalTurnaround = 0;
            double totalResponse = 0;

            for (Process process : processes) {
                totalWaiting += process.waitingTime;
                totalTurnaround += process.turnaroundTime;
                totalResponse += process.responseTime;
            }

            int count = processes.size();
            averageWaitingTime = count == 0 ? 0 : totalWaiting / count;
            averageTurnaroundTime = count == 0 ? 0 : totalTurnaround / count;
            averageResponseTime = count == 0 ? 0 : totalResponse / count;
        }
    }

    public static Result run(List<Process> inputProcesses, int contextSwitchTime) {
        if (inputProcesses == null) {
            throw new IllegalArgumentException("Process list cannot be null.");
        }
        if (contextSwitchTime < 0) {
            throw new IllegalArgumentException("Context switch time cannot be negative.");
        }

        List<Process> processes = new ArrayList<>();
        for (Process process : inputProcesses) {
            processes.add(process.copy());
        }

        List<Segment> timeline = new ArrayList<>();
        int currentTime = 0;
        int completed = 0;
        Process currentProcess = null;

        while (completed < processes.size()) {
            Process shortest = findShortestReadyProcess(processes, currentTime);

            // No process is ready: CPU stays idle until the next arrival.
            if (shortest == null) {
                int nextArrival = findNextArrival(processes);
                addSegment(timeline, "IDLE", currentTime, nextArrival);
                currentTime = nextArrival;
                currentProcess = null;
                continue;
            }

            // A different process must be loaded. Account for context-switch time.
            if (currentProcess != null && currentProcess != shortest) {
                if (contextSwitchTime > 0) {
                    addSegment(timeline, "CS", currentTime, currentTime + contextSwitchTime);
                    currentTime += contextSwitchTime;
                }

                // Re-evaluate after the context switch because new processes may have arrived.
                currentProcess = null;
                continue;
            }

            currentProcess = shortest;

            if (currentProcess.firstStartTime == -1) {
                currentProcess.firstStartTime = currentTime;
            }

            addSegment(timeline, currentProcess.id, currentTime, currentTime + 1);
            currentProcess.remainingBurst--;
            currentTime++;

            if (currentProcess.remainingBurst == 0) {
                currentProcess.completionTime = currentTime;
                currentProcess.calculateMetrics();
                completed++;
                
            }
        }

        processes.sort(Comparator.comparing(p -> p.id));
        return new Result(processes, timeline);
    }

    private static Process findShortestReadyProcess(List<Process> processes, int time) {
        Process best = null;

        for (Process process : processes) {
            if (process.arrivalTime <= time && process.remainingBurst > 0) {
                if (best == null
                        || process.remainingBurst < best.remainingBurst
                        || (process.remainingBurst == best.remainingBurst
                            && process.arrivalTime < best.arrivalTime)
                        || (process.remainingBurst == best.remainingBurst
                            && process.arrivalTime == best.arrivalTime
                            && process.id.compareTo(best.id) < 0)) {
                    best = process;
                }
            }
        }

        return best;
    }

    private static int findNextArrival(List<Process> processes) {
        int next = Integer.MAX_VALUE;

        for (Process process : processes) {
            if (process.remainingBurst > 0 && process.arrivalTime < next) {
                next = process.arrivalTime;
            }
        }

        if (next == Integer.MAX_VALUE) {
            throw new IllegalStateException("No unfinished process has a future arrival time.");
        }

        return next;
    }

    private static void addSegment(List<Segment> timeline, String label, int start, int end) {
        if (start == end) {
            return;
        }

        if (!timeline.isEmpty()) {
            Segment last = timeline.get(timeline.size() - 1);
            if (last.label.equals(label) && last.endTime == start) {
                timeline.set(timeline.size() - 1, new Segment(label, last.startTime, end));
                return;
            }
        }

        timeline.add(new Segment(label, start, end));
    }
}
