package pl.dimzi.cryptocurrencyanalyzer.model;

public class CurrencyData {
    private Long periodStart;
    private Double minimum;
    private Double maximum;
    private Double opening;
    private Double closing;
    private Double average;
    private Double volume;

    public CurrencyData(){
        minimum = 0.0;
        maximum = 0.0;
        opening = 0.0;
        closing = 0.0;
        average = 0.0;
        volume = 0.0;
        periodStart = 0L;
    }

    public CurrencyData(double minimum, double maximum, double opening, double closing, double average, double volume, long periodStart){
        this.minimum = minimum;
        this.maximum = maximum;
        this.opening = opening;
        this.closing = closing;
        this.average = average;
        this.volume = volume;
        this.periodStart = periodStart;
    }

    public Double getMinimum() {
        return minimum;
    }

    public void setMinimum(Double minimum) {
        this.minimum = minimum;
    }

    public Double getMaximum() {
        return maximum;
    }

    public void setMaximum(Double maximum) {
        this.maximum = maximum;
    }

    public Double getOpening() {
        return opening;
    }

    public void setOpening(Double opening) {
        this.opening = opening;
    }

    public Double getClosing() {
        return closing;
    }

    public void setClosing(Double closing) {
        this.closing = closing;
    }

    public Double getAverage() {
        return average;
    }

    public void setAverage(Double average) {
        this.average = average;
    }

    public long getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(long periodStart) {
        this.periodStart = periodStart;
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }

    @Override
    public String toString(){
        return periodStart + " - " + minimum + " - " + maximum + " - " + opening + " - " + closing + " - " + average + " - " + volume;
    }
}
