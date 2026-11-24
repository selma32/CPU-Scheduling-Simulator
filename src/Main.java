import java.util.*;

/**
 * OWNER: Member 4. Entry point. Run:  java -cp out Main
 */
public class Main {
    /**
     * TODO Member 4:
     *  1. Read: number of processes, then arrival / burst / priority for each, then Q and CS.
     *  2. Ask which scheduler to run: 1 = SJF, 2 = ACS, 3 = both.
     *  3. Call scheduler.run(processes, q, cs) and pass the result to show().
     */

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Number of processes: ");
        int n = sc.nextInt();

        if (n <= 0) {
            System.out.println("There must be at least one process.");
            return;
        }
        List<Process> processes = new ArrayList<>();

        for (int i = 1; i <= n; i++) {
            System.out.println("Process P" + i + ":");

            System.out.print("  Arrival time: ");
            int arrival = sc.nextInt();

            System.out.print("  Burst time: ");
            int burst = sc.nextInt();

            System.out.print("  Priority (smaller number = higher priority): ");
            int priority = sc.nextInt();

            if (arrival < 0 || burst <= 0) {
                System.out.println("Arrival must be >= 0 and burst must be > 0.");
                return;
            }

            processes.add(new Process(i, arrival, burst, priority));
        }

        System.out.print("Time quantum (Q): ");
        int q = sc.nextInt();

        System.out.print("Context switch time (CS): ");
        int cs = sc.nextInt();

        if (q <= 0 || cs < 0) {
            System.out.println("Q must be positive and CS cannot be negative.");
            return;
        }

        System.out.println("Which scheduler?  1 = SJF, 2 = ACS, 3 = both");
        int choice = sc.nextInt();

        if (choice < 1 || choice > 3) {
            System.out.println("Choice must be 1, 2 or 3.");
            return;
        }

        List<Scheduler> schedulers = new ArrayList<>();

        if (choice == 1 || choice == 3) {
            schedulers.add(new SJF());
        }
        if (choice == 2 || choice == 3) {
            schedulers.add(new ACS());
        }

        for (Scheduler s : schedulers) {

            SimulationResult result;

            try {
                result = s.run(processes, q, cs);
            } catch (UnsupportedOperationException e) {
                // a teammate has not finished this scheduler yet
                System.out.println("\n" + s.getClass().getSimpleName() + " is not implemented yet, skipping it.");
                continue;
            }

            show(result);
        }
    }

    /** TODO Member 4: print timeline, per-step table and results table (calls the Metrics.print* methods). */
    public static void show(SimulationResult r) {

        System.out.println("\n==================== " + r.schedulerName + " ====================");

        Metrics.printTimeline(r);
        Metrics.printSteps(r);
        Metrics.printTable(r);
    }

}


