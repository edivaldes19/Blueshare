package com.example.socialmediagamer.utils;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.socialmediagamer.activities.ContactanosActivity;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;

public class SendMail extends AsyncTask<Message, String, String> {
    private ProgressDialog progressDialog;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = ProgressDialog.show(progressDialog.getContext(), "Enviando correo electrónico...", "Por favor, espere un momento", true, false);
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
                Toast.makeText(progressDialog.getContext(), "Gracias por su queja", Toast.LENGTH_LONG).show();
            } else if (ContactanosActivity.affair.equals("Sugerencia")) {
                Toast.makeText(progressDialog.getContext(), "Gracias por su sugerencia", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(progressDialog.getContext(), "Error", Toast.LENGTH_SHORT).show();
        }
    }
}
