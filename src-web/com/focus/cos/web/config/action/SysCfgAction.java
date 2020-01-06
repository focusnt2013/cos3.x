package com.focus.cos.web.config.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;

import javax.servlet.ServletOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.focus.cos.web.action.GridAction;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.Skin;
import com.focus.cos.web.config.service.SysCfgMgr;
import com.focus.util.IOHelper;

public class SysCfgAction extends GridAction 
{
	private static final long serialVersionUID = 2004977066813171910L;
	private static final Log log = LogFactory.getLog(SysCfgAction.class);
	private SysCfgMgr sysCfgMgr;
	/*配置*/
	private JSONObject profile;
	/*当前正在试用的皮肤*/
	private Skin skin;
	
	/**
	 * 系统配置集群主数据库
	 * @return
	 */
	public String database(){
		try {
			JSONObject dbcfg = this.sysCfgMgr.getDatabaseConfig();
			this.jsonData = dbcfg.toString(4);
		} catch (Exception e) {
			this.jsonData = "{}";
			this.setResponseException(e.getMessage());
		}
		return "database";
	}
	
	public String dbserver(){
		
		return super.grid("/grid/local/sysdatabasecfg.xml");
	}
	
	/**
	 * 预设值
	 * @return
	 */
	public String preset()
	{
		//读取皮肤配置数据
		this.profile = SysCfgMgr.getConfig();
		try
		{
			this.profile.put("POP3Password", profile.has("POP3PasswordEncrypt")?profile.getString("POP3PasswordEncrypt"):"");
			if( profile != null && profile.has("Theme") )
			{
				super.datatype = profile.getString("Theme");
			}
			File webappDir = PathFactory.getWebappPath();
			File skinsDir = new File(webappDir, "skin/");
			//当前正在试用的皮肤
			File skinFile = new File(skinsDir, "config");
			if( !skinFile.exists() )
			{
				Skin.changeSkin("defone");
				skin = Skin.getInstance();
			}
			else
				skin = (Skin)IOHelper.readSerializableNoException(skinFile);
			if( skinsDir.exists() )
			{
				ArrayList<Skin> listSkin = new ArrayList<Skin>();
				for(File skinDir : skinsDir.listFiles() )
				{
					if( skinDir.isDirectory() && !skinDir.getName().equals(".svn") )
					{
						Skin skin = new Skin();
						skin.setName(skinDir.getName());
						File readmeFile = new File(skinDir, "readme.txt");
						if( readmeFile.exists() )
						{//从readme文件读取皮肤的配置信息
							Properties properites = new Properties();
							java.io.BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(readmeFile)));
							properites.load(reader);
							skin.setAuthor(properites.getProperty("author"));
							skin.setColor(properites.getProperty("color"));
							skin.setVersion(properites.getProperty("version"));
							listSkin.add(skin);
						}
						else
						{
							log.warn("Failed to find the readme of skin("+skin.getName()+").");
						}
					}
				}
				this.listData = listSkin;
			}
		}
		catch(Exception e)
		{
			log.error("Failed to load skin:", e);
		}
		return "preset";
	}

	/**
	 * 设置系统图标
	 * @return String
	 */
	public String doUploadLogo()
	{
		if( uploadfile == null )
		{
			return null;
		}
		JSONObject rsp = new JSONObject();
		rsp.put("id", "SysLogo");
		JSONObject account = (JSONObject) super.getSession().getAttribute("account");
		String s = String.format(this.getText("user.log.0169"), account.getString("username"));
		File logoSys = new File(PathFactory.getWebappPath(), "images/logo.png");
		IOHelper.copy(uploadfile, logoSys);
		rsp.put("url", Kit.URL_IMAGEPATH(super.getRequest())+"logo.png?ts="+logoSys.lastModified());
		logoper(s, "系统配置", null, null);
		ServletOutputStream out = null;
		try
		{
			log.debug("Succeed to upload the logo of sys:\r\n"+rsp.toString(4));
    		super.getResponse().setHeader("Content-disposition", "inline; filename="+System.currentTimeMillis()+".json");
            out = super.getResponse().getOutputStream();
            out.write(rsp.toString().getBytes("UTF-8"));
		}
		catch (IOException e)
		{
			log.error("Failed to resonse json", e);
		}
        finally
        {
        	if( out != null )
	    		try
				{
	            	 out.close();
				}
				catch (IOException e)
				{
					log.error("", e);
				}
        }
		return null;
	}
	

	public void setSysCfgMgr(SysCfgMgr sysCfgMgr) {
		this.sysCfgMgr = sysCfgMgr;
	}
	
	public JSONObject getProfile() {
		return profile;
	}
	public Skin getSkin()
	{
		return skin;
	}
}
