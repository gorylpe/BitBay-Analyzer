package pl.dimzi.cryptocurrencyanalyzer.bitbay.view;

import pl.dimzi.cryptocurrencyanalyzer.Log;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.enums.TradeType;
import pl.dimzi.cryptocurrencyanalyzer.enums.Period;
import pl.dimzi.cryptocurrencyanalyzer.model.CurrencyData;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

public class PlotPanel extends JPanel{

    private ArrayList<CurrencyData> visibleData;
    private TradeType tradeType;
    private Period period;

    private long dateStart;
    private long dateEnd;

    private boolean repainting;

    private final Font dateFont = new Font("Arial", Font.ITALIC, 10);

    private final Color backgroundColor = Color.WHITE;
    private final Color helpLineColor = new Color(0, 0, 0, 64);
    private final Color averagesColor = new Color(48, 63, 159);

    private final Color volumeColor = new Color(66, 165, 255, 64);

    private final Color candleIncreaseColor = new Color(67, 160, 71);
    private final Color candleDecreaseColor = new Color(198, 40, 40);

    /**
     * Initializes default values of plot panel.
     */
    public PlotPanel() {
        super();
    }

    /**
     * Sets data used to plotting.
     */
    public void changeDataType(TradeType tradeType, Period period) {
        Log.d(this, "Change data type");
        this.tradeType = tradeType;
        this.period = period;
    }

    public void setData(ArrayList<CurrencyData> visibleData, long dateStart, long dateEnd){
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.visibleData = visibleData;
        repaint();
    }

    public void setRepainting(boolean repainting){
        this.repainting = repainting;
    }

    public boolean getRepainting(){
        return repainting;
    }

    /**
     * Paints whole plot
     * @param g Graphics on which plot is drawn.
     */
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        long time = System.nanoTime();

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if(visibleData != null)
            drawCandles(g2d);

        repainting = false;
    }

    private void drawCandles(Graphics2D g2d){
        g2d.setStroke(new BasicStroke(1.5f));

        if(visibleData.size() > 0){
            long time = System.nanoTime();
            double valueMax = visibleData.stream().max(Comparator.comparingDouble(CurrencyData::getMaximum)).get().getMaximum();
            double valueMin = visibleData.stream().min(Comparator.comparingDouble(CurrencyData::getMinimum)).get().getMinimum();

            double xnum = (dateEnd - dateStart) / period.getPeriodLength();
            double dx = getWidth() / xnum;

            final int topBottomPadding = 10;
            double yscale = (getHeight() - 2*topBottomPadding) / (valueMax - valueMin);

            time = System.nanoTime() - time;
            Log.d(this, "Precalculations " + time + "ns");
            time = System.nanoTime();

            for(int i = 0; i < visibleData.size(); ++i){
                CurrencyData data = visibleData.get(i);

                //draw candle
                final boolean increase = data.getOpening() < data.getClosing();

                final int x = (int)Math.floor((1.0 * (data.getPeriodStart() - dateStart) / period.getPeriodLength() * dx));
                final int width = (int)dx;
                final int closingY = (int)((valueMax - data.getClosing()) * yscale) + topBottomPadding;
                final int openingY = (int)((valueMax - data.getOpening()) * yscale) + topBottomPadding;
                final int y = increase ? closingY : openingY;
                final int height = Math.max(Math.abs(closingY - openingY), 1);

                Color candleColor = increase ? candleIncreaseColor : candleDecreaseColor;

                g2d.setColor(candleColor);
                g2d.fillRect(x + width / 5, y, width * 3 / 5, height);

                final int minimumY = (int) ((valueMax - data.getMinimum()) * yscale) + topBottomPadding;
                final int maximumY = (int) ((valueMax - data.getMaximum()) * yscale) + topBottomPadding;
                //draw minmax
                g2d.setColor(candleColor);
                g2d.drawLine(x + width / 2 - 1, minimumY, x + width / 2 - 1, maximumY);
            }

            time = System.nanoTime() - time;
            Log.d(this, visibleData.size() + " visible data drawings " + time + "ns");
        }
    }
}
