package com.example.ntt_test.nlutestapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;
//import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class GoogleSearchTask extends AsyncTask<String , Void, ArrayList<String>> {

    private String key;
    private String cx;
    private String image_cx;
    private String qry = "";
    private String ENDPOINT;
    private ArrayList<String> imageUrl;
    private SearchTaskCallBack callBack;


    GoogleSearchTask(SearchTaskCallBack callBack, Context context) {
        this.callBack = callBack;

        key = context.getResources().getString(R.string.google_custom_search_api_key);
        cx = context.getResources().getString(R.string.google_image_cx);
        image_cx = context.getResources().getString(R.string.google_image_cx);

        imageUrl = new ArrayList<String>();
    }

    @Override
    protected ArrayList<String> doInBackground(String... params) {
        qry = params[0];
        ENDPOINT = "https://www.googleapis.com/customsearch/v1?key=" + key + "&safe=active" +
                "&cx=" + image_cx + "&num=6" + "&q=" + qry + "&searchType=image" + "&imgSize=medium" + "&alt=json";

        // TODO: attempt authentication against a network service.
        HttpURLConnection httpCon = null;
        StringBuilder sb = new StringBuilder();
        JSONObject jsonObject = null;

        try {
            // URL設定
            URL url = new URL(ENDPOINT);

            httpCon = (HttpsURLConnection) url.openConnection();
            httpCon.setRequestMethod("GET");
            httpCon.setInstanceFollowRedirects(false);
            httpCon.setRequestProperty("Accept", "application/json");


            // 時間制限
//            httpCon.setReadTimeout(10000);
//            httpCon.setConnectTimeout(20000);

            // 接続
            httpCon.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (httpCon.getInputStream())));


            String responseData;
            while ((responseData = br.readLine()) != null) {
                sb.append(responseData).append("\n");
            }
            br.close();
            httpCon.disconnect();
            Log.d("responseData: ", sb.toString());
            String jsonFormatString = sb.toString();

            jsonObject = new JSONObject(jsonFormatString);
//            jsonObject = parser.parse(JsonFormatString).getAsJsonObject();
//            jsonObject = new Gson().fromJson(JsonFormatString,);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            if (httpCon != null) {
                httpCon.disconnect();
            }
        }

        // return response JSON object
        assert jsonObject != null;
        Log.d("GoogleSearchResult ", ""+jsonObject);

        try {
//            int ll = jsonObject.getAsJsonArray("items").size();
            int ll = jsonObject.getJSONArray("items").length();
            for (int i = 0; i < ll; i++) {
//                String thumbnaillink = jsonObject.getAsJsonArray("items").get(i).getAsJsonObject().get("image").getAsJsonObject().get("thumbnailLink").toString();
//                String thumbnaillink = jsonObject.getJSONArray("items").getJSONObject(i).getJSONObject("image").get("thumbnailLink").toString();
                String thumbnaillink = jsonObject.getJSONArray("items").getJSONObject(i).get("link").toString();
                Log.d("thumbnail ", thumbnaillink);

                thumbnaillink = thumbnaillink.replaceAll("\"", "");
                imageUrl.add(thumbnaillink);
            }

        } catch (Exception ignored) {

        }

        return imageUrl;
    }

    @Override
    protected void onPostExecute(final ArrayList<String> imageUrl) {
        this.callBack.onImageSearchCompleted(imageUrl);
    }

//    private static SearchResults parseJson(String parseJson) throws Exception {
//
//        String response = parseJson;
//
//        // construct result object for return
//        SearchResults results = new SearchResults(new HashMap<String, String>(), response);
//
//        // extract Bing-related HTTP headers
//        Map<String, List<String>> headers = connection.getHeaderFields();
//        for (String header : headers.keySet()) {
//            if (header == null) continue;      // may have null key
//            if (header.startsWith("BingAPIs-") || header.startsWith("X-MSEdge-")) {
//                results.relevantHeaders.put(header, headers.get(header).get(0));
//            }
//        }
//        return results;
//    }

    @Override
    protected void onCancelled() {

    }
}
