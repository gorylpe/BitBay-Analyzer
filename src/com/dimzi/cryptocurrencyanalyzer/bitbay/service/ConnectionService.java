package com.dimzi.cryptocurrencyanalyzer.bitbay.service;

import com.dimzi.cryptocurrencyanalyzer.bitbay.model.TradeType;
import com.dimzi.cryptocurrencyanalyzer.bitbay.model.Trade;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class ConnectionService {

    Gson gson;

    public ConnectionService(){
        gson = new Gson();
    }

    private Trade[] getTradeEntriesFromServer(TradeType type, Long since){
        try{
            URL url = new URL(type.getUrl() + since);
            String jsonString = new Scanner(url.openStream()).useDelimiter("\\A").next();
            Trade[] tmp = gson.fromJson(jsonString, Trade[].class);

            return tmp;
        } catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Trade> getTradesFromToTid(TradeType type, Long from, Long to){
        ArrayList<Trade> trades = new ArrayList<>();
        Trade[] tmpTrades;
        do{
            tmpTrades = getTradeEntriesFromServer(type, from);
            if(tmpTrades == null) break;

            for(int i = 0; i < tmpTrades.length; ++i){
                if(tmpTrades[i].getTid() <= to){
                    trades.add(tmpTrades[i]);
                }
            }


        }while(tmpTrades.length > 0 && (from < to || to == -1));

        return trades;
    }

    public ArrayList<Trade> getTradesFromToNow(TradeType type, Long from){
        return getTradesFromToTid(type, from, -1L);
    }
}
