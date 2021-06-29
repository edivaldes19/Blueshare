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

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.socialmediagamer.utils.ValidateEmail.isEmailValid;

public class RegisterActivity extends AppCompatActivity {
    CircleImageView mCircleImageViewBack;
    TextInputEditText mTextInputUsername, mTextInputEmailR, mTextInputPhone, mTextInputPasswordR, mTextInputConfirmPasswordR;
    MaterialButton materialButtonRegister;
    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;
    ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mCircleImageViewBack = findViewById(R.id.circleImageBack);
        mTextInputUsername = findViewById(R.id.textInputUsernameR);
        mTextInputEmailR = findViewById(R.id.textInputEmailR);
        mTextInputPhone = findViewById(R.id.textInputPhoneR);
        mTextInputPasswordR = findViewById(R.id.textInputPasswordR);
        mTextInputConfirmPasswordR = findViewById(R.id.textInputConfirmPasswordR);
        materialButtonRegister = findViewById(R.id.btnRegister);
        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();
        mDialog = new ProgressDialog(this);
        mDialog.setTitle("Registrando...");
        mDialog.setMessage("Por favor, espere un momento");
        mDialog.setCancelable(false);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        validarCampos();
        materialButtonRegister.setOnClickListener(v -> {
            String username = Objects.requireNonNull(mTextInputUsername.getText()).toString().trim();
            String email = Objects.requireNonNull(mTextInputEmailR.getText()).toString().trim();
            String phone = Objects.requireNonNull(mTextInputPhone.getText()).toString().trim();
            String password = Objects.requireNonNull(mTextInputPasswordR.getText()).toString().trim();
            String confirmPassword = Objects.requireNonNull(mTextInputConfirmPasswordR.getText()).toString().trim();
            if (!username.isEmpty() && !email.isEmpty() && !phone.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty()) {
                if (isEmailValid(email)) {
                    if (password.equals(confirmPassword)) {
                        if (password.length() >= 6) {
                            createUser(username, email, phone, password);
                        } else {
                            Snackbar.make(v, "La contraseña debe ser mayor o igual a 6 caracteres", Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        Snackbar.make(v, "Las contraseñas no coinciden", Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(v, "Formato de correo electrónico inválido", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(v, "Complete los campos", Snackbar.LENGTH_SHORT).show();
            }
        });
        mCircleImageViewBack.setOnClickListener(v -> finish());
    }

    private void createUser(final String username, final String email, final String phone, final String password) {
        mDialog.show();
        mAuthProvider.register(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String id = mAuthProvider.getUid();
                User user = new User();
                user.setId(id);
                user.setEmail(email);
                user.setUsername(username);
                user.setPhone(phone);
                user.setTimestamp(new Date().getTime());
                mUsersProvider.create(user).addOnCompleteListener(task1 -> {
                    mDialog.dismiss();
                    if (task1.isSuccessful()) {
                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Error al registrar el usuario en la base de datos", Toast.LENGTH_SHORT).show();
                    }
                });
                Toast.makeText(RegisterActivity.this, "Bienvenido " + username, Toast.LENGTH_SHORT).show();
            } else {
                mDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "Error al registrar el usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validarCampos() {
        mTextInputUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Objects.requireNonNull(mTextInputUsername.getText()).toString().isEmpty()) {
                    mTextInputUsername.setError("El nombre de usuario es obligatorio");
                } else {
                    mTextInputUsername.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mTextInputEmailR.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Objects.requireNonNull(mTextInputEmailR.getText()).toString().isEmpty()) {
                    mTextInputEmailR.setError("El correo electrónico es obligatorio");
                } else {
                    mTextInputEmailR.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mTextInputPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Objects.requireNonNull(mTextInputPhone.getText()).toString().isEmpty()) {
                    mTextInputPhone.setError("El teléfono es obligatorio");
                } else {
                    mTextInputPhone.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mTextInputPasswordR.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Objects.requireNonNull(mTextInputPasswordR.getText()).toString().isEmpty()) {
                    mTextInputPasswordR.setError("La contraseña es obligatoria", null);
                } else {
                    mTextInputPasswordR.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mTextInputConfirmPasswordR.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Objects.requireNonNull(mTextInputConfirmPasswordR.getText()).toString().isEmpty()) {
                    mTextInputConfirmPasswordR.setError("Debe confirmar su contraseña", null);
                } else {
                    mTextInputConfirmPasswordR.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}