package com.sheffield.stocks.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "favorite_currencies")
public class CurrencyFavoriteEntity {

    @PrimaryKey
    @NonNull
    private String currencyId;

    @NonNull
    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(@NonNull String currencyId) {
        this.currencyId = currencyId;
    }
}
