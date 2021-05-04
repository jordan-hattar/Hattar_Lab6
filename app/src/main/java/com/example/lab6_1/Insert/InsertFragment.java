package com.example.lab6_1.Insert;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.example.lab6_1.AlreadyInDatabase;
import com.example.lab6_1.EmptyStock;
import com.example.lab6_1.NotInDatabase;
import com.example.lab6_1.PortfolioViewModel;
import com.example.lab6_1.R;
import com.example.lab6_1.db.DatabaseOperations;
import com.example.lab6_1.db.Stock;
import java.util.List;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class InsertFragment extends Fragment {

    private static final String TAG = "_MainActivity_";
    private Button addStockButton;
    private Button deleteStockButton;
    private EditText nameEditText;
    private EditText priceEditText;
    private PortfolioViewModel portfolioViewModel;
    private LiveData<List<Stock>> allStocks;
    private Observable<Stock> observable;
    private InsertViewModel insertViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        insertViewModel =
                new ViewModelProvider(this).get(InsertViewModel.class);
        View root = inflater.inflate(R.layout.fragment_insert, container, false);

        addStockButton = root.findViewById(R.id.insert_stock);
        deleteStockButton = root.findViewById(R.id.delete_stock);
        nameEditText = root.findViewById(R.id.stock_name_box);
        priceEditText = root.findViewById(R.id.stock_price_box);
        portfolioViewModel = new ViewModelProvider(this).get(PortfolioViewModel.class);
        allStocks = portfolioViewModel.getAllStocks();
        portfolioViewModel.getAllStocks().observe((LifecycleOwner)this,
                new Observer<List<Stock>>() {
                    @Override
                    public void onChanged(List<Stock> stocks) {
                        for (Stock stock : stocks) {
                            if (!allStocks.getValue().contains(stock)){
                                allStocks.getValue().add(stock);
                            }
                        }
                    }
                });

        addStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();
                double price = Double.parseDouble(priceEditText.getText().toString());
                Stock stock = new Stock(name, price);


                if (isStockInDatabase_faster(stock.name)) {
                    inDataBaseAlert();
                    return;
                }

                stock.databaseOperations = DatabaseOperations.INSERT;
                observable = io.reactivex.Observable.just(stock);
                io.reactivex.Observer<Stock> observer = getStockObserver(stock);

                ((io.reactivex.Observable) observable)
                        .observeOn(Schedulers.io())
                        .subscribe(observer);

            }
        });

        deleteStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();
                double price = Double.parseDouble(priceEditText.getText().toString());
                Stock stock = new Stock(name, price);

                if (isStockInDatabase_faster(stock.name) == false){
                    NotinDataBaseAlert();
                    return;
                }

                if(isStockEntered(stock.name)==true){
                    NotEntered();
                    return;
                }

                stock.databaseOperations = DatabaseOperations.DELETE;
                observable = io.reactivex.Observable.just(stock);
                io.reactivex.Observer<Stock> observer = getStockObserver(stock);

                ((io.reactivex.Observable) observable)
                        .observeOn(Schedulers.io())
                        .subscribe(observer);
            }
        });

        return root;
    }

    /*
     * Try in UI thread...:-(
     */
    private boolean isStockInDatabase(String name) {
        Stock stock = portfolioViewModel.getPortfolioDatabase().stockDao().isStockInDatabase(name);
        if (null == stock) {
            return false;
        } else {
            return true;
        }
    }


    private boolean isStockInDatabase_faster(String name) {
        boolean inDB = false;
        for (Stock stock : allStocks.getValue()) {
            if (name.equals(stock.name)) {
                inDB = true;
                break;
            }
        }

        return inDB;
    }

    private boolean isStockEntered(String name){
        boolean isEn = false;
        if(nameEditText == null){
            isEn = true;
        }

        return isEn;
    }

    /*
     * https://developer.android.com/guide/topics/ui/dialogs
     */
    private void inDataBaseAlert() {
        new AlreadyInDatabase().show(getParentFragmentManager(), TAG);
    }

    private void NotinDataBaseAlert() {
        new NotInDatabase().show(getParentFragmentManager(), TAG);
    }

    private void NotEntered() {
        new EmptyStock().show(getParentFragmentManager(), TAG);
    }
    private void listAll() {
        Log.i(TAG, "allStocks size: " + allStocks.getValue().size());
        for (Stock stock : allStocks.getValue()) {
            Log.i(TAG, "Stock: " + stock.name);
            Log.i(TAG, "Stock: " + stock.price);
        }
    }

    private io.reactivex.Observer<Stock> getStockObserver(Stock stock) { // OBSERVER
        return new io.reactivex.Observer<Stock>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "onSubscribe");
            }

            @Override
            public void onNext(@NonNull Stock stock) {
                switch(stock.databaseOperations) {
                    case INSERT:
                        if (!isStockInDatabase(stock.name)) {
                            portfolioViewModel.getPortfolioDatabase().stockDao().insert(stock);
                        }
                        break;
                    case DELETE:
                        portfolioViewModel.getPortfolioDatabase().stockDao().delete(stock);
                        break;
                    case UPDATE:
                        Log.i(TAG, "Update");
                        break;
                    default:
                        Log.i(TAG, "Default");
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "All items are emitted!");
            }
        };
    }
}