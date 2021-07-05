package com.example.socialmediagamer.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.example.socialmediagamer.R;
import com.example.socialmediagamer.providers.AuthProvider;
import com.example.socialmediagamer.providers.UsersProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.socialmediagamer.utils.Validations.isEmailValid;
import static com.example.socialmediagamer.utils.Validations.validateFieldsAsYouType;

public class ContactanosActivity extends AppCompatActivity {
    public static String affair;
    CoordinatorLayout coordinatorLayout;
    CircleImageView mCircleImageViewBack;
    TextInputEditText textInputUsernameForm, textInputEmailForm, textInputMessageForm;
    RadioGroup radioGroup;
    MaterialRadioButton materialRadioButton, materialRadioButtonComplain, materialRadioButtonSuggestion;
    final String emailProject = "proyectosmariorecio@gmail.com", passwordProject = "MR1704002053CV";
    String name, email, message;
    Session session;
    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactanos);
        coordinatorLayout = findViewById(R.id.coordinatorContactanos);
        mCircleImageViewBack = findViewById(R.id.circleImageBack);
        textInputUsernameForm = findViewById(R.id.textInputUsernameForm);
        textInputEmailForm = findViewById(R.id.textInputEmailForm);
        textInputMessageForm = findViewById(R.id.textInputMessageForm);
        materialRadioButtonComplain = findViewById(R.id.radioButtonComplain);
        materialRadioButtonSuggestion = findViewById(R.id.radioButtonSuggestion);
        radioGroup = findViewById(R.id.radioGroup);
        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();
        validateFieldsAsYouType(textInputMessageForm, "El mensaje es obligatorio");
        MaterialButton materialButton_enviar = findViewById(R.id.btnSendForm);
        materialButton_enviar.setOnClickListener(view -> {
            int radioID = radioGroup.getCheckedRadioButtonId();
            materialRadioButton = findViewById(radioID);
            name = Objects.requireNonNull(textInputUsernameForm.getText()).toString().trim();
            email = Objects.requireNonNull(textInputEmailForm.getText()).toString().trim();
            message = Objects.requireNonNull(textInputMessageForm.getText()).toString().trim();
            if (name.isEmpty() && !isEmailValid(email) && message.isEmpty() && (!materialRadioButtonComplain.isChecked() || !materialRadioButtonSuggestion.isChecked())) {
                Snackbar.make(view, "Complete los campos", Snackbar.LENGTH_SHORT).show();
            } else {
                if (!name.isEmpty()) {
                    if (isEmailValid(email)) {
                        if (!message.isEmpty()) {
                            if (materialRadioButtonComplain.isChecked() || materialRadioButtonSuggestion.isChecked()) {
                                affair = materialRadioButton.getText().toString().trim();
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
                                        return new PasswordAuthentication(emailProject, passwordProject);
                                    }
                                });
                                try {
                                    Message message = new MimeMessage(session);
                                    message.setFrom(new InternetAddress(email, name + " (" + email + ")"));
                                    message.setSubject(affair + " de Blueshare");
                                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailProject));
                                    message.setContent(this.message, "text/html; charset=utf-8");
                                    new SendMail().execute(message);
                                } catch (Exception e) {
                                    Snackbar.make(view, "Error al enviar correo electrónico", Snackbar.LENGTH_SHORT).show();
                                }
                            } else {
                                Snackbar.make(view, "Debe seleccionar queja o sugerencia", Snackbar.LENGTH_SHORT).show();
                            }
                        } else {
                            Snackbar.make(view, "El mensaje es obligatorio", Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        Snackbar.make(view, "Formato de correo electrónico inválido", Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(view, "El nombre de usuario es obligatorio", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        mCircleImageViewBack.setOnClickListener(v -> returnAfterSendingMail());
        getUser();
    }

    private void getUser() {
        mUsersProvider.getUser(mAuthProvider.getUid()).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                if (documentSnapshot.contains("username")) {
                    String username = documentSnapshot.getString("username");
                    textInputUsernameForm.setText(username);
                }
                if (documentSnapshot.contains("email")) {
                    String email = documentSnapshot.getString("email");
                    textInputEmailForm.setText(email);
                }
            }
        });
    }

    private void returnAfterSendingMail() {
        startActivity(new Intent(ContactanosActivity.this, HomeActivity.class));
        finish();
    }

    @SuppressLint("StaticFieldLeak")
    public class SendMail extends AsyncTask<Message, String, String> {
        private ProgressDialog progressDialog;

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
                if (ContactanosActivity.affair.equals("Queja")) {
                    Toast.makeText(ContactanosActivity.this, "Gracias por su queja", Toast.LENGTH_LONG).show();
                } else if (ContactanosActivity.affair.equals("Sugerencia")) {
                    Toast.makeText(ContactanosActivity.this, "Gracias por su sugerencia", Toast.LENGTH_LONG).show();
                }
                returnAfterSendingMail();
            } else {
                Snackbar.make(coordinatorLayout, "Error", Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}
