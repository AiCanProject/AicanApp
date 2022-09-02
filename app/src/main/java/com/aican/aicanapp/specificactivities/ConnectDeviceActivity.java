package com.aican.aicanapp.specificactivities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aican.aicanapp.R;
import com.aican.aicanapp.retrofit.DeviceClient;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConnectDeviceActivity extends AppCompatActivity {

    Button btnConnect, btnOpenSettings;
    TextInputEditText etSsid, etPass;
    TextInputLayout tilSsid,tilPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_device);

        btnConnect = findViewById(R.id.btnConnect);
        btnOpenSettings = findViewById(R.id.btnOpenSettings);
        etSsid = findViewById(R.id.etSsid);
        etPass = findViewById(R.id.etPassword);
        tilPass = findViewById(R.id.tilPassword);
        tilSsid = findViewById(R.id.tilSsid);


        btnConnect.setOnClickListener(v -> {
            if(validate()){
                callUrl(etSsid.getText().toString(), etPass.getText().toString());
            }
        });
        btnOpenSettings.setOnClickListener(v->{
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        });
    }

    private void callUrl(String homeSsid, String homePass) {
        DeviceClient.client.checkActive().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {

                Toast.makeText(ConnectDeviceActivity.this, "Credentials sent to device", Toast.LENGTH_SHORT).show();

                DeviceClient.client.connect(homeSsid, homePass).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                    }


                    @Override
                    public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                    }

                });
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                Toast.makeText(ConnectDeviceActivity.this, "Connection Failed", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private boolean validate() {
        if (etSsid.getText() == null || etSsid.getText().length() == 0) {
            tilSsid.setError("Invalid SSID");
            return false;
        }
        if (etPass.getText() == null || etPass.getText().length() == 0) {
            tilPass.setError("Invalid Password");
            return false;
        }
        return true;
    }
}