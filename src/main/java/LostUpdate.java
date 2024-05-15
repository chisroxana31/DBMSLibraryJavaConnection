import org.example.MySQLConnection;

import java.sql.*;

public class LostUpdate {
    public static void main(String[] args) {
        try (Connection connection = MySQLConnection.getConnection()) {

            int initialValue = getBookStock(connection, "New Book");
            System.out.println("Initial Stock: " + initialValue);

            Thread thread1 = new Thread(() -> updateBookStock(connection, "New Book", initialValue + 1));
            Thread thread2 = new Thread(() -> updateBookStock(connection, "New Book", initialValue - 2));

            thread1.start();
            thread2.start();

            thread1.join();
            thread2.join();

            int finalValue = getBookStock(connection, "New Book");
            System.out.println("Final Stock: " + finalValue);
        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int getBookStock(Connection connection, String bookName) throws SQLException {
        String sql = "SELECT stock FROM book WHERE title = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, bookName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("Stock");
                } else {
                    throw new SQLException("Book not found");
                }
            }
        }
    }

    private static void updateBookStock(Connection connection, String bookName, int newStock) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String sql = "UPDATE book SET stock = ? WHERE title = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, newStock);
            statement.setString(2, bookName);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Book not found");
            }
            System.out.println("Stock updated successfully by Thread-" + Thread.currentThread().getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
