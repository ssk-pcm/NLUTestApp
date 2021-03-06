package com.example.ntt_test.nlutestapp;

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
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class ImageActivity extends AppCompatActivity {

    // Verify the endpoint URI.  At this writing, only one endpoint is used for Bing
    // search APIs.  In the future, regional endpoints may be available.  If you
    // encounter unexpected authorization errors, double-check this value against
    // the endpoint for your Bing Web search instance in your Azure dashboard.
    static String host = "https://api.cognitive.microsoft.com";
    static String path = "/bing/v7.0/images/search";
    static String searchTerm = "puppies";
    //private String search;
    private GridView mGridView;
    private Button nextbtn;
    private TextView wordtext,cv;
    private int count = 0;
    private ArrayList<String> list = new ArrayList<>();
    private static final int REQUEST_CODE = 1000;
    private static Toast t;
    private SpeechRecognizer sr;
    private String input;
    private Intent getDataIntent, restartIntent;
    private Boolean restartFlag = false;
    private Boolean isSuggest = false;
    private String fileName, buffer;
    private String[] res = {"", "", "", ""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        mGridView = (GridView) findViewById(R.id.gridView);
        nextbtn = (Button) findViewById(R.id.nextbtn);
        wordtext = (TextView) findViewById(R.id.wordtext);
        cv = (TextView)findViewById(R.id.countview);
        getDataIntent = getIntent();
        restartIntent = new Intent(ImageActivity.this,ImageActivity.class);
        fileName = getDataIntent.getStringExtra("fileName");

        list.addAll(getDataIntent.getStringArrayListExtra("listword"));
//        search = intent.getStringExtra("word");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 画像検索開始
        imageSearch(0);
        // 同時に認識開始
        startListening();

        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 画像検索開始
//                imageSearch(count);
            }
        });

        // 自動遷移
        for (int i = 1; i < list.size(); i++) {
            delayExecute(i);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (restartFlag) {
            restartFlag = false;
          //  restartIntent.setClass(ImageActivity, ImageActivity.getClass());
            startActivity(restartIntent);
        } else deleteFile(fileName);
    }

    private void delayExecute(int times) {
        // 自動遷移
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                imageSearch(times);
                startListening();
            }
        }, 10000 * (times));
    }

    private void imageSearch(int num) {
        if (list.size() > num) {
            ImageSearchTask test = new ImageSearchTask(new WebSearchTaskCallBack() {
                @Override
                public void onWebSearchCompleted(String result) {
                }

                @Override
                public void onImageSearchCompleted(ArrayList<String> result) {
                    wordtext.setText(list.get(num));
                    cv.setText(String.valueOf(num));
                    for (int i = 0; i < result.size(); i++) {
                        System.out.println(result.get(i));
                        BitmapAdapter adapter = new BitmapAdapter(
                                getApplicationContext(),
                                result
                        );
                        mGridView.setAdapter(adapter);
                    }
                    if (num + 1 == list.size()) {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Listの最後に実行される
                                NLUCallTask nluTask = new NLUCallTask();
                                nluTask.setOnNLUCallBack(new NLUCallTask.NLUCallBackTask() {
                                    @Override
                                    public void NLUCallBack(JsonObject result) {
                                        super.NLUCallBack(result);
                                        try{
                                            for (int i = 0; i < 4; i++) {
                                                if (i < result.getAsJsonArray("concepts").size()) {
                                                    res[i] = result.getAsJsonArray("concepts").get(i).getAsJsonObject().get("text").getAsString();
                                                } else {
                                                    res[i] = "";
                                                }
                                            }
                                        }catch(JsonParseException e){
                                            nluTask.execute(readFile(fileName));
                                        }
                                        ArrayList<String> listWord = new ArrayList<>();
                                        if (!res[0].isEmpty()) {
                                            for (int i = 0; i < 4; i++) {
                                                if (!res[i].isEmpty()) listWord.add(res[i]);
                                            }
                                            restartIntent.putStringArrayListExtra("listword", listWord);
                                            // ファイルネームの保存
                                            restartIntent.putExtra("fileName", fileName);
                                            restartFlag = true;
                                        } else toast("検索ワードがありません");

                                        finish();
                                    }
                                });
                                nluTask.execute(readFile(fileName));
                            }
                        }, 10000);
                    }
                }
            });
            test.execute(list.get(num));
        }
    }

//    private void addSuggest() {
//        final AutosuggestTask suggest = new AutosuggestTask();
//        suggest.setOnAutosuggestCallBack(new AutosuggestTask.AutosuggestCallBackTask() {
//
//            @Override
//            public void AutosuggestCallBack(final String[] result) {
//                super.AutosuggestCallBack(result);
//
//                for (int i = 0; i < 4; i++) {
//                    if (result[i] != null) {
//                        res[i] = result[i];
//                    }
//                    System.out.println("res" + i + " is ：" + res[i]);
//                }
//
//                final Handler handler = new Handler();
//                // Handlerを使用してメイン(UI)スレッドに処理を依頼する
//                handler.post(() -> {
//                    text1.setText("Result: " + res[0]);
//                    text2.setText("Result: " + res[1]);
//                    text3.setText("Result: " + res[2]);
//                    text4.setText("Result: " + res[3]);
//
//                    nextIntent();
//                });
//
//            }
//        });
//
//        suggest.execute(
//                res[0] + " " + addKanamoji(),
//                res[1] + " " + addKanamoji(),
//                res[2] + " " + addKanamoji(),
//                res[3] + " " + addKanamoji()
////                res[0] + " " + addEnglishLetter(),
////                res[1] + " " + addEnglishLetter(),
////                res[2] + " " + addEnglishLetter(),
////                res[3] + " " + addEnglishLetter()
//        );
//    }
//
//    private void noSuggest() {
//        final Handler mhandler = new Handler(Looper.getMainLooper());
//        // Handlerを使用してメイン(UI)スレッドに処理を依頼する
//        mhandler.post(() -> {
//            text1.setText("Result: " + res[0]);
//            text2.setText("Result: " + res[1]);
//            text3.setText("Result: " + res[2]);
//            text4.setText("Result: " + res[3]);
//        });
//    }

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
                sr.setRecognitionListener(new ImageActivity.listener());
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

    // ファイルを保存
    public void saveFile(String file, String str) {

        // try-with-resources
        try (FileOutputStream fileOutputstream = openFileOutput(file,
                Context.MODE_APPEND)) {

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
                     new InputStreamReader(fileInputStream, "UTF-8"))) {

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
            // restartListeningService();
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

            resultsString += results_array.get(0);
            saveFile(fileName, resultsString);
            // トーストを使って結果表示
            buffer = readFile(fileName);
            toast(buffer);
        }

        // サウンドレベルが変わったときに呼ばれる
        // 呼ばれる保証はない
        public void onRmsChanged(float rmsdB) {
        }
    }

}