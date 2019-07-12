package com.example.ntt_test.nlutestapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class GoogleSearchTask extends AsyncTask<Void, Void, JSONObject> {

    private final String key = MainActivity.getContext().getResources().getString(R.string.google_custom_search_api_key);
    private final String qry = "Android";
    private final String cx = MainActivity.getContext().getResources().getString(R.string.google_api_cx);
    private final String image_cx = MainActivity.getContext().getResources().getString(R.string.google_image_cx);
    private final String ENDPOINT = "https://www.googleapis.com/customsearch/v1?key=" + key + "&cx="+ cx +"&q=" + qry + "&alt=json";

    GoogleSearchTask(String email, String password) {

    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        // TODO: attempt authentication against a network service.
        HttpURLConnection httpCon = null;
        StringBuffer sb = new StringBuffer();
        JSONObject jsonObject = null;

        try {
            // URL設定
            URL url = new URL(ENDPOINT);

            httpCon = (HttpsURLConnection) url.openConnection();
            httpCon.setRequestMethod("GET");
            httpCon.setInstanceFollowRedirects(false);
            httpCon.setRequestProperty("Accept", "application/json");


            // 時間制限
            httpCon.setReadTimeout(10000);
            httpCon.setConnectTimeout(20000);

            // 接続
            httpCon.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (httpCon.getInputStream())));

            httpCon.disconnect();
            String responseData;
            responseData = br.toString();
            Log.d("responseData: ", responseData);

            jsonObject = new JSONObject(responseData);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (httpCon != null) {
                httpCon.disconnect();
            }
        }

        // return response JSON object
        Log.d("jsonObject: ", "" + jsonObject);

        return jsonObject;
    }

    @Override
    protected void onPostExecute(final JSONObject jsonObject) {

    }

    @Override
    protected void onCancelled() {

    }
}
