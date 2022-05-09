package com.messagelogix.anonymousalerts.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.messagelogix.anonymousalerts.crypto.RSA;
import com.messagelogix.anonymousalerts.model.Contact;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the helper db class to manage database creation and contact management.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "contactManager",
            TABLE_CONTACTS = "contacts",
            KEY_ID = "id",
            KEY_NAME = "name",
            KEY_PHONE = "phone",
            KEY_EMAIL = "email";
    private static final String LOG_TAG = DatabaseHandler.class.getSimpleName();

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CONTACTS
                + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_NAME + " TEXT, " + KEY_PHONE + " TEXT, " + KEY_EMAIL + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        onCreate(db);
    }

    /**
     * Create a new contact in the database
     * @param contact
     */
    public void createContact(Contact contact) {
        SQLiteDatabase db = getWritableDatabase();

        Log.d(LOG_TAG, "contact = " + contact.toString());

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, Strings.isNullOrEmpty(contact.getName()) ? "" : RSA.encryptWithStoredKey(contact.getName()));
        values.put(KEY_PHONE,Strings.isNullOrEmpty(contact.getPhoneNumber()) ? "" : RSA.encryptWithStoredKey(contact.getPhoneNumber()));
        values.put(KEY_EMAIL,Strings.isNullOrEmpty(contact.getEmail()) ? "" : RSA.encryptWithStoredKey(contact.getEmail()));

        db.insert(TABLE_CONTACTS, null, values);
        db.close();
    }

    /***
     * Get the contact by the given id
     * @param id
     * @return
     */
    public Contact getContact(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACTS, new String[]{KEY_ID, KEY_NAME, KEY_PHONE, KEY_EMAIL}, KEY_ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        int contactId  = Integer.parseInt(cursor.getString(0));
        String name = Strings.isNullOrEmpty(cursor.getString(1)) ? "" : RSA.decryptWithStoredKey(cursor.getString(1));
        String phone = Strings.isNullOrEmpty(cursor.getString(2)) ? "" : RSA.decryptWithStoredKey(cursor.getString(2));
        String email = Strings.isNullOrEmpty(cursor.getString(3)) ? "" : RSA.decryptWithStoredKey(cursor.getString(3));
        Contact contact = new Contact(contactId, name, phone, email);
        cursor.close();
        db.close();
        return contact;
    }


    /**
     * Query the database and return a list of contact
     * @return
     */
    public List<Contact> getAllContacts() {
        List<Contact> contacts = new ArrayList<Contact>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_CONTACTS, new String[]{KEY_ID, KEY_NAME, KEY_PHONE, KEY_EMAIL}, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                int id  = Integer.parseInt(cursor.getString(0));
                Log.d(LOG_TAG, "phone =  " + cursor.getString(2) + " email = " + cursor.getString(3));
                String name = Strings.isNullOrEmpty(cursor.getString(1)) ? "" : RSA.decryptWithStoredKey(cursor.getString(1));
                String phone = Strings.isNullOrEmpty(cursor.getString(2)) ? "" : RSA.decryptWithStoredKey(cursor.getString(2));
                String email = Strings.isNullOrEmpty(cursor.getString(3)) ? "" : RSA.decryptWithStoredKey(cursor.getString(3));
                Contact contact = new Contact(id, name, phone, email);
                contacts.add(contact);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return contacts;
    }

    /**
     * Deletes the contact
     * @param contact
     */
    public void deleteContact(Contact contact) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + "=?", new String[]{String.valueOf(contact.getId())});
        db.close();
    }

    /**
     * Gets the count of contact
     * @return
     */
    public int getContactsCount() {
        int count;
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(TABLE_CONTACTS, new String[]{KEY_ID, KEY_NAME, KEY_PHONE, KEY_EMAIL}, null, null, null, null, null, null);
            count = cursor.getCount();
            cursor.close();
            db.close();
        } catch (Exception e) {
            count = 0;
        }
        return count;
    }

    /**
     * Updates the contact
     * @param contact
     * @return
     */
    public int updateContact(Contact contact) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, Strings.isNullOrEmpty(contact.getName()) ? "" : RSA.encryptWithStoredKey(contact.getName()));
        values.put(KEY_PHONE,Strings.isNullOrEmpty(contact.getPhoneNumber()) ? "" :RSA.encryptWithStoredKey(contact.getPhoneNumber()));
        values.put(KEY_EMAIL,Strings.isNullOrEmpty(contact.getEmail()) ? "" : RSA.encryptWithStoredKey(contact.getEmail()));

        return db.update(TABLE_CONTACTS, values, KEY_ID + "=?", new String[]{String.valueOf(contact.getId())});
    }

}
