package com.ritikaneema.bookstore2;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class BookContract {
    public static final String CONTENT_AUTHORITY = "com.ritikaneema.bookstore2";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" +CONTENT_AUTHORITY);
    public static final String PATH_BOOKS = "books";

    public static final int MIN_LIMIT = 0;
    public static final int MAX_LIMIT = 500;
    public static final int ONE = 1;

    private BookContract() {
    }

    public static final class BookEntry implements BaseColumns {

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        public static final String TABLE_NAME = "books";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_BOOK_NAME = "product_name";
        public static final String COLUMN_BOOK_PRICE = "price";
        public static final String COLUMN_BOOK_QUANTITY = "quantity";
        public static final String COLUMN_BOOK_SUPPLIER_NAME = "supplier_name";
        public static final String COLUMN_BOOK_SUPPLIER_PHONE_NO = "supplier_phone";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

    }
}
