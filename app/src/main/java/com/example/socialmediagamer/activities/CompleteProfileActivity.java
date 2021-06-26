package com.example.socialmediagamer.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.socialmediagamer.R;
import com.example.socialmediagamer.models.User;
import com.example.socialmediagamer.providers.AuthProvider;
import com.example.socialmediagamer.providers.UsersProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Date;
import java.util.Objects;

public class CompleteProfileActivity extends AppCompatActivity {
    TextInputEditText mtextInputUsername, mtextInputPhone;
    MaterialButton materialButtonRegister;
    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;
    ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compile_profile);
        mtextInputUsername = findViewById(R.id.textInputUsernameConfirm);
        mtextInputPhone = findViewById(R.id.textInputPhone);
        materialButtonRegister = findViewById(R.id.btnConfirm);
        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();
        mDialog = new ProgressDialog(this);
        mDialog.setTitle("Registrando...");
        mDialog.setMessage("Por favor, espere un momento");
        mDialog.setCancelable(false);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        validarCampos();
        materialButtonRegister.setOnClickListener(v -> {
            String username = Objects.requireNonNull(mtextInputUsername.getText()).toString().trim();
            String phone = Objects.requireNonNull(mtextInputPhone.getText()).toString().trim();
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
                Intent intent = new Intent(CompleteProfileActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(CompleteProfileActivity.this, "Error al registrar el usuario en la base de datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validarCampos() {
        mtextInputUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Objects.requireNonNull(mtextInputUsername.getText()).toString().isEmpty()) {
                    mtextInputUsername.setError("El nombre de usuario es obligatorio");
                } else {
                    mtextInputUsername.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mtextInputPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Objects.requireNonNull(mtextInputPhone.getText()).toString().isEmpty()) {
                    mtextInputPhone.setError("El teléfono es obligatorio");
                } else {
                    mtextInputPhone.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}