package com.example.ntt_test.nlutestapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.util.ArrayList;

public class TopActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private Boolean sug = false;
    private Switch sugSwich;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top);
        Button buttonStart = findViewById(R.id.startbtn);
        sugSwich = findViewById(R.id.suggestswch);
        sugSwich.setOnCheckedChangeListener(this);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 実験を開始
                Intent intent = new Intent(TopActivity.this, MainActivity.class);
                intent.putExtra("isSuggest",sug);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            //do stuff when Switch is ON
            sug = true;
        } else {
            //do stuff when Switch if OFF
            sug = false;
        }
    }
}
