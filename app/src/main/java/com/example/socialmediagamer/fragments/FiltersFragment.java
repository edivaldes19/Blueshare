package com.example.socialmediagamer.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.socialmediagamer.R;
import com.example.socialmediagamer.activities.FiltersActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

import java.util.Objects;

public class FiltersFragment extends Fragment {
    View mView;
    MaterialCardView mCardViewCulture, mCardViewSport, mCardViewLifestyle, mCardViewMusic, mCardViewProgramation, mCardViewVideogames;
    MaterialToolbar mToolbar;

    public FiltersFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_filters, container, false);
        mToolbar = mView.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(mToolbar);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle("Categorías");
        mCardViewCulture = mView.findViewById(R.id.cardViewCulture);
        mCardViewSport = mView.findViewById(R.id.cardViewSport);
        mCardViewLifestyle = mView.findViewById(R.id.cardViewLifestyle);
        mCardViewMusic = mView.findViewById(R.id.cardViewMusic);
        mCardViewProgramation = mView.findViewById(R.id.cardViewProgramation);
        mCardViewVideogames = mView.findViewById(R.id.cardViewVideogames);
        mCardViewCulture.setOnClickListener(v -> goToFilterActivity("Cultura"));
        mCardViewSport.setOnClickListener(v -> goToFilterActivity("Deporte"));
        mCardViewLifestyle.setOnClickListener(v -> goToFilterActivity("Estilo de vida"));
        mCardViewMusic.setOnClickListener(v -> goToFilterActivity("Música"));
        mCardViewProgramation.setOnClickListener(v -> goToFilterActivity("Programación"));
        mCardViewVideogames.setOnClickListener(v -> goToFilterActivity("Videojuegos"));
        return mView;
    }

    private void goToFilterActivity(String category) {
        Intent intent = new Intent(getContext(), FiltersActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
    }
}