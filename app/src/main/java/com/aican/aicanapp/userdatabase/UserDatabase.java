package com.aican.aicanapp.userdatabase;

import static androidx.camera.core.CameraX.getContext;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import android.os.Environment;
import android.text.method.CharacterPickerDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.aican.aicanapp.Dashboard.Dashboard;
import com.aican.aicanapp.Dashboard.SettingActivity;
import com.aican.aicanapp.R;
import com.aican.aicanapp.Source;
import com.aican.aicanapp.adapters.PrintLogAdapter;
import com.aican.aicanapp.adapters.UserAdapter;
import com.aican.aicanapp.adapters.UserDatabaseAdapter;
import com.aican.aicanapp.data.DatabaseHelper;
import com.aican.aicanapp.fragments.ph.PhFragment;
import com.aspose.cells.FileFormatType;
import com.aspose.cells.LoadOptions;
import com.aspose.cells.PdfCompliance;
import com.aspose.cells.PdfSaveOptions;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserDatabase extends AppCompatActivity {

    private ArrayList<UserDatabaseModel> userDatabaseModelList = new ArrayList<>();
    private DatabaseHelper databaseHelper;
    private Button printBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_database);

        databaseHelper = new DatabaseHelper(this);
        printBtn = findViewById(R.id.printBtn);

        RecyclerView recyclerView = findViewById(R.id.user_database_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        UserDatabaseAdapter adapter = new UserDatabaseAdapter(this, getList());
        recyclerView.setAdapter(adapter);

        printBtn.setOnClickListener(view -> {
            exportCsv();

            try {
                Workbook workbook = new Workbook(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/UserData/UserData.xlsx");
                PdfSaveOptions options = new PdfSaveOptions();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String currentDateandTime = sdf.format(new Date());
                options.setCompliance(PdfCompliance.PDF_A_1_B);

                workbook.save(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/UserData/UserData"+currentDateandTime+".pdf", options);

                String path1 = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/UserData";
                File root1 = new File(path1);
                fileNotWrite(root1);
                File[] filesAndFolders1 = root1.listFiles();

                if (filesAndFolders1 == null || filesAndFolders1.length == 0) {

                    return;
                } else {
                    for (int i = 0; i < filesAndFolders1.length; i++) {
                        if(filesAndFolders1[i].getName().endsWith(".csv") || filesAndFolders1[i].getName().endsWith(".xlsx")  ){
                            filesAndFolders1[i].delete();
                        }
                    }
                }

                String pathPDF = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/UserData/";
                File rootPDF = new File(pathPDF);
                fileNotWrite(rootPDF);
                File[] filesAndFoldersPDF = rootPDF.listFiles();
                File[] filesAndFoldersNewPDF = new File[1];


                if (filesAndFoldersPDF == null || filesAndFoldersPDF.length == 0) {
                    return;
                } else {
                    for (int i = 0; i < filesAndFoldersPDF.length; i++) {
                        if(filesAndFoldersPDF[i].getName().endsWith(".pdf")){
                            filesAndFoldersNewPDF[0]=filesAndFoldersPDF[i];
                        }
                    }
                }

                RecyclerView pdfRecyclerView= findViewById(R.id.userDataPDF);
                UserAdapter plAdapter = new UserAdapter(this, filesAndFoldersPDF);
                pdfRecyclerView.setAdapter(plAdapter);
                plAdapter.notifyDataSetChanged();
                pdfRecyclerView.setLayoutManager(new LinearLayoutManager(this));


            } catch (Exception e) {
                e.printStackTrace();
            }
        });




    }

    public void fileNotWrite(File file){
        file.setWritable(false);
        if(file.canWrite()){
            Log.d("csv","Not Working");
        } else {
            Log.d("csvnw","Working");
        }
    }

    private List<UserDatabaseModel> getList(){
        Cursor res = databaseHelper.get_data();
        if(res.getCount()==0){
            Toast.makeText(UserDatabase.this, "No entry", Toast.LENGTH_SHORT).show();
        }
        while(res.moveToNext()){
            userDatabaseModelList.add(new UserDatabaseModel(res.getString(0),res.getString(1),res.getString(4),res.getString(5)));
        }
        return userDatabaseModelList;
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        startActivity(new Intent(UserDatabase.this, Dashboard.class));
        finish();

    }

    public void exportCsv() {
        //We use the Download directory for saving our .csv file.
        File exportDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/UserData");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File file;
        PrintWriter printWriter = null;

        try {
            file = new File(exportDir, "UserData.csv");
            file.createNewFile();
            printWriter = new PrintWriter(new FileWriter(file), true);
            String nullEntry=" ";
            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            Cursor curCSV = db.rawQuery("SELECT * FROM UserDataDetails", null);

            printWriter.println(nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry);
            printWriter.println("UserData Table" + "," + nullEntry + "," + nullEntry + "," + nullEntry+ "," + nullEntry+ "," + nullEntry+ "," + nullEntry);
            printWriter.println("UserName,UserRole");
            printWriter.println(nullEntry + "," + nullEntry);

            while (curCSV.moveToNext()) {

                String userName = curCSV.getString(curCSV.getColumnIndex("Username"));
                String userRole = curCSV.getString(curCSV.getColumnIndex("Role"));

                String record = userName + "," + userRole;

                printWriter.println(record);
            }
            printWriter.println(nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry);
            printWriter.println(nullEntry + "," + nullEntry);
            printWriter.println("Operator Sign" + "," + nullEntry + "," + nullEntry + "," + nullEntry+ "," + "Supervisor Sign"+ "," + nullEntry+ "," + nullEntry);
            curCSV.close();
            db.close();

            LoadOptions loadOptions = new LoadOptions(FileFormatType.CSV);

            String inputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/UserData/";
            Workbook workbook = new Workbook(inputFile + "UserData.csv", loadOptions);
            Worksheet worksheet = workbook.getWorksheets().get(0);
            worksheet.getCells().setColumnWidth(0,18.5);
            worksheet.getCells().setColumnWidth(2,18.5);
            workbook.save(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/LabApp/UserData/UserData.xlsx", SaveFormat.XLSX);

        } catch (Exception e) {
            Log.d("csvexception", String.valueOf(e));
        }
    }
}