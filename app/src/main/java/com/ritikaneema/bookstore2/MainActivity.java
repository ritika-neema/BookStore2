package com.ritikaneema.bookstore2;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.ritikaneema.bookstore2.BookContract.BookEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    ListView listView;
    View dummyView;
    FloatingActionButton fab;

    private static final int BOOK_LOADER = 0;
    private BookCursorAdapter mBookCursorAdapter;

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.list_view);
        dummyView = findViewById(R.id.dummy_view);
        fab = findViewById(R.id.fab);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BooksEditorActivity.class);
                startActivity(intent);
            }
        });

        listView.setEmptyView(dummyView);

        mBookCursorAdapter = new BookCursorAdapter(this, null);
        listView.setAdapter(mBookCursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(MainActivity.this, BooksEditorActivity.class);

                Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);
                i.setData(currentBookUri);
                startActivity(i);
            }
        });
        getSupportLoaderManager().initLoader(BOOK_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_all_records:
                deleteAllPets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(BookEntry.CONTENT_URI, null, null);

        if (rowsDeleted == 0) {
            Toast.makeText(this, R.string.error_while_deleting_books,
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.all_books_deleted,
                    Toast.LENGTH_SHORT).show();
        }
        Log.v(LOG_TAG, getString(R.string.deleted_rows) + rowsDeleted);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        String[] projection = {BookEntry._ID,
                BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_QUANTITY};

        Log.i(LOG_TAG, getString(R.string.log_onCreateLoader));

        return new CursorLoader(this, BookEntry.CONTENT_URI, projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        Log.i(LOG_TAG, getString(R.string.log_onLoadFinished));
       mBookCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        Log.i(LOG_TAG, getString(R.string.log_onLoaderReset));
       mBookCursorAdapter.swapCursor(null);
    }
}
