package com.aican.aicanapp.Dashboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aican.aicanapp.Authentication.LoginActivity;
import com.aican.aicanapp.FirebaseAccounts.PrimaryAccount;
import com.aican.aicanapp.R;
import com.aican.aicanapp.specificactivities.Export;
import com.aican.aicanapp.userdatabase.UserDatabase;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class AdminLoginActivity extends AppCompatActivity {
    FirebaseAuth auth;
    TextInputLayout ilEmail, ilPass;
    String email, password;
    String primaryEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        auth = FirebaseAuth.getInstance(PrimaryAccount.getInstance(this));

        EditText mail = findViewById(R.id.etAdminEmail);
        EditText pass = findViewById(R.id.etAdminPassword);
        Button login = findViewById(R.id.btnAdminLogin);

        ilEmail = findViewById(R.id.inputLayoutEmail);
        ilPass = findViewById(R.id.inputLayoutPass);

        login.setOnClickListener(v ->{
            email = mail.getText().toString().trim();
            password = pass.getText().toString().trim();

            SharedPreferences sh = getSharedPreferences("loginprefs", MODE_PRIVATE);
            primaryEmail = sh.getString("email", "").trim();

            if (email.equals(primaryEmail)) {

                auth.signInWithEmailAndPassword(
                        email, password
                ).addOnSuccessListener(authResult -> {
                    Intent intent = getIntent();
                    String checkFlag = intent.getStringExtra("checkBtn");
                    if(checkFlag.equals("addUser")){
                        startSettingActivity();
                    }else if(checkFlag.equals("logout")){
                        logout();
                    } else if(checkFlag.equals("checkDatabase")){
                        userDatabase();
                    }
                }).addOnFailureListener(exception -> {
                    if (exception instanceof FirebaseAuthInvalidUserException) {
                        ilEmail.setError("This Email ID is not registered");
                    } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                        ilPass.setError("Incorrect Password");
                    } else {
                        Toast.makeText(this, "Unknown Error", Toast.LENGTH_SHORT).show();
                    }
                    exception.printStackTrace();
                });
            } else {
                Toast.makeText(this,"This email is not connected with this device",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startSettingActivity() {
        Intent intent = new Intent(this,SettingActivity.class);
        startActivity(intent);
    }

    private void logout(){
        FirebaseAuth.getInstance(PrimaryAccount.getInstance(getApplicationContext())).signOut();
        finish();

        //Clear Shared Preference Login Emailvedant
        SharedPreferences sharedPreferences = getSharedPreferences("loginprefs", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.clear();
        myEdit.apply();
        finish();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void userDatabase(){
        Intent intent = new Intent(this, UserDatabase.class);
        startActivity(intent);
    }
}