package com.dimzi.cryptocurrencyanalyzer;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Function;

public interface ExchangeManager {
    void update();

    enum CurrencyDataPeriodType {
        DAILY(  (period) -> period.plusDays(1),
                (period) -> period.with(LocalTime.of(0, 0)));

        private String name;
        private Function<LocalDateTime, LocalDateTime> plusFunction;
        Function<LocalDateTime, LocalDateTime> roundFunction;

        CurrencyDataPeriodType(Function<LocalDateTime, LocalDateTime> plusFunction,
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

    class AutoUpdateThread extends Thread{

        private final ExchangeManager manager;
        private final long interval;
        private boolean run;

        public AutoUpdateThread(ExchangeManager manager, long interval){
            this.manager = manager;
            this.interval = interval;
            this.run = true;
        }

        @Override
        public void run(){
            System.out.println("Starting autoupdate thread");

            while(run){
                manager.update();
                if(!run)
                    break;
                try {
                    Thread.sleep(interval);
                } catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }

        public void end(){
            System.out.println("Ending autoupdate thread");

            try{
                run = false;
                this.interrupt();
                this.join();
            } catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}
