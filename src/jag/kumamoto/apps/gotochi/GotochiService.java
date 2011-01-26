package jag.kumamoto.apps.gotochi;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * ご当地アプリ内で各都道府県アプリに依存しない機能を提供するサービスクラス.
 * @author aharisu
 *
 */
public class GotochiService extends Service{
	
	private static final String PREFERENCES_NAME = "gotochi-pref";
	private static final String LAST_KNOWN_LOCATION = "last-known-location";
	
	private boolean mIsRunning = false;
	private int mActivityCount = 0;
	private LocalLocationManager mLocalLocationManager;
	private final LocalLocationManager.OnLocalLocationChangeListener mLocalLocationListener = 
		new LocalLocationManager.OnLocalLocationChangeListener() {
		
		@Override public void onLocalLocationChange(PrefecturesCode cur, PrefecturesCode prev) {
			//都道府県を移動したことを通知するブロードキャストインテントを投げる
			
			Intent intent = new Intent(RootBroadcastReceiver.LOCATION_CHANGE_ACTION);
			intent.putExtra(RootBroadcastReceiver.CURRENT_LOCATION, cur);
			if(prev != null) {
				intent.putExtra(RootBroadcastReceiver.BEFORE_LOCATION, prev);
			}
			
			GotochiService.this.sendOrderedBroadcast(intent, null);
		}
		
		@Override public void onManagerException(LocalLocationManager.JudgeLocationException e) {
			//TODO 適切なエラーハンドリング
			
			checkDeviceCapabilities();
		}
	};
	
	
	private final IGotochiService.Stub mJudgmentLocationService = new IGotochiService.Stub() {

		@Override synchronized public int getActivityNumber() throws RemoteException {
			return ++mActivityCount;
		};
		
		@Override public void pause() throws RemoteException {
			onPause();
		}
		
		@Override public void restart() throws RemoteException {
			onRestart();
		}
		
		@Override public boolean isRunning() throws RemoteException {
			return mIsRunning;
		}
	};
	
	@Override public IBinder onBind(Intent intent) {
		return mJudgmentLocationService;
	}
	
	
	@Override public void onCreate() {
		super.onCreate();
		
		if(!checkDeviceCapabilities()) {
			mIsRunning = false;
			return;
		}
		
		mIsRunning = true;
		setupLocationModule();
	}

	
	private void onPause() {
		if(mIsRunning) {
			mIsRunning = false;
			
			stopLocationModule();
		}
	}
	
	
	private void onRestart() {
		
		if(!checkDeviceCapabilities()) {
			mIsRunning = false;
			return;
		}
		
		mIsRunning = true;
		if(mLocalLocationManager == null) {
			setupLocationModule();
		} else {
			//位置判定モジュールの再始動
			mLocalLocationManager.restartJudgeLocation();
		}
	}
	
	private void setupLocationModule() {
		//位置判定モジュールのスタート
		PrefecturesCode code = getLastKnownLocation();
		mLocalLocationManager = new LocalLocationManager(this, code, mLocalLocationListener);
		mLocalLocationManager.startJudgeLocation();
	}
	
	
	@Override public void onDestroy() {
		super.onDestroy();
		
		if(mIsRunning) {
			mIsRunning = false;
			
			stopLocationModule();
			putLastKnownLocation(mLocalLocationManager.getCurrentPrefecturesCode());
			mLocalLocationManager = null;
		}
	}
	
	private void stopLocationModule() {
		mLocalLocationManager.stopJudgeLocation();
	}	
	
	
	private PrefecturesCode getLastKnownLocation() {
		SharedPreferences pref = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
		int code =  pref.getInt(LAST_KNOWN_LOCATION, -1);
		if(code != -1) {
			return PrefecturesCode.values()[code - 1];
		}
		return null;
	}
	
	private void putLastKnownLocation(PrefecturesCode code) {
		if(code != null) {
			SharedPreferences pref = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
			SharedPreferences.Editor editor = pref.edit();
			
			editor.putInt(LAST_KNOWN_LOCATION, code.code);
			
			editor.commit();
		}
	}
	
	
	private boolean checkDeviceCapabilities() {
		Context context = getApplicationContext();
		boolean isNetworkConnected = DeviceCapabilitiesChecker.isNetworkConnected(context);
		//GPS限定ではなく、位置情報が取得できればなんでもいい
		//boolean gpsEnabled = DeviceCapabilitiesChecker.isRunningGPSService(context);
		boolean locationEnabled = DeviceCapabilitiesChecker.isEnableLocationService(context);
		
		if(isNetworkConnected && locationEnabled) {
			return true;
		}
		
		//設定の変更を促すアクティビティを表示する
		Intent intent = new Intent(context, ServiceShowWarningActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(ServiceShowWarningActivity.WARNING_NETWORK_DISABLE, !isNetworkConnected);
		intent.putExtra(ServiceShowWarningActivity.WARNING_LOCATION_DISABLE, !locationEnabled);
		//intent.putExtra(ServiceShowWarningActivity.WARNING_GPS_DISABLE, !gpsEnabled);
		
		context.startActivity(intent);
		
		return false;
	}
	
}
