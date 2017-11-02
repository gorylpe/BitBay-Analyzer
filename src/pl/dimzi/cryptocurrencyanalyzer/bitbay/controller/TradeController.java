package pl.dimzi.cryptocurrencyanalyzer.bitbay.controller;

import pl.dimzi.cryptocurrencyanalyzer.bitbay.enums.TradeType;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.model.Trade;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.service.ConnectionService;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.repository.Repository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class TradeController {

    Repository repository;
    ConnectionService connectionService;

    public TradeController(Repository repository, ConnectionService connectionService){
        this.repository = repository;
        this.connectionService = connectionService;
    }

    /**
     * Downloads and inserts to repo all trades from "from" to "to" dates.
     * @param type type of trade
     * @param from starting date
     * @param to ending date
     * @throws SQLException if adding trades goes wrong
     */
    public void downloadTrades(TradeType type, Long from, Long to) throws SQLException{
        Long fromTid = findTidByDate(type, from);
        Long toTid = findTidByDate(type, to);

        ArrayList<Trade> trades = connectionService.getTradesFromToTid(type, fromTid, toTid);

        repository.addTrades(trades, type);
    }

    /**
     * Updates trades starting at last trade in repo.
     * @param type type of trade
     * @throws SQLException if adding trades goes wrong
     */
    public void updateTrades(TradeType type) throws SQLException{
        Long fromTid = repository.getNewestTid(type);

        ArrayList<Trade> trades = connectionService.getTradesFromToNow(type, fromTid);

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
