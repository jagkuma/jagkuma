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

/**
 * 現在位置判定クラス<br/>
 * <br/>
 * このクラスを使用するためには、
 * GPSが有効になっていることと、ネットワークアクセスが可能である必要がある。
 * @author aharisu
 *
 */
public final class LocalLocationManager {
	
	/**
	 * 都道府県を移動したときに呼ばれるイベントリスナ<br/>
	 * GPSが無効になった時と、ネットワークアクセスに失敗したときの例外処理通知にも利用する
	 * @author aharisu
	 *
	 */
	public static interface OnLocalLocationChangeListener {
		public void onLocalLocationChange(PrefecturesCode cur, PrefecturesCode prev);
		public void onManagerException(JudgeLocationException e);
	}

	/**
	 * {@link OnLocalLocationChangeListener}の{@link onManagerException}メソッドで
	 * 例外を通知するため利用するに例外クラス<br/>
	 * 
	 * @author aharisu
	 *
	 */
	public static class JudgeLocationException extends Exception {
		
		private static final long serialVersionUID = -4343031233729920193L;
		
		
		/**
		 * GPSが無効になったことを示す定数
		 */
		public static final int EXCEPTION_DISABLE_GPS = 0;
		
		/**
		 * 取得した経緯度から都道府県を取得できなかったことを示す定数
		 */
		public static final int EXCEPTION_GET_ADMIN_AREA = 1;
		
		/**
		 * 未知の都道府県だった時の定数
		 */
		public static final int EXCEPTION_GET_PREFECTURES_CODE = 2;
		
		/**
		 * ネットワークアクセスが不可能だったときの定数
		 */
		public static final int EXCEPTION_IO = 3;
		
		public final int errorCode;
		public final IOException e;
		
		public JudgeLocationException(int errorCode) {
			this.errorCode = errorCode;
			this.e = null;
		}
		
		public JudgeLocationException(IOException e) {
			this.errorCode = EXCEPTION_IO;
			this.e = e;
		}
		
	}
	
	
	private PrefecturesCode mCurrentLocation;
	private final Context mContext;
	private boolean mJudgeStart = false;
	private final Geocoder mGeocoder;
	private final OnLocalLocationChangeListener mCallback;
	
	public LocalLocationManager(Context context, PrefecturesCode curLocation, 
			OnLocalLocationChangeListener listener) {
		
		mCurrentLocation = curLocation;
		mContext = context;
		mGeocoder = new Geocoder(context, Locale.JAPAN);
		mCallback = listener;
	}
	
	/**
	 * 現在地を取得する。nullの場合もある。
	 * @return 現在地を示す{@linkplain PrefecturesCode}定数。まだ一度も現在地を取得できていない場合はnull。
	 */
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
				mCallback.onManagerException(new JudgeLocationException(
						JudgeLocationException.EXCEPTION_DISABLE_GPS));
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
				mCallback.onManagerException(new JudgeLocationException(e));
			}catch(JudgeLocationException e) {
				mCallback.onManagerException(e);
			}
		}
	};
	
	private String pointToAdminArea(double latitude, double longtitude) throws IOException, JudgeLocationException{
		
		for(Address addr : mGeocoder.getFromLocation(latitude, longtitude, 5)) {
			String adminArea = addr.getAdminArea();
			if(adminArea != null) {
				return adminArea;
			}
		}
		
		throw new JudgeLocationException(JudgeLocationException.EXCEPTION_GET_ADMIN_AREA);
	}
	
	private PrefecturesCode adminAreaToPrefecturesCode(String adminArea) throws JudgeLocationException{
		if(mCurrentLocation != null && mCurrentLocation.name.equals(adminArea)) {
			return mCurrentLocation;
		}
		
		for(PrefecturesCode code : PrefecturesCode.values()) {
			if(code.name.equals(adminArea)) {
				return code;
			}
		}
		
		throw new JudgeLocationException(JudgeLocationException.EXCEPTION_GET_PREFECTURES_CODE);
	}
	
	public void startJudgeLocation() {
		setupLocationListener();
		
		LocationManager lm = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE); 
		Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(location != null) {
			mCurrentLocation = null;
			mLocationListener.onLocationChanged(location);
		} else if(mCurrentLocation != null){
			mCallback.onLocalLocationChange(mCurrentLocation, null);
		}
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
