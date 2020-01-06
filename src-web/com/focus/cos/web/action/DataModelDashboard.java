package com.focus.cos.web.action;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Node;

import com.focus.cos.web.common.ZKMgr;
import com.focus.util.Tools;
import com.focus.util.XMLParser;
import com.focus.util.Zookeeper;

/**
 * 构建Dashboard数据模型
 * @author focus
 *
 */
public abstract class DataModelDashboard extends DataModel {

	/*数据对象集合*/
	protected JSONObject data = new JSONObject();
	
	public DataModelDashboard(String type, HttpServletRequest request,
			HashMap<String, String> values, TemplateChecker checker) {
		super(type, request, values, checker);
		location = "local";
	}
	@Override
	public void build(Node datamodelNode, JSONObject dataColumns,JSONArray titleColumns, String gridxml,StringBuilder gridObj) throws Exception {
		test(depth, "构建Dashboard数据模型，location默认是'%s'", "local");
		if( javascript != null && javascript.length() > 0 )
		{
			test(depth, "Local数据模型配置的视图刷新前回调脚本 : %s", javascript);
			checker.js(javascript.toString(), depth);
			gridObj.append(",beforeTableView: ");
			gridObj.append(this.setLocalCallbackScript(javascript));
		}
		dataM.append(",\r\n\tlocation: 'local'");
		dataM.append(",\r\n\tsorting: 'local'");
		dataM.append(",\r\n\tdata: filterRows(dataLocal)");
        Node diggNode = XMLParser.getChildElementByTag(datamodelNode, "digg");
        int diggIndx = 0;
        long ts = System.currentTimeMillis();
        while( diggNode != null )
        {
        	String diggtype = XMLParser.getElementAttr( diggNode, "type" );
    		test(depth+1, "计算第%s个DIGG，类型是'%s'", diggIndx, diggtype);
        	if( "sql".equalsIgnoreCase(diggtype) )
        	{
        		this.buildSqlDiggData(diggNode, dataColumns, diggIndx);
        	}
        	else if( "mongo".equalsIgnoreCase(diggtype) )
        	{
        	}
        	else if( "oltp".equalsIgnoreCase(diggtype) )
        	{
        	}
        	else if( "zookeeper".equalsIgnoreCase(diggtype) )
        	{
        		this.buildZookeeperDiggData(diggNode);
        	}
    		if( plusDataIndx != null ) {
    		}
    		diggIndx += 1;
        	diggNode = XMLParser.getNextSibling(diggNode);
        }
        localArray = new JSONArray();
        localArray.put(data);
		test(depth, "构建Dashboard数据模型成功，计算总耗时%s毫秒, 结果: %s", System.currentTimeMillis() -ts, data.toString(4));
        finish();
	}

	/**
	 * 构建芒果数据库的数据
	 * @param e
	 * @param dataColumns
	 * @param diggIndx
	 * @throws Exception
	 */
	public void buildMongoDiggData(Node e, JSONObject dataColumns, int diggIndx) throws Exception{
		
	}
	/**
	 * 从ZK构建数据库
	 * @param e
	 * @param dataColumns
	 * @param diggIndx
	 * @throws Exception
	 */
	public void buildZookeeperDiggData(Node e) throws Exception{
		long ts = System.currentTimeMillis();
		String path = XMLParser.getElementAttr(e, "path");
		path = checker.parameter(path, request, "datamodel.dashboard.zookeeper", "path");
		String src = XMLParser.getElementAttr(e, "src");
		String count_name = XMLParser.getElementAttr(e, "count");
		path = path.substring(1);
		String args[] = Tools.split(path, "/");
		int value = 0;
		if( src.isEmpty() || src.equalsIgnoreCase("local") ){
			value = countZookeeper(ZKMgr.getZookeeper(), "", args, 0, e);
		}
		else{
			Zookeeper zookeeper = Zookeeper.getInstance(src);
			try{
				value = countZookeeper(zookeeper, "", args, 0, e);
			}
			catch(Exception ee){
				throw ee;
			}
			finally{
				zookeeper.close();
			}
		}
		test(depth+2, "计算耗时%s毫秒, 结果: %s", System.currentTimeMillis() - ts, value);
		data.put(count_name, value);
	}
	
	/**
	 * 计算ZK节点的数量
	 * @param zookeeper
	 * @param path
	 * @param args
	 * @param depth
	 * @param e
	 * @return
	 * @throws Exception
	 */
	private int countZookeeper(Zookeeper zookeeper, String path, String[] args, int depth, Node e) throws Exception{
		int c = 0;
		if( args.length <= depth ){
			String value = XMLParser.getElementAttr(e, "value");
			if( value.isEmpty() ){
				c = 1;
			}
			else{
				boolean decrypte = "true".equalsIgnoreCase(XMLParser.getElementAttr(e, "decrypte"));
				if( "array".equalsIgnoreCase(value) ) {
					JSONArray array = zookeeper.getJSONArray(path, decrypte);
					c = array.length();
				}
				else if( "object".equalsIgnoreCase(value) ){
					String key = XMLParser.getElementAttr(e, "key");
					JSONObject obj = zookeeper.getJSONObject(path, decrypte);
					if( obj.has(key) ){
						Object object = obj.get(key);
						if( object instanceof JSONArray ){
							c = ((JSONArray)object).length();
						}
						else if( object instanceof JSONObject ){
							c = ((JSONObject)object).length();
						}
					}
				}
			}
		}
		else {
			if(args[depth].equals("*")){
				Stat stat = zookeeper.exists(path);
				if( stat != null ){
					List<String> list = zookeeper.getChildren(path);
					for(String child : list){
						c += this.countZookeeper(zookeeper, path+"/"+child, args, depth+1, e);
					}
				}
			}
			else{
				c += this.countZookeeper(zookeeper, path+"/"+args[depth], args, depth+1, e);
			}
		}
		return c;
	}
	
	/**
	 * 通过OLTP构建数据库
	 * @param e
	 * @param dataColumns
	 * @param diggIndx
	 * @throws Exception
	 */
	public void buildOltpDiggData(Node e, JSONObject dataColumns, int diggIndx) throws Exception{
	}
	/**
	 * 构建SQL的查询
	 * @param e digg 节点
	 * @param dataIndx
	 * @throws Exception
	 */
	public void buildSqlDiggData(Node e, JSONObject dataColumns, int diggIndx) throws Exception{
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		long ts = System.currentTimeMillis();
		JSONObject diggObj = new JSONObject();
		try
		{
			String src = XMLParser.getElementAttr( e, "src" );
			src = checker.parameter(src, request, false);
			String from = XMLParser.getElementAttr( e, "from" );
	    	from = checker.parameter(from, request, false);
			if( from.startsWith("%") ){
				return;
			}
			JSONObject digg = new JSONObject();
			preloadDatabase(src, digg);

    		this.setQuerySql(e, dataColumns, sql, digg, diggIndx);
			Class.forName(driverClass); 
            connection = DriverManager.getConnection(jdbcUrl ,jdbcUsername, jdbcUserpswd);  
            statement = connection.createStatement();
            rs = statement.executeQuery(sql.toString());
            if( rs.next() )
            {
            	Iterator<?> iterator = dataColumns.keys();
            	while(iterator.hasNext()){
            		String dataIndx = iterator.next().toString();
            		JSONObject cell = dataColumns.getJSONObject(dataIndx);
            		if( cell.has("diggIndx") && cell.getInt("diggIndx") == diggIndx ){
            			this.data.put(dataIndx, rs.getObject(dataIndx));
            			diggObj.put(dataIndx, rs.getObject(dataIndx));
            		}
            	}
	        }
    		test(depth+2, "计算耗时%s毫秒, 结果: %s", System.currentTimeMillis() - ts, diggObj.toString(4));
		}
		catch(Exception e1)
		{
    		test(depth+2, "发现异常: %s", e1.getMessage());
    		throw e1;
		}
        finally
        {
        	if( rs != null )
				try
				{
					rs.close();
				}
				catch (SQLException e1)
				{
				}
        	if( statement != null )
				try
				{
					statement.close();
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
}
