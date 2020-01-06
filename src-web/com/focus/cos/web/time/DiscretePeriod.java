package com.focus.cos.web.time;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.focus.util.Tools;

public class DiscretePeriod implements java.io.Serializable
{
	private static final long serialVersionUID = 5470380372468257672L;
	public static final int HOUR = Calendar.HOUR_OF_DAY;//小时，0~23(周期离散)
	//public static final int HOUR_WEEK = 3;//星期x，000表示星期日的0点，001表示星期日的1点，602表示(周期离散)
	public static final int DAY_WEEK = Calendar.DAY_OF_WEEK;//星期x，0~6(周期离散)
	public static final int DAY_MONTH = Calendar.DAY_OF_MONTH;//月的天1~31(周期离散)
	//public static final int MONTH = 6;//月，1~12(周期离散)
	public static final int HOUR_DATE = 0x81|Calendar.HOUR_OF_DAY;//小时，yyyyMMdd00~yyyyMMdd23(连续离散)
	public static final int DAY_DATE = 0x80|Calendar.DAY_OF_MONTH;//日期，20100914(连续离散)
	//计划报表可以选择连续离散和周期离散，周期报表只能选择连续离散
	private int type;//离散时间的类型
	private int discreteTime;//离散时间值根据type不同
	
	public int getType()
	{
		return type;
	}
	public void setType(int type)
	{
		this.type = type;
	}
	public int getDiscreteTime()
	{
		return discreteTime;
	}
	public void setDiscreteTime(int discreteTime)
	{
		this.discreteTime = discreteTime;
	}
	
	/**
	 * 得到显示标签
	 */
	public String getLabel()
	{
		switch( type )
		{
			case HOUR_DATE:
			{
				String str = String.valueOf(discreteTime);
				return str.substring(0, 4)+"-"+str.substring(4, 6)+"-"+str.substring(6, 8)+" "+str.substring(8)+":00";
			}
			case DAY_DATE:
			{//
				String str = String.valueOf(discreteTime);
				return str.substring(0, 4)+"-"+str.substring(4, 6)+"-"+str.substring(6);
			}
			case HOUR:
				return discreteTime<10?("0"+discreteTime+":00"):(discreteTime+":00");
			case DAY_WEEK:
				switch( discreteTime )
				{
					case Calendar.SUNDAY:
						return "周日";
					case Calendar.MONDAY:
						return "周一";
					case Calendar.TUESDAY:
						return "周二";
					case Calendar.WEDNESDAY:
						return "周三";
					case Calendar.THURSDAY:
						return "周四";
					case Calendar.FRIDAY:
						return "周五";
					case Calendar.SATURDAY:
						return "周六";
					default:
						return "未知";
				}
			default:
				return String.valueOf(discreteTime);
		}
	}
	/**
	 * 根据离散时间得到开始时间和结束时间
	 * @param startTime 统计的开始时间和结束时间
	 * @param endTime 统计的开始时间和结束时间爱你
	 * @return
	 */
	public List<Date[]> getTimeRanges(Date startTime, Date endTime)
	{	
		ArrayList<Date[]> listTimeRange = new ArrayList<Date[]>();
		switch( type )
		{
			case HOUR_DATE:
			case DAY_DATE:
			{//
				Calendar calendar = Calendar.getInstance();
				String str = String.valueOf(discreteTime);
				try
				{
					SimpleDateFormat sdft = new SimpleDateFormat(Tools.getTimeFormat(str));
					Date startTime1 = sdft.parse(str);
					calendar.setTime(startTime1);
					int type1 = type&0x7F;
					calendar.add(type1, 1);
					if( type1 < Calendar.HOUR_OF_DAY )
					{
						calendar.set(Calendar.HOUR_OF_DAY, 0);
					}
					calendar.set(Calendar.MINUTE, 0);
					calendar.set(Calendar.SECOND, 0);
					calendar.set(Calendar.MILLISECOND, 0);
					Date endTime1 = calendar.getTime();
					if( startTime1.getTime() >= startTime.getTime() &&
						endTime1.getTime() <= endTime.getTime() )
					{
						Date times[] = new Date[2];
						times[0] = startTime1;
						times[1] = endTime1;
						listTimeRange.add(times);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			default:
			{
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(startTime);				
				while( calendar.getTimeInMillis() < endTime.getTime())
				{
					int time = calendar.get(type);
					if( time == this.discreteTime )
					{//时间等于离散时间
						Date times[] = new Date[2];
						times[0] = calendar.getTime();
						calendar.add(type, 1);
						if( type < Calendar.HOUR_OF_DAY )
						{
							calendar.set(Calendar.HOUR_OF_DAY, 0);
						}
						calendar.set(Calendar.MINUTE, 0);
						calendar.set(Calendar.SECOND, 0);
						calendar.set(Calendar.MILLISECOND, 0);
						if( calendar.getTimeInMillis() <= endTime.getTime())
						{//如果当前时间小于结束时间，那么时间范围的结束时间设置为小时加1的时间
							times[1] = calendar.getTime();
						}
						else
						{//如果当前时间大于结束时间，那么时间范围的结束时间设置为小时加1的时间
							times[1] = endTime;
						}
						listTimeRange.add(times);
					}
					else
					{
						calendar.add(type, 1);
						if( type < Calendar.HOUR_OF_DAY )
						{
							calendar.set(Calendar.HOUR_OF_DAY, 0);
						}
						calendar.set(Calendar.MINUTE, 0);
						calendar.set(Calendar.SECOND, 0);
						calendar.set(Calendar.MILLISECOND, 0);
					}
				}
				break;
			}
		}
		return listTimeRange;
	}
	
	public static void main(String args[])
	{
		Calendar calendar = Calendar.getInstance();
		Date endTime = calendar.getTime();
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		Date startTime = calendar.getTime();
		DiscretePeriod reportTaskDiscreteTime = new DiscretePeriod();
		reportTaskDiscreteTime.setType(DiscretePeriod.HOUR);
		reportTaskDiscreteTime.setDiscreteTime(3);
		List<Date[]> list = reportTaskDiscreteTime.getTimeRanges(startTime, endTime);
		System.out.println("TYPE:"+reportTaskDiscreteTime.getType());
		System.out.println("TIME:"+reportTaskDiscreteTime.getLabel());
		for( int i = 0; i < list.size(); i++ )
		{
			Date[] times = list.get(i);
			System.out.print("StartTime:"+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", times[0].getTime()));
			System.out.println(" EndTime:"+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", times[1].getTime()));
		}
		System.out.println("------------------------------------------------------");
		reportTaskDiscreteTime.setType(DiscretePeriod.DAY_MONTH);
		reportTaskDiscreteTime.setDiscreteTime(3);
		list = reportTaskDiscreteTime.getTimeRanges(startTime, endTime);
		System.out.println("TYPE:"+reportTaskDiscreteTime.getType());
		System.out.println("TIME:"+reportTaskDiscreteTime.getLabel());
		for( int i = 0; i < list.size(); i++ )
		{
			Date[] times = list.get(i);
			System.out.print("StartTime:"+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", times[0].getTime()));
			System.out.println(" EndTime:"+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", times[1].getTime()));
		}
		System.out.println("------------------------------------------------------");
		reportTaskDiscreteTime.setType(DiscretePeriod.DAY_WEEK);
		reportTaskDiscreteTime.setDiscreteTime(Calendar.TUESDAY);
		list = reportTaskDiscreteTime.getTimeRanges(startTime, endTime);
		System.out.println("TYPE:"+reportTaskDiscreteTime.getType());
		System.out.println("TIME:"+reportTaskDiscreteTime.getLabel());
		for( int i = 0; i < list.size(); i++ )
		{
			Date[] times = list.get(i);
			System.out.print("StartTime:"+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", times[0].getTime()));
			System.out.println(" EndTime:"+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", times[1].getTime()));
		}
		System.out.println("------------------------------------------------------");
		reportTaskDiscreteTime.setType(DiscretePeriod.DAY_DATE);
		reportTaskDiscreteTime.setDiscreteTime(20100911);
		list = reportTaskDiscreteTime.getTimeRanges(startTime, endTime);
		System.out.println("TYPE:"+reportTaskDiscreteTime.getType());
		System.out.println("TIME:"+reportTaskDiscreteTime.getLabel());
		for( int i = 0; i < list.size(); i++ )
		{
			Date[] times = list.get(i);
			System.out.print("StartTime:"+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", times[0].getTime()));
			System.out.println(" EndTime:"+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", times[1].getTime()));
		}
		System.out.println("------------------------------------------------------");
		reportTaskDiscreteTime.setType(DiscretePeriod.HOUR_DATE);
		reportTaskDiscreteTime.setDiscreteTime(2010091115);
		list = reportTaskDiscreteTime.getTimeRanges(startTime, endTime);
		System.out.println("TYPE:"+reportTaskDiscreteTime.getType());
		System.out.println("TIME:"+reportTaskDiscreteTime.getLabel());
		for( int i = 0; i < list.size(); i++ )
		{
			Date[] times = list.get(i);
			System.out.print("StartTime:"+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", times[0].getTime()));
			System.out.println(" EndTime:"+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", times[1].getTime()));
		}
		System.out.println("------------------------------------------------------");
	}
}
