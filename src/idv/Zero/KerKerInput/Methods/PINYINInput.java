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

public class PINYINInput extends idv.Zero.KerKerInput.IKerKerInputMethod {
	private enum InputState {STATE_INPUT, STATE_CHOOSE, STATE_SUGGEST};
	private InputState currentState;
	private String inputBufferRaw = "";
	private List<CharSequence> _currentCandidates;
	private HashMap<Character, String> K2N;
	private HashMap<Character, Character> N2K;
	private String _dbpath;
	private String _wordCompDBPath;
	private String _phraseCompDBPath;
	private String _lastInput;
	private String _lastLastInput;
	private int _currentPage;
	private int _totalPages;

	private SQLiteDatabase db;
	private SQLiteDatabase wordCompDB;
	private SQLiteDatabase phraseCompDB;

	private char last;
	private char last2;
	private char last3;
	private boolean sound_one;
	private boolean key_blank;

	public void initInputMethod(KerKerInputCore core) {
		super.initInputMethod(core);
		
		initKeyNameData();
		_currentPage = 0;
		_currentCandidates = new ArrayList<CharSequence>();

		Context c = core.getFrontend();
		
		_dbpath = c.getDatabasePath("cin.db").toString();
		_wordCompDBPath = c.getDatabasePath("wc.db").toString();
		_phraseCompDBPath = c.getDatabasePath("pc.db").toString();

		_lastInput = "";
		_lastLastInput = "";

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
				int size = dis.available();
				byte[] buffer = new byte[size];
				dis.read(buffer);
				dos.write(buffer);
				dos.flush();
				dos.close();
				dis.close();
			} catch (IOException e) {
				e.printStackTrace();
				Log.e("PINYINInput", "excepted1: " + e.getMessage());
			}			
		}

		try {
			wordCompDB = SQLiteDatabase.openDatabase(_wordCompDBPath, null, SQLiteDatabase.OPEN_READONLY);
			wordCompDB.setLocale(Locale.TRADITIONAL_CHINESE);
			wordCompDB.close();
		}
		catch(SQLiteException ex) {
			System.out.println("Error, no database file found. Copying...");

			// Create the database (and the directories required) then close it.
			wordCompDB = c.openOrCreateDatabase("wc.db", 0, null);
			wordCompDB.close();

			try {
				OutputStream dos = new FileOutputStream(_wordCompDBPath);
				InputStream dis = c.getResources().openRawResource(R.raw.wordcomplete);
				int size = dis.available();
				byte[] buffer = new byte[size];
				dis.read(buffer);
				dos.write(buffer);
				dos.flush();
				dos.close();
				dis.close();				
			} catch (IOException e) {
				Log.e("PINYINInput", "excepted2: " + e.getMessage());
				e.printStackTrace();
			}			
		}
		try
		{
			phraseCompDB = SQLiteDatabase.openDatabase(_phraseCompDBPath, null, SQLiteDatabase.OPEN_READONLY);
			phraseCompDB.setLocale(Locale.TRADITIONAL_CHINESE);
			phraseCompDB.close();
		}
		catch(SQLiteException ex)
		{
			System.out.println("Error, no database file found. Copying...");

			// Create the database (and the directories required) then close it.
			phraseCompDB = c.openOrCreateDatabase("pc.db", 0, null);
			phraseCompDB.close();

			try {
				OutputStream dos = new FileOutputStream(_phraseCompDBPath);
				InputStream dis = c.getResources().openRawResource(R.raw.phrasecomplete);
				int size = dis.available();
				byte[] buffer = new byte[size];
				dis.read(buffer);
				dos.write(buffer);
				dos.flush();
				dos.close();
				dis.close();				
			} catch (IOException e) {
				Log.e("PINYINInput", "excepted3: " + e.getMessage());
				e.printStackTrace();
			}			
		}

		try {
			phraseCompDB = SQLiteDatabase.openDatabase(_phraseCompDBPath, null, SQLiteDatabase.OPEN_READONLY);
			phraseCompDB.setLocale(Locale.TRADITIONAL_CHINESE);
			phraseCompDB.close();
		}
		catch(SQLiteException ex) {
			System.out.println("Error, no database file found. Copying...");

			// Create the database (and the directories required) then close it.
			phraseCompDB = c.openOrCreateDatabase("pc.db", 0, null);
			phraseCompDB.close();

			try {
				OutputStream dos = new FileOutputStream(_phraseCompDBPath);
				InputStream dis = c.getResources().openRawResource(R.raw.phrasecomplete);
				int size = dis.available();
				byte[] buffer = new byte[size];
				dis.read(buffer);
				dos.write(buffer);
				dos.flush();
				dos.close();
				dis.close();				
			} catch (IOException e) {
				Log.e("PINYINInput", "excepted2: " + e.getMessage());
				e.printStackTrace();
			}			
		}

	}
	
	public void onEnterInputMethod()
	{
		currentState = InputState.STATE_INPUT;
		inputBufferRaw = "";
		_lastInput = "";
		_lastLastInput = "";
		updateCandidates();
		// Copied, re-open it.
		db = SQLiteDatabase.openDatabase(_dbpath, null, SQLiteDatabase.OPEN_READONLY);
		db.setLocale(Locale.TRADITIONAL_CHINESE);

		wordCompDB = SQLiteDatabase.openDatabase(_wordCompDBPath, null, SQLiteDatabase.OPEN_READONLY);
		wordCompDB.setLocale(Locale.TRADITIONAL_CHINESE);

		phraseCompDB = SQLiteDatabase.openDatabase(_phraseCompDBPath, null, SQLiteDatabase.OPEN_READONLY);
		phraseCompDB.setLocale(Locale.TRADITIONAL_CHINESE);
	}        
	         
	public void onLeaveInputMethod()
	{        
		db.close();
		wordCompDB.close();
		phraseCompDB.close();
	}        
	         
	public String getName()
	{        
		return "拼音";
	}        
                 
	public Keyboard getDesiredKeyboard() {
		return new Keyboard(_core.getFrontend(), R.xml.kb_pinyin, R.id.mode_normal);
	}        
                 
	public void commitCurrentComposingBuffer() {
		commitText(getCompositeString());
	}        
                 
	public boolean onKeyEvent(int keyCode, int[] keyCodes) {
		return handleBPMFKeyEvent(keyCode, keyCodes);
	}        
	         
	private boolean handleBPMFKeyEvent(int keyCode, int[] keyCodes) {
		if (currentState == InputState.STATE_INPUT || currentState == InputState.STATE_SUGGEST)
		{
		        if (keyCode == Keyboard.KEYCODE_DELETE)
		        {
				last3 = last = last2 = 0;
				if (inputBufferRaw.length() == 0)
		        		_core.getFrontend().sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);

		        	while (inputBufferRaw.length() > 0)
		        	{
		        		inputBufferRaw = inputBufferRaw.substring(0, inputBufferRaw.length() - 1);
		        	}
		        	//else
		        	//        _core.getFrontend().sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
			}
			/*else if (keyCode == 32) // space
			{
				if (_currentCandidates.size() > 0 && !(currentState == InputState.STATE_SUGGEST))
				{
					currentState = InputState.STATE_CHOOSE;
					handleBPMFKeyEvent(32, null);
				}
				else {
					_core.getFrontend().sendKeyChar((char) keyCode);
				}
			}*/
			else if (keyCode == 10) // RETURN
			{
				if (inputBufferRaw.length() > 0 && !(currentState == InputState.STATE_SUGGEST)) {
					commitText(getCompositeString());
				}
				else
					_core.getFrontend().sendKeyChar((char) keyCode);
			}
			else
			{
				char c = (char)keyCode,cx=0;
				switch(c){
					case 'n':
						if(last == 'a')
							cx='0';
						else if(last == 'e' || last == 'i' || last == 'u'|| last == 'v')
							cx='p';
						else if(last == 'o'){
							cx='m';
							last3 = last2;
							last2 = last;
							last = c;
							return true;
						}
						else
							cx='s';
						break;
					case 'g':
						if(last2 == 'a' && last == 'n')
							cx=';';
						else if((last3 == 'i' || last3 =='y')  && (last2 == 'o' && last == 'n')){
							inputBufferRaw = ZhuYinComponentHelper.getComposedRawString(inputBufferRaw, Character.toString('m'));
							cx='/';
						}
						else if(last2 == 'o' && last == 'n'){
							inputBufferRaw = ZhuYinComponentHelper.getComposedRawString(inputBufferRaw, Character.toString('j'));
							cx='/';
						}
						else if(last == 'n'){
		        				//inputBufferRaw = inputBufferRaw.substring(0, inputBufferRaw.length() - 1);
							cx='/';
						}
						else
							cx='e';
						break;
					case 'h':
						if(last == 'z')
							cx = '5';
						else if(last == 'c')
							cx = 't';
						else if(last == 's')
							cx = 'g';
						else
							cx = 'c';
						break;
					case 'r':
						if(last == 'e')
							cx='-';
						else
							cx='b';
						break;
					case 'i':
						if(last == 'y' || last == 'h' || last == 'r' || last == 'z' || last == 'c' || last == 'r'){
							last3 = last2;
							last2 = last;
							last = c;
							return true;
						}
						else if(last == 'a')
							cx = '9';
						else if(last == 'e')
							cx = 'o';
						else if(last == 'u')
							cx = 'o';
						else
							cx='u';
						break;
					case 'u':
						if(last == 'o')
							cx = '.';
						else if(last == 'i')
							cx = '.';
						else if(last == 'y')
							cx = 'm';
						else if(last == 'j' || last == 'q' || last == 'x' || last == 'u' || last == 'y')
							cx = 'm';
						else
							cx='j';
						break;
					case 'o':
						if(last == 'a')
							cx = 'l';
						else if(last == 'i' && last2 !=0){
							last3 = last2;
							last2 = last;
							last = c;
							return true;
						}
						else
							cx='i';
						break;
					case 'e':
						if(last == 'y' ||last == 'i' ||last == 'u' ||   last == 'v')
							cx=',';
						else
							cx='k';
						break;
					case ',':
						if(inputBufferRaw.length() == 0)
							_core.commitText("，");
						return true;
					case '.':
						if(inputBufferRaw.length() == 0)
							_core.commitText("。");
						return true;
					case '?':
						if(inputBufferRaw.length() == 0)
							_core.commitText("？");
						return true;
					case '!':
						if(inputBufferRaw.length() == 0)
							_core.commitText("！");
						return true;
					case '6':
						if(inputBufferRaw.length() == 0)
							_core.commitText("：");
						return true;
					case '7':
						if(inputBufferRaw.length() == 0)
							_core.commitText("「");
						return true;
					case '8':
						if(inputBufferRaw.length() == 0)
							_core.commitText("」");
						return true;
					case '9':
						if(inputBufferRaw.length() == 0)
							_core.commitText("、");
						return true;
					case '0':
						if(inputBufferRaw.length() == 0)
							_core.commitText("…");
						return true;
					case ' ':
						if(inputBufferRaw.length() == 0)
							_core.commitText(" ");
						return true;
					default :
						if (N2K.containsKey(c))
							cx=N2K.get(c);
				}
				last3=last2;
				last2=last;
				last=c;
				inputBufferRaw = ZhuYinComponentHelper.getComposedRawString(inputBufferRaw, Character.toString(cx));
				
				// 如果是音調符號，直接進入選字模式。
				if (inputBufferRaw.length() > 0 && (c == ' ' || c == '1' || cx == '3' || cx == '4' || cx == '6' || cx == '7')){
					if(c=='1')
						sound_one=true;
					else if(c==' ')
						key_blank=true;
					currentState = InputState.STATE_CHOOSE;
				}

			}
			_core.setCompositeBuffer(getCompositeString());
			updateCandidates();
		}
		if (currentState == InputState.STATE_CHOOSE)
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
					Log.e("PINYINInput", "InputBuffer is requested to delete, but the buffer is empty");
				
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
				last3=last=last2=0;
				break;
			}
		}
		else if (currentState == InputState.STATE_SUGGEST) {
			switch (keyCode)
			{
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
			default:
				if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9 && (keyCode != 10 && keyCode != 32))
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
//			str.append(inputBufferRaw.charAt(i));
		}
		return str.toString();
	}

	private void updateCandidates() {
		if (inputBufferRaw.length() == 0)
		{
			_currentCandidates.clear();
			if (_lastInput.equals(""))
			{
				_core.hideCandidatesView();
			}
			else
			{
				try 
				{
					Cursor currentQuery = wordCompDB.rawQuery("Select val from word_complete where key = '" + _lastInput + "' ORDER BY cnt DESC", null);
					int count = currentQuery.getCount();
					int colIdx = currentQuery.getColumnIndex("val");
					_currentCandidates = new ArrayList<CharSequence>(count);
					
					currentQuery.moveToNext();
					for(int i=0;i<count;i++)
					{
						String ca = currentQuery.getString(colIdx);
						_currentCandidates.add(ca);
						currentQuery.moveToNext();
					}
					currentQuery.close();

					if(!(_lastInput.equals("") || _lastLastInput.equals("")))
					{
						currentQuery = phraseCompDB.rawQuery("Select val from word_complete where key = '" + _lastLastInput + _lastInput + "' ORDER BY cnt DESC", null);
						count = currentQuery.getCount();
						colIdx = currentQuery.getColumnIndex("val");
						
						currentQuery.moveToNext();
						for(int i=0;i<count;i++)
						{
							String ca = currentQuery.getString(colIdx);
							if (i==0)
								_currentCandidates.add(0, ca);
							else
								_currentCandidates.add(ca);
							currentQuery.moveToNext();
						}
						currentQuery.close();
					}

					_core.showCandidatesView();
					_core.setCandidates(_currentCandidates);
					currentQuery.close();
				}
				catch(Exception e) 
				{
					Log.e("BPMFInput", "" + e.getMessage());
				}
				finally {}
			}
			if ((!_lastInput.equals("")) && (!_lastLastInput.equals("")))
			{
				try 
				{
					Cursor currentQuery = phraseCompDB.rawQuery("Select val from word_complete where key = '" + _lastLastInput + _lastInput + "' ORDER BY cnt DESC", null);
					int count = currentQuery.getCount();
					int colIdx = currentQuery.getColumnIndex("val");
					
					currentQuery.moveToNext();
					for(int i=0;i<count;i++)
					{
						String ca = currentQuery.getString(colIdx);
						_currentCandidates.add(ca);
						currentQuery.moveToNext();
					}
					_core.showCandidatesView();
					_core.setCandidates(_currentCandidates);
					currentQuery.close();
				}
				catch(Exception e) 
				{
					Log.e("BPMFInput", "" + e.getMessage());
				}
				finally {}
			}
			return;
		}
		
		try
		{
			String query;
			if(sound_one){
				query = "Select val from bpmf where key = '" + inputBufferRaw.toString() + "'";
				sound_one = false;
			}
			else if(key_blank){
				query = "Select val from bpmf where key = '" + inputBufferRaw.toString() + "' OR key = '" + inputBufferRaw.toString() + "7'";
				key_blank = false;
			}
			else{
				query = "Select val from bpmf where key >= '" + inputBufferRaw.toString() + "' AND key < '" + inputBufferRaw.toString() + "zzz'";
			}
			Cursor currentQuery = db.rawQuery(query, null);
			if (currentQuery.getCount() == 0)
			{
				inputBufferRaw = inputBufferRaw.substring(0, inputBufferRaw.length() - 1);
				_currentCandidates.clear();
				_core.setCompositeBuffer(getCompositeString());
				//_core.showPopup(R.string.no_such_mapping);
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
				String _ca = "";
				for(int i=0;i<count;i++)
				{
					String ca = currentQuery.getString(colIdx);
					// bz: list are sorted by sqlite3, skip repeating word(s)
					if ( !ca.equals(_ca) )
					{
						_currentCandidates.add(ca);
					}
					currentQuery.moveToNext();
					_ca = ca;
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
		String _history = (_lastInput + str.toString()) ;
		int _length = _history.length();

		if (_length >=2) {
			_lastInput = _history.substring(_length-1);
			_lastLastInput = _history.substring(_length-2, _length-1);
		}
		else if (_length == 1) {
			_lastInput = _history.substring(_length-1);
			_lastLastInput = "";
		}
		else {
			_lastInput = "";
			_lastLastInput = "";
		}

		inputBufferRaw = "";
		updateCandidates();
		currentState = InputState.STATE_SUGGEST;
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
		N2K = new HashMap<Character, Character>();			
		N2K.put('b', '1');
		N2K.put('p', 'q');
		N2K.put('m', 'a');
		N2K.put('f', 'z');
		N2K.put('d', '2');
		N2K.put('t', 'w');
		N2K.put('n', 's');
		N2K.put('l', 'x'); 
		N2K.put('g', 'e'); 
		N2K.put('k', 'd');
		N2K.put('h', 'c');
		N2K.put('j', 'r');
		N2K.put('q', 'f');
		N2K.put('x', 'v');
		N2K.put('e', ',');
		N2K.put('r', 'b');
		N2K.put('z', 'y');
		N2K.put('c', 'h');
		N2K.put('s', 'n');
		N2K.put('i', 'u');
		N2K.put('u', 'j');
		N2K.put('v', 'm');
		N2K.put('a', '8');
		N2K.put('o', 'i');
		N2K.put('e', 'k');
		N2K.put('y', 'u');
		N2K.put('w', 'j');
		N2K.put('3', '3');
		N2K.put('4', '4');
		N2K.put('2', '6');
		N2K.put('5', '7');
	}
}
