import org.example.MySQLConnection;

import java.sql.*;

public class IsolationAndLockingExample {
    public static void main(String[] args) {
        try (Connection connection = MySQLConnection.getConnection()) {
            connection.setAutoCommit(false);

            // Set isolation level to Serializable
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            // Start transaction
            Savepoint savepoint = connection.setSavepoint();

            try {
                // Lock a row with exclusive lock
                lockBook(connection, 1);

                // Execute some operations
                // Update stock of book with id 1
                updateBookStock(connection, 1, 10);

                // Commit transaction
                connection.commit();
            } catch (SQLException e) {
                // Rollback to savepoint in case of an exception
                connection.rollback(savepoint);
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to lock a book with exclusive lock
    private static void lockBook(Connection connection, int bookId) throws SQLException {
        String lockSql = "SELECT * FROM book WHERE id = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(lockSql)) {
            statement.setInt(1, bookId);
            statement.executeQuery(); // Execute query to acquire exclusive lock
        }
    }

    // Method to update stock of a book
    private static void updateBookStock(Connection connection, int bookId, int newStock) throws SQLException {
        String updateSql = "UPDATE book SET stock = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
            statement.setInt(1, newStock);
            statement.setInt(2, bookId);
            statement.executeUpdate();
        }
    }
}
