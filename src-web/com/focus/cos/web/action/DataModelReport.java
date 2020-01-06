package com.focus.cos.web.action;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.focus.cos.web.service.SvrMgr;
import com.focus.util.QuickSort;
import com.focus.util.Tools;
import com.focus.util.XMLParser;
import com.mongodb.BasicDBObject;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

/**
 * 构建报表数据模型
 * @author focus
 *
 */
public abstract class DataModelReport extends DataModel {

	protected int pageSize = 0;
	public DataModelReport(String type, HttpServletRequest request,
			HashMap<String, String> values, TemplateChecker checker) {
		super(type, request, values, checker);
		location = "remote";
	}
	
	public abstract void setDimension(ArrayList<JSONObject> dimension, HashMap<String, JSONObject> dimensionMap, Object val,
			HashMap<String, String> merge);

	private boolean desc = false;
	@Override
	public void build(Node datamodelNode, JSONObject dataColumns,JSONArray titleColumns, String gridxml,StringBuilder gridObj) throws Exception {
		dataM.append(",\r\n\tlocation: 'remote'");
		String pagesize = XMLParser.getElementAttr( datamodelNode, "pagesize" );
		if( Tools.isNumeric(pagesize) )
		{
			pageSize = Integer.parseInt(pagesize);
		}
		else
		{
			pageSize = 20;
		}
        String sorting = XMLParser.getElementAttr( datamodelNode, "sorting" );
		if( sorting.isEmpty() || !sorting.equals("remote") )
		{
            test(depth, "<span style='color:#ff3399'>配置了不支持的排序方式(sorting: %s)，已改为默认'本地'.</span>", sorting);
			sorting = "remote";
		}
		test(depth, "分页(pagesize:%s), 排序方式(sorting:%s)", pageSize, sorting);
		dataM.append(",\r\n\tsorting: '"+sorting+"'");
		dataM.append(",\r\n\tmethod: 'GET'");
		String url = "";
		if( recIndx == null || recIndx.isEmpty() ){
    		test(depth, "报表模板数据模型需要配置维度关键字(请在grid节点上配置[recIndx]字段)");
    		throw new Exception("创建报表模板数据模型失败");
    	}
		diggObject.put("recIndx", recIndx);
        JSONArray diggs = new JSONArray();
//		this.snapshotable = true;
//		this.snapshot = Kit.URL_PATH(request)+"digg!snapshot.action?gridxml="+this.gridxml;
//			System.err.println(snapshot);
		diggObject.put("titleColumns", titleColumns);
		diggObject.put("dataColumns", dataColumns);
		if( !pagesize.isEmpty() ) diggObject.put("pagesize", pageSize);
		diggObject.put("diggs", diggs);
		diggObject.put("pq_sort", sortColumns.toString());
		diggObject.put("dataColumns", dataColumns);
    	Element dimensionNode = XMLParser.getFirstChildElement(datamodelNode);
    	if( dimensionNode == null || !"dimension".equalsIgnoreCase(dimensionNode.getTagName()) ){
    		test(depth, "报表模板数据模型必须首先维度建模(配置dimension节点)");
    		throw new Exception("创建报表模板数据模型失败");
    	}
    	String type = XMLParser.getElementAttr(dimensionNode, "type");
    	if( "mongo".equalsIgnoreCase(type) ){
    		//通过distinct表，获得建模数
    		String src = XMLParser.getElementAttr( dimensionNode, "src" );
    		src = src(src);
    		String database = XMLParser.getElementAttr( dimensionNode, "database" );
    		database = src(database);
    		String from = XMLParser.getElementAttr( dimensionNode, "from" );
        	from = src(from);
    		diggObject.put("mongo.database1", database);//第一数据库
    		diggObject.put("mongo.tablename", from);
    		preloadDatabase(src, diggObject);

        	BasicDBObject match = new BasicDBObject();
        	JSONObject column = dataColumns.getJSONObject(recIndx);
	    	Element conditionNode = XMLParser.getChildElementByTag(dimensionNode, "condition");
        	this.setQueryMongoWhere(conditionNode, column, null, match, 0);
			MongoCollection<Document> col = SvrMgr.getMongoCollection(
				diggObject.getString("mongo.host"), 
				diggObject.getInt("mongo.port"),
				diggObject.getString("mongo.username"),
				diggObject.getString("mongo.password"),
				diggObject.getString("mongo.database"),
				diggObject.getString("mongo.database1"),
				diggObject.getString("mongo.tablename")
			);
			HashMap<String, String> merge = new HashMap<String, String>();
	    	Element mergeNode = XMLParser.getChildElementByTag(dimensionNode, "merge");
	    	if( mergeNode != null ){
		    	Element itemNode = XMLParser.getFirstChildElement(mergeNode);
		    	while(itemNode!=null){
		    		from = XMLParser.getElementAttr( itemNode, "from" );
		    		String to = XMLParser.getElementAttr( itemNode, "to" );
		    		if( from.isEmpty() || to.isEmpty() ) continue;
		    		merge.put(from, to);
		    		itemNode = XMLParser.getNextSibling(itemNode);
		    	}
	    	}
			DistinctIterable<String> result = null;
			if( match != null && !match.isEmpty() ){
				result = col.distinct(recIndx, String.class).filter(match);
			}
			else{
				result = col.distinct(recIndx, String.class);
			}
			ArrayList<JSONObject> dimension = new ArrayList<JSONObject>();
			HashMap<String, JSONObject> dimensionMap = new HashMap<String, JSONObject>();
			MongoCursor<?> cursor = result.iterator();
			while(cursor.hasNext())
			{
				Object val = cursor.next();
				this.setDimension(dimension, dimensionMap, val, merge);
			}
			dimensionMap.clear();
	        for(int i = 0;i < sortColumns.length(); i++)
	        {
	        	JSONObject sort = sortColumns.getJSONObject(i);
	        	String dataIndx = sort.getString("dataIndx");
	        	String dir = sort.getString("dir");
	        	desc = dir.equalsIgnoreCase("down");
	        	if( recIndx.equals(dataIndx) ){
	        		
	        		QuickSort sorter = new QuickSort() {
						public boolean compareTo(Object sortSrc, Object pivot) {
							JSONObject l = (JSONObject)sortSrc;
							JSONObject r = (JSONObject)pivot;
							String lk = l.getString(recIndx);
							String rk = r.getString(recIndx);
							int ret = lk.compareTo(rk);
							return desc?ret>0:ret<0;
						}
					};
					sorter.sort(dimension);
	        		break;
	        	}
	        }
	        diggObject.put("dimension", dimension.toString().getBytes("UTF-8"));
        	this.test(depth, "成功建立报表维度(%s):", dimension.size());
	        for(JSONObject e : dimension){
	        	this.test(depth+1, "%s: %s", e.getString(recIndx), e.get(recIndx+"_val"));
	        }
	        dimension.clear();
    	}
    	
        Node diggNode = XMLParser.getChildElementByTag(datamodelNode, "digg");
        int indx = 1;
        while( diggNode != null )
        {
        	JSONObject digg = new JSONObject();
        	String diggtype = XMLParser.getElementAttr( diggNode, "type" );
        	digg.put("type", diggtype);
        	String timeformat = XMLParser.getElementAttr( diggNode, "timeformat" );
    		if( !timeformat.isEmpty() ) digg.put("timeformat", timeformat);
        	if( "sql".equalsIgnoreCase(diggtype) )
        	{
            	String src = XMLParser.getElementAttr( diggNode, "src" );
            	src = src(src, request, "datamodel.digg", "src");
            	preloadDatabase(src, digg);
	    		StringBuffer sql = new StringBuffer();
	    		this.setQuerySql(diggNode, dataColumns, sql, digg, indx);
	    		digg.put("sql", sql.toString());
            	test(depth+2, "%s", sql.toString());
        	}
        	else if( "mongo".equalsIgnoreCase(diggtype) )
        	{
            	String from = XMLParser.getElementAttr( diggNode, "from" );
            	String src = XMLParser.getElementAttr( diggNode, "src" );
            	src = src(src, request, "datamodel.digg", "src");
            	String database = XMLParser.getElementAttr( diggNode, "database" );
            	from = src(from, request, "datamodel.digg", "from");
            	digg.put("mongo.database1", database);//第一数据库
            	digg.put("mongo.tablename", from);
            	preloadDatabase(src, digg);
	    		this.setQueryMongo(diggNode, dataColumns, digg, indx);
        	}
        	else if( "oltp".equalsIgnoreCase(diggtype) )
        	{
            	String className = XMLParser.getElementAttr( diggNode, "class" );
	    		digg.put("class", className);
        	}
    		if( plusDataIndx != null ) {
    			String args[] = Tools.split(plusDataIndx, ",");
    			StringBuilder sb = new StringBuilder();
    			String dataType = null;
    			for(String arg:args ){
	    			Element columnNode = XMLParser.getChildElementByTag(diggNode, arg);
	    			if( columnNode != null ){
	    				String column = XMLParser.getElementAttr( columnNode, "column" );
	    				if( column.isEmpty() ) column = arg;
	    				dataType = XMLParser.getElementAttr( columnNode, "dataType" );
	    				if( dataType.isEmpty() ) continue;
	    				if( sb.length() > 0 ) sb.append(";");
	    				sb.append(arg+"::"+column+"::"+dataType);
	    			}
	    			else{
	    				if( dataColumns.has(arg) ){
	    					JSONObject c = dataColumns.getJSONObject(arg);
	    					dataType = c.has("dataType")?c.getString("dataType"):"";
	    					if( !dataType.isEmpty() ){
	    						if( sb.length() > 0 ) sb.append(";");
	    						sb.append(arg+"::"+arg+"::"+dataType);
	    					}
	    				}
	    			}
    			}
    			if( sb.length() > 0 ){
            		digg.put("dynamicParameter", sb.toString());//动态参数根据传承
    			}
    		}
        	diggs.put(digg);
        	digg.put("indx", String.valueOf(indx++));
        	diggNode = XMLParser.getNextSibling(diggNode);
        }
        diggObject.put("gridxml", gridxml);
    	test(depth, "一共有%s个联机事务处理配置，每个配置处理一组数据，多个配置构成一个完整的OLTP处理:%s", diggs.length(), diggs.toString(4));
        if( plusDataIndx == null )
		{
			url = "digg!data.action";
		}
		else
		{
			if( pageSize > 0 )
			{
//				gridObj.append("{ type: 'remote', rPP: "+pageSize+", strRpp: '{0}', rPPOptions: [1, 10, 20, 30, 40, 50, 100, 500, 1000] }");
				gridObj.append(String.format(",pageModel: { type: \"remote\", rPP: %s, strRpp: \"\" }", pageSize));
			}
			else {
				gridObj.append(",pageModel: {}");
			}
			String args[] = Tools.split(plusDataIndx, ",");
			url = "digg!data.action?id="+plusDataIndx;
			for(String arg : args){
				url += "&"+arg+"=%"+arg;
			}
		}
        dataM.append(",\r\n\turl: '"+url+"'");
		if( javascript.length() > 0 )
		{
			test(depth, "DIGG数据模型配置的视图刷新前回调脚本 : %s", javascript);
			this.checker.js(javascript.toString(), depth);
		}
		dataM.append(",\r\n\tgetData: "+this.setRemoteDataCallbackScript(javascript, plusDataIndx));
		
		finish();
	}
}
