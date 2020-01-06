package com.focus.cos.web.common;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import com.focus.util.IOHelper;
import com.focus.util.Tools;
import com.focus.util.XMLParser;

public class COSConfig
{
	private static final Log log = LogFactory.getLog(COSConfig.class);
	private static Integer LocalControlPort;
	private static Integer LocalWebPort;
    
	/**
	 * 得到本WEB端口
	 * @return
	 */
    public static int getLocalWebPort()
    {
    	if( LocalWebPort != null ) return LocalWebPort;
    	File tomcatdir = PathFactory.getWebappPath().getParentFile().getParentFile();
		File serverxml = new File(tomcatdir, "conf/server.xml");
		if( serverxml.exists() )
		{
        	try
        	{
	        	XMLParser xml = new XMLParser(serverxml);
	        	Element e = XMLParser.getElementByTag(xml.getRootNode(), "Connector");
	        	if( e != null )
	        	{
	        		String webPort = XMLParser.getElementAttr(e, "port");
	        		LocalWebPort = Tools.isNumeric(webPort)?Integer.parseInt(webPort):0;
	        		log.info("Found the port of web is "+LocalWebPort+" from "+serverxml.getAbsolutePath());
	        	}
	        	else
	        	{
	        		log.info("Not found the port of web from "+serverxml.getAbsolutePath());
	        	}
        	}
        	catch(Exception e)
        	{
        		log.info("Not found the port of web is "+LocalWebPort+" from "+serverxml.getAbsolutePath()+" for exception:", e);
        		LocalWebPort = 0;
        	}
		}
    	return LocalWebPort;
    }
    /**
     * 得到本地主控端口
     * @return
     */
    public static int getLocalControlPort()
    {
    	if( LocalControlPort != null ) return LocalControlPort;
		File wrapperCnf = new File(PathFactory.getCfgPath(), "wrapper.conf");
		if( wrapperCnf.exists() )
		{
			String txt = new String(IOHelper.readAsByteArray(wrapperCnf));
			int i = txt.indexOf("-Dcontrol.port=");
			if( i != -1 )
			{
				i += "-Dcontrol.port=".length();
				int j = txt.indexOf("\r\n", i);
				LocalControlPort = Integer.parseInt(txt.substring(i, j));
				log.info("Found the port "+LocalControlPort+" of monitor from "+wrapperCnf.getAbsolutePath());
			}
		}
		else
		{
			log.info("Setup default port of monitor for not found the config of wrapper from "+wrapperCnf.getAbsolutePath());
		}
		if( LocalControlPort == null ) LocalControlPort = 9527;
		return LocalControlPort;
    }
}
