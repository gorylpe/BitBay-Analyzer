package pl.dimzi.cryptocurrencyanalyzer;

import pl.dimzi.cryptocurrencyanalyzer.bitbay.controller.BitBayController;

import javax.swing.*;

public class ManagersFrame extends JFrame{

    ManagersFrame(){
        super("Exchange managers");

        add(BitBayController.INSTANCE.getManagerRootPanel());

        pack();
        setVisible(false);
    }
}
