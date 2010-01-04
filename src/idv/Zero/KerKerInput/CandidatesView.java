package idv.Zero.KerKerInput;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

public class CandidatesView extends View {
	private List<CharSequence> _cands;
	private Paint _pntText;
	private Drawable _drwHighlighted, _divider;
	private KerKerInputCore _core;
	private int _wordWidth = 34;
	private PopupWindow _winPreview;
	private TextView _txtPreview;
	private int _lastTouchedX;
	private int OUT_OF_BOUND = -1;
	private int[] _candIdxToX;
	private int[] _candIdxToWidth;
	private int[] _candIdxToPage;
	private int _scrollX;
	private int _currentCandidate;
	private int scrollStartCandID;
	private int _currentPage;
	
	public CandidatesView(Context c, AttributeSet attrs) {
		super(c, attrs);
		
		_drwHighlighted = c.getResources().getDrawable(R.drawable.highlight);
		_divider = c.getResources().getDrawable(R.drawable.divider);
		
		_cands = new ArrayList<CharSequence>();
		_candIdxToX = new int[0];
		_candIdxToWidth = new int[0];
		_candIdxToPage = new int[0];
		_currentPage = 0;
		
		_pntText = new Paint();
		_pntText.setColor(Color.WHITE);
		_pntText.setAntiAlias(true);
		_pntText.setTextSize(24);
		_pntText.setStrokeWidth(0);
				
		setHorizontalFadingEdgeEnabled(true);
		setHorizontalScrollBarEnabled(false);
		setVerticalScrollBarEnabled(false);
		
		_lastTouchedX = OUT_OF_BOUND;
		scrollStartCandID = 0;
	}
	
	public void initPopup()
	{
		Context c = _core.getFrontend();
		_winPreview = new PopupWindow(c);
		_winPreview.setWindowLayoutMode(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		_winPreview.setBackgroundDrawable(null);
		_txtPreview = (TextView) _core.getInflater().inflate(R.layout.candidates_preview, null);
		_winPreview.setContentView(_txtPreview);
	}

	public void setCandidates(List<CharSequence> lst)
	{
		_cands = lst;
		_candIdxToX = new int[lst.size()];
		_candIdxToWidth = new int[lst.size()];
		_candIdxToPage = new int[lst.size()];
		_currentPage = 0;
		scrollStartCandID = 0;
		invalidate();
	}
	
	public void setCore(KerKerInputCore core)
	{
		_core = core;
	}	
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		
		int x = -(_wordWidth + _divider.getIntrinsicWidth()), page = 0;
        int y = (int) (getHeight() + _pntText.getTextSize() - _pntText.descent()) / 2;
		int count = _cands.size();
		_drwHighlighted.setBounds(0, 0, _wordWidth, getHeight());
		_divider.setBounds(0, 0, _divider.getIntrinsicWidth(), getHeight());
		
		for(int i=0;i<count;i++)
		{
			CharSequence cand = _cands.get(i);
			float textWidth = _pntText.measureText(cand.toString());
			x += _wordWidth + _divider.getIntrinsicWidth();
			_candIdxToWidth[i] = (int)textWidth;
			_candIdxToX[i] = x;
			_candIdxToPage[i] = page;

			if (x + textWidth > getWidth())
			{
				page++;
				x = -(_wordWidth + _divider.getIntrinsicWidth());
				continue;
			}
			
			if (i < scrollStartCandID || page != _currentPage)
				continue;
			
			
			canvas.translate(x, 0);
			if (isCandSelected(x, x + _wordWidth))
			{
				_drwHighlighted.draw(canvas);
				showPopup(i);
			}

			canvas.drawText(cand.toString(), _wordWidth / 2 - textWidth / 2, y, _pntText);
			
			canvas.translate(_wordWidth, 0);
			_divider.draw(canvas);
			canvas.translate(-_wordWidth, 0);

			canvas.translate(-x, 0);			
		}
		_core.getCurrentInputMethod().setTotalPages(page + 1);
	}

	private boolean isCandSelected(int x, int xe) {
		if (_lastTouchedX >= x && _lastTouchedX <= xe)
			return true;
		else
			return false;
	}

    @Override
    public boolean onTouchEvent(MotionEvent me) {
    	
    	if (me.getAction() == MotionEvent.ACTION_UP || me.getAction() == MotionEvent.ACTION_OUTSIDE)
    	{
        	if (me.getAction() == MotionEvent.ACTION_UP)
        		commitCandidates();
        	hidePopup();
    	}
    	else if (me.getAction() == MotionEvent.ACTION_DOWN || me.getAction() == MotionEvent.ACTION_MOVE)
        	_lastTouchedX = (int) me.getX();
    	
    	
    	invalidate();    	
    	return true;
    }
    
    private void commitCandidates() {
    	if (_currentCandidate == OUT_OF_BOUND)
    		return;
    	
    	_core.commitCandidate(_currentCandidate);
	}

	private void showPopup(int candIndex)
    {
		_currentCandidate = candIndex;
		
    	CharSequence str = _cands.get(candIndex);
    	_txtPreview.setText(str);
    	_txtPreview.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
    	int wordWidth = (int)(_pntText.measureText(str.toString()));
    	int popupWidth = wordWidth + _txtPreview.getPaddingLeft() + _txtPreview.getPaddingRight();
    	int popupHeight = _txtPreview.getMeasuredHeight();
    	int popupX = _candIdxToX[candIndex] - _scrollX + (popupWidth - _wordWidth) / 2 + 8; // 8?
    	int popupY = -popupHeight;
    	
    	int[] offset = new int[2];
    	getLocationInWindow(offset);
    	popupY += offset[1];
    	
    	if (_winPreview.isShowing())
    		_winPreview.update(popupX, popupY, popupWidth, popupHeight);
    	else
    	{
    		_winPreview.setWidth(popupWidth);
    		_winPreview.setHeight(popupHeight);
    		_winPreview.showAtLocation(this, Gravity.NO_GRAVITY, popupX, popupY);
    	}
    	_txtPreview.setVisibility(VISIBLE);
    }
    
    private void hidePopup()
    {
    	_winPreview.dismiss();
    	_lastTouchedX = OUT_OF_BOUND;
    	_currentCandidate = OUT_OF_BOUND;
    }

    public void prevPage()
    {
    	if (_currentPage == 0) return;
    	
    	_currentPage--;
    	while(scrollStartCandID >= 0 && _candIdxToPage[scrollStartCandID] > _currentPage - 1)
    		scrollStartCandID--;
    	
    	scrollStartCandID++;
    	_core.getCurrentInputMethod().setCurrentPage(_currentPage);
    }

    public void nextPage()
    {
    	_currentPage++;
    	while(scrollStartCandID < _cands.size() && _candIdxToPage[scrollStartCandID] < _currentPage)
    		scrollStartCandID++;
    	
    	if (scrollStartCandID == _cands.size())
    	{
    		scrollStartCandID--;
    		prevPage();
    	}
    	_core.getCurrentInputMethod().setCurrentPage(_currentPage);
    }
}
