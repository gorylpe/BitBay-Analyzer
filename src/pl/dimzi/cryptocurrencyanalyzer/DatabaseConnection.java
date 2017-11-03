package pl.dimzi.cryptocurrencyanalyzer;

import java.sql.*;

public class DatabaseConnection {

    private static final String DRIVER = "org.sqlite.JDBC";

    private static Connection conn = null;

    /**
     * Returns connection to database with given database url
     * @return connection to database
     */
    public static Connection getConn(String DB_URL) throws SQLException{
        if(conn == null){
            try{
                Class.forName(DRIVER);
            } catch(ClassNotFoundException e){
                e.printStackTrace();
                throw new RuntimeException();
            }

            conn = DriverManager.getConnection(DB_URL);

            Log.d(conn, "Connection " + DB_URL + " started successfully");
        }

        return conn;
    }
}
