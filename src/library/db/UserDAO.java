package library.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.mindrot.jbcrypt.BCrypt;
import com.mongodb.client.MongoCursor;

import java.util.ArrayList;
import java.util.List;


import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;


public class UserDAO {
    private static final MongoDatabase database = MongoDBConnection.getDatabase();
    private static final MongoCollection<Document> usersCollection = database.getCollection("users");

    // ✅ Find user by username
    public static Document getUserByUsername(String username) {
        return usersCollection.find(eq("username", username)).first();
    }

    // ✅ Verify user password using BCrypt
    public static boolean verifyPassword(String inputPassword, String storedHash) {
        return BCrypt.checkpw(inputPassword, storedHash);
    }

    // ✅ Add a new user to MongoDB
    public static void addUser(String username, String password) {
        // Hash password before storing
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        Document user = new Document("username", username)
                .append("password", hashedPassword)
                .append("isAdmin", false); // Default to a regular user

        usersCollection.insertOne(user);
        System.out.println("✅ User added successfully to MongoDB!");
    }

    public static void addAdmin(String username, String password) {
        // Hash the password before storing
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
    
        Document admin = new Document("username", username)
                .append("password", hashedPassword)
                .append("isAdmin", true); // ✅ Set isAdmin to true
    
        usersCollection.insertOne(admin);
        System.out.println("✅ Admin added successfully to MongoDB!");
    }

    public static boolean deleteAdmin(String username) {
        Document admin = usersCollection.find(eq("username", username)).first();
    
        if (admin != null && admin.getBoolean("isAdmin", false)) {
            usersCollection.deleteOne(eq("username", username));
            System.out.println("✅ Admin deleted successfully!");
            return true;
        } else {
            System.out.println("❌ Admin not found!");
            return false;
        }
    }

    public static boolean changePassword(String username, String oldPassword, String newPassword) {
        Document user = usersCollection.find(eq("username", username)).first();
    
        if (user == null) {
            System.out.println("❌ User not found!");
            return false;
        }
    
        String storedHash = user.getString("password");
        if (!BCrypt.checkpw(oldPassword, storedHash)) {
            System.out.println("❌ Old password is incorrect!");
            return false;
        }
    
        // ✅ Hash new password before updating
        String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        usersCollection.updateOne(eq("username", username), set("password", hashedNewPassword));
    
        System.out.println("✅ Password changed successfully!");
        return true;
    }
    
}
