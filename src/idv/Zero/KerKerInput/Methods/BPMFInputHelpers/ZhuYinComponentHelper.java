package idv.Zero.KerKerInput.Methods.BPMFInputHelpers;

import java.util.HashMap;

public class ZhuYinComponentHelper {
	
	private enum ZhuYinMask { COMP_1, COMP_2, COMP_3, COMP_TONE };
	private static HashMap<String, ZhuYinMask> componentsToMask;
	
	public static String getComposedRawString(String oldRawString, String incomingSymbol)
	{
		initComponentsData();
		
		String[] arrComponents = new String[4];
		int size = oldRawString.length();
		for(int i=0;i<size;i++)
		{
			String sub = oldRawString.substring(i, i+1);
			if (componentsToMask.containsKey(sub))
				arrComponents[maskToInt(componentsToMask.get(sub))] = sub;
		}
		
		if (componentsToMask.containsKey(incomingSymbol))
			arrComponents[maskToInt(componentsToMask.get(incomingSymbol))] = incomingSymbol;
		
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<4;i++)
		{
			// No tone symbol until we have some components.
			if (i == 3 && sb.length() == 0)
				break;
			
			if (arrComponents[i] != null && !arrComponents[i].trim().equalsIgnoreCase(""))
				sb.append(arrComponents[i]);
		}
		
		return sb.toString();
	}
	
	private static int maskToInt(ZhuYinMask m)
	{
		int ret = 0;
		
		switch(m)
		{
		case COMP_1:
			ret = 0;
			break;
		case COMP_2:
			ret = 1;
			break;
		case COMP_3:
			ret = 2;
			break;
		case COMP_TONE:
			ret = 3;
			break;
		default:
			ret = 0;
			break;
		}
		
		return ret;
	}
	
	private static void initComponentsData()
	{
		if (componentsToMask == null)
			componentsToMask = new HashMap<String, ZhuYinMask>();
		else
			return ;

		String comp1 = "1qaz2wsxedcrfv5tgbyhn";
		String comp2 = "ujm";
		String comp3 = "8ik,9ol.0p;;/-";
		String compTone = "3467 ";
		
		for(String s : comp1.split(""))
			componentsToMask.put(s, ZhuYinMask.COMP_1);

		for(String s : comp2.split(""))
			componentsToMask.put(s, ZhuYinMask.COMP_2);

		for(String s : comp3.split(""))
			componentsToMask.put(s, ZhuYinMask.COMP_3);

		for(String s : compTone.split(""))
			componentsToMask.put(s, ZhuYinMask.COMP_TONE);
	}
}
