package com.aican.aicanapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.aicanapp.BuildConfig;
import com.aican.aicanapp.FileOpen;
import com.aican.aicanapp.R;
import com.aican.aicanapp.specificactivities.Export;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    Context context;
    File[] files;
    //ImageView imageView;

    public FileAdapter(Context context, File[] files) {
        this.context = context;
        this.files = files;
    }

    @NonNull
    @Override
    public FileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.file_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileAdapter.ViewHolder holder, int position) {

        File selectedFile = files[position];
        holder.textView.setText(selectedFile.getName());
        holder.imageView.setImageResource(R.drawable.ic_baseline_insert_drive_file_24);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String selectedFilePath = "/sdcard/Download/" + holder.textView.getText().toString();
                File file = new File(selectedFilePath);
                try {
                    FileOpen.openFile(v.getContext(), file);
                } catch (IOException e) {
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

                            String file = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/SensorData.csv";

                             Uri csvUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", new File(file));

                            Log.d("urifile", file);

                            Intent intentShare = new Intent();

                            intentShare.setAction(Intent.ACTION_SEND);
                            intentShare.setType("plain/text");
                            intentShare.putExtra(Intent.EXTRA_STREAM, csvUri);
                            intentShare.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intentShare.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intentShare.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                            Intent chooserIntent = Intent.createChooser(intentShare, "Sharing CSV");
                            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.getApplicationContext().startActivity(chooserIntent);




//                            intentShare.setAction(Intent.ACTION_SEND);
//                            intentShare.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                            intentShare.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                            intentShare.putExtra(Intent.EXTRA_STREAM, Uri.parse(file));
//                            intentShare.setType("text/csv");
//

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
