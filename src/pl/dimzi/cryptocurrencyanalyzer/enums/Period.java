package pl.dimzi.cryptocurrencyanalyzer.enums;

import java.util.function.BiFunction;
import java.util.function.Function;

public enum Period {
    DAILY(24 * 60 * 60),
    HOURLY(60 * 60);

    private String name;

    private final long periodLength;

    Period(long periodLengthInSeconds){
        name = name();
        this.periodLength = periodLengthInSeconds;
    }

    public String getName(){
        return name;
    }

    public long addPeriod(long timeToAddTo, long size){
        return timeToAddTo + periodLength * size;
    }

    public long addPeriod(long timeToAddTo, double size){
        return (long)(timeToAddTo + periodLength * size);
    }

    public long floorToPeriodType(long time){
        return (time / periodLength) * periodLength;
    }

    public long getPeriodLength(){
        return periodLength;
    }
}