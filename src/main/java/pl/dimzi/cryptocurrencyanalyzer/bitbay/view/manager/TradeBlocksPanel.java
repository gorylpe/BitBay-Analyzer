package pl.dimzi.cryptocurrencyanalyzer.bitbay.view.manager;

import pl.dimzi.cryptocurrencyanalyzer.Log;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.model.TradeBlock;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;

public class TradeBlocksPanel extends JPanel {
    private ArrayList<TradeBlock> visibleData;

    private long dateStart;
    private long dateEnd;

    private boolean repainting;

    private final Color backgroundColor = Color.WHITE;

    private final Font font = new Font("Arial", Font.BOLD, 15);

    TradeBlocksPanel(){
        super();
        setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    public void setData(ArrayList<TradeBlock> data, long dateStart, long dateEnd){
        this.visibleData = data;
        this.dateEnd = dateEnd;
        this.dateStart = dateStart;

        setRepainting(true);
        repaint();
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

        repainting = false;
    }

    private void drawData(Graphics2D g2d) {

        g2d.setStroke(new BasicStroke(2.0f));
        g2d.setColor(Color.BLACK);
        g2d.setFont(font);

        double xscale = getWidth() * 1.0 / (dateEnd - dateStart);

        for(TradeBlock block : visibleData){
            int startx = (int)((block.getDateStart() - dateStart) * xscale);
            int endx = (int)((block.getDateEnd() - dateStart) * xscale);

            int h = getHeight() / 2;

            g2d.drawLine(startx, h, endx, h);

            g2d.draw(new Ellipse2D.Double(startx - 5, h - 5, 10, 10));
            g2d.draw(new Ellipse2D.Double(endx - 5, h - 5, 10, 10));
            g2d.drawString(Instant.ofEpochSecond(block.getDateEnd()).atZone(ZoneId.systemDefault()).toLocalDateTime().toString(), endx - 20, h - 20);
            g2d.drawString(Instant.ofEpochSecond(block.getDateStart()).atZone(ZoneId.systemDefault()).toLocalDateTime().toString(), startx - 20, h - 20);
        }
    }
}