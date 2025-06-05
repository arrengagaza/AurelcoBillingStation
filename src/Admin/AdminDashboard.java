package Admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Calendar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.mindrot.jbcrypt.BCrypt;

public class AdminDashboard extends JFrame {
    private JTabbedPane tabbedPane;
    private JTable consumerTable, billingTable, disconnectionTable, reportTable, paymentTable;
    private DefaultTableModel consumerTableModel, billingTableModel, disconnectionTableModel, reportTableModel, paymentTableModel;
    private Connection connection;
    private JComboBox<String> monthComboBox, yearComboBox;
    private int currentAdminId; // Track the currently logged in admin

    public AdminDashboard(int adminId) {
        this.currentAdminId = adminId;
        initializeDatabase();
        createUI();
        loadConsumerData();
        loadBillingData();
        loadDisconnectionData();
        loadReportData();
        loadPaymentData();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // fallback (not recommended for production)
        }
    }

    private void initializeDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/aurelco?useSSL=false&serverTimezone=UTC";
            connection = DriverManager.getConnection(url, "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void createUI() {
        setTitle("Admin Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Admin Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Confirm Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                new login_admin().setVisible(true);
                dispose();
            }
        });
        headerPanel.add(logoutButton, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Consumer Management", createConsumerPanel());
        tabbedPane.addTab("Billing & Reading", createBillingPanel());
        tabbedPane.addTab("Disconnection", createDisconnectionPanel());
        tabbedPane.addTab("Payment Process", createPaymentPanel());
        tabbedPane.addTab("Reports", createReportPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createConsumerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] consumerColumns = {"Account #", "Meter #", "Fullname", "Address", "Contact #", "Email Address", "Status"};
        consumerTableModel = new DefaultTableModel(consumerColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        consumerTable = new JTable(consumerTableModel);
        customizeTable(consumerTable);

        JScrollPane scrollPane = new JScrollPane(consumerTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton editButton = new JButton("Edit Info");
        JButton addButton = new JButton("Add Consumer");
        JButton deleteButton = new JButton("Delete");
        JButton refreshButton = new JButton("Refresh");

        editButton.addActionListener(e -> editConsumer());
        addButton.addActionListener(e -> addConsumer());
        deleteButton.addActionListener(e -> deleteConsumer());
        refreshButton.addActionListener(e -> loadConsumerData());

        buttonPanel.add(editButton);
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createBillingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] billingColumns = {"Account #", "Meter #", "Fullname", "Previous Reading", "Consumption", "Amount", "Period of Billing", "Status"};
        billingTableModel = new DefaultTableModel(billingColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        billingTable = new JTable(billingTableModel);
        customizeTable(billingTable);

        JScrollPane scrollPane = new JScrollPane(billingTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton generateBillButton = new JButton("Generate Bill & Enter Reading");
        JButton refreshButton = new JButton("Refresh");

        generateBillButton.addActionListener(e -> generateBill());
        refreshButton.addActionListener(e -> loadBillingData());

        buttonPanel.add(generateBillButton);
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createDisconnectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] disconnectionColumns = {"Account #", "Meter #", "Fullname", "Address", "Bill Amount", "Due Date", "Status"};
        disconnectionTableModel = new DefaultTableModel(disconnectionColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        disconnectionTable = new JTable(disconnectionTableModel);
        customizeTable(disconnectionTable);

        JScrollPane scrollPane = new JScrollPane(disconnectionTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton issueNoticeButton = new JButton("Issue Notice");
        JButton disconnectButton = new JButton("Disconnect");
        JButton reconnectButton = new JButton("Reconnect");
        JButton refreshButton = new JButton("Refresh");

        issueNoticeButton.addActionListener(e -> issueNotice());
        disconnectButton.addActionListener(e -> disconnectConsumer());
        reconnectButton.addActionListener(e -> reconnectConsumer());
        refreshButton.addActionListener(e -> loadDisconnectionData());

        buttonPanel.add(issueNoticeButton);
        buttonPanel.add(disconnectButton);
        buttonPanel.add(reconnectButton);
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createPaymentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] paymentColumns = {"Account #", "Meter #", "Fullname", "Bill Amount", "Due Date", "Status"};
        paymentTableModel = new DefaultTableModel(paymentColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        paymentTable = new JTable(paymentTableModel);
        customizeTable(paymentTable);

        JScrollPane scrollPane = new JScrollPane(paymentTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton processPaymentButton = new JButton("Process Payment");
        JButton refreshButton = new JButton("Refresh");

        processPaymentButton.addActionListener(e -> processPayment());
        refreshButton.addActionListener(e -> loadPaymentData());

        buttonPanel.add(processPaymentButton);
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        monthComboBox = new JComboBox<>(months);
        monthComboBox.setSelectedIndex(new Date().getMonth());

        yearComboBox = new JComboBox<>();
        int currentYear = new Date().getYear() + 1900;
        for (int year = currentYear - 5; year <= currentYear; year++) {
            yearComboBox.addItem(String.valueOf(year));
        }
        yearComboBox.setSelectedItem(String.valueOf(currentYear));

        JButton generateReportButton = new JButton("Generate Report");
        JButton printButton = new JButton("Print Report");

        generateReportButton.addActionListener(e -> loadReportData());
        printButton.addActionListener(e -> printReport());

        controlPanel.add(new JLabel("Month:"));
        controlPanel.add(monthComboBox);
        controlPanel.add(new JLabel("Year:"));
        controlPanel.add(yearComboBox);
        controlPanel.add(generateReportButton);
        controlPanel.add(printButton);

        panel.add(controlPanel, BorderLayout.NORTH);

        String[] reportColumns = {"Account #", "Name", "Consumption", "Amount", "Status", "Payment Date"};
        reportTableModel = new DefaultTableModel(reportColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reportTable = new JTable(reportTableModel);
        customizeTable(reportTable);

        JScrollPane scrollPane = new JScrollPane(reportTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void customizeTable(JTable table) {
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("Arial", Font.PLAIN, 14));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);

        TableColumnModel columnModel = table.getColumnModel();
        if (table == consumerTable) {
            columnModel.getColumn(0).setPreferredWidth(80);
            columnModel.getColumn(1).setPreferredWidth(80);
            columnModel.getColumn(2).setPreferredWidth(150);
            columnModel.getColumn(3).setPreferredWidth(200);
            columnModel.getColumn(4).setPreferredWidth(100);
            columnModel.getColumn(5).setPreferredWidth(150);
            columnModel.getColumn(6).setPreferredWidth(80);
        } else if (table == billingTable) {
            columnModel.getColumn(0).setPreferredWidth(80);
            columnModel.getColumn(1).setPreferredWidth(80);
            columnModel.getColumn(2).setPreferredWidth(150);
            columnModel.getColumn(3).setPreferredWidth(100);
            columnModel.getColumn(4).setPreferredWidth(100);
            columnModel.getColumn(5).setPreferredWidth(100);
            columnModel.getColumn(6).setPreferredWidth(100);
            columnModel.getColumn(7).setPreferredWidth(80);
        } else if (table == disconnectionTable) {
            columnModel.getColumn(0).setPreferredWidth(80);
            columnModel.getColumn(1).setPreferredWidth(80);
            columnModel.getColumn(2).setPreferredWidth(150);
            columnModel.getColumn(3).setPreferredWidth(200);
            columnModel.getColumn(4).setPreferredWidth(100);
            columnModel.getColumn(5).setPreferredWidth(100);
            columnModel.getColumn(6).setPreferredWidth(80);
        } else if (table == reportTable) {
            columnModel.getColumn(0).setPreferredWidth(80);
            columnModel.getColumn(1).setPreferredWidth(150);
            columnModel.getColumn(2).setPreferredWidth(100);
            columnModel.getColumn(3).setPreferredWidth(100);
            columnModel.getColumn(4).setPreferredWidth(80);
            columnModel.getColumn(5).setPreferredWidth(100);
        } else if (table == paymentTable) {
            columnModel.getColumn(0).setPreferredWidth(80);
            columnModel.getColumn(1).setPreferredWidth(80);
            columnModel.getColumn(2).setPreferredWidth(150);
            columnModel.getColumn(3).setPreferredWidth(100);
            columnModel.getColumn(4).setPreferredWidth(100);
            columnModel.getColumn(5).setPreferredWidth(80);
        }
    }

    private void loadConsumerData() {
        try {
            consumerTableModel.setRowCount(0);
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT account_no, meter_number, CONCAT(last_name, ', ', given_name, ' ', middle_name) as fullname, " +
                    "address, mobile, email, status FROM consumers");

            while (rs.next()) {
                Vector<String> row = new Vector<>();
                row.add(rs.getString("account_no"));
                row.add(rs.getString("meter_number"));
                row.add(rs.getString("fullname"));
                row.add(rs.getString("address"));
                row.add(rs.getString("mobile"));
                row.add(rs.getString("email"));
                row.add(rs.getString("status"));
                consumerTableModel.addRow(row);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading consumer data", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadBillingData() {
        try {
            billingTableModel.setRowCount(0);
            String query = "SELECT c.account_no, c.meter_number, " +
                          "CONCAT(c.last_name, ', ', c.given_name) as fullname, " +
                          "COALESCE(b.previous_reading, 0) as previous_reading, " +
                          "COALESCE((b.current_reading - b.previous_reading), 0) as consumption, " +
                          "COALESCE(b.amount, 0) as amount, " +
                          "COALESCE(DATE_FORMAT(b.billing_date, '%M %Y'), 'N/A') as billing_period, " +
                          "COALESCE(b.status, 'No Bill') as status " +
                          "FROM consumers c " +
                          "LEFT JOIN bills b ON c.id = b.consumer_id " +
                          "AND b.id = (SELECT MAX(id) FROM bills WHERE consumer_id = c.id) " +
                          "WHERE c.status = 'active' " +
                          "ORDER BY c.account_no";

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                Vector<String> row = new Vector<>();
                row.add(rs.getString("account_no"));
                row.add(rs.getString("meter_number"));
                row.add(rs.getString("fullname"));
                row.add(String.valueOf(rs.getDouble("previous_reading")));
                row.add(String.valueOf(rs.getDouble("consumption")));
                row.add(String.format("₱%.2f", rs.getDouble("amount")));
                row.add(rs.getString("billing_period"));
                row.add(rs.getString("status"));
                billingTableModel.addRow(row);
            }

            rs.close();
            stmt.close();

            if (!hasData) {
                JOptionPane.showMessageDialog(this, "No active consumers found", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading billing data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDisconnectionData() {
        try {
            disconnectionTableModel.setRowCount(0);
            String query = "SELECT c.account_no, c.meter_number, " +
                          "CONCAT(c.last_name, ', ', c.given_name) as fullname, " +
                          "c.address, COALESCE(b.amount, 0) as amount, " +
                          "b.due_date, COALESCE(d.status, 'No Notice') as status " +
                          "FROM consumers c " +
                          "LEFT JOIN bills b ON c.id = b.consumer_id AND b.status IN ('pending', 'overdue') " +
                          "LEFT JOIN disconnections d ON b.id = d.bill_id AND d.consumer_id = c.id " +
                          "WHERE c.status IN ('active', 'pending disconnection') " +
                          "AND (b.amount IS NOT NULL OR d.status IS NOT NULL) " +
                          "ORDER BY COALESCE(d.notice_date, b.due_date) DESC";

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                Vector<String> row = new Vector<>();
                row.add(rs.getString("account_no"));
                row.add(rs.getString("meter_number"));
                row.add(rs.getString("fullname"));
                row.add(rs.getString("address"));
                row.add(String.format("₱%.2f", rs.getDouble("amount")));
                row.add(rs.getDate("due_date") != null ? new SimpleDateFormat("MM/dd/yyyy").format(rs.getDate("due_date")) : "N/A");
                row.add(rs.getString("status"));
                disconnectionTableModel.addRow(row);
            }

            rs.close();
            stmt.close();

            if (!hasData) {
                JOptionPane.showMessageDialog(this, "No consumers with unpaid bills or disconnection notices", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading disconnection data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPaymentData() {
        try {
            paymentTableModel.setRowCount(0);
            String query = "SELECT c.account_no, c.meter_number, " +
                          "CONCAT(c.last_name, ', ', c.given_name) as fullname, " +
                          "b.amount, b.due_date, b.status " +
                          "FROM bills b " +
                          "JOIN consumers c ON b.consumer_id = c.id " +
                          "WHERE b.status IN ('pending', 'overdue') " +
                          "ORDER BY b.due_date";

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                Vector<String> row = new Vector<>();
                row.add(rs.getString("account_no"));
                row.add(rs.getString("meter_number"));
                row.add(rs.getString("fullname"));
                row.add(String.format("₱%.2f", rs.getDouble("amount")));
                row.add(rs.getDate("due_date") != null ? new SimpleDateFormat("MM/dd/yyyy").format(rs.getDate("due_date")) : "N/A");
                row.add(rs.getString("status"));
                paymentTableModel.addRow(row);
            }

            rs.close();
            stmt.close();

            if (!hasData) {
                JOptionPane.showMessageDialog(this, "No unpaid bills found", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading payment data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadReportData() {
        try {
            reportTableModel.setRowCount(0);
            int month = monthComboBox.getSelectedIndex() + 1;
            int year = Integer.parseInt((String) yearComboBox.getSelectedItem());

            String query = "SELECT c.account_no, CONCAT(c.last_name, ', ', c.given_name) as name, " +
                          "b.consumption, b.amount, b.status, p.payment_date " +
                          "FROM bills b " +
                          "JOIN consumers c ON b.consumer_id = c.id " +
                          "LEFT JOIN payments p ON b.id = p.bill_id " +
                          "WHERE MONTH(b.billing_date) = ? AND YEAR(b.billing_date) = ?";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, month);
            stmt.setInt(2, year);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Vector<String> row = new Vector<>();
                row.add(rs.getString("account_no"));
                row.add(rs.getString("name"));
                row.add(String.valueOf(rs.getDouble("consumption")));
                row.add(String.format("₱%.2f", rs.getDouble("amount")));
                row.add(rs.getString("status"));
                row.add(rs.getDate("payment_date") != null ? new SimpleDateFormat("MM/dd/yyyy").format(rs.getDate("payment_date")) : "Unpaid");
                reportTableModel.addRow(row);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading report data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid year selected", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editConsumer() {
        int selectedRow = consumerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a consumer to edit", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String accountNo = (String) consumerTableModel.getValueAt(selectedRow, 0);

        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM consumers WHERE account_no = ?");
            stmt.setString(1, accountNo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JDialog editDialog = new JDialog(this, "Edit Consumer", true);
                editDialog.setSize(500, 400);
                editDialog.setLayout(new BorderLayout());

                JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
                formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

                JTextField lastNameField = new JTextField(rs.getString("last_name"));
                JTextField firstNameField = new JTextField(rs.getString("given_name"));
                JTextField middleNameField = new JTextField(rs.getString("middle_name"));
                JTextField emailField = new JTextField(rs.getString("email"));
                JTextField mobileField = new JTextField(rs.getString("mobile"));
                JPasswordField passwordField = new JPasswordField();
                JComboBox<String> statusCombo = new JComboBox<>(new String[]{"active", "inactive", "disconnected"});
                statusCombo.setSelectedItem(rs.getString("status"));

                formPanel.add(new JLabel("Last Name:"));
                formPanel.add(lastNameField);
                formPanel.add(new JLabel("First Name:"));
                formPanel.add(firstNameField);
                formPanel.add(new JLabel("Middle Name:"));
                formPanel.add(middleNameField);
                formPanel.add(new JLabel("Email:"));
                formPanel.add(emailField);
                formPanel.add(new JLabel("Mobile:"));
                formPanel.add(mobileField);
                formPanel.add(new JLabel("Password (leave blank to keep current):"));
                formPanel.add(passwordField);
                formPanel.add(new JLabel("Status:"));
                formPanel.add(statusCombo);

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
                JButton saveButton = new JButton("Save");
                JButton cancelButton = new JButton("Cancel");

                saveButton.addActionListener(e -> {
                    try {
                        String passwordUpdate = "";
                        if (passwordField.getPassword().length > 0) {
                            passwordUpdate = ", password = ?";
                        }

                        PreparedStatement updateStmt = connection.prepareStatement(
                            "UPDATE consumers SET last_name = ?, given_name = ?, middle_name = ?, email = ?, mobile = ?" + 
                            passwordUpdate + ", status = ? WHERE account_no = ?");
                        
                        int paramIndex = 1;
                        updateStmt.setString(paramIndex++, lastNameField.getText());
                        updateStmt.setString(paramIndex++, firstNameField.getText());
                        updateStmt.setString(paramIndex++, middleNameField.getText());
                        updateStmt.setString(paramIndex++, emailField.getText());
                        updateStmt.setString(paramIndex++, mobileField.getText());
                        
                        if (passwordField.getPassword().length > 0) {
                            updateStmt.setString(paramIndex++, hashPassword(new String(passwordField.getPassword())));
                        }
                        
                        updateStmt.setString(paramIndex++, (String) statusCombo.getSelectedItem());
                        updateStmt.setString(paramIndex++, accountNo);

                        updateStmt.executeUpdate();
                        JOptionPane.showMessageDialog(editDialog, "Consumer updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadConsumerData();
                        editDialog.dispose();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(editDialog, "Error updating consumer: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                });

                cancelButton.addActionListener(e -> editDialog.dispose());

                buttonPanel.add(saveButton);
                buttonPanel.add(cancelButton);

                editDialog.add(formPanel, BorderLayout.CENTER);
                editDialog.add(buttonPanel, BorderLayout.SOUTH);
                editDialog.setLocationRelativeTo(this);
                editDialog.setVisible(true);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error retrieving consumer data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void addConsumer() {
        JDialog addDialog = new JDialog(this, "Add Consumer", true);
        addDialog.setSize(600, 500);
        addDialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(11, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField accountNoField = new JTextField();
        JTextField meterNoField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField firstNameField = new JTextField();
        JTextField middleNameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField mobileField = new JTextField();
        JTextField addressField = new JTextField();
        JTextField birthdayField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"active", "inactive", "disconnected"});

        formPanel.add(new JLabel("Account Number:"));
        formPanel.add(accountNoField);
        formPanel.add(new JLabel("Meter Number:"));
        formPanel.add(meterNoField);
        formPanel.add(new JLabel("Last Name:"));
        formPanel.add(lastNameField);
        formPanel.add(new JLabel("First Name:"));
        formPanel.add(firstNameField);
        formPanel.add(new JLabel("Middle Name:"));
        formPanel.add(middleNameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Mobile:"));
        formPanel.add(mobileField);
        formPanel.add(new JLabel("Address:"));
        formPanel.add(addressField);
        formPanel.add(new JLabel("Birthday (YYYY-MM-DD):"));
        formPanel.add(birthdayField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("Status:"));
        formPanel.add(statusCombo);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
        try {
            // Validate required fields
            if (accountNoField.getText().isEmpty() || meterNoField.getText().isEmpty() || 
                lastNameField.getText().isEmpty() || firstNameField.getText().isEmpty() ||
                passwordField.getPassword().length == 0) {
                JOptionPane.showMessageDialog(addDialog, 
                    "Please fill all required fields", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Hash the password properly
            String plainPassword = new String(passwordField.getPassword());
            String hashedPassword;
            try {
                hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(addDialog, 
                    "Error hashing password: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Insert the new consumer
            try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO consumers (account_no, meter_number, last_name, given_name, " +
                "middle_name, email, mobile, address, birthday, password, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())")) {
                
                stmt.setString(1, accountNoField.getText());
                stmt.setString(2, meterNoField.getText());
                stmt.setString(3, lastNameField.getText());
                stmt.setString(4, firstNameField.getText());
                stmt.setString(5, middleNameField.getText());
                stmt.setString(6, emailField.getText());
                stmt.setString(7, mobileField.getText());
                stmt.setString(8, addressField.getText());
                stmt.setString(9, birthdayField.getText());
                stmt.setString(10, hashedPassword); // Store the properly hashed password
                stmt.setString(11, (String) statusCombo.getSelectedItem());

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(addDialog, 
                        "Consumer added successfully", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadConsumerData();
                    addDialog.dispose();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(addDialog, 
                "Error adding consumer: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    });

        cancelButton.addActionListener(e -> addDialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        addDialog.add(formPanel, BorderLayout.CENTER);
        addDialog.add(buttonPanel, BorderLayout.SOUTH);
        addDialog.setLocationRelativeTo(this);
        addDialog.setVisible(true);
    }

    private void deleteConsumer() {
    int selectedRow = consumerTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, 
            "Please select a consumer to delete", 
            "Warning", JOptionPane.WARNING_MESSAGE);
        return;
    }

    String accountNo = (String) consumerTableModel.getValueAt(selectedRow, 0);
    String name = (String) consumerTableModel.getValueAt(selectedRow, 2);

    int confirm = JOptionPane.showConfirmDialog(this, 
        "WARNING: This will permanently delete consumer " + name + "!\n" +
        "All associated bills and payments will be removed.\n" +
        "Are you sure you want to proceed?", 
        "Confirm Permanent Deletion", JOptionPane.YES_NO_OPTION);

    if (confirm == JOptionPane.YES_OPTION) {
        try {
            // Start transaction
            connection.setAutoCommit(false);

            // 1. Get consumer ID first
            int consumerId = 0;
            PreparedStatement getIdStmt = connection.prepareStatement(
                "SELECT id FROM consumers WHERE account_no = ?");
            getIdStmt.setString(1, accountNo);
            ResultSet rs = getIdStmt.executeQuery();
            if (rs.next()) {
                consumerId = rs.getInt("id");
            }
            rs.close();
            getIdStmt.close();

            if (consumerId == 0) {
                JOptionPane.showMessageDialog(this, 
                    "Consumer not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Delete payments first (they reference bills)
            PreparedStatement deletePaymentsStmt = connection.prepareStatement(
                "DELETE FROM payments WHERE bill_id IN " +
                "(SELECT id FROM bills WHERE consumer_id = ?)");
            deletePaymentsStmt.setInt(1, consumerId);
            deletePaymentsStmt.executeUpdate();
            deletePaymentsStmt.close();

            // 3. Then delete bills
            PreparedStatement deleteBillsStmt = connection.prepareStatement(
                "DELETE FROM bills WHERE consumer_id = ?");
            deleteBillsStmt.setInt(1, consumerId);
            deleteBillsStmt.executeUpdate();
            deleteBillsStmt.close();

            // 4. Delete from other related tables (meters, etc.)
            PreparedStatement deleteMetersStmt = connection.prepareStatement(
                "DELETE FROM meters WHERE consumer_id = ?");
            deleteMetersStmt.setInt(1, consumerId);
            deleteMetersStmt.executeUpdate();
            deleteMetersStmt.close();

            // 5. Finally delete the consumer
            PreparedStatement deleteConsumerStmt = connection.prepareStatement(
                "DELETE FROM consumers WHERE id = ?");
            deleteConsumerStmt.setInt(1, consumerId);
            int rowsDeleted = deleteConsumerStmt.executeUpdate();
            deleteConsumerStmt.close();

            if (rowsDeleted > 0) {
                connection.commit();
                consumerTableModel.removeRow(selectedRow);
                JOptionPane.showMessageDialog(this, 
                    "Consumer and all associated data deleted successfully", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadConsumerData(); // Refresh the table
            } else {
                connection.rollback();
                JOptionPane.showMessageDialog(this, 
                    "Failed to delete consumer", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, 
                "Error deleting consumer: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

    private void generateBill() {
    int selectedRow = billingTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a consumer to generate bill", "Warning", JOptionPane.WARNING_MESSAGE);
        return;
    }

    String accountNo = (String) billingTableModel.getValueAt(selectedRow, 0);
    String meterNo = (String) billingTableModel.getValueAt(selectedRow, 1);
    String fullName = (String) billingTableModel.getValueAt(selectedRow, 2);

    JDialog billDialog = new JDialog(this, "Generate Bill", true);
    billDialog.setSize(500, 400);
    billDialog.setLayout(new BorderLayout());

    JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
    formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

    // Non-editable fields
    JTextField accountNoField = new JTextField(accountNo);
    accountNoField.setEditable(false);
    JTextField meterNoField = new JTextField(meterNo);
    meterNoField.setEditable(false);
    JTextField nameField = new JTextField(fullName);
    nameField.setEditable(false);

    // Using a final array to hold the previous reading value
    final double[] previousReadingHolder = {0};
    try {
        PreparedStatement prevStmt = connection.prepareStatement(
            "SELECT current_reading FROM bills WHERE consumer_id = (SELECT id FROM consumers WHERE account_no = ?) ORDER BY billing_date DESC LIMIT 1");
        prevStmt.setString(1, accountNo);
        ResultSet rs = prevStmt.executeQuery();
        if (rs.next()) {
            previousReadingHolder[0] = rs.getDouble("current_reading");
        }
        rs.close();
        prevStmt.close();
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(billDialog, "Error retrieving previous reading: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }

    JTextField prevReadingField = new JTextField(String.valueOf(previousReadingHolder[0]));
    prevReadingField.setEditable(false);
    JTextField currentReadingField = new JTextField();
    
    // Current date for billing
    JTextField billingDateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    billingDateField.setEditable(false);

    formPanel.add(new JLabel("Account Number:"));
    formPanel.add(accountNoField);
    formPanel.add(new JLabel("Meter Number:"));
    formPanel.add(meterNoField);
    formPanel.add(new JLabel("Consumer Name:"));
    formPanel.add(nameField);
    formPanel.add(new JLabel("Previous Reading:"));
    formPanel.add(prevReadingField);
    formPanel.add(new JLabel("Current Reading:"));
    formPanel.add(currentReadingField);
    formPanel.add(new JLabel("Billing Date:"));
    formPanel.add(billingDateField);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    JButton generateButton = new JButton("Generate Bill");
    JButton cancelButton = new JButton("Cancel");

    generateButton.addActionListener(e -> {
        try {
            // Validate current reading
            if (currentReadingField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(billDialog, "Please enter current reading", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double currentReading;
            try {
                currentReading = Double.parseDouble(currentReadingField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(billDialog, "Please enter a valid number for current reading", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Validate reading progression
            if (currentReading < previousReadingHolder[0]) {
                JOptionPane.showMessageDialog(billDialog, 
                    "Current reading must be greater than previous reading (" + previousReadingHolder[0] + ")", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Get current rate
            double rate = 12.0; // Default rate
            try {
                PreparedStatement rateStmt = connection.prepareStatement(
                    "SELECT rate_per_kwh FROM rates ORDER BY effective_date DESC LIMIT 1");
                ResultSet rateRs = rateStmt.executeQuery();
                if (rateRs.next()) {
                    rate = rateRs.getDouble("rate_per_kwh");
                }
                rateRs.close();
                rateStmt.close();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(billDialog, 
                    "Using default rate (12.0) - Couldn't retrieve current rate: " + ex.getMessage(), 
                    "Warning", JOptionPane.WARNING_MESSAGE);
            }

            // Calculate consumption and amount
            double consumption = currentReading - previousReadingHolder[0];
            double amount = consumption * rate;

            // Get consumer ID
            int consumerId = 0;
    int meterId = 0;
    try {
        PreparedStatement consumerStmt = connection.prepareStatement(
            "SELECT c.id, m.id as meter_id FROM consumers c " +
            "LEFT JOIN meters m ON c.id = m.consumer_id " +
            "WHERE c.account_no = ?");
        consumerStmt.setString(1, accountNo);
        ResultSet consumerRs = consumerStmt.executeQuery();
        if (consumerRs.next()) {
            consumerId = consumerRs.getInt("id");
            meterId = consumerRs.getInt("meter_id");
            
            // If no meter exists, create one with installation date
            if (meterId == 0) {
                PreparedStatement meterStmt = connection.prepareStatement(
                    "INSERT INTO meters (consumer_id, meter_number, installation_date) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
                meterStmt.setInt(1, consumerId);
                meterStmt.setString(2, meterNo); // Using the displayed meter number
                meterStmt.setDate(3, new java.sql.Date(System.currentTimeMillis())); // Current date as installation date
                meterStmt.executeUpdate();
                
                ResultSet generatedKeys = meterStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    meterId = generatedKeys.getInt(1);
                }
                meterStmt.close();
            }
        }
        consumerRs.close();
        consumerStmt.close();
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(billDialog, 
            "Error retrieving consumer/meter ID: " + ex.getMessage(), 
            "Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
        return;
    }

    // Insert new bill with meter_id
    try {
        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO bills (consumer_id, meter_id, billing_date, due_date, previous_reading, " +
            "current_reading, consumption, rate, amount, status, created_at) " +
            "VALUES (?, ?, ?, DATE_ADD(?, INTERVAL 15 DAY), ?, ?, ?, ?, ?, 'pending', NOW())");
        
        Date billingDate = new Date(); // Current date
        stmt.setInt(1, consumerId);
        stmt.setInt(2, meterId);
        stmt.setDate(3, new java.sql.Date(billingDate.getTime()));
        stmt.setDate(4, new java.sql.Date(billingDate.getTime()));
        stmt.setDouble(5, previousReadingHolder[0]);
        stmt.setDouble(6, currentReading);
        stmt.setDouble(7, consumption);
        stmt.setDouble(8, rate);
        stmt.setDouble(9, amount);

        int rowsAffected = stmt.executeUpdate();
        stmt.close();

        if (rowsAffected > 0) {
        // Show success message
        JOptionPane.showMessageDialog(billDialog, 
            "Bill generated successfully!\n" +
            "Account: " + accountNo + "\n" +
            "Meter ID: " + meterId + "\n" +
            "Previous: " + previousReadingHolder[0] + "\n" +
            "Current: " + currentReading + "\n" +
            "Consumption: " + consumption + " kWh\n" +
            "Amount: ₱" + String.format("%.2f", amount), 
            "Success", JOptionPane.INFORMATION_MESSAGE);

        // Ask if user wants to print receipt
        int printOption = JOptionPane.showConfirmDialog(billDialog,
            "Would you like to print the bill receipt now?",
            "Print Receipt",
            JOptionPane.YES_NO_OPTION);

        if (printOption == JOptionPane.YES_OPTION) {
            printBillReceipt(consumerId, accountNo, fullName, meterNo, 
                previousReadingHolder[0], currentReading, consumption, rate, amount);
        }

        loadBillingData();
        billDialog.dispose();
        } else {
            JOptionPane.showMessageDialog(billDialog, 
                "Failed to generate bill - no rows affected", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(billDialog, 
            "Error inserting bill: " + ex.getMessage(), 
            "Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(billDialog, 
                "Unexpected error: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    });

    cancelButton.addActionListener(e -> billDialog.dispose());

    buttonPanel.add(generateButton);
    buttonPanel.add(cancelButton);

    billDialog.add(formPanel, BorderLayout.CENTER);
    billDialog.add(buttonPanel, BorderLayout.SOUTH);
    billDialog.setLocationRelativeTo(this);
    billDialog.setVisible(true);
}
    private void printBillReceipt(int consumerId, String accountNo, String fullName, String meterNo,
                            double prevReading, double currReading, double consumption, 
                            double rate, double amount) {
    try {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex > 0) {
                    return Printable.NO_SUCH_PAGE;
                }

                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                // Header
                Font headerFont = new Font("Arial", Font.BOLD, 18);
                g2d.setFont(headerFont);
                g2d.drawString("BILLING RECEIPT", 50, 50);

                // Company Info
                Font companyFont = new Font("Arial", Font.PLAIN, 10);
                g2d.setFont(companyFont);
                g2d.drawString("Aurelco Water Billing System", 50, 70);
                g2d.drawString("Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), 50, 85);

                // Draw line separator
                g2d.drawLine(50, 95, 400, 95);

                // Bill Details
                Font detailFont = new Font("Arial", Font.PLAIN, 12);
                g2d.setFont(detailFont);
                int y = 120;
                g2d.drawString("Account No: " + accountNo, 50, y);
                y += 20;
                g2d.drawString("Consumer Name: " + fullName, 50, y);
                y += 20;
                g2d.drawString("Meter No: " + meterNo, 50, y);
                y += 20;
                g2d.drawString("Previous Reading: " + prevReading, 50, y);
                y += 20;
                g2d.drawString("Current Reading: " + currReading, 50, y);
                y += 20;
                g2d.drawString("Consumption: " + consumption + " cubic meters", 50, y);
                y += 20;
                g2d.drawString("Rate: ₱" + String.format("%.2f", rate) + " per cubic meter", 50, y);
                y += 30;

                // Total Amount
                Font amountFont = new Font("Arial", Font.BOLD, 14);
                g2d.setFont(amountFont);
                g2d.drawString("TOTAL AMOUNT: ₱" + String.format("%.2f", amount), 50, y);
                y += 30;

                // Footer
                Font footerFont = new Font("Arial", Font.ITALIC, 10);
                g2d.setFont(footerFont);
                g2d.drawString("Thank you for your payment!", 50, y);
                y += 15;
                g2d.drawString("Please pay before due date to avoid penalties", 50, y);

                return Printable.PAGE_EXISTS;
            }
        });

        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(null, 
                    "Error printing receipt: " + e.getMessage(), 
                    "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, 
            "Error preparing print job: " + e.getMessage(), 
            "Print Error", JOptionPane.ERROR_MESSAGE);
    }
}

    private void issueNotice() {
        int selectedRow = disconnectionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a consumer to issue notice", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String accountNo = (String) disconnectionTableModel.getValueAt(selectedRow, 0);

        try {
            PreparedStatement checkStmt = connection.prepareStatement(
                "SELECT * FROM disconnections WHERE consumer_id = (SELECT id FROM consumers WHERE account_no = ?) AND status = 'notice_sent'");
            checkStmt.setString(1, accountNo);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Notice already issued for this consumer", "Warning", JOptionPane.WARNING_MESSAGE);
                rs.close();
                checkStmt.close();
                return;
            }
            rs.close();
            checkStmt.close();

            PreparedStatement billStmt = connection.prepareStatement(
                "SELECT b.id FROM bills b JOIN consumers c ON b.consumer_id = c.id " +
                "WHERE c.account_no = ? AND b.status IN ('pending', 'overdue') ORDER BY b.due_date DESC LIMIT 1");
            billStmt.setString(1, accountNo);
            rs = billStmt.executeQuery();

            if (rs.next()) {
                int billId = rs.getInt("id");

                PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO disconnections (consumer_id, bill_id, notice_date, status, processed_by, created_at) " +
                    "SELECT c.id, ?, NOW(), 'notice_sent', ?, NOW() FROM consumers c WHERE c.account_no = ?");
                stmt.setInt(1, billId);
                stmt.setInt(2, currentAdminId);
                stmt.setString(3, accountNo);
                stmt.executeUpdate();

                PreparedStatement updateStmt = connection.prepareStatement(
                    "UPDATE consumers SET status = 'pending disconnection' WHERE account_no = ?");
                updateStmt.setString(1, accountNo);
                updateStmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Disconnection notice issued successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadDisconnectionData();
                loadConsumerData();
            } else {
                JOptionPane.showMessageDialog(this, "No unpaid bills found for this consumer", "Error", JOptionPane.ERROR_MESSAGE);
            }
            rs.close();
            billStmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error issuing disconnection notice: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void disconnectConsumer() {
        int selectedRow = disconnectionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a consumer to disconnect", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String accountNo = (String) disconnectionTableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to disconnect consumer " + accountNo + "?", "Confirm Disconnection", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE disconnections SET disconnection_date = NOW(), status = 'disconnected', processed_by = ? " +
                    "WHERE consumer_id = (SELECT id FROM consumers WHERE account_no = ?) AND status = 'notice_sent'");
                stmt.setInt(1, currentAdminId);
                stmt.setString(2, accountNo);
                stmt.executeUpdate();

                PreparedStatement updateStmt = connection.prepareStatement(
                    "UPDATE consumers SET status = 'disconnected' WHERE account_no = ?");
                updateStmt.setString(1, accountNo);
                updateStmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Consumer disconnected successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadDisconnectionData();
                loadConsumerData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error disconnecting consumer: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void reconnectConsumer() {
        int selectedRow = disconnectionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a consumer to reconnect", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String accountNo = (String) disconnectionTableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to reconnect consumer " + accountNo + "?", "Confirm Reconnection", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE disconnections SET reconnection_date = NOW(), status = 'reconnected', processed_by = ? " +
                    "WHERE consumer_id = (SELECT id FROM consumers WHERE account_no = ?) AND status = 'disconnected'");
                stmt.setInt(1, currentAdminId);
                stmt.setString(2, accountNo);
                stmt.executeUpdate();

                PreparedStatement updateStmt = connection.prepareStatement(
                    "UPDATE consumers SET status = 'active' WHERE account_no = ?");
                updateStmt.setString(1, accountNo);
                updateStmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Consumer reconnected successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadDisconnectionData();
                loadConsumerData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error reconnecting consumer: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void processPayment() {
        int selectedRow = paymentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a bill to process payment", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String accountNo = (String) paymentTableModel.getValueAt(selectedRow, 0);
        String amountStr = (String) paymentTableModel.getValueAt(selectedRow, 3);
        double originalAmount = Double.parseDouble(amountStr.replace("₱", "").replace(",", ""));

        JDialog paymentDialog = new JDialog(this, "Process Payment (Cash Only)", true);
        paymentDialog.setSize(400, 500);
        paymentDialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField amountField = new JTextField(String.format("%.2f", originalAmount));
        JCheckBox seniorDiscountCheck = new JCheckBox("Apply Senior Citizen Discount (5%)", false);
        seniorDiscountCheck.setEnabled(false);
        JTextField cashAmountField = new JTextField();
        JTextField changeField = new JTextField("0.00");
        changeField.setEditable(false);
        JTextField orNumberField = new JTextField();
        JTextField paymentDateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

        // Check Senior Citizen eligibility
        boolean isSenior = false;
        try {
            PreparedStatement stmt = connection.prepareStatement(
                "SELECT birthday FROM consumers WHERE account_no = ?");
            stmt.setString(1, accountNo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Date birthday = rs.getDate("birthday");
                if (birthday != null) {
                    Calendar birthCal = Calendar.getInstance();
                    birthCal.setTime(birthday);
                    Calendar today = Calendar.getInstance();
                    int age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);
                    if (today.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) {
                        age--;
                    }
                    isSenior = age >= 60;
                    seniorDiscountCheck.setEnabled(isSenior);
                    seniorDiscountCheck.setSelected(isSenior);
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(paymentDialog, "Cannot verify Senior Citizen status: " + ex.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
        }

        // Update amount for discount
        seniorDiscountCheck.addActionListener(e -> {
            double amount = originalAmount;
            if (seniorDiscountCheck.isSelected()) {
                amount *= 0.95;
            }
            amountField.setText(String.format("%.2f", amount));
            try {
                double cashAmount = cashAmountField.getText().isEmpty() ? 0 : Double.parseDouble(cashAmountField.getText());
                double change = cashAmount - amount;
                changeField.setText(String.format("%.2f", change >= 0 ? change : 0));
            } catch (NumberFormatException ex) {
                changeField.setText("0.00");
            }
        });

        // Update change
        cashAmountField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateChange(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateChange(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateChange(); }

            private void updateChange() {
                try {
                    double cashAmount = cashAmountField.getText().isEmpty() ? 0 : Double.parseDouble(cashAmountField.getText());
                    double amount = Double.parseDouble(amountField.getText());
                    double change = cashAmount - amount;
                    changeField.setText(String.format("%.2f", change >= 0 ? change : 0));
                } catch (NumberFormatException ex) {
                    changeField.setText("0.00");
                }
            }
        });

        formPanel.add(new JLabel("Account Number:"));
        formPanel.add(new JLabel(accountNo));
        formPanel.add(new JLabel("Amount to Pay (₱):"));
        formPanel.add(amountField);
        formPanel.add(new JLabel("Senior Citizen Discount:"));
        formPanel.add(seniorDiscountCheck);
        formPanel.add(new JLabel("Cash Amount (₱):"));
        formPanel.add(cashAmountField);
        formPanel.add(new JLabel("Change (₱):"));
        formPanel.add(changeField);
        formPanel.add(new JLabel("OR Number:"));
        formPanel.add(orNumberField);
        formPanel.add(new JLabel("Payment Date (YYYY-MM-DD):"));
        formPanel.add(paymentDateField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            try {
                double amountPaid = Double.parseDouble(amountField.getText());
                double cashAmount = Double.parseDouble(cashAmountField.getText());
                String orNumber = orNumberField.getText().trim();
                String paymentDate = paymentDateField.getText();

                if (amountPaid <= 0) {
                    JOptionPane.showMessageDialog(paymentDialog, "Amount must be greater than 0", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (cashAmount < amountPaid) {
                    JOptionPane.showMessageDialog(paymentDialog, "Cash amount must be at least the amount to pay", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (orNumber.isEmpty()) {
                    JOptionPane.showMessageDialog(paymentDialog, "OR Number is required", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!paymentDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    JOptionPane.showMessageDialog(paymentDialog, "Invalid date format. Use YYYY-MM-DD", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                PreparedStatement billStmt = connection.prepareStatement(
                    "SELECT b.id, b.consumer_id FROM bills b JOIN consumers c ON b.consumer_id = c.id " +
                    "WHERE c.account_no = ? AND b.status IN ('pending', 'overdue') AND b.amount = ? LIMIT 1");
                billStmt.setString(1, accountNo);
                billStmt.setDouble(2, originalAmount);
                ResultSet rs = billStmt.executeQuery();

                if (rs.next()) {
                    int billId = rs.getInt("id");
                    int consumerId = rs.getInt("consumer_id");

                    PreparedStatement stmt = connection.prepareStatement(
                        "INSERT INTO payments (bill_id, consumer_id, amount, payment_date, payment_method, or_number, received_by, created_at) " +
                        "VALUES (?, ?, ?, ?, 'Cash', ?, ?, NOW())");
                    stmt.setInt(1, billId);
                    stmt.setInt(2, consumerId);
                    stmt.setDouble(3, amountPaid);
                    stmt.setDate(4, java.sql.Date.valueOf(paymentDate));
                    stmt.setString(5, orNumber);
                    stmt.setInt(6, currentAdminId);
                    stmt.executeUpdate();

                    PreparedStatement updateStmt = connection.prepareStatement(
                        "UPDATE bills SET status = 'paid' WHERE id = ?");
                    updateStmt.setInt(1, billId);
                    updateStmt.executeUpdate();

                    JOptionPane.showMessageDialog(paymentDialog, "Payment processed successfully. Change: ₱" + changeField.getText(), "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadPaymentData();
                    loadBillingData();
                    loadReportData();
                    paymentDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(paymentDialog, "Bill not found", "Error", JOptionPane.ERROR_MESSAGE);
                }
                rs.close();
                billStmt.close();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(paymentDialog, "Please enter valid amounts", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(paymentDialog, "Error processing payment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        cancelButton.addActionListener(e -> paymentDialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        paymentDialog.add(formPanel, BorderLayout.CENTER);
        paymentDialog.add(buttonPanel, BorderLayout.SOUTH);
        paymentDialog.setLocationRelativeTo(this);
        paymentDialog.setVisible(true);
    }

    private void printReport() {
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            
            // Set landscape orientation
            PageFormat pf = job.defaultPage();
            pf.setOrientation(PageFormat.LANDSCAPE);
            
            job.setPrintable(new Printable() {
                @Override
                public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                    if (pageIndex > 0) {
                        return Printable.NO_SUCH_PAGE;
                    }

                    Graphics2D g2d = (Graphics2D) graphics;
                    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                    // Print header
                    Font headerFont = new Font("Arial", Font.BOLD, 16);
                    g2d.setFont(headerFont);
                    String title = "Monthly Billing Report";
                    int titleWidth = g2d.getFontMetrics().stringWidth(title);
                    g2d.drawString(title, (int) (pageFormat.getImageableWidth() / 2 - titleWidth / 2), 50);

                    Font dateFont = new Font("Arial", Font.PLAIN, 12);
                    g2d.setFont(dateFont);
                    String date = "For " + monthComboBox.getSelectedItem() + " " + yearComboBox.getSelectedItem();
                    int dateWidth = g2d.getFontMetrics().stringWidth(date);
                    g2d.drawString(date, (int) (pageFormat.getImageableWidth() / 2 - dateWidth / 2), 80);

                    // Print table with headers
                    JTableHeader header = reportTable.getTableHeader();
                    JTable printTable = new JTable(reportTableModel);
                    printTable.setSize((int) pageFormat.getImageableWidth(), printTable.getPreferredSize().height);
                    printTable.setTableHeader(header);
                    
                    // Print header first
                    header.setSize((int) pageFormat.getImageableWidth(), header.getPreferredSize().height);
                    header.print(g2d);
                    
                    // Then print the table content
                    g2d.translate(0, header.getHeight());
                    printTable.print(g2d);

                    return Printable.PAGE_EXISTS;
                }
            }, pf);

            if (job.printDialog()) {
                job.print();
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, "Error printing report: " + e.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        closeConnection();
        super.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // You should pass the actual admin ID from your login system
            new AdminDashboard(1).setVisible(true);
        });
    }
}