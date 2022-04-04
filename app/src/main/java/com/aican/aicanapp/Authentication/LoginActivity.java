package com.aican.aicanapp.Authentication;


import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.FirebaseAccounts.PrimaryAccount;
import com.aican.aicanapp.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



public class LoginActivity extends AppCompatActivity {

    DatabaseReference database;
    ProgressDialog dialog;
    Button btn;
    TextInputLayout tilEmail,tilPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        database = FirebaseDatabase.getInstance(PrimaryAccount.getInstance(this)).getReference();

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        ImageView ivBackground = findViewById(R.id.ivBackground);
        TextView tvTitle = findViewById(R.id.tvTitle);
        LinearLayout llBackground = findViewById(R.id.backgroundll);

        btn = findViewById(R.id.wifi);

        View view = findViewById(R.id.view);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);

        btn.setOnClickListener(V -> {
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        });

        findViewById(R.id.btnSignUp).setOnClickListener(v -> {
            startActivity(
                    new Intent(this, SignUpActivity.class),
                    ActivityOptions.makeSceneTransitionAnimation(
                            this,
                            new Pair<>(ivBackground, getString(R.string.background_transition)),
                            new Pair<>(tvTitle, getString(R.string.title_transition))
                    ).toBundle()
            );
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            dialog = new ProgressDialog(this);
            dialog.setCancelable(false);
            dialog.setMessage("Please wait while we log you in");
            dialog.show();
            FirebaseAuth.getInstance(PrimaryAccount.getInstance(this)).signInWithEmailAndPassword(
                    etEmail.getText().toString(),
                    etPassword.getText().toString()
            ).addOnSuccessListener(authResult -> {
//              getUserFirebaseAccount(authResult.getUser().getUid());
                startMainActivity();

                SharedPreferences sharedPreferences = getSharedPreferences("loginprefs", MODE_PRIVATE);

                SharedPreferences.Editor myEdit = sharedPreferences.edit();

                myEdit.putString("email", etEmail.getText().toString());
                myEdit.commit();

            }).addOnFailureListener(exception -> {
                if (exception instanceof FirebaseAuthInvalidUserException) {
                    tilEmail.setError("This Email ID is not registered");
                } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                    tilPassword.setError("Incorrect Password");
                } else {
                    Toast.makeText(this, "Unknown Error", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
                exception.printStackTrace();
            });
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


    private void startMainActivity() {
        if (dialog != null) {
            dialog.dismiss();
        }
        startActivity(new Intent(this, Dashboard.class));
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    //    private void getUserFirebaseAccount(String uid) {
//        database.child("Users").child(uid).get().addOnSuccessListener(dataSnapshot -> {
//            setupAccount(dataSnapshot.getValue(String.class));
//        });
//    }
//
//    private void setupAccount(String accountId) {
//        database.child("Accounts").child(accountId).get().addOnSuccessListener(dataSnapshot -> {
//            UserAccount.DATABASE_URL = dataSnapshot.child("database").getValue(String.class);
//            UserAccount.API_KEY = dataSnapshot.child("api").getValue(String.class);
//            UserAccount.APP_ID = dataSnapshot.child("app").getValue(String.class);
//            UserAccount.PROJECT_ID = dataSnapshot.child("project").getValue(String.class);
//
//            dialog.dismiss();
//            startActivity(new Intent(this, MainActivity.class));
//        });
//    }
}