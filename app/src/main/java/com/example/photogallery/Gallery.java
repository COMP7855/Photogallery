package com.example.photogallery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.location.Location;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;


public class Gallery extends AppCompatActivity {
    public static final int SEARCH_ACTIVITY_REQUEST_CODE = 10;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String mCurrentPhotoPath;
    private ArrayList<String> photoPathList = null;
    private int photoIndex = 0;
    private Date startTimestamp = null;
    private Date endTimestamp = null; //JP
    private String keywords = null; //JP
    private FusedLocationProviderClient fusedLocationProviderClient;

    private TextView tvLatitude;
    private TextView tvLongitude;

    private Double latMin = -999.0;
    private Double latMax = 999.9;
    private Double longMin = -999.9;
    private Double longMax = 999.9;

    // upon entering gallery view
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        tvLatitude = (TextView) findViewById(R.id.latitude);
        tvLongitude = (TextView) findViewById(R.id.longitude);

        // update list of photos
        photoPathList = findPhotos(new Date(Long.MIN_VALUE), new Date(), "");
        if (photoPathList.size() == 0) {
            displayPhoto(null);
        } else {
            // display the first photo on the list
            displayPhoto(photoPathList.get(photoIndex));
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //check permission

    }

    // get last location and write it on the screen
    @SuppressLint("MissingPermission")
    private void getLocation() {
        // if location permission is granted
        if (checkPermissions())
        {
            // get last location
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>()
            {
                @Override
                public void onComplete(@NonNull Task<Location> task)
                {
                    //Initialize location
                    Location location = task.getResult();
                    // set textbox values to current longitude and latitude
                    if (location != null)
                    {
                        tvLatitude.setText(String.valueOf(location.getLatitude()));
                        tvLongitude.setText(String.valueOf(location.getLongitude()));
                    }
                }
            });

        }
    }

    // check location permissions
    private boolean checkPermissions()
    {
        if (ActivityCompat.checkSelfPermission
                (Gallery.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else
        {
            requestPermissions();
            return false;
        }
    }

    // request location permissions
    private void requestPermissions()
    {
        ActivityCompat.requestPermissions
                (Gallery.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        44);
    }

    // when search button is pressed, open Search Activity
    public void onButtonClick_search(View v) {

     Intent i = new Intent(Gallery.this, Search.class);

     startActivityForResult(i, SEARCH_ACTIVITY_REQUEST_CODE);

    }

    // when share button is pressed, share an image
    public void onButtonClick_share(View v) {
        share_Image(photoPathList.get(photoIndex));
    }

    // share the image at the passed filepath (called from onButtonClick_share)
    public void share_Image(String path) {
        //ImageView iv = (ImageView) findViewById(R.id.imageViewPic);
        //iv.setImageBitmap(BitmapFactory.decodeFile(path));
        File photoShare = new File(path);
        String send_to = "Hello";
        Uri photoURI = FileProvider.getUriForFile(this, "com.example.photogallery.fileprovider", photoShare);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, photoURI);
        shareIntent.setType("image/jpeg");
        //startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));

        startActivity(Intent.createChooser(shareIntent, send_to));

        //File photoShare = photos.get(index);
    }

    // when the "Snap" button is pressed, take a picture and store it
    public void onButtonClick_camera(View v) {
        // create intent for camera
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // if intent successfully created
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // initialize photo file variable
            File photoFile = null;
            try {
                // create the empty image file in the pictures directory with a temporary name
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                // request the camera takes a picture and store it in the photo file
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.photogallery.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    // update list of photos
    private ArrayList<String> findPhotos(Date startTimestamp, Date endTimestamp, String keywords) {
        // get filepath for Pictures folder
        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), "/Android/data/com.example.photogallery/files/Pictures");
        // create an empty list variable
        ArrayList<String> photos = new ArrayList<String>();
        // create an array of files located in the folder
        File[] fList = file.listFiles();

        if (fList != null) {
            Arrays.sort(fList, new Comparator<File>(){
                public int compare(File f1, File f2)
                {
                    return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                } });

            for (File f : fList) {
                String[] attr = f.getPath().split("_");
                Double latitude = Double.parseDouble(attr[4]);
                Double longitude = Double.parseDouble(attr[5]);

                if (
                        (// no timestamp given or
                        (startTimestamp == null && endTimestamp == null) ||
                                // within the date range specified
                                (f.lastModified() >= startTimestamp.getTime() && f.lastModified() <= endTimestamp.getTime())
                        ) // keywords is empty or the file contains the keywords from search
                                && (keywords == "" || f.getPath().contains(keywords))
                                //coordinates are within search area
                        && (latitude >= latMin)
                        && (latitude <= latMax)
                        && (longitude >= longMin)
                        && (longitude <= longMax)
                ) // add the photo to the list
                    photos.add(f.getPath());
            }
        }
        return photos;
    }

    // when the "Left" or "Right" buttons are pressed
    public void scrollPhotos(View v) {
        // update current photo's filename using caption text
        updatePhoto(photoPathList.get(photoIndex),
                ((EditText) findViewById(R.id.editTextCaption)).getText().toString(),
                tvLatitude.getText().toString(),
                tvLongitude.getText().toString()
        );
        // update list of photos to have the correct filenames
        if (startTimestamp == null){
            photoPathList = findPhotos(new Date(Long.MIN_VALUE), new Date(), "");  // JP
        }
        else {
            photoPathList = findPhotos(startTimestamp, endTimestamp,keywords);  // JP
        }

        // increase or decrease index depending on button press
        switch (v.getId()) {
            case R.id.buttonLeft:
                if (photoIndex > 0) {
                    photoIndex--;
                }
                break;
            case R.id.buttonRight:
                if (photoIndex < (photoPathList.size()-1)) {
                photoIndex++;
            }
            break;
            default:
                break;
        }
        // display the picture with the current index value
        displayPhoto(photoPathList.get(photoIndex));
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
            // set photo caption
            et.setText(attr[1]);
            // set photo timestamp
            tv.setText(attr[2] + "_" + attr [3]);
            if(attr.length >= 6)
            {
                tvLatitude.setText(attr[4]);
                tvLongitude.setText(attr[5]);
            }
            else
            {
                //tvLatitude.setText("Latitude");
                //tvLongitude.setText("Longitude");
            }
        }
    }

    // returns image file with a temporary name in the pictures directory
    // called from "Snap" button function onButtonClick_camera
    private File createImageFile() throws IOException {
        // Create an empty image file name with temporary caption, current time, and location
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "_caption_" + timeStamp + "_000_111_"; // 000 and 111 in place of long and lat
        // create the image file and store it the image file in the pictures directory
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // when a picture has been taken in the camera app
    // or when a search has been made
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // if a search was made
        if (requestCode == SEARCH_ACTIVITY_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                displaySearchResults(data);
            }
        }
        // if a photo was taken
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            // update list of photos
            photoPathList = findPhotos(new Date(Long.MIN_VALUE), new Date(), "");
            // display the most recent photo
            photoIndex = photoPathList.size()-1;
            displayPhoto(photoPathList.get(photoIndex));

            // get the current location
            getLocation();
        }
    }

    // display photos based on the search criteria
    private void displaySearchResults(Intent data)
    {
        // obtain time values from search
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            String from = (String) data.getStringExtra("STARTTIMESTAMP");
            String to = (String) data.getStringExtra("ENDTIMESTAMP");
            startTimestamp = format.parse(from);
            endTimestamp = format.parse(to);
        } catch (Exception ex) {
            startTimestamp = null;
            endTimestamp = null;
        }

        // obtain keywords from search
        keywords = (String) data.getStringExtra("KEYWORDS"); //JP

        // obtain location values from search
        try {
            String strLatMin = (String) data.getStringExtra("LATMIN");
            String strLatMax = (String) data.getStringExtra("LATMAX");
            String strLongMin = (String) data.getStringExtra("LONGMIN");
            String strLongMax = (String) data.getStringExtra("LONGMAX");
            latMin = Double.parseDouble(strLatMin);
            latMax = Double.parseDouble(strLatMax);
            longMin = Double.parseDouble(strLongMin);
            longMax = Double.parseDouble(strLongMax);
        } catch (Exception ex) {
            latMin = -999.0;
            latMax = 999.9;
            longMin = -999.9;
            longMax = 999.9;
        }

        // update list of photos based on search values
        photoPathList = findPhotos(startTimestamp, endTimestamp, keywords);
        photoIndex = 0;

        // if the list contains photos
        if (photoPathList.size() > 0)
        {
            // display the first photo in the list
            displayPhoto(photoPathList.get(photoIndex));
        }
        // if the list is empty
        else
        {
            // display default image
            displayPhoto(null);
        }
    }


    // when scrollPhotos runs ("Left or "Right" button pressed)
    // update the currently displayed pictures filename with the textbox caption
    private void updatePhoto(String path, String caption, String latitude, String longitude) {
        String[] attr = path.split("_");
        if (attr.length >= 6)
        {
            File to = new File(attr[0] + "_" + caption + "_" + attr[2] + "_" + attr[3]+ "_" +
                    latitude + "_" + longitude + "_.jpg");
            File from = new File(path);
            from.renameTo(to);
        }
        else if (attr.length >= 3)
        {
            File to = new File(attr[0] + "_" + caption + "_" + attr[2] + "_" + attr[3]+ "_.jpg");
            File from = new File(path);
            from.renameTo(to);
        }
    }

    public void onButtonClick_delete(View v)
    {

    }
}