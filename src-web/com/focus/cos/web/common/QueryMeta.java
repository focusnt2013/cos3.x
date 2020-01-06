package com.focus.cos.web.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.lang.time.DateFormatUtils;

public class QueryMeta implements java.io.Serializable
{
	private static final long serialVersionUID = -8409159264112024396L;
	private	final String baseDateFormat = "yyyy-MM-dd HH:mm:ss";
	private	final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public void setStartDate(Date startDate)
	{
		if( startDate == null )
		{
			this.attribute.remove("startDate");
		}
		else
		{
			this.set("startDate", startDate);
			this.set("sTime", DateFormatUtils.format(startDate,baseDateFormat));
		}
	}
	
	public Date getStartDate()
	{
		return (Date)get("startDate");
	}

	public void setEndDate(Date endDate)
	{
		if( endDate == null )
		{
			this.attribute.remove("endDate");
		}
		else
		{		
			this.set("endDate", endDate);
			this.set("eTime", DateFormatUtils.format(endDate,baseDateFormat));
		}
	}
	
	public Date getEndDate()
	{
		return (Date)get("endDate");
	}

	public String getSTime()
	{
		if( this.exist("sTime") )
		{
			return this.getString("sTime");
		}
		else
		{
			return this.getStartDateString();
		}
	}

	public void setSTime(String time)
	{
		if( time == null || time.isEmpty() ) return;
		this.set("sTime", time);
		try
		{
			this.set("startDate", simpleDateFormat.parse(time));
		}
		catch (ParseException e)
		{
		}
	}

	public String getETime()
	{
		if( this.exist("eTime") )
		{
			return this.getString("eTime");
		}
		else
		{
			return this.getEndDateString();
		}
	}

	public void setETime(String time)
	{
		if( time == null || time.isEmpty() ) return;
		this.set("eTime", time);
		try
		{
			this.set("endDate", simpleDateFormat.parse(time));
		}
		catch (ParseException e)
		{
		}
	}
	
	public String getKeyword()
	{
		return this.getString("keyword");
	}

	public void setKeyword(String keyword)
	{
		if( keyword != null && keyword.length() > 0 )
		{
			this.set("keyword", keyword.trim());
		}
	}

	public String getAppType()
	{
		return this.getString("appType");
	}

	public void setAppType(String appType)
	{
		if( appType != null && appType.length() > 0 )
		{
			this.set("appType", appType.trim());
		}
	}
	public String getMsgfmt()
	{
		return this.getString("msgfmt");
	}

	public void setMsgfmt(String msgfmt)
	{
		if( msgfmt != null && msgfmt.length() > 0 )
		{
			this.set("msgfmt", msgfmt.trim());
		}
	}
	
	public int getStatus()
	{
		if( !this.exist("status") )
		{
			return -1;
		}
		return (Integer)this.get("status");
	}

	public void setStatus(int status)
	{
		if( status >= 0 )
		{
			this.set("status", status);
		}
	}

    /*其他配置参数*/
    private HashMap<String, Object> attribute = new HashMap<String, Object>();
    /**
     * 获取参数值
     * @param name String
     * @return String
     */
    public Object get( String name )
    {
        return attribute.get( name );
    }

    public String getString( String name )
    {
        Object attr = attribute.get( name );
        if( attr == null )
            return "";
        return attr.toString();
    }
    
    /**
     * 判断是否存在对应参数
     * @param name
     * @return
     */
    public boolean exist( String name )
    {
    	return attribute.containsKey(name);
    }

    /**
     * 根据参数名设置参数值
     * @param name String
     * @param value String
     */
    public void set( String name, Object value )
    {
    	if( value != null )
    	{
    		if(value instanceof String )
    		{
    			 if(( (String)value).length() > 0 )
    			 {
    				 attribute.put( name, value );
    			 }
    		}
    		else
    		{
				 attribute.put( name, value );
    		}
    	}
    }

	public String getStartDateString()
	{
		Date date = getStartDate();
		if( date == null )
		{
			return "";
		}
		String dateFormat = baseDateFormat;
		if( this.exist("dateFormat") )
		{
			dateFormat = this.getString( "dateFormat" );
		}
		
		return DateFormatUtils.format(date,dateFormat);
	}
	
	public String getEndDateString()
	{
		Date date = getEndDate();
		if( date == null )
		{
			return "";
		}
		String dateFormat = baseDateFormat;
		if( this.exist("dateFormat") )
		{
			dateFormat = this.getString( "dateFormat" );
		}		
		return DateFormatUtils.format(date,dateFormat);
	}
}
