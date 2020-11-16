package ir.zroid.facerecognition.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.List;

import ir.zroid.facerecognition.face.resources.ZRoidFile;

public class PrepairDatabase extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "facedance.db";//"marzbani.db";
    private static final int DATABASE_VERSION = 1;

    public PrepairDatabase(Context context) {
        super(context, DATABASE_NAME, ZRoidFile.DATABASE_BACKUP, null, DATABASE_VERSION);


    }

    public List<String> getListOfRow(String query, String columnName) {
        SQLiteDatabase sql = this.getWritableDatabase();
        Cursor c = sql.rawQuery(query, null);
        final List<String> list = new ArrayList<String>();
        try {
            while(c.moveToNext()) {
                list.add(c.getString(c.getColumnIndex(columnName)));
            }
        } finally {
            c.close();
        }
        return list;
    }

    public String getString(String query, String columnName) {
        //query mesle : "SELECT * FROM data"
        SQLiteDatabase sql = this.getWritableDatabase();
        Cursor c = sql.rawQuery(query, null);
        c.moveToFirst();
        return c.getString(c.getColumnIndex(columnName));

    }
    public int getInt(String query, String columnName) {
        //query mesle : "SELECT * FROM data"
        SQLiteDatabase sql = this.getWritableDatabase();
        Cursor c = sql.rawQuery(query, null);
        c.moveToFirst();
        return c.getInt(c.getColumnIndex(columnName));
    }
    public void Query(String query) {
        SQLiteDatabase sql = this.getWritableDatabase();
        sql.execSQL(query);
    }
    public byte[] getBlob(String query, String columnName) {
        SQLiteDatabase sql = this.getWritableDatabase();
        Cursor c = sql.rawQuery(query, null);
       return c.getBlob(c.getColumnIndex(columnName));
    }
public int getRowCount(String query){
    SQLiteDatabase sql = this.getWritableDatabase();
    Cursor c = sql.rawQuery(query, null);
    return c.getCount();
}
    public int getColumnCount(String query){
        SQLiteDatabase sql = this.getWritableDatabase();
        Cursor c = sql.rawQuery(query, null);
        return c.getColumnCount();
    }



}