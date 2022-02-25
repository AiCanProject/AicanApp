package com.aican.aicanapp.Dashboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.aican.aicanapp.FirebaseAccounts.PrimaryAccount;
import com.aican.aicanapp.R;
import com.aican.aicanapp.fragments.ph.PhCalibFragment;
import com.aican.aicanapp.fragments.ph.PhFragment;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

public class SettingActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    EditText name, passcode;
    Button generate;
    Spinner spinner;
    String[] r = { "Operator", "Supervisor"};
    String Role;

    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        name = findViewById(R.id.assignedId);
        passcode = findViewById(R.id.assignedPwd);
        generate = findViewById(R.id.assignRole);
        spinner = findViewById(R.id.selectRole);
        ArrayAdapter role = new ArrayAdapter(this,android.R.layout.simple_spinner_item,r);
        role.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner

        spinner.setAdapter(role);
        mDatabase = FirebaseDatabase.getInstance(PrimaryAccount.getInstance(this)).getReference();

        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isEmailValid() && isPassCodeValid()){



//                    FirebaseDatabase.getInstance(PrimaryAccount.getInstance(SettingActivity.this))
//                            .getReference().child(FirebaseAuth.getInstance(PrimaryAccount.getInstance(SettingActivity.this)).getCurrentUser().getUid())
//                            .child("SUB_USER").push()
//                            .setValue(FieldValue.arrayUnion(Role.trim(),name.getText().toString().trim(), passcode.getText().toString().trim()));

                }
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(spinner.getSelectedItemPosition() == 0){
            Role = spinner.getSelectedItem().toString();

        }else if (spinner.getSelectedItemPosition() == 1) {
            Role = spinner.getSelectedItem().toString();

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private boolean isPassCodeValid() {
        String validName = passcode.getText().toString();
        if (validName.isEmpty()) {
            Toast.makeText(this, "Enter Passcode!", Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    private boolean isEmailValid() {
        String validName = name.getText().toString();
        if (validName.isEmpty()) {
            Toast.makeText(this, "Enter Email Address!", Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}