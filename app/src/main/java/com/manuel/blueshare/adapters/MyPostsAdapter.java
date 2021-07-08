package com.manuel.blueshare.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.manuel.blueshare.R;
import com.manuel.blueshare.activities.PostActivity;
import com.manuel.blueshare.activities.PostDetailActivity;
import com.manuel.blueshare.models.Post;
import com.manuel.blueshare.providers.AuthProvider;
import com.manuel.blueshare.providers.ImageProvider;
import com.manuel.blueshare.providers.LikesProvider;
import com.manuel.blueshare.providers.PostProvider;
import com.manuel.blueshare.providers.UsersProvider;
import com.manuel.blueshare.utils.RelativeTime;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsAdapter extends FirestoreRecyclerAdapter<Post, MyPostsAdapter.ViewHolder> {
    Context context;
    UsersProvider mUsersProvider;
    LikesProvider mLikesProvider;
    AuthProvider mAuthProvider;
    PostProvider mPostProvider;
    ImageProvider mImageProvider;

    public MyPostsAdapter(FirestoreRecyclerOptions<Post> options, Context context) {
        super(options);
        this.context = context;
        mUsersProvider = new UsersProvider();
        mLikesProvider = new LikesProvider();
        mAuthProvider = new AuthProvider();
        mPostProvider = new PostProvider();
        mImageProvider = new ImageProvider();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull final MyPostsAdapter.ViewHolder holder, int position, @NonNull final Post post) {
        holder.frameLayoutMyPost.setAnimation(AnimationUtils.loadAnimation(context, R.anim.slide));
        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        final String postId = document.getId();
        String relativeTime = RelativeTime.getTimeAgo(post.getTimestamp());
        holder.textViewRelativeTime.setText("Publicado " + relativeTime.toLowerCase());
        holder.textViewTitle.setText(post.getTitle());
        if (post.getIdUser().equals(mAuthProvider.getUid())) {
            holder.imageViewEdit.setVisibility(View.VISIBLE);
            holder.imageViewDelete.setVisibility(View.VISIBLE);
        } else {
            holder.imageViewEdit.setVisibility(View.GONE);
            holder.imageViewDelete.setVisibility(View.GONE);
        }
        if (post.getImage1() != null) {
            if (!post.getImage1().isEmpty()) {
                Picasso.get().load(post.getImage1()).into(holder.circleImagePost);
            } else if (post.getImage2() != null) {
                Picasso.get().load(post.getImage2()).into(holder.circleImagePost);
            }
        } else if (post.getImage2() != null) {
            Picasso.get().load(post.getImage2()).into(holder.circleImagePost);
        }
        holder.viewHolder.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("id", postId);
            context.startActivity(intent);
        });
        holder.imageViewEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostActivity.class);
            intent.putExtra("idPostUpdate", postId);
            intent.putExtra("postSelect", true);
            context.startActivity(intent);
        });
        holder.imageViewDelete.setOnClickListener(v -> showConfirmDelete(postId, post));
    }

    private void showConfirmDelete(String postId, Post post) {
        new AlertDialog.Builder(context).setIcon(R.drawable.ic_delete).setTitle("Eliminar publicación").setMessage("¿Está seguro de realizar esta acción?").setCancelable(false).setPositiveButton("Eliminar", (dialog, which) -> deletePost(postId, post)).setNegativeButton("Cancelar", null).show();
    }

    private void deletePost(String postId, Post post) {
        mPostProvider.delete(postId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (post.getImage1() != null) {
                    if (!post.getImage1().isEmpty()) {
                        mImageProvider.deleteFromPath(post.getImage1());
                    }
                }
                if (post.getImage2() != null) {
                    if (!post.getImage2().isEmpty()) {
                        mImageProvider.deleteFromPath(post.getImage2());
                    }
                }
                Toast.makeText(context, "Publicación eliminada exitosamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Error al eliminar la publicación", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_my_post, parent, false);
        return new ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout frameLayoutMyPost;
        MaterialTextView textViewTitle, textViewRelativeTime;
        CircleImageView circleImagePost;
        ShapeableImageView imageViewEdit, imageViewDelete;
        View viewHolder;

        public ViewHolder(View view) {
            super(view);
            frameLayoutMyPost = view.findViewById(R.id.frameLayoutMyPost);
            textViewTitle = view.findViewById(R.id.textViewTitleMyPost);
            textViewRelativeTime = view.findViewById(R.id.textViewRelativeTimeMyPost);
            circleImagePost = view.findViewById(R.id.circleImageMyPost);
            imageViewEdit = view.findViewById(R.id.imageViewEditMyPost);
            imageViewDelete = view.findViewById(R.id.imageViewDeleteMyPost);
            viewHolder = view;
        }
    }
}