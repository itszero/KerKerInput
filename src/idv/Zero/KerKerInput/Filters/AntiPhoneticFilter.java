package idv.Zero.KerKerInput.Filters;

import idv.Zero.KerKerInput.IKerKerInputFilter;

public class AntiPhoneticFilter extends IKerKerInputFilter {
	private String ANTI_STRING = "ㄅㄆㄇㄈㄉㄊㄋㄌㄍㄎㄏㄐㄑㄒㄓㄔㄕㄖㄗㄘㄙㄚㄛㄜㄝㄞㄟㄠㄡㄢㄣㄤㄥㄦㄧㄨㄩ";
	
	@Override
	public String onTextCommit(String text) {
		if (!text.equalsIgnoreCase("") && ANTI_STRING.contains(text))
		{
			_core.showPopup("注音文退散！！");
			return "";
		}
		else
			return text;
	}

	@Override
	public void onDelete() { }

}
