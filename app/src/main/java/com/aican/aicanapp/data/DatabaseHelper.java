package com.aican.aicanapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jetbrains.annotations.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(@Nullable Context context) {
        super(context, "Userdata.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("create Table Userdetails(name TEXT,role TEXT,id TEXT,passcode TEXT,expiryDate TEXT,dateCreated TEXT)");
        sqLiteDatabase.execSQL("create Table LogUserdetails(date TEXT, time TEXT, ph TEXT, temperature TEXT, batchnum TEXT, arnum TEXT, compound TEXT)");
        sqLiteDatabase.execSQL("create Table TempLogUserdetails(date TEXT, time TEXT, set_temp TEXT, temp1 TEXT, temp2 TEXT, batchnum TEXT, product TEXT)");
        sqLiteDatabase.execSQL("create Table PrintLogUserdetails(date TEXT, time TEXT, ph TEXT, temperature TEXT, batchnum TEXT, arnum TEXT, compound TEXT)");
        sqLiteDatabase.execSQL("create Table PrintTempLogUserdetails(date TEXT, time TEXT, set_temp TEXT, temp1 TEXT, temp2 TEXT, batchnum TEXT, product TEXT)");
        sqLiteDatabase.execSQL("create Table Calibdetails(pH TEXT, mV TEXT, date TEXT)");
        sqLiteDatabase.execSQL("create Table UserActiondetails(time TEXT, useraction TEXT, ph TEXT, temperature TEXT, mv TEXT, compound TEXT)");
        sqLiteDatabase.execSQL("create Table TempUserActiondetails(time TEXT, useraction TEXT, ph TEXT, temperature TEXT, mv TEXT, compound TEXT)");
        sqLiteDatabase.execSQL("create Table CalibData(PH TEXT, MV TEXT, DT TEXT, BFD TEXT)");
        sqLiteDatabase.execSQL("create Table UserDataDetails(Username TEXT,Role TEXT,expiryDate TEXT,dateCreated TEXT)");
        sqLiteDatabase.execSQL("create Table ProbeDetail(probeInfo TEXT)");
        sqLiteDatabase.execSQL("create Table ECProbeDetail(ecProbeInfo TEXT)");
        sqLiteDatabase.execSQL("create Table PHBuffer(ID INTEGER PRIMARY KEY AUTOINCREMENT,PH_BUFF TEXT, minMV TEXT, maxMV TEXT)");
        sqLiteDatabase.execSQL("create Table PrintLogECdetails(date TEXT,time TEXT, conductivity TEXT,TDS TEXT ,temperature TEXT, productName TEXT,batch TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Userdetails");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS LogUserdetails");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS TempLogUserdetails");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS PrintLogUserdetails");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS PrintTempLogUserdetails");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Calibdetails");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS UserActiondetails");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS TempUserActiondetails");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS CalibData");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS UserDataDetails");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS ProbeDetail");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS ECProbeDetail");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS PHBuffer");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS PrintLogECdetails");
        onCreate(sqLiteDatabase);
    }

    public boolean insertUserData(String userName,String role,String expiryDate,String dateCreated){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Username", userName);
        contentValues.put("Role", role);
        contentValues.put("expiryDate", expiryDate);
        contentValues.put("dateCreated", dateCreated);
        long result = db.insert("UserDataDetails",null,contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }

    }

    public boolean insertLogECDetails(String date,String time,String conductivity,String TDS,String temp,String productName,String batch){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("date", date);
        contentValues.put("time", time);
        contentValues.put("conductivity", conductivity);
        contentValues.put("TDS", TDS);
        contentValues.put("temperature", temp);
        contentValues.put("productName", productName);
        contentValues.put("batch", batch);
        long result = db.insert("PrintLogECdetails",null,contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }

    }

    public Boolean insertCalibration(String PH, String MV, String DT, String BFD) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("PH", PH);
        contentValues.put("MV", MV);
        contentValues.put("DT", DT);
        contentValues.put("BFD", BFD);
        long result = db.insert("CalibData", null, contentValues);
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public Boolean insertCalibData(String pH, String mV,String date){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("pH", pH);
        contentValues.put("mV", mV);
        contentValues.put("date", date);
        long result = db.insert("Calibdetails", null, contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public Boolean insert_data(String name, String role,String id, String passcode,String expiryDate,String dateCreated){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("role", role);
        contentValues.put("id", id);
        contentValues.put("passcode", passcode);
        contentValues.put("expiryDate", expiryDate);
        contentValues.put("dateCreated", dateCreated);
        long result = db.insert("Userdetails", null, contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }
    public Boolean insertPHBuffer(int id, String PH_BUFF, String minMV, String maxMV, Context context) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("ID", id);
        contentValues.put("PH_BUFF", PH_BUFF);
        contentValues.put("minMV", minMV);
        contentValues.put("maxMV", maxMV);
        long result = db.insert("PHBuffer", null, contentValues);
//        Toast.makeText(context, "" + result, Toast.LENGTH_SHORT).show();
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }
    public Boolean insert_probe(String data) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("probeInfo", data);
        long result = db.insert("ProbeDetail", null, contentValues);

        return result != -1;
    }

    public Boolean insert_ec_probe(String data) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("ecProbeInfo", data);
        long result = db.insert("ECProbeDetail", null, contentValues);

        return result != -1;
    }

    public Boolean updateBufferData(int id, String PH_BUFF, String minMV, String maxMV, Context context) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("ID", id);
        contentValues.put("PH_BUFF", PH_BUFF);
        contentValues.put("minMV", minMV);
        contentValues.put("maxMV", maxMV);
        long result;
        result = db.update("PHBuffer", contentValues, "ID" + "=?",
                new String[]{String.valueOf(id)});

        db.close();

        if (result == 0) {
//            Toast.makeText(context, "Update failed : " + result, Toast.LENGTH_SHORT).show();
            return false;
        } else {
//            Toast.makeText(context, "Updated : " + result, Toast.LENGTH_SHORT).show();

            return true;
        }
//        return result > 0;
    }

    // for pH log
    public Boolean insert_log_data(String date, String time, String ph, String temperature, String batchnum, String arnum, String compound){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("date", date);
        contentValues.put("time", time);
        contentValues.put("ph", ph);
        contentValues.put("temperature", temperature);
        contentValues.put("batchnum", batchnum);
        contentValues.put("arnum", arnum);
        contentValues.put("compound", compound);
        long result = db.insert("LogUserdetails", null, contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    // for temp log
    public Boolean insert_temp_log_data(String date, String time, String set_temp, String temp1,String temp2, String batchnum, String product){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("date", date);
        contentValues.put("time", time);
        contentValues.put("set_temp", set_temp);
        contentValues.put("temp1", temp1);
        contentValues.put("temp2", temp2);
        contentValues.put("batchnum", batchnum);
        contentValues.put("product", product);
        long result = db.insert("TempLogUserdetails", null, contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }


    // for pH log
    public Boolean print_insert_log_data(String date, String time, String ph, String temperature, String batchnum, String arnum, String compound){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("date", date);
        contentValues.put("time", time);
        contentValues.put("ph", ph);
        contentValues.put("temperature", temperature);
        contentValues.put("batchnum", batchnum);
        contentValues.put("arnum", arnum);
        contentValues.put("compound", compound);
        long result = db.insert("PrintLogUserdetails", null, contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    // for temp log - temp
    public Boolean print_insert_log_data_temp(String date, String time, String set_temp, String temp1,String temp2, String batchnum, String product){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("date", date);
        contentValues.put("time", time);
        contentValues.put("set_temp", set_temp);
        contentValues.put("temp1", temp1);
        contentValues.put("temp2", temp2);
        contentValues.put("batchnum", batchnum);
        contentValues.put("product", product);
        long result = db.insert("PrintTempLogUserdetails", null, contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    // for pH log
    public Boolean insert_action_data(String time, String useraction, String ph, String temperature, String mv, String compound){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("time", time);
        contentValues.put("useraction", useraction);
        contentValues.put("ph", ph);
        contentValues.put("temperature", temperature);
        contentValues.put("mv", mv);
        contentValues.put("compound", compound);
        long result = db.insertOrThrow("UserActiondetails", null, contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    // for temp log
    public Boolean insert_action_data_temp(String time, String useraction, String ph, String temp1, String temp2, String product){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("time", time);
        contentValues.put("useraction", useraction);
        contentValues.put("ph", ph);
        contentValues.put("temp1", temp1);
        contentValues.put("temp2", temp2);
        contentValues.put("product", product);
        long result = db.insertOrThrow("TempUserActiondetails", null, contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public Boolean delete_data(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from Userdetails", null);
        if(cursor.getCount()>0){
            long result = db.delete("Userdetails", "name=?", new String[]{name});
            if(result == -1){
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }
    }

    public Boolean delete_Userdata(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from UserDataDetails", null);
        if(cursor.getCount()>0){
            long result = db.delete("UserDataDetails", "Username=?", new String[]{name});
            if(result == -1){
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }
    }

    public Cursor get_data(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from Userdetails", null);
        return cursor;
    }


    public Cursor get_probe() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from ProbeDetail", null);
        return cursor;
    }

    public Cursor get_ec_probe() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from ECProbeDetail", null);
        return cursor;
    }

    public Cursor get_log(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM LogUserDetails", null);
        return cursor;
    }

    public boolean updateUserDetails(String name,String newName,String role,String password){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from UserDataDetails", null);

        if(cursor.getCount()>0){
            ContentValues dataToInsert = new ContentValues();
            dataToInsert.put("id",newName);
            dataToInsert.put("role",role);

            ContentValues dataToInsertUserData = new ContentValues();
            dataToInsertUserData.put("Username",newName);
            dataToInsertUserData.put("Role",role);

            if(!password.isEmpty())
                dataToInsert.put("passcode",password);

            long result = db.update("Userdetails",dataToInsert,"id=?", new String[]{name});
            long result2 = db.update("UserDataDetails",dataToInsertUserData,"Username=?", new String[]{name});
            if(result==-1 || result2==-1){
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }





    }
}
