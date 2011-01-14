package jag.kumamoto.apps.gotochi;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class GotochiService extends Service{
	
	private boolean mIsRunning = false;
	private int mActivityCount = 0;
	
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
		//TODO 位置判定モジュールのスタート
		
		
		//TODO 初期位置(起動時の都道府県)をすぐに検索して
		//一番最初のブロードキャストインテントを投げる
		//or
		//ぷリファレンスに保存しておいてそれを取得する
	}
	
	private void onPause() {
		mIsRunning = false;
		
		stopLocationModule();
	}
	
	private void onRestart() {
		
		if(!checkDeviceCapabilities()) {
			mIsRunning = false;
			return;
		}
		
		mIsRunning = true;
		//TODO 位置判定モジュールの再始動
	}
	
	@Override public void onDestroy() {
		super.onDestroy();
		
		mIsRunning = false;
		
		stopLocationModule();
	}
	
	private void stopLocationModule() {
		//TODO 位置判定モジュールの終了
	}
	
	private boolean checkDeviceCapabilities() {
		Context context = getApplicationContext();
		boolean isNetworkConnected = DeviceCapabilitiesChecker.isNetworkConnected(context);
		boolean gpsEnabled = DeviceCapabilitiesChecker.isRunningGPSService(context);
		
		if(isNetworkConnected && gpsEnabled) {
			return true;
		}
		
		//設定の変更を促すアクティビティを表示する
		Intent intent = new Intent(context, ServiceShowWarningActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(ServiceShowWarningActivity.WARNING_NETWORK_DISABLE, !isNetworkConnected);
		intent.putExtra(ServiceShowWarningActivity.WARNING_GPS_DISABLE, !gpsEnabled);
		
		context.startActivity(intent);
		
		return false;
	}
	
}
