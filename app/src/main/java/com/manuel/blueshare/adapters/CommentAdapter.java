package com.manuel.blueshare.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
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
import com.manuel.blueshare.models.Comment;
import com.manuel.blueshare.providers.AuthProvider;
import com.manuel.blueshare.providers.CommentsProvider;
import com.manuel.blueshare.providers.UsersProvider;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends FirestoreRecyclerAdapter<Comment, CommentAdapter.ViewHolder> {
    Context context;
    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;
    CommentsProvider mCommentsProvider;

    public CommentAdapter(FirestoreRecyclerOptions<Comment> options, Context context) {
        super(options);
        this.context = context;
        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();
        mCommentsProvider = new CommentsProvider();
    }

    @Override
    protected void onBindViewHolder(@NonNull CommentAdapter.ViewHolder holder, int position, @NonNull Comment comment) {
        holder.frameLayoutComments.setAnimation(AnimationUtils.loadAnimation(context, R.anim.slide));
        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        final String idUser = document.getString("idUser");
        final String commentId = document.getId();
        holder.textViewComment.setText(comment.getComment());
        getUserInfo(idUser, holder);
        if (comment.getIdUser().equals(mAuthProvider.getUid())) {
            holder.imageViewDeleteComment.setVisibility(View.VISIBLE);
        } else {
            holder.imageViewDeleteComment.setVisibility(View.GONE);
        }
        holder.imageViewDeleteComment.setOnClickListener(v -> showConfirmDelete(commentId));
    }

    private void showConfirmDelete(String commentId) {
        new AlertDialog.Builder(context).setIcon(R.drawable.ic_delete).setTitle("Eliminar comentario").setMessage("¿Está seguro de realizar esta acción?").setCancelable(false).setPositiveButton("Eliminar", (dialog, which) -> deleteComment(commentId)).setNegativeButton("Cancelar", null).show();
    }

    private void deleteComment(String idUser) {
        mCommentsProvider.delete(idUser).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Comentario eliminado exitosamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Error al eliminar el comentario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUserInfo(String idUser, final ViewHolder holder) {
        mUsersProvider.getUser(idUser).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                if (documentSnapshot.contains("username")) {
                    String username = documentSnapshot.getString("username");
                    holder.textViewUsername.setText(username);
                }
                if (documentSnapshot.contains("image_profile")) {
                    String imageProfile = documentSnapshot.getString("image_profile");
                    if (!TextUtils.isEmpty(imageProfile)) {
                        Picasso.get().load(imageProfile).into(holder.imageViewComment);
                    }
                }
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_comment, parent, false);
        return new ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout frameLayoutComments;
        MaterialTextView textViewUsername, textViewComment;
        CircleImageView imageViewComment;
        ShapeableImageView imageViewDeleteComment;
        View viewHolder;

        public ViewHolder(View view) {
            super(view);
            frameLayoutComments = view.findViewById(R.id.frameLayoutComments);
            textViewUsername = view.findViewById(R.id.textViewUsername);
            textViewComment = view.findViewById(R.id.textViewComment);
            imageViewComment = view.findViewById(R.id.circleImageComment);
            imageViewDeleteComment = view.findViewById(R.id.imageViewDeleteComment);
            viewHolder = view;
        }
    }
}