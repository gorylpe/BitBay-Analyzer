package pl.dimzi.cryptocurrencyanalyzer.bitbay.controller;


import pl.dimzi.cryptocurrencyanalyzer.bitbay.view.DetailsPanel;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.view.PlotPanel;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.view.Window;

import java.awt.event.*;

public class WindowController {

    private Window window;

    private PlotController plotController;
    private DetailsController detailsController;

    public WindowController(){
        window = new Window();

        plotController = new PlotController(window.getPlotPanel());
        detailsController = new DetailsController(window.getDetailsPanel());
    }

    private class PlotController extends MouseAdapter{
        private PlotPanel plot;

        private PlotController(PlotPanel plot){
            this.plot = plot;
            plot.addMouseListener(this);
            plot.addMouseWheelListener(this);
            plot.addMouseMotionListener(this);
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

        @Override
        public void mouseWheelMoved(MouseWheelEvent e){

        }
    }

    private class DetailsController{
        private DetailsPanel detailsPanel;

        private DetailsController(DetailsPanel detailsPanel){
            this.detailsPanel = detailsPanel;
        }
    }
}
