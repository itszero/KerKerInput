package idv.Zero.KerKerInput.Filters;

import java.util.Random;

import android.util.Log;

import idv.Zero.KerKerInput.IKerKerInputFilter;

public class DiceFilter extends IKerKerInputFilter {
	private StringBuilder sb = new StringBuilder();
	
	@Override
	public String onTextCommit(String text) {
		sb.append(text);
		
		if (sb.toString().indexOf("幫我丟骰子") > -1)
		{
			Random r = new Random();
			_core.showPopup("你丟出了 " + Integer.valueOf(r.nextInt(6) + 1).toString() + "點");
			sb = new StringBuilder();
		}
		else if(sb.length() > 5)
			sb.delete(0, 1);
		
		return text;
	}

	@Override
	public void onDelete() {
		if (sb.length() > 0)
			sb = new StringBuilder(sb.substring(0, sb.length() - 1));
	}

}
