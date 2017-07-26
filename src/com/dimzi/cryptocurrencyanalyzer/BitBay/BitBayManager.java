package com.dimzi.cryptocurrencyanalyzer.BitBay;

import com.dimzi.cryptocurrencyanalyzer.DatabaseManager;
import com.dimzi.cryptocurrencyanalyzer.ExchangeManager;
import com.google.gson.Gson;
import model.BitBayCurrencyData;
import model.BitBayTrade;
import model.BitBayTradeJSON;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.Date;

public class BitBayManager implements ExchangeManager {

    private final String DB_URL = "jdbc:sqlite:bitbay.db";

    public enum TradeType{
        ETHPLN;

        String name;
        String url;

        TradeType(){
            name = name();
            this.url = "https://bitbay.net/API/Public/" + name + "/trades.json?sort=asc&since=";
        }

        String getUrl(){
            return url;
        }

        String getTradesTableName(){
            return name + "_TRADES";
        }

        String getCurrencyDataTableName(CurrencyDataPeriodType currencyDataPeriodType){
            return name + "_" + currencyDataPeriodType.getName();
        }
    }

    private BitBayCurrencyData currentDailyCurrencyData;

    private AutoUpdateThread autoUpdateThread = null;
    private ArrayList<BitBayCurrencyObserver> observers = new ArrayList<>();

    private Connection connection;
    private Gson gson;

    public BitBayManager() throws SQLException{
        connection = DatabaseManager.startConnection(DB_URL);

        if(connection == null){
            throw new SQLException("Connection not initialized");
        }

        if(!initializeDatabase()){
            throw new SQLException("Database not initialized");
        }

        gson = new Gson();
    }

    public boolean initializeDatabase(){
        Statement statement;

        try{
            statement = connection.createStatement();

            String createTrades = "CREATE TABLE IF NOT EXISTS " + TradeType.ETHPLN.getTradesTableName() + " (" +
                    "tid BIGINT PRIMARY KEY, " +
                    "date DATETIME, " +
                    "price DOUBLE, " +
                    "amount DOUBLE, " +
                    "type VARCHAR(10))";
            statement.execute(createTrades);

            statement = connection.createStatement();

            String createDailyCurrencyData = "CREATE TABLE IF NOT EXISTS " + TradeType.ETHPLN.getCurrencyDataTableName(CurrencyDataPeriodType.DAILY) + " (" +
                    "periodStart DATETIME, " +
                    "minimum DOUBLE, " +
                    "maximum DOUBLE, " +
                    "opening DOUBLE, " +
                    "closing DOUBLE, " +
                    "average DOUBLE, " +
                    "volume DOUBLE)";
            statement.execute(createDailyCurrencyData);
        } catch(SQLException e){
            e.printStackTrace();
            return false;
        }

        System.out.println("Database initialized successfully");

        return true;
    }

    private boolean addTradeToDb(BitBayTradeJSON trade, TradeType type){
        try{
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + type.getTradesTableName() + " VALUES (?, ?, ?, ?, ?)");
            preparedStatement.setLong(1, trade.getTid());
            preparedStatement.setTimestamp(2, new java.sql.Timestamp(trade.getUnixTimestamp() * 1000));
            preparedStatement.setDouble(3, trade.getPrice());
            preparedStatement.setDouble(4, trade.getAmount());
            preparedStatement.setString(5, trade.getType());
            preparedStatement.execute();
        } catch(SQLException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private BitBayTradeJSON[] getTradeEntriesFromServer(TradeType type, Long since){
        try{
            URL url = new URL(type.getTradesTableName() + since);
            String jsonString = new Scanner(url.openStream()).useDelimiter("\\A").next();
            BitBayTradeJSON[] tmp = gson.fromJson(jsonString, BitBayTradeJSON[].class);

            return tmp;
        } catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

    private BitBayTrade getLastTrade(TradeType type){
        BitBayTrade trade = null;
        try{
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM " + type.getTradesTableName() + " ORDER BY tid DESC LIMIT 1");
            if(resultSet.next()){
                trade = new BitBayTrade(
                        resultSet.getLong(1),
                        resultSet.getTimestamp(2).toLocalDateTime(),
                        resultSet.getDouble(3),
                        resultSet.getDouble(4),
                        resultSet.getString(5));
            } else {
                trade = new BitBayTrade();
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return trade;
    }

    public void updateTrades(TradeType type){
        Long since = getLastTrade(type).getTid();

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        BitBayTradeJSON[] trades = getTradeEntriesFromServer(type, since);

        int totalAddedTrades = 0;

        boolean success;
        while(trades != null && trades.length > 0){
            for(int i = 0; i < trades.length; ++i){
                success = addTradeToDb(trades[i], type);
                if(!success)
                    break;
            }

            System.out.println("Added " + trades.length + " entries, since " + dateFormat.format(new Date(trades[trades.length - 1].getUnixTimestamp() * 1000)));

            totalAddedTrades += trades.length;

            since = trades[trades.length - 1].getTid();
            trades = getTradeEntriesFromServer(type, since);
        }

        if(totalAddedTrades > 0){
            System.out.println("Added total " + totalAddedTrades + " trades");
            this.notifyAllObservers();
        }
    }

    private LocalDateTime getLastDateOfCurrencyData(CurrencyDataPeriodType periodType, TradeType type){
        LocalDateTime minDate;

        try{
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT max(periodStart) FROM " + type.getCurrencyDataTableName(periodType));
            Timestamp timestamp = resultSet.getTimestamp(1);
            if(timestamp != null){
                minDate = timestamp.toLocalDateTime();
            } else {
                try{
                    resultSet = connection.createStatement().executeQuery("SELECT min(date) FROM " + type.getTradesTableName());
                    timestamp = resultSet.getTimestamp(1);
                    if(timestamp != null){
                        minDate = timestamp.toLocalDateTime();
                    } else {
                        minDate = LocalDateTime.now();
                    }
                } catch(SQLException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        } catch(SQLException e){
            e.printStackTrace();
            return null;
        }

        return minDate;
    }

    private BitBayCurrencyData tradesToCurrencyData(LocalDateTime periodStart, BitBayCurrencyData previousCurrencyData, CurrencyDataPeriodType periodType, TradeType tradeType) throws SQLException{
        BitBayCurrencyData currencyData;

        LocalDateTime periodEnd = LocalDateTime.from(periodStart);
        switch (periodType){
            case DAILY:
                periodEnd = periodEnd.plusDays(1);
                break;
            default:
                return null;
        }

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tradeType.getTradesTableName() + " WHERE date >= ? AND date < ? ORDER BY date ASC");
        statement.setTimestamp(1, Timestamp.valueOf(periodStart));
        statement.setTimestamp(2, Timestamp.valueOf(periodEnd));

        ResultSet resultSet = statement.executeQuery();

        ArrayList<BitBayTrade> tradesArray = new ArrayList<>();

        while (resultSet.next()) {
            long            tid     = resultSet.getLong(1);
            LocalDateTime   date    = resultSet.getTimestamp(2).toLocalDateTime();
            double          price   = resultSet.getDouble(3);
            double          amount  = resultSet.getDouble(4);
            String          type    = resultSet.getString(5);

            BitBayTrade trade = new BitBayTrade();
            trade.setTid(tid);
            trade.setDate(date);
            trade.setPrice(price);
            trade.setAmount(amount);
            trade.setType(type);

            tradesArray.add(trade);
        }

        if (tradesArray.size() > 0) {
            double minimum  = Double.MAX_VALUE;
            double maximum  = Double.MIN_VALUE;
            double opening  = tradesArray.get(0).getPrice();
            double closing  = tradesArray.get(tradesArray.size() - 1).getPrice();
            double average  = 0.0;
            double volume   = 0.0;

            for (BitBayTrade trade : tradesArray) {
                if (trade.getPrice() < minimum) {
                    minimum = trade.getPrice();
                }
                if (trade.getPrice() > maximum) {
                    maximum = trade.getPrice();
                }
                average += trade.getPrice() * trade.getAmount();
                volume += trade.getAmount();
            }

            average /= volume;

            currencyData = new BitBayCurrencyData(minimum, maximum, opening, closing, average, volume, periodStart);
        } else {
            currencyData = new BitBayCurrencyData(
                    previousCurrencyData.getClosing(),
                    previousCurrencyData.getClosing(),
                    previousCurrencyData.getClosing(),
                    previousCurrencyData.getClosing(),
                    previousCurrencyData.getClosing(),
                    0.0,
                    periodStart);
        }

        return currencyData;
    }

    private LocalDateTime roundToPeriodType(CurrencyDataPeriodType periodType, LocalDateTime localDateTime){
        switch(periodType){
            case DAILY:
                localDateTime = localDateTime.with(LocalTime.of(0, 0));
                break;
        }
        return localDateTime;
    }

    public void updateCurrencyData(TradeType tradeType, CurrencyDataPeriodType periodType) {

        LocalDateTime newest = getLastTrade(tradeType).getDate();
        newest = roundToPeriodType(periodType, newest);

        LocalDateTime lastCurrencyDataDate = getLastDateOfCurrencyData(periodType, tradeType);
        lastCurrencyDataDate = roundToPeriodType(periodType, lastCurrencyDataDate);

        int totalAddedCurrencyData = 0;

        //updates previous full days
        BitBayCurrencyData lastCurrencyData = new BitBayCurrencyData();
        for (LocalDateTime iTime = lastCurrencyDataDate.plusDays(1); iTime.isBefore(newest); iTime = iTime.plusDays(1)) {
            try {
                BitBayCurrencyData currencyData = tradesToCurrencyData(iTime, lastCurrencyData, periodType, tradeType);

                addCurrencyDataToDb(currencyData, tradeType.getCurrencyDataTableName(periodType));

                lastCurrencyData = currencyData;

                System.out.println("Added currency data type " + periodType.getName() + " starting at " + iTime);
                ++totalAddedCurrencyData;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if(totalAddedCurrencyData > 0){
            System.out.println("Total currency data added " + totalAddedCurrencyData);
        }

        //update current data
        try{
            currentDailyCurrencyData = tradesToCurrencyData(newest, lastCurrencyData, periodType, tradeType);
        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    private void addCurrencyDataToDb(BitBayCurrencyData currencyData, String table) throws SQLException{
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + table + " VALUES (?, ?, ?, ?, ?, ?, ?)");
        preparedStatement.setTimestamp(1, Timestamp.valueOf(currencyData.getPeriodStart()));
        preparedStatement.setDouble(2, currencyData.getMinimum());
        preparedStatement.setDouble(3, currencyData.getMaximum());
        preparedStatement.setDouble(4, currencyData.getOpening());
        preparedStatement.setDouble(5, currencyData.getClosing());
        preparedStatement.setDouble(6, currencyData.getAverage());
        preparedStatement.setDouble(7, currencyData.getVolume());
        preparedStatement.execute();
    }

    public void update(){
        updateTrades(TradeType.ETHPLN);
        updateCurrencyData(TradeType.ETHPLN, CurrencyDataPeriodType.DAILY);
    }

    public void currencyDataResultSetToArray(ArrayList<BitBayCurrencyData> currencyDataArray, ResultSet resultSet){
        try {
            while (resultSet.next()) {
                LocalDateTime date = resultSet.getTimestamp(1).toLocalDateTime();
                double minimum = resultSet.getDouble(2);
                double maximum = resultSet.getDouble(3);
                double opening = resultSet.getDouble(4);
                double closing = resultSet.getDouble(5);
                double average = resultSet.getDouble(6);
                double volume = resultSet.getDouble(7);

                BitBayCurrencyData currencyData = new BitBayCurrencyData();
                currencyData.setPeriodStart(date);
                currencyData.setMinimum(minimum);
                currencyData.setMaximum(maximum);
                currencyData.setOpening(opening);
                currencyData.setClosing(closing);
                currencyData.setAverage(average);
                currencyData.setVolume(volume);

                currencyDataArray.add(currencyData);
            }
        } catch(SQLException e){
            System.out.println("Error converting result set to arraylist");
            e.printStackTrace();
        }
    }

    public ArrayList<BitBayCurrencyData> getCurrencyDataFromPeriod(TradeType tradeType, LocalDateTime periodStart, LocalDateTime periodEnd, CurrencyDataPeriodType periodType){
        ArrayList<BitBayCurrencyData> currencyDataArray = new ArrayList<>();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tradeType.getCurrencyDataTableName(periodType) + " WHERE periodStart >= ? AND periodStart <= ? ORDER BY periodStart ASC");
            statement.setTimestamp(1, Timestamp.valueOf(periodStart));
            statement.setTimestamp(2, Timestamp.valueOf(periodEnd));

            ResultSet resultSet = statement.executeQuery();

            currencyDataResultSetToArray(currencyDataArray, resultSet);

        } catch(SQLException e){
            System.out.println("Error getting currency data");
            e.printStackTrace();
        }

        System.out.println("Got " + currencyDataArray.size() + " entries from BitBay currency data of type " + periodType.getName() + " from period " + periodStart + " - " + periodEnd);

        return currencyDataArray;
    }

    public ArrayList<BitBayCurrencyData> getCurrencyDataAll(TradeType tradeType, CurrencyDataPeriodType periodType) {
        ArrayList<BitBayCurrencyData> currencyDataArray = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tradeType.getCurrencyDataTableName(periodType) + " ORDER BY periodStart ASC");

            ResultSet resultSet = statement.executeQuery();

            currencyDataResultSetToArray(currencyDataArray, resultSet);

        } catch (SQLException e) {
            System.out.println("Error getting currency data");
            e.printStackTrace();
        }

        System.out.println("Got all " + currencyDataArray.size() + " entries from BitBay currency data of type " + periodType.getName());

        return currencyDataArray;
    }

    public void startAutoUpdateThread(final long interval){
        if(autoUpdateThread == null){
            autoUpdateThread = new AutoUpdateThread(this, interval);
            autoUpdateThread.start();
        }
    }

    public void stopAutoUpdateThread(){
        autoUpdateThread.end();
        autoUpdateThread = null;
    }

    public void attachObserver(BitBayCurrencyObserver observer){
        System.out.println("Adding observer " + observer.getClass().getName());
        observers.add(observer);
    }

    public void notifyAllObservers(){
        System.out.println("Notifying all BitBay observers");
        for(BitBayCurrencyObserver observer : observers){
            System.out.println("Notifying observer " + observer.getClass().getName());
            observer.update(this);
        }
    }
}
