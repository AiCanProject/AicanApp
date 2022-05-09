package com.aican.aicanappnoncfr.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanappnoncfr.data.DatabaseHelper;
import com.aican.aicanappnoncfr.R;
import com.aican.aicanappnoncfr.userdatabase.UserDatabase;
import com.aican.aicanappnoncfr.userdatabase.UserDatabaseModel;

import java.util.List;

public class UserDatabaseAdapter extends RecyclerView.Adapter<UserDatabaseAdapter.ViewHolder> {

    DatabaseHelper databaseHelper;

    Context context;
    List<UserDatabaseModel> users_list;

    public UserDatabaseAdapter(Context context, List<UserDatabaseModel> users_list) {
        this.context = context;
        this.users_list = users_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.tablelayout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        databaseHelper = new DatabaseHelper(context.getApplicationContext());

        if(users_list != null && users_list.size() > 0){
            UserDatabaseModel model = users_list.get(position);
            holder.user_role.setText(model.getUser_role());
            holder.user_name.setText(model.getUser_name());
        }
        else{
            return;
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Are you sure?")
                        .setMessage("Do you want to delete this record")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                UserDatabaseModel model = users_list.get(position);
                                databaseHelper.delete_data(model.getUser_name());
                                Toast.makeText(view.getContext(), "Record Deleted Successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(context, UserDatabase.class);
                                context.startActivity(intent);
                                ((Activity) context).finish();
                            }
                        }).setNegativeButton("No", null)
                        .show();
                return true;
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return users_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView user_role, user_name;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            user_role = itemView.findViewById(R.id.user_role);
            user_name = itemView.findViewById(R.id.user_name);
        }
    }
}
