package com.example.ntt_test.nlutestapp;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.CategoriesOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.ConceptsOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String SERVICE_NAME = "natural_language_understanding";
    private static final String URL = "https://gateway.watsonplatform.net/natural-language-understanding/api";

    private TextView text1, text2, text3, text4;
    private static final int REQUEST_CODE = 1000;
    private String nlu_username;
    private String nlu_password;
    private String input;
    private Context mContext;
    private String res1,res2,res3,res4 = "";
    private int lang ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
        nlu_username = mContext.getString(R.string.nlu_username);
        nlu_password = mContext.getString(R.string.nlu_password);

        text1 = findViewById(R.id.result1);
        text2 = findViewById(R.id.result2);
        text3 = findViewById(R.id.result3);
        text4 = findViewById(R.id.result4);
        Button buttonStart = findViewById(R.id.button_start);
        Button next = findViewById(R.id.button);

        // 言語選択 0:日本語、1:英語、2:オフライン、その他:General
        lang = 0;

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 音声認識を開始
                speech();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ImageActivity.class);
                intent.putExtra("word",res1);
                startActivity(intent);
            }
        });
    }

    protected void callNLU(String inputtext) {
        NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding(
                "2018-03-16",
                nlu_username,
                nlu_password
        );
//        String text = "阪神の福留が同点ソロ本塁打を放ち試合を振り出しに戻した。";
        String text = inputtext+" "+inputtext;
        System.out.println(text);

        CategoriesOptions categories = new CategoriesOptions();

        ConceptsOptions concepts = new ConceptsOptions.Builder()
                .limit(3)
                .build();

        Features features = new Features.Builder()
                .categories(categories)
                .concepts(concepts)
                .build();

        AnalyzeOptions parameters = new AnalyzeOptions.Builder()
                .text(text)
                .features(features)
                .build();

        AnalysisResults response = service
                .analyze(parameters)
                .execute();
        System.out.println(response); //Object形式で帰ってくる


        JsonParser parser = new JsonParser();
        JsonObject result = parser.parse(response.toString()).getAsJsonObject();
        System.out.println(result);
//                JSONObject json = new JSONObject();

        // 結果を抽出
        if (result.getAsJsonArray("concepts").size() > 0) {
            res1 = result.getAsJsonArray("concepts").get(0).getAsJsonObject().get("text").getAsString();
        }
        if (result.getAsJsonArray("concepts").size() > 1) {
            res2 = result.getAsJsonArray("concepts").get(1).getAsJsonObject().get("text").getAsString();
        }
        if (result.getAsJsonArray("concepts").size() > 2) {
            res3 = result.getAsJsonArray("concepts").get(2).getAsJsonObject().get("text").getAsString();
        }
        if (result.getAsJsonArray("concepts").size() > 3) {
            res4 = result.getAsJsonArray("concepts").get(3).getAsJsonObject().get("text").getAsString();
        }

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    private void speech(){
        // 音声認識の　Intent インスタンス
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        if(lang == 0){
            // 日本語
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.JAPAN.toString() );
        }
        else if(lang == 1){
            // 英語
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH.toString() );
        }
        else if(lang == 2){
            // Off line mode
            intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
        }
        else{
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        }

        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 100);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "音声を入力");

        try {
            // インテント発行
            startActivityForResult(intent, REQUEST_CODE);
        }
        catch (ActivityNotFoundException e) {
            e.printStackTrace();
            text1.setText("error");
        }
    }

    // 結果を受け取るために onActivityResult を設置
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // 認識結果を ArrayList で取得
            ArrayList<String> candidates =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if(candidates.size() > 0) {
                // 認識結果候補で一番有力なものを表示
//                textView.setText( candidates.get(0));
                input = candidates.get(0);

                // メイン(UI)スレッドでHandlerのインスタンスを生成する
                final Handler handler = new Handler();

                //NLUサービスにメッセージを送信する
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        // NLUサービスを呼び出す
                        callNLU(input);

                        // Handlerを使用してメイン(UI)スレッドに処理を依頼する
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                text1.setText("Result: " + res1);
                                text2.setText("Result: " + res2);
                                text3.setText("Result: " + res3);
                                text4.setText("Result: " + res4);
                            }
                        });
                    }
                });
                thread.start();
            }
        }
    }
}
