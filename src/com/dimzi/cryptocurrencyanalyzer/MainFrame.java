package com.dimzi.cryptocurrencyanalyzer;

import com.dimzi.cryptocurrencyanalyzer.BitBay.BitBayWindow;
import model.BitBayCurrencyData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.SQLException;
import java.util.ArrayList;

public class MainFrame extends JFrame {

    public MainFrame(){
        super("BitBay Analyzer");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        try {
            BitBayWindow bitBayWindow = new BitBayWindow();
            setContentPane(bitBayWindow.getPanelMain());
        } catch (SQLException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error creating bitbaywindow");
        }

        setVisible(true);
        pack();
    }
}
