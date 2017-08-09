package com.dimzi.cryptocurrencyanalyzer;

import com.dimzi.cryptocurrencyanalyzer.BitBay.BitBayManagerPanel;

import javax.swing.*;

public class ManagersFrame extends JFrame{

    BitBayManagerPanel bitBayManagerPanel;

    ManagersFrame(){
        super("Exchange managers");

        bitBayManagerPanel = new BitBayManagerPanel();
        add(bitBayManagerPanel.getMainPanel());

        pack();
        setVisible(false);
    }
}
