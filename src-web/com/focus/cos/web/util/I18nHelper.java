package com.focus.cos.web.util;

import java.util.Properties;

public class I18nHelper
{
    private Properties properites = new Properties();

    public I18nHelper()
    {
        try
        {
        	Properties webworkProperties = new Properties();
        	webworkProperties.load(I18nHelper.class.getClassLoader().getResourceAsStream("webwork.properties") );
        	String subfix = webworkProperties.getProperty("webwork.locale");
        	String path = "i18n/messages_"+subfix + ".properties";
//        	log.info("I18nHelper init path:" + path);
        	properites.load(I18nHelper.class.getClassLoader().getResourceAsStream(path) );
        }
        catch( Exception e )
        {
//        	log.error("Failed to build iln.", e);
        }
    }

    public void setString( String id, String value )
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
    public String getString( String id )
    {
        return getString( id, null );
    }

    public String getString( String id, String defult )
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

    public double getDouble( String id )
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

    public int getInteger( String id )
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
