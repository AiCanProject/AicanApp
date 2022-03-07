package com.aican.aicanapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteDb extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "aican_app";

    private static final String USER_TABLE_ROLE = "user_table";
    private static final String USER_COLUMN_ID = "role";
    private static final String USER_COLUMN_NAME = "name";
    private static final String USER_COLUMN_PASSCODE = "passcode";
    //public static final String USER_COLUMN_SIGNATURE = "signature";

    public SQLiteDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TABLE = "CREATE TABLE " + USER_TABLE_ROLE + "("
                + USER_COLUMN_ID + " INTEGER PRIMARY KEY," + USER_COLUMN_NAME + " TEXT,"
                + USER_COLUMN_PASSCODE + " TEXT" + ")";
        sqLiteDatabase.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + USER_TABLE_ROLE);
        onCreate(sqLiteDatabase);
    }

    public void addDetails(SqlDataClass data) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_COLUMN_NAME,data.getName());
        values.put(USER_COLUMN_ID, data.getUserId());
        values.put(USER_COLUMN_PASSCODE, data.getPassCode());
        //values.put(USER_COLUMN_ID, data.getRole());

        db.insert(USER_TABLE_ROLE,null, values);
        db.close();
        //Toast.makeText(this, "The new Row Id is " + newRowId, Toast.LENGTH_LONG).show();

    }
}
