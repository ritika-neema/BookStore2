package com.ritikaneema.bookstore2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ritikaneema.bookstore2.BookContract.BookEntry;

public class BookDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "books.db";
    private static final int DB_VERSION = 1;

    private static final String SQL_CREATE_BOOK_ENTRY =
            "CREATE TABLE " + BookEntry.TABLE_NAME + "("
                    + BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + BookEntry.COLUMN_BOOK_NAME + " TEXT NOT NULL, "
                    + BookEntry.COLUMN_BOOK_PRICE + " INTEGER NOT NULL, "
                    + BookEntry.COLUMN_BOOK_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                    + BookEntry.COLUMN_BOOK_SUPPLIER_NAME + " TEXT NOT NULL, "
                    + BookEntry.COLUMN_BOOK_SUPPLIER_PHONE_NO + " TEXT NOT NULL);";

    public BookDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_BOOK_ENTRY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
