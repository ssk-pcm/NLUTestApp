package com.example.ntt_test.nlutestapp;

import java.util.ArrayList;

public interface WebSearchTaskCallBack {
    public void onWebSearchCompleted(String result);
    public void onImageSearchCompleted(ArrayList<String> result);
}
