package idv.Zero.KerKerInput.Filters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import android.util.Log;

import idv.Zero.KerKerInput.IKerKerInputFilter;

public class WeatherFilter extends IKerKerInputFilter {
	private StringBuilder sb = new StringBuilder();
	
	@Override
	public String onTextCommit(String text) {
		sb.append(text);
		
		if (sb.toString().indexOf("台北") > -1)
		{
			_core.showPopup("台北 天氣查詢中...");
			sb = new StringBuilder();
			
			new Thread(new TaipeiWeatherQueryRunnable()).start();
		}
		else if(sb.length() > 2)
			sb.delete(0, 1);
		
		return text;
	}

	@Override
	public void onDelete() {
		if (sb.length() > 0)
			sb = new StringBuilder(sb.substring(0, sb.length() - 1));
	}
	
	private class TaipeiWeatherQueryRunnable implements Runnable
	{
		public void run()
		{
			try {
				URL remoteURL = new URL("http://bill.hypo.cc:4567/taipei");
				HttpURLConnection conn = (HttpURLConnection)remoteURL.openConnection();
				conn.setInstanceFollowRedirects(true);
				conn.setRequestMethod("GET");
			
				BufferedReader remoteInputReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuilder sbResult = new StringBuilder();
				String strTemp;
	
				while ((strTemp = remoteInputReader.readLine()) != null)
					sbResult.append(strTemp);
				
				remoteInputReader.close();
				
				_core.postShowPopup(sbResult.toString().trim());
			} catch (ProtocolException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
