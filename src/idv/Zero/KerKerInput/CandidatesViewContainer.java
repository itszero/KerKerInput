package idv.Zero.KerKerInput;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;

public class CandidatesViewContainer extends LinearLayout implements OnTouchListener {

	private View btnLeft, btnRight;
	private CandidatesView _candsView;
	
	public CandidatesViewContainer(Context context, AttributeSet attrs) {
		super(context, attrs);		
	}
	
	public void initContainer(KerKerInputCore c)
	{
		_candsView = (CandidatesView) findViewById(R.id.candidates);
		_candsView.setCore(c);
		_candsView.initPopup();
		btnLeft = findViewById(R.id.btn_left);
		btnLeft.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				_candsView.prevPage();
				_candsView.invalidate();
			}
		});
		btnRight = findViewById(R.id.btn_right);
		btnRight.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				_candsView.nextPage();
				_candsView.invalidate();
			}
		});
	}

	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void requestLayout() {
        /*int availWidth = _candsView.getWidth();
        int neededWidth = _candsView.computeHorizontalScrollRange();
        int x = _candsView.getmScrollX();
        boolean leftVisible = x > 0;
        boolean rightVisible = x + availWidth < neededWidth;
        btnLeft.setVisibility(leftVisible ? VISIBLE : GONE);
        btnRight.setVisibility(rightVisible ? VISIBLE : GONE);
        */
        super.requestLayout();
	}
	
	public void setCandidates(List<CharSequence> lst)
	{
		_candsView.setCandidates(lst);
	}
}
