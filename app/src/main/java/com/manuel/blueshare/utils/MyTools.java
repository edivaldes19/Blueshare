package com.manuel.blueshare.utils;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.manuel.blueshare.providers.UsersProvider;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyTools {
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static void validateFieldsAsYouType(TextInputEditText textInputEditText, String error) {
        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (Objects.requireNonNull(textInputEditText.getText()).toString().isEmpty()) {
                    textInputEditText.setError(error);
                } else {
                    textInputEditText.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    public static void validatePasswordFieldsAsYouType(TextInputEditText textInputEditText, String error) {
        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (Objects.requireNonNull(textInputEditText.getText()).toString().isEmpty()) {
                    textInputEditText.setError(error, null);
                } else {
                    textInputEditText.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    public static void compareDataString(ArrayList<String> strings, String text, CoordinatorLayout coordinatorLayout, String error) {
        if (strings != null && !strings.isEmpty()) {
            for (String s : strings) {
                if (s.equals(text)) {
                    Snackbar.make(coordinatorLayout, error, Snackbar.LENGTH_SHORT).show();
                    return;
                }
            }
        }
    }

    public static void deleteCurrentInformation(ArrayList<String> strings, String text) {
        if (strings != null && !strings.isEmpty()) {
            for (int i = 0; i < strings.size(); i++) {
                if (strings.get(i).equals(text)) {
                    strings.remove(i);
                    break;
                }
            }
        }
    }

    public static void isUserInfoExist(UsersProvider usersProvider, ArrayList<String> stringList, String field, CoordinatorLayout coordinatorLayout) {
        usersProvider.getAllUserDocuments().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot snapshot : Objects.requireNonNull(task.getResult())) {
                    if (snapshot.exists()) {
                        if (snapshot.contains(field)) {
                            String allFields = snapshot.getString(field);
                            stringList.add(allFields);
                        }
                    }
                }
            } else {
                Snackbar.make(coordinatorLayout, "Error al obtener la información de los usuarios", Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}