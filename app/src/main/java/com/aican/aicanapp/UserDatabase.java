package com.aican.aicanapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.os.Bundle;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class UserDatabase extends AppCompatActivity {

    String name_fetched, role_fetched;
    ArrayList<UserDatabaseModel> userDatabaseModelList = new ArrayList<>();

    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_database);

        databaseHelper = new DatabaseHelper(this);

        RecyclerView recyclerView = findViewById(R.id.user_database_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        UserDatabaseAdapter adapter = new UserDatabaseAdapter(this, getList());
        recyclerView.setAdapter(adapter);
    }

    private List<UserDatabaseModel> getList(){
        Cursor res = databaseHelper.get_data();
        if(res.getCount()==0){
            Toast.makeText(UserDatabase.this, "No entry", Toast.LENGTH_SHORT).show();
        }
        while(res.moveToNext()){
            name_fetched = res.getString(0);
            role_fetched = res.getString(1);
            userDatabaseModelList.add(new UserDatabaseModel(name_fetched,role_fetched));
        }
        return userDatabaseModelList;
    }
}