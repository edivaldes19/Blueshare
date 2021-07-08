package com.manuel.blueshare.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.manuel.blueshare.models.Message;

import java.util.HashMap;
import java.util.Map;

public class MessageProvider {
    private final CollectionReference mCollection;

    public MessageProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Messages");
    }

    public Task<Void> create(Message message) {
        DocumentReference document = mCollection.document();
        message.setId(document.getId());
        return document.set(message);
    }

    public Query getMessageByChat(String idChat) {
        return mCollection.whereEqualTo("idChat", idChat).orderBy("timestamp", Query.Direction.ASCENDING);
    }

    public Query getMessageByChatAndSender(String idChat, String idSender) {
        return mCollection.whereEqualTo("idChat", idChat).whereEqualTo("idSender", idSender).whereEqualTo("viewed", false);
    }

    public Query getLastThreeMessagesByChatAndSender(String idChat, String idSender) {
        return mCollection.whereEqualTo("idChat", idChat).whereEqualTo("idSender", idSender).whereEqualTo("viewed", false).orderBy("timestamp", Query.Direction.DESCENDING).limit(3);
    }

    public Query getLastMessage(String idChat) {
        return mCollection.whereEqualTo("idChat", idChat).orderBy("timestamp", Query.Direction.DESCENDING).limit(1);
    }

    public Query getLastMessageSender(String idChat, String idSender) {
        return mCollection.whereEqualTo("idChat", idChat).whereEqualTo("idSender", idSender).orderBy("timestamp", Query.Direction.DESCENDING).limit(1);
    }

    public void updateViewed(String idDocument, boolean state) {
        Map<String, Object> map = new HashMap<>();
        map.put("viewed", state);
        mCollection.document(idDocument).update(map);
    }
}