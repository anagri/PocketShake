package com.barefoot.pocketshake.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import com.barefoot.pocketshake.R;
import com.barefoot.pocketshake.data.EarthQuake;
import com.barefoot.pocketshake.workers.QuakeFeedParser;

public class FeedSynchronizer extends Service {

	public static final String BROADCAST_ACTION = "com.barefoot.pocketshake.service.FeedSynchronizer";
	private HttpClient client;
	private String feedUrl;
	private final Binder binder = new LocalBinder();
	//private String earthquakeFeed;
	private Intent broadcast=new Intent(BROADCAST_ACTION);
	private Timer timer = new Timer();
	private ArrayList<EarthQuake> earthQuakes = new ArrayList<EarthQuake>();
	private QuakeFeedParser parser;
	
	@Override
	public void onCreate() {
		super.onCreate();

		client = new DefaultHttpClient();
		feedUrl = getString(R.string.feed_url);
	
		//startservice();		
		updateFeed();
	}
	
	
	private void startservice() {
		timer.scheduleAtFixedRate( new TimerTask() {
			public void run() {
				updateFeed();
			}
		}, 0, 60000L);
	}
	
	private void stopservice() {
		if (timer != null){
			timer.cancel();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
//		if(timer != null) {
//			stopservice();
//		}
		client.getConnectionManager().shutdown();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	synchronized public String getEarthquakeData() {
		return (Integer.toString(earthQuakes.size()));
	}

	private void updateFeed() {
		new FetchFeedTask().execute();
	}

	public class LocalBinder extends Binder {
		public FeedSynchronizer getService() {
			return (FeedSynchronizer.this);
		}
	}
	
	protected ArrayList<EarthQuake> generateQuakes(InputStream content) {
		parser = new QuakeFeedParser(content);
		return parser.asParsedObject();
	}

	class FetchFeedTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... unused) {
			HttpGet getMethod = new HttpGet(feedUrl);
			HttpResponse response = null;

			try {
				response = client.execute(getMethod);
				HttpEntity entity = response.getEntity();
				ArrayList<EarthQuake> generateQuakes = generateQuakes(entity.getContent());
				synchronized(this) {
					earthQuakes = generateQuakes;
				}
				sendBroadcast(broadcast);
			} catch (Throwable t) {
				t.printStackTrace();
			}

			return (null);
		}

		@Override
		protected void onProgressUpdate(Void... unused) {
			// not needed here
		}

		@Override
		protected void onPostExecute(Void unused) {
			// not needed here
		}
	}

}
