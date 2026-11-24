import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Non-Preemptive Priority scheduler used by Tier 2 of the ACS.
 *
 * Lower priority number = higher priority (as in the assignment's test cases).
 * Selection uses Process.effectivePriority, which the aging mechanism (Tier 3)
 * may lower while a process waits. Ties are broken by who has been waiting
 * longest (FCFS), then arrival time, then id.
 */
public class PriorityScheduler {

    /** Hook for Tier 3 (Aging): called right before each selection. */
    public interface AgingPolicy {
        void age(List<Process> waiting, int now);
    }

    private static final AgingPolicy NO_AGING = (waiting, now) -> { };

    private static final Comparator<Process> ORDER =
            Comparator.<Process>comparingInt(p -> p.effectivePriority)
                    .thenComparingInt(p -> p.waitingSince)
                    .thenComparingInt(p -> p.arrivalTime)
                    .thenComparing(p -> p.id);

    private final List<Process> ready = new ArrayList<>();
    private AgingPolicy aging = NO_AGING;

    public void setAgingPolicy(AgingPolicy policy) {
        this.aging = policy == null ? NO_AGING : policy;
    }

    /** Adds a process demoted from Tier 1. Its waitingSince must already be set. */
    public void enqueue(Process p) {
        p.effectivePriority = p.Priority;
        ready.add(p);
    }

    public boolean isEmpty() {
        return ready.isEmpty();
    }

    /** Read-only view of the processes currently waiting, highest priority first. */
    public List<Process> waiting() {
        List<Process> sorted = new ArrayList<>(ready);
        sorted.sort(ORDER);
        return Collections.unmodifiableList(sorted);
    }

    /**
     * Applies aging at time 'now', then removes and returns the process with the
     * highest priority. The caller runs it to completion (no preemption).
     */
    public Process selectNext(int now) {
        if (ready.isEmpty()) {
            return null;
        }
        aging.age(ready, now);
        Process best = Collections.min(ready, ORDER);
        ready.remove(best);
        return best;
    }

    public static String describe(Process p) {
        return p.id + " (remaining: " + p.remainingBurst + ", Priority: " + p.effectivePriority + ")";
    }

    public String describeQueue() {
        if (ready.isEmpty()) {
            return "[Empty]";
        }
        StringBuilder sb = new StringBuilder("[");
        List<Process> sorted = waiting();
        for (int i = 0; i < sorted.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(describe(sorted.get(i)));
        }
        return sb.append("]").toString();
    }
}