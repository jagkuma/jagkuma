package jag.kumamoto.apps.gotochi;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

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
	
	public static final String URI_GOTOCHI_SCHEME = "gotochi";
	public static final String CATEGORY_GOTOCHI = "jag.gotochi.category.ACTIVITY";
	public static final String BEFORE_LOCATION_QUERY = "before";
	
	private static final String START_FROM_GOTOCHI_APP = "start_from_gotochi_app";
	
	@Override public void onReceive(Context context, Intent intent) {
		
		if(intent.getAction().equals(LOCATION_CHANGE_ACTION)) {
			PrefecturesCode cur = (PrefecturesCode) intent.getSerializableExtra(CURRENT_LOCATION);
			PrefecturesCode before = intent.hasExtra(BEFORE_LOCATION) ? 
					(PrefecturesCode)intent.getSerializableExtra(BEFORE_LOCATION) :
					null;
					
			Intent gotochiIntent = new Intent();
			StringBuilder builder = new StringBuilder()
				.append(URI_GOTOCHI_SCHEME).append("://")
				.append(cur.toString().toLowerCase());
			if(before != null) {
				builder.append("?")
					.append(BEFORE_LOCATION_QUERY).append("=").append(before.code);
			}
			gotochiIntent.setData(Uri.parse(builder.toString()));
			
			gotochiIntent.addCategory(CATEGORY_GOTOCHI);
			gotochiIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			
			gotochiIntent.putExtra(START_FROM_GOTOCHI_APP, true);
			
			try {
				context.startActivity(gotochiIntent);
			}catch(ActivityNotFoundException e) {
				//TODO エラーハンドリングどうしようか
				e.printStackTrace();
			}
		}
	}

}
