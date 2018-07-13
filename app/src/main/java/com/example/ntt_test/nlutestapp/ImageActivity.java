package com.example.ntt_test.nlutestapp;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

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
    private GridView mGridView;
    private Button nextbtn;
    private TextView wordtext;
    private int count = 0;
    private ArrayList<String> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        mGridView = (GridView) findViewById(R.id.gridView);
        nextbtn = (Button) findViewById(R.id.nextbtn);
        wordtext = (TextView) findViewById(R.id.wordtext);

        Intent intent = getIntent();

        list.addAll(intent.getStringArrayListExtra("listword"));
        search = intent.getStringExtra("word");

        ImageSearchTask test = new ImageSearchTask(new WebSearchTaskCallBack() {
            @Override
            public void onWebSearchCompleted(String result) {
            }

            @Override
            public void onImageSearchCompleted(ArrayList<String> result) {
                wordtext.setText(list.get(count));
                for (int i = 0; i < result.size(); i++) {
                    System.out.println(result.get(i));
                    BitmapAdapter adapter = new BitmapAdapter(
                            getApplicationContext(),
                            result
                    );
                    mGridView.setAdapter(adapter);
                }
                count++;
            }
        });
        test.execute(list.get(count));

        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (list.size() > count) {
                    ImageSearchTask test = new ImageSearchTask(new WebSearchTaskCallBack() {
                        @Override
                        public void onWebSearchCompleted(String result) {
                        }

                        @Override
                        public void onImageSearchCompleted(ArrayList<String> result) {
                            wordtext.setText(list.get(count));
                            for (int i = 0; i < result.size(); i++) {
                                System.out.println(result.get(i));
                                BitmapAdapter adapter = new BitmapAdapter(
                                        getApplicationContext(),
                                        result
                                );
                                mGridView.setAdapter(adapter);

                            }
                            count++;
                        }
                    });
                    test.execute(list.get(count));
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}