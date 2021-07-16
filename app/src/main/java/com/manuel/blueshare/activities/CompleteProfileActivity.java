package com.manuel.blueshare.activities;

import static com.manuel.blueshare.utils.MyTools.compareDataString;
import static com.manuel.blueshare.utils.MyTools.isUserInfoExist;
import static com.manuel.blueshare.utils.MyTools.validateFieldsAsYouType;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.manuel.blueshare.R;
import com.manuel.blueshare.models.User;
import com.manuel.blueshare.providers.AuthProvider;
import com.manuel.blueshare.providers.UsersProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class CompleteProfileActivity extends AppCompatActivity {
    CoordinatorLayout coordinatorLayout;
    TextInputEditText mTextInputUsername, mTextInputPhone;
    MaterialButton materialButtonRegister;
    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;
    ProgressDialog mDialog;
    ArrayList<String> mUsernameList, mPhoneList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);
        coordinatorLayout = findViewById(R.id.coordinatorComplete);
        mTextInputUsername = findViewById(R.id.textInputUsernameConfirm);
        mTextInputPhone = findViewById(R.id.textInputPhone);
        materialButtonRegister = findViewById(R.id.btnConfirm);
        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();
        mUsernameList = new ArrayList<>();
        mPhoneList = new ArrayList<>();
        mDialog = new ProgressDialog(this);
        mDialog.setTitle("Registrando...");
        mDialog.setMessage("Por favor, espere un momento");
        mDialog.setCancelable(false);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        validateFieldsAsYouType(mTextInputUsername, "El nombre de usuario es obligatorio");
        validateFieldsAsYouType(mTextInputPhone, "El número de teléfono es obligatorio");
        isUserInfoExist(mUsersProvider, mUsernameList, "username", coordinatorLayout);
        isUserInfoExist(mUsersProvider, mPhoneList, "phone", coordinatorLayout);
        materialButtonRegister.setOnClickListener(v -> completeInfo());
    }

    private void completeInfo() {
        String username = Objects.requireNonNull(mTextInputUsername.getText()).toString().trim();
        String phone = Objects.requireNonNull(mTextInputPhone.getText()).toString().trim();
        if (!TextUtils.isEmpty(username)) {
            if (!TextUtils.isEmpty(phone)) {
                compareDataString(mUsernameList, username, coordinatorLayout, "Ya existe un usuario con ese nombre");
                compareDataString(mPhoneList, phone, coordinatorLayout, "Ya existe un usuario con ese teléfono");
                updateUser(username, phone);
            } else {
                Snackbar.make(coordinatorLayout, "El número de teléfono es obligatorio", Snackbar.LENGTH_SHORT).show();
            }
        } else {
            Snackbar.make(coordinatorLayout, "El nombre de usuario es obligatorio", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void updateUser(String username, String phone) {
        String id = mAuthProvider.getUid();
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPhone(phone);
        user.setTimestamp(new Date().getTime());
        mDialog.show();
        mUsersProvider.update(user).addOnCompleteListener(task1 -> {
            mDialog.dismiss();
            if (task1.isSuccessful()) {
                startActivity(new Intent(CompleteProfileActivity.this, HomeActivity.class));
                finish();
            } else {
                Snackbar.make(coordinatorLayout, "Error al registrar el usuario en la base de datos", Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}