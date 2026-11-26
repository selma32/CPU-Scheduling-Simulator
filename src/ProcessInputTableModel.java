import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/** Editable table model for the process inputs accepted by the existing simulator. */
public final class ProcessInputTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {"Process", "Arrival time", "Burst time", "Priority"};
    private final List<String[]> rows = new ArrayList<>();

    public ProcessInputTableModel() {
        addProcess();
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return columnIndex == 0 ? "P" + (rowIndex + 1) : rows.get(rowIndex)[columnIndex - 1];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex > 0;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (columnIndex > 0) {
            rows.get(rowIndex)[columnIndex - 1] = value == null ? "" : value.toString().trim();
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    public String getInput(int rowIndex, int inputColumn) {
        return rows.get(rowIndex)[inputColumn];
    }

    public void addProcess() {
        rows.add(new String[]{"0", "1", "1"});
        fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
    }

    public void removeProcess(int rowIndex) {
        if (rows.size() > 1 && rowIndex >= 0 && rowIndex < rows.size()) {
            rows.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }
}
