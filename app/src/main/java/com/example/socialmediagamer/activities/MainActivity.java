package com.example.socialmediagamer.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.socialmediagamer.R;
import com.example.socialmediagamer.models.User;
import com.example.socialmediagamer.providers.AuthProvider;
import com.example.socialmediagamer.providers.UsersProvider;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import static com.example.socialmediagamer.utils.ValidateEmail.isEmailValid;

public class MainActivity extends AppCompatActivity {
    MaterialTextView materialTextView_register, materialTextViewForgotPassword;
    TextInputEditText mtextInputEmail, mtextInputPassword;
    MaterialButton materialButtonLogin;
    AuthProvider mAuthProvider;
    SignInButton mButtonGoogle;
    private GoogleSignInClient mGoogleSignInClient;
    UsersProvider mUsersProvider;
    ProgressDialog progressDialog, progressDialogResetPassword;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        materialTextViewForgotPassword = findViewById(R.id.forgotPassword);
        materialTextView_register = findViewById(R.id.textViewRegister);
        mtextInputEmail = findViewById(R.id.textInputEmail);
        mtextInputPassword = findViewById(R.id.textInputPassword);
        materialButtonLogin = findViewById(R.id.btnLogin);
        mButtonGoogle = findViewById(R.id.btnLoginGoogle);
        mAuthProvider = new AuthProvider();
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Iniciando sesión...");
        progressDialog.setMessage("Por favor, espere un momento");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialogResetPassword = new ProgressDialog(this);
        progressDialogResetPassword.setTitle("Enviando correo para restablecer la contraseña...");
        progressDialogResetPassword.setMessage("Por favor, espere un momento");
        progressDialogResetPassword.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id_strings)).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mUsersProvider = new UsersProvider();
        validarCampos();
        materialButtonLogin.setOnClickListener(v -> {
            String email = Objects.requireNonNull(mtextInputEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(mtextInputPassword.getText()).toString().trim();
            if (!email.isEmpty() && !password.isEmpty()) {
                if (isEmailValid(email)) {
                    progressDialog.show();
                    mAuthProvider.login(email, password).addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Snackbar.make(v, "El correo electrónico y/o contraseña son incorrectos", Snackbar.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Snackbar.make(v, "Formato de correo electrónico inválido", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(v, "Complete los campos", Snackbar.LENGTH_SHORT).show();
            }
        });
        mButtonGoogle.setOnClickListener(v -> resultLauncher.launch(new Intent(mGoogleSignInClient.getSignInIntent())));
        materialTextView_register.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RegisterActivity.class)));
        materialTextViewForgotPassword.setOnClickListener(v -> showDialogForgotPassword());
        if (!isOnline(this)) {
            Toast.makeText(getApplicationContext(), "Error de red, verifique su conexión", Toast.LENGTH_LONG).show();
            finishAffinity();
        }
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }

    private void showDialogForgotPassword() {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle("Restablecer contraseña");
        alert.setIcon(R.drawable.ic_settings_backup_restore);
        alert.setCancelable(false);
        TextInputEditText textInputEditText = new TextInputEditText(MainActivity.this);
        textInputEditText.setHint("Tu correo electrónico...");
        textInputEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        textInputEditText.setMaxLines(3);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(50, 0, 50, 50);
        textInputEditText.setLayoutParams(params);
        RelativeLayout container = new RelativeLayout(MainActivity.this);
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        container.setLayoutParams(relativeParams);
        container.addView(textInputEditText);
        alert.setView(container);
        alert.setPositiveButton("Enviar correo", (dialog, which) -> {
            String email = Objects.requireNonNull(textInputEditText.getText()).toString().trim();
            if (!email.isEmpty()) {
                if (isEmailValid(email)) {
                    progressDialogResetPassword.setCanceledOnTouchOutside(false);
                    progressDialogResetPassword.show();
                    mAuth.setLanguageCode("es");
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Se ha enviado un correo para restablecer la contraseña a: " + email, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "El correo electrónico ingresado no existe", Toast.LENGTH_SHORT).show();
                        }
                        progressDialogResetPassword.dismiss();
                    });
                } else {
                    Toast.makeText(this, "Formato de correo electrónico inválido", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "El correo electrónico es obligatorio", Toast.LENGTH_SHORT).show();
            }
        });
        alert.setNegativeButton("Cancelar", (dialog, which) -> {
        });
        alert.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuthProvider.getUserSession() != null) {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent intent = result.getData();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException ignored) {
            }
        }
    });

    private void firebaseAuthWithGoogle(String idToken) {
        progressDialog.show();
        mAuthProvider.googleLogin(idToken).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                String id = mAuthProvider.getUid();
                checkUserExist(id);
            } else {
                progressDialog.dismiss();
                Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserExist(final String id) {
        mUsersProvider.getUser(id).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                progressDialog.dismiss();
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                String email = mAuthProvider.getEmail();
                User user = new User();
                user.setId(id);
                user.setEmail(email);
                mUsersProvider.create(user).addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        startActivity(new Intent(MainActivity.this, CompleteProfileActivity.class));
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Error al guardar la información en la base de datos", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void validarCampos() {
        mtextInputEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Objects.requireNonNull(mtextInputEmail.getText()).toString().isEmpty()) {
                    mtextInputEmail.setError("El correo electrónico es obligatorio");
                } else {
                    mtextInputEmail.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mtextInputPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Objects.requireNonNull(mtextInputPassword.getText()).toString().isEmpty()) {
                    mtextInputPassword.setError("La contraseña es obligatoria", null);
                } else {
                    mtextInputPassword.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}