import model.TradeJSON;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

public class PlotPanel extends JPanel {

    ArrayList<TradeJSON> data;

    public PlotPanel(){
        super();
        setPreferredSize(new Dimension(1280, 720));
    }

    public void setData(ArrayList<TradeJSON> data) {
        this.data = data;
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.WHITE);
        g2d.drawRect(0, 0, getWidth(), getHeight());
        if(data != null && data.size() > 0){
            long x0 = data.get(0).getDate();
            long x1 = data.get(data.size() - 1).getDate();
            double y0 = data.stream().min(Comparator.comparing(TradeJSON::getPrice)).get().getPrice();
            double y1 = data.stream().max(Comparator.comparing(TradeJSON::getPrice)).get().getPrice();

            double width = (double)(x1 - x0);
            double height = y1 - y0;

            System.out.println(width + " " + height);

            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));

            g2d.drawString(Double.toString(y1), 10, 10);
            g2d.drawString(Double.toString(y0), 10, getHeight() - 30);

            DateFormat formatter = new SimpleDateFormat("HH:mm:ss");

            g2d.drawString(formatter.format(new Date(x0 * 1000)), 10, getHeight() - 10);
            g2d.drawString(formatter.format(new Date(x1 * 1000)), getWidth() - 50, getHeight() - 10);

            int lastx = (int)((data.get(0).getDate() - x0) * getWidth() / width);
            int lasty = (int)((y1 - data.get(0).getPrice()) * getHeight() / height);
            for(int i = 1; i < data.size(); ++i){
                TradeJSON trade = data.get(i);
                int x = (int)((trade.getDate() - x0) * getWidth() / width);
                int y = (int)((y1 - trade.getPrice()) * getHeight() / height);
                System.out.println(x + " " + y);

                g2d.drawLine(lastx, lasty, x, y);

                lastx = x;
                lasty = y;
            }
        }
    }
}
