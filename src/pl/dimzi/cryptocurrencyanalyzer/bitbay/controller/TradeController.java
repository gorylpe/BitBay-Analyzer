package pl.dimzi.cryptocurrencyanalyzer.bitbay.controller;

import pl.dimzi.cryptocurrencyanalyzer.bitbay.enums.TradeType;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.model.Trade;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.service.ConnectionService;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.repository.Repository;

import java.sql.SQLException;
import java.util.ArrayList;

public class TradeController {

    private Repository repository;
    private ConnectionService connectionService;

    public TradeController(Repository repository, ConnectionService connectionService){
        this.repository = repository;
        this.connectionService = connectionService;
    }

    /**
     * Downloads and inserts toDate repo all trades from "fromDate" to "toDate" dates.
     * @param type type of trade
     * @param fromDate starting date
     * @param toDate ending date
     * @throws SQLException if adding trades goes wrong
     */
    public void updateTradesUsingDate(TradeType type, Long fromDate, Long toDate) throws SQLException{
        Long from = findTidByDate(type, fromDate);
        Long to = findTidByDate(type, toDate);

        updateTrades(type, from, to);
    }

    /**
     * Updates trades starting at last trade in repo to now.
     * @param type type of trade
     * @param from start Tid
     * @throws SQLException if adding trades goes wrong
     */
    public void updateTrades(TradeType type, Long from) throws SQLException{
        updateTrades(type, from, -1L);
    }

    /**
     * Updates trades starting at last trade in repo to last given.
     * @param type type of trade
     * @param from start Tid
     * @param to end Tid
     * @throws SQLException if adding trades goes wrong
     */
    public void updateTrades(TradeType type, Long from, Long to) throws SQLException{
        ArrayList<Trade> trades;
        if(to == -1){
            trades = connectionService.getTradesFromToNow(type, from);
        } else {
            trades = connectionService.getTradesFromTo(type, from, to);
        }

        repository.addTrades(trades, type);
    }

    private long findTidByDate(TradeType type, Long date){
        long max = connectionService.getNewestTid(type);
        long min = 0;

        long mid = (min + max) / 2;
        Trade midTrade = connectionService.getClosestNextTrade(type, mid);

        while(min != mid && midTrade.getUnixTimestamp() != date){
            if(date > midTrade.getUnixTimestamp()){
                min = mid;
            } else if(date < midTrade.getUnixTimestamp()){
                max = mid;
            } else {
                break;
            }
            mid = (min + max) / 2;
            midTrade = connectionService.getClosestNextTrade(type, mid);
        }

        return midTrade.getTid();
    }
}
