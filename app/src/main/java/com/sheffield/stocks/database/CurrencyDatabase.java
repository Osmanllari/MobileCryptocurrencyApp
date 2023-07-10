package com.sheffield.stocks.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {CurrencyEntity.class, CurrencyDetailsEntity.class, CurrencyFavoriteEntity.class}, version = 8)
public abstract class CurrencyDatabase extends RoomDatabase {
    public abstract CurrencyDao currencyDao();

}
