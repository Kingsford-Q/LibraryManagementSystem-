package library;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import org.mindrot.jbcrypt.BCrypt;

import library.db.BookDAO;
import library.db.UserDAO;
import library.db.RequestDAO;
import library.db.BorrowedBooksDAO;

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
import org.bson.Document;
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
        gbc.insets = new Insets(0, 10, 28, 10);
        gbc.gridx = 0;
        gbc.weightx = 1;

        String[] menuItems = {
            "View Books", "Add Book",
            "Accept Borrow Requests","Add User", "Add Admin", "Delete Admin",
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
            UserDAO.addAdmin(username, password);

            usernameField.setText("");
            passwordField.setText("");

            JOptionPane.showMessageDialog(null, "Admin added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

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
            UserDAO.addUser(username, password);

            usernameField.setText("");
            passwordField.setText("");

            JOptionPane.showMessageDialog(null, "User added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

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

    // ✅ Load Books from MongoDB
    String[] columnNames = { "Title", "Author", "Genre", "Availability", "Actions" };
    List<Document> books = BookDAO.getAllBooks(); // Fetch from MongoDB

    Object[][] bookData = new Object[books.size()][5];

    for (int i = 0; i < books.size(); i++) {
        Document book = books.get(i);
        bookData[i][0] = book.getString("title");
        bookData[i][1] = book.getString("author");
        bookData[i][2] = book.getString("genre");
        bookData[i][3] = book.getBoolean("available") ? "Available" : "Not Available";
        bookData[i][4] = "Edit | Delete"; // Placeholder for buttons
    }

    DefaultTableModel model = new DefaultTableModel(bookData, columnNames) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 4; // Only the Actions column is editable
        }
    };

    JTable bookTable = new JTable(model);
    bookTable.setRowHeight(40);
    bookTable.setFont(new Font("Arial", Font.PLAIN, 14));
    bookTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
    bookTable.getTableHeader().setBackground(new Color(200, 200, 200));
    bookTable.getTableHeader().setForeground(Color.BLACK);
    bookTable.setGridColor(new Color(220, 220, 220));

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
    
            editButton.setPreferredSize(new Dimension(100, 40));
            deleteButton.setPreferredSize(new Dimension(100, 40));
            editButton.setFont(new Font("Arial", Font.PLAIN, 14));
            deleteButton.setFont(new Font("Arial", Font.PLAIN, 14));
            panel.add(editButton);
            panel.add(deleteButton);
    
            editButton.addActionListener(e -> editBook(bookTable, bookTable.getSelectedRow()));
            deleteButton.addActionListener(e -> deleteBook(bookTable, bookTable.getSelectedRow()));

        }
    
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            return panel;
        }
    
        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
    
    // Method to edit a book
    private void editBook(JTable bookTable, int row) {
        if (row < 0) return;
    
        String oldTitle = (String) bookTable.getValueAt(row, 0);
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
    
        JTextField titleField = new JTextField(20);
        titleField.setText(oldTitle);
        JTextField authorField = new JTextField(20);
        authorField.setText(author);
        JTextField genreField = new JTextField(20);
        genreField.setText(genre);
    
        JCheckBox availableCheckBox = new JCheckBox("Available");
        availableCheckBox.setSelected(available);
    
        JButton saveButton = new JButton("Save Changes");
    
        saveButton.addActionListener(e -> {
            String newTitle = titleField.getText().trim();
            String newAuthor = authorField.getText().trim();
            String newGenre = genreField.getText().trim();
            boolean newAvailable = availableCheckBox.isSelected();
    
            if (newTitle.isEmpty() || newAuthor.isEmpty() || newGenre.isEmpty()) {
                JOptionPane.showMessageDialog(null, "All fields must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            // ✅ Update MongoDB instead of JSON
            BookDAO.updateBook(oldTitle, newTitle, newAuthor, newGenre, newAvailable);
            displayBooksPanel(); // Refresh table
            editFrame.dispose();
        });
    
        gbc.gridy++;
        panel.add(new JLabel("Title:"), gbc);
        panel.add(titleField, gbc);
        gbc.gridy++;
        panel.add(new JLabel("Author:"), gbc);
        panel.add(authorField, gbc);
        gbc.gridy++;
        panel.add(new JLabel("Genre:"), gbc);
        panel.add(genreField, gbc);
        gbc.gridy++;
        panel.add(availableCheckBox, gbc);
        gbc.gridy++;
        panel.add(saveButton, gbc);
    
        editFrame.add(panel, BorderLayout.CENTER);
        editFrame.setVisible(true);
    }
    // Method to delete a book
    private void deleteBook(JTable bookTable, int row) {
        if (row < 0) return;
    
        String title = (String) bookTable.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete " + title + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
    
        if (confirm == JOptionPane.YES_OPTION) {
            // ✅ Delete from MongoDB
            BookDAO.deleteBook(title);
            displayBooksPanel(); 
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
        
            // ✅ Save to MongoDB
            addBookToDatabase(title, author, genre, available);
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

        // ✅ Add this at the end of addBookPanel()
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
private void addBookToDatabase(String title, String author, String genre, boolean available) {
    try {
        // ✅ Insert into MongoDB instead of JSON
        BookDAO.addBook(title, author, genre, available);

        JOptionPane.showMessageDialog(this, "Book added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        displayBooksPanel(); // Refresh table
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
    
        if (UserDAO.deleteAdmin(username)) { // ✅ Use MongoDB method
            JOptionPane.showMessageDialog(null, "Admin deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            usernameField.setText("");
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
    boolean success = UserDAO.changePassword(username, oldPassword, newPassword);

    if (success) {
        JOptionPane.showMessageDialog(null, "✅ Password changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    } else {
        JOptionPane.showMessageDialog(null, "❌ Failed to change password. Check username and old password!", "Error", JOptionPane.ERROR_MESSAGE);
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
        
            // ✅ Fetch the request from MongoDB instead of JSON
            Document request = RequestDAO.getRequestByRow(row);
            
            if (request == null) {
                JOptionPane.showMessageDialog(null, "No requests found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        
            String username = request.getString("username");
            String bookTitle = request.getString("title");
        
            if (accepted) {
                request.append("status", "Accepted");
        
                // ✅ Move to Borrowed Books in MongoDB
                BorrowedBooksDAO.moveToBorrowedBooks(
                    request.getString("title"),
                    request.getString("author"),
                    request.getString("genre"),
                    username
                );
            } else {
                request.append("status", "Rejected");
            }
        
            // ✅ Remove the request from MongoDB
            RequestDAO.deleteRequest(request);
        
            // ✅ Update the user's request status in MongoDB
            RequestDAO.updateUserRequestStatus(username, bookTitle, accepted ? "Accepted" : "Rejected");
        
            JOptionPane.showMessageDialog(null, accepted ? "Request Accepted" : "Request Rejected", "Request Status", JOptionPane.INFORMATION_MESSAGE);

            displayRequestedBooksPanel();
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
    List<Object[]> dataList = BorrowedBooksDAO.getBorrowedBooks();
    return dataList.toArray(new Object[0][]);
    }       

    private Object[][] loadRequestedBooksData() {
    List<Object[]> dataList = RequestDAO.getRequestedBooks();
    return dataList.toArray(new Object[0][]);
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
