import java.util.List;

/**
 * OWNER: Member 4. The shared interface agreed by the whole team (nothing to implement here).
 * SJF and ACS both implement it; Main and the tests only talk to this interface.
 */
public interface Scheduler {
    /**
     * Rules for every implementation:
     *  - work on a COPY of `processes` (Process.copyAll) and never modify the input list
     *  - record the Gantt chart with result.addSegment(...)
     *  - call result.addStep(...) once for EVERY time unit
     *  - put every finished process in result.processes (with firstStart and finish set)
     */
    SimulationResult run(List<Process> processes, int quantum, int contextSwitch);
}
