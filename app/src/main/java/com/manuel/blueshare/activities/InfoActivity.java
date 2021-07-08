package com.manuel.blueshare.activities;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.manuel.blueshare.R;
import com.google.android.material.textview.MaterialTextView;

public class InfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        MaterialTextView materialTextView = findViewById(R.id.textView_back);
        materialTextView.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        materialTextView.setOnClickListener(v -> {
            startActivity(new Intent(InfoActivity.this, HomeActivity.class));
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}