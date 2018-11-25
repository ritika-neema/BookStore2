package com.ritikaneema.bookstore2;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ritikaneema.bookstore2.BookContract.BookEntry;

public class BooksEditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    EditText mBookName;
    EditText mBookPrice;
    Button decrementBTN;
    TextView mBookQuantity;
    Button incrementBTN;
    EditText mSupplierName;
    EditText mSupplierPhone;
    Button mContactSupplier;


    private Uri mBookUri;
    private int quantity;
    private static final String LOG_TAG = BooksEditorActivity.class.getSimpleName();
    private static final int EXISTING_LOADER = 0;
    private String supplierPhone;


    private boolean mBookHasChanged = false;


    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mBookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books_editor);

        mBookName = findViewById(R.id.book_name_et);
        mBookPrice = findViewById(R.id.book_price_et);
        mBookQuantity = findViewById(R.id.book_quantity_tv);
        decrementBTN = findViewById(R.id.decr_btn);
        incrementBTN = findViewById(R.id.inc_btn);
        mSupplierName = findViewById(R.id.supplier_name_et);
        mSupplierPhone = findViewById(R.id.supplier_phone_et);
        mContactSupplier = findViewById(R.id.contact_supplier);


        Intent i = getIntent();
        mBookUri = i.getData();

        // If the intent DOES NOT contain a book content URI, then we know that we are
        // creating a new book.
        if (mBookUri == null) {
            // This is a new book, so change the app bar to say "Add a Book"
            setTitle(getString(R.string.add_book));

            // Invalidate the options menu, so the "Delete" and "Contact Supplier" menu option can be hidden.
            // (It doesn't make sense to delete a book or contact supplier that hasn't been created yet.)
            invalidateOptionsMenu();

            // Hiding the Button in Add Book as the supplier number hasn't been added yet
            mContactSupplier.setVisibility(View.GONE);

        } else {
            // Otherwise this is an existing book, so change app bar to say "Edit Book"
            setTitle(getString(R.string.edit_book));

            // OnClickListener for Call Button to call Supplier via an DIAL Intent
            mContactSupplier.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Parsing uri to supplier's phone number.
                    Uri phone = Uri.parse("tel:" + supplierPhone);

                    // Intent for Dialing
                    Intent phoneIntent = new Intent(Intent.ACTION_DIAL, phone);
                    // Verify that the intent will resolve to an activity
                    if (phoneIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(phoneIntent);
                    }
                }
            });

            // Initializing Loader Manager Callback for Activity
            getLoaderManager().initLoader(EXISTING_LOADER, null, this);
        }

        // OnClickListener for decrementing quantity by one.
        decrementBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity <= BookContract.MIN_LIMIT) {
                    Toast.makeText(BooksEditorActivity.this,
                            getString(R.string.decrement_quantity),
                            Toast.LENGTH_SHORT).show();
                } else {
                    quantity -= BookContract.ONE;
                    mBookQuantity.setText(String.valueOf(quantity));
                }
            }
        });

        // OnClickListener for incrementing quantity by one.
        incrementBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity >= BookContract.MAX_LIMIT) {
                    Toast.makeText(BooksEditorActivity.this,
                            getString(R.string.increment_quantity),
                            Toast.LENGTH_SHORT).show();
                } else {
                    quantity += BookContract.ONE;
                    mBookQuantity.setText(String.valueOf(quantity));
                }
            }
        });

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mBookName.setOnTouchListener(mTouchListener);
        mBookPrice.setOnTouchListener(mTouchListener);
        incrementBTN.setOnTouchListener(mTouchListener);
        decrementBTN.setOnTouchListener(mTouchListener);
        mSupplierName.setOnTouchListener(mTouchListener);
        mSupplierPhone.setOnTouchListener(mTouchListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.books_editor_menu, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mBookUri == null) {
            MenuItem deleteMenu = menu.findItem(R.id.delete_single_record);
            deleteMenu.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.save:
                saveBook();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.delete_single_record:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(BooksEditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{BookEntry._ID,
                BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_BOOK_SUPPLIER_NAME,
                BookEntry.COLUMN_BOOK_SUPPLIER_PHONE_NO};

        Log.i(LOG_TAG, getString(R.string.log_onCreateLoader));
        return new CursorLoader(this, mBookUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME));
            int priceInt = cursor.getInt(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE));
            String priceString = String.valueOf(priceInt);
            int quantityInt = cursor.getInt(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY));
            String quantityString = String.valueOf(quantityInt);

            // Setting quantity when item is retrieved from list
            quantity = Integer.parseInt(quantityString);

            String supplierName = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_NAME));
            supplierPhone = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_PHONE_NO));
            // Setting Text onto Views
            mBookName.setText(name);
            mBookPrice.setText(priceString);
            mBookQuantity.setText(quantityString);
            mSupplierName.setText(supplierName);
            mSupplierPhone.setText(supplierPhone);

            Log.i(LOG_TAG, getString(R.string.log_onLoadFinished));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(LOG_TAG, getString(R.string.log_onLoaderReset));
        mBookName.setText(getString(R.string.book_name_hint));
        mBookPrice.setText(getString(R.string.price_hint));
        mBookQuantity.setText(getString(R.string.quantity_int));
        mSupplierName.setText(getString(R.string.supplier_hint));
        mSupplierPhone.setText(getString(R.string.supplier_phone_hint));
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the Book hasn't changed, continue with handling back button press
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    private void saveBook() {
        String nameString = mBookName.getText().toString().trim();
        String priceString = mBookPrice.getText().toString().trim();
        String supplierNameString = mSupplierName.getText().toString().trim();
        String supplierPhoneString = mSupplierPhone.getText().toString().trim();

        if (TextUtils.isEmpty(nameString)) {
            mBookName.setError(getString(R.string.required));
            return;
        }

        if (TextUtils.isEmpty(priceString)) {
            mBookPrice.setError(getString(R.string.required));
            return;
        }
        if (TextUtils.isEmpty(supplierNameString)) {
            mSupplierName.setError(getString(R.string.required));
            return;
        }
        if (TextUtils.isEmpty(supplierPhoneString)) {
            mSupplierPhone.setError(getString(R.string.required));
            return;
        }

        int priceInt = Integer.parseInt(priceString);

        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_BOOK_NAME, nameString);
        values.put(BookEntry.COLUMN_BOOK_PRICE, priceInt);
        values.put(BookEntry.COLUMN_BOOK_QUANTITY, quantity);
        values.put(BookEntry.COLUMN_BOOK_SUPPLIER_NAME, supplierNameString);
        values.put(BookEntry.COLUMN_BOOK_SUPPLIER_PHONE_NO, supplierPhoneString);

        Log.i(LOG_TAG, getString(R.string.log_saveBook));
        if (mBookUri == null) {
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);
            if (newUri == null) {
                 Toast.makeText(this, getString(R.string.insert_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
               Toast.makeText(this, getString(R.string.insert_book_success),
                        Toast.LENGTH_SHORT).show();
            }
            finish();
        } else {
            int rowsAffected = getContentResolver().update(mBookUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_successful), Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete_single_record, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void deleteBook() {
         if (mBookUri != null) {
            int rowsDeleted = getContentResolver().delete(mBookUri, null, null);
           if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.delete_book_fail),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.delete_book_success),
                        Toast.LENGTH_SHORT).show();
            }
        }
        Log.i(LOG_TAG, getString(R.string.log_deleteBook));
        finish();
    }
}
