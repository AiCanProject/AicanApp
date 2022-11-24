package com.aican.aicanapp.Dashboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
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
import com.aican.aicanapp.specificactivities.Export;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
//                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                showOptionDialog();
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
                Source.expiryDate = getExpiryDate();
                Source.dateCreated = getPresentDate();
                Log.d("expiryDate", "onCreate: " + Source.expiryDate);
                databaseHelper.insert_data(Source.userName, Source.userRole, Source.userId, Source.userPasscode, Source.expiryDate, Source.dateCreated);
                databaseHelper.insertUserData(Source.userName, Source.userRole, Source.expiryDate, Source.dateCreated);
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
            } else {
                Toast.makeText(this, "Role Not Assigned", Toast.LENGTH_SHORT).show();
            }
        });

        Bitmap comLo = getSignImage();
        if (comLo != null) {
            imageView.setImageBitmap(comLo);
        }

    }

    private Bitmap getSignImage() {
        SharedPreferences sh = getSharedPreferences("signature", Context.MODE_PRIVATE);
        String photo = sh.getString("signature_data", "");
        Bitmap bitmap = null;

        if (!photo.equalsIgnoreCase("")) {
            byte[] b = Base64.decode(photo, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
        }
        return bitmap;
    }

    public static int PICK_IMAGE = 1;

    private void showOptionDialog() {
        Dialog dialog = new Dialog(SettingActivity.this);
        dialog.setContentView(R.layout.img_options_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);
        dialog.findViewById(R.id.gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                dialog.dismiss();
            }
        });
        dialog.show();
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
            saveImage(photo);
            imageView.setImageBitmap(photo);
            imageView.setVisibility(View.VISIBLE);
//            addSign.setText("Ok!");
//            addSign.setEnabled(false);
        }
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri picUri = data.getData();//<- get Uri here from data intent
                if (picUri != null) {
//                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    Bitmap photo = null;

                    try {
                        photo = android.provider.MediaStore.Images.Media.getBitmap(
                                this.getContentResolver(),
                                picUri);
                        saveImage(photo);
                        imageView.setImageBitmap(photo);
                        imageView.setVisibility(View.VISIBLE);
//                        selectCompanyLogo.setText("Ok!");
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                }
            }
        }
    }

    private void saveImage(Bitmap realImage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        realImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();

        String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);

        SharedPreferences shre = getSharedPreferences("signature", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = shre.edit();
        edit.putString("signature_data", encodedImage);
        edit.commit();
    }

    private String getExpiryDate() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String presentDate = dateFormat.format(date);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            cal.setTime(sdf.parse(presentDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // use add() method to add the days to the given date  
        cal.add(Calendar.DAY_OF_MONTH, 90);
        String expiryDate = sdf.format(cal.getTime());

        return expiryDate;
    }

    private String getPresentDate() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String presentDate = dateFormat.format(date);
        return presentDate;
    }
}