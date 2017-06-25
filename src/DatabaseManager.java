import com.google.gson.Gson;
import model.TradeJSON;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class DatabaseManager {

    private static final String DRIVER = "org.sqlite.JDBC";
    private static final String DB_URL = "jdbc:sqlite:trades.db";

    private static final String TRADES_TABLE_NAME = "trades";

    private static final String BITBAY_TRADES_URL = "https://bitbay.net/API/Public/ETHPLN/trades.json?sort=asc&since=";

    private static Connection defaultConnection;
    private static Gson defaultGson = new Gson();

    private static AutoUpdateThread autoUpdateThread = null;

    public static boolean startConnection(){
        try{
            Class.forName(DRIVER);
        } catch(ClassNotFoundException e){
            e.printStackTrace();
            return false;
        }

        try{
            defaultConnection = DriverManager.getConnection(DB_URL);
        } catch(SQLException e){
            e.printStackTrace();
            return false;
        }

        System.out.println("Connection started successfully");


        return true;
    }

    public static Connection getCustomConnection(){
        Connection connection = null;
        try{
            connection = DriverManager.getConnection(DB_URL);
        }catch(SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static boolean initializeDatabase(){
        Statement statement;

        String createTrades = "CREATE TABLE IF NOT EXISTS " + TRADES_TABLE_NAME + " (" +
                "tid BIGINT PRIMARY KEY, " +
                "date DATE, " +
                "price DOUBLE, " +
                "amount DOUBLE, " +
                "type VARCHAR(10))";

        try{
            statement = defaultConnection.createStatement();
            statement.execute(createTrades);
        } catch(SQLException e){
            e.printStackTrace();
            return false;
        }

        System.out.println("Database initialized successfully");

        return true;
    }

    private static boolean addTradeToDb(TradeJSON trade){
        try{
            PreparedStatement preparedStatement = defaultConnection.prepareStatement("INSERT INTO trades VALUES (?, ?, ?, ?, ?)");
            preparedStatement.setLong(1, trade.getTid());
            preparedStatement.setDate(2, new Date(trade.getDate() * 1000));
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

    private static TradeJSON[] getEntries(Long since){
        TradeJSON[] tmp = null;
        URL url;
        String jsonString;
        try{
            url = new URL(BITBAY_TRADES_URL + since);
            jsonString = new Scanner(url.openStream()).useDelimiter("\\A").next();
            tmp = defaultGson.fromJson(jsonString, TradeJSON[].class);
        } catch(IOException e){
            e.printStackTrace();
        }
        return tmp;
    }

    private static long getLastTid(){
        Long since = 0L;

        try{
            ResultSet resultSet = defaultConnection.createStatement().executeQuery("SELECT max(tid) FROM trades");
            since = resultSet.getLong(1);
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return since;
    }

    public static Date getLastDate(){
        Date date = null;
        try{
            ResultSet resultSet = defaultConnection.createStatement().executeQuery("SELECT max(date) FROM trades");
            date = resultSet.getDate(1);
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return date;
    }

    public static boolean updateTradesTable(){

        Long since = getLastTid();

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        TradeJSON[] trades = getEntries(since);
        boolean success;
        while(trades != null && trades.length > 0){
            for(int i = 0; i < trades.length; ++i){
                success = addTradeToDb(trades[i]);
                if(!success)
                    break;
            }

            System.out.println("Added " + trades.length + " entries, since " + dateFormat.format(new Date(trades[trades.length - 1].getDate() * 1000)));

            since = trades[trades.length - 1].getTid();
            trades = getEntries(since);
        }

        System.out.println("Ended adding new entries");
        return true;
    }

    public static class AutoUpdateThread extends Thread{

        private long interval;
        private boolean run;

        public AutoUpdateThread(long interval){
            this.interval = interval;
            this.run = true;
        }

        @Override
        public void run(){
            System.out.println("Starting autoupdate thread");

            while(run){
                updateTradesTable();
                try {
                    Thread.sleep(interval);
                } catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }

        public void end(){
            System.out.println("Ending autoupdate thread");

            try{
                run = false;
                this.join();
            } catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    public static void startAutoUpdateThread(final long interval){
        if(autoUpdateThread == null){
            autoUpdateThread = new AutoUpdateThread(interval);
            autoUpdateThread.start();
        }
    }

    public static void stopAutoUpdateThread(){
        autoUpdateThread.end();
    }
}
