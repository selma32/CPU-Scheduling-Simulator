/**
 * Shared simulator output helper. The default application entry point is the GUI.
 */
public class Main {
    /** Opens the desktop interface when this class is launched from an IDE or terminal. */
    public static void main(String[] args) throws Exception {
        SimulatorApp.main(args);
    }

    /** TODO Member 4: print timeline, per-step table and results table (calls the Metrics.print* methods). */
    public static void show(SimulationResult r) {

        System.out.println("\n==================== " + r.schedulerName + " ====================");

        Metrics.printTimeline(r);
        Metrics.printSteps(r);
        Metrics.printTable(r);
    }

}
