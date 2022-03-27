package com.aican.aicanapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(@Nullable Context context) {
        super(context, "Userdata.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("create Table Userdetails(name TEXT,role TEXT,id TEXT,passcode TEXT)");
        sqLiteDatabase.execSQL("create Table LogUserdetails(time TEXT, ph TEXT, mv TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Userdetails");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS LogUserdetails");
        onCreate(sqLiteDatabase);
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

    public Boolean insert_log_data(String time, String ph, String mv){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("time", time);
        contentValues.put("ph", ph);
        contentValues.put("mv", mv);
        long result = db.insert("LogUserdetails", null, contentValues);
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
        Cursor cursor = db.rawQuery("Select * from LogUserdetails", null);
        return cursor;
    }
}
