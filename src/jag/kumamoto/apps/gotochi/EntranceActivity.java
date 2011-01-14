package jag.kumamoto.apps.gotochi;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

public class EntranceActivity extends Activity{
	
	private IGotochiService mGotochiService = null;
	
	private final class GotochiServiceConnection implements ServiceConnection {
		private static final int CONNECTED_PROCESS_NONE = 0;
		private static final int CONNECTED_PROCESS_RESTART = 1;
		private static final int CONNECTED_PROCESS_PAUSE = 2;
		
		private int mConnectedProcess = CONNECTED_PROCESS_NONE;
		
		public void setConnectedProcessRestart() {
			mConnectedProcess = CONNECTED_PROCESS_RESTART;
		}
		
		public void setConnectedProcessPause() {
			mConnectedProcess = CONNECTED_PROCESS_PAUSE;
		}
		
		@Override public void onServiceDisconnected(ComponentName name) {
		}
		
		@Override public void onServiceConnected(ComponentName name, IBinder service) {
			mGotochiService = IGotochiService.Stub.asInterface(service);
			
			int process = mConnectedProcess;
			mConnectedProcess = CONNECTED_PROCESS_NONE;
			
			switch(process) {
			case CONNECTED_PROCESS_PAUSE:
				pauseGotochiService();
				break;
			case CONNECTED_PROCESS_RESTART:
				restartGotochiService();
				break;
			}
		}
	}
    private final GotochiServiceConnection mConnection = new GotochiServiceConnection();
    
	@Override protected void onStart() {
		super.onStart();
		
		if(isServiceRunning()) {
			if(mGotochiService == null) {
				//サービスは一時停止中の可能性があるので
				//コネクション設立後確認するためのフラグを立てておく
				mConnection.setConnectedProcessRestart();
				
				bindService(new Intent(this, GotochiService.class), mConnection, 0);
			} else {
				restartGotochiService();
			}
		} else {
			startGotochiService();
		}
	}

	/**
	 * 位置判定サービスが起動中かどうかを調べる
	 * @return 起動中であればtrue.起動していなければfalse
	 */
    private boolean isServiceRunning() {
        ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceInfos = am.getRunningServices(Integer.MAX_VALUE);
        
        int size = serviceInfos.size();
        String serviceName = GotochiService.class.getName();
        for (int i=0; i<size; ++i){
            if (serviceInfos.get(i).service.getClassName().equals(serviceName)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override protected void onStop() {
    	super.onStop();
    	
    	if(!isTopSelfApps()) {
    		//ご当地アプリ以外が表示されている場合は
    		//位置判定サービスを一時停止する
    		pauseGotochiService();
    	}
    }    
    
   //この実装少し不安。別の案を考えたほうがいいかも 
    /**
     * ご当地アプリのActivityがトップにある(表示されている)かどうかを調べる
     * @return トップにあるならtrue.隠れている場合はfalse
     */
    private boolean isTopSelfApps() {
    	ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
    	for(RunningTaskInfo info : am.getRunningTasks(1)) {
    		return getComponentName().compareTo(info.baseActivity) == 0;
    	}
    	
    	return false;
    }
    
    @Override protected void onDestroy() {
		mGotochiService = null;
		unbindService(mConnection);
    	
    	//super.onDestroy()呼び出しよりも先に呼び出すほうがベター
    	if(isFinishSelfApps()) {
    		stopGotochiService();
    	}
    	
    	super.onDestroy();
    }
    
    private boolean isFinishSelfApps() {
    	ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
    	ComponentName selfName = getComponentName();
    	
    	for(RunningTaskInfo info : am.getRunningTasks(Integer.MAX_VALUE)) {
    		if(selfName.compareTo(info.baseActivity) == 0) {
    			return info.numRunning == 0;
    		}
    	}
    	
    	//実行中のタスク一覧になければ終了している
    	return true;
    }
	
    
	private void startGotochiService() {
		Intent intent = new Intent(this, GotochiService.class);
		startService(intent);
		bindService(intent, mConnection, 0);
	}
	
	private void stopGotochiService() {
		stopService(new Intent(this, GotochiService.class));
	}
	
	private void pauseGotochiService() {
		if(mGotochiService != null) {
			try {
				mGotochiService.pause();
			}catch(RemoteException e) {
				//TODO どうしよう。再起動かな
				e.printStackTrace();
			}
		} else {
			//まだサービスとコネクションできていないので、
			//コネクション設立後にすぐポーズをするように
			mConnection.setConnectedProcessPause();
		}
		
	}
	
	private void restartGotochiService() {
		try {
			if(!mGotochiService.isRunning()) {
				mGotochiService.restart();
			}
		}catch(RemoteException e) {
			//TODO どうしよう 再起動かな
			e.printStackTrace();
		}
	}
    
}
