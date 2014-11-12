package com.gmail.snowmanam2.icecastplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

public class IcecastDatabase extends SQLiteOpenHelper {
	// Database Version
	private static final int DATABASE_VERSION = 1;
	// Database Name
	private static final String DATABASE_NAME = "IcecastDB";

	private static final String TABLE_ICECAST = "icecast";
	
	public static final String KEY_ID = "_id";
	public static final String KEY_NAME = "station_name";
	public static final String KEY_URL = "listen_url";
	public static final String KEY_GENRE = "genre";
	
	public static final String[] COLUMNS = {KEY_ID, KEY_NAME, KEY_URL, KEY_GENRE};
	
	SQLiteDatabase writedb;
	
	public IcecastDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION); 
	}
 
	@Override
	public void onCreate(SQLiteDatabase db) {
		// SQL statement to create book table
		String CREATE_ICECAST_TABLE = "CREATE TABLE "+TABLE_ICECAST+" ( " +
			KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			KEY_NAME + " TEXT, " +
			KEY_URL + " TEXT UNIQUE, "+
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
		//this.writedb.close();
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
	
	public Station getStationByUrl (String url) {
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor cursor = db.query(TABLE_ICECAST,
				COLUMNS,
				" "+KEY_URL+" ?",
				new String[]{url},
				null,
				null,
				null,
				null);
		
		if (cursor != null) cursor.moveToFirst();
		else return null;
		
		Station station = new Station();
		
		station.setStationName(cursor.getString(1));
		station.setListenUrl(cursor.getString(2));
		station.setGenre(cursor.getString(3));
		
		return station;
	}
	
	public Station getStationById (int id) {
SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor cursor = db.query(TABLE_ICECAST,
				COLUMNS,
				" "+KEY_ID+" ?",
				new String[]{String.valueOf(id)},
				null,
				null,
				null,
				null);
		
		if (cursor != null) cursor.moveToFirst();
		else return null;
		
		Station station = new Station();
		
		station.setStationName(cursor.getString(1));
		station.setListenUrl(cursor.getString(2));
		station.setGenre(cursor.getString(3));
		
		return station;
	}
	
	public Cursor getStationsByName (String input) {
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor cursor = db.query(TABLE_ICECAST,
				null,
				" "+KEY_NAME+" LIKE ?",
				new String[]{"%"+input+"%"},
				null,
				null,
				KEY_NAME,
				null);
		
		//db.close();
		return cursor;
		
	}
	
	public Cursor getStationsByNameISect (String[] inputs) {
		SQLiteDatabase db = this.getReadableDatabase();
		
		String[] vals = new String[inputs.length];
		
		String where = "";
		
		for (int i = 0; i < inputs.length; i++) {
			if (i > 0) where +=" AND ";
			where += KEY_NAME+" LIKE ?";
			vals[i] = "%"+inputs[i]+"%";
		}

		Cursor cursor = db.query(TABLE_ICECAST,
				null,
				where,
				vals,
				null,
				null,
				KEY_NAME,
				null);
		
		//db.close();
		return cursor;
	}
}