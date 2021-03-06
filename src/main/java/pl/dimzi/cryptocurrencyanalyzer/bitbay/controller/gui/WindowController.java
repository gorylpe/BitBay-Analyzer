package pl.dimzi.cryptocurrencyanalyzer.bitbay.controller.gui;


import pl.dimzi.cryptocurrencyanalyzer.bitbay.enums.TradeType;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.view.gui.DetailsPanel;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.view.gui.Window;
import pl.dimzi.cryptocurrencyanalyzer.enums.Period;
import pl.dimzi.cryptocurrencyanalyzer.model.CurrencyData;

import javax.swing.*;
import java.util.ArrayList;

public class WindowController {

    private Window window;

    private PlotController plotController;
    private DetailsController detailsController;

    public WindowController(){
        window = new Window();

        plotController = new PlotController(window.getPlotPanel());
        plotController.start();
        detailsController = new DetailsController(window.getDetailsPanel());
    }

    public void stop(){
        plotController.interrupt();
    }

    public void refreshCurrencyData(TradeType tradeType, Period period, ArrayList<CurrencyData> currencyData) {
        plotController.changeCurrencyData(tradeType, period, currencyData);
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
