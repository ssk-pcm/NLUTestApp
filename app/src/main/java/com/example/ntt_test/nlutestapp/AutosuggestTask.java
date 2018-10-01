package com.example.ntt_test.nlutestapp;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.net.ssl.HttpsURLConnection;

import android.os.AsyncTask;

/*
 * Gson: https://github.com/google/gson
 * Maven info:
 *     groupId: com.google.code.gson
 *     artifactId: gson
 *     version: 2.8.1
 *
 * Once you have compiled or downloaded gson-2.8.1.jar, assuming you have placed it in the
 * same folder as this file (Autosuggest.java), you can compile and run this program at
 * the command line as follows.
 *
 * javac Autosuggest.java -classpath .;gson-2.8.1.jar -encoding UTF-8
 * java -cp .;gson-2.8.1.jar Autosuggest
 */
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class AutosuggestTask extends AsyncTask<String, Void, String> {
    static String subscriptionKey = MainActivity.getContext().getResources().getString(R.string.suggest_key2);

    static String host = "https://api.cognitive.microsoft.com";
    static String path = "/bing/v7.0/Suggestions";

    static String mkt = "ja-JP";
    static String query = "鍵盤 ゆ";


    @Override
    protected String doInBackground(String... strings) {
        try {
            String response = get_suggestions();
            System.out.println(prettify(response));
        } catch (Exception e) {
            System.out.println(e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }

    public static String get_suggestions() throws Exception {
        String encoded_query = URLEncoder.encode(query, "UTF-8");
        String params = "?mkt=" + mkt + "&q=" + encoded_query;
        URL url = new URL(host + path + params);

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);
//        connection.setDoOutput(true);

        StringBuilder response = new StringBuilder();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        return response.toString();
    }

    public static String prettify(String json_text) {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(json_text).getAsJsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(json);
    }
}
