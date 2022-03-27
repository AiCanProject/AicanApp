package com.aican.aicanapp.userdatabase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.os.Bundle;

import android.widget.Toast;

import com.aican.aicanapp.R;
import com.aican.aicanapp.Source;
import com.aican.aicanapp.adapters.UserDatabaseAdapter;
import com.aican.aicanapp.data.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class UserDatabase extends AppCompatActivity {

    private ArrayList<UserDatabaseModel> userDatabaseModelList = new ArrayList<>();
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_database);

        databaseHelper = new DatabaseHelper(this);
        Source.id_fetched = new ArrayList<>();
        Source.passcode_fetched = new ArrayList<>();

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
            userDatabaseModelList.add(new UserDatabaseModel(res.getString(0),res.getString(1)));
        }
        return userDatabaseModelList;
    }
}