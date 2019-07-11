package com.example.ntt_test.nlutestapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

public class GoogleSearchTask extends AsyncTask<Void, Void, JSONObject> {

    private final String key = "";
    private final String qry = "Android";
    private final String ENDPOINT = "https://www.googleapis.com/customsearch/v1?key=" + key + "&cx=013036536707430787589:_pqjad5hr1a&q=" + qry + "&alt=json";
    private final String API_NAME = "login.php";
    private final String APP_PATH = "r87GUHdwef3r14F76DRTsdfh";

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
            URL url = new URL(ENDPOINT + API_NAME);

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
