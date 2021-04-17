package com.example.photogallery.Http;

import android.os.AsyncTask;

import com.example.photogallery.Http.AsyncResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequest extends AsyncTask<String, Void, String> {

    //private static final String TAG = "HttpRequest";
    public AsyncResponse delegate = null;

    @Override
    protected String doInBackground(String... strings) {

        HttpURLConnection conn = null;
        StringBuffer response = new StringBuffer();
        try {
            //set up the connection
            URL url = new URL(strings[0].toString());
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
                //Log.e(TAG, "I got an error", e);
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
                //Log.d(TAG, "Bad response status code:" + responseCode + response.toString());

                return "Fail";
            }

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