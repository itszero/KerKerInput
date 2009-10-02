package idv.Zero.KerKerInput;

import android.inputmethodservice.KeyboardView;
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

	public KeyboardView getCurrentKeyboardView()
	{
		if (_currentKBView == null)
		{
			_currentKBView = new KeyboardView(_core.getFrontend(), null);
			_currentKBView.setKeyboard(getCurrentKeyboard());
			_currentKBView.setOnKeyboardActionListener(_core);
		}
		
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
		_currentKB = kb;
		if (_currentKBView != null) // It will be null if it's first run.
			_currentKBView.setKeyboard(_currentKB);		
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
		getCurrentKeyboard().setImeOptions(_core.getFrontend().getResources(), imeOptions);

		int variation = imeOptions & EditorInfo.TYPE_MASK_VARIATION;		
		switch (variation)
		{
		case EditorInfo.TYPE_TEXT_VARIATION_URI:
			setKeyboardMode(R.id.mode_url);
			break;
		case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
			setKeyboardMode(R.id.mode_email);
			break;
		default:
			setKeyboardMode(R.id.mode_normal);
		}
	}
}
