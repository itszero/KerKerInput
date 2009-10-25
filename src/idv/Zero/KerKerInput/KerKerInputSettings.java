package idv.Zero.KerKerInput;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

public class KerKerInputSettings extends PreferenceActivity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.global_settings);
        try {
			findPreference("version").setSummary(getPackageManager().getPackageInfo("idv.Zero.KerKerInput", 0).versionName);
		} catch (NameNotFoundException e) {
			Log.e("KerKerInputSettings", "Unable to retreive framework version");
		}        
    }
}
