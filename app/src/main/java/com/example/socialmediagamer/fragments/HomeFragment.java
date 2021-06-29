package com.example.socialmediagamer.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediagamer.R;
import com.example.socialmediagamer.activities.ContactanosActivity;
import com.example.socialmediagamer.activities.InfoActivity;
import com.example.socialmediagamer.activities.MainActivity;
import com.example.socialmediagamer.activities.PostActivity;
import com.example.socialmediagamer.adapters.PostsAdapter;
import com.example.socialmediagamer.models.Post;
import com.example.socialmediagamer.providers.AuthProvider;
import com.example.socialmediagamer.providers.PostProvider;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.Query;
import com.mancj.materialsearchbar.MaterialSearchBar;

public class HomeFragment extends Fragment implements MaterialSearchBar.OnSearchActionListener {
    View mView;
    FloatingActionButton mFab;
    MaterialSearchBar mSearchBar;
    AuthProvider mAuthProvider;
    RecyclerView mRecyclerView;
    PostProvider mPostProvider;
    PostsAdapter mPostsAdapter, mPostsAdapterSearch;

    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_home, container, false);
        mFab = mView.findViewById(R.id.fab);
        mRecyclerView = mView.findViewById(R.id.recyclerViewHome);
        mSearchBar = mView.findViewById(R.id.searchBar);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAuthProvider = new AuthProvider();
        mPostProvider = new PostProvider();
        mSearchBar.setOnSearchActionListener(this);
        mSearchBar.inflateMenu(R.menu.main_menu);
        mSearchBar.getMenu().setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.itemLogout) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setTitle("¿Está seguro que desea cerrar sesión?");
                alert.setIcon(R.drawable.ic_logout);
                alert.setCancelable(false);
                alert.setPositiveButton("Cerrar sesión", (dialog, which) -> logout());
                alert.setNegativeButton("Cancelar", (dialog, which) -> {
                });
                alert.show();
            } else if (item.getItemId() == R.id.itemAcercaDe) {
                Intent intent = new Intent(getContext(), InfoActivity.class);
                startActivity(intent);
            } else if (item.getItemId() == R.id.itemContactanos) {
                Intent intent = new Intent(getContext(), ContactanosActivity.class);
                startActivity(intent);
            }
            return true;
        });
        mFab.setOnClickListener(v -> goToPost());
        showTooltip();
        return mView;
    }

    private void showTooltip() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mFab.setTooltipText("Crear una nueva publicación");
        }
    }

    private void searchByTitle(String title) {
        Query query = mPostProvider.getPostByTitle(title);
        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post.class).build();
        mPostsAdapterSearch = new PostsAdapter(options, getContext());
        mPostsAdapterSearch.notifyDataSetChanged();
        mRecyclerView.setAdapter(mPostsAdapterSearch);
        mPostsAdapterSearch.startListening();
    }

    private void getAllPost() {
        Query query = mPostProvider.getAll();
        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post.class).build();
        mPostsAdapter = new PostsAdapter(options, getContext());
        mPostsAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mPostsAdapter);
        mPostsAdapter.startListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        getAllPost();
    }

    @Override
    public void onStop() {
        super.onStop();
        mPostsAdapter.stopListening();
        if (mPostsAdapterSearch != null) {
            mPostsAdapterSearch.stopListening();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPostsAdapter.getListener() != null) {
            mPostsAdapter.getListener().remove();
        }
    }

    private void goToPost() {
        startActivity(new Intent(getContext(), PostActivity.class));
    }

    private void logout() {
        mAuthProvider.logout();
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
        if (!enabled) {
            getAllPost();
        }
    }

    @Override
    public void onSearchConfirmed(CharSequence text) {
        searchByTitle(text.toString());
    }

    @Override
    public void onButtonClicked(int buttonCode) {
    }
}