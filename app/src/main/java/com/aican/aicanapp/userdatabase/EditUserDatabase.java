package com.aican.aicanapp.userdatabase;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.aican.aicanapp.R;
import com.aican.aicanapp.data.DatabaseHelper;

public class EditUserDatabase extends AppCompatActivity {
    Spinner spinner;
    EditText name;
    String[] r = {"Operator", "Supervisor"};
    DatabaseHelper databaseHelper;
    String username="";
    String userRole="";
    Button update;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_database);

        spinner = findViewById(R.id.selectRole);
        name = findViewById(R.id.username);
        databaseHelper = new DatabaseHelper(this);
        update = findViewById(R.id.updateBtn);

        Intent intent = getIntent();

        username = intent.getStringExtra("username");
        userRole = intent.getStringExtra("userrole");

        name.setText(username);

        ArrayAdapter<String> role = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, r);
        role.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(role);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                userRole = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if(userRole.equals(r[0])){
            spinner.setSelection(0);
        }else {
            spinner.setSelection(1);
        }

        update.setOnClickListener(view -> {
            if(databaseHelper.updateUserDetails(username,name.getText().toString(),userRole)){

            }else{
                Toast.makeText(this, "Something", Toast.LENGTH_SHORT).show();
            }
        });
    }

}