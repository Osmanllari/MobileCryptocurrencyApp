package com.sheffield.stocks.currencyDetails;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.sheffield.stocks.currencyFavorites.CurrencyFavoritesActivity;
import com.sheffield.stocks.currency.MainActivity;
import com.sheffield.stocks.R;
import com.sheffield.stocks.database.CurrencyDatabase;
import com.sheffield.stocks.database.CurrencyDetailsEntity;
import com.sheffield.stocks.database.CurrencyFavoriteEntity;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class CurrencyDetailsActivity extends AppCompatActivity {
    private String currencyId;
    private CurrencyDetailsModal currencyDetailsModal;
    private static DecimalFormat df2 = new DecimalFormat("#.##");
    TextView currencyNameTV,symbolTV,rateTV,marketCapTV,marketCapRankTV,totalVolumeTV,high24hTV,low24hTV,
            priceChangeTV,percentageChangeTV,circulatingSupplyTV,totalSupplyTV,athTV,atlTV,lastUpdatedTV;
    ImageView currencyImageView;
    private CurrencyDatabase currencyDatabase;
    TextView internetSwitch;
    boolean connectionStatus = false;
    CheckBox cbHeart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_details);

        // Build db
        currencyDatabase = Room.databaseBuilder(getApplicationContext(), CurrencyDatabase.class, "currency_db")
                .fallbackToDestructiveMigration()
                .build();

        // Extracting the id from the click on MainActivity
        currencyId = getIntent().getStringExtra("currencyId");
        Log.d("CurrencyID: ", currencyId);

        currencyNameTV = findViewById(R.id.idTVCurrencyName);
        symbolTV = findViewById(R.id.idTVSymbol);
        rateTV = findViewById(R.id.idTVCurrencyRate);
        marketCapTV = findViewById(R.id.idTVCurrencyMarketCap);
        marketCapRankTV = findViewById(R.id.idTVCurrencyMarketCapRank);
        totalVolumeTV = findViewById(R.id.idTVCurrencyTotalVolume);
        high24hTV = findViewById(R.id.idTVCurrencyHigh24);
        low24hTV = findViewById(R.id.idTVCurrencyLow24);
        priceChangeTV = findViewById(R.id.idTVCurrencyPriceChange24H);
        percentageChangeTV = findViewById(R.id.idTVPercentageChange);
        circulatingSupplyTV = findViewById(R.id.idTVCurrencyCirculatingSupply);
        totalSupplyTV = findViewById(R.id.idTVCurrencyTotalSupply);
        athTV = findViewById(R.id.idTVCurrencyATH);
        atlTV = findViewById(R.id.idTVCurrencyATL);
        lastUpdatedTV = findViewById(R.id.idTVCurrencyLastUpdated);
        currencyImageView = findViewById(R.id.idIVCurrencyImage);

        internetSwitch = findViewById(R.id.internetSwitchDetails);

        cbHeart = findViewById(R.id.cbHeart);
        checkIfCurrencyIsFavorite();

        // Fetch the currency data (either from db or from API)
        fetchCurrencyDetails();

        // Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.stocks);

        bottomNavigationView.setOnItemSelectedListener(item ->{
            switch (item.getItemId()){
                case R.id.home:
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                    return true;
                case R.id.favorites:
                    startActivity(new Intent(getApplicationContext(), CurrencyFavoritesActivity.class));
                    finish();
                    return true;
                case R.id.stocks:
                    fetchCurrencyDetails(); //refresh the data
                    return true;
            }
            return false;
        });

        // Display message to user to explain internet connection status when clicking connection switch
        internetSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String infoText = "";
                if(connectionStatus) {
                    infoText = "App is ONLINE, information is up to date!";
                }
                else {
                    infoText = "App is OFFLINE, information may not be up to date!";
                }

                Toast.makeText(getApplicationContext(), infoText, Toast.LENGTH_SHORT).show();
            }
        });

        // Listener for the "favorite" checkbox
        cbHeart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeFavoriteStatus(isChecked);
            }
        });
    }

    // Add or remove a currency from favorites in the db
    private void changeFavoriteStatus(boolean status) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if(status) {
                    CurrencyFavoriteEntity cfe = new CurrencyFavoriteEntity();
                    cfe.setCurrencyId(currencyId);
                    currencyDatabase.currencyDao().addFavoriteCurrency(cfe);
                }
                else {
                    currencyDatabase.currencyDao().deleteCurrencyFromFavorites(currencyId);
                }

                checkIfCurrencyIsFavorite();
            }
        });
    }

    // Set the state of the favorite checkbox based on if the status of the currency
    private void checkIfCurrencyIsFavorite() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                CurrencyFavoriteEntity currencyFavoriteEntity = currencyDatabase.currencyDao().getFavoriteCurrencyById(currencyId);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cbHeart.setChecked(currencyFavoriteEntity != null);
                    }
                });
            }
        });
    }

    // API request to get details about a currency
    private void getCurrencyDetails() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://app-vpigadas.herokuapp.com/api/stocks/" + currencyId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        // Parsing the JSON response using Gson
                        Gson gson = new Gson();
                        CurrencyDetailsModal currencyDetails = gson.fromJson(response.toString(), CurrencyDetailsModal.class);

                        // Get data from currencyDetails object
                        String id = currencyDetails.getId();
                        String name = currencyDetails.getName();
                        String symbol = currencyDetails.getSymbol().toUpperCase();
                        double usdPrice = currencyDetails.getMarketData().getCurrentPrice().getUsd();
                        double market_cap = currencyDetails.getMarketData().getMarketCap().getUsd();
                        int market_cap_rank = currencyDetails.getMarketCapRank();
                        double total_volume = currencyDetails.getMarketData().getTotalVolume().getUsd();
                        double high24 = currencyDetails.getMarketData().getHigh24h().getUsd();
                        double low24 = currencyDetails.getMarketData().getLow24h().getUsd();
                        double price_change_24h = currencyDetails.getMarketData().getPriceChange24h();
                        double price_change_percentage_24h = currencyDetails.getMarketData().getPriceChangePercentage24h();
                        double circulating_supply = currencyDetails.getMarketData().getCirculatingSupply();
                        double total_supply = currencyDetails.getMarketData().getTotalSupply();
                        double ath = currencyDetails.getMarketData().getAth().getUsd();
                        double atl = currencyDetails.getMarketData().getAtl().getUsd();
                        String last_updated = currencyDetails.getLastUpdated();
                        last_updated = last_updated.replace("T", " ").replace("Z", "");
                        last_updated = last_updated.substring(0, last_updated.lastIndexOf("."));

                        // Extracting image URL
                        JSONObject imageObject = response.optJSONObject("image");
                        if (imageObject != null) {
                            String imageUrl = imageObject.optString("large");
                            currencyDetails.setImageUrl(imageUrl);
                        }

                        // Loading the image using Picasso
                        Picasso.get()
                                .load(currencyDetails.getImageUrl())
                                .into(currencyImageView);

                        CurrencyDetailsEntity currency = new CurrencyDetailsEntity();
                        currency.setId(id);
                        currency.setName(name);
                        currency.setSymbol(symbol);
                        currency.setPrice(usdPrice);
                        currency.setMarket_cap(market_cap);
                        currency.setMarket_cap_rank(market_cap_rank);
                        currency.setTotal_volume(total_volume);
                        currency.setHigh24(high24);
                        currency.setLow24(low24);
                        currency.setPrice_change_24h(price_change_24h);
                        currency.setPercentage(price_change_percentage_24h);
                        currency.setCirculating_supply(circulating_supply);
                        currency.setTotal_supply(total_supply);
                        currency.setAth(ath);
                        currency.setAtl(atl);
                        currency.setLast_updated(last_updated);

                        saveCurrencyToDatabase(currency);
                        updateUI(currency);

                        currencyDetailsModal = currencyDetails;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", error.toString());
                        Toast.makeText(CurrencyDetailsActivity.this, "Failed to get the data...", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Accept", "*/*");
                headers.put("Accept-Encoding", "gzip, deflate, br");
                headers.put("Connection","keep-alive");
                return headers;
            }
        };
        queue.add(request);
    }

    // Determine if the data should be fetched from the API or DB
    private void fetchCurrencyDetails() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            // Has internet
            connectionStatus = true;
            internetSwitch.setText("ONLINE");
            internetSwitch.setTextColor(Color.parseColor("#006400"));
            getCurrencyDetails();
        } else {
            // No internet
            connectionStatus = false;
            internetSwitch.setText("OFFLINE");
            internetSwitch.setTextColor(Color.parseColor("#8B0000"));
            getCurrencyDetailsFromDatabase();
        }
    }

    // Handle currency data retrieval from db
    private void getCurrencyDetailsFromDatabase() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                CurrencyDetailsEntity currency = currencyDatabase.currencyDao().getCurrencyById(currencyId);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (currency != null) {
                            // Update UI with currency details from the database
                            updateUI(currency);
                        } else {
                            Toast.makeText(CurrencyDetailsActivity.this, "Currency details not available offline.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    // Handle the UI
    private void updateUI(CurrencyDetailsEntity currency) {
        currencyNameTV.setText("Name: " + currency.getName());
        symbolTV.setText(currency.getSymbol());
        rateTV.setText("Current Price (USD): " + String.valueOf(currency.getPrice()));
        marketCapTV.setText("Market Cap: " + df2.format(currency.getMarket_cap())  + " $");
        marketCapRankTV.setText("Rank: " + df2.format(currency.getMarket_cap_rank()));
        totalVolumeTV.setText("Total Volume: " + df2.format(currency.getTotal_volume()) + " $");
        high24hTV.setText("High 24H: " + df2.format(currency.getHigh24()) + " $");
        low24hTV.setText("Low 24H: " + df2.format(currency.getLow24()) + " $");
        priceChangeTV.setText("Price Change 24H: " + df2.format(currency.getPrice_change_24h()) + " $");
        percentageChangeTV.setText("Percentage Change 24H: " +df2.format(currency.getPercentage()) + " %");
        circulatingSupplyTV.setText("Circulating Supply: " + df2.format(currency.getCirculating_supply()) + " " + currency.getSymbol());
        totalSupplyTV.setText("Total Supply: " + df2.format(currency.getTotal_supply()) + " " + currency.getSymbol());
        athTV.setText("ATH: " + df2.format(currency.getAth()) + " $");
        atlTV.setText("ATL: " + df2.format(currency.getAtl()) + " $");
        lastUpdatedTV.setText("Last Update: " + currency.getLast_updated());
    }

    // Handle saving currency data to the db
    private void saveCurrencyToDatabase(CurrencyDetailsEntity currency) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                currencyDatabase.currencyDao().insertCurrency(currency);
            }
        });
    }
}