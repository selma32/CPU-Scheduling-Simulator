import java.util.*;

/**
 * OWNER: Member 3 - ACS Tier 2 (Non-preemptive Priority) + Tier 3 (Aging).
 *
 * Rules: holds demoted (CPU-bound) processes; lower number = higher priority; non-preemptive (ACS runs the chosen
 * process to completion). Aging prevents starvation:
 *     effectivePriority = priority - floor(waited / AGING_INTERVAL),  waited = now - waitingSince
 * The ORIGINAL p.priority is never overwritten. Example (Test Case 2, t=11): P1 4 - floor(8/4) = 2, P2 2 - floor(4/4) = 1.
 */
public class ACSTier2 {
    public static final int AGING_INTERVAL = 4;   // confirm with the TA: fixed 4 time units per step

    // TODO Member 3: choose the data structure for the waiting processes (e.g. a List<Process>).

    /** TODO Member 3: add p, set p.tier = 2 and p.waitingSince = now. */
    public void add(Process p, int now) { throw new UnsupportedOperationException("TODO Member 3"); }

    /** TODO Member 3: true if no process is waiting in Tier 2. */
    public boolean isEmpty() { throw new UnsupportedOperationException("TODO Member 3"); }

    /** TODO Member 3: return a COPY of the waiting processes (used by ACS to print Tier 2 at every step). */
    public List<Process> view() { throw new UnsupportedOperationException("TODO Member 3"); }

    /** TODO Member 3: update effectivePriority of EVERY waiting process for time `now` (formula above). */
    public void applyAging(int now) { throw new UnsupportedOperationException("TODO Member 3"); }

    /**
     * TODO Member 3: applyAging(now), then remove and return the best process:
     * smallest effectivePriority, tie -> earlier arrival, tie -> lower id.
     */
    public Process pickNext(int now) { throw new UnsupportedOperationException("TODO Member 3"); }
}
