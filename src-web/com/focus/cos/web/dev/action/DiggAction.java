package com.focus.cos.web.dev.action;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jxl.CellView;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import redis.clients.jedis.Jedis;

import com.focus.cos.api.ApiUtils;
import com.focus.cos.api.LogSeverity;
import com.focus.cos.digg.Cache;
import com.focus.cos.digg.OLTP;
import com.focus.cos.web.action.DataModel;
import com.focus.cos.web.action.TemplateChecker;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.dev.service.DiggMgr;
import com.focus.cos.web.ops.service.SecurityMgr;
import com.focus.cos.web.service.SvrMgr;
import com.focus.cos.web.util.RsaKeyTools;
import com.focus.util.Base64X;
import com.focus.util.HttpUtils;
import com.focus.util.Tools;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.util.JSON;

/**
 * 通用数据查询模块
 * @author focus
 * @version v1.0.0.0 2017-4-8
 */
public class DiggAction extends DevAction 
{
	private static final long serialVersionUID = 1558752746646473382L;
	private static final Log log = LogFactory.getLog(DiggAction.class);
	private DiggMgr diggMgr;
	/*安全管理器*/
	private SecurityMgr securityMgr;
	/**/
	protected DiggChecker diggChecker;
//	private Cache cache = null;
	protected JSONArray diggs = null;
	protected HashMap<String, String> dataColumnsUpper = new HashMap<String, String>();//規避SQL返回是大寫的問題
	protected String referer = null;
	protected String filename;
	protected long totalRecords;
	protected int curPage;
	protected int pageSize;
	/*签名，用私钥进行验证*/
	private String signature;
	/*随机数*/
	private String nonce;
	/*时间戳*/
	private String ts;
	/**
	 * 做安全校验
	 * @param token 安全配置存现
	 * @return
	 */
	private boolean oauth(JSONObject token)
	{
	    String ip = super.getRequest().getRemoteAddr();
	    String referer = super.getRequest().getHeader("referer");
	    logInfo("oauth(%s\r\n\ttimestamp=%s\r\n\tnonce=%s\r\n\tsignature=%s\r\n) from %s / %s",
	    		this.account, this.ts, this.nonce, this.signature, referer, ip);
	    if (this.account == null) {
	    	super.setResponseException("没有安全令牌账户禁止访问GRID-DIGG服务。");
	    	return false;
	    }
	    if ((this.nonce == null) || (this.ts == null) || (this.signature == null)) {
	    	super.setResponseException("接口调用请求没有签名禁止访问GRID-DIGG服务。");
	    	return false;
	    }
	    if (Tools.isNumeric(this.ts)) {
	    	this.timestamp = Long.parseLong(this.ts);
	    }
	    else {
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    	try {
	    		this.timestamp = sdf.parse(this.ts).getTime();
	    	} catch (ParseException e) {
	    		super.setResponseException("服务请求的签名时间戳【" + this.ts + "】格式不正确，禁止访问GRID-DIGG服务。");
	    		return false;
	    	}
	    }
	    if (System.currentTimeMillis() - 86400000L > this.timestamp) {
	    	super.setResponseException("服务请求的签名时间戳过期，禁止访问GRID-DIGG服务。");
	    	return false;
	    }
	    if (this.securityMgr.checkNonce(this.nonce)) {
	    	super.setResponseException("服务请求的签名随机数【" + this.nonce + "】已经使用过，禁止访问GRID-DIGG服务。");
	    	return false;
	    }
	    if (!token.has("token")) {
	    	super.setResponseException("安全令牌账户【" + this.account + "】没有配置令牌，禁止通过该令牌访问GRID-DIGG服务。");
	    	return false;
	    }
	    try
	    {
	    	byte[] publicKey = Base64X.decode(token.getString("publickey"));
	    	if (publicKey != null)
	    	{
	    		signature = signature.replaceAll("\r", "");
	    		signature = signature.replaceAll("\n", "");
	    		if (!RsaKeyTools.verify(token.getString("token") + this.ts + this.nonce, publicKey, RsaKeyTools.hexStringToByteArray(this.signature))) {
	    			super.setResponseException("安全令牌账户【" + this.account + "】数字签名【" + this.signature + "】无效(时间戳:" + 
	    					this.timestamp + ",随机数:" + this.nonce + ")，禁止通过该令牌访问GRID-DIGG服务。");
	    			return false;
	    		}
	    	}
	    	else {
	    		log.error("Failed to confirm the signature by token: " + token.toString(4));
	    		super.setResponseException("安全令牌账户【" + this.account + "】签名公钥不存在，禁止通过该令牌访问GRID-DIGG服务。");
	    		return false;
	    	}
	    }
	    catch (Exception e)
	    {
	    	log.error("Failed to confirm the signature by token: " + token.toString(4), e);
	    	super.setResponseException("安全令牌账户【" + this.account + "】对签名进行验签发生异常[" + e + "]，禁止通过该令牌访问GRID-DIGG服务。");
	    	return false;
	    }
	    
	    if (!token.has("admin")) {
	    	super.setResponseException("安全令牌账户【" + this.account + "】没有配置管理用户，禁止通过该令牌访问GRID-DIGG服务。");
	    	return false;
	    }
	    
	    if (token.has("host")) {
		    String host = token.getString("host");
		    if( referer != null && !host.isEmpty() && referer.indexOf(host) == -1) {
		    	super.setResponseException("安全令牌账户【" + this.account + "】禁止您通过【" + referer + "】访问GRID-DIGG服务。");
		    	return false;
		    }
	    }
	    
	    if (token.has("ip") ) {
	    	String _ip = token.getString("ip");
	    	if( _ip != null && !_ip.isEmpty() ){
			    if ("0:0:0:0:0:0:0:1".equals(ip)) {
			    	ip = "127.0.0.1";
			    }
			    if (!token.getString("ip").equals(ip)) {
			    	super.setResponseException("安全令牌账户【" + this.account + "】禁止您通过该地址【" + ip + "】访问GRID-DIGG服务。");
			    	return false;
			    }
	    	}
	    }
	    return true;
	}
	/**
	 * 得到请求的
	 * @return
	 */
	private String getRequestInfo(){
		HttpServletRequest request = super.getRequest();
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("Receive the request of griddigg-export from %s@%s:%s, the headers and parameters is below ", 
			request.getRemoteUser(), request.getRemoteAddr(), request.getRemotePort()));
		Enumeration<String> names = request.getHeaderNames();
		while( names.hasMoreElements() )
		{
			String key = names.nextElement();
			String value = request.getHeader(key);
			if( "connection".equalsIgnoreCase(key) )
				value = "keep-alive";
			sb.append("\r\n\t\t"+key+": "+value);
		}
		Iterator<Map.Entry<String, String[]>> iterator = request.getParameterMap().entrySet().iterator();
		while(iterator.hasNext())
		{
			Map.Entry<String, String[]> e = iterator.next();
			sb.append("\r\n\t");
			sb.append(e.getKey());
			sb.append("=");
			for(String value : e.getValue())
				sb.append(value+"\t");
		}
		return sb.toString();
	}
	/**
	 * API导出调测
	 * @return
	 */
	public String apiexportdebug()
	{
		InputStream is = null;
		try{
			this.gridxml = gridxml==null?super.getRequest().getParameter("gridxmlid"):gridxml;
			workmode = TemplateChecker.WORK;
			is = this.getGridXmlInputStream();
    		String filetype = this.filetype;
			super.parse(is);
    		this.filetype = filetype;
			return export();
		}
		catch(Exception e){
			log.error("Failed to debug the export of api.", e);
			return null;
		}
	}
	/**
	 * 通过API导出数据
	 * @return
	 */
	public String apiexport(){
		thisisapi = true;
		InputStream is = null;
    	JSONObject dataJSON = new JSONObject();
		try
		{
			HttpServletRequest request = super.getRequest();
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("Receive the request of export from %s@%s:%s, the headers and parameters is below ", 
				request.getRemoteUser(), request.getRemoteAddr(), request.getRemotePort()));
    		Enumeration<String> names = request.getHeaderNames();
    		while( names.hasMoreElements() )
    		{
    			String key = names.nextElement();
    			String value = request.getHeader(key);
    			if( "connection".equalsIgnoreCase(key) )
    				value = "keep-alive";
    			sb.append("\r\n\t\t"+key+": "+value);
    		}
    		Iterator<Map.Entry<String, String[]>> iterator = request.getParameterMap().entrySet().iterator();
    		while(iterator.hasNext())
    		{
    			Map.Entry<String, String[]> e = iterator.next();
    			sb.append("\r\n\t");
    			sb.append(e.getKey());
    			sb.append("=");
    			for(String value : e.getValue())
    				sb.append(value+"\t");
    		}
			logInfo(sb.toString());
			System.err.println(sb.toString());
    		String zkpath = this.gridxml+"/api.json";
    		if( gridxml.startsWith("/grid/local") )
			{
				zkpath = "/cos/config"+this.gridxml+"/api.json";
			}
    		JSONObject api = ZKMgr.getZookeeper().getJSONObject(zkpath);
			int status = 0;
			message = "";
		    if( api == null ) {
		    	status = 404;
		    	message = "模板["+gridxml+"]的数据接口未配置，禁止访问GRID-DIGG服务";
			}
		    else if( api.getInt("status") == 0 ) {
		    	status = 402;
		    	message = "["+status+"] 模板["+gridxml+"]的数据接口未激活开通，禁止访问GRID-DIGG服务";
		    }
		    else if( api.getInt("status") == 2 ) {
		    	status = 402;
		    	message = "["+status+"] 模板["+gridxml+"]的数据接口已被禁用，禁止访问GRID-DIGG服务";
		    }
		    else if( api.has("security") ) {
		    	this.account = api.getString("security");
		    	if( !account.isEmpty() ){
		    		JSONObject token = ZKMgr.getZookeeper().getJSONObject("/cos/config/security/" + account);
		    		if (token == null) {
				    	status = 404;
		    			message = "["+status+"] 模板["+gridxml+"]的数据接口安全调用配置不存在，禁止访问GRID-DIGG服务";
		    		}
		    		else if( !this.oauth(token) ){
				    	status = 401;
		    			message = "["+status+"] "+this.responseException;
		    		}
		    	}
		    }
		    if( account == null || account.isEmpty() ){
		    	account = "匿名用户";
		    }
		    if( !message.isEmpty() ){
		    	String context = message;
		    	context += getRequestInfo();
		    	context += ("\r\n\r\n安全令牌: "+(api!=null?api.toString(4):""));
//		    	super.logsecurity(LogSeverity.ERROR, this.account, message, "接口安全", context, "diggcfg!datastyle.action?gridxml="+gridxml);
				dataJSON.put("hasException", true);
				dataJSON.put("message", message);
	        	super.logoper(LogSeverity.ERROR, String.format("调用DIGG模板[%s]的数据导出接口因为'%s'失败", new Object[] {
		        		this.gridxml, message}), 
		        		"DIGG-API", context, "diggcfg!version.action?id=" + this.gridxml);
	        	this.logError("Failed to response data from api for '%s'", message);
	        	securityMgr.writeLog(trans.append("\r\n").toString());
				return super.response(super.getResponse(), dataJSON.toString());
		    }
        	securityMgr.writeLog(trans.append("\r\n").toString());
        	this.gridxml = gridxml==null?super.getRequest().getParameter("gridxmlid"):gridxml;
			workmode = TemplateChecker.WORK;
			is = this.getGridXmlInputStream();
    		String filetype = this.filetype;
			super.parse(is);
			this.filetype = filetype;
		}
		catch (Exception e)
		{
			log.error("Failed to export", e);
			dataJSON.put("hasException", true);
			String c1 = e.toString();
			if( c1 != null ){
				c1 = c1.replaceAll("\"", "");
				c1 = c1.replaceAll("'", "");
			}
			dataJSON.put("message", "导出元数据模板["+gridxml+"]的数据出现异常"+c1);
			return super.response(super.getResponse(), dataJSON.toString());
		}
		return export();
	}
	/**
	 * 使用接口查询
	 * @return
	 */
	public String api(){
		thisisapi = true;
		InputStream is = null;
    	JSONObject dataJSON = new JSONObject();
    	long ts = System.currentTimeMillis();
		try
		{
			HttpServletRequest request = super.getRequest();
			StringBuffer sb = new StringBuffer();
			sb.append(String.format("Receive the request of griddigg from %s@%s:%s, the headers and parameters is below ", 
				request.getRemoteUser(), request.getRemoteAddr(), request.getRemotePort()));
    		Enumeration<String> names = request.getHeaderNames();
    		while( names.hasMoreElements() )
    		{
    			String key = names.nextElement();
    			String value = request.getHeader(key);
    			if( "connection".equalsIgnoreCase(key) )
    				value = "keep-alive";
    			sb.append("\r\n\t\t"+key+": "+value);
    		}
    		Iterator<Map.Entry<String, String[]>> iterator = request.getParameterMap().entrySet().iterator();
    		while(iterator.hasNext())
    		{
    			Map.Entry<String, String[]> e = iterator.next();
    			sb.append("\r\n\t");
    			sb.append(e.getKey());
    			sb.append("=");
    			for(String value : e.getValue())
    				sb.append(value+"\t");
    		}
			logInfo(sb.toString());
    		String zkpath = this.gridxml+"/api.json";
    		if( gridxml.startsWith("/grid/local") )
			{
				zkpath = "/cos/config"+this.gridxml+"/api.json";
			}
    		JSONObject api = ZKMgr.getZookeeper().getJSONObject(zkpath);
			int status = 0;
			message = "";
		    if( api == null ) {
		    	status = 404;
		    	message = "模板["+gridxml+"]的数据接口未配置，禁止访问GRID-DIGG服务";
			}
		    else if( api.getInt("status") == 0 ) {
		    	status = 402;
		    	message = "["+status+"] 模板["+gridxml+"]的数据接口未激活开通，禁止访问GRID-DIGG服务";
		    }
		    else if( api.getInt("status") == 2 ) {
		    	status = 402;
		    	message = "["+status+"] 模板["+gridxml+"]的数据接口已被禁用，禁止访问GRID-DIGG服务";
		    }
		    else if( api.has("security") ) {
		    	this.account = api.getString("security");
		    	if( !account.isEmpty() ){
		    		JSONObject token = ZKMgr.getZookeeper().getJSONObject("/cos/config/security/" + account);
		    		if (token == null) {
				    	status = 404;
		    			message = "["+status+"] 模板["+gridxml+"]的数据接口安全调用配置不存在，禁止访问GRID-DIGG服务";
		    		}
		    		else if( !this.oauth(token) ){
				    	status = 401;
		    			message = "["+status+"] "+this.responseException;
		    		}
		    	}
		    }
		    if( account == null || account.isEmpty() ){
		    	account = "匿名用户";
		    }
		    if( !message.isEmpty() ){
		    	String context = message;
		    	context += sb.toString();
		    	context += ("\r\n\r\n安全令牌: "+(api!=null?api.toString(4):""));
//		    	super.logsecurity(LogSeverity.ERROR, this.account, message, "接口安全", context, "diggcfg!datastyle.action?gridxml="+gridxml);
		    	super.getResponse().setStatus(200);
				dataJSON.put("hasException", true);
				dataJSON.put("message", message);
	        	super.logoper(LogSeverity.ERROR, String.format("调用DIGG模板[%s]的数据接口因为'%s'失败", new Object[] {
		        		this.gridxml, message}), 
		        		"DIGG-API", context, "diggcfg!version.action?id=" + this.gridxml);
	        	this.logError("Failed to response data from api for '%s'", message);
	        	securityMgr.writeLog(trans.append("\r\n").toString());
				return super.response(super.getResponse(), dataJSON.toString());
		    }
		    diggChecker = new DiggChecker(true);
			this.setDiggProgress(request);
			this.gridxml = gridxml==null?request.getParameter("gridxmlid"):gridxml;
			workmode = TemplateChecker.WORK;
			is = this.getGridXmlInputStream();
			super.parse(is);
			
    		diggChecker.print(-1, "%s", sb.toString());
    		this.setDiggConfig(request);
			
    		if( diggObject != null ) {
    			curPage = Integer.parseInt(Tools.getValidStr(super.getRequest().getParameter("pq_curpage"), "1"));
    			curPage = curPage<=0?1:curPage;
    			pageSize  = Integer.parseInt(Tools.getValidStr(super.getRequest().getParameter("pq_rpp"), "0"));
    			diggChecker.print(1, "当前页码:%s", curPage);
    			diggChecker.print(1, "分页规格:%s", pageSize);
    			if( pageSize == 0 )
    			{
    				if( diggObject.has("pagesize") )
    				{
    					pageSize = diggObject.getInt("pagesize");
    				}
    				else
    				{
    					pageSize = 50;
    				}
    			}
    			this.digg(dataJSON);
    			if( super.filterModel != null && super.filterModel.containsField("labels") ){
    				BasicDBObject labels = (BasicDBObject)super.filterModel.get("labels");
    				dataJSON.put("label", new JSONObject(labels.toString()));
    			}
    		}
    		else {
    			diggChecker.print(super.checker);
    	    	totalRecords = 1;
    			dataJSON.put("pageSize", 1);
    			dataJSON.put("curPageRecords", 1);
    			dataJSON.put("totalRecords", 1);
    			dataJSON.put("curPage", 1);
    			dataJSON.put("data", this.localDataArray);
    		}
//			System.err.println(dataJSON.toString(4));
        	long duration = System.currentTimeMillis() - ts;
    		String endtips = String.format("调用元数据模板[%s]数据接口成功，耗时%s毫秒，数据总记录一共%s条",gridxml, duration, totalRecords);
    		int count = dataJSON.getInt("curPageRecords");
    		this.diggChecker.print(0, "调用元数据模板[%s]数据接口成功，耗时%s毫秒，数据总记录一共%s条",gridxml, duration, totalRecords);
        	super.logoper(String.format("调用DIGG模板【%s, 版本v%s】的数据接口，获取第%s页(%s)数据成功", new Object[] {
        		this.gridtitle, this.gridversion, curPage, count }), 
        		"DIGG-API", endtips+sb.toString(), "diggcfg!version.action?id=" + this.gridxml);
        	if( count > 0 ){
        		//有记录的计算响应延时
        		this.diggMgr.setDiggApiSpeed(gridxml, duration);
        	}
        	this.logInfo("Succeed to response data: %s", dataJSON.toString(4));
        	securityMgr.writeLog(trans.append("\r\n").toString());
		}
		catch (Exception e)
		{
			this.diggChecker.print(0, "调用元数据模板[%s]的数据接口出现异常: %s", gridxml, e.toString());
			this.diggChecker.write(e);
			dataJSON.put("hasException", true);
			String c1 = e.getMessage();
			if( c1 != null ){
				c1 = c1.replaceAll("\"", "");
				c1 = c1.replaceAll("'", "");
			}
			dataJSON.put("message", "调用元数据模板["+gridxml+"]的数据接口出现异常"+c1);
    		SvrMgr.sendNotiefiesToSystemadmin(
				super.getRequest(),
				"接口管理",
				"调用元数据模板["+gridxml+"]的数据接口出现异常",
				"[<i class='fa fa-digg fa-fw'></i>] 异常描述"+e.getMessage()+"，堆栈如下所示：\r\n"+this.diggChecker.toString(),
				"", "", ""
            );
		    if( account == null || account.isEmpty() ){
		    	account = "匿名用户";
		    }
        	super.logoper(LogSeverity.ERROR, String.format("调用DIGG模板[%s]的数据接口，获取第%s页(%s)数据出现异常%s", new Object[] {
        		this.gridxml, curPage,  pageSize, e.getMessage()}), 
        		"DIGG-API", diggChecker.toString(), "diggcfg!version.action?id=" + this.gridxml);
        	this.logError(e, "Failed to response data from api.");
        	securityMgr.writeLog(trans.append("\r\n").toString());
		}
		return super.response(super.getResponse(), dataJSON.toString());
	}
	/**
	 * 接口调测
	 * @return
	 */
	public String apidebug()
	{
		thisisapi = true;
		InputStream is = null;
    	JSONObject dataJSON = new JSONObject();
    	long ts = System.currentTimeMillis();
		try
		{
			diggChecker = new DiggChecker(true);
			HttpServletRequest request = super.getRequest();
			this.setDiggProgress(request);
			this.gridxml = gridxml==null?request.getParameter("gridxmlid"):gridxml;
    		StringBuffer sb = new StringBuffer();
    		sb.append("Receive the request of digg, the parameters of below ");
    		HttpServletRequest req = super.getRequest();
    		Iterator<Map.Entry<String, String[]>> iterator = req.getParameterMap().entrySet().iterator();
    		while(iterator.hasNext())
    		{
    			Map.Entry<String, String[]> e = iterator.next();
    			sb.append("\r\n\t");
    			sb.append(e.getKey());
    			sb.append("=");
    			for(String value : e.getValue())
    				sb.append(value+"\t");
    		}
    		diggChecker.print(-1, "%s", sb.toString());
			String zkpath = this.gridxml+"/api.json";
			if( gridxml.startsWith("/grid/local") )
			{
				zkpath = "/cos/config"+this.gridxml+"/api.json";
			}
			JSONObject api = ZKMgr.getZookeeper().getJSONObject(zkpath);
			int status = 0;
		    if( api != null ) {
		    	diggChecker.print(0, "Found the object of api from %s", zkpath);
				message = "";
			    if( api.has("security") ) {
			    	this.account = api.getString("security");
			    	if( !account.isEmpty() ){
			    		JSONObject token = ZKMgr.getZookeeper().getJSONObject("/cos/config/security/" + account);
			    		if (token == null) {
					    	status = 404;
			    			message = "["+status+"] 模板["+gridxml+"]的数据接口安全调用配置不存在，禁止访问GRID-DIGG服务";
			    		}
			    		else if( !this.oauth(token) ){
					    	status = 401;
			    			message = "["+status+"] "+this.responseException;
			    		}
			    	}
			    }
			    if( !message.isEmpty() ){
			    	throw new Exception(message);
			    }
		    }
		    else{
		    	diggChecker.print(0, "Not found the object of api from %s", zkpath);
		    }
			is = this.getGridXmlInputStream();
	    	diggprogress.put("value", 1);
	    	diggprogress.put("message", "解析模板可能要花较长时间，请耐心等待...");
	    	if( workmode != TemplateChecker.WORK ) super.getSession().setAttribute(this.referer+".digg", diggprogress);
			super.parse(is);
    		this.setDiggConfig(request);
    		if( diggObject != null ) {
    			curPage = Integer.parseInt(Tools.getValidStr(super.getRequest().getParameter("pq_curpage"), "1"));
    			curPage = curPage<=0?1:curPage;
    			pageSize  = Integer.parseInt(Tools.getValidStr(super.getRequest().getParameter("pq_rpp"), "0"));
    			diggChecker.print(1, "当前页码:%s", curPage);
    			diggChecker.print(1, "分页规格:%s", pageSize);
    			if( pageSize == 0 )
    			{
    				if( diggObject.has("pagesize") )
    				{
    					pageSize = diggObject.getInt("pagesize");
    				}
    				else
    				{
    					pageSize = 50;
    				}
    			}
    			this.digg(dataJSON);
    		}
    		else{
		    	diggChecker.print(super.checker);
		    	diggprogress.put("value", 100);
    			diggprogress.put("message", "结束");
    	    	if( workmode != TemplateChecker.WORK ) super.getSession().setAttribute(this.referer+".digg", diggprogress);
    	    	totalRecords = 1;
    			dataJSON.put("pageSize", 1);
    			dataJSON.put("curPageRecords", 1);
    			dataJSON.put("totalRecords", 1);
    			dataJSON.put("curPage", 1);
    			dataJSON.put("data", this.localDataArray);	
    		}
//			System.err.println(dataJSON.toString(4));
    		this.diggChecker.print(0, "解析元数据模板成功，向用户返回数据一共%s条", totalRecords);
		}
		catch (Exception e)
		{
    		HttpServletRequest req = super.getRequest();
    		StringBuffer sb = new StringBuffer();
    		Iterator<Map.Entry<String, String[]>> iterator = req.getParameterMap().entrySet().iterator();
    		while(iterator.hasNext())
    		{
    			Map.Entry<String, String[]> ee = iterator.next();
    			sb.append("\r\n\t");
    			sb.append(ee.getKey());
    			sb.append("=");
    			for(String value : ee.getValue())
    				sb.append(value+"\t");
    		}
    		dataJSON.put("parameters", sb.toString());
			this.diggChecker.print(0, "执行元数据模板DIGG-API发生异常: %s", e.toString());
			this.diggChecker.write(e);
			dataJSON.put("hasException", true);
			String c1 = e.getMessage();
			if( c1 != null ){
				c1 = c1.replaceAll("\"", "");
				c1 = c1.replaceAll("'", "");
			}
			dataJSON.put("message", "执行元数据模板["+gridxml+"]DIGG-API查询出现异常"+c1);
		}
        finally
        {
        	long duration = System.currentTimeMillis() - ts;
    		this.diggChecker.print(1, "数据解析处理从时间[%s]开始，一共耗时%s秒", Tools.getFormatTime("HH:mm:ss", ts), duration/1000);
    		this.getSession().setAttribute("digg-api.log", this.diggChecker.payload());
    		int count = dataJSON.has("")?dataJSON.getInt(""):0;
            diggprogress.put("message", String.format("DIGG数据%s，涉及数据查询与运算%s次，共耗时%s", count, totalStep, Kit.getDurationMs(duration)));
            diggprogress.put("finish", true);
            String tag = this.referer+".digg";
//            System.err.println(tag);
    		super.getSession().setAttribute(tag, diggprogress);
        }
		this.setEditorType("json");
		this.setEditorContent(dataJSON.toString(4));
		return "xml_json_css_sql";
	}

	/**
	 * 查看digg的详细日志
	 * @return
	 */
	public String apidebuglog()
	{
    	ServletOutputStream sos = null;
		byte[] payload = null;
		try 
		{
	        payload = (byte[])super.getSession().getAttribute(filetype!=null&&!filetype.isEmpty()?filetype:"digg-api.log");
	        if( payload == null ){
	        	payload = ("DIGG-API调试日志已经不存在了.").getBytes("UTF-8");
	        }
			getResponse().setContentType("text/html");
			getResponse().setCharacterEncoding("UTF-8");
			sos = getResponse().getOutputStream();
			sos.print("<html><body style='padding: 1px;font-size:9pt;background:#000;color:#fff;word-break:keep-all;white-space:pre;'>");
			sos.flush();
		}
		catch (Exception e) 
		{
			try {
				payload = ("打开元数据模板DIGG-API调试日志出现异常"+e.toString()).getBytes("UTF-8");
			} catch (UnsupportedEncodingException e1) {
			}
		}
        finally
        {
        	if( sos != null )
        	{
	        	try
	        	{
	    			sos.write(payload);
	    			sos.flush();
	        	} catch (IOException e1) {
				}
        	}
        }
		return null;
	}
	/**
	 * 开放数据接口
	 * @return
	 */
	public String opendata(){
		HttpServletRequest req = super.getRequest();
		StringBuffer sb = new StringBuffer();
		sb.append("Get the request of data for open from");
		sb.append(req.getRequestURI());
		sb.append("\r\n----------------\r\nHTTP头信息:");
		sb.append("\r\n\t"+req.getRemoteAddr()+":"+req.getRemotePort()+"\t远端用户: "+req.getRemoteUser());
		sb.append("\r\n\t"+req.getServerPort());
		sb.append("\r\n\t"+req.getServletPath());
		sb.append("\r\n\t"+req.getContextPath());
		Enumeration<String> names = req.getHeaderNames();
		while( names.hasMoreElements() )
		{
			String key = names.nextElement();
			String value = req.getHeader(key);
			if( "connection".equalsIgnoreCase(key) )
				value = "keep-alive";
			sb.append("\r\n\t\t"+key+": "+value);
		}
		sb.append("\r\nHTTP参数:");
		Iterator<Map.Entry<String, String[]>> iterator = req.getParameterMap().entrySet().iterator();
		while(iterator.hasNext())
		{
			Map.Entry<String, String[]> e = iterator.next();
			sb.append("\r\n\t");
			sb.append(e.getKey());
			sb.append("=");
			for(String value : e.getValue())
				sb.append(value+"\t");
		}
		referer = req.getHeader("referer");
		sb.append("\r\n读取DIGG配置:"+referer);
		this.diggObject = (JSONObject)super.getSession().getAttribute(referer);
		if( this.diggObject != null ){
			sb.append("\r\n\tFound the config of digg from session "+diggObject.toString(4));
		}
		else{
			sb.append("\r\n\tNot found the config of digg from session.");
		}
		log.info(sb.toString());
		return data();
	}
	/**
	 * 开放DIGG
	 * @return
	 */
	public String open()
	{
		opendigg = true;
		HttpServletRequest req = super.getRequest();
		StringBuffer sb = new StringBuffer();
		sb.append("Get the request of digg for open from ");
		sb.append(req.getRequestURI());
		sb.append("\r\n----------------\r\nHTTP头信息:");
		sb.append("\r\n\t"+req.getRemoteAddr()+":"+req.getRemotePort()+"\t远端用户: "+req.getRemoteUser());
		sb.append("\r\n\t"+req.getServerPort());
		sb.append("\r\n\t"+req.getServletPath());
		sb.append("\r\n\t"+req.getContextPath());
		Enumeration<String> names = req.getHeaderNames();
		while( names.hasMoreElements() )
		{
			String key = names.nextElement();
			String value = req.getHeader(key);
			if( "connection".equalsIgnoreCase(key) )
				value = "keep-alive";
			sb.append("\r\n\t\t"+key+": "+value);
		}
		sb.append("\r\nHTTP参数:");
		Iterator<Map.Entry<String, String[]>> iterator = req.getParameterMap().entrySet().iterator();
		while(iterator.hasNext())
		{
			Map.Entry<String, String[]> e = iterator.next();
			sb.append("\r\n\t");
			sb.append(e.getKey());
			sb.append("=");
			for(String value : e.getValue())
				sb.append(value+"\t");
		}
		log.info(sb.toString());
		return query();
	}
	/**
	 * 通过DIGG
	 * @return
	 */
	public String query()
	{
		try 
		{
			this.gridxml = Kit.unicode2Chr(gridxml);
	        String result = super.grid(this.gridxml);
	        if (this.gridxml.indexOf("syslogquery") == -1) {
	        	super.logoper(String.format("执行DIGG模板【%s, 版本v%s】查询", new Object[] {
	        		this.gridtitle, this.gridversion }), 
	        		"DIGG-QUERY", this.gridxml, "diggcfg!version.action?id=" + this.gridxml);
	        }
	        return result;
		}
		catch (Exception e) 
		{
			super.logoper(LogSeverity.ERROR, String.format("执行DIGG模板【%s, 版本v%s】查询出现异常%s", new Object[] {
				this.gridtitle, this.gridversion, e.getMessage() }), 
				"DIGG-QUERY", this.diggChecker.toString(), "diggcfg!version.action?id=" + this.gridxml);
			super.setResponseException("打开DIGG模板[" + this.gridxml + "]失败，因为异常" + e);
		}
		return "alert";
	}
	
	/**
	 * 向gird界面返回数据
	 * @return
	 */
	public String data()
	{
    	ServletOutputStream out = null;
    	JSONObject dataJSON = new JSONObject();
    	long ts = System.currentTimeMillis();
		try
		{
			diggChecker = new DiggChecker(true);
    		HttpServletRequest request = super.getRequest();
			HttpServletResponse response = super.getResponse();
            out = response.getOutputStream();
			response.setContentType("text/json;charset=utf8");
    		response.setHeader("Content-disposition", "inline; filename="+System.currentTimeMillis()+".json");
    		StringBuffer sb = new StringBuffer();
    		sb.append("\r\n----------------\r\nHTTP参数:");
    		sb.append("\r\n\t"+request.getRemoteAddr()+":"+request.getRemotePort()+"\t远端用户: "+request.getRemoteUser());
    		Iterator<Map.Entry<String, String[]>> iterator = request.getParameterMap().entrySet().iterator();
    		while(iterator.hasNext())
    		{
    			Map.Entry<String, String[]> e = iterator.next();
    			sb.append("\r\n\t");
    			sb.append(e.getKey());
    			sb.append("=");
    			for(String value : e.getValue())
    				sb.append(value+"\t");
    		}
    		diggChecker.print(-1, "%s", sb.toString());
			this.setDiggProgress(request);
    		this.setDiggConfig(request);
			curPage = Integer.parseInt(Tools.getValidStr(super.getRequest().getParameter("pq_curpage"), "1"));
			curPage = curPage<=0?1:curPage;
			pageSize  = Integer.parseInt(Tools.getValidStr(super.getRequest().getParameter("pq_rpp"), "0"));
			diggChecker.print(1, "当前页码:%s", curPage);
			diggChecker.print(1, "分页规格:%s", pageSize);
			if( pageSize == 0 )
			{
				if( diggObject.has("pagesize") )
				{
					pageSize = diggObject.getInt("pagesize");
				}
				else
				{
					pageSize = 50;
				}
			}
    		this.digg(dataJSON);
    		int count = dataJSON.getInt("curPageRecords");
//			System.err.println(dataJSON.toString(4));
        	long duration = System.currentTimeMillis() - ts;
    		String endtips = String.format("执行元数据模板[%s]数据查询成功，耗时%s毫秒，数据总记录一共%s条", gridxml, duration, totalRecords);
    		this.diggChecker.print(0, "执行元数据模板[%s]数据查询成功，耗时%s毫秒，数据总记录一共%s条", gridxml, duration, totalRecords);
    		if(curPage>1 && !gridxml.isEmpty() && this.gridxml.indexOf("syslogquery") == -1) {
	        	super.logoper(String.format("执行DIGG模板【%s, 版本v%s】视图查询，获取第%s页(%s)数据成功", new Object[] {
	        		this.gridtitle, this.gridversion, curPage,  count}), 
	        		"DIGG-QUERY", endtips, "diggcfg!version.action?id=" + this.gridxml);
    		}
//    		System.err.print(diggChecker.toString());
        	if( count > 0 ){
        		//有记录的计算响应延时
        		this.diggMgr.setDiggQuerySpeed(gridxml, duration);
        	}
		}
		catch (Exception e)
		{
			this.diggChecker.print(0, "执行元数据模板DIGG发生异常: %s", e.toString());
			this.diggChecker.write(e);
			dataJSON.put("hasException", true);
			String c1 = e.getMessage();
			if( c1 != null ){
				c1 = c1.replaceAll("\"", "");
				c1 = c1.replaceAll("'", "");
			}
			dataJSON.put("message", "执行元数据模板["+(gridxml!=null?gridxml:"未知")+"]DIGG查询出现异常，"+c1);
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"数据管理",
					"执行DIGG模板["+gridxml+"]视图查询出现异常",
					"[<i class='fa fa-digg fa-fw'></i>] 查询数据出现异常"+e.getMessage()+"，堆栈如下所示：\r\n"+diggChecker.toString(),
					"helper!previewxml.action?path="+gridxml,
                    "情况确认", "#feedback?to="+super.getUserAccount());
        	super.logoper(LogSeverity.ERROR, String.format("执行DIGG模板[%s]视图查询出现异常: %s", new Object[] {
        		this.gridxml, e.getMessage()}), 
        		"DIGG-QUERY", diggChecker.toString(), "diggcfg!version.action?id=" + this.gridxml);
//    		System.out.println(diggChecker.toString());
		}
        finally
        {
        	long duration = System.currentTimeMillis() - ts;
    		this.diggChecker.print(1, "数据解析处理从时间[%s]开始，一共耗时%s秒", Tools.getFormatTime("HH:mm:ss", ts), duration/1000);
        	if( this.referer.indexOf("!debug") != -1 ){
        		this.getSession().setAttribute("digg.log", this.diggChecker.payload());
        	}
        	if( out != null )
	    		try
				{
	    			String json = dataJSON.toString();
					out.write(json.getBytes("UTF-8"));
	            	out.close();
				}
				catch (IOException e)
				{
					//log.error("返回数据时出现异常", e);
				}
        }
		return null;
	}
	/**
	 * 界面digg
	 * @throws Exception 
	 */
	protected void setDiggConfig(HttpServletRequest request) throws Exception
	{
		if( diggObject == null ){
			this.diggObject = (JSONObject)super.getSession().getAttribute(referer);
		}
        if( this.diggObject == null )
        {
    		diggChecker.print(0, "从会话【%s】中没有取出DIGG对象，该请求应该是模板解析直接返回数据", referer);
//        	diggChecker.print(1, "请求的会话中没有对象");
//            diggChecker.print(1, "数据模板: %s", gridxml);
//    		diggChecker.print(1, "数据列头数: %s", titleColumns.length());
//    		diggChecker.print(1, "文件名: %s", gridtitle);
//    		diggChecker.print(1, "数据列对象: %s", dataColumns.toString(4));
			throw new Exception("未知的元数据查询请求，因为无法从"+referer+"取得查询配置");
        }
		diggChecker.print(0, "从会话【%s】中取出DIGG对象", referer);
        diggs = this.diggObject.has("diggs")?this.diggObject.getJSONArray("diggs"):null;
        if( diggs == null )
        {
			throw new Exception("Unknown reqeust from "+referer);
        }
        diggChecker.print(1, "Digg个数: %s", diggs.length());
        this.id = diggObject.has("recIndx")?diggObject.getString("recIndx"):"";
		diggChecker.print(1, "数据主键: %s", id);
		dataColumns = diggObject.has("dataColumns")?diggObject.getJSONObject("dataColumns"):new JSONObject();
		diggChecker.print(1, "数据列对象: %s", dataColumns.toString(4));
        gridxml = diggObject.has("gridxml")?diggObject.getString("gridxml"):"";
        gridtitle = diggObject.has("gridtitle")?diggObject.getString("gridtitle"):"";
        gridversion = diggObject.has("gridversion")?diggObject.getString("gridversion"):"";
        
        diggChecker.print(1, "数据模板: %s", gridxml);
        titleColumns = diggObject.getJSONArray("titleColumns");
		diggChecker.print(1, "数据列头数: %s", titleColumns.length());
        filename = this.diggObject.getString("gridtitle");
		diggChecker.print(1, "文件名: %s", filename);
	}
	
	/**
	 * 观察
	 */
	private void watch(int t, String text, Object... args){
		if(workmode == TemplateChecker.WORK ){
    		diggprogress.put("message", String.format("已处理%s条/共%s条记录需要处理", diggStep, totalStep));
		}
		else{
    		diggprogress.put("message", (args.length>0?String.format(text, args):text)+String.format("<br/>已处理%s条/共%s条记录需要处理", diggStep, totalStep));
		}
		diggChecker.print(t, text, args);
    	int p = diggStep*100/totalStep;
		diggprogress.put("value", p);
		if( referer != null ) super.getSession().setAttribute(this.referer+".digg", diggprogress);
	}
	/**
	 * 重新设置总步骤
	 * @param size
	 */
	private void resetTotalStep(int size){
		totalStep = 1;//总步骤数计算
        for(int i = 0; i < diggs.length(); i++)
        {
        	totalStep = 1;
        	JSONObject digg = diggs.getJSONObject(i);
        	JSONArray joins = digg.has("joins")?digg.getJSONArray("joins"):new JSONArray();
        	totalStep += size*joins.length();
        	String type = digg.getString("type");
        	if( "mongo".equalsIgnoreCase(type) )
        	{
        		if( digg.has("group") || digg.has("count") )
    			{
                	totalStep += size;
    			}
        		if( digg.has("counts") )
    			{
                	totalStep += size*digg.getJSONArray("counts").length();
    			}
        	}
        }
	}
	/**
	 * 执行数据拔取根据分页要求
	 * @param dataJSON
	 * @throws Exception
	 */
	private JSONObject diggprogress = new JSONObject();
	private int totalStep = 0;
	private int diggStep = 0;
	private int diggIndx = 0;
	protected void digg(JSONObject dataJSON) throws Exception
	{
		long _ts = System.currentTimeMillis();
		resetTotalStep(pageSize);
		Cache cache;
		boolean totalcount = false;
        byte[] data = this.diggObject.has("dimension")?((byte[])this.diggObject.get("dimension")):null;
        if( data != null ){
        	cache = new Cache(data);
    		diggChecker.print(0, "本次请求是报表数据引擎，数据维度共%s条", cache.size());
			int skip = (curPage-1)*pageSize;
			cache.setSkip(skip, this.pageSize);
        	totalStep += 1;
	        this.watch(1, "完成【报表模板】的DIGG准备，报表一共%s个维度项", cache.size());
        }
        else{
        	cache = new Cache();
            this.watch(1, "完成【查询模板】的DIGG准备");
        }
        for(diggIndx = 0; diggIndx < diggs.length(); diggIndx++)
        {
        	long ts = System.currentTimeMillis();
        	totalcount = diggIndx == 0;
        	JSONObject digg = diggs.getJSONObject(diggIndx);
        	String type = digg.getString("type");
	        this.watch(0, "启动第%s层数据处理, 数据类型%s", diggIndx, type, Kit.getDurationMs(System.currentTimeMillis()-_ts));
	        diggChecker.print(1, "[%s]DIGG配置参数是: %s", diggIndx, digg.toString(4));
	        if( "sql".equalsIgnoreCase(type) )
        	{
	        	if( dataColumnsUpper.isEmpty() ){
	        		Iterator<?> iterator = dataColumns.keys();
	        		while(iterator.hasNext()){
	        			String dataIndx = iterator.next().toString();
	        			this.dataColumnsUpper.put(dataIndx.toUpperCase(), dataIndx);
	        		}
	        	}
        		if( digg.has("groupby") )
        		{
        			setGroupbyData(cache, digg, totalcount);
        		}
        		else
        		{
        			if( !totalcount || diggIndx > 0 ){
        				throw new Exception("多层数据挖掘之【数据查询】只允许放在第一个Digg执行.");
        			}
            		this.setSqlData(cache, digg);
	        		resetTotalStep(cache.size());
        		}
        	}
        	else if( "zookeeper".equalsIgnoreCase(type) )
        	{
//        		this.setZookeeperData(cache, digg);
        	}
        	else if( "redis".equalsIgnoreCase(type) )
        	{
//        		System.err.println(digg.toString(4));
        		this.setRedisData(cache, digg);
        	}
        	else if( "mongo".equalsIgnoreCase(type) )
        	{
        		if( digg.has("group") )
        		{
        			setAggregateData(cache, digg, totalcount);
        		}
        		else if( digg.has("count") )
    			{//计算数量
        			this.setMongoDiggCount(cache, digg);
    			}
        		else if( digg.has("counts") )
    			{
        			this.setMongoDiggCounts(cache, digg);
    			}
        		else if( digg.has("last") )
    			{
        			this.setMongoDiggLast(cache, digg);
    			}
        		else
        		{
        			if( !totalcount || diggIndx > 0 ){
        				throw new Exception("多层数据挖掘之【数据查询】只允许放在第一个Digg执行.");
        			}
	        		this.setMongoData(cache, digg);
	        		resetTotalStep(cache.size());
        		}
        	}
        	else if( "json".equalsIgnoreCase(type) ){
        		setJsonData(cache, digg);
        	}
        	else if( "oltp".equalsIgnoreCase(type) )
        	{
        		String className = digg.getString("class");
        		Class<?> clz = Class.forName(className);
        		OLTP oltp = (OLTP)clz.newInstance();
        		oltp.hanlde(cache, digg);
        	}
        	long duration = System.currentTimeMillis() - ts;
    		this.diggChecker.print(1, "该层数据解析处理从时间[%s]开始，一共耗时%s秒", Tools.getFormatTime("HH:mm:ss", ts), duration/1000);
        	diggStep += 1;
        	int p = diggStep*100/totalStep;
    		diggprogress.put("value", p);
    		if(workmode == TemplateChecker.WORK ){
        		diggprogress.put("message", String.format("已处理%s条/共%s条记录需要处理", diggStep, totalStep));
    		}
    		else{
        		diggprogress.put("message", String.format("完成第%s层数据处理，耗时%s秒<br/>已处理%s条/共%s条记录需要处理", diggIndx, duration/1000, diggStep, totalStep));
    		}
        	if( workmode != TemplateChecker.WORK ) super.getSession().setAttribute(this.referer+".digg", diggprogress);
        }
        diggprogress.put("value", 100);
    	if( workmode != TemplateChecker.WORK ) super.getSession().setAttribute(this.referer+".digg", diggprogress);
        
        for (int i = 0; i < titleColumns.length(); i++)
        {
        	JSONObject column = titleColumns.getJSONObject(i);
        	if( !column.has("translate") || !column.has("dataIndx") ){
        		continue;
        	}
        	String translate = column.getString("translate");
        	String dataIndx = column.getString("dataIndx");
    		this.watch(1, "发现字段%s翻译%s", dataIndx, translate);
        	if( "ip".equalsIgnoreCase(translate) ){
        		HashMap<Object, String> regions = new HashMap<Object, String>();
    			for(int j = cache.start(); j < cache.size(); j++)
    			{
    				JSONObject row = cache.get(j);
//            		System.err.println(row.toString(4));
    				if( row.has(dataIndx) )
    				{
        				Object ip = getRowValue(row, dataIndx);
        				if( ip != null ){
        					if( regions.containsKey(ip) )
        					{
        						row.put(dataIndx, regions.get(ip));
        					}
        					else
        					{
        						String region = HttpUtils.getIpRegion(ip.toString());
        						row.put(dataIndx, region);
        						regions.put(ip, region);
        					}
        					row.put(dataIndx+"__", ip.toString());	
        				}
    				}
    			}
        	}
        	else if( "location".equalsIgnoreCase(translate) ){
        		String location = column.has("location")?column.getString("location"):"";
        		if(location.isEmpty()){
        			continue;
        		}
        		String query = column.has("query")?column.getString("query"):"公司";
        		this.watch(2, "经纬度位置字段%s翻译，查询字符串是%s", location, query);
        		String[] args = Tools.split(location, ",");
        		if( args.length != 2 ){
            		this.watch(3, "配置不正确");
        			continue;
        		}
    			String longitude = args[0].trim();
    			String latitude = args[1].trim();
        		this.watch(3, "经度字段%s,纬度字段%s, 从%s扫描%s条记录进行翻译", longitude, latitude, cache.start(), cache.size());
    			for(int j = cache.start(); j < cache.size(); j++)
    			{
    				JSONObject row = cache.get(j);
    				Object x = getRowValue(row, longitude, j==19);
    				Object y = getRowValue(row, latitude, j==19);
    				if( x == null || y == null ) {
    					continue;
    				}
					String l = Tools.location(x.toString(), y.toString(), query);
            		this.watch(5, "经纬度%s, %s地址是%s", x, y, l);
					row.put(dataIndx, l);
    			}
        	}
        }
        if( data != null ){
	    	String old_pq_sort = "";
	    	if( diggObject.has("pq_sort")) old_pq_sort = diggObject.getString("pq_sort");
	    	if( !old_pq_sort.equals(pq_sort) )
	    	{//重新拍下
	    		if( pq_sort != null && pq_sort.startsWith("[") && pq_sort.endsWith("]") )
	    		{
	    			JSONArray sort = new JSONArray(pq_sort);
	    			cache.sort(sort);
	                diggObject.put("pq_sort", pq_sort);
//	                if( referer != null ) super.getSession().setAttribute(referer, diggObject);
	    		}
	    	}
			JSONObject filter = super.getPqFilter();
			JSONArray conditions = filter.has("data")?filter.getJSONArray("data"):null;
			for(int i = cache.start(); i < cache.size(); i++ )
			{
				JSONObject row = cache.get(i);
				if( conditions != null && this.diggMgr.filterByConditions(conditions, dataColumns, row) )
				{
					cache.remove(i);
					i -= 1;
				}
			}
			totalRecords = cache.size();
        }
        diggprogress.put("message", String.format("DIGG数据%s，涉及数据查询与运算%s次，共耗时%s", cache.size(), totalStep, Kit.getDurationMs(System.currentTimeMillis()-_ts)));
        diggprogress.put("finish", true);
    	if( workmode != TemplateChecker.WORK ) super.getSession().setAttribute(this.referer+".digg", diggprogress);
		dataJSON.put("pageSize", this.pageSize);
		dataJSON.put("curPageRecords", cache.size());
		dataJSON.put("totalRecords", totalRecords);
		dataJSON.put("curPage", curPage);
		dataJSON.put("data", cache.getData());
//		System.err.println(dataJSON.toString(4));
	}

	/**
	 * 设置芒果数据的最后个对象
	 * @param digg
	 * @throws Exception
	 */
	private void setMongoDiggLast(Cache cache, JSONObject digg)
		throws Exception
	{
		long ts = System.currentTimeMillis();
		MongoCollection<Document> col = DiggMgr.getMongoCollection(
			digg.getString("mongo.host"), 
			digg.getInt("mongo.port"),
			digg.getString("mongo.username"),
			new String(Base64X.decode(digg.getString("mongo.password"))),
			digg.getString("mongo.database"),
			digg.getString("mongo.database1"),
			digg.getString("mongo.tablename")
		);
		String last = digg.getString("last");
		for(int j = cache.start(); j < cache.size(); j++)
		{
			JSONObject row = cache.get(j);
			BasicDBObject where = null;
//			log.info(digg.toString(4));
			if( digg.has("condition") )
			{
				String json = digg.getString("condition");
				where = (BasicDBObject)JSON.parse(json);
				Iterator<String> iterator = where.keySet().iterator();
				while(iterator.hasNext()){
					String key = iterator.next();
					String key0 = where.getString(key);
					key0 = key0.substring(1);
					key0 = key0.substring(0, key0.length()-1);
					Object val = row.has(key0)?row.get(key0):null;
					if( val == null ){
						break;
					}
					where.put(key, val);
				}
			}
			FindIterable<Document> find = where!=null?col.find(where):col.find();
			find = find.sort(new Document("_id",-1));
			find = find.limit(1);
			MongoCursor<Document> cursor = find.iterator();
			this.watch(2, "LAST(%s)，耗时%s", where.toString(), Kit.getDurationMs(System.currentTimeMillis()-ts));
			if(cursor.hasNext())
			{
				Document doc = cursor.next();
				diggChecker.print(2, "数据: %s", doc.toString());
				Object _id = doc.remove("_id");
				doc.put("_id", _id.toString());
				String json = doc.toJson();
				row.put(last, new JSONObject(json));
			}
			diggStep += 1;
		}
	}
	/**
	 * 设置芒果数据表数量
	 * @param digg
	 * @throws Exception
	 */
	private void setMongoDiggCount(Cache cache, JSONObject digg)
		throws Exception
	{
		long ts = System.currentTimeMillis();
		String tablename = digg.getString("mongo.tablename");
		MongoCollection<Document> col = null;
		boolean s = false;
		if( tablename.indexOf("$") != -1 ){
			s = true;
		}
		else{
			col = DiggMgr.getMongoCollection(
				digg.getString("mongo.host"), 
				digg.getInt("mongo.port"),
				digg.getString("mongo.username"),
				new String(Base64X.decode(digg.getString("mongo.password"))),
				digg.getString("mongo.database"),
				digg.getString("mongo.database1"),
				digg.getString("mongo.tablename")
			);
		}
		String from = tablename;
		String count = digg.getString("count");
		for(int j = cache.start(); j < cache.size(); j++)
		{
			JSONObject row = cache.get(j);
			if( s ) {
				from = this.parameter(tablename, row, '$');
				col = DiggMgr.getMongoCollection(
					digg.getString("mongo.host"), 
					digg.getInt("mongo.port"),
					digg.getString("mongo.username"),
					new String(Base64X.decode(digg.getString("mongo.password"))),
					digg.getString("mongo.database"),
					digg.getString("mongo.database1"),
					from
				);
			}
			BasicDBObject where = null;
			if( !digg.has("condition") )
			{
				long r = col.count();
				row.put(count, r);
		        this.watch(1, "COUNT(1) from %s is %s ，耗时%s", from, r, Kit.getDurationMs(System.currentTimeMillis()-ts));
				continue;
			}
			String json = digg.getString("condition");
			where = (BasicDBObject)JSON.parse(json);
			this.setMongoWhere(where, row);
//			System.err.println(where.toString());
			long r = col.count(where);
			diggStep += 1;
	        this.watch(1, "COUNT(%s) from %s is %s ，耗时%s", where.toString(), from, r, Kit.getDurationMs(System.currentTimeMillis()-ts));
			row.put(count, (int)r);
		}
	}
	
	/**
	 * 设置芒果数据库的条件
	 * @param where
	 * @param row
	 */
	public void setMongoWhere(BasicDBObject where, JSONObject row){
		Iterator<String> iterator = where.keySet().iterator();
		while(iterator.hasNext()){
			String key = iterator.next();
			Object condition = where.get(key);
			if( condition == null ){
				continue;
			}
			if( condition instanceof String ){
				String key0 = condition.toString();
				if( key0.startsWith("%") && key0.endsWith("%")  ){
					key0 = key0.substring(1);
					key0 = key0.substring(0, key0.length()-1);
					Object val = row.has(key0)?row.get(key0):null;
					if( val == null ){
						where.remove(key);
						continue;
					}
					if( val instanceof JSONArray ){
						where.put(key, JSON.parse(val.toString()));
					}
					else{
						where.put(key, val);
					}
				}
				else if(key0.startsWith("$day")){
                	String args[] = Tools.split(key0, " ");
                	if( args.length == 3 ){
                		Object val = row.get(args[2]);
                		if( val != null && Tools.isNumeric(args[1]) ){
                			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                			try{
                  		      	int day = Integer.parseInt(args[1]);
                  		      	Calendar calendar = Calendar.getInstance();
                  		      	calendar.setTime(sdf.parse(val.toString()));
                  		      	calendar.add(Calendar.DAY_OF_MONTH, day);
                  		      	val = Tools.getFormatTime("yyyy-MM-dd", calendar.getTimeInMillis());
                  		      	where.put(key, val);
                			}
                			catch(Exception e){
                			}
                		}
                	}	
				}
			}
			else if( condition instanceof BasicDBList ){
				BasicDBList list = (BasicDBList)condition;
				for(int i = 0; i < list.size(); i++){
					Object o = list.get(i);
					if( o == null ) continue;
					if( o instanceof String ){
						key = o.toString();
						if( key.startsWith("%") && key.endsWith("%") ){
							key = key.substring(1);
							key = key.substring(0, key.length()-1);
							Object val = row.has(key)?row.get(key):null;
							if( val == null ){
								list.remove(i);
								i -= 1;
								continue;
							}
							list.set(i, val);
						}
					}
					else if( o instanceof BasicDBObject ){
						//o = { "schedule_date" : { "$lt" : "%done_date%"}} 
						BasicDBObject e = (BasicDBObject)o;
						this.setMongoWhere(e, row);
						if( e.isEmpty() ){
							list.remove(i);
							i -= 1;
						}
					}
				}
			}
			else if( condition instanceof BasicDBObject ){
				this.setMongoWhere((BasicDBObject)condition, row);
			}
		}
	}
	/**
	 * 统计算数
	 * @param cache
	 * @param digg
	 * @throws Exception
	 */
	private void setMongoDiggCounts(Cache cache, JSONObject digg)
		throws Exception
	{
		long ts = System.currentTimeMillis();
		String tablename = digg.getString("mongo.tablename");
		MongoCollection<Document> col = null;
		boolean s = false;
		if( tablename.indexOf("$") != -1 ){
			s = true;
		}
		else{
			col = DiggMgr.getMongoCollection(
				digg.getString("mongo.host"), 
				digg.getInt("mongo.port"),
				digg.getString("mongo.username"),
				new String(Base64X.decode(digg.getString("mongo.password"))),
				digg.getString("mongo.database"),
				digg.getString("mongo.database1"),
				digg.getString("mongo.tablename")
			);
		}
		String from = tablename;
		JSONArray counts = digg.getJSONArray("counts");
		for(int j = cache.start(); j < cache.size(); j++)
		{
			JSONObject row = cache.get(j);
			if( s ) {
				from = this.parameter(tablename, row, '$');
				col = DiggMgr.getMongoCollection(
					digg.getString("mongo.host"), 
					digg.getInt("mongo.port"),
					digg.getString("mongo.username"),
					new String(Base64X.decode(digg.getString("mongo.password"))),
					digg.getString("mongo.database"),
					digg.getString("mongo.database1"),
					from
				);
			}
			for(int i = 0; i < counts.length(); i++ ){
				BasicDBObject where = null;
				JSONObject count = counts.getJSONObject(i);
				if("count".equalsIgnoreCase(count.getString("type"))){
					if( !count.has("condition") )
					{
						long r = col.count();
						row.put(count.getString("column"), r);
						this.watch(1, "COUNT(1) from %s to %s is %s ，耗时%s", from, count.getString("column"), r, Kit.getDurationMs(System.currentTimeMillis()-ts));
						continue;
					}
					String json = count.getString("condition");
					where = (BasicDBObject)JSON.parse(json);
					setMongoWhere(where, row);
					this.watch(1, "COUNT(%s) from %s to %s", where.toString(), from, count.getString("column"));
					long r = col.count(where);
					diggStep += 1;
					row.put(count.getString("column"), (int)r);
				}
				else if("eval".equalsIgnoreCase(count.getString("type"))){
					String expression = count.getString("expression");
					String dataType = count.has("dataType")?count.getString("dataType"):"";
					expression = parameter(expression, row, '%');
					try {
						Evaluator evaluator = new Evaluator();
						try {
							String result = evaluator.evaluate(expression);
							this.watch(1, "Eval( %s ) result is %s", expression, result);
							if( !"NaN".equalsIgnoreCase(result) ){
								row.put(count.getString("column"), result);
								if( !dataType.isEmpty() ){
									if( "percent".equals(dataType) ){
										double d = Double.parseDouble(result);
									    final java.text.DecimalFormat DFORMAT = new java.text.DecimalFormat("0.00%");
										row.put(count.getString("column"), DFORMAT.format(d));
									}
								}
							}
						} 
						catch (EvaluationException e) {
							this.watch(1, "Failed to eval( %s ) for %s", expression, e.toString());
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	/**
	 * 设置Digg的进度
	 * @param request
	 */
	private void setDiggProgress(HttpServletRequest request){
		//获取查询参数对象
        referer = request.getHeader("referer");
        if( id != null && !id.isEmpty() )
        {
        	referer += "#"+id;
        }
    	if( this.referer != null && (
    		this.referer.indexOf("!debug") != -1 ||
    		this.referer.indexOf("diggcfg!api.action") != -1) ){
    		workmode = TemplateChecker.DEBUG;
    	}
    	diggprogress.put("value", 0);
    	diggprogress.put("message", "启动DIGG.");
    	if( workmode != TemplateChecker.WORK ) super.getSession().setAttribute(this.referer+".digg", diggprogress);
		diggChecker.print(0, "请求来自于: %s, 工作模式: %s", referer, workmode);
	}
	
	/**
	 * 导出数据
	 * @return
	 */
	public String export()
	{
    	ServletOutputStream out = null;
    	JSONObject progress = new JSONObject();
    	progress.put("value", 0);
    	long ts = System.currentTimeMillis();
    	int rowIndx = 0;
    	try{
			diggChecker = new DiggChecker(false);
			StringBuffer sb = new StringBuffer();
    		sb.append("\r\nReceive the request of export, the parameters of below ");
    		HttpServletRequest req = super.getRequest();
    		Iterator<Map.Entry<String, String[]>> iterator = req.getParameterMap().entrySet().iterator();
    		while(iterator.hasNext())
    		{
    			Map.Entry<String, String[]> e = iterator.next();
    			sb.append("\r\n\t");
    			sb.append(e.getKey());
    			sb.append("=");
    			for(String value : e.getValue())
    				sb.append(value+"\t");
    		}
    		
			HttpServletResponse response = super.getResponse();
			this.setDiggProgress(req);
			this.setDiggConfig(req);
			progress.put("message", "下载文件:"+filename);
			if(!thisisapi){//如果接口调用就不记录进度
				super.getSession().setAttribute(this.referer+".export", progress);
			}
			this.curPage = 0;
			this.pageSize = 256;
	        for(int i = 0; i < diggs.length(); i++)
	        {
	        	JSONObject digg = diggs.getJSONObject(i);
	        	String type = digg.getString("type");
	        	if( "mongo".equalsIgnoreCase(type) )
	        	{
	        		if( digg.has("group") || digg.has("count") )
	    			{
	        			pageSize = 10;
	        			break;
	    			}
	        	}
	        	else if( "sql".equalsIgnoreCase(type) )
	        	{
	        		if( digg.has("groupby") )
	        		{
	        			pageSize = 10;
	        			break;
	        		}
	        	}
	        }
			this.pq_filter = this.diggObject.has("pq_filter")?this.diggObject.getString("pq_filter"):pq_filter;
			this.pq_filter = this.pq_filter!=null?pq_filter:"";
//			this.pq_sort = this.diggObject.has("pq_sort")?this.diggObject.getString("pq_sort"):"";
//			pageSize  = Integer.parseInt(Tools.getValidStr(super.getRequest().getParameter("pq_rpp"), "50"));
			labelColumns = diggObject.has("labelColumns")?diggObject.getJSONObject("labelColumns"):new JSONObject();
//			System.err.println(labelColumns.toString(4));
			out = response.getOutputStream();
			int cellIndx = 0;
			if( "sql".equalsIgnoreCase(filetype) )
			{
				super.setResponseException("暂不支持导出SQL脚本.");
				return "alert";
			}
			else if( "csv".equalsIgnoreCase(filetype) )
			{
				filename = filename+".csv";
		    	PrintWriter writer = null;
				response.setContentType("text/csv;charset=UTF-8");// 定义输出类型
				response.setHeader("Content-disposition", "attachment; filename*=UTF-8''"+URLEncoder.encode(filename,"UTF-8"));
				writer = new PrintWriter(new OutputStreamWriter (out, "GB2312"));
//				System.err.println(titleColumns.toString(4));
		        /** ***************以下是CSV第一行列标题**********************/
		        for (int i = 0; i < titleColumns.length(); i++)
		        {
		        	JSONObject column = titleColumns.getJSONObject(i);
		        	if( column.has("exportable") && !column.getBoolean("exportable") ){
		        		continue;
		        	}
		        	if( cellIndx > 0 ) writer.print(',');
		        	cellIndx += 1;
		        	writer.print('"');
		        	writer.print(Tools.delHTMLTag(column.getString("title")));  
		        	writer.print('"');
		        }
		    	writer.println();
		    	JSONObject dataJSON = new JSONObject();
		    	do
		    	{
		    		diggChecker = new DiggChecker(true);
		    		diggChecker.print(-1, "%s", sb.toString());
		    		this.curPage += 1;
			    	this.digg(dataJSON);
			    	rowIndx += 1;
			    	rowIndx += this.export(dataJSON.getJSONArray("data"), writer, null, rowIndx);
			        if( getSession().getAttribute(referer +".abort") != null )
			        {
			        	super.getSession().removeAttribute(referer +".abort");
			        	throw new Exception("用户中止了数据导出");
			        }
			        rowIndx -= 1;
			    	progress.put("message", "已下载"+rowIndx+"条/总共"+totalRecords+"条记录");
			    	progress.put("value", totalRecords>0?(rowIndx*100/totalRecords):100);
					if(!thisisapi){
						super.getSession().setAttribute(this.referer+".export", progress);
					}
		    	}
		    	while( curPage * pageSize < this.totalRecords );
		    	progress.put("value", 100);
				if(!thisisapi){
					super.getSession().setAttribute(this.referer+".export", progress);
				}
		        writer.flush();
		        /** *********关闭文件************* */  
		        writer.close();
			}
			else if( "xls".equalsIgnoreCase(filetype) )
			{
            	filename = filename+".xls";
            	response.setContentType("application/vnd.ms-excel;charset=UTF-8");// 定义输出类型
            	response.setHeader("Content-disposition", "attachment; filename*=UTF-8''"+URLEncoder.encode(filename,"UTF-8"));
            	// 设定输出文件头        
        	    //定义输出流，以便打开保存对话框_______________________end  
                /** **********创建工作簿************ */  
                WritableWorkbook workbook = Workbook.createWorkbook(out);
                /** **********创建工作表************ */  
                WritableSheet sheet = workbook.createSheet("数据", 0); 
                /** **********设置纵横打印（默认为纵打）、打印纸***************** */  
                jxl.SheetSettings sheetset = sheet.getSettings();  
                sheetset.setProtected(false);  
                /** ************设置单元格字体************** */  
                WritableFont BoldFont = new WritableFont(WritableFont.createFont("微软雅黑"), 9,WritableFont.BOLD);  
               
                /** ************以下设置三种单元格样式，灵活备用************ */  
                // 用于标题居中  
                /** ***************以下是EXCEL开头大标题，暂时省略********************* */  
                //sheet.mergeCells(0, 0, colWidth, 0);  
                //sheet.addCell(new Label(0, 0, "XX报表", wcf_center));  
                /** ***************以下是EXCEL第一行列标题**********************/
                String mergeName = null;
                boolean hasParentTitle = false;
                int mergeIndx = 0;
                for (int i = 0; i < titleColumns.length(); i++)
                {  
                	JSONObject column = titleColumns.getJSONObject(i);
                	if( !column.has("dataIndx") ) continue;
                	if( column.has("exportable") && !column.getBoolean("exportable") ){
                		continue;
                	}
                	if( column.has("parent_title") ){
                		hasParentTitle = true;
                		String title = Tools.delHTMLTag(column.getString("parent_title"));
                		if( !title.equals(mergeName) ){
                			if( mergeName != null ){
                        		WritableCellFormat wcf_center = new WritableCellFormat(BoldFont);  
                        		wcf_center.setBorder(Border.ALL, BorderLineStyle.THIN); // 线条  
                                wcf_center.setVerticalAlignment(VerticalAlignment.CENTRE); // 文字垂直对齐  
                                wcf_center.setWrap(false); // 文字是否换行  
        	        			wcf_center.setAlignment(Alignment.CENTRE);
        	        			wcf_center.setBackground(Colour.GRAY_25);
                            	CellView cv = new CellView();
                        		cv.setAutosize(true);
                        		sheet.setColumnView(mergeIndx, cv);
                    			sheet.addCell(new Label(mergeIndx, 0, mergeName, wcf_center));
                			}
                			if( mergeIndx != -1 && cellIndx - mergeIndx > 0 ){
                				sheet.mergeCells(mergeIndx, 0, cellIndx - 1, 0);
                			}
                			mergeIndx = cellIndx;
                			mergeName = title;
                		}
                	}
                	else{
            			if( mergeName != null ){
                    		WritableCellFormat wcf_center = new WritableCellFormat(BoldFont);  
                    		wcf_center.setBorder(Border.ALL, BorderLineStyle.THIN); // 线条  
                            wcf_center.setVerticalAlignment(VerticalAlignment.CENTRE); // 文字垂直对齐  
                            wcf_center.setWrap(false); // 文字是否换行  
                			wcf_center.setAlignment(Alignment.CENTRE);
                			wcf_center.setBackground(Colour.GRAY_25);
                        	CellView cv = new CellView();
                    		cv.setAutosize(true);
                    		sheet.setColumnView(mergeIndx, cv);
                			sheet.addCell(new Label(mergeIndx, 0, mergeName, wcf_center));
            			}
            			if( mergeIndx != -1 && cellIndx - mergeIndx > 1 ){
            				sheet.mergeCells(mergeIndx, 0, cellIndx - 1, 0);
            			}
                		mergeName = null;
                		mergeIndx = -1;
                		
                	}
                	cellIndx += 1;
                }
    			if( mergeName != null ){
            		WritableCellFormat wcf_center = new WritableCellFormat(BoldFont);  
            		wcf_center.setBorder(Border.ALL, BorderLineStyle.THIN); // 线条  
                    wcf_center.setVerticalAlignment(VerticalAlignment.CENTRE); // 文字垂直对齐  
                    wcf_center.setWrap(false); // 文字是否换行  
        			wcf_center.setAlignment(Alignment.CENTRE);
        			wcf_center.setBackground(Colour.GRAY_25);
                	CellView cv = new CellView();
            		cv.setAutosize(true);
            		sheet.setColumnView(mergeIndx, cv);
        			sheet.addCell(new Label(mergeIndx, 0, mergeName, wcf_center));
    			}
    			if( mergeIndx != -1 && cellIndx - mergeIndx > 1 ){
    				sheet.mergeCells(mergeIndx, 0, cellIndx - 1, 0);
    			}
                cellIndx = 0;
                for (int i = 0; i < titleColumns.length(); i++)
                {  
                	JSONObject column = titleColumns.getJSONObject(i);
                	if( !column.has("dataIndx") ) continue;
                	if( column.has("exportable") && !column.getBoolean("exportable") ){
                		continue;
                	}
                    WritableCellFormat wcf = new WritableCellFormat(BoldFont);  
                    wcf.setBorder(Border.ALL, BorderLineStyle.THIN); // 线条  
                    wcf.setVerticalAlignment(VerticalAlignment.CENTRE); // 文字垂直对齐  
                    wcf.setAlignment(Alignment.LEFT); // 文字水平对齐  
                    wcf.setWrap(false); // 文字是否换行  
		        	if( column.has("align") && "center".equalsIgnoreCase(column.getString("align")) ){
		        		wcf.setAlignment(Alignment.CENTRE);
		        	}
		        	wcf.setBackground(Colour.GRAY_25);
                	CellView cv = new CellView();
            		cv.setAutosize(true);
            		cv.setSize(128);
            		String title = Tools.delHTMLTag(column.getString("title"));
                	if( column.has("parent_title") ){
                		sheet.setColumnView(cellIndx, cv);
                		sheet.addCell(new Label(cellIndx++, 1, title, wcf));
                	}
                	else if( hasParentTitle ){
                		sheet.mergeCells(cellIndx, 0, cellIndx, 1);
                		sheet.setColumnView(cellIndx, cv);
                		sheet.addCell(new Label(cellIndx++, 0, title, wcf));
                	}
                	else {
                		sheet.setColumnView(cellIndx, cv);
                		sheet.addCell(new Label(cellIndx++, 0, title, wcf));
                	}
                }
                /** ***************以下是EXCEL正文数据********************* */
		    	JSONObject dataJSON = new JSONObject();
		    	do
		    	{
		    		diggChecker = new DiggChecker(true);
		    		diggChecker.print(-1, "%s", sb.toString());
		    		this.curPage += 1;
			    	this.digg(dataJSON);
			    	rowIndx += hasParentTitle?2:1;
			    	rowIndx += this.export(dataJSON.getJSONArray("data"), null, sheet, rowIndx);
			        if( getSession().getAttribute(referer +".abort") != null )
			        {
			        	super.getSession().removeAttribute(referer +".abort");
//			        	throw new Exception("用户中止了数据导出");
			        	break;
			        }
			        rowIndx -= hasParentTitle?2:1;
			    	progress.put("message", "已下载"+rowIndx+"条/总共"+totalRecords+"条记录");
			    	progress.put("value", totalRecords>0?(rowIndx*100/totalRecords):100);
					if(!thisisapi){
						super.getSession().setAttribute(this.referer+".export", progress);
					}
		    	}
		    	while( curPage * pageSize < this.totalRecords );
		    	progress.put("value", 100);
				if(!thisisapi){
					super.getSession().setAttribute(this.referer+".export", progress);
				}
				/** **********将以上缓存中的内容写到EXCEL文件中******** */  
	            workbook.write();
	            out.flush();
	            /** *********关闭文件************* */  
	            workbook.close();
			}
			message = "导出数据完毕，共"+totalRecords+"条记录，共耗时"+Kit.getDurationMs(System.currentTimeMillis()-ts);
	    	progress.put("message", message);
	    	progress.put("finish", true);
			if(!thisisapi){
				super.getSession().setAttribute(this.referer+".export", progress);
			}
	        if (this.gridxml.indexOf("syslogquery") == -1) {
	        	super.logoper(String.format("执行DIGG模板【%s, 版本v%s】视图查询导出数据到Excel文件一共%s条记录", new Object[] {
	        		this.gridtitle, this.gridversion, totalRecords }), 
	        		"DIGG-EXPORT", message, "diggcfg!version.action?id=" + this.gridxml);
	        	long duration = System.currentTimeMillis() - ts;
        		//有记录的计算响应延时
        		this.diggMgr.setDiggExportSpeed(gridxml, duration);
	        }
		}
		catch (Exception e)
		{
			log.error("Failed to export", e);
			this.diggChecker.print(0, "执行元数据模板DIGG导出发生异常: %s", e.toString());
			this.diggChecker.write(e);
        	if( this.referer.indexOf("!debug") != -1 ){
        		this.getSession().setAttribute("export.log", this.diggChecker.payload());
        	}
	    	progress.put("exception", true);
	    	progress.put("message", "导出数据到"+this.filetype+"出现异常: "+e.getMessage());
			if(!thisisapi){
				super.getSession().setAttribute(this.referer+".export", progress);
			}
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"数据管理",
					"执行["+gridxml+"]数据导出出现异常",
					"[<i class='fa fa-digg fa-fw'></i>] 导出数据出现异常"+e.getMessage()+"，堆栈如下所示：\r\n"+this.getRequestInfo()+"\r\n"+diggChecker.toString(),
					"helper!previewxml.action?path="+gridxml,
                    "情况确认", "#feedback?to="+super.getUserAccount());
	        if (this.gridxml.indexOf("syslogquery") == -1) {
	        	super.logoper(String.format("执行DIGG模板【%s, 版本v%s】视图查询导出数据到Excel文件出现异常:%s", new Object[] {
	        		this.gridtitle, this.gridversion, e.getMessage() }), 
	        		"DIGG-EXPORT", diggChecker.toString(), "diggcfg!version.action?id=" + this.gridxml, e);
	        }
		}
        finally
        {
        	if( this.referer != null && this.referer.indexOf("diggcfg!api.action") != -1 ){
        		this.getSession().setAttribute("export.log", this.diggChecker.payload());
        	}
    		try
			{
            	if( out != null ) out.close();
			}
			catch (IOException e)
			{
				log.error("", e);
			}
        }
		return null;
	}	
	
	/**
	 * 
	 * @param row
	 * @param dataIndx
	 * @return
	 */
	private Object getRowValue(JSONObject row, String dataIndx)
	{
		return getRowValue(row, dataIndx, false);
	}
	private Object getRowValue(JSONObject row, String dataIndx, boolean debug)
	{
		String[] args = Tools.split(dataIndx, ".");
		if( args.length == 1 )
		{
			return row.has(dataIndx)?row.get(dataIndx):null;
		}
		return getRowValue(row, args, 0, debug);
	}
	
	private Object getRowValue(JSONObject row, String args[], int i, boolean debug)
	{
		if( row == null ) return null;
		if( i + 1 == args.length )
		{
			if( debug ) this.watch(4+i, "最后返回----------%s: exist=%s", args[i], row.has(args[i]));
			return row.has(args[i])?row.get(args[i]):null;
		}
		if( debug ) this.watch(4+i, "[%s] %s: exist=%s", i, args[i], row.has(args[i]));
		if( row.has(args[i])){
			 Object obj = row.get(args[i++]);
			 if( debug ) this.watch(4+i, "[%s] %s=%s", i, args[i-1], obj.toString());
			 if( obj instanceof JSONObject ){
				 return getRowValue((JSONObject)obj, args, i, debug);
			 }
			 else if( obj instanceof JSONArray && args[i+1].equalsIgnoreCase("length")){
				 return ((JSONArray)obj).length();
			 }
		}
		return null;
	}
	/**
	 * 导出分页数据
	 * @param data
	 * @param writer
	 * @param sheet
	 * @param index
	 * @return 数据条数
	 * @throws Exception
	 */
	private int export(JSONArray data, PrintWriter writer, WritableSheet sheet, int index)
		throws Exception
	{
		WritableCellFormat wcf = null;
//		System.err.println(titleColumns.toString(4));
        for(int k = 0;k < data.length(); k++)
        {
        	JSONObject row = data.getJSONObject(k);
            for (int i = 0, ij = 0; i < titleColumns.length(); i++)
            {
            	if( ij > 0 && writer != null ) writer.print(',');
            	JSONObject column = titleColumns.getJSONObject(i);
            	if( !column.has("dataIndx") ){
            		continue;
            	}
	        	if( column.has("exportable") && !column.getBoolean("exportable") ){
	        		continue;
	        	}
        		String dataIndx = column.getString("dataIndx");
        		Object obj = getRowValue(row, dataIndx);
        		if( obj == null ){
        			ij += 1;
        			continue;
        		}
        		String dataType = column.getString("dataType");
    			boolean noquote = super.dataTypeIsNumber(dataType);
        		if( obj != null && labelColumns != null && labelColumns.has(dataIndx) )
        		{
        			JSONObject labels = labelColumns.getJSONObject(dataIndx);
        			if( labels.has(obj.toString()) )
        			{
        				obj = labels.getString(obj.toString());
        				noquote = false;
        			}
        		}
        		
        		if( writer != null )
        		{
                	if( !noquote ) writer.print('"');
                	writer.print(obj);  
                	if( !noquote ) writer.print('"');
        		}
        		if( sheet != null )
        		{
        			WritableFont NormalFont = new WritableFont(WritableFont.createFont("微软雅黑"), 9);
                    // 用于正文居左  
                    wcf = new WritableCellFormat(NormalFont);  
                    wcf.setBorder(Border.ALL, BorderLineStyle.THIN); // 线条  
                    wcf.setVerticalAlignment(VerticalAlignment.CENTRE); // 文字垂直对齐  
                    wcf.setAlignment(Alignment.LEFT); // 文字水平对齐  
                    wcf.setWrap(true); // 文字是否换行  
    	        	if( column.has("pq_cellcls") ){
    	        		String pq_cellcls = column.getString("pq_cellcls");
    	        		wcf.setBackground(getColour(pq_cellcls));
    	        	} 
		        	if( column.has("align") && "center".equalsIgnoreCase(column.getString("align")) ){
		        		wcf.setAlignment(Alignment.CENTRE);
		        	}
            		CellView cv = new CellView();
            		cv.setAutosize(true);
            		sheet.setColumnView(ij, cv);
        			if( noquote ) sheet.addCell(new Number(ij, index, Double.parseDouble(obj.toString()), wcf));  
        			else sheet.addCell(new Label(ij, index, obj.toString(), wcf));
        		}
    			ij += 1;
            }
            index += 1;
    		if( writer != null ) writer.println();
        }
        return data.length();
	}
	
	public static Colour getColour(String strColor) {
		try{
			Color cl = Color.decode(strColor);
			Colour color = null;
			Colour[] colors = Colour.getAllColours();
			if ((colors != null) && (colors.length > 0)) {
				Colour crtColor = null;
				int[] rgb = null;
				int diff = 0;
				int minDiff = 999;
				for (int i = 0; i < colors.length; i++) {
					crtColor = colors[i];
					rgb = new int[3];
					rgb[0] = crtColor.getDefaultRGB().getRed();
					rgb[1] = crtColor.getDefaultRGB().getGreen();
					rgb[2] = crtColor.getDefaultRGB().getBlue();

					diff = Math.abs(rgb[0] - cl.getRed())
							+ Math.abs(rgb[1] - cl.getGreen())
							+ Math.abs(rgb[2] - cl.getBlue());
					if (diff < minDiff) {
						minDiff = diff;
						color = crtColor;
					}
				}
			}
			if (color == null)
				color = Colour.BLACK;
			return color;
		}
		catch(Exception e){
			e.printStackTrace();
			return Colour.WHITE;
		}
	}
	/**
	 * 数据快照生成网页展示
	 * @return
	public String snapshot()
	{
        try
        {
    		gridxml = Kit.unicode2Chr(gridxml);
    		log.info("Found the request of snapshot from gridxml="+gridxml);
            super.grid(gridxml);
            if( diggObject == null )
            {
            	super.responseException = "DIGG对象未生成，不能进行快照";
            	return "alert";
            }

            JSONArray diggs = diggObject.has("diggs")?diggObject.getJSONArray("diggs"):null;
            if( diggs == null )
            {
            	super.responseException = "DIGG对象异常，不能进行快照";
            	return "alert";
            }
            JSONArray titleColumns = diggObject.getJSONArray("titleColumns");
	//        System.err.println(sObject.toString(4));
            this.id = diggObject.has("recIndx")?diggObject.getString("recIndx"):"";
			JSONObject dataColumns = diggObject.has("dataColumns")?diggObject.getJSONObject("dataColumns"):new JSONObject();
	        gridxml = diggObject.has("gridxml")?diggObject.getString("gridxml"):"";
	        OLTP oltp = null;
	        JSONObject digg = null;
	        for(int i = 0; i < diggs.length(); i++)
	        {
	        	JSONObject e = diggs.getJSONObject(i);
	        	String type = e.getString("type");
	        	if( "sql".equalsIgnoreCase(type) )
	        	{
	        		digg = e;
	        	}
	        	else if( "mongo".equalsIgnoreCase(type) )
	        	{
	        		digg = e;
	        	}
	        	else if( "oltp".equalsIgnoreCase(type) )
	        	{
	        		String className = e.getString("class");
	        		Class<?> clz = Class.forName(className);
	        		oltp = (OLTP)clz.newInstance();
	        	}
	        }
	        if( this.diggObject.has("labels") )
	        {
	        	String json = this.diggObject.getString("labels");
	        	this.localDataObject = new JSONObject(json);
	//        	System.err.println(localDataObject.toString(4));
	        }
	        
	        StringBuffer colM = new StringBuffer("<table class='pq-grid-header-table' cellpadding='0' cellspacing='0'><tbody><tr><td style='width:31px;'></td>");
        	snapshotWidth = 31;
            for (int i = 0; i < titleColumns.length(); i++) {
            	JSONObject column = titleColumns.getJSONObject(i);
            	if( !column.has("dataIndx") ) continue;
            	int w = 100;
            	if( column.has("width") )
            		w = column.getInt("width");
            	else if( column.has("minWidth") )
            		w = column.getInt("minWidth");
            	colM.append("<td style='width:"+w+"px;'></td>");
            	snapshotWidth += w;
            }
        	colM.append("</tr><tr class='pq-grid-title-row'>");
            colM.append("<td pq-grid-col-indx='-1' class='pq-grid-number-col' rowspan='1'><div class='pq-td-div'>#</div></td>");
        	for (int i = 0; i < titleColumns.length(); i++) {
            	JSONObject column = titleColumns.getJSONObject(i);
            	if( !column.has("dataIndx") ) continue;
            	colM.append("<td pq-grid-col-indx='"+i+"' pq-col-indx='0' pq-row-indx='0' class='pq-grid-col  pq-wrap-text  pq-right-col' rowspan='1' colspan='1'>");
            	colM.append("<div class='pq-td-div'>"+column.getString("title")+"<span class='pq-col-sort-icon'>&nbsp;</span></div>");
            	colM.append("</td>");
            }
            colM.append("</tr></tbody></table>");
            

            StringBuffer dataM = new StringBuffer("<table style='margin-bottom: 0px;' class='pq-grid-table pq-grid-td-border-right pq-grid-td-border-bottom ' cellpadding='0' cellspacing='0'>" +
    				"<tbody><tr class='pq-row-hidden'><td style='width:31px;'></td>");
            for (int i = 0; i < titleColumns.length(); i++) {
            	JSONObject column = titleColumns.getJSONObject(i);
            	if( !column.has("dataIndx") ) continue;
            	if( column.has("width") )
            		dataM.append("<td style='width:"+column.getInt("width")+"px;' pq-top-col-indx='0'></td>");
            	else if( column.has("minWidth") )
            		dataM.append("<td style='width:"+column.getInt("minWidth")+"px;' pq-top-col-indx='0'></td>");
            	else//缺省的列宽
            		dataM.append("<td style='width:100px;' pq-top-col-indx='0'></td>");
            }
            dataM.append("</tr>");
            this.snapshotFromSql(titleColumns, dataColumns, digg, oltp, dataM);
	        dataM.append("</tbody></table>");
            
    		this.colModel = colM.toString();
        	this.dataModel = dataM.toString();
        	return "gridsnapshot";
		}
		catch (Exception e)
		{
			log.error("Failed to snapshot of "+gridxml+" for exception:", e);
			ByteArrayOutputStream out1 = new ByteArrayOutputStream(1024);
			PrintStream ps = new PrintStream(out1);
    		e.printStackTrace(ps);
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"数据管理",
					"执行["+gridxml+"]数据快照发生异常",
					"[<i class='fa fa-digg fa-fw'></i>] 快照数据出现异常"+e.getMessage()+"，堆栈如下所示：\r\n"+out1.toString(),
					"helper!previewxml.action?path="+gridxml,
                    "情况确认", "#feedback?to="+super.getUserAccount());
        	super.responseException = "快照数据出现异常"+e.getMessage();
    		return "alert";
		}
	}
	 */

	/**
	 * 导出数据到csv或者xls从
	 * @param digg
	 * @param oltp
	 * @param dataM
	 * @throws Exception
	private void snapshotFromSql(JSONArray titleColumns, JSONObject dataColumns, JSONObject digg, OLTP oltp, StringBuffer dataM)
		throws Exception
	{
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		try
		{
			StringBuffer sql = new StringBuffer();
			StringBuffer sqlCount = new StringBuffer();
			setSqlSelect(dataColumns, digg, sql, sqlCount, this.pq_filter);
			log.debug(sql.toString());

            Class.forName(driverClass); 
            connection = DriverManager.getConnection(jdbcUrl ,jdbcUsername, jdbcUserpswd);  
            statement = connection.createStatement();
            rs = statement.executeQuery(sqlCount.toString());
            rs.next();
            int totalRecords = rs.getInt(1);
            rs.close();
            rs = statement.executeQuery(sql.toString());
            ResultSetMetaData meta = rs.getMetaData();

//			System.err.println(titleColumns.toString(4));
            int i = 0;
            while( rs.next() )
            {
                i += 1;
            	JSONObject row = new JSONObject();
            	this.setRowData(meta, rs, row, dataColumns, digg.has("timeformat")?digg.getString("timeformat"):"");
            	if( oltp != null ) oltp.handle(row, digg);

				dataM.append("<tr pq-row-indx='"+(i)+"' class='pq-grid-row");
				if( i%2 == 1 ) dataM.append(" pq-grid-oddRow'>");
				else  dataM.append("'>");
				dataM.append("<td class='pq-grid-number-cell ui-state-default'><div class='pq-td-div'>"+i+"</div></td>");
                for (int j = 0; j < titleColumns.length(); j++)
                {
                	JSONObject column = titleColumns.getJSONObject(j);
                	if( !column.has("dataIndx") ) continue;
            		String dataIndx = column.getString("dataIndx");
            		if( !row.has(dataIndx) ) continue;
            		Object value = row.get(dataIndx);
            		if( value != null && localDataObject != null && localDataObject.has(dataIndx) )
            		{
            			JSONObject labels = localDataObject.getJSONObject(dataIndx);
            			if( labels.has(value.toString()) )
            			{
            				value = labels.getString(value.toString());
            			}
            		}
            		dataM.append("<td class='pq-grid-cell pq-wrap-text ' style='' pq-col-indx='0'>");
		        	dataM.append("<div class='pq-td-div pq-wrap-text'>"+value+"</div>");
		        	dataM.append("</td>");
                }
		        dataM.append("</tr>");
	        }
        	this.bottomInfo = "共 "+totalRecords+" 条数据，本快照显示了 "+i+" 条记录数据。";
		}
		catch (Exception e)
		{
			throw e;
		}
        finally
        {
        	if( rs != null )
				try
				{
					rs.close();
				}
				catch (SQLException e)
				{
				}
        	if( statement != null )
				try
				{
					statement.close();
				}
				catch (SQLException e)
				{
				}
        	if( connection != null )
				try
				{
					connection.close();
				}
				catch (SQLException e)
				{
				}
        }
	}
	 */

	/**
	 * 芒果数据库的聚合
	 * @param digg
	 * @return
	 */
	public void setAggregateData(Cache cache, JSONObject digg, boolean totalcount)
		throws Exception
	{
		long ts = System.currentTimeMillis();
		MongoCollection<Document> col = DiggMgr.getMongoCollection(
				digg.getString("mongo.host"), 
				digg.getInt("mongo.port"),
				digg.getString("mongo.username"),
				new String(Base64X.decode(digg.getString("mongo.password"))),
				digg.getString("mongo.database"),
				digg.getString("mongo.database1"),
				digg.getString("mongo.tablename"));
		JSONObject columns = digg.getJSONObject("columns");
		JSONObject group = digg.getJSONObject("group");
		//计算数据总量
		String _id = group.getJSONObject("_id").keySet().iterator().next().toString();
		ArrayList<BasicDBObject> aggregate = new ArrayList<BasicDBObject>();
		BasicDBObject match = null;
		if( digg.has("condition") )
		{
			match = (BasicDBObject)JSON.parse(digg.getString("condition"));
		}
		else
		{
			match = new BasicDBObject();
		}
		this.diggMgr.setMongoQueryConditions(pq_filter, match, columns);
		
		if( totalcount && totalRecords == 0 )
		{//第一个digg加载缓存
	        diggChecker.print(1, "通过数据库的distinct特性(%s)计算总条数: %s", "只支持单维度，芒果不允许", _id);
			DistinctIterable<String> result = null;
			if( !match.isEmpty() ){
				result = col.distinct(_id, String.class).filter(match);
			}
			else{
				result = col.distinct(_id, String.class);
			}
			
			MongoCursor<String> dr = result.iterator();
			while( dr.hasNext() ) {
				dr.next();
				this.totalRecords += 1;
			}

            this.diggStep += 1;
	        this.watch(1, "计算出DIGG总记录条数%s，耗时%s", totalRecords, Kit.getDurationMs(System.currentTimeMillis()-ts));
		}
		else pq_filter = "";

        diggChecker.print(1, "聚合字段: %s ", _id);
		if( totalcount ){
			if( !match.isEmpty() ){
				aggregate.add(new BasicDBObject("$match", match));
		        diggChecker.print(2, "$match: %s", match.toString());
			}
			if( totalRecords > 0 )
			{
				int skip = (curPage-1)*pageSize;
				aggregate.add(new BasicDBObject("$group", (BasicDBObject)JSON.parse(group.toString())));
				diggChecker.print(2, "$group: %s", group.toString());
				if( pq_sort != null && pq_sort.startsWith("["))
				{
					JSONArray pq_sort = new JSONArray(this.pq_sort);
					Document sort = new Document();
					for(int i = 0; i < pq_sort.length(); i++)
					{
						JSONObject e = pq_sort.getJSONObject(i);
						if( !e.has("dir") ) continue;
						String indx = e.getString("dataIndx");	
						int dir = 1;
						if( "down".equalsIgnoreCase(e.getString("dir")) || "u".equalsIgnoreCase(e.getString("dir")) )
						{
							dir = -1;
						}
						if( indx.equals(_id) ){
							indx = "_id";
						}
						sort.put(indx, dir);
					}
					diggObject.put("pq_sort", this.pq_sort);
//					super.getSession().setAttribute(referer, diggObject);
					aggregate.add(new BasicDBObject("$sort", sort));
					diggChecker.print(2, "$sort: %s", sort.toString());
				}
				aggregate.add(new BasicDBObject("$skip", skip));
				diggChecker.print(2, "$skip: %s", skip);
				aggregate.add(new BasicDBObject("$limit", pageSize));
				diggChecker.print(2, "$limit: %s", pageSize);
				String timeformat = digg.has("timeformat")?digg.getString("timeformat"):"yyyy-MM-dd HH:mm:ss";
				MongoCursor<Document> cursor = col.aggregate(aggregate).iterator();
				ArrayList<Joiner> joiners = new ArrayList<Joiner>();
				this.setJoiner(digg, joiners);
				diggChecker.print(1, "游标处理聚合数据");
				while( cursor.hasNext() ){
					Document doc = cursor.next();
					diggChecker.print(2, "数据: %s", doc.toString());
					Object d = ((Document)doc.get("_id")).get(_id);
					doc.remove("_id");
					JSONObject row = this.setMongoData(doc, timeformat);
					row.put(_id, d);
					for(Joiner joiner : joiners )
					{
						this.setJoinData(joiner, row);
					}
					cache.add(row);
				}
				cursor.close();
			}
		}
		else{
			BasicDBList ranges = new BasicDBList();
			HashMap<Object, JSONObject> map = new HashMap<Object, JSONObject>();
			for( JSONObject row : cache ){
				Object val = row.get(_id);
				ranges.add(val);
				map.put(val, row);
			}
			match.put(columns.has(_id)?columns.getJSONObject(_id).getString("dataIndx"):_id, new BasicDBObject("$in", ranges));
			diggChecker.print(2, "$range: %s", ranges.toString());
			if( !match.isEmpty() ){
				aggregate.add(new BasicDBObject("$match", match));
		        diggChecker.print(2, "$match: %s", match.toString());
			}
			aggregate.add(new BasicDBObject("$group", (BasicDBObject)JSON.parse(group.toString())));
			diggChecker.print(2, "$group: %s", group.toString());
			String timeformat = digg.has("timeformat")?digg.getString("timeformat"):"yyyy-MM-dd HH:mm:ss";
			MongoCursor<Document> cursor = col.aggregate(aggregate).iterator();
			ArrayList<Joiner> joiners = new ArrayList<Joiner>();
			this.setJoiner(digg, joiners);
			diggChecker.print(1, "游标处理聚合数据，缓存数据%s", map.size());
			while( cursor.hasNext() ){
				Document doc = cursor.next();
				diggChecker.print(2, "数据: %s", doc.toString());
				Object d = ((Document)doc.get("_id")).get(_id);
				doc.remove("_id");
				JSONObject row0 = this.setMongoData(doc, timeformat);
				for(Joiner joiner : joiners )
				{
					this.setJoinData(joiner, row0);
				}
				JSONObject row = map.get(d);
				if( row != null ){
					Iterator<?> iterator = row0.keySet().iterator();
					while(iterator.hasNext()){
						String key = iterator.next().toString();
						row.put(key, row0.get(key));
					}
				}
				else{
					diggChecker.error(2, "未关联聚合数据: %s", d.toString());
				}
			}
		}
	}
	
	/**
	 * 将数据从芒果返回对象中写入到行记录对象中
	 * @param doc
	 */
	private JSONObject setMongoData(Document doc, String timeformat)
	{
		JSONObject row = new JSONObject(doc.toJson());
		setMongoData(row, timeformat, "");
		return row;
	}
	
	/**
	 * 设置芒果数据库转换
	 * @param row
	 * @param timeformat
	 * @param parent
	 */
	private void setMongoData(JSONObject row, String timeformat, String parent)
	{
		Iterator<?> iterator = row.keys();
		while( iterator.hasNext() )
		{
			String key = iterator.next().toString();
			Object val = row.get(key);
			if( val instanceof JSONObject ){
				JSONObject obj = (JSONObject)val;
				if( obj.has("$numberLong") ){
					val = Long.parseLong(obj.getString("$numberLong"));
				}
				else {
					this.setMongoData(obj, timeformat, key+".");
				}
			}
			if( this.dataColumns.has(parent+key) )
			{
				JSONObject column = this.dataColumns.getJSONObject(parent+key);
				String dataType = column.getString("dataType");
				if( "time".equalsIgnoreCase(dataType) )
				{
        			String format = column!=null&&column.has("format")?column.getString("format"):timeformat;
					if(  val instanceof Long )
					{
						column.put("dataType0", "long");
						val = Tools.getFormatTime(format, (Long)val);
					}
					else if( val instanceof Integer )
					{
						column.put("dataType0", "integer");
						val = Tools.getFormatTime(format, (Integer)val);
					}
					else if( val instanceof Double )
					{
						Double value = (Double)val;
						column.put("dataType0", "float");
						val = Tools.getFormatTime(format, value.longValue());
					}
					else if( val instanceof String )
					{
						if( Tools.isNumeric(val.toString()))
						{
							val = Tools.getFormatTime(format, Long.parseLong(val.toString()));
						}
					}
				}
			}
			row.put(key, val);
		}
	}
	
	
	/**
	 * 条件获取Redis数据
	 * @param jedis
	 * @param table
	 * @param down
	 * @return
	 */
	private Set<String> getReidsDataByConditions(Jedis jedis, String table, boolean down){

		BasicDBObject filter = (BasicDBObject)JSON.parse(pq_filter);
		BasicDBList array = (BasicDBList)filter.get("data");
		if( array.size() > 0 )
		{//查询数据涉及中文，Unicode解码先
			BasicDBObject condition = (BasicDBObject)array.get(0);
			String name = condition.getString("dataIndx");
//			Object value1;
//			Object value2;
			String value = condition.getString("value");
//			String dataType = condition.getString("dataType");
			if( !name.equals(id) ){
				table += "."+name;
			}
			if( condition.getString("condition").equals("contain") )
			{
//				if( down ){
//					return jedis.zrevrangeByLex(table, "["+value, "-");
//				}
//				else{
//					return jedis.zrangeByLex(table, "["+value, "+");
//				}
			}
			else if( condition.getString("condition").equals("begin") )
			{
//				if( down ){
//					return jedis.zrevrangeByLex(table, "["+value, "-");
//				}
//				else{
//					return jedis.zrangeByLex(table, "["+value, "+");
//				}
			}
			else if( condition.getString("condition").equals("range") )
			{
//				BasicDBList ranges = (BasicDBList)condition.get("value");
			}
			else if( condition.getString("condition").equals("equal") )
			{
				if( down ){
					return jedis.zrevrangeByLex(table, "["+value, "-", 0, 1);
				}
				else{
					return jedis.zrangeByLex(table, "["+value, "+", 0, 1);
				}
			}
			else if( condition.getString("condition").equals("lte") || condition.getString("condition").equals("gte") )
			{
			}
			else if( condition.getString("condition").equals("between") )
			{
//				value1 = condition.get("value");
//				value2 = condition.get("value2");
//				if( down ){
//					return jedis.zrevrangeByLex(table, "["+value2, "["+value1);
//				}
//				else{
//					return jedis.zrangeByLex(table, "["+value1, "["+value2);
//				}
			}
		}
		return null;
	}
	/**
	 * Redis内存数据库查询
	 * @param cache
	 * @param digg
	 */
	public void setRedisData(Cache cache, JSONObject digg)
			throws Exception
	{
		long ts = System.currentTimeMillis();
		Jedis jedis = new Jedis(digg.getString("redis.host"), digg.getInt("redis.port"));
		if( digg.has("redis.password") && !digg.getString("redis.password").isEmpty() ){
			jedis.auth(new String(Base64X.decode(digg.getString("redis.password"))));
		}
    	diggObject.put("pq_filter", pq_filter);
//    	if( referer != null ) super.getSession().setAttribute(referer, diggObject);
    	try
    	{
//    		System.out.println(id);
//    		System.out.println(pq_filter);
    		this.diggChecker.print(1, "计算DIGG总记录条数，条件是%s", pq_filter);
    		String table = digg.getString("table");
    		String index = "";
    		Set<String> result = null;
    		boolean down = false;
			if( pq_sort != null && pq_sort.startsWith("["))
			{
				JSONArray sort = new JSONArray(pq_sort);
				if(sort.length()>0)
				{
					JSONObject e = sort.getJSONObject(0);
					index = "."+e.getString("dataIndx");
					if( e.has("dir") ){
						if( "down".equalsIgnoreCase(e.getString("dir")) || "u".equalsIgnoreCase(e.getString("dir")) )
						{
							down = true;
						}
					}
				}
                diggObject.put("pq_sort", pq_sort);
//                if( referer != null ) super.getSession().setAttribute(referer, diggObject);
    	        this.watch(1, "执行DIGG，排序参数%s", pq_sort);
			}
    		if( pq_filter != null && pq_filter.startsWith("{"))
    		{
    			result = this.getReidsDataByConditions(jedis, table, down);
    			if( result != null ){
    				totalRecords = result.size();
    				this.diggChecker.print(1, "条件查询DIGG总记录条数%s", totalRecords);
    			}
    		}
    		else{
				totalRecords = jedis.zcard(table);
				this.diggStep += 1;
				this.watch(1, "计算出DIGG总记录条数%s，耗时%s，查询索引表%s", 
					totalRecords, Kit.getDurationMs(System.currentTimeMillis()-ts), index);
				if( totalRecords > 0 ){
		    		String max = "+";
		    		String min = "-";
					int skip = (curPage-1)*pageSize;
					if( index.isEmpty() ) {
						result = jedis.zrangeByLex(table, min, max, skip, pageSize);
					}
					else if( down ){
						result = jedis.zrevrangeByLex(table+index, max, min, skip, pageSize);
					}
					else{
						result = jedis.zrangeByLex(table+index, min, max, skip, pageSize);
					}
				}
    		}
			if( result != null && !result.isEmpty() ) {
//				String timeformat = digg.has("timeformat")?digg.getString("timeformat"):"yyyy-MM-dd HH:mm:ss";
				Iterator<String> iterator = result.iterator();
				ArrayList<Joiner> joiners = new ArrayList<Joiner>();
				this.setJoiner(digg, joiners);
				while(iterator.hasNext())
				{
					String key = iterator.next();
					key = key.substring(key.lastIndexOf(' ')+1);
					String val = jedis.get(key);
					System.err.print(key+"=");
					System.err.println(val);
					JSONObject row = new JSONObject();
					if( val != null && !val.isEmpty())
					{
						row = new JSONObject(val);
					}
	            	this.diggChecker.print(2, "数据: %s", row.toString());
					for(Joiner joiner : joiners )
					{
						this.setJoinData(joiner, row);
					}
					cache.add(row);
				}
				for(Joiner joiner : joiners )
				{
					joiner.close();
				}
			}
    	}
    	catch(Exception e)
    	{
    		throw e;
    	}
    	finally{
    		jedis.close();
    	}
	}
	/**
	 * 芒果对象查询
	 * @return
	 */
	public void setMongoData(Cache cache, JSONObject digg)
		throws Exception
	{
		long ts = System.currentTimeMillis();
		BasicDBObject where = null;
		if( digg.has("condition") )
		{
			String json = digg.getString("condition");
			where = (BasicDBObject)JSON.parse(json);
		}
		else
		{
			where = new BasicDBObject();
		}
		MongoCollection<Document> col = null;
		if( digg.has("dynamicParameter") ){
			String dynamicParameter = digg.getString("dynamicParameter");
			String args[] = Tools.split(dynamicParameter, ";");
			for(String arg : args){
				String keys[] = Tools.split(arg, "::");
				String leftkey = keys[0];
				String rightkey = keys[1];
				String dataType = keys[2];
				String val = super.getRequest().getParameter(leftkey);
				String values[] = Tools.split(val, ",");
				if( val == null || val.isEmpty() ){
					continue;
				}
				if( values != null && values.length > 0 ){
					BasicDBList ranges = new BasicDBList();
					for(int i = 0; i < values.length; i++ ){
						if( !dataType.equalsIgnoreCase("string") )
						{
							String n = values[i];
							if( n != null && Tools.isNumeric(n))
							{
								if(n.indexOf('.') != -1) ranges.add(Integer.parseInt(n));
								else ranges.add(Double.parseDouble(n));
							}
						}
						else{
							ranges.add(values[i]);
						}
					}
					where.put(rightkey, new BasicDBObject("$in", ranges));
				}
				else{
					if( !dataType.equalsIgnoreCase("string") )
					{
						String n = val.toString();
						if( n != null && Tools.isNumeric(n))
						{
							if(n.indexOf('.') != -1) where.put(rightkey, Integer.parseInt(n));
							else where.put(rightkey, Double.parseDouble(n));
						}
					}
					else{
						where.put(rightkey, val);
					}
				}
			}
		}
		col = DiggMgr.getMongoCollection(
			digg.getString("mongo.host"), 
			digg.getInt("mongo.port"),
			digg.getString("mongo.username"),
			new String(Base64X.decode(digg.getString("mongo.password"))),
			digg.getString("mongo.database"),
			digg.getString("mongo.database1"),
			digg.getString("mongo.tablename")
		);
		diggMgr.setMongoQueryConditions(pq_filter, where, dataColumns);
    	diggObject.put("pq_filter", pq_filter);
//    	if( referer != null ) super.getSession().setAttribute(referer, diggObject);
    	try
    	{
    		this.diggChecker.print(1, "计算DIGG总记录条数，条件是%s", where.toString());
			if( where.isEmpty() ){
				totalRecords = col.count();
			}
			else
			{
				totalRecords = col.count(where);
			}
			this.diggStep += 1;
	        this.watch(1, "计算出DIGG总记录条数%s，耗时%s", totalRecords, Kit.getDurationMs(System.currentTimeMillis()-ts));
	        
			FindIterable<Document> find = null;
			if( where.isEmpty() )
			{
				find = col.find();
			}
			else
			{
				find = col.find(where);
			}
			
			if( totalRecords > 0 )
			{
				int skip = (curPage-1)*pageSize;
	//			String pq_sort = request.getParameter("pq_sort");
				if( pq_sort != null && pq_sort.startsWith("["))
				{
					JSONArray sort = new JSONArray(pq_sort);
					for(int i = 0; i < sort.length(); i++)
					{
						JSONObject e = sort.getJSONObject(i);
						if( !e.has("dir") ) continue;
						String indx = e.getString("dataIndx");	
						int dir = 1;
						if( "down".equalsIgnoreCase(e.getString("dir")) || "u".equalsIgnoreCase(e.getString("dir")) )
						{
							dir = -1;
						}
						find = find.sort(new BasicDBObject(indx, dir));
					}
	                diggObject.put("pq_sort", pq_sort);
//	                if( referer != null ) super.getSession().setAttribute(referer, diggObject);
	    	        this.watch(1, "执行DIGG，排序参数%s", pq_sort);
				}
				else
				{
					find = find.sort(new BasicDBObject("_id", -1));
				}
				find = find.skip(skip).limit(pageSize);
			}
			if( totalRecords > 0 )
			{
				String timeformat = digg.has("timeformat")?digg.getString("timeformat"):"yyyy-MM-dd HH:mm:ss";
				MongoCursor<Document> cursor = find.iterator();
				ArrayList<Joiner> joiners = new ArrayList<Joiner>();
				this.setJoiner(digg, joiners);
				while(cursor.hasNext())
				{
					Document doc = cursor.next();
	            	this.diggChecker.print(2, "芒果数据: %s", doc.toJson());
					Object _id = doc.remove("_id");
					doc.put("_id", _id.toString());
					JSONObject row = this.setMongoData(doc, timeformat);
					for(Joiner joiner : joiners )
					{
						this.setJoinData(joiner, row);
					}
					cache.add(row);
				}
				for(Joiner joiner : joiners )
				{
					joiner.close();
				}
			}
    	}
    	catch(Exception e)
    	{
    		log.error("Failed to set the data of mongodb from "+digg.toString(4)+"for "+e);
    		DiggMgr.release(digg.getString("mongo.username"), digg.getInt("mongo.port"), digg.getString("mongo.database"));
    		throw e;
    	}
//		System.err.println(data.toString(4));
	}
	/**
	 * 设置SQL的排序
	 * @param request
	 * @param digg
	 * @param sql
	 */
	private void setSqlOrder(JSONObject digg, StringBuffer sql)
	{
		if( pq_sort == null || pq_sort.isEmpty() )
		{
			pq_sort = digg.has("pq_sort")?digg.getString("pq_sort"):"";
		}
		if( pq_sort.startsWith("[") && pq_sort.endsWith("]") )
		{
			JSONArray sort = new JSONArray(pq_sort);
			if( sort.length() > 0 )
			{
				sql.append(" order by ");
				for(int i = 0; i < sort.length(); i++)
				{
					JSONObject e = sort.getJSONObject(i);
					if( !e.has("dir") ) continue;
					String indx = e.getString("dataIndx");	
					String field = indx;
//					if( dataColumns != null && dataColumns.has(indx) ){
//						field = dataColumns.getJSONObject(indx).getString("column");
//					}
					if( i > 0 ) sql.append(",");
					sql.append(" ");
					sql.append(field);
					if( "down".equalsIgnoreCase(e.getString("dir")) || "u".equalsIgnoreCase(e.getString("dir")) )
						sql.append(" desc");
				}
			}
        	if( referer != null ){
                diggObject.put("pq_sort", pq_sort);
//                if( referer != null ) super.getSession().setAttribute(referer, diggObject);
        	}
		}
	}
	/**
	 * 设置查询的SQL预计
	 * @param request
	 * @param sql
	 * @param sqlCount
	 * @return
	 * @throws Exception
	 */
	private void setSqlSelect(JSONObject digg, StringBuffer sql, StringBuffer sqlCount, String pq_filter) throws Exception
	{
		db = digg.has("dbtype")?digg.getString("dbtype"):"h2";
    	jdbcUrl = digg.getString("jdbc.url");
    	jdbcUsername = digg.getString("jdbc.username");
    	jdbcUserpswd = new String(Base64X.decode(digg.getString("jdbc.password")));
    	driverClass = digg.getString("jdbc.driverClass");
		if( jdbcUserpswd == null ) jdbcUserpswd = "";
		if( jdbcUrl == null || jdbcUrl.isEmpty() || jdbcUsername == null || jdbcUsername.isEmpty() || driverClass == "" || driverClass.isEmpty() )
		{
			throw new Exception("因为jdbc配置连接参数无效执行SQL脚本失败");
		}
		sql.append(digg.getString("sql"));
		if( sqlCount != null ){
			if( digg.has("sqlCount") ){
				sqlCount.append(digg.getString("sqlCount"));
			}
			else{
				int i = sql.toString().toLowerCase().indexOf(" from ");
				if( i == -1 )
				{
					throw new Exception("Not a correct format of SQL.");
				}
				sqlCount.append("select count(1)"+sql.toString().substring(i));
			}
		}

		if(digg.has("dynamicParameter")){
			String _sql = sql.toString().toLowerCase();
			if( _sql.indexOf("where") == -1 ){
				sql.append(" WHERE 1=1");
				sqlCount.append(" WHERE 1=1");
			}
			String dynamicParameter = digg.getString("dynamicParameter");
			String args[] = Tools.split(dynamicParameter, ";");
			for(String arg : args){
				String keys[] = Tools.split(arg, "::");
				String leftkey = keys[0];
				String rightkey = keys[1];
				String dataType = keys[2];
				String val = super.getRequest().getParameter(leftkey);
				if( !dataType.equalsIgnoreCase("string") &&
					!dataType.equalsIgnoreCase("time"))
				{
					String n = val.toString();
					if( n != null && Tools.isNumeric(n))
					{
						sql.append(" AND "+rightkey+"="+val);
						sqlCount.append(" AND "+rightkey+"="+val+"");
					}
				}
				else{
					sql.append(" AND "+rightkey+"='"+val+"'");
					sqlCount.append(" AND "+rightkey+"='"+val+"'");
				}
			}				
		}
//		String pq_filter = request.getParameter("pq_filter");
        DiggMgr.setQueryConditions(pq_filter, dataColumns, digg.has("indx")?digg.getString("indx"):"0", sql);
        DiggMgr.setQueryConditions(pq_filter, dataColumns, digg.has("indx")?digg.getString("indx"):"0", sqlCount);

		if( sqlCount != null && sqlCount.indexOf("distinct") != -1 && "h2".equals(db) )
		{
			sqlCount.insert(0, "select count(1) from (");
			sqlCount.append(") tmp");
		}
		
        if( pq_filter != null && !pq_filter.isEmpty() )
        {
	    	diggObject.put("pq_filter", pq_filter);
//	    	if( referer != null ) super.getSession().setAttribute(referer, diggObject);
        }
//        else{
//	    	super.getSession().removeAttribute(referer);
//        }
	}

	/**
	 * 设置行数据
	 * @param meta
	 * @param rs
	 * @param row
	 * @param dataColumns
	 * @throws Exception
	 */
	private void setSQLRowData(ResultSetMetaData meta, ResultSet rs, JSONObject row, String timeformat)
		throws Exception
	{
		timeformat = timeformat==null||timeformat.isEmpty()?"yyyy-MM-dd HH:mm:ss":timeformat;
        String value = null;
        for(int i = 1; i <= meta.getColumnCount(); i++ )
        {
        	int type = meta.getColumnType(i);
        	String colname = meta.getColumnLabel(i);
        	JSONObject cell = null;
			if( dataColumns != null && dataColumns.has(colname) )
			{
				cell = dataColumns.getJSONObject(colname);
			}
			else{
				colname = dataColumnsUpper.containsKey(colname)?dataColumnsUpper.get(colname):colname;
			}
			String format = "";
        	switch( type )
        	{
        		case java.sql.Types.CLOB:
        		case java.sql.Types.BLOB:
            		String content = rs.getString(i);
            		if( content != null && content.length() > 0 )
            		{
            			row.put(colname, "展开查看详情");
            			content = Tools.replaceStr(content, "<", "&lt;");
            			content = Tools.replaceStr(content, ">", "&gt;");
            		}
            		else
            		{
            			row.put(colname, "N/A");
            		}
        			row.put(colname, content);
        			break;
        		case java.sql.Types.TIMESTAMP:
        			format = cell!=null&&cell.has("format")?cell.getString("format"):timeformat;
        			Timestamp ts = rs.getTimestamp(i);
        			if( ts != null )
        			{
        				row.put(colname, Tools.getFormatTime(format, ts.getTime()));
        			}
        			else
        			{
        				row.put(colname, "");
        			}
        			break;
        		case java.sql.Types.DATE:
        			format = cell!=null&&cell.has("format")?cell.getString("format"):timeformat;
        			Date date = rs.getDate(i);
        			if( date != null ) {
            			row.put(colname, Tools.getFormatTime(format, date.getTime()));
        			}
        			else {
        				row.put(colname, "");
        			}
        			break;
        		case java.sql.Types.TIME:
        			format = cell!=null&&cell.has("format")?cell.getString("format"):timeformat;
        			Time time = rs.getTime(i);
        			if( time != null ) {
                		row.put(colname, Tools.getFormatTime(format, time.getTime()));
        			}
        			else {
        				row.put(colname, "");
        			}
        			break;
        		case java.sql.Types.BOOLEAN:
            		row.put(colname, rs.getBoolean(i));
        			break;
        		case java.sql.Types.BIGINT:
            		row.put(colname, rs.getLong(i));
        			break;
        		case java.sql.Types.DOUBLE:
        			row.put(colname, rs.getDouble(i));
        			break;
        		case java.sql.Types.DECIMAL:
        		case java.sql.Types.NUMERIC:
        		case java.sql.Types.FLOAT:
            		row.put(colname, rs.getFloat(i));
        			break;
        		case java.sql.Types.CHAR:
            		row.put(colname, rs.getString(i));
            		break;
        		case java.sql.Types.INTEGER:
            		row.put(colname, rs.getInt(i));
        			break;
        		case java.sql.Types.NULL:
            		row.put(colname, rs.getString(i));
//        			break;
        		case java.sql.Types.VARCHAR:
        			value = rs.getString(i);
        			if( value != null )
        			{
            			value = value.replaceAll("<", "&lt;");
            			value = value.replaceAll(">", "&gt;");
        			}
            		row.put(colname, value);
        			break;
        		default:// java.sql.Types.VARCHAR:
            		row.put(colname, rs.getString(i));
        			break;
        	}	            	
        } 
	}
	
	/**
	 * 设置Join引擎
	 * @param dataColumns
	 * @param joiners
	 * @throws Exception
	 */
	class Joiner
	{
		String leftkey;//映射主表的字段
		String rightkey;//映射父表的字段 
		String srctype;
		MongoCollection<Document> col;
		String objname;//芒果结果下映射的对象名
		Statement stmt;
		Connection con;
		String sql;//sql数据库数据类型
		String from;
//		JSONObject dataColumns;
		String timeformat;
		JSONObject data = new JSONObject();
		boolean filtermine;
		boolean filterbefore;
		boolean more;
		boolean setrow;
		String setcol;
		JSONObject filter;
		JSONObject condition;
		JSONObject group;
		JSONObject id;
		String sort;
		String dir;
		JSONObject config;
		
		public void close()
		{
			if( stmt != null )
				try {
					stmt.close();
				} catch (SQLException e1) {
				}
			if( con != null )
				try {
					con.close();
				} catch (SQLException e) {
				}
		}
	}
	/**
	 * 加载连接配置
	 * @param digg
	 * @param joiners
	 * @throws Exception
	 */
	private void setJoiner(JSONObject digg, ArrayList<Joiner> joiners)
		throws Exception
	{
		JSONArray joins = digg.has("joins")?digg.getJSONArray("joins"):null;
		if( joins == null ) return;
		JSONObject join = null;
		try
		{
	        diggChecker.print(1, "需要执行 %s条左连接查询", joins.length());
	    	for(int i = 0; i < joins.length(); i++ )
	    	{
				Joiner joiner = new Joiner();
				joiner.config = joins.getJSONObject(i);
				join = joiner.config;
//				joiner.dataColumns = dataColumns;
				joiner.leftkey = joiner.config.getString("leftkey");
				if( !dataColumns.has(joiner.leftkey) ) continue;
				JSONObject dataColumn = dataColumns.getJSONObject(joiner.leftkey);
				String src = joiner.config.getString("src");
	        	presetDatabase(src, joiner.config);
				joiner.rightkey = joiner.config.getString("rightkey");
				joiner.srctype = db;
				joiner.from = joiner.config.getString("from");
				joiner.more = joiner.config.has("more")?joiner.config.getBoolean("more"):false;
				joiner.setrow = joiner.config.has("setrow")?joiner.config.getBoolean("setrow"):false;
				joiner.setcol = joiner.config.has("setcol")?joiner.config.getString("setcol"):"";
				joiner.objname = joiner.config.has("object")?joiner.config.getString("object"):joiner.from;
				if( "mongo".equalsIgnoreCase(db) )
				{
					int c = Tools.countChar(joiner.from, '%');
					if( c%2 != 0 ){
						MongoCollection<Document> col = DiggMgr.getMongoCollection(
								joiner.config.getString("mongo.host"), 
								joiner.config.getInt("mongo.port"),
								joiner.config.getString("mongo.username"),
								new String(Base64X.decode(joiner.config.getString("mongo.password"))),
								joiner.config.getString("mongo.database"),
								joiner.config.has("database")?joiner.config.getString("database"):joiner.config.getString("mongo.database"),
										joiner.from);
						joiner.col = col;
					}
					if( joiner.config.has("condition") )
					{
						joiner.condition = joiner.config.getJSONObject("condition");
					}
					if( joiner.config.has("group") )
					{
						joiner.group = joiner.config.getJSONObject("group");
					}
					if( joiner.config.has("sort") )
					{
						joiner.sort = joiner.config.getString("sort");
					}
					if( joiner.config.has("dir") )
					{
						joiner.dir = joiner.config.getString("dir");
					}
				}
				else if( "redis".equalsIgnoreCase(db) )
				{
					
				}
				else// if( "sql".equalsIgnoreCase(db) )
				{
					boolean end = false;
					String datatype = dataColumn.getString("dataType");
					StringBuffer sql = new StringBuffer();
					if( joiner.config.has("group") )
					{
						joiner.group = joiner.config.getJSONObject("group");
					}
					if( joiner.group != null ){
						sql.append("select ");
						JSONArray columns = joiner.group.getJSONArray("columns");
						for( int j = 0; j < columns.length(); j++ ){
							JSONObject column = columns.getJSONObject(j);
							String as = column.getString("as");
							String clause = column.getString("clause");
							if( j > 0 ) sql.append(",");
							sql.append(clause);
							sql.append(" as ");
							sql.append(as);
						}
						sql.append(" from ");
						sql.append(joiner.config.getString("from"));
						sql.append(" where 1=1");
					}
					else if( joiner.config.has("count") ){
						JSONObject count = joiner.config.getJSONObject("count");
						String dbtype = joiner.config.has("dbtype") ? joiner.config.getString("dbtype") : "";
						if ("mysql".equalsIgnoreCase(dbtype)) {
							sql.append("select count(" + count.getString("clause") + ") as " + count.getString("as") + " from ");
						}
						else{
							end = true;
							sql.append("select count(1) as " + count.getString("as") + " from(");
							sql.append("select " + count.getString("clause") + " from ");
						}
						sql.append(joiner.config.getString("from"));
						sql.append(" where 1=1");
					}
					else if( joiner.config.has("sum") ){
						JSONObject sum = joiner.config.getJSONObject("sum");
						sql.append("select sum(" + sum.getString("clause") + ") as " + sum.getString("as") + " from ");
						sql.append(joiner.config.getString("from"));
						sql.append(" where 1=1");
					}
					else{
						sql.append("select * from "+joiner.config.getString("from")+" where 1=1");
					}
					if( joiner.rightkey != null && !joiner.rightkey.isEmpty() ){
						sql.append(" and (");
						if( DataModel.quote(datatype) )
							sql.append(joiner.rightkey+"='%"+joiner.leftkey+"%'");
						else
							sql.append(joiner.rightkey+"=%"+joiner.leftkey+"%");
						sql.append("%ISNULL%)");
					}

					if( joiner.config.has("id") ){
						joiner.id = joiner.config.getJSONObject("id");
					}
					
					if (joiner.config.has("where")) {
						sql.append(" and " + joiner.config.getString("where"));
					}
					if( joiner.config.has("condition") )
					{
						joiner.condition = joiner.config.getJSONObject("condition");
						this.setJoinSqlWhere(joiner.condition, dataColumns, sql);
					}
		            if (end) {
		                sql.append(")");
		            }
					Class.forName(driverClass);
					joiner.sql = sql.toString();
					if( joiner.config.has("sort") )
					{
						joiner.sort = joiner.config.getString("sort");
					}
					if( joiner.config.has("dir") )
					{
						joiner.dir = joiner.config.getString("dir");
					}

					if( joiner.group != null )
					{
						joiner.sql += " group by "+joiner.group.getString("value");
					}
					joiner.con = DriverManager.getConnection(jdbcUrl ,jdbcUsername, jdbcUserpswd); 
				}
				if( joiner.config.has("filter") )
				{
					joiner.filter = joiner.config.getJSONObject("filter");
					joiner.filtermine = joiner.filter.has("type")?"mine".equalsIgnoreCase(joiner.filter.getString("type")):false;
					joiner.filterbefore = joiner.filter.has("callback")?"before".equalsIgnoreCase(joiner.filter.getString("callback")):false;
				}
				if(!joiner.filtermine){
					joiner.filtermine = joiner.config.has("skip")?joiner.config.getBoolean("skip"):false;
				}
				if( joiner.config.has("timeformat") )
				{
					joiner.timeformat = joiner.config.getString("timeformat");
				}
				joiners.add(joiner);
	    	}
		}
		catch(Exception e)
		{
			throw new Exception("Failed to set the joiner"+join.toString(4), e);
		}
	}

	/**
	 * 过滤多条join数据
	 * @param rows
	 * @param filter
	 * @param result
	 * @return true 表示数据有效正确符合
	 */
	private boolean filterJoinMore(JSONArray rows, Joiner joiner, JSONObject row)
		throws Exception
	{
		String[] args = null;
		if( !joiner.setcol.isEmpty() ){
			args = Tools.split(joiner.setcol, ",");
		}
		HashMap<String, JSONArray> values = new HashMap<String, JSONArray>();
		for(int i = 0; i < rows.length(); i ++)
		{
			JSONObject joinRow = rows.getJSONObject(i);
			if( joiner.filter != null && !joiner.filterbefore ){
				if( !this.filterJoinData(joinRow, joiner.filter) ){
					rows.remove(i);
					i -= 1;
					continue;
				}
			}
			if( args != null ){
				for( String col : args ){
					String[] keyval = Tools.split(col, ":");
					if( keyval.length == 1 ){
						JSONArray array = values.get(col);
						if( array == null ){
							array = new JSONArray();
							values.put(col, array);
						}
						if( row.has(col) ){
							array.put(joinRow.get(col));
						}
					}
					else if( keyval.length == 2 ){
						JSONArray array = values.get(keyval[1]);
						if( array == null ){
							array = new JSONArray();
							values.put(keyval[1], array);
						}
						if( joinRow.has(keyval[0]) ){
							array.put(joinRow.get(keyval[0]));
						}
					}
				}
			}
		}
		if( !values.isEmpty() ){
			Iterator<String> iterator = values.keySet().iterator();
			while(iterator.hasNext()){
				String key = iterator.next();
				JSONArray array = values.get(key);
				row.put(key, array);
				diggChecker.print(5, "设置主列数据: %s %s", key, array.toString());
			}
		}
		return rows.length()>0;
	}
	/**
	 * 
	 * @param row
	 * @param filter
	 * @param result
	 * @return true 表示数据有效正确符合
	 */
	private boolean filterJoinData(JSONObject row, JSONObject filter)
		throws Exception
	{
		boolean b = false;
		String compound = "";
		try
		{
			String value = filter.getString("value");
			if( !row.has(filter.getString("column")) ) return false;
			String value1 = row.get(filter.getString("column")).toString();
			if( "=".equals(filter.getString("method")) )
			{
				b = value1.equals(value);
			}
			else if( "<>".equals(filter.getString("method")) )
			{
				b = !value1.equals(value);
			}
			compound = filter.getString("compound");
			if( "and".equalsIgnoreCase(compound) && !b )
			{
				return false;
			}
		}
		catch(Exception e)
		{
			throw new Exception("Failed to filter by"+filter.toString(4)+"\r\n\tThe data of row is "+row.toString(4));
		}
		
		if( filter.has("children") )
		{
			JSONArray children = filter.getJSONArray("children");
			for(int i = 0; i < children.length(); i++)
			{
				JSONObject child = children.getJSONObject(i);
				if( "and".equalsIgnoreCase(compound) && !b )
				{
					b = b & this.filterJoinData(row, child);
				}
				else
				{
					b = b | this.filterJoinData(row, child);
				}
				if( !b ) return false;
			}
		}
		return b;
	}
	/**
	 * 设置非more模式的Join数据
	 * @param joiner
	 * @param row
	 * @param val
	 * @return
	 * @throws Exception
	 */
	private boolean setJoinDataNomore(Joiner joiner, JSONObject row, Object val)
		throws Exception
	{
		Exception _e = null;
		JSONObject joinData = null;
		if( joiner.data != null && joiner.data.has(val.toString()) && joiner.sql != null && joiner.sql.indexOf("count(") == -1 )
		{
			joinData = joiner.data.getJSONObject(val.toString());
		}
		else
		{
			joinData = new JSONObject();
			if( "mongo".equalsIgnoreCase(joiner.srctype) ){
				try {
					MongoCollection<Document> col = joiner.col;
					if( col == null ){
						diggChecker.print(4, "[0]JOIN.mongo: %s@%s", joiner.from, joiner.config.getString("mongo.database"));
						String from = parameter(joiner.from, row, '%');
						diggChecker.print(4, "[1]JOIN.mongo: %s@%s", from, joiner.config.getString("mongo.database"));
						col = DiggMgr.getMongoCollection(
							joiner.config.getString("mongo.host"), 
							joiner.config.getInt("mongo.port"),
							joiner.config.getString("mongo.username"),
							new String(Base64X.decode(joiner.config.getString("mongo.password"))),
							joiner.config.getString("mongo.database"),
							joiner.config.has("database")?joiner.config.getString("database"):joiner.config.getString("mongo.database"),
							from);
					}
					BasicDBObject match = new BasicDBObject();
//					if( joiner.leftkey.equalsIgnoreCase("childrenPartner") ){
//						System.err.println(row.toString(4));
//					}
					if( joiner.condition != null ) {
						diggChecker.print(4, "[0]JOIN.match: %s", joiner.condition.toString());
						this.setJoinMongoWhere(joiner.condition, dataColumns, match, row);
					}
					if( joiner.group == null ){
						if( !joiner.rightkey.isEmpty() ){
							match.put(joiner.rightkey, val);
						}
						long count = col.count(match);
						joinData.put("_count", count);
						JSONObject match1 = new JSONObject(match.toString());
						diggChecker.print(4, "[1]JOIN.match: %s", match1.toString());
						FindIterable<Document> find = col.find(match);
						if( joiner.sort != null && !joiner.sort.isEmpty() ){
							find = find.sort(Filters.eq(joiner.sort, "down".equalsIgnoreCase(joiner.dir)?-1:1));
						}
						else{
							find = find.sort(new BasicDBObject("_id", -1));
						}
						find = find.limit(1);
						MongoCursor<Document> cursor = find.iterator();
						if( cursor.hasNext() )
						{
							Document doc = cursor.next();
							Kit.merge(joinData, doc);
							joiner.data.put(val.toString(), joinData);
						}
					}
					else {
						if( val instanceof JSONArray ){
							match.put(joiner.rightkey, new BasicDBObject("$in", JSON.parse(val.toString())));
						}
						else{
							match.put(joiner.rightkey, val);
						}
						ArrayList<BasicDBObject> list = new ArrayList<BasicDBObject>();
						list.add(new BasicDBObject("$match", match));
						BasicDBObject group = new BasicDBObject();
						String value = joiner.group.getString("value");
						String[] args = Tools.split(value, ",");
						BasicDBObject groupfileds = new BasicDBObject();
						for(String arg:args)
						{
							groupfileds.put(arg, "$"+arg);
							
						}
						group.put("_id", groupfileds);
						JSONArray counts = joiner.group.getJSONArray("counts");
						for(int i = 0; i < counts.length(); i++)
						{
							JSONObject count = counts.getJSONObject(i);
							String name = count.getString("name");
							String function = count.getString("function");
							String parameter = count.getString("parameter");
							group.put(name, new BasicDBObject(function, Tools.isNumeric(parameter)?Integer.parseInt(parameter):parameter));
						}
						list.add(new BasicDBObject("$group", group));
						JSONArray group1 = new JSONArray(list.toString());
						diggChecker.print(4, "[1]执行JOIN.aggregate: %s", group1.toString());
						MongoCursor<Document> cursor = col.aggregate(list).iterator();
						int c = 0;
						while( cursor.hasNext() )
						{
							Document doc = cursor.next();
							Document _id = (Document)doc.get("_id");
							Iterator<String> iterator = _id.keySet().iterator();
							StringBuffer key = new StringBuffer();
							while(iterator.hasNext())
							{
								String k = iterator.next();
								key.append(_id.getString(k).toString());
							}
							for(int i = 0; i < counts.length(); i++)
							{
								JSONObject count = counts.getJSONObject(i);
								String name = count.getString("name");
								Object result = doc.get(name);//统计结果
								if( joinData.has(name) )
								{
									Double sum = (Double)joinData.get(name);
									if( result instanceof Double )
									{
										sum += (Double)result;
									}
									else if( result instanceof Long )
									{
										sum += (Long)result;
									}
									else if( result instanceof Integer )
									{
										sum += (Integer)result;
									}
									joinData.put(name, sum);
								}
								else
								{
									joinData.put(name, Double.valueOf(result.toString()));
								}
								name = key+"_"+name;
								joinData.put(name, result);
							}
							c += 1;
						}
						joinData.put("_count", c);
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			else if( "redis".equalsIgnoreCase(joiner.srctype) )
			{
				val = Tools.replaceStr(joiner.rightkey, "%"+joiner.leftkey+"%", val.toString());
				Jedis jedis = null;
				try{
					jedis = new Jedis(joiner.config.getString("redis.host"), joiner.config.getInt("redis.port"));
					if( joiner.config.has("redis.password") && !joiner.config.getString("redis.password").isEmpty() ){
						jedis.auth(new String(Base64X.decode(joiner.config.getString("redis.password"))));
					}
					String json = jedis.get(val.toString());
					if( json != null ){
						joinData = new JSONObject(json); 
					}
				}
				catch(Exception e){
					_e = e;
				}
				finally {
					jedis.close();
				}
			}
			else
			{
				ResultSet rs = null;
				try
				{
					
					this.sql = Tools.replaceStr(joiner.sql, "%"+joiner.leftkey+"%", val.toString());
					if( val.toString().isEmpty() ){
						this.sql = Tools.replaceStr(this.sql, "%ISNULL%", " OR ISNULL("+joiner.rightkey+")");
					}
					else{
						this.sql = Tools.replaceStr(this.sql, "%ISNULL%", "");
					}
					this.sql = parameter(this.sql, row, '%');
					diggChecker.print(4, "[1]执行SQL: %s", this.sql);
					if( joiner.sort != null && !joiner.sort.isEmpty() ){
						this.sql += " order by " + joiner.sort;
						if( "down".equalsIgnoreCase(joiner.dir) ){
							this.sql += " desc";
						}
					}
					if( joiner.stmt == null ) joiner.stmt = joiner.con.createStatement();
					rs = joiner.stmt.executeQuery(this.sql);
					ResultSetMetaData meta = rs.getMetaData();
					if( rs.next() )
					{
						this.setSQLRowData(meta, rs, joinData, joiner.timeformat);
						if( joiner.condition == null ){
							joiner.data.put(val.toString(), joinData);
						}
					}
				}
				catch(Exception e)
				{
					_e = e;
				}
				finally{
					if( rs != null ){
						rs.close();
					}
				}
			}
		}
		
		if( joinData.length() > 0 )
		{
			if( joiner.filter != null && !joiner.filterbefore )
			{
				boolean result = this.filterJoinData(joinData, joiner.filter);
				if( !result ) return joiner.filtermine;
			}
			if( joiner.id != null ){
				String column = joiner.id.getString("column");
				String as = joiner.id.getString("as");
				if( joinData.has(column) ){
					joinData.put(as, joinData.get(column));
					joinData.remove(column);
				}
			}
			if( joiner.setrow ){
				//根据setrow标识，将数据设置到主表上
				Kit.merge(row, joinData);
			}
			else{
				row.put(joiner.objname, joinData);
				if( !joiner.setcol.isEmpty() ){
					String[] args = Tools.split(joiner.setcol, ",");
					for( String col : args ){
						String[] keyval = Tools.split(col, ":");
						if( keyval.length == 1 ){
							if( joinData.has(col) ){
								row.put(col, joinData.get(col));
							}
						}
						else if( keyval.length == 2 ){
							if( joinData.has(keyval[0]) ){
								row.put(keyval[1], joinData.get(keyval[0]));
							}
						}
					}
				}
			}
		}
		else{
			if( joiner.id != null ){
				String as = joiner.id.getString("as");
				Object def = joiner.id.get("default");
				joinData.put(as, def);
				if( joiner.setrow ){
					//根据setrow标识，将数据设置到主表上
					Kit.merge(row, joinData);
				}
				else{
					row.put(joiner.objname, joinData);
				}
			}
		}
		if( _e != null ){
			joinData.put("_exception", _e.getMessage());
		}
		row.put(joiner.objname, joinData);
		return true;
	}

	/**
	 * 设置连接数据(多条关联数据数组存储)
	 * @param joiner
	 * @param row
	 * @param val
	 * @return
	 * @throws Exception
	 */
	private boolean setJoinDataMore(Joiner joiner, JSONObject row, Object val)
		throws Exception
	{
		JSONArray joinData = null;
		if( joiner.data.has(val.toString()) )
		{
			joinData = joiner.data.getJSONArray(val.toString());
		}
		else
		{
			joinData = new JSONArray();
			if( "mongo".equalsIgnoreCase(joiner.srctype) )
			{
				try
				{
					MongoCollection<Document> col = joiner.col;
					if( col == null ){
						diggChecker.print(4, "[0]JOIN.mongo: %s@%s", joiner.from, joiner.config.getString("mongo.database"));
						String from = parameter(joiner.from, row, '%');
						diggChecker.print(4, "[1]JOIN.mongo: %s@%s", from, joiner.config.getString("mongo.database"));
						col = DiggMgr.getMongoCollection(
								joiner.config.getString("mongo.host"), 
								joiner.config.getInt("mongo.port"),
								joiner.config.getString("mongo.username"),
								new String(Base64X.decode(joiner.config.getString("mongo.password"))),
								joiner.config.getString("mongo.database"),
								joiner.config.has("database")?joiner.config.getString("database"):joiner.config.getString("mongo.database"),
								from);
					}
					
					BasicDBObject match = new BasicDBObject();
					if( joiner.condition != null )
					{
						diggChecker.print(4, "[0]JOIN.match: %s", joiner.condition.toString());
						this.setJoinMongoWhere(joiner.condition, dataColumns, match, row);
					}
					if( joiner.group == null )
					{
						match.put(joiner.rightkey, val);
						FindIterable<Document> find = col.find(match);
						if( joiner.sort != null && !joiner.sort.isEmpty() ){
							find = find.sort(Filters.eq(joiner.sort, "down".equalsIgnoreCase(joiner.dir)?-1:1));
						}
						else{
							find = find.sort(new BasicDBObject("_id", -1));
						}
						MongoCursor<Document> cursor = find.iterator();
						while( cursor.hasNext() )
						{
							JSONObject obj = new JSONObject();
							Document doc = cursor.next();
							Kit.merge(obj, doc);
							joinData.put(obj);
						}
						joiner.data.put(val.toString(), joinData);
					}
					else
					{
						match.put(joiner.rightkey, val);
						ArrayList<BasicDBObject> list = new ArrayList<BasicDBObject>();
						list.add(new BasicDBObject("$match", match));
						BasicDBObject group = new BasicDBObject();
						String value = joiner.group.getString("value");
						String[] args = Tools.split(value, ",");
						BasicDBObject groupfileds = new BasicDBObject();
						for(String arg:args)
						{
							groupfileds.put(arg, "$"+arg);
							
						}
						group.put("_id", groupfileds);
						JSONArray counts = joiner.group.getJSONArray("counts");
						for(int i = 0; i < counts.length(); i++)
						{
							JSONObject count = counts.getJSONObject(i);
							String name = count.getString("name");
							String function = count.getString("function");
							String parameter = count.getString("parameter");
							
							group.put(name, new BasicDBObject(function, Tools.isNumeric(parameter)?Integer.parseInt(parameter):parameter));
						}
						list.add(new BasicDBObject("$group", group));
						MongoCursor<Document> cursor = col.aggregate(list).iterator();
						int c = 0;
						while( cursor.hasNext() )
						{
							JSONObject obj = new JSONObject();
							Document doc = cursor.next();
							Document _id = (Document)doc.get("_id");
							Iterator<String> iterator = _id.keySet().iterator();
							StringBuffer key = new StringBuffer();
							while(iterator.hasNext())
							{
								String k = iterator.next();
								key.append(_id.getString(k).toString());
							}
							for(int i = 0; i < counts.length(); i++)
							{
								JSONObject count = counts.getJSONObject(i);
								String name = count.getString("name");
								Object result = doc.get(name);//统计结果
								if( obj.has(name) )
								{
									Double sum = (Double)obj.get(name);
									if( result instanceof Double )
									{
										sum += (Double)result;
									}
									else if( result instanceof Long )
									{
										sum += (Long)result;
									}
									else if( result instanceof Integer )
									{
										sum += (Integer)result;
									}
									obj.put(name, sum);
								}
								else
								{
									obj.put(name, Double.valueOf(result.toString()));
								}
								name = key+"_"+name;
								obj.put(name, result);
							}
							c += 1;
							joinData.put(obj);
							obj.put("count", c);
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				ResultSet rs = null;
				try
				{
		            this.sql = Tools.replaceStr(joiner.sql, "%"+joiner.leftkey+"%", val.toString());
					if( val.toString().isEmpty() ){
						this.sql = Tools.replaceStr(this.sql, "%ISNULL%", " OR ISNULL("+joiner.rightkey+")");
					}
					else{
						this.sql = Tools.replaceStr(this.sql, "%ISNULL%", "");
					}
					this.sql = parameter(this.sql, row, '%');
					diggChecker.print(4, "[1]执行SQL: %s", this.sql);
		            if( joiner.stmt == null ) joiner.stmt = joiner.con.createStatement();
					rs = joiner.stmt.executeQuery(this.sql);
		            ResultSetMetaData meta = rs.getMetaData();
		            while( rs.next() )
		            {
		            	JSONObject obj = new JSONObject();
		            	this.setSQLRowData(meta, rs, obj, joiner.timeformat);
		            	joinData.put(obj);
			        }
					rs.close();
					joiner.data.put(val.toString(), joinData);
				}
				catch(Exception e)
				{
				}
			}
		}
		if( joinData.length() > 0 )
		{
			boolean result = this.filterJoinMore(joinData, joiner, row);
			if( !result ) return joiner.filtermine;
		}
		row.put(joiner.objname, joinData);
		return true;
	}
	/**
	 * 设置左连接的数据
	 * @param joiner
	 * @param row
	 * @return
	 * @throws Exception
	 */
	private boolean setJoinData(Joiner joiner, JSONObject row)
		throws Exception
	{
//		if( joiner.id != null ){
//			String column = joiner.id.getString("column");
//			String as = joiner.id.getString("as");
//			if( row.has(column) ){
//				row.put(as, row.get(column));
//				row.remove(column);
//			}
//			else if( joiner.id.has("default") ){
//				Object def = joiner.id.get("default");
//				row.put(as, def);
//				row.put(joiner.rightkey, "");
//			}
//		}
		long ts = System.currentTimeMillis();
		Object value = null;
		try
		{
			value = this.getRowValue(row, joiner.leftkey);
			diggChecker.print(3, "左连接(%s.%s=%s)生成对象%s", joiner.from, joiner.leftkey, value!=null?value.toString():"null", joiner.objname);
			if( value == null )
			{
//				row.put(joiner.leftkey, "null");
				diggChecker.error(4, "因为%s的值不存在，左连接被中止: %s", joiner.leftkey, joiner.filtermine);
				return joiner.filtermine;
			}
			if( joiner.sql != null ) diggChecker.print(4, "[0]JOIN.SQL: %s", joiner.sql);
//			String val = value.toString();
			if( joiner.filter != null && joiner.filterbefore )
			{
				boolean result = this.filterJoinData(row, joiner.filter);
				if( !result ) return joiner.filtermine;
			}
			if( joiner.more )
			{
				return this.setJoinDataMore(joiner, row, value);
			}
			return this.setJoinDataNomore(joiner, row, value);
		}
		catch(Exception e){
			throw e;
		}
		finally{
            this.diggStep += 1;
	        this.watch(4, "耗时: %s", Kit.getDurationMs(System.currentTimeMillis()-ts));
	        diggChecker.print(4, "结果: %s", row.has(joiner.objname)?row.get(joiner.objname).toString():("没有产生数据"+row.toString(4)));
		}
	}

	/**
	 * 
	 * @param condition
	 * @param dataColumns
	 * @param where
	 */
	private void setJoinMongoWhere(JSONObject condition, JSONObject dataColumns, BasicDBObject where, JSONObject row)
	{
		if( condition == null ) return;
//		String compound = condition.has("compound")?condition.getString("compound"):"";
//		if( compound.isEmpty() ) return;
		String method = condition.has("method")?condition.getString("method"):"";
		if( method.isEmpty() ) return;
		String value = condition.has("value")?condition.getString("value"):"";
		if( value.isEmpty() ) return;
		String columnName = condition.has("column")?condition.getString("column"):"";
		if( columnName.isEmpty() ) return;
		String dataType = condition.has("dataType")?condition.getString("dataType"):"";
		if( dataType.isEmpty() ){
			if( !dataColumns.has(columnName) )
			{
				return;
			}
			JSONObject column = dataColumns.getJSONObject(columnName);
			if( !column.has("dataType") )
			{
				return;
			}
			dataType = column.getString("dataType");
		}
		Object val = null;
		if("$today".equals(value))
		{
			val = Tools.getFormatTime("yyyy-MM-dd", System.currentTimeMillis());
		}
		else if(value.startsWith("$today("))
		{
			value = value.substring("$today(".length()+1);
			value = value.substring(0, value.lastIndexOf(")"));
			if( !Tools.isNumeric(value) ){
				return;
			}
			int day = Integer.parseInt(value);
			val = Tools.getFormatTime("yyyy-MM-dd", System.currentTimeMillis()-day*Tools.MILLI_OF_DAY);
		}
		else if("$yesterday".equals(value))
		{
			val = Tools.getFormatTime("yyyy-MM-dd", System.currentTimeMillis()-Tools.MILLI_OF_DAY);
		}
		else if(value.startsWith("$yesterday("))
		{
			value = value.substring("$yesterday(".length()+1);
			value = value.substring(0, value.lastIndexOf(")"));
			if( !row.has(value) )
			{
				return;
			}
			value = row.getString(value);
			long time = Tools.getDateInMillis(value);
			val = Tools.getFormatTime("yyyy-MM-dd", time-Tools.MILLI_OF_DAY);
		}
		else if(value.startsWith("$"))
		{
			value = value.substring(1);
			val = this.getRowValue(row, value.toString());
			if( val == null )
			{
				return;
			}
		}
		else if( value.indexOf('#') != -1 ){
			int i = value.indexOf('#');
			if( i != -1 ){
				int j = value.lastIndexOf('#');
				String key = null;
				if( i != j ){
					String value0 = value.substring(0, i);
					String value1 = value.substring(j+1);
					key = value.substring(i+1, j);
					if( row.has(key) ){
						val = row.get(key);
						if( !value0.isEmpty() || !value1.isEmpty() ){
							val  = value0+val+value1;
						}
					}
					else{
						if( !value0.isEmpty() || !value1.isEmpty() ){
							val  = value0+value1;
						}
						else{
							return;
						}
					}
				}
				else{
					val = value;
				}
			}
		}
		else if( value.indexOf('%') != -1 ){
			if(method.equalsIgnoreCase("$in"))
			{
				int i = value.indexOf('%');
				if( i != -1 ){
					int j = value.lastIndexOf('%');
					String key = null;
					if( i != j ){
						String value0 = value.substring(0, i);
						String value1 = value.substring(j+1);
						key = value.substring(i+1, j);
						if( row.has(key) ){
							val = row.get(key);
						}
						else{
							if( !value0.isEmpty() || !value1.isEmpty() ){
								val  = value0+value1;
							}
							else{
								return;
							}
						}
					}
				}
				else{
					val = row.get(value);
				}
				if( val == null ) {
					val = value;
				}

			}
			else{
				int i = value.indexOf('%');
				if( i != -1 ){
					int j = value.lastIndexOf('%');
					String key = null;
					if( i != j ){
						String value0 = value.substring(0, i);
						String value1 = value.substring(j+1);
						key = value.substring(i+1, j);
						if( row.has(key) ){
							val = row.get(key);
							if( !value0.isEmpty() || !value1.isEmpty() ){
								val  = value0+val+value1;
							}
						}
						else{
							if( !value0.isEmpty() || !value1.isEmpty() ){
								val  = value0+value1;
							}
							else{
								return;
							}
						}
					}
					else{
						val = value;
					}
				}
			}
		}
		else{
			if( "int".equalsIgnoreCase(dataType) || "integer".equalsIgnoreCase(dataType) )
			{
				if( row.has(value) ){
					val = row.getInt(value);
				}
				else if( Tools.isNumeric(value) ){
					val = Integer.parseInt(value);
				}
				else{
					return;
				}
			}
			else
			{
				if( row.has(value) ){
					val = row.getString(value);
				}
				else {
					val = value;
				}
			}
		}
		if( method.equalsIgnoreCase("=") || method.equalsIgnoreCase("equal") || method.equalsIgnoreCase("eq") )
		{
			where.put(columnName, val);
		}
		else{
			where.put(columnName, new BasicDBObject(method, val));
		}
		if( condition.has("children") )
		{
			JSONArray children = condition.getJSONArray("children");
			for(int i = 0; i < children.length(); i++)
			{
				JSONObject child = children.getJSONObject(i);
				this.setJoinMongoWhere(child, dataColumns, where, row);
			}
		}
	}
	/**
	 * 根据condition配置产生SQL语句
	 * @param condition
	 * @param dataColumns
	 * @param sql
	 * @param where
	 */
	private void setJoinSqlWhere(JSONObject condition, JSONObject dataColumns, StringBuffer sql)
	{
		if( condition == null ) return;
//		<condition compound='and' method='&lt;&gt;' value='admin' quote='true'/>
		String compound = condition.has("compound")?condition.getString("compound"):"";
		if( compound.isEmpty() ) return;
		String method = condition.has("method")?condition.getString("method"):"";
		if( method.isEmpty() ) return;
		String value = condition.has("value")?condition.getString("value"):"";
		if( value.isEmpty() ) return;
		String columnName = condition.has("column")?condition.getString("column"):"";
		if( columnName.isEmpty() ) return;
		String dataType = condition.has("dataType")?condition.getString("dataType"):"";
		JSONObject column = null;
		if( dataType.isEmpty() ){

			if( !dataColumns.has(columnName) ) return;
			column = dataColumns.getJSONObject(columnName);
			dataType = column.has("dataType")?column.getString("dataType"):"";
			columnName = column.getString("dataIndx");
		}
		
		boolean quote = true;
		quote = !dataType.equals("int")&&!dataType.equalsIgnoreCase("long")&&!dataType.equalsIgnoreCase("boolean")&&!dataType.equalsIgnoreCase("number");
    	boolean isvar = false;//是否是变量
    	if( method.equalsIgnoreCase("IN") )
    	{
    		method = " IN ";
    		if( value.startsWith("%") && value.endsWith("%") )
        	{//根据产生的数据动态生成条件
    			if( column != null )
    			{
	    			JSONArray in = column.has("in")?column.getJSONArray("in"):new JSONArray();
	    			in.put(value);
	    			column.put("in", in);
    			}
        	}
    		else
    		{
    			if( value.startsWith("#") && value.endsWith("#") )
            	{
    				String key = value.substring(1);
    				key = key.substring(0, key.length()-1);
    				String val = super.getRequest().getParameter(key);
    				if( val != null && !val.isEmpty() ) value = val;
    				else{
    					if( !values.containsKey(value) ) return;
    					value = values.containsKey(value)?values.get(value):value;
    				}
            	}
    			String[] args = Tools.split(value, ",");
    			StringBuffer sb = new StringBuffer();
    			sb.append("(");
    			for(int i = 0; i < args.length; i++)
    			{
    				if( i > 0 ) sb.append(",");
    				if(quote) sb.append("'");
    				sb.append(args[i]);
    				if(quote) sb.append("'");
    			}
    			sb.append(")");
    			value = sb.toString();
    		}
    	}
    	else
    	{
    		isvar = value.startsWith("%") && value.endsWith("%");
    		value = (quote?"'":"")+value+(quote?"'":"");
    	}
    	
		sql.append(" ");
		if( sql.indexOf("where") == -1 )
		{
			sql.append(" where ");
		}
		else
		{
			sql.append(compound);
		}

		sql.append("(");
		if( isvar && "=".equals(method) )
    	{
			sql.append("(");
    	}
		sql.append(columnName);
		sql.append(method);
		sql.append(value);
		if( isvar && "=".equals(method) )
    	{
			sql.append("OR ISNULL("+columnName+")");
			sql.append(")");
    	}
		
		if( condition.has("children") )
		{
			JSONArray children = condition.getJSONArray("children");
			for(int i = 0; i < children.length(); i++)
			{
				JSONObject child = children.getJSONObject(i);
				this.setJoinSqlWhere(child, dataColumns, sql);
			}
		}
		sql.append(")");
	}
	/**
	 * 设置Group By的分组信息
	 * @param dataColumns
	 * @param sql
	 */
	private void setGroupbyIn(StringBuffer sql, Cache cache)
	{
    	Iterator<?> iterator = dataColumns.keys();
    	while( iterator.hasNext() )
    	{
			String key = iterator.next().toString();//主表字段
			JSONObject dataColumn = dataColumns.getJSONObject(key);
			if( dataColumn.has("in") )
			{
				JSONArray in = dataColumn.getJSONArray("in");
				boolean quote = true;
				String type = dataColumn.has("dataType")?dataColumn.getString("dataType"):"";
				quote = !type.equals("int")&&!type.equalsIgnoreCase("long")&&!type.equalsIgnoreCase("boolean")&&!type.equalsIgnoreCase("number");
            	for(int i = 0; i < in.length(); i++)
            	{
            		String value = in.getString(i);
            		int start = 0;
            		while( (start=sql.indexOf(value)) != -1 )
            		{
            			int end = start + value.length();
            			key = value.substring(1);
            			key = key.substring(0, key.length()-1);
            			
            			StringBuffer sb = new StringBuffer();
            			sb.append("(");
            			HashMap<String, Object> duplicater = new HashMap<String, Object>();
            			for(int j = cache.start(); j < cache.size(); j++)
            			{
            				JSONObject row = cache.get(j);
            				if( !row.has(key) ){
            			        diggChecker.print(1, "跳过条件传参数因为对应字段没有数据: %s", key);
            					continue;
            				}
            				String val = row.get(key).toString();
            				if( duplicater.containsKey(val) ) continue;
            				duplicater.put(val, null);
            				if( sb.length() > 1 ) sb.append(",");
            				if(quote) sb.append("'");
            				sb.append(val);
            				if(quote) sb.append("'");
            			}
            			sb.append(")");
            			if( "('')".equals(sb.toString()) ){
            				JSONObject column = dataColumn.has("column")?dataColumn.getJSONObject("column"):null;
            				if( column != null ){
            					if( column.has(String.valueOf(diggIndx)) ){
                					sql.insert(end, " OR ISNULL("+column.getString(String.valueOf(diggIndx))+")");
            					}
            				}
            			}
            			sql.replace(start, end, sb.toString());
            		}
            	}
			}
    	}
	}
	
	/**
	 * 设置聚合过滤条件，第一个Digg会被调用，用于构建聚合的基础条件
	 * @param request
	 * @param digg
	 */
	public String setFirstGroupbyFilter(JSONObject digg)
	{
		JSONObject filter = getPqFilter();
    	String old_pq_filter = "";
    	if( diggObject.has("pq_filter")) old_pq_filter = diggObject.getString("pq_filter");
        /*if( !filter.has("data") )
        {
        	if( digg.has("default") )
        	{
        		JSONObject defaultObject = digg.getJSONObject("default");
        		//根据缺省参数生成过滤条件
        		log.info("["+gridxml+"] Found the filter of default is "+defaultObject);
//        		{"mode":"AND","data":[{"dataIndx":"work_seq_no","value":"201707181545016799","condition":"contain","dataType":"string","cbFn":""}]}
        		filter.put("mode", "AND");
        		JSONArray conditions = new JSONArray();
        		Iterator<?> iterator = defaultObject.keys();
        		while(iterator.hasNext())
        		{
        			String dataIndx = iterator.next().toString();
        			String defaultValue = defaultObject.getString(dataIndx);
        			
        			JSONObject condition = new JSONObject();
        			condition.put("dataIndx", dataIndx);
        			if( "$today".equals(defaultValue) ){
        				defaultValue = Tools.getFormatTime("yyyy-MM-dd");
            			condition.put("value", defaultValue);
            			condition.put("condition", "equal");
            			condition.put("dataType", "string");
            			condition.put("cbFn", "");
            			conditions.put(condition);
        			}
        			else if( "$yesterday".equals(defaultValue) ){
        				defaultValue = Tools.getFormatTime("yyyy-MM-dd", System.currentTimeMillis()-Tools.MILLI_OF_DAY);
            			condition.put("value", defaultValue);
            			condition.put("condition", "equal");
            			condition.put("dataType", "string");
            			condition.put("cbFn", "");
            			conditions.put(condition);
        			}
        			else if( defaultValue.endsWith("days") ){
        				String days = defaultValue.substring(0, defaultValue.length()-4);
        				if( !Tools.isNumeric(days) ) days = "3";
        				defaultValue = Tools.getFormatTime("yyyy-MM-dd", System.currentTimeMillis()-Integer.parseInt(days)*Tools.MILLI_OF_DAY);
            			condition.put("value", defaultValue);
            			condition.put("condition", "gte");
            			condition.put("dataType", "string");
            			condition.put("cbFn", "");
            			conditions.put(condition);
        			}
        		}
        		filter.put("data", conditions);
        	}
        }*/
        if( filter.has("data") )
        {
        	int count = 0;
    		JSONArray conditions = filter.getJSONArray("data");
    		for(int i = 0; i < conditions.length(); i++)
    		{//检查上传的过滤条件，如果上传的参数仅仅只有group by条件，那么删除缓存
    			JSONObject condition = conditions.getJSONObject(i);
    			String dataIndx = condition.getString("dataIndx");
    			if( digg.has("groupby") && digg.getString("groupby").indexOf(dataIndx) != -1 )
    			{
            		log.info("["+gridxml+"] Found the filter("+dataIndx+") of groupby("+digg.getString("groupby")+")");
    			}
    			else count += 1;
    		}
            String pq_filter = filter.toString();
    		if( !old_pq_filter.equals(filter.toString()) && count == 0 )
    		{//新旧条件不一致触发清楚缓存
        		log.info("["+gridxml+"] Need to clear cache for filter"+filter.toString(4));
        		this.diggMgr.deleteDiggCache(super.getUserAccount(), gridxml);//删除缓存
                if( referer != null ){
                	diggObject.put("pq_filter", pq_filter);
//                	if( referer != null ) super.getSession().setAttribute(referer, diggObject);
                }
    		}
    		else
    		{
    			System.err.println("Not need relaod");
    		}
    		return filter.toString();
        }
    	return pq_filter;
	}
	/**
	 * 生成聚合数据
	 * @param request
	 * @param digg
	 * @param data
	 * @param totalcount 是否是主表，如果是主表要计算数据总量
	 */
	private void setGroupbyData(Cache cache, JSONObject digg, boolean totalcount)
		throws Exception
	{
		long ts = System.currentTimeMillis();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		ArrayList<Joiner> joiners = new ArrayList<Joiner>();
		StringBuffer sql = new StringBuffer();
		StringBuffer sqlCount = null;
		try
		{
			String pq_filter = this.pq_filter;
			if( totalcount )
			{//第一个digg加载缓存
				if( pageSize > 0 ){
					sqlCount = new StringBuffer();
				}
//				pq_filter = this.setFirstGroupbyFilter(digg);
			}
			else{
				pq_filter = "";
				if( cache.isEmpty() ){
			        diggChecker.print(1, "Skip execute for not found master data.");
					return;
				}
			}
			setSqlSelect(digg, sql, sqlCount, pq_filter);
			sql.append(" group by ");
			sql.append(digg.getString("groupby"));//执行聚合
            Class.forName(driverClass); 
        	
            connection = DriverManager.getConnection(jdbcUrl ,jdbcUsername, jdbcUserpswd);  
            statement = connection.createStatement();
			if( totalcount && pageSize > 0 ){
		        diggChecker.print(1, "计算总记录条数的SQL: %s", sqlCount.toString());
	            rs = statement.executeQuery(sqlCount.toString());
	            rs.next();
	            totalRecords = rs.getInt(1);
	            rs.close();
	            setSqlOrder(digg, sql);
	            this.diggStep += 1;
		        this.watch(1, "计算出DIGG总记录条数%s，耗时%s", totalRecords, Kit.getDurationMs(System.currentTimeMillis()-ts));
			}
	        diggChecker.print(1, "聚合传参前SQL: %s", sql.toString());
            this.setGroupbyIn(sql, cache);
			int skip = (curPage-1)*pageSize;
			if( skip > totalRecords ){
				skip = 0;
				curPage = 1;
			}
    		db = digg.has("dbtype")?digg.getString("dbtype"):"";
    		if( totalcount && pageSize > 0 ){
    			if( "oracle".equals(db) )
    			{
    				sql.insert(0, "select * from (select a1.*,rownum rn from (");
    				sql.append(") a1 where rownum <"+(skip+pageSize)+") where rn>="+skip);
    			}
    			else
    			{
    				sql.append(" limit "+skip+","+pageSize); 
    			}
    		}
			diggChecker.print(1, "执行聚合查询SQL: %s", sql.toString());
            rs = statement.executeQuery(sql.toString());
            ResultSetMetaData meta = rs.getMetaData();
//            System.out.println(dataColumns.toString(4));
			this.setJoiner(digg, joiners);
			int countrow = 0;
			String groupby = digg.has("groupby")?digg.getString("groupby"):"";
			String[] args = Tools.split(groupby, ",");
            while( rs.next() )
            {
            	boolean invalid = false;
            	JSONObject row = new JSONObject();
            	this.setSQLRowData(meta, rs, row, digg.has("timeformat")?digg.getString("timeformat"):"");
//				for(int i = 0; i < args.length; i++ ){
//					if( !row.has(args[i]) ){
//						row.put(args[i], "");
//					}
//				}
            	diggChecker.print(2, "数据(%s): %s", groupby, row.toString());
            	countrow += 1;
            	for(Joiner joiner : joiners )
				{
					if( !this.setJoinData(joiner, row) )
					{
						invalid = true;
						break;
					}
				}
				if( !totalcount )
				{//延展的数据，结果合并到结构上
//			        diggChecker.print(2, sb.toString());
					String[] vals = new String[args.length];
					for(int i = 0; i < args.length; i++ ){
						vals[i] = row.has(args[i])?row.get(args[i]).toString():"";
					}
					boolean yes = true;
					String val = null;
					String val0 = null;
					String arg = null;
					for(int i = cache.start(); i < cache.size(); i++ )
					{
						JSONObject row0 = cache.get(i);
//						if( vals.length > 1 ){
							//GROUP字段是多个的时候，采用这种方法
						yes = true;
						for(int j = 0; j < args.length; j++ ){
							arg = args[j];
							if(dataColumns.has(arg) && dataColumns.getJSONObject(arg).has("title") ){
								//groupby内容在grid的定义中没有就跳过，不影响聚合数据
								val = vals[j];
								val0 = row0.has(arg)?row0.get(arg).toString():"";
								if( Tools.isNumeric(val0) ){
									val0 = String.valueOf(row0.getLong(arg));
								}
								yes = val0.equals(val);
								if( !yes )
								{//只要一个不满足就跳出选项
									break;
								}
							}
						}
						if( yes ){
							Kit.merge(row0, row);
							diggChecker.print(3, "数据匹配: %s", row0.toString());
							break;
						}
					}
					if(!yes){
		            	diggChecker.print(3, "[%s] 值[%s]映射[%s]失败", arg, val, val0);
					}
				}
				else
				{
					if( invalid ){
		            	diggChecker.error(3, "左连接[%s]，取得的数据被丢弃", "失败");
						continue;
					}
					cache.add(row);
				}
	        }
            diggChecker.print(1, "完成聚合查询处理数据%s条", countrow);
		}
		catch (Exception e)
		{
			throw e;
		}
        finally
        {
			for(Joiner joiner : joiners )
			{
				joiner.close();
			}
        	if( rs != null )
				try
				{
					rs.close();
				}
				catch (SQLException e)
				{
				}
        	if( statement != null )
				try
				{
					statement.close();
				}
				catch (SQLException e)
				{
				}
        	if( connection != null )
				try
				{
					connection.close();
				}
				catch (SQLException e)
				{
				}
        }
	}
	
	/**
	 * SQL的数据查询
	 * @return
	 */
	private void setSqlData(Cache cache, JSONObject digg)
		throws Exception
	{
		long ts = System.currentTimeMillis();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		ArrayList<Joiner> joiners = new ArrayList<Joiner>();
		StringBuffer sql = new StringBuffer();
		StringBuffer sqlCount = new StringBuffer();
		try
		{
			int skip = (curPage-1)*pageSize;
//				log.info(digg.toString(4));
			setSqlSelect(digg, sql, sqlCount, this.pq_filter);
			setSqlOrder(digg, sql);
			
            Class.forName(driverClass); 
            connection = DriverManager.getConnection(jdbcUrl ,jdbcUsername, jdbcUserpswd);  
            statement = connection.createStatement();
			this.diggChecker.print(1, "计算DIGG总记录条数，执行SQL：%s", sqlCount.toString());
            rs = statement.executeQuery(sqlCount.toString());
            rs.next();
            totalRecords = rs.getInt(1);
			this.diggStep += 1;
	        this.watch(1, "计算出DIGG总记录条数%s，耗时%s", totalRecords, Kit.getDurationMs(System.currentTimeMillis()-ts));
	        if( totalRecords < skip){
	        	skip = 0;
	        	curPage = 1;
	        }
	        if( curPage > 0 && pageSize > 0 ){
				db = digg.has("dbtype")?digg.getString("dbtype"):"";
				if( "oracle".equals(db) )
				{
					sql.insert(0, "select * from (select a1.*,rownum rn from (");
					sql.append(") a1 where rownum <"+(skip+pageSize)+") where rn>="+skip);
				}
				else
				{
					sql.append(" limit "+skip+","+pageSize); 
				}
			}
            rs.close();
            if( totalRecords > 0 ){
                rs = statement.executeQuery(sql.toString());
    			this.diggChecker.print(1, "执行DIGG查询，执行SQL：%s", sql.toString());
                ResultSetMetaData meta = rs.getMetaData();
    			this.setJoiner(digg, joiners);
                while( rs.next() )
                {
                	JSONObject row = new JSONObject();
                	this.setSQLRowData(meta, rs, row, digg.has("timeformat")?digg.getString("timeformat"):"");
                	this.diggChecker.print(2, "数据: %s", row.toString());
    				for(Joiner joiner : joiners )
    				{
    					this.setJoinData(joiner, row);
    				}
                	cache.add(row);
    	        }
            }
		}
		catch (Exception e)
		{
//			log.error("Found exception\r\n\t"+sqlCount+"\r\n\t"+sql, e);
			throw e;
		}
        finally
        {
			for(Joiner joiner : joiners )
			{
				joiner.close();
			}
        	if( rs != null )
				try
				{
					rs.close();
				}
				catch (SQLException e)
				{
				}
        	if( statement != null )
				try
				{
					statement.close();
				}
				catch (SQLException e)
				{
				}
        	if( connection != null )
				try
				{
					connection.close();
				}
				catch (SQLException e)
				{
				}
        }
	}
	
	/**
	 * 	处理数据	
	 * @param cache
	 * @param digg
	 */
	public void setJsonData(Cache cache, JSONObject digg)
		throws Exception
	{
		String url = digg.has("src")?digg.getString("src"):null;
		if( url == null ){
			throw new Exception("DIGG没有配置src.");
		}
		if( !url.startsWith("http") ){
			url = Kit.URL_LOCAL(super.getRequest())+url;
		}
		JSONObject data = digg.has("data")?digg.getJSONObject("data"):new JSONObject();
		if( !data.has("value") ){
			throw new Exception("DIGG没有配置data.");
		}
		String method = digg.has("method")?digg.getString("method"):"post";
		StringBuffer params = new StringBuffer();
		JSONObject page = digg.has("page")?digg.getJSONObject("page"):new JSONObject();
		if( this.curPage > 0 && this.pageSize > 0 ){
			if( page.has("value") ){
				params.append("&");
				params.append(page.getString("value"));
				params.append("=");
				params.append(this.curPage);
			}
			if( page.has("size") ){
				params.append("&");
				params.append(page.getString("size"));
				params.append("=");
				params.append(this.pageSize);
			}	
		}
		byte[] buf = null;
		if( "post".equalsIgnoreCase(method) ){
			StringBuilder payload = new StringBuilder();
	        if( this.pq_filter != null && !this.pq_filter.isEmpty() && this.pq_filter.startsWith("{")) {
	        	payload.append("pq_filter=");
	        	payload.append(pq_filter.toString());
	        }
	        if( this.pq_sort != null && !this.pq_sort.isEmpty() && this.pq_sort.startsWith("{") ){
				payload.append("&");
	        	payload.append("pq_sort=");
	        	payload.append(pq_sort.toString());
	        }
	        if( url.indexOf('?') == -1 && params.length() > 0 ){
	        	params.setCharAt(0, '?');
	        }
	        url += params.toString();
	        this.watch(1, "[%s]拉取数据通过地址[%s]", "GET", url);
	        buf = this.doPost(url, payload.toString().getBytes("UTF-8"));
		}
		else{
	        JSONObject filter = digg.has("filter")?digg.getJSONObject("filter"):null;
	        if( filter != null ){
	        	this.setDiggJsonGet(filter, params);
	        }
	        if( url.indexOf('?') == -1 && params.length() > 0 ){
	        	params.setCharAt(0, '?');
	        }
	        url += params.toString();
	        this.watch(1, "[%s]拉取数据通过地址[%s]", "GET", url);
	        buf = this.doGet(url);
		}
		String json = null;
		JSONObject response = null;
		try{
			json = new String(buf, "UTF-8");
			response = new JSONObject(json);
		}
		catch(Exception e){
			if( json.toLowerCase().indexOf("<html") != -1 ){
				json = Tools.delHTMLTag(json);
				throw new Exception("错误解析返回数据: "+json, e);
			}
			throw new Exception("错误解析返回数据: "+json, e);
		}
		if( data.has("result") ){
			JSONObject result = data.getJSONObject("result");
			if( response.has(result.getString("value")) ) {
				Object r = response.get(result.getString("value"));
				String code = result.getString("code");
				if( !code.equalsIgnoreCase(r.toString())){
			        this.watch(1, "拉取数据返回错误: %s", response.toString(4));
					throw new Exception(response.getString(result.getString("message")));
				}
			}
		}
		JSONArray list = (JSONArray)getRowValue(response, data.getString("value"));
		if( list == null ){
	        this.watch(1, "拉取数据中没有分页列表[%s]对应的数据: %s", data.getString("value"), response.toString(4));
			throw new Exception("拉取数据中没有分页列表对应的数据");
		}
		Object size = getRowValue(response, data.getString("size"));
		if( size == null ){
	        this.watch(1, "拉取数据中没有总数据数量[%s]对应的数据: %s", data.getString("value"), response.toString(4));
			throw new Exception("拉取数据中没有总数据数量对应的数据");
		}
		if( size instanceof Long  ){
			this.totalRecords = (Long) size;
		}
		else if( size instanceof Integer  ){
			this.totalRecords = (Integer) size;
		}
		else{
	        this.watch(1, "拉取数据中总数据数量[%s]的数据格式不支持: %s", data.getString("size"), response.toString(4));
			throw new Exception("拉取数据中总数据数量的数据格式不支持");
		}
		for( int i = 0; i < list.length(); i++ ){
			JSONObject e = list.getJSONObject(i);
			cache.add(e);
		}
	}
	
	/**
	 * 设置DIGG的JSON的get参数
	 * @param filter
	 * @param params
	 */
	private void setDiggJsonGet(JSONObject filter, StringBuffer params){

        if( this.pq_filter == null || this.pq_filter.isEmpty() || !this.pq_filter.startsWith("{"))
        {
        	return;
        }
        JSONObject pq_filter = new JSONObject(this.pq_filter);
		JSONArray conditions = pq_filter.getJSONArray("data");
		for(int i = 0; i < conditions.length(); i++)
		{//查询数据涉及中文，Unicode解码先
			JSONObject condition = conditions.getJSONObject(i);
			String dataIndx = condition.getString("dataIndx");
			String value = null;//condition.getString("value");
			String type = condition.getString("dataType");
			if( condition.getString("condition").equals("range") )
			{
				JSONArray range = condition.getJSONArray("value");
				StringBuffer sb = new StringBuffer();
				for(int j = 0;j < range.length();j++)
				{
					Object o = range.get(j);
					if( j > 0 ) sb.append(",");
					if( type.equals("string") )	sb.append("'"+o+"'");
					else sb.append(o);
				}
				JSONObject obj = filter.has(dataIndx)?filter.getJSONObject(dataIndx):new JSONObject();
				if( obj.has("value") ){
					params.append("&");
					params.append(obj.getString("value"));
					params.append("=");
					params.append(sb.toString());	
				}
			}
			else if( condition.getString("condition").equals("gte") || condition.getString("condition").equals("gt") )
			{
				value = condition.getString("value");
				JSONObject obj = filter.has(dataIndx)?filter.getJSONObject(dataIndx):new JSONObject();
				if( obj.has("value") ){
					params.append("&");
					params.append(obj.getString("value"));
					params.append("=");
					params.append(value);	
				}
			}
			else if( condition.getString("condition").equals("lte") || condition.getString("condition").equals("lt"))
			{
				value = condition.getString("value");
				JSONObject obj = filter.has(dataIndx)?filter.getJSONObject(dataIndx):new JSONObject();
				if( obj.has("value2") ){
					params.append("&");
					params.append(obj.getString("value2"));
					params.append("=");
					params.append(value);	
				}
			}
			else if( condition.getString("condition").equals("between") )
			{
				value = condition.getString("value");
				String value2 = condition.getString("value2");
				value = condition.getString("value");
				JSONObject obj = filter.has(dataIndx)?filter.getJSONObject(dataIndx):new JSONObject();
				if( obj.has("value") ){
					params.append("&");
					params.append(obj.getString("value"));
					params.append("=");
					params.append(value);	
				}
				if( obj.has("value2") ){
					params.append("&");
					params.append(obj.getString("value2"));
					params.append("=");
					params.append(value2);	
				}
			}
			else{
				value = condition.getString("value");
				try {
					value = URLEncoder.encode(value, "utf-8");
				} catch (UnsupportedEncodingException e) {
				}
				JSONObject obj = filter.has(dataIndx)?filter.getJSONObject(dataIndx):new JSONObject();
				if( obj.has("value") ){
					params.append("&");
					params.append(obj.getString("value"));
					params.append("=");
					params.append(value);	
				}
			}
		}
	}

	/**
	 * 
	 * @param uri
	 * @return
	 * @throws Exception
	 */
	public byte[] doGet(String url)
		throws Exception
	{
		HttpURLConnection con = null;
		try
		{
			if(url.startsWith("https"))
			{
				javax.net.ssl.HostnameVerifier hv = new javax.net.ssl.HostnameVerifier() {  
					public boolean verify(String urlHostName, javax.net.ssl.SSLSession session) {  
						return true;  
					}  
				};  
				ApiUtils.trustAllHttpsCertificates();  
				HttpsURLConnection.setDefaultHostnameVerifier(hv);  
			}
			con = (HttpURLConnection)new URL(url).openConnection();
			con.setConnectTimeout( 7000 );
			con.setReadTimeout( 15000 );
			con.setRequestMethod("GET");
			Enumeration<String> names =  super.getRequest().getHeaderNames();
			while( names.hasMoreElements() )
			{
				String key = names.nextElement();
				con.setRequestProperty(key, super.getRequest().getHeader(key));
			}
			con.connect();
			if( con.getResponseCode() != 200 )
			{
				throw new Exception(con.getResponseCode()+": "+con.getResponseMessage());
			}
			InputStream is = con.getInputStream();
			boolean gzip = "gzip".equals(con.getHeaderField("Content-Encoding"));
			if( gzip ) is = new GZIPInputStream(is);
			return ApiUtils.readFullInputStream(is);
		}
		catch (Exception e)
		{
			throw e;
		}
		finally
		{
			if (con != null)
			{
				con.disconnect();
			}
		}
	}
	/**
	 * 
	 * @param url
	 * @param payload
	 * @param parameters
	 * @param noencrypt 2表示重定向
	 * @return
	 * @throws Exception
	 */
	public byte[] doPost(String url, byte[] payload)
		throws Exception
	{
		HttpURLConnection con = null;
		try
		{
			if(url.startsWith("https"))
			{
				javax.net.ssl.HostnameVerifier hv = new javax.net.ssl.HostnameVerifier() {  
					public boolean verify(String urlHostName, javax.net.ssl.SSLSession session) {  
						return true;  
					}  
				};  
				ApiUtils.trustAllHttpsCertificates();  
				HttpsURLConnection.setDefaultHostnameVerifier(hv);  
			}
			con = (HttpURLConnection)new URL(url).openConnection();
			con.setConnectTimeout( 7000 );
			con.setReadTimeout( 15000 );
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			Enumeration<String> names =  super.getRequest().getHeaderNames();
			while( names.hasMoreElements() )
			{
				String key = names.nextElement();
				con.setRequestProperty(key, super.getRequest().getHeader(key));
			}
			con.connect();
			if( payload != null )
			{
				con.getOutputStream().write(payload);
				con.getOutputStream().flush();
			}
			if( con.getResponseCode() != 200 )
			{
				throw new Exception(con.getResponseMessage());
			}
			InputStream is = con.getInputStream();
			boolean gzip = "gzip".equals(con.getHeaderField("Content-Encoding"));
			if( gzip ) is = new GZIPInputStream(is);
			return ApiUtils.readFullInputStream(is);
		}
		catch (Exception e)
		{
			throw e;
		}
		finally
		{
			if (con != null)
			{
				con.disconnect();
			}
		}
	}
	/**
	 * 通配参数
	 * @param text
	 * @param data
	 * @param wc 通配符
	 * @return
	 * @throws Exception
	 */
	private String parameter(String text, JSONObject data, char wc)
		throws Exception
	{
        StringBuffer sbMatch = null;
        StringBuffer result = new StringBuffer();
        int len = text.length();
        ArrayList<String> removing = new ArrayList<String>();
        for( int i = 0; i < len; i++ )
        {
            char c = text.charAt( i );
            if( c == wc )
            {
                if( sbMatch == null )
                {
                    sbMatch = new StringBuffer();
                }
                else
                {
                    Object value = null;
                    if( data != null ){
                    	value = this.getRowValue(data, sbMatch.toString());
                    }
                    if( value == null  )
                    {
                    	String args[] = Tools.split(sbMatch.toString(), " ");
                    	if( args.length == 3 ){
                    		value = data.get(args[2]);
                    		if( value != null && "$day".equalsIgnoreCase(args[0]) && Tools.isNumeric(args[1]) ){
                    			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                  		      	int day = Integer.parseInt(args[1]);
                  		      	Calendar calendar = Calendar.getInstance();
                  		      	calendar.setTime(sdf.parse(value.toString()));
                  		      	calendar.add(Calendar.DAY_OF_MONTH, day);
                  		      	value = Tools.getFormatTime("yyyy-MM-dd", calendar.getTimeInMillis());
                            	result.append( value );
                    		}
                    	}
                    	else{
                    		String tag = "OR ISNULL("+sbMatch+")";
                    		if( text.indexOf(tag) == -1 ){
                    			result.append( wc );
                    			result.append(sbMatch.toString());
                    			result.append( wc );
                    		}
                    		else{
                        		result.append( "" );
                    		}
                    	}
                    }
                    else
                    {
                    	String args[] = Tools.split(sbMatch.toString(), ".");
                		String tag = "OR ISNULL("+args[args.length-1]+")";
                		if( text.indexOf(tag) != -1 ){
                			removing.add(tag);
                		}
                    	result.append( value );
                    }
                    sbMatch = null;
                }
            }
            else if( sbMatch == null )
            {
            	result.append( c );
            }
            else if( sbMatch != null )
            {
                sbMatch.append( c );
            }
        }
        if( sbMatch != null )
        {
        	result.append( wc );
        	result.append(sbMatch.toString());
        }
        for( String tag : removing ){
        	int start = result.indexOf(tag);
        	int end = start + tag.length();
        	result.delete(start, end);
        }
        return result.toString();
	}
	public void setDiggMgr(DiggMgr diggMgr) {
		this.diggMgr = diggMgr;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}
	public void setTs(String ts) {
		this.ts = ts;
	}
	public void setSecurityMgr(SecurityMgr securityMgr) {
		this.securityMgr = securityMgr;
	}
	
	public static void main(String[] args) {
		try {
			Evaluator evaluator = new Evaluator();
			try {
				System.out.println(evaluator.evaluate("34*100/(33+33)"));
			} catch (EvaluationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
