package com.aican.aicanapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanapp.R;
import com.aican.aicanapp.utils.PDFViewer;

import java.io.File;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{

    Context context;
    File[] files;

    public UserAdapter(Context context, File[] files) {
        this.context = context;
        this.files = files;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.file_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        File selectedFile = files[position];
        holder.textView.setText(selectedFile.getName());
        holder.imageView.setImageResource(R.drawable.ic_baseline_insert_drive_file_24);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());

                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/LabApp/UserData/"+selectedFile.getName();
                File file = new File(path);

                try {
                    if(file.length()!=0) {
                        Intent intent = new Intent(context.getApplicationContext(), PDFViewer.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("Path", path);
                        context.getApplicationContext().startActivity(intent);
                    }else{
                        Toast.makeText(context, "File is empty", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.getMenu().add("DELETE");
                popupMenu.getMenu().add("SHARE");

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getTitle().equals("DELETE")) {
                            boolean deleted = selectedFile.delete();
                            if (deleted) {
                                Toast.makeText(context.getApplicationContext(), "DELETED ", Toast.LENGTH_SHORT).show();
                                v.setVisibility(View.GONE);
                            }
                        }
                        if (item.getTitle().equals("SHARE")) {

                            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/LabApp/UserData/"+selectedFile.getName();
                            File file = new File(path);

                            try {
                                if (!file.exists()){
                                    Toast.makeText(context, "File doesn't exists", Toast.LENGTH_LONG).show();
                                }else {
                                    Intent intentShare = new Intent(Intent.ACTION_SEND);
                                    intentShare.setType("application/pdf");
                                    intentShare.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    intentShare.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                    intentShare.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intentShare.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file));

                                    Intent chooserIntent = Intent.createChooser(intentShare, "Send file");
                                    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                    context.getApplicationContext().startActivity(chooserIntent);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        return true;
                    }
                });

                popupMenu.show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.file_name_text_view);
            imageView = itemView.findViewById(R.id.icon_view);
        }
    }
}


