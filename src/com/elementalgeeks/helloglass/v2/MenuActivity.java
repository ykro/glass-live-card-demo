package com.elementalgeeks.helloglass.v2;

import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MenuActivity extends Activity {	 
	private final static int SPEECH_REQUEST = 0;
	public final static String WORDS_KEY = "words";
	
	private boolean resumed;
	private boolean sRquest = false;
    private CustomService.CustomBinder customService;
    
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof CustomService.CustomBinder) {
            	customService = (CustomService.CustomBinder) service;
                openOptionsMenu();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(customService == null) {
        	bindService(new Intent(this, CustomService.class), connection, 0);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        resumed = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        resumed = false;
    }
   
    @Override
    public void openOptionsMenu() {
        if (resumed && customService != null) {
            super.openOptionsMenu();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_listen:
            	customService.listen();
            	return true;
            case R.id.action_speak:
            	sRquest = true;
    			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    		    startActivityForResult(intent, SPEECH_REQUEST);
            	return true;            	
            case R.id.action_stop:
                stopService(new Intent(this, CustomService.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        unbindService(connection);
        if (!sRquest) {
        	finish();
        }
    }   
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {    	
        if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String strResult = results.get(0);
            if (!strResult.isEmpty()) {
	            SharedPreferences prefs = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
	            prefs.edit().putString(WORDS_KEY, strResult).commit();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }    
}
