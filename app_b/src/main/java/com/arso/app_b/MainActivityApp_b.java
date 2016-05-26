package com.arso.app_b;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.arso.sqlitehandler.HistoryProvider;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class MainActivityApp_b extends AppCompatActivity {

    private static Activity thisAppActivity;
    private static long mMillisUntilFinished = 11 * 1000; // 10 секунд
    private static long mMillisUntilFinishedOnDelete = 16 * 1000; // 15 секунд
    private static long mMillisDefault = 1000;

    private String mId;
    private String mUrl;
    String mTestFolders = "/BIGDIG/test/B";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent i = getIntent();
        int mode = i.getIntExtra("mode", 3); // режим ввода информации по кнопке "open" - 1 или со списка - 2
        String ref = i.getStringExtra("url");
        String id = i.getStringExtra("id");
        String oldStatus = i.getStringExtra("status");

        if(mode == 1){ // открыли по кнопке
            super.onCreate(savedInstanceState);
            setContentView(R.layout.picture_viewer_app_b);

            DownloadImage downloadImage = new DownloadImage((ImageView) findViewById(R.id.imageView));
            downloadImage.execute(ref);

            HashMap<String, Object> pic = null;
            try {
                pic = downloadImage.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            insertData(ref, (Integer) pic.get("status"));
        }

        if(mode == 2){ // открыли из списка
            super.onCreate(savedInstanceState);
            setContentView(R.layout.picture_viewer_app_b);

            Cursor res = getContentResolver().query(HistoryProvider.CONTENT_URI, null, "id = " + id + "", null, null);
            res.moveToFirst();

            if (!res.isAfterLast()){
                res.moveToFirst();
                String url = res.getString(res.getColumnIndex(HistoryProvider.REF));

                DownloadImage downloadImage = new DownloadImage((ImageView) findViewById(R.id.imageView));
                downloadImage.execute(url);
                HashMap<String, Object> pic = null;
                try {
                    pic = downloadImage.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                if(Integer.valueOf(oldStatus) == 1){ // удаление ссылки со статусом "1" через 15 сек после открытия
                    thisAppActivity = this;
                    MyCountOnDelete timerCountOnDelete;
                    timerCountOnDelete = new MyCountOnDelete(mMillisUntilFinishedOnDelete, mMillisDefault);
                    timerCountOnDelete.setBm((Bitmap) pic.get("pic"));
                    timerCountOnDelete.setId(Integer.valueOf(id));
                    timerCountOnDelete.setUrl(url);
                    timerCountOnDelete.start();

                }else { // в противном случае апдейтим статусы по айдишнику записи
                    updateData(url, (Integer) pic.get("status"), Integer.valueOf(id));
                }
            }
        }

        if (mode == 3){
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main_activity_app_b);
            thisAppActivity = this;
            MyCount timerCount;
            timerCount = new MyCount(mMillisUntilFinished, mMillisDefault);
            timerCount.start();
        }
    }

    private void insertData(String ref, int status){
        ContentValues values = new ContentValues();
        values.put(HistoryProvider.REF, (ref));
        values.put(HistoryProvider.DATE, System.currentTimeMillis());
        values.put(HistoryProvider.STATUS, status);

        Uri uri = getContentResolver().insert(HistoryProvider.CONTENT_URI, values);
    }

    private void updateData(String ref, int status, int id){
        ContentValues values = new ContentValues();
        values.put(HistoryProvider.REF, ref);
        values.put(HistoryProvider.DATE, System.currentTimeMillis());
        values.put(HistoryProvider.STATUS, status);

        int rowsAffected = getContentResolver().update(HistoryProvider.CONTENT_URI, values, "id="+id, null);
//        if (rowsAffected == 0) {
//            Toast.makeText(getBaseContext(), "0 rows updated",
//                    Toast.LENGTH_LONG).show();
//        } else {
//            Toast.makeText(getBaseContext(), rowsAffected + " rows updated",
//                    Toast.LENGTH_LONG).show();
//        }
    }
    /*таймер по закрытию APP_B из лаунчера*/
    public class MyCount extends CountDownTimer {
        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            mMillisUntilFinished = 11 * mMillisDefault;
            thisAppActivity.finish();
        }

        @Override
        public void onTick(long millisUntilFinished) {

            mMillisUntilFinished = millisUntilFinished;
            int sec = (int) (millisUntilFinished / mMillisDefault);

            TextView txt = (TextView) findViewById(R.id.appTxt);
            String KEY_TEXT_VALUE = "This app can be open only from APP_A and it will close automaticaly in " + sec + " seconds.";
            txt.setText(KEY_TEXT_VALUE);
        }
    }
    /*таймер по закрытию APP_B с содержимым....хотя можно объединить все в один класс с внутренней проработкой каждого варианта*/
    public class MyCountOnDelete extends CountDownTimer {

        Bitmap bm;
        int id;
        String url;

        public MyCountOnDelete(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        void setBm(Bitmap bm){
            this.bm = bm;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public void onFinish() {
            mMillisUntilFinishedOnDelete = 16 * mMillisDefault;
            getContentResolver().delete(HistoryProvider.CONTENT_URI, "id = " + id + "", null);

            savePictureOnSDCARD(bm); // сохраняем на карту памяти удаляемую ссылку

            Toast.makeText(getBaseContext(), url + " - was deleted from list!",
                    Toast.LENGTH_SHORT).show();

            thisAppActivity.finish();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            mMillisUntilFinishedOnDelete = millisUntilFinished;
            int sec = (int) (millisUntilFinished / mMillisDefault);
        }
    }

    private void savePictureOnSDCARD(Bitmap bm) {
        File extStorageDirectory = Environment.getExternalStorageDirectory();

        File myDirectory = new File(extStorageDirectory, mTestFolders);

        if(!myDirectory.exists()) {
            myDirectory.mkdirs();
        }

        OutputStream outStream = null;
        String path = extStorageDirectory.toString() + mTestFolders;
        File file = new File(path, mId+".PNG");
        try {
            outStream = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();

            Toast.makeText(this, "Saved to: " + path, Toast.LENGTH_LONG).show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
