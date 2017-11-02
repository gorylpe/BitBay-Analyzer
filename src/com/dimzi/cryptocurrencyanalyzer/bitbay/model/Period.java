package com.dimzi.cryptocurrencyanalyzer.bitbay.model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Function;

public enum Period {
    DAILY(  (period) -> period.plusDays(1),
            (period) -> period.with(LocalTime.of(0, 0)));

    private String name;
    private Function<LocalDateTime, LocalDateTime> plusFunction;
    Function<LocalDateTime, LocalDateTime> roundFunction;

    Period(Function<LocalDateTime, LocalDateTime> plusFunction,
           Function<LocalDateTime, LocalDateTime> roundFunction){
        name = name();
        this.plusFunction = plusFunction;
        this.roundFunction = roundFunction;
    }

    public String getName(){
        return name;
    }

    public LocalDateTime plusPeriod(LocalDateTime localDateTime){
        return plusFunction.apply(localDateTime);
    }

    public LocalDateTime roundToPeriodType(LocalDateTime localDateTime){
        return roundFunction.apply(localDateTime);
    }
}