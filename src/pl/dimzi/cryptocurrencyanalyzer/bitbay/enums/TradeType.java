package pl.dimzi.cryptocurrencyanalyzer.bitbay.enums;


import pl.dimzi.cryptocurrencyanalyzer.enums.Period;
import pl.dimzi.cryptocurrencyanalyzer.model.CurrencyData;

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
    private String since;

    TradeType(String firstCurrency, String secondCurrency){
        this.name = name();
        this.url = "https://bitbay.net/API/Public/" + name + "/trades.json?sort=asc";
        this.since = "&since=";

        this.firstCurrency = firstCurrency;
        this.secondCurrency = secondCurrency;
    }

    public String getUrl(){
        return url;
    }

    public String getSince() {
        return since;
    }

    public String getTradesTableName(){
        return name + "_TRADES";
    }

    public String getCurrencyDataTableName(Period periodType){
        return name + "_" + periodType.getName();
    }

    public String getFirstCurrency() {
        return firstCurrency;
    }

    public String getSecondCurrency() {
        return secondCurrency;
    }
}