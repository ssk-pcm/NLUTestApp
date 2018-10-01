package com.example.ntt_test.nlutestapp;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private static final String SERVICE_NAME = "natural_language_understanding";
    private static final String URL = "https://gateway.watsonplatform.net/natural-language-understanding/api";

    private TextView text1, text2, text3, text4;
    private static final int REQUEST_CODE = 1000;
    private String nlu_username;
    private String nlu_password;
    private String input;
    private String res1, res2, res3, res4 = null;
    private int lang;
    private Boolean en = false;

    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        mContext = getApplicationContext();
        nlu_username = mContext.getString(R.string.nlu_username);
        nlu_password = mContext.getString(R.string.nlu_password);

        text1 = findViewById(R.id.result1);
        text2 = findViewById(R.id.result2);
        text3 = findViewById(R.id.result3);
        text4 = findViewById(R.id.result4);
        Button buttonStart = findViewById(R.id.button_start);
        Button next = findViewById(R.id.button);
        Button wsbtn1 = findViewById(R.id.wsbtn1);
        Button wsbtn2 = findViewById(R.id.wsbtn2);
        Button wsbtn3 = findViewById(R.id.wsbtn3);
        Button wsbtn4 = findViewById(R.id.wsbtn4);
        Switch enSwich = findViewById(R.id.enswich);

        enSwich.setOnCheckedChangeListener(this);

        // 言語選択 0:日本語、1:英語、2:オフライン、その他:General
        lang = 0;

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ボタンを押すたびにresを空にする
                res1 = null;
                res2 = null;
                res3 = null;
                res4 = null;
                // 音声認識を開始
                speech();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ImageActivity.class);
                if (res1 != null) {
                    ArrayList<String> listWord = new ArrayList<>();
                    intent.putExtra("word", res1);
                    listWord.add(res1);
                    if (res2 != null) listWord.add(res2);
                    if (res3 != null) listWord.add(res3);
                    if (res4 != null) listWord.add(res4);
                    intent.putStringArrayListExtra("listword", listWord);
                    startActivity(intent);
                }
            }
        });

        wsbtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input="太陽光";
                if(en){// 英語に翻訳して検索する
                    //英語に翻訳する
                    TranslateTask translate = new TranslateTask();
                    translate.setOnCallBack(new TranslateTask.CallBackTask() {

                        @Override
                        public void CallBack(String result) {
                            super.CallBack(result);

                            // 検索
                            webSearch(result);
                        }
                    });

                    //翻訳を実行
                    translate.execute(input);
                }else{// 日本語のまま検索する
                    // 検索
                    webSearch(input);
                }

            }
        });
        wsbtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input=res1 + " " + res2;
                if(en){// 英語に翻訳して検索する
                    //英語に翻訳する
                    TranslateTask translate = new TranslateTask();
                    translate.setOnCallBack(new TranslateTask.CallBackTask() {

                        @Override
                        public void CallBack(String result) {
                            super.CallBack(result);

                            // 検索
                            webSearch(result);
                        }
                    });

                    //翻訳を実行
                    translate.execute(input);
                }else{// 日本語のまま検索する
                    // 検索
                    webSearch(input);
                }
            }
        });
        wsbtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input=res1 + " " + res3;
                if(en){// 英語に翻訳して検索する
                    //英語に翻訳する
                    TranslateTask translate = new TranslateTask();
                    translate.setOnCallBack(new TranslateTask.CallBackTask() {

                        @Override
                        public void CallBack(String result) {
                            super.CallBack(result);

                            // 検索
                            webSearch(result);
                        }
                    });

                    //翻訳を実行
                    translate.execute(input);
                }else{// 日本語のまま検索する
                    // 検索
                    webSearch(input);
                }
            }
        });
        wsbtn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input=res2 + " " + res3;
                if(en){// 英語に翻訳して検索する
                    //英語に翻訳する
                    TranslateTask translate = new TranslateTask();
                    translate.setOnCallBack(new TranslateTask.CallBackTask() {

                        @Override
                        public void CallBack(String result) {
                            super.CallBack(result);

                            // 検索
                            webSearch(result);
                        }
                    });

                    //翻訳を実行
                    translate.execute(input);
                }else{// 日本語のまま検索する
                    // 検索
                    webSearch(input);
                }
            }
        });

        Button sss = (Button)findViewById(R.id.suggest);
        sss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AutosuggestTask suggest = new AutosuggestTask();
                suggest.execute();
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
        String text = inputtext;
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

    private void speech() {
        // 音声認識の　Intent インスタンス
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        if (lang == 0) {
            // 日本語
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.JAPAN.toString());
        } else if (lang == 1) {
            // 英語
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH.toString());
        } else if (lang == 2) {
            // Off line mode
            intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
        } else {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        }

        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 100);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "音声を入力");
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        SpeechRecognizer recognizer = SpeechRecognizer.createSpeechRecognizer(this);

        try {
            // インテント発行
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            text1.setText("error");
        }
    }

    // 音声認識の結果受け取り
    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // 認識結果を ArrayList で取得
            ArrayList<String> candidates =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (candidates.size() > 0) {
                input = candidates.get(0);


                if(en){// 英語に翻訳して検索する
                    //英語に翻訳する
                    TranslateTask translate = new TranslateTask();
                    translate.setOnCallBack(new TranslateTask.CallBackTask() {

                        @Override
                        public void CallBack(String result) {
                            super.CallBack(result);

                            // 検索
                            webSearch(result);
                        }
                    });

                    //翻訳を実行
                    translate.execute(input);
                }else{// 日本語のまま検索する
                    // 検索
                    webSearch(input);
                }
            }
        }
    }

    private void webSearch(String word) {
        WebSearchTask webSearchTask = new WebSearchTask(new WebSearchTaskCallBack() {
            @Override
            public void onWebSearchCompleted(final String result) {
                // メイン(UI)スレッドでHandlerのインスタンスを生成する
                final Handler handler = new Handler();

                //NLUサービスにメッセージを送信する
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        // NLUサービスを呼び出す
                        callNLU(result);

                        // Handlerを使用してメイン(UI)スレッドに処理を依頼する
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                text1.setText("Result: " + res1);
                                text2.setText("Result: " + res2);
                                text3.setText("Result: " + res3);
                                text4.setText("Result: " + res4);
                                System.out.println(res1 + "\n" + res2 + "\n" + res3 + "\n" + res4);
                            }
                        });
                        // 画像検索開始
                        //nextIntent();
                    }
                });
                thread.start();
            }

            @Override
            public void onImageSearchCompleted(ArrayList<String> e) {

            }
        });
        webSearchTask.execute("filetype:html "+word);
    }

    private void nextIntent() {
        Intent intent = new Intent(MainActivity.this, ImageActivity.class);
        if (res1 != null) {
            ArrayList<String> listWord = new ArrayList<>();
            intent.putExtra("word", res1);
            listWord.add(res1);
            if (res2 != null) listWord.add(res2);
            if (res3 != null) listWord.add(res3);
            if (res4 != null) listWord.add(res4);
            intent.putStringArrayListExtra("listword", listWord);
            startActivity(intent);
        }
    }

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(b) {
            //do stuff when Switch is ON
            en = true;
        } else {
            //do stuff when Switch if OFF
            en = false;
        }
    }
}
