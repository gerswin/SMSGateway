package com.parawebs.smsgateway;

/**
 * Created by gerswin on 19/08/14.
 */
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "smsgateway";

    // Contacts table name
    private static final String TABLE_CONTACTS = "count";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String COUNTER = "count";
    private static final String STATS = "status";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + COUNTER + " INTEGER,"
                + STATS + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);

    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new contact
    void createCounters(int counter, String status) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COUNTER, counter); // count
        values.put(STATS, status); // status

        // Inserting Row
        db.insert(TABLE_CONTACTS, null, values);
        db.close(); // Closing database connection
    }

    // Getting single contact
    Counter getCounter(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID,
                        COUNTER, STATS}, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        Counter counter=null;
        if (cursor != null)
            if (cursor.moveToFirst())
            {
                 counter = new Counter(Integer.parseInt(cursor.getString(0)),
                        cursor.getInt(1), cursor.getString(2));
            }
            else
            {
                 counter = new Counter(0,0, null);
            }


        // return contact
        return counter;
    }

    // Getting All Contacts
    public List<Counter> getAllContacts() {
        List<Counter> counterList = new ArrayList<Counter>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Counter counter = new Counter();
                counter.setID(Integer.parseInt(cursor.getString(0)));
                counter.setCount(cursor.getInt(1));
                counter.setStatus(cursor.getString(2));
                // Adding contact to list
                counterList.add(counter);
            } while (cursor.moveToNext());
        }

        // return contact list
        return counterList;
    }

    // Updating single contact
    public int updateContact(Counter counter) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COUNTER, counter.getCount());
        values.put(STATS, counter.getStatus());

        // updating row
        return db.update(TABLE_CONTACTS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(counter.getID()) });
    }

    public void plusCounter(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        // updating row
        //String q = "UPDATE count SET counter = counter + 1 WHERE id = ?";
        db.execSQL("UPDATE count SET count = count + 1 WHERE id = " + String.valueOf(id));
    }

    // Deleting single contact
    public void deleteContact(Counter counter) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
                new String[] { String.valueOf(counter.getID()) });
        db.close();
    }


    // Getting contacts Count
    public int getContactsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

}