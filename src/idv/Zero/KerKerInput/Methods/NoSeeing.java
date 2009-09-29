package idv.Zero.KerKerInput.Methods;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.inputmethodservice.Keyboard;
import android.view.KeyEvent;
import idv.Zero.KerKerInput.KerKerInputCore;
import idv.Zero.KerKerInput.R;

public class NoSeeing extends idv.Zero.KerKerInput.IKerKerInputMethod {
	private StringBuilder inputBufferRaw = new StringBuilder();
	private List<CharSequence> _currentCandidates;
	private String _dbpath;
	private int _currentPage;
	private int _totalPages;
	
	private SQLiteDatabase db;
	
	public void initInputMethod(KerKerInputCore core) {
		super.initInputMethod(core);
		
		_currentPage = 0;
		_currentCandidates = new ArrayList<CharSequence>();

		final Context c = core.getFrontend();
		
		_dbpath = c.getDatabasePath("noseeing.db").toString();

		try
		{
			db = SQLiteDatabase.openDatabase(_dbpath, null, SQLiteDatabase.OPEN_READONLY);
		}
		catch(SQLiteException ex)
		{
			db = null;
			System.out.println("Error, no database file found. Copying...");

			new Thread(new Runnable() {
				public void run() {
					// Create the database (and the directories required) then close it.
					db = c.openOrCreateDatabase("noseeing.db", 0, null);
					db.close();

					try {
						OutputStream dos = new FileOutputStream(_dbpath);
						InputStream dis = c.getResources().openRawResource(R.raw.noseeing);
						byte[] buffer = new byte[32768];
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

					db = SQLiteDatabase.openDatabase(_dbpath, null, SQLiteDatabase.OPEN_READONLY);
					db.setLocale(Locale.TRADITIONAL_CHINESE);
			}
			}).start();
		}
	}
	
	public void onEnterInputMethod()
	{
		inputBufferRaw.delete(0, inputBufferRaw.length());
		updateCandidates();
	}
	
	public void destroyInputMethod()
	{
		if (db != null)
			db.close();
	}
	
	public String getName()
	{
		return "嘸蝦米";
	}

	public Keyboard getDesiredKeyboard() {
		return new Keyboard(_core.getFrontend(), R.xml.kb_noseeing, R.id.mode_normal);
	}

	public boolean onKeyEvent(int keyCode, int[] keyCodes) {
		return handleNoSeeingInput(keyCode, keyCodes);
	}
	
	private boolean handleNoSeeingInput(int keyCode, int[] keyCodes) {
		if (keyCode == Keyboard.KEYCODE_DELETE)
		{
			if (inputBufferRaw.length() > 0)
			{
				inputBufferRaw.deleteCharAt(inputBufferRaw.length() - 1);
			}
			else
				_core.getFrontend().sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
		}
		else if (keyCode == 10)
		{
			if (inputBufferRaw.length() > 0)
				commitText(getCompositeString());
			else
				_core.getFrontend().sendKeyChar((char) keyCode);
		}
		else if(((keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) || (keyCode == 32)) && inputBufferRaw.length() > 0)
		{
			// Noseeing users tend to use SPACE to select first candidate
			if (keyCode == 32)
			{
				if (_currentCandidates.size() > 0)
					keyCode = KeyEvent.KEYCODE_0;
				else
					_core.getFrontend().sendKeyChar((char) keyCode);
			}
			
			if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9)
			{
				int CANDIDATES_PER_PAGE = (_currentCandidates.size() / _totalPages);
			    if ((_currentPage * CANDIDATES_PER_PAGE + keyCode - KeyEvent.KEYCODE_0 - 1) < _currentCandidates.size())
			    {
			        commitCandidate(_currentPage * CANDIDATES_PER_PAGE + keyCode - KeyEvent.KEYCODE_0);
			        inputBufferRaw.delete(0, inputBufferRaw.length());
			    }
			}
		}
		else
		{
			char c = (char)keyCode;
			inputBufferRaw.append(c);				
		}
		
		_core.setCompositeBuffer(getCompositeString());
		updateCandidates();
		
		return true;
	}

	private CharSequence getCompositeString() {
		StringBuilder str = new StringBuilder();
		int length = inputBufferRaw.length();
		for(int i=0;i<length;i++)
		{
			str.append(Character.toUpperCase(inputBufferRaw.charAt(i)));
		}
		return str.toString();
	}

	private void updateCandidates() {
		if (inputBufferRaw.length() == 0)
		{
			_core.hideCandidatesView();
			return;
		}
		
		if (db == null)
			return;
		
		try
		{
			//Cursor currentQuery = db.rawQuery("Select val from noseeing where key glob '" + inputBufferRaw.toString() + "*'", null);
			Cursor currentQuery = db.rawQuery("Select val from noseeing where key >= '" + inputBufferRaw.toString() + "' AND key < '" + inputBufferRaw.toString() + "zzz'", null);
			if (currentQuery.getCount() == 0)
			{
				inputBufferRaw.deleteCharAt(inputBufferRaw.length() - 1);
				_currentCandidates.clear();
				_core.setCompositeBuffer(getCompositeString());
				_core.showPopup(R.string.no_such_mapping);
				_core.hideCandidatesView();
				updateCandidates();
				return;
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
				
				_core.setCandidates(_currentCandidates);
				_core.showCandidatesView();
			}
		}
		catch(Exception e) {}
		finally
		{
		}
	}

	public void commitCandidate(int selectedCandidate)
	{
		commitText(_currentCandidates.get(selectedCandidate));
	}
	
	private void commitText(CharSequence str)
	{
		_core.getConnection().commitText(str, 1);
		_core.hideCandidatesView();
		inputBufferRaw.delete(0, inputBufferRaw.length());
	}
	
	public void setTotalPages(int totalPages)
	{
		_totalPages = totalPages;
	}
	
	public void setCurrentPage(int currentPage)
	{
		_currentPage = currentPage;
	}
}
