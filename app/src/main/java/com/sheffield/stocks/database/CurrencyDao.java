package com.sheffield.stocks.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CurrencyDao {

    //Currency Entity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCurrencies(List<CurrencyEntity> currencies);

    @Query("SELECT * FROM currencies")
    List<CurrencyEntity> getCurrencies();

    @Query("Select * FROM currencies WHERE id = :currencyId")
    CurrencyEntity getCurrencyEntityById(String currencyId);


    //Currency Details
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCurrency(CurrencyDetailsEntity currency);

    @Query("SELECT * FROM currency_details WHERE id = :currencyId")
    CurrencyDetailsEntity getCurrencyById(String currencyId);


    //Favorite Currencies

    // get the currencies that match (being on both tables means they are favorite)
    @Query("SELECT * FROM currencies INNER JOIN favorite_currencies ON currencies.id = favorite_currencies.currencyId")
    List<CurrencyEntity> getFavoriteCurrencies();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addFavoriteCurrency(CurrencyFavoriteEntity favoriteCurrency);

    @Query("SELECT * FROM favorite_currencies WHERE currencyId = :currencyId")
    CurrencyFavoriteEntity getFavoriteCurrencyById(String currencyId);

    @Query("DELETE FROM favorite_currencies WHERE currencyId = :currencyId")
    void deleteCurrencyFromFavorites(String currencyId);
}
