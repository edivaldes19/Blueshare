package com.example.socialmediagamer.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.example.socialmediagamer.R;
import com.example.socialmediagamer.models.User;
import com.example.socialmediagamer.providers.AuthProvider;
import com.example.socialmediagamer.providers.UsersProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Date;
import java.util.Objects;

import static com.example.socialmediagamer.utils.Validations.validateFieldsAsYouType;

public class CompleteProfileActivity extends AppCompatActivity {
    CoordinatorLayout coordinatorLayout;
    TextInputEditText mTextInputUsername, mTextInputPhone;
    MaterialButton materialButtonRegister;
    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;
    ProgressDialog mDialog;

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
        mDialog = new ProgressDialog(this);
        mDialog.setTitle("Registrando...");
        mDialog.setMessage("Por favor, espere un momento");
        mDialog.setCancelable(false);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        validateFieldsAsYouType(mTextInputUsername, "El nombre de usuario es obligatorio");
        validateFieldsAsYouType(mTextInputPhone, "El número de teléfono es obligatorio");
        materialButtonRegister.setOnClickListener(v -> {
            String username = Objects.requireNonNull(mTextInputUsername.getText()).toString().trim();
            String phone = Objects.requireNonNull(mTextInputPhone.getText()).toString().trim();
            if (!username.isEmpty() && !phone.isEmpty()) {
                updateUser(username, phone);
            } else {
                Snackbar.make(v, "Complete los campos", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUser(final String username, final String phone) {
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