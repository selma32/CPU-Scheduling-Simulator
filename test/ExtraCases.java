import java.util.*;

/** OWNER: Member 5. Our own test cases (beyond the two in the assignment) used in the experiments. */
public class ExtraCases {

    /** Test Case 3: 6 processes, mixed short (interactive) and long (batch) jobs, staggered arrivals. */
    public static List<Process> case3() {
        return Arrays.asList(
            new Process(1, 0, 10, 3), new Process(2, 1, 2, 1), new Process(3, 2, 8, 4),
            new Process(4, 4, 1, 2), new Process(5, 6, 12, 5), new Process(6, 8, 3, 1));
    }

    /** Test Case 4: starvation / aging: five CPU-bound jobs at t=0, priorities 1..5 (P5 is the lowest). */
    public static List<Process> case4() {
        return Arrays.asList(
            new Process(1, 0, 9, 5), new Process(2, 0, 8, 4), new Process(3, 0, 7, 3),
            new Process(4, 0, 6, 2), new Process(5, 0, 5, 1));
    }

    /** Test Case 5: CPU idle gap between arrivals (P3 arrives after everything finished). */
    public static List<Process> case5() {
        return Arrays.asList(
            new Process(1, 0, 3, 2), new Process(2, 1, 2, 1), new Process(3, 12, 4, 3), new Process(4, 13, 6, 2));
    }

    /** Edge case: burst exactly equal to Q must FINISH, not be demoted. */
    public static List<Process> caseExactQuantum() {
        return Arrays.asList(new Process(1, 0, 3, 2), new Process(2, 0, 3, 1));
    }

    public static void main(String[] args) {
        boolean ok = true;
        // Exact-quantum edge case, Q=3 CS=0: both finish inside Tier 1 -> P1 3, P2 6; nothing is demoted.
        ok &= TestRunner.check("Edge: burst == Q (ACS)", new ACS(), caseExactQuantum(), 3, 0, new int[]{3, 6}, false);
        // Case 5, Q=3 CS=0 by hand: P1 0-3, P2 3-5, idle 5-12, P3 12-15 (demoted), P4 15-18 (demoted),
        // Tier 2 at t=18: P4 prio 2 beats P3 prio 3 -> P4 18-21, P3 21-22.
        SimulationResult r = new ACS().run(case5(), 3, 0);
        List<Process> d = new ArrayList<>(r.processes); d.sort(Comparator.comparingInt(p -> p.id));
        int[] fin = new int[4]; for (Process p : d) fin[p.id - 1] = p.finish;
        boolean c5 = Arrays.equals(fin, new int[]{3, 5, 22, 21});   // hand-computed
        ok &= c5;
        System.out.println("Case 5 (idle gap) ACS finish " + Arrays.toString(fin) + " expected [3, 5, 22, 21] -> " + (c5 ? "PASS" : "FAIL"));
        // Sanity invariants on every case / every (Q,CS): all processes finish, finish >= arrival+burst, no overlap.
        int[][] pairs = {{2,0},{3,0},{3,1},{4,1},{4,2}};
        List<List<Process>> cases = Arrays.asList(TestCase1.processes(), TestCase2.processes(), case3(), case4(), case5());
        Scheduler[] ss = {new SJF(), new ACS(), new PlainRR()};
        int checks = 0;
        for (List<Process> c : cases) for (int[] pr : pairs) for (Scheduler s : ss) {
            SimulationResult res = s.run(c, pr[0], pr[1]);
            boolean good = res.processes.size() == c.size();
            for (Process p : res.processes) good &= p.finish >= p.arrival + p.burst && p.firstStart >= p.arrival && p.waiting() >= 0;
            int prevEnd = 0; for (SimulationResult.Segment sg : res.timeline) { good &= sg.start >= prevEnd; prevEnd = sg.end; }
            int busy = 0; for (SimulationResult.Segment sg : res.timeline) if (!sg.label.equals("CS") && !sg.label.equals("IDLE")) busy += sg.end - sg.start;
            int burstSum = 0; for (Process p : c) burstSum += p.burst;
            good &= busy == burstSum;   // CPU executed exactly the total burst time
            if (!good) { System.out.println("INVARIANT FAIL: " + s.getClass().getSimpleName() + " Q=" + pr[0] + " CS=" + pr[1]); ok = false; }
            checks++;
        }
        System.out.println("Invariant checks run: " + checks + (ok ? "  -> ALL PASS" : "  -> SOME FAILED"));
    }
}
