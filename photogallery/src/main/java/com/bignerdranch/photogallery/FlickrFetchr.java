package com.bignerdranch.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hongy_000 on 2017/9/16.
 */

public class FlickrFetchr {

    private static final String TAG = FlickrFetchr.class.getSimpleName();

    private static final String API_KEY = "aeef2a25da952db7325b46add2fb1208";

    public byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            InputStream inputStream = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = inputStream.read(buffer)) > 0) {
               outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            return outputStream.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchItems(){

        List<GalleryItem> items = new ArrayList<>();

        try {
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    .build().toString();
            String jsonString = getUrlString(url);
            Log.d(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);
        } catch (JSONException jsonException){
            Log.e(TAG, "Failed to parse json ", jsonException);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items ", ioe);
        }

        return items;
    }

    public void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws IOException, JSONException {

        JSONObject photoJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photoJsonObject.getJSONArray("photo");

        for (int i =0; i < photoJsonArray.length(); i++) {
            JSONObject object = photoJsonArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();
            item.setId(object.getString("id"));
            item.setCaption(object.getString("title"));

            if (!object.has("url_s")) {
                continue;
            }

            item.setUrl(object.getString("url_s"));
            items.add(item);
        }
    }
}