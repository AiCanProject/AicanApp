package com.aican.aicanapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserDatabaseAdapter extends RecyclerView.Adapter<UserDatabaseAdapter.ViewHolder> {

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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(users_list != null && users_list.size() > 0){
            UserDatabaseModel model = users_list.get(position);
            holder.user_id.setText(model.getUser_id());
            holder.user_role.setText(model.getUser_role());
            holder.user_name.setText(model.getUser_name());
        }
        else{
            return;
        }
    }

    @Override
    public int getItemCount() {
        return users_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView user_role, user_id, user_name;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            user_role = itemView.findViewById(R.id.user_role);
            user_name = itemView.findViewById(R.id.user_name);
            user_id = itemView.findViewById(R.id.user_id);
        }
    }
}
