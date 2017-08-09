package com.dimzi.cryptocurrencyanalyzer.BitBay;

import model.BitBayCurrencyData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Comparator;
import java.util.List;

public class BitBayPlotPanel extends JPanel implements MouseListener, MouseMotionListener{

    private List<BitBayCurrencyData> data;
    private double valueRangeSize;
    private double valueMin;
    private double valueMax;
    private double volumeMin;
    private double volumeMax;
    private double volumeRangeSize;

    private boolean isMouseOver;
    private int mouseX;
    private int mouseY;

    private boolean isDrawingAverages;

    private final Font dateFont = new Font("Arial", Font.ITALIC, 10);
    private final Font valueFont = new Font("Arial", Font.BOLD, 12);
    private final Font textFont = new Font("Arial", Font.PLAIN, 12);

    private final Color backgroundColor = Color.WHITE;
    private final Color helpLineColor = new Color(0, 0, 0, 64);
    private final Color averagesColor = new Color(48, 63, 159);

    private final Color volumeColor = new Color(66, 165, 255, 64);

    private final Color candleIncreaseColor = new Color(67, 160, 71);
    private final Color candleDecreaseColor = new Color(198, 40, 40);

    private final Color detailsBackgroundColor = new Color(255, 255, 255, 192);

    /**
     * Initializes default values of plot panel.
     */
    public BitBayPlotPanel(){
        super();
        isMouseOver = false;

        isDrawingAverages = false;

        super.addMouseListener(this);
        super.addMouseMotionListener(this);
    }

    /**
     * Sets data used to plotting.
     * @param data data array used to plotting.
     */
    public void setData(List<BitBayCurrencyData> data) {
        this.data = data;
        valueMin = data.stream().min(Comparator.comparing(BitBayCurrencyData::getMinimum)).get().getMinimum() * 0.95;
        valueMax = data.stream().max(Comparator.comparing(BitBayCurrencyData::getMaximum)).get().getMaximum() * 1.05;
        valueRangeSize = valueMax - valueMin;

        volumeMin = data.stream().min(Comparator.comparing(BitBayCurrencyData::getVolume)).get().getVolume();
        volumeMax = data.stream().max(Comparator.comparing(BitBayCurrencyData::getVolume)).get().getVolume() * 1.25;
        volumeRangeSize = volumeMax - volumeMin;
    }


    /**
     * Sets boolean of average values should be drawn
     * @param drawing boolean of average values should be drawn
     */
    public void setDrawingAverages(boolean drawing){
        isDrawingAverages = drawing;
        repaint();
    }

    /**
     * Paints whole plot
     * @param g Graphics on which plot is drawn.
     */
    @Override
    public void paintComponent(Graphics g){
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

        if(data != null && data.size() > 0){
            //width of one fragment of data cell
            final double dx = (double)getWidth() / data.size();
            //value y scale to transform currency value to height on the screen
            final double valueyscale = getHeight() / valueRangeSize;
            //volume y scale to transform currency value to heigh on the screen
            final double volumeyscale = getHeight() / volumeRangeSize;

            g2d.setColor(backgroundColor);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            drawHelpLines(g2d);
            drawVolume(g2d, dx, volumeyscale);
            if(isDrawingAverages)
                drawAverages(g2d, dx, valueyscale);
            else
                drawCandles(g2d, dx, valueyscale);

            if(isMouseOver){
                drawDetailedCurrencyDataInfo(g2d, dx);
            }
        }
    }

    private void drawHelpLines(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(0.5f));
        g2d.setColor(helpLineColor);

        int xspace = 75;
        for(int i = xspace; i < getWidth(); i += xspace){
            g2d.drawLine(i, 0, i, getHeight());
        }

        int yspace = 50;
        for(int i = getHeight() - yspace; i > 0; i -= yspace){
            g2d.drawLine(0, i, getWidth(), i);
        }
    }

    private void drawAverages(Graphics2D g2d, final double dx, final double yscale){
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
    }

    private void drawCandles(Graphics2D g2d, final double dx, final double yscale){
        g2d.setStroke(new BasicStroke(1.5f));

        BitBayCurrencyData currencyData;
        for(int i = 0; i < data.size(); ++i){
            currencyData = data.get(i);

            //draw candle
            final boolean increase = currencyData.getOpening() < currencyData.getClosing();

            final int x = (int)(i * dx);
            final int width = (int)dx;
            final int closingY = (int)((valueMax - currencyData.getClosing()) * yscale);
            final int openingY = (int)((valueMax - currencyData.getOpening()) * yscale);
            final int y = increase ? closingY : openingY;
            final int height = Math.max(Math.abs(closingY - openingY), 1);

            Color candleColor = increase ? candleIncreaseColor : candleDecreaseColor;

            g2d.setColor(candleColor);
            g2d.fillRect(x + width / 5, y, width * 3 / 5, height);

            //draw minmax
            g2d.setColor(candleColor);
            g2d.drawLine(x + width / 2 - 1, (int) ((valueMax - currencyData.getMinimum()) * yscale), x + width / 2 - 1, (int) ((valueMax - currencyData.getMaximum()) * yscale));
        }
    }

    private void drawVolume(Graphics2D g2d, double dx, double yscale) {
        g2d.setStroke(new BasicStroke(1.5f));

        BitBayCurrencyData currencyData;
        for (int i = 0; i < data.size(); ++i) {
            currencyData = data.get(i);

            int x = (int)(i * dx);
            int width = (int)dx;
            final int y = (int)((volumeMax - currencyData.getVolume()) * yscale);
            final int height = (int)(currencyData.getVolume() * yscale);

            g2d.setColor(volumeColor);
            x += width / 10;
            width = width * 4 / 5;
            if(width <= 0) width = 1;
            g2d.fillRect(x, y, width, height);
        }
    }

    private void drawDetailedCurrencyDataInfo(Graphics2D g2d, double dx) {
        final int width = 160;
        final int height = 120;

        int coveredDataIndex = mouseX * data.size() / getWidth();
        if(coveredDataIndex >= data.size())
            coveredDataIndex = data.size() - 1;
        if(coveredDataIndex < 0)
            coveredDataIndex = 0;

        BitBayCurrencyData currencyData = data.get(coveredDataIndex);

        final int currencyX = (int)((coveredDataIndex + 0.5) * dx) - 1;

        g2d.setStroke(new BasicStroke(1.5f));
        g2d.setColor(Color.GRAY);
        g2d.drawLine(currencyX, 0, currencyX, getHeight());

        g2d.drawLine(0, mouseY, getWidth(), mouseY);
        double yvalueAtMousePos = valueRangeSize * (getHeight() - mouseY) / getHeight() + valueMin;
        double yvolumeAtMousePos = volumeRangeSize * (getHeight() - mouseY) / getHeight() + volumeMin;

        float horizontalLineFontSize = (getWidth() > getHeight() ? getHeight() / 25.0f : getWidth() / 25.0f);
        g2d.setFont(textFont.deriveFont(horizontalLineFontSize));

        String formattedYvalue = String.format("%.2f", yvalueAtMousePos);
        g2d.drawString(formattedYvalue, getWidth() - horizontalLineFontSize * formattedYvalue.length() * 2 / 3, mouseY);
        g2d.drawString(String.format("%.2f", yvolumeAtMousePos), 10, mouseY);

        //dialog drawing
        int dialogX = mouseX - width * 11 / 10;
        int dialogY = mouseY - height / 2;

        if(dialogX < 0)                   dialogX = 0;
        if(dialogX + width > getWidth())  dialogX = getWidth() - width;
        if(dialogY < 0)                   dialogY = 0;
        if(dialogY + height > getHeight())dialogY = getHeight() - height;

        g2d.setColor(detailsBackgroundColor);
        g2d.fillRoundRect(dialogX, dialogY, width, height, 10, 10);
        g2d.setColor(Color.GRAY);
        g2d.drawRoundRect(dialogX, dialogY, width, height, 10, 10);

        String dayOfWeek = currencyData.getPeriodStart().toLocalDate().getDayOfWeek().name();
        dayOfWeek = dayOfWeek.substring(0, 1).toUpperCase() + dayOfWeek.substring(1).toLowerCase();
        final String localDate = currencyData.getPeriodStart().toLocalDate().toString();
        final String localTime = currencyData.getPeriodStart().toLocalTime().toString();

        g2d.setColor(Color.BLACK);
        g2d.setFont(dateFont);
        g2d.drawString(String.format("%s, %s  %s",
                dayOfWeek,
                localDate,
                localTime),
                dialogX + 5,
                dialogY + 15);

        g2d.setFont(textFont);
        g2d.drawString("Opening", dialogX + width / 12, dialogY + height / 4);
        g2d.drawString("Closing", dialogX + width * 7 / 12, dialogY + height / 4);
        g2d.drawString("Minimum", dialogX + width / 12, dialogY + height * 2 / 4);
        g2d.drawString("Maximum", dialogX + width * 7 / 12, dialogY + height * 2 / 4);
        g2d.drawString("Volume", dialogX + width * 4 / 12, dialogY + height * 3 / 4);
        g2d.setFont(valueFont);
        g2d.drawString(String.format("%.2f", currencyData.getOpening()), dialogX + width / 12, dialogY + height * 3 / 8);
        g2d.drawString(String.format("%.2f", currencyData.getClosing()), dialogX + width * 7 / 12, dialogY + height * 3 / 8);
        g2d.drawString(String.format("%.2f", currencyData.getMinimum()), dialogX + width / 12, dialogY + height * 5 / 8);
        g2d.drawString(String.format("%.2f", currencyData.getMaximum()), dialogX + width * 7 / 12, dialogY + height * 5 / 8);
        g2d.drawString(String.format("%.2f", currencyData.getVolume()), dialogX + width * 4 / 12, dialogY + height * 7 / 8);
        /*g2d.drawString(String.format("Closing: %.2f", currencyData.getClosing()), dialogX + 10, dialogY + 32);
        g2d.drawString(String.format("Average: %.2f", currencyData.getAverage()), dialogX + 10, dialogY + 52);
        g2d.drawString(String.format("Volume: %.2f", currencyData.getVolume()), dialogX + 10, dialogY + 92);*/
    }

    private void setMousePosition(MouseEvent e){
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {
        isMouseOver = true;
        setMousePosition(e);
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        isMouseOver = false;
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        isMouseOver = true;
        setMousePosition(e);
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        isMouseOver = true;
        setMousePosition(e);
        repaint();
    }
}
