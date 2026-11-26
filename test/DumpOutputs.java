import java.io.*;
import java.util.*;

/** OWNER: Member 5. Saves the full simulator output of every test case to report/outputs/*.txt (used in the report). */
public class DumpOutputs {
    public static void main(String[] args) throws Exception {
        Object[][] runs = {
            {"TC1_ACS", new ACS(), TestCase1.processes(), 3, 0}, {"TC1_SJF", new SJF(), TestCase1.processes(), 3, 0},
            {"TC2_ACS", new ACS(), TestCase2.processes(), 3, 1}, {"TC2_SJF", new SJF(), TestCase2.processes(), 3, 1},
            {"TC3_ACS", new ACS(), ExtraCases.case3(), 3, 1},    {"TC3_SJF", new SJF(), ExtraCases.case3(), 3, 1},
            {"TC4_ACS", new ACS(), ExtraCases.case4(), 3, 0},    {"TC4_SJF", new SJF(), ExtraCases.case4(), 3, 0},
            {"TC5_ACS", new ACS(), ExtraCases.case5(), 3, 0},    {"TC5_SJF", new SJF(), ExtraCases.case5(), 3, 0}};
        PrintStream real = System.out;
        new File("report/outputs").mkdirs();
        for (Object[] r : runs) {
            PrintStream ps = new PrintStream(new FileOutputStream("report/outputs/" + r[0] + ".txt"));
            System.setOut(ps);
            @SuppressWarnings("unchecked") List<Process> procs = (List<Process>) r[2];
            Main.show(((Scheduler) r[1]).run(procs, (Integer) r[3], (Integer) r[4]));
            ps.close();
        }
        System.setOut(real);
    }
}
