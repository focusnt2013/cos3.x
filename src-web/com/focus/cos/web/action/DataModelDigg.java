package com.focus.cos.web.action;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.focus.util.Tools;
import com.focus.util.XMLParser;

/**
 * 通过数据源
 * @author focus
 *
 */
public abstract class DataModelDigg extends DataModel {

	protected int pageSize = 0;
	public DataModelDigg(String type, HttpServletRequest request,
			HashMap<String, String> values, TemplateChecker checker) {
		super(type, request, values, checker);
		location = "local";
	}
	/**
	 * 得到数据链接
	 * @return
	 */
	public abstract String getDiggDataUrl();
	@Override
	public void build(Node datamodelNode, JSONObject dataColumns,JSONArray titleColumns, String gridxml,StringBuilder gridObj) throws Exception {
		location = XMLParser.getElementAttr( datamodelNode, "location", "remote" );
		if( plusDataIndx != null ){
			location = "remote";
		}
		dataM.append(",\r\n\tlocation: '"+location+"'");
		String pagesize = XMLParser.getElementAttr( datamodelNode, "pagesize" );
		if( Tools.isNumeric(pagesize) )
		{
			pageSize = Integer.parseInt(pagesize);
		}
		else
		{
			pageSize = 20;
		}
        String sorting = XMLParser.getElementAttr( datamodelNode, "sorting", "remote" );
//		if( sorting.isEmpty() || !sorting.equals("remote") )
//		{
//            test(depth, "<span style='color:#ff3399'>配置了不支持的排序方式(sorting: %s)，已改为默认'本地'.</span>", sorting);
//			sorting = "remote";
//		}
		test(depth, "分页(pagesize:%s), 排序方式(sorting:%s)", pageSize, sorting);
		dataM.append(",\r\n\tsorting: '"+sorting+"'");
		dataM.append(",\r\n\tmethod: 'GET'");
		String url = "";
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
    	diggObject.put("recIndx", recIndx);
        Node diggNode = XMLParser.getChildElementByTag(datamodelNode, type);
        int indx = 0;
        while( diggNode != null )
        {
        	JSONObject digg = this.a(indx, diggNode, dataColumns, titleColumns, gridxml, gridObj, pagesize);
        	diggs.put(digg);
        	indx += 1;
        	diggNode = XMLParser.getNextSibling(diggNode);
        }
        diggObject.put("gridxml", gridxml);
    	test(depth, "一共有%s个联机事务处理配置，每个配置处理一组数据，多个配置构成一个完整的OLTP处理:%s", diggs.length(), diggs.toString(4));
		url = getDiggDataUrl();
        if( javascript.length() > 0 )
		{
			test(depth, "DIGG数据模型配置的视图刷新前回调脚本 : %s", javascript);
			this.checker.js(javascript.toString(), depth);
		}
        if( plusDataIndx != null )
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
			url += "?id="+plusDataIndx;
			for(String arg : args){
				url += "&"+arg+"=%"+arg;
			}
            dataM.append(",\r\n\turl: '"+url+"'");
    		dataM.append(",\r\n\tgetData: "+this.setRemoteDataCallbackScript(javascript, plusDataIndx));
		}
        else if("remote".equalsIgnoreCase(location)) {
            dataM.append(",\r\n\turl: '"+url+"'");
            dataM.append(",\r\n\tgetData: function(remoteData){return handleRemoteData(remoteData);}");
        }
		finish();
	}
	
	/**
	 * 
	 * @param indx
	 * @param diggNode
	 * @param dataColumns
	 * @param titleColumns
	 * @param gridxml
	 * @param gridObj
	 * @param pagesize
	 */
	private JSONObject a(int indx, Node diggNode, JSONObject dataColumns,JSONArray titleColumns, String gridxml,StringBuilder gridObj, String pagesize)
		throws Exception
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
    	else if( "redis".equalsIgnoreCase(diggtype) )
    	{
        	String from = XMLParser.getElementAttr( diggNode, "from" );
        	String src = XMLParser.getElementAttr( diggNode, "src" );
        	src = src(src, request, "datamodel.digg", "src");
        	digg.put("table", from);
        	preloadDatabase(src, digg);
//    		this.setQueryRedis(diggNode, dataColumns, digg, indx);
    	}
    	else if( "json".equalsIgnoreCase(diggtype) ){
    		if( indx > 0 ) {
    			checker.err();
    			checker.test(1, "[%s] 配置数据模型[digg]错误，[json]类型只能配置在首位.", "严重错误");
    		}
    		if( pagesize.isEmpty() ){
    			pageSize = 0;
    			diggObject.put("pagesize", 0);
    		}
    		String src = XMLParser.getElementAttr( diggNode, "src" );
        	src = src(src, request, "datamodel.digg", "src");
        	digg.put("src", src);
    		String method = XMLParser.getElementAttr( diggNode, "method" );
    		method = method.isEmpty()?"post":method;
        	digg.put("method", method);

			Element dataNode = XMLParser.getChildElementByTag(diggNode, "data");
			JSONObject data = new JSONObject();
			Element resultNode = dataNode!=null?XMLParser.getChildElementByTag(dataNode, "result"):null;
			JSONObject result = new JSONObject();
			result.put("value", XMLParser.getElementAttr( resultNode, "value", "hasException" ));
			String code = XMLParser.getElementValue(resultNode);
			result.put("code", code.isEmpty()?"false":code);
			result.put("message", XMLParser.getElementAttr( resultNode, "message", "message" ));
			data.put("result", result);
			data.put("value", XMLParser.getElementAttr( dataNode, "value", "data" ));
			data.put("size", XMLParser.getElementAttr( dataNode, "size", "totalRecords" ));
        	digg.put("data", data);
			
			Element pageNode = XMLParser.getChildElementByTag(diggNode, "page");
			JSONObject page = new JSONObject();
        	page.put("value", XMLParser.getElementAttr( pageNode, "value", "pq_curpage" ));
        	page.put("size", XMLParser.getElementAttr( pageNode, "size", "pq_rpp" ));
        	digg.put("page", page);

			Element filterNode = XMLParser.getChildElementByTag(diggNode, "filter");
			filterNode = filterNode!=null?XMLParser.getFirstChildElement(filterNode):null;
			JSONObject filter = new JSONObject();
        	while( filterNode != null ){
        		JSONObject f = new JSONObject();
        		f.put("value", XMLParser.getElementAttr( filterNode, "value" ));
        		String value2 = XMLParser.getElementAttr( filterNode, "value2" );
        		if(!value2.isEmpty()){
        			f.put("value2", value2);
        		}
        		filter.put(filterNode.getNodeName(), f);
        		filterNode = XMLParser.getNextSibling(filterNode);
        	}
        	digg.put("filter", filter);
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
    	digg.put("indx", String.valueOf(indx));
    	return digg;
	}
}
