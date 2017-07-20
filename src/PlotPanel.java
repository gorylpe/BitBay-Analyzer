import model.BitBayCurrencyData;
import model.BitBayTradeJSON;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlotPanel extends JPanel implements MouseListener, MouseMotionListener{

    private List<BitBayCurrencyData> data;

    public PlotPanel(){
        super();
    }

    public void setData(List<BitBayCurrencyData> data) {
        this.data = data;
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.WHITE);
        g2d.drawRect(0, 0, getWidth(), getHeight());
        if(data != null && data.size() > 0){
            double ymin = data.stream().min(Comparator.comparing(BitBayCurrencyData::getAverage)).get().getMinimum() * 0.95;
            double ymax = data.stream().max(Comparator.comparing(BitBayCurrencyData::getAverage)).get().getMaximum() * 1.05;

            double width = data.size();
            double height = ymax - ymin;

            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1));

            final double dx = getWidth() / width;
            final double dy = getHeight() / height;

            BitBayCurrencyData currencyData = data.get(0);
            int lastx = (int)(0.5 * dx);
            int lasty = (int)((ymax - currencyData.getAverage()) * dy);
            g2d.drawLine(lastx, (int)((ymax - currencyData.getMinimum()) * dy), lastx, (int)((ymax - currencyData.getMaximum()) * dy));

            for(int i = 1; i < data.size(); ++i){
                currencyData = data.get(i);
                int x = (int)((i + 0.5) * dx);
                int y = (int)((ymax - currencyData.getAverage()) * dy);

                g2d.setColor(Color.BLACK);
                g2d.drawLine(lastx, lasty, x, y);
                g2d.setColor(Color.GRAY);
                g2d.drawLine(x, (int)((ymax - currencyData.getMinimum()) * dy), x, (int)((ymax - currencyData.getMaximum()) * dy));

                lastx = x;
                lasty = y;
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
