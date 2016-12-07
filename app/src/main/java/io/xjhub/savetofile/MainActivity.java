package io.xjhub.savetofile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MainActivity";
    private static final int REQUEST_CODE = 43;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Do not call setContentView if activity is transparent
        // setContentView(R.layout.activity_main);

        // Get intent, action
        Intent intent = getIntent();
        String action = intent.getAction();

        if (Intent.ACTION_SEND.equals(action)) {
            handleSendFile(intent);
        } else {
            // Finish activity when started manually
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && resultData != null) {
                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                try {
                    Uri contentUri = Uri.parse(sharedPref.getString("contentUri", null));
                    Uri targetUri = resultData.getData();

                    copyFile(contentUri, targetUri);

                    Toast.makeText(this, R.string.success, Toast.LENGTH_LONG).show();

                } catch (NullPointerException | IOException e) {
                    Log.e(LOG_TAG, Log.getStackTraceString(e));
                    Toast.makeText(this, R.string.fail, Toast.LENGTH_LONG).show();
                }

                finish();
            }
        }
    }

    protected void handleSendFile(Intent intent) {
        Uri contentUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (contentUri != null) {
            // Save contentUri into shared preferences
            SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
            editor.putString("contentUri", contentUri.toString());
            editor.apply();

            // Create empty file
            Intent docIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            docIntent.addCategory(Intent.CATEGORY_OPENABLE);
            docIntent.setType(intent.getType());
            docIntent.putExtra(Intent.EXTRA_TITLE, getFileNameFromUri(contentUri));
            startActivityForResult(docIntent, REQUEST_CODE);
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;

        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        }

        return fileName;
    }

    private void copyFile(Uri inUri, Uri outUri) throws IOException {
        try (InputStream inputStream = getContentResolver().openInputStream(inUri);
             OutputStream outputStream = getContentResolver().openOutputStream(outUri))
        {
            if (inputStream != null && outputStream != null) {
                ByteStreams.copy(inputStream, outputStream);
            } else {
                throw new NullPointerException();
            }
        }
    }

}
