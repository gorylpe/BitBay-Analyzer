package pl.dimzi.cryptocurrencyanalyzer.bitbay.service;

import pl.dimzi.cryptocurrencyanalyzer.Log;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.enums.TradeType;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.model.Trade;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class ConnectionService {

    private Gson gson;

    private Long lastTimeOfRequest = System.currentTimeMillis();

    public ConnectionService(){
        gson = new Gson();
    }

    private void waitForNextRequest(long time){
        long timeNow = System.currentTimeMillis();
        if(timeNow - time < lastTimeOfRequest){
            try{
                long sleepTime = time - (timeNow - lastTimeOfRequest);
                Thread.sleep(sleepTime > 0 ? sleepTime : 0);
                lastTimeOfRequest = System.currentTimeMillis();
            } catch(InterruptedException e){}
        }
    }

    private Trade[] get50TradeEntriesFromServer(TradeType type, Long since){
        try{
            waitForNextRequest(500);
            Log.d(this, "Ask:" + type.getUrl(since));
            URL url = new URL(type.getUrl(since));
            String jsonString = new Scanner(url.openStream()).useDelimiter("\\A").next();
            Log.d(this, "Response:" + jsonString);

            return gson.fromJson(jsonString, Trade[].class);
        } catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets trades from server from "from" to "to" tids includes both.
     * @param type trade type
     * @param from tid to start from, including
     * @param to tid to end at, including
     * @return trades array
     */
    public ArrayList<Trade> getTradesFromTo(TradeType type, Long from, Long to){
        ArrayList<Trade> trades = new ArrayList<>();
        Trade[] tmpTrades;
        do{
            tmpTrades = get50TradeEntriesFromServer(type, from);
            if(tmpTrades == null) break;

            for(int i = 0; i < tmpTrades.length; ++i){
                if(tmpTrades[i].getTid() <= to){
                    trades.add(tmpTrades[i]);
                }
            }
            from = tmpTrades[tmpTrades.length - 1].getTid();

            Log.d(this, "Got trades from tid " + tmpTrades[0].getTid() + " to " + tmpTrades[tmpTrades.length - 1].getTid());
        }while(tmpTrades.length > 0 && (from < to || to == -1));

        return trades;
    }

    public ArrayList<Trade> getTradesToNow(TradeType type, Long from){
        return getTradesFromTo(type, from, -1L);
    }

    public Long getNewestTid(TradeType type){
        try {
            waitForNextRequest(200);
            URL url = new URL(type.getDescUrl());
            String jsonString = new Scanner(url.openStream()).useDelimiter("\\A").next();
            Trade[] tmp = gson.fromJson(jsonString, Trade[].class);
            if(tmp != null && tmp.length > 0){
                return tmp[tmp.length - 1].getTid();
            }
        } catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public Trade getClosestNextTrade(TradeType type, Long since){
        try {
            waitForNextRequest(200);
            URL url = new URL(type.getUrl(since));
            String jsonString = new Scanner(url.openStream()).useDelimiter("\\A").next();
            Trade[] tmp = gson.fromJson(jsonString, Trade[].class);
            if(tmp != null && tmp.length > 0){
                return tmp[0];
            }
        } catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
