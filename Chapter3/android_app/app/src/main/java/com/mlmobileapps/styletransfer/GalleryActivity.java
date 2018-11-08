package com.mlmobileapps.styletransfer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class GalleryActivity extends AppCompatActivity {


    private File mFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        mFile = new File(getExternalFilesDir(null), "pic.jpg");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());

                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);

                // Get the dimensions of the bitmap
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();

                // Decode the image file into a Bitmap sized to fill the View
                //bmOptions.inJustDecodeBounds = false;
                bmOptions.inMutable = true;

                Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length, bmOptions);
                ResultHolder.dispose();
                ResultHolder.setImage(bitmap);
                Intent intent = new Intent(getApplicationContext(), ResultImageActivity.class);
                startActivity(intent);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Now you can do whatever you want with your inpustream, save it as file, upload to a server, decode a bitmap...
        }
        finish();
    }
}
