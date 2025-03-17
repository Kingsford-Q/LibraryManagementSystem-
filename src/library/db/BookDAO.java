package library.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.*;

import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    private static final MongoDatabase database = MongoDBConnection.getDatabase();
    private static final MongoCollection<Document> booksCollection = database.getCollection("books");
    private static final MongoCollection<Document> borrowedBooksCollection = database.getCollection("borrowed_books");


    // ✅ Fetch all books from MongoDB
    public static List<Document> getAllBooks() {
        List<Document> booksList = new ArrayList<>();

        try (MongoCursor<Document> cursor = booksCollection.find().iterator()) {
            while (cursor.hasNext()) {
                Document book = cursor.next();

                // ✅ Check if the book exists in borrowed_books
                Document borrowedBook = borrowedBooksCollection.find(eq("title", book.getString("title"))).first();
                
                // ✅ Set availability field correctly
                String availability = (borrowedBook != null) ? "Borrowed" : "Available";

                book.append("availability", availability); // ✅ Add computed field

                booksList.add(book);
            }
        }

        return booksList;
    }
    
    

    // ✅ Add a new book
    public static void addBook(String title, String author, String genre, boolean available) {
        Document book = new Document("title", title)
                .append("author", author)
                .append("genre", genre)
                .append("available", available);

        booksCollection.insertOne(book);
        System.out.println("✅ Book added successfully!");
    }

    // ✅ Delete a book by title
    public static void deleteBook(String title) {
        booksCollection.deleteOne(eq("title", title));
        System.out.println("🗑 Book deleted successfully!");
    }

    // ✅ Update book details
    public static void updateBook(String oldTitle, String newTitle, String newAuthor, String newGenre, boolean newAvailable) {
        booksCollection.updateOne(eq("title", oldTitle), new Document("$set", new Document("title", newTitle)
                .append("author", newAuthor)
                .append("genre", newGenre)
                .append("available", newAvailable)));
        System.out.println("🔄 Book updated successfully!");
    }

    
}
