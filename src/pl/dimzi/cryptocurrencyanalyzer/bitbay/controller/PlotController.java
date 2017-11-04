package pl.dimzi.cryptocurrencyanalyzer.bitbay.controller;

import pl.dimzi.cryptocurrencyanalyzer.Log;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.enums.TradeType;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.view.PlotPanel;
import pl.dimzi.cryptocurrencyanalyzer.enums.Period;
import pl.dimzi.cryptocurrencyanalyzer.model.CurrencyData;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

public class PlotController extends MouseAdapter {
    private PlotPanel plot;

    private Period currentPeriod;

    private long dateStart;
    private long dateEnd;
    private int dateRange;

    private final int dataRangeMin = 10;
    private final int dataRangeMax = 100;
    private final double dataRangePercentChangeOnZoom = 1.2;

    private final double partOfPeriodToDragAtOnePxDrag = 1.0 / 500;

    private Point mouseLastPress;
    private Point mouseLastPosition;

    PlotController(PlotPanel plot){
        this.plot = plot;
        plot.addMouseListener(this);
        plot.addMouseWheelListener(this);
        plot.addMouseMotionListener(this);
    }

    public void refreshCurrencyData(TradeType tradeType, Period period, ArrayList<CurrencyData> currencyData) {
        plot.refreshCurrencyData(tradeType, period, currencyData);
        currentPeriod = period;

        //TODO DEBUG VAL
        setDateStart(1509408000);
        setDateRange(30);
    }

    public void drag(double dx){
        long dateShift = (long)(dx * partOfPeriodToDragAtOnePxDrag * dateRange * currentPeriod.getPeriodLength());
        setDateStart(dateStart + dateShift);
    }

    public void zoom(int scrollAmount) {
        int newDataRange = (int)(dateRange * Math.pow(dataRangePercentChangeOnZoom, (double)scrollAmount));
        if(newDataRange < dataRangeMin)
            newDataRange = dataRangeMin;
        if(newDataRange > dataRangeMax)
            newDataRange = dataRangeMax;
        Log.d(this, "dr " + newDataRange);
        setDateRange(newDataRange);
    }

    private void setDateStart(long dateStart){
        this.dateStart = dateStart;
        recalculateDateEnd();
        plot.setNewDateRange(dateStart, dateEnd);
    }

    public void setDateRange(int dateRange) {
        this.dateRange = dateRange;
        recalculateDateEnd();
        plot.setNewDateRange(dateStart, dateEnd);
    }

    private void recalculateDateEnd() {
        dateEnd = currentPeriod.addPeriod(dateStart, dateRange);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        mouseLastPosition = e.getPoint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseLastPosition = e.getPoint();
        mouseLastPress = e.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseLastPosition = e.getPoint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        mouseLastPosition = e.getPoint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mouseLastPosition = e.getPoint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseLastPosition = e.getPoint();

        Point dragTo = e.getPoint();
        double dx = mouseLastPress.getX() - dragTo.getX();
        mouseLastPress = dragTo;
        drag(dx);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseLastPosition = e.getPoint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e){
        zoom(e.getWheelRotation());
    }
}