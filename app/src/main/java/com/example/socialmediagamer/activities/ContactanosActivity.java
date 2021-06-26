package com.example.socialmediagamer.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.socialmediagamer.R;
import com.example.socialmediagamer.providers.AuthProvider;
import com.example.socialmediagamer.providers.UsersProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactanosActivity extends AppCompatActivity {
    CircleImageView mcircleImageViewBack;
    TextInputEditText textInputUsernameForm, textInputEmailForm, textInputMessageForm;
    RadioGroup radioGroup;
    MaterialRadioButton materialRadioButton, materialRadioButton_queja, materialRadioButton_sugerencia;
    final String correoProyecto = "proyectosmariorecio@gmail.com", contrasenaProyecto = "MR1704002053CV";
    String nombre, correo, mensaje, mUsername = "", mEmail = "";
    static String asunto;
    Session session;
    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactanos);
        mcircleImageViewBack = findViewById(R.id.circleImageBack);
        textInputUsernameForm = findViewById(R.id.textInputUsernameForm);
        textInputEmailForm = findViewById(R.id.textInputEmailForm);
        textInputMessageForm = findViewById(R.id.textInputMessageForm);
        materialRadioButton_queja = findViewById(R.id.radioButtonComplain);
        materialRadioButton_sugerencia = findViewById(R.id.radioButtonSuggestion);
        radioGroup = findViewById(R.id.radioGroup);
        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();
        validarCampo();
        MaterialButton materialButton_enviar = findViewById(R.id.btnSendForm);
        materialButton_enviar.setOnClickListener(view -> {
            int radioID = radioGroup.getCheckedRadioButtonId();
            materialRadioButton = findViewById(radioID);
            nombre = Objects.requireNonNull(textInputUsernameForm.getText()).toString().trim();
            correo = Objects.requireNonNull(textInputEmailForm.getText()).toString().trim();
            mensaje = Objects.requireNonNull(textInputMessageForm.getText()).toString().trim();
            if (!evaluarCampos(nombre) && !validarEmail(correo) && !evaluarCampos(mensaje) && (!materialRadioButton_queja.isChecked() || !materialRadioButton_sugerencia.isChecked())) {
                Snackbar.make(view, "Complete los campos", Snackbar.LENGTH_SHORT).show();
            } else {
                if (evaluarCampos(nombre)) {
                    if (validarEmail(correo)) {
                        if (evaluarCampos(mensaje)) {
                            if (materialRadioButton_queja.isChecked() || materialRadioButton_sugerencia.isChecked()) {
                                asunto = materialRadioButton.getText().toString().trim();
                                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                StrictMode.setThreadPolicy(policy);
                                Properties properties = new Properties();
                                properties.put("mail.smtp.host", "smtp.gmail.com");
                                properties.put("mail.smtp.socketFactory.port", "465");
                                properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                                properties.put("mail.smtp.auth", "true");
                                properties.put("mail.smtp.port", "465");
                                session = Session.getDefaultInstance(properties, new Authenticator() {
                                    @Override
                                    protected PasswordAuthentication getPasswordAuthentication() {
                                        return new PasswordAuthentication(correoProyecto, contrasenaProyecto);
                                    }
                                });
                                try {
                                    Message message = new MimeMessage(session);
                                    message.setFrom(new InternetAddress(correo, nombre + " (" + correo + ")"));
                                    message.setSubject(asunto + "s de Reciobook");
                                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(correoProyecto));
                                    message.setContent(mensaje, "text/html; charset=utf-8");
                                    new SendMail().execute(message);
                                } catch (Exception e) {
                                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Snackbar.make(view, "Seleccione Queja o Sugerencia", Snackbar.LENGTH_SHORT).show();
                            }
                        } else {
                            textInputMessageForm.setError("El mensaje es obligatorio");
                        }
                    } else {
                        textInputEmailForm.setError("El correo electrónico es obligatorio");
                    }
                } else {
                    textInputUsernameForm.setError("El nombre de usuario es obligatorio");
                }
            }
        });
        mcircleImageViewBack.setOnClickListener(v -> finish());
        getUser();
    }

    private void getUser() {
        mUsersProvider.getUser(mAuthProvider.getUid()).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                if (documentSnapshot.contains("username")) {
                    mUsername = documentSnapshot.getString("username");
                    textInputUsernameForm.setText(mUsername);
                }
                if (documentSnapshot.contains("email")) {
                    mEmail = documentSnapshot.getString("email");
                    textInputEmailForm.setText(mEmail);
                }
            }
        });
    }

    public boolean validarEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    public boolean evaluarCampos(String valor) {
        return !valor.equals("");
    }

    private void validarCampo() {
        textInputMessageForm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (Objects.requireNonNull(textInputMessageForm.getText()).toString().isEmpty()) {
                    textInputMessageForm.setError("El mensaje es obligatorio");
                } else {
                    textInputMessageForm.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    public class SendMail extends AsyncTask<Message, String, String> {
        public ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(ContactanosActivity.this, "Enviando correo electrónico...", "Por favor, espere un momento", true, false);
        }

        @Override
        protected String doInBackground(Message... messages) {
            try {
                Transport.send(messages[0]);
                return "Éxito";
            } catch (MessagingException e) {
                e.printStackTrace();
                return "Error";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            if (s.equals("Éxito")) {
                if (ContactanosActivity.asunto.equals("Queja")) {
                    Toast.makeText(ContactanosActivity.this, "Gracias por su queja", Toast.LENGTH_SHORT).show();
                } else if (ContactanosActivity.asunto.equals("Sugerencia")) {
                    Toast.makeText(ContactanosActivity.this, "Gracias por su sugerencia", Toast.LENGTH_SHORT).show();
                }
                Intent intent = new Intent(ContactanosActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(ContactanosActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
    }
}