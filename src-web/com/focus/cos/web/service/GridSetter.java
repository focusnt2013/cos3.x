package com.focus.cos.web.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;

import com.focus.cos.api.LogSeverity;
import com.focus.cos.api.LogType;
import com.focus.cos.api.SyslogClient;
import com.focus.cos.web.action.GridAction;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.ZKMgr;
import com.focus.util.Base64X;
import com.focus.util.Tools;
import com.focus.util.XMLParser;
import com.focus.util.Zookeeper;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.util.JSON;

/**
 * 修改Grid表格的数据
 * @author focus
 *
 */
public abstract class GridSetter
{
	private static final Log log = LogFactory.getLog(GridSetter.class);
	public static final int ADD = 0;
	public static final int UPDATE = 1;
	public static final int DELETE = 2;
	public static final String[] METHODLABELS = {"ADD", "UPDATE", "DELETE"};
//	protected HttpServletRequest req;
//	protected HttpServletResponse rsp;
	protected String gridtitle;
	protected String myid;
	protected JSONObject operator;//操作者账号
	protected String type;
	protected int method = -1;
	protected String path = "";
	protected boolean encrypte = false;
	protected boolean skipnull = false;
//	protected String mode = "";
	/*SQL连接参数*/
	protected String jdbcUrl;
	protected String jdbcUsername;
	protected String jdbcUserpswd;
	protected String driverClass;
	protected String jdbcDatabase;
	protected String jdbcFrom;
	/**/
	protected String mongoHost;
	protected int mongoPort;
	protected String mongoUsername;
	protected String mongoPassword;
	protected String mongoDatabase;
	protected String mongoFrom;
	/*处理增删改操作的grid节点对象*/
	protected Element filednode;//字段节点
	protected String filedtag;//字段标签
	protected Element datamodelnode;
	protected String logformat = "";
	/*返回值*/
	protected JSONObject response = new JSONObject();
	public GridSetter()
	{
	}
	/**
	 * Grid的设置类构造器
	 * @param req
	 * @param rsp
	 * @param path
	 * @param logformat
	 */
	public GridSetter(GridAction gridAction) throws Exception
	{
		filedtag = "cell";
		skipnull = gridAction.isSkipnull();
		load(gridAction.getGridXmlInputStream());
		this.path = gridAction.checkParamter(this.path);
//		StringBuffer sb = new StringBuffer(String.format("Receive the request of gird-set[%s]", this.logformat));
//    	Enumeration<String> names = req.getParameterNames();
//    	while (names.hasMoreElements())
//    	{
//	        String key = (String)names.nextElement();
//	        String value = req.getParameter(key);
//	        sb.append("\r\n\t" + key + " = " + value);
//    	}
//    	log.info(sb);
	}

	/**
	 * 根据xml配置加载
	 * @param is
	 * @throws Exception
	 */
	protected void load(InputStream is) throws Exception
	{
		XMLParser xml = new XMLParser(is);
		filednode = XMLParser.getChildElementByTag( xml.getRootNode(), "grid" );
		if( filednode == null ){
			filednode = XMLParser.getChildElementByTag( xml.getRootNode(), "form" );
		}
		gridtitle = XMLParser.getElementAttr(xml.getRootNode(), "title");
		datamodelnode = XMLParser.getChildElementByTag(xml.getRootNode(), "datamodel");
		type = XMLParser.getElementAttr(datamodelnode, "type");
		if( "zookeeper".equalsIgnoreCase(type) ) {
			this.path = XMLParser.getElementAttr(datamodelnode, "value");
			if( path.isEmpty() )
			{
				throw new Exception("模板没有配置存储地址[value]");
			}
//			this.path = gridAction.parameter(this.path);
			encrypte = XMLParser.getElementAttr(datamodelnode, "encrypte").equalsIgnoreCase("true");
		}
		else if( "digg".equalsIgnoreCase(type) ) {
			this.presetDatabase(XMLParser.getFirstChildElement(datamodelnode));
		}
	}
	
	public abstract JSONObject set() throws Exception;
	public abstract void update(JSONObject old, JSONObject set) throws Exception;
	/**
	 * 执行设置
	 * @param operator 后台操作者账号
	 * @return
	 */
	public JSONObject execute(JSONObject operator)
	{
		this.operator = operator;
		try 
		{
			JSONObject data = set();
			this.logformat = "新增"+gridtitle;
			if( method == UPDATE ) logformat = "修改"+gridtitle;
			if( method == DELETE ) logformat = "删除"+gridtitle;
			switch(method)
			{
			case 0:
				return this.add(data);
			case 1:
				return this.update(data);
			case 2:
				return this.delete();
			default:
				response.put("hasException", true);
				response.put("message", "提交数据中缺少必要参数.");
				break;
			}
		}
		catch (Exception e) 
		{
			log.error("Failed to execute.", e);
			try 
			{
				response.put("hasException", true);
				response.put("message", e.getMessage());
			}
			catch (JSONException e1) 
			{
			}
		}
		return response;
	}
	/**
	 * 
	 * @param data
	 * @param path
	 * @return
	 */
	protected JSONObject add(JSONObject data)
	{
		JSONObject response = new JSONObject();
		if( data == null )
		{
			response.put("hasException", true);
			response.put("message", "提交数据中缺少必要参数.");
		}
		else
		try
		{
			if( "zookeeper".equalsIgnoreCase(type) ) {
				ZooKeeper zookeeper = ZKMgr.getZooKeeper();
				Stat stat = zookeeper.exists(path, false); 
				if( stat != null)
				{
					path += "/"+myid;
					stat = zookeeper.exists(path, false); 
					if( stat != null )
					{

						response.put("hasException", true);
						response.put("message", "您要新增的数据已经存在.");
					}
					else
					{
						return this.set(zookeeper, stat, data, response );
					}
				}
				else
				{
					response = this.set(zookeeper, stat, data, response );
				}
			}
			else if( "digg".equalsIgnoreCase(type) ) {
				insertDiggRow(data);
			}
		}
		catch (Exception e)
		{
			log.error("Failed to add", e);
			try 
			{
				response.put("hasException", true);
				response.put("message", e.getMessage());
			}
			catch (JSONException e1) 
			{
			}
		}
		return response;
	}
	
	/**
	 * 设置数据类型
	 * @param cellNode
	 * @param dataTypes
	 * @throws Exception
	 */
	private void setDataAttributes(Element filedNode, HashMap<String, Element> cells) throws Exception
	{
        for( ; filedNode != null; filedNode = XMLParser.getNextSibling(filedNode))
        {
            Element childNode = XMLParser.getChildElementByTag(filedNode, filedtag);
            if( childNode != null )
            {
            	this.setDataAttributes(childNode, cells);
            }
            else
            {
            	String dataIndx = XMLParser.getElementAttr( filedNode, "dataIndx" );
            	if( dataIndx.isEmpty() ){
            		continue;
            	}
            	cells.put(dataIndx, filedNode);
            }
        }
	}
	/**
	 * 
	 * @param data
	 * @return
	 */
	protected JSONObject update(JSONObject data)
	{
		if( data == null )
		{
			response.put("hasException", true);
			response.put("message", logformat+"失败因为缺少上传参数。");
		}
		else
		try
		{
			if( "zookeeper".equalsIgnoreCase(type) ) {
				Zookeeper zookeeper = ZKMgr.getZookeeper();
				path += "/"+myid;
				Stat stat = zookeeper.exists(path, false); 
				if( stat == null)
				{
					zookeeper.createNode(path, new byte[0]);
				}
				stat = zookeeper.exists(path, false); 
				if( stat != null )
				{
					JSONObject old = zookeeper.getJSONObject(path, encrypte);
					update(old, data);
					response = this.set(zookeeper.i(), stat, data, response );
				}
				else
				{
					response.put("hasException", true);
					response.put("message", "您要设置的数据("+myid+")并不存在.");
				}
			}
			else if( "digg".equalsIgnoreCase(type) ) {
				this.updateDiggRow(data);
			}
		}
		catch (Exception e)
		{
			log.error("Failed to update the data to path["+path+"]", e);
			try 
			{
				response.put("hasException", true);
				response.put("message", e.getMessage());
			}
			catch (JSONException e1) 
			{
			}
		}
		return response;
	}
	/**
	 * 设置节点数据
	 * @return
	 */
	private JSONObject set(ZooKeeper zookeeper, Stat stat, JSONObject data, JSONObject response)
	{
		JSONObject account = operator;
		try
		{
			if( account == null )
			{
				response.put("hasException", true);
				response.put("message", "会话不存在或者已经过期。");
			}
			if( !response.has("hasException") || !response.getBoolean("hasException") )
			{
				String s = String.format("%s%s【%s】", account.getString("username"), logformat, myid);
				byte[] payload = data.toString().getBytes("UTF-8");
				if( encrypte )
				{
					payload = Base64X.encode(payload).getBytes();
				}
				if( stat == null )
				{
					zookeeper.create(path, payload, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
				else
				{
					zookeeper.setData(path, payload, stat.getVersion());
				}
				SyslogClient.write(
						LogType.操作日志,
						LogSeverity.INFO,
						s,
						account.getString("username"),
						logformat,
						null,
						null);
				response.put("recId", myid);
			}
		}
		catch (Exception e)
		{
			log.warn("Failed to set the data of "+path+" for exception "+e);
			try 
			{
				String s = String.format("%s%s【"+myid+"】出现异常%s", account.getString("username"), logformat, e.getMessage());
				SyslogClient.write(
						LogType.操作日志,
						LogSeverity.ERROR,
						s,
						account.getString("username"),
						logformat,
						null,
						null);
				response.put("hasException", true);
				response.put("message", s);
			}
			catch (JSONException e1) 
			{
			}
		}
		return response;
	}
	/**
	 * 
	 * @param data
	 * @param path
	 * @return
	 */
	protected JSONObject delete()
	{
		JSONObject account = operator;
		JSONObject response = new JSONObject();
		try
		{
			if( account == null )
			{
				response.put("hasException", true);
				response.put("message", "会话不存在或者已经过期。");
			}
			else
			{
				if( "zookeeper".equalsIgnoreCase(type) ) {
					Zookeeper zookeeper = ZKMgr.getZookeeper();
					Stat stat = zookeeper.exists(path, false); 
					if( stat != null)
					{
						String delpath = path +"/"+ myid;
						zookeeper.deleteNode(delpath);
						SyslogClient.write(
								LogType.操作日志,
								LogSeverity.INFO,
								String.format("删除%s%s", logformat, myid),
								account.getString("username"),
								logformat,
								null,
								null);
					}
					else
					{
						response.put("hasException", true);
						response.put("message", "删除"+logformat+"失败因为配置节点不存在"+path);
					}
				}
				else if( "digg".equalsIgnoreCase(type) ) {
					this.deleteDiggRow();
					SyslogClient.write(
							LogType.操作日志,
							LogSeverity.INFO,
							String.format("删除%s%s", logformat, myid),
							account.getString("username"),
							logformat,
							null,
							null);
				}
			}
		}
		catch (Exception e)
		{
			log.warn("Failed to delete the data of "+path+" for exception "+e);
			try 
			{
				String s = String.format("删除%s出现异常%s", logformat, e.getMessage());
				SyslogClient.write(
						LogType.操作日志,
						LogSeverity.ERROR,
						s,
						account.getString("username"),
						logformat,
						null,
						null);
				
				response.put("hasException", true);
				response.put("message", s);
			}
			catch (JSONException e1) 
			{
			}
		}
		return response;
	}

	/**
	 * 返回json数据
	 * @param rsp
	 * @param json
	 * @return
	private String response(String json)
	{
		ServletOutputStream out = null;
		try
		{
    		rsp.setHeader("Content-disposition", "inline; filename="+System.currentTimeMillis()+".json");
            out = rsp.getOutputStream();
            out.write(json.getBytes("UTF-8"));
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
	 */
	
	/**
	 * 通过HTTP请求传参设置
	 * @param text
	 * @return
	 * @throws Exception
	private String parameter(String text)
		throws Exception
	{
        StringBuffer sbMatch = null;
        StringBuffer result = new StringBuffer();
        int len = text.length();
        for( int i = 0; i < len; i++ )
        {
            char c = text.charAt( i );
            if( c == '%' )
            {
                if( sbMatch == null )
                {
                    sbMatch = new StringBuffer();
                }
                else if( sbMatch.length() > 16 )
                {
                	result.append( "%" );
                	result.append(sbMatch.toString());
                	result.append( "%" );
                    sbMatch = null;
                }
                else
                {
                    String property = null;
                    if( req != null && (property == null || property.isEmpty()) ){
                    	property = this.req.getParameter(sbMatch.toString());
                    }
                    if( property == null || property.isEmpty() ){
                    	property = System.getProperty(sbMatch.toString(), "");
                    }
                    if( property == null || property.isEmpty() )
                    {                        	
                    	result.append( "%" );
	                	result.append(sbMatch.toString());
	                	result.append( "%" );
	                    sbMatch = null;
                    }
                    else
                    {
                    	result.append( property );
	                    sbMatch = null;
                    }
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
        	result.append( "%" );
        	result.append(sbMatch.toString());
        }
        return result.toString();
	}
	 */
	
	/**
	 * 插入数据到配置的数据库
	 * @param data
	 * @throws Exception
	 */
	private void insertDiggRow(JSONObject data) throws Exception
	{
		if( driverClass != null ) {
			this.insertDiggSqlRow(data);
		}
		else if( mongoHost != null ) {
			this.insertDiggMongoRow(data);
		}
	}
	
	/**
	 * 递归设置数据对象
	 * @param row
	 * @param args
	 * @param depth
	 * @return
	 */
	private void setDataObject(Document row, String[] args, int depth, Object val){
		if( args.length == depth+1 ){
			if( val instanceof JSONObject ){
				val = Document.parse(val.toString());
			}
			else if( val instanceof JSONArray ){
				val = (BasicDBList)JSON.parse(val.toString());
			}
			row.put(args[depth], val);
			return;
		}
		String name = args[depth];
		Document obj = null;
		if( row.containsKey(name) ){
			obj = (Document)row.get(name);
		}
		else{
			obj = new Document();
			row.put(name, obj);
		}
		this.setDataObject(obj, args, depth+1, val);
	}
	/**
	 * 执行插入数据到芒果数据库
	 * @param data
	 * @throws Exception
	 */
	private void insertDiggMongoRow(JSONObject data) throws Exception{
		log.info("Ready to insert mongo "+data.toString(4));
		Document row = new Document();
		HashMap<String, Element> dataCells = new HashMap<String, Element>();
		Element filedNode = XMLParser.getFirstChildElement(filednode);
		this.setDataAttributes(filedNode, dataCells);
		Iterator<Element> iterator = dataCells.values().iterator();
		while(iterator.hasNext()){
			filedNode = iterator.next();
			String dataIndx = XMLParser.getElementAttr(filedNode, "dataIndx");
			String dataType = XMLParser.getElementAttr(filedNode, "dataType");
			String title = XMLParser.getElementAttr(filedNode, "title");
			if( dataType == null || dataType.isEmpty() ) {
				throw new Exception(String.format("表格单元[%s/%s]没有设置数据类型", title,dataIndx));
			}
			boolean onlyshow = "true".equalsIgnoreCase(XMLParser.getElementAttr( filedNode, "onlyshow" ));
			if( onlyshow ){
				continue;
			}
			Object val = data.has(dataIndx)?data.get(dataIndx):null;
			if( val == null || val.toString().isEmpty() ){
				boolean skip = skipnull||"true".equalsIgnoreCase(XMLParser.getElementAttr( filedNode, "hidden" ));
				if( skip ){
					row.remove(dataIndx);
					continue;
				}
				boolean nullable = "true".equalsIgnoreCase(XMLParser.getElementAttr( filedNode, "nullable" ));
            	if( !nullable ){
            		nullable = "hidden".equalsIgnoreCase(XMLParser.getElementAttr( filedNode, "type" ));
            	}
				if( !nullable ){
					throw new Exception(String.format("表格单元[%s]字段[%s]不允许为空没有数据", 
							XMLParser.getElementAttr(filedNode, "title"), dataIndx));
				}
				if( val == null || !dataType.equalsIgnoreCase("string") ){
					continue;
				}
			}
			if( val instanceof JSONObject || val instanceof JSONArray ){
				val = JSON.parse(val.toString());
			}
			String[] args = Tools.split(dataIndx, ".");
			this.setDataObject(row, args, 0, val);
		}
		
        MongoClient mongo = null;
		try{
    		mongo = getMongoClient(this.mongoHost, this.mongoPort,this.mongoUsername,this.mongoPassword,this.mongoDatabase);
    		MongoDatabase db = mongo.getDatabase(this.mongoDatabase);
    		MongoCollection<Document> col = db.getCollection(this.mongoFrom);
    		col.insertOne(row);
    		log.info(String.format("Succeed to execute insert to %s\r\n\tThe result is %s", mongoFrom, row.toJson()));
			JSONObject account = operator;
			String s = String.format("%s%s【%s】", account.getString("username"), logformat, myid);
			SyslogClient.write(
					LogType.操作日志,
					LogSeverity.INFO,
					s,
					account.getString("username"),
					logformat,
					null,
					null);
		}
		catch(Exception e)
		{
           log.info("Failed to execute insert "+row, e);
			throw new Exception(String.format("向芒果数据库[%s]新增记录失败因为异常%s", mongoDatabase, e.toString()));
		}
		finally
		{
    		if(mongo != null)
    		{
    			mongo.close();
    		}
		}
	}
	/**
	 * 插入数据到SQL关系数据库
	 * @param data
	 * @throws Exception
	 */
	private void insertDiggSqlRow(JSONObject data) throws Exception
	{
		HashMap<String, Element> dataCells = new HashMap<String, Element>();
		Element filedNode = XMLParser.getFirstChildElement( filednode );
		this.setDataAttributes(filedNode, dataCells);
		Element diggNode = XMLParser.getFirstChildElement(datamodelnode);
		StringBuilder sql = new StringBuilder(String.format("INSERT INTO %s(", jdbcFrom));
		//NID, USERACCOUNT, TITLE, FILTER, NOTIFYTIME, CONTEXT, CONTEXTLINK, CONTEXTIMG, ACTION, ACTIONLINK, STATE, PRIORITY) ");
		ArrayList<Object> values = new ArrayList<Object>();
        for(Element dataNode = XMLParser.getFirstChildElement(diggNode); dataNode != null; dataNode = XMLParser.getNextSibling(dataNode))
        {
        	String autoincr = XMLParser.getElementAttr(dataNode, "autoincr");
			if( "true".equalsIgnoreCase(autoincr) ){
				continue;
			}
			String dataIndx = dataNode.getNodeName();
			filedNode = dataCells.get(dataIndx);
			String dataType = XMLParser.getElementAttr(dataNode, "dataType");
			if( dataType.isEmpty() ){
				dataType = XMLParser.getElementAttr(filedNode, "dataType");
			}
			if( dataType == null || dataType.isEmpty() ) {
				throw new Exception(String.format("表格单元[%s/%s]没有设置数据类型", XMLParser.getElementAttr(filedNode, "title"), dataIndx));
			}
			String clause = XMLParser.getElementAttr(dataNode, "clause");
			if( clause.isEmpty() ){
				clause = XMLParser.getElementAttr(dataNode, "column");
			}
			if( clause.isEmpty() ){
				clause = dataIndx;
			}
			Object val = data.has(dataIndx)?data.get(dataIndx):null;
			if( val == null || val.toString().isEmpty() ){
				boolean skip = skipnull||"true".equalsIgnoreCase(XMLParser.getElementAttr( filedNode, "hidden" ));
				if( skip ){
					continue;
				}
				boolean nullable = "true".equalsIgnoreCase(XMLParser.getElementAttr( filedNode, "nullable" ));
				if( !nullable ){
					throw new Exception(String.format("表格单元[%s]字段[%s]不允许卫东没有数据", 
							XMLParser.getElementAttr(filedNode, "title"), dataIndx.equals(clause)?clause:(dataIndx+"=>"+clause)));
				}

				if( val == null || !dataType.equalsIgnoreCase("string") ){
					continue;
				}
			}
			if( values.size() > 0 ){
				sql.append(",");
			}
			sql.append(clause);
			values.add(val);
		}
		sql.append(") VALUES(");
        for(int i = 0; i < values.size(); i++ )
        {
			if( i > 0 ){
				sql.append(",");
			}
        	sql.append("?");
        }
		sql.append(")");

		Connection connection = null;
		PreparedStatement pstmt = null;
		try{
			Class.forName(driverClass); 
            connection = DriverManager.getConnection(jdbcUrl ,jdbcUsername, jdbcUserpswd);
            pstmt = connection.prepareStatement(sql.toString());
            for(int i = 0; i < values.size(); ){
            	Object value = values.get(i++);
            	if( value instanceof Integer ){
            		pstmt.setInt(i, (Integer)value);
            	}
            	else if( value instanceof Long ){
            		pstmt.setLong(i, (Long)value);
            	}
            	else if( value instanceof Double ){
            		pstmt.setDouble(i, (Double)value);
            	}
            	else if( value instanceof Float ){
            		pstmt.setFloat(i, (Float)value);
            	}
            	else{
            		pstmt.setString(i, value.toString());
            	}
            }
           pstmt.addBatch();
           int result = pstmt.executeUpdate();
           log.info("Succeed to execute "+sql+" \r\n\tThe result is "+result);
			JSONObject account = operator;
			String s = String.format("%s%s【%s】", account.getString("username"), logformat, myid);
			SyslogClient.write(
					LogType.操作日志,
					LogSeverity.INFO,
					s,
					account.getString("username"),
					logformat,
					null,
					null);
		}
		catch(Exception e)
		{
           log.info("Failed to execute "+sql, e);
			throw new Exception(String.format("向数据库[%s]新增记录失败因为异常%s", jdbcDatabase, e.toString()));
		}
		finally
		{
			if( pstmt != null )
				try
				{
					pstmt.close();
				}
				catch (SQLException e1)
				{
				}
        	if( connection != null )
				try
				{
					connection.close();
				}
				catch (SQLException e1)
				{
				}
		}
	}
	
	/**
	 * 得到芒果的主键条件
	 * @param recIndx
	 * @param dataCells
	 * @return
	 * @throws Exception
	 */
	private Bson getMongoRecIndx(String recIndx, HashMap<String, Element> dataCells, JSONObject dataRow)
		throws Exception
	{
		if( dataRow != null ){
			Object recVal = dataRow.has(recIndx)?dataRow.get(recIndx):myid;
			if( recVal == null ){
				throw new Exception(String.format("主键[%s]数据不存在", recIndx));
			}
			return recIndx.equalsIgnoreCase("_id")?new BasicDBObject("_id", new ObjectId(recVal.toString())):new BasicDBObject(recIndx, recVal);
		}
		else{
			Bson _id = recIndx.equalsIgnoreCase("_id")?new BasicDBObject("_id", new ObjectId(myid)):null;
			if( _id == null ){
				Element cellNode = dataCells.get(recIndx);
				if( cellNode == null ){
					throw new Exception(String.format("没有配置主键[%s]的数据单元", recIndx));
				}
				String dataType = XMLParser.getElementAttr(cellNode, "dataType");
				if( dataType == null || dataType.isEmpty() ) {
					throw new Exception(String.format("主键[%s]表格单元[%s]没有设置数据类型", recIndx,
							XMLParser.getElementAttr(cellNode, "title")));
				}
				if( "integer".equalsIgnoreCase(dataType) ){
					_id = new BasicDBObject(recIndx, Integer.parseInt(myid));
				}
				else if( "long".equalsIgnoreCase(dataType) ){
					_id = new BasicDBObject(recIndx, Long.parseLong(myid));
				}
				else if( "float".equalsIgnoreCase(dataType) ){
					_id = new BasicDBObject(recIndx, Float.parseFloat(myid));
				}
				else{
					_id = new BasicDBObject(recIndx, myid);
				}
			}
			return _id;
		}
	}
	
	/**
	 * 更新数据
	 * @param data
	 * @throws Exception
	 */
	private void updateDiggRow(JSONObject data) throws Exception{
		if( driverClass != null ) {
			this.updateDiggSqlRow(data);
		}
		else if( mongoHost != null ) {
			this.updateDiggMongoRow(data);
		}
	}
	
	/**
	 * 得到数据模型唯一ID
	 * @return
	 */
	public String getRecIndx(){
		String recIndx = XMLParser.getElementAttr( filednode, "recIndx" );
		if( recIndx.isEmpty() ){
			recIndx = XMLParser.getElementAttr( datamodelnode, "recIndx" );
		}
		return recIndx;
	}
	/**
	 * 更新芒果数据库的数据
	 * @param data
	 * @throws Exception
	 */
	private void updateDiggMongoRow(JSONObject data) throws Exception{
		String recIndx = getRecIndx();
		HashMap<String, Element> dataCells = new HashMap<String, Element>();
		Element filedNode = XMLParser.getFirstChildElement(filednode);
		this.setDataAttributes(filedNode, dataCells);
		Bson _id = getMongoRecIndx(recIndx, dataCells, data);
		log.info("Ready to update("+myid+") mongo "+data.toString(4)+"\r\n"+recIndx+" "+_id);
        MongoClient mongo = null;
        Document row = null;
		try{
    		mongo = getMongoClient(this.mongoHost, this.mongoPort,this.mongoUsername,this.mongoPassword,this.mongoDatabase);
    		MongoDatabase db = mongo.getDatabase(this.mongoDatabase);
    		MongoCollection<Document> col = db.getCollection(this.mongoFrom);
    		FindIterable<Document> find = col.find(_id);
    		MongoCursor<Document> cursor = find.iterator();
    		while(cursor.hasNext()){
    			if( row != null ) {
        			throw new Exception(String.format("主键[%s]%s不唯一", recIndx, row.toJson()));
    			}
				row = cursor.next();
			}
    		if( row == null ){
    			throw new Exception(String.format("主键[%s]%s对应的行数据不存在", recIndx, _id.toString()));
    		}
			Iterator<Element> iterator = dataCells.values().iterator();
			while(iterator.hasNext()){
				filedNode = iterator.next();
				String dataIndx = XMLParser.getElementAttr(filedNode, "dataIndx");
				String dataType = XMLParser.getElementAttr(filedNode, "dataType");
				String title = XMLParser.getElementAttr(filedNode, "title");
				if( dataType == null || dataType.isEmpty() ) {
					throw new Exception(String.format("表格单元[%s/%s]没有设置数据类型", title,dataIndx));
				}
				boolean onlyshow = "true".equalsIgnoreCase(XMLParser.getElementAttr( filedNode, "onlyshow" ));
				if( onlyshow ){
					row.remove(dataIndx);
					continue;
				}
				Object val = data.has(dataIndx)?data.get(dataIndx):null;
				if( val == null || val.toString().isEmpty() ){
					boolean skip = skipnull||"true".equalsIgnoreCase(XMLParser.getElementAttr( filedNode, "hidden" ))||"password".equalsIgnoreCase(dataType);
					if( skip ){
						row.remove(dataIndx);
						continue;
					}
					boolean nullable = "true".equalsIgnoreCase(XMLParser.getElementAttr( filedNode, "nullable" ));
	            	if( !nullable ){
	            		nullable = "hidden".equalsIgnoreCase(XMLParser.getElementAttr( filedNode, "type" ));
	            	}
					if( !nullable ){
						throw new Exception(String.format("表格单元[%s]字段[%s]不允许为空没有数据", 
								XMLParser.getElementAttr(filedNode, "title"), dataIndx));
					}
					if( val == null || !dataType.equalsIgnoreCase("string") ){
						continue;
					}
				}
				String[] args = Tools.split(dataIndx, ".");
				this.setDataObject(row, args, 0, val);
			}
			UpdateResult result = col.updateOne(_id, new BasicDBObject("$set",row));
    		log.info("Succeed to execute update \r\n\tThe result is "+result.toString());
			JSONObject account = operator;
			String s = String.format("%s%s【%s】", account.getString("username"), logformat, myid);
			SyslogClient.write(
					LogType.操作日志,
					LogSeverity.INFO,
					s,
					account.getString("username"),
					logformat,
					null,
					null);
		}
		catch(Exception e)
		{
           log.info("Failed to execute update "+row, e);
			throw new Exception(String.format("从芒果数据库[%s]更新记录失败因为异常%s", mongoDatabase, e.toString()));
		}
		finally
		{
    		if(mongo != null)
    		{
    			mongo.close();
    		}
		}
	}
	/**
	 * 更新SQL关系数据库的数据
	 * @param data
	 * @throws Exception
	 */
	private void updateDiggSqlRow(JSONObject data) throws Exception{
		String recIndx = getRecIndx();
//		System.err.println(data.toString(4));
		HashMap<String, Element> dataCells = new HashMap<String, Element>();
		Element filedNode = XMLParser.getFirstChildElement( filednode );
		this.setDataAttributes(filedNode, dataCells);
		Element diggNode = XMLParser.getFirstChildElement(datamodelnode);
		StringBuilder sql = new StringBuilder(String.format("UPDATE %s SET ", jdbcFrom));
		//NID, USERACCOUNT, TITLE, FILTER, NOTIFYTIME, CONTEXT, CONTEXTLINK, CONTEXTIMG, ACTION, ACTIONLINK, STATE, PRIORITY) ");
		ArrayList<Object> values = new ArrayList<Object>();
		String priKey = "";
		String priKeyDataType = null;
        for(Element dataNode = XMLParser.getFirstChildElement(diggNode); dataNode != null; dataNode = XMLParser.getNextSibling(dataNode))
        {
        	String autoincr = XMLParser.getElementAttr(dataNode, "autoincr");
			String dataIndx = dataNode.getNodeName();
			filedNode = dataCells.get(dataIndx);
			String dataType = XMLParser.getElementAttr(dataNode, "dataType");
			if( dataType.isEmpty() ){
				dataType = XMLParser.getElementAttr(filedNode, "dataType");
			}
			if( dataType == null || dataType.isEmpty() ) {
				throw new Exception(String.format("表格单元[%s/%s]没有设置数据类型", XMLParser.getElementAttr(filedNode, "title"), dataIndx));
			}
			String clause = XMLParser.getElementAttr(dataNode, "clause");
			if( clause.isEmpty() ){
				clause = XMLParser.getElementAttr(dataNode, "column");
			}
			if( clause.isEmpty() ){
				clause = dataIndx;
			}
			if( recIndx.equals(dataIndx) ){
				priKey = clause;
				priKeyDataType = dataType;
				continue;
			}
			Object val = data.has(dataIndx)?data.get(dataIndx):null;
			if( val == null || val.toString().isEmpty() ){
				boolean skip = skipnull||"true".equalsIgnoreCase(XMLParser.getElementAttr( filedNode, "hidden" ))||"password".equalsIgnoreCase(dataType);
				if( skip ){
					continue;
				}
				boolean nullable = "true".equalsIgnoreCase(XMLParser.getElementAttr( filedNode, "nullable" ));
				if( !nullable ){
					throw new Exception(String.format("表格单元[%s]字段[%s]不允许为空没有数据", 
							XMLParser.getElementAttr(filedNode, "title"), dataIndx.equals(clause)?clause:(dataIndx+"=>"+clause)));
				}
				if( val == null || !dataType.equalsIgnoreCase("string") ){
					continue;
				}
			}
			if( "true".equalsIgnoreCase(autoincr) ){
				continue;
			}
			if( values.size() > 0 ){
				sql.append(",");
			}
			sql.append(clause);
			sql.append("=?");
			values.add(val);
		}
		sql.append(String.format(" WHERE %s=?", priKey));
    	if( "int".equalsIgnoreCase(priKeyDataType) ||
			"integer".equalsIgnoreCase(priKeyDataType) )
    	{
    		values.add(Integer.parseInt(myid));
    	}
    	else if( "long".equalsIgnoreCase(priKeyDataType) )
    	{
    		values.add(Long.parseLong(myid));
    	}
    	else {
    		values.add(myid);
    	}

		Connection connection = null;
		PreparedStatement pstmt = null;
		try{
            connection = DriverManager.getConnection(jdbcUrl ,jdbcUsername, jdbcUserpswd);
            pstmt = connection.prepareStatement(sql.toString());
            for(int i = 0; i < values.size(); ){
            	Object value = values.get(i++);
            	if( value instanceof Integer ){
            		pstmt.setInt(i, (Integer)value);
            	}
            	else if( value instanceof Long ){
            		pstmt.setLong(i, (Long)value);
            	}
            	else if( value instanceof Double ){
            		pstmt.setDouble(i, (Double)value);
            	}
            	else if( value instanceof Float ){
            		pstmt.setFloat(i, (Float)value);
            	}
            	else{
            		pstmt.setString(i, value.toString());
            	}
            }
           pstmt.addBatch();
           int result = pstmt.executeUpdate();
           log.info("Succeed to execute "+sql+" \r\n\tThe result is "+result);
			JSONObject account = operator;
			String s = String.format("%s%s【%s】", account.getString("username"), logformat, myid);
			SyslogClient.write(
					LogType.操作日志,
					LogSeverity.INFO,
					s,
					account.getString("username"),
					logformat,
					null,
					null);
		}
		catch(Exception e)
		{
           log.info("Failed to execute "+sql, e);
			throw new Exception(String.format("向数据库[%s]新增记录失败因为异常%s", jdbcDatabase, e.toString()));
		}
		finally
		{
			if( pstmt != null )
				try
				{
					pstmt.close();
				}
				catch (SQLException e1)
				{
				}
        	if( connection != null )
				try
				{
					connection.close();
				}
				catch (SQLException e1)
				{
				}
		}
	}
	
	/**
	 * 删除行数据
	 * @throws Exception
	 */
	private void deleteDiggRow() throws Exception{

		if( driverClass != null ) {
			this.deleteDiggSqlRow();
		}
		else if( mongoHost != null ) {
			this.deleteDiggMongoRow();
		}
	}

	/**
	 * 删除数据
	 * @param data
	 * @throws Exception
	 */
	private void deleteDiggMongoRow() throws Exception{
		String recIndx = getRecIndx();
		if( myid == null || myid.isEmpty() ){
			throw new Exception(String.format("没有要删除行数据的主键[%s]数据", recIndx));
		}

		HashMap<String, Element> dataCells = new HashMap<String, Element>();
		Element filedNode = XMLParser.getFirstChildElement( filednode );
		this.setDataAttributes(filedNode, dataCells);
		Bson _id = getMongoRecIndx(recIndx, dataCells, null);
        MongoClient mongo = null;
		try{
    		mongo = getMongoClient(this.mongoHost, this.mongoPort,this.mongoUsername,this.mongoPassword,this.mongoDatabase);
    		MongoDatabase db = mongo.getDatabase(this.mongoDatabase);
    		MongoCollection<Document> col = db.getCollection(this.mongoFrom);
    		DeleteResult result = col.deleteOne(_id);
    		log.info("Succeed to execute delete "+_id.toString()+" \r\n\tThe result is "+result);
			JSONObject account = operator;
			String s = String.format("%s%s【%s】", account.getString("username"), logformat, myid);
			SyslogClient.write(
					LogType.操作日志,
					LogSeverity.INFO,
					s,
					account.getString("username"),
					logformat,
					null,
					null);
		}
		catch(Exception e)
		{
           log.info("Failed to execute delete "+myid, e);
			throw new Exception(String.format("从芒果数据库[%s]删除数据记录[%s]失败因为异常%s", mongoDatabase, myid, e.toString()));
		}
		finally
		{
    		if(mongo != null)
    		{
    			mongo.close();
    		}
		}
	}
	/**
	 * 删除数据
	 * @param data
	 * @throws Exception
	 */
	private void deleteDiggSqlRow() throws Exception{
		String recIndx = getRecIndx();
		if( myid == null || myid.isEmpty() ){
			throw new Exception(String.format("没有要删除行数据的主键[%s]数据", recIndx));
		}
		HashMap<String, Element> dataCells = new HashMap<String, Element>();
		Element filedNode = XMLParser.getFirstChildElement( filednode );
		this.setDataAttributes(filedNode, dataCells);
		Element diggNode = XMLParser.getFirstChildElement(datamodelnode);
		String clause = "";
		String dataType = null;
        for(Element dataNode = XMLParser.getFirstChildElement(diggNode); dataNode != null; dataNode = XMLParser.nextSibling(dataNode))
        {
			String dataIndx = dataNode.getNodeName();
			if( !recIndx.equalsIgnoreCase(dataIndx) ){
				continue;
			}
			filedNode = dataCells.get(dataIndx);
			dataType = XMLParser.getElementAttr(dataNode, "dataType");
			if( dataType.isEmpty() ){
				dataType = XMLParser.getElementAttr(filedNode, "dataType");
			}
			if( dataType == null || dataType.isEmpty() ) {
				throw new Exception(String.format("主键[%s]没有设置数据类型", dataIndx));
			}
			clause = XMLParser.getElementAttr(dataNode, "clause");
			if( clause.isEmpty() ){
				clause = XMLParser.getElementAttr(dataNode, "column");
			}
			if( clause.isEmpty() ){
				clause = dataIndx;
			}
			break;
		}
        if( "string".equals(dataType) ){
        	dataType = "'";
        }
        else{
        	dataType = "";
        }
        
		String sql = String.format("DELETE FROM %s WHERE %s=%s%s%s", jdbcFrom, clause, dataType, myid, dataType);
		Connection connection = null;
		Statement stmt = null;
		try{
			connection = DriverManager.getConnection(jdbcUrl ,jdbcUsername, jdbcUserpswd);
            stmt = connection.createStatement();
            int result = stmt.executeUpdate(sql);
        	log.info("Succeed to execute "+sql+" \r\n\tThe result is "+result);
		}
		catch(Exception e)
		{
           log.info("Failed to execute "+sql, e);
			throw new Exception(String.format("向数据库[%s]新增记录失败因为异常%s", jdbcDatabase, e.toString()));
		}
		finally
		{
			if( stmt != null )
				try
				{
					stmt.close();
				}
				catch (SQLException e1)
				{
				}
        	if( connection != null )
				try
				{
					connection.close();
				}
				catch (SQLException e1)
				{
				}
		}
	}
	/**
	 * 设置数据库
	 * @param src
	 * @param sObject
	 * @throws Exception
	 */
	public void presetDatabase(Element digg) throws Exception
	{
		String src = XMLParser.getElementAttr(digg, "src");
		if( src == null || src.isEmpty() ) src = "local";
		StringBuffer sb = new StringBuffer();
		sb.append("Receive the request, the parameters of below ");
    	if( "local".equalsIgnoreCase(src) )
    	{
			Properties jdbcConfig = new Properties();
	    	jdbcConfig.load(new FileInputStream(new File(PathFactory.getWebappPath(), "WEB-INF/classes/config/jdbc.properties")));
	    	jdbcUrl = jdbcConfig.getProperty("jdbc.url");
	    	jdbcUsername = jdbcConfig.getProperty("jdbc.username");
	    	jdbcUserpswd = jdbcConfig.getProperty("jdbc.password");
	    	driverClass = jdbcConfig.getProperty("jdbc.driverClass");
    		if( jdbcUserpswd == null ) jdbcUserpswd = "";
    		if( jdbcUrl == null || jdbcUrl.isEmpty() || jdbcUsername == null || jdbcUsername.isEmpty() || driverClass == "" || driverClass.isEmpty() )
    		{
    			throw new Exception("因为jdbc配置连接参数(h2)无效执行数据库加载");
    		}
    	}
		else if( src.startsWith("/cos/config/modules/") )
    	{
    		JSONObject datasource = ZKMgr.getZookeeper().getJSONObject(src, true);
    		if( datasource == null ||
				!datasource.has("dbtype") ||
				!datasource.has("dbaddr") ||
				!datasource.has("dbname") ||
				!datasource.has("dbusername") 
				)
    		{
    			throw new Exception("未发现正确数据源配置("+src+","+datasource+")无法进行数据查询");
    		}
    		String dbtype = datasource.getString("dbtype");
			String dbaddr = datasource.getString("dbaddr");
			String dbname = datasource.getString("dbname");
			String username = datasource.getString("dbusername");
			String password = datasource.has("dbpassword")?datasource.getString("dbpassword"):"";
			password = new String(Base64X.decode(password));
    		if( "mongo".equals(dbtype) )
    		{
    			String args[] = Tools.split(dbaddr, ":");
    			this.mongoHost = args[0];
    			this.mongoPort = Integer.parseInt(args[1]);
    			this.mongoUsername = username;
    			this.mongoPassword = password;
    			this.mongoDatabase = dbname;
    			this.mongoFrom = XMLParser.getElementAttr(digg, "from");
//    			mongoFrom = parameter(mongoFrom);
    		}
    		else if( "redis".equals(dbtype) )
    		{
//    			String args[] = Tools.split(dbaddr, ":");
//        		sObject.put("redis.host", args[0]);
//        		sObject.put("redis.port", Integer.parseInt(args[1]));
//        		sObject.put("redis.password", password);
    		}
    		else
    		{
    			if("h2".equalsIgnoreCase(dbtype))
    			{
    				driverClass = "org.h2.Driver";
    				jdbcUrl = "jdbc:h2:tcp://"+dbaddr+"/../h2/"+dbname;
    			}
    			else if("mysql".equalsIgnoreCase(dbtype))
    			{
    				driverClass = "com.mysql.jdbc.Driver";
    				jdbcUrl = "jdbc:mysql://"+dbaddr+"/"+dbname+"?lastUpdateConnt=true&amp;useUnicode=true&amp;characterEncoding=UTF-8";
    			}
    			else if("oracle".equalsIgnoreCase(dbtype))
    			{
    				driverClass = "oracle.jdbc.driver.OracleDriver";
    				jdbcUrl = "jdbc:oracle:thin:@"+dbaddr+":"+dbname;
    			}
    			else
    			{
        			throw new Exception("不支持的数据源类型"+dbtype);
    			}
    			jdbcDatabase = dbname;
    			jdbcUsername = username;
    			jdbcUserpswd = password;
    			this.jdbcFrom = XMLParser.getElementAttr(digg, "from");
//    			jdbcFrom = parameter(jdbcFrom);
        		if( jdbcUrl == null || jdbcUrl.isEmpty() || jdbcUsername == null || jdbcUsername.isEmpty() || driverClass == "" || driverClass.isEmpty() )
        		{
        			throw new Exception("因为jdbc配置连接参数("+src+")无效执行数据库加载");
        		}
    		}
    	}
	}

	/**
	 * 
	 */
	public MongoClient getMongoClient(String host, int port, String username, String password, String database) throws Exception
	{
		List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
		MongoCredential credential = MongoCredential.createScramSha1Credential(username, database, password.toCharArray());
		credentialsList.add(credential);
		ServerAddress serverAddress = new ServerAddress(host, port); 
		return new MongoClient(serverAddress, credentialsList);
	}
}
