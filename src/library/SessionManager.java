package library;
public class SessionManager {
    private static String loggedInUsername;

    public static void setLoggedInUser(String username) {
        loggedInUsername = username;
    }

    public static String getLoggedInUser() {
        return loggedInUsername;
    }

    public static void clearSession() {
        loggedInUsername = null;
    }
}
