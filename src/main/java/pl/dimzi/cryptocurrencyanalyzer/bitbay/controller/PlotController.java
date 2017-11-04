package pl.dimzi.cryptocurrencyanalyzer.bitbay.controller;

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

    private ArrayList<CurrencyData> currencyData;
    private Period period;

    private ArrayList<CurrencyData> visibleData;

    private long dateStart;
    private long dateEnd;
    private double dateRange;

    private final double dataRangeMin = 10;
    private final double dataRangeMax = 500;
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
        this.currencyData = currencyData;
        this.period = period;

        //TODO DEBUG VAL
        setDateStart(1508008000);
        setDateRange(30);

        plot.refreshVisibleCurrencyData(tradeType, period, visibleData);
        plot.setDateRange(dateStart, dateEnd);
    }

    private void drag(double dx){
        long dateShift = (long)(dx * partOfPeriodToDragAtOnePxDrag * dateRange * period.getPeriodLength());
        setDateStart(dateStart + dateShift);
        plot.setDateRange(dateStart, dateEnd);
        plot.setVisibleData(visibleData);
    }

    private void zoom(int scrollAmount) {
        final long dateUnderMouse = getAboveWhichDateIsMouse();

        double newDataRange = dateRange * Math.pow(dataRangePercentChangeOnZoom, (double)scrollAmount);
        if(newDataRange < dataRangeMin)
            newDataRange = dataRangeMin;
        if(newDataRange > dataRangeMax)
            newDataRange = dataRangeMax;
        setDateRange(newDataRange);

        final long newDateUnderMouse = getAboveWhichDateIsMouse();
        final long deltaDate = dateUnderMouse - newDateUnderMouse;

        setDateStart(dateStart + deltaDate);

        plot.setDateRange(dateStart, dateEnd);
        plot.setVisibleData(visibleData);
    }

    private void setDateStart(long dateStart){
        this.dateStart = dateStart;
        updateDateEndAndVisibleCurrencyData();
    }

    private void setDateRange(double dateRange) {
        this.dateRange = dateRange;
        updateDateEndAndVisibleCurrencyData();
    }

    private void updateDateEndAndVisibleCurrencyData(){
        recalculateDateEnd();
        recalculateVisibleCurrencyData();
    }

    private void recalculateDateEnd() {
        dateEnd = period.addPeriod(dateStart, dateRange);
    }

    private void recalculateVisibleCurrencyData(){
        this.visibleData = new ArrayList<>();

        final long visibleDateStart = dateStart - period.getPeriodLength();
        final long visibleDateEnd = dateEnd + period.getPeriodLength();
        for(int i = 0; i < currencyData.size(); ++i){
            CurrencyData data = currencyData.get(i);
            if(data.getPeriodStart() > visibleDateStart && data.getPeriodStart() < visibleDateEnd){
                visibleData.add(data);
            }
        }
    }

    private int getAboveWhichIndexIsMouse(){
        final double dx = plot.getWidth() / dateRange;
        final double firstDxStart = - (dateStart % period.getPeriodLength()) * 1.0 / (dateEnd - dateStart) * plot.getWidth();
        final double firstDxEnd = dx + firstDxStart;

        double mouseX = mouseLastPosition.getX();

        if(mouseX < firstDxEnd)
            return 0;

        mouseX -= firstDxEnd;
        return (int)(mouseX / dx) + 1;
    }

    private long getAboveWhichDateIsMouse(){
        return (long)((mouseLastPosition.getX() * 1.0 / plot.getWidth()) * (dateEnd - dateStart)) + dateStart;
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