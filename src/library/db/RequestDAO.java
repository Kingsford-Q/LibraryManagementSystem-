package library.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import static com.mongodb.client.model.Filters.*;


import java.util.ArrayList;
import java.util.List;

public class RequestDAO {
    private static final MongoDatabase database = MongoDBConnection.getDatabase();
    private static final MongoCollection<Document> requestsCollection = database.getCollection("requests");

    public static Document getRequestByRow(int row) {
        return requestsCollection.find().skip(row).first(); // Fetch request at row index
    }

    public static void deleteRequest(Document request) {
        requestsCollection.deleteOne(eq("_id", request.getObjectId("_id"))); // Remove from MongoDB
    }

    public static void updateUserRequestStatus(String username, String bookTitle, String status) {
        // ✅ Update the user's request history in MongoDB
        MongoCollection<Document> userRequests = database.getCollection(username + "_requests");
        userRequests.updateOne(eq("title", bookTitle), new Document("$set", new Document("status", status)));
    }

    public static List<Object[]> getRequestedBooks() {
        List<Object[]> requestedBooks = new ArrayList<>();

        try (MongoCursor<Document> cursor = requestsCollection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();

                requestedBooks.add(new Object[]{
                        doc.getString("title"),
                        doc.getString("author"),
                        doc.getString("genre"),
                        doc.getString("username"),
                        "Actions" // Placeholder for action buttons
                });
            }
        }

        return requestedBooks;
    }

    public static void addBookRequest(String username, String title, String author, String genre, String availability) {
        Document request = new Document("username", username)
                .append("title", title)
                .append("author", author)
                .append("genre", genre)
                .append("availability", availability)
                .append("status", "pending");
    
        // ✅ Insert into the global requests collection
        requestsCollection.insertOne(request);
    
        // ✅ Ensure the user's personal request collection exists
        MongoCollection<Document> userRequestsCollection = database.getCollection(username + "_requests");
    
        // ✅ Store the request in the user's request collection
        userRequestsCollection.insertOne(request);
    
        System.out.println("✅ Book request saved successfully for " + username);
    }
    

    // ✅ Check if the request already exists
    public static boolean isDuplicateRequest(String username, String title) {
    Document existingRequest = requestsCollection.find(and(eq("username", username), eq("title", title))).first();
    return existingRequest != null;
    }

    public static List<Object[]> getRequestedBooks(String username) {
        List<Object[]> requestedBooks = new ArrayList<>();
        MongoCollection<Document> userRequestsCollection = database.getCollection(username + "_requests");
    
        try (MongoCursor<Document> cursor = userRequestsCollection.find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
    
                requestedBooks.add(new Object[]{
                        doc.getString("title"),
                        doc.getString("author"),
                        doc.getString("genre"),
                        doc.getString("availability"),
                        doc.getString("status")
                });
            }
        }
    
        return requestedBooks;
    }
    
    public static List<Document> getUserRequestedBooks(String username) {
        MongoCollection<Document> userRequestsCollection = database.getCollection(username + "_requests");
        return userRequestsCollection.find().into(new ArrayList<>());
    }
    


}
