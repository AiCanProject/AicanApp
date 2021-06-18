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

public class SignUpActivity extends AppCompatActivity {

    public static final String MY_USER_ID = "mUid";
    private static final String TAG = "SignUpActivity";
    private static String accountKey;
    ProgressDialog dialog;
    DatabaseReference database;
    EditText etEmail;
    EditText etPassword;
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

                        saveUid(authResult.getUser().getUid());
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

    private void startMainActivity() {
        if (dialog != null) {
            dialog.dismiss();
        }
        startActivity(new Intent(this, Dashboard.class));
    }

    private void saveUid(String uid) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(MY_USER_ID, uid).apply();
    }


//    private void lookForAvailableAccounts(String uid) {
//        database.child("Available").get().addOnSuccessListener(dataSnapshot -> {
//            if(dataSnapshot.getChildrenCount()==0){
//                Toast.makeText(this, "Sorry, no accounts available", Toast.LENGTH_SHORT).show();
//                dialog.dismiss();
//            }else {
//                DataSnapshot availableAccount = dataSnapshot.getChildren().iterator().next();
//                String accountKey = Objects.requireNonNull(availableAccount.getKey());
//                String accountId = availableAccount.getValue(String.class);
//                setupAccount(uid, accountId);
//                database.child("Available").child(accountKey).removeValue();
//                database.child("UsedAccounts").child(accountId).push().setValue(uid);
//            }
//        });
//
//    }
//
//    private void setupAccount(String uid, String accountId) {
//        database.child("Users").child(uid).setValue(accountId);
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