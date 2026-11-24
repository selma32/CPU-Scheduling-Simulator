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

        // Step 1: Validate the input
        if (quantum <= 0 || cs < 0) {
            throw new IllegalArgumentException(
                    "Quantum must be positive and CS cannot be negative"
            );
        }

        // Step 2: Copy the processes
        List<Process> processes = Process.copyAll(input);

        // Step 3: Create the two tiers
        ACSTier1 tier1 = new ACSTier1();
        ACSTier2 tier2 = new ACSTier2();

        // Step 4: Create the result object
        SimulationResult result = new SimulationResult("ACS");

        // Step 5: Initialize the simulation clock
        int time = 0;

        // Step 6: Remember the previously running process
        Process lastRun = null;

        Set<Process> admitted = new HashSet<>();

        while (result.processes.size() < processes.size()) {

            // Add newly arrived processes
            for (Process p : processes) {
                if (p.arrival <= time && !admitted.contains(p)) {
                    tier1.add(p);
                    admitted.add(p);
                }
            }

            // Take the snapshot BEFORE picking the next process (the picked process is still inside the queues)
            tier2.applyAging(time);
            List<Process> snapT1 = tier1.view();
            List<Process> snapT2 = tier2.view();
            boolean snapshotUsed = false;

            Process next = null;
            boolean fromTier1 = false;

            if (!tier1.isEmpty()) {
                next = tier1.poll();
                fromTier1 = true;
            }
            else if (!tier2.isEmpty()) {
                next = tier2.pickNext(time);
            }

            if (next == null) {
                result.addStep(time, null, tier1.view(), tier2.view());
                result.addSegment("IDLE", time, time + 1);
                time++;
                lastRun = null;
                continue;
            }

            // Context Switch
            if (lastRun != null && lastRun != next && cs > 0) {

                int csStart = time;

                for (int i = 0; i < cs; i++) {

                    // Add newly arrived processes
                    for (Process p : processes) {
                        if (p.arrival <= time && !admitted.contains(p)) {
                            tier1.add(p);
                            admitted.add(p);
                        }
                    }

                    // Apply aging
                    tier2.applyAging(time);

                    // Record context switch step
                    if (!snapshotUsed) {
                        result.addStep(time, "CS", snapT1, snapT2);
                        snapshotUsed = true;
                    } else {
                        result.addStep(time, "CS", tier1.view(), tier2.view());
                    }

                    time++;
                }

                result.addSegment("CS", csStart, time);
            }



            // Record first start time
            if (next.firstStart == -1) {
                next.firstStart = time;
            }

            // Calculate how long the process will run
            int runLength;

            if (fromTier1) {
                runLength = tier1.sliceLength(next, quantum);
            }
            else {
                runLength = next.remaining;
            }

            // Execute the process
            int runStart = time;
            int used = 0;

            while (used < runLength) {

                // Add processes that arrived during execution
                for (Process p : processes) {
                    if (p.arrival <= time && !admitted.contains(p)) {
                        tier1.add(p);
                        admitted.add(p);
                    }
                }

                // Apply aging before recording the current step
                tier2.applyAging(time);

                // Record current simulation state
                if (!snapshotUsed) {
                    result.addStep(time, next.name(), snapT1, snapT2);
                    snapshotUsed = true;
                } else {
                    result.addStep(time, next.name(), tier1.view(), tier2.view());
                }

                // Execute one time unit
                next.remaining--;

                // Advance the clock
                time++;
                used++;
            }

            // Record the execution segment
            result.addSegment(next.name(), runStart, time);

            // Check if the process finished
            if (next.remaining == 0) {

                next.finish = time;
                result.processes.add(next);

            }

            // Otherwise, demote it from Tier 1 to Tier 2
            else if (fromTier1 &&
                    tier1.shouldDemote(next, used, quantum)) {

                tier2.add(next, time);

            }

            lastRun = next;

        }

        return result;
    }

}