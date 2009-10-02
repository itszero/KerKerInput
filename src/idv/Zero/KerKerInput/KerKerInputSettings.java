package idv.Zero.KerKerInput;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class KerKerInputSettings extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        // android.os.Debug.waitForDebugger();
        
        ListView lstView = (ListView) findViewById(R.id.lstSettings);
        lstView.setAdapter(new SettingsListAdapter(this));
    }
}
