package com.sheffield.stocks.currencyDetails;

public class CurrencyDetailsModal {
    private String id;
    private String name;
    private String symbol;
    private int market_cap_rank;
    private String last_updated;
    private MarketData market_data;

    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    public MarketData getMarketData() {
        return market_data;
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

    public int getMarketCapRank() { return market_cap_rank; }

    public void setMarketCapRank(int market_cap_rank) { this.market_cap_rank = market_cap_rank; }

    public String getLastUpdated() { return last_updated; }

    public void setLastUpdated(String lastUpdated) { this.last_updated = lastUpdated; }
}

class MarketData {
    private CurrentPrice current_price;
    private MarketCap market_cap;
    private TotalVolume total_volume;
    private High24h high_24h;
    private Low24h low_24h;
    private double price_change_24h;
    private double price_change_percentage_24h;
    private double circulating_supply;
    private double total_supply;
    private Ath ath;
    private Atl atl;

    public CurrentPrice getCurrentPrice() {
        return current_price;
    }

    public MarketCap getMarketCap() {return  market_cap;}

    public TotalVolume getTotalVolume() {return total_volume;}

    public High24h getHigh24h() { return high_24h; }

    public Low24h getLow24h() { return low_24h; }

    public double getPriceChange24h() {return price_change_24h;}

    public double getPriceChangePercentage24h() {return price_change_percentage_24h;}

    public double getCirculatingSupply() {return circulating_supply;}

    public double getTotalSupply() {return total_supply;}

    public Ath getAth() {return ath;}

    public Atl getAtl() {return atl;}
}

class CurrentPrice {
    private double eur;
    private double usd;

    public double getEur() {return eur;}
    public double getUsd() {return usd;}
}

class MarketCap {
    private double eur;
    private double usd;

    public double getEur() {return eur;}
    public double getUsd() {return usd;}
}


class TotalVolume {
    private double eur;
    private double usd;

    public double getEur() {return eur;}
    public double getUsd() {return usd;}
}

class High24h {
    private double eur;
    private double usd;

    public double getEur() {return eur;}
    public double getUsd() {return usd;}
}

class Low24h {
    private double eur;
    private double usd;

    public double getEur() {return eur;}
    public double getUsd() {return usd;}
}

class Ath {
    private double eur;
    private double usd;

    public double getEur() {return eur;}
    public double getUsd() {return usd;}
}

class Atl {
    private double eur;
    private double usd;

    public double getEur() {return eur;}
    public double getUsd() {return usd;}
}


