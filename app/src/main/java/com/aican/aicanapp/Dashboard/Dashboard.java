package com.aican.aicanapp.Dashboard;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.aican.aicanapp.AddDevice.AddDeviceOption;
import com.aican.aicanapp.R;
import com.aican.aicanapp.adapters.*;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Dashboard extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    //Recyclerviews-------------------------------------------------------------------

     private RecyclerView tempRecyclerView,coolingRecyclerView,phRecyclerView,pumpRecyclerView;
     private FloatingActionButton addNewDevice;
    //---------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        addNewDevice = findViewById(R.id.add_new_device);

        addNewDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toAddDevice = new Intent(Dashboard.this, AddDeviceOption.class);
                startActivity(toAddDevice);
            }
        });

        //----------------------------------------
        setUpToolBar();
        setUpTemp();
        setUpCooling();
        setUpPh();
        setUpPump();
        //----------------------------------------

    }

    //Toolbar------------------------------------------------------------------------------------------------------
    public void setUpToolBar(){
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.app_name,R.string.app_name);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }
    //Toolbar------------------------------------------------------------------------------------------------------

    //Temperature RC------------------------------------------------------------------------------------------------------
    public void setUpTemp(){
        tempRecyclerView = findViewById(R.id.temp_recyclerview);
        tempRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false));
        tempRecyclerView.setAdapter(new TempAdapter(Dashboard.this,new String[]{"98℃","25℃","69℃","78℃","55℃"}));
    }
    //Temperature RC------------------------------------------------------------------------------------------------------

    //Cooling RC------------------------------------------------------------------------------------------------------
    public void setUpCooling(){
        coolingRecyclerView = findViewById(R.id.cooling_recyclerview);
        coolingRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        coolingRecyclerView.setAdapter(new CoolingAdapter(new String[]{"-2℃","0℃","-10℃","1℃"}));
    }
    //Cooling RC------------------------------------------------------------------------------------------------------

    //Ph RC------------------------------------------------------------------------------------------------------
    public void setUpPh(){
        phRecyclerView = findViewById(R.id.ph_recyclerview);
        phRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        phRecyclerView.setAdapter(new PhAdapter(new String[]{"2","7","4","6"},Dashboard.this));
    }
    //Ph RC------------------------------------------------------------------------------------------------------

    //Pump RC------------------------------------------------------------------------------------------------------
    public void setUpPump(){
        pumpRecyclerView = findViewById(R.id.pump_recyclerview);
        pumpRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        pumpRecyclerView.setAdapter(new PumpAdapter(new String[]{"Dose","Pump","Dose","Pump"}));
    }
    //Pump RC------------------------------------------------------------------------------------------------------
}