package com.dimzi.cryptocurrencyanalyzer;

import com.dimzi.cryptocurrencyanalyzer.BitBay.BitBayManager;
import com.dimzi.cryptocurrencyanalyzer.BitBay.BitBayWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class MainFrame extends JFrame implements ActionListener{

    public MainFrame(){
        super("BitBay Analyzer");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();

        JMenuItem managersButton = new JMenuItem("Managers");
        managersButton.addActionListener((ActionEvent e) -> {

        });

        JMenu viewsMenu = new JMenu("Views");

        BitBayWindow bitBayWindow = new BitBayWindow();
        setContentPane(bitBayWindow.getPanelMain());

        setVisible(true);
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
