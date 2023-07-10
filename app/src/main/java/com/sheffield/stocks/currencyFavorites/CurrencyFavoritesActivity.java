package com.sheffield.stocks.currencyFavorites;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sheffield.stocks.currency.CurrencyRVAdapter;
import com.sheffield.stocks.currency.CurrencyRVModal;
import com.sheffield.stocks.currency.MainActivity;
import com.sheffield.stocks.R;
import com.sheffield.stocks.currencyDetails.CurrencyDetailsActivity;
import com.sheffield.stocks.database.CurrencyDatabase;
import com.sheffield.stocks.database.CurrencyEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CurrencyFavoritesActivity extends AppCompatActivity implements CurrencyRVAdapter.OnItemClickListener {

    private EditText searchEdt;
    private RecyclerView currenciesRV;
    private ProgressBar loadingPB;
    private ArrayList<CurrencyRVModal> currencyRVModalArrayList;
    private CurrencyRVAdapter currencyRVAdapter;
    private CurrencyDatabase currencyDatabase;
    private TextView internetSwitch;
    boolean connectionStatus = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Build db
        currencyDatabase = Room.databaseBuilder(getApplicationContext(), CurrencyDatabase.class, "currency_db")
                .fallbackToDestructiveMigration()
                .build();

        searchEdt = findViewById(R.id.idEdtSearch);
        currenciesRV = findViewById(R.id.idRVCurrencies);
        loadingPB = findViewById(R.id.idPBLoading);
        currencyRVModalArrayList = new ArrayList<>();
        currencyRVAdapter = new CurrencyRVAdapter(currencyRVModalArrayList, this);
        currenciesRV.setLayoutManager(new LinearLayoutManager(this));
        currenciesRV.setAdapter(currencyRVAdapter);
        internetSwitch = findViewById(R.id.switchOnlineOffline);

        // Fetch currency data (from db or API)
        fetchCurrencyData();

        // Setting the click listener in the adapter
        currencyRVAdapter.setOnItemClickListener(this);

        // Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.favorites);

        bottomNavigationView.setOnItemSelectedListener(item ->{
            switch (item.getItemId()){
                case R.id.home:
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                    return true;
                case R.id.favorites:
                    return true;
                case R.id.stocks:
                    fetchCurrencyData(); //refresh the data
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

        searchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filterCurrencies(s.toString());
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        CurrencyRVModal currency = currencyRVModalArrayList.get(position);
        Intent intent = new Intent(CurrencyFavoritesActivity.this, CurrencyDetailsActivity.class);
        intent.putExtra("currencyId", currency.getId()); // Sending the id of the selected coin to the second activity
        startActivity(intent);
    }

    private void filterCurrencies(String currency) {
        ArrayList<CurrencyRVModal> filteredList = new ArrayList<>();
        for (CurrencyRVModal item : currencyRVModalArrayList) {
            if (item.getName().toLowerCase().contains(currency.toLowerCase())) {
                filteredList.add(item);
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No currency found", Toast.LENGTH_SHORT).show();
        } else {
            currencyRVAdapter.filterList(filteredList);
        }
    }

    private void getCurrencyData() {
        loadingPB.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://app-vpigadas.herokuapp.com/api/stocks/";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        loadingPB.setVisibility(View.GONE);
                        try {
                            List<CurrencyEntity> currencies = new ArrayList<>();
                            JSONObject jsonObject = new JSONObject();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject currencyObj = response.getJSONObject(i);
                                String id = currencyObj.getString("id");
                                String name = currencyObj.getString("name");
                                String symbol = currencyObj.getString("symbol");
                                double price = currencyObj.getDouble("current_price");
                                double percentage = currencyObj.getDouble("price_change_percentage_24h");
                                currencyRVModalArrayList.add(new CurrencyRVModal(id,name,symbol,price,percentage));

                                CurrencyEntity currency = new CurrencyEntity();
                                currency.setId(id);
                                currency.setName(name);
                                currency.setSymbol(symbol);
                                currency.setPrice(price);
                                currency.setPercentage(percentage);
                            }
                            // Save currencies to the Room database
                            saveCurrenciesToDatabase(currencies);
                            getCurrenciesFromDatabase();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(CurrencyFavoritesActivity.this, "Failed to extract JSON data.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingPB.setVisibility(View.GONE);
                        Log.e("Error", error.toString());
                        Toast.makeText(CurrencyFavoritesActivity.this, "Failed to get the data...", Toast.LENGTH_SHORT).show();
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
        queue.add(jsonArrayRequest);
    }

    private void saveCurrenciesToDatabase(List<CurrencyEntity> currencies) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                currencyDatabase.currencyDao().insertCurrencies(currencies);
            }
        });
    }

    private void updateUI(List<CurrencyEntity> currencies) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Clear the previous list of currencies and add the new ones
                currencyRVModalArrayList.clear();
                for (CurrencyEntity currency : currencies) {
                    currencyRVModalArrayList.add(new CurrencyRVModal(
                            currency.getId(),
                            currency.getName(),
                            currency.getSymbol(),
                            currency.getPrice(),
                            currency.getPercentage()
                    ));
                }
                currencyRVAdapter.notifyDataSetChanged();
            }
        });
    }

    // Retrieves the currencies from db (only the user's favorite currencies)
    private void getCurrenciesFromDatabase() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                List<CurrencyEntity> currencies = currencyDatabase.currencyDao().getFavoriteCurrencies();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI(currencies);
                    }
                });
            }
        });
    }

    // Determine if the data should be fetched from the API or DB
    private void fetchCurrencyData() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            // Has internet
            connectionStatus = true;
            internetSwitch.setText("ONLINE");
            internetSwitch.setTextColor(Color.parseColor("#006400"));
            getCurrencyData();
        } else {
            // No internet
            connectionStatus = false;
            internetSwitch.setText("OFFLINE");
            internetSwitch.setTextColor(Color.parseColor("#8B0000"));
            getCurrenciesFromDatabase();
        }
    }
}