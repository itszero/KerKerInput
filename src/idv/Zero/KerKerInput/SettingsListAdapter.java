package idv.Zero.KerKerInput;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class SettingsListAdapter extends BaseAdapter
{
	Context _ctx;
	SharedPreferences prefs;
	
	public SettingsListAdapter(Context ctx)
	{
		_ctx = ctx;
		prefs = _ctx.getSharedPreferences("KerKerInput_settings", 0);
	}
	
	
	@Override
	public int getCount() {
		return 2;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = null;
		TextView title = null, content = null;
		switch (position)
		{
		case 0:
			v = View.inflate(_ctx, android.R.layout.simple_list_item_2, null);
			title = (TextView) v.findViewById(android.R.id.text1);
			content = (TextView) v.findViewById(android.R.id.text2);
			title.setText("輸入法版本");
			content.setText(getVersion());
			break;
		case 1:
			v = View.inflate(_ctx, android.R.layout.simple_list_item_multiple_choice, null);
			title = (TextView) v.findViewById(android.R.id.text1);
			content = (TextView) v.findViewById(android.R.id.text2);
			/*CheckBox box = (CheckBox) v.findViewById(android.R.id.checkbox);
			box.setChecked(prefs.getBoolean("audio_feedback", false));
			box.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					CheckBox vBox = (CheckBox)v;
					prefs.edit().putBoolean("audio_feedback", vBox.isChecked()).commit();
				}
			});*/
			title.setText("發出聲響");
			content.setText("按下按鍵時發出聲音，產生回饋感。");
			break;
		}
		return v;
	}

	private String getVersion()
	{
		PackageInfo pkgInfo = null;
		try {
			pkgInfo = _ctx.getPackageManager().getPackageInfo(_ctx.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (pkgInfo != null)
			return pkgInfo.versionName;
		else
			return "";
	}
}
