package com.arso.app_b;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import java.io.InputStream;
import java.util.HashMap;

public class DownloadImage extends AsyncTask<String, Void, HashMap<String, Object>> {
    ImageView bmImage;
    Integer status = 3;
    HashMap<String, Object> map;

    public DownloadImage(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected HashMap<String, Object> doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        map = new HashMap<>();

        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
            status = 1;
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            status = 2; // "protocol not found" либо слишком большой файл, либо отсутствие инета
            e.printStackTrace();
        }
        map.put("pic", mIcon11);
        map.put("status", status);
        return map;
    }

    protected void onPostExecute( HashMap<String, Object> result) {
        bmImage.setImageBitmap((Bitmap) result.get("pic"));
        result.put("status", status);
    }
}
