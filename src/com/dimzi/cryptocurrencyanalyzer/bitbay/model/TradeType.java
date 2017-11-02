package com.dimzi.cryptocurrencyanalyzer.bitbay.model;


import com.dimzi.cryptocurrencyanalyzer.ExchangeManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public enum TradeType {
    ETHPLN("ETH", "PLN");

    private String name;
    private String firstCurrency;
    private String secondCurrency;
    private String url;

    public Map<Period, ArrayList<CurrencyData>> currencyDataMap;
    public Map<Period, CurrencyData> currentCurrencyDataMap;

    TradeType(String firstCurrency, String secondCurrency){
        this.name = name();
        this.url = "https://bitbay.net/API/Public/" + name + "/trades.json?sort=asc&since=";
        this.currencyDataMap = Collections.synchronizedMap(new EnumMap<Period, ArrayList<CurrencyData>>(Period.class));
        this.currentCurrencyDataMap = Collections.synchronizedMap(new EnumMap<Period, CurrencyData>(Period.class));

        this.firstCurrency = firstCurrency;
        this.secondCurrency = secondCurrency;
    }

    public String getUrl(){
        return url;
    }

    public String getTradesTableName(){
        return name + "_TRADES";
    }

    public String getCurrencyDataTableName(Period periodType){
        return name + "_" + periodType.getName();
    }

    public void setCurrencyData(Period periodType, ArrayList<CurrencyData> currencyDataArrayList){
        currencyDataMap.put(periodType, currencyDataArrayList);
    }

    public ArrayList<CurrencyData> getCurrencyData(Period periodType){
        return currencyDataMap.get(periodType);
    }

    public void setCurrentCurrencyData(Period periodType, CurrencyData currencyData){
        currentCurrencyDataMap.put(periodType, currencyData);
    }

    public CurrencyData getCurrentCurrencyData(Period periodType){
        return currentCurrencyDataMap.get(periodType);
    }

    public String getFirstCurrency() {
        return firstCurrency;
    }

    public String getSecondCurrency() {
        return secondCurrency;
    }
}