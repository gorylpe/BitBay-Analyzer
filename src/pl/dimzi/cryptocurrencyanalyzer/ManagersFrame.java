package pl.dimzi.cryptocurrencyanalyzer;

import pl.dimzi.cryptocurrencyanalyzer.bitbay.view.ManagerPanel;

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
