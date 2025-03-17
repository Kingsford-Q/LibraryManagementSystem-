package library.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import static com.mongodb.client.model.Filters.eq;


public class BorrowedBooksDAO {
    private static final MongoDatabase database = MongoDBConnection.getDatabase();
    private static final MongoCollection<Document> borrowedBooksCollection = database.getCollection("borrowed_books");
    private static final MongoCollection<Document> booksCollection = database.getCollection("books");


    public static void moveToBorrowedBooks(String title, String author, String genre, String username) {
        try {
            // Set borrowed timestamp and return date (7 days later)
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            String borrowedAt = dateFormat.format(calendar.getTime());
    
            calendar.add(Calendar.DAY_OF_YEAR, 7);
            String returnBy = dateFormat.format(calendar.getTime());
    
            // ✅ Create the borrowed book document
            Document borrowedBook = new Document("title", title)
                    .append("author", author)
                    .append("genre", genre)
                    .append("borrower", username)
                    .append("borrowed_at", borrowedAt)
                    .append("return_by", returnBy)
                    .append("availability", "Borrowed");
    
            // ✅ Insert into the global borrowed_books collection
            borrowedBooksCollection.insertOne(borrowedBook);
    
            // ✅ Ensure the user's personal borrowed_books collection exists
            MongoCollection<Document> userBorrowedBooks = database.getCollection(username + "_borrowed_books");
    
            // ✅ Create a separate document for the user's borrowed books
            Document userBorrowedBook = new Document(borrowedBook); // Copy fields
            userBorrowedBook.remove("_id"); // Remove the auto-generated _id to prevent duplication
            userBorrowedBooks.insertOne(userBorrowedBook); // ✅ Save to user-specific collection
    
            // ✅ Update book availability in the books collection
            MongoCollection<Document> booksCollection = database.getCollection("books");
            booksCollection.updateOne(eq("title", title),
                    new Document("$set", new Document("available", false).append("availability", "Borrowed")));
    
            System.out.println("✅ Book moved to Borrowed Books successfully!");
            System.out.println("✅ User-specific borrowed_books collection updated!");
            System.out.println("✅ Book availability updated in books collection!");
    
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Error in moving book to Borrowed Books: " + e.getMessage());
        }
    }
    
    
    
    
    public static List<Object[]> getBorrowedBooks() {
            List<Object[]> borrowedBooks = new ArrayList<>();
    
            try (MongoCursor<Document> cursor = borrowedBooksCollection.find().iterator()) {
                while (cursor.hasNext()) {
                    Document doc = cursor.next();
    
                    borrowedBooks.add(new Object[]{
                            doc.getString("title"),
                            doc.getString("author"),
                            doc.getString("genre"),
                            doc.getString("borrower"),
                            doc.getString("borrowed_at"),
                            doc.getString("return_by"),
                            doc.getString("availability")
                    });
                }
            }
    
            return borrowedBooks;
        }

        public static List<Object[]> getUserBorrowedBooks(String username) {
            List<Object[]> borrowedBooks = new ArrayList<>();
        
            // ✅ Connect to the user's personal borrowed_books collection
            MongoCollection<Document> userBorrowedCollection = database.getCollection(username + "_borrowed_books");
        
            try (MongoCursor<Document> cursor = userBorrowedCollection.find().iterator()) {
                while (cursor.hasNext()) {
                    Document doc = cursor.next();
        
                    borrowedBooks.add(new Object[]{
                        doc.getString("title"),
                        doc.getString("author"),
                        doc.getString("genre"),
                        doc.getString("borrowed_at"),
                        doc.getString("return_by"),
                        "Return" // Placeholder for action button
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("❌ Error fetching user borrowed books: " + e.getMessage());
            }
        
            return borrowedBooks;
        }

        public static boolean processBookReturn(String title, String author, String username) {
            if (database == null) {
                System.out.println("❌ Error: MongoDB database connection is null!");
                return false;
            }
        
            // ✅ Remove from borrowed_books collection
            DeleteResult borrowedDelete = borrowedBooksCollection.deleteOne(and(eq("title", title), eq("author", author), eq("borrower", username)));
            System.out.println("🗑 Deleted from borrowed_books: " + borrowedDelete.getDeletedCount());
        
            // ✅ Remove from user's borrowed collection
            MongoCollection<Document> userBorrowedCollection = database.getCollection(username + "_borrowed_books");
            DeleteResult userDelete = userBorrowedCollection.deleteOne(and(eq("title", title), eq("author", author)));
            System.out.println("🗑 Deleted from " + username + "_borrowed_books: " + userDelete.getDeletedCount());
        
            // ✅ Update books collection to mark as available
            UpdateResult bookUpdate = booksCollection.updateOne(and(eq("title", title), eq("author", author)),
                    combine(set("availability", "Available"), set("available", true)));
            System.out.println("📖 Updated books collection: " + bookUpdate.getModifiedCount());
        
            return borrowedDelete.getDeletedCount() > 0 && userDelete.getDeletedCount() > 0;
        }
        
        
}
