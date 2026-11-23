import java.util.*;

/**
 * OWNER: Member 4 (integration). Connects Tier 1 (Member 2) + Tier 2 / Aging (Member 3).
 * You OWN THE CLOCK and the context-switch logic here.
 */
public class ACS implements Scheduler {

    /**
     * TODO Member 4 - suggested algorithm:
     *   copy the input (Process.copyAll); create ACSTier1 and ACSTier2; time = 0; lastRun = null
     *   while (not all processes are finished):
     *     1. Add every process with arrival <= time that has not entered yet to tier1.
     *     2. Pick next: tier1 not empty -> tier1.poll()   (Tier 1 always has priority)
     *                   else tier2 not empty -> tier2.pickNext(time)   (aging applied inside)
     *                   else CPU idle -> log "IDLE", time++, continue
     *     3. If lastRun != null and lastRun != next: context switch -> addSegment("CS", ...), time += cs
     *        (still log steps and still add arrivals during the CS)
     *     4. If next.firstStart == -1 set it to time.
     *     5. Run: from Tier 1 for tier1.sliceLength(...) units; from Tier 2 until it finishes (non-preemptive).
     *        Advance tick by tick, calling result.addStep(...) at every time unit and adding arrivals on time.
     *     6. If remaining == 0 -> finish = time, add to result.processes.
     *        else if it came from Tier 1 and tier1.shouldDemote(...) -> tier2.add(next, time).
     *     7. lastRun = next
     *   Return the result.
     *
     * Decide with the team: the order in the Tier 1 queue when a new arrival and a preempted process arrive at the
     * same instant (see Test Case 1 at t=3), and whether a Tier 2 process that finishes counts as a switch next time.
     */
    @Override
    public SimulationResult run(List<Process> input, int quantum, int cs) {
        throw new UnsupportedOperationException("TODO Member 4");
    }
}
