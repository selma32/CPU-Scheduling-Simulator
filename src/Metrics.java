import java.util.*;

/**
 * OWNER: Member 4.
 * Formulas: Waiting = Turnaround - Burst | Turnaround = Finish - Arrival | Response = FirstStart - Arrival
 * Plus the console printing used by Main.
 */
public class Metrics {
    /** TODO Member 4: average waiting time of the list. */
    public static double avgWaiting(List<Process> ps) { throw new UnsupportedOperationException("TODO Member 4"); }

    /** TODO Member 4: average turnaround time of the list. */
    public static double avgTurnaround(List<Process> ps) { throw new UnsupportedOperationException("TODO Member 4"); }

    /** TODO Member 4: average response time of the list. */
    public static double avgResponse(List<Process> ps) { throw new UnsupportedOperationException("TODO Member 4"); }

    /** TODO Member 4: print the Gantt chart, one line per segment, e.g. "[0-3] P1". */
    public static void printTimeline(SimulationResult r) { throw new UnsupportedOperationException("TODO Member 4"); }

    /** TODO Member 4: print one line per time step: time, CPU process, Tier 1 queue, Tier 2 queue (format of the assignment PDF). */
    public static void printSteps(SimulationResult r) { throw new UnsupportedOperationException("TODO Member 4"); }

    /**
     * TODO Member 4: print the results table sorted by id:
     * Process | Arrival | Burst | Finish | Turnaround | Waiting | Response
     * then Average Turnaround, Average Waiting, Average Response and CPU utilization.
     */
    public static void printTable(SimulationResult r) { throw new UnsupportedOperationException("TODO Member 4"); }
}
