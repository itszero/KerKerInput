package idv.Zero.KerKerInput;

import idv.Zero.KerKerInput.KBManager.NativeKeyboardTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputConnection;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Vibrator;

public class KerKerInputCore implements OnKeyboardActionListener {
	public enum InputMode { MODE_ABC, MODE_SYM, MODE_SYM_ALT, MODE_IME };
	
	private KerKerInputService _frontEnd = null;
	private KBManager _kbm = null;
	private ArrayList<IKerKerInputMethod> _methods;
	private IKerKerInputMethod _currentMethod;
	private InputMode _currentMode;
	private CandidatesViewContainer _candidatesContainer;
	private Handler _handler;
	private PopupWindow _winMsg;
	private TextView _txtvMsg;
	private Paint _pntText;
	private Boolean shouldVibrate, shouldMakeNoise;
	private SoundPool sndPool = null;
	private HashMap<Integer, Integer> sndPoolMap = null;
	private boolean _candidatesShown;
	
	public KerKerInputCore(KerKerInputService fe)
	{
		_frontEnd = fe;
		_kbm = new KBManager(this);
		_methods = new ArrayList<IKerKerInputMethod>();
		_currentMethod = null;
		_currentMode = InputMode.MODE_ABC;
		_handler = new Handler();
		_candidatesShown = false;
		
		_pntText = new Paint();
		_pntText.setColor(Color.WHITE);
		_pntText.setAntiAlias(true);
		_pntText.setTextSize(24);
		_pntText.setStrokeWidth(0);
		
		setShouldVibrate(false);
		setShouldMakeNoise(true);
	}
	
	public void initCore()
	{
		Log.i("KerKerInputCore", "Initializing...");
		_winMsg = null;
		registerAvailableInputMethods();
	}
	
	public void registerAvailableInputMethods() {
		IKerKerInputMethod m;
		
		m = new idv.Zero.KerKerInput.Methods.BPMFInput();
		_methods.add(m);
		m = new idv.Zero.KerKerInput.Methods.NoSeeing();
		_methods.add(m);
		m = new idv.Zero.KerKerInput.Methods.CJInput();
		_methods.add(m);
	}

	public KerKerInputService getFrontend()
	{
		return _frontEnd;
	}
	
	public LayoutInflater getInflater()
	{
		return _frontEnd.getLayoutInflater();
	}
	
	public View requestInputView()
	{
		_kbm = new KBManager(this);
		return _kbm.getCurrentKeyboardView();
	}
	
	public View requestCandidatesView()
	{
		_candidatesContainer = (CandidatesViewContainer)getInflater().inflate(R.layout.candidates_bar, null);
		_candidatesContainer.initContainer(this);
		return _candidatesContainer;
	}
	
	public InputConnection getConnection()
	{
		return _frontEnd.getCurrentInputConnection();
	}
	
	public void requestNextInputMethod()
	{
		if (_currentMethod == null)
		{
			setCurrentInputMethod(_methods.get(0));
		}
		else
		{
			int curIndex = _methods.indexOf(_currentMethod);
			if (_methods.size() > curIndex + 1)
				setCurrentInputMethod(_methods.get(curIndex + 1));
			else
				setCurrentInputMethod(_methods.get(0));
		}
	}
	
	public void setCurrentInputMethod(IKerKerInputMethod method)
	{
		if (_currentMethod != null)
			_currentMethod.destroyInputMethod();
		
		_currentMethod = method;
		_currentMethod.initInputMethod(this);
		_currentMethod.onEnterInputMethod();
		_kbm.setCurrentKeyboard(_currentMethod.getDesiredKeyboard());
		showIMENamePopup(_currentMethod.getName());
	}
	
	public IKerKerInputMethod getCurrentInputMethod()
	{
		return _currentMethod;
	}
	
	public KBManager getKeyboardManager()
	{
		return _kbm;
	}

	// Physical Keyboard input
	public boolean onKeyDown(int keyCode, KeyEvent e)
	{
		// Allow user to user BACK key to hide SIP
		if (e.getKeyCode() == KeyEvent.KEYCODE_BACK || e.getKeyCode() == KeyEvent.KEYCODE_SEARCH || e.getKeyCode() == KeyEvent.KEYCODE_MENU ||
				e.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN && e.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP && e.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT && e.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT)
			return false;
		
		if (e.getKeyCode() == KeyEvent.KEYCODE_ALT_LEFT)
		{
			if (_currentMode == InputMode.MODE_IME)
			{
				_currentMethod.commitCurrentComposingBuffer();
				_currentMode = InputMode.MODE_ABC;
				showIMENamePopup("英數鍵盤");
			}
			else
			{
				_currentMode = InputMode.MODE_IME;
				if (_currentMethod == null)
					requestNextInputMethod();
				_currentMethod.onEnterInputMethod();
				showIMENamePopup(_currentMethod.getName());
			}
			return true;
		}
		
		if (_currentMode == InputMode.MODE_IME && _currentMethod != null && _currentMethod.wantHandleEvent(e.getKeyCode()))
			return _currentMethod.onKeyEvent(keyCode, new int[]{keyCode});
		
		return false;
	}
	
	public boolean onKeyMultiple(int keyCode, int count, KeyEvent e)
	{
		return false;
	}

	// Virtual Keyboard input
	public void onKey(int primaryCode, int[] keyCodes) {
		if (primaryCode == KBManager.KEYCODE_NEXT_IME)
		{
			_currentMode = InputMode.MODE_IME;
			requestNextInputMethod();
		}
		else if (primaryCode == KBManager.KEYCODE_DO_OUTPUT_CHARS)
			return; // Let IME onText listener handle it.
		else if (_currentMode != InputMode.MODE_IME || (_currentMethod != null && !_currentMethod.wantHandleEvent(primaryCode)))
		{
			// If the IME does not want the event, we assume it's an plain-English keyboard.
			switch(primaryCode) {
			case Keyboard.KEYCODE_SHIFT: // Shift Key
				if (_currentMode == InputMode.MODE_IME)
					_currentMethod.commitCurrentComposingBuffer();

				Boolean isShifted = !_kbm.getCurrentKeyboard().isShifted();
				_kbm.getCurrentKeyboard().setShifted(isShifted);
				KeyboardView kv = _kbm.getCurrentKeyboardView();
				kv.setShifted(isShifted);
				
				// Dirty Hack :( Force KeyboardView wipe out its buffer...
				kv.onSizeChanged(kv.getWidth(), kv.getHeight(), 0, 0);
				break;
			case KBManager.KEYCODE_SYM: // 123 Keyboard
				if (_currentMode == InputMode.MODE_IME)
					_currentMethod.commitCurrentComposingBuffer();

				_currentMode = InputMode.MODE_SYM;
				_kbm.setNativeKeyboard(NativeKeyboardTypes.MODE_SYM);
				showIMENamePopup("123");
				hideCandidatesView();
				
				break;
			case KBManager.KEYCODE_SYM_ALT: // 123 Keyboard
				if (_currentMode == InputMode.MODE_IME)
					_currentMethod.commitCurrentComposingBuffer();

				_currentMode = InputMode.MODE_SYM_ALT;
				_kbm.setNativeKeyboard(NativeKeyboardTypes.MODE_SYM_ALT);
				showIMENamePopup("#+=");
				hideCandidatesView();
				break;
			case KBManager.KEYCODE_ABC: // ABC Keyboard
				if (_currentMode == InputMode.MODE_IME)
					_currentMethod.commitCurrentComposingBuffer();

				_currentMode = InputMode.MODE_ABC;
				_kbm.setNativeKeyboard(NativeKeyboardTypes.MODE_ABC);
				showIMENamePopup("ABC");
				hideCandidatesView();
				break;
			case KBManager.KEYCODE_IME: // IME Keyboard
				_currentMode = InputMode.MODE_IME;
				if (_currentMethod == null)
					requestNextInputMethod();
				_currentMethod.onEnterInputMethod();
				_kbm.setNativeKeyboard(NativeKeyboardTypes.MODE_IME);
				showIMENamePopup(_currentMethod.getName());
				break;
			case Keyboard.KEYCODE_DELETE:
				getFrontend().sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
				break;
			default:
				if (getKeyboardManager().getCurrentKeyboard().isShifted())
					getFrontend().sendKeyChar(Character.toUpperCase((char) primaryCode));
				else
					getFrontend().sendKeyChar((char) primaryCode);
				break;
			}
		}
		else
			if (_currentMethod != null) _currentMethod.onKeyEvent(primaryCode, keyCodes);
		
		// User feedbacks
		if (shouldMakeNoise)
		{
			switch(primaryCode)
			{
			case '\n':
				playAudioResource(R.raw.returndown);
				playAudioResource(R.raw.returnup);
				break;
			case ' ':
				playAudioResource(R.raw.spacedown);
				playAudioResource(R.raw.spaceup);
				break;
			default:
				playAudioResource(R.raw.keydown);
				playAudioResource(R.raw.keyup);
			}
		}
		
		if (shouldVibrate)
			((Vibrator)getFrontend().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);
	}

	public void onText(CharSequence text) {
		_currentMethod.onTextEvent(text);
	}
	
	public void swipeDown() {
		getFrontend().requestHideSelf(0);
	}

	// Currently we have nothing to do here...
	public void onPress(int primaryCode) {}
	public void onRelease(int primaryCode) {}
	public void swipeLeft() {}
	public void swipeRight() {}
	public void swipeUp() {}

	public void commitCandidate(int selectedCandidate) {
		_currentMethod.commitCandidate(selectedCandidate);
	}

	public void setCandidates(List<CharSequence> candidates) {
		_candidatesContainer.setCandidates(candidates);
	}

	public void clearCandidates() {
		_candidatesContainer.setCandidates(new ArrayList<CharSequence>());
	}

	public void showCandidatesView() {
		_frontEnd.setCandidatesViewShown(true);
		_candidatesShown = true;
	}
	
	public void hideCandidatesView() {
		if (_currentMode != InputMode.MODE_IME)
		{
			_frontEnd.setCandidatesViewShown(false);
			_candidatesShown = false;
		}
		
		if (_candidatesContainer != null)
			clearCandidates();
	}
	
	public void setCompositeBuffer(CharSequence buf) {
		getConnection().setComposingText(buf, 1);
	}
	
	public void showIMENamePopup(final String imeName) {
		_handler.postDelayed(new Runnable(){
			public void run() {
				try{
					showPopup(imeName);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}, 400);
	}

	public void showPopup(int resid)
	{
		showPopup(getFrontend().getResources().getString(resid));
	}
	
	public void showPopup(final CharSequence msg)
    {
		if (!_frontEnd.isInputViewShown() && !_candidatesShown)
			return;
		
		if (_winMsg == null)
		{
			Log.i("KerKerInputCore", "Re-create popup window");
			_winMsg = new PopupWindow(_frontEnd);
			_winMsg.setWindowLayoutMode(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			_winMsg.setBackgroundDrawable(null);
			_txtvMsg = (TextView) getInflater().inflate(R.layout.candidates_preview, null);
			_winMsg.setContentView(_txtvMsg);
		}

		try
		{
			View baseView = (_frontEnd.isInputViewShown() ? _kbm.getCurrentKeyboardView() : _candidatesContainer);
			
	    	_txtvMsg.setText(msg);
	    	_txtvMsg.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
	    	int wordWidth = (int)(_pntText.measureText(msg.toString()));
	    	int popupWidth = wordWidth + _txtvMsg.getPaddingLeft() + _txtvMsg.getPaddingRight();
	    	int popupHeight = _txtvMsg.getMeasuredHeight();
	    	int popupX = (baseView.getWidth() - popupWidth) / 2;
	    	int popupY = -popupHeight;
	    	
	    	int[] offset = new int[2];
	    	baseView.getLocationInWindow(offset);
	    	popupY += offset[1];
	    	
	    	if (_winMsg.isShowing())
	    		_winMsg.update(popupX, popupY, popupWidth, popupHeight);
	    	else
	    	{
	    		_winMsg.setWidth(popupWidth);
	    		_winMsg.setHeight(popupHeight);
	    		_winMsg.showAtLocation(baseView, Gravity.NO_GRAVITY, popupX, popupY);
	    	}
	    	_txtvMsg.setVisibility(View.VISIBLE);
	    	
	    	_handler.postDelayed(new Runnable() {
				public void run() {
					if (_txtvMsg.getText() == msg.toString())
						hidePopup();
				}
	    	}, 700);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Log.e("KerKerInputCore", "Show popup failed, force reset popup window.");
			_winMsg = null;
			_txtvMsg = null;
		}
    }
    
    private void hidePopup()
    {
    	try
    	{
    		_winMsg.dismiss();
    	}
    	catch(Exception e)
    	{}
    }

	public void commitText(CharSequence str) {
		getConnection().commitText(str, 1);
		hideCandidatesView();
	}

	public void setShouldMakeNoise(Boolean shouldMakeNoise) {
		this.shouldMakeNoise = shouldMakeNoise;
		
		if (shouldMakeNoise)
		{
			sndPool = new SoundPool(6, AudioManager.STREAM_SYSTEM, 0);
		}
		else
		{
			if (sndPool != null)
				sndPool.release();
			sndPool = null;
			
			sndPoolMap = null;
		}
	}

	public Boolean shouldMakeNoise() {
		return shouldMakeNoise;
	}

	public void setShouldVibrate(Boolean shouldVibrate) {
		this.shouldVibrate = shouldVibrate;
	}

	public Boolean shouldVibrate() {
		return shouldVibrate;
	}

	public void initSounds()
	{
		if (sndPoolMap != null)
			return;
		
		sndPoolMap = new HashMap<Integer, Integer>();
		sndPoolMap.put(R.raw.keydown, sndPool.load(getFrontend().getResources().openRawResourceFd(R.raw.keydown), 1));
		sndPoolMap.put(R.raw.keyup, sndPool.load(getFrontend().getResources().openRawResourceFd(R.raw.keyup), 1));
		sndPoolMap.put(R.raw.spacedown, sndPool.load(getFrontend().getResources().openRawResourceFd(R.raw.spacedown), 1));
		sndPoolMap.put(R.raw.spacedown, sndPool.load(getFrontend().getResources().openRawResourceFd(R.raw.spaceup), 1));
		sndPoolMap.put(R.raw.returndown, sndPool.load(getFrontend().getResources().openRawResourceFd(R.raw.returndown), 1));
		sndPoolMap.put(R.raw.returnup, sndPool.load(getFrontend().getResources().openRawResourceFd(R.raw.returnup), 1));
	}
	
	public void releaseSounds()
	{
		if (sndPoolMap != null)
		{
			sndPoolMap.clear();
			sndPoolMap = null;
		}
		
		if (sndPool != null)
		{
			sndPool.release();
			sndPool = null;
		}
	}
	
	private void playAudioResource(final int resourceID)
	{
		if (sndPool == null || sndPoolMap == null)
			initSounds();
		
		int volume = ((AudioManager)getFrontend().getSystemService(Context.AUDIO_SERVICE)).getStreamVolume(AudioManager.STREAM_SYSTEM);
		Integer rid = sndPoolMap.get(resourceID);
		if (rid != null)
			sndPool.play(rid, volume, volume, 1, 0, 1f);
	}
	
	public void setCurrentMode(InputMode mode)
	{
		_currentMode = mode;
	}
}
