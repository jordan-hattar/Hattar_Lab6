package com.example.lab6_1.Insert;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class InsertViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public InsertViewModel() {
        mText = new MutableLiveData<>();
    }

    public LiveData<String> getText() {
        return mText;
    }
}