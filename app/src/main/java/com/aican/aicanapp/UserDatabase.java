package com.aican.aicanapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class UserDatabase extends AppCompatActivity {

    private static final String FILE_NAME = "user_log.txt";

    private String[] lines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_database);

        RecyclerView recyclerView = findViewById(R.id.user_database_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        UserDatabaseAdapter adapter = new UserDatabaseAdapter(this, getList());
        recyclerView.setAdapter(adapter);
    }

    private List<UserDatabaseModel> getList(){

        int count=0;
        FileInputStream fis = null;
        try {
            fis = this.openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
                count++;
            }
            //Log.d("86461651", String.valueOf(sb.capacity()));
        /*    Log.d("86461651", String.valueOf(sb));
            Log.d("86461651", String.valueOf(count));*/
            lines = sb.toString().split("\\n");
       /* //    Log.d("86461651", String.valueOf(lines[0]));
         //   Log.d("86461651", String.valueOf(lines[3]));
            for(int i=0;i<count;i++){
                Log.d("86461651", String.valueOf(lines[i]) + "     aaa");
            }*/

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        List<UserDatabaseModel> userDatabaseModelList = new ArrayList<>();
        for(int i=1;i<count;i=i+2){
            Log.d("86461651", String.valueOf(lines[i]) + "      bbb");
            Log.d("86461651", String.valueOf(lines[i+1]) + "       bbb");
            userDatabaseModelList.add(new UserDatabaseModel(lines[i],lines[i+1]));
        }
        return userDatabaseModelList;
    }
}