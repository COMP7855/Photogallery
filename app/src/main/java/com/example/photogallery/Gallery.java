package com.example.photogallery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.location.Location;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;


public class Gallery extends AppCompatActivity {
    public static final int SEARCH_ACTIVITY_REQUEST_CODE = 10;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String mCurrentPhotoPath;
    private ArrayList<String> photos = null;
    private int index = 0;
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

    // upon signing in to photogallery
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        tvLatitude = (TextView) findViewById(R.id.latitude);
        tvLongitude = (TextView) findViewById(R.id.longitude);

        // update list of photos
        photos = findPhotos(new Date(Long.MIN_VALUE), new Date(), "");
        if (photos.size() == 0) {
            displayPhoto(null);
        } else {
            // display the first photo on the list
            displayPhoto(photos.get(index));
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //check permission

    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                //Initialize location
                    Location location = task.getResult();
                    if (location != null) {
                        tvLatitude.setText(String.valueOf(location.getLatitude()));
                        tvLongitude.setText(String.valueOf(location.getLongitude()));
                    }
                }
        });
    }

    //when the "Search" button is pressed
   // public void onButtonClick_search(View v) {
       // Intent myIntent = new Intent(getBaseContext(), Search.class);
       // startActivity(myIntent);
    //}

     public void onButtonClick_search(View v) {

     Intent i = new Intent(Gallery.this, Search.class);

     startActivityForResult(i, SEARCH_ACTIVITY_REQUEST_CODE);

    }

    public void onButtonClick_share(View v) {
        share_Image(photos.get(index));
    }

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

                if (((startTimestamp == null && endTimestamp == null) || (f.lastModified() >= startTimestamp.getTime()
                        && f.lastModified() <= endTimestamp.getTime())
                ) && (keywords == "" || f.getPath().contains(keywords))
                        && (latitude >= latMin)
                        && (latitude <= latMax)
                        && (longitude >= longMin)
                        && (longitude <= longMax)
                )
                    photos.add(f.getPath());
            }
        }
        return photos;
    }



    // when the "Left" or "Right" buttons are pressed
    public void scrollPhotos(View v) {
        // update current photo's filename using caption text
        updatePhoto(photos.get(index),
                ((EditText) findViewById(R.id.editTextCaption)).getText().toString(),
                tvLatitude.getText().toString(),
                tvLongitude.getText().toString()
        );
        // update list of photos to have the correct filenames
        if (startTimestamp == null){
            photos = findPhotos(new Date(Long.MIN_VALUE), new Date(), "");  // JP
        }
        else {
            photos = findPhotos(startTimestamp, endTimestamp,keywords);  // JP
        }

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


    // creates an image file with a temporary name
    // called from "Snap" button function onButtonClick_camera
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "_caption_" + timeStamp + "_latitude_" + "_longitude_" + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // when a picture has been taken in the camera app
    // or when a search has been made
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // search
        if (requestCode == SEARCH_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //Date startTimestamp , endTimestamp;
                try {
                    String from = (String) data.getStringExtra("STARTTIMESTAMP");
                    String to = (String) data.getStringExtra("ENDTIMESTAMP");
                    startTimestamp = format.parse(from);
                    endTimestamp = format.parse(to);
                } catch (Exception ex) {
                    startTimestamp = null;
                    endTimestamp = null;
                }
                keywords = (String) data.getStringExtra("KEYWORDS"); //JP

                //DecimalFormat formatLongLat = new DecimalFormat("0.0000");
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
                    latMin = null;
                    latMax = null;
                    longMin = null;
                    longMax = null;
                }
                index = 0;
                photos = findPhotos(startTimestamp, endTimestamp, keywords);
                if (photos.size() == 0) {
                    displayPhoto(null);
                } else {
                    displayPhoto(photos.get(index));
                }
            }
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //ImageView mImageView = (ImageView) findViewById(R.id.imageViewPic);
            //mImageView.setImageBitmap(BitmapFactory.decodeFile(mCurrentPhotoPath));
            photos = findPhotos(new Date(Long.MIN_VALUE), new Date(), "");
            index = photos.size()-1; // JP
            displayPhoto(photos.get(index)); // JP

            if (ActivityCompat.checkSelfPermission(Gallery.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //When permission granted
                getLocation();
            } else {
                ActivityCompat.requestPermissions(Gallery.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
            }
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

}