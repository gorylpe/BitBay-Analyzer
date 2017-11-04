package main.java.pl.dimzi.cryptocurrencyanalyzer.bitbay.model;

public class TradeBlock {
    Long dateStart;
    Long dateEnd;

    public TradeBlock(long dateStart, long dateEnd){
        this.dateEnd = dateEnd;
        this.dateStart = dateStart;
    }

    public long getDateStart(){
        return dateStart;
    }

    public void setDateStart(long dateStart){
        this.dateStart = dateStart;
    }

    public long getDateEnd(){
        return dateEnd;
    }

    public void setDateEnd(long dateEnd){
        this.dateEnd = dateEnd;
    }
}
