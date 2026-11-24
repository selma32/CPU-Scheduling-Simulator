import java.util.*;

public class ACSTier2 {
    public static final int AGING_INTERVAL = 4;

    private final List<Process> processes = new ArrayList<>();

    public void add(Process p, int now) {
        p.tier = 2;
        p.waitingSince = now;
        processes.add(p);
    }

    public boolean isEmpty() {
        return processes.isEmpty();
    }

    public List<Process> view() {
        return new ArrayList<>(processes);
    }

    public void applyAging(int now) {
        for (Process p : processes){
            int waited = now - p.waitingSince;
            p.effectivePriority = p.priority - (waited / AGING_INTERVAL);
        }
    }

     /*
        smallest effectivePriority, tie -> earlier arrival, tie -> lower id.
     */
    public Process pickNext(int now) {
        if (processes.isEmpty()) return null;
        applyAging(now);
        Process next = Collections.min(processes,
                Comparator.comparingInt((Process p) -> p.effectivePriority)
                        .thenComparingInt(p -> p.arrival)
                        .thenComparingInt(p -> p.id));
        processes.remove(next);
        return next;
    }
}
