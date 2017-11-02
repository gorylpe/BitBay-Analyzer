package pl.dimzi.cryptocurrencyanalyzer.bitbay.controller;

import pl.dimzi.cryptocurrencyanalyzer.Log;
import pl.dimzi.cryptocurrencyanalyzer.enums.Period;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.enums.TradeType;
import pl.dimzi.cryptocurrencyanalyzer.model.CurrencyData;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.model.Trade;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.repository.Repository;

import java.sql.SQLException;
import java.util.ArrayList;

public class CurrencyDataController {

    private Repository repository;

    public CurrencyDataController(Repository repository){
        this.repository = repository;
    }

    /**
     * Updates currency data starting at last trade in repo to last given.
     * @param type type of trades
     * @param dateFrom start date as timestamp
     * @param dateTo end date as timestamp
     * @throws SQLException if adding trades goes wrong
     */
    public void updateCurrencyData(TradeType type, Long dateFrom, Long dateTo) throws SQLException{
        for(Period period : Period.values()){
            ArrayList<CurrencyData> data = new ArrayList<>();
            for(long i = period.floorToPeriodType(dateFrom); i < period.floorToPeriodType(dateTo); i = period.plusPeriod(i)){
                ArrayList<Trade> trades = repository.getTradesByDate(type, i, period.plusPeriod(i));

                if (trades.size() > 0) {
                    double minimum  = Double.MAX_VALUE;
                    double maximum  = Double.MIN_VALUE;
                    double opening  = trades.get(0).getPrice();
                    double closing  = trades.get(trades.size() - 1).getPrice();
                    double average  = 0.0;
                    double volume   = 0.0;

                    for (Trade trade : trades) {
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

                    data.add(new CurrencyData(minimum, maximum, opening, closing, average, volume, i));
                }
            }
            repository.addCurrencyData(data, type, period);
            Log.d(this, "Added " + data.size() + " currency datas of period " + period.getName());
        }
    }
}
