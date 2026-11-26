import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Horizontal comparison of per-process waiting and turnaround values from the result. */
public final class ProcessMetricsChart extends JPanel {
    private final List<Process> processes;
    private final boolean darkMode;
    private final int maximum;

    public ProcessMetricsChart(SimulationResult result, boolean darkMode) {
        this.processes = new ArrayList<>(result.processes);
        this.processes.sort(Comparator.comparingInt(process -> process.id));
        this.darkMode = darkMode;
        int max = 1;
        for (Process process : processes) {
            max = Math.max(max, process.turnaround());
        }
        this.maximum = max;
        setOpaque(false);
        setPreferredSize(new Dimension(620, Math.max(100, processes.size() * 36 + 38)));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color text = darkMode ? new Color(232, 224, 240) : new Color(59, 54, 68);
        Color muted = darkMode ? new Color(163, 152, 178) : new Color(128, 120, 139);
        Color track = darkMode ? new Color(52, 47, 66) : new Color(243, 240, 246);
        Color waiting = darkMode ? new Color(239, 157, 190) : new Color(200, 112, 145);
        Color turnaround = darkMode ? new Color(183, 151, 226) : new Color(137, 108, 177);
        int labelWidth = 45;
        int valueWidth = 38;
        int left = labelWidth;
        int right = Math.max(left + 30, getWidth() - valueWidth - 10);
        int width = right - left;
        int rowHeight = 34;
        int top = 7;
        Font body = new Font("Segoe UI", Font.PLAIN, 11);
        FontMetrics metrics = g.getFontMetrics(body);
        g.setFont(body);

        for (int i = 0; i < processes.size(); i++) {
            Process process = processes.get(i);
            int y = top + i * rowHeight;
            g.setColor(text);
            g.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g.drawString(process.name(), 0, y + 13);
            g.setFont(body);

            int turnaroundWidth = (int) Math.round(width * process.turnaround() / (double) maximum);
            int waitingWidth = (int) Math.round(width * process.waiting() / (double) maximum);
            g.setColor(track);
            g.fillRoundRect(left, y, width, 9, 8, 8);
            g.setColor(turnaround);
            g.fillRoundRect(left, y, Math.max(2, turnaroundWidth), 9, 8, 8);
            g.setColor(track);
            g.fillRoundRect(left, y + 14, width, 9, 8, 8);
            g.setColor(waiting);
            g.fillRoundRect(left, y + 14, Math.max(2, waitingWidth), 9, 8, 8);

            g.setColor(muted);
            String turnLabel = Integer.toString(process.turnaround());
            String waitLabel = Integer.toString(process.waiting());
            g.drawString(turnLabel, right + 8, y + metrics.getAscent());
            g.drawString(waitLabel, right + 8, y + 14 + metrics.getAscent());
        }
        g.dispose();
    }
}
