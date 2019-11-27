package com.example.ntt_test.nlutestapp;

import android.content.Context;
import android.os.AsyncTask;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.net.ssl.HttpsURLConnection;

/*
 * Gson: https://github.com/google/gson
 * Maven info:
 *     groupId: com.google.code.gson
 *     artifactId: gson
 *     version: 2.8.1
 */
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class TranslateTask extends AsyncTask<String,Void,String> {

    // Replace the subscriptionKey string value with your valid subscription key.
    private static String subscriptionKey;

    //static String text = "Hello world!";
    private TranslateCallBackTask callbacktask;

    TranslateTask(Context context){
        subscriptionKey = context.getResources().getString(R.string.translator_key1);
    }

    @Override
    protected String doInBackground(String... params) {
        String response = null;
        try {
            response = Translate (params[0]);
            //System.out.println (prettify (response));
        }
        catch (Exception e) {
            System.out.println (e);
        }
        System.out.println(prettify(response));
        assert response != null;
        response = response.replace("[","");
        response = response.replace("]","");

        JsonParser parser = new JsonParser();
        JsonObject jo = parser.parse(response).getAsJsonObject();

//        System.out.println("translate word : "+jo.getAsJsonObject("translations").get("text").toString());
        return jo.getAsJsonObject("translations").get("text").toString(); // translate word
    }


    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        callbacktask.TranslateCallBack(result);
    }


    void setOnTranslateCallBack(TranslateCallBackTask _cbj) {
        callbacktask = _cbj;
    }


    /**
     * コールバック用のstaticなclass
     */
    public static class TranslateCallBackTask {
        public void TranslateCallBack(String result) {
        }
    }

    private static String Translate(String text) throws Exception {
        String host = "https://api.cognitive.microsofttranslator.com";
        String path = "/translate?api-version=3.0";
        // Translate to German and Italian.
        String params = "&to=en";
        URL url = new URL (host + path + params);

        List<RequestBody> objList = new ArrayList<RequestBody>();
        objList.add(new RequestBody(text));
        String content = new Gson().toJson(objList);

        return Post(url, content);
    }

    private static String prettify(String json_text) {
        JsonParser parser = new JsonParser();
        JsonElement json = parser.parse(json_text);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        //System.out.println(gson.toJson(json));
        return gson.toJson(json);
    }

    public static class RequestBody {
        String Text;

        public RequestBody(String text) {
            this.Text = text;
        }
    }

    private static String Post(URL url, String content) throws Exception {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        //connection.setRequestProperty("Content-Length", content.length() + "");
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);
        connection.setRequestProperty("X-ClientTraceId", java.util.UUID.randomUUID().toString());
        connection.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        byte[] encoded_content = content.getBytes(StandardCharsets.UTF_8);
        wr.write(encoded_content, 0, encoded_content.length);
        wr.flush();
        wr.close();

        StringBuilder response = new StringBuilder ();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        return response.toString();
    }
}