package pl.dimzi.cryptocurrencyanalyzer.bitbay.controller;

import pl.dimzi.cryptocurrencyanalyzer.enums.Period;
import pl.dimzi.cryptocurrencyanalyzer.model.CurrencyData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public enum BitBayController {
    INSTANCE;

    public Map<Period, ArrayList<CurrencyData>> currencyDataMap;

    BitBayController() {
        this.currencyDataMap = Collections.synchronizedMap(new EnumMap<Period, ArrayList<CurrencyData>>(Period.class));
    }


    public void setCurrencyData(Period periodType, ArrayList<CurrencyData> currencyDataArrayList){
        currencyDataMap.put(periodType, currencyDataArrayList);
    }

    public ArrayList<CurrencyData> getCurrencyData(Period periodType){
        return currencyDataMap.get(periodType);
    }
}
