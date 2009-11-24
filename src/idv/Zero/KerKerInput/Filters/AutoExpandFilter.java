package idv.Zero.KerKerInput.Filters;

import idv.Zero.KerKerInput.IKerKerInputFilter;

public class AutoExpandFilter extends IKerKerInputFilter {
	private StringBuilder sb = new StringBuilder();
	
	@Override
	public String onTextCommit(String text) {
		sb.append(text);
		
		if (sb.toString().indexOf("kki") > -1)
		{
			sb.delete(0, sb.toString().indexOf("kki"));
			_core.showPopup("自動展開 KKI => 科科輸入法");
			text = sb.toString().replace("kki", "科科輸入法");
			_core.getConnection().deleteSurroundingText(2, 0);
			sb = new StringBuilder();
		}
		else if(sb.length() > 3)
			sb.delete(0, 1);
		
		return text;
	}

	@Override
	public void onDelete() {
		if (sb.length() > 0)
			sb = new StringBuilder(sb.substring(0, sb.length() - 1));
	}

}
