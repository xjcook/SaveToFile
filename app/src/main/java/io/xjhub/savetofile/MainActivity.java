package io.xjhub.savetofile;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int requestCode = 43;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendMultipleImages(intent); // Handle multiple images being sent
            }
        } else {
            // Handle other intents, such as being started from the home screen
        }
    }

    protected void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // Update UI to reflect text being shared
        }
    }

    protected void handleSendImage(Intent sentIntent) {
        Uri imageUri = (Uri) sentIntent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            // Update UI to reflect image being shared
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(sentIntent.getType());
            intent.putExtra(Intent.EXTRA_TITLE, getFileNameFromUri(imageUri));
            startActivityForResult(intent, requestCode);
        }
    }

    protected void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            // Update UI to reflect multiple images being shared
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

}
