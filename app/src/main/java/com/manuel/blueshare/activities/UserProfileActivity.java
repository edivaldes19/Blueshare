package com.manuel.blueshare.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.manuel.blueshare.R;
import com.manuel.blueshare.adapters.MyPostsAdapter;
import com.manuel.blueshare.models.Post;
import com.manuel.blueshare.providers.AuthProvider;
import com.manuel.blueshare.providers.PostProvider;
import com.manuel.blueshare.providers.UsersProvider;
import com.manuel.blueshare.utils.ViewedMessageHelper;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {
    CoordinatorLayout coordinatorLayout;
    MaterialToolbar mToolbar;
    ShapeableImageView mImageViewCover;
    CircleImageView mCircleImageProfile;
    MaterialTextView mTextViewUsername, mTextViewEmail, mTextViewPhone, mTextViewPostNumber, mTextViewPostExist;
    MaterialButton mButtonCall;
    FloatingActionButton mFabChat;
    RecyclerView mRecyclerView;
    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;
    PostProvider mPostProvider;
    MyPostsAdapter mAdapter;
    ListenerRegistration mListener, mListenerGetUser;
    String mExtraIdUser, mExtraImagePathProfile, mExtraImagePathCover, mUsername = "", mPhone = "";

    @SuppressLint("QueryPermissionsNeeded")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        coordinatorLayout = findViewById(R.id.coordinatorUserProfile);
        mTextViewUsername = findViewById(R.id.textViewUsername);
        mTextViewEmail = findViewById(R.id.textViewEmail);
        mTextViewPhone = findViewById(R.id.textViewPhone);
        mTextViewPostNumber = findViewById(R.id.textViewPostNumber);
        mCircleImageProfile = findViewById(R.id.circleImageProfile);
        mImageViewCover = findViewById(R.id.imageViewCover);
        mButtonCall = findViewById(R.id.btnCall);
        mTextViewPostExist = findViewById(R.id.textViewPostExist);
        mRecyclerView = findViewById(R.id.recyclerViewMyPost);
        mFabChat = findViewById(R.id.fabChat);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(UserProfileActivity.this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();
        mPostProvider = new PostProvider();
        mExtraIdUser = getIntent().getStringExtra("idUser");
        if (mAuthProvider.getUid().equals(mExtraIdUser)) {
            mFabChat.setEnabled(false);
            mFabChat.setVisibility(View.GONE);
        }
        getUser();
        getPostNumber();
        checkIfExistPost();
        requestCallPermission();
        mFabChat.setOnClickListener(v -> goToChatActivity());
        mCircleImageProfile.setOnClickListener(v -> goToPhotoUser(mExtraImagePathProfile, "Perfil de ", mUsername));
        mImageViewCover.setOnClickListener(v -> goToPhotoUser(mExtraImagePathCover, "Portada de ", mUsername));
        mButtonCall.setOnClickListener(v -> call());
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy < 0) {
                    mFabChat.show();
                } else if (dy > 0) {
                    mFabChat.hide();
                }
            }
        });
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void call() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requestCallPermission()) {
                if (!TextUtils.isEmpty(mPhone)) {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + mPhone));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                } else {
                    Snackbar.make(coordinatorLayout, "Error al realizar la llamada, el teléfono está vacío", Snackbar.LENGTH_LONG).show();
                }
            } else {
                Snackbar.make(coordinatorLayout, "El permiso de teléfono está desactivado, actívelo manualmente", Snackbar.LENGTH_LONG).show();
            }
        } else {
            Snackbar.make(coordinatorLayout, "No es posible realizar llamadas porque su versión de Android es menor a 6", Snackbar.LENGTH_LONG).show();
        }
    }

    public boolean requestCallPermission() {
        int callsPermission = ActivityCompat.checkSelfPermission(UserProfileActivity.this, Manifest.permission.CALL_PHONE);
        if (callsPermission != PackageManager.PERMISSION_GRANTED) {
            int request = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, request);
            }
            return false;
        }
        return true;
    }

    private void goToPhotoUser(String path, String type, String username) {
        Intent intent = new Intent(UserProfileActivity.this, PhotoActivity.class);
        intent.putExtra("pathImageForeign", path);
        intent.putExtra("typeImageForeign", type);
        intent.putExtra("username", username);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void goToChatActivity() {
        Intent intent = new Intent(UserProfileActivity.this, ChatActivity.class);
        intent.putExtra("idUser1", mAuthProvider.getUid());
        intent.putExtra("idUser2", mExtraIdUser);
        intent.putExtra("idUser2", mExtraIdUser);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        Query query = mPostProvider.getPostByUser(mExtraIdUser);
        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post.class).build();
        mAdapter = new MyPostsAdapter(options, UserProfileActivity.this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.startListening();
        ViewedMessageHelper.updateOnline(true, UserProfileActivity.this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, UserProfileActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListener != null) {
            mListener.remove();
        }
        if (mListenerGetUser != null) {
            mListenerGetUser.remove();
        }
    }

    @SuppressLint("SetTextI18n")
    private void checkIfExistPost() {
        mListener = mPostProvider.getPostByUser(mExtraIdUser).addSnapshotListener((value, error) -> {
            if (value != null) {
                int numberPost = value.size();
                if (numberPost > 0) {
                    mTextViewPostExist.setText("Publicaciones ");
                    mTextViewPostExist.setTextColor(Color.parseColor("#1876f2"));
                } else {
                    mTextViewPostExist.setText("No hay publicaciones ");
                    mTextViewPostExist.setTextColor(Color.GRAY);
                }
            }
        });
    }

    private void getPostNumber() {
        mPostProvider.getPostByUser(mExtraIdUser).get().addOnSuccessListener(queryDocumentSnapshots -> {
            int numberPost = queryDocumentSnapshots.size();
            mTextViewPostNumber.setText(String.valueOf(numberPost));
        });
    }

    private void getUser() {
        mListenerGetUser = mUsersProvider.getUserReference(mExtraIdUser).addSnapshotListener((documentSnapshot, error) -> {
            if (documentSnapshot != null) {
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.contains("username")) {
                        String username = documentSnapshot.getString("username");
                        mTextViewUsername.setText(username);
                    }
                    if (documentSnapshot.contains("email")) {
                        String email = documentSnapshot.getString("email");
                        mTextViewEmail.setText(email);
                    }
                    if (documentSnapshot.contains("phone")) {
                        String phone = documentSnapshot.getString("phone");
                        mTextViewPhone.setText(phone);
                        mPhone = phone;
                    }
                    if (documentSnapshot.contains("image_profile")) {
                        String imageProfile = documentSnapshot.getString("image_profile");
                        if (!TextUtils.isEmpty(imageProfile)) {
                            mExtraImagePathProfile = imageProfile;
                            Picasso.get().load(imageProfile).into(mCircleImageProfile);
                        }
                    }
                    if (documentSnapshot.contains("image_cover")) {
                        String imageCover = documentSnapshot.getString("image_cover");
                        if (!TextUtils.isEmpty(imageCover)) {
                            mExtraImagePathCover = imageCover;
                            Picasso.get().load(imageCover).into(mImageViewCover);
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }
}