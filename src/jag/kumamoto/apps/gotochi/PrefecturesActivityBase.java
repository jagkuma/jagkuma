package jag.kumamoto.apps.gotochi;



import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;


/**
 * 各都道府県のActivityを実装するときに、普通のActivityクラスの代わりにベースクラスにするActivity.
 * 位置判断サービスとの接続などの処理を行う.
 * <br/>
 * クラスが持つ文字列定数は,ご当地アプリ本体のパッケージ名やクラス名,その中で定義されている定数である.
 * @author aharisu
 * @version 0.1
 *
 */
public abstract class PrefecturesActivityBase extends Activity{
	
	public static final String ENTRANCE_ACTIVITY_PACKAGE_NAME = "jag.kumamoto.apps.gotochi";
	public static final String ENTRANCE_ACTIVITY_CLASS_NAME = "jag.kumamoto.apps.gotochi.EntranceActivity";
	public static final String GOTOCHI_SERVICE_PACKAGE_NAME = "jag.kumamoto.apps.gotochi";
	public static final String GOTOCHI_SERVICE_CLASS_NAME = "jag.kumamoto.apps.gotochi.GotochiService";
	
	/**
	 * 他都道府県に移動したときに発行されるインテントに付属するアクション.
	 * このアクションを含むインテントはご当地サービスによって発行される.
	 */
	public static final String LOCATION_CHANGE_ACTION = "jag.kumamoto.apps.gotochi.LOCATION_CHANGE";
	
	/**
	 * 他都道府県に移動したときに発行されるインテントに付属するURIデータのスキーム.
	 * このURIデータを含むインテントはご当地サービスによって発行される.
	 */
	public static final String URI_GOTOCHI_SCHEME = "gotochi";
	
	private static final String START_FROM_GOTOCHI_APP = "start_from_gotochi_app";
	
	public static final String METADATA_GOTOCHI_APP = "GotochiApp";
	
	
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
			
			onServiceBind(mGotochiService);
		}
	}
    private final GotochiServiceConnection mConnection = new GotochiServiceConnection();
    
    
    private boolean mReceiverRegisterd = false;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(LOCATION_CHANGE_ACTION)) {
				//ロケーションが変更された時のイベントハンドラを実行
				if(onLocationChange(context, intent)) {
					abortBroadcast();
				} else {
					//デフォルトの処理は強制的にアクティビティを終了させる
					finish();
				}
			}
		}
	};
    
	
	private boolean mIsGotochiApp = false;
	
	/**
	 * アプリケーションがご当地アプリとして機能しているか.
	 * <br/>
	 * アプリケーションは以下の条件が全てそろっているときにご当地アプリとして機能する.
	 * <ul>
	 * 	<li>ご当地アプリ本体がインストールされている.</li>
	 * 	<li>ご当地アプリ本体からのインテントで起動する.</li>
	 * 	<li>アプリケーションでGET_TASKSのパーミッションが許可されている.</li>
	 * </ul>
	 * 
	 * @return ご当地アプリであればtrue.単独アプリであればfalse.
	 */
	public final boolean isGotochiApp() {
		return mIsGotochiApp;
	}
	
	
	
	/**
	 * ご当地アプリで利用できるサービスのインスタンスを取得する.
	 * <br/>
	 * {@link #onServiceBind() onServiceBind}後でなければインスタンスを取得することはできない.
	 * <br/>
	 * {@link #isGotochiApp() isGotochiApp}がfalseなら、このメソッドは常にnullを返す.
	 * @return ご当地アプリサービスのインスタンス.もしくはnull.
	 */
	protected final IGotochiService getGotochiService() {
		return mGotochiService;
	}
	
	
	
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mIsGotochiApp = checkGotochiApp();
	}	
	
	private boolean checkGotochiApp() {
		
		 //ご当地アプリから投げる暗黙のインテントから起動していなければならない
		//各都道府県の二つ目以降のActivityには自動的にこのパラメータが付加される
		if(!getIntent().getBooleanExtra(START_FROM_GOTOCHI_APP, false)) {
			 return false;
		}
		 
		//本来はここで適切なインテントフィルタが設定されているか調べたいが、
		//方法がわからない（その方法がないorまだ実装されていない？）ので省略。
		
		PackageManager pm = getPackageManager();
		if(pm.checkPermission(Manifest.permission.GET_TASKS, this.getPackageName()) ==
				PackageManager.PERMISSION_DENIED) {
			//GET_TASKSがパーミッションで許可されていなければならない
			return false;
		}
		
		try {
			pm.getServiceInfo(
					new ComponentName(GOTOCHI_SERVICE_PACKAGE_NAME, GOTOCHI_SERVICE_CLASS_NAME),
					0);
			//getServiceInfoで例外が投げられないならばServiceはインストールされている
			return true;
		} catch (NameNotFoundException e) {
			//例外が起きた場合はご当地サービスは起動されていない
			return false;
		}
	}
	
	
	
	@Override protected void onStart() {
		super.onStart();
		
		if(!mIsGotochiApp) {
			//ご当地アプリとして有効でないので処理をしない
			return;
		}
		
		if(isServiceRunning()) {
			if(mGotochiService == null) {
				//サービスは一時停止中の可能性があるので
				//コネクション設立後確認するためのフラグを立てておく
				mConnection.setConnectedProcessRestart();
				Intent intent = new Intent();
				intent.setClassName(GOTOCHI_SERVICE_PACKAGE_NAME,
						GOTOCHI_SERVICE_CLASS_NAME);
				
				bindService(intent, mConnection, 0);
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
        
        try {
	        List<ActivityManager.RunningServiceInfo> serviceInfos = am.getRunningServices(Integer.MAX_VALUE);
	        
	        int size = serviceInfos.size();
	        for (int i=0; i<size; ++i){
	            if (serviceInfos.get(i).service.getClassName().equals(GOTOCHI_SERVICE_CLASS_NAME)) {
	                return true;
	            }
	        }
        } catch(SecurityException e) {
        	//getRunnningServicesがパーミッションで許可されていない
        	//事前にパーミッションのチェックをしているが一応例外ハンドラを書いておく
        }
        
        return false;
    }
    
    
    
    @Override protected void onStop() {
    	super.onStop();
    	
		if(!mIsGotochiApp) {
			//ご当地アプリとして有効でないので処理をしない
			return;
		}
    	
    	if(!isTopSelfApps()) {
    		//ご当地アプリ以外が表示されている場合は
    		//位置判定サービスを一時停止する
    		pauseGotochiService();
    	}
    }
    
    //インテントフィルタの取得の仕方がわからないので
    //しょうがなくメタデータを利用する
    /**
     * ご当地アプリのActivityがトップにある(表示されている)かどうかを調べる.
     * @return トップにあるならtrue.隠れている場合はfalse.
     */
    private boolean isTopSelfApps() {
    	
    	ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
    	try {
	    	List<RunningTaskInfo> infoList = am.getRunningTasks(1); 
	    	int size = infoList.size();
	    	for(int i = 0;i < size; ++i) {
				Bundle metaData = getPackageManager()
						.getActivityInfo(infoList.get(i).baseActivity, PackageManager.GET_META_DATA).metaData;
	    		if(metaData != null)
	    			return metaData.getBoolean(METADATA_GOTOCHI_APP, false);
	    	}
    	} catch(SecurityException e) {
        	//事前にパーミッションのチェックをしているが一応例外ハンドラを書いておく
    	} catch(NameNotFoundException e) {
			//この場合は結果をfalseにするためにここでは何もしない
    	}
    	
    	return false;
    }
    
    
    
    @Override protected void onDestroy() {
    	
		if(mIsGotochiApp) {
			//ご当地アプリとして有効なときのみ処理を行う
    	
	    	if(mReceiverRegisterd) {
	    		mReceiverRegisterd = false;
		    	unregisterReceiver(mReceiver);
	    	}
	    	
	    	if(mGotochiService != null) {
				mGotochiService = null;
				unbindService(mConnection);
	    	}
			
			if(isFinishSelfApps()) {
				stopGotochiService();
			}
		}
		
    	super.onDestroy();
    }
    
    private boolean isFinishSelfApps() {
    	ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
    	try {
	    	List<RunningTaskInfo> infoList = am.getRunningTasks(Integer.MAX_VALUE);
	    	int size = infoList.size();
	    	for(int i = 0;i < size; ++i) {
				Bundle metaData = getPackageManager()
						.getActivityInfo(infoList.get(i).baseActivity, PackageManager.GET_META_DATA).metaData;
				
	    		if(metaData != null && metaData.getBoolean(METADATA_GOTOCHI_APP, false))
	    			return false;
	    	} 
    	}catch(SecurityException e) {
        	//事前にパーミッションのチェックをしているが一応例外ハンドラを書いておく
    	}catch(NameNotFoundException e) {
			//この場合は結果をfalseにするためにここでは何もしない
    	}
    	
    	//実行中のタスク一覧になければ終了している
    	return true;
    }
    
    
    
    
    /**
     * ご当地アプリが起動したのでサービスを起動させる.
     */
	private void startGotochiService() {
		Intent intent = new Intent();
		intent.setClassName(GOTOCHI_SERVICE_PACKAGE_NAME,
				GOTOCHI_SERVICE_CLASS_NAME);
		startService(intent);
		
		bindService(intent, mConnection, 0);
	}
	
	/**
	 * ご当地アプリが終了したのでサービスを終了させる.
	 */
	private void stopGotochiService() {
		Intent intent = new Intent();
		intent.setClassName(GOTOCHI_SERVICE_PACKAGE_NAME,
				GOTOCHI_SERVICE_CLASS_NAME);
		
		stopService(intent);
	}
	
	/**	
	 * 他のアプリ画面が表示されたのでサービスの動作を一時停止させる.
	 */
	private void pauseGotochiService() {
		if(mGotochiService != null) {
			try {
				mGotochiService.pause();
			}catch(RemoteException e) {
				//どうしよう。再起動かな
				e.printStackTrace();
			}
		} else {
			//まだサービスとコネクション設立できていないので、
			//コネクション設立後にすぐポーズをするようにフラグをセット
			mConnection.setConnectedProcessPause();
		}
	}
	
	/**
	 * 他のアプリ画面から復帰したのでサービスの動作を再始動させる.
	 */
	private void restartGotochiService() {
		try {
			if(!mGotochiService.isRunning()) {
				mGotochiService.restart();
			}
		}catch(RemoteException e) {
			//どうしよう 再起動かな
			e.printStackTrace();
		}
	}
	
	
	
	//継承先がstartActivity～メソッド群を実行してActivityを起動させるのをフックし、
	//追加情報を付加する
	
	@Override public void startActivityForResult(Intent intent, int requestCode) {
		intent.putExtra(START_FROM_GOTOCHI_APP, mIsGotochiApp);
		
		super.startActivityForResult(intent, requestCode);
	}
	
	@Override public void startActivityFromChild(Activity child, Intent intent, int requestCode) {
		intent.putExtra(START_FROM_GOTOCHI_APP, mIsGotochiApp);
		
		super.startActivityFromChild(child, intent, requestCode);
	}
	
	@Override public boolean startActivityIfNeeded(Intent intent, int requestCode) {
		intent.putExtra(START_FROM_GOTOCHI_APP, mIsGotochiApp);
		
		return super.startActivityIfNeeded(intent, requestCode);
	}
	
	@Override public boolean startNextMatchingActivity(Intent intent) {
		intent.putExtra(START_FROM_GOTOCHI_APP, mIsGotochiApp);
		
		return super.startNextMatchingActivity(intent);
	}
	
    
    
    /**
     * ご当地サービスのインスタンスを取得したときに実行される.
     * <br/>
     * サービスインスタンスから何らかのデータを取得したい場合は
     * このメソッドをオーバーライドしその中で記述するか
     * このメソッドの実行後でなければならない.
     * <br/>
     * メソッドをオーバーライドする時は
     * 必ず先頭でsuperクラスのonServiceBindを実行しなければならない.
     * 
     * @param service ご当地サービスのインスタンス
     */
    protected void onServiceBind(IGotochiService service) {
		//TODO まだ県を移動していないか確かめる
		//そんなにシビアにする必要はないのかな？
    		
		if(!mReceiverRegisterd) {
    		mReceiverRegisterd = true;
	    	try {
		    	//ロケーションが変わった時に呼ばれる、デフォルトのブロードキャストレシーバを登録する
		    		
			    	IntentFilter filter = new IntentFilter(LOCATION_CHANGE_ACTION);
			    	
		    		//適切な順序でブロードキャストレシーバが呼ばれるようにするため、
		    		//プライオリティーをセットする
			    	filter.setPriority(mGotochiService.getActivityNumber());
			    	
			    	registerReceiver(mReceiver, filter);
	    		
	    	} catch(RemoteException e) {
				//どうしよう 再起動かな
				e.printStackTrace();
	    	}
		}
    }
    
    
    
    /**
     * 都道府県を移動したときに実行される.
     * <br/>
     * メソッドをオーバーライドしtrueを戻り値にした場合
     * Activityは終了しない.
     * <br/>
     * オーバーライドしない、もしくはfalseを戻り値にした場合
     * Activityは強制的に終了する.
     * @param context {@link BroadcastReceiver#onReceive(Context, Intent)}のContext引数
     * @param intent {@link BroadcastReceiver#onReceive(Context, Intent)}のIntent引数
     * @return Activityを終了させないときはtrue.終了させるときはfalse.
     */
    protected boolean onLocationChange(Context context, Intent intent) {
    	return false;
    }

    
}
