
import java.util.*;

public class SJF implements Scheduler {

    @Override
    public SimulationResult run(List<Process> input, int quantum, int cs) {

        // Step 1: Copy input processes
        List<Process> processes = Process.copyAll(input);

        // Step 2: Create simulation result
        SimulationResult result =
                new SimulationResult("Preemptive SJF");

        // Step 3: Initialize time and previous process
        int time = 0;
        Process previous = null;

        boolean switching = false;

        // Step 4: Run until all processes finish
        while (result.processes.size() < processes.size()) {

            Process selected = null;

            // Find the process with the shortest remaining time
            for (Process p : processes) {
                if (p.arrival <= time && p.remaining > 0) {
                    if (selected == null ||
                            p.remaining < selected.remaining ||
                            (p.remaining == selected.remaining &&
                                    p.arrival < selected.arrival) ||
                            (p.remaining == selected.remaining &&
                                    p.arrival == selected.arrival &&
                                    p.id < selected.id)) {

                        selected = p;
                    }
                }
            }

            // Handle idle CPU
            if (selected == null) {

                result.addSegment("IDLE", time, time + 1);
                result.addStep(time, "IDLE", null, null);

                time++;
                previous = null;
                switching = false;

                continue;
            }

            // Handle context switching
            if (previous != null &&
                    previous != selected &&
                    cs > 0 &&
                    !switching) {

                result.addSegment("CS", time, time + cs);

                for (int i = 0; i < cs; i++) {
                    result.addStep(time, "CS", null, null);
                    time++;
                }

                switching = true;
                continue;
            }

            switching = false;

            // Set first start time
            if (selected.firstStart == -1) {
                selected.firstStart = time;
            }

            // Execute for one time unit
            result.addSegment(selected.name(), time, time + 1);
            result.addStep(time, selected.name(), null, null);

            selected.remaining--;
            time++;

            // Check if process finished
            if (selected.remaining == 0) {
                selected.finish = time;
                result.processes.add(selected);
            }

            previous = selected;
        }

        return result;
    }
}
