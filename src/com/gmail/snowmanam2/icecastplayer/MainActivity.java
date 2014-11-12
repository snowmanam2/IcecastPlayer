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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private ListView lv;
	private EditText et;
	private TextView tv;
	private Button button;
	private long downloadID;
	private IcecastDatabase db;
	private SimpleCursorAdapter dataAdapter;
	private MediaPlayer mp;
	
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

	    		//onDownloadCompleted(downloadedFileUriString);
	    		BuildDatabaseTask task = new BuildDatabaseTask();
	    		task.execute(downloadedFileUriString);
	    	}
	    }
	};


	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		tv = (TextView) findViewById(R.id.textView1);
		button = (Button) findViewById(R.id.download_button);
		et = (EditText) findViewById(R.id.search_field);
		lv = (ListView) findViewById(R.id.listView1);
		
		
		String downloadCompleteIntentName = DownloadManager.ACTION_DOWNLOAD_COMPLETE;
		IntentFilter downloadCompleteIntentFilter = new IntentFilter(downloadCompleteIntentName);
		getApplicationContext().registerReceiver(downloadCompleteReceiver, downloadCompleteIntentFilter);
		this.db = new IcecastDatabase(getApplicationContext());
		
		tv.setText(db.getNumStations() + " stations in database.");
	
		Cursor cursor = db.getStationsByName("");
		
		String[] columns = new String[] {
			IcecastDatabase.KEY_NAME,
			//IcecastDatabase.KEY_URL,
			IcecastDatabase.KEY_GENRE
		};
		
		int[] to = new int[] {
			R.id.station_name,
			//R.id.listen_url,
			R.id.genre,
		};
		
		dataAdapter = new SimpleCursorAdapter (this, R.layout.station_info, cursor, columns, to, 0);
		lv.setAdapter(dataAdapter);
		dataAdapter.setFilterQueryProvider(new FilterQueryProvider () {
				public Cursor runQuery (CharSequence constraint) {
					return db.getStationsByName(constraint.toString());
				}
			});
		
		et.addTextChangedListener(new TextWatcher() { 
			public void afterTextChanged(Editable s) {
			}
			
			public void beforeTextChanged(CharSequence s, int start, 
					int count, int after) {
			}
			
			public void onTextChanged(CharSequence s, int start,
					int before, int count) {
				dataAdapter.getFilter().filter(s.toString());
			}
		});
		
		lv.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?>adapter,View v, int position, long id){

				Cursor cursor = (Cursor) lv.getItemAtPosition(position);
				String url = cursor.getString(cursor.getColumnIndexOrThrow(IcecastDatabase.KEY_URL));
				//Toast.makeText(getApplicationContext(), name,Toast.LENGTH_SHORT).show();

				//if (mp.isPlaying()) mp.reset();
				mp.reset();
				
				try {
					mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
					mp.setDataSource(url);
				} catch (IOException e) {
					Log.e("Icecast Player", "Couldn't open stream");
				}
				
				mp.prepareAsync();
				Toast.makeText(getApplicationContext(), "Starting stream...",Toast.LENGTH_LONG).show();
			}
		});
		
		mp = new MediaPlayer();
		mp.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared (MediaPlayer m) {
				mp.start();
				Toast.makeText(getApplicationContext(), "Stream started",Toast.LENGTH_SHORT).show();
			}
		});
		mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		
		getWindow().setSoftInputMode(
			      WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	@Override
	public void onStop () {
		super.onStop();
		
		mp.stop();
		mp.release();
	}
	
	@Override
	public void onPause () {
		super.onPause();
		
		mp.stop();
	}
	
	@Override
	public void onResume () {
		super.onResume();

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void startDownload (View view){
		tv.setText("Downloading file...");
		button.setEnabled(false);
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://dir.xiph.org/yp.xml"));
		request.setDescription("Icecast data file");
		request.setTitle("Icecast");
		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "yp.xml");
		
		DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		
		downloadID = manager.enqueue(request);
	}

	
	private class BuildDatabaseTask extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground (String... params) {
			
			InputStream in;
			try {
				in = getContentResolver().openInputStream(Uri.parse(params[0]));
			} catch (FileNotFoundException e) {
				Log.w("Icecast Player", "Couldn't open downloaded file");
				return "";
			}

			IcecastXmlParser parser = new IcecastXmlParser();
			
			try {
				parser.parse(in, db);
			} catch (XmlPullParserException e) {
				//tv.setText("Parsing Error");
				return "Parsing Error";
			} catch (IOException e) {
				//tv.setText("IO Error");
				return "IO Error";
			}
			
			return "";
		}
		
		@Override
		protected void onPreExecute () {
			tv.setText("Building database...");
		}
		
		@Override
		protected void onPostExecute (String result) {
			if (result.equals("")){
				tv.setText("Done. "+db.getNumStations()+" stations in database.");
 			} else {
 				tv.setText(result);
 			}
			
			button.setEnabled(true);
		}
	}
	
	public void purgeDatabase (View view) {
		db.purge();
		
		tv.setText(db.getNumStations() + " stations in database.");
	}
	
}
