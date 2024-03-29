package com.manuel.blueshare.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.manuel.blueshare.R;
import com.manuel.blueshare.adapters.CommentAdapter;
import com.manuel.blueshare.adapters.SliderAdapter;
import com.manuel.blueshare.models.Comment;
import com.manuel.blueshare.models.FCMBody;
import com.manuel.blueshare.models.FCMResponse;
import com.manuel.blueshare.models.SliderItem;
import com.manuel.blueshare.providers.AuthProvider;
import com.manuel.blueshare.providers.CommentsProvider;
import com.manuel.blueshare.providers.LikesProvider;
import com.manuel.blueshare.providers.NotificationProvider;
import com.manuel.blueshare.providers.PostProvider;
import com.manuel.blueshare.providers.TokenProvider;
import com.manuel.blueshare.providers.UsersProvider;
import com.manuel.blueshare.utils.RelativeTime;
import com.manuel.blueshare.utils.ViewedMessageHelper;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostDetailActivity extends AppCompatActivity {
    MaterialToolbar mToolbar;
    MaterialCardView mCardViewShowProfile;
    MaterialTextView mTextViewTitle, mTextViewDescription, mTextViewUsername, mTextViewCategory, mTextViewRelativeTime, mTextViewLikes;
    FloatingActionButton mFabComment;
    ShapeableImageView mImageViewCategory;
    CircleImageView mCircleImageViewProfile;
    SliderView mSliderView;
    SliderAdapter mSliderAdapter;
    RecyclerView mRecyclerView;
    CommentAdapter mAdapter;
    List<SliderItem> mSliderItems = new ArrayList<>();
    PostProvider mPostProvider;
    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;
    CommentsProvider mCommentsProvider;
    LikesProvider mLikeProvider;
    NotificationProvider mNotificationProvider;
    TokenProvider mTokenProvider;
    ListenerRegistration mListener;
    String mExtraPostId, mIdUser = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        mSliderView = findViewById(R.id.imageSlider);
        mTextViewTitle = findViewById(R.id.textViewTitle);
        mTextViewDescription = findViewById(R.id.textViewDescription);
        mTextViewUsername = findViewById(R.id.textViewUsername);
        mTextViewCategory = findViewById(R.id.textViewCategory);
        mImageViewCategory = findViewById(R.id.imageViewCategory);
        mTextViewRelativeTime = findViewById(R.id.textViewRelativeTime);
        mTextViewLikes = findViewById(R.id.textViewLikes);
        mCircleImageViewProfile = findViewById(R.id.circleImageProfile);
        mRecyclerView = findViewById(R.id.recyclerViewComments);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(PostDetailActivity.this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mCardViewShowProfile = findViewById(R.id.cardViewAuthor);
        mFabComment = findViewById(R.id.fabComment);
        mPostProvider = new PostProvider();
        mUsersProvider = new UsersProvider();
        mCommentsProvider = new CommentsProvider();
        mAuthProvider = new AuthProvider();
        mLikeProvider = new LikesProvider();
        mNotificationProvider = new NotificationProvider();
        mTokenProvider = new TokenProvider();
        mExtraPostId = getIntent().getStringExtra("id");
        getPost();
        getNumberLikes();
        mFabComment.setOnClickListener(v -> showDialogComment());
        mCardViewShowProfile.setOnClickListener(v -> goToShowProfile());
    }

    @SuppressLint("SetTextI18n")
    private void getNumberLikes() {
        mListener = mLikeProvider.getLikesByPost(mExtraPostId).addSnapshotListener((value, error) -> {
            if (value != null) {
                int numberLikes = value.size();
                if (numberLikes == 0) {
                    mTextViewLikes.setText("Sin me gusta");
                } else {
                    mTextViewLikes.setText(numberLikes + " Me gusta");
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Query query = mCommentsProvider.getCommentsByPost(mExtraPostId);
        FirestoreRecyclerOptions<Comment> options = new FirestoreRecyclerOptions.Builder<Comment>().setQuery(query, Comment.class).build();
        mAdapter = new CommentAdapter(options, PostDetailActivity.this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.startListening();
        ViewedMessageHelper.updateOnline(true, PostDetailActivity.this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, PostDetailActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListener != null) {
            mListener.remove();
        }
    }

    private void showDialogComment() {
        AlertDialog.Builder alert = new AlertDialog.Builder(PostDetailActivity.this);
        alert.setTitle("Nuevo comentario");
        alert.setIcon(R.drawable.ic_insert_comment);
        alert.setCancelable(false);
        TextInputEditText textInputEditText = new TextInputEditText(PostDetailActivity.this);
        textInputEditText.setHint("Tu comentario...");
        textInputEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50)});
        textInputEditText.setMaxLines(3);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(50, 0, 50, 50);
        textInputEditText.setLayoutParams(params);
        RelativeLayout container = new RelativeLayout(PostDetailActivity.this);
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        container.setLayoutParams(relativeParams);
        container.addView(textInputEditText);
        alert.setView(container);
        alert.setPositiveButton("Comentar", (dialog, which) -> {
            String value = Objects.requireNonNull(textInputEditText.getText()).toString().trim();
            if (!TextUtils.isEmpty(value)) {
                createComment(value);
            } else {
                Toast.makeText(this, "No es posible comentar textos vacíos", Toast.LENGTH_LONG).show();
            }
        });
        alert.setNegativeButton("Cancelar", null);
        alert.show();
    }

    private void createComment(final String value) {
        Comment comment = new Comment();
        comment.setComment(value);
        comment.setIdPost(mExtraPostId);
        comment.setIdUser(mAuthProvider.getUid());
        comment.setTimestamp(new Date().getTime());
        mCommentsProvider.create(comment).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sendNotification(value);
                Toast.makeText(PostDetailActivity.this, "Comentario creado exitosamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(PostDetailActivity.this, "Error al crear el comentario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotification(final String comment) {
        if (mIdUser == null) {
            return;
        }
        mTokenProvider.getToken(mIdUser).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                if (documentSnapshot.contains("token")) {
                    String token = documentSnapshot.getString("token");
                    Map<String, String> data = new HashMap<>();
                    data.put("title", "Nuevo comentario");
                    data.put("body", comment);
                    FCMBody body = new FCMBody(token, "high", "4500s", data);
                    mNotificationProvider.sendNotification(body).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body() != null) {
                                if (response.body().getSuccess() == 1) {
                                    //Notificación enviada exitosamente
                                } else {
                                    Toast.makeText(PostDetailActivity.this, "Error al enviar la notificación", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(PostDetailActivity.this, "Error al enviar la notificación", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                        }
                    });
                }
            } else {
                Toast.makeText(PostDetailActivity.this, "No existe el token de notificaciones", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToShowProfile() {
        if (!mIdUser.isEmpty()) {
            Intent intent = new Intent(PostDetailActivity.this, UserProfileActivity.class);
            intent.putExtra("idUser", mIdUser);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Cargando ID de usuario...", Toast.LENGTH_SHORT).show();
        }
    }

    private void instancesSlider() {
        mSliderAdapter = new SliderAdapter(PostDetailActivity.this, mSliderItems);
        mSliderView.setSliderAdapter(mSliderAdapter);
        mSliderView.setIndicatorAnimation(IndicatorAnimationType.THIN_WORM);
        mSliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        mSliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT);
        mSliderView.setIndicatorSelectedColor(Color.BLUE);
        mSliderView.setIndicatorUnselectedColor(Color.WHITE);
        mSliderView.setScrollTimeInSec(5);
        mSliderView.setAutoCycle(true);
        mSliderView.startAutoCycle();
    }

    @SuppressLint("SetTextI18n")
    private void getPost() {
        mPostProvider.getPostById(mExtraPostId).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                if (documentSnapshot.contains("image1")) {
                    String image1 = documentSnapshot.getString("image1");
                    SliderItem item = new SliderItem();
                    item.setImageUrl(image1);
                    mSliderItems.add(item);
                }
                if (documentSnapshot.contains("image2")) {
                    String image2 = documentSnapshot.getString("image2");
                    SliderItem item = new SliderItem();
                    item.setImageUrl(image2);
                    mSliderItems.add(item);
                }
                if (documentSnapshot.contains("title")) {
                    String title = documentSnapshot.getString("title");
                    mTextViewTitle.setText(title);
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("postTitle", title);
                    editor.apply();
                }
                if (documentSnapshot.contains("description")) {
                    String description = documentSnapshot.getString("description");
                    mTextViewDescription.setText(description);
                }
                if (documentSnapshot.contains("category")) {
                    String category = documentSnapshot.getString("category");
                    if (!TextUtils.isEmpty(category)) {
                        switch (category) {
                            case "Cultura":
                                mTextViewCategory.setTextColor(Color.parseColor("#A25918"));
                                mImageViewCategory.setImageResource(R.drawable.ic_category_culture);
                                break;
                            case "Deporte":
                                mTextViewCategory.setTextColor(Color.BLACK);
                                mImageViewCategory.setImageResource(R.drawable.ic_category_sports);
                                break;
                            case "Estilo de vida":
                                mTextViewCategory.setTextColor(Color.parseColor("#A901DB"));
                                mImageViewCategory.setImageResource(R.drawable.ic_category_lifestyle);
                                break;
                            case "Música":
                                mTextViewCategory.setTextColor(Color.RED);
                                mImageViewCategory.setImageResource(R.drawable.ic_category_music);
                                break;
                            case "Programación":
                                mTextViewCategory.setTextColor(Color.BLUE);
                                mImageViewCategory.setImageResource(R.drawable.ic_category_programation);
                                break;
                            case "Videojuegos":
                                mTextViewCategory.setTextColor(Color.parseColor("#008000"));
                                mImageViewCategory.setImageResource(R.drawable.ic_category_videogames);
                                break;
                        }
                    }
                    mTextViewCategory.setText(category);
                }
                if (documentSnapshot.contains("idUser")) {
                    mIdUser = documentSnapshot.getString("idUser");
                    getUserInfo(mIdUser);
                }
                if (documentSnapshot.contains("timestamp")) {
                    long timestamp = documentSnapshot.getLong("timestamp");
                    String relativeTime = RelativeTime.getTimeAgo(timestamp);
                    mTextViewRelativeTime.setText("Publicado " + relativeTime.toLowerCase());
                }
                instancesSlider();
            }
        });
    }

    private void getUserInfo(String idUser) {
        mUsersProvider.getUser(idUser).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                if (documentSnapshot.contains("username")) {
                    String username = documentSnapshot.getString("username");
                    mTextViewUsername.setText(username);
                }
                if (documentSnapshot.contains("image_profile")) {
                    String imageProfile = documentSnapshot.getString("image_profile");
                    Picasso.get().load(imageProfile).into(mCircleImageViewProfile);
                }
            }
        });
    }
}