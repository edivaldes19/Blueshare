package com.example.socialmediagamer.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediagamer.R;
import com.example.socialmediagamer.activities.EditProfileActivity;
import com.example.socialmediagamer.activities.PhotoActivity;
import com.example.socialmediagamer.adapters.MyPostsAdapter;
import com.example.socialmediagamer.models.Post;
import com.example.socialmediagamer.providers.AuthProvider;
import com.example.socialmediagamer.providers.ImageProvider;
import com.example.socialmediagamer.providers.PostProvider;
import com.example.socialmediagamer.providers.UsersProvider;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    View mView;
    LinearLayout mLinearLayoutEditProfile;
    ShapeableImageView mImageViewCover;
    CircleImageView mCircleImageProfile;
    MaterialTextView mTextViewUsername, mTextViewEmail, mTextViewPhone, mTextViewPostNumber, mTextViewPostExist;
    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;
    PostProvider mPostProvider;
    ImageProvider mImageProvider;
    RecyclerView mRecyclerView;
    MyPostsAdapter mAdapter;
    ListenerRegistration mListener, mListenerGetUser;
    String mExtraImagePathProfile, mExtraImagePathCover;

    public ProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_profile, container, false);
        mLinearLayoutEditProfile = mView.findViewById(R.id.linearLayoutEditProfile);
        mTextViewUsername = mView.findViewById(R.id.textViewUsername);
        mTextViewEmail = mView.findViewById(R.id.textViewEmail);
        mTextViewPhone = mView.findViewById(R.id.textViewPhone);
        mTextViewPostExist = mView.findViewById(R.id.textViewPostExist);
        mTextViewPostNumber = mView.findViewById(R.id.textViewPostNumber);
        mCircleImageProfile = mView.findViewById(R.id.circleImageProfile);
        mImageViewCover = mView.findViewById(R.id.imageViewCover);
        mRecyclerView = mView.findViewById(R.id.recyclerViewMyPost);
        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();
        mPostProvider = new PostProvider();
        mImageProvider = new ImageProvider();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mLinearLayoutEditProfile.setOnClickListener(v -> goToEditProfile());
        mCircleImageProfile.setOnClickListener(v -> goToMyPhoto(mExtraImagePathProfile, "Mi foto de perfil"));
        mImageViewCover.setOnClickListener(v -> goToMyPhoto(mExtraImagePathCover, "Mi foto de portada"));
        getUser();
        getPostNumber();
        checkIfExistPost();
        return mView;
    }

    private void goToMyPhoto(String path, String type) {
        Intent intent = new Intent(getContext(), PhotoActivity.class);
        intent.putExtra("pathImage", path);
        intent.putExtra("pathType", type);
        startActivity(intent);
    }

    @SuppressLint("SetTextI18n")
    private void checkIfExistPost() {
        mListener = mPostProvider.getPostByUser(mAuthProvider.getUid()).addSnapshotListener((value, error) -> {
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

    @Override
    public void onStart() {
        super.onStart();
        Query query = mPostProvider.getPostByUser(mAuthProvider.getUid());
        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post.class).build();
        mAdapter = new MyPostsAdapter(options, getContext());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mListener != null) {
            mListener.remove();
        }
        if (mListenerGetUser != null) {
            mListenerGetUser.remove();
        }
    }

    private void goToEditProfile() {
        Intent intent = new Intent(getContext(), EditProfileActivity.class);
        startActivity(intent);
    }

    private void getPostNumber() {
        mPostProvider.getPostByUser(mAuthProvider.getUid()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            int numberPost = queryDocumentSnapshots.size();
            mTextViewPostNumber.setText(String.valueOf(numberPost));
        });
    }

    private void getUser() {
        mListenerGetUser = mUsersProvider.getUserReference(mAuthProvider.getUid()).addSnapshotListener((documentSnapshot, error) -> {
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
                    }
                    if (documentSnapshot.contains("image_cover")) {
                        String imageCover = documentSnapshot.getString("image_cover");
                        if (imageCover != null) {
                            if (!imageCover.isEmpty()) {
                                mExtraImagePathCover = imageCover;
                                Picasso.get().load(imageCover).into(mImageViewCover);
                            }
                        }
                    }
                    if (documentSnapshot.contains("image_profile")) {
                        String imageProfile = documentSnapshot.getString("image_profile");
                        if (imageProfile != null) {
                            if (!imageProfile.isEmpty()) {
                                mExtraImagePathProfile = imageProfile;
                                Picasso.get().load(imageProfile).into(mCircleImageProfile);
                            }
                        }
                    }
                }
            }
        });
    }
}