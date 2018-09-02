package com.example.ntt_test.nlutestapp;

import android.os.AsyncTask;

import java.io.*;
import java.net.*;
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
    static String subscriptionKey = MainActivity.getContext().getResources().getString(R.string.translator_key1);

    static String host = "https://api.cognitive.microsofttranslator.com";
    static String path = "/translate?api-version=3.0";

    // Translate to German and Italian.
    static String params = "&to=en";

    //static String text = "Hello world!";
    private CallBackTask callbacktask;

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
        response = response.replace("[","");
        response = response.replace("]","");

        JsonParser parser = new JsonParser();
        JsonObject jo = parser.parse(response).getAsJsonObject();

        System.out.println("translate word : "+jo.getAsJsonObject("translations").get("text").toString());
        String translateWord = jo.getAsJsonObject("translations").get("text").toString();
        return translateWord;
    }


    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        callbacktask.CallBack(result);
    }


    public void setOnCallBack(CallBackTask _cbj) {
        callbacktask = _cbj;
    }


    /**
     * コールバック用のstaticなclass
     */
    public static class CallBackTask {
        public void CallBack(String result) {
        }
    }

    public static String Translate (String text) throws Exception {
        URL url = new URL (host + path + params);

        List<RequestBody> objList = new ArrayList<RequestBody>();
        objList.add(new RequestBody(text));
        String content = new Gson().toJson(objList);

        return Post(url, content);
    }

    public static String prettify(String json_text) {
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

    public static String Post (URL url, String content) throws Exception {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        //connection.setRequestProperty("Content-Length", content.length() + "");
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);
        connection.setRequestProperty("X-ClientTraceId", java.util.UUID.randomUUID().toString());
        connection.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        byte[] encoded_content = content.getBytes("UTF-8");
        wr.write(encoded_content, 0, encoded_content.length);
        wr.flush();
        wr.close();

        StringBuilder response = new StringBuilder ();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        return response.toString();
    }
}