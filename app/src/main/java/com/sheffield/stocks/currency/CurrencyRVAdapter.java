package com.sheffield.stocks.currency;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sheffield.stocks.R;
import com.sheffield.stocks.database.CurrencyEntity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CurrencyRVAdapter extends RecyclerView.Adapter<CurrencyRVAdapter.ViewHolder> {
    private ArrayList<CurrencyRVModal> currencyRVModalArrayList;
    private Context context;
    private static DecimalFormat df2 = new DecimalFormat("#.##");

    private OnItemClickListener clickListener;

    public CurrencyRVAdapter(ArrayList<CurrencyRVModal> currencyRVModalArrayList, Context context) {
        this.currencyRVModalArrayList = currencyRVModalArrayList;
        this.context = context;
    }

    public CurrencyRVAdapter(List<CurrencyEntity> currencies) {
    }

    public void filterList(ArrayList<CurrencyRVModal> filteredList){
        currencyRVModalArrayList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CurrencyRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.currency_rv_item,parent,false);
        return new CurrencyRVAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CurrencyRVAdapter.ViewHolder holder, int position) {
        CurrencyRVModal currencyRVModal = currencyRVModalArrayList.get(position);
        holder.currencyNameTV.setText(currencyRVModal.getName());
        holder.symbolTV.setText(currencyRVModal.getSymbol());
        holder.rateTV.setText("$ "+df2.format(currencyRVModal.getPrice()));
        holder.percentageChangeTV.setText(df2.format(currencyRVModal.getPercentageChange())+" %");

        double priceChangePercentage = currencyRVModal.getPercentageChange();

        if (priceChangePercentage >= 0) {
            holder.percentageChangeTV.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green));
        } else {
            holder.percentageChangeTV.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));
        }

        // Adding the click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return currencyRVModalArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView currencyNameTV,symbolTV,rateTV,percentageChangeTV;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            currencyNameTV = itemView.findViewById(R.id.idTvCurrencyName);
            symbolTV = itemView.findViewById(R.id.idTVSymbol);
            rateTV = itemView.findViewById(R.id.idTVCurrencyRate);
            percentageChangeTV = itemView.findViewById(R.id.idTVPercentageChange);
        }
    }

    // Defining the click listener interface
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    // Setting the click listener method
    public void setOnItemClickListener(OnItemClickListener listener) {
        clickListener = listener;
    }
}

