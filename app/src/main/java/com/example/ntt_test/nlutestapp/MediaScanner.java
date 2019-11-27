package com.example.ntt_test.nlutestapp;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;

class MediaScanner {
    private Context mContext;
    private String filename;

    MediaScanner(String fileName, Context context){
        mContext = context;
        this.filename = fileName;
    }

    void mediaScan(){
        //保存したファイルをメディアとして認識させPCからの閲覧を可能にさせる
        String[] paths = {Environment.getExternalStorageDirectory().getPath()
                + "/NLUTestApp/" + filename};
//        String[] mimeTypes = {"text/csv"};
        MediaScannerConnection.scanFile(
                mContext,
                paths,
                null, // mimeTypes
                mScanCompletedListener);
    }

    //メディアスキャンのコールバックリスナー
    private MediaScannerConnection.OnScanCompletedListener mScanCompletedListener = (path, uri) -> {
        Log.d("MediaScannerConnection", "Scanned " + path + ":");
        Log.d("MediaScannerConnection", "-> uri=" + uri);
    };
}
