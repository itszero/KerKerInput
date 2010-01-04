package idv.Zero.KerKerInput;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.view.inputmethod.EditorInfo;

public class Keyboard extends android.inputmethodservice.Keyboard {

    public Keyboard(Context context, int xmlLayoutResId) {
		super(context, xmlLayoutResId);
	}


	public Keyboard(Context context, int xmlLayoutResId, int modeId) {
		super(context, xmlLayoutResId, modeId);
	}

	private Key keyEnter;
    
    @Override
    protected Key createKeyFromXml(Resources res, Row parent, int x, int y, XmlResourceParser parser) {
        Key key = new Key(res, parent, x, y, parser);
        if (key.codes[0] == 10) {
        	keyEnter = key;
        }
        key.popupCharacters = key.label;
        return key;
    }
    
    void setImeOptions(Resources res, int options) {
        if (keyEnter == null) {
            return;
        }
        
        switch (options & (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
            case EditorInfo.IME_ACTION_GO:
            	keyEnter.iconPreview = null;
            	keyEnter.icon = null;
            	keyEnter.label = "前往";
                break;
            case EditorInfo.IME_ACTION_DONE:
            	keyEnter.iconPreview = null;
            	keyEnter.icon = null;
            	keyEnter.label = "完成";
                break;
            case EditorInfo.IME_ACTION_NEXT:
            	keyEnter.iconPreview = null;
            	keyEnter.icon = null;
            	keyEnter.label = "下格";
                break;
            case EditorInfo.IME_ACTION_SEARCH:
            	keyEnter.iconPreview = res.getDrawable(R.drawable.sym_keyboard_search);
            	keyEnter.icon = res.getDrawable(R.drawable.sym_keyboard_search);
            	keyEnter.label = null;
                break;
            case EditorInfo.IME_ACTION_SEND:
            	keyEnter.iconPreview = null;
            	keyEnter.icon = null;
                keyEnter.label = "送出";
                break;
            default:
            	keyEnter.icon = res.getDrawable(R.drawable.sym_keyboard_return);
            	keyEnter.label = null;
                break;
        }
    }
}