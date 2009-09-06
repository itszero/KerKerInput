package idv.Zero.KerKerInput;

import java.util.ArrayList;
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
import android.graphics.Color;
import android.graphics.Paint;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Handler;

public class KerKerInputCore implements OnKeyboardActionListener {
	private KerKerInputService _frontEnd = null;
	private KBManager _kbm = null;
	private ArrayList<IKerKerInputMethod> _methods;
	private IKerKerInputMethod _currentMethod;
	private CandidatesViewContainer _candidatesContainer;
	private Handler _handler;
	private PopupWindow _winMsg;
	private TextView _txtvMsg;
	private Paint _pntText;
	
	public KerKerInputCore(KerKerInputService fe)
	{
		_frontEnd = fe;
		_kbm = new KBManager(this);
		_methods = new ArrayList<IKerKerInputMethod>();
		_currentMethod = null;		
		_handler = new Handler();
		
		_pntText = new Paint();
		_pntText.setColor(Color.WHITE);
		_pntText.setAntiAlias(true);
		_pntText.setTextSize(24);
		_pntText.setStrokeWidth(0);
	}
	
	public void initCore()
	{
		_winMsg = new PopupWindow(_frontEnd);
		_winMsg.setWindowLayoutMode(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		_winMsg.setBackgroundDrawable(null);
		_txtvMsg = (TextView) getInflater().inflate(R.layout.candidates_preview, null);
		_winMsg.setContentView(_txtvMsg);
		
		registerAvailableInputMethods();
		requestNextInputMethod();
	}
	
	public void registerAvailableInputMethods() {
		IKerKerInputMethod m;
		m = new idv.Zero.KerKerInput.Methods.NoSeeing();
		_methods.add(m);
		m = new idv.Zero.KerKerInput.Methods.BPMFInput();
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
			_currentMethod = _methods.get(0);
		}
		else
		{
			int curIndex = _methods.indexOf(_currentMethod);
			_currentMethod.onExitInputMethod();
			if (_methods.size() > curIndex + 1)
				_currentMethod = _methods.get(curIndex + 1);
			else
				_currentMethod = _methods.get(0);
		}
		
		_currentMethod.initInputMethod(this);
		_currentMethod.onEnterInputMethod();
		_kbm.setCurrentKeyboard(_currentMethod.getDesiredKeyboard());
		_handler.postDelayed(new Runnable(){
			public void run() {
				try{
					showPopup(_currentMethod.getName());
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}, 700);
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
		if (e.getKeyCode() == KeyEvent.KEYCODE_BACK)
			return false;
		else if (e.getKeyCode() == KeyEvent.KEYCODE_MENU)
			return false;
		else if (e.getKeyCode() == KeyEvent.KEYCODE_ALT_LEFT)
			requestNextInputMethod();
		
		return _currentMethod.onKeyEvent(keyCode, new int[]{keyCode});
	}
	
	public boolean onKeyMultiple(int keyCode, int count, KeyEvent e)
	{
		return false;
	}

	// Virtual Keyboard input
	public void onKey(int primaryCode, int[] keyCodes) {
		if (primaryCode == -999)
			requestNextInputMethod();
		else if (!_currentMethod.wantHandleEvent(primaryCode))
		{
			// If the IME does not want the event, we assume it's an plain-English keyboard.
			switch(primaryCode) {
			case -1: // Shift Key
				Boolean isShifted = !_kbm.getCurrentKeyboard().isShifted();
				_kbm.getCurrentKeyboard().setShifted(isShifted);
				KeyboardView kv = _kbm.getCurrentKeyboardView();
				kv.setShifted(isShifted);
				
				// Dirty Hack :( Force KeyboardView wipe out its buffer...
				kv.onSizeChanged(kv.getWidth(), kv.getHeight(), 0, 0);
				break;
			case -2: // 123 Keyboard
				
				break;
			case Keyboard.KEYCODE_DELETE:
				getFrontend().sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
			default:
				if (getKeyboardManager().getCurrentKeyboard().isShifted())
					getFrontend().sendKeyChar(Character.toUpperCase((char) primaryCode));
				else
					getFrontend().sendKeyChar((char) primaryCode);
				break;
			}
		}
		else
			_currentMethod.onKeyEvent(primaryCode, keyCodes);
	}

	public void onPress(int primaryCode) {
		// TODO Auto-generated method stub
		
	}

	public void onRelease(int primaryCode) {
	}

	public void onText(CharSequence text) {
		_currentMethod.onTextEvent(text);
	}

	public void swipeDown() {
		// TODO Auto-generated method stub
		
	}

	public void swipeLeft() {
		// TODO Auto-generated method stub
		
	}

	public void swipeRight() {
		// TODO Auto-generated method stub
		
	}

	public void swipeUp() {
		// TODO Auto-generated method stub
		
	}

	public void commitCandidate(int selectedCandidate) {
		_currentMethod.commitCandidate(selectedCandidate);
	}

	public void setCandidates(List<CharSequence> candidates) {
		_candidatesContainer.setCandidates(candidates);
	}

	public void showCandidatesView() {
		_frontEnd.setCandidatesViewShown(true);
	}
	
	public void hideCandidatesView() {
		_frontEnd.setCandidatesViewShown(false);
	}
	
	public void setCompositeBuffer(CharSequence buf) {
		getConnection().setComposingText(buf, 1);
	}
	
	public void showPopup(int resid)
	{
		showPopup(getFrontend().getResources().getString(resid));
	}
	
	public void showPopup(CharSequence msg)
    {
		Log.i("POPUP", "Going to popup: " + msg);
    	_txtvMsg.setText(msg);
    	_txtvMsg.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
    	int wordWidth = (int)(_pntText.measureText(msg.toString()));
    	int popupWidth = wordWidth + _txtvMsg.getPaddingLeft() + _txtvMsg.getPaddingRight();
    	int popupHeight = _txtvMsg.getMeasuredHeight();
    	int popupX = (_kbm.getCurrentKeyboardView().getWidth() - popupWidth) / 2;
    	int popupY = -popupHeight;
    	
    	int[] offset = new int[2];
    	_kbm.getCurrentKeyboardView().getLocationInWindow(offset);
    	popupY += offset[1];
    	
    	if (_winMsg.isShowing())
    		_winMsg.update(popupX, popupY, popupWidth, popupHeight);
    	else
    	{
    		_winMsg.setWidth(popupWidth);
    		_winMsg.setHeight(popupHeight);
    		_winMsg.showAtLocation(_kbm.getCurrentKeyboardView(), Gravity.NO_GRAVITY, popupX, popupY);
    	}
    	_txtvMsg.setVisibility(View.VISIBLE);
    	
    	_handler.postDelayed(new Runnable() {
			public void run() {
				hidePopup();
			}
    	}, 1000);
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

}
