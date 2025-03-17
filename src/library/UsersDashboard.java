package library;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import library.db.BookDAO;
import library.db.BorrowedBooksDAO;
import library.db.RequestDAO;
import library.db.UserDAO;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.combine;



public class UsersDashboard extends JFrame {
    private JPanel content;

    public UsersDashboard() {

        ImageIcon windowIcon = new ImageIcon(getClass().getResource("/library/images/logo.png"));
        setIconImage(windowIcon.getImage());
        
        setTitle("Users Dashboard - Library Management System");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Sidebar Panel
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(260, getHeight()));
        sidebar.setBackground(new Color(30, 30, 30));
        sidebar.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(new Color(30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 10, 20, 10);
        gbc.gridx = 0;
        gbc.weightx = 1;

        String[] menuItems = {"View Books", "Requested Books", "Borrowed Books", "Change Password"};

        for (int i = 0; i < menuItems.length; i++) {
            int iconNumber = 13 + i;  // Start from icon12.png, then icon13.png, etc.
            JButton button = createSidebarButton(menuItems[i], "library/images/icon" + iconNumber + ".png");
            gbc.gridy = i;

            if (menuItems[i].equals("View Books")) {
                button.addActionListener(e -> displayBooksPanel());
            } else if (menuItems[i].equals("Borrowed Books")) {
                button.addActionListener(e -> displayBorrowedBooksPanel());
            } else if (menuItems[i].equals("Change Password")) {
                button.addActionListener(e -> showChangePasswordDialog());
            } else if (menuItems[i].equals("Requested Books")) { 
                button.addActionListener(e -> {
                    String currentUsername = SessionManager.getLoggedInUser();
                    if (currentUsername != null) {
                        displayRequestedBooksPanel();
                    } else {
                        JOptionPane.showMessageDialog(null, "Error: No user is logged in!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }

            buttonPanel.add(button, gbc);
        }

        // Logout Button
        gbc.gridy++;
        gbc.weighty = 1; // Pushes everything above up
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        buttonPanel.add(filler, gbc);

    // **Add the Logout button at the bottom**
        gbc.gridy++;
        gbc.weighty = 0; // Reset weighty so Logout stays at the bottom
        JButton logoutButton = createSidebarButton("Logout", "library/images/logout.png", new Color(180, 0, 0));
        logoutButton.addActionListener(e -> logout());
        buttonPanel.add(logoutButton, gbc);

        JScrollPane sidebarScroll = new JScrollPane(buttonPanel);
        sidebarScroll.setBorder(null);
        sidebarScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sidebarScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sidebar.add(sidebarScroll, BorderLayout.CENTER);

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
        header.setPreferredSize(new Dimension(getWidth(), 70));
        header.setBackground(new Color(20, 20, 20));

        JLabel logoLabel = new JLabel(loadImage("library/images/logo.png", 50, 50));
        JLabel titleLabel = new JLabel("Library Management - Users Panel", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        header.add(logoLabel);
        header.add(titleLabel);

        // Content Area
        content = new JPanel(new BorderLayout());
        content.setBackground(new Color(200, 200, 200));
        content.add(new JLabel("Select an option from the sidebar", JLabel.CENTER), BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
        setVisible(true);
    }

    private JButton createSidebarButton(String text, String iconPath) {
        return createSidebarButton(text, iconPath, new Color(50, 50, 50));
    }

    private JButton createSidebarButton(String text, String iconPath, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);

        ImageIcon icon = loadImage(iconPath, 20, 20);
        if (icon != null) {
            button.setIcon(icon);
        }

        // Hover Effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private ImageIcon loadImage(String path, int width, int height) {
        java.net.URL imgUrl = getClass().getResource("/" + path);
        if (imgUrl != null) {
            ImageIcon icon = new ImageIcon(imgUrl);
            Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } else {
            System.out.println("❌ Image NOT found: " + path);
            return null;
        }
    }

    private void logout() {
        int option = JOptionPane.showConfirmDialog(
            null, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
    
        if (option == JOptionPane.YES_OPTION) {
            // Clear the logged-in user session
            SessionManager.clearSession();
    
            // Show login window
            LoginWindow login = new LoginWindow();
            login.setVisible(true);
    
            // Close the current user dashboard
            dispose();
        }
    }
    

    private void showChangePasswordDialog() {
    JPasswordField oldPasswordField = new JPasswordField();
    JPasswordField newPasswordField = new JPasswordField();
    JPasswordField confirmPasswordField = new JPasswordField();

    Object[] fields = {
        "Old Password:", oldPasswordField,
        "New Password:", newPasswordField,
        "Confirm New Password:", confirmPasswordField
    };

    int option = JOptionPane.showConfirmDialog(
        null, fields, "Change Password", JOptionPane.OK_CANCEL_OPTION);

    if (option == JOptionPane.OK_OPTION) {
        String oldPassword = new String(oldPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Fields cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(null, "New passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String loggedInUser = SessionManager.getLoggedInUser();
        if (loggedInUser == null) {
            JOptionPane.showMessageDialog(null, "Error: No user is logged in!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        verifyAndChangePassword(loggedInUser, oldPassword, newPassword);
    }
}

private void verifyAndChangePassword(String username, String oldPassword, String newPassword) {
    boolean success = UserDAO.changePassword(username, oldPassword, newPassword);

    if (success) {
        JOptionPane.showMessageDialog(null, "✅ Password changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    } else {
        JOptionPane.showMessageDialog(null, "❌ Incorrect old password!", "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    private void displayBooksPanel() {
        content.removeAll();
        content.setLayout(new BorderLayout());

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchPanel.setBackground(new Color(245, 245, 245));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 16));
        searchField.setPreferredSize(new Dimension(300, 40));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 150, 150), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 15));
        searchButton.setForeground(Color.WHITE);
        searchButton.setBackground(new Color(50, 150, 50));
        searchButton.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchButton.setFocusPainted(false);

        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        content.add(searchPanel, BorderLayout.NORTH);

        String[] columnNames = { "Title", "Author", "Genre", "Availability", "Actions" };

        List<Document> books = BookDAO.getAllUserBooks();
        Object[][] bookData = new Object[books.size()][5];

        for (int i = 0; i < books.size(); i++) {
            Document book = books.get(i);
            bookData[i][0] = book.getString("title");
            bookData[i][1] = book.getString("author");
            bookData[i][2] = book.getString("genre");
            bookData[i][3] = book.getBoolean("available") ? "Available" : "Borrowed";
            bookData[i][4] = "Edit | Delete"; // Placeholder for buttons
        }

        DefaultTableModel model = new DefaultTableModel(bookData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only the Actions column is editable
            }
        };

        JTable bookTable = new JTable(model);
        bookTable.setRowHeight(40); // Set consistent row height
        bookTable.setFont(new Font("Arial", Font.PLAIN, 14)); // Set font size
        bookTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14)); // Set header font size
        bookTable.getTableHeader().setBackground(new Color(200, 200, 200)); // Set header background color
        bookTable.getTableHeader().setForeground(Color.BLACK); // Set header text color
        bookTable.setGridColor(new Color(220, 220, 220)); // Set grid color

        bookTable.getColumn("Actions").setCellRenderer(new RequestButtonRenderer());
        bookTable.getColumn("Actions").setCellEditor(new requestButtonEditor(new JCheckBox(), bookTable));

        JScrollPane scrollPane = new JScrollPane(bookTable);

        content.add(scrollPane, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();

        // Search functionality
        searchButton.addActionListener(e -> {
            String query = searchField.getText().trim().toLowerCase();
            filterTable(bookTable, query);
        });
    }

    // Method to filter the table based on the search query
    private void filterTable(JTable table, String query) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        if (query.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
        }
    }


    // Custom renderer for the borrow button
    class RequestButtonRenderer extends JPanel implements TableCellRenderer {
        private final JButton requestButton = new JButton("Request Book");

        public RequestButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER));
            requestButton.setPreferredSize(new Dimension(130, 40));
            requestButton.setFont(new Font("Arial", Font.PLAIN, 14));
            add(requestButton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            table.setRowHeight(row, requestButton.getPreferredSize().height);
            return this;
        }
    }

    // Custom editor for the borrow button
    class requestButtonEditor extends DefaultCellEditor {
        private final JPanel panel = new JPanel();
        private final JButton requestButton = new JButton("Request Book");
        private final JTable bookTable;

        public requestButtonEditor(JCheckBox checkBox, JTable bookTable) {
            super(checkBox);
            this.bookTable = bookTable;
            panel.setLayout(new FlowLayout(FlowLayout.CENTER));
            requestButton.setPreferredSize(new Dimension(130, 40));
            requestButton.setFont(new Font("Arial", Font.PLAIN, 14));
            panel.add(requestButton);

            requestButton.addActionListener(e -> requestBook(bookTable, bookTable.getSelectedRow()));
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            table.setRowHeight(row, requestButton.getPreferredSize().height);
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    // Borrow book method
    public void requestBook(JTable bookTable, int selectedRow) {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select a book to request.");
            return;
        }
    
        // Get book details
        String title = (String) bookTable.getValueAt(selectedRow, 0);
        String author = (String) bookTable.getValueAt(selectedRow, 1);
        String genre = (String) bookTable.getValueAt(selectedRow, 2);
        String availability = (String) bookTable.getValueAt(selectedRow, 3);
    
        // Get logged-in user
        String username = SessionManager.getLoggedInUser();
        if (username == null) {
            JOptionPane.showMessageDialog(null, "Error: User not logged in.");
            return;
        }
    
        // Check for duplicate request in MongoDB
        if (RequestDAO.isDuplicateRequest(username, title)) {
            JOptionPane.showMessageDialog(null, "You have already requested this book.");
            return;
        }
    
        // ✅ Save request to MongoDB
        RequestDAO.addBookRequest(username, title, author, genre, availability);
    
        JOptionPane.showMessageDialog(null, "Book request sent successfully!");
    
        // ✅ Refresh the Requests Panel to show the new request
        displayRequestedBooksPanel();
    }
    

    
    private void displayRequestedBooksPanel() {
        content.removeAll();
        content.setLayout(new BorderLayout());
    
        // ✅ Get logged-in user automatically
        String username = SessionManager.getLoggedInUser();
        if (username == null) {
            JOptionPane.showMessageDialog(null, "Error: User not logged in.");
            return;
        }
    
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchPanel.setBackground(new Color(245, 245, 245));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
        JTextField searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 16));
        searchField.setPreferredSize(new Dimension(300, 40));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 150, 150), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    
        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 15));
        searchButton.setForeground(Color.WHITE);
        searchButton.setBackground(new Color(50, 150, 50));
        searchButton.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchButton.setFocusPainted(false);
    
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        content.add(searchPanel, BorderLayout.NORTH);
    
        // ✅ Fetch user’s requests from MongoDB
        String[] columnNames = { "Title", "Author", "Genre", "Status", "Requested At"};
        Object[][] bookData = loadRequestedBooks(username);
    
        DefaultTableModel model = new DefaultTableModel(bookData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // No editable columns
            }
        };
    
        JTable requestTable = new JTable(model);
        requestTable.setRowHeight(40);
        requestTable.setFont(new Font("Arial", Font.PLAIN, 14));
        requestTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        requestTable.getTableHeader().setBackground(new Color(200, 200, 200));
        requestTable.getTableHeader().setForeground(Color.BLACK);
        requestTable.setGridColor(new Color(220, 220, 220));
    
        JScrollPane scrollPane = new JScrollPane(requestTable);
        content.add(scrollPane, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    
        // Search functionality
        searchButton.addActionListener(e -> {
            String query = searchField.getText().trim().toLowerCase();
            filterTable(requestTable, query);
        });
    }
    

    private Object[][] loadRequestedBooks(String username) {
        List<Document> requests = RequestDAO.getUserRequestedBooks(username); // ✅ Fetch requests for user
        
        Object[][] data = new Object[requests.size()][5];
        for (int i = 0; i < requests.size(); i++) {
            Document book = requests.get(i);
            data[i][0] = book.getString("title");
            data[i][1] = book.getString("author");
            data[i][2] = book.getString("genre");
            data[i][3] = book.getString("status");
            data[i][4] = book.getString("requested_at"); // Ensure `requested_at` exists in MongoDB
        }
        return data;
    }

    private void displayBorrowedBooksPanel() {
        content.removeAll();
        content.setLayout(new BorderLayout());
    
        // Get logged-in user
        String username = SessionManager.getLoggedInUser();
        if (username == null) {
            JOptionPane.showMessageDialog(null, "User not logged in!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Object[]> borrowedBooksList = BorrowedBooksDAO.getUserBorrowedBooks(username);
        Object[][] bookData = borrowedBooksList.toArray(new Object[0][]);

        // Debugging Output
        System.out.println("✅ Loaded Borrowed Books Data for " + username);
        for (Object[] row : bookData) {
            System.out.println(Arrays.toString(row));
        }
    
        // Table headers (Added "Return Book" column)
        String[] columnNames = { "Title", "Author", "Genre", "Borrowed At", "Return By", "Action" };
    
        // Debugging output
        System.out.println("✅ Loaded Borrowed Books Data:");
        for (Object[] row : bookData) {
            System.out.println(Arrays.toString(row));
        }
    
        // Create table model
        DefaultTableModel model = new DefaultTableModel(bookData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only the "Return Book" button column is editable
            }
        };
    
        // Create JTable
        JTable borrowedBooksTable = new JTable(model);
        borrowedBooksTable.setRowHeight(40);
        borrowedBooksTable.setFont(new Font("Arial", Font.PLAIN, 14));
        borrowedBooksTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        borrowedBooksTable.getTableHeader().setBackground(new Color(200, 200, 200));
        borrowedBooksTable.getTableHeader().setForeground(Color.BLACK);
        borrowedBooksTable.setGridColor(new Color(220, 220, 220));
    
        // Set custom renderer and editor for "Return Book" button
        borrowedBooksTable.getColumn("Action").setCellRenderer(new ReturnBookButtonRenderer());
        borrowedBooksTable.getColumn("Action").setCellEditor(new ReturnBookButtonEditor(new JCheckBox(), borrowedBooksTable, username));
    
        // Wrap table inside a scrollable pane
        JScrollPane scrollPane = new JScrollPane(borrowedBooksTable);
        content.add(scrollPane, BorderLayout.CENTER);
    
        // Refresh UI
        content.revalidate();
        content.repaint();
    
        System.out.println("📢 Borrowed Books Panel Updated");
    }

    class ReturnBookButtonRenderer extends JPanel implements TableCellRenderer {
        private final JButton returnButton = new JButton("Return Book");
    
        public ReturnBookButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER));
            
            // Button Styling
            returnButton.setPreferredSize(new Dimension(120, 30));
            returnButton.setFont(new Font("Arial", Font.PLAIN, 14));
            returnButton.setBackground(new Color(200, 50, 50)); // Red button
            returnButton.setForeground(Color.WHITE);
            returnButton.setFocusPainted(false);
    
            add(returnButton);
        }
    
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    class ReturnBookButtonEditor extends DefaultCellEditor {
        private final JButton returnButton;
        private JTable table;
        private String username;
        private int selectedRow;
    
        public ReturnBookButtonEditor(JCheckBox checkBox, JTable table, String username) {
            super(checkBox);
            this.table = table;
            this.username = username;
    
            returnButton = new JButton("Return Book");
            returnButton.setFont(new Font("Arial", Font.PLAIN, 14));
            returnButton.setBackground(new Color(200, 50, 50));
            returnButton.setForeground(Color.WHITE);
            returnButton.setFocusPainted(false);
            returnButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    
            returnButton.addActionListener(e -> returnBook());
        }
    
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            selectedRow = row;
            return returnButton;
        }
    
        private void returnBook() {
            // Get book details from the row
            String title = (String) table.getValueAt(selectedRow, 0);
            String author = (String) table.getValueAt(selectedRow, 1);
    
            boolean success = BorrowedBooksDAO.processBookReturn(title, author, username);

            if (success) {
                ((DefaultTableModel) table.getModel()).removeRow(selectedRow);
                JOptionPane.showMessageDialog(null, "Book returned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Error returning book. Check logs!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    
    
    }
}