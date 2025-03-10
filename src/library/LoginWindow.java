package library;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.mindrot.jbcrypt.BCrypt;

public class LoginWindow extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;

    public LoginWindow() {

        // Window Properties - Fullscreen
        setTitle("Library Management System - Login");
        // Set Window Icon
        ImageIcon windowIcon = new ImageIcon(getClass().getResource("/library/images/logo.png"));
        setIconImage(windowIcon.getImage());

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Background Panel with Gradient
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                int width = getWidth();
                int height = getHeight();
                Color color1 = new Color(25, 25, 25);
                Color color2 = new Color(45, 45, 45);
                GradientPaint gp = new GradientPaint(0, 0, color1, width, height, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, width, height);
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());

        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setPreferredSize(new Dimension(420, 450));
        cardPanel.setBackground(new Color(255, 255, 255, 40)); // Slightly Transparent
        cardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        // Library Logo (Ensure the image exists)
        JLabel logoLabel = new JLabel();
        ImageIcon originalIcon = new ImageIcon(getClass().getResource("/library/images/logo.png"));

        if (originalIcon.getIconWidth() > 0) {
            // Resize the image
            Image resizedImage = originalIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(resizedImage));
            gbc.gridy = 0;
            cardPanel.add(logoLabel, gbc);
        }

        // Title Label
        JLabel titleLabel = new JLabel("Library System Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        cardPanel.add(titleLabel, gbc);

        // Username Field
        gbc.gridwidth = 1;
        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.WHITE);
        cardPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(15);
        styleInputField(usernameField);
        cardPanel.add(usernameField, gbc);

        // Password Field
        gbc.gridy = 3;
        gbc.gridx = 0;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.WHITE);
        cardPanel.add(passLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        styleInputField(passwordField);
        cardPanel.add(passwordField, gbc);

        // Login Button
        JButton loginButton = new JButton("Login");
        styleButton(loginButton);
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        cardPanel.add(loginButton, gbc);

        // Message Label
        messageLabel = new JLabel("", JLabel.CENTER);
        messageLabel.setForeground(Color.RED);
        gbc.gridy = 5;
        cardPanel.add(messageLabel, gbc);

        // Add panels to frame
        backgroundPanel.add(cardPanel);
        add(backgroundPanel, BorderLayout.CENTER);

        // Handle Login
        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            User loggedInUser = authenticateUser(username, password);

            if (loggedInUser != null) {
                // Store the username for later use
                SessionManager.setLoggedInUser(loggedInUser.getUsername());
            
                dispose(); // Close login window
            
                if (loggedInUser.isAdmin()) {
                    new AdminDashboard(); // Open Admin Dashboard
                } else {
                    new UsersDashboard(); // Open User Dashboard
                }
            } else {
                messageLabel.setText("❌ Invalid username or password!");
                messageLabel.setForeground(Color.RED);
                
                // Ensure no new login window is created
                revalidate();
                repaint();
            }
            
        });

        


        setVisible(true);
    }

    // Apply styles to input fields
    private void styleInputField(JTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150), 1),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
        field.setBackground(new Color(230, 230, 230));
    }

    // Apply styles to button with hover effect
    private void styleButton(JButton button) {
    button.setBackground(new Color(0, 153, 255)); // Light Blue
    button.setForeground(Color.WHITE);
    button.setFont(new Font("Arial", Font.BOLD, 14));
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
    button.setCursor(new Cursor(Cursor.HAND_CURSOR));

    // Add hover effect
    button.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            button.setBackground(new Color(0, 123, 205)); // Darker Blue on hover
        }

        @Override
        public void mouseExited(java.awt.event.MouseEvent evt) {
            button.setBackground(new Color(0, 153, 255)); // Original Light Blue
        }
    });
}


    private static User authenticateUser(String username, String password) {
        String adminFilePath = "src/library/users/admins.json";
        String userFilePath = "src/library/users/users.json"; // Path for regular users

        // Check in admins.json
        User user = checkUserInFile(adminFilePath, username, password, true);
        if (user != null) return user;

        // Check in users.json
        return checkUserInFile(userFilePath, username, password, false);
    }

    // Helper method to check in a given JSON file
    private static User checkUserInFile(String filePath, String username, String password, boolean isAdmin) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("Error: " + filePath + " not found at " + file.getAbsolutePath());
                return null;
            }

            String content = new String(Files.readAllBytes(Paths.get(filePath))); // Read JSON file
            JSONArray usersArray = new JSONArray(content); // Convert to JSON array

            for (int i = 0; i < usersArray.length(); i++) {
                JSONObject userObject = usersArray.getJSONObject(i);
                String storedUsername = userObject.getString("username");
                String storedHashedPassword = userObject.getString("password"); // Hashed with BCrypt

                boolean userIsAdmin = userObject.optBoolean("isAdmin", isAdmin);

                if (storedUsername.equals(username) && verifyPassword(password, storedHashedPassword)) {
                    return new User(username, userIsAdmin);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ✅ Use BCrypt for secure password verification
    private static boolean verifyPassword(String inputPassword, String storedHashedPassword) {
        return BCrypt.checkpw(inputPassword, storedHashedPassword);
    }

    
}

