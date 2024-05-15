import org.example.MySQLConnection;

import java.sql.*;

public class NonRepeatableReads {
    public static void main(String[] args) {
        try (Connection connection = MySQLConnection.getConnection()) {
            connection.setAutoCommit(false); // Disable auto-commit

            // Execute transaction 1
            System.out.println("Executing Transaction 1...");
            int initialPrice1 = getBookPrice(connection, "Bell Jar");
            System.out.println("Initial Price (Transaction 1): $" + initialPrice1);

            // Sleep to simulate another transaction modifying data
            Thread.sleep(3000);

            // Execute transaction 2
            System.out.println("Executing Transaction 2...");
            double updatedPrice = initialPrice1 * 2;
            updateBookPrice(connection, "Bell Jar", updatedPrice);
            connection.commit();
            System.out.println("Transaction 2 committed.");

            // Execute transaction 1 again
            System.out.println("Executing Transaction 1 again...");
            int initialPrice2 = getBookPrice(connection, "Bell Jar");
            System.out.println("Initial Price (Transaction 1 again): $" + initialPrice2);

            // Commit transaction 1
            connection.commit();
            System.out.println("Transaction 1 committed.");

        } catch (SQLException | InterruptedException e) {
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
