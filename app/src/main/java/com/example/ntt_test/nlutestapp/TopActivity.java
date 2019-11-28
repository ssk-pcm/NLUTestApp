package com.example.ntt_test.nlutestapp;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TopActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private Boolean sug = false;
    private static Toast t;
    private String timeLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top);

        timeLog = getNowDate();
        String csvFileName = timeLog + ".csv";

        Button buttonStart = findViewById(R.id.startbtn);
        Switch sugSwich = findViewById(R.id.suggestswch);
        sugSwich.setOnCheckedChangeListener(this);

        //データ保存用ディレクトリを作成
        String path = Environment.getExternalStorageDirectory().getPath() + "/NLUTestApp/";
        File root = new File(path);
        if (!root.exists()) {
            root.mkdir();
        }

        saveCsvFile(csvFileName);

        buttonStart.setOnClickListener(v -> {
            // 実験を開始
            Intent intent = new Intent(TopActivity.this, MainActivity.class);
            intent.putExtra("isSuggest",sug);
            intent.putExtra("timeLog",timeLog);
            intent.putExtra("csvFileName",csvFileName);
            startActivity(intent);
        });

        Button debugbtn = findViewById(R.id.debugbtn);
        TextView suggestText = findViewById(R.id.suggesttext);
        final AutosuggestTask suggest = new AutosuggestTask(this);

        debugbtn.setOnClickListener(v -> {
//            debugSuggest(suggest,suggestText);
            debugGoogleSearch(suggestText);
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        //do stuff when Switch is ON
        //do stuff when Switch if OFF
        sug = b;
    }

    private void saveCsvFile(String csvFileName) {
        // 現在ストレージが書き込みできるかチェック
        if (isExternalStorageWritable()) {
            String filePath = Environment.getExternalStorageDirectory().getPath()
                    + "/NLUTestApp/" + csvFileName;
            //Log.d("debug", filePath);

            File file = new File(filePath);
            boolean mmkdir = file.getParentFile().mkdir();
            FileOutputStream fileOutputStream;
            try {
                fileOutputStream = new FileOutputStream(file, true);
                OutputStreamWriter outputStreamWriter
                        = new OutputStreamWriter(fileOutputStream, "Shift_JIS");
                BufferedWriter bw = new BufferedWriter(outputStreamWriter);
                //csvに書き込む
                bw.write("Suggest_word"+","+"TimeStamp"+"\n");
                bw.flush();
                bw.close();
                toast("csvファイルを作成しました");
//                outputStreamWriter.close();
//                fileOutputStream.close();
            } catch (Exception e) {
                toast("保存に失敗しました");
                e.printStackTrace();
            }
        }
    }

    private void debugSuggest(AutosuggestTask suggest,TextView suggestText){
        suggest.setOnAutosuggestCallBack(new AutosuggestTask.AutosuggestCallBackTask(){
            @Override
            public void AutosuggestCallBack(String[] result) {
                super.AutosuggestCallBack(result);
                suggestText.setText("");
                for (String s : result) {
                    suggestText.setText("set: " + s + "\n");
                }
            }
        });
        suggest.execute(
                "コーヒー さ",
                "水　お",
                "お寿司　い",
                "猫　か"
        );
    }

    private void debugGoogleSearch(TextView suggestText){
        GoogleSearchTask gt = new GoogleSearchTask(new SearchTaskCallBack() {
            @Override
            public void onWebSearchCompleted(String result) {
            }

            @Override
            public void onImageSearchCompleted(ArrayList<String> result) {
                StringBuilder aa = new StringBuilder();
                for(int i = 0;i < result.size();i++){
                   aa.append(result.get(i)).append("\n");
                }
                suggestText.setText(aa.toString());
            }
        }, this);
        gt.execute("猫");
    }

    // Checks if external storage is available for read and write
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private static String getNowDate() {
        final DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.JAPANESE);
        final Date date = new Date(System.currentTimeMillis());
        return df.format(date);
    }

    //トーストが連続して表示されるのを防ぐ
    public void toast(String message) {
        if (t != null) {
            t.cancel();
        }
        t = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        t.show();
    }

    //メディアスキャンのコールバックリスナー
    MediaScannerConnection.OnScanCompletedListener mScanCompletedListener = (path, uri) -> {
        Log.d("MediaScannerConnection", "Scanned " + path + ":");
        Log.d("MediaScannerConnection", "-> uri=" + uri);
    };
}
