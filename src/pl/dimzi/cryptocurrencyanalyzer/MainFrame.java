package pl.dimzi.cryptocurrencyanalyzer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import pl.dimzi.cryptocurrencyanalyzer.*;

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

        pl.dimzi.cryptocurrencyanalyzer.bitbay.view.Window bitBayWindow = new pl.dimzi.cryptocurrencyanalyzer.bitbay.view.Window();
        add(bitBayWindow.getPanelMain(), BorderLayout.CENTER);

        setVisible(true);
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}