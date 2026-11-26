import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;

/**
 * JavaFX front-end for the Cloud-Cluster CPU Scheduling Simulator.
 * Uses SJF, ACS, SimulationResult, Metrics and Process exactly as they are.
 * Has a Light / Dark theme toggle.
 *
 * Run:  java --module-path <javafx-lib> --add-modules javafx.controls SchedulerGUI
 */
public class SchedulerGUI extends Application {

    private static final int UNIT = 40;      // pixels per time unit in the Gantt chart
    private static final int BAR_H = 52;

    // ------------------------------------------------------------------ stylesheet

    private static final String CSS = """
            .root {
                -fx-font-family: "Segoe UI", "Helvetica Neue", Arial, sans-serif;
                -fx-font-size: 13px;
            }

            /* ---------- LIGHT THEME ---------- */
            .root.light {
                -c-bg: #f2f4f8;
                -c-panel: #ffffff;
                -c-text: #1f2937;
                -c-muted: #6b7280;
                -c-border: #d8dde6;
                -c-accent: #2f6fed;
                -c-accent-hover: #1f5bd1;
                -c-stripe: #f6f8fc;
                -c-header: #eaeef5;
                -c-card: #eaf1ff;
                -fx-base: #e8ecf3;
                -fx-background: -c-bg;
                -fx-control-inner-background: -c-panel;
                -fx-control-inner-background-alt: -c-stripe;
                -fx-accent: -c-accent;
                -fx-focus-color: -c-accent;
                -fx-faint-focus-color: transparent;
                -fx-text-base-color: -c-text;
            }

            /* ---------- DARK THEME ---------- */
            .root.dark {
                -c-bg: #1b1d23;
                -c-panel: #252830;
                -c-text: #e6e8ee;
                -c-muted: #9aa3b2;
                -c-border: #3a3f4b;
                -c-accent: #5b9bff;
                -c-accent-hover: #7ab0ff;
                -c-stripe: #2b2f38;
                -c-header: #30343f;
                -c-card: #2a3550;
                -fx-base: #2e323c;
                -fx-background: -c-bg;
                -fx-control-inner-background: -c-panel;
                -fx-control-inner-background-alt: -c-stripe;
                -fx-accent: -c-accent;
                -fx-focus-color: -c-accent;
                -fx-faint-focus-color: transparent;
                -fx-text-base-color: -c-text;
            }

            .root { -fx-background-color: -c-bg; }
            .label { -fx-text-fill: -c-text; }

            /* header */
            .header-bar {
                -fx-background-color: -c-panel;
                -fx-border-color: -c-border;
                -fx-border-width: 0 0 1 0;
                -fx-padding: 12 18 12 18;
            }
            .app-title { -fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: -c-text; }
            .app-subtitle { -fx-font-size: 12px; -fx-text-fill: -c-muted; }
            .theme-toggle {
                -fx-background-color: -c-header;
                -fx-text-fill: -c-text;
                -fx-background-radius: 18;
                -fx-padding: 7 16 7 16;
                -fx-cursor: hand;
                -fx-font-weight: bold;
            }
            .theme-toggle:hover { -fx-background-color: -c-border; }

            /* side panel */
            .side-panel {
                -fx-background-color: -c-panel;
                -fx-border-color: -c-border;
                -fx-border-width: 0 1 0 0;
                -fx-padding: 16;
            }
            .section-title { -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: -c-text; }
            .hint { -fx-font-size: 11px; -fx-text-fill: -c-muted; }

            /* buttons */
            .button {
                -fx-background-radius: 8;
                -fx-border-radius: 8;
                -fx-cursor: hand;
            }
            .primary-button {
                -fx-background-color: -c-accent;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-font-size: 14px;
                -fx-padding: 11 14 11 14;
                -fx-background-radius: 10;
            }
            .primary-button:hover { -fx-background-color: -c-accent-hover; }
            .primary-button:pressed { -fx-opacity: 0.85; }

            /* tables */
            .table-view {
                -fx-background-color: -c-panel;
                -fx-border-color: -c-border;
                -fx-border-radius: 8;
                -fx-background-radius: 8;
            }
            .table-view .column-header, .table-view .filler,
            .table-view .column-header-background {
                -fx-background-color: -c-header;
                -fx-border-color: transparent;
            }
            .table-view .column-header .label {
                -fx-text-fill: -c-text;
                -fx-font-weight: bold;
            }
            .table-row-cell { -fx-table-cell-border-color: transparent; }
            .table-row-cell:selected { -fx-background-color: -c-accent; }
            .table-row-cell:selected .table-cell { -fx-text-fill: white; }
            .table-cell { -fx-text-fill: -c-text; -fx-alignment: CENTER-LEFT; }

            /* tabs */
            .tab-pane > .tab-header-area > .tab-header-background { -fx-background-color: transparent; }
            .tab-pane > .tab-header-area > .headers-region > .tab {
                -fx-background-color: -c-header;
                -fx-background-radius: 8 8 0 0;
                -fx-padding: 6 18 6 18;
            }
            .tab-pane > .tab-header-area > .headers-region > .tab:selected {
                -fx-background-color: -c-accent;
            }
            .tab .tab-label { -fx-text-fill: -c-text; -fx-font-weight: bold; }
            .tab:selected .tab-label { -fx-text-fill: white; }
            .tab-pane > .tab-content-area { -fx-background-color: transparent; }

            /* scroll panes */
            .result-scroll, .result-scroll > .viewport { -fx-background-color: transparent; }
            .gantt-scroll {
                -fx-background-color: -c-panel;
                -fx-border-color: -c-border;
                -fx-border-radius: 8;
                -fx-background-radius: 8;
            }
            .gantt-scroll > .viewport { -fx-background-color: -c-panel; }
            .gantt-pane { -fx-background-color: -c-panel; }
            .gantt-rect { -fx-stroke: -c-border; -fx-stroke-width: 1.2; }
            .gantt-tick { -fx-fill: -c-muted; -fx-font-size: 11px; }
            .gantt-label { -fx-fill: #1b1f27; }

            /* stat cards */
            .stat-card {
                -fx-background-color: -c-card;
                -fx-background-radius: 10;
                -fx-padding: 10 18 10 18;
            }
            .stat-value { -fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: -c-accent; }
            .stat-caption { -fx-font-size: 11px; -fx-text-fill: -c-muted; }

            /* legend */
            .legend-text { -fx-font-size: 11px; -fx-text-fill: -c-muted; }

            /* status bar */
            .status-bar {
                -fx-background-color: -c-panel;
                -fx-border-color: -c-border;
                -fx-border-width: 1 0 0 0;
                -fx-padding: 7 16 7 16;
            }
            .status-text { -fx-text-fill: -c-muted; -fx-font-size: 12px; }

            .text-field, .spinner .text-field, .combo-box {
                -fx-background-radius: 6;
                -fx-border-radius: 6;
            }
            .separator .line { -fx-border-color: -c-border; }
            """;

    /** Editable row of the input table. */
    public static class Row {
        final SimpleIntegerProperty id = new SimpleIntegerProperty();
        final SimpleIntegerProperty arrival = new SimpleIntegerProperty();
        final SimpleIntegerProperty burst = new SimpleIntegerProperty();
        final SimpleIntegerProperty priority = new SimpleIntegerProperty();

        Row(int id, int a, int b, int p) {
            this.id.set(id);
            this.arrival.set(a);
            this.burst.set(b);
            this.priority.set(p);
        }
    }

    private final ObservableList<Row> rows = FXCollections.observableArrayList();
    private final Spinner<Integer> quantumSpinner = new Spinner<>(1, 100, 3);
    private final Spinner<Integer> csSpinner = new Spinner<>(0, 100, 1);
    private final ComboBox<String> schedulerBox = new ComboBox<>(
            FXCollections.observableArrayList("SJF", "ACS", "Both (compare)"));
    private final TabPane resultTabs = new TabPane();
    private final Label status = new Label("Add processes and press Run.");
    private BorderPane root;
    private ToggleButton themeToggle;

    @Override
    public void start(Stage stage) throws Exception {
        quantumSpinner.setEditable(true);
        csSpinner.setEditable(true);
        quantumSpinner.setPrefWidth(120);
        csSpinner.setPrefWidth(120);
        schedulerBox.getSelectionModel().select(2);
        schedulerBox.setPrefWidth(160);

        loadSample();

        root = new BorderPane();
        root.getStyleClass().add("light");
        root.setTop(buildHeader());
        root.setLeft(buildInputPanel());
        root.setCenter(buildResultPanel());
        root.setBottom(statusBar());

        Scene scene = new Scene(root, 1300, 800);

        // write the stylesheet to a temp file and load it
        File css = File.createTempFile("scheduler-gui", ".css");
        css.deleteOnExit();
        Files.writeString(css.toPath(), CSS);
        scene.getStylesheets().add(css.toURI().toString());

        stage.setTitle("Cloud-Cluster CPU Scheduling Simulator");
        stage.setScene(scene);
        stage.show();
    }

    // ------------------------------------------------------------------ header + theme

    private HBox buildHeader() {
        Label title = new Label("CPU Scheduling Simulator");
        title.getStyleClass().add("app-title");
        Label sub = new Label("Preemptive SJF  vs  Adaptive Container Scheduler (ACS)");
        sub.getStyleClass().add("app-subtitle");
        VBox titles = new VBox(2, title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        themeToggle = new ToggleButton("\uD83C\uDF19  Dark mode");
        themeToggle.getStyleClass().add("theme-toggle");
        themeToggle.setOnAction(e -> applyTheme(themeToggle.isSelected()));

        HBox bar = new HBox(10, titles, spacer, themeToggle);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("header-bar");
        return bar;
    }

    private void applyTheme(boolean dark) {
        root.getStyleClass().removeAll("light", "dark");
        root.getStyleClass().add(dark ? "dark" : "light");
        themeToggle.setText(dark ? "\u2600  Light mode" : "\uD83C\uDF19  Dark mode");
    }

    // ------------------------------------------------------------------ input side

    private VBox buildInputPanel() {
        Label title = new Label("Processes");
        title.getStyleClass().add("section-title");

        TableView<Row> table = new TableView<>(rows);
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(330);

        TableColumn<Row, String> nameCol = new TableColumn<>("Process");
        nameCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>("P" + c.getValue().id.get()));
        nameCol.setEditable(false);

        table.getColumns().add(nameCol);
        table.getColumns().add(editableCol("Arrival", r -> r.arrival));
        table.getColumns().add(editableCol("Burst", r -> r.burst));
        table.getColumns().add(editableCol("Priority", r -> r.priority));

        Button add = new Button("+ Add");
        add.setOnAction(e -> rows.add(new Row(rows.size() + 1, 0, 1, 1)));

        Button remove = new Button("Remove");
        remove.setOnAction(e -> {
            Row sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                rows.remove(sel);
                renumber();
            }
        });

        Button clear = new Button("Clear");
        clear.setOnAction(e -> rows.clear());

        Button sample = new Button("Sample");
        sample.setOnAction(e -> loadSample());

        HBox rowButtons = new HBox(6, add, remove, clear, sample);

        Label pTitle = new Label("Parameters");
        pTitle.getStyleClass().add("section-title");

        GridPane params = new GridPane();
        params.setHgap(12);
        params.setVgap(10);
        params.addRow(0, new Label("Time quantum (Q)"), quantumSpinner);
        params.addRow(1, new Label("Context switch (CS)"), csSpinner);
        params.addRow(2, new Label("Scheduler"), schedulerBox);

        Button run = new Button("\u25B6  Run simulation");
        run.setMaxWidth(Double.MAX_VALUE);
        run.setDefaultButton(true);
        run.getStyleClass().add("primary-button");
        run.setOnAction(e -> runSimulation());

        Label hint = new Label("Smaller priority number = higher priority.\nDouble-click a cell to edit it.");
        hint.getStyleClass().add("hint");

        VBox box = new VBox(12, title, table, rowButtons, new Separator(), pTitle, params, run, hint);
        box.getStyleClass().add("side-panel");
        box.setPrefWidth(390);
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private TableColumn<Row, Integer> editableCol(String title, Function<Row, SimpleIntegerProperty> prop) {
        TableColumn<Row, Integer> col = new TableColumn<>(title);
        col.setCellValueFactory(c -> prop.apply(c.getValue()).asObject());
        col.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter() {
            @Override
            public Integer fromString(String s) {
                try {
                    return super.fromString(s.trim());
                } catch (Exception ex) {
                    return null;     // ignore invalid text
                }
            }
        }));
        col.setOnEditCommit(ev -> {
            Integer v = ev.getNewValue();
            if (v != null) {
                prop.apply(ev.getRowValue()).set(v);
            }
            ev.getTableView().refresh();
        });
        return col;
    }

    private void renumber() {
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).id.set(i + 1);
        }
    }

    private void loadSample() {
        rows.setAll(
                new Row(1, 0, 8, 3),
                new Row(2, 1, 4, 1),
                new Row(3, 2, 9, 4),
                new Row(4, 3, 5, 2),
                new Row(5, 6, 2, 5));
        quantumSpinner.getValueFactory().setValue(3);
        csSpinner.getValueFactory().setValue(1);
    }

    // ------------------------------------------------------------------ result side

    private VBox buildResultPanel() {
        resultTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        resultTabs.getTabs().clear();

        VBox box = new VBox(resultTabs);
        VBox.setVgrow(resultTabs, Priority.ALWAYS);
        box.setPadding(new Insets(14));
        return box;
    }

    private HBox statusBar() {
        status.getStyleClass().add("status-text");
        HBox bar = new HBox(status);
        bar.getStyleClass().add("status-bar");
        return bar;
    }

    private void runSimulation() {
        if (rows.isEmpty()) {
            error("There must be at least one process.");
            return;
        }
        List<Process> processes = new ArrayList<>();
        for (Row r : rows) {
            if (r.arrival.get() < 0 || r.burst.get() <= 0) {
                error("P" + r.id.get() + ": arrival must be >= 0 and burst must be > 0.");
                return;
            }
            processes.add(new Process(r.id.get(), r.arrival.get(), r.burst.get(), r.priority.get()));
        }

        int q, cs;
        try {
            q = readSpinner(quantumSpinner);
            cs = readSpinner(csSpinner);
        } catch (NumberFormatException ex) {
            error("Q and CS must be whole numbers.");
            return;
        }
        if (q <= 0 || cs < 0) {
            error("Q must be positive and CS cannot be negative.");
            return;
        }

        int choice = schedulerBox.getSelectionModel().getSelectedIndex();
        List<Scheduler> schedulers = new ArrayList<>();
        if (choice == 0 || choice == 2) schedulers.add(new SJF());
        if (choice == 1 || choice == 2) schedulers.add(new ACS());

        resultTabs.getTabs().clear();
        List<String> summary = new ArrayList<>();
        for (Scheduler s : schedulers) {
            try {
                SimulationResult res = s.run(processes, q, cs);
                resultTabs.getTabs().add(new Tab(res.schedulerName, buildResultView(res)));
                summary.add(String.format("%s: avg TAT %.2f, avg WT %.2f",
                        res.schedulerName,
                        Metrics.avgTurnaround(res.processes),
                        Metrics.avgWaiting(res.processes)));
            } catch (UnsupportedOperationException ex) {
                resultTabs.getTabs().add(new Tab(s.getClass().getSimpleName(),
                        new Label("  " + s.getClass().getSimpleName() + " is not implemented yet.")));
            } catch (Exception ex) {
                error(s.getClass().getSimpleName() + " failed: " + ex);
            }
        }
        status.setText(summary.isEmpty() ? "No results." : String.join("   |   ", summary));
    }

    private int readSpinner(Spinner<Integer> sp) {
        int v = Integer.parseInt(sp.getEditor().getText().trim());
        sp.getValueFactory().setValue(v);
        return v;
    }

    private ScrollPane buildResultView(SimulationResult r) {
        // --- Gantt chart (fixed height so it never gets squeezed)
        Label gTitle = sectionTitle("Gantt chart");
        ScrollPane gantt = new ScrollPane(buildGantt(r));
        gantt.getStyleClass().add("gantt-scroll");
        gantt.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        gantt.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        gantt.setMinHeight(BAR_H + 85);
        gantt.setPrefHeight(BAR_H + 85);
        gantt.setMaxHeight(BAR_H + 85);

        HBox legend = buildLegend(r);

        // --- metrics
        Label mTitle = sectionTitle("Final metrics");
        TableView<Process> metrics = new TableView<>();
        metrics.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        List<Process> sorted = new ArrayList<>(r.processes);
        sorted.sort(Comparator.comparingInt(p -> p.id));
        metrics.setItems(FXCollections.observableArrayList(sorted));
        metrics.getColumns().add(col("Process", (Process p) -> p.name()));
        metrics.getColumns().add(col("Arrival", (Process p) -> p.arrival));
        metrics.getColumns().add(col("Burst", (Process p) -> p.burst));
        metrics.getColumns().add(col("Finish", (Process p) -> p.finish));
        metrics.getColumns().add(col("Turnaround", (Process p) -> p.turnaround()));
        metrics.getColumns().add(col("Waiting", (Process p) -> p.waiting()));
        metrics.getColumns().add(col("Response", (Process p) -> p.response()));
        double mh = 62 + 28 * Math.min(sorted.size(), 8);
        metrics.setMinHeight(mh);
        metrics.setPrefHeight(mh);

        HBox cards = new HBox(12,
                statCard("Avg Turnaround", String.format("%.2f", Metrics.avgTurnaround(sorted))),
                statCard("Avg Waiting", String.format("%.2f", Metrics.avgWaiting(sorted))),
                statCard("Avg Response", String.format("%.2f", Metrics.avgResponse(sorted))),
                statCard("CPU Utilization", String.format("%.1f%%", r.cpuUtilization())));

        // --- per-step table
        Label sTitle = sectionTitle("State at every time step");
        TableView<SimulationResult.Step> steps = new TableView<>();
        steps.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        steps.setItems(FXCollections.observableArrayList(r.steps));
        TableColumn<SimulationResult.Step, String> t = col("t", (SimulationResult.Step s) -> s.time);
        t.setMaxWidth(60);
        TableColumn<SimulationResult.Step, String> cpu = col("CPU", (SimulationResult.Step s) -> s.running);
        cpu.setMaxWidth(80);
        steps.getColumns().add(t);
        steps.getColumns().add(cpu);
        steps.getColumns().add(col("Tier 1 queue", (SimulationResult.Step s) -> s.tier1));
        steps.getColumns().add(col("Tier 2 queue", (SimulationResult.Step s) -> s.tier2));
        steps.setPrefHeight(280);
        steps.setMinHeight(180);

        VBox box = new VBox(10, gTitle, gantt, legend, mTitle, metrics, cards, sTitle, steps);
        box.setPadding(new Insets(12, 4, 12, 4));

        ScrollPane sp = new ScrollPane(box);
        sp.getStyleClass().add("result-scroll");
        sp.setFitToWidth(true);
        return sp;
    }

    private Label sectionTitle(String s) {
        Label l = new Label(s);
        l.getStyleClass().add("section-title");
        return l;
    }

    private VBox statCard(String caption, String value) {
        Label v = new Label(value);
        v.getStyleClass().add("stat-value");
        Label c = new Label(caption);
        c.getStyleClass().add("stat-caption");
        VBox card = new VBox(2, v, c);
        card.getStyleClass().add("stat-card");
        return card;
    }

    private <T> TableColumn<T, String> col(String title, Function<T, Object> f) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(String.valueOf(f.apply(cd.getValue()))));
        return c;
    }

    // ------------------------------------------------------------------ Gantt chart

    private Pane buildGantt(SimulationResult r) {
        Pane pane = new Pane();
        pane.getStyleClass().add("gantt-pane");
        int y = 12;

        for (SimulationResult.Segment s : r.timeline) {
            double x = 14 + s.start * UNIT;
            double w = (s.end - s.start) * UNIT;

            Rectangle rect = new Rectangle(x, y, w, BAR_H);
            rect.getStyleClass().add("gantt-rect");
            rect.setArcWidth(10);
            rect.setArcHeight(10);
            rect.setFill(colorFor(s.label));
            Tooltip.install(rect, new Tooltip(s.label + "  [" + s.start + " - " + s.end + "]"));
            pane.getChildren().add(rect);

            Text label = new Text(s.label);
            label.getStyleClass().add("gantt-label");
            label.setFont(Font.font(null, FontWeight.BOLD, 13));
            double tw = label.getLayoutBounds().getWidth();
            if (tw <= w - 4) {
                label.setX(x + (w - tw) / 2);
                label.setY(y + BAR_H / 2.0 + 5);
                pane.getChildren().add(label);
            }

            Text tick = new Text(String.valueOf(s.start));
            tick.getStyleClass().add("gantt-tick");
            tick.setX(x - tick.getLayoutBounds().getWidth() / 2);
            tick.setY(y + BAR_H + 20);
            pane.getChildren().add(tick);
        }

        if (!r.timeline.isEmpty()) {
            SimulationResult.Segment last = r.timeline.get(r.timeline.size() - 1);
            Text end = new Text(String.valueOf(last.end));
            end.getStyleClass().add("gantt-tick");
            end.setX(14 + last.end * UNIT - end.getLayoutBounds().getWidth() / 2);
            end.setY(y + BAR_H + 20);
            pane.getChildren().add(end);
            pane.setPrefWidth(28 + last.end * UNIT + 20);
        }
        pane.setMinHeight(y + BAR_H + 32);
        pane.setPrefHeight(y + BAR_H + 32);
        return pane;
    }

    private HBox buildLegend(SimulationResult r) {
        TreeSet<Integer> ids = new TreeSet<>();
        for (SimulationResult.Segment s : r.timeline) {
            if (!s.label.equals("CS") && !s.label.equals("IDLE")) {
                try {
                    ids.add(Integer.parseInt(s.label.substring(1)));
                } catch (Exception ignored) { }
            }
        }
        HBox legend = new HBox(14);
        legend.setAlignment(Pos.CENTER_LEFT);
        for (int id : ids) {
            legend.getChildren().add(legendItem("P" + id, colorFor("P" + id)));
        }
        legend.getChildren().add(legendItem("Context switch", colorFor("CS")));
        legend.getChildren().add(legendItem("Idle", colorFor("IDLE")));
        return legend;
    }

    private HBox legendItem(String text, Color c) {
        Rectangle sw = new Rectangle(14, 14);
        sw.setArcWidth(4);
        sw.setArcHeight(4);
        sw.setFill(c);
        sw.getStyleClass().add("gantt-rect");
        Label l = new Label(text);
        l.getStyleClass().add("legend-text");
        HBox item = new HBox(5, sw, l);
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
    }

    private Color colorFor(String label) {
        if (label.equals("CS")) return Color.web("#c9ced8");
        if (label.equals("IDLE")) return Color.web("#eceff4");
        int id;
        try {
            id = Integer.parseInt(label.substring(1));
        } catch (Exception e) {
            return Color.LIGHTBLUE;
        }
        return Color.hsb((id * 57) % 360, 0.38, 0.98);
    }

    // ------------------------------------------------------------------ helpers

    private void error(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText("Invalid input");
        a.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}