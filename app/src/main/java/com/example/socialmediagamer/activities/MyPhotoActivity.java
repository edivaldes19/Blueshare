package com.example.socialmediagamer.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.socialmediagamer.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.ortiz.touchview.TouchImageView;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class MyPhotoActivity extends AppCompatActivity {
    MaterialToolbar mToolbar;
    TouchImageView imageView;
    String mExtraMyImagePath, mExtraMyImageType, mExtraForeignImagePath, mExtraForeignImageType, mExtraUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_photo);
        mExtraMyImagePath = getIntent().getStringExtra("pathImage");
        mExtraMyImageType = getIntent().getStringExtra("pathType");
        mExtraForeignImagePath = getIntent().getStringExtra("pathImageForeign");
        mExtraForeignImageType = getIntent().getStringExtra("pathTypeForeign");
        mExtraUsername = getIntent().getStringExtra("username");
        imageView = findViewById(R.id.imageViewMyPhoto);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (mExtraUsername != null && !mExtraUsername.isEmpty()) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(mExtraForeignImageType + mExtraUsername);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            Picasso.get().load(mExtraForeignImagePath).into(imageView);
        } else {
            Objects.requireNonNull(getSupportActionBar()).setTitle(mExtraMyImageType);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            Picasso.get().load(mExtraMyImagePath).into(imageView);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
        return true;
    }
}