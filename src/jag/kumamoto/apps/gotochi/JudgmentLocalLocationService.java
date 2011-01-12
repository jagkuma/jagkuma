package jag.kumamoto.apps.gotochi;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class JudgmentLocalLocationService extends Service{
	
	private boolean mIsRunning = false;
	
	private final IJudgmentLocalLocationService.Stub mJudgmentLocationService = new IJudgmentLocalLocationService.Stub() {

		
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
		
		//TODO 位置判定モジュールのスタート
		
		mIsRunning = true;
		
		//TODO 初期位置(起動時の都道府県)をすぐに検索して
		//一番最初のブロードキャストインテントを投げる
	}
	
	private void onPause() {
		mIsRunning = false;
		
		//TODO 位置判定モジュールの一時停止
	}
	
	private void onRestart() {
		mIsRunning = true;
		
		//TODO 位置判定モジュールの再始動
	}
	
	@Override public void onDestroy() {
		super.onDestroy();
		
		mIsRunning = false;
		
		//TODO 位置判定モジュールの終了
	}
	
}
