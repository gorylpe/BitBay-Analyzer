import model.BitBayCurrencyData;
import model.BitBayTradeJSON;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

public class PlotPanel extends JPanel {

    ArrayList<BitBayCurrencyData> data;

    public PlotPanel(){
        super();
        setPreferredSize(new Dimension(1280, 720));
    }

    public void setData(ArrayList<BitBayCurrencyData> data) {
        this.data = data;
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.WHITE);
        g2d.drawRect(0, 0, getWidth(), getHeight());
        if(data != null && data.size() > 0){
            double y0 = data.stream().min(Comparator.comparing(BitBayCurrencyData::getAverage)).get().getAverage() * 0.9;
            double y1 = data.stream().max(Comparator.comparing(BitBayCurrencyData::getAverage)).get().getAverage() * 1.1;

            double width = data.size();
            double height = y1 - y0;

            System.out.println(data.size() + " " + height);

            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));

            g2d.drawString(Double.toString(y1), 10, 10);
            g2d.drawString(Double.toString(y0), 10, getHeight() - 30);

            g2d.drawString(data.get(0).getPeriodStart().toString(), 10, getHeight() - 10);
            g2d.drawString(data.get(data.size() - 1).getPeriodStart().toString(), getWidth() - 100, getHeight() - 10);

            int lastx = 0;
            int lastAverageY = (int)((y1 - data.get(0).getAverage()) * getHeight() / height);

            final double xscale = getWidth() / width;
            final double yscale = getHeight() / height;

            for(int i = 1; i < data.size(); ++i){
                BitBayCurrencyData currencyData = data.get(i);
                int x = (int)(i * xscale);
                int averageY = (int)((y1 - currencyData.getAverage()) * yscale);

                g2d.setColor(Color.BLACK);
                g2d.drawLine(lastx, lastAverageY, x, averageY);
                g2d.setColor(Color.GRAY);
                g2d.drawLine(x, (int)((y1 - currencyData.getMinimum()) * yscale), x, (int)((y1 - currencyData.getMaximum()) * yscale));

                lastx = x;
                lastAverageY = averageY;
            }
        }
    }
}
