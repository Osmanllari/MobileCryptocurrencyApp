package com.sheffield.stocks.currency;

public class CurrencyRVModal {
    private final String id;
    private String name;
    private String symbol;
    private double price;
    private double percentageChange;

    public CurrencyRVModal(String id, String name, String symbol, double price, double percentageChange) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.price = price;
        this.percentageChange = percentageChange;
    }
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPercentageChange() { return percentageChange; }

    public void setPercentageChange(double percentageChange) { this.percentageChange = percentageChange; }

}
