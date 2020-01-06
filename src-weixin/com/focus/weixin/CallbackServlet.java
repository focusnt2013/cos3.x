package com.focus.weixin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.w3c.dom.Node;

import com.focus.cos.api.AlarmSeverity;
import com.focus.cos.api.AlarmType;
import com.focus.util.Log;
import com.focus.util.MongoX;
import com.focus.util.Tools;
import com.focus.util.XMLParser;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;

public abstract class CallbackServlet extends HttpServlet {

	private static final long serialVersionUID = -2289914884174706324L;
	protected CallbackServer server;
	/*互动时间戳*/
	private HashMap<String, Custom> custom = new HashMap<String, Custom>();
	
	public CallbackServlet(CallbackServer server)
	{
		this.server = server;
		if( this.getVersion() != null ) System.out.println("#Version:"+this.getVersion());
	}
	
	static class Custom
	{
		long timestamp;
		int count;//点击在线咨询计数器
	}
	
	/**
	 * 返回回调实现程序的版本号
	 * return version
	 */
	public abstract String getVersion();
	
	/**
	 * 处理消息与事件
	 * @param request
	 * @param response
	 * @return
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException 
	{
		StringBuffer log = new StringBuffer("Receive callback from "+request.getRemoteAddr()+" "+request.getRequestURL());
		byte[] bs = null;
		try
		{
			ServletInputStream servletInputStream = request.getInputStream();
			bs = readFullInputStream(servletInputStream);
			log.append("\r\n");
			log.append(new String(bs,"UTF-8"));
		}
		catch (IOException exception) 
		{
			Log.err(exception);
			throw exception;
		}
		ByteArrayInputStream bais = new ByteArrayInputStream(bs);
		long ts = System.currentTimeMillis();
		org.bson.Document e = null;
		String openid = null;
		e = new org.bson.Document();
		e.put("IP", getIp(request));
		e.put("UserAgent", request.getHeader("User-Agent"));
		e.put("CreateDate", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", ts));
		e.put("timestamp", ts);
		try
		{
			XMLParser xmlParser = new XMLParser(bais);
			Node xml = xmlParser.getRootNode();
			String toUserName = getXmlValue(xml, "ToUserName");
			String fromUserName = getXmlValue(xml, "FromUserName");
			openid = fromUserName;
			String createTime = getXmlValue(xml, "CreateTime");
			String msgType = getXmlValue(xml, "MsgType");
			e.put("MsgType", msgType);
			e.put("FromUserName", fromUserName);
			e.put("ToUserName", toUserName);
			e.put("CreateTime", createTime);
			if("event".equalsIgnoreCase(msgType))
			{
				if( !this.handleForEvent(request, response, xml, e, log) )
					response.getWriter().println("");
			}
			else
			{
				if( !this.handleForMessage(request, response, xml, e, log) )
					response.getWriter().println("");
			}
			Log.msg("************************************************************************************************\r\n"+log.toString());
		}
		catch (Exception exception) 
		{
			e.put("exception", exception.toString());
			Log.err(log.toString(), exception);
			CallbackServer.handleException("微信公众号开发者模式处理回调异常", log.toString(), exception, CallbackServer.ModuleID+"_servlet", AlarmType.S, AlarmSeverity.BLUE);
		}
		finally
		{
			try
			{
				MongoCollection<org.bson.Document> col_callback = MongoX.getDBCollection(server.getWeixinno()+"_callback");
				MongoCollection<org.bson.Document> col_user = MongoX.getDBCollection(server.getWeixinno()+"_users");
				if( openid != null ) MongoX.update(col_user, new BasicDBObject("openid", openid), new org.bson.Document("timestamp", ts));
				if( e != null )	col_callback.insertOne(e);
			}
			catch(Exception exception)
			{
				CallbackServer.handleException("微信公众号开发者模式存储数据异常", log.toString(), exception, CallbackServer.ModuleID+"_mongodb", AlarmType.S, AlarmSeverity.YELLOW);
			}
		}
	}
	
	/**
	 * 处理消息
	 * @param request
	 * @param response
	 * @param xml
	 * @param e
	 * @param log
	 * @throws Exception
	 */
	private boolean handleForMessage(HttpServletRequest request, HttpServletResponse response, Node xml, org.bson.Document e, StringBuffer log) throws Exception
	{
		boolean responsed = false;
		if( server.isCustom() )
		{
			long ts = System.currentTimeMillis();
			String fromUserName = this.getXmlValue(xml, "FromUserName");
			String toUserName = this.getXmlValue(xml, "ToUserName"); 
			Custom c = custom.get(fromUserName);
			if( c != null )
			{
				c.timestamp = ts;
			}
			// 转多客服
			String respXML = "<xml>" +
			"<ToUserName><![CDATA["+fromUserName+"]]></ToUserName>" +
			"<FromUserName><![CDATA["+toUserName+"]]></FromUserName>" +
			"<CreateTime>"+(ts / 1000)+"</CreateTime>" +
			"<MsgType><![CDATA[transfer_customer_service]]></MsgType>" +
			"</xml>";
			response.getOutputStream().write(respXML.getBytes("UTF-8"));
			log.append("\r\n\tRedirect to transfer_customer_service.");
			responsed = true;
		}
		
		String msgType = getXmlValue(xml, "MsgType");
		//https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140453&token=&lang=zh_CN
		if("text".equalsIgnoreCase(msgType))
		{
			/*
				ToUserName	开发者微信号
				FromUserName	发送方帐号（一个OpenID）
				CreateTime	消息创建时间 （整型）
				MsgType	text
				Content	文本消息内容
				MsgId	消息id，64位整型
			*/
			e.put("MsgID", this.getXmlValue(xml, "MsgID"));
			e.put("Content", this.getXmlValue(xml, "Content"));
			if( !responsed ) responsed = this.handleForMessageText(request, response, xml);
		}
		else if("image".equalsIgnoreCase(msgType))
		{
			/*
				ToUserName	开发者微信号
				FromUserName	发送方帐号（一个OpenID）
				CreateTime	消息创建时间 （整型）
				MsgType	image
				PicUrl	图片链接（由系统生成）
				MediaId	图片消息媒体id，可以调用多媒体文件下载接口拉取数据。
				MsgId	消息id，64位整型
			*/
			e.put("PicUrl", this.getXmlValue(xml, "PicUrl"));
			e.put("MediaId", this.getXmlValue(xml, "MediaId"));
			e.put("MsgID", this.getXmlValue(xml, "MsgID"));
			if( !responsed ) responsed = this.handleForMessageImage(request, response, xml);
		}
		else if("voice".equalsIgnoreCase(msgType))
		{
			/*
				ToUserName	开发者微信号
				FromUserName	发送方帐号（一个OpenID）
				CreateTime	消息创建时间 （整型）
				MsgType	语音为voice
				MediaId	语音消息媒体id，可以调用多媒体文件下载接口拉取该媒体
				Format	语音格式：amr
				Recognition	语音识别结果，UTF8编码
				MsgID	消息id，64位整型
			 */
			e.put("MediaId", this.getXmlValue(xml, "MediaId"));
			e.put("Format", this.getXmlValue(xml, "Format"));
			e.put("Recognition", this.getXmlValue(xml, "Recognition"));
			e.put("MsgID", this.getXmlValue(xml, "MsgID"));
			if( !responsed ) responsed = this.handleForMessageVoice(request, response, xml);
		}
		else if("video".equalsIgnoreCase(msgType))
		{
			/*
				ToUserName	开发者微信号
				FromUserName	发送方帐号（一个OpenID）
				CreateTime	消息创建时间 （整型）
				MsgType	语音为voice
				MediaId	视频消息媒体id，可以调用多媒体文件下载接口拉取数据。
				ThumbMediaId	视频消息缩略图的媒体id，可以调用多媒体文件下载接口拉取数据。
				MsgId	消息id，64位整型
			*/
			e.put("MediaId", this.getXmlValue(xml, "MediaId"));
			e.put("ThumbMediaId", this.getXmlValue(xml, "ThumbMediaId"));
			e.put("MsgID", this.getXmlValue(xml, "MsgID"));
			if( !responsed ) responsed = this.handleForMessageVideo(request, response, xml);
		}
		else if("shortvideo".equalsIgnoreCase(msgType))
		{
			/**
				ToUserName	开发者微信号
				FromUserName	发送方帐号（一个OpenID）
				CreateTime	消息创建时间 （整型）
				MsgType	小视频为shortvideo
				MediaId	视频消息媒体id，可以调用多媒体文件下载接口拉取数据。
				ThumbMediaId	视频消息缩略图的媒体id，可以调用多媒体文件下载接口拉取数据。
				MsgId	消息id，64位整型
			 */
			e.put("MediaId", this.getXmlValue(xml, "MediaId"));
			e.put("ThumbMediaId", this.getXmlValue(xml, "ThumbMediaId"));
			e.put("MsgID", this.getXmlValue(xml, "MsgID"));
			if( !responsed ) responsed = this.handleForMessageShortvideo(request, response, xml);
		}
		else if("location".equalsIgnoreCase(msgType))
		{
			/**
				ToUserName	开发者微信号
				FromUserName	发送方帐号（一个OpenID）
				CreateTime	消息创建时间 （整型）
				MsgType	location
				Location_X	地理位置维度
				Location_Y	地理位置经度
				Scale	地图缩放大小
				Label	地理位置信息
				MsgId	消息id，64位整型
			 */
			e.put("Location_X", this.getXmlValue(xml, "Location_X"));
			e.put("Location_Y", this.getXmlValue(xml, "Location_Y"));
			e.put("Scale", this.getXmlValue(xml, "Scale"));
			e.put("Label", this.getXmlValue(xml, "Label"));
			e.put("MsgID", this.getXmlValue(xml, "MsgID"));
			if( !responsed ) responsed = this.handleForMessageLocation(request, response, xml);
		}
		else if("link".equalsIgnoreCase(msgType))
		{
			/**
				ToUserName	接收方微信号
				FromUserName	发送方微信号，若为普通用户，则是一个OpenID
				CreateTime	消息创建时间
				MsgType	消息类型，link
				Title	消息标题
				Description	消息描述
				Url	消息链接
				MsgId	消息id，64位整型
			*/
			e.put("Title", this.getXmlValue(xml, "Title"));
			e.put("Description", this.getXmlValue(xml, "Description"));
			e.put("Url", this.getXmlValue(xml, "Url"));
			e.put("MsgID", this.getXmlValue(xml, "MsgID"));
			if( !responsed ) responsed = this.handleForMessageLink(request, response, xml);
		}
		else
		{
			log.append("\r\n\tUnknown message("+msgType+").");
			if( !responsed ) responsed = this.handleForMessageUnknown(request, response, xml);
		}
		return responsed;
	}
	
	/**
	 * 处理文本消息的抽象方法（由继承类实现）
	 * @param request http请求消息
	 * @param response http响应消息（由事件继承实现)
	 * @param xml 微信传过来的xml节点对象 org.w3c.dom.Node
			ToUserName	开发者微信号
			FromUserName	发送方帐号（一个OpenID）
			CreateTime	消息创建时间 （整型）
			MsgType	text
			Content	文本消息内容
			MsgId	消息id，64位整型
	 * @return 微信服务器在五秒内收不到响应会断掉连接，并且重新发起请求，总共重试三次。
	 * 	如果抽象方法实现消息回复返回true，否则返回false，由上层框架负责回复空串，微信服务器不会对此作任何处理，并且不会发起重试。
	 */
	public abstract boolean handleForMessageText(HttpServletRequest request, HttpServletResponse response, Node xml) throws Exception;
	
	/**
	 * 处理图片消息的抽象方法（由继承类实现）
	 * @param request http请求消息
	 * @param response http响应消息（由事件继承实现)
	 * @param xml 微信传过来的xml节点对象 org.w3c.dom.Node
			ToUserName	开发者微信号
			FromUserName	发送方帐号（一个OpenID）
			CreateTime	消息创建时间 （整型）
			MsgType	image
			PicUrl	图片链接（由系统生成）
			MediaId	图片消息媒体id，可以调用多媒体文件下载接口拉取数据。
			MsgId	消息id，64位整型
	 * @return 微信服务器在五秒内收不到响应会断掉连接，并且重新发起请求，总共重试三次。
	 * 	如果抽象方法实现消息回复返回true，否则返回false，由上层框架负责回复空串，微信服务器不会对此作任何处理，并且不会发起重试。
	 */
	public abstract boolean handleForMessageImage(HttpServletRequest request, HttpServletResponse response, Node xml) throws Exception;
	
	/**
	 * 处理音频消息的抽象方法（由继承类实现）
	 * @param request http请求消息
	 * @param response http响应消息（由事件继承实现)
	 * @param xml 微信传过来的xml节点对象 org.w3c.dom.Node
			ToUserName	开发者微信号
			FromUserName	发送方帐号（一个OpenID）
			CreateTime	消息创建时间 （整型）
			MsgType	语音为voice
			MediaId	语音消息媒体id，可以调用多媒体文件下载接口拉取该媒体
			Format	语音格式：amr
			Recognition	语音识别结果，UTF8编码
			MsgID	消息id，64位整型
	 * @return 微信服务器在五秒内收不到响应会断掉连接，并且重新发起请求，总共重试三次。
	 * 	如果抽象方法实现消息回复返回true，否则返回false，由上层框架负责回复空串，微信服务器不会对此作任何处理，并且不会发起重试。
	 */
	public abstract boolean handleForMessageVoice(HttpServletRequest request, HttpServletResponse response, Node xml) throws Exception;
	
	/**
	 * 处理视频消息的抽象方法（由继承类实现）
	 * @param request http请求消息
	 * @param response http响应消息（由事件继承实现)
	 * @param xml 微信传过来的xml节点对象 org.w3c.dom.Node
			ToUserName	开发者微信号
			FromUserName	发送方帐号（一个OpenID）
			CreateTime	消息创建时间 （整型）
			MsgType	视频为video
			MediaId	视频消息媒体id，可以调用多媒体文件下载接口拉取数据。
			ThumbMediaId	视频消息缩略图的媒体id，可以调用多媒体文件下载接口拉取数据。
			MsgId	消息id，64位整型
	 * @return 微信服务器在五秒内收不到响应会断掉连接，并且重新发起请求，总共重试三次。
	 * 	如果抽象方法实现消息回复返回true，否则返回false，由上层框架负责回复空串，微信服务器不会对此作任何处理，并且不会发起重试。
	 */
	public abstract boolean handleForMessageVideo(HttpServletRequest request, HttpServletResponse response, Node xml) throws Exception;
	
	/**
	 * 处理小视频消息的抽象方法（由继承类实现）
	 * @param request http请求消息
	 * @param response http响应消息（由事件继承实现)
	 * @param xml 微信传过来的xml节点对象 org.w3c.dom.Node
			ToUserName	开发者微信号
			FromUserName	发送方帐号（一个OpenID）
			CreateTime	消息创建时间 （整型）
			MsgType	小视频为shortvideo
			MediaId	视频消息媒体id，可以调用多媒体文件下载接口拉取数据。
			ThumbMediaId	视频消息缩略图的媒体id，可以调用多媒体文件下载接口拉取数据。
			MsgId	消息id，64位整型
	 * @return 微信服务器在五秒内收不到响应会断掉连接，并且重新发起请求，总共重试三次。
	 * 	如果抽象方法实现消息回复返回true，否则返回false，由上层框架负责回复空串，微信服务器不会对此作任何处理，并且不会发起重试。
	 */
	public abstract boolean handleForMessageShortvideo(HttpServletRequest request, HttpServletResponse response, Node xml) throws Exception;
	
	/**
	 * 处理位置消息的抽象方法（由继承类实现）
	 * @param request http请求消息
	 * @param response http响应消息（由事件继承实现)
	 * @param xml 微信传过来的xml节点对象 org.w3c.dom.Node
			ToUserName	开发者微信号
			FromUserName	发送方帐号（一个OpenID）
			CreateTime	消息创建时间 （整型）
			MsgType	location
			Location_X	地理位置维度
			Location_Y	地理位置经度
			Scale	地图缩放大小
			Label	地理位置信息
			MsgId	消息id，64位整型
	 * @return 微信服务器在五秒内收不到响应会断掉连接，并且重新发起请求，总共重试三次。
	 * 	如果抽象方法实现消息回复返回true，否则返回false，由上层框架负责回复空串，微信服务器不会对此作任何处理，并且不会发起重试。
	 */
	public abstract boolean handleForMessageLocation(HttpServletRequest request, HttpServletResponse response, Node xml) throws Exception;
	
	/**
	 * 处理链接消息的抽象方法（由继承类实现）
	 * @param request http请求消息
	 * @param response http响应消息（由事件继承实现)
	 * @param xml 微信传过来的xml节点对象 org.w3c.dom.Node
			ToUserName	接收方微信号
			FromUserName	发送方微信号，若为普通用户，则是一个OpenID
			CreateTime	消息创建时间
			MsgType	消息类型，link
			Title	消息标题
			Description	消息描述
			Url	消息链接
			MsgId	消息id，64位整型
	 * @return 微信服务器在五秒内收不到响应会断掉连接，并且重新发起请求，总共重试三次。
	 * 	如果抽象方法实现消息回复返回true，否则返回false，由上层框架负责回复空串，微信服务器不会对此作任何处理，并且不会发起重试。
	 */
	public abstract boolean handleForMessageLink(HttpServletRequest request, HttpServletResponse response, Node xml) throws Exception;
	
	/**
	 * 处理未知消息的抽象方法（由继承类实现）
	 * @param request http请求消息
	 * @param response http响应消息（由事件继承实现)
	 * @param xml 微信传过来的xml节点对象 org.w3c.dom.Node
	 * @return 微信服务器在五秒内收不到响应会断掉连接，并且重新发起请求，总共重试三次。
	 * 	如果抽象方法实现消息回复返回true，否则返回false，由上层框架负责回复空串，微信服务器不会对此作任何处理，并且不会发起重试。
	 */
	public abstract boolean handleForMessageUnknown(HttpServletRequest request, HttpServletResponse response, Node xml) throws Exception;
	
	/**
	 * 处理事件
	 * @param request
	 * @param response
	 * @param xml
	 * @param e
	 * @param log
	 * @throws Exception 
	 */
	private boolean handleForEvent(HttpServletRequest request, HttpServletResponse response, Node xml, org.bson.Document e, StringBuffer log) throws Exception
	{
		String event = getXmlValue(xml, "Event");
		String eventKey = getXmlValue(xml, "EventKey");
		e.put("Event", event);
		e.put("EventKey", eventKey);
		boolean responsed = false;
		
		if("view".equalsIgnoreCase(event))
		{
			responsed = this.handleForEventView(request, response, xml);
		}
		else if( "TEMPLATESENDJOBFINISH".equalsIgnoreCase(event) )
		{//模版消息
			e.put("MsgID", this.getXmlValue(xml, "MsgID"));
			e.put("Status", this.getXmlValue(xml, "Status"));
		}
		else if("click".equalsIgnoreCase(event))
		{
			if("custom".equalsIgnoreCase(eventKey))
			{
				responsed = this.handleForEventCustom(request, response, xml, log);
			}
			else
			{
				responsed = this.handleForEventClick(request, response, xml);
			}
		}
		else if( "subscribe".equalsIgnoreCase(event) || "scan".equalsIgnoreCase(event) )
		{
			e.put("Ticket", this.getXmlValue(xml, "Ticket"));
			String openid = this.getXmlValue(xml, "FromUserName");
			log.append("\r\n\tFound the user "+openid+" to bind.");
			org.bson.Document user = server.getUserInfo(openid);
			responsed = this.handleForEventSubscribe(request, response, xml, user!=null?user.toJson():null);
		}
		else if("unsubscribe".equalsIgnoreCase(event))
		{
			String openid = this.getXmlValue(xml, "FromUserName");
			responsed = this.handleForEventUnsubscribe(request, response, xml);
			MongoCollection<org.bson.Document> col_user = MongoX.getDBCollection(server.getWeixinno()+"_users");
			MongoX.update(col_user, new BasicDBObject("openid", openid), new org.bson.Document("bind", 0));
			log.append("\r\n\tSet the user "+openid+" to unbind.");
		}
		else if("LOCATION".equalsIgnoreCase(event))
		{
			e.put("Latitude", this.getXmlValue(xml, "Latitude"));
			e.put("Longitude", this.getXmlValue(xml, "Longitude"));
			e.put("Precision", this.getXmlValue(xml, "Precision"));
			responsed = this.handleForEventLocation(request, response, xml);
		}
		else if("location_select".equalsIgnoreCase(event))
		{
			//<SendLocationInfo><Location_X><![CDATA[39.96284480333743]]></Location_X> <Location_Y><![CDATA[116.3065396415107]]></Location_Y> <Scale><![CDATA[15]]></Scale> <Label><![CDATA[北京市海淀区苏州桥金洲大厦内(长春桥路北)]]></Label> <Poiname><![CDATA[海淀区苏州桥金洲大厦内(长春桥路北)]]></Poiname> </SendLocationInfo>
			Node node = XMLParser.getElementByTag(xml, "SendLocationInfo");
			if( node != null )
			{
				BasicDBObject sendLocationInfo = new BasicDBObject();
				sendLocationInfo.put("Location_X", this.getXmlValue(node, "Location_X"));
				sendLocationInfo.put("Location_Y", this.getXmlValue(node, "Location_Y"));
				sendLocationInfo.put("Scale", this.getXmlValue(node, "Scale"));
				sendLocationInfo.put("Label", this.getXmlValue(node, "Label"));
				sendLocationInfo.put("Poiname", this.getXmlValue(node, "Poiname"));
				e.put("SendLocationInfo", sendLocationInfo);
			}
			responsed = this.handleForEventLocationSelect(request, response, xml);
		}
		else if("pic_sysphoto".equalsIgnoreCase(event))
		{
			/**
			 * <SendPicsInfo><Count>1</Count> <PicList><item><PicMd5Sum><![CDATA[cfdc714e447c95e37e3d44d5cd32d5f6]]></PicMd5Sum> </item> </PicList> </SendPicsInfo>
			 */
			Node node = XMLParser.getElementByTag(xml, "SendPicsInfo");
			if( node != null )
			{
				BasicDBList picList = new BasicDBList();
				node = XMLParser.getElementByTag(node, "PicMd5Sum");
				while( node != null )
				{
					picList.add(XMLParser.getElementValue(node));
					node = XMLParser.nextSibling(node);
				}
				e.put("SendPicsInfo", picList);
			}
			responsed = this.handleForEventPicSysphoto(request, response, xml);
		}
		else if("scancode_waitmsg".equalsIgnoreCase(event))
		{
			/**
			 * <ScanCodeInfo><ScanType><![CDATA[qrcode]]></ScanType> <ScanResult><![CDATA[http://lvmonogram.jp/store]]></ScanResult> </ScanCodeInfo>
			 */
			Node node = XMLParser.getElementByTag(xml, "ScanCodeInfo");
			if( node != null )
			{
				BasicDBObject scanCodeInfo = new BasicDBObject();
				scanCodeInfo.put("ScanType", this.getXmlValue(node, "ScanType"));
				scanCodeInfo.put("ScanResult", this.getXmlValue(node, "ScanResult"));
				e.put("ScanCodeInfo", scanCodeInfo);
			}
			responsed = this.handleForEventScancodeWaitmsg(request, response, xml);
		}
		else if("scancode_push".equalsIgnoreCase(event))
		{
			/**
			 * <ScanCodeInfo><ScanType><![CDATA[qrcode]]></ScanType> <ScanResult><![CDATA[http://lvmonogram.jp/store]]></ScanResult> </ScanCodeInfo>
			 */
			Node node = XMLParser.getElementByTag(xml, "ScanCodeInfo");
			if( node != null )
			{
				BasicDBObject scanCodeInfo = new BasicDBObject();
				scanCodeInfo.put("ScanType", this.getXmlValue(node, "ScanType"));
				scanCodeInfo.put("ScanResult", this.getXmlValue(node, "ScanResult"));
				e.put("ScanCodeInfo", scanCodeInfo);
			}
			responsed = this.handleForEventScancodePush(request, response, xml);
		}
		else
		{
			log.append("\r\n\tUnknown event "+event);
			responsed = this.handleForEventUnknown(request, response, xml);
		}
		return responsed;
	}
	/**
	 * 处理用户点击资讯按钮的操作
	 * @param fromUserName
	 * @param toUserName
	 * @param response
	 * @param log
	 */
	private boolean handleForEventCustom(HttpServletRequest request, HttpServletResponse response, Node xml, StringBuffer log)
		throws Exception
	{
		if( !server.isCustom() ) return false;
		boolean responsed = false;
		String fromUserName = this.getXmlValue(xml, "FromUserName");
		String toUserName = this.getXmlValue(xml, "ToUserName"); 
		long ts = System.currentTimeMillis();
		Custom c = custom.get(fromUserName);
		if( c == null )
		{
			c = new Custom();
			c.timestamp = ts;
			c.count = 0;
			custom.put(fromUserName, c);
		}
		else if( ts - c.timestamp > Tools.MILLI_OF_HOUR )
		{
			c.timestamp = ts;
			c.count = 0;
		}

		if( c.count == 0 ) 
		{
			responsed = this.handleForEventCustom(request, response, xml);
		}
		else
		{
			final String[] responses = {
				"感谢您在线咨询，请发送你的问题。",
				"请激活文本输入框，发送你的问题。",
				"谢谢，点击左下角图标打开文本输入框，发送你的问题。",
				"……",
				"今天天气不错！",
				"您吃饭了吗？",
				"好吧，您赢了！",
				"别按了，如果您再按我就不理你了!",
			};
			if( c.count < responses.length )
			{
				String respXML = "<xml>" +
						"<ToUserName><![CDATA["+fromUserName+"]]></ToUserName>" +
						"<FromUserName><![CDATA["+toUserName+"]]></FromUserName>" +
						"<CreateTime>"+(ts / 1000)+"</CreateTime>" +
						"<MsgType><![CDATA[text]]></MsgType>" +
						"<Content><![CDATA["+responses[c.count]+"]]></Content>" +
						"</xml>";
				response.getOutputStream().write(respXML.getBytes("UTF-8"));
				responsed = true;
			}
//			else
//			{
//				String resp = null;//makeSearchResp("理财投资互联网金融", fromUserName, toUserName);
//				if(resp != null)
//				{
//					response.getOutputStream().write(resp.getBytes("UTF-8"));
//					log.append("\r\n==Response the result of search.");
//				}
//				else
//				{
//					// 转多客服
//					String respXML = "<xml>" +
//					"<ToUserName><![CDATA["+fromUserName+"]]></ToUserName>" +
//					"<FromUserName><![CDATA["+toUserName+"]]></FromUserName>" +
//					"<CreateTime>"+(ts / 1000)+"</CreateTime>" +
//					"<MsgType><![CDATA[transfer_customer_service]]></MsgType>" +
//					"</xml>";
//					response.getOutputStream().write(respXML.getBytes("UTF-8"));
//					log.append("\r\n==Redirect to transfer_customer_service.");
//				}
//			}
		}
		c.count += 1;
		log.append("\r\n\tResponse custom(ts="+c.timestamp+",count="+c.count+", size="+this.custom.size()+").");
		return responsed;
	}
	/**
	 * 响应客户请求因为用户点击的资讯
	 * @param xml 微信输入的数据
	 * @return 微信服务器在五秒内收不到响应会断掉连接，并且重新发起请求，总共重试三次。
	 * 	如果抽象方法实现消息回复返回true，否则返回false，由上层框架负责回复空串，微信服务器不会对此作任何处理，并且不会发起重试。
	 */
	public abstract boolean handleForEventCustom(HttpServletRequest request, HttpServletResponse response, Node xml) throws Exception;

	/**
	 * 处理点击事件抽象方法（由继承类实现）
	 * @param request http请求消息
	 * @param response http响应消息（由事件继承实现)
	 * @param xml 微信传过来的xml节点对象 org.w3c.dom.Node
	 * @return 微信服务器在五秒内收不到响应会断掉连接，并且重新发起请求，总共重试三次。
	 * 	如果抽象方法实现消息回复返回true，否则返回false，由上层框架负责回复空串，微信服务器不会对此作任何处理，并且不会发起重试。
	 */
	public abstract boolean handleForEventClick(HttpServletRequest request, HttpServletResponse response, Node xml) throws Exception;

	/**
	 * 处理打开网页的事件抽象方法（由继承类实现）
	 * @param request http请求消息
	 * @param response http响应消息（由事件继承实现)
	 * @param xml 微信传过来的xml节点对象 org.w3c.dom.Node
	 * @return 微信服务器在五秒内收不到响应会断掉连接，并且重新发起请求，总共重试三次。
	 * 	如果抽象方法实现消息回复返回true，否则返回false，由上层框架负责回复空串，微信服务器不会对此作任何处理，并且不会发起重试。
	 */
	public abstract boolean handleForEventView(HttpServletRequest request, HttpServletResponse response, Node xml) throws Exception;

	/**
	 * 处理其它事件的抽象方法（由继承类实现）
	 * @param request http请求消息
	 * @param response http响应消息（由事件继承实现)
	 * @param xml 微信传过来的xml节点对象 org.w3c.dom.Node
	 * @return 微信服务器在五秒内收不到响应会断掉连接，并且重新发起请求，总共重试三次。
	 * 	如果抽象方法实现消息回复返回true，否则返回false，由上层框架负责回复空串，微信服务器不会对此作任何处理，并且不会发起重试。
	 */
	public abstract boolean handleForEventOther(HttpServletRequest request, HttpServletResponse response, Node xml) throws Exception;
	/**
	 * 处理系统拍照事件的抽象方法（由继承类实现）
	 * @param request http请求消息 <SendPicsInfo><Count>1</Count> <PicList><item><PicMd5Sum><![CDATA[8c16299e9eddfebad1c0ac4913bfb728]]></PicMd5Sum> </item> </PicList> </SendPicsInfo>
	 * @param response http响应消息（由事件继承实现)
	 * @param xml 微信传过来的xml节点对象 org.w3c.dom.Node
	 * @return 微信服务器在五秒内收不到响应会断掉连接，并且重新发起请求，总共重试三次。
	 * 	如果抽象方法实现消息回复返回true，否则返回false，由上层框架负责回复空串，微信服务器不会对此作任何处理，并且不会发起重试。
	 */
	public abstract boolean handleForEventPicSysphoto(HttpServletRequest request, HttpServletResponse response, Node xml) throws Exception;
	/**
	 * 处理扫码带提示事件的抽象方法（由继承类实现）
	 * @param request http请求消息 <SendPicsInfo><Count>1</Count> <PicList><item><PicMd5Sum><![CDATA[8c16299e9eddfebad1c0ac4913bfb728]]></PicMd5Sum> </item> </PicList> </SendPicsInfo>
	 * @param response http响应消息（由事件继承实现)
	 * @param xml 微信传过来的xml节点对象 org.w3c.dom.Node
	 * @return 微信服务器在五秒内收不到响应会断掉连接，并且重新发起请求，总共重试三次。
	 * 	如果抽象方法实现消息回复返回true，否则返回false，由上层框架负责回复空串，微信服务器不会对此作任何处理，并且不会发起重试。
	 */
	public abstract boolean handleForEventScancodeWaitmsg(HttpServletRequest request, HttpServletResponse response, Node xml) throws Exception;
	/**
	 * 处理扫码推事件的抽象方法（由继承类实现）
	 * @param request http请求消息 <SendPicsInfo><Count>1</Count> <PicList><item><PicMd5Sum><![CDATA[8c16299e9eddfebad1c0ac4913bfb728]]></PicMd5Sum> </item> </PicList> </SendPicsInfo>
	 * @param response http响应消息（由事件继承实现)
	 * @param xml 微信传过来的xml节点对象 org.w3c.dom.Node
	 * @return 微信服务器在五秒内收不到响应会断掉连接，并且重新发起请求，总共重试三次。
	 * 	如果抽象方法实现消息回复返回true，否则返回false，由上层框架负责回复空串，微信服务器不会对此作任何处理，并且不会发起重试。
	 */
	public abstract boolean handleForEventScancodePush(HttpServletRequest request, HttpServletResponse response, Node xml) throws Exception;
	/**
	 * 处理位置事件的抽象方法（由继承类实现）
	 * @param request http请求消息
	 * @param response http响应消息（由事件继承实现)
	 * @param xml 微信传过来的xml节点对象 org.w3c.dom.Node
	 * @return 微信服务器在五秒内收不到响应会断掉连接，并且重新发起请求，总共重试三次。
	 * 	如果抽象方法实现消息回复返回true，否则返回false，由上层框架负责回复空串，微信服务器不会对此作任何处理，并且不会发起重试。
	 */
	public abstract boolean handleForEventLocation(HttpServletRequest request, HttpServletResponse response, Node xml) throws Exception;
	/**
	 * 处理用户主动发送位置事件（精度更好）的抽象方法（由继承类实现）
	 * @param request http请求消息 <SendLocationInfo><Location_X><![CDATA[39.96284480333743]]></Location_X> <Location_Y><![CDATA[116.3065396415107]]></Location_Y> <Scale><![CDATA[15]]></Scale> <Label><![CDATA[北京市海淀区苏州桥金洲大厦内(长春桥路北)]]></Label> <Poiname><![CDATA[海淀区苏州桥金洲大厦内(长春桥路北)]]></Poiname> </SendLocationInfo>
	 * @param response http响应消息（由事件继承实现)
	 * @param xml 微信传过来的xml节点对象 org.w3c.dom.Node
	 * @return 微信服务器在五秒内收不到响应会断掉连接，并且重新发起请求，总共重试三次。
	 * 	如果抽象方法实现消息回复返回true，否则返回false，由上层框架负责回复空串，微信服务器不会对此作任何处理，并且不会发起重试。
	 */
	public abstract boolean handleForEventLocationSelect(HttpServletRequest request, HttpServletResponse response, Node xml) throws Exception;
	/**
	 * 处理用户绑定公众号事件的抽象方法（由继承类实现）
	 * @param request http请求消息
	 * @param response http响应消息（由事件继承实现)
	 * @param xml 微信传过来的xml节点对象 org.w3c.dom.Node
	 * @param json 用户数据
	 * {
		subscribe	用户是否订阅该公众号标识，值为0时，代表此用户没有关注该公众号，拉取不到其余信息。
		openid	用户的标识，对当前公众号唯一
		nickname	用户的昵称
		sex	用户的性别，值为1时是男性，值为2时是女性，值为0时是未知
		city	用户所在城市
		country	用户所在国家
		province	用户所在省份
		language	用户的语言，简体中文为zh_CN
		headimgurl	用户头像，最后一个数值代表正方形头像大小（有0、46、64、96、132数值可选，0代表640*640正方形头像），用户没有头像时该项为空。若用户更换头像，原有头像URL将失效。
		subscribe_time	用户关注时间，为时间戳。如果用户曾多次关注，则取最后关注时间
		unionid	只有在用户将公众号绑定到微信开放平台帐号后，才会出现该字段。
		remark	公众号运营者对粉丝的备注，公众号运营者可在微信公众平台用户管理界面对粉丝添加备注
		groupid	用户所在的分组ID（兼容旧的用户分组接口）
		tagid_list	用户被打上的标签ID列表
	 * }
	 * @return 微信服务器在五秒内收不到响应会断掉连接，并且重新发起请求，总共重试三次。
	 * 	如果抽象方法实现消息回复返回true，否则返回false，由上层框架负责回复空串，微信服务器不会对此作任何处理，并且不会发起重试。
	 */
	public abstract boolean handleForEventSubscribe(HttpServletRequest request, HttpServletResponse response, Node xml, String json) throws Exception;
	/**
	 * 处理事件的抽象方法（由继承类实现）
	 * @param request http请求消息
	 * @param response http响应消息（由事件继承实现)
	 * @param xml 微信传过来的xml节点对象 org.w3c.dom.Node
	 * @return 微信服务器在五秒内收不到响应会断掉连接，并且重新发起请求，总共重试三次。
	 * 	如果抽象方法实现消息回复返回true，否则返回false，由上层框架负责回复空串，微信服务器不会对此作任何处理，并且不会发起重试。
	 */
	public abstract boolean handleForEventUnsubscribe(HttpServletRequest request, HttpServletResponse response, Node xml) throws Exception;
	/**
	 * 处理事件的抽象方法（由继承类实现）
	 * @param request http请求消息
	 * @param response http响应消息（由事件继承实现)
	 * @param xml 微信传过来的xml节点对象 org.w3c.dom.Node
	 * @return 微信服务器在五秒内收不到响应会断掉连接，并且重新发起请求，总共重试三次。
	 * 	如果抽象方法实现消息回复返回true，否则返回false，由上层框架负责回复空串，微信服务器不会对此作任何处理，并且不会发起重试。
	 */
	public abstract boolean handleForEventUnknown(HttpServletRequest request, HttpServletResponse response, Node xml) throws Exception;
	/**
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String signature = request.getParameter("signature");
		String timestamp = request.getParameter("timestamp");
		String nonce = request.getParameter("nonce");
		String echostr = request.getParameter("echostr");
		StringBuffer log = new StringBuffer("doGet from "+request.getRemoteAddr());
		log.append("\r\n\t"+request.getRequestURL()+" "+request.getContextPath());
		log.append("\r\n\tsignature="+signature);
		log.append("\r\n\ttimestamp="+timestamp);
		log.append("\r\n\tnonce="+nonce);
		log.append("\r\n\techostr="+echostr);
        Enumeration<String> e1 = request.getHeaderNames();
		log.append("\r\n\t----------------");
        while(e1.hasMoreElements())
        {
        	String key = e1.nextElement();
        	log.append("\r\n\t");
        	log.append(key);
        	log.append("=");
        	log.append(request.getHeader(key));
        }
        e1 = request.getParameterNames();
		log.append("\r\n\t----------------");
        while (e1.hasMoreElements()) {
            String key = e1.nextElement();
            log.append("\r\n\t");
            log.append(key);
            log.append("=");
            log.append(request.getParameter(key));
        }
		if( signature == null || timestamp == null || nonce == null )
		{
			log.append("\r\n####Unknown request.");
        	response.getWriter().println("Unknown request.");
        	Log.err(log.toString());
        	return;
		}
		try
		{
	        Object[] tmpArr = new Object[]{server.getToken(), timestamp, nonce};
	        Arrays.sort(tmpArr);
	        String signature1 = DigestUtils.shaHex(tmpArr[0].toString()+tmpArr[1].toString()+tmpArr[2].toString());
	        if( signature.equals(signature1) )
	        {
				log.append("\r\n####Succeed to auth.");
	        	response.getWriter().println(echostr);
	        	Log.msg(log.toString());
	        }
	        else
	        {
				log.append("\r\n####Failed to auth.");
	        	response.getWriter().println("false");
	        	Log.err(log.toString());
	        }
		}
		catch(Exception e)
		{
			log.append("\r\n####Failed to doGet for exception");
        	Log.err(log.toString(), e);
		}
	}

	/**
	 * 读取输入流为二进制数组
	 * @param servletInputStream
	 * @return
	 */
	public static byte[] readFullInputStream(InputStream servletInputStream)
	{
		List<byte[]> all = new ArrayList<byte[]>();
		int read = -1;
		int count = 0;
		byte[] catchs = new byte[1024];
		try {
			while((read = servletInputStream.read(catchs))>-1){
				count += read;
				byte[] data = new byte[read];
				System.arraycopy(catchs,0, data, 0, read);
				all.add(data);
			}
		} catch (IOException e) {
			Log.err(e);
		}
		
		byte[] allDatas = new byte[count];
		int copyCounts = 0;
		for(byte[] b : all){
			System.arraycopy(b, 0, allDatas, copyCounts, b.length);
			copyCounts += b.length;
		}
		
		return allDatas;
	}
	
	/**
	 * 根据微信回调XML节点对象以及数据标签查询数据值
	 * @param xml
	 * @param tag
	 * @return 查询结果值
	 */
	public String getXmlValue(Node xml, String tag)
	{
		Node node = XMLParser.getElementByTag(xml, tag);
		if( node == null ) return "";
		String value = node.getTextContent();
		if( value != null ) value = value.trim();
		return value;
//		Node child = node.getFirstChild();
//		if( child == null ) return XMLParser.getElementValue(node);
//		return child.getNodeValue();
	}
	/**
	 * 
	 * @param xml
	 * @param tag
	 * @return
	 */
	public BasicDBObject getXmlObject(Node xml, String tag)
	{
		BasicDBObject object = new BasicDBObject();
		Node node = XMLParser.getElementByTag(xml, tag);
		if( node == null ) return object;
		/**
		 * <SendPicsInfo><Count>1</Count> <PicList><item><PicMd5Sum><![CDATA[cfdc714e447c95e37e3d44d5cd32d5f6]]></PicMd5Sum> </item> </PicList> </SendPicsInfo>
		 */
		Node child = XMLParser.getFirstChildElement(node);
		while( child != null )
		{
			object.put(child.getNodeName(), child.getNodeValue());
		}
		return object;
	}
    /**
     * 得到指定request的真实请求IP
     * @param request
     * @return
     */
    public static String getIp(HttpServletRequest request)
    {
        String ip = request.getRemoteAddr();
        String ip_ = request.getHeader("x-forwarded-for");
        ip_ = ip_ == null ? request.getHeader("x-real-ip") : ip_;
        if (ip_ != null)  ip = ip_;
        return ip;
    }
}
