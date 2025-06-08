import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class ExpenseTracker extends JFrame {

    // DB Config
    private final String DB_URL = "jdbc:mysql://localhost:3306/expense_db";
    private final String DB_USER = "root"; // change as needed
    private final String DB_PASS = "1234567890";     // change as needed

    // GUI components
    private JTextField txtCategory, txtAmount, txtDate;
    private JTable table;
    private DefaultTableModel model;

    // Constructor
    public ExpenseTracker() {
        setTitle("Expense Tracker");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        fetchExpenses(); // Load table on startup
    }

    private void initComponents() {
        JLabel lblCategory = new JLabel("Category:");
        JLabel lblAmount = new JLabel("Amount:");
        JLabel lblDate = new JLabel("Date (YYYY-MM-DD):");

        txtCategory = new JTextField(10);
        txtAmount = new JTextField(10);
        txtDate = new JTextField(10);

        JButton btnAdd = new JButton("Add Expense");
        JButton btnRefresh = new JButton("Refresh Table");

        // Table Setup
        model = new DefaultTableModel(new String[]{"Category", "Amount", "Date"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        // Layout
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 4, 10, 10));
        panel.add(lblCategory); panel.add(txtCategory);
        panel.add(lblAmount); panel.add(txtAmount);
        panel.add(lblDate); panel.add(txtDate);
        panel.add(btnAdd); panel.add(btnRefresh);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Button Events
        btnAdd.addActionListener(e -> addExpense());
        btnRefresh.addActionListener(e -> fetchExpenses());
    }

    private void addExpense() {
        String category = txtCategory.getText().trim();
        String amountStr = txtAmount.getText().trim();
        String date = txtDate.getText().trim();

        try {
            double amount = Double.parseDouble(amountStr);

            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            String sql = "INSERT INTO expenses (category, amount, date) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, category);
            stmt.setDouble(2, amount);
            stmt.setString(3, date);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Expense added.");
                fetchExpenses(); // refresh
                clearInputs();
            }

            stmt.close();
            conn.close();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for amount.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }

    private void fetchExpenses() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT category, amount, date FROM expenses");

            model.setRowCount(0); // Clear existing
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("category"));
                row.add(rs.getDouble("amount"));
                row.add(rs.getDate("date"));
                model.addRow(row);
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error fetching data: " + ex.getMessage());
        }
    }

    private void clearInputs() {
        txtCategory.setText("");
        txtAmount.setText("");
        txtDate.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ExpenseTracker().setVisible(true));
    }
}
