package main.java.pl.dimzi.cryptocurrencyanalyzer.bitbay.controller;


import main.java.pl.dimzi.cryptocurrencyanalyzer.bitbay.enums.TradeType;
import main.java.pl.dimzi.cryptocurrencyanalyzer.bitbay.view.DetailsPanel;
import main.java.pl.dimzi.cryptocurrencyanalyzer.bitbay.view.Window;
import main.java.pl.dimzi.cryptocurrencyanalyzer.enums.Period;
import main.java.pl.dimzi.cryptocurrencyanalyzer.model.CurrencyData;

import javax.swing.*;
import java.util.ArrayList;

public class WindowController {

    private Window window;

    private PlotController plotController;
    private DetailsController detailsController;

    public WindowController(){
        window = new Window();

        plotController = new PlotController(window.getPlotPanel());
        detailsController = new DetailsController(window.getDetailsPanel());
    }

    public void refreshCurrencyData(TradeType tradeType, Period period, ArrayList<CurrencyData> currencyData) {
        plotController.refreshCurrencyData(tradeType, period, currencyData);
    }

    public JPanel getRootPanel(){
        return window.getRootPanel();
    }

    private class DetailsController{
        private DetailsPanel detailsPanel;

        private DetailsController(DetailsPanel detailsPanel){
            this.detailsPanel = detailsPanel;
        }
    }
}
