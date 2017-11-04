package main.pl.dimzi.cryptocurrencyanalyzer;

public class Log {
    private static boolean debug = false;

    public static void enableDebug(boolean enable){ debug = enable;}
    public static void d(Object o, String msg){
        if(debug){
            System.out.println("DEBUG: "+o.getClass().getSimpleName()+": "+msg);
        }
    }
    public static void e(Object o, String msg){
        System.out.println("ERROR: "+o.getClass()+": "+msg);
    }
}
