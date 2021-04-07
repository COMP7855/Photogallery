package com.example.photogallery;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class WeatherAPI extends AsyncTask<String, Void, String> {

    private static final String TAG = "GalleryActivityWeather";
    public AsyncResponse delegate = null;

    @Override
    protected String doInBackground(String... strings) {
        String apiEndPoint = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/";
        String location = "Langley, BC, CA";
        String startDate = "2021-4-7"; //optional
        String unitGroup = "metric"; //us,metric,uk
        String apiKey = "F3SHHHG8JTW3X9YECHGC9SFR7"; //sign up for a free api key at https://www.visualcrossing.com/weather/weather-data-services

        String method = "GET"; // GET OR POST

        //Build the URL pieces
        StringBuilder requestBuilder = new StringBuilder(apiEndPoint);
        requestBuilder.append(location);

        if (startDate != null && !startDate.isEmpty()) {
            requestBuilder.append("/").append(startDate);
        }

        //Build the parameters to send via GET or POST
        StringBuilder paramBuilder = new StringBuilder();
        paramBuilder.append("&").append("unitGroup=").append(unitGroup);
        paramBuilder.append("&").append("key=").append(apiKey);

        // add the parameters to the request
        requestBuilder.append("?").append(paramBuilder);

        HttpURLConnection conn = null;
        StringBuffer response = new StringBuffer();
        try {
            //set up the connection
            URL url = new URL(requestBuilder.toString());
            //URL url = new URL("http://www.android.com/");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            //check the response code and set up the reader for the appropriate stream
            int responseCode = 0;
            boolean isSuccess = false;
            try {
                responseCode = conn.getResponseCode();
                isSuccess = responseCode == 200;
            } catch (Exception e) {
                Log.e(TAG, "I got an error", e);
            }


            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(isSuccess ? conn.getInputStream() : conn.getErrorStream()))
            ) {

                //read the response
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            }
            if (!isSuccess) {
                Log.d(TAG, "Bad response status code:" + responseCode + response.toString());

                return "Fail";
            }

            //pass the string response to be parsed and used
            //parseWeatherDataJson(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return response.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }
}