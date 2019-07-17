package com.example.ntt_test.nlutestapp;

import android.os.AsyncTask;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.watson.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.natural_language_understanding.v1.model.CategoriesOptions;
import com.ibm.watson.natural_language_understanding.v1.model.ConceptsOptions;
import com.ibm.watson.natural_language_understanding.v1.model.Features;
import static com.example.ntt_test.nlutestapp.MainActivity.getContext;

public class NLUCallTask extends AsyncTask<String,Void,JsonObject> {
    private String nlu_username = getContext().getResources().getString(R.string.nlu_username);
    private String nlu_password = getContext().getResources().getString(R.string.nlu_password);

    private NLUCallBackTask callbacktask;
    private String[] res = {"", "", "", ""};

    @Override
    protected JsonObject doInBackground(String... params) {
        NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding(
                "2019-07-12",
                nlu_username,
                nlu_password
        );

        //URLを読み込み
        String text = params[0];
        System.out.println(text);

        CategoriesOptions categories = new CategoriesOptions.Builder()
                .build();

        ConceptsOptions concepts = new ConceptsOptions.Builder()
                .limit(4)
                .build();

        Features features = new Features.Builder()
                .categories(categories)
                .concepts(concepts)
                .build();

        AnalyzeOptions parameters = new AnalyzeOptions.Builder()
                .url(text)
                .features(features)
                .build();

        AnalysisResults response = service
                .analyze(parameters)
                .execute()
                .getResult();
        System.out.println(response); //Object形式で帰ってくる


        JsonParser parser = new JsonParser();
        JsonObject result = parser.parse(response.toString()).getAsJsonObject();
        System.out.println(result);
//                JSONObject json = new JSONObject();
        // NLUの結果を抽出
        for (int i = 0; i < 4; i++) {
            if (i < result.getAsJsonArray("concepts").size()) {
                res[i] = result.getAsJsonArray("concepts").get(i).getAsJsonObject().get("text").getAsString();
            } else {
                res[i] = "";
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(JsonObject jsonObject) {
        super.onPostExecute(jsonObject);
    }

    public void setOnNLUCallBack(NLUCallBackTask _cbj) {
        callbacktask = _cbj;
    }


    /**
     * コールバック用のstaticなclass
     */
    public static class NLUCallBackTask {
        public void NLUCallBack(String result) {
        }
    }
}
