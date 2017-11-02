package pl.dimzi.cryptocurrencyanalyzer.bitbay.enums;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Function;

public enum Period {
    DAILY(  (period) -> period.plusDays(1),
            (period) -> period.with(LocalTime.of(0, 0)));

    private String name;
    private Function<LocalDateTime, LocalDateTime> plusFunction;
    Function<LocalDateTime, LocalDateTime> floorFunction;

    Period(Function<LocalDateTime, LocalDateTime> plusFunction,
           Function<LocalDateTime, LocalDateTime> floorFunction){
        name = name();
        this.plusFunction = plusFunction;
        this.floorFunction = floorFunction;
    }

    public String getName(){
        return name;
    }

    public LocalDateTime plusPeriod(LocalDateTime localDateTime){
        return plusFunction.apply(localDateTime);
    }

    public LocalDateTime floorToPeriodType(LocalDateTime localDateTime){
        return floorFunction.apply(localDateTime);
    }
}