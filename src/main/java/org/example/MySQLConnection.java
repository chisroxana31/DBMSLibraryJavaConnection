package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
public class MySQLConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/labormap";
    private static final String USER = "root";
    private static final String PASSWORD = "252525";
//
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

}
