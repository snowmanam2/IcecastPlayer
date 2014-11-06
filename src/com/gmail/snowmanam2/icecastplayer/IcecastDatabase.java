package com.gmail.snowmanam2.icecastplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class IcecastDatabase extends SQLiteOpenHelper {
	// Database Version
	private static final int DATABASE_VERSION = 1;
	// Database Name
	private static final String DATABASE_NAME = "IcecastDB";

	private static final String TABLE_ICECAST = "icecast";
	
	private static final String KEY_NAME = "station_name";
	private static final String KEY_URL = "listen_url";
	private static final String KEY_GENRE = "genre";
	
	SQLiteDatabase writedb;
	
	public IcecastDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION); 
	}
 
	@Override
	public void onCreate(SQLiteDatabase db) {
		// SQL statement to create book table
		String CREATE_ICECAST_TABLE = "CREATE TABLE icecast ( " +
			KEY_NAME + " TEXT, " +
			KEY_URL + " TEXT PRIMARY KEY, "+
			KEY_GENRE + " TEXT )";
 
		// create Icecast table
		db.execSQL(CREATE_ICECAST_TABLE);
	}
 
	@Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older books table if existed
		db.execSQL("DROP TABLE IF EXISTS "+TABLE_ICECAST);

		// create fresh books table
		this.onCreate(db);
	}
	
	public void purge () {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ICECAST);
		
		this.onCreate(db);
		db.close();
	}
	
	public void beginTransaction (){
		this.writedb = this.getWritableDatabase();
		this.writedb.beginTransaction();
	}
	
	public void endTransaction () {
		this.writedb.setTransactionSuccessful();
		this.writedb.endTransaction();
		this.writedb.close();
	}
	
	public void addStation (Station station) {
		
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, station.getStationName());
		values.put(KEY_URL, station.getListenUrl());
		values.put(KEY_GENRE, station.getGenre());
		
		this.writedb.insertWithOnConflict(TABLE_ICECAST, null, values, SQLiteDatabase.CONFLICT_REPLACE);
	}
	
	public int getNumStations () {
		SQLiteDatabase db = this.getReadableDatabase();
		
		String query = "SELECT COUNT("+KEY_URL+") FROM " + TABLE_ICECAST;
		Cursor cursor = db.rawQuery(query, null);
		
		if(cursor.moveToFirst()){
			return cursor.getInt(0);
		} else {
			return 0;
		}
	}
	
}