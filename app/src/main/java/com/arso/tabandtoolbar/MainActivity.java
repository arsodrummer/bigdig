package com.arso.tabandtoolbar;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import com.arso.sqlitehandler.HistoryProvider;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private final int TEST_TAB = 1;
    private final int HISTORY_TAB = 2;
    private TabHost tabHost;
    private static ArrayList<HashMap<String, String>> mHistoryArray;
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initTabs();

        initButtonOpen();

        initButtonClear();

        initHistory(null);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        initHistory(null);
    }

    void openAPP_B(Intent i){
        ComponentName componentName = new ComponentName("com.arso.app_b", "com.arso.app_b.MainActivityApp_b");

        try{
            i.setComponent(componentName);
            startActivity(i);}
        catch (Exception e){
            Toast.makeText(getApplicationContext(), "APP_B doesn't install on your device!", Toast.LENGTH_SHORT).show();
        }

    }

    private void initButtonOpen(){
        Button bOpen = (Button) findViewById(R.id.buttonOpen);
        bOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url= ((TextView) findViewById(R.id.editTextURL)).getText().toString();

                Intent i = new Intent();
                i.setAction(Intent.ACTION_VIEW);
                i.addCategory(Intent.CATEGORY_DEFAULT);
                i.putExtra("mode", TEST_TAB);
                i.setType("text/plain");
                i.putExtra("url", url);
                i.setType("text/plain");

                openAPP_B(i);

            }
        });
    }

    private void initHistory( String order){
        HashMap<String, String> map;
        mHistoryArray = new ArrayList<HashMap<String, String>>();
        ArrayList<String> values = new ArrayList<String>();

        Cursor res = getContentResolver().query(HistoryProvider.CONTENT_URI, null, null, null, order);
        res.moveToFirst();

        while(!res.isAfterLast()){
            map = new HashMap<String, String>();
            map.put(HistoryProvider._ID,    res.getString(res.getColumnIndex(HistoryProvider._ID)));
            map.put(HistoryProvider.DATE,   res.getString(res.getColumnIndex(HistoryProvider.DATE)));
            map.put(HistoryProvider.REF,    res.getString(res.getColumnIndex(HistoryProvider.REF)));
            map.put(HistoryProvider.STATUS, res.getString(res.getColumnIndex(HistoryProvider.STATUS)));
            mHistoryArray.add(map);

            values.add(res.getString(res.getColumnIndex(HistoryProvider.REF)));

            res.moveToNext();
        }

        CustomHistoryList<String> customHistoryList = new CustomHistoryList<>(this, R.id.listViewHistory, R.id.textViewURL, values, mHistoryArray);
        lv = (ListView)findViewById(R.id.listViewHistory);
        lv.setAdapter(customHistoryList);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
                int id = arg2 + 1;

                Intent i = new Intent();
                i.setAction(Intent.ACTION_VIEW);
                i.addCategory(Intent.CATEGORY_LAUNCHER);
                i.putExtra("mode", HISTORY_TAB);
                i.setType("text/plain");
                i.putExtra("id", mHistoryArray.get(arg2).get(HistoryProvider._ID).toString());
                i.setType("text/plain");
                i.putExtra("status", mHistoryArray.get(arg2).get(HistoryProvider.STATUS).toString());
                i.setType("text/plain");

                openAPP_B(i);
            }
        });
    }

    private void initButtonClear(){
        Button bClear = (Button) findViewById(R.id.buttonClear);
        bClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextView) findViewById(R.id.editTextURL)).setText("");
            }
        });
    }

    private void initTabs(){
        tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();
        TabHost.TabSpec tabSpec;
        tabSpec = tabHost.newTabSpec("test");
        tabSpec.setIndicator("Test");
        tabSpec.setContent(R.id.test);
        tabHost.addTab(tabSpec);
        tabSpec = tabHost.newTabSpec("history");
        tabSpec.setIndicator("History");
        tabSpec.setContent(R.id.history);
        tabHost.addTab(tabSpec);
    }

    /* меню сортировки таблицы*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_sort_AZ_dateTime) {
            String order = "dateTime ASC";
            initHistory(order);

            return true;
        }

        if (id == R.id.action_sort_ZA_dateTime) {
            String order = "dateTime DESC";
            initHistory(order);
            return true;
        }

        if (id == R.id.action_sort_AZ_status) {
            String order = "status ASC";
            initHistory(order);

            return true;
        }

        if (id == R.id.action_sort_ZA_status) {
            String order = "status DESC";
            initHistory(order);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
