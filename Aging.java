
import java.util.List;

public class Aging implements PriorityScheduler.AgingPolicy {

    @Override
    public void age(List<Process> waiting, int now) {

        for (Process p : waiting) {

            int waitingTime = now - p.waitingSince;

            int aging = waitingTime / 4;
            // priority can not be less than 1 
            p.effectivePriority = Math.max(1,p.Priority - aging  );
        }
    }
}
