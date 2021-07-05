package com.example.socialmediagamer.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediagamer.R;
import com.example.socialmediagamer.activities.ChatActivity;
import com.example.socialmediagamer.models.Chat;
import com.example.socialmediagamer.providers.AuthProvider;
import com.example.socialmediagamer.providers.ChatsProvider;
import com.example.socialmediagamer.providers.MessageProvider;
import com.example.socialmediagamer.providers.UsersProvider;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsAdapter extends FirestoreRecyclerAdapter<Chat, ChatsAdapter.ViewHolder> {
    Context context;
    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;
    ChatsProvider mChatsProvider;
    MessageProvider mMessagesProvider;
    ListenerRegistration mListener, mListenerLastMessage;

    public ChatsAdapter(FirestoreRecyclerOptions<Chat> options, Context context) {
        super(options);
        this.context = context;
        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();
        mChatsProvider = new ChatsProvider();
        mMessagesProvider = new MessageProvider();
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull final Chat chat) {
        holder.frameLayoutChats.setAnimation(AnimationUtils.loadAnimation(context, R.anim.slide));
        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        final String chatId = document.getId();
        if (mAuthProvider.getUid().equals(chat.getIdUser1())) {
            getUserInfo(chat.getIdUser2(), holder);
        } else {
            getUserInfo(chat.getIdUser1(), holder);
        }
        holder.viewHolder.setOnClickListener(view -> goToChatActivity(chatId, chat.getIdUser1(), chat.getIdUser2()));
        getLastMessage(chatId, holder.textViewLastMessage);
        String idSender;
        if (mAuthProvider.getUid().equals(chat.getIdUser1())) {
            idSender = chat.getIdUser2();
        } else {
            idSender = chat.getIdUser1();
        }
        getMessageNotRead(chatId, idSender, holder.textViewMessageNotRead, holder.frameLayoutMessageNotRead);
    }

    private void getMessageNotRead(String chatId, String idSender, final MaterialTextView textViewMessageNotRead, final FrameLayout frameLayoutMessageNotRead) {
        mListener = mMessagesProvider.getMessageByChatAndSender(chatId, idSender).addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (queryDocumentSnapshots != null) {
                int size = queryDocumentSnapshots.size();
                if (size > 0) {
                    frameLayoutMessageNotRead.setVisibility(View.VISIBLE);
                    textViewMessageNotRead.setText(String.valueOf(size));
                } else {
                    frameLayoutMessageNotRead.setVisibility(View.GONE);
                }
            }
        });
    }

    public ListenerRegistration getListener() {
        return mListener;
    }

    public ListenerRegistration getListenerLastMessage() {
        return mListenerLastMessage;
    }

    private void getLastMessage(String chatId, final MaterialTextView textViewLastMessage) {
        mListenerLastMessage = mMessagesProvider.getLastMessage(chatId).addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (queryDocumentSnapshots != null) {
                int size = queryDocumentSnapshots.size();
                if (size > 0) {
                    String lastMessage = queryDocumentSnapshots.getDocuments().get(0).getString("message");
                    textViewLastMessage.setText(lastMessage);
                }
            }
        });
    }

    private void goToChatActivity(String chatId, String idUser1, String idUser2) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("idChat", chatId);
        intent.putExtra("idUser1", idUser1);
        intent.putExtra("idUser2", idUser2);
        context.startActivity(intent);
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
                            Picasso.get().load(imageProfile).into(holder.circleImageChat);
                        }
                    }
                }
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_chat, parent, false);
        return new ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout frameLayoutChats;
        MaterialTextView textViewUsername, textViewLastMessage, textViewMessageNotRead;
        CircleImageView circleImageChat;
        FrameLayout frameLayoutMessageNotRead;
        View viewHolder;

        public ViewHolder(View view) {
            super(view);
            frameLayoutChats = view.findViewById(R.id.frameLayoutChats);
            textViewUsername = view.findViewById(R.id.textViewUsernameChat);
            textViewLastMessage = view.findViewById(R.id.textViewLastMessageChat);
            textViewMessageNotRead = view.findViewById(R.id.textViewMessageNotRead);
            circleImageChat = view.findViewById(R.id.circleImageChat);
            frameLayoutMessageNotRead = view.findViewById(R.id.frameLayoutMessageNotRead);
            viewHolder = view;
        }
    }
}
