import java.util.*;

/*
    Formulas: Waiting = Turnaround - Burst | Turnaround = Finish - Arrival | Response = FirstStart - Arrival
*/
public class Metrics {

    public static double avgWaiting(List<Process> ps) {
        double total = 0;
        for (Process p : ps){
            total += p.waiting();
        }
        return total / ps.size();
    }

    public static double avgTurnaround(List<Process> ps) {
        double total = 0;
        for (Process p: ps){
            total += p.turnaround();
        }
        return total / ps.size();
    }

    public static double avgResponse(List<Process> ps) {
        double total = 0;
        for (Process p: ps){
            total += p.response();
        }
        return total / ps.size();
    }

    public static void printTimeline(SimulationResult r) {
        for (SimulationResult.Segment s : r.timeline){
            System.out.println("[" + s.start + "-" + s.end + "] " + s.label);
        }
    }

    public static void printSteps(SimulationResult r) {
        System.out.println("\n=== " + r.schedulerName + " - state at every time step ===");
        for (SimulationResult.Step s : r.steps){
            System.out.println("t=" + s.time + "  CPU: " + s.running);
            System.out.println("Tier 1: " + s.tier1);
            System.out.println("Tier 2: " + s.tier2);
            System.out.println();
        }
    }

    public static void printTable(SimulationResult r) {
        System.out.println("\n=== " + r.schedulerName + " - final metrics ===");
        System.out.printf("%-10s %-10s %-10s %-10s %-12s %-10s %-10s%n",
                "Process", "Arrival", "Burst", "Finish", "Turnaround", "Waiting", "Response");

        List<Process> processes = new ArrayList<>(r.processes);
        processes.sort(Comparator.comparingInt(p -> p.id));

        for (Process p : processes) {
            System.out.printf("%-10s %-10d %-10d %-10d %-12d %-10d %-10d%n",
                    p.name(), p.arrival, p.burst, p.finish, p.turnaround(), p.waiting(), p.response());
        }

        System.out.printf("Average Turnaround Time: %.2f%n", avgTurnaround(processes));
        System.out.printf("Average Waiting Time: %.2f%n", avgWaiting(processes));
        System.out.printf("Average Response Time: %.2f%n", avgResponse(processes));
        System.out.printf("CPU Utilization: %.1f%%%n", r.cpuUtilization());
    }

}
