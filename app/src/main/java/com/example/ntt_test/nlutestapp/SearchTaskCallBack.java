package com.example.ntt_test.nlutestapp;

import java.util.ArrayList;

public interface SearchTaskCallBack {
    public void onWebSearchCompleted(String result);
    public void onImageSearchCompleted(ArrayList<String> result);
}
