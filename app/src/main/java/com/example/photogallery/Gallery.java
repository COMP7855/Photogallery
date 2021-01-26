package com.example.photogallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class Gallery extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    String mCurrentPhotoPath;
    private ArrayList<String> photos = null;
    private int index = 0;

    // upon signing in to photogallery
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        // update list of photos
        photos = findPhotos();
        if (photos.size() == 0) {
            displayPhoto(null);
        } else {
            // display the first photo on the list
            displayPhoto(photos.get(index));
        }
    }

    // when the "Search" button is pressed
    public void onButtonClick_search(View v) {
        Intent myIntent = new Intent(getBaseContext(), Search.class);
        startActivity(myIntent);
    }

    // when the "Snap" button is pressed
    public void onButtonClick_camera(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.photogallery.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    // update list of photos
    private ArrayList<String> findPhotos() {
        // get filepath for Pictures folder
        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), "/Android/data/com.example.photogallery/files/Pictures");
        // create an empty list variable
        ArrayList<String> photos = new ArrayList<String>();
        // create an array of files located in the folder
        File[] fList = file.listFiles();
        if (fList != null) {
            // sort files (JP)
            Arrays.sort(fList, new Comparator<File>(){
                public int compare(File f1, File f2)
                {
                    return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                } });
            // for all files in the folder...
            for (File f : fList) {
                // add the filepath to the photos variable
                photos.add(f.getPath());
            }
        }
        return photos;
    }

    // when the "Left" or "Right" buttons are pressed
    public void scrollPhotos(View v) {
        // update current photo's filename using caption text
        updatePhoto(photos.get(index), ((EditText) findViewById(R.id.editTextCaption)).getText().toString());
        // update list of photos to have the correct filenames
        photos = findPhotos();  // JP
        // increase or decrease index depending on button press
        switch (v.getId()) {
            case R.id.buttonLeft:
                if (index > 0) {
                    index--;
                }
                break;
            case R.id.buttonRight:
                if (index < (photos.size()-1)) {
                index++;
            }
            break;
            default:
                break;
        }
        // display the picture with the current index value
        displayPhoto(photos.get(index));
    }

    // display the photo
    private void displayPhoto(String path) {
        ImageView iv = (ImageView) findViewById(R.id.imageViewPic);
        TextView tv = (TextView) findViewById(R.id.textViewTimeStamp);
        EditText et = (EditText) findViewById(R.id.editTextCaption);
        if (path == null || path =="") {
            iv.setImageResource(R.mipmap.ic_launcher);
            et.setText("");
            tv.setText("");
        } else {
            iv.setImageBitmap(BitmapFactory.decodeFile(path));
            String[] attr = path.split("_");
            et.setText(attr[1]);
            tv.setText(attr[2] + "_"+ attr [3]);
        }
    }

    // creates an image file with a temporary name
    // called from "Snap" button function onButtonClick_camera
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "_caption_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // when a picture has been taken in the camera app
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //ImageView mImageView = (ImageView) findViewById(R.id.imageViewPic);
            //mImageView.setImageBitmap(BitmapFactory.decodeFile(mCurrentPhotoPath));
            photos = findPhotos();
            index = photos.size()-1; // JP
            displayPhoto(photos.get(index)); // JP
        }
    }

    // when scrollPhotos runs ("Left or "Right" button pressed)
    // update the currently displayed pictures filename with the textbox caption
    private void updatePhoto(String path, String caption) {
        String[] attr = path.split("_");
        if (attr.length >= 3) {
            File to = new File(attr[0] + "_" + caption + "_" + attr[2] + "_" + attr[3]+ "_.jpg");
            //File to = new File(attr[0] + "_" + caption + "_" + attr[2] + "_" + attr[3]);
            File from = new File(path);
            from.renameTo(to);
        }
    }

}