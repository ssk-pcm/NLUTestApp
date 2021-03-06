package com.example.ntt_test.nlutestapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class ImageSearchTask extends AsyncTask<String, Void, Bitmap> {

    // Replace the subscriptionKey string value with your valid subscription key.
    static String subscriptionKey;

    // Verify the endpoint URI.  At this writing, only one endpoint is used for Bing
    // search APIs.  In the future, regional endpoints may be available.  If you
    // encounter unexpected authorization errors, double-check this value against
    // the endpoint for your Bing Web search instance in your Azure dashboard.
    static String host = "https://api.cognitive.microsoft.com";
    static String path = "/bing/v7.0/images/search";
    private Bitmap thumbnail = null;
    private ArrayList<String> imageUrl;
    //    static String searchTerm = "タイヤ";
    private WebSearchTaskCallBack callBack;

    static JsonObject imageObject;

    public ImageSearchTask(WebSearchTaskCallBack callBack) {
     //   mImageView = imageView;
        this.callBack = callBack;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        subscriptionKey = MainActivity.getContext().getResources().getString(R.string.azure_api_key1);
        imageUrl = new ArrayList<String>();
    }

    // 非同期処理
    @Override
    protected Bitmap doInBackground(String... params) {
        //画像検索
        imageObject = imageGet(params[0]);

            for(int i = 0;i < imageObject.getAsJsonArray("value").size();i++){
                try{
                    String addUrl =imageObject.getAsJsonArray("value").get(i).getAsJsonObject().get("thumbnailUrl").toString();
                    addUrl = addUrl.replaceAll("\"", "");
                    imageUrl.add(addUrl);
                }catch(NullPointerException e){
                    System.out.println("");
                }
            }
        return thumbnail;
    }

    // 非同期処理が終了後、結果をメインスレッドに返す
    @Override
    protected void onPostExecute(Bitmap result) {
//        final ImageView imageView = mImageView;
//        imageView.setImageBitmap(result);

        this.callBack.onImageSearchCompleted(imageUrl);
    }

    public static SearchResults SearchImages(String searchQuery) throws Exception {
        // construct URL of search request (endpoint + query string)
        URL url = new URL(host + path + "?q=" + URLEncoder.encode(searchQuery, "UTF-8"));
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);

        // receive JSON body
        InputStream stream = connection.getInputStream();
        String response = new Scanner(stream).useDelimiter("\\A").next();

        // construct result object for return
        SearchResults results = new SearchResults(new HashMap<String, String>(), response);

        // extract Bing-related HTTP headers
        Map<String, List<String>> headers = connection.getHeaderFields();
        for (String header : headers.keySet()) {
            if (header == null) continue;      // may have null key
            if (header.startsWith("BingAPIs-") || header.startsWith("X-MSEdge-")) {
                results.relevantHeaders.put(header, headers.get(header).get(0));
            }
        }
        stream.close();
        return results;
    }

    // pretty-printer for JSON; uses GSON parser to parse and re-serialize
    public static String prettify(String json_text) {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(json_text).getAsJsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(json);
    }


    public static JsonObject imageGet(String searchTerm) {
        JsonObject resultObject = new JsonObject();

        if (subscriptionKey.length() != 32) {
            System.out.println("Invalid Bing Search API subscription key!");
            System.out.println("Please paste yours into the source code.");
            System.exit(1);
        }


        try {
            System.out.println("Searching the Web for: " + searchTerm);

            SearchResults result = SearchImages(searchTerm);

            System.out.println("\nRelevant HTTP Headers:\n");
            for (String header : result.relevantHeaders.keySet())
                System.out.println(header + ": " + result.relevantHeaders.get(header));

            System.out.println("\nJSON Response:\n");
           // System.out.println(prettify(result.jsonResponse));

            JsonParser parser = new JsonParser();
            resultObject = parser.parse(result.jsonResponse).getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(1);
        }
        return resultObject;
    }

}