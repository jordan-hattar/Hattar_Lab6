package com.example.lab6_1;

import android.app.Application;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.lab6_1.db.PortfolioDatabase;
import com.example.lab6_1.db.Stock;


public class PortfolioViewModel extends AndroidViewModel {

    private static PortfolioDatabase portfolioDatabase;
    private LiveData<List<Stock>> allStocks;


    public PortfolioViewModel(@NonNull Application application) {
        super(application);

        portfolioDatabase = PortfolioDatabase.getInstance(application);
        allStocks = portfolioDatabase.stockDao().getAll();
    }

    public LiveData<List<Stock>> getAllStocks() {
        return allStocks;
    }

    public PortfolioDatabase getPortfolioDatabase(){
        return portfolioDatabase;
    }
}