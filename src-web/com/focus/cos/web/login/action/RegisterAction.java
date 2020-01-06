package com.focus.cos.web.login.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.focus.cos.CosServer;
import com.focus.cos.control.WrapperShell;
import com.focus.cos.web.Version;
import com.focus.cos.web.action.CosBaseAction;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.config.service.CfgMgr;
import com.focus.cos.web.config.service.SftCfgMgr;
import com.focus.cos.web.config.service.SysCfgMgr;
import com.focus.cos.web.dev.service.ModulesMgr;
import com.focus.util.IOHelper;
import com.opensymphony.xwork.ModelDriven;
import com.sixlegs.png.PngImage;

/**
 * 注册COS的使用
 * 1、构建系统与软件配置参数
 * 2、自动为用户组件子系统
 * 3、自动为用户创建菜单配置项目
 * 4、自动为用户配置系统管理员权限
 * 5、自动为用户配置初始的系统监控配置
 * @author focus
 *
 */
public class RegisterAction extends CosBaseAction implements ModelDriven
{
	private static final long serialVersionUID = 4160697963027905429L;
	private static final Log log = LogFactory.getLog(RegisterAction.class);
	private File importSysLogo;
	private JSONObject sysConfig;
	private JSONObject softwareConfig;
	
	/**
	 * 打开注册页面
	 * @return
	 */
	public String doCheckin()
	{
		sysConfig = new JSONObject();
		sysConfig.put("SysName", "刘问章");
		BufferedReader reader = null;
		InputStream is = null;
		try 
		{
			is = this.getClass().getResourceAsStream("/com/focus/cos/web/login/action/brand_icons.txt");
			reader = new BufferedReader( new InputStreamReader(is) );
			String line = null;
			StringBuffer icons = new StringBuffer();
			while( (line = reader.readLine()) != null )
			{
				int i = line.indexOf(':');
				if( i == -1 ) continue;
				line = line.substring(1, i);
				icons.append(",'"+line+"'");
			}
			icons.setCharAt(0, '[');
			icons.append(']');
			this.setMessageCode(icons.toString());
		}
		catch(Exception e)
		{
			this.setResponseException(e.toString());
			log.debug("Failed to checkin", e);
		}
		finally
		{
			if( reader != null )
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
				}
		}
		return "checkin";
	}
	
	/**
	 * 上传系统的ＬＯＧＯ
	 * @return
	 */
	public String doUploadLogo()
	{
		JSONObject rsp = new JSONObject();
		rsp.put("id", "SysLogo");
		if( importSysLogo != null )
		{
			try
			{
				File file = new File(PathFactory.getWebappPath(), "images/logo.png");
				PngImage png = new PngImage();
				png.read(importSysLogo);
				if( png.getWidth() <= 200 && png.getHeight() <= 50 )
				{
					IOHelper.copy(importSysLogo, file);
					log.info("Succeed to upload logo.png from "+importSysLogo+" to "+file);
				}
				else
				{
				}
			}
			catch(Exception e)
			{
				log.error("Failed to upload the logo from "+importSysLogo, e);
			}
		}
		ServletOutputStream out = null;
		try
		{
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
	/**
	 * 完成签字
	 * @return
	 */
	public String doSignin()
	{
		String SysName = super.getRequest().getParameter("SysName");
		String SysDescr = super.getRequest().getParameter("SysDescr");
		String SysContact = super.getRequest().getParameter("SysContact");
		String SysContactName = super.getRequest().getParameter("SysContactName");
		String SoftwareName = super.getRequest().getParameter("SoftwareName");
		String SoftwareVersion = super.getRequest().getParameter("SoftwareVersion");
		String SoftwareVendor = super.getRequest().getParameter("SoftwareVendor");
		String Copyright = "Copyright@ "+SoftwareVendor+" All Rights Reserved";
		String Theme = super.getRequest().getParameter("Theme");
//		File fileHardwareConfig = new File(FileUtils.getSysCfgPath(), "HardwareConfig");
		sysConfig = new JSONObject();
		sysConfig.put("SysName", SysName);
		sysConfig.put("SysDescr", SysDescr);
		File file = new File(PathFactory.getCfgPath(), "key.l");
		String licenseKey = WrapperShell.License;
		if( file.exists() )
		{
			licenseKey = new String(IOHelper.readAsByteArray(file));
			if( licenseKey.trim().isEmpty() )
			{
				licenseKey = WrapperShell.License;
			}
		}
		sysConfig.put("SysObjectID", licenseKey);
		sysConfig.put("SysContactName", SysContactName);
		sysConfig.put("SysContact", SysContact);
		sysConfig.put("SysLogo", "logo.png");
		sysConfig.put("SMTP", "smtp.ym.163.com");
		sysConfig.put("POP3Username", "cosguest@focusnt.com");
		sysConfig.put("POP3Password", "");
		sysConfig.put("POP3PasswordEncrypt", "8MnpkrESnrufVF==");
		sysConfig.put("SysMailName", "COS共享体验账户");
		sysConfig.put("DemoNotifies", true);
		sysConfig.put("Theme", Theme);
		
		softwareConfig = new JSONObject();
		softwareConfig.put("SoftwareName", SoftwareName);
		softwareConfig.put("SoftwareVersion", SoftwareVersion);
		softwareConfig.put("SoftwareVendor", SoftwareVendor);
		softwareConfig.put("About", SysName);
		softwareConfig.put("Copyright", Copyright);
		try
		{
			CfgMgr.set(sysConfig, SysCfgMgr.id);
			CfgMgr.set(softwareConfig, SftCfgMgr.id);
			Thread thread = new Thread(){
				public void run(){
					String title = "感谢您使用COS，关于产品的问题您都可以通过该邮件回复。";
					String content =
						"系统超级管理员账号是admin，缺省默认密码是123456。请登录后修改您的超级管理员账号密码。\r\n"+
						"目前您的系统使用的是我们为您提供的共享邮箱。请通过系统参数配置设置您的系统邮箱。\r\n"+
						Version.Remark;
					try {
						CosServer.sendSystemEmail(sysConfig, title, content);
					} catch (Exception e) {
						log.error("Failed to send the first email after register", e);
					}
				}
			};
			thread.start();
	    	ModulesMgr.createDefaultModule(ZKMgr.getZookeeper());
			super.getResponse().sendRedirect(Kit.URL_PATH(super.getRequest())+"main");
			return null;
		}
		catch (Exception e)
		{
			log.error("Failed to redirect:", e);
			super.setResponseException("注册系统失败因为异常"+e);
			return doCheckin();
		}
	}
	
	public void setImportSysLogo(File importSysLogo)
	{
		this.importSysLogo = importSysLogo;
	}

	@Override
	public Object getModel() {
		// TODO Auto-generated method stub
		return null;
	}
	public JSONObject getSysConfig() {
		return sysConfig;
	}

	public JSONObject getSoftwareConfig() {
		return softwareConfig;
	}
}
