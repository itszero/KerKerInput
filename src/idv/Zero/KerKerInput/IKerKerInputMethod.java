package idv.Zero.KerKerInput;

import android.inputmethodservice.Keyboard;

public interface IKerKerInputMethod {
	
	/* Input Method Core */
	public void initInputMethod(KerKerInputCore core);
	public String getName();
	public void onEnterInputMethod();
	public void onExitInputMethod();

	/* Keyboard Events */
	public Keyboard getDesiredKeyboard();
	public boolean wantHandleEvent(int keyCode);
	public boolean onKeyEvent(int keyCode, int[] keyCodes);
	public void onTextEvent(CharSequence text);
	public void commitCandidate(int currentCandidate);
	
	/* Candidates Handling */
	public void setTotalPages(int totalPages);
	public void setCurrentPage(int currentPage);
}
