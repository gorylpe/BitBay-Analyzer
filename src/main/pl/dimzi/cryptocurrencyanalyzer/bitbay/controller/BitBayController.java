package main.pl.dimzi.cryptocurrencyanalyzer.bitbay.controller;

import main.pl.dimzi.cryptocurrencyanalyzer.Log;
import main.pl.dimzi.cryptocurrencyanalyzer.bitbay.enums.TradeType;
import main.pl.dimzi.cryptocurrencyanalyzer.bitbay.repository.Repository;
import main.pl.dimzi.cryptocurrencyanalyzer.bitbay.service.ConnectionService;
import main.pl.dimzi.cryptocurrencyanalyzer.enums.Period;
import main.pl.dimzi.cryptocurrencyanalyzer.model.CurrencyData;

import javax.swing.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

public enum BitBayController {
    INSTANCE;

    private Repository repo;
    private ConnectionService connectionService;

    private TradeController tradeController;
    private CurrencyDataController currencyDataController;
    private WindowController windowController;

    private Map<Period, Map<TradeType, ArrayList<CurrencyData>>> currencyData;

    BitBayController() {
        try {
            repo = new Repository();
            connectionService = new ConnectionService();

            tradeController = new TradeController(repo, connectionService);
            currencyDataController = new CurrencyDataController(repo);
            windowController = new WindowController();
        }catch (SQLException e){
            Log.e(this, e.getMessage());
        }

        currencyData = new EnumMap<>(Period.class);
        for (Period period : Period.values()) {
            currencyData.put(period, new EnumMap<>(TradeType.class));
        }

        //DEBUG SOME INITIALIZATIONS
        try {
            long startTime = 1504568424;
            long stopTime = 1509752424;
            Log.d(this, "Getting trades of " + startTime + " to " + stopTime);
            //TODO DEBUG DOWNLOAD TRADES
            //tradeController.updateTradesUsingDate(TradeType.ETHPLN, startTime, stopTime);
            currencyDataController.updateCurrencyData(TradeType.ETHPLN, startTime, stopTime);
        }catch (SQLException e){
            Log.e(this, e.getMessage());
        }
    }

    public void refreshCurrencyData(Period period, TradeType type) throws SQLException{
        long time = System.currentTimeMillis();
        ArrayList<CurrencyData> data = repo.getCurrencyDataAll(type, period);
        currencyData.get(period).put(type, data);
        windowController.refreshCurrencyData(type, period, data);
        Log.d(this, "Refreshing data time " + (System.currentTimeMillis() - time) + "ms");
    }

    public JPanel getRootPanel(){
        return windowController.getRootPanel();
    }
}
