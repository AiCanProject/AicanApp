package com.aican.aicanapp.Authentication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.FirebaseAccounts.PrimaryAccount;
import com.aican.aicanapp.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    public static final String MY_USER_ID = "mUid";
    private static final String TAG = "SignUpActivity";
    private static String accountKey;
    ProgressDialog dialog;
    DatabaseReference database;
    EditText etEmail;
    EditText etPassword;
    EditText etName;
    ImageView ivBackBtn;
    TextInputLayout tilName, tilEmail, tilPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        ImageView ivBackground = findViewById(R.id.ivBackground);
        TextView tvTitle = findViewById(R.id.tvTitle);
        ivBackBtn = findViewById(R.id.ivBackBtn);
        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etName = findViewById(R.id.etName);

        database = FirebaseDatabase.getInstance(PrimaryAccount.getInstance(this)).getReference();

        findViewById(R.id.btnSignUp).setOnClickListener(v -> {
            dialog = new ProgressDialog(this);
            dialog.setCancelable(false);
            dialog.setMessage("Please wait while we set up your account");
            dialog.show();
            FirebaseAuth.getInstance(PrimaryAccount.getInstance(this))
                    .createUserWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString())
                    .addOnSuccessListener(authResult -> {
//                        lookForAvailableAccounts(authResult.getUser().getUid());
                        String uid = authResult.getUser().getUid();
                        saveName(uid);
                        saveUid(uid);
                        startMainActivity();
                    })
                    .addOnFailureListener(exception -> {
                        if (exception instanceof FirebaseAuthInvalidUserException) {
                            tilEmail.setError("This Email cannot be used to create account");
                        } else if (exception instanceof FirebaseAuthUserCollisionException) {
                            tilEmail.setError("Email already exists");
                        } else if (exception instanceof FirebaseAuthWeakPasswordException) {
                            tilPassword.setError("Weak Password");
                        } else if (exception instanceof FirebaseAuthEmailException) {
                            tilEmail.setError("Incorrect Email");
                        } else {
                            Toast.makeText(this, "Unknown error", Toast.LENGTH_SHORT).show();
                        }
                        exception.printStackTrace();
                        dialog.dismiss();
                    });

        });

        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            onBackPressed();
        });

        ivBackBtn.setOnClickListener(v -> {
            onBackPressed();
        });

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilEmail.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilPassword.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void saveName(String uid) {
        HashMap<String, String> map = new HashMap<>();
        map.put("NAME", etName.getText().toString());
        FirebaseFirestore.getInstance(PrimaryAccount.getInstance(this))
                .collection("NAMES").document(uid)
                .set(map);
    }

    private void startMainActivity() {
        if (dialog != null) {
            dialog.dismiss();
        }
        startActivity(new Intent(this, Dashboard.class));
    }

    private void saveUid(String uid) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(MY_USER_ID, uid).apply();
    }
}