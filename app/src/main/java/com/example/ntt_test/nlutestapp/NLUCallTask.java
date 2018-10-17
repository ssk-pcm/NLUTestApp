package com.example.ntt_test.nlutestapp;

import android.os.AsyncTask;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.CategoriesOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.ConceptsOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features;

public class NLUCallTask extends AsyncTask<String,Void,JsonObject> {
    private String nlu_username = MainActivity.getContext().getResources().getString(R.string.nlu_username);;
    private String nlu_password = MainActivity.getContext().getResources().getString(R.string.nlu_password);;

    @Override
    protected JsonObject doInBackground(String... params) {
        NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding(
                "2018-03-16",
                nlu_username,
                nlu_password
        );

        String text = params[0];
        System.out.println(text);

        CategoriesOptions categories = new CategoriesOptions();

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
                .execute();
        System.out.println(response); //Object形式で帰ってくる


        JsonParser parser = new JsonParser();
        JsonObject result = parser.parse(response.toString()).getAsJsonObject();
//        System.out.println(result);

        return result;
    }
}
