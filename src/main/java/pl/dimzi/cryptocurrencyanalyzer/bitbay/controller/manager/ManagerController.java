package pl.dimzi.cryptocurrencyanalyzer.bitbay.controller.manager;

import pl.dimzi.cryptocurrencyanalyzer.bitbay.model.TradeBlock;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.view.manager.ManagerPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ManagerController implements ActionListener{

    private ActionListener actionListener;

    private ManagerPanel managerPanel;

    private TradeBlocksController tradeBlocksController;


    public ManagerController(ActionListener actionListener){
        this.actionListener = actionListener;

        managerPanel = new ManagerPanel();

        tradeBlocksController = new TradeBlocksController(managerPanel.getTradeBlocksPanel());
    }

    public void refreshTradeBlocks(ArrayList<TradeBlock> tradeBlocks){
        tradeBlocksController.refreshTradeBlocks(tradeBlocks);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //push action to upper controller
        actionListener.actionPerformed(e);
    }

    public JPanel getRootPanel() {
        return managerPanel;
    }
}
