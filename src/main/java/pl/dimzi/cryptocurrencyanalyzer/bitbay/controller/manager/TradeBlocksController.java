package pl.dimzi.cryptocurrencyanalyzer.bitbay.controller.manager;

import pl.dimzi.cryptocurrencyanalyzer.Log;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.model.TradeBlock;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.view.manager.TradeBlocksPanel;
import pl.dimzi.cryptocurrencyanalyzer.enums.Period;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class TradeBlocksController extends Thread implements MouseListener, MouseMotionListener, MouseWheelListener {
    //TODO MERGE THIS AND PLOT CONTROLLER INTO PLOTTING LIBRARY

    private TradeBlocksPanel tradeBlocksPanel;

    private final int frameLengthInMs = 50;
    private boolean needsRepaint;
    private boolean needsUpdateVisibleData;
    private int framesLost = 0;

    private ArrayList<TradeBlock> data;
    private ArrayList<TradeBlock> visibleData;

    private Period period;

    private long dateStart;
    private long dateEnd;
    private double dateRange;

    private final double dataRangeMin = 1;
    private final double dataRangeMax = 500;
    private final double dataRangePercentChangeOnZoom = 1.25;

    private Point mouseLastPress;
    private Point mouseLastPosition;

    public TradeBlocksController(TradeBlocksPanel tradeBlocksPanel) {
        this.tradeBlocksPanel = tradeBlocksPanel;

        //watch in daily scale
        period = Period.DAILY;
    }

    @Override
    public void run(){
        while(!Thread.currentThread().isInterrupted()){
            long time = System.currentTimeMillis();
            if(needsUpdateVisibleData){
                recalculateVisibleTradeBlocks();
                needsUpdateVisibleData = false;
            }
            if(needsRepaint){
                if(tradeBlocksPanel.getRepainting()){
                    ++framesLost;
                    Log.d(this, "Choke, frames lost: " + framesLost);
                } else {
                    tradeBlocksPanel.setData(visibleData, dateStart, dateEnd);
                    tradeBlocksPanel.setRepainting(true);
                    needsRepaint = false;

                    framesLost = 0;
                }
            }
            try{
                long sleepTime = frameLengthInMs - (System.currentTimeMillis() - time);
                if(sleepTime > 0) Thread.sleep(sleepTime);
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void refreshTradeBlocks(ArrayList<TradeBlock> data) {
        this.data = data;
    }

    private void drag(double dx){
        //move date as same percent as screen x
        long dateShift = (long)(dx * 1.0 / tradeBlocksPanel.getWidth() * dateRange * period.getPeriodLength());
        setDateStart(dateStart + dateShift);
        recalculateDateEnd();

        needsRepaint = true;
        needsUpdateVisibleData = true;
    }

    private void zoom(int scrollAmount) {
        final long dateUnderMouse = getAboveWhichDateIsMouse();

        double newDataRange = dateRange * Math.pow(dataRangePercentChangeOnZoom, (double)scrollAmount);
        if(newDataRange < dataRangeMin)
            newDataRange = dataRangeMin;
        if(newDataRange > dataRangeMax)
            newDataRange = dataRangeMax;

        setDateRange(newDataRange);
        recalculateDateEnd();

        final long newDateUnderMouse = getAboveWhichDateIsMouse();
        final long deltaDate = dateUnderMouse - newDateUnderMouse;

        setDateStart(dateStart + deltaDate);
        recalculateDateEnd();

        Log.d(this, dateRange + "");

        needsRepaint = true;
        needsUpdateVisibleData = true;
    }

    private void setDateStart(long dateStart){
        this.dateStart = dateStart;
    }

    private void setDateRange(double dateRange) {
        this.dateRange = dateRange;
    }

    private void recalculateDateEnd() {
        dateEnd = period.addPeriod(dateStart, dateRange);
    }

    private void recalculateVisibleTradeBlocks(){
        this.visibleData = new ArrayList<>();

        final long visibleDateStart = dateStart;
        final long visibleDateEnd = dateEnd;
        for(int i = 0; i < data.size(); ++i){
            TradeBlock block = data.get(i);
            if(block.getDateEnd() > visibleDateStart && block.getDateEnd() < visibleDateEnd){
                visibleData.add(block);
            }
        }
    }

    private long getAboveWhichDateIsMouse(){
        return (long)((mouseLastPosition.getX() * 1.0 / tradeBlocksPanel.getWidth()) * (dateEnd - dateStart)) + dateStart;
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
