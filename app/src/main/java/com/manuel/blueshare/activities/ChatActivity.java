package com.manuel.blueshare.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;
import com.manuel.blueshare.R;
import com.manuel.blueshare.adapters.MessagesAdapter;
import com.manuel.blueshare.models.Chat;
import com.manuel.blueshare.models.FCMBody;
import com.manuel.blueshare.models.FCMResponse;
import com.manuel.blueshare.models.Message;
import com.manuel.blueshare.providers.AuthProvider;
import com.manuel.blueshare.providers.ChatsProvider;
import com.manuel.blueshare.providers.MessageProvider;
import com.manuel.blueshare.providers.NotificationProvider;
import com.manuel.blueshare.providers.TokenProvider;
import com.manuel.blueshare.providers.UsersProvider;
import com.manuel.blueshare.utils.RelativeTime;
import com.manuel.blueshare.utils.ViewedMessageHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    CoordinatorLayout coordinatorLayout;
    ChatsProvider mChatsProvider;
    MessageProvider mMessageProvider;
    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;
    NotificationProvider mNotificationProvider;
    TokenProvider mTokenProvider;
    TextInputEditText mEditTextMessage;
    CircleImageView mCircleImageProfile, mImageViewSendMessage;
    MaterialTextView mTextViewUsername, mTextViewRelativeTime;
    ShapeableImageView mImageViewBack;
    RecyclerView mRecyclerViewMessage;
    MessagesAdapter mAdapter;
    View mActionBarView;
    LinearLayoutManager mLinearLayoutManager;
    ListenerRegistration mListener;
    String mExtraIdUser1, mExtraIdUser2, mExtraIdChat, mMyUsername, mUsernameChat, mImageReceiver = "", mImageSender = "";
    long mIdNotificationChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        coordinatorLayout = findViewById(R.id.coordinatorChat);
        mChatsProvider = new ChatsProvider();
        mMessageProvider = new MessageProvider();
        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();
        mNotificationProvider = new NotificationProvider();
        mTokenProvider = new TokenProvider();
        mEditTextMessage = findViewById(R.id.editTextMessage);
        mImageViewSendMessage = findViewById(R.id.circleImageViewSendMessage);
        mRecyclerViewMessage = findViewById(R.id.recyclerViewMessage);
        mLinearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        mLinearLayoutManager.setStackFromEnd(true);
        mRecyclerViewMessage.setLayoutManager(mLinearLayoutManager);
        mExtraIdUser1 = getIntent().getStringExtra("idUser1");
        mExtraIdUser2 = getIntent().getStringExtra("idUser2");
        mExtraIdChat = getIntent().getStringExtra("idChat");
        showCustomToolbar();
        getMyInfoUser();
        mImageViewSendMessage.setOnClickListener(view -> sendMessage());
        checkIfChatExist();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
        ViewedMessageHelper.updateOnline(true, ChatActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, ChatActivity.this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListener != null) {
            mListener.remove();
        }
    }

    private void getMessageChat() {
        Query query = mMessageProvider.getMessageByChat(mExtraIdChat);
        FirestoreRecyclerOptions<Message> options = new FirestoreRecyclerOptions.Builder<Message>().setQuery(query, Message.class).build();
        mAdapter = new MessagesAdapter(options, ChatActivity.this);
        mRecyclerViewMessage.setAdapter(mAdapter);
        mAdapter.startListening();
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                updateViewed();
                int numberMessage = mAdapter.getItemCount();
                int lastMessagePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastMessagePosition == -1 || (positionStart >= (numberMessage - 1) && lastMessagePosition == (positionStart - 1))) {
                    mRecyclerViewMessage.scrollToPosition(positionStart);
                }
            }
        });
    }

    private void sendMessage() {
        String textMessage = Objects.requireNonNull(mEditTextMessage.getText()).toString().trim();
        if (!textMessage.isEmpty()) {
            final Message message = new Message();
            message.setIdChat(mExtraIdChat);
            if (mAuthProvider.getUid().equals(mExtraIdUser1)) {
                message.setIdSender(mExtraIdUser1);
                message.setIdReceiver(mExtraIdUser2);
            } else {
                message.setIdSender(mExtraIdUser2);
                message.setIdReceiver(mExtraIdUser1);
            }
            message.setTimestamp(new Date().getTime());
            message.setViewed(false);
            message.setIdChat(mExtraIdChat);
            message.setMessage(textMessage);
            mMessageProvider.create(message).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    mEditTextMessage.setText(null);
                    mAdapter.notifyDataSetChanged();
                    getToken(message);
                } else {
                    Snackbar.make(coordinatorLayout, "Datos inválidos", Snackbar.LENGTH_SHORT).show();
                }
            });
        }
    }

    @SuppressLint("InflateParams")
    private void showCustomToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mActionBarView = inflater.inflate(R.layout.custom_chat_toolbar, null);
        actionBar.setCustomView(mActionBarView);
        mCircleImageProfile = mActionBarView.findViewById(R.id.circleImageProfile);
        mTextViewUsername = mActionBarView.findViewById(R.id.textViewUsername);
        mTextViewRelativeTime = mActionBarView.findViewById(R.id.textViewRelativeTime);
        mImageViewBack = mActionBarView.findViewById(R.id.imageViewBack);
        mImageViewBack.setOnClickListener(view -> finish());
        getUserInfo();
    }

    @SuppressLint("SetTextI18n")
    private void getUserInfo() {
        String idUserInfo;
        if (mAuthProvider.getUid().equals(mExtraIdUser1)) {
            idUserInfo = mExtraIdUser2;
        } else {
            idUserInfo = mExtraIdUser1;
        }
        mListener = mUsersProvider.getUserRealtime(idUserInfo).addSnapshotListener((documentSnapshot, e) -> {
            if (documentSnapshot != null) {
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.contains("username")) {
                        mUsernameChat = documentSnapshot.getString("username");
                        mTextViewUsername.setText(mUsernameChat);
                    }
                    if (documentSnapshot.contains("online")) {
                        boolean online = documentSnapshot.getBoolean("online");
                        if (online) {
                            mTextViewRelativeTime.setText("en línea");
                        } else if (documentSnapshot.contains("lastConnection")) {
                            long lastConnection = documentSnapshot.getLong("lastConnection");
                            String relativeTime = RelativeTime.getTimeAgo(lastConnection);
                            mTextViewRelativeTime.setText("Conectado(a) " + relativeTime.toLowerCase());
                        }
                    }
                    if (documentSnapshot.contains("image_profile")) {
                        mImageReceiver = documentSnapshot.getString("image_profile");
                        if (mImageReceiver != null) {
                            if (!mImageReceiver.equals("")) {
                                Picasso.get().load(mImageReceiver).into(mCircleImageProfile);
                            }
                        }
                    }
                }
            }
        });
    }

    private void checkIfChatExist() {
        mChatsProvider.getChatByUser1AndUser2(mExtraIdUser1, mExtraIdUser2).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null) {
                int size = queryDocumentSnapshots.size();
                if (size == 0) {
                    createChat();
                } else {
                    mExtraIdChat = queryDocumentSnapshots.getDocuments().get(0).getId();
                    mIdNotificationChat = queryDocumentSnapshots.getDocuments().get(0).getLong("idNotification");
                    getMessageChat();
                    updateViewed();
                }
            }
        });
    }

    private void updateViewed() {
        String idSender;
        if (mAuthProvider.getUid().equals(mExtraIdUser1)) {
            idSender = mExtraIdUser2;
        } else {
            idSender = mExtraIdUser1;
        }
        mMessageProvider.getMessageByChatAndSender(mExtraIdChat, idSender).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                mMessageProvider.updateViewed(document.getId(), true);
            }
        });
    }

    private void createChat() {
        Chat chat = new Chat();
        chat.setIdUser1(mExtraIdUser1);
        chat.setIdUser2(mExtraIdUser2);
        chat.setTimestamp(new Date().getTime());
        chat.setId(mExtraIdUser1 + mExtraIdUser2);
        Random random = new Random();
        int n = random.nextInt(1000000);
        chat.setIdNotification(n);
        mIdNotificationChat = n;
        ArrayList<String> ids = new ArrayList<>();
        ids.add(mExtraIdUser1);
        ids.add(mExtraIdUser2);
        chat.setIds(ids);
        mChatsProvider.create(chat);
        mExtraIdChat = chat.getId();
        getMessageChat();
    }

    private void getToken(final Message message) {
        String idUser;
        if (mAuthProvider.getUid().equals(mExtraIdUser1)) {
            idUser = mExtraIdUser2;
        } else {
            idUser = mExtraIdUser1;
        }
        mTokenProvider.getToken(idUser).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                if (documentSnapshot.contains("token")) {
                    String token = documentSnapshot.getString("token");
                    getLastThreeMessages(message, token);
                }
            } else {
                Toast.makeText(ChatActivity.this, "El token de notificaciones del usuario no existe", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLastThreeMessages(final Message message, final String token) {
        mMessageProvider.getLastThreeMessagesByChatAndSender(mExtraIdChat, mAuthProvider.getUid()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            ArrayList<Message> messageArrayList = new ArrayList<>();
            for (DocumentSnapshot d : queryDocumentSnapshots.getDocuments()) {
                if (d.exists()) {
                    Message message1 = d.toObject(Message.class);
                    messageArrayList.add(message1);
                }
            }
            if (messageArrayList.size() == 0) {
                messageArrayList.add(message);
            }
            Collections.reverse(messageArrayList);
            Gson gson = new Gson();
            String messages = gson.toJson(messageArrayList);
            sendNotification(token, messages, message);
        });
    }

    private void sendNotification(final String token, String messages, Message message) {
        final Map<String, String> data = new HashMap<>();
        data.put("title", "Nuevo mensaje");
        data.put("body", message.getMessage());
        data.put("idNotification", String.valueOf(mIdNotificationChat));
        data.put("messages", messages);
        data.put("usernameSender", mMyUsername);
        data.put("usernameReceiver", mUsernameChat);
        data.put("idSender", message.getIdSender());
        data.put("idReceiver", message.getIdReceiver());
        data.put("idChat", message.getIdChat());
        if (mImageSender.equals("")) {
            mImageSender = "IMAGEN_NO_VALIDA";
        }
        if (mImageReceiver.equals("")) {
            mImageReceiver = "IMAGEN_NO_VALIDA";
        }
        data.put("imageSender", mImageSender);
        data.put("imageReceiver", mImageReceiver);
        String idSender;
        if (mAuthProvider.getUid().equals(mExtraIdUser1)) {
            idSender = mExtraIdUser2;
        } else {
            idSender = mExtraIdUser1;
        }
        mMessageProvider.getLastMessageSender(mExtraIdChat, idSender).get().addOnSuccessListener(queryDocumentSnapshots -> {
            int size = queryDocumentSnapshots.size();
            String lastMessage;
            if (size > 0) {
                lastMessage = queryDocumentSnapshots.getDocuments().get(0).getString("message");
                data.put("lastMessage", lastMessage);
            }
            FCMBody body = new FCMBody(token, "high", "4500s", data);
            mNotificationProvider.sendNotification(body).enqueue(new Callback<FCMResponse>() {
                @Override
                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                    if (response.body() != null) {
                        if (response.body().getSuccess() == 1) {
                            //Notificación enviada exitosamente
                        } else {
                            Toast.makeText(ChatActivity.this, "Error al enviar notificación", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ChatActivity.this, "Error al enviar notificación, cuerpo nulo", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<FCMResponse> call, Throwable t) {
                }
            });
        });
    }

    private void getMyInfoUser() {
        mUsersProvider.getUser(mAuthProvider.getUid()).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                if (documentSnapshot.contains("username")) {
                    mMyUsername = documentSnapshot.getString("username");
                }
                if (documentSnapshot.contains("image_profile")) {
                    mImageSender = documentSnapshot.getString("image_profile");
                }
            }
        });
    }
}