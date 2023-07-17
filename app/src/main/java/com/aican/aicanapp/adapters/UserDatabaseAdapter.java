package com.aican.aicanapp.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanapp.data.DatabaseHelper;
import com.aican.aicanapp.R;
import com.aican.aicanapp.specificactivities.PhActivity;
import com.aican.aicanapp.userdatabase.EditUserDatabase;
import com.aican.aicanapp.userdatabase.UserDatabase;
import com.aican.aicanapp.userdatabase.UserDatabaseModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        View view = LayoutInflater.from(context).inflate(R.layout.tablelayout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        databaseHelper = new DatabaseHelper(context.getApplicationContext());

        if (users_list != null && users_list.size() > 0) {
            UserDatabaseModel model = users_list.get(position);
            holder.user_role.setText(model.getUser_role());
            holder.user_name.setText(model.getUser_name());
            if (model.getUser_role().equals("Admin")) {
                holder.expiry_date.setText("No expiry");
            } else {
                holder.expiry_date.setText(model.getExpiry_date());
            }
            holder.dateCreated.setText(model.getDateCreated());
        } else {
            return;
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (users_list.get(position).getUser_role().equals("Admin")) {

                } else {
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
                }

                return true;
            }
        });

        holder.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EditUserDatabase.class);
                intent.putExtra("username", users_list.get(position).getUser_name());
                intent.putExtra("userrole", users_list.get(position).getUser_role());
                intent.putExtra("passcode", users_list.get(position).getPasscode());
                intent.putExtra("uid", users_list.get(position).getId());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.getApplicationContext().startActivity(intent);
            }
        });

        if (users_list.get(position).getUser_role().equals("Admin")) {
            holder.deleteBtn.setVisibility(View.GONE);
        }

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                UserDatabaseModel model = users_list.get(position);
                databaseHelper.delete_data(model.getUser_name());
                databaseHelper.delete_Userdata(model.getUser_name());
                databaseHelper.insert_action_data(time, date, "Username: " + model.getUser_name() + " Deleted", "", "", "", "", PhActivity.DEVICE_ID);

                Toast.makeText(view.getContext(), "Record Deleted Successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, UserDatabase.class);
                context.startActivity(intent);
                ((Activity) context).finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return users_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView user_role, user_name, expiry_date, dateCreated;
        ImageButton editBtn, deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            user_role = itemView.findViewById(R.id.user_role);
            user_name = itemView.findViewById(R.id.user_name);
            editBtn = itemView.findViewById(R.id.editBtn);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
            expiry_date = itemView.findViewById(R.id.expiry_date);
            dateCreated = itemView.findViewById(R.id.dateCreated);
        }
    }
}
