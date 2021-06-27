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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {
    CircleImageView mcircleImageViewBack;
    TextInputEditText mtextInputUsername, mtextInputEmailR, mtextInputPhone, mtextInputPasswordR, mtextInputConfirmPasswordR;
    MaterialButton materialButtonRegister;
    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;
    ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mcircleImageViewBack = findViewById(R.id.circleImageBack);
        mtextInputUsername = findViewById(R.id.textInputUsernameR);
        mtextInputEmailR = findViewById(R.id.textInputEmailR);
        mtextInputPhone = findViewById(R.id.textInputPhoneR);
        mtextInputPasswordR = findViewById(R.id.textInputPasswordR);
        mtextInputConfirmPasswordR = findViewById(R.id.textInputConfirmPasswordR);
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
            String username = Objects.requireNonNull(mtextInputUsername.getText()).toString().trim();
            String email = Objects.requireNonNull(mtextInputEmailR.getText()).toString().trim();
            String phone = Objects.requireNonNull(mtextInputPhone.getText()).toString().trim();
            String password = Objects.requireNonNull(mtextInputPasswordR.getText()).toString().trim();
            String confirmPassword = Objects.requireNonNull(mtextInputConfirmPasswordR.getText()).toString().trim();
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
        mcircleImageViewBack.setOnClickListener(v -> finish());
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

    private boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
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
        mtextInputEmailR.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Objects.requireNonNull(mtextInputEmailR.getText()).toString().isEmpty()) {
                    mtextInputEmailR.setError("El correo electrónico es obligatorio");
                } else {
                    mtextInputEmailR.setError(null);
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
        mtextInputPasswordR.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Objects.requireNonNull(mtextInputPasswordR.getText()).toString().isEmpty()) {
                    mtextInputPasswordR.setError("La contraseña es obligatoria", null);
                } else {
                    mtextInputPasswordR.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mtextInputConfirmPasswordR.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Objects.requireNonNull(mtextInputConfirmPasswordR.getText()).toString().isEmpty()) {
                    mtextInputConfirmPasswordR.setError("Debe confirmar su contraseña", null);
                } else {
                    mtextInputConfirmPasswordR.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}