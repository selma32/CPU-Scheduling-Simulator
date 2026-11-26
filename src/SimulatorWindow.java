import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/** Main Swing interface. It adapts the existing schedulers without changing their behavior. */
public final class SimulatorWindow extends JFrame {
    private static Color BACKGROUND = new Color(250, 248, 252);
    private static Color WHITE = Color.WHITE;
    private static Color PINK_TINT = new Color(252, 240, 244);
    private static Color PURPLE = new Color(146, 117, 181);
    private static Color TEXT = new Color(52, 49, 59);
    private static Color MUTED = new Color(130, 124, 140);
    private static Color BORDER = new Color(237, 232, 241);
    private static Color ACCENT = new Color(232, 166, 183);
    private static Color ACTION = new Color(133, 101, 171);
    private static Color SURFACE_ALT = new Color(248, 246, 250);
    private static Color GRID = new Color(237, 232, 241);
    private static Color TABLE_HEADER = new Color(248, 246, 250);
    private static Color TABLE_SELECTION = new Color(245, 229, 237);
    private static Color TABLE_SELECTION_TEXT = new Color(52, 49, 59);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 25);

    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pageContainer = new JPanel(pageLayout);
    private final List<JButton> navigationButtons = new ArrayList<>();
    private final List<GanttChart> ganttCharts = new ArrayList<>();
    private JPanel dashboardPage;
    private final ProcessInputTableModel processTableModel = new ProcessInputTableModel();
    private final JTable processTable = new JTable(processTableModel);
    private final JSpinner quantumSpinner = new JSpinner(new SpinnerNumberModel(3, 1, Integer.MAX_VALUE, 1));
    private final JSpinner contextSwitchSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
    private final JComboBox<String> schedulerSelector = new JComboBox<>(
            new String[]{"Preemptive SJF", "ACS", "Both (SJF + ACS)"});
    private final JButton runButton = primaryButton("Run simulation");
    private final JLabel runStatus = new JLabel("Ready when you are");
    private final JPanel resultsPage = new JPanel(new BorderLayout());
    private List<SimulationResult> latestResults = new ArrayList<>();
    private boolean darkMode;
    private boolean animateNextResult;
    private int currentPage;
    private Timer loadingTimer;
    private int loadingDots;
    private JLabel topContext;

    public SimulatorWindow() {
        super("CPU Scheduling Simulator");
        setPalette();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 680));
        setSize(1360, 900);
        setLocationRelativeTo(null);
        runButton.addActionListener(event -> runSimulation());
        rebuildWorkspace();
    }

    private void setPalette() {
        if (darkMode) {
            BACKGROUND = new Color(25, 23, 32);
            WHITE = new Color(37, 34, 47);
            PINK_TINT = new Color(64, 47, 67);
            PURPLE = new Color(205, 176, 236);
            TEXT = new Color(243, 238, 247);
            MUTED = new Color(173, 164, 185);
            BORDER = new Color(67, 60, 79);
            ACCENT = new Color(236, 165, 195);
            ACTION = new Color(131, 98, 167);
            SURFACE_ALT = new Color(45, 41, 57);
            GRID = new Color(75, 67, 88);
            TABLE_HEADER = new Color(48, 44, 61);
            TABLE_SELECTION = new Color(83, 61, 91);
            TABLE_SELECTION_TEXT = new Color(250, 245, 255);
        } else {
            BACKGROUND = new Color(250, 248, 252);
            WHITE = Color.WHITE;
            PINK_TINT = new Color(252, 240, 244);
            PURPLE = new Color(133, 101, 171);
            TEXT = new Color(52, 49, 59);
            MUTED = new Color(123, 116, 135);
            BORDER = new Color(232, 226, 238);
            ACCENT = new Color(216, 139, 165);
            ACTION = new Color(133, 101, 171);
            SURFACE_ALT = new Color(248, 246, 250);
            GRID = new Color(237, 232, 241);
            TABLE_HEADER = new Color(243, 239, 247);
            TABLE_SELECTION = new Color(245, 229, 237);
            TABLE_SELECTION_TEXT = new Color(52, 49, 59);
        }
        applyDefaults();
    }

    private void applyDefaults() {
        UIManager.put("Table.selectionBackground", TABLE_SELECTION);
        UIManager.put("Table.selectionForeground", TABLE_SELECTION_TEXT);
        UIManager.put("TabbedPane.selected", WHITE);
        UIManager.put("TabbedPane.contentAreaColor", WHITE);
        UIManager.put("ToolTip.background", WHITE);
        UIManager.put("ToolTip.foreground", TEXT);
        UIManager.put("TextField.background", WHITE);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("ComboBox.background", WHITE);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("Spinner.background", WHITE);
        UIManager.put("Spinner.foreground", TEXT);
    }

    private void rebuildWorkspace() {
        stopAndClearCharts();
        navigationButtons.clear();
        setPalette();
        getContentPane().removeAll();
        pageContainer.removeAll();
        resultsPage.removeAll();
        pageContainer.setBackground(BACKGROUND);
        dashboardPage = createDashboardPage();
        pageContainer.add(dashboardPage, "Dashboard");
        pageContainer.add(createSimulatorPage(), "Simulator");
        if (latestResults.isEmpty()) {
            showEmptyResults();
        } else {
            renderResults();
        }
        pageContainer.add(resultsPage, "Results");
        add(createTopBar(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(pageContainer, BorderLayout.CENTER);
        showPage(currentPage);
        revalidate();
        repaint();
    }

    private void stopAndClearCharts() {
        for (GanttChart chart : ganttCharts) {
            chart.stopPlayback();
        }
        ganttCharts.clear();
    }

    private void refreshDashboard() {
        if (dashboardPage == null) {
            return;
        }
        stopAndClearCharts();
        pageContainer.remove(dashboardPage);
        dashboardPage = createDashboardPage();
        pageContainer.add(dashboardPage, "Dashboard");
        pageContainer.revalidate();
        pageContainer.repaint();
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(18, 0));
        topBar.setBackground(WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                BorderFactory.createEmptyBorder(11, 24, 11, 24)));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 11, 0));
        brand.setOpaque(false);
        JLabel mark = new JLabel("S");
        mark.setOpaque(true);
        mark.setBackground(PINK_TINT);
        mark.setForeground(PURPLE);
        mark.setFont(new Font("Segoe UI", Font.BOLD, 17));
        mark.setHorizontalAlignment(SwingConstants.CENTER);
        mark.setPreferredSize(new Dimension(36, 36));
        JLabel name = new JLabel("Schedulia");
        name.setForeground(TEXT);
        name.setFont(new Font("Segoe UI", Font.BOLD, 17));
        JLabel tagline = new JLabel("  CPU SCHEDULING STUDIO");
        tagline.setForeground(MUTED);
        tagline.setFont(new Font("Segoe UI", Font.BOLD, 10));
        brand.add(mark);
        brand.add(name);
        brand.add(tagline);
        topBar.add(brand, BorderLayout.WEST);

        topContext = new JLabel();
        topContext.setForeground(MUTED);
        topContext.setFont(new Font("Segoe UI", Font.BOLD, 10));
        topContext.setHorizontalAlignment(SwingConstants.CENTER);
        topBar.add(topContext, BorderLayout.CENTER);

        JButton themeToggle = new StyledButton(darkMode ? "LIGHT MODE" : "DARK MODE",
                darkMode ? SURFACE_ALT : WHITE, TEXT, BORDER, 11);
        themeToggle.setToolTipText("Switch the complete interface between light and dark themes.");
        themeToggle.addActionListener(event -> {
            darkMode = !darkMode;
            rebuildWorkspace();
        });
        topBar.add(themeToggle, BorderLayout.EAST);
        return topBar;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(WHITE);
        sidebar.setPreferredSize(new Dimension(226, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER),
                BorderFactory.createEmptyBorder(27, 16, 20, 16)));

        JLabel section = new JLabel("WORKSPACE");
        section.setForeground(MUTED);
        section.setFont(new Font("Segoe UI", Font.BOLD, 10));
        section.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 0));
        section.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(section);

        addNavigationButton(sidebar, "Dashboard", 0);
        addNavigationButton(sidebar, "Simulator", 1);
        addNavigationButton(sidebar, "Results", 2);

        sidebar.add(Box.createVerticalGlue());
        JPanel note = new JPanel();
        note.setBackground(PINK_TINT);
        note.setLayout(new BoxLayout(note, BoxLayout.Y_AXIS));
        note.setBorder(BorderFactory.createEmptyBorder(13, 13, 13, 13));
        JLabel noteTitle = new JLabel("Reading the timeline");
        noteTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        noteTitle.setForeground(TEXT);
        JLabel noteText = new JLabel("<html>Each color marks a process.<br>CS and IDLE are separate states.</html>");
        noteText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        noteText.setForeground(MUTED);
        note.add(noteTitle);
        note.add(Box.createVerticalStrut(6));
        note.add(noteText);
        note.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(note);
        return sidebar;
    }

    private void addNavigationButton(JPanel sidebar, String label, int index) {
        JButton button = new StyledButton(label, WHITE, MUTED, BORDER, 12);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.addActionListener(event -> showPage(index));
        navigationButtons.add(button);
        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(5));
    }

    private void showPage(int index) {
        currentPage = index;
        String page = index == 0 ? "Dashboard" : index == 1 ? "Simulator" : "Results";
        pageLayout.show(pageContainer, page);
        for (int i = 0; i < navigationButtons.size(); i++) {
            boolean selected = i == index;
            JButton button = navigationButtons.get(i);
            button.setBackground(selected ? PINK_TINT : WHITE);
            button.setForeground(selected ? PURPLE : MUTED);
            button.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 10));
        }
        if (topContext != null) {
            String[] contexts = {"STUDIO / OVERVIEW", "STUDIO / SIMULATION WORKSPACE",
                    "STUDIO / PERFORMANCE ANALYSIS"};
            topContext.setText(contexts[index]);
        }
    }

    private JPanel createDashboardPage() {
        JPanel content = new JPanel(new BorderLayout(0, 15));
        content.setBackground(BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(25, 30, 30, 30));
        content.add(pageHeading("Scheduling studio",
                "Shape a workload. Watch the scheduler turn it into a timeline."), BorderLayout.NORTH);
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        GradientPanel hero = new GradientPanel();
        hero.setLayout(new BorderLayout(18, 14));
        hero.setBorder(BorderFactory.createEmptyBorder(21, 24, 20, 24));
        JPanel heroCopy = new JPanel(new BorderLayout(10, 0));
        heroCopy.setOpaque(false);
        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel(latestResults.isEmpty() ? "READY TO EXPLORE" : "LATEST RUN  /  " + latestResults.size() + " SCHEDULER" + (latestResults.size() == 1 ? "" : "S"));
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 10));
        eyebrow.setForeground(PURPLE);
        JLabel title = new JLabel(latestResults.isEmpty()
                ? "<html>From arrivals to a living<br>execution trace.</html>"
                : "<html>" + latestResults.get(0).schedulerName + "<br>execution trace</html>");
        title.setFont(new Font("Segoe UI", Font.BOLD, 27));
        title.setForeground(TEXT);
        JLabel description = new JLabel(latestResults.isEmpty()
                ? "<html>Use your actual process inputs to build a schedule.<br>Every block and metric comes straight from the simulator.</html>"
                : "<html>" + latestResults.get(0).processes.size() + " processes  /  "
                    + latestResults.get(0).timeline.size() + " timeline blocks  /  "
                    + latestResults.get(0).totalTime() + " time units</html>");
        description.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        description.setForeground(MUTED);
        JButton configure = primaryButton("Configure a simulation");
        configure.addActionListener(event -> showPage(1));
        copy.add(eyebrow);
        copy.add(Box.createVerticalStrut(7));
        copy.add(title);
        copy.add(Box.createVerticalStrut(7));
        copy.add(description);
        JPanel actionArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionArea.setOpaque(false);
        actionArea.add(configure);
        heroCopy.add(copy, BorderLayout.CENTER);
        heroCopy.add(actionArea, BorderLayout.EAST);
        hero.add(heroCopy, BorderLayout.NORTH);

        JPanel visualization = new JPanel(new BorderLayout(0, 9));
        visualization.setOpaque(false);
        JPanel visualHeader = new JPanel(new BorderLayout());
        visualHeader.setOpaque(false);
        JLabel visualTitle = new JLabel(latestResults.isEmpty() ? "EXECUTION CANVAS" : "SCHEDULE TRACE");
        visualTitle.setForeground(TEXT);
        visualTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        visualHeader.add(visualTitle, BorderLayout.WEST);
        if (!latestResults.isEmpty()) {
            JButton replay = secondaryButton("Replay");
            replay.setToolTipText("Animate the actual recorded timeline from start to finish.");
            visualHeader.add(replay, BorderLayout.EAST);
            GanttChart chart = new GanttChart(latestResults.get(0), darkMode);
            ganttCharts.add(chart);
            replay.addActionListener(event -> chart.play());
            visualization.add(visualHeader, BorderLayout.NORTH);
            visualization.add(chart, BorderLayout.CENTER);
            visualization.add(createLegend(latestResults.get(0)), BorderLayout.SOUTH);
        } else {
            visualization.add(visualHeader, BorderLayout.NORTH);
            visualization.add(new EmptyTimelineCanvas(), BorderLayout.CENTER);
        }
        hero.add(visualization, BorderLayout.CENTER);
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, latestResults.isEmpty() ? 334 : 370));
        body.add(hero);
        body.add(Box.createVerticalStrut(14));

        boolean narrow = getWidth() < 1120;
        JPanel lower = narrow
                ? new JPanel(new GridLayout(2, 1, 0, 12))
                : new JPanel(new GridLayout(1, 2, 14, 0));
        lower.setOpaque(false);
        lower.addComponentListener(new java.awt.event.ComponentAdapter() {
            private boolean stacked = narrow;

            @Override
            public void componentResized(java.awt.event.ComponentEvent event) {
                boolean shouldStack = lower.getWidth() < 820;
                if (shouldStack != stacked) {
                    stacked = shouldStack;
                    lower.setLayout(shouldStack
                            ? new GridLayout(2, 1, 0, 12)
                            : new GridLayout(1, 2, 14, 0));
                    lower.setMaximumSize(new Dimension(Integer.MAX_VALUE, shouldStack ? 300 : 155));
                    lower.revalidate();
                }
            }
        });
        lower.setMaximumSize(new Dimension(Integer.MAX_VALUE, narrow ? 300 : 155));
        lower.add(workflowPanel());
        lower.add(workloadSummaryPanel());
        body.add(lower);
        content.add(body, BorderLayout.CENTER);

        return scrollPage(content);
    }

    private JPanel workflowPanel() {
        JPanel panel = cardPanel();
        panel.setLayout(new BorderLayout(0, 12));
        panel.add(sectionHeading("How it flows", "The interface visualizes the scheduler output, not a second simulation."), BorderLayout.NORTH);
        JPanel steps = new JPanel(new GridLayout(1, 3, 5, 0));
        steps.setOpaque(false);
        steps.add(flowStep("01", "Workload", "Arrival / burst"));
        steps.add(flowStep("02", "Scheduler", "SJF or ACS"));
        steps.add(flowStep("03", "Timeline", "Actual segments"));
        panel.add(steps, BorderLayout.CENTER);
        return panel;
    }

    private JPanel flowStep(String number, String title, String detail) {
        JPanel step = new JPanel();
        step.setOpaque(false);
        step.setLayout(new BoxLayout(step, BoxLayout.Y_AXIS));
        JLabel badge = new JLabel(number);
        badge.setForeground(PURPLE);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        JLabel name = new JLabel(title);
        name.setForeground(TEXT);
        name.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel caption = new JLabel(detail);
        caption.setForeground(MUTED);
        caption.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        step.add(badge);
        step.add(Box.createVerticalStrut(4));
        step.add(name);
        step.add(Box.createVerticalStrut(3));
        step.add(caption);
        return step;
    }

    private JPanel workloadSummaryPanel() {
        JPanel panel = cardPanel();
        panel.setLayout(new BorderLayout(0, 10));
        int processCount = processTableModel.getRowCount();
        panel.add(sectionHeading("Current workload", processCount + " process" + (processCount == 1 ? "" : "es")
                + " in the input table"), BorderLayout.NORTH);
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        chips.setOpaque(false);
        for (int row = 0; row < Math.min(processCount, 6); row++) {
            String processName = "P" + (row + 1);
            chips.add(processChip(processName, GanttChart.colorForLabel(processName, darkMode)));
        }
        if (processCount > 6) {
            chips.add(processChip("+" + (processCount - 6), PURPLE));
        }
        panel.add(chips, BorderLayout.CENTER);
        JButton edit = secondaryButton("Edit workload");
        edit.addActionListener(event -> showPage(1));
        panel.add(edit, BorderLayout.SOUTH);
        return panel;
    }

    private JLabel processChip(String label, Color color) {
        JLabel chip = new JLabel(label);
        chip.setOpaque(true);
        chip.setBackground(PINK_TINT);
        chip.setForeground(color);
        chip.setFont(new Font("Segoe UI", Font.BOLD, 11));
        chip.setHorizontalAlignment(SwingConstants.CENTER);
        chip.setBorder(BorderFactory.createEmptyBorder(5, 9, 5, 9));
        return chip;
    }

    private JPanel createLegend(SimulationResult result) {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        legend.setOpaque(false);
        for (String label : GanttChart.labelsIn(result)) {
            JLabel item = new JLabel(label);
            item.setIcon(new ColorSwatch(GanttChart.colorForLabel(label, darkMode)));
            item.setIconTextGap(5);
            item.setForeground(MUTED);
            item.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            legend.add(item);
        }
        return legend;
    }

    private JPanel createSimulatorPage() {
        JPanel content = new JPanel();
        content.setBackground(BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(34, 38, 34, 38));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(pageHeading("Simulation setup", "Enter your workload and choose which existing scheduler to run."));
        content.add(Box.createVerticalStrut(20));

        JPanel controls = cardPanel();
        controls.setLayout(new BorderLayout(0, 16));
        controls.add(sectionHeading("Simulation parameters", "Time quantum and context-switch time are used by the existing scheduler API."), BorderLayout.NORTH);
        JPanel form = new JPanel(new GridLayout(1, 3, 16, 0));
        form.setOpaque(false);
        form.add(labeledControl("Scheduling algorithm", schedulerSelector,
                "Choose Preemptive SJF, ACS, or run both algorithms."));
        form.add(labeledControl("Time quantum (Q)", quantumSpinner,
                "Q must be a positive integer."));
        form.add(labeledControl("Context switch (CS)", contextSwitchSpinner,
                "CS must be zero or a positive integer."));
        controls.add(form, BorderLayout.CENTER);
        controls.setMaximumSize(new Dimension(Integer.MAX_VALUE, 154));
        content.add(controls);
        content.add(Box.createVerticalStrut(16));

        JPanel processCard = cardPanel();
        processCard.setLayout(new BorderLayout(0, 14));
        JPanel processHeader = new JPanel(new BorderLayout(14, 0));
        processHeader.setOpaque(false);
        processHeader.add(sectionHeading("Process workload", "Lower priority numbers represent higher priority in ACS."), BorderLayout.CENTER);
        JPanel rowActions = new JPanel();
        rowActions.setOpaque(false);
        JButton add = secondaryButton("+  Add process");
        add.setToolTipText("Add another process row.");
        add.addActionListener(event -> {
            processTableModel.addProcess();
            int lastRow = processTableModel.getRowCount() - 1;
            processTable.setRowSelectionInterval(lastRow, lastRow);
        });
        JButton remove = secondaryButton("Remove selected");
        remove.setToolTipText("Remove the selected row. At least one process is required.");
        remove.addActionListener(event -> {
            int selected = processTable.getSelectedRow();
            if (selected < 0) {
                showInputError("Select a process row to remove.");
            } else {
                processTableModel.removeProcess(processTable.convertRowIndexToModel(selected));
            }
        });
        rowActions.add(add);
        rowActions.add(remove);
        processHeader.add(rowActions, BorderLayout.EAST);
        processCard.add(processHeader, BorderLayout.NORTH);

        styleInputTable(processTable);
        processTable.setToolTipText("Enter non-negative arrival times, positive burst times, and integer priorities.");
        JScrollPane tableScroll = new JScrollPane(processTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(BORDER));
        tableScroll.setPreferredSize(new Dimension(680, 224));
        processCard.add(tableScroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(16, 0));
        footer.setOpaque(false);
        runStatus.setForeground(MUTED);
        runStatus.setFont(BODY_FONT);
        footer.add(runStatus, BorderLayout.CENTER);
        runButton.setToolTipText("Validate the inputs and run the selected existing scheduler.");
        footer.add(runButton, BorderLayout.EAST);
        processCard.add(footer, BorderLayout.SOUTH);
        processCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 430));
        content.add(processCard);
        return scrollPage(content);
    }

    private void runSimulation() {
        if (processTable.isEditing() && !processTable.getCellEditor().stopCellEditing()) {
            showInputError("Finish editing the selected process value before running.");
            return;
        }

        List<Process> inputs = new ArrayList<>();
        for (int row = 0; row < processTableModel.getRowCount(); row++) {
            Integer arrivalValue = parseInteger(processTableModel.getInput(row, 0),
                    "Arrival time for P" + (row + 1), row, 1);
            if (arrivalValue == null) {
                return;
            }
            Integer burstValue = parseInteger(processTableModel.getInput(row, 1),
                    "Burst time for P" + (row + 1), row, 2);
            if (burstValue == null) {
                return;
            }
            int burst = burstValue;
            if (burst <= 0) {
                showInputError("Burst time for P" + (row + 1) + " must be greater than zero.");
                return;
            }
            Integer priorityValue = parseInteger(processTableModel.getInput(row, 2),
                    "Priority for P" + (row + 1), row, 3);
            if (priorityValue == null) {
                return;
            }
            inputs.add(new Process(row + 1, arrivalValue, burst, priorityValue));
        }

        int quantum = (Integer) quantumSpinner.getValue();
        int contextSwitch = (Integer) contextSwitchSpinner.getValue();
        String selection = (String) schedulerSelector.getSelectedItem();
        List<Scheduler> schedulers = new ArrayList<>();
        if ("Preemptive SJF".equals(selection) || "Both (SJF + ACS)".equals(selection)) {
            schedulers.add(new SJF());
        }
        if ("ACS".equals(selection) || "Both (SJF + ACS)".equals(selection)) {
            schedulers.add(new ACS());
        }

        runButton.setEnabled(false);
        startLoadingIndicator();
        new SwingWorker<RunOutcome, Void>() {
            @Override
            protected RunOutcome doInBackground() {
                RunOutcome outcome = new RunOutcome();
                for (Scheduler scheduler : schedulers) {
                    try {
                        outcome.results.add(scheduler.run(inputs, quantum, contextSwitch));
                    } catch (UnsupportedOperationException exception) {
                        outcome.skippedSchedulers.add(scheduler.getClass().getSimpleName());
                    }
                }
                return outcome;
            }

            @Override
            protected void done() {
                stopLoadingIndicator();
                runButton.setEnabled(true);
                try {
                    RunOutcome outcome = get();
                    if (outcome.results.isEmpty()) {
                        runStatus.setText("No scheduler was available to run.");
                        showInputError("The selected scheduler is not implemented yet.");
                        return;
                    }
                    latestResults = new ArrayList<>(outcome.results);
                    refreshDashboard();
                    animateNextResult = true;
                    renderResults();
                    animateNextResult = false;
                    showPage(0);
                    runStatus.setText("Simulation complete");
                    if (!outcome.skippedSchedulers.isEmpty()) {
                        JOptionPane.showMessageDialog(SimulatorWindow.this,
                                String.join(", ", outcome.skippedSchedulers) + " is not implemented yet and was skipped.",
                                "Simulation complete with a skipped scheduler", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    runStatus.setText("Simulation interrupted.");
                    showInputError("The simulation was interrupted. Please try again.");
                } catch (ExecutionException exception) {
                    runStatus.setText("Simulation could not be completed.");
                    Throwable cause = exception.getCause();
                    showInputError("The simulator reported an error: "
                            + (cause == null ? exception.getMessage() : cause.getMessage()));
                }
            }
        }.execute();
    }

    private void startLoadingIndicator() {
        runStatus.setForeground(PURPLE);
        loadingDots = 0;
        loadingTimer = new Timer(320, event -> {
            loadingDots = (loadingDots + 1) % 4;
            runStatus.setText("Running simulation" + ".".repeat(loadingDots));
        });
        runStatus.setText("Running simulation");
        loadingTimer.start();
    }

    private void stopLoadingIndicator() {
        if (loadingTimer != null) {
            loadingTimer.stop();
            loadingTimer = null;
        }
    }

    private Integer parseInteger(String value, String label, int row, int column) {
        try {
            int parsed = Integer.parseInt(value);
            if (column == 1 && parsed < 0) {
                showInputError(label + " must be zero or greater.");
                return null;
            }
            processTable.setRowSelectionInterval(row, row);
            processTable.setColumnSelectionInterval(column, column);
            return parsed;
        } catch (NumberFormatException exception) {
            showInputError(label + " must be a whole number.");
            processTable.setRowSelectionInterval(row, row);
            processTable.setColumnSelectionInterval(column, column);
            return null;
        }
    }

    private void renderResults() {
        resultsPage.removeAll();
        JPanel content = new JPanel();
        content.setBackground(BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(34, 38, 34, 38));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(pageHeading("Simulation results", "A clear summary of the scheduler output, from timeline to per-process metrics."));
        content.add(Box.createVerticalStrut(18));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(BODY_FONT);
        tabs.setBackground(WHITE);
        tabs.setForeground(TEXT);
        tabs.setUI(new ThemeTabbedPaneUI());
        for (SimulationResult result : latestResults) {
            tabs.addTab(result.schedulerName, createResultView(result));
        }
        animateNextResult = false;
        tabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        tabs.setPreferredSize(new Dimension(760, 700));
        tabs.setMaximumSize(new Dimension(Integer.MAX_VALUE, 760));
        tabs.setMinimumSize(new Dimension(0, 0));
        tabs.setAlignmentX(LEFT_ALIGNMENT);
        content.add(tabs);
        JScrollPane outerScroll = new JScrollPane(new ScrollableColumnPanel(content));
        outerScroll.setBorder(null);
        outerScroll.getViewport().setBackground(BACKGROUND);
        outerScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        installScrollBarTheme(outerScroll);
        resultsPage.add(outerScroll, BorderLayout.CENTER);
        resultsPage.revalidate();
        resultsPage.repaint();
    }

    private JScrollPane createResultView(SimulationResult result) {
        JPanel content = new JPanel();
        content.setBackground(BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(18, 12, 24, 12));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setAlignmentX(LEFT_ALIGNMENT);

        JPanel metrics = new JPanel(new GridLayout(1, 4, 12, 0));
        metrics.setOpaque(false);
        metrics.add(metricCard("Avg. turnaround", format(Metrics.avgTurnaround(result.processes)), "time units"));
        metrics.add(metricCard("Avg. waiting", format(Metrics.avgWaiting(result.processes)), "time units"));
        metrics.add(metricCard("Avg. response", format(Metrics.avgResponse(result.processes)), "time units"));
        metrics.add(metricCard("CPU utilization", String.format(Locale.US, "%.1f%%", result.cpuUtilization()),
                result.totalTime() + " total time units"));
        metrics.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));
        metrics.setAlignmentX(LEFT_ALIGNMENT);
        content.add(metrics);
        content.add(Box.createVerticalStrut(16));

        JPanel timelineCard = cardPanel();
        timelineCard.setLayout(new BorderLayout(0, 12));
        JPanel timelineHeading = new JPanel(new BorderLayout(12, 0));
        timelineHeading.setOpaque(false);
        timelineHeading.add(sectionHeading("Execution timeline",
                "Color-coded CPU intervals from the real " + result.schedulerName + " run."), BorderLayout.CENTER);
        JButton replay = secondaryButton("Replay trace");
        replay.setToolTipText("Animate the existing result timeline; this does not rerun or alter the simulation.");
        timelineHeading.add(replay, BorderLayout.EAST);
        timelineCard.add(timelineHeading, BorderLayout.NORTH);
        GanttChart chart = new GanttChart(result, darkMode);
        ganttCharts.add(chart);
        if (animateNextResult) {
            chart.play();
            animateNextResult = false;
        }
        replay.addActionListener(event -> chart.play());
        JScrollPane chartScroll = new JScrollPane(chart,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        chartScroll.setBorder(BorderFactory.createLineBorder(BORDER));
        chartScroll.getViewport().setBackground(WHITE);
        chartScroll.setPreferredSize(new Dimension(760, 138));
        timelineCard.add(chartScroll, BorderLayout.CENTER);
        timelineCard.add(createLegend(result), BorderLayout.SOUTH);
        timelineCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 222));
        content.add(timelineCard);
        content.add(Box.createVerticalStrut(14));

        JPanel metricChartCard = cardPanel();
        metricChartCard.setLayout(new BorderLayout(0, 10));
        JPanel metricHeader = new JPanel(new BorderLayout());
        metricHeader.setOpaque(false);
        metricHeader.add(sectionHeading("Per-process timing",
                "Compare each process's waiting and turnaround time, in scheduler time units."), BorderLayout.CENTER);
        JPanel metricLegend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        metricLegend.setOpaque(false);
        metricLegend.add(legendItem("Turnaround", darkMode ? new Color(183, 151, 226) : new Color(137, 108, 177)));
        metricLegend.add(legendItem("Waiting", darkMode ? new Color(239, 157, 190) : new Color(200, 112, 145)));
        metricHeader.add(metricLegend, BorderLayout.EAST);
        metricChartCard.add(metricHeader, BorderLayout.NORTH);
        ProcessMetricsChart metricChart = new ProcessMetricsChart(result, darkMode);
        metricChartCard.add(metricChart, BorderLayout.CENTER);
        metricChartCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                Math.max(175, Math.min(290, 92 + result.processes.size() * 36))));
        content.add(metricChartCard);
        content.add(Box.createVerticalStrut(14));

        JPanel processResultsCard = cardPanel();
        processResultsCard.setLayout(new BorderLayout(0, 12));
        processResultsCard.add(sectionHeading("Per-process results", "Turnaround = finish - arrival; waiting = turnaround - burst."), BorderLayout.NORTH);
        processResultsCard.add(tableScroll(createProcessResultsTable(result)), BorderLayout.CENTER);
        processResultsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        content.add(processResultsCard);
        content.add(Box.createVerticalStrut(14));

        JPanel statesCard = cardPanel();
        statesCard.setLayout(new BorderLayout(0, 12));
        statesCard.add(sectionHeading("Simulation steps", "Queue snapshots and CPU state exactly as recorded by the scheduler."), BorderLayout.NORTH);
        statesCard.add(tableScroll(createStepsTable(result)), BorderLayout.CENTER);
        statesCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 370));
        content.add(statesCard);

        JScrollPane resultScroll = new JScrollPane(new ScrollableColumnPanel(content));
        resultScroll.setBorder(null);
        resultScroll.getVerticalScrollBar().setUnitIncrement(18);
        resultScroll.getViewport().setBackground(BACKGROUND);
        resultScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        installScrollBarTheme(resultScroll);
        return resultScroll;
    }

    private JLabel legendItem(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setIcon(new ColorSwatch(color));
        label.setIconTextGap(5);
        label.setForeground(MUTED);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        return label;
    }

    private JTable createProcessResultsTable(SimulationResult result) {
        List<Process> processes = new ArrayList<>(result.processes);
        processes.sort((first, second) -> Integer.compare(first.id, second.id));
        Object[][] data = new Object[processes.size()][7];
        for (int row = 0; row < processes.size(); row++) {
            Process process = processes.get(row);
            data[row] = new Object[]{process.name(), process.arrival, process.burst, process.finish,
                    process.turnaround(), process.waiting(), process.response()};
        }
        return readOnlyTable(data, new String[]{"Process", "Arrival", "Burst", "Finish", "Turnaround", "Waiting", "Response"});
    }

    private JTable createStepsTable(SimulationResult result) {
        Object[][] data = new Object[result.steps.size()][4];
        for (int row = 0; row < result.steps.size(); row++) {
            SimulationResult.Step step = result.steps.get(row);
            data[row] = new Object[]{step.time, step.running, step.tier1, step.tier2};
        }
        return readOnlyTable(data, new String[]{"Time", "CPU", "Tier 1 queue", "Tier 2 queue"});
    }

    private JTable readOnlyTable(Object[][] data, String[] columns) {
        JTable table = new JTable(new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        styleTable(table);
        return table;
    }

    private void showEmptyResults() {
        resultsPage.setBackground(BACKGROUND);
        JPanel empty = new JPanel(new BorderLayout());
        empty.setBackground(BACKGROUND);
        JPanel pageContent = new JPanel();
        pageContent.setBackground(BACKGROUND);
        pageContent.setBorder(BorderFactory.createEmptyBorder(30, 34, 30, 34));
        pageContent.setLayout(new BoxLayout(pageContent, BoxLayout.Y_AXIS));
        pageContent.add(pageHeading("Analysis", "Run a scheduler to reveal a trace and per-process comparisons."));
        pageContent.add(Box.createVerticalStrut(18));

        JPanel canvasCard = cardPanel();
        canvasCard.setLayout(new BorderLayout(0, 12));
        canvasCard.add(sectionHeading("Execution trace", "The timeline is drawn from actual scheduler segments."), BorderLayout.NORTH);
        canvasCard.add(new EmptyTimelineCanvas(), BorderLayout.CENTER);
        canvasCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 270));
        pageContent.add(canvasCard);
        pageContent.add(Box.createVerticalStrut(14));

        JPanel prompt = cardPanel();
        prompt.setLayout(new BorderLayout(16, 0));
        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Your analysis starts with a workload");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(TEXT);
        JLabel detail = new JLabel("Process times, queue history, and CPU metrics will appear here after a run.");
        detail.setFont(BODY_FONT);
        detail.setForeground(MUTED);
        copy.add(title);
        copy.add(Box.createVerticalStrut(6));
        copy.add(detail);
        JButton action = primaryButton("Set up simulation");
        action.addActionListener(event -> showPage(1));
        prompt.add(copy, BorderLayout.CENTER);
        prompt.add(action, BorderLayout.EAST);
        pageContent.add(prompt);
        empty.add(pageContent, BorderLayout.CENTER);
        resultsPage.add(empty, BorderLayout.CENTER);
    }

    private JPanel pageHeading(String title, String subtitle) {
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT);
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(BODY_FONT);
        subtitleLabel.setForeground(MUTED);
        heading.add(titleLabel);
        heading.add(Box.createVerticalStrut(5));
        heading.add(subtitleLabel);
        heading.setAlignmentX(LEFT_ALIGNMENT);
        heading.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        return heading;
    }

    private JPanel sectionHeading(String title, String subtitle) {
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT);
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(MUTED);
        heading.add(titleLabel);
        heading.add(Box.createVerticalStrut(4));
        heading.add(subtitleLabel);
        return heading;
    }

    private JPanel labeledControl(String label, JComponent control, String tooltip) {
        JPanel field = new JPanel();
        field.setOpaque(false);
        field.setLayout(new BoxLayout(field, BoxLayout.Y_AXIS));
        JLabel caption = new JLabel(label);
        caption.setFont(new Font("Segoe UI", Font.BOLD, 12));
        caption.setForeground(TEXT);
        control.setToolTipText(tooltip);
        control.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        control.setBackground(WHITE);
        control.setForeground(TEXT);
        if (control instanceof JSpinner) {
            JComponent editor = ((JSpinner) control).getEditor();
            editor.setBackground(WHITE);
            editor.setForeground(TEXT);
            for (java.awt.Component child : editor.getComponents()) {
                if (child instanceof JTextField) {
                    child.setBackground(WHITE);
                    child.setForeground(TEXT);
                    ((JTextField) child).setCaretColor(PURPLE);
                }
            }
        }
        field.add(caption);
        field.add(Box.createVerticalStrut(7));
        field.add(control);
        return field;
    }

    private JPanel metricCard(String label, String value, String detail) {
        JPanel card = cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel caption = new JLabel(label);
        caption.setForeground(MUTED);
        caption.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        JLabel metric = new JLabel(value);
        metric.setForeground(PURPLE);
        metric.setFont(new Font("Segoe UI", Font.BOLD, 22));
        JLabel subtext = new JLabel(detail);
        subtext.setForeground(MUTED);
        subtext.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        card.add(caption);
        card.add(Box.createVerticalStrut(4));
        card.add(metric);
        card.add(Box.createVerticalStrut(2));
        card.add(subtext);
        return card;
    }

    private JPanel cardPanel() {
        JPanel card = new RoundedPanel();
        card.setBorder(BorderFactory.createEmptyBorder(17, 18, 17, 18));
        card.setAlignmentX(LEFT_ALIGNMENT);
        return card;
    }

    private JPanel scrollPage(JPanel content) {
        JScrollPane scroll = new JScrollPane(new ScrollableColumnPanel(content));
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        installScrollBarTheme(scroll);
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(BACKGROUND);
        page.add(scroll, BorderLayout.CENTER);
        return page;
    }

    private JScrollPane tableScroll(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(WHITE);
        scroll.setPreferredSize(new Dimension(760, 154));
        installScrollBarTheme(scroll);
        return scroll;
    }

    private void installScrollBarTheme(JScrollPane scroll) {
        scroll.getVerticalScrollBar().setUI(new ThemedScrollBarUI());
        scroll.getVerticalScrollBar().setBackground(BACKGROUND);
        scroll.getHorizontalScrollBar().setUI(new ThemedScrollBarUI());
        scroll.getHorizontalScrollBar().setBackground(BACKGROUND);
    }

    private void styleInputTable(JTable table) {
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        styleTable(table);
        table.setRowHeight(34);
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(0).setMaxWidth(120);
        for (int column = 1; column < table.getColumnCount(); column++) {
            table.getColumnModel().getColumn(column).setPreferredWidth(150);
        }
    }

    private void styleTable(JTable table) {
        table.setFont(BODY_FONT);
        table.setForeground(TEXT);
        table.setBackground(WHITE);
        table.setGridColor(BORDER);
        table.setRowHeight(30);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setForeground(TEXT);
        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        table.getTableHeader().setOpaque(true);
        table.getTableHeader().setDefaultRenderer(new ThemedHeaderRenderer());
        table.setSelectionBackground(TABLE_SELECTION);
        table.setSelectionForeground(TABLE_SELECTION_TEXT);
        ThemedCellRenderer renderer = new ThemedCellRenderer();
        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(String.class, renderer);
        table.setDefaultRenderer(Number.class, renderer);
    }

    private JButton primaryButton(String text) {
        return new StyledButton(text, ACTION, WHITE, null, 13);
    }

    private JButton secondaryButton(String text) {
        return new StyledButton(text, WHITE, PURPLE, BORDER, 12);
    }

    private static String format(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private void showInputError(String message) {
        JOptionPane.showMessageDialog(this, message, "Check your inputs", JOptionPane.WARNING_MESSAGE);
    }

    private static final class RunOutcome {
        private final List<SimulationResult> results = new ArrayList<>();
        private final List<String> skippedSchedulers = new ArrayList<>();
    }

    private static final class ScrollableColumnPanel extends JPanel implements javax.swing.Scrollable {
        private ScrollableColumnPanel(JPanel content) {
            setLayout(new BorderLayout());
            setBackground(BACKGROUND);
            add(content, BorderLayout.CENTER);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return 20;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return Math.max(20, visibleRect.height - 20);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private final class ThemeTabbedPaneUI extends BasicTabbedPaneUI {
        @Override
        protected void paintTabBackground(java.awt.Graphics graphics, int tabPlacement, int tabIndex,
                int x, int y, int width, int height, boolean selected) {
            graphics.setColor(selected ? WHITE : SURFACE_ALT);
            graphics.fillRoundRect(x + 2, y + 2, Math.max(0, width - 4), height, 10, 10);
        }

        @Override
        protected void paintTabBorder(java.awt.Graphics graphics, int tabPlacement, int tabIndex,
                int x, int y, int width, int height, boolean selected) {
        }

        @Override
        protected void paintText(java.awt.Graphics graphics, int tabPlacement, Font font,
                java.awt.FontMetrics metrics, int tabIndex, String title,
                java.awt.Rectangle textRect, boolean selected) {
            graphics.setFont(font.deriveFont(Font.BOLD, 12f));
            graphics.setColor(selected ? PURPLE : MUTED);
            graphics.drawString(title, textRect.x, textRect.y + metrics.getAscent());
        }

        @Override
        protected void paintContentBorder(java.awt.Graphics graphics, int tabPlacement, int selectedIndex) {
            java.awt.Insets insets = tabPane.getInsets();
            int tabHeight = calculateTabAreaHeight(JTabbedPane.TOP, runCount, maxTabHeight);
            int x = insets.left;
            int y = insets.top + tabHeight - 2;
            int width = Math.max(0, tabPane.getWidth() - insets.left - insets.right - 1);
            int height = Math.max(0, tabPane.getHeight() - y - insets.bottom - 1);
            graphics.setColor(WHITE);
            graphics.fillRoundRect(x, y, width, height, 15, 15);
            graphics.setColor(BORDER);
            graphics.drawRoundRect(x, y, width, height, 15, 15);
        }
    }

    private final class RoundedPanel extends JPanel {
        private RoundedPanel() {
            setOpaque(false);
            setBackground(WHITE);
        }

        @Override
        protected void paintComponent(java.awt.Graphics graphics) {
            java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(darkMode ? new Color(155, 124, 191, 25) : new Color(52, 49, 59, 12));
            g.fillRoundRect(1, 2, getWidth() - 3, getHeight() - 3, 18, 18);
            g.setColor(WHITE);
            g.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 5, 18, 18);
            g.setColor(BORDER);
            g.drawRoundRect(0, 0, getWidth() - 3, getHeight() - 5, 18, 18);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private final class StyledButton extends JButton {
        private final Color outline;
        private float hoverAmount;
        private float hoverTarget;
        private Timer hoverTimer;

        private StyledButton(String text, Color background, Color foreground, Color outline, int fontSize) {
            super(text);
            this.outline = outline;
            setFont(new Font("Segoe UI", Font.BOLD, fontSize));
            setForeground(foreground);
            setBackground(background);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(9, 15, 9, 15));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent event) {
                    animateHover(1f);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent event) {
                    animateHover(0f);
                }
            });
        }

        private void animateHover(float target) {
            hoverTarget = target;
            if (hoverTimer != null && hoverTimer.isRunning()) {
                return;
            }
            hoverTimer = new Timer(15, event -> {
                float difference = hoverTarget - hoverAmount;
                if (Math.abs(difference) < 0.04f) {
                    hoverAmount = hoverTarget;
                    ((Timer) event.getSource()).stop();
                } else {
                    hoverAmount += difference * 0.32f;
                }
                repaint();
            });
            hoverTimer.start();
        }

        @Override
        protected void paintComponent(java.awt.Graphics graphics) {
            java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = getBackground();
            if (!isEnabled()) {
                fill = new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 110);
            } else if (hoverAmount > 0) {
                Color destination = outline == null
                        ? (darkMode ? new Color(155, 124, 191) : new Color(168, 139, 198))
                        : (darkMode ? SURFACE_ALT : PINK_TINT);
                fill = blend(fill, destination, hoverAmount * 0.42f);
            }
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            if (outline != null) {
                g.setColor(outline);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            }
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static Color blend(Color start, Color end, float amount) {
        float bounded = Math.max(0, Math.min(1, amount));
        return new Color(
                (int) (start.getRed() + (end.getRed() - start.getRed()) * bounded),
                (int) (start.getGreen() + (end.getGreen() - start.getGreen()) * bounded),
                (int) (start.getBlue() + (end.getBlue() - start.getBlue()) * bounded));
    }

    private final class ThemedCellRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focused, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, focused, row, column);
            setFont(BODY_FONT);
            setOpaque(true);
            setForeground(selected ? TABLE_SELECTION_TEXT : TEXT);
            setBackground(selected ? TABLE_SELECTION : (row % 2 == 0 ? WHITE : SURFACE_ALT));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, GRID),
                    BorderFactory.createEmptyBorder(0, 9, 0, 9)));
            return this;
        }
    }

    private final class ThemedHeaderRenderer extends DefaultTableCellRenderer {
        private ThemedHeaderRenderer() {
            setHorizontalAlignment(SwingConstants.LEFT);
            setOpaque(true);
        }

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focused, int row, int column) {
            super.getTableCellRendererComponent(table, value, false, false, row, column);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(TEXT);
            setBackground(TABLE_HEADER);
            setOpaque(true);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                    BorderFactory.createEmptyBorder(0, 9, 0, 9)));
            return this;
        }
    }

    private final class ThemedScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void paintTrack(java.awt.Graphics graphics, javax.swing.JComponent component,
                java.awt.Rectangle bounds) {
            graphics.setColor(BACKGROUND);
            graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }

        @Override
        protected void paintThumb(java.awt.Graphics graphics, javax.swing.JComponent component,
                java.awt.Rectangle bounds) {
            if (bounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(darkMode ? new Color(124, 101, 151) : new Color(198, 185, 211));
            int inset = 3;
            g.fillRoundRect(bounds.x + inset, bounds.y + inset,
                    Math.max(2, bounds.width - inset * 2), Math.max(2, bounds.height - inset * 2), 8, 8);
            g.dispose();
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return zeroSizeScrollButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return zeroSizeScrollButton();
        }

        private JButton zeroSizeScrollButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }
    }

    private final class GradientPanel extends JPanel {
        private GradientPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(java.awt.Graphics graphics) {
            java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
            Color start = darkMode ? new Color(51, 40, 61) : new Color(252, 239, 244);
            Color end = darkMode ? new Color(39, 43, 61) : new Color(240, 233, 249);
            g.setPaint(new java.awt.GradientPaint(0, 0, start, getWidth(), getHeight(), end));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class ColorSwatch implements javax.swing.Icon {
        private final Color color;

        private ColorSwatch(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(java.awt.Component component, java.awt.Graphics graphics, int x, int y) {
            java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color);
            g.fillRoundRect(x, y + 2, 12, 9, 5, 5);
            g.dispose();
        }

        @Override
        public int getIconWidth() {
            return 13;
        }

        @Override
        public int getIconHeight() {
            return 13;
        }
    }

    private static final class EmptyTimelineCanvas extends JPanel {
        private EmptyTimelineCanvas() {
            setOpaque(false);
            setPreferredSize(new Dimension(620, 150));
        }

        @Override
        protected void paintComponent(java.awt.Graphics graphics) {
            super.paintComponent(graphics);
            java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            int left = 54;
            int right = Math.max(left + 1, getWidth() - 24);
            int top = 14;
            String[] lanes = {"CPU", "TIER 1", "TIER 2"};
            for (int i = 0; i < lanes.length; i++) {
                int y = top + i * 36;
                g.setColor(MUTED);
                g.setFont(new Font("Segoe UI", Font.BOLD, 9));
                g.drawString(lanes[i], 0, y + 14);
                g.setColor(GRID);
                g.fillRoundRect(left, y, right - left, 18, 8, 8);
                g.setColor(BORDER);
                g.drawLine(left, y + 9, right, y + 9);
            }
            g.setColor(BACKGROUND);
            g.fillRoundRect(Math.max(left + 10, (getWidth() - 270) / 2), 42, 270, 42, 14, 14);
            g.setColor(PURPLE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            String message = "Run a simulation to reveal its trace";
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(message, (getWidth() - metrics.stringWidth(message)) / 2, 67);
            g.setColor(MUTED);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            String hint = "No schedule is drawn until the real scheduler returns its results.";
            metrics = g.getFontMetrics();
            g.drawString(hint, (getWidth() - metrics.stringWidth(hint)) / 2, 104);
            g.dispose();
        }
    }
}
