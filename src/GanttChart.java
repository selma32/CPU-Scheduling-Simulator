import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Interactive Java2D timeline backed directly by a scheduler result. */
public final class GanttChart extends JPanel {
    private static final Color[] LIGHT_PROCESS_COLORS = {
            new Color(195, 115, 145), new Color(133, 103, 176), new Color(211, 157, 83),
            new Color(90, 151, 139), new Color(182, 113, 171), new Color(97, 132, 182)
    };
    private static final Color[] DARK_PROCESS_COLORS = {
            new Color(238, 158, 187), new Color(188, 160, 231), new Color(239, 193, 128),
            new Color(131, 203, 185), new Color(222, 157, 214), new Color(153, 182, 233)
    };

    private final SimulationResult result;
    private final int totalTime;
    private boolean darkMode;
    private double revealedTime;
    private Timer playback;

    public GanttChart(SimulationResult result, boolean darkMode) {
        this.result = result;
        this.darkMode = darkMode;
        this.totalTime = Math.max(1, result.totalTime());
        this.revealedTime = totalTime;
        setOpaque(false);
        setPreferredSize(new Dimension((int) Math.min(3000L, Math.max(480L, (long) totalTime * 36 + 60)), 118));
        setFont(new Font("Segoe UI", Font.PLAIN, 12));
        setToolTipText("Hover over a segment to inspect its exact interval.");
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent event) {
                updateTooltip(event.getX());
            }
        });
    }

    public void play() {
        if (playback != null && playback.isRunning()) {
            playback.stop();
        }
        revealedTime = 0;
        final long started = System.nanoTime();
        playback = new Timer(25, event -> {
            double elapsed = (System.nanoTime() - started) / 1_000_000_000.0;
            double progress = Math.min(1.0, elapsed / 2.2);
            revealedTime = totalTime * progress;
            repaint();
            if (progress >= 1.0) {
                ((Timer) event.getSource()).stop();
                playback = null;
            }
        });
        playback.start();
    }

    public void stopPlayback() {
        if (playback != null) {
            playback.stop();
            playback = null;
        }
    }

    private void updateTooltip(int mouseX) {
        int left = 34;
        int usable = Math.max(1, getWidth() - left - 34);
        double time = Math.max(0, Math.min(totalTime, (mouseX - left) * (double) totalTime / usable));
        for (SimulationResult.Segment segment : result.timeline) {
            if (time >= segment.start && time < segment.end) {
                setToolTipText(segment.label + "  |  " + segment.start + " - " + segment.end
                        + "  |  " + (segment.end - segment.start) + " time unit"
                        + (segment.end - segment.start == 1 ? "" : "s"));
                return;
            }
        }
        setToolTipText("Time " + Math.round(time));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color text = darkMode ? new Color(230, 222, 238) : new Color(59, 54, 68);
        Color muted = darkMode ? new Color(165, 154, 180) : new Color(133, 125, 145);
        Color grid = darkMode ? new Color(75, 66, 89) : new Color(231, 225, 237);
        int left = 35;
        int right = Math.max(left + 1, getWidth() - 34);
        int width = right - left;
        int barY = 24;
        int barHeight = 43;
        FontMetrics metrics = g.getFontMetrics();

        g.setColor(darkMode ? new Color(33, 30, 43) : new Color(250, 248, 252));
        g.fillRoundRect(left, barY, width, barHeight, 12, 12);

        for (SimulationResult.Segment segment : result.timeline) {
            if (segment.start >= revealedTime) {
                continue;
            }
            double visibleEnd = Math.min(segment.end, revealedTime);
            int x = left + (int) Math.round((double) segment.start / totalTime * width);
            int endX = left + (int) Math.round(visibleEnd / totalTime * width);
            int segmentWidth = Math.max(1, endX - x);
            g.setColor(colorForLabel(segment.label, darkMode));
            g.fillRoundRect(x, barY, segmentWidth, barHeight, 9, 9);
            if (segmentWidth >= metrics.stringWidth(segment.label) + 12) {
                g.setColor(isSpecial(segment.label) ? text : Color.WHITE);
                g.drawString(segment.label, x + (segmentWidth - metrics.stringWidth(segment.label)) / 2,
                        barY + (barHeight + metrics.getAscent() - metrics.getDescent()) / 2);
            }
        }

        int tickStep = Math.max(1, (int) Math.ceil(totalTime / 16.0));
        g.setFont(getFont().deriveFont(10f));
        metrics = g.getFontMetrics();
        for (int time = 0; time <= totalTime; time += tickStep) {
            int x = left + (int) Math.round((double) time / totalTime * width);
            g.setColor(grid);
            g.drawLine(x, barY + barHeight + 4, x, barY + barHeight + 10);
            g.setColor(muted);
            String label = Integer.toString(time);
            g.drawString(label, x - metrics.stringWidth(label) / 2, barY + barHeight + 26);
        }
        if (totalTime % tickStep != 0) {
            String endLabel = Integer.toString(totalTime);
            g.setColor(muted);
            g.drawString(endLabel, right - metrics.stringWidth(endLabel) / 2, barY + barHeight + 26);
        }

        if (revealedTime > 0 && revealedTime < totalTime) {
            int playhead = left + (int) Math.round(revealedTime / totalTime * width);
            g.setColor(darkMode ? new Color(255, 223, 239) : new Color(132, 92, 161));
            g.setStroke(new java.awt.BasicStroke(2f));
            g.drawLine(playhead, barY - 5, playhead, barY + barHeight + 3);
            g.fillOval(playhead - 4, barY - 9, 8, 8);
        }
        g.dispose();
    }

    public static List<String> labelsIn(SimulationResult result) {
        Set<String> labels = new LinkedHashSet<>();
        for (SimulationResult.Segment segment : result.timeline) {
            labels.add(segment.label);
        }
        return new ArrayList<>(labels);
    }

    public static Color colorForLabel(String label, boolean darkMode) {
        if ("CS".equals(label)) {
            return darkMode ? new Color(110, 97, 127) : new Color(198, 190, 207);
        }
        if ("IDLE".equals(label)) {
            return darkMode ? new Color(62, 58, 72) : new Color(222, 218, 227);
        }
        String number = label.startsWith("P") ? label.substring(1) : label;
        int index;
        try {
            index = Math.max(0, Integer.parseInt(number) - 1);
        } catch (NumberFormatException exception) {
            index = Math.abs(label.hashCode());
        }
        Color[] colors = darkMode ? DARK_PROCESS_COLORS : LIGHT_PROCESS_COLORS;
        return colors[Math.floorMod(index, colors.length)];
    }

    private static boolean isSpecial(String label) {
        return "CS".equals(label) || "IDLE".equals(label);
    }
}
