import org.example.MySQLConnection;

import java.sql.*;

public class DirtyReads {
    public static void main(String[] args) {
        try (Connection connection = MySQLConnection.getConnection()) {
            connection.setAutoCommit(false); // Disable auto-commit

            // Execute transaction 1
            System.out.println("Executing Transaction 1...");
            int initialValue = getBookPrice(connection, "New Book");
            System.out.println("Initial Price: $" + initialValue);

            // Execute transaction 2
            System.out.println("Executing Transaction 2...");
            double updatedValue = initialValue * 2;
            System.out.println("Updated Price before rollback: $" + updatedValue);
            updateBookPrice(connection, "New Book", updatedValue);

            // Rollback transaction 2 to simulate a failure
            connection.rollback();
            System.out.println("Transaction 2 rolled back.");

            // Execute transaction 1 again after rollback
            System.out.println("Executing Transaction 1 after rollback...");
            int finalValue = getBookPrice(connection, "New Book");
            System.out.println("Final Price after rollback: $" + finalValue);

            // Commit transaction 1
            connection.commit();
            System.out.println("Transaction 1 committed.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static int getBookPrice(Connection connection, String bookName) throws SQLException {
        String sql = "SELECT price FROM book WHERE title = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, bookName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("price");
                } else {
                    throw new SQLException("Book not found");
                }
            }
        }
    }

    private static void updateBookPrice(Connection connection, String bookName, double newPrice) throws SQLException {
        String sql = "UPDATE book SET price = ? WHERE title = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, newPrice);
            statement.setString(2, bookName);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Book not found");
            }
            System.out.println("Price updated successfully.");
        }
    }
}
