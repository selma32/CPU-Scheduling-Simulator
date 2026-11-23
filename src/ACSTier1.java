import java.util.*;

/**
 * OWNER: Member 2 - ACS Tier 1: Round Robin.
 *
 * Rules: every new process enters Tier 1 (FIFO). It runs at most Q units. If it finishes it leaves; if it used the
 * WHOLE quantum and is not done it is flagged CPU-bound and demoted to Tier 2.
 * (remaining == exactly Q means it FINISHES, it is not demoted.)
 */
public class ACSTier1 {
    // TODO Member 2: choose the data structure (e.g. a Deque<Process>) for the FIFO queue.

    /** TODO Member 2: append p to the end of the queue and set p.tier = 1. */
    public void add(Process p) { throw new UnsupportedOperationException("TODO Member 2"); }

    /** TODO Member 2: true if the queue is empty. */
    public boolean isEmpty() { throw new UnsupportedOperationException("TODO Member 2"); }

    /** TODO Member 2: remove and return the process at the front (null if empty). */
    public Process poll() { throw new UnsupportedOperationException("TODO Member 2"); }

    /** TODO Member 2: return a COPY of the queue in order (used by ACS to print Tier 1 at every step). */
    public List<Process> view() { throw new UnsupportedOperationException("TODO Member 2"); }

    /** TODO Member 2: how long p should run now = min(quantum, p.remaining). */
    public int sliceLength(Process p, int quantum) { throw new UnsupportedOperationException("TODO Member 2"); }

    /** TODO Member 2: called after p ran `used` units. Return true if p must go to Tier 2 (used the full quantum AND remaining > 0). */
    public boolean shouldDemote(Process p, int used, int quantum) { throw new UnsupportedOperationException("TODO Member 2"); }
}
