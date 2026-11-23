import java.util.*;

/**
 * OWNER: Member 4 (Core, Metrics & Integration)
 * Shared data model used by EVERYONE. Do not rename fields/methods without telling the team.
 * Conventions: id = 1 for P1, 2 for P2 ...  |  lower priority number = HIGHER priority.
 */
public class Process {
    // ---- inputs (never modified after construction) ----
    public final int id, arrival, burst, priority;

    // ---- simulation state (modified by the schedulers) ----
    public int remaining;          // time left to run (starts = burst)
    public int firstStart = -1;    // first time the process got the CPU (-1 = never ran yet)
    public int finish = -1;        // completion time (-1 = not finished)
    public int effectivePriority;  // priority after aging; the original `priority` is NEVER overwritten
    public int tier = 1;           // ACS only: 1 = Round Robin, 2 = Priority
    public int waitingSince = -1;  // ACS only: time the process entered Tier 2 (used for aging)

    /** TODO Member 4: assign the inputs; remaining = burst; effectivePriority = priority. */
    public Process(int id, int arrival, int burst, int priority) {
        this.id = id; this.arrival = arrival; this.burst = burst; this.priority = priority;
        // TODO Member 4: initialise remaining and effectivePriority
    }

    /** TODO Member 4: return a fresh copy built from the ORIGINAL inputs (id, arrival, burst, priority). */
    public Process copy() { throw new UnsupportedOperationException("TODO Member 4"); }

    /** TODO Member 4: copy every process in the list (schedulers call this first so the input is never modified). */
    public static List<Process> copyAll(List<Process> list) { throw new UnsupportedOperationException("TODO Member 4"); }

    /** TODO Member 4: "P" + id */
    public String name() { throw new UnsupportedOperationException("TODO Member 4"); }

    /** TODO Member 4: Turnaround = finish - arrival */
    public int turnaround() { throw new UnsupportedOperationException("TODO Member 4"); }

    /** TODO Member 4: Waiting = turnaround - burst */
    public int waiting() { throw new UnsupportedOperationException("TODO Member 4"); }

    /** TODO Member 4: Response = firstStart - arrival */
    public int response() { throw new UnsupportedOperationException("TODO Member 4"); }

    /** TODO Member 4: format used in the per-step queue output, e.g. "P1 (remaining: 4, Priority: 2)" (use effectivePriority). */
    @Override public String toString() { throw new UnsupportedOperationException("TODO Member 4"); }
}
