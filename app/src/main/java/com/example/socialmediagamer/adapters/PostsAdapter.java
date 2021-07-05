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
import com.example.socialmediagamer.providers.CommentsProvider;
import com.example.socialmediagamer.providers.LikesProvider;
import com.example.socialmediagamer.providers.UsersProvider;
import com.example.socialmediagamer.utils.RelativeTime;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.squareup.picasso.Picasso;

import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends FirestoreRecyclerAdapter<Post, PostsAdapter.ViewHolder> {
    Context context;
    UsersProvider mUsersProvider;
    LikesProvider mLikesProvider;
    CommentsProvider mCommentsProvider;
    AuthProvider mAuthProvider;
    MaterialTextView mTextViewNumberFilter;
    ListenerRegistration mListener, mListenerComments;

    public PostsAdapter(FirestoreRecyclerOptions<Post> options, Context context) {
        super(options);
        this.context = context;
        mUsersProvider = new UsersProvider();
        mLikesProvider = new LikesProvider();
        mCommentsProvider = new CommentsProvider();
        mAuthProvider = new AuthProvider();
    }

    public PostsAdapter(FirestoreRecyclerOptions<Post> options, Context context, MaterialTextView materialTextViewFilter) {
        super(options);
        this.context = context;
        mUsersProvider = new UsersProvider();
        mLikesProvider = new LikesProvider();
        mCommentsProvider = new CommentsProvider();
        mAuthProvider = new AuthProvider();
        mTextViewNumberFilter = materialTextViewFilter;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull final PostsAdapter.ViewHolder holder, int position, @NonNull final Post post) {
        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        final String postId = document.getId();
        String relativeTime = RelativeTime.getTimeAgo(post.getTimestamp());
        holder.textViewRelativeTime.setText("Publicado " + relativeTime.toLowerCase());
        if (mTextViewNumberFilter != null) {
            int numberFilter = getSnapshots().size();
            mTextViewNumberFilter.setText(String.valueOf(numberFilter));
        }
        holder.textViewTitle.setText(post.getTitle());
        holder.textViewDescription.setText(post.getDescription());
        if (post.getImage1() != null) {
            if (!post.getImage1().isEmpty()) {
                Picasso.get().load(post.getImage1()).into(holder.imageViewPost);
            }
        }
        holder.viewHolder.setOnClickListener(v ->
        {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("id", postId);
            context.startActivity(intent);
        });
        holder.imageViewLike.setOnClickListener(v ->
        {
            Like like = new Like();
            like.setIdUser(mAuthProvider.getUid());
            like.setIdPost(postId);
            like.setTimestamp(new Date().getTime());
            meGusta(like, holder);
        });
        getUserInfo(post.getIdUser(), holder);
        getNumberLikesByPost(postId, holder);
        getNumberCommentsByPost(postId, holder);
        checkIfExistLike(postId, mAuthProvider.getUid(), holder);
    }

    private void getNumberLikesByPost(String idPost, final ViewHolder holder) {
        mListener = mLikesProvider.getLikesByPost(idPost).addSnapshotListener((value, error) -> {
            if (value != null) {
                int numberLikes = value.size();
                holder.textViewContadorLikes.setText(String.valueOf(numberLikes));
            }
        });
    }

    private void getNumberCommentsByPost(String idPost, final ViewHolder holder) {
        mListenerComments = mCommentsProvider.getCommentsByPost(idPost).addSnapshotListener((value, error) -> {
            if (value != null) {
                int numberComments = value.size();
                holder.textViewContadorComments.setText(String.valueOf(numberComments));
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
                            Picasso.get().load(imageProfile).into(holder.circleImageViewUser);
                        }
                    }
                }
            }
        });
    }

    public ListenerRegistration getListener() {
        return mListener;
    }

    public ListenerRegistration getListenerComments() {
        return mListenerComments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_post, parent, false);
        return new ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageViewUser;
        MaterialTextView textViewTitle, textViewUsername, textViewRelativeTime, textViewDescription, textViewContadorLikes, textViewContadorComments;
        ShapeableImageView imageViewPost, imageViewLike;
        View viewHolder;

        public ViewHolder(View view) {
            super(view);
            circleImageViewUser = view.findViewById(R.id.circleImagePostAuthor);
            textViewTitle = view.findViewById(R.id.textViewTitlePostCard);
            textViewUsername = view.findViewById(R.id.textViewUsernamePostCard);
            textViewRelativeTime = view.findViewById(R.id.textViewRelativeTimePost);
            textViewDescription = view.findViewById(R.id.textViewDescriptionPostCard);
            textViewContadorLikes = view.findViewById(R.id.textViewContadorLikes);
            textViewContadorComments = view.findViewById(R.id.textViewContadorComments);
            imageViewPost = view.findViewById(R.id.imageViewPostCard);
            imageViewLike = view.findViewById(R.id.imageViewLike);
            viewHolder = view;
        }
    }
}