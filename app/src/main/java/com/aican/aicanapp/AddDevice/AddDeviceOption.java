package com.aican.aicanapp.AddDevice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;

import com.aican.aicanapp.R;

public class AddDeviceOption extends AppCompatActivity {

    CardView cvTemp, cvCooling, cvPh, cvPump, indusPh;

    public static final String TYPE_TEMP = "temp";
    public static final String TYPE_COOLING = "cooling";
    public static final String TYPE_PH = "ph";
    public static final String TYPE_PUMP = "pump";
    public static final String TYPE_INDUS_PH = "iph";

    public static final String KEY_DEVICE_TYPE = "type";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device_option);

        cvTemp = findViewById(R.id.cvTemp);
        cvCooling = findViewById(R.id.cvCooling);
        cvPh = findViewById(R.id.cvPh);
        cvPump = findViewById(R.id.cvPump);
        indusPh = findViewById(R.id.indusPh);

        cvTemp.setOnClickListener(v->{
            startQrActivity(TYPE_TEMP);
        });
        cvCooling.setOnClickListener(v->{
            startQrActivity(TYPE_COOLING);
        });
        cvPh.setOnClickListener(v->{
            startQrActivity(TYPE_PH);
        });
        cvPump.setOnClickListener(v->{
            startQrActivity(TYPE_PUMP);
        });
        indusPh.setOnClickListener(v->{
            startQrActivity(TYPE_INDUS_PH);
        });
    }

    private void startQrActivity(String type){
        Intent intent = new Intent(this, ScanQrActivity.class);
        intent.putExtra(KEY_DEVICE_TYPE, type);
        startActivity(intent);
    }
}