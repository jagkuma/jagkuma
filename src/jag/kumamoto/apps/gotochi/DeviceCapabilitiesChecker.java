package jag.kumamoto.apps.gotochi;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class DeviceCapabilitiesChecker {
	
	public static final boolean isNetworkConnected(Context context) {
		ConnectivityManager manager = (ConnectivityManager)
			context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		NetworkInfo info = manager.getActiveNetworkInfo();
		if(info != null) {
			return info.isConnected();
		}
		
		return false;
	}
	
	public static void startWifiSettingsActivity(Context context) {
    	Intent intent = new Intent(android.provider.Settings.ACTION_AIRPLANE_MODE_SETTINGS);
    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	
    	context.startActivity(intent);
	}
	
	public static final boolean isRunningGPSService(Context context) {
		String gs = android.provider.Settings.Secure.getString(context.getContentResolver(),
				android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		
		return gs.indexOf("gps", 0) >= 0;
	}
	
	public static void startGPSSettingsActivity(Context context) {
		Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		context.startActivity(intent);
	}
	
	public static void startSettingsActivity(Context context) {
		Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		context.startActivity(intent);
	}
	
}
