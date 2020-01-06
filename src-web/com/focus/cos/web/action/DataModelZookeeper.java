package com.focus.cos.web.action;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.zookeeper.data.Stat;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Node;

import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.service.SvrMgr;
import com.focus.util.Tools;
import com.focus.util.XMLParser;
import com.focus.util.Zookeeper;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.util.JSON;

public abstract class DataModelZookeeper extends DataModel {
	protected boolean isForm;
	public DataModelZookeeper(String type, HttpServletRequest request, HashMap<String, String> values, TemplateChecker checker){
		super("zookeeper", request, values, checker);
		isForm = "form".equalsIgnoreCase(type);
		location = "local";
	}
	
	/**
	 * 构建数据模型
	 */
	public void build(Node datamodelNode, JSONObject dataColumns, JSONArray titleColumns, String gridxml, StringBuilder gridObj) 
		throws Exception
	{
		int depth = plusDataIndx==null?1:2;
		String zkpath = XMLParser.getElementAttr( datamodelNode, "value" );
		boolean encrypte = "true".equalsIgnoreCase(XMLParser.getElementAttr( datamodelNode, "encrypte" ));
		boolean gzip = "true".equalsIgnoreCase(XMLParser.getElementAttr( datamodelNode, "gzip" ));
		String mode = XMLParser.getElementAttr( datamodelNode, "mode" );
		String src = XMLParser.getElementAttr(datamodelNode, "src");
		Zookeeper zk = ZKMgr.getZookeeper();
		if( !src.isEmpty() ){
			zk = Zookeeper.getInstance(src);
		}
		if( mode.isEmpty() ) mode = "0";
		test(depth, "Zookeeopr模式(mode:%s), 数据是否加密(encrypte:%s), 数据是否压缩(gzip:%s), 表示数据来自于ZK节点目录", mode, encrypte, gzip);
		test(depth, "Zookeeopr数据路径(value:%s, 如果配置中定义通配符参数表示在真实运行环境下根据HttpRequest获取对应参数)", zkpath);
        zkpath = src(zkpath, request, "Zookeeper的数据地址", "datamodel.value");
		int m = Tools.isNumeric(mode)?Integer.parseInt(mode):-1;
		final String[] Modes = {"[0]目录下子节点", "[1]目录对象", "[2]目录对象字段"};
		if( m >=0 && m < 3 )
		{
			if( isForm ){//给FORM用的
				String recIndx = XMLParser.getElementAttr( datamodelNode, "recIndx" );
				if( !recIndx.isEmpty() ){
					String recVal = request.getParameter(recIndx);
					if( recVal != null ){
						localObject = zk.getJSONObject(zkpath+"/"+recVal, encrypte, gzip);
					}
				}
			}
			else if( "2".equals(mode) ){
				JSONObject localObject = zk.getJSONObject(zkpath, encrypte, gzip);
				if( localObject != null )
				{
					String subval = XMLParser.getElementAttr( datamodelNode, "subval" );
					if( localObject.has(subval) )
					{
						localArray = localObject.getJSONArray(subval);
						test(1, "配置字段, 目录对象字段(subval:%s), 数据真实存在. ", Modes[m], subval);
					}
					else
					{
						test(1, "<span style='color:#ff3399'>模式说明: %s, 目录对象字段(subval:%s), 目录对象中不存该字段的数据模板无效.</span>", Modes[m], subval);
						checker.err();
					}
				}
				else
				{
					if( !checker.hasDynamicParameter() ){
						test(1, "<span style='color:#ff3399'>模式说明: %s, 数据不存在模板无效.</span>", Modes[m]);
						checker.err();
					}
				}
			}
			else if( zkpath.indexOf("%") == -1 )
			{
				if( !"1".equals(mode) )
				{
					localArray = new JSONArray();
					loadData(zk, zkpath, localArray, encrypte, gzip);
				}
				else{
					this.localArray = zk.getJSONArrayObject(zkpath, encrypte, gzip);
				}
				String childrenname = XMLParser.getElementAttr( datamodelNode, "children" );
				if( localArray != null && !childrenname.isEmpty() )
				{
					List<String> list = zk.getChildren(zkpath);
			        for(int i = 0; i < localArray.length(); i++){
			        	JSONObject row = localArray.getJSONObject(i);
			        	String nodename = list.get(i);
			        	JSONArray children = zk.getJSONArray(zkpath+"/"+nodename, encrypte);
			        	row.put(childrenname, children);
			        }
				}
			}
		}
		else
		{
            test(1, "<span style='color:#ff3399'>Zookeeper模式(mode%s)未知，请配置支持的模式(%s)</span>", mode, Modes);
            checker.err();
		}
		String condition = XMLParser.getElementAttr( datamodelNode, "condition" );
		if( !condition.isEmpty()  ){
			condition = src(condition, request, "Zookeeper的条件过滤", "datamodel.condition");
			if(!condition.endsWith("%")){
				String args[] = Tools.split(condition,"=");
				for(int i = 0; i < localArray.length(); i++){
					JSONObject row = localArray.getJSONObject(i);
					if( row.has(args[0]) ){
						String val = row.get(args[0]).toString();
						if( val.isEmpty() || args[1].indexOf(val) == -1 ){
							localArray.remove(i);
							i -= 1;
						}
					}
					else{
						localArray.remove(i);
						i -= 1;
					}
				}
			}
		}
		if( localArray != null ){
			test(depth, "从Zookeeopr获取的数据条数是:%s", localArray.length());			
			this.setZookeeperDigg(datamodelNode, dataColumns, depth+1);
		}
		dataM.append(",\r\n\tlocation: 'local'");
		dataM.append(",\r\n\tsorting: 'local'");
		test(depth, "location默认是'%s',sorting默认是'%s'", "local", "local");
		if( plusDataIndx == null )
		{
			dataM.append(",\r\n\tdata: filterRows(dataLocal)");
			if( localArray != null )
			{
				ArrayList<String> tags = new ArrayList<String>();
				for(int i = 0; i < titleColumns.length(); i++ )
				{
					JSONObject e = titleColumns.getJSONObject(i);
					if( e.has("dataType") && "tag".equalsIgnoreCase(e.getString("dataType")) )
					{
						tags.add(e.getString("dataIndx") );
					}
				}
				for(int i = 0; i < localArray.length(); i++ )
				{
					JSONObject e = localArray.getJSONObject(i);
					for(String tag : tags)
					{
						if( e.has(tag) )
						{
							e.put(tag+".obj", new JSONObject(e.getJSONObject(tag).toString()));
						}
					}
				}
			}
		}
		else
		{
			dataM.append(",\r\n\tdata: dataLocalPlus['"+plusDataIndx+"']");
//			this.localObject.put(plusDataIndx, this.localArray);
		}
		if( javascript != null && javascript.length() > 0 )
		{
			test(depth, "Local数据模型配置的视图刷新前回调脚本 : %s", javascript);
			checker.js(javascript.toString(), depth);
			gridObj.append(",beforeTableView: ");
			gridObj.append(this.setLocalCallbackScript(javascript));
		}
		finish();
	}

	/**
	 * 加载数据
	 * @param zookeeper
	 * @param path
	 * @param data
	 * @param decrypte
	 * @param gzip
	 */
	public void loadData(Zookeeper zookeeper, String path, JSONArray data, boolean decrypte, boolean gzip)
	{
		try
		{
			Stat stat = zookeeper.exists(path, false); 
			if( stat != null)
			{
				List<String> nodes = zookeeper.getChildren(path, false);
				for(String node : nodes)
				{
					JSONObject obj = zookeeper.getJSONObject(path+"/"+node, decrypte, gzip);
					if( obj != null ){
						if( !obj.has(recIndx) ){
							obj.put(recIndx, node);
						}
						data.put(obj);
					}
				}
			}
		}
		catch(Exception e)
		{
		}
	}
	/**
	 * 设置Zookeeper的Digg查询
	 * @param datamodelNode
	 * @param dataColumns
	 */
	private void setZookeeperDigg(Node datamodelNode, JSONObject dataColumns, int depth)
	{
        for(int i = 0; i < localArray.length(); i++){
        	JSONObject row = localArray.getJSONObject(i);
            Node diggNode = XMLParser.getChildElementByTag(datamodelNode, "digg");
            int indx = 0;
            while( diggNode != null ) {
            	try{
	            	JSONObject digg = new JSONObject();
	            	String diggtype = XMLParser.getElementAttr( diggNode, "type" );
	            	String count = XMLParser.getElementAttr( diggNode, "count" );
	            	String lastObject = XMLParser.getElementAttr( diggNode, "lastObject" );
	            	digg.put("type", diggtype);
	            	String timeformat = XMLParser.getElementAttr( diggNode, "timeformat" );
		    		if( !timeformat.isEmpty() ) digg.put("timeformat", timeformat);
	            	if( "sql".equalsIgnoreCase(diggtype) )
	            	{
			    		if( !count.isEmpty() ){
		            		String src = XMLParser.getElementAttr( diggNode, "src" );
		            		src = src(src, true, row);
		            		String from = XMLParser.getElementAttr( diggNode, "from" );
		                	from = src(from, true, row);
		            		if( from.startsWith("%") ){
		            			continue;
		            		}
		            		preloadDatabase(src, digg);
				    		StringBuffer sql = new StringBuffer();
				    		this.setQuerySql(diggNode, dataColumns, sql, digg, indx);
		            		String _sql = src(sql.toString(), true, row);
		            		_sql = "select count(1) "+ _sql.substring(_sql.toLowerCase().indexOf("from"));

		            		Connection connection = null;
		            		Statement statement = null;
		            		ResultSet rs = null;
		            		try
		    				{
		                        Class.forName(driverClass); 
		                        connection = DriverManager.getConnection(jdbcUrl ,jdbcUsername, jdbcUserpswd);  
		                        statement = connection.createStatement();
		                        rs = statement.executeQuery(_sql);
		    		            if( rs.next() )
		    		            {
		    		            	row.put(count, rs.getInt(1));
		    			        }
		    				}
		    				catch(Exception e)
		    				{
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
	            	}
	            	else if( "mongo".equalsIgnoreCase(diggtype) )
	            	{
	            		String setrow = XMLParser.getElementAttr( diggNode, "setrow" );
	            		String src = XMLParser.getElementAttr( diggNode, "src" );
	            		src = src(src, true, row);
	            		String database = XMLParser.getElementAttr( diggNode, "database" );
	            		database = src(database, true, row);
	            		if( database.startsWith("%") ){
	            			continue;
	            		}
	            		String from = XMLParser.getElementAttr( diggNode, "from" );
	                	from = src(from, true, row);
	            		if( from.startsWith("%") ){
	            			continue;
	            		}
	                	digg.put("mongo.database1", database);//第一数据库
	                	digg.put("mongo.tablename", from);
	                	preloadDatabase(src, digg);
			    		this.setQueryMongo(diggNode, dataColumns, digg, indx);
			    		test(depth, "digg(%s) from %s@%s, database is %s)", type, from, src, database);
        				BasicDBObject where = null;
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
        				if( where != null ){
        					test(depth+1, "where:%s", where.toString());
        				}
	        			MongoCollection<Document> col = SvrMgr.getMongoCollection(
	        					mongoHost, mongoPort, mongoUsername, mongoUserpswd,
	    					digg.getString("mongo.database"),
	    					digg.getString("mongo.database1"),
	    					digg.getString("mongo.tablename")
	    				);
			    		if( !count.isEmpty() ){
	    					long r = where!=null?col.count(where):col.count();
	    					row.put(count, (int)r);
	    					test(depth+1, "count(%s): %s", count, r);
			    		}
			    		if( !lastObject.isEmpty() ){
			    			FindIterable<Document> find = where!=null?col.find(where):col.find();
			    			find = find.sort(new Document("_id",-1));
			    			find = find.limit(1);
			    			MongoCursor<Document> cursor = find.iterator();
							if(cursor.hasNext())
							{
								Document doc = cursor.next();
								Object _id = doc.remove("_id");
								doc.put("_id", _id.toString());
								test(depth+1, "last_object(%s): %s", lastObject, doc.toJson());
			    				if("true".equalsIgnoreCase(setrow)){
			    					setMongoData(doc, row);
			    				}
			    				else{
			    					String json = doc.toJson();
			    					row.put(lastObject, new JSONObject(json));
			    				}
							}
							else{
		    					test(depth+1, "not found data.");
							}
			    		}
			    		if( digg.has("group") ){
			    			ArrayList<BasicDBObject> aggregate = new ArrayList<BasicDBObject>();
			    			if( where != null ){
			    				aggregate.add(new BasicDBObject("$match", where));
			    			}
			    			JSONObject group = digg.getJSONObject("group");
			    			aggregate.add(new BasicDBObject("$group", (BasicDBObject)JSON.parse(group.toString())));
	    					test(depth+1, "group: %s", group.toString(4));
			    			aggregate.add(new BasicDBObject("$limit", 1));
			    			MongoCursor<Document> cursor = col.aggregate(aggregate).iterator();
			    			if( cursor.hasNext() ){
			    				Document doc = cursor.next();
			    				if("true".equalsIgnoreCase(setrow)){
			    					setMongoData(doc, row);
			    				}
			    				else{
			    					row.put(from, new JSONObject(doc.toJson()));
			    				}
			    			}
			    		}
	            	}
	            	else if( "oltp".equalsIgnoreCase(diggtype) )
	            	{
	                	String className = XMLParser.getElementAttr( diggNode, "class" );
			    		digg.put("class", className);
	            	}
	            	indx += 1;
            	}
            	catch(Exception e){
            		e.printStackTrace();
					test(depth, "[%s] Failed to digg for exception: %s", "严重错误", e.getMessage());
            	}
            	finally{
            		diggNode = XMLParser.getNextSibling(diggNode);	            	
            	}
            }
        }
	}

	/**
	 * 将数据从芒果返回对象中写入到行记录对象中
	 * @param doc
	 */
	private void setMongoData(Document doc, JSONObject row)
	{
		Iterator<?> iterator = doc.keySet().iterator();
		while( iterator.hasNext() )
		{
			String key = iterator.next().toString();
			Object val = doc.get(key);
			row.put(key, val);
		}
	}
}
