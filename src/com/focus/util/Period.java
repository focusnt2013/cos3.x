package com.focus.util;

import java.io.Serializable;
import java.util.Calendar;

public class Period implements Serializable
{
    /*星期*/
    private int week = 0;
    /*开始时间 XX:XX*/
    private int startTime = 0;
    /*结束s XX:XX*/
    private int endTime = 0;

    public Period( int s, int e )
    {
        this.startTime = s;
        this.endTime = e;
    }

    public Period( int w, int s, int e )
    {
        this.week = w;
        this.startTime = s;
        this.endTime = e;
    }

    /**
     * 返回每天00:00:00的时间
     * @return int
     */
    private int getDayTimeInSeconds()
    {
        Calendar time = Calendar.getInstance();
        int d = time.get( time.DAY_OF_MONTH );
        if( lastDayOfMonth != d )
        {
            int y = time.get( time.YEAR );
            int m = time.get( time.MONTH );
            time.clear();
            time.set( y, m, d );
            lastDayOfMonth = d;
            dayTimeInSeconds = ( int ) ( time.getTimeInMillis() / 1000 );
        }
        return dayTimeInSeconds;
    }

    /**
     * 判断时间
     * @param day int
     * @param time int
     * @return boolean
     */
    private int lastDayOfMonth = 0;
    private int dayTimeInSeconds = 0;
    public boolean isPeriod()
    {
        int currentTime = Tools.current();
        if( week > 0 && week <= 7 )
        {
            int w = Calendar.getInstance().get( Calendar.DAY_OF_WEEK );
            if( w != week )
            {
                return false;
            }
        }
//
        int s = this.getStartTime();
        int e = this.getEndTime();
//        System.out.println( "开始时间:"+Tools.getFormatTime("MM月dd日 HH:mm", s) );
//        System.out.println( "结束时间:"+Tools.getFormatTime("MM月dd日 HH:mm", e) );
        return( currentTime >= s ) && ( currentTime < e );
    }

    public int getStartTime()
    {
        int currentTime = Tools.current();
        int s = getDayTimeInSeconds() + startTime;
        int e = getDayTimeInSeconds() + endTime;
        if( this.startTime >= this.endTime )
        {
            if( currentTime < e )
            {
                s -= Tools.SECOND_OF_DAY;
            }
        }
        return s;
    }

    public int getEndTime()
    {
        int currentTime = Tools.current();
        int s = getDayTimeInSeconds() + startTime;
        int e = getDayTimeInSeconds() + endTime;
        if( this.startTime >= this.endTime )
        {
            if( currentTime > e )
            {
                e += Tools.SECOND_OF_DAY;
            }
        }
        return e;
    }

    public int getWeek()
    {
        return week;
    }

    public void setWeek( int week )
    {
        this.week = week;
    }

    public void setStartTime( int startTime )
    {
        this.startTime = startTime;
    }

    public void setEndTime( int endTime )
    {
        this.endTime = endTime;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "[星期" + this.week );
        sb.append( "][" );
        sb.append( Tools.getSmartTime( "HH:mm", getStartTime() ) );
        sb.append( "-" );
        sb.append( Tools.getSmartTime( "HH:mm", getEndTime() ) );
        sb.append( "]" );
        return sb.toString();
    }

    public int timeStart()
    {
        return this.startTime;
    }

    public int timeEnd()
    {
        return this.endTime;
    }

    public static void main( String args[] )
    {
        Period p = new Period( 18 * Tools.SECOND_OF_HOUR,
                               12 * Tools.SECOND_OF_HOUR );
        Calendar time = Calendar.getInstance();
        int y = time.get( time.YEAR );
        int m = time.get( time.MONTH );
        int d = time.get( time.DAY_OF_MONTH );
        System.out.println( "今天是星期" + time.get( time.DAY_OF_WEEK ) );

        System.out.print( "时间段:\r\n\t" );
        System.out.print( Tools.getFormatTime( "MM-dd HH:mm", p.getStartTime() ) );
        System.out.print( " - " );
        System.out.println( Tools.getFormatTime( "MM-dd HH:mm", p.getEndTime() ) );

        System.out.print( "全日期时间段:\r\n\t" );
        System.out.print( Tools.getFormatTime( "yyyy-MM-dd HH:mm",
                                               p.getStartTime() ) );
        System.out.print( " - " );
        System.out.println( Tools.getFormatTime( "yyyy-MM-dd HH:mm",
                                                 p.getEndTime() ) );

        System.out.print( "当前时间:\r\n\t" );
        System.out.println( Tools.getFormatTime( "yyyy-MM-dd HH:mm",
                                                 Tools.current() ) );
//        System.out.print( "时间段判断:\r\n\t" );
//        System.out.println( p.isPeriod() );

//        Period p1 = new Period( 18 * Tools.SECOND_OF_HOUR,
//                                23 * Tools.SECOND_OF_HOUR );
//        System.out.println( p1.isPeriod() );
//        Period p2 = new Period( 10 * Tools.SECOND_OF_HOUR,
//                                13 * Tools.SECOND_OF_HOUR );
//        System.out.println( p2.isPeriod() );
//        Period p3 = new Period( 1 * Tools.SECOND_OF_HOUR,
//                                8 * Tools.SECOND_OF_HOUR );
//        System.out.println( p3.isPeriod() );
        Period p4 = new Period( 18 * Tools.SECOND_OF_HOUR,
                                10 * Tools.SECOND_OF_HOUR );
        System.out.println( p4.isPeriod() );
        Period p5 = new Period( 18 * Tools.SECOND_OF_HOUR,
                                16 * Tools.SECOND_OF_HOUR );
        System.out.println( p5.isPeriod() );
        Period p6 = new Period( 0 * Tools.SECOND_OF_HOUR,
                                0 * Tools.SECOND_OF_HOUR );
        System.out.println( p6.isPeriod() );
        Period p7 = new Period( 12 * Tools.SECOND_OF_HOUR,
                                12 * Tools.SECOND_OF_HOUR );
        System.out.println( p7.isPeriod() );
    }
}
