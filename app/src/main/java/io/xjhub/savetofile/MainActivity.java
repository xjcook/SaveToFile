package io.xjhub.savetofile;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private static final int requestCode = 43;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get intent, action
        Intent intent = getIntent();
        String action = intent.getAction();

        if (Intent.ACTION_SEND.equals(action)) {
            handleSendFile(intent);
        } else {
            finish();
        }
    }

    protected void handleSendFile(Intent intent) {
        Uri contentUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (contentUri != null) {
            Intent docIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            docIntent.addCategory(Intent.CATEGORY_OPENABLE);
            docIntent.setType(intent.getType());
            docIntent.putExtra(Intent.EXTRA_TITLE, getFileNameFromUri(contentUri));
            startActivityForResult(docIntent, requestCode);
        }
    }

    private String getFileNameFromUri(Uri contentUri) {
        String fileName = null;
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        } finally {
            cursor.close();
        }

        return fileName;
    }

}
