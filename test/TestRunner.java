import java.util.*;

/** OWNER: Member 5. Shared helper used by TestCase1 / TestCase2 / ExtraCases. */
public class TestRunner {

    /** Runs one scheduler, prints the full output and compares finish times. Returns true if all match. */
    public static boolean check(String name, Scheduler scheduler, List<Process> procs, int q, int cs,
                                int[] expectedFinishById, boolean verbose) {
        System.out.println("\n############ " + name + "  (Q=" + q + ", CS=" + cs + ") ############");
        SimulationResult r = scheduler.run(procs, q, cs);
        if (verbose) Main.show(r);

        List<Process> done = new ArrayList<>(r.processes);
        done.sort(Comparator.comparingInt(p -> p.id));

        boolean all = done.size() == expectedFinishById.length;
        if (!all) System.out.println("FAIL: expected " + expectedFinishById.length + " finished processes, got " + done.size());

        for (Process p : done) {
            int exp = expectedFinishById[p.id - 1];
            boolean ok = p.finish == exp;
            all &= ok;
            System.out.printf("%-4s expected finish=%-3d actual=%-3d %s%n", p.name(), exp, p.finish, ok ? "PASS" : "FAIL");
        }
        System.out.println(all ? ">>> " + name + ": ALL PASS" : ">>> " + name + ": FAILED");
        return all;
    }

    /** Same as above for ACS (the signature given in the project skeleton). */
    public static void check(String name, List<Process> procs, int q, int cs, int[] expectedFinishById) {
        check(name, new ACS(), procs, q, cs, expectedFinishById, true);
    }
}
