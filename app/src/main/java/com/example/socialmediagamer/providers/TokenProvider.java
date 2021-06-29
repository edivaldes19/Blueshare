package com.example.socialmediagamer.providers;

import com.example.socialmediagamer.models.Token;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class TokenProvider {
    private final CollectionReference mCollection;

    public TokenProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Tokens");
    }

    public void create(final String idUser) {
        if (idUser == null) {
            return;
        }
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s -> {
            Token token = new Token(s);
            mCollection.document(idUser).set(token);
        });
    }

    public Task<DocumentSnapshot> getToken(String idUser) {
        return mCollection.document(idUser).get();
    }
}