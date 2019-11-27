package com.example.ntt_test.nlutestapp;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.ibm.watson.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.natural_language_understanding.v1.model.CategoriesOptions;
import com.ibm.watson.natural_language_understanding.v1.model.ConceptsOptions;
import com.ibm.watson.natural_language_understanding.v1.model.Features;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    private Boolean en = false;
    private static Toast t;
    private SpeechRecognizer sr;
    private String textFileName, timeLog, csvFileName;
    private Intent mNextIntent;
    private Boolean listenFlag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        mContext = this;
        nlu_username = this.getString(R.string.nlu_username);
        nlu_password = this.getString(R.string.nlu_password);

        text1 = findViewById(R.id.result1);
        text2 = findViewById(R.id.result2);
        text3 = findViewById(R.id.result3);
        text4 = findViewById(R.id.result4);
        Button buttonStart = findViewById(R.id.button_start);
        Button next = findViewById(R.id.button);
        Intent mIntent = getIntent();
        mNextIntent = new Intent(MainActivity.this, ImageActivity.class);
        timeLog = mIntent.getStringExtra("timeLog");
        textFileName = timeLog + ".txt";
//        String csvFileName = timeLog + ".csv";
        csvFileName = mIntent.getStringExtra("csvFileName");
//        saveCsvFile(csvFileName);

        // 言語選択 0:日本語、1:英語、2:オフライン、その他:General
        int lang = 0;

        // Autosuggestを使うかの判定を受け取る
        en = mIntent.getBooleanExtra("isSuggest", false);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 音声認識を開始
                listenFlag = true;
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
                } else toast("MainActivity:検索ワードがありません");
            }
        });

        Button sss = (Button) findViewById(R.id.suggest);


        sss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopListening();
                MediaScanner ms = new MediaScanner(csvFileName, MainActivity.this);
                ms.mediaScan();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (listenFlag) startListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopListening();
        listenFlag = false;
    }

    protected void callNLU(String inputText) {
        NLUCallTask task = new NLUCallTask(this,Constants.ANALYZE_FORMAT.URL);
        task.setOnNLUCallBack(new NLUCallTask.NLUCallBackTask() {
            @Override
            public void NLUCallBack(JsonObject result) {
                super.NLUCallBack(result);

                try {
                    for (int i = 0; i < 4; i++) {
                        if (i < result.getAsJsonArray("concepts").size()) {
                            res[i] = result.getAsJsonArray("concepts").get(i).getAsJsonObject().get("text").getAsString();
                        } else {
                            res[i] = "";
                        }
                    }
                } catch (JsonParseException | NullPointerException e) {
//                    nluTask.execute(readFile(fileName));
                }

                // AutosuggestをONにする
                if (en) {
                    // 抽出結果をAutosuggestにかける
                    addSuggest();
                } else {
                    noSuggest();
                }
            }
        });
        task.execute(inputText);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // Checks if external storage is available for read and write
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
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
//            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
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

    private void webSearch(String word) {

        WebSearchTask webSearchTask = new WebSearchTask(new SearchTaskCallBack() {
            @Override
            public void onWebSearchCompleted(final String result) {

                //NLUサービスにメッセージを送信する
//                Thread thread = new Thread(new Runnable() {
//                    public void run() {
                // NLUサービスを呼び出す
                // TODO: NLUの呼び出しをAsyncTaskに切り替える
                callNLU(result);


//                    }
//                });
//                thread.start();
            }

            @Override
            public void onImageSearchCompleted(ArrayList<String> e) {

            }
        }, this);
        webSearchTask.execute("filetype:html " + word);
    }

    private void nextIntent() {
        ArrayList<String> listWord = new ArrayList<>();
        if (!res[0].isEmpty()) {
            for (int i = 0; i < 4; i++) {
                if (!res[i].isEmpty()) listWord.add(res[i]);
            }
            mNextIntent.putStringArrayListExtra("listword", listWord);
            mNextIntent.putExtra("isSuggest", en);
            mNextIntent.putExtra("csvFileName", csvFileName);
            listenFlag = false;
            startActivity(mNextIntent);
        } else toast("nextIntent:検索ワードがありません");
    }

    private void addSuggest() {
        final AutosuggestTask suggest = new AutosuggestTask(this);
        suggest.setOnAutosuggestCallBack(new AutosuggestTask.AutosuggestCallBackTask() {

            @Override
            public void AutosuggestCallBack(final String[] result) {
                super.AutosuggestCallBack(result);

                for (int i = 0; i < 4; i++) {
                    if (result[i] != null) {
                        res[i] = result[i];
                    }
                    Log.d("AddSuggest : " + i , res[i]);
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
                res[0] + " " + addKanamoji(),
                res[1] + " " + addKanamoji(),
                res[2] + " " + addKanamoji(),
                res[3] + " " + addKanamoji()
//                res[0] + " " + addEnglishLetter(),
//                res[1] + " " + addEnglishLetter(),
//                res[2] + " " + addEnglishLetter(),
//                res[3] + " " + addEnglishLetter()
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
        try{
            FileOutputStream fileOutputstream = new FileOutputStream(new File(
                    Environment.getExternalStorageDirectory().getPath()
                            + "/NLUTestApp/" + file));
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
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(fileInputStream, StandardCharsets.UTF_8))) {

            String lineBuffer;
            while ((lineBuffer = reader.readLine()) != null) {
                text = lineBuffer;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return text;
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

            ArrayList results_array = partialResults.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
            toast(results_array.get(0).toString());
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

            // ファイルネームの保存
            mNextIntent.putExtra("timeLog", timeLog);
            // 音声認識のtxt保存
            saveFile(textFileName, resultsString);
            // 結果表示
            text1.setText(resultsString);

            webSearch(resultsString);
        }

        // サウンドレベルが変わったときに呼ばれる
        // 呼ばれる保証はない
        public void onRmsChanged(float rmsdB) {
        }
    }
}
