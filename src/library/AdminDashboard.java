package library;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import org.mindrot.jbcrypt.BCrypt;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class AdminDashboard extends JFrame {
    
    private JPanel content;
     // Main content panel

    public AdminDashboard() {
        setTitle("Admin Dashboard - Library Management System");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Sidebar Panel (Fixed Width)
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

        String[] menuItems = {
            "View Books", "Add Book",
            "Accept Borrow Requests", "Accept Return Requests","Add User", "Add Admin", "Delete Admin",
            "Change Password", "Borrowed Books"
        };

        for (int i = 0; i < menuItems.length; i++) {
            JButton button = createSidebarButton(menuItems[i], "images/icon" + (i + 1) + ".png");
            gbc.gridy = i;

            if (menuItems[i].equals("View Books")) {
                button.addActionListener(e -> displayBooksPanel());
            }

            if (menuItems[i].equals("Accept Borrow Requests")) {
                button.addActionListener(e -> displayRequestedBooksPanel());
            }

            // Add this inside AdminDashboard constructor, where buttons are created
            if (menuItems[i].equals("Add Book")) {
                button.addActionListener(e -> addBookPanel()); // Make "Add Book" functional
            }

            if (menuItems[i].equals("Add User")) {
                button.addActionListener(e -> addUser());
            }

            if (menuItems[i].equals("Add Admin")) {
                button.addActionListener(e -> addAdmin());
            }

            if (menuItems[i].equals("Delete Admin")) {
                button.addActionListener(e -> deleteAdminPanel()); // Show delete admin panel
            }

            if (menuItems[i].equals("Change Password")) {
                button.addActionListener(e -> showChangePasswordDialog()); // Show delete admin panel
            }
            if (menuItems[i].equals("Borrowed Books")) {
                button.addActionListener(e -> displayBorrowedBooksPanel()); 
            }

            
            buttonPanel.add(button, gbc);
        }

        // Logout Button
        gbc.gridy = menuItems.length;
        JButton logoutButton = createSidebarButton("Logout", "images/logout.png", new Color(180, 0, 0));
        logoutButton.addActionListener(e -> logout()); // Ensure it triggers the logout function
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

        JLabel logoLabel = new JLabel(loadImage("images/logo.png", 50, 50));
        JLabel titleLabel = new JLabel("Library Management - Admin Panel", JLabel.CENTER);
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

    private void addAdmin() {
        content.removeAll();
        content.revalidate();
        content.repaint();
        
        content.setLayout(new BorderLayout());
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(15, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel heading = new JLabel("Add New Admin", SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        heading.setForeground(new Color(50, 50, 50));
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(heading, gbc);
        
        gbc.anchor = GridBagConstraints.WEST;
        
        JTextField usernameField = createRoundedTextField();
        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 150, 150), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        passwordField.setPreferredSize(new Dimension(250, 40));
        
        JButton addButton = createStyledButton("Add Admin", new Color(50, 150, 50));
        
        addButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
        
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "All fields must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        
            String hashedPassword = hashPassword(password);
            saveAdminToJson(username, hashedPassword);

            usernameField.setText("");
            passwordField.setText("");

        });
        
        JLabel spacer = new JLabel(" ");
        spacer.setPreferredSize(new Dimension(1, 20));
        gbc.gridy++;
        panel.add(spacer, gbc);
        
        addFormRow(panel, "Username:", usernameField, gbc);
        addFormRow(panel, "Password:", passwordField, gbc);
        
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(addButton, gbc);
        
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        content.add(scrollPane, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }
    
    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }
    
    private void saveAdminToJson(String username, String hashedPassword) {
        try {
            String basePath = Paths.get("").toAbsolutePath().toString();
            String filePath = Paths.get(basePath, "src", "library", "users", "admins.json").toString();
        
            File file = new File(filePath);
            File parentDir = file.getParentFile();
        
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
        
            JSONArray adminArray;
            if (file.exists() && file.length() > 0) {
                String jsonContent = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                adminArray = new JSONArray(jsonContent);
            } else {
                adminArray = new JSONArray();
            }
        
            for (int i = 0; i < adminArray.length(); i++) {
                JSONObject admin = adminArray.getJSONObject(i);
                if (admin.getString("username").equals(username)) {
                    JOptionPane.showMessageDialog(null, "Username already exists!");
                    return;
                }
            }
        
            JSONObject newAdmin = new JSONObject();
            newAdmin.put("username", username);
            newAdmin.put("password", hashedPassword);
            adminArray.put(newAdmin);
        
            Files.write(file.toPath(), adminArray.toString(4).getBytes(StandardCharsets.UTF_8));
        
            JOptionPane.showMessageDialog(null, "Admin added successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving admin: " + e.getMessage());
        }
    }

    private void addUser() {
        content.removeAll();
        content.revalidate();
        content.repaint();
        
        content.setLayout(new BorderLayout());
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(15, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel heading = new JLabel("Add New User", SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        heading.setForeground(new Color(50, 50, 50));
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(heading, gbc);
        
        gbc.anchor = GridBagConstraints.WEST;
        
        JTextField usernameField = createRoundedTextField();
        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 150, 150), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        passwordField.setPreferredSize(new Dimension(250, 40));
        
        JButton addButton = createStyledButton("Add User", new Color(50, 150, 50));
        
        addButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
        
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "All fields must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        
            String hashedPassword = hashPassword(password);
            saveUserToJson(username, hashedPassword);

            usernameField.setText("");
            passwordField.setText("");

        });
        
        JLabel spacer = new JLabel(" ");
        spacer.setPreferredSize(new Dimension(1, 20));
        gbc.gridy++;
        panel.add(spacer, gbc);
        
        addFormRow(panel, "Username:", usernameField, gbc);
        addFormRow(panel, "Password:", passwordField, gbc);
        
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(addButton, gbc);
        
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        content.add(scrollPane, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }
    
    private void saveUserToJson(String username, String hashedPassword) {
        try {
            String basePath = Paths.get("").toAbsolutePath().toString();
            String filePath = Paths.get(basePath, "src", "library", "users", "users.json").toString();
        
            File file = new File(filePath);
            File parentDir = file.getParentFile();
        
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
        
            JSONArray adminArray;
            if (file.exists() && file.length() > 0) {
                String jsonContent = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                adminArray = new JSONArray(jsonContent);
            } else {
                adminArray = new JSONArray();
            }
        
            for (int i = 0; i < adminArray.length(); i++) {
                JSONObject admin = adminArray.getJSONObject(i);
                if (admin.getString("username").equals(username)) {
                    JOptionPane.showMessageDialog(null, "Username already exists!");
                    return;
                }
            }
        
            JSONObject newAdmin = new JSONObject();
            newAdmin.put("username", username);
            newAdmin.put("password", hashedPassword);
            adminArray.put(newAdmin);
        
            Files.write(file.toPath(), adminArray.toString(4).getBytes(StandardCharsets.UTF_8));
        
            JOptionPane.showMessageDialog(null, "User added successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving user: " + e.getMessage());
        }
    }
    
    

    private ImageIcon loadImage(String path, int width, int height) {
        String newPath = "src/library/" + path; // Adjusted path
        File file = new File(newPath);
        if (file.exists()) {
            ImageIcon icon = new ImageIcon(newPath);
            Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } else {
            System.out.println("Image NOT found: " + newPath);
        }
        return null;
    }

    // Display Books Panel
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
        Object[][] bookData = loadBooksFromLocalStorage();

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

        bookTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        bookTable.getColumn("Actions").setCellEditor(new ButtonEditor(new JCheckBox(), bookTable));

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

    // Custom renderer for the action buttons
    class ButtonRenderer extends JPanel implements TableCellRenderer {
        private final JButton editButton = new JButton("Edit");
        private final JButton deleteButton = new JButton("Delete");
    
        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER));
            editButton.setPreferredSize(new Dimension(100, 40)); // Set preferred size
            editButton.setFont(new Font("Arial", Font.PLAIN, 14)); // Set font size
            deleteButton.setPreferredSize(new Dimension(100, 40)); // Set preferred size
            deleteButton.setFont(new Font("Arial", Font.PLAIN, 14)); // Set font size
            add(editButton);
            add(deleteButton);
        }
    
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            table.setRowHeight(row, Math.max(editButton.getPreferredSize().height, deleteButton.getPreferredSize().height));
            return this;
        }
    }
    
    // Custom editor for the action buttons
    class ButtonEditor extends DefaultCellEditor {
        private final JPanel panel = new JPanel();
        private final JButton editButton = new JButton("Edit");
        private final JButton deleteButton = new JButton("Delete");
        private final JTable bookTable;
    
        public ButtonEditor(JCheckBox checkBox, JTable bookTable) {
            super(checkBox);
            this.bookTable = bookTable;
            panel.setLayout(new FlowLayout(FlowLayout.CENTER));
            editButton.setPreferredSize(new Dimension(100, 40)); // Set preferred size
            editButton.setFont(new Font("Arial", Font.PLAIN, 14)); // Set font size
            deleteButton.setPreferredSize(new Dimension(100, 40)); // Set preferred size
            deleteButton.setFont(new Font("Arial", Font.PLAIN, 14)); // Set font size
            panel.add(editButton);
            panel.add(deleteButton);
    
            editButton.addActionListener(e -> editBook(bookTable, bookTable.getSelectedRow()));
            deleteButton.addActionListener(e -> deleteBook(bookTable, bookTable.getSelectedRow()));
        }
    
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            table.setRowHeight(row, Math.max(editButton.getPreferredSize().height, deleteButton.getPreferredSize().height));
            return panel;
        }
    
        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
    
    // Method to edit a book
    private void editBook(JTable bookTable, int row) {
        String title = (String) bookTable.getValueAt(row, 0);
        String author = (String) bookTable.getValueAt(row, 1);
        String genre = (String) bookTable.getValueAt(row, 2);
        boolean available = bookTable.getValueAt(row, 3).equals("Available");

        JFrame editFrame = new JFrame("Edit Book");
        editFrame.setSize(400, 400);
        editFrame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(15, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel heading = new JLabel("Edit Book", SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        heading.setForeground(new Color(50, 50, 50));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(heading, gbc);

        gbc.anchor = GridBagConstraints.WEST;

        JTextField titleField = createRoundedTextField();
        titleField.setText(title);
        JTextField authorField = createRoundedTextField();
        authorField.setText(author);
        JTextField genreField = createRoundedTextField();
        genreField.setText(genre);

        JCheckBox availableCheckBox = new JCheckBox("Available");
        availableCheckBox.setBackground(new Color(245, 245, 245));
        availableCheckBox.setFont(new Font("Arial", Font.PLAIN, 14));
        availableCheckBox.setSelected(available);

        JButton saveButton = createStyledButton("Save Changes", new Color(50, 150, 50));

        saveButton.addActionListener(e -> {
            String newTitle = titleField.getText().trim();
            String newAuthor = authorField.getText().trim();
            String newGenre = genreField.getText().trim();
            boolean newAvailable = availableCheckBox.isSelected();

            if (newTitle.isEmpty() || newAuthor.isEmpty() || newGenre.isEmpty()) {
                JOptionPane.showMessageDialog(null, "All fields must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            saveBookChanges(bookTable, row, newTitle, newAuthor, newGenre, newAvailable);
            editFrame.dispose();
        });

        gbc.gridy++;
        JLabel spacer = new JLabel(" ");
        spacer.setPreferredSize(new Dimension(1, 20));
        panel.add(spacer, gbc);

        addFormRow(panel, "Title:", titleField, gbc);
        addFormRow(panel, "Author:", authorField, gbc);
        addFormRow(panel, "Genre:", genreField, gbc);

        gbc.gridy++;
        panel.add(availableCheckBox, gbc);

        gbc.gridy++;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(saveButton, gbc);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        editFrame.add(scrollPane, BorderLayout.CENTER);
        editFrame.setVisible(true);
    }

    // Method to save the changes to the book
    private void saveBookChanges(JTable bookTable, int row, String newTitle, String newAuthor, String newGenre, boolean newAvailable) {
        String oldTitle = (String) bookTable.getValueAt(row, 0);
        String filePath = new File("src/library/books/books.json").getAbsolutePath();
        File file = new File(filePath);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            reader.close();

            JSONArray booksArray = new JSONArray(jsonContent.toString());
            for (int i = 0; i < booksArray.length(); i++) {
                JSONObject book = booksArray.getJSONObject(i);
                if (book.getString("title").equals(oldTitle)) {
                    book.put("title", newTitle);
                    book.put("author", newAuthor);
                    book.put("genre", newGenre);
                    book.put("available", newAvailable);
                    break;
                }
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(booksArray.toString(4));
            writer.close();

            JOptionPane.showMessageDialog(this, "Book updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            displayBooksPanel();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating book!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Method to delete a book
    private void deleteBook(JTable bookTable, int row) {
        String title = (String) bookTable.getValueAt(row, 0);
        String filePath = new File("src/library/books/books.json").getAbsolutePath();
        File file = new File(filePath);
    
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            reader.close();
    
            JSONArray booksArray = new JSONArray(jsonContent.toString());
            for (int i = 0; i < booksArray.length(); i++) {
                JSONObject book = booksArray.getJSONObject(i);
                if (book.getString("title").equals(title)) {
                    booksArray.remove(i);
                    break;
                }
            }
    
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(booksArray.toString(4)); // Pretty-print JSON
            writer.close();
    
            JOptionPane.showMessageDialog(this, "Book deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            displayBooksPanel(); // Refresh books table
    
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting book!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Load books from books.json
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


    // Show Add Book Form
    private void addBookPanel() {
        content.removeAll(); // Clears previous content properly
        content.revalidate();
        content.repaint();
    
        content.setLayout(new BorderLayout());
    
        // Main Panel Styling
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245)); // Light Gray Background
        panel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60)); // Padding
    
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(15, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(245, 245, 245));
    
        // Header Title (Centered)
        JLabel heading = new JLabel("Add New Book", SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        heading.setForeground(new Color(50, 50, 50));
    
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Make it span across two columns
        gbc.anchor = GridBagConstraints.CENTER; // Center the title
        gbc.insets = new Insets(0, 0, 10, 0); // Add bottom spacing for separation
        panel.add(heading, gbc);
    
        // Reset anchor for the rest of the elements
        gbc.anchor = GridBagConstraints.WEST;
    
        // Text Fields & Labels
        JTextField titleField = createRoundedTextField();
        JTextField authorField = createRoundedTextField();
        JTextField genreField = createRoundedTextField();
    
        JCheckBox availableCheckBox = new JCheckBox("Available");
        availableCheckBox.setBackground(new Color(245, 245, 245));
        availableCheckBox.setFont(new Font("Arial", Font.PLAIN, 14));
    
        JButton addButton = createStyledButton("Add Book", new Color(50, 150, 50));
    
        addButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String genre = genreField.getText().trim();
            boolean available = availableCheckBox.isSelected();
    
            if (title.isEmpty() || author.isEmpty() || genre.isEmpty()) {
                JOptionPane.showMessageDialog(null, "All fields must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            addBookToStorage(title, author, genre, available);
        });
    
        // Spacer Below the Title (Extra Space Before Form Fields)
        gbc.gridy++;
        JLabel spacer = new JLabel(" ");
        spacer.setPreferredSize(new Dimension(1, 20)); // Adjust spacing as needed
        panel.add(spacer, gbc);
    
        // Adding Form Fields
        addFormRow(panel, "Title:", titleField, gbc);
        addFormRow(panel, "Author:", authorField, gbc);
        addFormRow(panel, "Genre:", genreField, gbc);
    
        gbc.gridy++;
        panel.add(availableCheckBox, gbc);
    
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(addButton, gbc);
    
        // Scroll Pane (In case of small screens)
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    
        content.add(scrollPane, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }
    

// 🏗️ Create Rounded TextField
private JTextField createRoundedTextField() {
    JTextField textField = new JTextField(20); // Keeps the width
    textField.setFont(new Font("Arial", Font.PLAIN, 16)); // Increase font size
    textField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(150, 150, 150), 1), // Outer border
        BorderFactory.createEmptyBorder(8, 10, 8, 10) // Inner padding (top, left, bottom, right)
    ));
    textField.setPreferredSize(new Dimension(250, 40)); // Keep good height
    return textField;
}


// 🎨 Create Styled Button
private JButton createStyledButton(String text, Color bgColor) {
    JButton button = new JButton(text);
    button.setFont(new Font("Arial", Font.BOLD, 15));
    button.setForeground(Color.WHITE);
    button.setBackground(bgColor);
    button.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    button.setFocusPainted(false);
    return button;
}

// 🏗️ Helper Function to Add Form Rows
private void addFormRow(JPanel panel, String labelText, JTextField field, GridBagConstraints gbc) {
    gbc.gridy++;
    JLabel label = new JLabel(labelText);
    label.setFont(new Font("Arial", Font.BOLD, 14));
    label.setForeground(new Color(60, 60, 60));
    panel.add(label, gbc);

    gbc.gridy++;
    panel.add(field, gbc);
}



// Add Book to books.json
private void addBookToStorage(String title, String author, String genre, boolean available) {
    String filePath = new File("src/library/books/books.json").getAbsolutePath();
    File file = new File(filePath);
    JSONArray booksArray = new JSONArray();

    try {
        // Ensure books.json exists
        if (file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            reader.close();

            // Load existing books only if the file is not empty
            if (!jsonContent.toString().trim().isEmpty()) {
                booksArray = new JSONArray(jsonContent.toString());
            }
        } else {
            file.getParentFile().mkdirs(); // Create books directory if not exists
            file.createNewFile();
        }

        // Create new book object
        JSONObject newBook = new JSONObject();
        newBook.put("title", title);
        newBook.put("author", author);
        newBook.put("genre", genre);
        newBook.put("available", available);

        // Add to the array and save
        booksArray.put(newBook);

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(booksArray.toString(4)); // Pretty-print JSON
        writer.close();

        JOptionPane.showMessageDialog(this, "Book added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        displayBooksPanel(); // Refresh books table

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error saving book!", "Error", JOptionPane.ERROR_MESSAGE);
    }
}


private void deleteAdminPanel() {
    content.removeAll();
    content.revalidate();
    content.repaint();

    content.setLayout(new BorderLayout());

    // Main Panel Styling
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    panel.setBackground(new Color(245, 245, 245));
    panel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = GridBagConstraints.RELATIVE;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(15, 10, 5, 10);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // Header Title
    JLabel heading = new JLabel("Delete Admin", SwingConstants.CENTER);
    heading.setFont(new Font("Arial", Font.BOLD, 22));
    heading.setForeground(new Color(50, 50, 50));

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 2;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.insets = new Insets(0, 0, 10, 0);
    panel.add(heading, gbc);

    gbc.anchor = GridBagConstraints.WEST;

    // Username Field
    JTextField usernameField = createRoundedTextField();
    
    JButton deleteButton = createStyledButton("Delete Admin", new Color(200, 50, 50));

    deleteButton.addActionListener(e -> {
        String username = usernameField.getText().trim();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Username field cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (deleteAdminFromStorage(username)) {
            JOptionPane.showMessageDialog(null, "Admin deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            usernameField.setText(""); // Clear the field after successful deletion
        } else {
            JOptionPane.showMessageDialog(null, "Admin not found!", "Error", JOptionPane.ERROR_MESSAGE);
            usernameField.setText("");
        }
    });

    // Spacer Below the Title
    gbc.gridy++;
    JLabel spacer = new JLabel(" ");
    spacer.setPreferredSize(new Dimension(1, 20));
    panel.add(spacer, gbc);

    // Adding Form Fields
    addFormRow(panel, "Username:", usernameField, gbc);

    gbc.gridy++;
    gbc.anchor = GridBagConstraints.CENTER;
    panel.add(deleteButton, gbc);

    // Scroll Pane (In case of small screens)
    JScrollPane scrollPane = new JScrollPane(panel);
    scrollPane.setBorder(null);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

    content.add(scrollPane, BorderLayout.CENTER);
    content.revalidate();
    content.repaint();
}



private boolean deleteAdminFromStorage(String username) {
    try {
        String filePath = "src/library/users/admins.json";
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("Error: admins.json not found at " + file.getAbsolutePath());
            return false;
        }

        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        JSONArray adminsArray = new JSONArray(content);

        boolean found = false;
        for (int i = 0; i < adminsArray.length(); i++) {
            JSONObject adminObject = adminsArray.getJSONObject(i);
            if (adminObject.getString("username").equals(username)) {
                adminsArray.remove(i);
                found = true;
                break;
            }
        }

        if (found) {
            Files.write(Paths.get(filePath), adminsArray.toString(4).getBytes());
            return true;
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
    return false;
}

private void showChangePasswordDialog() {
    JTextField usernameField = new JTextField();
    JPasswordField oldPasswordField = new JPasswordField();
    JPasswordField newPasswordField = new JPasswordField();
    JPasswordField confirmPasswordField = new JPasswordField();

    Object[] fields = {
        "Username:", usernameField,
        "Old Password:", oldPasswordField,
        "New Password:", newPasswordField,
        "Confirm New Password:", confirmPasswordField
    };

    int option = JOptionPane.showConfirmDialog(
        null, fields, "Change Password", JOptionPane.OK_CANCEL_OPTION);

    if (option == JOptionPane.OK_OPTION) {
        String username = usernameField.getText().trim();
        String oldPassword = new String(oldPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (username.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Fields cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(null, "New passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        verifyAndChangePassword(username, oldPassword, newPassword);
    }
}

private void verifyAndChangePassword(String username, String oldPassword, String newPassword) {
    String filePath = new File("src/library/users/admins.json").getAbsolutePath();
    JSONArray admins = readAdminsFromFile(filePath);

    for (int i = 0; i < admins.length(); i++) {
        JSONObject admin = admins.getJSONObject(i);

        if (admin.getString("username").equals(username)) {
            String storedHashedPassword = admin.getString("password");

            // Verify old password
            if (!BCrypt.checkpw(oldPassword, storedHashedPassword)) {
                JOptionPane.showMessageDialog(null, "Old password is incorrect!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Hash new password and update JSON
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            admin.put("password", hashedPassword);
            writeAdminsToFile(filePath, admins);

            JOptionPane.showMessageDialog(null, "Password changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
    }

    JOptionPane.showMessageDialog(null, "Admin not found!", "Error", JOptionPane.ERROR_MESSAGE);
}


private JSONArray readAdminsFromFile(String filePath) {
    try {
        String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        return new JSONArray(content);
    } catch (IOException e) {
        e.printStackTrace();
        return new JSONArray();
    }
}

private void writeAdminsToFile(String filePath, JSONArray admins) {
    try (FileWriter file = new FileWriter(filePath)) {
        file.write(admins.toString(4)); 
        file.flush();
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    private void displayRequestedBooksPanel() {
        content.removeAll();
        content.setLayout(new BorderLayout());

        // Title
        JLabel titleLabel = new JLabel("Manage Book Requests", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        content.add(titleLabel, BorderLayout.NORTH);

        // Load requested books data
        Object[][] requestData = loadRequestedBooksData();

        String[] columnNames = { "Title", "Author", "Genre", "Requested By", "Actions" };
        
        DefaultTableModel model = new DefaultTableModel(requestData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only the Actions column is editable
            }
        };

        JTable requestTable = new JTable(model);
        requestTable.setRowHeight(40);
        requestTable.setFont(new Font("Arial", Font.PLAIN, 14));
        requestTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        requestTable.getTableHeader().setBackground(new Color(200, 200, 200));
        requestTable.getTableHeader().setForeground(Color.BLACK);
        requestTable.setGridColor(new Color(220, 220, 220));

        requestTable.getColumn("Actions").setCellRenderer(new RequestButtonRenderer());
        requestTable.getColumn("Actions").setCellEditor(new BookRequestButtonEditor(new JCheckBox(), requestTable));

        JScrollPane scrollPane = new JScrollPane(requestTable);
        content.add(scrollPane, BorderLayout.CENTER);

        content.revalidate();
        content.repaint();
    }

    class BookRequestButtonEditor extends DefaultCellEditor {
        private final JPanel panel = new JPanel();
        private final JButton acceptButton = new JButton("Accept");
        private final JButton rejectButton = new JButton("Reject");
        private final JTable requestTable;
    
        public BookRequestButtonEditor(JCheckBox checkBox, JTable requestTable) {
            super(checkBox);
            this.requestTable = requestTable;
            panel.setLayout(new FlowLayout(FlowLayout.CENTER));
    
            // Styling Accept Button
            acceptButton.setPreferredSize(new Dimension(90, 30));
            acceptButton.setFont(new Font("Arial", Font.PLAIN, 14));
            acceptButton.setBackground(new Color(50, 150, 50));
            acceptButton.setForeground(Color.WHITE);
            acceptButton.setFocusPainted(false);
    
            // Styling Reject Button
            rejectButton.setPreferredSize(new Dimension(90, 30));
            rejectButton.setFont(new Font("Arial", Font.PLAIN, 14));
            rejectButton.setBackground(new Color(200, 50, 50));
            rejectButton.setForeground(Color.WHITE);
            rejectButton.setFocusPainted(false);
    
            panel.add(acceptButton);
            panel.add(rejectButton);
    
            // Action Listeners for Buttons
            acceptButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    processRequest(requestTable.getSelectedRow(), true);
                }
            });
    
            rejectButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    processRequest(requestTable.getSelectedRow(), false);
                }
            });
        }
    
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            return panel;
        }
    
        @Override
        public Object getCellEditorValue() {
            return "";
        }
    
        private void processRequest(int row, boolean accepted) {
            if (row < 0) {
                JOptionPane.showMessageDialog(null, "No request selected!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        
            String adminRequestPath = new File("src/library/requests/admin_requests/requests.json").getAbsolutePath();
            
            try (BufferedReader reader = new BufferedReader(new FileReader(adminRequestPath))) {
                StringBuilder jsonString = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonString.append(line);
                }
        
                JSONArray requestsArray = new JSONArray(jsonString.toString());
        
                if (requestsArray.length() == 0) {
                    JOptionPane.showMessageDialog(null, "No requests found!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
        
                // Get the request object at the selected row
                JSONObject request = requestsArray.getJSONObject(row);
                String username = request.getString("username");
                String bookTitle = request.getString("title");
        
                if (accepted) {
                    request.put("status", "Accepted");
                    moveToBorrowedBooks(request, username);
                } else {
                    request.put("status", "Rejected");
                }
        
                // Remove from requests.json
                requestsArray.remove(row);
        
                // Save the updated requests.json
                try (FileWriter fileWriter = new FileWriter(adminRequestPath)) {
                    fileWriter.write(requestsArray.toString(4)); // Pretty print JSON
                }
        
                // Update user's request file
                updateUserRequestStatus(username, bookTitle, accepted ? "Accepted" : "Rejected");
        
                JOptionPane.showMessageDialog(null, accepted ? "Request Accepted" : "Request Rejected", "Request Status", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error processing request!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        
        // Helper method to read file content
        private String readFile(String filePath) throws IOException {
            File file = new File(filePath);
            if (!file.exists()) return "[]"; // Return empty JSON array if file doesn't exist
            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        }
        
        // Helper method to write file content
        private void writeFile(String filePath, String content) throws IOException {
            Files.write(Paths.get(filePath), content.getBytes(StandardCharsets.UTF_8));
        }
        
    }

    

    private void moveToBorrowedBooks(JSONObject request, String username) {
        // Paths to required files
        String borrowedBooksPath = new File("src/library/requests/admin_requests/borrowed_books.json").getAbsolutePath();
        String userBorrowedBooksPath = new File("src/library/requests/user_requests/" + username + "_borrowed_books.json").getAbsolutePath();
        String booksFilePath = new File("src/library/books/books.json").getAbsolutePath();
    
        try {
            // ✅ Read and update global borrowed books
            JSONArray borrowedBooksArray = readJsonArray(borrowedBooksPath);
            JSONArray userBorrowedBooksArray = readJsonArray(userBorrowedBooksPath);
            JSONArray booksArray = readJsonArray(booksFilePath);
    
            // ✅ Prepare book object with timestamp & return deadline
            JSONObject borrowedBook = prepareBorrowedBook(request, username);
    
            // ✅ Add the borrowed book to global & user-specific lists
            borrowedBooksArray.put(borrowedBook);
            userBorrowedBooksArray.put(borrowedBook);
            
            // ✅ Update book availability in books.json
            boolean bookFound = false;
            for (int i = 0; i < booksArray.length(); i++) {
                JSONObject book = booksArray.getJSONObject(i);
                if (book.getString("title").equals(request.getString("title")) &&
                    book.getString("author").equals(request.getString("author"))) {
                    
                    // ❌ Prevent double borrowing if already borrowed
                    if (book.has("availability") && book.getString("availability").equals("Borrowed")) {
                        System.out.println("⚠️ Error: Book is already borrowed!");
                        return;
                    }
                    
                    book.put("availability", "Borrowed"); // ✅ Update status
                    book.put("available", false); // ✅ Ensure 'available' field is updated
                    bookFound = true;
                    System.out.println("📌 Book status updated: " + book.toString());
                    break;
                }
            }
    
            if (!bookFound) {
                System.out.println("❌ Error: Book not found in books.json!");
                return; // Exit to prevent unnecessary writes
            }
    
            // ✅ Save changes to respective JSON files
            writeJsonToFile(borrowedBooksPath, borrowedBooksArray);
            writeJsonToFile(userBorrowedBooksPath, userBorrowedBooksArray);
            writeJsonToFile(booksFilePath, booksArray);
            System.out.println("✅ All files successfully updated!");
    
            // ✅ Refresh UI to reflect changes
            displayBooksPanel();
    
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Unexpected error in moveToBorrowedBooks: " + e.getMessage());
        }
    }
    

    private void writeJsonToFile(String filePath, JSONArray jsonArray) {
        try {
            Files.write(new File(filePath).toPath(), jsonArray.toString(4).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to write JSON to " + filePath);
        }
    }
    
    
    
    

    private JSONObject prepareBorrowedBook(JSONObject request, String username) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        
        // Set borrowed timestamp
        String borrowedAt = dateFormat.format(calendar.getTime());
    
        // Set return deadline (7 days from borrowed date)
        calendar.add(Calendar.DAY_OF_YEAR, 7);
        String returnBy = dateFormat.format(calendar.getTime());
    
        // Create new JSON object for borrowed book
        JSONObject borrowedBook = new JSONObject();
        borrowedBook.put("title", request.getString("title"));
        borrowedBook.put("author", request.getString("author"));
        borrowedBook.put("genre", request.getString("genre"));
        borrowedBook.put("availability", "Borrowed"); // Change availability
        borrowedBook.put("borrowed_at", borrowedAt);
        borrowedBook.put("return_by", returnBy);
        borrowedBook.put("borrower", username);
    
        return borrowedBook;
    }
    
    
    
    // Helper method to read a JSON array from a file
    private JSONArray readJsonArray(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) return new JSONArray(); // Return empty array if file doesn't exist
    
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            return new JSONArray(jsonString.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }
    
    // Helper method to write a JSON array to a file
    private void writeJsonArray(String filePath, JSONArray jsonArray) {
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(jsonArray.toString(4)); // Pretty print JSON
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateUserRequestStatus(String username, String bookTitle, String status) {
        String userRequestPath = new File("src/library/requests/user_requests/" + username + "_requests.json").getAbsolutePath();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(userRequestPath))) {
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
    
            JSONArray userRequestsArray = new JSONArray(jsonString.toString());
    
            for (int i = 0; i < userRequestsArray.length(); i++) {
                JSONObject userRequest = userRequestsArray.getJSONObject(i);
                if (userRequest.getString("title").equals(bookTitle)) {
                    userRequest.put("status", status);
                    break;
                }
            }
    
            try (FileWriter fileWriter = new FileWriter(userRequestPath)) {
                fileWriter.write(userRequestsArray.toString(4));
            }
    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class RequestButtonRenderer extends JPanel implements TableCellRenderer {
        private final JButton acceptButton = new JButton("Accept");
        private final JButton rejectButton = new JButton("Reject");
    
        public RequestButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER));
            
            // Styling Accept Button
            acceptButton.setPreferredSize(new Dimension(90, 30));
            acceptButton.setFont(new Font("Arial", Font.PLAIN, 14));
            acceptButton.setBackground(new Color(50, 150, 50));
            acceptButton.setForeground(Color.WHITE);
            acceptButton.setFocusPainted(false);
            
            // Styling Reject Button
            rejectButton.setPreferredSize(new Dimension(90, 30));
            rejectButton.setFont(new Font("Arial", Font.PLAIN, 14));
            rejectButton.setBackground(new Color(200, 50, 50));
            rejectButton.setForeground(Color.WHITE);
            rejectButton.setFocusPainted(false);
    
            add(acceptButton);
            add(rejectButton);
        }
    
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }
    

    private void displayBorrowedBooksPanel() {
        content.removeAll();
        content.setLayout(new BorderLayout());
    
        // Title
        JLabel titleLabel = new JLabel("Borrowed Books", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        content.add(titleLabel, BorderLayout.NORTH);
    
        // Load borrowed books data
        Object[][] borrowedData = loadBorrowedBooksData();
    
        String[] columnNames = { "Title", "Author", "Genre", "Borrower", "Borrowed At", "Return By", "Availability" };
        
        DefaultTableModel model = new DefaultTableModel(borrowedData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // No cells are editable
            }
        };
    
        JTable table = new JTable(model);
        table.setRowSorter(new TableRowSorter<>(model));
        JScrollPane scrollPane = new JScrollPane(table);
        content.add(scrollPane, BorderLayout.CENTER);
    
        content.revalidate();
        content.repaint();
    }
    
    private Object[][] loadBorrowedBooksData() {
        List<Object[]> dataList = new ArrayList<>();
        String filePath = "src/library/requests/admin_requests/borrowed_books.json";
        
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(content);
    
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                dataList.add(new Object[] {
                    obj.getString("title"),
                    obj.getString("author"),
                    obj.getString("genre"),
                    obj.getString("borrower"),
                    obj.getString("borrowed_at"),
                    obj.getString("return_by"),
                    obj.getString("availability")
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return dataList.toArray(new Object[0][]);
    }
    




    
    
    

    private Object[][] loadRequestedBooksData() {
        String adminRequestPath = new File("src/library/requests/admin_requests/requests.json").getAbsolutePath();
        List<Object[]> data = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(adminRequestPath))) {
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }

            // Parse JSON
            JSONArray requestsArray = new JSONArray(jsonString.toString());
            for (int i = 0; i < requestsArray.length(); i++) {
                JSONObject request = requestsArray.getJSONObject(i);
                data.add(new Object[]{
                    request.getString("title"),
                    request.getString("author"),
                    request.getString("genre"),
                    request.getString("username"),
                    "Actions"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data.toArray(new Object[0][]);
    }



private void logout() {
    int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
    
    if (confirm == JOptionPane.YES_OPTION) {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(content);
        if (topFrame != null) {
            topFrame.dispose(); // Close the Admin Dashboard
        }
        SwingUtilities.invokeLater(LoginWindow::new); // Open Login Window
    }
}

}
