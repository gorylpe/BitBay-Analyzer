import com.google.gson.Gson;
import model.TradeJSON;

import javax.swing.*;
import javax.swing.border.Border;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {

    PlotPanel plotPanel;

    public MainFrame(){
        super("BitBay Analyzer");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel contentPanel = new JPanel();
        Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        contentPanel.setBorder(padding);
        setContentPane(contentPanel);

        plotPanel = new PlotPanel();

        plotPanel.setData(getData());
        plotPanel.repaint();

        add(plotPanel);
        setVisible(true);
        pack();
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T,Object> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public ArrayList<TradeJSON> getData(){
        ArrayList<TradeJSON> trades = new ArrayList<>();

        try {
            URL url = new URL("https://bitbay.net/API/Public/ETHPLN/trades.json?sort=desc");
            String jsonString = new Scanner(url.openStream()).useDelimiter("\\A").next();
            Gson gson = new Gson();
            TradeJSON[] tmp = gson.fromJson(jsonString, TradeJSON[].class);
            trades.addAll(Arrays.asList(tmp));

            for(int i = 1; i < 20; ++i){
                Long lastTid = tmp[tmp.length - 1].getTid();
                url = new URL("https://bitbay.net/API/Public/ETHPLN/trades.json?sort=asc&"+"since="+(lastTid - 50 * i));
                jsonString = new Scanner(url.openStream()).useDelimiter("\\A").next();
                tmp = gson.fromJson(jsonString, TradeJSON[].class);
                trades.addAll(Arrays.asList(tmp));
                trades = trades.stream().filter(distinctByKey(TradeJSON::getTid)).collect(Collectors.toCollection(ArrayList::new));
            }
        } catch(IOException e){
            e.printStackTrace();
        }

        trades.sort(Comparator.comparing(TradeJSON::getDate));

        return trades;
    }
}
