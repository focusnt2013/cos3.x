package com.focus.cos.web.common.filter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.json.JSONObject;

import com.focus.cos.api.LogSeverity;
import com.focus.cos.api.LogType;
import com.focus.cos.api.Syslog;
import com.focus.cos.api.SyslogClient;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.user.service.RoleMgr;

/**
 * Description:检查用户是否登录
 * Create Date:Nov 13, 2008
 * @author Focus
 *
 * @since 1.0
 */
public class LoginCheckFilter implements Filter
{
	private static final Log log = LogFactory.getLog(LoginCheckFilter.class);
	/*过滤映射表*/
	private HashMap<String, String> filter = new HashMap<String, String>(); 

	public void init(FilterConfig filterConfig) throws ServletException
	{
		filter.put("security!sso.action", null);
		filter.put("security!sysuser.action", null);
		filter.put("security!syslog.action", null);
		filter.put("security!sysalarm.action", null);
		filter.put("security!sysnotify.action", null);
		filter.put("security!sysemail.action", null);
		filter.put("security!programpublish.action", null);
		filter.put("security!synchfiles.action", null);
		filter.put("security!addsysuser.action", null);
		filter.put("security!reportmonitor.action", null);
		filter.put("security!configmonitor.action", null);
		filter.put("digg!api.action", null);
		filter.put("digg!open.action", null);
		filter.put("digg!opendata.action", null);
		filter.put("digg!apiexport.action", null);
		filter.put("helper!download.action", null);
		filter.put("helper!developer.action", null);
		filter.put("helper!icon.action", null);
		filter.put("sftcfg!about.action", null);
		filter.put("http!get.action", null);
		filter.put("http!post.action", null);
		filter.put("http!getinfo.action", null);
		filter.put("login!main.action", null);
//		filter.put("login!index.action", null);
//		filter.put("login!navigate.action", null);
//		filter.put("login!signin.action", null);
		filter.put("login!signout.action", null);
		filter.put("cos!signin.action", null);
		filter.put("cos!checkin.action", null);
		filter.put("cos!uploadLogo.action", null);
		filter.put("login!license.action", null);
		filter.put("sessionout", null);
		filter.put("digg!snapshot.action", null);
		filter.put("digg!download.action", null);
		filter.put("monitorload!serverchart.action", null);
		filter.put("digg!snapshot.action", null);
		filter.put("editor!xml.action", null);
		filter.put("editor!javascript.action", null);
		filter.put("editor!json.action", null);
		filter.put("http!post.action", null);
	}
	
	public void destroy()
	{
		filter.clear();
	}
	/**
	 * 
	 */
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
			FilterChain filterChain) throws IOException, ServletException
	{
		HttpServletRequest request = (HttpServletRequest)servletRequest;
		HttpServletResponse response = (HttpServletResponse)servletResponse;
		HttpSession session = request.getSession(false);
		Syslog syslog = new Syslog();
		syslog.setLogtext(request.getRequestURI());
		String uri = syslog.getLogtext();
		if( uri.isEmpty() ){
			filterChain.doFilter(servletRequest, servletResponse);
			return;
		}
		uri = uri.substring(uri.lastIndexOf("/")+1);
		try
		{
			String referer = request.getHeader("referer");
			String ip = Kit.getIp(request);
			String userAgent = request.getHeader("user-agent");
            syslog.setLogtext(Kit.URLPATH(request));
			String cookie = request.getHeader("cookie");
			JSONObject context = new JSONObject();
			context.put("ip", ip);
			context.put("user-agent", userAgent);
			context.put("cookie", cookie);
			context.put("referer", referer);
			syslog.setContext(context.toString(4));
			syslog.setCategory(uri.endsWith(".action")?uri.substring(0, uri.length()-7):"");
			syslog.setLogseverity(LogSeverity.INFO.getValue());
			syslog.setLogtype(LogType.用户日志.getValue());
//			System.err.println("doFilter:"+uri);
//			String flag = WebWorkConfigLoader.getProperty("webwork.filter.login", "true");
			if( uri.endsWith(".html") || uri.endsWith(".htm") ) filterChain.doFilter(servletRequest, servletResponse);
			else if( filter.containsKey(uri) ) filterChain.doFilter(servletRequest, servletResponse);
			else if( uri.indexOf("svrapi/") != -1 ) filterChain.doFilter(servletRequest, servletResponse);
//			else if( !"true".equals(flag) ) filterChain.doFilter(servletRequest, servletResponse);
			else
			{
				try
				{
					ZooKeeper zookeeper = ZKMgr.getZooKeeper();
					//记录cookie
					Cookie[] cookies = request.getCookies();
					boolean logon = false;
					if( cookies != null )
						for( Cookie c : cookies )
						{
							String name = c.getName();// get the cookie name
							String value = c.getValue(); // get the cookie value
							if( "COSSESSIONID".equalsIgnoreCase(name) )
							{
								String path = "/cos/login/cookie/"+value;
								Stat stat = zookeeper.exists(path, false); 
								if( stat == null)
								{
									log.warn("Not found cookie("+value+") from "+uri+" and force to quite.");
									session.invalidate();
									response.sendRedirect(Kit.URL_PATH(request)+ "sessionout");
									return;
								}
								else if( session.getAttribute("account") == null )
								{
									int version = -1;
									if( session.getAttribute("account_expired") != null )
									{
										version = (Integer)session.getAttribute("account_expired");
									}
									if( version != stat.getVersion() )
									{
										session.setAttribute("account_expired", stat.getVersion());
										String json = new String(zookeeper.getData(path, false, stat), "UTF-8");
										session.setAttribute("account", new JSONObject(json));
									}
								}
								logon = true;
								break;
							}
						}
					if( !logon )
					{
						log.warn("Not found session("+session.getId()+") logon.");
						session.invalidate();
						response.sendRedirect(Kit.URL_PATH(request)+ "sessionout");
						return;
					}
				}
				catch(Exception e)
				{
				}
				//检查Session是否过期
				if(session == null)
				{
					StringBuffer sb = new StringBuffer();
					sb.append("Session timeout url " + uri);
					Enumeration<String> names = request.getHeaderNames();
					while( names.hasMoreElements() )
					{
						String key = names.nextElement();
						String value = request.getHeader(key);
						if( "connection".equalsIgnoreCase(key) )
							value = "keep-alive";
						sb.append("\r\n\t\t"+key+" = "+value);
					}
					log.warn(sb.toString());
					response.sendRedirect(Kit.URL_PATH(request)+ "sessionout");
					return;
				}

				session.setAttribute("referer", uri.toString());			
				if( session.getAttribute("license") != null )
				{
					response.sendRedirect("login!license.action");
					return;
				}
				
				JSONObject account = (JSONObject)session.getAttribute("account");
				if( session.getAttribute("account") == null)
				{
					log.error("Session timeout url " + uri);
					response.sendRedirect(Kit.URL_PATH(request)+ "sessionout");
					return;
				}
				try
				{
					if( !account.has("roleid") || RoleMgr.isRoleAbort(account.getInt("roleid")) )
					{
						log.error("Session timeout url " + uri);
						response.sendRedirect(Kit.URL_PATH(request)+ "sessionout");
						return;
					}
//					log.info("Found valid uri "+uri);
					filterChain.doFilter(servletRequest, servletResponse);
					session.setAttribute("referer", uri.toString());
				}
				catch(Exception e)
				{
					if( e.getCause() instanceof IllegalArgumentException )
					{
						//log.error("Failed to handle filter for IllegalArgumentException ", e);
						response.sendRedirect(Kit.URL_PATH(request)+ "404");
					}
					else
					{
						log.info("Found invalid uri["+uri+"] for "+e.getMessage());
//						log.error("Failed to handle filter Exception", e);
						session.setAttribute("exception", e.getCause());
						response.sendRedirect(Kit.URL_PATH(request)+ "500");
					}
				}
			}
		}
		catch(Exception exception)
		{
		}
		finally
		{
			try
			{
				JSONObject account = (JSONObject)session.getAttribute("account");
				if( account != null ) syslog.setAccount(account.getString("username"));
			}
			catch(Exception e)
			{}
			SyslogClient.submit(syslog);
		}
	}
}
