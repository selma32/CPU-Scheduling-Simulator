import java.util.*;

/**
 * OWNER: Member 5. Baseline used ONLY in the report: plain Round Robin with NO Tier 2 demotion.
 * Same conventions as ACS (new arrival enters the queue before a preempted process; no CS after idle).
 * It lets us measure what Tier 2 demotion changes (CPU utilization vs response time).
 */
public class PlainRR implements Scheduler {
    @Override
    public SimulationResult run(List<Process> input, int quantum, int cs) {
        List<Process> ps = Process.copyAll(input);
        SimulationResult r = new SimulationResult("Plain RR (baseline)");
        Deque<Process> q = new ArrayDeque<>();
        Set<Process> admitted = new HashSet<>();
        int time = 0;
        Process last = null;
        while (r.processes.size() < ps.size()) {
            admit(ps, admitted, q, time);
            if (q.isEmpty()) { r.addSegment("IDLE", time, time + 1); time++; last = null; continue; }
            Process next = q.poll();
            if (last != null && last != next && cs > 0) {
                for (int i = 0; i < cs; i++) { time++; admit(ps, admitted, q, time); }
                r.addSegment("CS", time - cs, time);
            }
            if (next.firstStart == -1) next.firstStart = time;
            int run = Math.min(quantum, next.remaining), start = time;
            for (int i = 0; i < run; i++) { next.remaining--; time++; admit(ps, admitted, q, time); }
            r.addSegment(next.name(), start, time);
            if (next.remaining == 0) { next.finish = time; r.processes.add(next); }
            else q.offer(next);
            last = next;
        }
        return r;
    }
    private static void admit(List<Process> ps, Set<Process> a, Deque<Process> q, int t) {
        for (Process p : ps) if (p.arrival <= t && a.add(p)) q.offer(p);
    }
}
