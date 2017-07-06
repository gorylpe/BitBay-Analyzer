import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {

        //DatabaseManager.startAutoUpdateThread(10000);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            final BitBayManager bitBayManager = new BitBayManager();

            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    MainFrame mainFrame = new MainFrame();
                    bitBayManager.attachObserver(mainFrame);
                    bitBayManager.updateCurrencyData(BitBayManager.TradeType.ETHPLN, ExchangeManager.CurrencyDataPeriodType.DAILY);
                    bitBayManager.notifyAllObservers();
                }
            });

        } catch(Exception e){
            e.printStackTrace();
        }
    }
}