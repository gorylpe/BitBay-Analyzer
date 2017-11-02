package com.dimzi.cryptocurrencyanalyzer.service;

import com.dimzi.cryptocurrencyanalyzer.BitBay.TradeType;
import com.dimzi.cryptocurrencyanalyzer.DatabaseManager;
import com.dimzi.cryptocurrencyanalyzer.ExchangeManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class BitBayDatabaseService {
    private final String DB_URL = "jdbc:sqlite:bitbay.db";

    private Connection conn;

    public BitBayDatabaseService() throws SQLException{
        conn = DatabaseManager.getConn(DB_URL);
        initializeDatabase(conn);
    }

    private void initializeDatabase (Connection conn) throws SQLException{
        Statement statement;

        TradeType[] types = TradeType.values();

        for(TradeType type : types){
            try{
                statement = conn.createStatement();

                String createTrades = "CREATE TABLE IF NOT EXISTS " + type.getTradesTableName() + " (" +
                        "tid BIGINT PRIMARY KEY, " +
                        "date DATETIME, " +
                        "price DOUBLE, " +
                        "amount DOUBLE, " +
                        "type VARCHAR(10))";
                statement.execute(createTrades);

                ExchangeManager.Period[] periodTypes = ExchangeManager.Period.values();

                for(ExchangeManager.Period period : periodTypes){
                    statement = conn.createStatement();
                    String createDailyCurrencyData = "CREATE TABLE IF NOT EXISTS " + type.getCurrencyDataTableName(period) + " (" +
                            "periodStart DATETIME, " +
                            "minimum DOUBLE, " +
                            "maximum DOUBLE, " +
                            "opening DOUBLE, " +
                            "closing DOUBLE, " +
                            "average DOUBLE, " +
                            "volume DOUBLE)";
                    statement.execute(createDailyCurrencyData);
                }
            } catch(SQLException e){
                e.printStackTrace();
                throw new SQLException("Database initialization failed");
            }
        }

        System.out.println("Database initialized successfully");
    }
}
