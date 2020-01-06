package com.focus.cos.api;

/**
 * 告警类型
 * @author focus
 * final static String[] ORGSEVERITY = { "致命", "严重", "重要", "警告", "提醒" }
 */
public enum AlarmSeverity
{
	BLACK("致命"),RED("严重"),ORANGE("重要"),YELLOW("次要"),BLUE("警告");

	private String value;

	private AlarmSeverity(String v)
	{
		this.value = v;
	}
	
//	public final static String[] getValues()
//	{
//		final String[] ORGSEVERITY = { "致命", "严重", "重要", "次要", "警告" }; 
//		return ORGSEVERITY;
//	}

	public final static String[] getColors()
	{
		final String[] ORGSEVERITY = { "BLACK", "RED", "ORANGE", "YELLOW", "BLUE" }; 
		return ORGSEVERITY;
	}

	public String getValue()
	{
		return this.value;
	}
	
	public int getIntValue()
	{
		for(int i = 0; i < values().length; i++)
			if( this.equals(values()[i]) )
				return i;
		return -1;
	}

	public static AlarmSeverity get(String ind)
	{
		for (AlarmSeverity s : AlarmSeverity.values())
		{
			if (s.getValue().equalsIgnoreCase(ind))
			{
				return s;
			}
		}
		return null;
	}
	
	public static AlarmSeverity get(int ind)
	{
		return AlarmSeverity.values()[ind];
	}
}
