package pl.dimzi.cryptocurrencyanalyzer.bitbay.view;

import pl.dimzi.cryptocurrencyanalyzer.Log;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.enums.TradeType;
import pl.dimzi.cryptocurrencyanalyzer.enums.Period;
import pl.dimzi.cryptocurrencyanalyzer.model.CurrencyData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class PlotPanel extends JPanel{

    private ArrayList<CurrencyData> currencyData;
    private TradeType tradeType;
    private Period period;

    private long dateStart;
    private long dateEnd;
    private int dateRange;

    private final Font dateFont = new Font("Arial", Font.ITALIC, 10);
    private final Font valueFont = new Font("Arial", Font.BOLD, 12);
    private final Font textFont = new Font("Arial", Font.PLAIN, 12);

    private final Color backgroundColor = Color.WHITE;
    private final Color helpLineColor = new Color(0, 0, 0, 64);
    private final Color averagesColor = new Color(48, 63, 159);

    private final Color volumeColor = new Color(66, 165, 255, 64);

    private final Color candleIncreaseColor = new Color(67, 160, 71);
    private final Color candleDecreaseColor = new Color(198, 40, 40);

    /**
     * Initializes default values of plot panel.
     */
    public PlotPanel(){
        super();

        dateRange = 30;
        //TODO DEBUG VAL
        dateStart = 1509410705;
    }

    /**
     * Sets data used to plotting.
     * @param currencyData data array used to plotting.
     */
    public void refreshCurrencyData(TradeType tradeType, Period period, ArrayList<CurrencyData> currencyData) {
        Log.d(this, "Refreshing currency data");
        this.tradeType = tradeType;
        this.period = period;
        this.currencyData = currencyData;

        dateEnd = period.addPeriod(dateStart, dateRange);

        Log.d(this, "New dates, start " + dateStart + " end " + dateEnd + " elements " + currencyData.size());

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

        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if(currencyData != null)
            drawCandles(g2d);
    }

    private void drawCandles(Graphics2D g2d){
        g2d.setStroke(new BasicStroke(1.5f));

        List<CurrencyData> visible = new ArrayList<>();

        for(int i = 0; i < currencyData.size(); ++i){
            CurrencyData data = currencyData.get(i);
            if(data.getPeriodStart() > dateStart && data.getPeriodStart() < dateEnd){
                visible.add(data);
            }
        }

        double valueMax = visible.stream().max(Comparator.comparingDouble(CurrencyData::getMaximum)).get().getMaximum();
        double valueMin = visible.stream().max(Comparator.comparingDouble(CurrencyData::getMinimum)).get().getMinimum();

        double xnum = (dateEnd - dateStart) / period.getPeriodLength();
        double dx = getWidth() / xnum;

        double yscale = getHeight() / (valueMax - valueMin);

        for(int i = 0; i < currencyData.size(); ++i){
            CurrencyData data = currencyData.get(i);

            //draw candle
            final boolean increase = data.getOpening() < data.getClosing();

            final int x = (int)(i * dx);
            final int width = (int)dx;
            final int closingY = (int)((valueMax - data.getClosing()) * yscale);
            final int openingY = (int)((valueMax - data.getOpening()) * yscale);
            final int y = increase ? closingY : openingY;
            final int height = Math.max(Math.abs(closingY - openingY), 1);

            Color candleColor = increase ? candleIncreaseColor : candleDecreaseColor;

            g2d.setColor(candleColor);
            g2d.fillRect(x + width / 5, y, width * 3 / 5, height);

            //draw minmax
            g2d.setColor(candleColor);
            g2d.drawLine(x + width / 2 - 1, (int) ((valueMax - data.getMinimum()) * yscale), x + width / 2 - 1, (int) ((valueMax - data.getMaximum()) * yscale));
        }
    }
}
