package main.pl.dimzi.cryptocurrencyanalyzer;

import main.pl.dimzi.cryptocurrencyanalyzer.bitbay.controller.BitBayController;
import main.pl.dimzi.cryptocurrencyanalyzer.bitbay.enums.TradeType;
import main.pl.dimzi.cryptocurrencyanalyzer.enums.Period;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        Log.enableDebug(true);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {

                    MainFrame mainFrame = new MainFrame();

                    try {
                        BitBayController.INSTANCE.refreshCurrencyData(Period.HOURLY, TradeType.ETHPLN);
                    }catch (Exception e){}
                }
            });

        } catch(Exception e){
            e.printStackTrace();
        }
    }
}