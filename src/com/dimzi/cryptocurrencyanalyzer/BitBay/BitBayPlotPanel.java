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

    private boolean isMouseOver;
    private int mouseX;
    private int mouseY;

    public enum PlotType{
        AVERAGES("Averages"),
        CLOSINGS("Closings"),
        JAPANESE("Japanese");

        String name;

        PlotType(String name){
            this.name = name;
        }

        @Override
        public String toString(){
            return name;
        }
    }

    private PlotType plotType;

    private final Font dateFont = new Font("Arial", Font.ITALIC, 10);
    private final Font closingFont = new Font("Arial", Font.BOLD, 15);
    private final Font otherDataFont = new Font("Arial", Font.PLAIN, 15);

    private final Color backgroundColor = new Color(197, 202, 233);
    private final Color simplePlotColor = new Color(48, 63, 159);
    private final Color underSimplePlotColor = new Color(63, 81, 181, 64);

    private final Color japaneseIncreaseColor = Color.GREEN;
    private final Color japaneseDecreaseColor = Color.RED;

    private final Color detailsBackgroundColor = new Color(255, 255, 255, 127);

    public BitBayPlotPanel(){
        super();
        isMouseOver = false;

        plotType = PlotType.AVERAGES;

        super.addMouseListener(this);
        super.addMouseMotionListener(this);
    }

    public void setData(List<BitBayCurrencyData> data) {
        this.data = data;
        valueMin = data.stream().min(Comparator.comparing(BitBayCurrencyData::getAverage)).get().getMinimum() * 0.95;
        valueMax = data.stream().max(Comparator.comparing(BitBayCurrencyData::getAverage)).get().getMaximum() * 1.05;
        valueRangeSize = valueMax - valueMin;
    }

    public void setType(PlotType type){
        plotType = type;
        repaint();
    }

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
            //y scale to transform currency value to height on the screen
            final double yscale = getHeight() / valueRangeSize;

            drawPlot(g2d, dx, yscale);

            if(isMouseOver){
                drawDetailedCurrencyDataInfo(g2d, dx, yscale);
            }
        }
    }

    private void drawPlot(Graphics2D g2d, final double dx, final double yscale){
        switch(plotType){
            case CLOSINGS:
                drawClosingsOrAverages(g2d, dx, yscale, true);
                break;
            case AVERAGES:
                drawClosingsOrAverages(g2d, dx, yscale, false);
                break;
            case JAPANESE:
                drawJapanese(g2d, dx, yscale);
                break;
        }
    }

    private void drawClosingsOrAverages(Graphics2D g2d, final double dx, final double yscale, final boolean closings){
        g2d.setStroke(new BasicStroke(1.5f));

        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        int i = 0;
        BitBayCurrencyData currencyData = data.get(i);

        int lastx = (int) ((i + 0.5) * dx);
        double value = closings ? currencyData.getClosing() : currencyData.getAverage();
        int lasty = (int) ((valueMax - value) * yscale);

        //draw minmax
        g2d.setColor(simplePlotColor);
        g2d.drawLine(lastx, (int) ((valueMax - currencyData.getMinimum()) * yscale), lastx, (int) ((valueMax - currencyData.getMaximum()) * yscale));

        for (i = 1; i < data.size(); ++i) {
            currencyData = data.get(i);
            int x = (int) ((i + 0.5) * dx);
            value = closings ? currencyData.getClosing() : currencyData.getAverage();
            int y = (int) ((valueMax - value) * yscale);
            //draw closing
            g2d.setColor(simplePlotColor);
            g2d.drawLine(lastx, lasty, x, y);
            //draw under closing
            g2d.setColor(underSimplePlotColor);
            int[] xPoints = {lastx, x, x, lastx};
            int[] yPoints = {lasty, y, getHeight(), getHeight()};
            int nPoints = 4;
            g2d.fillPolygon(xPoints, yPoints, nPoints);
            //draw minmax
            g2d.setColor(simplePlotColor);
            g2d.drawLine(x, (int) ((valueMax - currencyData.getMinimum()) * yscale), x, (int) ((valueMax - currencyData.getMaximum()) * yscale));

            lastx = x;
            lasty = y;
        }
    }

    private void drawJapanese(Graphics2D g2d, final double dx, final double yscale){
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

            Color candleColor = increase ? japaneseIncreaseColor : japaneseDecreaseColor;

            g2d.setColor(candleColor);
            g2d.fillRect(x + width / 5, y, width * 3 / 5, height);

            //draw minmax
            g2d.setColor(candleColor);
            g2d.drawLine(x + width / 2, (int) ((valueMax - currencyData.getMinimum()) * yscale), x + width / 2, (int) ((valueMax - currencyData.getMaximum()) * yscale));
        }
    }

    private void drawDetailedCurrencyDataInfo(Graphics2D g2d, double dx, double yscale) {
        final int width = 200;
        final int height = 100;

        int coveredDataIndex = mouseX * data.size() / getWidth();
        if(coveredDataIndex >= data.size())
            coveredDataIndex = data.size();
        if(coveredDataIndex < 0)
            coveredDataIndex = 0;

        BitBayCurrencyData currencyData = data.get(coveredDataIndex);

        final int currencyX = (int)((coveredDataIndex + 0.5) * dx);

        switch(plotType){
            case AVERAGES: {
                final int currencyAverageY = (int) ((valueMax - currencyData.getAverage()) * yscale);
                g2d.setColor(simplePlotColor);
                g2d.fillOval(currencyX - 3, currencyAverageY - 3, 7, 7);
                break;
            }
            case CLOSINGS:{
                final int currencyClosingY = (int)((valueMax - currencyData.getClosing()) * yscale);
                g2d.setColor(simplePlotColor);
                g2d.fillOval(currencyX - 3, currencyClosingY - 3, 7, 7);
                break;
            }
        }

        g2d.setColor(Color.GRAY);
        g2d.drawLine(currencyX, 0, currencyX, getHeight());

        int dialogX = mouseX - width * 11 / 10;
        int dialogY = mouseY - height / 2;

        if(dialogX < 0)                   dialogX = 0;
        if(dialogX + width > getWidth())  dialogX = getWidth() - width;
        if(dialogY < 0)                   dialogY = 0;
        if(dialogY + height > getHeight())dialogY = getHeight() - height;

        g2d.setColor(detailsBackgroundColor);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.fillRoundRect(dialogX, dialogY, width, height, 10, 10);

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
                dialogX + 10,
                dialogY + 15);

        g2d.setFont(closingFont);
        g2d.drawString(String.format("Closing: %.2f", currencyData.getClosing()), dialogX + 10, dialogY + 32);
        g2d.setFont(otherDataFont);
        g2d.drawString(String.format("Average: %.2f", currencyData.getAverage()), dialogX + 10, dialogY + 52);
        g2d.drawString(String.format("Opening: %.2f", currencyData.getOpening()), dialogX + 10, dialogY + 72);
        g2d.drawString(String.format("Volume: %.2f", currencyData.getVolume()), dialogX + 10, dialogY + 92);
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
