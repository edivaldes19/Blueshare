package com.manuel.blueshare.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.manuel.blueshare.R;
import com.manuel.blueshare.adapters.PostsAdapter;
import com.manuel.blueshare.models.Post;
import com.manuel.blueshare.providers.AuthProvider;
import com.manuel.blueshare.providers.PostProvider;
import com.manuel.blueshare.utils.ViewedMessageHelper;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.Query;

import java.util.Objects;

public class CategoriesActivity extends AppCompatActivity {
    MaterialToolbar mToolbar;
    MaterialTextView mTextViewNumberFilter;
    AuthProvider mAuthProvider;
    RecyclerView mRecyclerView;
    PostProvider mPostProvider;
    PostsAdapter mPostsAdapter;
    String mExtraCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        mExtraCategory = getIntent().getStringExtra("category");
        mRecyclerView = findViewById(R.id.recyclerViewFilter);
        mTextViewNumberFilter = findViewById(R.id.textViewNumberFilter);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(mExtraCategory);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(CategoriesActivity.this, 2));
        mAuthProvider = new AuthProvider();
        mPostProvider = new PostProvider();
    }

    @Override
    public void onStart() {
        super.onStart();
        Query query = mPostProvider.getPostByCategoryAndTimestamp(mExtraCategory);
        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post.class).build();
        mPostsAdapter = new PostsAdapter(options, CategoriesActivity.this, mTextViewNumberFilter);
        mRecyclerView.setAdapter(mPostsAdapter);
        mPostsAdapter.startListening();
        ViewedMessageHelper.updateOnline(true, CategoriesActivity.this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mPostsAdapter.stopListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, CategoriesActivity.this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }
}