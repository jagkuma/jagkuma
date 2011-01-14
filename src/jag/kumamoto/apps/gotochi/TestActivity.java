package jag.kumamoto.apps.gotochi;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class TestActivity extends Activity{
	
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
    	Button btn = new Button(this);
    	btn.setText("TESTTEST");
    	btn.setOnClickListener(new View.OnClickListener() {
			
			@Override public void onClick(View v) {
				ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
				for(RunningTaskInfo info : am.getRunningTasks(Integer.MAX_VALUE)) {
					Log.i("Activity", info.toString());
				}
				
			}
		});
    	
    	setContentView(btn);
	}
	
}
