package pl.dimzi.cryptocurrencyanalyzer.bitbay.view.manager;

import pl.dimzi.cryptocurrencyanalyzer.Log;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.model.TradeBlock;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;

public class TradeBlocksPanel extends JPanel {
    private ArrayList<TradeBlock> visibleData;

    private long dateStart;
    private long dateEnd;

    private boolean repainting;

    private final Color backgroundColor = Color.WHITE;

    TradeBlocksPanel(){
        super();
        setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    public void setData(ArrayList<TradeBlock> data, long dateStart, long dateEnd){
        this.visibleData = data;
        this.dateEnd = dateEnd;
        this.dateStart = dateStart;
    }

    public void setRepainting(boolean repainting){
        this.repainting = repainting;
    }

    public boolean getRepainting(){
        return repainting;
    }

    @Override
    public void paintComponent(Graphics g){
        Graphics2D g2d = (Graphics2D)g;

        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if(visibleData != null){
            drawData(g2d);
        }

        Log.d(this, System.currentTimeMillis() + "");

        repainting = false;
    }

    private void drawData(Graphics2D g2d) {

    }
}