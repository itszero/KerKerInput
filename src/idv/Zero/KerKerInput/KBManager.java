package idv.Zero.KerKerInput;

import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;

public class KBManager {
	public static final int KEYCODE_SYM = -100;
	public static final int KEYCODE_SYM_ALT = -101;
	public static final int KEYCODE_ABC = -102;
	public static final int KEYCODE_IME = -103;
	public static final int KEYCODE_NEXT_IME = -104;
	public static final int KEYCODE_DO_OUTPUT_CHARS = -105;
	
	private KerKerInputCore _core = null;
	private KeyboardView _currentKBView = null;
	private Keyboard _currentKB = null;
	
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
			_core.requestNextInputMethod();
		
		return _currentKB;
	}
	
	public enum NativeKeyboardTypes {MODE_ABC, MODE_SYM, MODE_SYM_ALT, MODE_IME};
	
	public void setNativeKeyboard(NativeKeyboardTypes mode)
	{
		switch(mode)
		{
		case MODE_ABC:
			setCurrentKeyboardFromResource(R.xml.kbd_qwerty, R.id.mode_normal);
			break;
		case MODE_SYM:
			setCurrentKeyboardFromResource(R.xml.kbd_sym, R.id.mode_normal);
			break;
		case MODE_SYM_ALT:
			setCurrentKeyboardFromResource(R.xml.kbd_sym_alt, R.id.mode_normal);
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
}
