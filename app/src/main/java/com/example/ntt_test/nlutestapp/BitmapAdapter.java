package com.example.ntt_test.nlutestapp;


import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.Context.WINDOW_SERVICE;

public class BitmapAdapter extends BaseAdapter {

    //GridView内で画像を表示するために作成したレイアウト
    private static final int RESOURCE_ID = R.layout.grid_item;
    private ArrayList<String> imageList = new ArrayList<>();
    private LayoutInflater mInflater;
    private int ScreenWidthHalf = 0;
    private Context context;


    public BitmapAdapter(Context context, List<String> objects) {
        super();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageList.addAll(objects);
        this.context = context;

        // 画面の横幅の半分を計算
        WindowManager wm = (WindowManager)
                context.getSystemService(WINDOW_SERVICE);
        if (wm != null) {
            Display disp = wm.getDefaultDisplay();
            Point size = new Point();
            disp.getSize(size);

            int screenWidth = size.x;
            ScreenWidthHalf = screenWidth / 2;
        }
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(RESOURCE_ID, null);
        }
        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView1);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Picasso.with(context)
                .load(addUrl(position))
                .resize(ScreenWidthHalf, ScreenWidthHalf)
                .into(imageView);

        return convertView;
    }

    private String addUrl(int number) {

        return String.format(Locale.US,
                imageList.get(number));
    }

}
