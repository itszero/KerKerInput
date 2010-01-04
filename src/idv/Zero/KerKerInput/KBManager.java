package idv.Zero.KerKerInput;

import idv.Zero.KerKerInput.KerKerInputCore.InputMode;
import android.util.Log;
import android.view.inputmethod.EditorInfo;

public class KBManager {
	public static final int KEYCODE_SYM = -100;
	public static final int KEYCODE_SYM_ALT = -101;
	public static final int KEYCODE_ABC = -102;
	public static final int KEYCODE_IME = -103;
	public static final int KEYCODE_NEXT_IME = -104;
	public static final int KEYCODE_DO_OUTPUT_CHARS = -105;
	public static final int KEYCODE_DPAD_UP = -106;
	public static final int KEYCODE_DPAD_DOWN = -107;
	public static final int KEYCODE_DPAD_LEFT = -108;
	public static final int KEYCODE_DPAD_RIGHT = -109;
	public static final int KEYCODE_IME_MENU = -110;
	
	private KerKerInputCore _core = null;
	private KeyboardView _currentKBView = null;
	private Keyboard _currentKB = null;
	private int _kbMode = R.id.mode_normal;
	private int imeOptions = 0;
	private NativeKeyboardTypes _currentKBType = NativeKeyboardTypes.MODE_ABC;
	
	public KBManager(KerKerInputCore core)
	{
		_core = core;
	}

	public void resetKeyboard()
	{
		_currentKB = null;
		getCurrentKeyboard();
	}
	
	public void setKeyboardView(KeyboardView kv)
	{
		Log.i("KBManager", "setKeyboardView = " + kv);
		_currentKBView = kv;
		return;
	}
	
	public KeyboardView getCurrentKeyboardView()
	{
		return _currentKBView;
	}
	
	public Keyboard getCurrentKeyboard()
	{
		if (_currentKB == null)
			setNativeKeyboard(NativeKeyboardTypes.MODE_ABC);
		
		return _currentKB;
	}
	
	public enum NativeKeyboardTypes {MODE_ABC, MODE_SYM, MODE_SYM_ALT, MODE_IME};
	
	public void setNativeKeyboard(NativeKeyboardTypes type)
	{
		_currentKBType = type;
		switch(type)
		{
		case MODE_ABC:
			setCurrentKeyboardFromResource(R.xml.kbd_qwerty, getKeyboardMode());
			break;
		case MODE_SYM:
			setCurrentKeyboardFromResource(R.xml.kbd_sym, getKeyboardMode());
			break;
		case MODE_SYM_ALT:
			setCurrentKeyboardFromResource(R.xml.kbd_sym_alt, getKeyboardMode());
			break;
		case MODE_IME:
			setCurrentKeyboard(_core.getCurrentInputMethod().getDesiredKeyboard());
			break;
		}
	}
	
	public void setCurrentKeyboardFromResource(int kbResource, int mode)
	{
		setCurrentKeyboard(new Keyboard(_core.getFrontend(), kbResource, mode));
	}
	
	public void setCurrentKeyboard(Keyboard kb)
	{
		if (kb == null)
			return;
		
		_currentKB = kb;
		Log.i("KBManager", "currentKBView = " + _currentKBView);
		if (_currentKBView != null) // It will be null if it's first run.
		{
			Log.i("KBManager", "Update KeyboardView for new Keyboard");
			_currentKBView.setKeyboard(_currentKB);
			applyIMEOptions();
		}
	}
	
	public void setKeyboardMode(int kbmode)
	{
		_kbMode = kbmode;
		setNativeKeyboard(_currentKBType);
	}
	
	public int getKeyboardMode()
	{
		return _kbMode;
	}

	public void setImeOptions(int inputType)
	{
		imeOptions = inputType;
		applyIMEOptions();
	}
	
	public void applyIMEOptions()
	{
		if (_currentKB == null)
			return;
		
		_currentKB.setImeOptions(_core.getFrontend().getResources(), imeOptions);

		int variation = imeOptions & EditorInfo.TYPE_MASK_VARIATION;		
		switch (variation)
		{
		case EditorInfo.TYPE_TEXT_VARIATION_URI:
			_core.setCurrentMode(InputMode.MODE_ABC);
			_kbMode = R.id.mode_url;
			break;
		case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
			_core.setCurrentMode(InputMode.MODE_ABC);
			_kbMode = R.id.mode_email;
			break;
		default:
			_kbMode = R.id.mode_normal;
		}
		
		if (_currentKBView != null)
			_currentKBView.setKeyboard(getCurrentKeyboard());
	}
}
