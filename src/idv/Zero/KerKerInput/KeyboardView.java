package idv.Zero.KerKerInput;

import android.content.Context;
import android.graphics.Color;
import android.inputmethodservice.Keyboard.Key;
import android.util.AttributeSet;

public class KeyboardView extends android.inputmethodservice.KeyboardView {

	public KeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public KeyboardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);		
	}
	
	protected boolean onLongPress(Key popupKey)
	{
		if (popupKey.codes[0] == KBManager.KEYCODE_NEXT_IME)
		{
			getOnKeyboardActionListener().onKey(KBManager.KEYCODE_IME_MENU, null);
			return true;
		}
		else
			return super.onLongPress(popupKey);
	}
}
