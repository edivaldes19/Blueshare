package com.example.socialmediagamer.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

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

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.socialmediagamer.utils.Validations.isEmailValid;
import static com.example.socialmediagamer.utils.Validations.validateFieldsAsYouType;
import static com.example.socialmediagamer.utils.Validations.validatePasswordFieldsAsYouType;

public class RegisterActivity extends AppCompatActivity {
    CoordinatorLayout coordinatorLayout;
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
        coordinatorLayout = findViewById(R.id.coordinatorRegister);
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
        validateFieldsAsYouType(mTextInputUsername, "El nombre de usuario es obligatorio");
        validateFieldsAsYouType(mTextInputEmailR, "El correo electrónico es obligatorio");
        validateFieldsAsYouType(mTextInputPhone, "El número de teléfono es obligatorio");
        validatePasswordFieldsAsYouType(mTextInputPasswordR, "La contraseña es obligatoria");
        validatePasswordFieldsAsYouType(mTextInputConfirmPasswordR, "Debe confirmar su contraseña");
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
                        Snackbar.make(coordinatorLayout, "Error al registrar el usuario en la base de datos", Snackbar.LENGTH_SHORT).show();
                    }
                });
                Toast.makeText(RegisterActivity.this, "Bienvenido " + username, Toast.LENGTH_LONG).show();
            } else {
                mDialog.dismiss();
                Snackbar.make(coordinatorLayout, "Error al registrar el usuario", Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}