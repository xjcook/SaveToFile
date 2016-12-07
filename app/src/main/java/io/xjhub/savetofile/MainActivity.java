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
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && resultData != null) {
                // Get contentUri from shared preferences
                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                Uri contentUri = null;
                try {
                    contentUri = Uri.parse(sharedPref.getString("contentUri", null));
                } catch (NullPointerException e) {
                    // TODO Meaningful error message to user
                    Log.e(LOG_TAG, Log.getStackTraceString(e));
                }

                // Copy file
                Uri targetUri = resultData.getData();
                try {
                    copyFile(contentUri, targetUri);
                } catch (IOException e) {
                    // TODO Meaningful error message to user
                    Log.e(LOG_TAG, Log.getStackTraceString(e));
                }

                // Inform user
                Toast.makeText(this, R.string.success, Toast.LENGTH_LONG).show();
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
        Cursor cursor = getContentResolver().query(uri, null, null, null, null, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        } finally {
            cursor.close();
        }

        return fileName;
    }

    private void copyFile(Uri inUri, Uri outUri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(inUri);
        OutputStream outputStream = getContentResolver().openOutputStream(outUri);

        try {
            ByteStreams.copy(inputStream, outputStream);
        } finally {
            outputStream.close();
            inputStream.close();
        }
    }


}
