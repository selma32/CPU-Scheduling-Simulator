import java.util.*;

public class ACSTier2 {
    public static final int AGING_INTERVAL = 4;   // confirm with the TA: fixed 4 time units per step

    private final List<Process> processes = new ArrayList<>();

    public void add(Process p, int now) {
        p.tier = 2;
        p.waitingSince = now;
        processes.add(p);
    }

    /** TODO Member 3: true if no process is waiting in Tier 2. */
    public boolean isEmpty() {
        return processes.isEmpty();
    }

    /** TODO Member 3: return a COPY of the waiting processes (used by ACS to print Tier 2 at every step). */
    public List<Process> view() {
        return new ArrayList<>(processes);
    }

    /** TODO Member 3: update effectivePriority of EVERY waiting process for time `now` (formula above). */
    public void applyAging(int now) {
        for (Process p : processes){
            int waited = now - p.waitingSince;
            p.effectivePriority = p.priority - (waited / AGING_INTERVAL);
        }
    }

    /**
     * TODO Member 3: applyAging(now), then remove and return the best process:
     * smallest effectivePriority, tie -> earlier arrival, tie -> lower id.
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
