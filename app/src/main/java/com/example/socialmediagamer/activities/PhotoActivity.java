package com.example.socialmediagamer.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.socialmediagamer.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.ortiz.touchview.TouchImageView;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class PhotoActivity extends AppCompatActivity {
    MaterialToolbar mToolbar;
    TouchImageView touchImageView;
    String mExtraMyImagePath, mExtraMyImageType, mExtraForeignImagePath, mExtraForeignImageType, mExtraUsername, mExtraUrlImagePost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_photo);
        mExtraMyImagePath = getIntent().getStringExtra("pathImage");
        mExtraMyImageType = getIntent().getStringExtra("pathType");
        mExtraForeignImagePath = getIntent().getStringExtra("pathImageForeign");
        mExtraForeignImageType = getIntent().getStringExtra("pathTypeForeign");
        mExtraUsername = getIntent().getStringExtra("username");
        mExtraUrlImagePost = getIntent().getStringExtra("urlImagePost");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String title = preferences.getString("postTitle", "Imagen");
        touchImageView = findViewById(R.id.imageViewMyPhoto);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (mExtraUsername != null && !mExtraUsername.isEmpty()) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(mExtraForeignImageType + mExtraUsername);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            Picasso.get().load(mExtraForeignImagePath).into(touchImageView);
        } else {
            Objects.requireNonNull(getSupportActionBar()).setTitle(mExtraMyImageType);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            Picasso.get().load(mExtraMyImagePath).into(touchImageView);
        }
        if (mExtraUrlImagePost != null && !mExtraUrlImagePost.isEmpty() && title != null && !title.isEmpty()) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            Picasso.get().load(mExtraUrlImagePost).into(touchImageView);
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