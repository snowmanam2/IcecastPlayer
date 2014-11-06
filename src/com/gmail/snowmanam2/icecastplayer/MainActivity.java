package com.gmail.snowmanam2.icecastplayer;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView tv;
	private long downloadID;
	IcecastDatabase db;
	
	private BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
	    	if (id == downloadID) {
	    		DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
	    		DownloadManager.Query query = new DownloadManager.Query();
	    		query.setFilterById(id);
	    		Cursor cursor = downloadManager.query(query);

	    		// it shouldn't be empty, but just in case
	    		if (!cursor.moveToFirst()) {
	    		    Log.e("Icecast Player", "Empty row");
	    		    return;
	    		}

	    		int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
	    		if (DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusIndex)) {
	    		    Log.w("Icecast Player", "Download Failed");
	    		    return;
	    		}

	    		int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
	    		String downloadedFileUriString = cursor.getString(uriIndex);

	    		onDownloadCompleted(downloadedFileUriString);
	    	}
	    }
	};


	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		tv = (TextView) findViewById(R.id.textView1);
		
		String downloadCompleteIntentName = DownloadManager.ACTION_DOWNLOAD_COMPLETE;
		IntentFilter downloadCompleteIntentFilter = new IntentFilter(downloadCompleteIntentName);
		getApplicationContext().registerReceiver(downloadCompleteReceiver, downloadCompleteIntentFilter);
		this.db = new IcecastDatabase(getApplicationContext());
		
		tv.setText(db.getNumStations() + " stations in database.");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void startDownload (View view){
		tv.setText("Downloading file...");
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://dir.xiph.org/yp.xml"));
		request.setDescription("Icecast data file");
		request.setTitle("Icecast");
		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "yp.xml");
		
		DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		
		downloadID = manager.enqueue(request);
	}

	public void onDownloadCompleted (String uri) {
		tv.setText ("Downloaded file.\nBuilding Database...");
		
		InputStream in;
		try {
			in = getContentResolver().openInputStream(Uri.parse(uri));
		} catch (FileNotFoundException e) {
			Log.w("Icecast Player", "Couldn't open downloaded file");
			return;
		}

		IcecastXmlParser parser = new IcecastXmlParser();
		
		try {
			parser.parse(in, this.db);
		} catch (XmlPullParserException e) {
			tv.setText("Parsing Error");
			return;
		} catch (IOException e) {
			tv.setText("IO Error");
		}
		//tv.setText("Done.");
		tv.setText("Done. " + db.getNumStations() +" stations in database.");
	}
	
	public void purgeDatabase (View view) {
		db.purge();
		
		tv.setText(db.getNumStations() + " stations in database.");
	}
	
}
