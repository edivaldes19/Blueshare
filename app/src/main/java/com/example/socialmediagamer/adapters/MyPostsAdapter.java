package com.example.socialmediagamer.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediagamer.R;
import com.example.socialmediagamer.activities.PostActivity;
import com.example.socialmediagamer.activities.PostDetailActivity;
import com.example.socialmediagamer.models.Post;
import com.example.socialmediagamer.providers.AuthProvider;
import com.example.socialmediagamer.providers.ImageProvider;
import com.example.socialmediagamer.providers.LikesProvider;
import com.example.socialmediagamer.providers.PostProvider;
import com.example.socialmediagamer.providers.UsersProvider;
import com.example.socialmediagamer.utils.RelativeTime;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsAdapter extends FirestoreRecyclerAdapter<Post, MyPostsAdapter.ViewHolder> {
    Context context;
    UsersProvider mUsersProvider;
    LikesProvider mLikesProvider;
    AuthProvider mAuthProvider;
    PostProvider mPostProvider;
    ImageProvider mImageProvider;
    String pathImagePrimary, pathImageSecondary;

    public MyPostsAdapter(FirestoreRecyclerOptions<Post> options, Context context) {
        super(options);
        this.context = context;
        mUsersProvider = new UsersProvider();
        mLikesProvider = new LikesProvider();
        mAuthProvider = new AuthProvider();
        mPostProvider = new PostProvider();
        mImageProvider = new ImageProvider();
    }

    @Override
    protected void onBindViewHolder(@NonNull final MyPostsAdapter.ViewHolder holder, int position, @NonNull final Post post) {
        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        final String postId = document.getId();
        String relativeTime = RelativeTime.getTimeAgo(post.getTimestamp(), context);
        holder.textViewRelativeTime.setText(relativeTime);
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
                pathImagePrimary = post.getImage1();
                Picasso.with(context).load(post.getImage1()).into(holder.circleImagePost);
            }
        }
        if (post.getImage2() != null) {
            if (!post.getImage2().isEmpty()) {
                pathImageSecondary = post.getImage2();
            }
        }
        holder.viewHolder.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("id", postId);
            context.startActivity(intent);
        });
        holder.imageViewEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostActivity.class);
            intent.putExtra("idPostUpdate", postId);
            intent.putExtra("PostSelect", true);
            context.startActivity(intent);
        });
        holder.imageViewDelete.setOnClickListener(v -> showConfirmDelete(postId));
    }

    private void showConfirmDelete(String postId) {
        new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_menu_delete).setTitle("Eliminar publicación").setMessage("¿Está seguro de realizar esta acción?").setCancelable(false).setPositiveButton("Eliminar", (dialog, which) -> deletePost(postId)).setNegativeButton("Cancelar", null).show();
    }

    private void deletePost(String postId) {
        mPostProvider.delete(postId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mImageProvider.deleteFromPath(pathImagePrimary);
                mImageProvider.deleteFromPath(pathImageSecondary);
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
        MaterialTextView textViewTitle, textViewRelativeTime;
        CircleImageView circleImagePost;
        ShapeableImageView imageViewEdit, imageViewDelete;
        View viewHolder;

        public ViewHolder(View view) {
            super(view);
            textViewTitle = view.findViewById(R.id.textViewTitleMyPost);
            textViewRelativeTime = view.findViewById(R.id.textViewRelativeTimeMyPost);
            circleImagePost = view.findViewById(R.id.circleImageMyPost);
            imageViewEdit = view.findViewById(R.id.imageViewEditMyPost);
            imageViewDelete = view.findViewById(R.id.imageViewDeleteMyPost);
            viewHolder = view;
        }
    }
}