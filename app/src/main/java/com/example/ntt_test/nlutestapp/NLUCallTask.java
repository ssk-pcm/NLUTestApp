package com.example.ntt_test.nlutestapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.natural_language_understanding.v1.model.CategoriesOptions;
import com.ibm.watson.natural_language_understanding.v1.model.ConceptsOptions;
import com.ibm.watson.natural_language_understanding.v1.model.Features;


public class NLUCallTask extends AsyncTask<String, Void, JsonObject> {
    private String nlu_username;
    private String nlu_password;
    private String API_KEY;
    private JsonObject result = new JsonObject();
    private NLUCallBackTask callbacktask;
    private int format;

    NLUCallTask(Context context, int format) {
        nlu_username = context.getResources().getString(R.string.nlu_username);
        nlu_password = context.getResources().getString(R.string.nlu_password);
        API_KEY = context.getResources().getString(R.string.NLU_API_KEY);
        this.format = format;
    }

    @Override
    protected JsonObject doInBackground(String... params) {
//        NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding(
//                "2019-07-12",
//                nlu_username,
//                nlu_password
//        );
        IamAuthenticator authenticator = new IamAuthenticator(API_KEY);
        NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding("2019-07-12",authenticator);


        NaturalLanguageUnderstanding naturalLanguageUnderstanding = new NaturalLanguageUnderstanding("2019-07-12", authenticator);
        service.setServiceUrl("https://gateway.watsonplatform.net/natural-language-understanding/api");

        try {
            //テキストを読み込み
            String text = params[0];
            Log.d("Send NLU text : ",text);

            CategoriesOptions categories = new CategoriesOptions.Builder()
                    .build();

            ConceptsOptions concepts = new ConceptsOptions.Builder()
                    .limit(4)
                    .build();

            Features features = new Features.Builder()
                    .categories(categories)
                    .concepts(concepts)
                    .build();

            AnalyzeOptions parameters;
            if (format == Constants.ANALYZE_FORMAT.URL) {
                parameters = new AnalyzeOptions.Builder()
                        .url(text)
                        .features(features)
                        .build();
            } else {
                parameters = new AnalyzeOptions.Builder()
                        .text(text)
                        .features(features)
                        .build();
            }

            AnalysisResults response = service
                    .analyze(parameters)
                    .execute()
                    .getResult();
//            System.out.println(response); //Object形式で帰ってくる


            JsonParser parser = new JsonParser();
            result = parser.parse(response.toString()).getAsJsonObject();
            Log.d("NLUCallTask",result.toString());
        } catch (RuntimeException e) {
            Log.d("Watson Error", "" + e);
        }
//                JSONObject json = new JSONObject();
        // NLUの結果を抽出
//        for (int i = 0; i < 4; i++) {
//            if (i < result.getAsJsonArray("concepts").size()) {
//                res[i] = result.getAsJsonArray("concepts").get(i).getAsJsonObject().get("text").getAsString();
//            } else {
//                res[i] = "";
//            }
//        }
        return result;
    }

    @Override
    protected void onPostExecute(JsonObject jsonObject) {
        super.onPostExecute(jsonObject);
        callbacktask.NLUCallBack(jsonObject);
    }

    public void setOnNLUCallBack(NLUCallBackTask _cbj) {
        callbacktask = _cbj;
    }


    /**
     * コールバック用のstaticなclass
     */
    public static class NLUCallBackTask {
        public void NLUCallBack(JsonObject result) {
        }
    }
}
