package com.manuel.blueshare.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.manuel.blueshare.R;
import com.ortiz.touchview.TouchImageView;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class PhotoActivity extends AppCompatActivity {
    MaterialToolbar mToolbar;
    TouchImageView mTouchImageView;
    String mExtraMyImagePath, mExtraMyImageType, mExtraForeignImagePath, mExtraForeignImageType, mExtraUsername, mExtraUrlImagePost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        mExtraMyImagePath = getIntent().getStringExtra("pathImage");
        mExtraMyImageType = getIntent().getStringExtra("typeImage");
        mExtraForeignImagePath = getIntent().getStringExtra("pathImageForeign");
        mExtraForeignImageType = getIntent().getStringExtra("typeImageForeign");
        mExtraUsername = getIntent().getStringExtra("username");
        mExtraUrlImagePost = getIntent().getStringExtra("urlImagePost");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String title = preferences.getString("postTitle", "Imagen");
        mTouchImageView = findViewById(R.id.imageViewMyPhoto);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (!TextUtils.isEmpty(mExtraUsername)) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(mExtraForeignImageType + mExtraUsername);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            Picasso.get().load(mExtraForeignImagePath).into(mTouchImageView);
        } else {
            Objects.requireNonNull(getSupportActionBar()).setTitle(mExtraMyImageType);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            Picasso.get().load(mExtraMyImagePath).into(mTouchImageView);
        }
        if (!TextUtils.isEmpty(mExtraUrlImagePost) && !TextUtils.isEmpty(title)) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            Picasso.get().load(mExtraUrlImagePost).into(mTouchImageView);
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