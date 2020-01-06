package com.focus.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class Config
{
    private File file;
    private long lastModify;

    public Config( String path )
    {
        file = new File( path );
    }

    public Config( File path )
    {
        file = path;
    }

    private Properties properites = new Properties();

    private void loadConfig()
    {
        try
        {
            if( file.exists() && file.isFile() )
            {
                InputStreamReader reader = new InputStreamReader(new FileInputStream( file ), "UTF-8");
                properites.load( reader );
                reader.close();
                lastModify = file.lastModified();
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    public void setString( String id, String value )
    {
        if( id == null || value == null || id.equals( "" ) || value.equals( "" ) )
        {
            return;
        }

        if( properites.size() == 0 )
        {
            loadConfig();
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
        if( properites.size() == 0 )
        {
            loadConfig();
        }
        else
        {
            if( file.lastModified() > lastModify )
            {
                lastModify = file.lastModified();
                loadConfig();
            }
        }
        if( defult != null )
        {
            result = properites.getProperty( id, defult );
            if( result == null || result.trim().isEmpty() ){
            	result = defult; 
            }
        }
        else
        {
            result = properites.getProperty( id );
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
        if( properites.size() == 0 )
        {
            loadConfig();
        }
        else
        {
            if( file.lastModified() > lastModify )
            {
                lastModify = file.lastModified();
                loadConfig();
            }
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

    public int getInteger( String id, int def )
    {
        String result;
        if( id == null )
        {
            return 0;
        }
        if( properites.size() == 0 )
        {
            loadConfig();
        }
        else
        {
            if( file.lastModified() > lastModify )
            {
                lastModify = file.lastModified();
                loadConfig();
            }
        }
        result = properites.getProperty( id, id );
        if( result.equals( id ) )
        {
            result = "0";
        }
        try
        {
        	def = Integer.parseInt( result );
        }
        catch( Exception e )
        {
        }
        return def;
    }
}
