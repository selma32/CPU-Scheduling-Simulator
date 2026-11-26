import java.util.*;

/** OWNER: Member 5 - Test Case 1 from the assignment PDF. Q = 3, CS = 0. */
public class TestCase1 {
    public static final int Q = 3, CS = 0;

    public static List<Process> processes() {
        List<Process> l = new ArrayList<>();
        l.add(new Process(1, 0, 4, 2));
        l.add(new Process(2, 1, 7, 3));
        l.add(new Process(3, 3, 2, 1));
        return l;
    }

    /** ACS finish times from the PDF: P1=9, P2=13, P3=8. */
    public static int[] expectedFinish() { return new int[]{9, 13, 8}; }

    /** SJF (SRTF) finish times, computed by hand: P1=4, P2=13, P3=6. */
    public static int[] expectedFinishSJF() { return new int[]{4, 13, 6}; }

    public static void main(String[] args) {
        TestRunner.check("Test Case 1 - ACS", processes(), Q, CS, expectedFinish());
        TestRunner.check("Test Case 1 - SJF", new SJF(), processes(), Q, CS, expectedFinishSJF(), true);
    }
}
