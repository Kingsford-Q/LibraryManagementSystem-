package library;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

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


public class UsersDashboard extends JFrame {
    private JPanel content;

    public UsersDashboard() {
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
                        displayRequestedBooksPanel(currentUsername);
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
    String filePath = new File("src/library/users/users.json").getAbsolutePath();
    JSONArray users = readUsersFromFile(filePath);

    for (int i = 0; i < users.length(); i++) {
        JSONObject user = users.getJSONObject(i);

        if (user.getString("username").equals(username)) {
            String storedHashedPassword = user.getString("password");

            // Verify old password
            if (!BCrypt.checkpw(oldPassword, storedHashedPassword)) {
                JOptionPane.showMessageDialog(null, "Old password is incorrect!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Hash new password and update JSON
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            user.put("password", hashedPassword);
            writeUsersToFile(filePath, users);

            JOptionPane.showMessageDialog(null, "Password changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
    }

    JOptionPane.showMessageDialog(null, "User not found!", "Error", JOptionPane.ERROR_MESSAGE);
}

private JSONArray readUsersFromFile(String filePath) {
    try {
        String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        return new JSONArray(content);
    } catch (IOException e) {
        e.printStackTrace();
        return new JSONArray();
    }
}

private void writeUsersToFile(String filePath, JSONArray users) {
    try (FileWriter file = new FileWriter(filePath)) {
        file.write(users.toString(4));
        file.flush();
    } catch (IOException e) {
        e.printStackTrace();
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
        Object[][] bookData = loadAvailableBooksFromLocalStorage();

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

    // Method to load only available books
    private Object[][] loadAvailableBooksFromLocalStorage() {
        Object[][] allBooks = loadBooksFromLocalStorage();
        return Arrays.stream(allBooks)
            .filter(book -> "Available".equals(book[3])) // Only include available books
            .toArray(Object[][]::new);
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
    
        // Check for duplicate request
        if (isDuplicateRequest(username, title)) {
            JOptionPane.showMessageDialog(null, "You have already requested this book.");
            return;
        }
    
        // Create JSON object for the request
        JSONObject requestObj = new JSONObject();
        requestObj.put("username", username);
        requestObj.put("title", title);
        requestObj.put("author", author);
        requestObj.put("genre", genre);
        requestObj.put("availability", availability);
        requestObj.put("status", "pending");
    
        // Save request to both user and admin files
        saveRequest(username, requestObj);
    
        JOptionPane.showMessageDialog(null, "Book request sent successfully!");
    }

    private void saveRequest(String username, JSONObject bookRequest) {
    try {
        // Define paths
        String userRequestPath = new File("src/library/requests/user_requests/" + username + "_requests.json").getAbsolutePath();
        String adminRequestPath = new File("src/library/requests/admin_requests/requests.json").getAbsolutePath();

        File userRequestsFile = new File(userRequestPath);
        File adminRequestsFile = new File(adminRequestPath);

        // Ensure directories exist
        userRequestsFile.getParentFile().mkdirs();
        adminRequestsFile.getParentFile().mkdirs();

        // Load existing user requests
        JSONArray userRequestsArray = new JSONArray();
        if (userRequestsFile.exists()) {
            String existingData = new String(Files.readAllBytes(userRequestsFile.toPath()), StandardCharsets.UTF_8);
            if (!existingData.isEmpty()) {
                userRequestsArray = new JSONArray(existingData);
            }
        }
        userRequestsArray.put(bookRequest);

        // Save back to file
        Files.write(userRequestsFile.toPath(), userRequestsArray.toString(4).getBytes(StandardCharsets.UTF_8));

        // Load existing admin requests
        JSONArray adminRequestsArray = new JSONArray();
        if (adminRequestsFile.exists()) {
            String existingData = new String(Files.readAllBytes(adminRequestsFile.toPath()), StandardCharsets.UTF_8);
            if (!existingData.isEmpty()) {
                adminRequestsArray = new JSONArray(existingData);
            }
        }
        adminRequestsArray.put(bookRequest);

        // Save back to admin requests file
        Files.write(adminRequestsFile.toPath(), adminRequestsArray.toString(4).getBytes(StandardCharsets.UTF_8));

    } catch (IOException e) {
        e.printStackTrace();
    }
}


    private JSONArray readJsonArray(File file) {
        if (!file.exists()) return new JSONArray(); // If file doesn't exist, return an empty array

        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            return new JSONArray(content);
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONArray(); // Return empty if there's an error
        }
    }

    private boolean isDuplicateRequest(String username, String title) {
        File userFile = new File(new File("").getAbsolutePath() + "/src/library/users/user_requests.json");

        // Read existing requests
        JSONArray userRequestsArray = readJsonArray(userFile);

        for (int i = 0; i < userRequestsArray.length(); i++) {
            JSONObject request = userRequestsArray.getJSONObject(i);
            if (request.getString("username").equals(username) && request.getString("title").equals(title)) {
                return true; // Duplicate found
            }
        }
        return false; // No duplicate
    }

    
    


    private Object[][] loadBooksFromLocalStorage() {
        String filePath = "/library/books/books.json"; // Path inside src/

        try {
            InputStream is = getClass().getResourceAsStream(filePath);
            if (is == null) {
                System.out.println("books.json not found!");
                return new Object[0][4];
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            reader.close();

            JSONArray booksArray = new JSONArray(jsonContent.toString());
            Object[][] data = new Object[booksArray.length()][4];

            for (int i = 0; i < booksArray.length(); i++) {
                JSONObject book = booksArray.getJSONObject(i);
                data[i][0] = book.getString("title");
                data[i][1] = book.getString("author");
                data[i][2] = book.getString("genre");
                data[i][3] = book.getBoolean("available") ? "Available" : "Borrowed";
            }

            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return new Object[0][4];
        }
    }

    private void displayRequestedBooksPanel(String username) {
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
    
        // Table Setup
        String[] columnNames = { "Title", "Author", "Genre", "Status",};
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
        String filePath = "src/library/requests/user_requests/" + username + "_requests.json";
        File file = new File(filePath);
    
        if (!file.exists()) {
            return new Object[0][5]; // Return empty table if file does not exist
        }
    
        try {
            JSONArray requestsArray = readJsonArray(new File(filePath));
            Object[][] data = new Object[requestsArray.length()][5];
    
            for (int i = 0; i < requestsArray.length(); i++) {
                JSONObject book = requestsArray.getJSONObject(i);
                data[i][0] = book.getString("title");
                data[i][1] = book.getString("author");
                data[i][2] = book.getString("genre");
                data[i][3] = book.getString("status");
                data[i][4] = book.optString("requested_at", "N/A");
            }
    
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return new Object[0][5]; // Return empty table on error
        }
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
    
        // Table headers (Added "Return Book" column)
        String[] columnNames = { "Title", "Author", "Genre", "Borrowed At", "Return By", "Action" };
        Object[][] bookData = loadBorrowedBooks(username);
    
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
            String genre = (String) table.getValueAt(selectedRow, 2);
    
            // Update the files to remove the book from borrowed and mark it as available
            processBookReturn(title, author, username);
    
            // Remove the book from the table
            ((DefaultTableModel) table.getModel()).removeRow(selectedRow);
    
            JOptionPane.showMessageDialog(null, "Book returned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void processBookReturn(String title, String author, String username) {
    File borrowedBooksFile = new File("src/library/requests/admin_requests/borrowed_books.json");
    File userBorrowedBooksFile = new File("src/library/requests/user_requests/" + username + "_borrowed_books.json");
    File booksFile = new File("src/library/books/books.json");

    try {
        // Read borrowed books and remove the returned one
        JSONArray borrowedBooks = readJsonArray(borrowedBooksFile);
        JSONArray updatedBorrowedBooks = new JSONArray();

        for (int i = 0; i < borrowedBooks.length(); i++) {
            JSONObject book = borrowedBooks.getJSONObject(i);
            if (!book.getString("title").equals(title) || !book.getString("author").equals(author)) {
                updatedBorrowedBooks.put(book);
            }
        }
        Files.write(borrowedBooksFile.toPath(), updatedBorrowedBooks.toString(4).getBytes(StandardCharsets.UTF_8));

        // Read user borrowed books and remove the returned one
        JSONArray userBorrowedBooks = readJsonArray(userBorrowedBooksFile);
        JSONArray updatedUserBorrowedBooks = new JSONArray();

        for (int i = 0; i < userBorrowedBooks.length(); i++) {
            JSONObject book = userBorrowedBooks.getJSONObject(i);
            if (!book.getString("title").equals(title) || !book.getString("author").equals(author)) {
                updatedUserBorrowedBooks.put(book);
            }
        }
        Files.write(userBorrowedBooksFile.toPath(), updatedUserBorrowedBooks.toString(4).getBytes(StandardCharsets.UTF_8));

        // Update books.json to mark the book as "Available"
        JSONArray books = readJsonArray(booksFile);
        for (int i = 0; i < books.length(); i++) {
            JSONObject book = books.getJSONObject(i);
            if (book.getString("title").equals(title) && book.getString("author").equals(author)) {
                book.put("available", true); // ✅ Boolean update
                book.put("availability", "Available"); // ✅ String update
                break;
            }
        }

        // Debugging: Print updated books.json
        System.out.println("📢 Updated books.json:");
        System.out.println(books.toString(4));

        Files.write(booksFile.toPath(), books.toString(4).getBytes(StandardCharsets.UTF_8));

        // ✅ Refresh UI for admin
        displayBooksPanel();

    } catch (IOException e) {
        e.printStackTrace();
    }
}


    
    private Object[][] loadBorrowedBooks(String username) {
        // Get user-specific borrowed books file path
        String userBorrowedBooksPath = new File("src/library/requests/user_requests/" + username + "_borrowed_books.json").getAbsolutePath();
        File file = new File(userBorrowedBooksPath);
    
        if (!file.exists()) {
            System.out.println("❌ Borrowed books file not found: " + userBorrowedBooksPath);
            return new Object[0][5]; // Return empty if no borrowed books
        }
    
        try {
            JSONArray borrowedBooksArray = readJsonArray(file);
            List<Object[]> filteredBooks = new ArrayList<>();
    
            // Debugging: Print raw JSON content
            System.out.println("📄 Borrowed Books JSON Content:");
            System.out.println(borrowedBooksArray.toString(2));
    
            for (int i = 0; i < borrowedBooksArray.length(); i++) {
                JSONObject bookObject = borrowedBooksArray.getJSONObject(i);
    
                // Debug: Print each book's details
                System.out.println("🔍 Checking book: " + bookObject.getString("title"));
    
                // Ensure book has an "availability" field and is "Borrowed"
                if (!bookObject.has("availability") || !bookObject.getString("availability").equals("Borrowed")) {
                    System.out.println("   ❌ Skipping (Not Borrowed)");
                    continue; // Skip books that aren't borrowed
                }
    
                Object[] bookData = new Object[5];
                bookData[0] = bookObject.getString("title");
                bookData[1] = bookObject.getString("author");
                bookData[2] = bookObject.getString("genre");
                bookData[3] = bookObject.getString("borrowed_at");
                bookData[4] = bookObject.getString("return_by");
    
                filteredBooks.add(bookData);
            }
    
            return filteredBooks.toArray(new Object[0][5]);
    
        } catch (Exception e) {
            e.printStackTrace();
            return new Object[0][5];
        }
    }
    
    
    
}