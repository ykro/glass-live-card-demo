package com.elementalgeeks.helloglass.v2;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;

public class CustomService extends Service {
	private static final String LIVE_CARD_ID = "gdgdemo";	
	
	private LiveCard liveCard;
	private TextToSpeech txtToSpeech;
    private TimelineManager timelineManager;    
    private final IBinder binder = new CustomBinder();
    
	public class CustomBinder extends Binder {

		public void listen() {
			SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
			String words = prefs.getString(MenuActivity.WORDS_KEY, getString(R.string.fallback_string));
			txtToSpeech.speak(words, TextToSpeech.QUEUE_FLUSH, null);			
		}
	}
	
    @Override
    public void onCreate() {
    	super.onCreate();
    	timelineManager = TimelineManager.from(this);
    	txtToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.card);
	    if (liveCard == null) {
	    	liveCard = timelineManager.createLiveCard(LIVE_CARD_ID);
	        liveCard.setViews(views);
	         
	        Intent menuIntent = new Intent(this, MenuActivity.class);
	        menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
	        liveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
	        liveCard.publish(LiveCard.PublishMode.REVEAL);
	     } 
	     
	     return START_STICKY;
	}
    
    @Override
    public void onDestroy() {
        if (liveCard != null && liveCard.isPublished()) {
            liveCard.unpublish();
            liveCard = null;
        }
        txtToSpeech.shutdown();
        txtToSpeech = null;
        
        super.onDestroy();
    }    
	
	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}

}
