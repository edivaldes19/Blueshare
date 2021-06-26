package com.example.socialmediagamer.receivers;

import android.app.NotificationManager;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.socialmediagamer.models.FCMBody;
import com.example.socialmediagamer.models.FCMResponse;
import com.example.socialmediagamer.models.Message;
import com.example.socialmediagamer.providers.MessageProvider;
import com.example.socialmediagamer.providers.NotificationProvider;
import com.example.socialmediagamer.providers.TokenProvider;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.socialmediagamer.services.MyFirebaseMessagingClient.NOTIFICATION_REPLY;

public class MessageReceiver extends BroadcastReceiver {
    String mExtraIdSender;
    String mExtraIdReceiver;
    String mExtraIdChat;
    String mExtraUsernameSender;
    String mExtraUsernameReceiver;
    String mExtraImageSender;
    String mExtraImageReceiver;
    int mExtraIdNotification;
    TokenProvider mTokenProvider;
    NotificationProvider mNotificationProvider;

    @Override
    public void onReceive(Context context, Intent intent) {
        mExtraIdSender = intent.getExtras().getString("idSender");
        mExtraIdReceiver = intent.getExtras().getString("idReceiver");
        mExtraIdChat = intent.getExtras().getString("idChat");
        mExtraUsernameSender = intent.getExtras().getString("usernameSender");
        mExtraUsernameReceiver = intent.getExtras().getString("usernameReceiver");
        mExtraImageSender = intent.getExtras().getString("imageSender");
        mExtraImageReceiver = intent.getExtras().getString("imageReceiver");
        mExtraIdNotification = intent.getExtras().getInt("idNotification");
        mTokenProvider = new TokenProvider();
        mNotificationProvider = new NotificationProvider();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(mExtraIdNotification);
        String message = Objects.requireNonNull(getMessageText(intent)).toString();
        sendMessage(message);
    }

    private void sendMessage(String messageText) {
        final Message message = new Message();
        message.setIdChat(mExtraIdChat);
        message.setIdSender(mExtraIdReceiver);
        message.setIdReceiver(mExtraIdSender);
        message.setTimestamp(new Date().getTime());
        message.setViewed(false);
        message.setIdChat(mExtraIdChat);
        message.setMessage(messageText);
        MessageProvider messagesProvider = new MessageProvider();
        messagesProvider.create(message).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                getToken(message);
            }
        });
    }

    private void getToken(final Message message) {
        mTokenProvider.getToken(mExtraIdSender).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                if (documentSnapshot.contains("token")) {
                    String token = documentSnapshot.getString("token");
                    Gson gson = new Gson();
                    ArrayList<Message> messagesArray = new ArrayList<>();
                    messagesArray.add(message);
                    String messages = gson.toJson(messagesArray);
                    sendNotification(token, messages, message);
                }
            }
        });
    }

    private void sendNotification(final String token, String messages, Message message) {
        final Map<String, String> data = new HashMap<>();
        data.put("title", "Nuevo mensaje");
        data.put("body", message.getMessage());
        data.put("idNotification", String.valueOf(mExtraIdNotification));
        data.put("messages", messages);
        data.put("usernameSender", mExtraUsernameReceiver);
        data.put("usernameReceiver", mExtraUsernameSender);
        data.put("idSender", message.getIdSender());
        data.put("idReceiver", message.getIdReceiver());
        data.put("idChat", message.getIdChat());
        data.put("imageSender", mExtraImageReceiver);
        data.put("imageReceiver", mExtraImageSender);
        FCMBody body = new FCMBody(token, "high", "4500s", data);
        mNotificationProvider.sendNotification(body).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {
            }
        });

    }

    private CharSequence getMessageText(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(NOTIFICATION_REPLY);
        }
        return null;
    }
}