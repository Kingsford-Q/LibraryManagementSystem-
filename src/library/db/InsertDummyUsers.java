package library.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;

public class InsertDummyUsers {
    public static void main(String[] args) {
        MongoDBConnection.connect();
        MongoDatabase database = MongoDBConnection.getDatabase();
        MongoCollection<Document> usersCollection = database.getCollection("users");

        // Insert Admin User
        usersCollection.insertOne(new Document("username", "admin")
                .append("password", BCrypt.hashpw("admin123", BCrypt.gensalt()))
                .append("isAdmin", true));

        // Insert Regular User
        usersCollection.insertOne(new Document("username", "user1")
                .append("password", BCrypt.hashpw("user123", BCrypt.gensalt()))
                .append("isAdmin", false));

        System.out.println("✅ Dummy Users Inserted Successfully!");
    }
}
