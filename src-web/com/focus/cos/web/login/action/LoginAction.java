package com.focus.cos.web.login.action;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.crypto.Cipher;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.json.JSONObject;

import com.focus.cos.api.LogSeverity;
import com.focus.cos.api.Status;
import com.focus.cos.api.Sysnotify;
import com.focus.cos.api.SysnotifyClient;
import com.focus.cos.web.Version;
import com.focus.cos.web.action.CosBaseAction;
import com.focus.cos.web.common.COSConfig;
import com.focus.cos.web.common.HelperMgr;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.Skin;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.config.service.SftCfgMgr;
import com.focus.cos.web.config.service.SysCfgMgr;
import com.focus.cos.web.dev.service.ModulesMgr;
import com.focus.cos.web.login.service.CaptchaServiceSingleton;
import com.focus.cos.web.login.service.ClearCookie;
import com.focus.cos.web.login.service.Login;
import com.focus.cos.web.login.vo.Permission;
import com.focus.cos.web.ops.service.MonitorMgr;
import com.focus.cos.web.service.SvrMgr;
import com.focus.cos.web.user.service.RoleMgr;
import com.focus.cos.web.user.vo.User;
import com.focus.skit.menu.KMenu;
import com.focus.skit.tree.KTree;
import com.focus.util.HttpUtils;
import com.focus.util.IOHelper;
import com.focus.util.Tools;
import com.mongodb.BasicDBObject;
import com.octo.captcha.service.CaptchaServiceException;
import com.octo.captcha.service.image.ImageCaptchaService;
import com.opensymphony.xwork.ModelDriven;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

/**
 * Description:LoginAction
 * Create Date:Oct 19, 2008
 * @author Focus
 * @modify by Liu Xue Aug 21, 2010
 * @since 2.0
 */
public class LoginAction extends CosBaseAction implements ModelDriven
{
	private static final long serialVersionUID = 6890605126459787911L;

	private static final Log log = LogFactory.getLog(LoginAction.class);
	
	private Login login;
	/*状态*/
	private int status;
	//关于
	private String version;
	//关于版本说明
	private String versionTips;
	/*模块标识*/
	private String username;
	/*模块描述*/
	private String password;
	/*唯一的组件*/
	private JSONObject uniqeCompoent;
	/*授权提醒*/
	private String licenseTips;
	/*授权提醒*/
	private String lingpai;
	/*首页墙纸*/
	private KTree tree = new KTree();
	private ArrayList<Permission> permissions;
	private String defaultView;
	/*工具栏*/
	private KMenu toolbars = new KMenu();
	/**
	 * 执行登录
	 * @return
	 */
	//登录的标识，一次性有效
	private boolean loginflag = false;
	//强制要求设置密码
	private boolean forceResetPassword = false;
	//格子大小
	private String forceResetPasswordCount;
	
	private JSONObject token = new JSONObject();//用户的权限令牌
	public String doSignin()
	{
		User user = null;
        HttpServletRequest request = super.getRequest();
        String accept = request.getHeader("accept");
        if (!accept.startsWith("text/html") ) {//目前主流浏览器针对无法识别Content-Type的链接地址都会尝试发起两个请求。其中一个请求认为是图片。
                                              //针对非text/html的请求进行过滤，避免做两次查询浪费资源，以及链接不准确的情况
            String useragent = request.getHeader("user-agent");
            if (useragent.toUpperCase().indexOf("MSIE") == -1) {//IE浏览器不会做两个请求，因此要排除IE的请求
                return null;
            }
        }
        if (!request.getMethod().equalsIgnoreCase("post") ) {//只接受post请求
        	return null;
        }
		JSONObject sysConfig = SysCfgMgr.getConfig();
		String str = sysConfig.has("AuthFailRepeat")?sysConfig.getString("AuthFailRepeat"):"";
		if( !Tools.isNumeric(str) ) str = "5";
		int iAuthFailRepeat = Integer.parseInt(str);
		str = sysConfig.has("AuthFailLock")?sysConfig.getString("AuthFailLock"):"";
		if( !Tools.isNumeric(str) ) str = "10";
		int iAuthFailLock = Integer.parseInt(str);
		str = sysConfig.has("AuthResetPeriod")?sysConfig.getString("AuthResetPeriod"):"";
		if( !Tools.isNumeric(str) ) str = "0";
		int iAuthResetPeriod = Integer.parseInt(str);
		loginflag = true;//当用户发起登录时打开
		String ip = Kit.getIp(request);
		String region = HttpUtils.getIpRegion(ip);
		if (!confirmCode())
		{
			setResponseException("验证码输入不正确！");
			logsecurity(LogSeverity.ERROR, username, super.getResponseException(), "用户登录", null, null);
			return index();
		}
		try{
			user = login.findByAccount(username);
		}
		catch(Exception e){
			setResponseException("数据库连接异常请联系系统管理员。");
			return index();
		}
		if( user == null )
		{
			setResponseException("账号并不存在。");
			logsecurity(LogSeverity.ERROR, username, super.getResponseException(), "用户登录", null, null);
			return index();
		}
		if( RoleMgr.isRoleAbort(user.getRoleid()) )
		{
			setResponseException("所属的角色权限组所有用户已经被禁止登录。");
			logsecurity(LogSeverity.ERROR, username, super.getResponseException(), "用户登录", null, null);
			return index();
		}
		if( user.getStatus() == Status.Disable.getValue() )
		{
			setResponseException("该用户账号已经被停用。");
			logsecurity(LogSeverity.ERROR, username, super.getResponseException(), "用户登录", null, null);
			return index();
		}
		boolean loginResult = DigestUtils.md5Hex(password).equals(user.getPassword());
		//密码验证
		if( !loginResult)
		{
			user.setErrorCount(user.getErrorCount()+1);
			iAuthFailLock *= Tools.MILLI_OF_MINUTE;
			if(user.getErrorCount() >=iAuthFailRepeat)
			{
				user.setStatus(Status.Suspension.getValue());
				Sysnotify notify = SvrMgr.sendNotiefiesToSystemadmin(super.getRequest(),
						"用户账号安全", 
						"用户账号["+user.getUsername()+"]"+user.getErrorCount()+"次用错误密码登录", 
						"用户账号["+user.getUsername()+"]"+user.getErrorCount()+"次用错误密码登录，登录IP是"+ip+"，登录区域是"+region+"，账号被锁定"+Kit.getDurationMs(iAuthFailLock), 
						"user!query.action",
						"用户管理", 
						"user!usermgr.action");
				super.setResponseException(notify.getContext());
			}
			else
			{
				super.setResponseException("您还有"+(iAuthFailRepeat-user.getErrorCount())+"次机会重试密码，超过账号将被锁定禁止登录"+Kit.getDurationMs(iAuthFailLock));
			}
			user.setLastLogin(new Date());
			user.setLastLoginIp(ip);
			user.setLastLoginRegion(region);
			login.update(user);
			//登录失败日志
			logsecurity(LogSeverity.ERROR, username, super.getResponseException(), "用户登录", null, null);
			return index();
		}
		HttpSession session = super.getSession();
		//状态判断
		switch(user.getStatus().byteValue()) 
		{
			case 2://Status.Disable:
			{
				Sysnotify notify = SvrMgr.sendNotiefiesToSystemadmin(super.getRequest(),
					"用户账号安全", 
					this.getText("label.ema.user")+username+this.getText("label.ema.login.fail"), 
					"该用户账号已经被停用后用户仍然尝试登录，登录IP是"+ip+"，登录区域是"+region+"，请了解相关停用原因通知该用户。", 
					"user!query.action",
					"用户管理", 
					"user!usermgr.action");
				logsecurity(LogSeverity.ERROR, username, notify.getContext(), "用户登录", null, notify.getContextlink());
				super.setResponseException(notify.getContext());
				return index();
			}
			case 3://Status.Suspension:
			{
				Calendar now = Calendar.getInstance();
				now.add(Calendar.MINUTE, -iAuthFailLock);
				Date lastLogin = user.getLastLogin();
				if(lastLogin != null && lastLogin.after(now.getTime()))
				{
					long unlockTime = lastLogin.getTime()+iAuthFailLock*Tools.MILLI_OF_MINUTE;
//					setResponseException(this.getText("label.ema.login.error.3times"));
					Sysnotify notify = SvrMgr.sendNotiefiesToSystemadmin(super.getRequest(),
						"用户账号安全", 
						this.getText("label.ema.user")+this.getText("label.ema.login.fail"), 
						"该用户账号用错误密码登录超过"+user.getErrorCount()+
						"次，登录IP是"+ip+"，登录区域是"+region+
						"，需等待锁定期("+Tools.getFormatTime("yyyy-MM-dd HH:mm", unlockTime)+
						")结束或者联系系统管理员。", 
						"user!query.action",
						"用户管理", 
						"user!usermgr.action");
					logsecurity(LogSeverity.ERROR, username, notify.getContext(), "用户登录", null, notify.getContextlink());
					super.setResponseException(notify.getContext());
					return index();
				}
				break;
			}
			default:
			{
				long lastChangePasswordTime = user.getLastChangePasswordTime();
				if( lastChangePasswordTime == 0 ){
					session.setAttribute("forceResetPassword", "初始登录需要重置密码");
				}
				else {
					if( iAuthResetPeriod == 0 ) iAuthResetPeriod = 30;
					lastChangePasswordTime = user.getLastChangePasswordTime();
					long day = System.currentTimeMillis() - lastChangePasswordTime;
					day /= Tools.MILLI_OF_DAY;
					final long T = iAuthResetPeriod*Tools.MILLI_OF_DAY;
					long expireTime = lastChangePasswordTime+T;//到期时间
					if( System.currentTimeMillis() > expireTime && T > 0 )
					{//密码到期，但是判断上次登录时间，如果
						user.setErrorCount(user.getErrorCount()+1);
						if(user.getErrorCount() > iAuthFailRepeat)
						{
							//被提示重置密码多次后仍然不重置密码就强行关闭
							Sysnotify notify = new Sysnotify();
//							notify.setAction("修改密码");
//							notify.setActionlink("user!preModifyPwd.action");
//							notify.setUseraccount(user.getUsername());
//							notify.setFilter("用户账号安全");
//							notify.setNotifytime(new Date());
//							notify.setPriority(1);
//							notify.setTitle("您的账号已经超过"+day+"天没有修改过密码，为了您的账号安全系统将暂停您登录，您可联系系统管理员重置密码");
//							notify.setContext("您的账号上次登录时间是"+Tools.getFormatTime("yyyy-MM-dd HH:mm",  user.getLastLoginTime())+
//									"，修改密码的时间是"+Tools.getFormatTime("yyyy-MM-dd HH:mm",  user.getLastChangePassword().getTime())+
//									"，已经超过"+day+"天没有修改过密码，为了您的账号安全系统将暂停您登录，您可联系系统管理员重置密码。");
//							SysnotifyClient.submit( notify);
//							super.setResponseException(notify.getContext());
							notify = SvrMgr.sendNotiefiesToSystemadmin(super.getRequest(),
									"用户账号安全", 
									"用户账号["+user.getUsername()+"]已经超过"+day+"天没有修改过密码，为了系统安全已强制禁用其账号", 
									"用户账号["+user.getUsername()+"]上次登录时间是"+Tools.getFormatTime("yyyy-MM-dd HH:mm",  user.getLastLoginTime())+
									"，修改密码的时间是"+Tools.getFormatTime("yyyy-MM-dd HH:mm",  user.getLastChangePassword().getTime())+
									"，已经超过"+day+"天没有修改过密码，为了系统安全已强制禁用其账号，，系统管理员可帮他重置该账号密码。", 
									"user!query.action",
									"用户管理", 
									"user!usermgr.action");
							logsecurity(LogSeverity.ERROR, username, notify.getContext(), "用户登录", null, notify.getContextlink());
//							return index();
							user.setStatus(Status.Disable.getValue());
						}
						else
						{//密码超期登录计数没超过限制
							int lastChance = iAuthFailRepeat-user.getErrorCount();
							session.setAttribute("forceResetPassword", "您的账号超过"+day+"天没有修改过密码，已经超出允许的时间范围，您还有"+lastChance+"次登录机会修改密码，超出限制将禁止您登录本系统。");
							session.setAttribute("forceResetPasswordCount", lastChance);
						}
					}
					else
					{//正常登录
						if(user.getErrorCount() > 0)
						{
							user.setErrorCount(0);
						}
						if(user.getStatus() == Status.Suspension.getValue())
						{
							//可以为用户自动解锁
							user.setStatus(Status.Enable.getValue());
						}
						if( iAuthResetPeriod - day <= 3 )
						{
							Sysnotify notify = new Sysnotify();
							notify.setAction("修改密码");
							notify.setActionlink("user!preModifyPwd.action");
							notify.setUseraccount(user.getUsername());
							notify.setFilter("用户账号安全");
							notify.setNotifytime(new Date());
							notify.setPriority(1);
							notify.setTitle("您的账号已经超过"+day+"天没有修改过密码，还差"+day+"天将密码到期，为了您的账号安全请重置密码");
							notify.setContext("您的账号上次登录时间是"+Tools.getFormatTime("yyyy-MM-dd HH:mm", user.getLastLoginTime())+
									"，修改密码的时间是"+Tools.getFormatTime("yyyy-MM-dd HH:mm", user.getLastChangePassword().getTime())+
									"，已经超过"+day+"天没有修改过密码，为了您的账号安全请重置密码。");
							SysnotifyClient.submit( notify);
						}
					}
				}
				break;
			}
		}
		user.setToken();
		lingpai = user.getToken();
		//将上次登录的时间放在session中，用于展示系统通知
		long lastLastLoginTime = user.getLastLoginTime();
		String loginRemark = "您的账号上次的登录时间是"+Tools.getFormatTime("yyyy-MM-dd HH:mm", lastLastLoginTime)+
				"，登录IP是"+user.getLastLoginIp()+"，登录区域是"+user.getLastLoginRegion()+"。"+
				"您本次登录时间是"+Tools.getFormatTime("yyyy-MM-dd HH:mm", System.currentTimeMillis())+
				"，登录IP是"+ip+"，登录区域是"+region+"。请随时确保您的账号安全。";
		if( lastLastLoginTime == 0 ){
			loginRemark = "这是您第一次登录，您本次登录时间是"+Tools.getFormatTime("yyyy-MM-dd HH:mm", System.currentTimeMillis())+
				"，登录IP是"+ip+"，登录区域是"+region+"。请随时确保您的账号安全。";
		}
		session.setAttribute("Timestamp-LastLogin", lastLastLoginTime);
		if( "admin".equals(user.getUsername()) && (user.getEmail() == null || user.getEmail().isEmpty()) ){
			user.setEmail(sysConfig.has("SysContact")?sysConfig.getString("SysContact"):"");
		}
		user.setLastLogin(new Date());
		user.setLastLoginIp(ip);
		user.setLastLoginRegion(region);
		login.update(user);
		JSONObject cookie = new JSONObject();
		try
		{
			cookie = ZKMgr.getZookeeper().getJSONObject("/cos/user/properties/"+user.getUsername(), true);
		}
		catch(Exception e )
		{
			log.error("Failed to get the data of user from zookeeper for "+e);
		}
		cookie = cookie!=null?cookie:new JSONObject();
		cookie.put("id", user.getId());
		cookie.put("roleid", user.getRoleid());
		cookie.put("username", user.getUsername());
		if("admin".equals(user.getUsername()) )
			cookie.put("realname", "系统管理员");
		else if( user.getRealname() == null )
			cookie.put("realname", user.getUsername());
		else
			cookie.put("realname", user.getRealname());
		if( user.getEmail() != null )
			cookie.put("email", user.getEmail());
		cookie.put("token", user.getToken());
		cookie.put("lastLogin", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", user.getLastLogin().getTime()));
		cookie.put("lastLoginTimestamp", user.getLastLoginTime());
		if( !login.setLoginCookie(cookie, super.getRequest().getCookies()) )
		{
			session.invalidate();
			setResponseException("登录会话失效，请重新尝试登录");
			return main();
		}
		session.setAttribute("account_name", user.getUsername());
		session.setAttribute("account", cookie);
		logsecurity("用户账号【"+user.getRealname()+"】登录成功，登录IP是"+ip+"，登录区域是"+region+"。", "用户登录", null, null);
//		session.setAttribute(ATTRIBUTE_USER, user);
		this.viewTitle = this.getText("label.ema.login.home.page");
		try{
			Sysnotify notify = new Sysnotify();
			notify.setUseraccount(user.getUsername());
			notify.setFilter("用户账号安全");
			notify.setNotifytime(new Date());
			notify.setPriority(1);
			notify.setTitle("欢迎您"+(cookie.has("count")?("第"+cookie.getInt("count")+"次"):"首次")+"使用"+SysCfgMgr.get("SysName"));
			notify.setContext(loginRemark);
			SysnotifyClient.send( notify);
		}
		catch(Exception e){
			log.error("Failed to send the notify of system for ", e);
		}
		
		try
		{
			super.getResponse().sendRedirect(Kit.URL_PATH(super.getRequest())+"main");
		}
		catch (Exception e)
		{
			log.error("Failed to redirect:", e);
		}
		return index();
	}
	
	public boolean isForceResetPassword() {
		return forceResetPassword;
	}

	public String getForceResetPasswordCount() {
		return forceResetPasswordCount!=null&&!forceResetPasswordCount.isEmpty()?forceResetPasswordCount:"-1";
	}

	/**
	 * 验证码
	 * @return boolean
	 * @since 3.0
	 */
	private boolean confirmCode()
	{
		// 难证码是否正确flag
		Boolean isResponseCorrect = Boolean.FALSE;

		// 取session用来验证是否在同一session中
		if (super.getSession() == null)
		{
			return false;
		}
		String captchaId = super.getSession().getId();

		// 取前台输入的难证码
		String response = super.getRequest().getParameter("j_captcha_response");
		Key privateKey = null;
		try
		{
			privateKey = (Key)super.getSession().getAttribute("privateKey");
			if( privateKey != null )
			{
				Cipher c = Cipher.getInstance("RSA");
	    		c.init(Cipher.DECRYPT_MODE, privateKey);
				byte[] payload = Base64.decode(response);
				//使用私钥初始化编码器用于解密
				response = new String(c.doFinal(payload));
				log.info("Succeed to get the j_captcha_response by rsa("+privateKey+").");
			}
			else
			{
				log.warn("Faield to get the j_captcha_response for not found privateKey.");
			}
			response = Tools.replaceStr(response, " ", "");
			ImageCaptchaService ics = CaptchaServiceSingleton.getInstance();
			// 取得验证对象，并检验session和输入验证码是否正确
			isResponseCorrect = ics.validateResponseForID(captchaId, response);
			// 返回验证结果
			return isResponseCorrect;
		}
		catch (CaptchaServiceException e)
		{
//			log.error("取验证码异常", e);
	        HttpServletRequest request = super.getRequest();
//	        String accept = request.getHeader("accept");
            StringBuffer sb = new StringBuffer();
            sb.append("Unknown request("+request.getMethod()+") from ");
            String ip = request.getRemoteAddr();
            sb.append("\r\n\tip="+ip);
            Enumeration<String> e1 = request.getHeaderNames();
            BasicDBObject pv = new BasicDBObject();
            pv.put("ip", ip);
            while(e1.hasMoreElements())
            {
            	String key = e1.nextElement();
            	sb.append("\r\n\t");
            	sb.append(key);
            	sb.append("=");
            	pv.put(key, request.getHeader(key));
            	sb.append(request.getHeader(key)+"\t");
            }
            pv.put("path", request.getServletPath());
            sb.append("\r\n\tpath="+request.getServletPath());
            log.info(sb.toString());
		} catch (Exception e) {
//			log.error("Failed to decode the crcode("+response+") for ", e);
		}
		return false;
	}
	
	/**
	 * 退出系统，清除session
	 * 
	 * @return String
	 * @author Focus
	 * @throws IOException 
	 */
	public String doSignout()
	{
		log.debug("do Logout");
		logsecurity("用户退出系统登录状态。", "用户登录", null, null);
		ClearCookie.logout(super.getUserToken(), super.getUserCookie());
		HttpSession session = super.getSession();
		session.invalidate();
		try
		{
			super.getResponse().sendRedirect(Kit.URL_PATH(super.getRequest())+"main");
		}
		catch (IOException e)
		{
			log.error("Failed to redirect:", e);
			return "logout";
		}
		return null;//返回首页
	}
	/**
	 * 打开首页
	 * @return
	 */
	public String index()
	{
		if( loginflag )
		{//如果进入这个函数，标识为true，表示是从ｌｏｇｉｎ操作过来的，执行判断
			Skin skin = Skin.getInstance();
			if( skin != null && skin.getName().equals("defone") )
			{
				this.setCookie();
				Key publicKey = (Key)super.getSession().getAttribute("privateKey");
				if( publicKey == null )
				{
					try
					{
						super.getResponse().sendRedirect(Kit.URL_PATH(super.getRequest())+"main");
					}
					catch (IOException e)
					{
						log.error("Failed to redirect:", e);
					}
					return null;
				}
                this.lingpai = Base64.encode(publicKey.getEncoded());
				return "login_defone";
			}
		}
		if( !isLogon() )
		{//登录失败，要重定向到登录界面
			//Add by lx at 2016-6-15 below
			Skin skin = Skin.getInstance();
			if( skin != null && skin.getName().equals("defone") )
			{
				try
				{
					super.getResponse().sendRedirect(Kit.URL_PATH(super.getRequest())+"main");
				}
				catch (IOException e)
				{
					log.error("Failed to redirect:", e);
				}
				return null;
			}
		}
//		String dataurl = "control!costimeline.action?id=COSPortal&ip=127.0.0.1&port="+COSConfig.getLocalControlPort();
//		this.version = "helper!timeline.action?dataurl="+Kit.chr2Unicode(dataurl);
		this.account = super.getUserAccount();
		JSONObject sysConfig = SysCfgMgr.getConfig();
		if( sysConfig != null && sysConfig.has("DemoNotifies") && sysConfig.getBoolean("DemoNotifies"))
		{
			super.getSession().setAttribute("abortOtherNotifies",true);
		}
		/**
		 * 暂时删除授权管理的功能 at 2017.1.18
		 */
		this.licenseTips = login.checkLicense(super.getRequest());
		if( licenseTips!= null && !licenseTips.isEmpty() && "admin".equals(this.account) )
		{
			Sysnotify notify = new Sysnotify();
			notify.setAction("联系授权");
			notify.setActionlink("mailto:licence@focusnt.com.cn);");
			notify.setUseraccount("admin");
			notify.setFilter("服务授权");
			notify.setNotifytime(new Date());
			notify.setPriority(0);
			notify.setTitle("本平台所使用的【云架构开放式应用服务系统】授权提醒["+Tools.getFormatTime("yyyy-MM-dd", System.currentTimeMillis())+"]");
			notify.setContext(licenseTips);
			SysnotifyClient.submit(notify);
		}
        try
        {
        	this.viewTitle = sysConfig!=null&&sysConfig.has("SysName")?sysConfig.getString("SysName"):this.getText("label.ema.login.home.page");
    		JSONObject role = RoleMgr.getRolePrivileges(super.getUserRole(), super.getUserAccount());
//			File fileRole = new File(PathFactory.getCfgPath(), "role/"+super.getUserRole() );
//			if( fileRole.exists() ) role = (Role)IOHelper.readSerializableNoException(fileRole);
    		List<JSONObject> list = ModulesMgr.getConfigs();
    		boolean hasCmp = list.size()>1;
    		for(int i = 0; i < list.size(); i++)
    		{
    			JSONObject profile = list.get(i);
    			if( profile.has("Disabled") && profile.get("Disabled").toString().equalsIgnoreCase("true") )
    			{
    				list.remove(i);
    				i -= 1;
    				continue;
    			}
    			if( role == null || !role.has( profile.getString("id") ))
    			{//角色是否有组件权限，如果没有就不允许用户看到这个组件；同时组件如果不存在也不允许用户在首页看见组件

    				list.remove(i);
    				i -= 1;
    				continue;
    			}
    		}
    		this.listData = list;
    		if( listData.size() == 1 )
    		{
    			this.uniqeCompoent = (JSONObject)listData.get(0);
    		}
    		else if( listData.isEmpty() )
    		{
    			if( !hasCmp )
	    		{
	    			this.status = super.getUserRole()==1?1:3;
	    		}
    			else if( ZKMgr.getZookeeper().exists("/cos/config/modules") == null )
    			{
	    			this.status = super.getUserRole()==1?2:3;
    			}
    		}
    		
    		if( !listData.isEmpty() && role == null )
			{
    			this.status = super.getUserRole()==1?4:3;
			}
        }
        catch(Exception e)
        {
        	this.status = -1;
        	super.setResponseException("因为"+e.getMessage()+",暂无法提供服务，请联系系统管理员解决。");
        	//ZK链接有问题，这个时候提示相关功能暂时不可用
        }
        String forceResetPasswordTips = (String)super.getSession().getAttribute("forceResetPassword");
        if( forceResetPasswordTips != null )
        {
        	this.forceResetPassword = true;
        	this.responseMessage = forceResetPasswordTips;
        	Object o = super.getSession().getAttribute("forceResetPasswordCount");
        	this.forceResetPasswordCount = o!=null?o.toString():"-1";
        	super.getSession().removeAttribute("forceResetPassword");
        	super.getSession().removeAttribute("forceResetPasswordCount");
        }
		Skin skin = Skin.getInstance();
		if( skin != null && skin.getName().equals("defone") )
		{
			return "index_defone";
		}
		return "index";
	}
	
	/**
	 * 为登录用户产生创建Cookie
	 */
	private void setCookie()
	{
		HttpServletResponse response = super.getResponse();
		Cookie cookie = new Cookie("COSSESSIONID", com.focus.cos.web.util.Tools.generateUUID());
		cookie.setPath("/");
		response.addCookie(cookie);
	}
	
	/**
	 * 
	 * @return
	 */
	public String license()
	{
		File file = new File(PathFactory.getDataPath(), "cos.l");
		if( file.exists() )
		{
			Key privateKey = null;
			DataInputStream dis = null;
			licenseTips = "当前您的软件服务授权已过期，请联系您的服务提供商。";
			try
			{
				dis = new DataInputStream(new ByteArrayInputStream(IOHelper.readAsByteArray(file)));
				int len = dis.readInt();
				byte[] wrappedKey = new byte[len];
				dis.readFully(wrappedKey);
				Cipher c = Cipher.getInstance("RSA");
				file = new File(PathFactory.getDataPath(), "identity.pk");
				privateKey = (Key)IOHelper.readSerializable(file);
				c.init(Cipher.UNWRAP_MODE, privateKey);
				Key key = c.unwrap(wrappedKey, "AES", Cipher.SECRET_KEY);

				c = Cipher.getInstance("AES");
				c.init(Cipher.DECRYPT_MODE, key);
				len = dis.readInt();
				byte[] value = new byte[len];
				dis.readFully(value);
				licenseTips = new String(c.doFinal(value));
			}
			catch (Exception e)
			{
			}
			finally
			{
				if( dis != null )
					try
					{
						dis.close();
					}
					catch (IOException e)
					{
					}
			}
			this.setResponseMessage(licenseTips);
			return "licenseTimeout";
		}
		return null;
	}
	/**
	 * 
	 * @param request
	 * @return
	 */
	private String getRequestDetails(HttpServletRequest request){

		StringBuffer sb = new StringBuffer();
        sb.append("http://" + request.getHeader("Host") + request.getContextPath()+request.getServletPath());        Enumeration<String> e1 = request.getHeaderNames();
        while (e1.hasMoreElements()) {
            String key = e1.nextElement();
            sb.append("\r\n\t");
            sb.append(key);
            sb.append(": ");
            sb.append(request.getHeader(key) + "\t");
        }
        e1 = request.getParameterNames();
        sb.append("\r\n\t====================");
        while (e1.hasMoreElements()) {
            String key = e1.nextElement();
            sb.append("\r\n\t");
            sb.append(key);
            sb.append("=");
            sb.append(request.getParameter(key) + "\t");
        }
        sb.append("\r\n\tremote_ip=" + request.getRemoteAddr());
        return sb.toString();
	}
	/**
	 * 打开通用的面板
	 * @return
	 */
	private static boolean TheMainNotOpened = true;
	public String main()
	{
        HttpServletRequest request = super.getRequest();
		try
		{
			Version.initialized(Kit.LOC(super.getRequest()));
			MonitorMgr.getInstance().checkStart();
	        String accept = request.getHeader("accept");
	        if( accept == null ) return null;
	        if (!accept.startsWith("text/html")) {//目前主流浏览器针对无法识别Content-Type的链接地址都会尝试发起两个请求。其中一个请求认为是图片。
	                                              //针对非text/html的请求进行过滤，避免做两次查询浪费资源，以及链接不准确的情况
	            String useragent = request.getHeader("user-agent");
	            if (useragent.toUpperCase().indexOf("MSIE") == -1) {//IE浏览器不会做两个请求，因此要排除IE的请求
	                return null;
	            }
	        }
			Object timestamp = request.getSession().getAttribute("Timestamp-Login");
			if( timestamp != null )
			{
				long ts = (Long)timestamp;
				ts = System.currentTimeMillis() - ts;
				if( ts < 1000 )
				{
			        log.warn("Found the request of login repeated\r\n"+this.getRequestDetails(request));
					return null;
				}
			}
			if( TheMainNotOpened ){
				log.info("First access the main by "+this.getRequestDetails(request));
				TheMainNotOpened = false;
			}
			final HashMap<String, String> themes = new HashMap<String, String>();
			themes.put("default", "#18bc9c");
			themes.put("blue", "#23bab5");
			themes.put("honey_flower", "#674172");
			themes.put("razzmatazz", "#DB0A5B");
			themes.put("ming", "#336E7B");
			themes.put("yellow", "#ffd800");
			defaultView = null;
			Cookie[] cookies = super.getRequest().getCookies();
			if( cookies != null )
				for( Cookie c : cookies )
				{
					String name = c.getName();// get the cookie name
					String value = c.getValue(); // get the cookie value
					if("themeStyle".equals(name)){
//						System.err.println(value);
						defaultView = value;
						break;
					}
				}
			if( defaultView != null && !defaultView.isEmpty() && themes.containsKey(this.defaultView) ){
				request.getSession().setAttribute("System-Theme", themes.get(this.defaultView));
			}
	        request.getSession().setAttribute("Timestamp-Login", System.currentTimeMillis());
			JSONObject sysConfig = SysCfgMgr.getConfig();
			if( sysConfig != null )
			{
				Kit.EnableHttps = "true".equals(SysCfgMgr.get("EnableHttps"));
				Kit.EnableHttps = "true".equals(SysCfgMgr.get("EnableHttps"));
				if( defaultView == null || defaultView.isEmpty() ){
					this.defaultView = sysConfig.has("Theme")?sysConfig.getString("Theme"):"default";
					request.getSession().setAttribute("System-Theme", themes.get(this.defaultView));
				}
				viewTitle = super.getText("label.ema.login");
				if( !sysConfig.has("PortalUrl") )
				{
					String host = super.getRequest().getHeader("host");
					String args[] = Tools.split(host, ".");
					boolean b = true;
					for(String str: args)
					{
						if( !Tools.isNumeric(str) ) b = false;
					}
					if( args.length != 4 && !b )
					{//域名就设置
						sysConfig.put("PortalUrl", Kit.URL(super.getRequest()));
						SysCfgMgr.set(sysConfig, "system");
						log.info("Succeed to the url of portal "+sysConfig.getString("PortalUrl"));
					}
				}
			}
			else
			{
				super.getResponse().sendRedirect(Kit.URL_PATH(super.getRequest())+"checkin");
				return null;
			}
		}
		catch(Exception e)
		{
			log.error("Failed to main for exception:", e);
		}
		if( !isLogon() )
		{
			this.setCookie();
		}
		Skin skin = Skin.getInstance();
		if( skin != null && skin.getName().equals("defone") )
		{
			if( !isLogon() ) 
			{
				Key publicKey = (Key)request.getSession().getAttribute("publicKey");
				Key privateKey = (Key)request.getSession().getAttribute("privateKey");
				if( publicKey == null || privateKey == null )
					try
					{
						KeyPairGenerator keyPair = KeyPairGenerator.getInstance("RSA");
		                SecureRandom random = new SecureRandom();
		                keyPair.initialize(512, random);
		                KeyPair keyP = keyPair.generateKeyPair();
		                privateKey = keyP.getPrivate();
		                publicKey = keyP.getPublic();
		                request.getSession().setAttribute("privateKey", privateKey);
					}
					catch(Exception e)
					{
						log.error("Failed to create RSA for ", e);
					}
                this.lingpai = Base64.encode(publicKey.getEncoded());
				return "login_defone";
			}
			account = super.getUserAccount();
	        log.info(String.format("[%s] Succeed to open the main of portal by %s", request.getSession().getId(), account));
			toolbars = new KMenu();
			ArrayList<KTree> trees = new ArrayList<KTree>();
			permissions = new ArrayList<Permission>();
			login.loadNavigate(toolbars, trees, permissions, request, super.getUser(), token, true);
			login.setToken(super.getUserToken(), token);
			this.listData = trees;
			this.versionTips = Version.getVersionUpgradeInfo();
			this.version = Version.getValue();
//			this.alarms = alarmMgr.queryInstanceAlarm(-1,"","","",null);
			StringBuffer dm = new StringBuffer();
			if( "admin".equalsIgnoreCase(account) )
			{//超级管理员才可以使用这个用户权限
			}
			if( Version.getCOSSecurityKey() != null )
			{
				dm.append("<li class='divider'></li>");
				String dataurl = "control!costimeline.action?id=COSPortal&ip=127.0.0.1&port="+COSConfig.getLocalControlPort();
				dm.append("<li><a href=\"#\" onclick=\"openView('关于COS主界面框架系统', 'helper!timeline.action?dataurl="+Kit.chr2Unicode(dataurl)+"');\"><i class=\"fa fa-lastfm fa-fw\"></i> 关于COS主界面框架系统</a></li>");
				dataurl = "control!costimeline.action?id=COSControl&ip=127.0.0.1&port="+COSConfig.getLocalControlPort();
				dm.append("<li><a href=\"#\" onclick=\"openView('关于COS主控引擎', 'helper!timeline.action?dataurl="+Kit.chr2Unicode(dataurl)+"');\"><i class=\"fa fa-linux fa-fw\"></i> 关于COS主控引擎</a></li>");
			}
			developerMenu = dm.toString();
			Long ts = (Long)super.getSession().getAttribute("Timestamp-LastLogin");
			if( ts != null ) this.timestamp = ts;
			return "portal_defone";
		}
		return "portal";
	}
//	private String reportMenu;
	private String developerMenu;
	public String fogotpassword()
	{
		return "fogotpassword";
	}
	
	/**
	 * 直接打开监控界面
	 * @return
	public String monitor()
	{
		try
		{
			File file = new File(PathFactory.getSysCfgPath(), "SysConfig");
			sysConfig = (Profile) IOHelper.readSerializable(file);
			file = new File(PathFactory.getSysCfgPath(), "SoftwareConfig");
			softwareConfig = (Profile) IOHelper.readSerializable(file);
			defaultView = "omt!monitor.action";
			account = "FocusMonitor";
			viewTitle = super.getText("label.ema.omt.monitoring");
		}
		catch(Exception e)
		{
		}
		return "monitor";
	}
	 */
	
	/**
	 * 用于转换中兴的密码
	 * @param s
	 * @return
	private static String GetHexFromChs(String s)
    {
		StringBuffer sb = new StringBuffer();
		try
		{
			 byte[] bytes = s.getBytes("gb2312");
		        for (int i = 0; i < bytes.length; i++)
		        {
		        	int k = bytes[i] < 0 ? 256 + bytes[i] : bytes[i];
		            if(k < 0x10)
		            {
		                sb.append('0');
		            }
		            sb.append(Integer.toHexString(k));

		            if(i + 1 == bytes.length)
		            {
		                return sb.toString();
		            }
		        }
		}
		catch (Exception e) {
			// TODO: handle exception
		}
        return "";
    }
	 */

	
	public void setLogin(Login login)
	{
		this.login = login;
	}

	public Object getModel()
	{
		return null;
	}
	
	public int getStatus()
	{
		return status;
	}

	public void setUsername(String username)
	{
		Key privateKey = null;
		try
		{
			privateKey = (Key)super.getSession().getAttribute("privateKey");
			if( privateKey != null )
			{
				Cipher c = Cipher.getInstance("RSA");
	    		c.init(Cipher.DECRYPT_MODE, privateKey);
				byte[] payload = Base64.decode(username);
				//使用私钥初始化编码器用于解密
				username = new String(c.doFinal(payload));
				log.info("Succeed to get the username by rsa("+privateKey+").");
			}
			else
			{
				log.warn("Faield to get the username for not found privateKey.");
			}
		}
		catch(Exception e)
		{
//			log.error("Failed to decode the username by \r\n\t"+privateKey, e);
		}
		this.username = username;
	}

	public void setPassword(String password)
	{
		Key privateKey = null;
		try
		{
			privateKey = (Key)super.getSession().getAttribute("privateKey");
			if( privateKey != null )
			{
				Cipher c = Cipher.getInstance("RSA");
	    		c.init(Cipher.DECRYPT_MODE, privateKey);
				byte[] payload = Base64.decode(password);
				//使用私钥初始化编码器用于解密
				password = new String(c.doFinal(payload));
				log.info("Succeed to get the passowrd by rsa("+privateKey+").");
			}
			else
			{
				log.warn("Faield to get the password for not found privateKey.");
			}
		}
		catch(Exception e)
		{
//			log.error("Failed to decode the password by \r\n\t"+privateKey, e);
		}
		this.password = password;
	}

	public JSONObject getSysConfig()
	{
		return SysCfgMgr.getConfig();
	}

	public JSONObject getSoftwareConfig()
	{
		return SftCfgMgr.getConfig();
	}

	public JSONObject getUniqeCompoent()
	{
		return uniqeCompoent;
	}
	public KTree getTree()
	{
		return tree;
	}

	public String getDefaultView()
	{
		return defaultView;
	}

	public String getSystemLogo()
	{
		return Kit.URL_IMAGEPATH(super.getRequest())+"logo.png";
	}

	public String getSystemName()
	{
		return SysCfgMgr.get("SysName");
	}

	public String getSoftwareId()
	{
		return SftCfgMgr.get("SoftwareName");
	}

	public String getSoftwareVersion()
	{
		return SftCfgMgr.get("SoftwareVersion");
	}
	
	/**
	 * 清理Cookie
	 * @return
	 */
	public String clearCookie()
	{
		Exception e = ClearCookie.execute();
		if( e == null )
			this.setResponseMessage("清理Cookie成功。");
		else
			this.setResponseException("清理Cookie出现异常"+e);
		return "close";
	}

	public String getLicenseTips()
	{
		return licenseTips;
	}

	public String getLingpai()
	{
		return lingpai;
	}

	public String getWallpaper()
	{
		if(super.getSession().getAttribute("first") == null )
		{
			super.getSession().setAttribute("first", System.currentTimeMillis());
			return "images/home.jpg";
		}
		return HelperMgr.getImgWallpaper();
	}

	public String getVersion()
	{
		return version;
	}

	public KMenu getToolbars()
	{
		return toolbars;
	}

	public String getVersionTips()
	{
		return versionTips;
	}

	public String getDeveloperMenu()
	{
		return developerMenu;
	}
	
	private Boolean developer;
	public boolean isDeveloper()
	{
		if( developer != null ) return developer;
		developer = super.isSysadmin();
		if( !developer )
		{//检查当前用户是否是开发者
			try
			{
				ZooKeeper zookeeper = ZKMgr.getZooKeeper();
				Stat stat = zookeeper.exists("/cos/config/modules", false); 
				if( stat != null)
				{
					List<String> nodes = zookeeper.getChildren("/cos/config/modules", false);
					for(String cmpid : nodes)
					{
						JSONObject cmp = ZKMgr.getZookeeper().getJSONObject("/cos/config/modules/"+cmpid, false);
						if( cmp.has("Developers") )
						{
							JSONObject develpers = cmp.getJSONObject("Developers");
							if( develpers.has(super.getUserAccount()))
							{
								developer = true;
								break;
							}
						}
					}
				}
			}
			catch(Exception e)
			{
			}
		}
		return developer;
	}
	//是否自动升级
	public boolean isAutoupgrade()
	{
		File upgradeDir = new File(PathFactory.getWebappPath(), "upgrade.aot");
		return !upgradeDir.exists();
	}
}