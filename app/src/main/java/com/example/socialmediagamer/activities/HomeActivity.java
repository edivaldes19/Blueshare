package com.example.socialmediagamer.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.socialmediagamer.R;
import com.example.socialmediagamer.fragments.ChatsFragment;
import com.example.socialmediagamer.fragments.FiltersFragment;
import com.example.socialmediagamer.fragments.HomeFragment;
import com.example.socialmediagamer.fragments.ProfileFragment;
import com.example.socialmediagamer.providers.AuthProvider;
import com.example.socialmediagamer.providers.TokenProvider;
import com.example.socialmediagamer.providers.UsersProvider;
import com.example.socialmediagamer.utils.ViewedMessageHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity implements ConnectionReceiver.ReceiverListener {
    BottomNavigationView bottomNavigationView;
    TokenProvider mTokenProvider;
    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;
    String mImageProfile = "", mImageCover = "";
    boolean isPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        mTokenProvider = new TokenProvider();
        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();
        openFragment(new HomeFragment());
        createToken();
        checkConnection();
        getUser();
    }

    private void getUser() {
        mUsersProvider.getUser(mAuthProvider.getUid()).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                if (documentSnapshot.contains("image_profile") && documentSnapshot.contains("image_cover")) {
                    mImageProfile = documentSnapshot.getString("image_profile");
                    mImageCover = documentSnapshot.getString("image_cover");
                    if ((mImageProfile == null || mImageProfile.isEmpty()) || (mImageCover == null || mImageCover.isEmpty())) {
                        new AlertDialog.Builder(this).setIcon(R.drawable.ic_tip).setTitle("Recomendación").setMessage("Establezca una imagen de perfil y de portada para evitar posibles bloqueos en la aplicación.").setCancelable(false).
                                setPositiveButton("Ir a editar perfil", (dialog, which) -> {
                                    startActivity(new Intent(HomeActivity.this, EditProfileActivity.class));
                                }).setNegativeButton("En otro momento", null).show();
                    }
                }
            }
        });
    }

    private void checkConnection() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(new ConnectionReceiver(), intentFilter);
        ConnectionReceiver.listener = this;
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
        showSnackBar(isConnected);
    }

    private void showSnackBar(boolean isConnected) {
        if (!isConnected) {
            Toast.makeText(getApplicationContext(), "Error de red, verifique su conexión", Toast.LENGTH_LONG).show();
            finishAffinity();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ViewedMessageHelper.updateOnline(true, HomeActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, HomeActivity.this);
        checkConnection();
    }

    public void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = item -> {
        if (item.getItemId() == R.id.itemHome) {
            openFragment(new HomeFragment());
        } else if (item.getItemId() == R.id.itemChats) {
            openFragment(new ChatsFragment());
        } else if (item.getItemId() == R.id.itemFilters) {
            openFragment(new FiltersFragment());
        } else if (item.getItemId() == R.id.itemProfile) {
            openFragment(new ProfileFragment());
        }
        return true;
    };

    private void createToken() {
        mTokenProvider.create(mAuthProvider.getUid());
    }

    @Override
    public void onBackPressed() {
        if (isPressed) {
            finishAffinity();
            System.exit(0);
        } else {
            Toast.makeText(getApplicationContext(), "Presione de nuevo para salir", Toast.LENGTH_SHORT).show();
            isPressed = true;
        }
        Runnable runnable = () -> isPressed = false;
        new Handler().postDelayed(runnable, 3000);
    }

    @Override
    public void onNetworkChange(boolean isConnected) {
        showSnackBar(isConnected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkConnection();
    }
}