package com.arso.sqlitehandler;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class HistoryProvider extends ContentProvider {
    static final String PROVIDER_NAME = "com.arso.sqlitehandler.HistoryProvider";
    static final String URL = "content://" + PROVIDER_NAME + "/history";
    public static final Uri CONTENT_URI = Uri.parse(URL);

    public static final String _ID = "id";
    public static final String REF = "url";
    public static final String STATUS = "status";
    public static final String DATE = "dateTime";

    static final int HISTORY = 1;
    static final int HISTORY_ID = 2;

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "history", HISTORY);
        uriMatcher.addURI(PROVIDER_NAME, "history/#", HISTORY_ID);
    }

    private SQLiteDatabase db;
    static final String HISTORY_TABLE_NAME = "history";
    public static final String DATABASE_NAME = "db.db";
    static final int DATABASE_VERSION = 1;
    static String CREATE_DB_TABLE = "CREATE TABLE IF NOT EXISTS `history` (" +
                                    "`id`	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                                    "`dateTime`	INTEGER NOT NULL," +
                                    "`url`	TEXT NOT NULL," +
                                    "`status`	INTEGER NOT NULL" +
                                    ")";

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " +  HISTORY_TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        db = dbHelper.getWritableDatabase();
        return (db == null)? false:true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        long rowID = db.insert(	HISTORY_TABLE_NAME, "", values);

        if (rowID > 0)
        {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(HISTORY_TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case HISTORY:
                break;

            case HISTORY_ID:
                qb.appendWhere( _ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (sortOrder == null || sortOrder == ""){
            sortOrder = REF;
        }
        Cursor c = qb.query(db,	projection,	selection, selectionArgs,null, null, sortOrder);

        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)){
            case HISTORY:
                count = db.delete(HISTORY_TABLE_NAME, selection, selectionArgs);
                break;

            case HISTORY_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete( HISTORY_TABLE_NAME, _ID +  " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)){
            case HISTORY:
                count = db.update(HISTORY_TABLE_NAME, values, selection, selectionArgs);
                break;

            case HISTORY_ID:
                count = db.update(HISTORY_TABLE_NAME, values, _ID + " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" +selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
