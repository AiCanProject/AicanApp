package com.aican.aicanapp.DownloadHandler;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.aican.aicanapp.BuildConfig;
import com.aican.aicanapp.R;
import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;

import java.io.File;

public class DownloadHandler {

    AlertDialog.Builder alertDialog;
    AlertDialog.Builder failed;
    ProgressDialog progressDialog;
    File fileDestination;
    DownloadCompletedListener downloadCompletedListener;


    public void downloadFile(String url, String fileName, Context context, File filePath) {


        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Downloading....");
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("Downloaded successfully");
        alertDialog.setMessage(fileName + " is downloaded successfully");
        alertDialog.setIcon(R.drawable.done);
        alertDialog.setPositiveButton("Open", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                Intent intent = new Intent(context, DownloadedFiles.class);
//                context.startActivity(intent);
            }
        });

        failed = new AlertDialog.Builder(context);
        failed.setTitle("Downloading failed");
        failed.setMessage("Your file is not downloaded successfully");
        failed.setIcon(R.drawable.error);

//        File file = new File(Environment.getExternalStorageDirectory(), "/Khan Sir App");
//        File file = new File(getExternalFilesDir(null) + "/" + "Khan Sir App");


        int downloadID = PRDownloader.download(url, filePath.getPath(), fileName)
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {

                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {

                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {

                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {
                        long per = progress.currentBytes * 100 / progress.totalBytes;
                        progressDialog.setMessage("Downloading : " + per + " %");


                    }
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {

//                        Intent i = new Intent(context,MainActivity.class).putExtra("page","d")
//                                .putExtra("sTitle",fileName);
//                        context.startActivity(i);
//                        ((Activity)context).finish();
//                        ((Activity) context).overridePendingTransition(R.anim.no_animation, R.anim.no_animation);

//                        ViewHolderClass.downloadBtn.setVisibility(View.GONE);

                        progressDialog.dismiss();
//                        alertDialog.show();

                        Toast.makeText(context, "Download completed", Toast.LENGTH_SHORT).show();

//                        Intent target = new Intent(Intent.ACTION_INSTALL_PACKAGE);

                        String PATH = Environment.getExternalStorageDirectory() + "/" + context.getString(R.string.folderLocation) + "/" + "labApp.apk";
//                        File file = new File(PATH);
                        File file;
                        file = new File(context.getExternalFilesDir(null) + "/" + context.getString(R.string.folderLocation) + "/" + "labApp.apk");
                        if (file.exists()) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(uriFromFile(context, new File(context.getExternalFilesDir(null) + "/" + context.getString(R.string.folderLocation) + "/" + "labApp.apk")), "application/vnd.android.package-archive");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            try {
                                context.startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                                Log.e("TAG", "Error in opening the file!");
                            }
                        } else {
                            Toast.makeText(context, "installing", Toast.LENGTH_LONG).show();
                        }
                    }

                    Uri uriFromFile(Context context, File file) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
                        } else {
                            return Uri.fromFile(file);
                        }


                    }


                    @Override
                    public void onError(Error error) {


                        Toast.makeText(context, "Something went wrong : " + error.getServerErrorMessage(), Toast.LENGTH_SHORT).show();
                        failed.show();
                        progressDialog.dismiss();

                    }


                });
//        Toast.makeText(context, "Download completed " + tof[0], Toast.LENGTH_SHORT).show();

    }


}
