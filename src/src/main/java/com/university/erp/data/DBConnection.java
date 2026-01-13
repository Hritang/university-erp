package com.university.erp.data;

import com.university.erp.util.ConfigLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {


    public static Connection getConnection() {
        try {
            ConfigLoader config = new ConfigLoader();
            String url = config.getDbUrl();
            String username = config.getDbUsername();
            String password = config.getDbPassword();


            Connection connection = DriverManager.getConnection(url, username, password);
            System.out.println("Database connection successful!");
            return connection;

        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            e.printStackTrace();
            return null;
        }
    }
}
