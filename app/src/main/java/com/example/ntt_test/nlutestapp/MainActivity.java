package com.example.ntt_test.nlutestapp;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.CategoriesOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.ConceptsOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private TextView text1, text2, text3, text4;
    private static final int REQUEST_CODE = 1000;
    private String nlu_username;
    private String nlu_password;
    private String input;
    private String res1, res2, res3, res4 = null;
    private String[] res = {"", "", "", ""};
    private int lang;
    private Boolean en = false;
    private static Toast t;
    private SpeechRecognizer sr;

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

        // 言語選択 0:日本語、1:英語、2:オフライン、その他:General
        lang = 0;

        // Autosuggestを使うかの判定を受け取る
        Intent mIntent = getIntent();
        en = mIntent.getBooleanExtra("isSuggest", false);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 音声認識を開始
                startListening();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ImageActivity.class);
                ArrayList<String> listWord = new ArrayList<>();
                if (!res[0].isEmpty()) {
                    for (int i = 0; i < 4; i++) {
                        if (!res[i].isEmpty()) listWord.add(res[i]);
                    }
                    intent.putStringArrayListExtra("listword", listWord);
                    startActivity(intent);
                } else toast("検索ワードがありません");
            }
        });

        wsbtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input = "太陽光";
                // 検索
                webSearch(input);
            }
        });
        wsbtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input = "エヴァンゲリオン";
                if (en) {
                    //英語に翻訳する
                    TranslateTask translate = new TranslateTask();
                    translate.setOnTranslateCallBack(new TranslateTask.TranslateCallBackTask() {

                        @Override
                        public void TranslateCallBack(String result) {
                            super.TranslateCallBack(result);

                            // 検索
                            webSearch(result);
                        }
                    });

                    //翻訳を実行
                    translate.execute(input);
                } else {
                    // 日本語のまま検索する
                    webSearch(input);
                }
            }
        });
        wsbtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input = res1 + " " + res3;
                if (en) {// 英語に翻訳して検索する
                    //英語に翻訳する
                    TranslateTask translate = new TranslateTask();
                    translate.setOnTranslateCallBack(new TranslateTask.TranslateCallBackTask() {

                        @Override
                        public void TranslateCallBack(String result) {
                            super.TranslateCallBack(result);

                            // 検索
                            webSearch(result);
                        }
                    });

                    //翻訳を実行
                    translate.execute(input);
                } else {// 日本語のまま検索する
                    // 検索
                    webSearch(input);
                }
            }
        });
        wsbtn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Button sss = (Button) findViewById(R.id.suggest);


        sss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopListening();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        startListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopListening();
    }

    protected JsonObject callNLU(String inputtext) {
        NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding(
                "2018-03-16",
                nlu_username,
                nlu_password
        );

        //URLを読み込み
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

    // 音声認識を開始する
    protected void startListening() {
        try {
            if (sr == null) {
                sr = SpeechRecognizer.createSpeechRecognizer(this);
                if (!SpeechRecognizer.isRecognitionAvailable(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), "音声認識が使えません",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                sr.setRecognitionListener(new listener());
            }
            // インテントの作成
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            // 言語モデル指定
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.JAPANESE.toString());
//            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                    RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            sr.startListening(intent);
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "startListening()でエラーが起こりました",
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // 音声認識を終了する
    protected void stopListening() {
        if (sr != null) sr.destroy();
        sr = null;
//        toast("音声認識を停止しました");
    }

    // 音声認識を再開する
    public void restartListeningService() {
        stopListening();
        startListening();
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

                // 音声認識のtxt保存
                saveFile(fileName, input);
                if(input.length() == 0){
                    textView.setText(R.string.no_text);
                }
                else{
                    textView.setText(R.string.saved);
                }
                // 検索
                webSearch(input);
            }
        }
    }

    private void webSearch(String word) {

        WebSearchTask webSearchTask = new WebSearchTask(new WebSearchTaskCallBack() {
            @Override
            public void onWebSearchCompleted(final String result) {

                //NLUサービスにメッセージを送信する
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        // NLUサービスを呼び出す
                        JsonObject nluResult = callNLU(result);

                        // AutosuggestをONにする
                        if (en) {
                            // 抽出結果をAutosuggestにかける
                            addSuggest();
                        } else {
                            noSuggest();
                        }
                    }
                });
                thread.start();
            }

            @Override
            public void onImageSearchCompleted(ArrayList<String> e) {

            }
        });
        webSearchTask.execute("filetype:html " + word);
    }

    private void nextIntent() {
        Intent intent = new Intent(MainActivity.this, ImageActivity.class);
        ArrayList<String> listWord = new ArrayList<>();
        if (!res[0].isEmpty()) {
            for (int i = 0; i < 4; i++) {
                if (!res[i].isEmpty()) listWord.add(res[i]);
            }
            intent.putStringArrayListExtra("listword", listWord);
            startActivity(intent);
        } else toast("検索ワードがありません");
    }

    private void addSuggest() {
        final AutosuggestTask suggest = new AutosuggestTask();
        suggest.setOnAutosuggestCallBack(new AutosuggestTask.AutosuggestCallBackTask() {

            @Override
            public void AutosuggestCallBack(final String[] result) {
                super.AutosuggestCallBack(result);

                for (int i = 0; i < 4; i++) {
                    if (result[i] != null) {
                        res[i] = result[i];
                    }
                    System.out.println("res" + i + " is ：" + res[i]);
                }

                final Handler handler = new Handler();
                // Handlerを使用してメイン(UI)スレッドに処理を依頼する
                handler.post(() -> {
                    text1.setText("Result: " + res[0]);
                    text2.setText("Result: " + res[1]);
                    text3.setText("Result: " + res[2]);
                    text4.setText("Result: " + res[3]);

                    nextIntent();
                });

            }
        });

        suggest.execute(
//                res[0] + " " + addKanamoji(),
//                res[1] + " " + addKanamoji(),
//                res[2] + " " + addKanamoji(),
//                res[3] + " " + addKanamoji()
                res[0] + " " + addEnglishLetter(),
                res[1] + " " + addEnglishLetter(),
                res[2] + " " + addEnglishLetter(),
                res[3] + " " + addEnglishLetter()
        );
    }

    private void noSuggest() {
        final Handler mhandler = new Handler(Looper.getMainLooper());
        // Handlerを使用してメイン(UI)スレッドに処理を依頼する
        mhandler.post(() -> {
            text1.setText("Result: " + res[0]);
            text2.setText("Result: " + res[1]);
            text3.setText("Result: " + res[2]);
            text4.setText("Result: " + res[3]);

            nextIntent();
        });
    }

    private static String addKanamoji() {
        Random r = new Random();
        int n = r.nextInt(43);

        String str = "あいうえお" +
                "かきくけこ" +
                "さしすせそ" +
                "たちつてと" +
                "なにぬねの" +
                "はひふへほ" +
                "まみむめも" +
                "やゆよ" +
                "らりるれろ" +
                "わ";

        return str.substring(n + 1, n + 2);
    }

    private static String addEnglishLetter() {
        Random r = new Random();
        int n = r.nextInt(26);

        String str = "abcdefghijklmnopqrstuvwxyz";

        return str.substring(n + 1, n + 2);
    }

    // ファイルを保存
    public void saveFile(String file, String str) {

        // try-with-resources
        try (FileOutputStream fileOutputstream = openFileOutput(file,
                Context.MODE_PRIVATE);){

            fileOutputstream.write(str.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ファイルを読み出し
    public String readFile(String file) {
        String text = null;

        // try-with-resources
        try (FileInputStream fileInputStream = openFileInput(file);
             BufferedReader reader= new BufferedReader(
                     new InputStreamReader(fileInputStream, "UTF-8"))) {

            String lineBuffer;
            while( (lineBuffer = reader.readLine()) != null ) {
                text = lineBuffer ;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return text;
    }

    public static Context getContext() {
        return mContext;
    }

    //トーストが連続して表示されるのを防ぐ
    public void toast(String message) {
        if (t != null) {
            t.cancel();
        }
        t = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        t.show();
    }

    // RecognitionListenerの定義
    // 中が空でも全てのメソッドを書く必要がある
    class listener implements RecognitionListener {
        // 話し始めたときに呼ばれる
        public void onBeginningOfSpeech() {
            /*Toast.makeText(getApplicationContext(), "onBeginningofSpeech",
                    Toast.LENGTH_SHORT).show();*/
        }

        // 結果に対する反応などで追加の音声が来たとき呼ばれる
        // しかし呼ばれる保証はないらしい
        public void onBufferReceived(byte[] buffer) {
        }

        // 話し終わった時に呼ばれる
        public void onEndOfSpeech() {
            /*Toast.makeText(getApplicationContext(), "onEndofSpeech",
                    Toast.LENGTH_SHORT).show();*/
        }

        // ネットワークエラーか認識エラーが起きた時に呼ばれる
        public void onError(int error) {
            String reason = "";
            switch (error) {
                // Audio recording error
                case SpeechRecognizer.ERROR_AUDIO:
                    reason = "ERROR_AUDIO";
                    break;
                // Other client side errors
                case SpeechRecognizer.ERROR_CLIENT:
                    reason = "ERROR_CLIENT";
                    break;
                // Insufficient permissions
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    reason = "ERROR_INSUFFICIENT_PERMISSIONS";
                    break;
                // 	Other network related errors
                case SpeechRecognizer.ERROR_NETWORK:
                    reason = "ERROR_NETWORK";
                    /* ネットワーク接続をチェックする処理をここに入れる */
                    break;
                // Network operation timed out
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    reason = "ERROR_NETWORK_TIMEOUT";
                    break;
                // No recognition result matched
                case SpeechRecognizer.ERROR_NO_MATCH:
                    reason = "ERROR_NO_MATCH";
                    break;
                // RecognitionService busy
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    reason = "ERROR_RECOGNIZER_BUSY";
                    break;
                // Server sends error status
                case SpeechRecognizer.ERROR_SERVER:
                    reason = "ERROR_SERVER";
                    /* ネットワーク接続をチェックをする処理をここに入れる */
                    break;
                // No speech input
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    reason = "ERROR_SPEECH_TIMEOUT";
                    break;
            }
//            Toast.makeText(getApplicationContext(), reason, Toast.LENGTH_SHORT).show();
            toast(reason);
            restartListeningService();
        }

        // 将来の使用のために予約されている
        public void onEvent(int eventType, Bundle params) {
        }

        // 部分的な認識結果が利用出来るときに呼ばれる
        // 利用するにはインテントでEXTRA_PARTIAL_RESULTSを指定する必要がある
        public void onPartialResults(Bundle partialResults) {
        }

        // 音声認識の準備ができた時に呼ばれる
        public void onReadyForSpeech(Bundle params) {
//            toast("話してください");
        }

        // 認識結果が準備できた時に呼ばれる
        public void onResults(Bundle results) {
            // 結果をArrayListとして取得
            ArrayList results_array = results.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
            // 取得した文字列を結合
            String resultsString = "";
//            for (int i = 0; i < results.size(); i++) {
//                resultsString += results_array.get(i) + ";";
//            }
            resultsString += results_array.get(0);
            // トーストを使って結果表示
            text1.setText(resultsString);
            webSearch(resultsString);
        }

        // サウンドレベルが変わったときに呼ばれる
        // 呼ばれる保証はない
        public void onRmsChanged(float rmsdB) {
        }
    }
}
