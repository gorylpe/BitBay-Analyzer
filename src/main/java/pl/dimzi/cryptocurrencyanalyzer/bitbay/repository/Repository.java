package main.java.pl.dimzi.cryptocurrencyanalyzer.bitbay.repository;

import main.java.pl.dimzi.cryptocurrencyanalyzer.Log;
import main.java.pl.dimzi.cryptocurrencyanalyzer.bitbay.model.TradeBlock;
import main.java.pl.dimzi.cryptocurrencyanalyzer.model.CurrencyData;
import main.java.pl.dimzi.cryptocurrencyanalyzer.bitbay.model.Trade;
import main.java.pl.dimzi.cryptocurrencyanalyzer.bitbay.enums.TradeType;
import main.java.pl.dimzi.cryptocurrencyanalyzer.enums.Period;
import main.java.pl.dimzi.cryptocurrencyanalyzer.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.locks.ReentrantLock;

public class Repository {
    private final String DB_URL = "jdbc:sqlite:bitbay.db";

    private Connection conn;
    private ReentrantLock connLock = new ReentrantLock();

    public Repository() throws SQLException {
        Log.d(this, "Initializing bitbay repository...");
        conn = DatabaseConnection.getConn(DB_URL);
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

                String createBlocks = "CREATE TABLE IF NOT EXISTS " + type.getTradeBlocksTableName() + " (" +
                        "dateStart DATETIME, " +
                        "dateEnd DATETIME," +
                        "UNIQUE(dateStart, dateEnd));";
                statement.execute(createBlocks);

                Period[] periodTypes = Period.values();

                for (Period period : periodTypes) {
                    statement = conn.createStatement();
                    String createDailyCurrencyData = "CREATE TABLE IF NOT EXISTS " + type.getCurrencyDataTableName(period) + " (" +
                            "periodStart DATETIME PRIMARY KEY, " +
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

        Log.d(this, "Database initialized successfully");
    }

    public void addTrades(ArrayList<Trade> trades, TradeType type) throws SQLException {
        connLock.lock();
        conn.setAutoCommit(false);
        String sql = "INSERT OR REPLACE INTO " + type.getTradesTableName() + " VALUES (?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        for (Trade trade : trades) {
            preparedStatement.setLong(1, trade.getTid());
            preparedStatement.setTimestamp(2, new java.sql.Timestamp(trade.getUnixTimestamp()));
            preparedStatement.setDouble(3, trade.getPrice());
            preparedStatement.setDouble(4, trade.getAmount());
            preparedStatement.setString(5, trade.getType());
            preparedStatement.addBatch();
        }
        try {
            preparedStatement.executeBatch();
        } finally {
            conn.setAutoCommit(true);
            connLock.unlock();
        }

        mergeTradesBlocksWithNewTrades(trades, type);
    }

    private void mergeTradesBlocksWithNewTrades(ArrayList<Trade> trades, TradeType type) throws SQLException{
        Long minDate = trades.stream().min(Comparator.comparingLong(Trade::getUnixTimestamp)).get().getUnixTimestamp();
        Long maxDate = trades.stream().max(Comparator.comparingLong(Trade::getUnixTimestamp)).get().getUnixTimestamp();

        ArrayList<TradeBlock> tradeBlocksEndingInNewBlock =
                getTradeBlocksByDate(type, minDate, maxDate, " WHERE dateEnd >= ? AND dateEnd <= ?");

        ArrayList<TradeBlock> tradeBlocksStartingInNewBlock =
                getTradeBlocksByDate(type, minDate, maxDate, " WHERE dateStart >= ? AND dateStart <= ?");

        StringBuilder builder = new StringBuilder();
        for(TradeBlock block : tradeBlocksEndingInNewBlock){
            builder.append("(").append(block.getDateStart()).append(", ").append(block.getDateEnd()).append(") ");
        }
        for(TradeBlock block : tradeBlocksStartingInNewBlock){
            builder.append("(").append(block.getDateStart()).append(", ").append(block.getDateEnd()).append(") ");
        }

        Log.d(this, "Merging (" + minDate + ", " + maxDate + ") " + " with" + builder.toString());

        Long newBlockMinDate = minDate;
        for(TradeBlock block : tradeBlocksEndingInNewBlock){
            if(block.getDateStart() < newBlockMinDate){
                newBlockMinDate = block.getDateStart();
            }
        }

        Long newBlockMaxDate = maxDate;
        for(TradeBlock block : tradeBlocksStartingInNewBlock){
            if(block.getDateEnd() > newBlockMaxDate){
                newBlockMaxDate = block.getDateEnd();
            }
        }

        removeTradeBlocksByDate(type, minDate, maxDate, " WHERE dateEnd >= ? AND dateEnd <= ?");
        removeTradeBlocksByDate(type, minDate, maxDate, " WHERE dateStart >= ? AND dateStart <= ?");

        String sql = "INSERT OR REPLACE INTO " + type.getTradeBlocksTableName() + " VALUES(?, ?)";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setLong(1, minDate);
        statement.setLong(2, maxDate);
        connLock.lock();
        try{
            statement.execute();
        } finally {
            connLock.unlock();
        }
    }

    private void removeTradeBlocksByDate(TradeType type, Long minDate, Long maxDate, String sqlParam) throws SQLException {
        String sql;
        PreparedStatement statement;

        sql = "DELETE FROM " + type.getTradeBlocksTableName() + sqlParam;
        statement = conn.prepareStatement(sql);
        statement.setLong(1, minDate);
        statement.setLong(2, maxDate);
        connLock.lock();
        try{
            statement.execute();
        } finally {
            connLock.unlock();
        }
    }

    private ArrayList<TradeBlock> getTradeBlocksByDate(TradeType type, long minDate, long maxDate, String sqlParam) throws SQLException{
        ArrayList<TradeBlock> array = new ArrayList<>();

        String sql;
        PreparedStatement statement;
        ResultSet resultSet;

        sql = "SELECT * FROM " + type.getTradeBlocksTableName() + sqlParam;
        statement = conn.prepareStatement(sql);
        statement.setLong(1, minDate);
        statement.setLong(2, maxDate);

        connLock.lock();
        try{
            resultSet = statement.executeQuery();
        } finally {
            connLock.unlock();
        }
        while (resultSet.next()){
            TradeBlock tradeBlock = new TradeBlock(resultSet.getLong(1), resultSet.getLong(2));
            array.add(tradeBlock);
        }

        return array;
    }

    public void addCurrencyData(ArrayList<CurrencyData> datas, TradeType type, Period period) throws SQLException {
        connLock.lock();
        conn.setAutoCommit(false);
        String sql = "INSERT OR REPLACE INTO " + type.getCurrencyDataTableName(period) + " VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        for (CurrencyData data : datas) {
            preparedStatement.setTimestamp(1, new java.sql.Timestamp(data.getPeriodStart()));
            preparedStatement.setDouble(2, data.getMinimum());
            preparedStatement.setDouble(3, data.getMaximum());
            preparedStatement.setDouble(4, data.getOpening());
            preparedStatement.setDouble(5, data.getClosing());
            preparedStatement.setDouble(6, data.getAverage());
            preparedStatement.setDouble(7, data.getVolume());
            preparedStatement.addBatch();
        }
        try {
            preparedStatement.executeBatch();
        } finally {
            conn.setAutoCommit(true);
            connLock.unlock();
        }
    }

    public Trade getNewestTrade(TradeType type) throws SQLException{
        ResultSet resultSet = conn.createStatement().executeQuery(
                "SELECT * FROM " + type.getTradesTableName() + " ORDER BY tid DESC LIMIT 1");
        Trade trade = new Trade();
        if(resultSet.next()){
            trade.setTid(resultSet.getLong(1));
            trade.setDate(resultSet.getTimestamp(2).getTime());
            trade.setPrice(resultSet.getDouble(3));
            trade.setAmount(resultSet.getDouble(4));
            trade.setType(resultSet.getString(5));
        }
        return trade;
    }

    public ArrayList<Trade> getTradesByDate(TradeType tradeType, Long from, Long to) throws SQLException{
        String sql = "SELECT * FROM " + tradeType.getTradesTableName() + " WHERE date >= ? AND date < ? ORDER BY date ASC";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setLong(1, from);
        statement.setLong(2, to);

        ResultSet resultSet = statement.executeQuery();

        ArrayList<Trade> trades = new ArrayList<>();
        while (resultSet.next()) {
            long            tid     = resultSet.getLong(1);
            long            date    = resultSet.getTimestamp(2).getTime();
            double          price   = resultSet.getDouble(3);
            double          amount  = resultSet.getDouble(4);
            String          type    = resultSet.getString(5);

            Trade trade = new Trade();
            trade.setTid(tid);
            trade.setDate(date);
            trade.setPrice(price);
            trade.setAmount(amount);
            trade.setType(type);

            trades.add(trade);
        }

        return trades;
    }

    public ArrayList<CurrencyData> getCurrencyDataByDateToLast(TradeType type, Period period, Long from) throws SQLException{
        return getCurrencyDataByDate(type, period, from, -1L);
    }

    public ArrayList<CurrencyData> getCurrencyDataByDateFromFirst(TradeType type, Period period, Long to) throws SQLException{
        return getCurrencyDataByDate(type, period, -1L, to);
    }

    public ArrayList<CurrencyData> getCurrencyDataAll(TradeType type, Period period) throws SQLException{
        return getCurrencyDataByDate(type, period, -1L, -1L);
    }

    public ArrayList<CurrencyData> getCurrencyDataByDate(TradeType type, Period period, Long from, Long to) throws SQLException{
        String sql = "SELECT * FROM " + type.getCurrencyDataTableName(period);
        if(from != -1L)     sql += "WHERE date >= ?";
        if(to != -1L)       sql += (from != -1L ? " AND date < ?" : "WHERE date < ?");
        sql += " ORDER BY periodStart ASC";
        PreparedStatement statement = conn.prepareStatement(sql);

        if(from != -1L)     statement.setLong(1, from);
        if(to != -1L)       statement.setLong(from != -1L ? 2 : 1, to);

        ResultSet resultSet = statement.executeQuery();

        ArrayList<CurrencyData> data = new ArrayList<>();
        while (resultSet.next()) {
            long date = resultSet.getTimestamp(1).getTime();
            double minimum = resultSet.getDouble(2);
            double maximum = resultSet.getDouble(3);
            double opening = resultSet.getDouble(4);
            double closing = resultSet.getDouble(5);
            double average = resultSet.getDouble(6);
            double volume = resultSet.getDouble(7);

            CurrencyData currencyData = new CurrencyData();
            currencyData.setPeriodStart(date);
            currencyData.setMinimum(minimum);
            currencyData.setMaximum(maximum);
            currencyData.setOpening(opening);
            currencyData.setClosing(closing);
            currencyData.setAverage(average);
            currencyData.setVolume(volume);

            data.add(currencyData);
        }

        return data;
    }
}
