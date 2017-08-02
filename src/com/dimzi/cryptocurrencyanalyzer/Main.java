package com.dimzi.cryptocurrencyanalyzer;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {

        //com.dimzi.cryptocurrencyanalyzer.DatabaseManager.startAutoUpdateThread(10000);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    MainFrame mainFrame = new MainFrame();
                }
            });

        } catch(Exception e){
            e.printStackTrace();
        }
    }
}