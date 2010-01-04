package idv.Zero.KerKerInput.Methods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import idv.Zero.KerKerInput.IKerKerInputMethod;
import idv.Zero.KerKerInput.KerKerInputCore;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 此 class 提供 .cin-based 輸入法一共同的處理機制，輸入法只需繼承
 * 此 class 針對 UI 優化即可。 
 * 
 * @author Zero
 *
 */
public class CINBasedInput extends IKerKerInputMethod {
	public enum InputState {STATE_INPUT, STATE_CHOOSE};
	
	// CINBasedInput wrapper data
	private String _strIMEIdentifier = null;
	private SQLiteDatabase _db = null;
	private String _dbPath = null;
	
	// CIN data
	private HashMap<String, String> keyToName;
	
	// State Data
	private InputState currentState;
	private String inputBufferRaw = "";
	private List<CharSequence> _currentCandidates;
	private int _currentPage;
	private int _totalPages;

	/* IME entry point and some metadata accessors */
	public CINBasedInput(String imeIdentifier, String dbPath)
	{
		_strIMEIdentifier = imeIdentifier;
		if (dbPath != null)
			_dbPath = dbPath;
	}
	
	public void initInputMethod(KerKerInputCore core) {
		super.initInputMethod(core);
		
		Context c = core.getFrontend();
		if (_dbPath == null)
			_dbPath = c.getDatabasePath("cin.db").toString();
		
		_db = SQLiteDatabase.openDatabase(_dbPath, null, SQLiteDatabase.OPEN_READONLY);
		_db.setLocale(Locale.TRADITIONAL_CHINESE);
		readKeyMapping();
		_db.close();
	}
	
	@Override
	public void onEnterInputMethod() {
		_db = SQLiteDatabase.openDatabase(_dbPath, null, SQLiteDatabase.OPEN_READONLY);
		_db.setLocale(Locale.TRADITIONAL_CHINESE);
		resetState();
	}
	
	@Override
	public void onLeaveInputMethod() {
		_db.close();
	}
	
	@Override
	public String getName() {
		String name = readMetadata("name");
		if (name == null)
			return "無名";
		else
			return name;
	}
	
	@Override
	public void commitCandidate(int currentCandidate) {
		// TODO Auto-generated method stub
	}

	@Override
	public void commitCurrentComposingBuffer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onKeyEvent(int keyCode, int[] keyCodes) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCurrentPage(int currentPage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTotalPages(int totalPages) {
		// TODO Auto-generated method stub
		
	}

	// Private methods
	private void resetState()
	{
		currentState = InputState.STATE_INPUT;
		inputBufferRaw = "";
		_currentPage = 0;
		_totalPages = 0;
		_currentCandidates = new ArrayList<CharSequence>();
	}
	
	private String readMetadata(String key)
	{
		Cursor currentQuery = _db.rawQuery("SELECT " + key + " FROM metadata_" + _strIMEIdentifier, null);
		if (currentQuery.getCount() == 0)
			return null;
		else
			return currentQuery.getString(0);
	}
	
	private void readKeyMapping()
	{
		Cursor currentQuery = _db.rawQuery("SELECT key, name FROM keyname_" + _strIMEIdentifier, null);
		for(int i=0; i < currentQuery.getCount();i++)
			keyToName.put(currentQuery.getString(0), currentQuery.getString(1));
	}
}
