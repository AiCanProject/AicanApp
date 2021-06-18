package com.aican.aicanapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.aican.aicanapp.Dashboard.*;

public class MainActivity extends AppCompatActivity {

    private Button login, register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login = findViewById(R.id.main_login_btn);
        register = findViewById(R.id.main_register_btn);

        // Login button on click listener
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkLogin();
            }
        });

    }
    // Go to DashBoard
    public void checkLogin(){
        Intent toDashBoard = new Intent(MainActivity.this, Dashboard.class);
        startActivity(toDashBoard);
    }

}