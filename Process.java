import java.util.ArrayList;
import java.util.List;

public class Process {
    public final String id;
    public final int arrivalTime;
    public final int burstTime;
    public final int Priority;

    public int remainingBurst;
    public int completionTime = -1;
    public int firstStartTime = -1;
    public int waitingTime;
    public int turnaroundTime;
    public int responseTime;

    public Process(String id, int arrivalTime, int burstTime, int Priority) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Process ID cannot be empty.");
        }
        if (arrivalTime < 0 || burstTime <= 0) {
            throw new IllegalArgumentException("Arrival time must be >= 0 and burst time must be > 0.");
        }

        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.Priority = Priority;
        this.remainingBurst = burstTime;
    }

    public Process copy() {
        return new Process(id, arrivalTime, burstTime, Priority);
    }

    public void calculateMetrics() {
        turnaroundTime = completionTime - arrivalTime;
        waitingTime = turnaroundTime - burstTime;
        responseTime = firstStartTime - arrivalTime;
    }
}
