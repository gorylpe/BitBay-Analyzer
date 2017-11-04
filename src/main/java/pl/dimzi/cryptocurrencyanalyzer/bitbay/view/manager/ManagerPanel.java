package pl.dimzi.cryptocurrencyanalyzer.bitbay.view.manager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ManagerPanel extends JPanel{
    JLabel logo;
    TradeBlocksPanel tradeBlocksPanel;

    public ManagerPanel(){
        super();

        setPreferredSize(new Dimension(600, 200));

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        logo = new JLabel("BitBay");
        logo.setPreferredSize(new Dimension(200 ,200));
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        add(logo, c);

        tradeBlocksPanel = new TradeBlocksPanel();
        tradeBlocksPanel.setPreferredSize(new Dimension(400, 200));
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;

        add(tradeBlocksPanel, c);
    }

    public TradeBlocksPanel getTradeBlocksPanel() {
        return tradeBlocksPanel;
    }
}
