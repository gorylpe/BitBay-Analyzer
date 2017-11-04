package pl.dimzi.cryptocurrencyanalyzer;

import pl.dimzi.cryptocurrencyanalyzer.bitbay.controller.BitBayController;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.enums.TradeType;
import pl.dimzi.cryptocurrencyanalyzer.enums.Period;

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
                    try {
                        BitBayController.INSTANCE.start();
                        BitBayController.INSTANCE.refreshCurrencyData(Period.HOURLY, TradeType.ETHPLN);

                        MainFrame mainFrame = new MainFrame();
                    }catch (Exception e){}
                }
            });

        } catch(Exception e){
            e.printStackTrace();
        }
    }
}