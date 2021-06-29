package com.example.socialmediagamer.providers;

import com.example.socialmediagamer.models.Post;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PostProvider {
    private final CollectionReference mCollection;

    public PostProvider() {
        mCollection = FirebaseFirestore.getInstance().collection("Posts");
    }

    public Task<Void> save(Post post) {
        String id = mCollection.document().getId();
        post.setId(id);
        return mCollection.document(id).set(post);
    }

    public Task<Void> update(Post post) {
        Map<String, Object> map = new HashMap<>();
        map.put("category", post.getCategory());
        map.put("description", post.getDescription());
        map.put("image1", post.getImage1());
        map.put("image2", post.getImage2());
        map.put("timestamp", new Date().getTime());
        map.put("title", post.getTitle());
        return mCollection.document(post.getId()).update(map);
    }

    public Query getAll() {
        return mCollection.orderBy("timestamp", Query.Direction.DESCENDING);
    }

    public Query getPostByCategoryAndTimestamp(String category) {
        return mCollection.whereEqualTo("category", category).orderBy("timestamp", Query.Direction.DESCENDING);
    }

    public Query getPostByTitle(String title) {
        return mCollection.orderBy("title").startAt(title).endAt(title + '\uf8ff');
    }

    public Query getPostByUser(String id) {
        return mCollection.whereEqualTo("idUser", id);
    }

    public Task<DocumentSnapshot> getPostById(String id) {
        return mCollection.document(id).get();
    }

    public Task<Void> delete(String id) {
        return mCollection.document(id).delete();
    }
}