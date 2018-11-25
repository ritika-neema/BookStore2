package com.ritikaneema.bookstore2;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ritikaneema.bookstore2.BookContract.BookEntry;

public class BookCursorAdapter extends CursorAdapter {
    private static final String LOG_TAG = BookCursorAdapter.class.getSimpleName();
    TextView bookName;
    TextView bookPrice;
    TextView bookQuantity;
    Button saleButton;


    public BookCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.i(LOG_TAG,context.getString(R.string.log_newView));
        return LayoutInflater.from(context).inflate(R.layout.book_list_item, parent, false);
    }


    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {


        bookName = view.findViewById(R.id.title_book_name);
        bookPrice = view.findViewById(R.id.book_price);
        bookQuantity = view.findViewById(R.id.book_quantity);
        saleButton = view.findViewById(R.id.sale_btn);


        final String name = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME));
        final int price = cursor.getInt(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE));

        String priceString = String.valueOf(price);
        final int quantityInt = cursor.getInt(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY));
        String quantityString = String.valueOf(quantityInt);

        int idColIndex = cursor.getColumnIndex(BookEntry._ID);

        final long idVal = Integer.parseInt(cursor.getString(idColIndex));

        bookName.setText(name);
        bookPrice.setText(priceString);
        bookQuantity.setText(quantityString);

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri newUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, idVal);

                int quantity = quantityInt - BookContract.ONE;
                if (quantity >= BookContract.MIN_LIMIT) {
                    ContentValues values = new ContentValues();
                    values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantity);
                    context.getContentResolver().update(newUri, values, null, null);
                    Log.i(LOG_TAG, context.getString(R.string.log_update_success));
                } else {
                    Toast.makeText(context, context.getString(R.string.sell_btn_toast), Toast.LENGTH_SHORT).show();
                    Log.e(LOG_TAG, context.getString(R.string.log_update_fail));
                }
            }
        });

    }
}
