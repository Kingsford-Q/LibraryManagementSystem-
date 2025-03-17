package library;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

public class MongoDBConnection {
    private static final String DATABASE_NAME = "LibraryDB";
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    public static void connect() {
        try {
            // Connect to MongoDB running locally on port 27017
            mongoClient = MongoClients.create("mongodb://localhost:27017");
            database = mongoClient.getDatabase(DATABASE_NAME);
            System.out.println("✅ Connected to MongoDB successfully!");
        } catch (Exception e) {
            System.out.println("❌ Failed to connect to MongoDB.");
            e.printStackTrace();
        }
    }

    public static MongoDatabase getDatabase() {
        return database;
    }

    public static void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("🔴 MongoDB connection closed.");
        }
    }
}
