package library.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBConnection {
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    // Connect to MongoDB only once
    public static void connect() {
        if (mongoClient == null) {
            try {
                mongoClient = MongoClients.create("mongodb://localhost:27017"); // Change if needed
                database = mongoClient.getDatabase("LibraryDB");
                System.out.println("MongoDB Connected Successfully!");
            } catch (Exception e) {
                System.out.println("Failed to connect to MongoDB: " + e.getMessage());
            }
        }
    }

    public static MongoDatabase getDatabase() {
        return database;
    }

    public static void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("🔌 MongoDB Connection Closed.");
        }
    }
}
