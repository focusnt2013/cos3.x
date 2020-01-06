package com.focus.cos.web.dev.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.data.Stat;
import org.bson.Document;
import org.directwebremoting.WebContextFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.digg.Cache;
import com.focus.cos.web.common.AjaxResult;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.service.SvrMgr;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.util.JSON;

/**
 * 微信管理器
 * @author focus
 *
 */
public class DiggMgr extends SvrMgr
{
	private static final Log log = LogFactory.getLog(DiggMgr.class);
	/*记录每个模板的平均延时*/
	private BasicDBObject speedQuery = new BasicDBObject();
	private BasicDBObject speedExport = new BasicDBObject();
	private BasicDBObject speedApi = new BasicDBObject();
	
	/**
	 * 记录DIGG的速度
	 * @param gridxml
	 * @param duration
	 */
	public BasicDBObject getSpeedQuery() {
		return speedQuery;
	}
	public void setDiggQuerySpeed(String gridxml, long duration){
		speedQuery.put(gridxml, Kit.getDurationMs(duration));
	}
	/**
	 * 记录DIGG的速度
	 * @param gridxml
	 * @param duration
	 */
	public void setDiggExportSpeed(String gridxml, long duration){
		speedExport.put(gridxml, Kit.getDurationMs(duration));
	}
	public BasicDBObject getSpeedExport() {
		return speedExport;
	}
	/**
	 * 记录DIGG的速度
	 * @param gridxml
	 * @param duration
	 */
	public void setDiggApiSpeed(String gridxml, long duration){
		speedApi.put(gridxml, Kit.getDurationMs(duration));
	}
	public BasicDBObject getSpeedApi() {
		return speedApi;
	}
	/**
	 * 
	 * @return
	 */
	public AjaxResult<Integer> stopExport()
	{
		AjaxResult<Integer> result = new AjaxResult<Integer>();
		JSONObject progress = null;
		try
		{
			org.directwebremoting.WebContext web = WebContextFactory.get(); 
			HttpServletRequest request = web.getHttpServletRequest();
	        String referer = request.getHeader("referer");
	        progress = (JSONObject)request.getSession().getAttribute(referer + ".export");
	        request.getSession().setAttribute(referer + ".abort", 1);
	        log.info("Get the progress("+progress+") from "+referer);
	        result.setMessage("中止导出数据指令已经发出，当前状态是："+progress.getString("message")+"。之后的数据不会传送。");
			result.setSucceed(true);
		}
		catch(Exception e)
		{
			log.error("", e);
			result.setMessage("中止导出数据指令异常"+e.getMessage());
		}
		return result;
	}
	/**
	 * 获取下载CSV文件异常
	 * @return
	 */
	public AjaxResult<Integer> getExportProgress()
	{
		AjaxResult<Integer> result = new AjaxResult<Integer>();
		try
		{
			org.directwebremoting.WebContext web = WebContextFactory.get(); 
			HttpServletRequest request = web.getHttpServletRequest();
	        String referer = request.getHeader("referer");
	        referer += ".export";
	        JSONObject progress = (JSONObject)request.getSession().getAttribute(referer);
//	        log.info("Get the progress("+progress+") from "+referer);
			if( progress == null ){
				result.setResult(0);
				result.setMessage("没有获取下载任务的进度数据");
			}
			else
			{
				result.setMessage(progress.getString("message"));
				result.setResult(progress.getInt("value"));
				result.setSucceed(!progress.has("exception"));
				if(progress.has("finish")) result.setResult(200);
			}
		}
		catch(Exception e)
		{
			log.error("", e);
			result.setMessage("获取进度异常"+e.getMessage());
		}
		return result;
	}
	
	/**
	 * 得到执行数据的进度
	 * @return
	 */
	public AjaxResult<Integer> getDiggProgress()
	{
		AjaxResult<Integer> rsp = new AjaxResult<Integer>();
		try
		{
			org.directwebremoting.WebContext web = WebContextFactory.get(); 
			HttpServletRequest request = web.getHttpServletRequest();
	        String referer = request.getHeader("referer");
	        referer += ".digg";
	        JSONObject progress = (JSONObject)request.getSession().getAttribute(referer);
//	        log.info("Get the progress("+progress+") from "+referer);
			if( progress == null ){
				log.warn("Failed to get progress of digg from "+referer);
				rsp.setResult(0);
				rsp.setMessage("没有获取DIGG任务["+referer+"]的进度数据");
			}
			else
			{
				rsp.setMessage(progress.getString("message"));
				rsp.setSucceed(!progress.has("exception"));
				if(progress.has("finish")) rsp.setResult(200);
				else if(progress.has("value")) rsp.setResult(progress.getInt("value"));
				else rsp.setResult(0);
			}
		}
		catch(Exception e)
		{
			log.error("", e);
			rsp.setMessage("获取进度异常"+e.getMessage());
		}
		return rsp;
	}
	/**
	 * 删除缓存
	 * @param request
	 * @param account
	 */
	public void deleteDiggCache(String account, String gridxml)
	{
    	String zkpath = "/cos/data/digg/cache/"+account+"/"+Tools.encodeMD5(gridxml);
    	try
    	{
            Stat stat = ZKMgr.getZookeeper().exists(zkpath);
            if( stat != null )
            {
        		log.info("Clear the cache of digg from "+zkpath);
        		ZKMgr.getZookeeper().delete(zkpath, stat.getVersion());
        	}    		
    	}
    	catch(Exception e)
    	{
    	}
	}
	
	/**
	 * 保存Digg的缓存
	 * @param request
	 * @param account
	 * @param gridxml
	 * @param cache
	 */
	public void setDiggCache(String account, String gridxml, Cache cache)
	{
    	String zkpath = "/cos/data/digg/cache/"+account+"/"+Tools.encodeMD5(gridxml);
    	log.info("Cache the data("+cache.size()+") to "+zkpath);
		try
		{
			Zookeeper zookeeper = ZKMgr.getZookeeper();
	    	Stat stat = zookeeper.exists(zkpath);
	    	if( stat != null ) zookeeper.setData(zkpath, cache.toBytes(), stat.getVersion(), true);
	    	else zookeeper.create(zkpath, cache.toBytes(), true);
		}
		catch(Exception e)
		{
		}
	}
	
	/**
	 * 得到缓存
	 * @param request
	 * @param account
	 * @param gridxml
	 * @return
	 */
	public Cache getDiggCache(String account, String gridxml)
	{
		try
		{
			Zookeeper zookeeper = ZKMgr.getZookeeper();
	        String zkpath = "/cos/data/digg/cache/"+account;
	        Stat stat = zookeeper.exists(zkpath);
	        if( stat == null )
	        {
	        	zookeeper.createNode(zkpath, "元数据查询缓存".getBytes("UTF-8"));
	        }
	    	zkpath += "/"+Tools.encodeMD5(gridxml);
	    	stat = zookeeper.exists(zkpath);	
			if( stat != null )
			{
				byte[] payload = zookeeper.getData(zkpath, false, stat, true);
		    	if( payload != null )
		    		return new Cache(payload);
			}
    		log.info("Not found the cache("+gridxml+") of "+zkpath);
		}
		catch(Exception e)
		{
		}
		return new Cache();
	}
	

	/**
	 * 根据条件过滤指定的行数据
	 * @param conditions
	 * @param dataColumns
	 * @param row
	 * @return
	 */
    public boolean filterByConditions(
    		JSONArray conditions,
    		JSONObject dataColumns,
    		JSONObject row )
    {
		for(int i = 0; i < conditions.length(); i++)
		{
			JSONObject condition = conditions.getJSONObject(i);
			String dataIndx = condition.getString("dataIndx");
			/*if( dataColumns != null && dataColumns.has(name) )
			{
				JSONObject column = dataColumns.getJSONObject(name);
				name = column.getString("column");
			}*/
			if( !row.has(dataIndx) ){
//				System.err.println(row.toString(4));
				return true;
			}
			String value = null;//condition.getString("value");
//			String type = condition.getString("dataType");
			if( condition.getString("condition").equals("contain") )
			{
				String dataValue = row.getString(dataIndx);
				value = condition.getString("value");
				if( dataValue.indexOf(value) == -1 )
				{
					return true;
				}
			}
			else if( condition.getString("condition").equals("begin") )
			{
				String dataValue = row.getString(dataIndx);
				value = condition.getString("value");
				if( !dataValue.startsWith(value) )
				{
					return true;
				}
			}
			else if( condition.getString("condition").equals("range") )
			{
				String dataValue = row.get(dataIndx).toString();
				JSONArray range = condition.getJSONArray("value");
				int j = 0;
				for(j = 0; j < range.length(); j++)
				{
					value = range.get(j).toString();
					if( value.equals(dataValue) )
					{
						break;
					}
				}
				if( j == range.length() ) return true;
			}
			else if( condition.getString("condition").equals("equal") )
			{
				value = condition.getString("value");
				String dataValue = row.get(dataIndx).toString();
				if( !dataValue.equals(value) )
				{
					return true;
				}
			}
			else if( condition.getString("condition").equals("gte") )
			{
			}
			else if( condition.getString("condition").equals("lte") )
			{
			}
			else if( condition.getString("condition").equals("between") )
			{
			}
		}
		return false;
    }
    /**
     * 设置查询条件
     * @param pq_filter
     * @param sqlCoumns
     * @param diggIndx
     * @param sql
     * @param sqlCount
     * @param where
     */
    public static void setQueryConditions(
    		String filter,
    		JSONObject dataColumns,
    		String diggIndx,
    		StringBuffer sql)
    	throws Exception
    {

		JSONObject pq_filter = null;
        if( filter == null || filter.isEmpty() || !filter.startsWith("{"))
        {
        	pq_filter = new JSONObject();
        }
        else
        {
        	pq_filter = new JSONObject(filter);
        }
    	if( !pq_filter.has("data") ) return;
		String _sql = sql.toString().toLowerCase();
		if( _sql.indexOf("where") == -1 ){
			sql.append(" WHERE 1=1");
		}
		try
		{
			JSONArray conditions = pq_filter.getJSONArray("data");
			for(int i = 0; i < conditions.length(); i++)
			{//查询数据涉及中文，Unicode解码先
				JSONObject condition = conditions.getJSONObject(i);
				String dataIndx = condition.getString("dataIndx");
				String columnValue = dataIndx;
				if( dataColumns != null && dataColumns.has(dataIndx) )
				{
					JSONObject column = dataColumns.getJSONObject(dataIndx);
					if( column.has("column") ){
						JSONObject columns = column.getJSONObject("column");
						columnValue = columns.getString(diggIndx);
					}
				}
				String value = null;//condition.getString("value");
				String type = condition.getString("dataType");
				if( condition.getString("condition").equals("contain") )
				{
					value = condition.getString("value");
					if( value.isEmpty() ) continue;
					if( value.toLowerCase().equals("null") )
					{
						sql.append(" AND ");
						sql.append(columnValue+" is null");
					}
					else
					{
						sql.append(" AND ");
						sql.append(columnValue+" like '%"+value+"%'");
					}
				}
				else if( condition.getString("condition").equals("begin") )
				{
					value = condition.getString("value");
					if( value.isEmpty() ) continue;
					if( value.toLowerCase().equals("null") )
					{
						sql.append(" AND ");
						sql.append(columnValue+" is "+value);
					}
					else
					{
						sql.append(" AND ");
						sql.append(columnValue+" like '"+value+"%'");
					}
				}
				else if( condition.getString("condition").equals("range") )
				{
					JSONArray range = condition.getJSONArray("value");
					if( range.length() == 0 ){
						continue;
					}
					StringBuffer sb = new StringBuffer();
					for(int j = 0;j < range.length();j++)
					{
						Object o = range.get(j);
						if( j > 0 ) sb.append(",");
						if( type.equals("string") )	sb.append("'"+o+"'");
						else sb.append(o);
					}
					sql.append(" AND ");
					sql.append(columnValue+" in ("+sb+")");
				}
				else if( condition.getString("condition").equals("equal") )
				{
					value = condition.getString("value");
					if( value.isEmpty() ) continue;
					sql.append(" AND ");
					if( type.equals("string") || "time".equals(type) )
					{
						sql.append(columnValue+"='"+value+"'");
					}
					else
					{
						sql.append(columnValue+"="+value);
					}
				}
				else if( condition.getString("condition").equals("gte") )
				{
					value = condition.getString("value");
					if( value.isEmpty() ) continue;
					sql.append(" AND ");
					if( "int".equalsIgnoreCase(type) || 
						"long".equalsIgnoreCase(type) ||
						"number".equalsIgnoreCase(type) )
					{
						sql.append(columnValue+">="+value);
					}
					else
					{
						sql.append(columnValue+">='"+value+"'");
					}
				}
				else if( condition.getString("condition").equals("lte") )
				{
					value = condition.getString("value");
					if( value.isEmpty() ) continue;
					sql.append(" AND ");
					if( type.equals("string") || type.equalsIgnoreCase("time") )
					{
						sql.append(columnValue+"<'"+value+"'");
					}
					else
					{
						sql.append(columnValue+"<"+value);
					}
				}
				else if( condition.getString("condition").equals("between") )
				{
					value = condition.getString("value");
					if( value.isEmpty() ) continue;
					sql.append(" AND ");
					String value2 = condition.getString("value2");
					if( type.equals("string") || type.equalsIgnoreCase("time") )
					{
						sql.append(columnValue+">='"+value+"'");
						sql.append(" AND ");
						sql.append(columnValue+"<'"+value2+"'");
					}
					else
					{
						sql.append(columnValue+">="+value);
						sql.append(" AND ");
						sql.append(columnValue+"<"+value2);
					}
				}
			}
		}
		catch(Exception e)
		{
			log.error("Failed to set condition of query"+pq_filter.toString(4), e);
			throw e;
		}
    }
	
    /**
     * 设置查询条件
     * @param pq_filter
     * @param sqlCoumns
     * @param sql
     * @param sqlCount
     * @param where
     * @throws ParseException 
     */
	private boolean setJoinFuzzyWhere(BasicDBObject where, String name, String value, JSONObject dataColumns)
		throws Exception
	{
		if( dataColumns == null || !dataColumns.has(name) ) return false;
//		log.info(dataColumns.toString(4));
		JSONObject column = dataColumns.getJSONObject(name);
		if( !column.has("join") ) return false;
		JSONObject join = column.getJSONObject("join");
		if( !join.has("label") || !join.has("key") ) return false;
		String key = join.getString("key");
		String dbtype = join.getString("dbtype");
		if( "mongo".equalsIgnoreCase(dbtype) )
		{
			MongoCollection<Document> col = getMongoCollection(
				join.getString("mongo.host"), 
				join.getInt("mongo.port"),
				join.getString("mongo.username"),
				join.getString("mongo.password"),
				join.getString("mongo.database"),
				join.getString("mongo.database1"),
				join.getString("mongo.tablename"));
			BasicDBObject regex = new BasicDBObject();
			regex.put("$regex", value);
			regex.put("$options", "i");
			BasicDBList ranges = new BasicDBList();
			String label = join.getString("label");
			MongoCursor<Document> cursor = col.find(new BasicDBObject(label, regex)).iterator();
			if( cursor != null )
				while(cursor.hasNext())
				{
					Document doc = cursor.next();
					ranges.add(doc.get(key));
				}
			if( ranges.isEmpty() ) return false;
			where.put(name, new BasicDBObject("$in", ranges));
			return true;
		}
		else
		{
		}
		return false;
	}
	/**
	 * 
	 * @param pq_filter
	 * @param where
	 * @param dataColumns
	 * @throws Exception
	 */
    public void setMongoQueryConditions(String pq_filter, BasicDBObject where, JSONObject dataColumns) throws Exception
    {
		if( pq_filter == null || !pq_filter.startsWith("{") || where == null)
		{
			return;
		}
		BasicDBObject filter = (BasicDBObject)JSON.parse(pq_filter);
//		pq_filter={"mode":"AND","data":[{"dataIndx":"name","value":"49639659B08B","condition":"begin","dataType":"string","cbFn":""}]}	
//		_=1451468973133
		BasicDBList array = (BasicDBList)filter.get("data");
		for(int i = 0; i < array.size(); i++)
		{//查询数据涉及中文，Unicode解码先
			BasicDBObject condition = (BasicDBObject)array.get(i);
			String name = condition.getString("dataIndx");
			Object value1;
			Object value2;
			String value = condition.getString("value");
			String dataType = condition.getString("dataType");
			if( condition.getString("condition").equals("contain") )
			{
				if( value.toLowerCase().equals("null") )
				{
					where.put(name, null);
				}
				else if( !this.setJoinFuzzyWhere(where, name, value, dataColumns) )
				{
					if("tag".equalsIgnoreCase(dataType))
					{
						String args[] = Tools.split(value, ",");
						BasicDBList ranges = new BasicDBList();//(BasicDBList)condition.get("value");
						for(String arg: args){
							ranges.add(arg.trim());
						}
						where.put(name, new BasicDBObject("$all", ranges));
					}
					else
					{
						BasicDBObject regex = new BasicDBObject();
						regex.put("$regex", value);
						regex.put("$options", "i");
						where.put(name, regex);
					}
				}
			}
//			else if( condition.getString("condition").equals("in") )
//			{
//				if( value.toLowerCase().equals("null") )
//				{
//					where.put(name, "");
//				}
//				else
//				{
//					BasicDBObject regex = new BasicDBObject();
//					regex.put("$regex", value);
//					regex.put("$options", "i");
//					where.put(name, regex);
//				}
//			}
			else if( condition.getString("condition").equals("begin") )
			{
//				value = Common.unicode2Chr(value);
				if( value.toLowerCase().equals("null") )
				{
					where.put(name, "");
				}
				else
				{
					BasicDBObject regex = new BasicDBObject();
					regex.put("$regex", value);
					regex.put("$options", "i");
					where.put(name, regex);
				}
			}
			else if( condition.getString("condition").equals("range") )
			{
				BasicDBList ranges = (BasicDBList)condition.get("value");
				if( !dataType.equalsIgnoreCase("string") )
				{
					for(int j = 0;j < ranges.size();j++)
					{
						String n = (String)ranges.get(j);
						if( n != null && Tools.isNumeric(n)  )
						{
							if(n.indexOf('.') != -1) ranges.set(j, Integer.parseInt(n));
							else ranges.set(j, Double.parseDouble(n));
						}
						else
						{
							ranges.set(j, null);
						}
					}
				}
				where.put(name, new BasicDBObject("$in", ranges));
			}
			else if( condition.getString("condition").equals("equal") )
			{
				if( !dataType.equalsIgnoreCase("string") && !dataType.equalsIgnoreCase("tag") )
				{
					String n = condition.get("value").toString();
					if( n != null && Tools.isNumeric(n))
					{
						if(n.indexOf('.') == -1){
							where.put(name, Integer.parseInt(n));
						}
						else where.put(name, Double.parseDouble(n));
					}
				}
				else{
					where.put(name, condition.get("value"));
				}
			}
			/*else if( condition.getString("condition").equals("gte") )
			{
				if( "ts".equals(dataType) )
				{
					where.remove(name);
					int time = (int)(sdf.parse(value).getTime()/1000);
					where.put("subscribe_time", new BasicDBObject("$gte", time));
				}
				else
				{
					value1 = condition.get("value");
					if( value1 instanceof String )
					{
						if( "integer".equals(dataType) )
						{
							if( value1 != null ){
								value1 = Integer.parseInt(value1.toString());
							}
						}
						else if( "long".equals(dataType) )
						{
							if( value1 != null ){
								value1 = Long.parseLong(value1.toString());
							}
						}
						else if( "float".equals(dataType) )
						{
							if( value1 != null ){
								value1 = Double.parseDouble(value1.toString());
							}
						}
					}
					if( value1 != null )
						where.put(name, new BasicDBObject("$gte", value1));
				}
			}*/
			else if( condition.getString("condition").equals("lte") || condition.getString("condition").equals("gte") )
			{
				value1 = condition.get("value");
				if( "time".equals(dataType) )
				{
					if( dataColumns.has(name) ){
						JSONObject column = dataColumns.getJSONObject(name);
	        			String format = column!=null&&column.has("format")?column.getString("format"):"yyyy-MM-dd HH:mm:ss";
						if( column.has("dataType0") ){
							format = format.substring(0, value1.toString().length());
					        SimpleDateFormat sdf = new SimpleDateFormat(format);
							String dataType0 = column.getString("dataType0");
							if( dataType0.equalsIgnoreCase("long") ){
								value1 = sdf.parse(value1.toString()).getTime();
							}
							else if( dataType0.equalsIgnoreCase("integer") ){
								value1 = (int)(sdf.parse(value1.toString()).getTime()/1000);
							}
						}
					}
				}
				else{
					if( value1 instanceof String )
					{
						if( "integer".equals(dataType) )
						{
							value1 = Integer.parseInt(value1.toString());
						}
						else if( "long".equals(dataType) )
						{
							value1 = Long.parseLong(value1.toString());
						}
						else if( "float".equals(dataType) )
						{
							value1 = Double.parseDouble(value1.toString());
						}
					}
				}
				if( value1 != null )
					where.put(name, new BasicDBObject("$"+condition.getString("condition"), value1));
			}
			else if( condition.getString("condition").equals("between") )
			{
				value1 = condition.get("value");
				value2 = condition.get("value2");
				if( value1 instanceof String || value2 instanceof String )
				{
					if( "time".equals(dataType) )
					{
						if( dataColumns.has(name) ){
							JSONObject column = dataColumns.getJSONObject(name);
		        			String format = column!=null&&column.has("format")?column.getString("format"):"yyyy-MM-dd HH:mm:ss";
							if( column.has("dataType0") ){
						        SimpleDateFormat sdf = new SimpleDateFormat(format);
								String dataType0 = column.getString("dataType0");
								if( dataType0.equalsIgnoreCase("long") ){
									if( value1 != null ){
										value1 = sdf.parse(value1.toString()).getTime();
									}
									if( value2 != null ){
										value2 = sdf.parse(value2.toString()).getTime();
									}
								}
								else if( dataType0.equalsIgnoreCase("integer") ){
									if( value1 != null ){
										value1 = (int)(sdf.parse(value1.toString()).getTime()/1000);
									}
									if( value2 != null ){
										value2 = (int)(sdf.parse(value2.toString()).getTime()/1000);
									}
								}
							}
						}
					}
					else if( "integer".equals(dataType) )
					{
						if( value1 != null ){
							value1 = Integer.parseInt(value1.toString());
						}
						if( value2 != null ){
							value2 = Integer.parseInt(value2.toString());
						}
					}
					else if( "long".equals(dataType) )
					{
						if( Tools.isNumeric(value1.toString()) ){
							value1 = Long.parseLong(value1.toString());
						}
						if( Tools.isNumeric(value2.toString()) ){
							value2 = Long.parseLong(value2.toString());
						}
					}
					else if( "float".equals(dataType) )
					{
						if( value1 != null ){
							value1 = Double.parseDouble(value1.toString());
						}
						if( value2 != null ){
							value2 = Double.parseDouble(value2.toString());
						}
					}
				}
				BasicDBObject range = new BasicDBObject();
				if( value1 != null )
					range.append("$gte", value1);
				if( value2 != null )
					range.append("$lte", value2);
				if( !range.isEmpty() ) where.put(name, range);
			}
		}
    }
	/**
	 * 关闭数据库释放资源
	 */
	public synchronized void close()
	{
		Iterator<MongoClient> iterator = Mongos.values().iterator();
		while(iterator.hasNext())
		{
			iterator.next().close();
		}
		notifyAll();
	}
}
