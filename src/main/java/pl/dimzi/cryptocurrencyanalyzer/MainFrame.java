package main.java.pl.dimzi.cryptocurrencyanalyzer;

import main.java.pl.dimzi.cryptocurrencyanalyzer.bitbay.controller.BitBayController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame implements ActionListener{

    ManagersFrame managersFrame;

    public MainFrame(){
        super("BitBay Analyzer");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        managersFrame = new ManagersFrame();

        setLayout(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();

        JMenu mainMenu = new JMenu("Main");
        menuBar.add(mainMenu);

        JMenuItem managersButton = new JMenuItem("Managers");
        managersButton.addActionListener((ActionEvent e) -> {
            if(managersFrame != null){
                managersFrame.setVisible(true);
            }
        });
        mainMenu.add(managersButton);
        JMenuItem exitButton = new JMenuItem("Exit");
        exitButton.addActionListener((ActionEvent e) -> {
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });
        mainMenu.add(exitButton);

        JMenu viewsMenu = new JMenu("Views");
        menuBar.add(viewsMenu);
        add(menuBar, BorderLayout.PAGE_START);

        add(BitBayController.INSTANCE.getRootPanel(), BorderLayout.CENTER);

        setVisible(true);
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
