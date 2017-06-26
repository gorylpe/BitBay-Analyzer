import java.awt.*;

public class Main {

    public static void main(String[] args) {
        if(!DatabaseManager.startConnection())
            return;

        if(!DatabaseManager.initializeDatabase())
            return;

        DatabaseManager.updateTradesTable();

        //DatabaseManager.startAutoUpdateThread(10000);

        AnalysisManager.initializeDays();

        //EventQueue.invokeLater(MainFrame::new);
    }
}