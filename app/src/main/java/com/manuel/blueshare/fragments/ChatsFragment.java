package com.manuel.blueshare.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.Query;
import com.manuel.blueshare.R;
import com.manuel.blueshare.adapters.ChatsAdapter;
import com.manuel.blueshare.models.Chat;
import com.manuel.blueshare.providers.AuthProvider;
import com.manuel.blueshare.providers.ChatsProvider;

import java.util.Objects;

public class ChatsFragment extends Fragment {
    View mView;
    ChatsAdapter mAdapter;
    RecyclerView mRecyclerView;
    ChatsProvider mChatsProvider;
    AuthProvider mAuthProvider;
    MaterialToolbar mToolbar;

    public ChatsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_chats, container, false);
        mRecyclerView = mView.findViewById(R.id.recyclerViewChats);
        mToolbar = mView.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(mToolbar);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle("Chats");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mChatsProvider = new ChatsProvider();
        mAuthProvider = new AuthProvider();
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Query query = mChatsProvider.getAll(mAuthProvider.getUid());
        FirestoreRecyclerOptions<Chat> options = new FirestoreRecyclerOptions.Builder<Chat>().setQuery(query, Chat.class).build();
        mAdapter = new ChatsAdapter(options, getContext());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter.getListener() != null) {
            mAdapter.getListener().remove();
        }
        if (mAdapter.getListenerLastMessage() != null) {
            mAdapter.getListenerLastMessage().remove();
        }
    }
}