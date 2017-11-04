package pl.dimzi.cryptocurrencyanalyzer.bitbay.controller;

import pl.dimzi.cryptocurrencyanalyzer.DatabaseConnection;
import pl.dimzi.cryptocurrencyanalyzer.Log;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.controller.data.CurrencyDataController;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.controller.data.TradeController;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.controller.gui.WindowController;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.controller.manager.ManagerController;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.enums.TradeType;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.repository.Repository;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.service.ConnectionService;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.view.manager.ManagerPanel;
import pl.dimzi.cryptocurrencyanalyzer.enums.Period;
import pl.dimzi.cryptocurrencyanalyzer.model.CurrencyData;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;

public enum BitBayController implements ActionListener{
    INSTANCE;

    private final String DB_URL = "jdbc:sqlite:bitbay.db";

    private Repository repo;
    private ConnectionService connectionService;

    private TradeController tradeController;
    private CurrencyDataController currencyDataController;
    private WindowController windowController;
    private ManagerController managerController;

    BitBayController() {
        managerController = new ManagerController(this);
    }

    public void start(){
        try {
            repo = new Repository(DatabaseConnection.getConn(DB_URL));
            connectionService = new ConnectionService();

            tradeController = new TradeController(repo, connectionService);
            currencyDataController = new CurrencyDataController(repo);
            windowController = new WindowController();
        }catch (SQLException e){
            Log.e(this, e.getMessage());
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
        windowController.refreshCurrencyData(type, period, data);
    }

    public JPanel getWindowRootPanel(){
        return windowController.getRootPanel();
    }

    public JPanel getManagerRootPanel(){
        return managerController.getRootPanel();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
