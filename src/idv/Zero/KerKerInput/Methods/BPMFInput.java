package idv.Zero.KerKerInput.Methods;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.view.KeyEvent;
import idv.Zero.KerKerInput.KerKerInputCore;
import idv.Zero.KerKerInput.Keyboard;
import idv.Zero.KerKerInput.R;
import idv.Zero.KerKerInput.Methods.BPMFInputHelpers.ZhuYinComponentHelper;

public class BPMFInput extends idv.Zero.KerKerInput.IKerKerInputMethod {
	private enum InputState {STATE_INPUT, STATE_CHOOSE};
	private InputState currentState;
	private String inputBufferRaw = "";
	private List<CharSequence> _currentCandidates;
	private HashMap<Character, String> K2N;
	private String _dbpath;
	private int _currentPage;
	private int _totalPages;
	
	private SQLiteDatabase db;
	
	public void initInputMethod(KerKerInputCore core) {
		super.initInputMethod(core);
		
		initKeyNameData();
		_currentPage = 0;
		_currentCandidates = new ArrayList<CharSequence>();

		Context c = core.getFrontend();
		
		_dbpath = c.getDatabasePath("cin.db").toString();

		try
		{
			db = SQLiteDatabase.openDatabase(_dbpath, null, SQLiteDatabase.OPEN_READONLY);
			db.setLocale(Locale.TRADITIONAL_CHINESE);
			db.close();
		}
		catch(SQLiteException ex)
		{
			System.out.println("Error, no database file found. Copying...");

			// Create the database (and the directories required) then close it.
			db = c.openOrCreateDatabase("cin.db", 0, null);
			db.close();

			try {
				OutputStream dos = new FileOutputStream(_dbpath);
				InputStream dis = c.getResources().openRawResource(R.raw.bpmf);
				byte[] buffer = new byte[4096];
				while (dis.read(buffer) > 0)
				{
					dos.write(buffer);
				}
				dos.flush();
				dos.close();
				dis.close();				
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}
	
	public void onEnterInputMethod()
	{
		currentState = InputState.STATE_INPUT;
		inputBufferRaw = "";
		updateCandidates();
		// Copied, re-open it.
		db = SQLiteDatabase.openDatabase(_dbpath, null, SQLiteDatabase.OPEN_READONLY);
		db.setLocale(Locale.TRADITIONAL_CHINESE);
	}
	
	public void onLeaveInputMethod()
	{
		db.close();
	}
	
	public String getName()
	{
		return "注音";
	}

	public Keyboard getDesiredKeyboard() {
		return new Keyboard(_core.getFrontend(), R.xml.kb_zhuyin, R.id.mode_normal);
	}

	public void commitCurrentComposingBuffer() {
		commitText(getCompositeString());
	}

	public boolean onKeyEvent(int keyCode, int[] keyCodes) {
		return handleBPMFKeyEvent(keyCode, keyCodes);
	}
	
	private boolean handleBPMFKeyEvent(int keyCode, int[] keyCodes) {
		if (currentState == InputState.STATE_INPUT)
		{
			if (keyCode == Keyboard.KEYCODE_DELETE)
			{
				if (inputBufferRaw.length() > 0)
				{
					inputBufferRaw = inputBufferRaw.substring(0, inputBufferRaw.length() - 1);
				}
				else
					_core.getFrontend().sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
			}
			else if (keyCode == 32) // space
			{
				if (_currentCandidates.size() > 0)
				{
					currentState = InputState.STATE_CHOOSE;
					handleBPMFKeyEvent(32, null);
				}
				else
					_core.getFrontend().sendKeyChar((char) keyCode);
			}
			else if (keyCode == 10) // RETURN
			{
				if (inputBufferRaw.length() > 0)
					commitText(getCompositeString());
				else
					_core.getFrontend().sendKeyChar((char) keyCode);
			}
			else
			{
				char c = (char)keyCode;
				inputBufferRaw = ZhuYinComponentHelper.getComposedRawString(inputBufferRaw, Character.toString(c));
				
				// 如果是音調符號，直接進入選字模式。
				if (inputBufferRaw.length() > 0 && (c == '3' || c == '4' || c == '6' || c == '7'))
					currentState = InputState.STATE_CHOOSE;
			}
			_core.setCompositeBuffer(getCompositeString());
			updateCandidates();
		}
		else if (currentState == InputState.STATE_CHOOSE)
		{
			switch (keyCode)
			{
			case Keyboard.KEYCODE_DELETE:
				currentState = InputState.STATE_INPUT;
				if (inputBufferRaw.length() > 0)
				{
					inputBufferRaw = inputBufferRaw.substring(0, inputBufferRaw.length() - 1);
					_core.setCompositeBuffer(getCompositeString());
					updateCandidates();
				}
				else
					Log.e("BPMFInput", "InputBuffer is requested to delete, but the buffer is empty");
				
				break;
			// TODO: Make sure DPad & Keyboard L/R keyCode
			case -103: // DPad Left
				if (_currentPage > 0)
					_currentPage--;
				else
					_currentPage = _totalPages - 1;
				break;
			case -104: // DPad Right
				if (_currentPage < _totalPages - 1)
					_currentPage++;
				else
					_currentPage = 0;
				break;
			case ' ':
			case 10:
				keyCode = KeyEvent.KEYCODE_0;
			default:
				if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9)
				{
				    // This prevents user hit tone symbol twice makes program crash.
				    // It's because first sign interpreted as bpmf symbol, and second one is treated as candidate choose.
				    // Also prevent user select non-exist candidates on physical kb.
					if (_totalPages < 0) break;
					
					int CANDIDATES_PER_PAGE = (_currentCandidates.size() / _totalPages);
				    if ((_currentPage * CANDIDATES_PER_PAGE + keyCode - KeyEvent.KEYCODE_0 - 1) < _currentCandidates.size())
				    {
				        commitCandidate(_currentPage * CANDIDATES_PER_PAGE + keyCode - KeyEvent.KEYCODE_0 - 1);
				    }
				}
				break;
			}
		}
		
		return true;
	}

	private CharSequence getCompositeString() {
		StringBuilder str = new StringBuilder();
		int length = inputBufferRaw.length();
		for(int i=0;i<length;i++)
		{
			if (K2N.containsKey(inputBufferRaw.charAt(i)))
				str.append(K2N.get(inputBufferRaw.charAt(i)));
		}
		return str.toString();
	}

	private void updateCandidates() {
		if (inputBufferRaw.length() == 0)
		{
			_currentCandidates.clear();
			_core.hideCandidatesView();
			return;
		}
		
		try
		{
			// Cursor currentQuery = db.rawQuery("Select val from bpmf where key glob '" + inputBufferRaw.toString() + "*'", null);
			Cursor currentQuery = db.rawQuery("Select val from bpmf where key >= '" + inputBufferRaw.toString() + "' AND key < '" + inputBufferRaw.toString() + "zzz'", null);
			if (currentQuery.getCount() == 0)
			{
				inputBufferRaw = inputBufferRaw.substring(0, inputBufferRaw.length() - 1);
				_currentCandidates.clear();
				_core.setCompositeBuffer(getCompositeString());
				_core.showPopup(R.string.no_such_mapping);
				_core.hideCandidatesView();
				currentState = InputState.STATE_INPUT;
				updateCandidates();
			}
			else
			{
				int count = Math.min(currentQuery.getCount(), 50);
				int colIdx = currentQuery.getColumnIndex("val");
				_currentCandidates = new ArrayList<CharSequence>(count);
				
				currentQuery.moveToNext();
				for(int i=0;i<count;i++)
				{
					String ca = currentQuery.getString(colIdx);
					_currentCandidates.add(ca);
					currentQuery.moveToNext();
				}
				_core.showCandidatesView();
				_core.setCandidates(_currentCandidates);
			}
			currentQuery.close();
		}
		catch(Exception e) {}
		finally
		{
		}
	}

	public void commitCandidate(int selectedCandidate)
	{
		if (selectedCandidate < 0)
			selectedCandidate = 0;
		if (_currentCandidates.size() == 0)
			return;
		
		commitText(_currentCandidates.get(selectedCandidate));
	}
		
	public void setTotalPages(int totalPages)
	{
		_totalPages = totalPages;
	}
	
	public void setCurrentPage(int currentPage)
	{
		_currentPage = currentPage;
	}
	
	private void commitText(CharSequence str)
	{
		_core.commitText(str);
		inputBufferRaw = "";
		updateCandidates();
		currentState = InputState.STATE_INPUT;
	}
	
	private void initKeyNameData()
	{
		K2N = new HashMap<Character, String>();			
		K2N.put(',', "ㄝ");
		K2N.put('-', "ㄦ");
		K2N.put('.', "ㄡ");
		K2N.put('/', "ㄥ");
		K2N.put('0', "ㄢ");
		K2N.put('1', "ㄅ");
		K2N.put('2', "ㄉ");
		K2N.put('3', "ˇ"); 
		K2N.put('4', "ˋ"); 
		K2N.put('5', "ㄓ");
		K2N.put('6', "ˊ");
		K2N.put('7', "˙");
		K2N.put('8', "ㄚ");
		K2N.put('9', "ㄞ");
		K2N.put(';', "ㄤ");
		K2N.put('a', "ㄇ");
		K2N.put('b', "ㄖ");
		K2N.put('c', "ㄏ");
		K2N.put('d', "ㄎ");
		K2N.put('e', "ㄍ");
		K2N.put('f', "ㄑ");
		K2N.put('g', "ㄕ");
		K2N.put('h', "ㄘ");
		K2N.put('i', "ㄛ");
		K2N.put('j', "ㄨ");
		K2N.put('k', "ㄜ");
		K2N.put('l', "ㄠ");
		K2N.put('m', "ㄩ");
		K2N.put('n', "ㄙ");
		K2N.put('o', "ㄟ");
		K2N.put('p', "ㄣ");
		K2N.put('q', "ㄆ");
		K2N.put('r', "ㄐ");
		K2N.put('s', "ㄋ");
		K2N.put('t', "ㄔ");
		K2N.put('u', "ㄧ");
		K2N.put('v', "ㄒ");
		K2N.put('w', "ㄊ");
		K2N.put('x', "ㄌ");
		K2N.put('y', "ㄗ");
		K2N.put('z', "ㄈ");
	}
}
