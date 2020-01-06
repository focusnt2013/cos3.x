package com.focus.cos.ops.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONObject;

import com.focus.cos.CosServer;
import com.focus.cos.api.AlarmSeverity;
import com.focus.cos.api.AlarmType;
import com.focus.cos.api.StringUtils;
import com.focus.cos.api.Sysalarm;
import com.focus.cos.api.SysalarmClient;
import com.focus.cos.api.Sysemail;
import com.focus.cos.api.email.MailAuthenticationException;
import com.focus.cos.api.email.MailSendException;
import com.focus.cos.api.email.io.UrlResource;
import com.focus.cos.api.email.javamail.JavaMailSenderImpl;
import com.focus.cos.api.email.javamail.MimeMessageHelper;
import com.focus.cos.control.SystemEmailRunner;
import com.focus.cos.ops.dao.EmailOutboxDao;
import com.focus.sql.ConnectionPool;
import com.focus.util.Base64X;
import com.focus.util.ConfigUtil;
import com.focus.util.IOHelper;
import com.focus.util.Log;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;
import com.sun.mail.smtp.SMTPSendFailedException;

/**
 * 发送Email的引擎
 * 从TB_EMAIL_OUTBOX表读取待发送邮件，执行发送
 * @author focus
 */
public class EmailSender extends CosServer
{
	public static final String TAG_SNAPSHOT = "snapshot==";
	public static final String TAG_IMAGES = "images==";
	public static final String TAG_HTML = "html==";
	
    public static String ModuleID = "SystemEmail";
	public static int limit = 300;
	private EmailOutboxDao emailOutboxDao;
	private Zookeeper zookeeper;

	public EmailSender() throws Exception
	{
		int port = 0;
		String mp = System.getProperty("control.port", "9081");
		if( Tools.isNumeric(mp) ) port = Integer.parseInt(mp);
			try
			{
				zookeeper = Zookeeper.getInstance("127.0.0.1:"+port);
			}
			catch(Exception e)
			{
				throw new Exception("Failed to build the zookeeper", e);
			}
	}

	public static String[][] Versions = {
		{"3.16.8.27",	"针对新的主控架构重构"},
		{"3.16.9.22",	"自动告警确认恢复的问题"},
		{"3.16.9.24",	"告警自动确认备注"},
		{"3.16.9.25",	"告警产生逻辑修改"},
		{"3.16.10.27",	"发送邮件的昵称设置"},
		{"3.17.5.10",	"支持缺省的邮件配置，如果非加密密码字段为空的情况下实现从加密字段中获取邮件密码。"},
		{"3.17.8.3",	"解决告警接口调用异常引起的发送告警状态错误的问题；增加异常错误情况下错误原因记录字段。"},
		{"3.18.3.16",	"解决读取系统参数配置中邮箱密码的问题。"},
		{"3.19.7.4",	"解决异常信息入库问题。"},
		{"3.19.8.22",	"解决附件的问题。"},
		{"3.19.9.20",	"解决引擎在遇到要发送邮件长时间加载问题。"},
		{"3.19.12.12",	"解决附件加载问题; 实现用户自定义From发送"},
	};
	public static void main(String args[])
	{
        long ts = System.currentTimeMillis();
        //启动日志管理器
        Log.getInstance().setSubroot(ModuleID);
        Log.getInstance().setDebug(false);
        Log.getInstance().setLogable(true);
        Log.getInstance().start();
		Log.print("Let's begin at %s", Tools.getFormatTime("HH:mm:ss", ts) );

        String Version = Versions[Versions.length-1][0];
      	System.out.println("#Version:"+Version);
        StringBuffer info = new StringBuffer("================================================================================================");
		info.append("\r\n"+ModuleID+" "+Version);
		info.append("\r\n\tThe dir of user is "+System.getProperty("user.dir"));
		info.append("\r\n\tCopyright (C) 2008-2016 Focusnt.  All Rights Reserved.");
		info.append("\r\n================================================================================================");
		Log.msg( info.toString() );
        
		try
		{
	    	EmailSender sender = new EmailSender();
	    	buildTimeline(sender.zookeeper, getSecurityKey(), ModuleID, SystemEmailRunner.Name, SystemEmailRunner.Remark, Versions);
	    	sender.execute();
	        Log.msg("Finish " + ModuleID + " process.");
		}
		catch (Exception e)
		{
			Log.err(e);
		}
		System.exit(0);
	}

	/**
	 * 执行邮件发送
	 */
	public void execute()
	{
        File dir = new File(ConfigUtil.getWorkPath(), "data/email");
        if( !dir.exists()){
        	dir.mkdirs();
        }
        File file = new File(dir, "exception.txt");
        try
		{
            //启动数据库连接管理
            String dbDriver = System.getProperty("cos.jdbc.driver", "org.h2.Driver");
            String dbUser = System.getProperty("cos.jdbc.user", "sa");
            String dbPassword = System.getProperty("cos.jdbc.password", "");
            String dbUrl = System.getProperty("cos.jdbc.url", "jdbc:h2:tcp://localhost/../h2/cos");
            long ts = System.currentTimeMillis();
    		ConnectionPool.connect(dbUrl, dbDriver, dbUser, dbPassword);
    		ts = System.currentTimeMillis()-ts;
    		Log.printf("Succeed to connect h2 spend %sms", ts);
//    		ConnectionPool.connect("jdbc:h2:tcp://localhost/../h2/cos", "org.h2.Driver", "sa", "");
    		List<Sysemail> outboxList = EmailOutboxDao.loadOutboxEmail();
			boolean ok = false;
			if(outboxList != null && !outboxList.isEmpty() )
			{
				emailOutboxDao = new EmailOutboxDao();
				emailOutboxDao.prepareUpdate();
				String path = "/cos/config/system";
				JSONObject profile0 = getEmailConfig(path, zookeeper);
				if( profile0 == null )
				{
					Log.war("Failed to execute for not found the config from "+path);
					return;
				}
				Log.msg("Ready to send mail(size="+outboxList.size()+") by smtp-sys:\r\n\t"+profile0.toString(4));
				for(int i = 0; i < outboxList.size(); i++)
				{
					Sysemail mail = outboxList.get(i);
					try
					{
						JSONObject profile = profile0;
						if( mail.getSysid() != null && !mail.getSysid().trim().isEmpty())
						{
							path = "/cos/config/cmp/"+mail.getSysid();
							JSONObject profile1 = this.getEmailConfig(path, zookeeper);
							if( profile1 != null ) profile = profile1;
						}
						sendMail(mail, profile);//可能使用组件配置发送，缺省使用系统配置发送邮件
						ok = true;
						emailOutboxDao.update(mail.getEid(), 1, "");
					}
					catch(MailAuthenticationException e)
					{
						Log.err("Failed to send email for MailAuthenticationException.");
						handleException(e, ModuleID+"_Config", AlarmType.E, AlarmSeverity.RED);
						break;
					}
					catch(EmailExceptionContent e)
					{
						emailOutboxDao.update(mail.getEid(), 2, e.getMessage());
						Log.err("Failed to send email("+mail.getEid()+")"+e);
						handleException(e, ModuleID+"_"+e.getMessage(), AlarmType.S, AlarmSeverity.YELLOW);
					}
					catch(SMTPSendFailedException e)
					{
						emailOutboxDao.update(mail.getEid(), 2, e.getMessage());
						Log.err("Failed to send email("+mail.getEid()+")"+e);
						handleException(e, ModuleID+"_"+e.getMessage(), AlarmType.S, AlarmSeverity.YELLOW);
					}
					catch( MailSendException e )
					{
						String msg = e.getMessage();
						emailOutboxDao.update(mail.getEid(), 2, msg.length()>140?msg.substring(0,140):msg);
						Log.err("Failed to send email("+mail.getEid()+")"+e);
						handleException(e, ModuleID+"_"+e.getMessage(), AlarmType.S, AlarmSeverity.YELLOW);
					}
					catch( SendFailedException e )
					{
						emailOutboxDao.update(mail.getEid(), 3, e.getMessage());
						Log.err("Failed to send email("+mail.getEid()+")"+e);
						handleException(e, ModuleID+"_"+e.getMessage(), AlarmType.S, AlarmSeverity.YELLOW);
					}
					catch(MessagingException e)
					{
						emailOutboxDao.update(mail.getEid(), 2, e.getMessage());
						Log.err("Failed to send email("+mail.getEid()+")"+e);
						handleException(e, ModuleID+"_"+e.getMessage(), AlarmType.S, AlarmSeverity.YELLOW);
					}
					catch(Exception e)
					{
						emailOutboxDao.update(mail.getEid(), 2, e.getMessage());
						Log.err("Failed to send email("+mail.getEid()+")\r\n"+e+"\r\n"+e);
						handleException(e, ModuleID+"_"+e.getMessage(), AlarmType.S, AlarmSeverity.YELLOW);
					}
					// 置状态为已处理
				}
				Log.msg("Execute batch.");
				emailOutboxDao.batch();
				emailOutboxDao.commit();
				emailOutboxDao.close();
			}
			else
			{
				Log.msg("Not found email need to send.");
			}
			if( ok )
			{
				SysalarmClient.close("Sys", ModuleID+"_Config", "SMTP邮件参数已经配置成功");
			}
			if( file.exists() ){
				file.delete();
				SysalarmClient.close("Sys", ModuleID, "系统邮件程序引擎已经正常工作");//只要成功正常发送，就自动确认消息
			}
		}
		catch (Exception e)
		{
			Log.err("Failed to send email for exception("+e.getClass().getName()+")", e);
			handleException(e, ModuleID, AlarmType.E, AlarmSeverity.RED);
			IOHelper.writeFile(file, e.getMessage().getBytes());
		}
		ConnectionPool.disconnect();
		if( zookeeper != null )
			zookeeper.close();
	}

	/**
	 * 处理异常
	 * @param e
	 */
	private void handleException(Exception e, String alarmId, AlarmType t, AlarmSeverity s)
	{
		Sysalarm alarm = new Sysalarm();
        alarm.setSysid("Sys");
        alarm.setId(alarmId);
        alarm.setSeverity(s.getValue());
        alarm.setType(t.getValue());
        alarm.setCause(e.getMessage());
        alarm.setTitle("系统邮件程序引擎工作出现异常"+e.getMessage());
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
			PrintStream ps = new PrintStream(out);
			e.printStackTrace(ps);
	        alarm.setText("邮件引擎启动后出现异常错误:"+e+"，请系统管理员检查相关配置参数是否正确设置。\r\n"+out.toString());
			ps.close();
			SysalarmClient.send(alarm);
		}
		catch(Exception e1)
		{
		}
	}
	
	/**
	 * 发送邮件
	 * @throws Exception
	 */
	public void sendMail(Sysemail mail, JSONObject profile)
		throws Exception
	{
        if(profile == null) return ;
        String email = profile.getString("POP3Username");
		String host = profile.getString("SMTP");
		String userName = profile.getString("POP3Username");
		String password = profile.getString("POP3Password");
		if( profile.has("POP3PasswordEncrypt") )
		{
			password = profile.getString("POP3PasswordEncrypt");
			password = new String(Base64X.decode(password));
		}
		String nickname = profile.getString("SysMailName");
		String auth = "true";
		String subject = mail.getSubject();
		if( email.isEmpty() || host.isEmpty() || userName.isEmpty() || password.isEmpty() || nickname.isEmpty() )
		{
			Sysalarm alarm = new Sysalarm();
            alarm.setSysid("Sys");
            alarm.setId(ModuleID);
            alarm.setSeverity(AlarmSeverity.ORANGE.getValue());
            alarm.setType(AlarmType.S.getValue());
            alarm.setCause("未配置邮件发送参数");
            alarm.setTitle("邮件发送引擎工作不正常因为未配置发送邮件的参数");
            alarm.setText("请打开系统参数配置界面配置相关参数。");
    		SysalarmClient.send(alarm);
    		return;
		}
		try
		{
			if( subject.startsWith("【") ){
				int i = subject.indexOf("】");
				if( i != -1 ){
					nickname = subject.substring(1, i);
					subject = subject.substring(i+1);
					subject = subject.trim();
				}
			}
			nickname = javax.mail.internet.MimeUtility.encodeText(nickname);
		}
		catch (UnsupportedEncodingException e){} 
		InternetAddress from = new InternetAddress(nickname+" <"+email+">");
		if(subject == null || subject.length() == 0)
		{
			subject = Tools.getI18nProperty("ema.mail.sysmail", "系统邮件");
		}
		//邮件地址合法性验证
		if(mail.getMailTo() == null || mail.getMailTo().isEmpty() )
		{
			throw new Exception("Not found the address of email.");
		}
		ArrayList<String> tos = new ArrayList<String>();
		String[] mails = mail.getMailTo().split(";");
		for(int m = 0; m < mails.length; m++)
		{
			if(isEmail(mails[m]))
			{
				tos.add(mails[m]);
			}
		}
		if(tos.isEmpty())
			throw new Exception(Tools.getI18nProperty("ema.mail.info.addrinvalid", "邮件发送地址不合法！"));

		mail.setMailTo(StringUtils.collectionToDelimitedString(tos,";"));
		String[] to = new String[tos.size()];
		tos.toArray(to);
		String[] cc = Tools.split(mail.getCc(), ";");
		/**
		 * 文件方式：非连接外都是文件
		 * 链接方式:http://xxxxx
		 */
		sendMail(host, from, userName, password, auth, to, cc, subject, mail.getContent(), Tools.split(mail.getAttachments(), ";"));
	}
	
	/**
	 * 发用邮箱
	 * @param host
	 * @param from
	 * @param userName
	 * @param password
	 * @param auth
	 * @param to
	 * @param subject
	 * @param content
	 * @param attchMailFilename
	 * @return
	 */
	public static void sendMail(
			String host,
			InternetAddress from, 
			String userName,
			String password,
			String auth,
			String to[],
			String cc[],
			String subject,
			String content,
			String attachments[])
		throws Exception
	{
		try
		{
			JavaMailSenderImpl sender = new JavaMailSenderImpl();
			sender.setHost(host);
			sender.setUsername(userName);
			sender.setPassword(password);
//			Log.msg("Send email by "+userName+"("+password+").");

			Properties pos = new Properties();
			pos.put("mail.smtp.auth", auth);
			pos.put("mail.smtp.timeout", "25000");
			pos.put("mail.smtp.localhost", "localhost");
			sender.setJavaMailProperties(pos);

			MimeMessageHelper helper = null;
			MimeMessage msg = sender.createMimeMessage();
			if ( attachments != null && attachments.length > 0)
			{//附件xxxxx, <文件名>;xxxx, <文件名>
				helper = new MimeMessageHelper(msg, true, "UTF-8");
				appendAttach(helper, attachments);
			}
			if( content.startsWith(TAG_SNAPSHOT+"http://") )
			{//snapshot== http://为前缀表示邮件内容通过镜像下载snapshot-后面的链接得到
				if( helper == null ) helper = new MimeMessageHelper(msg, true, "UTF-8");
				setSnapshotContent(helper, content.substring(TAG_SNAPSHOT.length()));
			}
			else if( content.startsWith(TAG_IMAGES+"http://") )
			{//images== http://为前缀表示邮件内容由多个图片组成
				if( helper == null ) helper = new MimeMessageHelper(msg, true, "UTF-8");
				setImageContent(helper, Tools.split(content.substring(TAG_IMAGES.length()), ";"));
			}
			else if( content.startsWith(TAG_HTML) )
			{
				if( helper == null )helper = new MimeMessageHelper(msg, true, "UTF-8");
				setHtmlContent(helper, new StringBuffer(content.substring(TAG_HTML.length())));
			}
			else
			{
				if( helper == null )helper = new MimeMessageHelper(msg, false, "UTF-8");
				helper.setText(content, false);
			}
			helper.setTo(to);
			if( cc != null && cc.length > 0 )
				helper.setCc(cc);
			helper.setFrom(from);
			helper.setSubject(subject);
			sender.send(helper.getMimeMessage());
		}
		catch(Exception e)
		{//发送异常，可以稍后尝试发送
			throw e;
		}
	}
	/**
	 * 判断邮件内容中是否是一个链接，如果是一个链接尝试将链接下载下来
	 * @param helper
	 * @param content
	 * @return
	 */
	private static void setHtmlContent(MimeMessageHelper helper, StringBuffer html)
		throws Exception
	{
		try
		{
			HashMap<String, String> imgs = new HashMap<String, String>();
			int i = html.indexOf("cid:");
			int j = 0;
			String c = null;
			while( i >= 0 )
			{
				c = ""+html.charAt(i-1);
				i += 4;
				j = html.indexOf(c, i);
				if( j == -1 ) break;
				String uri = html.substring(i, j);
				String key = Tools.encodeMD5(uri);
				html.replace(i, j, key);
				imgs.put(key, uri);
				i += key.length();
				i = html.indexOf("cid:", i);
			}
			helper.setText(html.toString(), true);// true表示发送的是html邮件
			Set<String> set = imgs.keySet();
			Iterator<?> it = set.iterator();
			while (it.hasNext())
			{
				String key = (String) it.next();
				String imgUrl = imgs.get(key);
					helper.addInline(key, new UrlResource(imgUrl));
			}
		}
		catch(Exception e)
		{
			Log.err("Failed to setSnapshotContent", e);
			throw new EmailExceptionContent(e);
		}
	}
	
	/**
	 * 判断邮件内容中是否是一个链接，如果是一个链接尝试将链接下载下来
	 * @param helper
	 * @param content
	 * @return
	 */
	private static void setSnapshotContent(MimeMessageHelper helper, String uri)
		throws Exception
	{
		StringBuffer sb = new StringBuffer();
		HttpURLConnection connection = null;
    	InputStream in = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try
		{
			/*StringBuffer html = new StringBuffer("<html><head></head><body>");
			HashMap<String, String> imgs = new HashMap<String, String>();
			HttpClient httpclient = new HttpClient();
			GetMethod getMethod = new GetMethod(uri);
			httpclient.executeMethod(getMethod);
	
			Header ct = getMethod.getResponseHeader("Content-Type");
			if (ct != null && ct.getValue() != null)
			{
				if (ct.getValue().toLowerCase().indexOf("text") != -1)
				{
					Parser parser = new Parser(uri);
					parser.setEncoding("utf-8");
					URL url = new URL(uri);
					HtmlparserUrlModifier modifier = new HtmlparserUrlModifier(url);
					parser.visitAllNodesWith(modifier);
					html.append(modifier.getModifiedResult());
					imgs.putAll(modifier.getImgMap());
				}
				else if (ct.getValue().toLowerCase().indexOf("image") != -1)
				{
					String key = Tools.encodeMD5(uri);
					html.append("<img src = 'cid:" + key + "'>");
					imgs.put(key, uri);
				}
				else
				{
					html.append("<iframe src = '" + uri + "'></iframe>");
				}
			}
			else
			{
				html.append("<iframe src = '" + uri + "'></iframe>");
			}
			html.append("</body></html>");
			helper.setText(html.toString(), true);// true表示发送的是html邮件
			Set<String> set = imgs.keySet();
			Iterator<?> it = set.iterator();
			while (it.hasNext())
			{
				String key = (String) it.next();
				String imgUrl = imgs.get(key);
				helper.addInline(key, new UrlResource(imgUrl));
			}*/
				URLConnection theConnection = new URL(uri).openConnection();
		        if( !(theConnection instanceof HttpURLConnection) )
		        {
					throw new EmailExceptionContent("无法连接到快照网址"+uri);
		        }
		        connection = (HttpURLConnection) theConnection;
		        connection.setRequestMethod( "GET" );
		        connection.setConnectTimeout( 7000 );
		        connection.setReadTimeout( 15000 );
		        connection.setRequestProperty("Accept", "text/html");
		        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
		        connection.setRequestProperty("Accept-Language", "zh-cn");
		        connection.setRequestProperty("Connection", "close");
		        connection.setRequestProperty("Pragma", "no-cache");
		        connection.setRequestProperty("Cache-Control", "no-cache");
		        connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.3; .NET4.0C; .NET4.0E)");
		        connection.connect();
		        
		        if( connection.getResponseCode() != 200 )
		        {
					throw new EmailExceptionContent("快照网址访问失败("+connection.getResponseCode()+"):"+uri);
		        }
		        if( connection.getContentType().indexOf("text/html") == -1 )
		        {
					throw new EmailExceptionContent("快照网址不是HTML网页:"+uri);
		        }
		    	in = connection.getInputStream();
				Iterator<String> iterator = connection.getHeaderFields().keySet().iterator();
		    	while( iterator.hasNext() )
		    	{
		    		String name = iterator.next();
		    		String value = connection.getHeaderField(name);
		    		if( "Transfer-Encoding".equalsIgnoreCase(name) && value.equals("chunked") )
		    		{
		    			continue;
		    		}
		    		if( "Content-Encoding".equalsIgnoreCase(name) && value.equals("gzip") )
		    		{
		    			in = new GZIPInputStream(in);
		    			continue;
		    		}
					sb.append("\r\n\t\t"+name+" = "+value);
		    	}
				String charsetName = "UTF-8";
				String ct = connection.getContentType();
				int i = ct.indexOf("charset=") ;
				if( i != -1 ) charsetName=ct.substring(i+"charset=".length());
				
		    	int b = -1;
//		    	int len = 0;
	            while( ( b = in.read() ) != -1 )
	            {
//	            	len += 1;
	            	out.write(b);
	            }
				helper.setText(out.toString(charsetName), true);// true表示发送的是html邮件
		}
		catch(Exception e)
		{
			Log.err("Failed to setSnapshotContent"+sb, e);
			throw new EmailExceptionContent(e);
		}
		finally
		{
			try
			{
				out.close();
			}
			catch(Exception e)
			{
			}
			if( in != null ) 
				try
				{
					in.close();
				}
				catch(Exception e)
				{
				}
			if( connection != null ) 
				try
				{
					connection.disconnect();
				}
				catch(Exception e)
				{
				}
		}
	}
	
	/**
	 * 设置多个图片到邮件正文直接显示
	 * @param helper
	 * @param content
	 * @return
	 */
	private static void setImageContent(MimeMessageHelper helper, String[] uris)
		throws Exception
	{
		try
		{
			StringBuffer html = new StringBuffer("<html><head></head><body>");
			HashMap<String, String> imgs = new HashMap<String, String>();
			HttpClient httpclient = new HttpClient();
			for(String uri : uris)
			{
				GetMethod getMethod = new GetMethod(uri);
				httpclient.executeMethod(getMethod);
		
				Header ct = getMethod.getResponseHeader("Content-Type");
				if (ct != null && ct.getValue() != null)
				{
					if (ct.getValue().toLowerCase().indexOf("image") != -1)
					{
						String key = Tools.encodeMD5(uri);
						html.append("<img src = 'cid:" + key + "'><br/>");
						imgs.put(key, uri);
					}
					else
					{
						continue;
					}
				}
				else
				{
					continue;
				}
			}
	
			html.append("</body></html>");
			helper.setText(html.toString(), true);// true表示发送的是html邮件
			Set<String> set = imgs.keySet();
			Iterator<?> it = set.iterator();
			while (it.hasNext())
			{
				String key = (String) it.next();
				String imgUrl = imgs.get(key);
				helper.addInline(key, new UrlResource(imgUrl));
			}
		}
		catch(Exception e)
		{
			throw new EmailExceptionContent(e);
		}
	}
	
	/**
	 * 添加附件到邮件中
	 * @param helper
	 * @param attchMailFilename
	 * @throws Exception
	 */
	private static void appendAttach(MimeMessageHelper helper, String[] attachments)
		throws Exception
	{
		StringBuilder sb = new StringBuilder("Append the attachments to email");
		try
		{
			//https://zbjfdfs.focusnt.com/group1/M00/00/76/wKgAU13g6XqAU9c_AAMM14gEeHY065.pdf, 申请表.pdf;https://zbjfdfs.focusnt.com/group1/M00/00/63/wKgAU12NiGaAEV_rAASi0_Pe23g809.pdf, 源代码.pdf;https://zbjfdfs.focusnt.com/group1/M00/00/63/wKgAU12NiGaAEV_rAASi0_Pe23g809.pdf, 产品说明书.pdf
	//	helper.addAttachment(MimeUtility.encodeText("一.png"),
	//	    new FileDataSource("D:/focusnt/echat/trunk/IDE/5.png"));
			for(String attachment : attachments )
			{// 迭代所有附件
				String args[] = Tools.split(attachment, ", ");
				if( args[0].isEmpty() ) continue;
				String path = args[0];
				sb.append("\r\n\t"+path);
				String attachmentFilename = null;
				if( args.length == 1 )
				{
					int i = path.lastIndexOf('/');
					if( i == -1 ) continue;
					attachmentFilename = attachment.substring(i);
					i = attachmentFilename.lastIndexOf('.');
					if( i == -1 ) attachmentFilename = MimeUtility.encodeText("未知文件");
					else{
						sb.append(" ["+attachmentFilename+"]");
						attachmentFilename = MimeUtility.encodeText(attachmentFilename);
					}
				}
				else
				{
					sb.append(" ["+args[1]+"]");
					attachmentFilename = MimeUtility.encodeText(args[1]);
				}
				sb.append("  "+attachmentFilename);
				DataSource dataSource = null;
				if (attachment.startsWith("http://")||attachment.startsWith("https://"))
				{
					dataSource = new URLDataSource(new URL(path));
				}
				else
				{
					dataSource = new FileDataSource(path);
				}
				helper.addAttachment(attachmentFilename, dataSource);
			}
		}
		catch(Exception e)
		{
			PrintWriter out = new PrintWriter(new OutputStreamWriter(new ByteArrayOutputStream()));
			e.printStackTrace(out);
			throw new EmailExceptionContent(e);
		}
		finally{
			Log.war(sb.toString());
		}
	}

	private static boolean isEmail(String mail)
	{
		boolean b = mail.matches("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
		//System.out.println(b);
		return b;		
	}
	
	/*
	 *  将notify通过短信发送到目标邮箱
	 */
	public static void sendSystemEmail(
		String to[],
		String cc[],
		String title,
		String content,
		String portalUrl,
		String username,
		JSONObject profile)
	{
		InputStream is = MessageNotify.class.getResourceAsStream("/com/focus/cos/template_mail.html");
		try
		{
			String template = new String(IOHelper.readAsByteArray(is), "UTF-8");
			String logoUrl = portalUrl+"images/logo.png";
			template = template.replaceAll("%logo.url%", logoUrl);
			template = template.replaceAll("%url.login%", portalUrl);
			template = template.replaceAll("%sys.name%", profile.getString("SysName"));
			template = template.replaceAll("%responser%", username);
			template = template.replaceAll("%title%", title);
			template = template.replaceAll("%time%", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
			template = template.replaceAll("%context%", content);
			template = template.replaceAll("%sys.descr%", profile.getString("SysDescr"));
			template = template.replaceAll("%sys.contact%", profile.getString("SysContact"));
			template = template.replaceAll("%sys.email%", profile.getString("POP3Username"));

			final HashMap<String, String> themes = new HashMap<String, String>();
			themes.put("default", "#18bc9c");
			themes.put("blue", "#23bab5");
			themes.put("honey_flower", "#674172");
			themes.put("razzmatazz", "#DB0A5B");
			themes.put("ming", "#336E7B");
			themes.put("yellow", "#ffd800");
			String theme = "#18bc9c";
			if( profile != null )
			{
				theme = profile.has("Theme")?profile.getString("Theme"):"default";
				theme =  themes.get(theme);
			}
			template = template.replaceAll("%skin.color%", theme);
	        String email = profile.getString("POP3Username");
			String host = profile.getString("SMTP");
			String userName = profile.getString("POP3Username");
			String password = profile.getString("POP3Password");
			if( profile.has("POP3PasswordEncrypt") )
			{
				password = profile.getString("POP3PasswordEncrypt");
				password = new String(Base64X.decode(password));
			}
			String nickname = profile.getString("SysMailName");
			nickname = javax.mail.internet.MimeUtility.encodeText(nickname);
			InternetAddress from = new InternetAddress(nickname+" <"+email+">");
			String auth = "true";
			String subject = "系统消息: "+title;
			sendMail(host, from, userName, password, auth, to, cc, subject, TAG_HTML+template, null);
		}
		catch (Exception e)
		{
			Log.err(e);
		}
		
	}
}
