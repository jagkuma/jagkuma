package jag.kumamoto.apps.gotochi;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends PrefecturesActivityBase {
	
	@Override protected final PrefecturesCode getPrefecturesCode() {
		return PrefecturesCode.Kumamoto;
	}
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
       setContentView(R.layout.main);
        
        setupViewEventHandler();
    }
    
    /**
     * ビューのイベントハンドラの設定を行う
     */
    private void setupViewEventHandler() {
    	
        findViewById(R.id_main.btnAmbition).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) {
				
				//野望を表示するよ!
				Toast.makeText(MainActivity.this, getResources().getString(R.string.ambition), 
						Toast.LENGTH_LONG).show();
			}
		});
    }
    
}