package idv.Zero.KerKerInput;

import idv.Zero.KerKerInput.KerKerInputCore.InputMode;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.preference.PreferenceManager;
import android.view.*;
import android.view.inputmethod.EditorInfo;

public class KerKerInputService extends InputMethodService {
	private KerKerInputCore _core = null;
	private KeyboardView _currentKBView = null;
	
	public KerKerInputService()
	{
		super();
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
		_currentKBView = (KeyboardView) View.inflate(this, R.layout.keyboard_view, null);
		_currentKBView.setKeyboard(_core.getKeyboardManager().getCurrentKeyboard());
		_currentKBView.setOnKeyboardActionListener(_core);
		_core.getKeyboardManager().setKeyboardView(_currentKBView);
		
		return _currentKBView;
	}
	
	public void restoreKerKerKeyboardView()
	{
		setInputView(_currentKBView);
	}
	
	public void onStartInputView(EditorInfo info, boolean restarting)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		_core.setShouldVibrate(prefs.getBoolean("vibration", false));
		_core.setShouldMakeNoise(prefs.getBoolean("audio", true));
		
		// Force generate a keyboard
		_core.getKeyboardManager().resetKeyboard();
		_core.setCurrentMode(InputMode.MODE_ABC);
		
		if (_core.getCurrentInputMethod() != null)
		{
			_core.getCurrentInputMethod().onLeaveInputMethod();
			_core.getCurrentInputMethod().onEnterInputMethod();
		}
		
		_core.getKeyboardManager().setImeOptions(info.imeOptions);

		// Refresh all cache
		_currentKBView.closing();
	}
	
	public void onUnbindInput()
	{
		super.onUnbindInput();
		
		_core.releaseSounds();
	}
	
	public void onFinishInputView(boolean finishing)
	{
		super.onFinishInputView(finishing);
		if (_core.getCurrentInputMethod() != null)
			_core.getCurrentInputMethod().onLeaveInputMethod();
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
}
