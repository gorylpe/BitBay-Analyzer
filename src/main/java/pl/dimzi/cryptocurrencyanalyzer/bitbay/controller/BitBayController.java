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

    }

    public void start(){
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
        Log.d(this, "Started");
    }

    public void stop(){
        try {
            repo.stop(); repo = null;
            connectionService = null;

            tradeController = null;
            currencyDataController = null;

            windowController.stop(); windowController = null;
        }catch (SQLException e){
            Log.e(this, e.getMessage());
        }
    }

    public void refreshCurrencyData(Period period, TradeType type) throws SQLException{
        ArrayList<CurrencyData> data = repo.getCurrencyDataAll(type, period);
        currencyData.get(period).put(type, data);
        windowController.refreshCurrencyData(type, period, data);
    }

    public JPanel getRootPanel(){
        return windowController.getRootPanel();
    }
}
