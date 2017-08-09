package com.dimzi.cryptocurrencyanalyzer.BitBay;

import model.BitBayCurrencyData;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Piotr on 07.08.2017.
 */
public class BitBaySelectionPanel extends JPanel {
    private List<BitBayCurrencyData> data;
    private double valueRangeSize;
    private double valueMin;
    private double valueMax;

    private int rangeStart;
    private int rangeEnd;

    private final Color averagesColor = new Color(48, 63, 159);

    /**
     * Sets data used to plotting.
     *
     * @param data data array used to plotting.
     */
    public void setData(List<BitBayCurrencyData> data) {
        this.data = data;
        valueMin = data.stream().min(Comparator.comparing(BitBayCurrencyData::getMinimum)).get().getMinimum() * 0.95;
        valueMax = data.stream().max(Comparator.comparing(BitBayCurrencyData::getMaximum)).get().getMaximum() * 1.05;
        valueRangeSize = valueMax - valueMin;
    }

    public void setRange(int start, int end){
        this.rangeStart = start;
        this.rangeEnd = end;
    }

    /**
     * Paints whole plot
     *
     * @param g Graphics on which plot is drawn.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if(data != null && data.size() > 0) {
            //width of one fragment of data cell
            final double dx = (double) getWidth() / data.size();
            //value y scale to transform currency value to height on the screen
            final double yscale = getHeight() / valueRangeSize;

            g2d.setStroke(new BasicStroke(1.5f));

            int i = 0;
            BitBayCurrencyData currencyData = data.get(i);

            int lastx = (int) ((i + 0.5) * dx);
            double value = currencyData.getAverage();
            int lasty = (int) ((valueMax - value) * yscale);

            for (i = 1; i < data.size(); ++i) {
                currencyData = data.get(i);
                int x = (int) ((i + 0.5) * dx);
                value = currencyData.getAverage();
                int y = (int) ((valueMax - value) * yscale);
                //draw closing
                g2d.setColor(averagesColor);
                g2d.drawLine(lastx - 1, lasty, x - 1, y);

                lastx = x;
                lasty = y;
            }

            g2d.setColor(new Color(0, 255, 0, 64));
            g2d.fillRect((int)(rangeStart * dx), 0, (int)((rangeEnd - rangeStart) * dx), getHeight());
        }
    }
}
