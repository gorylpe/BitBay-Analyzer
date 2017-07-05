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

    private final String ETH_TRADES_TABLE_NAME = "ETHtrades";
    private final String ETH_TRADES_URL = "https://bitbay.net/API/Public/ETHPLN/trades.json?sort=asc&since=";

    private final String ETH_CURRENCY_DATA_DAILY_TABLE_NAME = "ETHcurrencyDataDaily";
    private BitBayCurrencyData currentDailyCurrencyData;


    private AutoUpdateThread autoUpdateThread = null;
    private ArrayList<CurrencyObserver> observers = new ArrayList<>();

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

            String createTrades = "CREATE TABLE IF NOT EXISTS " + ETH_TRADES_TABLE_NAME + " (" +
                    "tid BIGINT PRIMARY KEY, " +
                    "date DATETIME, " +
                    "price DOUBLE, " +
                    "amount DOUBLE, " +
                    "type VARCHAR(10))";
            statement.execute(createTrades);

            statement = connection.createStatement();

            String createDailyCurrencyData = "CREATE TABLE IF NOT EXISTS " + ETH_CURRENCY_DATA_DAILY_TABLE_NAME + " (" +
                    "periodStart DATETIME, " +
                    "minimum DOUBLE, " +
                    "maximum DOUBLE, " +
                    "opening DOUBLE, " +
                    "closing DOUBLE, " +
                    "average DOUBLE, " +
                    "volume DOUBLE," +
                    "type VARCHAR(10))";
            statement.execute(createDailyCurrencyData);
        } catch(SQLException e){
            e.printStackTrace();
            return false;
        }

        System.out.println("Database initialized successfully");

        return true;
    }

    private boolean addTradeToDb(BitBayTradeJSON trade, String table){
        try{
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + table + " VALUES (?, ?, ?, ?, ?)");
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

    private BitBayTradeJSON[] getEntriesFromServer(Long since){
        BitBayTradeJSON[] tmp = null;
        URL url;
        String jsonString;
        try{
            url = new URL(ETH_TRADES_URL + since);
            jsonString = new Scanner(url.openStream()).useDelimiter("\\A").next();
            tmp = gson.fromJson(jsonString, BitBayTradeJSON[].class);
        } catch(IOException e){
            e.printStackTrace();
        }
        return tmp;
    }

    private BitBayTrade getLastETHTrade(){
        BitBayTrade trade = null;
        try{
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM " + ETH_TRADES_TABLE_NAME + " ORDER BY tid DESC LIMIT 1");
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

    public void updateETHTrades(){
        Long since = getLastETHTrade().getTid();

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        BitBayTradeJSON[] trades = getEntriesFromServer(since);

        int totalAddedTrades = 0;

        boolean success;
        while(trades != null && trades.length > 0){
            for(int i = 0; i < trades.length; ++i){
                success = addTradeToDb(trades[i], ETH_TRADES_TABLE_NAME);
                if(!success)
                    break;
            }

            System.out.println("Added " + trades.length + " entries, since " + dateFormat.format(new Date(trades[trades.length - 1].getUnixTimestamp() * 1000)));

            totalAddedTrades += trades.length;

            since = trades[trades.length - 1].getTid();
            trades = getEntriesFromServer(since);
        }

        if(totalAddedTrades > 0){
            System.out.println("Added total " + totalAddedTrades + " trades");
            this.notifyAllObservers();
        }
    }

    private LocalDateTime getLastDateOfDailyCurrentData(){
        LocalDateTime minDate = null;

        try{
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT min(date) FROM " + ETH_TRADES_TABLE_NAME);
            minDate = resultSet.getTimestamp(1).toLocalDateTime();
        } catch(SQLException e) {
            e.printStackTrace();
            return null;
        }

        try{
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT max(periodStart) FROM " + ETH_CURRENCY_DATA_DAILY_TABLE_NAME);
            Timestamp timestamp = resultSet.getTimestamp(1);
            if(timestamp != null){
                minDate = timestamp.toLocalDateTime();
            }
        } catch(SQLException e){
            e.printStackTrace();
            return null;
        }

        return minDate;
    }

    private BitBayCurrencyData transformTradesToCurrencyData(LocalDateTime periodStart, BitBayCurrencyData previousCurrencyData, String periodType) throws SQLException{
        BitBayCurrencyData currencyData;

        LocalDateTime periodEnd = LocalDateTime.from(periodStart);
        switch (periodType){
            case "daily":
                periodEnd = periodEnd.plusDays(1);
                break;
        }

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + ETH_TRADES_TABLE_NAME + " WHERE date >= ? AND date < ? ORDER BY date ASC");
        statement.setTimestamp(1, Timestamp.valueOf(periodStart));
        statement.setTimestamp(2, Timestamp.valueOf(periodEnd));

        ResultSet resultSet = statement.executeQuery();

        ArrayList<BitBayTrade> tradesArray = new ArrayList<>();

        LocalDateTime date;
        Double price;
        String type;
        Double amount;
        Long tid;

        while (resultSet.next()) {
            tid = resultSet.getLong(1);
            date = resultSet.getTimestamp(2).toLocalDateTime();
            price = resultSet.getDouble(3);
            amount = resultSet.getDouble(4);
            type = resultSet.getString(5);

            BitBayTrade trade = new BitBayTrade();
            trade.setTid(tid);
            trade.setDate(date);
            trade.setPrice(price);
            trade.setAmount(amount);
            trade.setType(type);

            tradesArray.add(trade);
        }

        if (tradesArray.size() > 0) {
            double minimum = Double.MAX_VALUE;
            double maximum = Double.MIN_VALUE;
            double opening = tradesArray.get(0).getPrice();
            double closing = tradesArray.get(tradesArray.size() - 1).getPrice();
            double average = 0.0;
            double volume = 0.0;

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

            currencyData = new BitBayCurrencyData(minimum, maximum, opening, closing, average, volume, periodStart, periodType);
        } else {
            currencyData = new BitBayCurrencyData(
                    previousCurrencyData.getClosing(),
                    previousCurrencyData.getClosing(),
                    previousCurrencyData.getClosing(),
                    previousCurrencyData.getClosing(),
                    previousCurrencyData.getClosing(),
                    0.0,
                    periodStart,
                    periodType);
        }

        return currencyData;
    }

    private LocalDateTime roundToFullDay(LocalDateTime localDateTime){
        return localDateTime.with(LocalTime.of(0, 0));
    }

    public void updateETHCurrentDataDaily() {

        LocalDateTime newest = getLastETHTrade().getDate();
        newest = roundToFullDay(newest);

        LocalDateTime lastCurrencyDataDate = getLastDateOfDailyCurrentData();
        lastCurrencyDataDate = roundToFullDay(lastCurrencyDataDate);

        //updates previous full days
        BitBayCurrencyData lastCurrencyData = new BitBayCurrencyData();
        for (LocalDateTime iTime = lastCurrencyDataDate.plusDays(1); iTime.isBefore(newest); iTime = iTime.plusDays(1)) {
            try {
                BitBayCurrencyData currencyData = transformTradesToCurrencyData(iTime, lastCurrencyData, "daily");

                addCurrencyDataToDb(currencyData, ETH_CURRENCY_DATA_DAILY_TABLE_NAME);

                lastCurrencyData = currencyData;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        //update current data
        try{
            currentDailyCurrencyData = transformTradesToCurrencyData(newest, lastCurrencyData, "daily");
        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    private void addCurrencyDataToDb(BitBayCurrencyData currencyData, String table) throws SQLException{
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + table + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        preparedStatement.setTimestamp(1, Timestamp.valueOf(currencyData.getPeriodStart()));
        preparedStatement.setDouble(2, currencyData.getMinimum());
        preparedStatement.setDouble(3, currencyData.getMaximum());
        preparedStatement.setDouble(4, currencyData.getOpening());
        preparedStatement.setDouble(5, currencyData.getClosing());
        preparedStatement.setDouble(6, currencyData.getAverage());
        preparedStatement.setDouble(7, currencyData.getVolume());
        preparedStatement.setString(8, currencyData.getType());
        preparedStatement.execute();
    }

    public void update(){
        updateETHTrades();
        updateETHCurrentDataDaily();
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

    public void attachObserver(CurrencyObserver observer){
        observers.add(observer);
    }

    public void notifyAllObservers(){
        System.out.println("Notifying all observers");
        for(CurrencyObserver observer : observers){
            observer.update();
        }
    }
}
