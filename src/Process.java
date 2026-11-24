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


    public Process(int id, int arrival, int burst, int priority) {

        this.id = id;
        this.arrival = arrival;
        this.burst = burst;
        this.priority = priority;

        this.remaining = burst;
        this.effectivePriority = priority;


    }

    public Process copy() {
        return new Process(id, arrival, burst, priority);
    }


    public static List<Process> copyAll(List<Process> list) {
        List<Process> copies = new ArrayList<>();

        for (Process p : list) {
            copies.add(p.copy());
        }

        return copies;
    }

    public String name() {
        return "P" + id;
    }

    public int turnaround() {
        return finish - arrival;
    }

    public int waiting() {
        return turnaround() - burst;
    }

    public int response() {
        return firstStart - arrival;
    }

    @Override
    public String toString() {
        return name() +
                " (remaining: " + remaining +
                ", Priority: " + effectivePriority + ")";
    }
}






