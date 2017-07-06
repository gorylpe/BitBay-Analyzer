import model.BitBayCurrencyData;

import javax.swing.*;
import javax.swing.border.Border;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class MainFrame extends JFrame implements BitBayCurrencyObserver {

    PlotPanel plotPanel;

    public MainFrame(){
        super("BitBay Analyzer");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel contentPanel = new JPanel();
        Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        contentPanel.setBorder(padding);
        setContentPane(contentPanel);

        plotPanel = new PlotPanel();

        add(plotPanel);
        setVisible(true);
        pack();
    }

    @Override
    public void update(BitBayManager manager) {
        System.out.println("MainFrame updating");
        ArrayList<BitBayCurrencyData> currencyDataArray = manager.getCurrencyDataFromPeriod(
                BitBayManager.TradeType.ETHPLN,
                LocalDateTime.of(2017, 4, 1, 0, 0, 0),
                LocalDateTime.of(2017, 6, 27, 0, 0, 0),
                ExchangeManager.CurrencyDataPeriodType.DAILY);

        plotPanel.setData(currencyDataArray);
        plotPanel.repaint();
    }
}
