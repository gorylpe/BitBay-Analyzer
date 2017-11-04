package main.java.pl.dimzi.cryptocurrencyanalyzer;

import main.java.pl.dimzi.cryptocurrencyanalyzer.view.ManagerPanel;

import javax.swing.*;

public class ManagersFrame extends JFrame{

    ManagerPanel managerPanel;

    ManagersFrame(){
        super("Exchange managers");

        managerPanel = new ManagerPanel();
        add(managerPanel.getMainPanel());

        pack();
        setVisible(false);
    }
}
