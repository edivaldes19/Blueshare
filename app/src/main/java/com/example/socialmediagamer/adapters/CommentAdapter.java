package com.example.socialmediagamer.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediagamer.R;
import com.example.socialmediagamer.models.Comment;
import com.example.socialmediagamer.providers.AuthProvider;
import com.example.socialmediagamer.providers.CommentsProvider;
import com.example.socialmediagamer.providers.UsersProvider;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentSnapshot;
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
                    if (imageProfile != null) {
                        if (!imageProfile.isEmpty()) {
                            Picasso.get().load(imageProfile).into(holder.imageViewComment);
                        }
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
        MaterialTextView textViewUsername, textViewComment;
        CircleImageView imageViewComment;
        ShapeableImageView imageViewDeleteComment;
        View viewHolder;

        public ViewHolder(View view) {
            super(view);
            textViewUsername = view.findViewById(R.id.textViewUsername);
            textViewComment = view.findViewById(R.id.textViewComment);
            imageViewComment = view.findViewById(R.id.circleImageComment);
            imageViewDeleteComment = view.findViewById(R.id.imageViewDeleteComment);
            viewHolder = view;
        }
    }
}