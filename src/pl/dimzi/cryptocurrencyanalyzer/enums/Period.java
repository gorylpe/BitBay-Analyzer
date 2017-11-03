package pl.dimzi.cryptocurrencyanalyzer.enums;

import java.util.function.Function;

public enum Period {
    DAILY(  (period) -> period + 24 * 3600,
            (period) -> (period / (24 * 3600)) * 24 * 3600);

    private String name;
    private Function<Long, Long> plusFunction;
    Function<Long, Long> floorFunction;

    Period(Function<Long, Long> plusFunction,
           Function<Long, Long> floorFunction){
        name = name();
        this.plusFunction = plusFunction;
        this.floorFunction = floorFunction;
    }

    public String getName(){
        return name;
    }

    public Long plusPeriod(Long time){
        return plusFunction.apply(time);
    }

    public Long floorToPeriodType(Long time){
        return floorFunction.apply(time);
    }
}