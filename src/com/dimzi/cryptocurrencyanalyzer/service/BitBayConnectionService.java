package com.dimzi.cryptocurrencyanalyzer.service;

import com.dimzi.cryptocurrencyanalyzer.BitBay.TradeType;
import com.dimzi.cryptocurrencyanalyzer.model.BitBayTrade;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class BitBayConnectionService {

    Gson gson;

    public BitBayConnectionService(){
        gson = new Gson();
    }

    private BitBayTrade[] getTradeEntriesFromServer(TradeType type, Long since){
        try{
            URL url = new URL(type.getUrl() + since);
            String jsonString = new Scanner(url.openStream()).useDelimiter("\\A").next();
            BitBayTrade[] tmp = gson.fromJson(jsonString, BitBayTrade[].class);

            return tmp;
        } catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<BitBayTrade> getTradesFromToTid(TradeType type, Long from, Long to){
        ArrayList<BitBayTrade> trades = new ArrayList<>();
        BitBayTrade[] tmpTrades;
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

    public ArrayList<BitBayTrade> getTradesFromToNow(TradeType type, Long from){
        return getTradesFromToTid(type, from, -1L);
    }
}
