package com.dimzi.cryptocurrencyanalyzer.bitbay.service;

import com.dimzi.cryptocurrencyanalyzer.bitbay.model.CurrencyData;
import com.dimzi.cryptocurrencyanalyzer.bitbay.model.Trade;
import com.dimzi.cryptocurrencyanalyzer.bitbay.model.TradeType;
import com.dimzi.cryptocurrencyanalyzer.bitbay.model.Period;
import com.dimzi.cryptocurrencyanalyzer.DatabaseManager;
import com.dimzi.cryptocurrencyanalyzer.ExchangeManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class DatabaseService {
    private final String DB_URL = "jdbc:sqlite:bitbay.db";

    private Connection conn;
    private ReentrantLock connLock = new ReentrantLock();

    public DatabaseService() throws SQLException {
        conn = DatabaseManager.getConn(DB_URL);
        initializeDatabase(conn);
    }

    private void initializeDatabase(Connection conn) throws SQLException {
        Statement statement;

        TradeType[] types = TradeType.values();

        for (TradeType type : types) {
            try {
                statement = conn.createStatement();

                String createTrades = "CREATE TABLE IF NOT EXISTS " + type.getTradesTableName() + " (" +
                        "tid BIGINT PRIMARY KEY, " +
                        "date DATETIME, " +
                        "price DOUBLE, " +
                        "amount DOUBLE, " +
                        "type VARCHAR(10))";
                statement.execute(createTrades);

                Period[] periodTypes = Period.values();

                for (Period period : periodTypes) {
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
            } catch (SQLException e) {
                e.printStackTrace();
                throw new SQLException("Database initialization failed");
            }
        }

        System.out.println("Database initialized successfully");
    }

    public void addTradesToDb(ArrayList<Trade> trades, TradeType type) throws SQLException {
        for (Trade trade : trades) {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO " + type.getTradesTableName() + " VALUES (?, ?, ?, ?, ?)");
            preparedStatement.setLong(1, trade.getTid());
            preparedStatement.setTimestamp(2, new java.sql.Timestamp(trade.getUnixTimestamp() * 1000));
            preparedStatement.setDouble(3, trade.getPrice());
            preparedStatement.setDouble(4, trade.getAmount());
            preparedStatement.setString(5, trade.getType());
            connLock.lock();
            try {
                preparedStatement.execute();
            } finally {
                connLock.unlock();
            }
        }
    }

    public void addCurrencyDataToDb(ArrayList<CurrencyData> datas, TradeType type, Period period) throws SQLException {
        for (CurrencyData data : datas) {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO " + type.getCurrencyDataTableName(period) + " VALUES (?, ?, ?, ?, ?, ?, ?)");
            preparedStatement.setTimestamp(1, Timestamp.valueOf(data.getPeriodStart()));
            preparedStatement.setDouble(2, data.getMinimum());
            preparedStatement.setDouble(3, data.getMaximum());
            preparedStatement.setDouble(4, data.getOpening());
            preparedStatement.setDouble(5, data.getClosing());
            preparedStatement.setDouble(6, data.getAverage());
            preparedStatement.setDouble(7, data.getVolume());
            connLock.lock();
            try {
                preparedStatement.execute();
            } finally {
                connLock.unlock();
            }
        }
    }

    public ArrayList<CurrencyData> getCurrencyDataAll(TradeType tradeType, TradeType type, Period periodType) {
        //TODO
        return null;
    }
}
