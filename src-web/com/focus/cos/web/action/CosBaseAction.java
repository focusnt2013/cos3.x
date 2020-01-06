package com.focus.cos.web.action;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.focus.cos.api.LogSeverity;
import com.focus.cos.api.LogType;
import com.focus.cos.api.SyslogClient;
import com.focus.cos.web.common.Kit;
import com.focus.util.Tools;
import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.ActionSupport;

/**
 * Description: Action基类
 * Create Date:Oct 17, 2008
 * @author Focus
 *
 * @since 1.0
 */
public class CosBaseAction extends ActionSupport
{
	private static final Log log = LogFactory.getLog(CosBaseAction.class);

	private static final long serialVersionUID = 1L;
	//
	public static String ModuleID = "COSPortal";
	//账号
	protected String account;
	// 操作结果信息 
	protected String message = "";
	// 操作结果代码 
	protected String messageCode = "";
    // 用户Session的User Attribute的Key
    //protected final static String ATTRIBUTE_ACCOUNT = "account";
    // 用户Session的Authority Attribute的Key
    //protected final static String ATTRIBUTE_AUTHORITY = "authority";
	/*返回信息*/
	protected String responseMessage;
	/*异常信息*/
	protected String responseException;
	/*确认信息*/
	protected String responseConfirm;
	/*提示后页面重定向*/
	protected String responseRedirect;
	/*视图标题*/
	protected String viewTitle;
	/*数据列表*/
	protected List<?> listData;
	/*记录唯一标识*/
	protected String id;
	/*页面时间戳*/
	protected long timestamp;
	//视图宽度
	protected int ww;
	//视图高度
	protected int wh;
	//是否功能授权
	protected boolean grant;
	//得到随机码
	public String getNonce(){
		return Tools.getUniqueValue();
	}
	public boolean isGrant() {
		return grant;
	}

	/**
	 * 获取到request
	 * 
	 * @return HttpServletRequest
	 * @author Focus
	 */
	protected HttpServletRequest getRequest()
	{
		return ServletActionContext.getRequest();
	}

	/**
	 * 获取到response
	 * 
	 * @return HttpServletResponse
	 * @author Focus
	 */
	protected HttpServletResponse getResponse()
	{
		return ServletActionContext.getResponse();
	}

	/**
	 * 获取到Session
	 * 
	 * @return HttpSession
	 * @author Focus
	 */
	protected HttpSession getSession()
	{
		return ServletActionContext.getRequest().getSession();
	}

	/**
	 * 获取ServletContext
	 * @return
	 */
	protected ServletContext getServletContext()
	{
		return ServletActionContext.getServletContext();
	}
	
	/**
	 * 输出
	 * 
	 * @param msg
	 * @throws IOException
	 */
	protected void rspWrite(String rspStr) throws IOException
    {
    	PrintWriter out = null;
		try
		{
			this.getResponse().setContentType("text/html;charset=utf-8");
			out = this.getResponse().getWriter();
			out.print(rspStr);
		}
		finally
		{
			if (out != null)
			{
				out.close();
			}
		}
    }

	/**
	 * 用JavaScript弹出提示信息
	 * @return String
	 * @author Focus
	 */
	protected String jsAlert(String msg)
	{
		try
		{
			String rspStr = "<script language='javascript'>alert('" + msg + "');</script>";
			rspWrite(rspStr);
		}
		catch (Exception e)
		{
			log.error("jsAlert", e);
		}
		return null;
	}
	
	/**
	 * 用JavaScript弹出提示信息后关闭
	 * @return String
	 * @author Focus
	 */
	protected String jsAlertAndClose(String msg)
	{
		try
		{
			String rspStr = "<script language='javascript'>alert('" + msg + "');window.close();</script>";
			rspWrite(rspStr);
		}
		catch (Exception e)
		{
			log.error("jsAlertAndClose", e);
		}
		return null;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getMessageCode()
	{
		return messageCode;
	}

	public void setMessageCode(String messageCode)
	{
		this.messageCode = messageCode;
	}

	public String getResponseMessage()
	{
		return responseMessage;
	}

	public String getResponseRedirect() 
	{
		return responseRedirect;
	}

	public void setResponseMessage(String responseMessage)
	{
		this.responseMessage = responseMessage;
	}

	public String getResponseException()
	{
		return responseException;
	}

	public void setResponseException(String responseException)
	{
		responseException = responseException.replaceAll("\"", "");
		responseException = responseException.replaceAll("'", "");
		this.responseException = responseException;
	}

	public String getViewTitle()
	{
		return viewTitle;
	}

	public void setViewTitle(String viewTitle)
	{
		this.viewTitle = viewTitle;
	}

	public String getResponseConfirm()
	{
		return responseConfirm;
	}

	public void setResponseConfirm(String responseConfirm)
	{
		this.responseConfirm = responseConfirm;
	}

	public List<?> getListData()
	{
		return listData;
	}

	public String getAccount()
	{
		return account;
	}

	public void setAccount(String account)
	{
		this.account = account;
	}

	public String getId()
	{
		return id;
	}
	
	public Long getLongId()
	{
		if( Tools.isNumeric(id)) return Long.parseLong(id);
		return 0L;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	/**
	 * 安全日志
	 * @return
	 */
	public void logsecurity(
			String account,
			String text,
			String category,
			String context,
			String contextlink,
			Exception e)
	{
		SyslogClient.write(LogType.安全日志,
				e==null?LogSeverity.INFO:LogSeverity.ERROR,
				text, 
				account,
				category,
				context,
				contextlink);
	}
	public void logsecurity(
			LogSeverity severity,
			String account,
			String text,
			String category,
			String context,
			String contextlink)
	{
		SyslogClient.write(
				LogType.安全日志,
				severity,
				text, 
				account,
				category,
				context,
				contextlink);
	}
	public void logsecurity(
			String text,
			String category,
			String context,
			String contextlink)
	{
		logsecurity(this.getUserAccount(), text, category, context, contextlink, null);
	}
	/**
	 * 操作日志
	 * @return
	 */
	public void logoper(
			LogSeverity severity,
			String account,
			String text,
			String category,
			String context,
			String contextlink,
			Exception e)
	{
		if( e != null ){
			context = context!=null?context:"";
			try
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
				PrintStream ps = new PrintStream(out);
				e.printStackTrace(ps);
				ps.close();
				context += out.toString();
			}
			catch(Exception e1)
			{
			}
		}
		SyslogClient.write(
				LogType.操作日志,
				severity,//e==null?LogSeverity.INFO:LogSeverity.ERROR,
				text, 
				account,
				category,
				context,
				contextlink);
	}
	public void logoper(
			String text,
			String category,
			String context,
			String contextlink,
			Exception e)
	{
		this.logoper(e==null?LogSeverity.INFO:LogSeverity.ERROR, this.getUserAccount(), text, category, context, contextlink, e);
	}
	public void logoper(
			String account,
			String text,
			String category,
			String context,
			String contextlink,
			Exception e)
	{
		this.logoper(e==null?LogSeverity.INFO:LogSeverity.ERROR, account, text, category, context, contextlink, null);
	}
	/**
	 * 操作日志
	 * @return
	 */
	public void logoper(
			LogSeverity severity,
			String text,
			String category,
			String context,
			String contextlink)
	{
		this.logoper(severity, this.getUserAccount(), text, category, context, contextlink, null);
	}
	/**
	 * 操作日志
	 * @return
	 */
	public void logoper(
			String text,
			String category,
			String context,
			String contextlink)
	{
		this.logoper(LogSeverity.INFO, this.getUserAccount(), text, category, context, contextlink, null);
	}
	

	/**
	 * 用户日志（记录用户访问页面的数据）
	 * @return
	 */
	public void loguser(
			String account,
			String text,
			String category,
			String context,
			String contextlink,
			Exception e)
	{
		SyslogClient.write(
				LogType.操作日志,
				e==null?LogSeverity.INFO:LogSeverity.ERROR,
				text, 
				account,
				category,
				context,
				contextlink);
	}
	/**
	 * 用户日志（记录用户访问页面的数据）
	 * @return
	 */
	public void loguser(
			String text,
			String category,
			String context,
			String contextlink)
	{
		loguser(this.getUserAccount(), text, category, context, contextlink, null);
	}

	public boolean isLogon()
	{
		return null!=this.getSession().getAttribute("account");
	}

	public JSONObject getUser()
	{
		JSONObject account = (JSONObject) this.getSession().getAttribute("account");
		return account;
	}

	public String getUserAccount()
	{
		JSONObject account = (JSONObject) this.getSession().getAttribute("account");
	    this.account = (this.account != null ? this.account : (account != null) && (account.has("username")) ? account.getString("username") : "unknown");
//		this.account = account!=null&&account.has("username")?account.getString("username"):(this.account!=null?this.account:"unknown");
		return this.account;
	}

	public String getUserToken()
	{
		JSONObject account = (JSONObject) this.getSession().getAttribute("account");
		return account!=null&&account.has("token")?account.getString("token"):"unknown";
	}

	public String getUserCookie()
	{
		JSONObject account = (JSONObject) this.getSession().getAttribute("account");
		return account!=null&&account.has("sessionid")?account.getString("sessionid"):"unknown";
	}

	public String getUserName()
	{
		JSONObject account = (JSONObject) this.getSession().getAttribute("account");
		return account!=null&&account.has("realname")?account.getString("realname"):"unknown";
	}

	public String getUserEmail()
	{
		JSONObject account = (JSONObject) this.getSession().getAttribute("account");
		return account!=null&&account.has("email")?account.getString("email"):"";
	}
	
	public int getUserRole()
	{
		JSONObject account = (JSONObject) this.getSession().getAttribute("account");
		return account!=null&&account.has("roleid")?account.getInt("roleid"):-1;
	}

	public long getUserLastLoginTimestamp()
	{
		JSONObject account = (JSONObject) this.getSession().getAttribute("account");
		return account!=null&&account.has("lastLoginTimestamp")?account.getLong("lastLoginTimestamp"):-1;
	}
	
	public String getUserHead()
	{
		JSONObject account = (JSONObject) this.getSession().getAttribute("account");
		return account!=null&&account.has("head")?account.getString("head"):"images/role/a1.png";
	}
	
	public String getLastLoginTime()
	{
		JSONObject account = (JSONObject) this.getSession().getAttribute("account");
		return account!=null&&account.has("lastLogin")?account.getString("lastLogin"):"unknown";
	}

	public Date getLastLoginDate()
	{
		JSONObject account = (JSONObject) this.getSession().getAttribute("account");
		String date = account!=null&&account.has("lastLogin")?account.getString("lastLogin"):"2013-04-12 10:08:00";
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try 
		{
			return sdf.parse(date);
		}
		catch (ParseException e) 
		{
			return new Date();
		}
	}
	
	public String getThemeColor()
	{
		String theme = (String)getSession().getAttribute("System-Theme");
		return theme;
	}
	
	public String getThemeColorLight()
	{
		String theme = (String)getSession().getAttribute("System-Theme");
		if( theme != null && theme.startsWith("#") )
		{
			int r = Integer.valueOf(""+theme.charAt(1)+theme.charAt(2), 16);
			int g = Integer.valueOf(""+theme.charAt(3)+theme.charAt(4), 16);
			int b = Integer.valueOf(""+theme.charAt(5)+theme.charAt(6), 16);
			int a = 0;
			while( a < 200 )
			{
				if( r < 255 ) r += 1; if( g < 255 ) g += 1; if( b < 255 ) b += 1;
				a = r;
				a = r>g?g:r;
				a = g>b?b:g;
			}
			theme = "#"+Integer.toHexString(r)+Integer.toHexString(g)+Integer.toHexString(b);
		}
		return theme;
	}
	
	/**
	 * 用户信息
	 * @return
	 */
	public String getUserInfo()
	{
		JSONObject account = (JSONObject) this.getSession().getAttribute("account");
		if( account == null ) return "n/a";
		if( account.has("username") && account.getString("username").equals("admin") )
		{
			return "超级管理员";
		}
		StringBuffer sb = new StringBuffer();
		if( account.has("corp"))
			sb.append(account.getString("corp"));
		if( account.has("dept"))
			sb.append(account.getString("dept"));
		if( account.has("duty"))
			sb.append(account.getString("duty"));
		if( account.has("mobile"))
		{
			if( sb.length() > 0 ) sb.append(",");
			sb.append("手机"+account.getString("mobile"));
		}
		if( sb.length() == 0 ) sb.append("-");
		return sb.toString();
	}
	

	/**
	 * 预览XML数据
	 * @return
	 * @throws IOException 
	 */
	protected void previewxml(ServletOutputStream out, byte[] payload) throws IOException
	{
		getResponse().setContentType("text/html");
		getResponse().setCharacterEncoding("UTF-8");
		out.println("<html><body style='padding: 1px;font-size:9pt;background:#000;color:#fff;word-break:keep-all;white-space:pre;'>");
		int k = 0;
		for( int i = 0; i < payload.length;  )
		{
			byte b = payload[i];
			char c = (char)payload[i++];
			if( c == '<' )
			{
				char c1 = (char)payload[i];
				if( c1 == '/' )
				{
					k -= 1;
				}
				for(int j = 0; j < k; j++)
				{
    				out.print("\t");
				}
				out.print("&lt;");
				if( c1 != '/' && c1 != '?' ) k += 1;
			}
			else if( c == '>' && i < payload.length)
			{
				out.print("&gt;");
				if( k > 0 && payload[i-2] == '/' )
					k -= 1;
				char c1 = (char)payload[i];
				if( c1 != '\r' && c1 != '\n' )
				{
					out.print("\r\n");
				}
			}
			else
			{
				out.write(b);
			}
		}
		out.println("</body>\r\n\r\n<script type='text/javascript' LANGUAGE='JavaScript'>if(parent&&parent.setScrollBottom){parent.setScrollBottom();}if(parent&&parent.skit_hiddenLoading){parent.skit_hiddenLoading();}</script></html>");
	}


	public boolean isSysadmin()
	{
		return getUserRole()==1;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public Object getModel() {
		return null;
	}

	public int getWw() {
		return ww;
	}

	public void setWw(int ww) {
		this.ww = ww;
	}

	public int getWh() {
		return wh;
	}

	public void setWh(int wh) {
		this.wh = wh;
	}

	public void setEditorContent(String content) 
	{
		this.message = content;
	}

	public void setEditorType(String type) 
	{
		this.messageCode = type;
	}

	public String getEditorContent() {
		return message!=null&&!message.isEmpty()?Kit.chr2Unicode(message):"";
	}

	public String getEditorType() {
		return messageCode!=null&&!messageCode.isEmpty()?messageCode:"";
	}
}
