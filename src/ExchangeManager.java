

public interface ExchangeManager {
    boolean initializeDatabase();
    void update();
    void attachObserver(CurrencyObserver observer);
    void notifyAllObservers();

    enum CurrencyDataPeriodType {
        DAILY;

        String name;

        CurrencyDataPeriodType(){
            name = name();
        }

        String getName(){
            return name;
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
                this.join();
            } catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}
