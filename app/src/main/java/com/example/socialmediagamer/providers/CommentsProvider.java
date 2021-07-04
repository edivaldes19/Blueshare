package com.example.socialmediagamer.providers;

import com.example.socialmediagamer.models.Comment;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class CommentsProvider {
    private final CollectionReference mCollection;

    public CommentsProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Comments");
    }

    public Task<Void> create(Comment comment) {
        DocumentReference document = mCollection.document();
        String id = document.getId();
        comment.setId(id);
        return document.set(comment);
    }

    public Query getCommentsByPost(String idPost) {
        return mCollection.whereEqualTo("idPost", idPost);
    }

    public Task<Void> delete(String id) {
        return mCollection.document(id).delete();
    }
}