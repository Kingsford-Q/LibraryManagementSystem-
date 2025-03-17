package library;

import library.db.MongoDBConnection;

public class Main {
    public static void main(String[] args) {
        // Initialize MongoDB connection
        MongoDBConnection.connect();

        // Open Login Window
        new LoginWindow();
    }
}
