package com.focus.cos.api;

/**
 * 告警类型
 * @author focus
 *
 */
public enum AlarmType
{
	B("业务告警"),Q("服务质量告警"),S("系统告警"),E("环境告警"),D("设备告警");

	private String value;

	private AlarmType(String v)
	{
		this.value = v;
	}
	
//	public final static String[] getValues()
//	{
//		final String[] ORGTYPE = { "业务告警", "服务质量告警", "系统告警", "环境告警", "设备告警"  }; 
//		return ORGTYPE;
//	}

	public static String getLabel(String value)
	{
		if( value.equals(B.value) ) return "B";
		if( value.equals(Q.value) ) return "Q";
		if( value.equals(S.value) ) return "S";
		if( value.equals(E.value) ) return "E";
		if( value.equals(D.value) ) return "D";
		return "";
	}
	
	public String getValue()
	{
		return this.value;
	}

	public static AlarmType get(String ind)
	{
		for (AlarmType s : AlarmType.values())
		{
			if (s.getValue().equalsIgnoreCase(ind))
			{
				return s;
			}
		}
		return null;
	}
}
