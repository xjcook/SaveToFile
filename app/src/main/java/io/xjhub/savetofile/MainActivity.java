package io.xjhub.savetofile;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
    private static final int REQUEST_CODE = 41;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 51;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Do not call setContentView if activity is transparent
        // setContentView(R.layout.activity_main);

        // Get intent, action
        Intent intent = getIntent();
        String action = intent.getAction();

        if (Intent.ACTION_SEND.equals(action)) {
            Uri contentUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (contentUri != null && "file".equals(contentUri.getScheme())) {
                // Check & request permission when file scheme
                int permissionCheck = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE);

                if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_READ_EXTERNAL_STORAGE);
                } else {
                    // Permission is already granted
                    handleSendFile(intent);
                }
            } else {
                // Content scheme does not need to check permission
                handleSendFile(intent);
            }
        } else {
            // Finish activity when started manually
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // We have permission to read file
                    handleSendFile(getIntent());
                } else {
                    // Finish activity when permission was denied
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && resultData != null) {
                new CopyFileTask().execute(resultData.getData());
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

    private class CopyFileTask extends AsyncTask<Uri, Void, Void> {

        @Override
        protected Void doInBackground(Uri... uris) {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            try {
                Uri contentUri = Uri.parse(sharedPref.getString("contentUri", null));
                Uri targetUri = uris.length > 0 ? uris[0] : null;
                copyFile(contentUri, targetUri);

            } catch (NullPointerException | IOException e) {
                cancel(true);
                Log.e(LOG_TAG, Log.getStackTraceString(e));
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(MainActivity.this, R.string.success, Toast.LENGTH_LONG).show();
            finish();
        }

        @Override
        protected void onCancelled(Void aVoid) {
            Toast.makeText(MainActivity.this, R.string.fail, Toast.LENGTH_LONG).show();
            finish();
        }
    }

}
