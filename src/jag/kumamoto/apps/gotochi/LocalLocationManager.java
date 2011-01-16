package jag.kumamoto.apps.gotochi;

import java.io.IOException;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public final class LocalLocationManager {
	public static interface OnLocalLocationChangeListener {
		public void onLocalLocationChange(PrefecturesCode cur, PrefecturesCode prev);
		public void onManagerException();
	}

	private PrefecturesCode mCurrentLocation;
	private final Context mContext;
	private boolean mJudgeStart = false;
	private final Geocoder mGeocoder;
	private final OnLocalLocationChangeListener mCallback;
	
	public LocalLocationManager(Context context, OnLocalLocationChangeListener listener) {
		this(context, null, listener);
	}
	
	public LocalLocationManager(Context context, PrefecturesCode curLocation, 
			OnLocalLocationChangeListener listener) {
		
		mCurrentLocation = curLocation;
		mContext = context;
		mGeocoder = new Geocoder(context, Locale.JAPAN);
		mCallback = listener;
	}
	
	public PrefecturesCode getCurrentPrefecturesCode() {
		return mCurrentLocation;
	}
		private final LocationListener mLocationListener = new LocationListener() {
		
		@Override public void onStatusChanged(String provider, int status, Bundle extras) {
		}
		
		@Override public void onProviderEnabled(String provider) {
		}
		
		@Override public void onProviderDisabled(String provider) {
			if(provider.equals(LocationManager.GPS_PROVIDER)) {
				mCallback.onManagerException();
			}
		}
		
		@Override public void onLocationChanged(Location location) {
			try {
				String adminArea = pointToAdminArea(location.getLatitude(), location.getLongitude());
				if(adminArea != null) {
					PrefecturesCode code = adminAreaToPrefecturesCode(adminArea);
					if(code != null && mCurrentLocation != code) {
						PrefecturesCode prev = mCurrentLocation;
						mCurrentLocation = code;
						
						mCallback.onLocalLocationChange(mCurrentLocation, prev);
					}
				}
			}catch(IOException e) {
				mCallback.onManagerException();
			}
		}
	};
	
	private String pointToAdminArea(double latitude, double longtitude) throws IOException{
		
		for(Address addr : mGeocoder.getFromLocation(latitude, longtitude, 5)) {
			String adminArea = addr.getAdminArea();
			if(adminArea != null) {
				return adminArea;
			}
		}
		
		return null;
	}
	
	private PrefecturesCode adminAreaToPrefecturesCode(String adminArea) {
		if(mCurrentLocation != null && mCurrentLocation.name.equals(adminArea)) {
			return mCurrentLocation;
		}
		
		for(PrefecturesCode code : PrefecturesCode.values()) {
			if(code.name.equals(adminArea)) {
				return code;
			}
		}
		
		//例外を投げるようにするか？
		return null;
	}
	
	public void startJudgeLocation() {
		setupLocationListener();
		
		LocationManager lm = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE); 
		Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		mLocationListener.onLocationChanged(location);
	}
	
	public void restartJudgeLocation() {
		setupLocationListener();
	}
	
	private void setupLocationListener() {
		if(!mJudgeStart) {
			mJudgeStart = true;
			
			LocationManager lm = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE); 
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					0,//TODO 数値を適切な値に修正
					0,//TODO 数値を適切な値に修正
					mLocationListener);
		}
	}
	
	public void stopJudgeLocation() {
		if(mJudgeStart) {
			LocationManager lm = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE); 
			lm.removeUpdates(mLocationListener);
			
			mJudgeStart = false;
		}
	}
	
}
