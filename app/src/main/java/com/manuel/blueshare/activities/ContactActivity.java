package com.manuel.blueshare.activities;

import static com.manuel.blueshare.utils.MyTools.isEmailValid;
import static com.manuel.blueshare.utils.MyTools.validateFieldsAsYouType;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.manuel.blueshare.R;
import com.manuel.blueshare.providers.AuthProvider;
import com.manuel.blueshare.providers.UsersProvider;

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

public class ContactActivity extends AppCompatActivity {
    public static String mReasonStatic;
    CoordinatorLayout coordinatorLayout;
    CircleImageView mCircleImageViewBack;
    TextInputEditText textInputUsernameForm, textInputEmailForm, textInputMessageForm;
    MaterialTextView mTextViewReasonSelected;
    RadioGroup mRadioGroup;
    MaterialRadioButton materialRadioButton, materialRadioButtonComplain, materialRadioButtonSuggestion;
    final String mEmailProject = "appsmanuel1219@gmail.com", mPasswordProject = "e12171922M/";
    Session mSession;
    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;

    @SuppressLint({"RestrictedApi", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        coordinatorLayout = findViewById(R.id.coordinatorContactanos);
        mCircleImageViewBack = findViewById(R.id.circleImageBack);
        textInputUsernameForm = findViewById(R.id.textInputUsernameForm);
        textInputEmailForm = findViewById(R.id.textInputEmailForm);
        textInputMessageForm = findViewById(R.id.textInputMessageForm);
        materialRadioButtonComplain = findViewById(R.id.radioButtonComplain);
        materialRadioButtonSuggestion = findViewById(R.id.radioButtonSuggestion);
        mTextViewReasonSelected = findViewById(R.id.textViewReasonSelected);
        mRadioGroup = findViewById(R.id.radioGroup);
        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();
        validateFieldsAsYouType(textInputMessageForm, "El mensaje es obligatorio");
        getUser();
        materialRadioButtonComplain.setOnClickListener(v -> mTextViewReasonSelected.setText("Motivo: " + materialRadioButtonComplain.getText().toString().trim()));
        materialRadioButtonSuggestion.setOnClickListener(v -> mTextViewReasonSelected.setText("Motivo: " + materialRadioButtonSuggestion.getText().toString().trim()));
        MaterialButton buttonSend = findViewById(R.id.btnSendForm);
        buttonSend.setOnClickListener(view -> sendForm());
        mCircleImageViewBack.setOnClickListener(v -> returnAfterSendingMail());
    }

    private void sendForm() {
        int radioID = mRadioGroup.getCheckedRadioButtonId();
        materialRadioButton = findViewById(radioID);
        String name = Objects.requireNonNull(textInputUsernameForm.getText()).toString().trim();
        String email = Objects.requireNonNull(textInputEmailForm.getText()).toString().trim();
        String messageInput = Objects.requireNonNull(textInputMessageForm.getText()).toString().trim();
        if (TextUtils.isEmpty(name) && !isEmailValid(email) && TextUtils.isEmpty(messageInput) && (!materialRadioButtonComplain.isChecked() || !materialRadioButtonSuggestion.isChecked())) {
            Snackbar.make(coordinatorLayout, "Complete los campos", Snackbar.LENGTH_SHORT).show();
        } else {
            if (!TextUtils.isEmpty(name)) {
                if (isEmailValid(email)) {
                    if (!TextUtils.isEmpty(messageInput)) {
                        if (materialRadioButtonComplain.isChecked() || materialRadioButtonSuggestion.isChecked()) {
                            mReasonStatic = materialRadioButton.getText().toString().trim();
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                            StrictMode.setThreadPolicy(policy);
                            Properties properties = new Properties();
                            properties.put("mail.smtp.host", "smtp.gmail.com");
                            properties.put("mail.smtp.socketFactory.port", "465");
                            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                            properties.put("mail.smtp.auth", "true");
                            properties.put("mail.smtp.port", "465");
                            mSession = Session.getDefaultInstance(properties, new Authenticator() {
                                @Override
                                protected PasswordAuthentication getPasswordAuthentication() {
                                    return new PasswordAuthentication(mEmailProject, mPasswordProject);
                                }
                            });
                            try {
                                Message message = new MimeMessage(mSession);
                                message.setFrom(new InternetAddress(email, name + " (" + email + ")"));
                                message.setSubject(mReasonStatic + " de Blueshare");
                                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mEmailProject));
                                message.setContent(messageInput, "text/html; charset=utf-8");
                                new SendMail().execute(message);
                            } catch (Exception e) {
                                Snackbar.make(coordinatorLayout, "Error al enviar correo electrónico", Snackbar.LENGTH_SHORT).show();
                            }
                        } else {
                            Snackbar.make(coordinatorLayout, "Debe seleccionar queja o sugerencia", Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        Snackbar.make(coordinatorLayout, "El mensaje es obligatorio", Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(coordinatorLayout, "Formato de correo electrónico inválido", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(coordinatorLayout, "El nombre de usuario es obligatorio", Snackbar.LENGTH_SHORT).show();
            }
        }
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
        startActivity(new Intent(ContactActivity.this, HomeActivity.class));
        finish();
    }

    @SuppressLint("StaticFieldLeak")
    public class SendMail extends AsyncTask<Message, String, String> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(ContactActivity.this, "Enviando correo electrónico...", "Por favor, espere un momento", true, false);
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
                if (ContactActivity.mReasonStatic.equals("Queja")) {
                    Toast.makeText(ContactActivity.this, "Gracias por su queja", Toast.LENGTH_LONG).show();
                } else if (ContactActivity.mReasonStatic.equals("Sugerencia")) {
                    Toast.makeText(ContactActivity.this, "Gracias por su sugerencia", Toast.LENGTH_LONG).show();
                }
                returnAfterSendingMail();
            } else {
                Snackbar.make(coordinatorLayout, "Error", Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}