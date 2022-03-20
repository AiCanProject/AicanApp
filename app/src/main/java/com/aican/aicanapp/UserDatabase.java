package com.aican.aicanapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class UserDatabase extends AppCompatActivity {

    RecyclerView recyclerView;
    UserDatabaseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_database);

        recyclerView = findViewById(R.id.user_database_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserDatabaseAdapter(this, getList());
        recyclerView.setAdapter(adapter);
    }

    private List<UserDatabaseModel> getList(){
        List<UserDatabaseModel> userDatabaseModelList = new ArrayList<>();
        userDatabaseModelList.add(new UserDatabaseModel("1","2","3"));
        userDatabaseModelList.add(new UserDatabaseModel("4","5","6"));
        userDatabaseModelList.add(new UserDatabaseModel("7adaadadad","adadada","daasdad"));
        return userDatabaseModelList;
    }
}