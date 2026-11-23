import java.util.*;

/**
 * OWNER: Member 2 - ACS Tier 1: Round Robin.
 *
 * Rules: every new process enters Tier 1 (FIFO). It runs at most Q units. If it finishes it leaves; if it used the
 * WHOLE quantum and is not done it is flagged CPU-bound and demoted to Tier 2.
 * (remaining == exactly Q means it FINISHES, it is not demoted.)
 */
public class ACSTier1 {
    private Queue<Process> readyQueue = new ArrayDeque<>();

    public void add(Process p) {
        p.tier = 1;
        readyQueue.offer(p);
    }

    public boolean isEmpty() {
        return readyQueue.isEmpty();
    }

    public Process poll() {
        return readyQueue.poll();
    }

    public List<Process> view() {
        return new ArrayList<>(readyQueue);
    }

    public int sliceLength(Process p, int quantum) {
        return Math.min(quantum, p.remaining);
    }

    public void runOneTick(Process p) {
        if (p.remaining > 0) {
            p.remaining--;
        }
    }

    public boolean shouldDemote(Process p, int used, int quantum) {
        return used == quantum && p.remaining > 0;
    }
}
