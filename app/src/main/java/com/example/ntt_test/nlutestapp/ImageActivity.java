package com.example.ntt_test.nlutestapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import java.util.ArrayList;

public class ImageActivity extends AppCompatActivity {

    // Replace the subscriptionKey string value with your valid subscription key.
    static String subscriptionKey = "678a4900e849413380f04c00066662a5";

    // Verify the endpoint URI.  At this writing, only one endpoint is used for Bing
    // search APIs.  In the future, regional endpoints may be available.  If you
    // encounter unexpected authorization errors, double-check this value against
    // the endpoint for your Bing Web search instance in your Azure dashboard.
    static String host = "https://api.cognitive.microsoft.com";
    static String path = "/bing/v7.0/images/search";
    static String searchTerm = "puppies";
    private String search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);

        Intent intent = getIntent();
        search = intent.getStringExtra("word");

        ImageSearchTask test = new ImageSearchTask(imageView, new WebSearchTaskCallBack() {
            @Override
            public void onWebSearchCompleted(String result) {
            }

            @Override
            public void onImageSearchCompleted(ArrayList<String> result) {
                for(int i = 0;i < result.size();i++){
                    System.out.println(result.get(i));
                }
            }
        });
        test.execute(search);
    }
}