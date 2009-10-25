package idv.Zero.KerKerInput;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.*;
import android.view.inputmethod.EditorInfo;

public class KerKerInputService extends InputMethodService {
	private String updateServiceURL = "http://zero.itszero.info/KerKerInput/version.dat";
	private KerKerInputCore _core = null;
	private KeyboardView _currentKBView = null;
	
	public KerKerInputService()
	{
		super();
		// android.os.Debug.waitForDebugger();
		
		// invokeVersionCheck();
		_core = new KerKerInputCore(this);		
	}
	
	@Override
	public void onInitializeInterface()
	{
		_core.initCore();
	}
	
	@Override
	public View onCreateInputView()
	{
		if (_currentKBView == null)
		{
			_currentKBView = new KeyboardView(_core.getFrontend(), null);
			_currentKBView.setKeyboard(_core.getKeyboardManager().getCurrentKeyboard());
			_currentKBView.setOnKeyboardActionListener(_core);
		}
		_core.getKeyboardManager().setKeyboardView(_currentKBView);
		
		return _currentKBView;
	}

	public void onStartInput(EditorInfo info, boolean restarting)
	{
		_core.getKeyboardManager().setImeOptions(info.imeOptions);
	}
	
	public void onStartInputView(EditorInfo info, boolean restarting)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		_core.setShouldVibrate(prefs.getBoolean("vibration", false));
		_core.setShouldMakeNoise(prefs.getBoolean("audio", true));
	}
	
	public void onUnbindInput()
	{
		_core.releaseSounds();
	}
	
	@Override
	public View onCreateCandidatesView()
	{
		return _core.requestCandidatesView();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent e)
	{
		if (!_core.onKeyDown(generateVKBCode(e), e))
			return super.onKeyDown(keyCode, e);
		else
			return true;
	}
	
	@Override
	public boolean onKeyMultiple(int keyCode, int count, KeyEvent e)
	{
		if (!_core.onKeyMultiple(generateVKBCode(e), count, e))
			return super.onKeyMultiple(keyCode, count, e);
		else
			return true;
	}
	
	private int generateVKBCode(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.KEYCODE_DEL)
			return -5;
		else if (e.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP)
			return KBManager.KEYCODE_DPAD_UP;
		else if (e.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN)
			return KBManager.KEYCODE_DPAD_DOWN;
		else if (e.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT)
			return KBManager.KEYCODE_DPAD_LEFT;
		else if (e.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT)
			return KBManager.KEYCODE_DPAD_RIGHT;
		
		return e.getUnicodeChar();
	}
	
	private void invokeVersionCheck()
	{
		final Handler AlertDialogShowCallBack = new Handler();
		
		new Thread() {
		    public void run() {		        
        		try {
            		int currentVerCode = KerKerInputService.this.getPackageManager().getPackageInfo("idv.Zero.KerKerInput", 0).versionCode;
            		String currentVersion = KerKerInputService.this.getPackageManager().getPackageInfo("idv.Zero.KerKerInput", 0).versionName;
            		final String remoteVerInfo[] = FileDownload.getContent(updateServiceURL).split("\n");
            		if (remoteVerInfo.length >= 4)
            		{
            		    int remoteVerCode = Integer.parseInt(remoteVerInfo[0]);
            		    if (remoteVerCode > currentVerCode)
            		    {
            		        final String update_str = KerKerInputService.this.getResources().getText(R.string.update_str).toString().replace("{CURRENT_VER}", currentVersion).replace("{REMOTE_VER}", remoteVerInfo[1]).replace("{UPDATE_MSG}", remoteVerInfo[3].replace("\\n", "\n")+"\n");
            		        final Runnable createAlertDialog = new Runnable() {
            		            public void run() {            		                
                                    new AlertDialog.Builder(KerKerInputService.this).setTitle(R.string.app_name).setMessage(update_str).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).setPositiveButton(R.string.download, new DialogInterface.OnClickListener(){
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent i = new Intent(Intent.ACTION_VIEW);
                                            i.setData(Uri.parse(remoteVerInfo[2]));
                                            KerKerInputService.this.startActivity(i);
                                            dialog.dismiss();
                                        }
                                    }).show();
                                }
                            };
                            AlertDialogShowCallBack.post(createAlertDialog);
            		    }
            		}
        		}
        		catch(Exception ex)
        		{
        		    ex.printStackTrace();
        		}
		    }
        }.start();
	}
}
