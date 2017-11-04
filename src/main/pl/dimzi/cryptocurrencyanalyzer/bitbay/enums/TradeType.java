package main.pl.dimzi.cryptocurrencyanalyzer.bitbay.enums;


import main.pl.dimzi.cryptocurrencyanalyzer.enums.Period;

public enum TradeType {
    ETHPLN("ETH", "PLN");

    private String name;
    private String firstCurrency;
    private String secondCurrency;
    private String sinceUrl;
    private String descUrl;

    TradeType(String firstCurrency, String secondCurrency){
        this.name = name();
        String url = "https://bitbay.net/API/Public/" + name + "/trades.json";
        this.descUrl = url + "?sort=desc";
        this.sinceUrl = url + "?sort=asc&since=";

        this.firstCurrency = firstCurrency;
        this.secondCurrency = secondCurrency;
    }

    public String getDescUrl(){
        return descUrl;
    }

    public String getUrl(long since){
        return sinceUrl + since;
    }

    public String getTradesTableName(){
        return name + "_TRADES";
    }

    public String getTradeBlocksTableName() { return name + "_BLOCKS"; }

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