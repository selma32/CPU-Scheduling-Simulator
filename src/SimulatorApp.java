import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Desktop entry point for the CPU Scheduling Simulator. */
public final class SimulatorApp {
    private SimulatorApp() {
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingUtilities.invokeLater(() -> new SimulatorWindow().setVisible(true));
    }
}
