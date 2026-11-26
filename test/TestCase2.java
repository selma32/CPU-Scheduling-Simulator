import java.util.*;

/** OWNER: Member 5 - Test Case 2 from the assignment PDF. Q = 3, CS = 1. */
public class TestCase2 {
    public static final int Q = 3, CS = 1;

    public static List<Process> processes() {
        List<Process> l = new ArrayList<>();
        l.add(new Process(1, 0, 5, 4));
        l.add(new Process(2, 1, 6, 2));
        l.add(new Process(3, 4, 4, 3));
        return l;
    }

    /** ACS finish times from the PDF: P1=18, P2=15, P3=20. */
    public static int[] expectedFinish() { return new int[]{18, 15, 20}; }

    /** SJF (SRTF) finish times with CS=1, by hand: P1=5, CS, P3 6-10, CS, P2 11-17 -> P1=5, P2=17, P3=10. */
    public static int[] expectedFinishSJF() { return new int[]{5, 17, 10}; }

    public static void main(String[] args) {
        TestRunner.check("Test Case 2 - ACS", processes(), Q, CS, expectedFinish());
        TestRunner.check("Test Case 2 - SJF", new SJF(), processes(), Q, CS, expectedFinishSJF(), true);
    }
}
