import org.example.MySQLConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ConcurringTransactions implements Runnable {
    private final Connection connection;
    private final String tableName;

    public ConcurringTransactions(Connection connection, String tableName) {
        this.connection = connection;
        this.tableName = tableName;
    }

    public static void main(String[] args) {
        try (Connection connection = MySQLConnection.getConnection()) {
            Thread bookThread = new Thread(new ConcurringTransactions(connection, "book"));
            Thread authorThread = new Thread(new ConcurringTransactions(connection, "author"));

            bookThread.start();
            authorThread.start();

            bookThread.join();
            authorThread.join();
        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            switch (tableName) {
                case "book":
                    insertBookRecord();
                    break;
                case "author":
                    insertAuthorRecord();
                    break;
                default:
                    System.out.println("Invalid table name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertBookRecord() throws SQLException {
        String sql = "INSERT INTO book (id,title, stock) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, 100);
            statement.setString(2, "New Book");
            statement.setInt(3, 10); // Assuming initial stock is 10
            statement.executeUpdate();
            System.out.println("Book record inserted successfully.");
        }
    }

    private void insertAuthorRecord() throws SQLException {
        String sql = "INSERT INTO author (id,name) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, 5);
            statement.setString(2, "New Author");
            statement.executeUpdate();
            System.out.println("Author record inserted successfully.");
        }
    }

}
