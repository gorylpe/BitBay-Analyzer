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


    }
}
