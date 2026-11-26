import java.io.*;
import java.util.*;

/** OWNER: Member 5. Runs every test case x (Q,CS) pair x scheduler and writes results.csv for the report graphs. */
public class ExperimentRunner {
    public static void main(String[] args) throws IOException {
        String[] names = {"TC1", "TC2", "TC3", "TC4", "TC5"};
        List<List<Process>> cases = Arrays.asList(TestCase1.processes(), TestCase2.processes(),
                ExtraCases.case3(), ExtraCases.case4(), ExtraCases.case5());
        int[][] pairs = {{2,0},{3,0},{3,1},{4,1},{4,2}};
        String[] sn = {"SJF", "ACS", "RR"};
        Scheduler[] ss = {new SJF(), new ACS(), new PlainRR()};

        try (PrintWriter out = new PrintWriter(new FileWriter("results.csv"))) {
            out.println("testcase,scheduler,Q,CS,avgWaiting,avgTurnaround,avgResponse,cpuUtil,contextSwitches,totalTime");
            for (int i = 0; i < cases.size(); i++)
                for (int[] pr : pairs)
                    for (int s = 0; s < ss.length; s++) {
                        SimulationResult r = ss[s].run(cases.get(i), pr[0], pr[1]);
                        int cs = 0;
                        for (SimulationResult.Segment sg : r.timeline) if (sg.label.equals("CS")) cs += (sg.end - sg.start) / Math.max(1, pr[1]);
                        out.printf(Locale.US, "%s,%s,%d,%d,%.2f,%.2f,%.2f,%.1f,%d,%d%n", names[i], sn[s], pr[0], pr[1],
                                Metrics.avgWaiting(r.processes), Metrics.avgTurnaround(r.processes),
                                Metrics.avgResponse(r.processes), r.cpuUtilization(), cs, r.totalTime());
                    }
        }
        // Full grid Q in 1..6, CS in 0..3 (shows the Q / CS relation as continuous curves)
        try (PrintWriter out = new PrintWriter(new FileWriter("results_grid.csv"))) {
            out.println("testcase,scheduler,Q,CS,avgWaiting,avgTurnaround");
            for (int i = 0; i < cases.size(); i++)
                for (int q = 1; q <= 6; q++)
                    for (int c = 0; c <= 3; c++)
                        for (int s = 0; s < ss.length; s++) {
                            SimulationResult r = ss[s].run(cases.get(i), q, c);
                            out.printf(Locale.US, "%s,%s,%d,%d,%.2f,%.2f%n", names[i], sn[s], q, c,
                                    Metrics.avgWaiting(r.processes), Metrics.avgTurnaround(r.processes));
                        }
        }
        System.out.println("results.csv and results_grid.csv written");
    }
}
