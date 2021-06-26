package com.example.socialmediagamer.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediagamer.R;
import com.example.socialmediagamer.activities.PostDetailActivity;
import com.example.socialmediagamer.models.Like;
import com.example.socialmediagamer.models.Post;
import com.example.socialmediagamer.providers.AuthProvider;
import com.example.socialmediagamer.providers.LikesProvider;
import com.example.socialmediagamer.providers.UsersProvider;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.Objects;

public class PostsAdapter extends FirestoreRecyclerAdapter<Post, PostsAdapter.ViewHolder> {
    Context context;
    UsersProvider mUsersProvider;
    LikesProvider mLikesProvider;
    AuthProvider mAuthProvider;
    MaterialTextView mTextViewNumberFilter;
    ListenerRegistration mListener;

    public PostsAdapter(FirestoreRecyclerOptions<Post> options, Context context) {
        super(options);
        this.context = context;
        mUsersProvider = new UsersProvider();
        mLikesProvider = new LikesProvider();
        mAuthProvider = new AuthProvider();
    }

    public PostsAdapter(FirestoreRecyclerOptions<Post> options, Context context, MaterialTextView materialTextView) {
        super(options);
        this.context = context;
        mUsersProvider = new UsersProvider();
        mLikesProvider = new LikesProvider();
        mAuthProvider = new AuthProvider();
        mTextViewNumberFilter = materialTextView;
    }

    @Override
    protected void onBindViewHolder(@NonNull final PostsAdapter.ViewHolder holder, int position, @NonNull final Post post) {
        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        final String postId = document.getId();
        if (mTextViewNumberFilter != null) {
            int numberFilter = getSnapshots().size();
            mTextViewNumberFilter.setText(String.valueOf(numberFilter));
        }
        holder.textViewTitle.setText(post.getTitle());
        holder.textViewDescription.setText(post.getDescription());
        if (post.getImage1() != null) {
            if (!post.getImage1().isEmpty()) {
                Picasso.with(context).load(post.getImage1()).into(holder.imageViewPost);
            }
        }
        holder.viewHolder.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("id", postId);
            context.startActivity(intent);
        });
        holder.imageViewLike.setOnClickListener(v -> {
            Like like = new Like();
            like.setIdUser(mAuthProvider.getUid());
            like.setIdPost(postId);
            like.setTimestamp(new Date().getTime());
            meGusta(like, holder);
        });
        getUserInfo(post.getIdUser(), holder);
        getNumberLikesByPost(postId, holder);
        checkIfExistLike(postId, mAuthProvider.getUid(), holder);
    }

    @SuppressLint("SetTextI18n")
    private void getNumberLikesByPost(String idPost, final ViewHolder holder) {
        mListener = mLikesProvider.getLikesByPost(idPost).addSnapshotListener((value, error) -> {
            if (value != null) {
                int numberLikes = Objects.requireNonNull(value).size();
                holder.textViewContadorLikes.setText(numberLikes + " Me gusta");
            }
        });
    }

    private void meGusta(final Like like, final ViewHolder holder) {
        mLikesProvider.getLikeByPostAndUser(like.getIdPost(), mAuthProvider.getUid()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            int numberDocuments = queryDocumentSnapshots.size();
            if (numberDocuments > 0) {
                String idLike = queryDocumentSnapshots.getDocuments().get(0).getId();
                holder.imageViewLike.setImageResource(R.drawable.ic_like_off);
                mLikesProvider.delete(idLike);
            } else {
                holder.imageViewLike.setImageResource(R.drawable.ic_like_on);
                mLikesProvider.create(like);
            }
        });
    }

    private void checkIfExistLike(String idPost, String idUser, final ViewHolder holder) {
        mLikesProvider.getLikeByPostAndUser(idPost, idUser).get().addOnSuccessListener(queryDocumentSnapshots -> {
            int numberDocuments = queryDocumentSnapshots.size();
            if (numberDocuments > 0) {
                holder.imageViewLike.setImageResource(R.drawable.ic_like_on);
            } else {
                holder.imageViewLike.setImageResource(R.drawable.ic_like_off);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void getUserInfo(String idUser, final ViewHolder holder) {
        mUsersProvider.getUser(idUser).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                if (documentSnapshot.contains("username")) {
                    String username = documentSnapshot.getString("username");
                    holder.textViewUsername.setText(username);
                }
            }
        });
    }

    public ListenerRegistration getListener() {
        return mListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_post, parent, false);
        return new ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView textViewTitle, textViewUsername, textViewDescription, textViewContadorLikes;
        ShapeableImageView imageViewPost, imageViewLike;
        View viewHolder;

        public ViewHolder(View view) {
            super(view);
            textViewTitle = view.findViewById(R.id.textViewTitlePostCard);
            textViewUsername = view.findViewById(R.id.textViewUsernamePostCard);
            textViewDescription = view.findViewById(R.id.textViewDescriptionPostCard);
            textViewContadorLikes = view.findViewById(R.id.textViewContadorLikes);
            imageViewPost = view.findViewById(R.id.imageViewPostCard);
            imageViewLike = view.findViewById(R.id.imageViewLike);
            viewHolder = view;
        }
    }
}