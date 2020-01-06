package com.focus.cos.web.action;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.ZKMgr;
import com.focus.util.Base64X;
import com.focus.util.Tools;
import com.focus.util.XMLParser;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public abstract class DataModel {
	/*数据模型结构定义*/
	protected StringBuffer dataM = new StringBuffer();
	/*排序字段*/
	private StringBuffer sortIndx = new StringBuffer();
	/*排序顺序*/
	private StringBuffer sortDir = new StringBuffer();
	/*数据模型的类型 digg json zookeeper dimension(report)*/
	protected String type;
	/*外部传参数*/
	protected HttpServletRequest request;
	/*本地数据数组 用于存储列表数据*/
	protected JSONArray localArray;
	protected JSONObject localObject;
	/*外部传参数*/
	protected HashMap<String, String> values = new HashMap<String, String>();//记录映射值的数据
	/*JDBC的连接*/
	protected String dbtype;//数据库类型
//	protected String jdbcUrl;
//	protected String jdbcUsername;
//	protected String jdbcUserpswd;
//	protected String driverClass;
	/*数据模型的渲染脚本*/
	protected String javascript;
	/*数据模型的扩展参数*/
	protected String plusDataIndx;
	/**/
	protected TemplateChecker checker;
	/*数据模型深度*/
	protected int depth;
	/*唯一关键字*/
	protected String recIndx;
	/*排序配置*/
	protected JSONArray sortColumns;
	/*位置*/
	protected String location;
	/*数据样式*/
	protected JSONObject dataStyle = new JSONObject();
	/*数据驱动配置*/
	protected JSONObject diggObject = new JSONObject();
	/*JDBC的连接配置*/
	protected String jdbcUrl;
	protected String jdbcUsername;
	protected String jdbcUserpswd;
	protected String driverClass;
	/*芒果的连接配置*/
	protected String mongoHost;
	protected int mongoPort;
	protected String mongoDatabase;
	protected String mongoUsername;
	protected String mongoUserpswd;
	/**/
	protected String redisHost;
	protected int redisPort;
	protected String redisPassword;
	
	public JSONObject getDiggObject(){
		return diggObject;
	}
	
	/**
	 * 数据模型构建
	 * @param type
	 * @param request
	 * @param values
	 */
	public DataModel(String type, HttpServletRequest request, HashMap<String, String> values, TemplateChecker checker){
		this.type = type;
		this.request = request;
		this.values = values;
		this.checker = checker;
		location = "local";
	}
	
	/**
	 * 根据排序字段初始化
	 * @param recIndx
	 * @param sortColumns
	 */
	public void initialize(String recIndx, JSONArray sortColumns, String plusDataIndx, String javascript)
	{
		this.sortColumns = sortColumns;
		this.recIndx = recIndx;
		this.plusDataIndx = plusDataIndx;
		this.javascript = javascript;
		this.depth = plusDataIndx==null?1:2;
        for(int i = 0;i < sortColumns.length(); i++)
        {
        	JSONObject sort = sortColumns.getJSONObject(i);
        	if( i > 0 ) sortIndx.append(",");
        	sortIndx.append("'"+sort.getString("dataIndx")+"'");
        	if( i > 0 ) sortDir.append(",");
        	sortDir.append("'"+sort.getString("dir")+"'");
        }
        String dataType = "JSON";
        dataM.append("\r\n\tdataType: '"+dataType+"'");
		dataM.append(",\r\n\trecIndx: '"+recIndx+"'");
        if( sortIndx.length()>0 ) dataM.append(",\r\n\tsortIndx: ["+sortIndx+"]");
        if( sortDir.length()>0 ) dataM.append(",\r\n\tsortDir: ["+sortDir+"]");
        test(depth, "数据模型类型(type:%s), 主键(recIndx:%s), 排序字段(sortIndx:%s), 排序方向(sortDir:%s), 是否是主表模型(%s)  是否有处理脚本(%s, 配置datamodel节点的CDATA)", type, recIndx, sortIndx, sortDir, plusDataIndx==null, javascript.length()>0);
	}

	/**
	 * 构建
	 */
	public abstract void build(Node datamodelNode, JSONObject dataColumns, JSONArray titleColumns, String gridxml, StringBuilder gridObj) throws Exception;
	/**
	 * 完成
	 */
	public abstract void finish();

	/**
	 * 从HTTP中获取条件参数
	 * @param where
	 * @return
	 */
	private String buildSqlWhereParamter(StringBuffer sql){
		String sqlwhere = this.request.getParameter("sqlwhere");
		if( sqlwhere == null ){
			sqlwhere = "";
		}
		if( sql == null ){
			sql = new StringBuffer();
		}
		if( sql.length() > 0 && !sqlwhere.isEmpty() ){
			sql.append(" and ");
		}
		if( !sqlwhere.isEmpty() ){
			sql.append("(");
			sql.append(sqlwhere);
			sql.append(")");
		}
		return sql.toString();
	}
	/**
	 * 处理SQL的条件参数，可接受外部传参数
	 * @param where
	 * @return
	 */
	private String buildSqlWhere(String where, JSONObject digg) throws Exception{
		if ((where.isEmpty()) || (where == null)){
			
			return digg.has("parameter")?buildSqlWhereParamter(null):"";
		}
		where = this.checker.parameter(where, this.request, true);
		StringBuffer sql = new StringBuffer();
		StringBuilder function = null;
		StringBuilder parameter = null;
		for (int i = 0; i < where.length(); i++) {
		  char c = where.charAt(i);
		  if (c == '$')
		  {
		    function = new StringBuilder();

		  }
		  else if (function == null) {
		    sql.append(c);

		  }
		  else if (c == '(')
		  {
		    parameter = new StringBuilder();
		  }
		  else if (c == ')')
		  {
		    if ("time".equalsIgnoreCase(function.toString()))
		    {
		      String timeformat = digg.has("timeformat") ? digg.getString("timeformat") : "yyy-MM-dd";
		      long timestamp = System.currentTimeMillis();
		      if (Tools.isNumeric(parameter.toString())) {
		        timestamp = Long.parseLong(parameter.toString());
		      }
		      Calendar calendar = Calendar.getInstance();
		      calendar.setTimeInMillis(timestamp);
		      String time = Tools.getFormatTime(timeformat, calendar.getTimeInMillis());
		      if ("oracle".equals(this.dbtype)) {
		        sql.append("TO_DATE('" + time + "', 'yyyy-mm-dd hh24:mi:ss')");
		      }
		      else {
		        sql.append(time);
		      }
		    }
		    else if ("day".equalsIgnoreCase(function.toString()))
		    {
		      int day = 0;
		      if (Tools.isNumeric(parameter.toString())) {
		        day = Integer.parseInt(parameter.toString());
		      }
		      Calendar calendar = Calendar.getInstance();
		      calendar.add(5, day);
		      String time = Tools.getFormatTime("yyyy-MM-dd", calendar.getTimeInMillis());
		      if ("oracle".equals(this.dbtype)) {
		        sql.append("TO_DATE('" + time + "', 'yyyy-mm-dd hh24:mi:ss')");
		      }
		      else {
		        sql.append(time);
		      }
		    }
		    else {
		      throw new Exception("在类型为sql的digg中where配置中发行不支持的函数" + function + "(" + parameter + ")");
		    }
		    function = null;
		    parameter = null;

		  }
		  else if (parameter != null) { parameter.append(c);
		  } 
		  else { function.append(c);
		  }
		}

		return digg.has("parameter")?buildSqlWhereParamter(sql):sql.toString();
	}
	/**
	 * 设置查询SQL
	 * @param sqlNode
	 * @param sql
	 * @param dataColumns
	 * @throws Exception
	 */
	protected void setQuerySql(Node diggNode, JSONObject dataColumns, StringBuffer sql, JSONObject digg, int diggIndx) throws Exception
	{
		sql.append("select ");
    	String from = XMLParser.getElementAttr(diggNode, "from");
    	String groupby = XMLParser.getElementAttr( diggNode, "groupby" );
    	String where = XMLParser.getElementAttr(diggNode, "inner");
    	if( where.isEmpty() ) where = XMLParser.getElementAttr(diggNode, "where");
    	String parameter = XMLParser.getElementAttr(diggNode, "parameter");
    	if( "true".equalsIgnoreCase(parameter) ){
    		digg.put("parameter", true);
    	}
		int i = 0;
		ArrayList<Element> columnNodes = new ArrayList<Element>();
		Element columnNode = XMLParser.getFirstChildElement(diggNode);
		HashMap<String, String> groupMybe = new HashMap<String, String>();
        while( columnNode != null )
        {
        	JSONObject cell = null;
        	String dataIndx = "";
        	//从表中获取列对象节点
        	if( dataColumns.has(columnNode.getNodeName()) )
        	{
        		cell = dataColumns.getJSONObject(columnNode.getNodeName());
        		dataIndx = cell.has("dataIndx")?cell.getString("dataIndx"):"";
        	}
        	else
        	{//如果表中没有列对象节点，那么构建新的节点
        		cell = new JSONObject();
        		String dataType = XMLParser.getElementAttr(columnNode, "dataType");
        		cell.put("dataType", dataType);
        		cell.put("dataIndx", columnNode.getNodeName());
        		dataColumns.put(columnNode.getNodeName(), cell);
        		if( !dataType.isEmpty() ) dataIndx = columnNode.getNodeName();
            	if( "int".equalsIgnoreCase(dataType) ||
        			"integer".equalsIgnoreCase(dataType) )
            	{
                	dataStyle.put(dataIndx, 0);
            	}
            	else if( "long".equalsIgnoreCase(dataType) )
            	{
                	dataStyle.put(dataIndx, 0L);
            	}
            	else if( "number".equalsIgnoreCase(dataType) ||
            			 "float".equalsIgnoreCase(dataType) ||
            			 "double".equalsIgnoreCase(dataType) )
            	{
                	dataStyle.put(dataIndx, 0.0);
            	}
            	else if( !dataType.isEmpty() ) {
                	dataStyle.put(dataIndx, dataType);
            	}
        	}
    		cell.put("diggIndx", diggIndx);
        	JSONObject columns = null;
        	if( cell.has("column") ){
        		columns = cell.getJSONObject("column");
        	}
        	else{
        		columns = new JSONObject();
        		cell.put("column", columns);
        	}
    		String clause = XMLParser.getElementAttr(columnNode, "column");
    		clause = clause.isEmpty()?XMLParser.getElementAttr(columnNode, "clause"):clause;
    		if( !clause.isEmpty() ){
    			columns.put(String.valueOf(diggIndx), clause);
    		}
    		else{
    			clause = dataIndx;
    		}
    		String format = XMLParser.getElementAttr(columnNode, "format");
    		if( !format.isEmpty() )	cell.put("format", format);
        	String defaultValue = XMLParser.getElementAttr(columnNode, "default");
        	if( !defaultValue.isEmpty() )
        	{
        		JSONObject defaultObject = new JSONObject();
        		if( digg.has("default") )
        		{
        			defaultObject = digg.getJSONObject("default");
        		}
        		defaultObject.put(columnNode.getNodeName(), defaultValue);
        		digg.put("default", defaultObject);
        	}
    		String exclude = XMLParser.getElementAttr(columnNode, "exclude");
        	if( !clause.isEmpty() && !"true".equalsIgnoreCase(exclude) )
        	{
            	columnNodes.add(columnNode);
	        	columns.put(String.valueOf(diggIndx), clause);
	            if( !groupby.isEmpty() ){
	            	String notgroup = XMLParser.getElementAttr(columnNode, "notgroup");
	            	if("true".equals(notgroup)){
//	            		loadSqlDiggJoins(columnNode, digg);
	                	columnNode = XMLParser.getNextSibling(columnNode);
	            		continue;
	            	}
	            	clause = checker.parameter(clause, request, true);
	            }
	        	if( i > 0 ) sql.append(",");
//	        	if( "channel_code".equals(columnNode.getNodeName()) ){
//	        		System.err.println(clause);
//	        	}
	            sql.append(clause);
	        	sql.append(" AS ");
	        	sql.append(columnNode.getNodeName());
	        	i += 1;
	        	groupMybe.put(columnNode.getNodeName(), clause);
        	}
        	Element joinNode = XMLParser.getChildElementByTag(columnNode, "join");
        	while( joinNode != null )
        	{//如果列配置下有join节点，则加载join
            	JSONArray joins = null;
            	if( digg.has("joins") )
            	{
            		joins = digg.getJSONArray("joins");
            	}
            	else
            	{
            		joins = new JSONArray();
            		digg.put("joins", joins);
            	}
            	JSONObject join = createQueryJoin(columnNode.getNodeName(), joinNode);
            	String joinwhere = XMLParser.getElementAttr(joinNode, "where");
            	if (!joinwhere.isEmpty()) {
                	joinwhere = buildSqlWhere(joinwhere, join);
                	join.put("where", joinwhere);
            	}
            	joins.put(join);
            	joinNode = XMLParser.nextSibling(joinNode);
        	}
        	columnNode = XMLParser.getNextSibling(columnNode);
        }
        sql.append(" from "+from);
        where = buildSqlWhere(where, digg);
        if (!where.isEmpty()) {
        	sql.append(" where " + where);
    	}
        StringBuffer sqlCount = null;
        if( !groupby.isEmpty() )
        {
        	digg.put("groupby", groupby);
    		//select count(distinct DATE_FORMAT(create_time, '%Y-%m-%d'), keyword) from search_log
        	if( dbtype.equalsIgnoreCase("h2") ){
            	sqlCount = new StringBuffer("select distinct ");
            	String args[] = Tools.split(groupby, ",");
            	for(i = 0; i < args.length; i++ ){
            		String arg = args[i].trim();
            		if( i > 0 ) sqlCount.append(',');
            		sqlCount.append(groupMybe.containsKey(arg)?groupMybe.get(arg):arg);
            	}
        		sqlCount.append(" from "+from);
        		if (!where.isEmpty()) {
        			sqlCount.append(" where " + where);
        		}
        	}
        	else{
            	sqlCount = new StringBuffer("select count(distinct ");
            	String args[] = Tools.split(groupby, ",");
            	for(i = 0; i < args.length; i++ ){
            		String arg = args[i].trim();
            		if( i > 0 ) sqlCount.append(',');
            		sqlCount.append(groupMybe.containsKey(arg)?groupMybe.get(arg):arg);
            	}
        		sqlCount.append(") from "+from);
        		if (!where.isEmpty()) {
        			sqlCount.append(" where " + where);
        		}
        	}
        }
        for(Element node : columnNodes)
        {
        	Element conditionNode = XMLParser.getChildElementByTag(node, "condition");
        	if( conditionNode != null )
        	{
        		JSONObject column = dataColumns.getJSONObject(node.getNodeName());
        		i = sql.length();
        		this.setQuerySqlWhere(conditionNode, column, sql, diggIndx);
            	if( sqlCount != null ){
            		sqlCount.append(sql.substring(i));
            	}
        	}
        }
        if( !groupby.isEmpty() )
        {
        	digg.put("sqlCount", sqlCount.toString());
        }
	}
	/**
	 * 
	 */
	public static MongoClient getMongoClient(String host, int port, String username, String password, String database) throws Exception
	{
		List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
		MongoCredential credential = MongoCredential.createScramSha1Credential(username, database, password.toCharArray());
		credentialsList.add(credential);
		ServerAddress serverAddress = new ServerAddress(host, port); 
		return new MongoClient(serverAddress, credentialsList);
	}
	/**
	 * 
	 * @param cellNode
	 */
	private List<JSONObject> loadGridMongoFor(Node e) throws Exception
	{
		ArrayList<JSONObject> list = new ArrayList<JSONObject>();
		String src = XMLParser.getElementAttr(e, "src");
    	String from = XMLParser.getElementAttr( e, "from" );
    	String database = XMLParser.getElementAttr( e, "database" );
    	JSONObject cfg = new JSONObject();
    	cfg.put("type", "mongo");
    	cfg.put("mongo.database1", database);//第一数据库
    	cfg.put("mongo.tablename", from);
    	preloadDatabase(src, cfg);
    	MongoClient mongo = null;
    	try
    	{
    		DataModel datamodel = new DataModel("", this.request, values, checker) {
				@Override
				public void finish() {
				}
				
				@Override
				public void build(Node datamodelNode, JSONObject dataColumns, JSONArray titleColumns, String gridxml, StringBuilder gridObj)
						throws Exception {
					
				}
			};
    		mongo = getMongoClient(mongoHost, mongoPort, mongoUsername, mongoUserpswd, mongoDatabase);
    		MongoDatabase db = mongo.getDatabase(database);
    		MongoCollection<Document> col = db.getCollection(from);

    		Node conditionNode = XMLParser.getChildElementByTag(e, "condition");
    		BasicDBObject condition = new BasicDBObject();
    		datamodel.setQueryMongoWhere(conditionNode, null, null, condition, 0);
    		col.find(condition);

			FindIterable<Document> find = col.find(condition);
    		Node sortNode = XMLParser.getChildElementByTag(e, "sort");
    		if( sortNode != null ){
    			String sortColumn = XMLParser.getElementAttr(sortNode, "column");
    			int dir = 1;
				if( "down".equalsIgnoreCase(XMLParser.getElementAttr(sortNode, "dir")) || "u".equalsIgnoreCase(XMLParser.getElementAttr(sortNode, "dir")) )
				{
					dir = -1;
				}
    			find = find.sort(new Document(sortColumn, dir));
    		}
    		MongoCursor<Document> cursor = find.iterator();
    		while( cursor.hasNext() )
    		{
    			Document doc = cursor.next();
    			list.add(new JSONObject(doc.toJson()));
    		}
    		return list;
    	}
    	catch(Exception e1)
    	{
    		throw e1;
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
	 * 加载循环日期
	 * @param e
	 * @return
	 * @throws Exception
	 */
	private List<JSONObject> loadGridDateFor(Node e) throws Exception
	{
		String format = XMLParser.getElementAttr(e, "format");
		if( format.isEmpty() ){
			format = "yyyy-MM-dd";
		}
		ArrayList<JSONObject> list = new ArrayList<JSONObject>();
		Element startNode = XMLParser.getChildElementByTag( e, "start" );
		Element endNode = XMLParser.getChildElementByTag( e, "end" );

		String start = XMLParser.getElementAttr(startNode, "column");
    	String end = XMLParser.getElementAttr(endNode, "column" );
		String startFormat = XMLParser.getElementAttr(startNode, "format");
    	if( startFormat.isEmpty() ){
    		startFormat = "yyyy-MM-dd";
    	}
		String endFormat = XMLParser.getElementAttr(endNode, "format" );
    	if( endFormat.isEmpty() ){
    		endFormat = "yyyy-MM-dd";
    	}
    	start = checker.parameter(start, request, false);
    	end = checker.parameter(end, request, false);
		Calendar c = Calendar.getInstance();
    	if( start.isEmpty() || end.isEmpty() ){
    		c.add(Calendar.DAY_OF_MONTH, -6);//半年内的数据
    		start = Tools.getFormatTime(startFormat, c.getTimeInMillis());
    		end = Tools.getFormatTime(endFormat, System.currentTimeMillis());
    	}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date startDate = sdf.parse(start);
		Date endDate = sdf.parse(end);
		c.setTime(endDate);
		for( int i = 0; c.getTimeInMillis() > startDate.getTime(); i ++ ){
			c.setTime(endDate);
			c.add(Calendar.DAY_OF_MONTH, -i);
			JSONObject obj = new JSONObject();
			obj.put("date", Tools.getFormatTime(format, c.getTimeInMillis()));
			obj.put("start", c.getTimeInMillis());
			obj.put("end", c.getTimeInMillis()+Tools.MILLI_OF_DAY);
			list.add(obj);
		}
    	return list;
	}
	/**
	 * 加载月份
	 * @param e
	 * @return
	 * @throws Exception
	 */
	private List<JSONObject> loadGridMonthFor(Node e) throws Exception
	{
		String format = XMLParser.getElementAttr(e, "format");
		if( format.isEmpty() ){
			format = "yyyy-MM";
		}
		ArrayList<JSONObject> list = new ArrayList<JSONObject>();
		Element startNode = XMLParser.getChildElementByTag( e, "start" );
		Element endNode = XMLParser.getChildElementByTag( e, "end" );

		String start = XMLParser.getElementAttr(startNode, "column");
    	String end = XMLParser.getElementAttr(endNode, "column" );
		String startFormat = XMLParser.getElementAttr(startNode, "format");
    	if( startFormat.isEmpty() ){
    		startFormat = "yyyy-MM";
    	}
		String endFormat = XMLParser.getElementAttr(endNode, "format" );
    	if( endFormat.isEmpty() ){
    		endFormat = "yyyy-MM";
    	}
    	start = checker.parameter(start, request, false);
    	end = checker.parameter(end, request, false);
		Calendar c = Calendar.getInstance();
    	if( start.isEmpty() || end.isEmpty() ){
    		c.add(Calendar.MONTH, -5);//半年内的数据
    		start = Tools.getFormatTime(startFormat, c.getTimeInMillis());
    		end = Tools.getFormatTime(endFormat, System.currentTimeMillis());
    	}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		Date startDate = sdf.parse(start);
		Date endDate = sdf.parse(end);
		c.setTime(endDate);
		for( int i = 0; c.getTimeInMillis() > startDate.getTime(); i ++ ){
			c.setTime(endDate);
			c.add(Calendar.MONTH, -i);
			JSONObject obj = new JSONObject();
			obj.put("month", Tools.getFormatTime(format, c.getTimeInMillis()));
			obj.put("start", c.getTimeInMillis());
			c.add(Calendar.MONTH, 1);
			obj.put("end", c.getTimeInMillis());
			list.add(obj);
		}
    	return list;
	}
	/**
	 * 设置芒果的统计
	 * @param diggNode
	 * @param dataColumns
	 * @param digg
	 * @param diggIndx
	 * @throws Exception
	 */
	private void setMongoCounts(Node diggNode, JSONObject dataColumns, JSONObject digg)
		 throws Exception{
        Node childcellNode = XMLParser.getFirstChildElement(diggNode);
        if( childcellNode == null ){
        	return;
        }
        JSONArray counts = new JSONArray();
    	if( childcellNode.getNodeName().equalsIgnoreCase("gd:for") ){
    		//支持for循环产生grid节点
    		String type = XMLParser.getElementAttr( childcellNode, "type" );
    		List<JSONObject> list = null;
    		Node node = XMLParser.getChildElementByTag(childcellNode, type);
    		if( type.equalsIgnoreCase("mongo") ) {
                list = loadGridMongoFor(node);
    		}
    		else if( type.equalsIgnoreCase("date") ) {
                list = loadGridDateFor(node);
    		}
    		else if( type.equalsIgnoreCase("month") ) {
                list = loadGridMonthFor(node);
    		}
        	Element dataElement = XMLParser.getNextSibling(node);
            for( ; dataElement != null; dataElement = XMLParser.nextSibling(dataElement))
            {
        		for( JSONObject e: list )
        		{
        			this.setMongoCounts(counts, dataElement, e);
        		}
            }
    	}
    	else{
            for( ; childcellNode != null; childcellNode = XMLParser.getNextSibling(childcellNode))
            {
    			this.setMongoCounts(counts, childcellNode, null);
            }
    	}
    	digg.put("counts", counts);
	}

	/**
	 * 得到字符串中的参数变量
	 * @param value
	 * @return
	 */
	public static String getVariable(String value, JSONObject values){
		int i = value.indexOf("{{");
		int j = value.lastIndexOf("}}");
		if( i == -1 || j == -1 ){
			return value;
		}
		StringBuffer sb = new StringBuffer(value);
		String var = value.substring(i+2, j);
		Object val = values.has(var)?values.get(var):null;
		if( val != null ) {
			sb.delete(i, j+2);
			sb.insert(i, val.toString());
		}
		return sb.toString();
	}
	
	private void setMongoCounts(JSONArray counts, Node dataNode, JSONObject keymap)
	{
		String tagName = dataNode.getNodeName();
		JSONObject field = new JSONObject();
		if( "count".equalsIgnoreCase(tagName) ){
			field.put("type", "count");
			BasicDBObject condition = new BasicDBObject();
    		field.put("column", getVariable(XMLParser.getElementAttr( dataNode, "column" ), keymap));
    		Element conditionNode = XMLParser.getChildElementByTag(dataNode, "condition");
        	if( conditionNode != null )
        	{
        		this.setQueryMongoWhere(conditionNode, null, keymap, condition, 0);
        	}
            if( !condition.isEmpty() )
            	field.put("condition", condition.toString());
            counts.put(field);
		}
		else if( "sum".equalsIgnoreCase(tagName) ){
			field.put("type", "sum");
			BasicDBObject condition = new BasicDBObject();
    		field.put("column", getVariable(XMLParser.getElementAttr( dataNode, "column" ), keymap));
    		Element conditionNode = XMLParser.getChildElementByTag(dataNode, "condition");
        	if( conditionNode != null )
        	{
        		this.setQueryMongoWhere(conditionNode, null, keymap, condition, 0);
        	}
            if( !condition.isEmpty() )
            	field.put("condition", condition.toString());
            counts.put(field);
		}
		else if( "eval".equalsIgnoreCase(tagName) ){
			field.put("type", "eval");
    		field.put("column", getVariable(XMLParser.getElementAttr( dataNode, "column" ), keymap));
    		field.put("dataType", XMLParser.getElementAttr( dataNode, "dataType" ));
    		field.put("expression", XMLParser.getElementAttr(dataNode, "expression"));
            counts.put(field);
		}
	}
	/**
	 * 设置
	 * @param diggNode
	 * @param dataColumns
	 * @throws Exception
	 */
	protected void setQueryMongo(Node diggNode, JSONObject dataColumns, JSONObject digg, int diggIndx) throws Exception
	{
		String count = XMLParser.getElementAttr( diggNode, "count" );
		if( !count.isEmpty() ){
			if( count.equals("*") ){
				this.setMongoCounts(diggNode, dataColumns, digg);
				return;
			}
			else{
				digg.put("count", count);//count表示存储结果的对象名称
			}
		}
		String last = XMLParser.getElementAttr( diggNode, "last" );
		if( !last.isEmpty() ){
			digg.put("last", last);//last表示最后一个对象
		}
		
		String aggregate = XMLParser.getElementAttr( diggNode, "aggregate" );
		JSONObject group = null;
		if( !aggregate.isEmpty() ){
			String[] args = Tools.split(aggregate, ":");
			String key = args[0];
			String val = args.length>1?args[1]:args[0];
			group = new JSONObject();
			JSONObject groupby = new JSONObject();
			groupby.put(key, "$"+val);
			group.put("_id", groupby);
			digg.put("group", group);
		}

		BasicDBObject condition = new BasicDBObject();
		Element columnNode = XMLParser.getFirstChildElement(diggNode);
        while( columnNode != null )
        {
        	String columnName = columnNode.getNodeName();
    		String columnValue = XMLParser.getElementAttr( columnNode, "column" );
        	columnValue = columnValue.isEmpty()?columnName:columnValue;
        	JSONObject cell = null;
        	String dataType = null;
        	if( !dataColumns.has(columnName) || 
        		 (dataColumns.getJSONObject(columnName).has("dataType") &&
        		 dataColumns.getJSONObject(columnName).getString("dataType").isEmpty())
        	  )
        	{
        		cell = new JSONObject();
        		dataType = XMLParser.getElementAttr( columnNode, "dataType", "string" );
        		cell.put("dataType", dataType);
        		cell.put("dataIndx", columnValue);
        		dataColumns.put(columnName, cell);
            	if( "int".equalsIgnoreCase(dataType) ||
        			"integer".equalsIgnoreCase(dataType) ){
                	dataStyle.put(columnName, 0);
            	}
            	else if( "long".equalsIgnoreCase(dataType) ){
                	dataStyle.put(columnName, 0L);
            	}
            	else if( "number".equalsIgnoreCase(dataType) ||
            			 "float".equalsIgnoreCase(dataType) ||
            			 "double".equalsIgnoreCase(dataType) ){
                	dataStyle.put(columnName, 0.0);
            	}
            	else if( !dataType.isEmpty() ){
                	dataStyle.put(columnName, dataType);
            	}
        	}
        	else{
        		cell = dataColumns.getJSONObject(columnName);
        		dataType = cell.has("dataType")?cell.getString("dataType"):null;
        	}
        	
        	JSONObject columns = null;
        	if( !digg.has("columns") ){
        		columns = new JSONObject();
        		digg.put("columns", columns);
        	}
        	else{
        		columns = digg.getJSONObject("columns");
        	}
        	JSONObject column = columns.has(columnName)?columns.getJSONObject(columnName):new JSONObject();
        	columns.put(columnName, column);
        	column.put("dataIndx", columnValue);
        	if( dataType != null ){
        		column.put("dataType", dataType);
        	}
        	if( group != null ){
        		String operator = XMLParser.getElementAttr( columnNode, "operator" );
        		if( !operator.isEmpty() ){
        			cell.put("operator", operator);
            		JSONObject o = new JSONObject();
            		if(Tools.isNumeric(columnValue)){
                		o.put(operator, Integer.parseInt(columnValue));
            		}
            		else{
            			o.put(operator, "$"+columnValue);
            		}
            		group.put(columnName, o);
            		column.put("dataIndx", o);
        		}
        	}
        	
    		String format = XMLParser.getElementAttr( columnNode, "format" );
    		if( !format.isEmpty() ) cell.put("format", format);

        	Element conditionNode = XMLParser.getChildElementByTag(columnNode, "condition");
        	if( conditionNode != null )
        	{
        		this.setQueryMongoWhere(conditionNode, column, null, condition, diggIndx);
        	}
        	
        	Element joinNode = XMLParser.getChildElementByTag(columnNode, "join");
        	JSONArray joins = null;
        	if( digg.has("joins") )
        	{
        		joins = digg.getJSONArray("joins");
        	}
        	else
        	{
        		joins = new JSONArray();
        		digg.put("joins", joins);
        	}
        	while( joinNode != null )
        	{//如果列配置下有join节点，则加载join
        		JSONObject join = this.createQueryJoin(columnName, joinNode);
//            	String joinwhere = XMLParser.getElementAttr(joinNode, "where");
//            	if (!joinwhere.isEmpty()) {
//	            	joinwhere = buildSqlWhere(joinwhere, join);
//	            	join.put("where", joinwhere);
//            	}
            	joins.put(join);
            	joinNode = XMLParser.nextSibling(joinNode);
        	}
        	
        	//TODO: 添加芒果条件
        	columnNode = XMLParser.getNextSibling(columnNode);
        }
        if( !condition.isEmpty() )
        	digg.put("condition", condition.toString());
	}
	
	/**
	 * 得到column字段
	 * @param cell
	 * @param diggIndx
	 * @return
	 */
	private String getColumnValue(JSONObject cell, int diggIndx){
		if( cell.has("column") ){
			JSONObject column = cell.getJSONObject("column");
			if( column.has(String.valueOf(diggIndx)) ){
				return column.getString(String.valueOf(diggIndx));
			}
		}
		return "";
	}

	/**
	 * 设置预设的查询条件
	 * @param columnNode
	 * @param sql
	 */
	protected void setQuerySqlWhere(Node conditionNode, JSONObject cell, StringBuffer sql, int diggIndx)
	{
		if( conditionNode == null ) return;
//		<condition compound='and' method='&lt;&gt;' value='admin' quote='true'/>
		String compound = XMLParser.getElementAttr(conditionNode, "compound");
		if( compound.isEmpty() ) return;
		String method = XMLParser.getElementAttr(conditionNode, "method");
		if( method.isEmpty() ) return;
		String value = XMLParser.getElementAttr(conditionNode, "value");
		if( value.isEmpty() ) return;
		String columnName = XMLParser.getElementAttr(conditionNode, "column");
		String dataType = XMLParser.getElementAttr(conditionNode, "dataType");
		boolean quote = true;
		if( cell != null )
		{
			if( columnName.isEmpty() )
			{
				columnName = getColumnValue(cell, diggIndx);//cell.has("column")?cell.getString("column"):"";
				if( columnName.isEmpty() )
				{
					String dataIndx = cell.getString("dataIndx");
					columnName = dataIndx;
				}
			}
			if( dataType.isEmpty() ) {
				dataType = cell.has("dataType")?cell.getString("dataType"):"";
			}
			if( dataType.isEmpty() ){
				dataType = cell.has("dataType")?cell.getString("dataType"):"";
			}
		}
		quote = !dataType.equals("int")&&
				!dataType.equalsIgnoreCase("integer")&&
				!dataType.equalsIgnoreCase("long")&&
				!dataType.equalsIgnoreCase("boolean")&&
				!dataType.equalsIgnoreCase("float")&&
				!dataType.equalsIgnoreCase("double")&&
				!dataType.equalsIgnoreCase("number");
    	
    	if( method.equalsIgnoreCase("IN") )
    	{
    		method = " IN ";
    		if( value.startsWith("%") && value.endsWith("%") )
        	{//根据产生的数据动态生成条件
    			JSONArray in = cell.has("in")?cell.getJSONArray("in"):new JSONArray();
    			in.put(value);
    			cell.put("in", in);
        	}
    		else
    		{
    			value = getQuerVariable(value, null);
    			if(value == null ) return;
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
			value = getQuerVariable(value, null);
			if(value == null ) return;
			if( method.trim().toLowerCase().equals("like") ){
	    		value = ("'%")+value+("%'");
	    		method = " "+method;
			}
			else if( value.equalsIgnoreCase("null") && ( method.trim().toLowerCase().equals("=") || method.trim().toLowerCase().equals("equal")) ){
				value = "null";
			}
			else{
				value = (quote?"'":"")+value+(quote?"'":"");
			}
    	}
    	
		sql.append(" ");
		if( sql.indexOf("where") == -1 )
		{
			sql.append("where");
		}
		else
		{
			sql.append(compound);
		}

		sql.append("(");
		if( value.equalsIgnoreCase("null") ){
			sql.append("(ISNULL(");
			sql.append(columnName);
    		sql.append(")");
    		if( quote ){
        		sql.append(" or ");
    			sql.append(columnName);
        		sql.append("=");
        		sql.append("''");
    		}
    		sql.append(")");
		}
		else{
			sql.append(columnName);
			sql.append(method);
			sql.append(value);
		}
    	Node conditionChildNode = XMLParser.getChildElementByTag(conditionNode, "condition");
        for( ; conditionChildNode != null; conditionChildNode = XMLParser.nextSibling(conditionChildNode) )
        {
    		this.setQuerySqlWhere(conditionChildNode, cell, sql, diggIndx);
        }
		sql.append(")");
	}
	
	/**
	 * 得到查询变量
	 * @param value
	 */
	private String getQuerVariable(String value, JSONObject keymap)
	{
		int i = value.indexOf('#');
		if( i != -1 ){
			int j = value.lastIndexOf('#');
			String key = null;
			if( i == j ){
				return value;
			}
			String value0 = value.substring(0, i);
			String value1 = value.substring(j+1);
			key = value.substring(i+1, j);
			String val = request.getParameter(key);
			key = "#"+key+"#";
			if( val != null && !val.isEmpty() ){
				value = value0+val+value1;
			}
			else if( values.containsKey(key) ){
//			else if( values.containsKey(value) ){
				value = value0+values.get(key)+value1;
			}
			else{
				value = null;
			}
			return value;
    	}
		if(keymap == null ) {
			return value;
		}
		return getVariable(value, keymap);
	}
	/**
	 * 过滤条件设置
	 * @param conditionNode
	 * @return
	 */
	private boolean filterCondition(Node conditionNode){
    	Node matchNode = XMLParser.getChildElementByTag(conditionNode, "match");
    	if( matchNode != null ){
    		String method = XMLParser.getElementAttr(matchNode, "method");
    		String value = XMLParser.getElementAttr(matchNode, "value");
    		String column = XMLParser.getElementAttr(matchNode, "column");
    		if( !column.isEmpty() && !value.isEmpty() && !method.isEmpty() ){
    			String val = request.getParameter(column);
    			if( val == null || val.isEmpty() ){
    				if( values.containsKey(column) ){
	    				val = values.get(column);
	    			}
    				else{
    					return true;
    				}
    			}
    			if( val != null && !val.isEmpty() ){
    				if( method.endsWith("in") ){
    				}
    				else if( method.endsWith("nin") ){
    				}
    				else if( method.equalsIgnoreCase(">") || method.endsWith("gt") ) {
    				}
    				else if( method.equalsIgnoreCase("<") || method.endsWith("lt") ) {
    				}
    				else if( method.equalsIgnoreCase(">=") || method.endsWith("gte") ) {
    				}
    				else if( method.equalsIgnoreCase("<=") || method.endsWith("lte") ) {
    				}
    				else if( method.equalsIgnoreCase("!=") || method.endsWith("ne") ) {
    		    		return val.equals(value);//过滤
    				}
    				else if( method.equalsIgnoreCase("like") || method.endsWith("regex") ) {
    				}
    		    	else if( method.endsWith("eq") || method.equalsIgnoreCase("=") ){
    		    		return !val.equals(value);//不匹配的时候才不过滤
    		    	}
    			}
    		}
    		return true;
    	}
    	Node filterNode = XMLParser.getChildElementByTag(conditionNode, "filter");
    	if( filterNode != null ){
    		String method = XMLParser.getElementAttr(filterNode, "method");
    		String value = XMLParser.getElementAttr(filterNode, "value");
    		String column = XMLParser.getElementAttr(filterNode, "column");
    		if( !column.isEmpty() && !value.isEmpty() && !method.isEmpty() ){
    			String val = request.getParameter(column);
    			if( val == null || val.isEmpty() ){
    				if( value.equalsIgnoreCase("null") ){
    					return true;
    				}
    				if( values.containsKey(column) ){
	    				val = values.get(column);
	    			}
    			}
    			if( val != null && !val.isEmpty() ){
    				if( method.endsWith("in") ){
    				}
    				else if( method.endsWith("nin") ){
    				}
    				else if( method.equalsIgnoreCase(">") || method.endsWith("gt") ) {
    				}
    				else if( method.equalsIgnoreCase("<") || method.endsWith("lt") ) {
    				}
    				else if( method.equalsIgnoreCase(">=") || method.endsWith("gte") ) {
    				}
    				else if( method.equalsIgnoreCase("<=") || method.endsWith("lte") ) {
    				}
    				else if( method.equalsIgnoreCase("!=") || method.endsWith("ne") ) {
    		    		return !val.equals(value);
    				}
    				else if( method.equalsIgnoreCase("like") || method.endsWith("regex") ) {
    				}
    		    	else if( method.endsWith("eq") || method.equalsIgnoreCase("=") ){
    		    		return val.equals(value);
    		    	}
    			}
    		}
    	}
		return false;
	}
	/**
	 * 设置
	 * @param conditionNode
	 * @param cell 单元格配置表
	 * @param keymap 通配替换表
	 * @param condition
	 * @param diggIndx
	 */
	protected void setQueryMongoWhere(Node conditionNode, JSONObject cell, JSONObject keymap, BasicDBObject condition, int diggIndx){
		if( conditionNode == null ) return;
		if( filterCondition(conditionNode) ){
			return;//被过滤掉了
		}
		String compound = XMLParser.getElementAttr(conditionNode, "compound");
    	Node conditionChildNode = XMLParser.getChildElementByTag(conditionNode, "condition");
    	BasicDBList andor = null;
    	BasicDBObject parent = null;
    	if( conditionChildNode != null ){
    		if( compound.isEmpty() ) return;
    		andor = new BasicDBList();
    		parent = condition;
    		condition = new BasicDBObject();
    	}
		String method = XMLParser.getElementAttr(conditionNode, "method");
		if( method.isEmpty() ) return;
		String value = XMLParser.getElementAttr(conditionNode, "value");
		if( value.isEmpty() ) return;
		String columnName = XMLParser.getElementAttr(conditionNode, "column");
		String dataType = XMLParser.getElementAttr(conditionNode, "dataType");
		String dataType0 = null;
		if( cell != null )
		{
			if( columnName.isEmpty() )
			{
				columnName = this.getColumnValue(cell, diggIndx);//column.has("column")?column.getString("column"):"";
				if( columnName.isEmpty() )
				{
					String dataIndx = cell.getString("dataIndx");
					columnName = dataIndx;
				}
			}
			dataType0 = cell.has("dataType")?cell.getString("dataType"):"";
			if( dataType.isEmpty() ) {
				dataType = dataType0;
			}
		}
		if( method.equalsIgnoreCase("in") ){
			method = "$in";
		}
		else if( method.equalsIgnoreCase("not in") ){
			method = "$nin";
		}
		BasicDBObject _condition = null;
		if( condition.containsField(columnName) ){
			_condition = (BasicDBObject)condition.get(columnName);
		}
		else{
			_condition = new BasicDBObject();
		}
		
    	if( method.equalsIgnoreCase("$in") || method.equalsIgnoreCase("$nin") )
    	{
    		if( value.startsWith("%") && value.endsWith("%") )
        	{//根据产生的数据动态生成条件
    			if( cell != null ){
    				JSONArray ranges = cell.has("in")?cell.getJSONArray("in"):new JSONArray();
    				ranges.put(value);
    				cell.put(method, ranges);
    			}
    			_condition.put(method, value);
    			condition.put(columnName, _condition);
        	}
    		else
    		{
    			value = getQuerVariable(value, keymap);
    			if( value == null ) return;
    			String[] args = null;
    			if( value.indexOf(',') != -1 ){
        			args = Tools.split(value, ",");
    			}
    			else if( value.indexOf(' ') != -1 ){
        			args = Tools.split(value, " ");
    			}
    			else{
    				args = new String[]{value.trim()};
    			}
				BasicDBList ranges = new BasicDBList();
    			for(int i = 0; i < args.length; i++)
    			{
    				if( args[i].trim().isEmpty() ) continue;
    				ranges.add(getDataValue(dataType, args[i], dataType0));
    			}
    			_condition.put(method, ranges);
    			condition.put(columnName, _condition);
    		}
    	}
		else if( method.equalsIgnoreCase(">") || method.equalsIgnoreCase("$gt") )
		{
			value = getQuerVariable(value, keymap);	if( value == null ) return;
			_condition.put("$gt", getDataValue(dataType, value, dataType0));
			condition.put(columnName, _condition);
		}
		else if( method.equalsIgnoreCase("<") || method.equalsIgnoreCase("$lt") )
		{
			value = getQuerVariable(value, keymap);	if( value == null ) return;
			_condition.put("$lt", getDataValue(dataType, value, dataType0));
			condition.put(columnName, _condition);
		}
		else if( method.equalsIgnoreCase(">=") || method.equalsIgnoreCase("$gte") )
		{
			value = getQuerVariable(value, keymap);	if( value == null ) return;
			_condition.put("$gte", getDataValue(dataType, value, dataType0));
			condition.put(columnName, _condition);
		}
		else if( method.equalsIgnoreCase("<=") || method.equalsIgnoreCase("$lte") )
		{
			value = getQuerVariable(value, keymap);	if( value == null ) return;
			_condition.put("$lte", getDataValue(dataType, value, dataType0));
			condition.put(columnName, _condition);
		}
		else if( method.equalsIgnoreCase("!=") || method.equalsIgnoreCase("$ne") )
		{
			value = getQuerVariable(value, keymap);	if( value == null ) return;
			_condition.put("$ne", getDataValue(dataType, value, dataType0));
			condition.put(columnName, _condition);
		}
		else if( method.equalsIgnoreCase("like") || method.equalsIgnoreCase("$regex") )
		{
			value = getQuerVariable(value, keymap);	if( value == null ) return;
			_condition.put("$regex", value);
			_condition.put("$options", "i");
			condition.put(columnName, _condition);
		}
    	else{
			value = getQuerVariable(value, keymap);	if( value == null ) return;
			if( Tools.countChar(value, ',') > 0 ){
				return;
			}
			condition.put(columnName, getDataValue(dataType, value, dataType0));
    	}
    	if( andor != null ){
    		andor.add(condition);//当前节点的条件
    	}
        for( ; conditionChildNode != null; conditionChildNode = XMLParser.nextSibling(conditionChildNode) )
        {
        	condition = new BasicDBObject();
    		this.setQueryMongoWhere(conditionChildNode, cell, keymap, condition, diggIndx);
    		if( !condition.isEmpty() ){
    			andor.add(condition);
    		}
        }
        if( parent != null && !andor.isEmpty() ){
        	parent.put("$"+compound, andor);
        }
	}

	/**
	 * 根据数据类型描述和字符数据返回对象
	 * @param dataType
	 * @param value
	 * @param dataType0 表格上配置的数据类型
	 * @return
	 */
	protected static Object getDataValue(String dataType, String value, String dataType0)
	{
		if( value.startsWith("%") && value.endsWith("%") ){
			return value;
		}
		Object val = value;
    	if( dataType.startsWith("int") || dataType.startsWith("integer") || dataType.equalsIgnoreCase("long")  )
    	{
    		if( Tools.isNumeric(value) ){
    			val = dataType.equalsIgnoreCase("long")?Long.parseLong(value):Integer.parseInt(value);
    		}
    		else{
    			val = 0;
    			if( "time".equalsIgnoreCase(dataType0) ){
    				String format = "yyyy-MM-dd HH:mm:ss";
					format = format.substring(0, value.toString().length());
			        SimpleDateFormat sdf = new SimpleDateFormat(format);
			        try{
			        	if( dataType0.equalsIgnoreCase("long") ){
			        		val = sdf.parse(value.toString()).getTime();
			        	}
			        	else{
			        		val = (int)(sdf.parse(value.toString()).getTime()/1000);
			        	}
			        }
			        catch(Exception e){
			        }
    			}
    		}
    	}
    	else if( dataType.equalsIgnoreCase("number") || dataType.equalsIgnoreCase("double")||dataType.equalsIgnoreCase("float") )
    	{
    		if( Tools.isNumeric(value) ){
    			val = Double.valueOf(value);
    		}
    		else{
    			val = 0.0;
    		}
    	}
    	else if( dataType.equalsIgnoreCase("bool") || dataType.equalsIgnoreCase("boolean") )
    	{
    		val = Boolean.valueOf(value);
    	}
    	
    	if( "null".equalsIgnoreCase(value) ){
    		val = null;
    	}
    	return val;
	}
	/**
	 * 设置JDBC连接参数
	 * @param src
	 * @param sObject
	 * @throws Exception
	 */
	public void preloadDatabase(String src, JSONObject sObject) throws Exception
	{
		if( src == null || src.isEmpty() ) src = "local";
		StringBuffer sb = new StringBuffer();
		sb.append("Receive the request, the parameters of below ");
		HttpServletRequest req = request;
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
//		log.info(sb.toString());
    	if( "local".equalsIgnoreCase(src) )
    	{
			Properties jdbcConfig = new Properties();
			dbtype = "h2";
	    	jdbcConfig.load(new FileInputStream(new File(PathFactory.getWebappPath(), "WEB-INF/classes/config/jdbc.properties")));
	    	jdbcUrl = jdbcConfig.getProperty("jdbc.url");
	    	jdbcUsername = jdbcConfig.getProperty("jdbc.username");
	    	jdbcUserpswd = jdbcConfig.getProperty("jdbc.password");
	    	driverClass = jdbcConfig.getProperty("jdbc.driverClass");
    		if( jdbcUserpswd == null ) jdbcUserpswd = "";
    		if( jdbcUrl == null || jdbcUrl.isEmpty() || jdbcUsername == null || jdbcUsername.isEmpty() || driverClass == "" || driverClass.isEmpty() )
    		{
    			throw new Exception("因为jdbc配置连接参数("+dbtype+")无效执行数据库加载");
    		}
    		sObject.put("dbtype", dbtype);
    		sObject.put("jdbc.driverClass", driverClass);
    		sObject.put("jdbc.username", jdbcUsername);
    		sObject.put("jdbc.password", new String(Base64X.encode(jdbcUserpswd.getBytes())));
    		sObject.put("jdbc.url", jdbcUrl);
    	}
		else if( src.startsWith("/cos/config/modules/") )
    	{
    		JSONObject datasource = ZKMgr.getZookeeper().getJSONObject(src, true);
    		if( datasource == null ||
				!datasource.has("dbtype") ||
				!datasource.has("dbaddr") ||
				!datasource.has("dbname") ||
				!datasource.has("dbusername") 
//				!datasource.has("dbpassword") 
				)
    		{
    			throw new Exception("未发现正确数据源配置("+src+","+datasource+")无法进行数据查询");
    		}
    		dbtype = datasource.getString("dbtype");
    		sObject.put("dbtype", dbtype);
			String dbaddr = datasource.getString("dbaddr");
			String dbname = datasource.getString("dbname");
			String username = datasource.getString("dbusername");
			String password = datasource.has("dbpassword")?datasource.getString("dbpassword"):"";
    		if( "mongo".equals(dbtype) )
    		{
    			String args[] = Tools.split(dbaddr, ":");
        		mongoUsername = username;
        		mongoUserpswd = new String(Base64X.decode(password));
        		mongoDatabase = dbname;
        		mongoHost = args[0];
        		mongoPort = Integer.parseInt(args[1]);
        		sObject.put("mongo.host", mongoHost);
        		sObject.put("mongo.port", mongoPort);
        		sObject.put("mongo.username", username);
        		sObject.put("mongo.password", password);
        		sObject.put("mongo.database", mongoDatabase);
    		}
    		else if( "redis".equals(dbtype) )
    		{
    			String args[] = Tools.split(dbaddr, ":");
        		redisHost = args[0];
        		redisPort = Integer.parseInt(args[1]);
        		redisPassword = new String(Base64X.decode(password));
        		sObject.put("redis.host", redisHost);
        		sObject.put("redis.port", redisPort);
        		sObject.put("redis.password", redisPassword);
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
    			jdbcUsername = username;
    			jdbcUserpswd = password;
        		sObject.put("jdbc.driverClass", driverClass);
        		sObject.put("jdbc.username", jdbcUsername);
        		sObject.put("jdbc.password", jdbcUserpswd);
        		sObject.put("jdbc.url", jdbcUrl);
        		jdbcUserpswd = new String(Base64X.decode(password));
        		if( jdbcUrl == null || jdbcUrl.isEmpty() || jdbcUsername == null || jdbcUsername.isEmpty() || driverClass == "" || driverClass.isEmpty() )
        		{
        			throw new Exception("因为jdbc配置连接参数("+src+")无效执行数据库加载");
        		}
    		}
    	}
	}

	/**
	 * 创建查询左连接 
	 * @param columnName
	 * @param joinNode
	 * @return
	 * @throws Exception
	 */
	private JSONObject createQueryJoin(String leftkey, Element joinNode) throws Exception
	{
		JSONObject join = new JSONObject();
    	String objectName = XMLParser.getElementAttr(joinNode, "object");
    	if( !objectName.isEmpty()) join.put("object", objectName);
		join.put("leftkey", leftkey);
		String clause = XMLParser.getElementAttr(joinNode, "clause");
		clause = clause.isEmpty()?XMLParser.getElementAttr(joinNode, "column"):clause;
    	join.put("rightkey", clause);
    	String database = XMLParser.getElementAttr(joinNode, "database");
    	database = src(database, true, null);
    	String src = XMLParser.getElementAttr(joinNode, "src");
    	src = src(src, true, null);
    	join.put("src", src);
    	String from = XMLParser.getElementAttr(joinNode, "from");
    	from = src(from, true, null);
    	join.put("from", from);
    	if( objectName.isEmpty() ){
    		objectName = from;
    	}
    	join.put("database", database);
    	String setrow = XMLParser.getElementAttr(joinNode, "setrow");
    	join.put("setrow", "true".equals(setrow));
    	String setcol = XMLParser.getElementAttr(joinNode, "setcol");
    	if( !setcol.isEmpty() ){
    		join.put("setcol", setcol);//需要放到主表去的字段
    	}
    	String timeformat = XMLParser.getElementAttr(joinNode, "timeformat");
    	if( !timeformat.isEmpty() )	join.put("timeformat", timeformat);
    	String more = XMLParser.getElementAttr( joinNode, "more" );
    	if( "true".equalsIgnoreCase(more) ){
        	join.put("more", true);
    		dataStyle.put(objectName, new JSONArray());
    	}
    	else{
        	join.put("more", false);
        	dataStyle.put(objectName, new JSONObject());
    	}
    	String sort = XMLParser.getElementAttr( joinNode, "sort" );
    	join.put("sort", sort);
    	String dir = XMLParser.getElementAttr( joinNode, "dir" );
    	join.put("dir", dir);
    	String skip = XMLParser.getElementAttr( joinNode, "skip" );
    	join.put("skip", "true".equalsIgnoreCase(skip));
    	Element filterNode = XMLParser.getChildElementByTag(joinNode, "filter");
        if( filterNode != null )
        {
        	JSONObject filter = this.getQueryFilter(filterNode, null);
        	if( filter != null )
        	{
            	filter.put("type", XMLParser.getElementAttr(filterNode, "type"));
            	filter.put("callback", XMLParser.getElementAttr(filterNode, "callback"));
    			join.put("filter", filter);
        		
        	}
        	filterNode = XMLParser.getNextSibling(filterNode);
        }
        //join下面可能是条件
    	Element conditionNode = XMLParser.getChildElementByTag(joinNode, "condition");
    	if( conditionNode != null )
    	{
    		JSONObject condition = this.getQueryCondition(conditionNode, null);
    		if( condition != null ){
    			join.put("condition", condition);
//    	    	String isnull = XMLParser.getElementAttr( conditionNode, "isnull" );
//    			if( "true".equalsIgnoreCase(isnull) ){
//    				condition.put("isnull", true);
//    			}
    		}
    	}
    	Element sumNode = XMLParser.getChildElementByTag(joinNode, "sum");
    	if( sumNode != null )
    	{
    		String as = XMLParser.getElementAttr(sumNode, "as");
    		clause = XMLParser.getElementAttr(sumNode, "clause");
    		JSONObject sum = new JSONObject();
    		sum.put("as", as);
    		sum.put("clause", clause);
    		join.put("sum", sum);
    	}
    	Element countNode = XMLParser.getChildElementByTag(joinNode, "count");
    	if( countNode != null )
    	{
    		String as = XMLParser.getElementAttr(countNode, "as");
    		clause = XMLParser.getElementAttr(countNode, "clause");
    		JSONObject count = new JSONObject();
    		count.put("as", as);
    		count.put("clause", clause);
    		join.put("count", count);
    	}
    	Element idNode = XMLParser.getChildElementByTag(joinNode, "id");
    	if( idNode != null )
    	{
    		String as = XMLParser.getElementAttr(idNode, "as");
    		String def = XMLParser.getElementAttr(idNode, "default");
    		clause = XMLParser.getElementAttr(idNode, "clause");
    		if( clause.isEmpty() ){
    			clause = XMLParser.getElementAttr(idNode, "column");
    		}
    		JSONObject id = new JSONObject();
    		id.put("as", as);
    		id.put("column", clause);
    		if( !def.isEmpty() ){
    			if( Tools.isNumeric(def) ){
    				id.put("default", def.indexOf('.')!=-1?0:Long.parseLong(def));
    			}
    			else{
    				id.put("default", def);
    			}
    		}
    		join.put("id", id);
    	}
    	Element groupNode = XMLParser.getChildElementByTag(joinNode, "group");
    	if( groupNode != null )
    	{
    		String value = XMLParser.getElementAttr(groupNode, "value");
    		if( value.isEmpty() ){
        		throw new Exception("模板字段【"+leftkey+"】左连接配置了GROUP聚合名，但是没有配置聚合字段");
        	}
    		JSONObject group = new JSONObject();
    		group.put("value", value);
        	Element columnNode = XMLParser.getFirstChildElement(groupNode);
        	JSONArray columns = new JSONArray();
        	while(columnNode!=null)
        	{
            	String as = columnNode.getNodeName();
            	if( "mongo".equals(dbtype) )
        		{
            		String function = XMLParser.getElementAttr(columnNode, "function");
            		if( function.isEmpty() ){
            			function = XMLParser.getElementAttr(columnNode, "operator");
            		}
            		String parameter = XMLParser.getElementAttr(columnNode, "parameter");
            		if( parameter.isEmpty() ){
            			parameter = XMLParser.getElementAttr(columnNode, "column");
            		}
            		if( !as.isEmpty() && !function.isEmpty() && !parameter.isEmpty() )
            		{
    	            	JSONObject count = new JSONObject();
    	            	count.put("name", as);
    	            	count.put("function", function);
    	            	count.put("parameter", parameter);
    	            	columns.put(count);
    	            	group.put("counts", columns);
    	    			join.put("group", group);
            		}	
        		}
            	else{
	            	JSONObject column = new JSONObject();
	            	column.put("as", as);
	            	value = XMLParser.getElementAttr(columnNode, "clause");
	            	if( value.isEmpty() ){
	            		throw new Exception("模板字段【"+leftkey+"】左连接配置了GROUP聚合別名【"+as+"】，但是没有配置聚合值");
	            	}
	            	column.put("clause", value);
	            	columns.put(column);
	            	group.put("columns", columns);
	    			join.put("group", group);
            	}
        		columnNode = XMLParser.getNextSibling(columnNode);
        	}
        	if( !join.has("group") ){
        		throw new Exception("模板字段【"+leftkey+"】左连接配置了GROUP聚合，但是没有配置任何聚合列字段和计算式");
        	}
    	}
    	return join;
	}

	/**
	 * 得到JOIN的过滤参数
	 * @param conditionNode
	 * @param columnName
	 * @return
	 */
	private JSONObject getQueryFilter(Element filterNode, String columnName)
	{
		if( filterNode == null )	return null;
		String method = XMLParser.getElementAttr(filterNode, "method");
		if( method.isEmpty() ) return null;
		String value = XMLParser.getElementAttr(filterNode, "value");
		if( value.isEmpty() ) return null;
		String columnName1 = XMLParser.getElementAttr(filterNode, "column");
		if( columnName1.isEmpty() && (columnName == null || columnName.isEmpty()) )
		{
			return null;
		}
		String compound = XMLParser.getElementAttr(filterNode, "compound");
		if( compound.isEmpty() ) compound = "and";
		if( columnName == null || columnName.isEmpty() )
		{
			columnName = columnName1;
		}
		JSONObject filter = new JSONObject();
		filter.put("column", columnName);
		filter.put("compound", compound);
		filter.put("method", method);
		filter.put("value", value);
		JSONArray children = new JSONArray();
		filter.put("children", children);
    	Element childNode = XMLParser.getChildElementByTag(filterNode, "filter");
    	while( childNode != null )
    	{
    		JSONObject child = this.getQueryFilter(childNode, columnName);
    		if( child != null )
    		{
    			children.put(child);
    		}
    		childNode = XMLParser.nextSibling(childNode);
    	}
		return filter;
	}
	
	/**
	 * 得到condtion节点下的参数
	 * @param conditionNode
	 * @param columnName
	 * @return
	 */
	private JSONObject getQueryCondition(Element conditionNode, String columnName)
	{
		if( conditionNode == null )	return null;
		String compound = XMLParser.getElementAttr(conditionNode, "compound");
		if( compound.isEmpty() ) return null;
		String method = XMLParser.getElementAttr(conditionNode, "method");
		if( method.isEmpty() ) return null;
		String value = XMLParser.getElementAttr(conditionNode, "value");
		if( value.isEmpty() ) return null;
		String columnName1 = XMLParser.getElementAttr(conditionNode, "column");
		if( columnName1.isEmpty() && (columnName == null || columnName.isEmpty()) )
		{
			return null;
		}
		if( columnName1 != null && !columnName1.isEmpty() )
		{
			columnName = columnName1;
		}
		JSONObject condition = new JSONObject();
		condition.put("column", columnName);
		condition.put("compound", compound);
		condition.put("method", method);
		String dataType = XMLParser.getElementAttr(conditionNode, "dataType");
		if( !dataType.isEmpty() ) condition.put("dataType", dataType);
		condition.put("value", this.getQuerVariable(value, null));
		JSONArray children = new JSONArray();
		condition.put("children", children);
    	Element childNode = XMLParser.getChildElementByTag(conditionNode, "condition");
    	while( childNode != null )
    	{
    		JSONObject child = this.getQueryCondition(childNode, columnName);
    		if( child != null )
    		{
    			children.put(child);
    		}
    		childNode = XMLParser.nextSibling(childNode);
    	}
		return condition;
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public static boolean quote(String type)
	{
		return "string".equalsIgnoreCase(type) || "time".equalsIgnoreCase(type);
	}
	/**
	 * 构建数据
	 * @param text
	 * @param skip
	 * @param data
	 * @return
	 * @throws Exception
	 */
	protected String src(String text)
			throws Exception
	{
		return checker.parameter(text, request, null, null);
	}

	protected String src(String text, JSONObject data)
		throws Exception
	{
		return checker.parameter(text, request, data, null, null, false);
	}

	protected String src(String text, boolean skip, JSONObject data)
		throws Exception
	{
		return checker.parameter(text, request, data, null, null, skip);
	}

	protected String src(String text, HttpServletRequest request, String nodeName, String nodeAttr)
		throws Exception
	{
		return checker.parameter(text, request, null, nodeName, nodeAttr, false);
	}
	/**
	 * 设置本本地回调脚本
	 * @param javascript
	 * @return
	 */
	protected String setLocalCallbackScript(String javascript)
	{
		StringBuffer sb = new StringBuffer("function(event, ui){");
		sb.append(this.setJavascript(javascript));
		sb.append("\r\n}");
		return sb.toString();
	}
	/**
	 * 设置收到远程数据后预处理数据的回调脚本
	 * @param javascript
	 * @return
	 */
	protected String setRemoteDataCallbackScript(String javascript, String plusDataIndx)
	{
		StringBuffer sb = new StringBuffer("function(remoteData){");
		sb.append("\r\nif(remoteData.hasException){//如果返回数据有异常的情况下直接回调不做数据预处理");
		sb.append("\r\n\treturn callbackAfterReceiveRemoteData(remoteData);");
		sb.append("\r\n}");
		if( plusDataIndx != null && !plusDataIndx.isEmpty() ){
			sb.append("\r\ndataLocalPlus['"+plusDataIndx+"']=remoteData.data;//将远程返回的数据对象赋值给本地全局扩展数据对象的字段【"+plusDataIndx+"】");
		}
		else{
			sb.append("\r\n\tdataRemote=remoteData.data;//将远程返回的数据对象赋值给本地全局变量");
		}
		sb.append(this.setJavascript(javascript));
		sb.append("\r\n\treturn callbackAfterReceiveRemoteData(remoteData);//执行接收到远程数据后的回调");
		sb.append("\r\n}");
		return sb.toString();
	}
	/**
	 * 脚本加上try catch 捕获异常 方便检查问题
	 * @param javascript
	 * @return
	 */
	protected String setJavascript(String javascript)
	{
		if( javascript == null ) javascript = "";
		StringBuffer sb = new StringBuffer();
		sb.append("\r\ntry{\r\n");
		sb.append("/*用户自定义脚本区域below*/\r\n");
		sb.append(javascript);
		sb.append("/*用户自定义脚本区域above*/\r\n}");
		sb.append("\r\ncatch(e){");
		sb.append("\r\nif( top && top.skit_alert) top.skit_alert('脚本执行异常'+e.message+', 行数'+e.lineNumber);");
		sb.append("\r\nelse alert('脚本执行异常'+e.message+', 行数'+e.lineNumber);");
		sb.append("\r\n}");
		return sb.toString();
	}
	
	protected void test(int t, String text, Object... args)
	{
		checker.test(t, text, args);
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getLocation() {
		return location;
	}
	
	public String toString(){
		if( dataM.charAt(0) == '{') return dataM.toString();
        dataM.insert(0, '{');
		dataM.append("\r\n}");
		return dataM.toString();
	}
	
	public void setDataStyle(JSONObject dataStyle) {
		this.dataStyle = dataStyle;
	}
}
