import org.example.MySQLConnection;

import java.sql.*;

public class PhantomReads {
    public static void main(String[] args) {
        try (Connection connection = MySQLConnection.getConnection()) {
            connection.setAutoCommit(false); // Disable auto-commit

            // Execute transaction 1
            System.out.println("Executing Transaction 1...");
            int initialCount1 = getBooksCountWithStock(connection, 5);
            System.out.println("Initial Count of Books with 5 Stock (Transaction 1): " + initialCount1);

            // Sleep to simulate another transaction modifying data
            Thread.sleep(3000);

            // Execute transaction 2
            System.out.println("Executing Transaction 2...");
            addNewBook(connection, "The white mask", 5);
            connection.commit();
            System.out.println("Transaction 2 committed.");

            // Execute transaction 1 again
            System.out.println("Executing Transaction 1 again...");
            int initialCount2 = getBooksCountWithStock(connection, 5);
            System.out.println("Initial Count of Books with 5 Stock (Transaction 1 again): " + initialCount2);

            // Commit transaction 1
            connection.commit();
            System.out.println("Transaction 1 committed.");

        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int getBooksCountWithStock(Connection connection, int stockCount) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM book WHERE stock = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, stockCount);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("count");
                } else {
                    throw new SQLException("Failed to get count of books with stock " + stockCount);
                }
            }
        }
    }

    private static void addNewBook(Connection connection, String bookName, int stock) throws SQLException {
        String sql = "INSERT INTO book (title, stock) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, bookName);
            statement.setInt(2, stock);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to add new book");
            }
            System.out.println("New book added successfully.");
        }
    }
}
