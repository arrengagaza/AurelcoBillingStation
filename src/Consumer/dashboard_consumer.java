package Consumer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import javax.swing.table.JTableHeader;

public class dashboard_consumer extends JFrame {
    private JTabbedPane tabbedPane;
    private JTable billTable, noticeTable;
    private DefaultTableModel billTableModel, noticeTableModel;
    private Connection connection;
    private String consumerId;
    private JLabel profileImageLabel;
    private JTextField lastNameField, firstNameField, middleNameField;
    private JPasswordField passwordField;
    private JTextField accountNoField, meterNoField, emailField, mobileField, addressField;

    public dashboard_consumer(String consumerId) {
        this.consumerId = consumerId;
        initializeDatabase();
        createUI();
        loadConsumerData();
        loadBillData();
        loadNoticeData();
    }

    private void initializeDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/aurelco?useSSL=false&serverTimezone=UTC";
            connection = DriverManager.getConnection(url, "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Database connection failed: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void createUI() {
        setTitle("Consumer Dashboard - Account: " + consumerId);
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header panel with logout button
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Consumer Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (response == JOptionPane.YES_OPTION) {
                new login().setVisible(true);
                dispose();
            }
        });
        headerPanel.add(logoutButton, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Tabbed pane for different sections
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Consumer Info", createConsumerPanel());
        tabbedPane.addTab("Previous Bill & Reading", createBillPanel());
        tabbedPane.addTab("Disconnection Notice", createNoticePanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createConsumerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Profile picture panel
        JPanel profilePanel = new JPanel(new BorderLayout());
        profileImageLabel = new JLabel();
        profileImageLabel.setHorizontalAlignment(JLabel.CENTER);
        profileImageLabel.setPreferredSize(new Dimension(200, 200));
        
        // Load default profile image
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/default_profile.png"));
            Image image = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            profileImageLabel.setIcon(new ImageIcon(image));
        } catch (Exception e) {
            profileImageLabel.setText("No Image");
        }

        JButton uploadButton = new JButton("Upload Profile Picture");
        uploadButton.addActionListener(e -> uploadProfilePicture());

        profilePanel.add(profileImageLabel, BorderLayout.CENTER);
        profilePanel.add(uploadButton, BorderLayout.SOUTH);

        // Info panel
        JPanel infoPanel = new JPanel(new GridLayout(9, 2, 10, 10));
        infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        infoPanel.add(new JLabel("Account Number:"));
        accountNoField = new JTextField();
        accountNoField.setEditable(false);
        infoPanel.add(accountNoField);

        infoPanel.add(new JLabel("Meter Number:"));
        meterNoField = new JTextField();
        meterNoField.setEditable(false);
        infoPanel.add(meterNoField);

        infoPanel.add(new JLabel("Last Name:"));
        lastNameField = new JTextField();
        infoPanel.add(lastNameField);

        infoPanel.add(new JLabel("First Name:"));
        firstNameField = new JTextField();
        infoPanel.add(firstNameField);

        infoPanel.add(new JLabel("Middle Name:"));
        middleNameField = new JTextField();
        infoPanel.add(middleNameField);

        infoPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        emailField.setEditable(false);
        infoPanel.add(emailField);

        infoPanel.add(new JLabel("Mobile:"));
        mobileField = new JTextField();
        mobileField.setEditable(false);
        infoPanel.add(mobileField);

        infoPanel.add(new JLabel("Address:"));
        addressField = new JTextField();
        addressField.setEditable(false);
        infoPanel.add(addressField);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton updateButton = new JButton("Update Info");
        JButton refreshButton = new JButton("Refresh");

        updateButton.addActionListener(e -> updateConsumerInfo());
        refreshButton.addActionListener(e -> loadConsumerData());

        buttonPanel.add(updateButton);
        buttonPanel.add(refreshButton);

        // Add components to main panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(profilePanel, BorderLayout.WEST);
        contentPanel.add(infoPanel, BorderLayout.CENTER);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createBillPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Table setup
        String[] billColumns = {"Billing Period", "Previous Reading", "Current Reading", 
                              "Consumption", "Amount", "Due Date", "Status"};
        billTableModel = new DefaultTableModel(billColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        billTable = new JTable(billTableModel);
        customizeTable(billTable);

        JScrollPane scrollPane = new JScrollPane(billTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadBillData());
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createNoticePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Table setup
        String[] noticeColumns = {"Notice Date", "Disconnection Date", "Amount Due", 
                                "Due Date", "Status", "Reason"};
        noticeTableModel = new DefaultTableModel(noticeColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        noticeTable = new JTable(noticeTableModel);
        customizeTable(noticeTable);

        JScrollPane scrollPane = new JScrollPane(noticeTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadNoticeData());
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
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
    }

    private void loadConsumerData() {
        try {
            String query = "SELECT account_no, meter_number, last_name, given_name, middle_name, " +
                         "email, mobile, address FROM consumers WHERE id = ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, consumerId);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    accountNoField.setText(rs.getString("account_no"));
                    meterNoField.setText(rs.getString("meter_number"));
                    lastNameField.setText(rs.getString("last_name"));
                    firstNameField.setText(rs.getString("given_name"));
                    middleNameField.setText(rs.getString("middle_name"));
                    emailField.setText(rs.getString("email"));
                    mobileField.setText(rs.getString("mobile"));
                    addressField.setText(rs.getString("address"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading consumer data: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadBillData() {
        try {
            billTableModel.setRowCount(0);
            String query = "SELECT DATE_FORMAT(billing_date, '%M %Y') as billing_period, " +
                         "previous_reading, current_reading, " +
                         "(current_reading - previous_reading) as consumption, " +
                         "amount, due_date, status " +
                         "FROM bills WHERE consumer_id = ? ORDER BY billing_date DESC";
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, consumerId);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    Object[] row = {
                        rs.getString("billing_period"),
                        rs.getDouble("previous_reading"),
                        rs.getDouble("current_reading"),
                        rs.getDouble("consumption"),
                        String.format("₱%.2f", rs.getDouble("amount")),
                        new SimpleDateFormat("MM/dd/yyyy").format(rs.getDate("due_date")),
                        rs.getString("status")
                    };
                    billTableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading bill data: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadNoticeData() {
        try {
            noticeTableModel.setRowCount(0);
            String query = "SELECT d.notice_date, d.disconnection_date, b.amount, " +
                         "b.due_date, d.status, d.reason " +
                         "FROM disconnections d " +
                         "JOIN bills b ON d.bill_id = b.id " +
                         "WHERE d.consumer_id = ? ORDER BY d.notice_date DESC";
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, consumerId);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    Object[] row = {
                        new SimpleDateFormat("MM/dd/yyyy").format(rs.getDate("notice_date")),
                        rs.getDate("disconnection_date") != null ? 
                            new SimpleDateFormat("MM/dd/yyyy").format(rs.getDate("disconnection_date")) : "N/A",
                        String.format("₱%.2f", rs.getDouble("amount")),
                        new SimpleDateFormat("MM/dd/yyyy").format(rs.getDate("due_date")),
                        rs.getString("status"),
                        rs.getString("reason") != null ? rs.getString("reason") : "N/A"
                    };
                    noticeTableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading notice data: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateConsumerInfo() {
        try {
            String query = "UPDATE consumers SET last_name = ?, given_name = ?, " +
                         "middle_name = ? WHERE id = ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, lastNameField.getText());
                stmt.setString(2, firstNameField.getText());
                stmt.setString(3, middleNameField.getText());
                stmt.setString(4, consumerId);
                
                int updated = stmt.executeUpdate();
                if (updated > 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Consumer information updated successfully", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadConsumerData();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error updating consumer information: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void uploadProfilePicture() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Profile Picture");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Image files", ImageIO.getReaderFileSuffixes()));
        
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                BufferedImage image = ImageIO.read(selectedFile);
                if (image != null) {
                    Image scaledImage = image.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                    profileImageLabel.setIcon(new ImageIcon(scaledImage));
                    
                    // Here you would save the image to database or file system
                    // For example:
                    // saveProfilePictureToDatabase(selectedFile);
                    
                    JOptionPane.showMessageDialog(this, 
                        "Profile picture updated successfully", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "Error loading image: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    public static void main(String[] args) {
        // For testing - in real usage, you would pass the actual consumer ID
        SwingUtilities.invokeLater(() -> {
            dashboard_consumer dashboard = new dashboard_consumer("1");
            dashboard.setVisible(true);
        });
    }
}