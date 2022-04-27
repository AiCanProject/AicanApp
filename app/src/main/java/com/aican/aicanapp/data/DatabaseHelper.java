package com.aican.aicanapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.jetbrains.annotations.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(@Nullable Context context) {
        super(context, "Userdata.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create Table Userdetails(name TEXT,role TEXT,id TEXT,passcode TEXT)");
        sqLiteDatabase.execSQL("create Table LogUserdetails(time TEXT, ph TEXT, temperature TEXT, compound TEXT)");
        sqLiteDatabase.execSQL("create Table PrintLogUserdetails(time TEXT, ph TEXT, temperature TEXT, compound TEXT)");
        sqLiteDatabase.execSQL("create Table Calibdetails(pH TEXT, mV TEXT, date TEXT)");
        sqLiteDatabase.execSQL("create Table UserActiondetails(time TEXT, useraction TEXT, ph TEXT, temperature TEXT, mv TEXT, compound TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Userdetails");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS LogUserdetails");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS PrintLogUserdetails");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Calibdetails");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS UserActiondetails");
        onCreate(sqLiteDatabase);
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

    public Boolean insert_data(String name, String role,String id, String passcode){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("role", role);
        contentValues.put("id", id);
        contentValues.put("passcode", passcode);
        long result = db.insert("Userdetails", null, contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public Boolean insert_log_data(String time, String ph, String temperature, String compound){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("time", time);
        contentValues.put("ph", ph);
        contentValues.put("temperature", temperature);
        contentValues.put("compound", compound);
        long result = db.insert("LogUserdetails", null, contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public Boolean print_insert_log_data(String time, String ph, String temperature, String compound){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("time", time);
        contentValues.put("ph", ph);
        contentValues.put("temperature", temperature);
        contentValues.put("compound", compound);
        long result = db.insert("PrintLogUserdetails", null, contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

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

    public Boolean delete_data(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from Userdetails where name = ?", new String[]{name});
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

    public Cursor get_data(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from Userdetails", null);
        return cursor;
    }

    public Cursor get_log(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM LogUserDetails", null);
        return cursor;
    }
}
