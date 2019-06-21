package com.example.ntt_test.nlutestapp;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class ImageActivity extends AppCompatActivity {

    // Replace the subscriptionKey string value with your valid subscription key.
    static String subscriptionKey = "678a4900e849413380f04c00066662a5";

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
    private TextView wordtext;
    private int count = 0;
    private ArrayList<String> list = new ArrayList<>();
    private static final int REQUEST_CODE = 1000;
    private static Toast t;
    private SpeechRecognizer sr;
    private String input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        mGridView = (GridView) findViewById(R.id.gridView);
        nextbtn = (Button) findViewById(R.id.nextbtn);
        wordtext = (TextView) findViewById(R.id.wordtext);

        Intent intent = getIntent();

        list.addAll(intent.getStringArrayListExtra("listword"));
//        search = intent.getStringExtra("word");
        // 画像検索開始
        imageSearch();
        // 同時に認識開始
        startListening();

        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 画像検索開始
                imageSearch();
            }
        });

        // 自動遷移
        for (int i = 0;i < list.size();i++){
            delayExecutio(i);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void imageSearch() {
        if (list.size() > count) {
            ImageSearchTask test = new ImageSearchTask(new WebSearchTaskCallBack() {
                @Override
                public void onWebSearchCompleted(String result) {
                }

                @Override
                public void onImageSearchCompleted(ArrayList<String> result) {
                    wordtext.setText(list.get(count));
                    for (int i = 0; i < result.size(); i++) {
                        System.out.println(result.get(i));
                        BitmapAdapter adapter = new BitmapAdapter(
                                getApplicationContext(),
                                result
                        );
                        mGridView.setAdapter(adapter);
                    }
                    count++;
                    if(count == list.size()){
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 10000 );
                    }
                }
            });
            test.execute(list.get(count));
        }
    }

    private void delayExecutio(int times) {
        // 自動遷移
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                imageSearch();
                startListening();
            }
        }, 10000 * (times + 1));
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
                // 検索
                toast("認識した文字："+input);
            }
        }
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
//            text1.setText(resultsString);
//            webSearch(resultsString);
        }

        // サウンドレベルが変わったときに呼ばれる
        // 呼ばれる保証はない
        public void onRmsChanged(float rmsdB) {
        }
    }

}