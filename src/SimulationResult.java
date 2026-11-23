import java.util.*;

/**
 * OWNER: Member 4.
 * Everything a scheduler produces: finished processes, Gantt timeline, per-time-step state.
 *
 * USAGE (Members 1-4):
 *   result.addSegment("P1", 0, 3);                    // CPU ran P1 from t=0 to t=3  ("CS" = context switch, "IDLE" = idle)
 *   result.addStep(t, "P1", tier1List, tier2List);    // once for EVERY time unit t (lists may be null for SJF)
 */
public class SimulationResult {
    /** One Gantt chart block. */
    public static class Segment {
        public final String label; public final int start, end;
        public Segment(String label, int start, int end) { this.label = label; this.start = start; this.end = end; }
    }

    /** State at the start of one time unit. tier1/tier2 are already formatted text, e.g. "[P1 (remaining: 4, Priority: 2)]" or "[Empty]". */
    public static class Step {
        public final int time; public final String running, tier1, tier2;
        public Step(int time, String running, String tier1, String tier2) {
            this.time = time; this.running = running; this.tier1 = tier1; this.tier2 = tier2;
        }
    }

    public final String schedulerName;
    public final List<Process> processes = new ArrayList<>();   // finished processes
    public final List<Segment> timeline = new ArrayList<>();    // Gantt chart
    public final List<Step> steps = new ArrayList<>();          // per-time-step table

    public SimulationResult(String schedulerName) { this.schedulerName = schedulerName; }

    /** TODO Member 4: add a Gantt segment (ignore empty ones); merge with the previous segment if same label and contiguous. */
    public void addSegment(String label, int start, int end) { throw new UnsupportedOperationException("TODO Member 4"); }

    /** TODO Member 4: store a Step; running == null -> "-"; null/empty list -> "[Empty]", otherwise list.toString(). */
    public void addStep(int time, String running, List<Process> tier1, List<Process> tier2) { throw new UnsupportedOperationException("TODO Member 4"); }

    /** TODO Member 4: end time of the last segment (0 if none). */
    public int totalTime() { throw new UnsupportedOperationException("TODO Member 4"); }

    /** TODO Member 4: 100 * (time spent running processes, NOT "CS"/"IDLE") / totalTime(). Needed for the report. */
    public double cpuUtilization() { throw new UnsupportedOperationException("TODO Member 4"); }
}
