package com.dimzi.cryptocurrencyanalyzer;

import java.sql.*;

public class DatabaseManager {

    private static final String DRIVER = "org.sqlite.JDBC";

    /**
     * Returns connection to database with given database url
     * @param DB_URL destination database URL
     * @return connection to database
     */
    public static Connection startConnection(String DB_URL){
        Connection connection;
        try{
            Class.forName(DRIVER);
        } catch(ClassNotFoundException e){
            e.printStackTrace();
            throw new RuntimeException();
        }

        try{
            connection = DriverManager.getConnection(DB_URL);
        } catch(SQLException e){
            e.printStackTrace();
            return null;
        }

        System.out.println("Connection " + DB_URL + " started successfully");

        return connection;
    }
}
