package com.focus.cos.web.common;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class I18nUtil
{
	private static final Log log = LogFactory.getLog(I18nUtil.class);
    private static Properties properites = new Properties();
	static
	{
        try
        {
        	Properties webworkProperties = new Properties();
        	webworkProperties.load(I18nUtil.class.getClassLoader().getResourceAsStream("webwork.properties") );
        	String subfix = webworkProperties.getProperty("webwork.locale");
        	String path = "i18n/messages_"+subfix + ".properties";
        	log.info("Succeed to initialize I18nUtil:" + path);
        	properites.load(I18nUtil.class.getClassLoader().getResourceAsStream(path) );
        }
        catch( Exception e )
        {
        	log.error("Failed to build iln.", e);
        }
	}

    public static void setString( String id, String value )
    {
        if( id == null || value == null || id.equals( "" ) || value.equals( "" ) )
        {
            return;
        }

        properites.setProperty( id, value );
    }

    /*
     * Read the information from Config table
     */
    public static String getString( String id )
    {
        return getString( id, null );
    }

    public static String getString( String id, String defult )
    {
        String result;
        if( id == null )
        {
            return defult;
        }
        if( defult != null )
        {
            result = properites.getProperty( id, defult );
        }
        else
        {
            result = properites.getProperty( id, "" );
        }
        return result;
    }

    public static double getDouble( String id )
    {
        String result;
        if( id == null )
        {
            return 0;
        }
        result = properites.getProperty( id, id );
        if( result.equals( id ) )
        {
            result = "0";
        }
        double c = 0;
        try
        {
            c = Double.parseDouble( result );
        }
        catch( Exception e )
        {
        }
        return c;
    }

    public static int getInteger( String id )
    {
        String result;
        if( id == null )
        {
            return 0;
        }
        result = properites.getProperty( id, id );
        if( result.equals( id ) )
        {
            result = "0";
        }
        int c = 0;
        try
        {
            c = Integer.parseInt( result );
        }
        catch( Exception e )
        {
        }
        return c;
    }
}
