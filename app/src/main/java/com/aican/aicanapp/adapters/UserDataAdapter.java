package com.aican.aicanapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
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

import java.io.File;

public class UserDataAdapter extends RecyclerView.Adapter<UserDataAdapter.ViewHolder> {

    Context context;
    File[] files;
    //ImageView imageView;

    public UserDataAdapter(Context context, File[] files) {
        this.context = context;
        this.files = files;
    }

    @NonNull
    @Override
    public UserDataAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.file_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserDataAdapter.ViewHolder holder, int position) {

        File selectedFile = files[position];
        holder.textView.setText(selectedFile.getName());
        holder.imageView.setImageResource(R.drawable.ic_baseline_insert_drive_file_24);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String path2 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/LabApp/Useractivity/DataUserActivity.csv";

                File file2 = new File(path2);

                try {

                    Intent dIntent = new Intent(Intent.ACTION_VIEW);
                    dIntent.setDataAndType(Uri.fromFile(file2), "text/plain");
                    dIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    dIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    dIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    dIntent.setClassName("csv.file.reader", "csv.file.reader.CsvFileViewerActivity");

                    Intent vIntent = Intent.createChooser(dIntent, "Open User Data");
                    vIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.getApplicationContext().startActivity(vIntent);

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

                            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/LabApp/Useractivity/DataUserActivity.csv";
                            File file = new File(path);

                            try {
                                Intent mIntent = new Intent(Intent.ACTION_VIEW);

                                mIntent.setData(Uri.fromFile(file));
                                mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                mIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                mIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                mIntent.setClassName("csv.to.excel", "csv.to.excel.HomeActivity");

                                Intent chooserIntent = Intent.createChooser(mIntent, "Convert PDF");
                                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(chooserIntent);

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
