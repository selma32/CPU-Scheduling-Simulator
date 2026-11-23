import java.util.*;

/**
 * OWNER: Member 1 - Preemptive SJF (Shortest Remaining Time First).
 */
public class SJF implements Scheduler {

    /**
     * TODO Member 1:
     *  1. Copy the input (Process.copyAll). `quantum` is ignored by SJF.
     *  2. At EVERY time unit run the ready process (arrival <= time, remaining > 0) with the SMALLEST remaining time;
     *     a newly arrived shorter process preempts the running one.
     *  3. Tie-break: earlier arrival, then lower id.
     *  4. Context switch: when the CPU moves from process A to a different process B, spend `cs` units:
     *     result.addSegment("CS", t, t + cs); time += cs. (First start / idle -> process: no CS, confirm with Member 4.)
     *  5. Set firstStart on first run, finish when remaining == 0, add finished processes to result.processes.
     *  6. Log Gantt segments with addSegment and call addStep at every time unit (tier lists may be null).
     *  7. Hand-verify with Test Case 1 and 2 and give the results to Member 5.
     */
    @Override
    public SimulationResult run(List<Process> input, int quantum, int cs) {
        throw new UnsupportedOperationException("TODO Member 1");
    }
}
