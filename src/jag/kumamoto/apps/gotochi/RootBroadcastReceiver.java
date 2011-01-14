package jag.kumamoto.apps.gotochi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RootBroadcastReceiver extends BroadcastReceiver{
	
	/**
	 * 他都道府県に移動したときに発行されるインテントに付属するアクション
	 * これは位置判定サービスによって発行される。
	 */
	public static final String LOCATION_CHANGE_ACTION = "jag.kumamoto.apps.gotochi.LOCATION_CHANGE";
	
	/**
	 * 移動によって他県に移動したときに発行されるインテントに対して付属されている付加情報
	 * 移動する前の都道府県が得られる
	 */
	public static final String BEFORE_LOCATION = "before_location";
	/**
	 * 他都道府県に移動したときに発行されるインテントに対して付属されている付加情報
	 * 現在の都道府県が得られる
	 */
	public static final String CURRENT_LOCATION = "current_location";
	
	@Override public void onReceive(Context context, Intent intent) {
		
		if(intent.getAction().equals(LOCATION_CHANGE_ACTION)) {
			PrefecturesCode pref = (PrefecturesCode) intent.getSerializableExtra(CURRENT_LOCATION);
		}
	}

}
