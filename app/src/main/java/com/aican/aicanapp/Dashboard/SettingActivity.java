package com.aican.aicanapp.Dashboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.aican.aicanapp.data.DatabaseHelper;
import com.aican.aicanapp.FirebaseAccounts.PrimaryAccount;
import com.aican.aicanapp.R;
import com.aican.aicanapp.Source;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileOutputStream;
import java.io.IOException;

public class SettingActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String FILE_NAME = "user_info.txt";
    private static final String FILE_NAMEE = "user_calibrate.txt";

    DatabaseHelper databaseHelper;

    EditText name, passcode, userId;
    Button generate, addSign;
    Spinner spinner;
    ImageView imageView, user_database;
    String[] r = {"Operator", "Supervisor"};
    String Role;

    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_REQUEST = 1888;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        userId = findViewById(R.id.assignedId);
        name = findViewById(R.id.assignedName);
        passcode = findViewById(R.id.assignedPwd);
        generate = findViewById(R.id.assignRole);
        spinner = findViewById(R.id.selectRole);
        addSign = findViewById(R.id.addSignature);
        imageView = findViewById(R.id.signature);
        user_database = findViewById(R.id.btnUserDatabase);

        addSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        ArrayAdapter<String> role = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, r);
        role.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(role);
        spinner.setOnItemSelectedListener(this);
        mDatabase = FirebaseDatabase.getInstance(PrimaryAccount.getInstance(this)).getReference();

        databaseHelper = new DatabaseHelper(this);

        user_database.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AdminLoginActivity.class);
                intent.putExtra("checkBtn", "checkDatabase");
                startActivity(intent);
            }
        });

        generate.setOnClickListener(view -> {
            if (isEmailValid() && isPassCodeValid()) {
                Source.userRole = Role;
                Source.userId = userId.getText().toString();
                Source.userName = name.getText().toString();
                Source.userPasscode = passcode.getText().toString();
                databaseHelper.insert_data(Source.userName, Source.userRole, Source.userId, Source.userPasscode);
                String details = Role + "\n" + name.getText().toString() + "\n" + passcode.getText().toString() + "\n" + userId.getText().toString();

                FileOutputStream fos = null;

                Toast.makeText(getApplicationContext(), "Role Assigned", Toast.LENGTH_SHORT).show();
                try {
                    fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
                    fos.write(details.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            else{
                Toast.makeText(this, "Role Not Assigned", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(SettingActivity.this, Dashboard.class));
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Role = adapterView.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Role = spinner.getSelectedItem().toString();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
            imageView.setVisibility(View.VISIBLE);
            addSign.setText("Ok!");
            addSign.setEnabled(false);
        }
    }
}