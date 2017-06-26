import model.CurrencyData;
import model.Trade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class AnalysisManager implements CurrencyObserver{

    private static ArrayList<CurrencyData> days;

    private static Connection connection = DatabaseManager.getCustomConnection();

    private static ArrayList<Trade> getTradesSince(Date sinceDate){
        ArrayList<Trade> tmp = new ArrayList<>();

        try{
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM trades WHERE date >= ?");
            preparedStatement.setDate(1, new java.sql.Date(sinceDate.getTime()));
            ResultSet resultSet = preparedStatement.executeQuery();

            Date date;
            Double price;
            String type;
            Double amount;
            Long tid;

            while (resultSet.next()){
                tid = resultSet.getLong(1);
                date = new Date(resultSet.getDate(2).getTime());
                price = resultSet.getDouble(3);
                amount = resultSet.getDouble(4);
                type = resultSet.getString(5);

                Trade trade = new Trade();
                trade.setTid(tid);
                trade.setDate(date);
                trade.setPrice(price);
                trade.setAmount(amount);
                trade.setType(type);

                tmp.add(trade);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return tmp;
    }

    public static void initializeDays(){
        days = new ArrayList<>(365);

        Date now = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);

        calendar.add(Calendar.DATE, -365);

        ArrayList<Trade> trades = getTradesSince(calendar.getTime());

        HashMap<Date, ArrayList<Trade>> daysTrades = new HashMap<>(365);
        for(int i = 0; i < 365; ++i){
            daysTrades.put(calendar.getTime(), new ArrayList<>());
            calendar.add(Calendar.DATE, 1);
        }

        for(int i = 0; i < trades.size(); ++i){
            calendar.setTime(trades.get(i).getDate());
            //round
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);

            daysTrades.get(calendar.getTime()).add(trades.get(i));
        }

        calendar.setTime(now);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.add(Calendar.DATE, -365);

        for(int i = 0; i < 365; ++i){
            ArrayList<Trade> dayTrades = daysTrades.get(calendar.getTime());

            if(dayTrades.size() == 0){
                if(days.size() > 0){
                    CurrencyData currencyData = new CurrencyData();
                    currencyData.setAverage(days.get(i - 1).getClosing());
                    currencyData.setMinimum(days.get(i - 1).getClosing());
                    currencyData.setMaximum(days.get(i - 1).getClosing());
                    currencyData.setOpening(days.get(i - 1).getClosing());
                    currencyData.setClosing(days.get(i - 1).getClosing());
                    currencyData.setPeriodStart(calendar.getTime());
                    days.add(currencyData);
                } else {
                    CurrencyData currencyData = new CurrencyData();
                    currencyData.setAverage(0.0);
                    currencyData.setMinimum(0.0);
                    currencyData.setMaximum(0.0);
                    currencyData.setOpening(0.0);
                    currencyData.setClosing(0.0);
                    currencyData.setPeriodStart(calendar.getTime());
                    days.add(currencyData);
                }
            } else {

                double opening = dayTrades.get(dayTrades.size() - 1).getPrice();
                double closing = dayTrades.get(0).getPrice();

                double minimal = Double.MAX_VALUE;
                double maximal = Double.MIN_VALUE;
                double wages = 0.0;
                double sum = 0.0;
                for (Trade trade : dayTrades) {
                    Double amount = trade.getAmount();
                    Double price = trade.getPrice();

                    wages += amount;
                    sum += price * amount;

                    if (price < minimal)
                        minimal = price;

                    if (price > maximal)
                        maximal = price;
                }

                CurrencyData currencyData = new CurrencyData();
                currencyData.setAverage(sum / wages);
                currencyData.setMinimum(minimal);
                currencyData.setMaximum(maximal);
                currencyData.setOpening(opening);
                currencyData.setClosing(closing);
                currencyData.setPeriodStart(calendar.getTime());
                days.add(currencyData);
            }

            System.out.println(calendar.getTime());
            System.out.println("Average: " + String.format("%.2f", days.get(days.size() - 1).getAverage()) + "zl " +
                                "Minimum: " + days.get(days.size() - 1).getMinimum() + "zl " +
                                "Maximum: " + days.get(days.size() - 1).getMaximum() + "zl " +
                                "Opening: " + days.get(days.size() - 1).getOpening() + "zl " +
                                "Closing: " + days.get(days.size() - 1).getClosing() + "zl ");

            calendar.add(Calendar.DATE, 1);
        }
    }


    @Override
    public void update() {

    }
}
