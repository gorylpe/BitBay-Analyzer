package model;

import java.time.LocalDateTime;

public class BitBayTrade {
    private Long tid;
    private LocalDateTime date;
    private Double price;
    private Double amount;
    private String type;

    public BitBayTrade(){
        tid = 0L;
        date = LocalDateTime.now();
        price = 0.0;
        amount = 0.0;
        type = "";
    }

    public BitBayTrade(long tid, LocalDateTime date, double price, double amount, String type){
        this.tid = tid;
        this.date = date;
        this.price = price;
        this.amount = amount;
        this.type = type;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Long getTid() {
        return tid;
    }

    public void setTid(Long tid) {
        this.tid = tid;
    }
}
