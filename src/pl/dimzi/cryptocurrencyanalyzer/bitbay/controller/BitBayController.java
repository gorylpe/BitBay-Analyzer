package pl.dimzi.cryptocurrencyanalyzer.bitbay.controller;

import pl.dimzi.cryptocurrencyanalyzer.Log;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.enums.TradeType;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.repository.Repository;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.service.ConnectionService;
import pl.dimzi.cryptocurrencyanalyzer.enums.Period;
import pl.dimzi.cryptocurrencyanalyzer.model.CurrencyData;

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
            long startTime = 1509410705;
            long stopTime = 1509669905;
            Log.d(this, "Getting trades of " + startTime + " to " + stopTime);
            //TODO DEBUG DOWNLOAD TRADES
            //tradeController.updateTradesUsingDate(TradeType.ETHPLN, startTime, stopTime);
            currencyDataController.updateCurrencyData(TradeType.ETHPLN, startTime, stopTime);
        }catch (SQLException e){
            Log.e(this, e.getMessage());
        }
    }

    public void refreshCurrencyData(Period period, TradeType type) throws SQLException{
        ArrayList<CurrencyData> data = repo.getCurrencyDataAll(type, period);
        currencyData.get(period).put(type, data);
        windowController.refreshCurrencyData(type, period, data);
    }

    public ArrayList<CurrencyData> getCurrencyData(TradeType type, Period period){
        return currencyData.get(period).get(type);
    }

    public JPanel getRootPanel(){
        return windowController.getRootPanel();
    }
}
