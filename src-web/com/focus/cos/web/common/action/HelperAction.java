package com.focus.cos.web.common.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.api.SysemailClient;
import com.focus.cos.web.Version;
import com.focus.cos.web.action.GridAction;
import com.focus.cos.web.common.HelperMgr;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.util.IOHelper;
import com.focus.util.QuickSort;
import com.focus.util.Tools;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.opensymphony.xwork.ModelDriven;

/**
 * 一些服务组件
 * @author focus
 *
 */
public class HelperAction extends GridAction implements ModelDriven
{
	private static final Log log = LogFactory.getLog(HelperAction.class);
	private static final long serialVersionUID = -5227997046023996176L;
	private HelperMgr helperMgr;
	private String filename;
	/*经纬度精度*/
	private String longitude;
	private String latitude;
	private String precision;
	/*输出脚本*/
	private String html;
	/*正则表达式例程*/
	private String regexpexample;
	/**
	 * 颜色表
	 * @return
	 */
	public String colors()
	{
		List<String> pq_cellcls = IOHelper.readLines(this.getClass().getResourceAsStream("/com/focus/cos/web/action/pq_cls.css"), null);
		QuickSort sorter = new QuickSort() {
			@Override
			public boolean compareTo(Object sortSrc, Object pivot) {
				String l = sortSrc.toString().split(",")[1].substring(1);
				String r = pivot.toString().split(",")[1].substring(1);
				return Integer.parseInt(l, 16) < Integer.parseInt(r, 16);
			}
		};
		sorter.sort(pq_cellcls);
		StringBuilder sb = new StringBuilder();
		StringBuilder html = new StringBuilder();
		int i = 0, k = 0, d = 4;
		String col = super.getRequest().getParameter("col");
		if( col != null ){
			d = Integer.parseInt(col);
		}
		for(String css : pq_cellcls){
        	String args[] = Tools.split(css, ",");
        	String color = "#fff";
        	k = Integer.parseInt(args[1].substring(1), 16);
        	if( k >= 0xdcdcdc ){
        		color = "#000";
        	}
//        	tr td.blue
//        	{
//        	    background: #00ccff;
//        	    color: #fff;
//        	}
        	if( i%d == 0 ){
        		html.append("<tr>");	
        	}
        	html.append(String.format("<td class='%s' align='center' style='width:160px;cursor:pointer' title='%s' onclick='a(this)'>%s</td>", args[0],args[1],args[0]));
        	sb.append(String.format("\r\ntr td.%s{background: %s;color: %s;}", args[0], args[1], color));
        	if( i%d == d-1 ){
        		html.append("</tr>");	
        	}
        	i += 1;
        }
		this.html = html.toString();
		this.pq_cellcls = sb.toString();
		return "colors";
	}
	/**
	 * 显示进度页面
	 * @return
	 */
	public String progress()
	{
		return "progress";
	}
	/**
	 * 时间线
	 * @return
	 */
	public String timeline()
	{
		super.jsonData = super.getRequest().getParameter("dataurl");
		jsonData = Kit.unicode2Chr(jsonData);
//		log.debug("timeline: "+jsonData);
		return "timeline";
	}
	/**
	 * 开发手册帮助
	 * @return
	 */
	public String developer()
	{
		if( id == null || id.isEmpty() ) id = "start";
		return id;
	}

	/**
	 * 处理异常的页面
	 * @return
	 */
	public String exception()
	{
		Exception e = (Exception)getSession().getAttribute("exception");
		if( e == null )
		{
			return "close";
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(out);
		e.printStackTrace(writer);
		this.regexpexample = new String(out.toByteArray());
		try {
			out.close();
		} catch (IOException e1) {
		}
		return "exception";
	}
	/**
	 * 正则表达式工具
	 * @return
	 */
	public String regexp()
	{
		InputStream is = this.getClass().getResourceAsStream("/com/focus/cos/web/ops/action/regexp.txt");
		try
		{
			this.regexpexample = new String(IOHelper.readAsByteArray(is), "UTF-8");
			return "regexp";
		} 
		catch (Exception e) 
		{
			super.setResponseException("解析正则表达式实例文件出现异常"+e);
			return "close";
		}
		finally
		{
			try {
				is.close();
			} catch (IOException e) {
			}
		}
	}
	/**
	 * 百度地图
	 * @return
	 */
	public String baidumap()
	{
		return "baidumap";
	}
	/**
	 * SQL查询分析器
	 * @return
	 */
	public String sqlquery()
	{
		log.debug("Show the database "+db);
		if( db != null && db.equals("report ") ) db = "report+";
		return "sqlquery";
	}
	
	/**
	 * 根据db参数加载
	 * @throws Exception
	private void loadJdbc() throws Exception
	{
		if( jdbcUrl == null || jdbcUrl.isEmpty() )
		{
			if( db == null || db.isEmpty() || db.equalsIgnoreCase("local") ) db = "cos";
			if( "cos".equals(db) )
			{
				Properties jdbcConfig = new Properties();
		    	File webapppath = new File(PathFactory.getWebappPath().getParentFile(), db);
		    	jdbcConfig.load(new FileInputStream(new File(webapppath, "WEB-INF/classes/config/jdbc.properties")));
		    	jdbcUrl = jdbcConfig.getProperty("jdbc.url");
		    	jdbcUsername = jdbcConfig.getProperty("jdbc.username");
		    	jdbcUserpswd = jdbcConfig.getProperty("jdbc.password");
		    	driverClass = jdbcConfig.getProperty("jdbc.driverClass");
			}
			else if( db.startsWith("/cos/config/monitor/") )
			{
				JSONObject jdbcConfig = ZKMgr.getZookeeper().getJSONObject(db);
				if( jdbcConfig != null )
				{
			    	jdbcUrl = jdbcConfig.getString("jdbc.url");
			    	jdbcUsername = jdbcConfig.getString("jdbc.username");
			    	jdbcUserpswd = jdbcConfig.getString("jdbc.password");
			    	jdbcUserpswd = new String(Base64X.decode(jdbcUserpswd));
			    	driverClass = jdbcConfig.getString("jdbc.driver");
				}
			}
		}
		if( jdbcUserpswd == null ) jdbcUserpswd = "";
		if( jdbcUrl == null || jdbcUrl.isEmpty() || jdbcUsername == null || jdbcUsername.isEmpty() || driverClass == "" || driverClass.isEmpty() )
		{
			throw new Exception("因为jdbc配置连接参数("+db+")无效执行数据库加载");
		}
	}
	 */
	/**
	 * 数据库
	 * @return
	 */
	public String database()
	{
		if( db == null || db.isEmpty() || db.equalsIgnoreCase("local") ){
			if( jdbcUrl == null || jdbcUrl.isEmpty() ){
				db = "cos";
			}
			else{
				values.put("db", "");
			}
		}
		String rsp = grid("/grid/local/sysdatabase.xml");
		if( this.checker.errcount() > 0 ){
			log.error(this.checker.toString());
		}
		return rsp;
	}

	/**
	 * 显示指定数据库的数据结构
	 * @return
	 */
	public String databasedata()
	{
    	ServletOutputStream out = null;
		Connection connection = null;
		ResultSet rs = null;
		Statement statement = null;
		BasicDBObject dataJSON = new BasicDBObject();
		StringBuilder sb = new StringBuilder();
		sb.append("\r\n\tReceive the request, the parameters of below ");
		HttpServletRequest req = super.getRequest();
		Iterator<Map.Entry<String, String[]>> iterator = req.getParameterMap().entrySet().iterator();
		while(iterator.hasNext())
		{
			Map.Entry<String, String[]> e = iterator.next();
			sb.append("\r\n\t\t");
			sb.append(e.getKey());
			sb.append("=");
			for(String value : e.getValue())
				sb.append(value+"\t");
		}
		String sql = "";
		BasicDBList data = new BasicDBList();
		int count = 0;
		try
		{
			presetDatabase(super.db);
    		HttpServletResponse response = super.getResponse();
    		response.setContentType("text/json;charset=utf8");
    		response.setHeader("Content-disposition", "inline; filename="+System.currentTimeMillis()+".json");
    		out = response.getOutputStream();
            Class.forName(driverClass); 
//            System.err.println("1:"+
//        		"\r\n\t"+driverClass+
//        		"\r\n\t"+jdbcUrl+
//        		"\r\n\t"+jdbcUsername+
//        		"\r\n\t"+jdbcUserpswd);
            connection = DriverManager.getConnection(jdbcUrl ,jdbcUsername, jdbcUserpswd);
            String tableNamePattern = super.getRequest().getParameter("tableNamePattern");
            if( tableNamePattern != null )
            {
            	tableNamePattern = "%"+tableNamePattern+"%";
            }
            rs =  connection.getMetaData().getTables(null, null, tableNamePattern, new String[]{"TABLE"});
            statement = connection.createStatement();
            sb.append("\r\n\tQuery all table from tableNamePattern("+tableNamePattern+")");
            while( rs.next() )
            {
            	BasicDBObject row = new BasicDBObject();
            	String tableName = rs.getObject("TABLE_NAME").toString();
				row.put("TABLE_NAME", rs.getObject("TABLE_NAME"));
				row.put("TABLE_TYPE", rs.getObject("TABLE_TYPE"));
				row.put("TABLE_CAT", rs.getObject("TABLE_CAT"));
				row.put("TABLE_SCHEM", rs.getObject("TABLE_SCHEM"));
				row.put("REMARKS", rs.getObject("REMARKS"));
				ResultSet prs = connection.getMetaData().getPrimaryKeys(rs.getString("TABLE_CAT"), rs.getString("TABLE_SCHEM"), tableName);
				if( prs.next() )
				{
					row.put("COLUMN_NAME", prs.getObject("COLUMN_NAME"));
					row.put("KEY_SEQ", prs.getObject("KEY_SEQ"));
					row.put("PK_NAME", prs.getObject("PK_NAME"));
				}
				sb.append("\r\n\t\t"+row.toString());
				prs.close();

				String schem = row.getString("TABLE_SCHEM");
				if( schem == null ) schem = "";
				if( "oracle".equals(datatype) )
				{
					if( schem.startsWith("APEX") ||
						schem.startsWith("APPQOSSYS") ||
						schem.startsWith("EXFSYS") ||
						schem.startsWith("DBSNMP") ||
						schem.startsWith("MDSYS") ||
						schem.startsWith("CTXSYS") ||
						tableName.indexOf("$") != -1 ||
						tableName.indexOf("/") != -1){
						continue;
					}
				}
				sql = "select count(1) from "+tableName;
				if( !"oracle".equals(datatype) )
				{
					ResultSet crs = statement.executeQuery(sql);
					if( crs.next() ) row.put("TABLE_COUNT", crs.getInt(1));
					else row.put("TABLE_COUNT", 0);
					crs.close();
				}
            	data.add(row);
            	count += 1;
				ResultSet rs1 = connection.getMetaData().getColumns( null, "%", tableName, "%");
				BasicDBList columns = new BasicDBList();
				while(rs1.next())
				{
	            	BasicDBObject column = new BasicDBObject();
	            	column.put("COLUMN_NAME", rs1.getString("COLUMN_NAME"));
	            	column.put("TYPE_NAME", null);
					String TYPE_NAME = rs1.getString("TYPE_NAME");
					if( TYPE_NAME != null )
					{
						column.put("TYPE_NAME", TYPE_NAME);
					}
					column.put("DATA_TYPE", rs1.getInt("DATA_TYPE"));
					column.put("COLUMN_SIZE", rs1.getInt("COLUMN_SIZE"));
					column.put("NULLABLE", rs1.getInt("NULLABLE")==1?"是":"否");
					if( !"oracle".equals(datatype) )
					{
						column.put("IS_AUTOINCREMENT", rs1.getString("IS_AUTOINCREMENT"));
					}
					column.put("REMARKS", null);
					String REMARKS = rs1.getString("REMARKS");
					if( REMARKS != null )
					{
						column.put("REMARKS", REMARKS);
					}
					column.put("COLUMN_DEF", null);
					String COLUMN_DEF = rs1.getString("COLUMN_DEF");
					if( COLUMN_DEF != null )
					{
						column.put("COLUMN_DEF", COLUMN_DEF);
					}
					columns.add(column);
				}
				row.put("columns", columns);
				rs1.close();
	        }
		}
		catch (Exception e)
		{
			log.error("Failed to get the database by sql("+sql+") for exception:"+sb, e);
			dataJSON.put("hasException", true);
			dataJSON.put("message", "查询数据库出现异常:"+e);
		}
        finally
        {
			dataJSON.put("totalRecords", count);
			dataJSON.put("curPage", 0);
			dataJSON.put("data", data);
    		try
			{
				String json = dataJSON.toString();
    			out.write(json.getBytes("UTF-8"));
            	if( out != null ) out.close();
			}
			catch (IOException e)
			{
				log.error("", e);
			}
			if( statement != null )
				try
				{
					statement.close();
				}
				catch (SQLException e1)
				{
				}
        	if( rs != null )
				try
				{
					rs.close();
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
		return null;
	}
	
	/**
	 * 执行SQL语句
	 * @return
	 */
	public String sql()
	{
		PrintWriter out = null;
		try
		{
			HttpServletRequest req = super.getRequest();
			HttpServletResponse rsp = super.getResponse();
			if( sql != null ) sql = sql.trim();
        	JSONObject digg = new JSONObject();
    		if(super.db != null && !super.db.isEmpty() )
        	{
    			presetDatabase(super.db, digg);
        	}
			Connection connection = null;
			Statement statement = null;
			ResultSet rs = null;
	        try 
	        {
	            Class.forName(driverClass); 
	            connection = DriverManager.getConnection(jdbcUrl ,jdbcUsername, jdbcUserpswd);  
	            if(sql.trim().toLowerCase().startsWith("select"))
	            {
	            	int i = sql.toLowerCase().lastIndexOf(" limit ");
	            	if( i > 0 )
	            	{
	            		sql = sql.substring(0, i);
	            	}
	            	JSONObject sObject = new JSONObject();
	            	JSONArray diggs = new JSONArray();
	            	sObject.put("diggs", diggs);
	            	diggs.put(digg);
	            	digg.put("type", "sql");
	            	digg.put("sql", sql);
		    		statement = connection.createStatement();
		            rs = statement.executeQuery(sql);
		    		StringBuffer dataM = new StringBuffer();
		    		StringBuffer colM = new StringBuffer();
//		    		boolean groupby = sql.trim().toLowerCase().indexOf("group by") != -1;
	    			filterModel.put("header", true);
		    		JSONArray titleColumns = helperMgr.setModelOfSql(rs, "digg!data.action", colM, dataM, details, filterModel, digg);
		    		sObject.put("titleColumns", titleColumns);
		    		sObject.put("gridtitle", "SQL查询");
//		    		exportUrl = "digg!export.action";
		        	this.colModel = colM.toString();
		        	this.dataModel = dataM.toString();
		        	sObject.put("colModel", colModel);
		        	super.getSession().setAttribute(Kit.URLPATH(req), sObject);
//            		log.debug(colM.toString());
//            		log.debug(dataM.toString());
		        	this.editable = true;
					this.pageModel = "{ type: dataModel.location, rPP: 20, strRpp: '{0}', rPPOptions: [1, 10, 20, 30, 40, 50, 100, 500, 1000] }";
		    		return "gridquery";
	            }
	            if(sql.toLowerCase().startsWith("create") 
				|| sql.toLowerCase().startsWith("drop")
				|| sql.toLowerCase().startsWith("delete")
				|| sql.toLowerCase().startsWith("update")
				|| sql.toLowerCase().startsWith("alter")
				|| sql.toLowerCase().startsWith("declare")
				|| sql.toLowerCase().startsWith("show")
				|| sql.toLowerCase().startsWith("truncate"))
				{
		            statement = connection.createStatement();
					int count = statement.executeUpdate(sql);
					out = new PrintWriter(new OutputStreamWriter(rsp.getOutputStream(), "UTF-8"));
					rsp.setContentType("text/plain");
					rsp.setCharacterEncoding("UTF-8");
					rsp.setHeader("Content-disposition", "inline; filename=sql.txt");
					out.println(sql);
					out.println("......");
					out.println();
					out.flush();
					out.println("Influence "+count+" rows.");
				}
				else
				{
					out = new PrintWriter(new OutputStreamWriter(rsp.getOutputStream(), "UTF-8"));
					rsp.setContentType("text/plain");
					rsp.setCharacterEncoding("UTF-8");
					rsp.setHeader("Content-disposition", "inline; filename=sql.txt");
					out.println(sql);
					out.println("......");
					out.println();
					out.flush();
				}
	        }
	        catch (Exception e)
	        {
				out = new PrintWriter(new OutputStreamWriter(rsp.getOutputStream(), "UTF-8"));
				rsp.setContentType("text/plain");
				rsp.setCharacterEncoding("UTF-8");
	    		rsp.setHeader("Content-disposition", "inline; filename=sql.txt");
	    		out.println(sql);
	    		out.println("......");
	    		out.println();
	    		out.flush();
	        	e.printStackTrace(out);
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
			return null;
		}
		catch(Exception e)
		{
			log.error("Failed to execute sql ", e);
			this.responseException = "Failed to execute sql:"+ e.getMessage();
			return "alert";
		}
		finally
		{
			if( out != null ) out.close();
		}
	}
	
	/**
	 * 执行升级
	 * @return
	 */
	public String upgrade()
	{
		File upgradeTempDir = new File(PathFactory.getAppPath(), "temp/upgrade/web");
		if( upgradeTempDir.exists() )
		{
			File file = new File(upgradeTempDir, "version.txt");
			if( file.exists() )
			{
				try
				{
					this.responseConfirm = "升级版本是: "+IOHelper.readFirstLine(file);
					this.responseMessage = new String(IOHelper.readAsByteArray(file), "UTF-8");
					this.editable = true;
				} 
				catch (UnsupportedEncodingException e)
				{
				}
			}
			if( !editable )
				responseConfirm = "升级文件可能已经损坏或被删除请重新下载升级文件。";
		}
		else
		{
			File file = new File(PathFactory.getWebappPath(), "version.txt");
			if( file.exists() )
			{
				try
				{
					responseConfirm = "升级已经及完成。您当前的版本是："+IOHelper.readFirstLine(file);
					this.responseMessage = new String(IOHelper.readAsByteArray(file), "UTF-8");
				} 
				catch (UnsupportedEncodingException e)
				{
				}
			}
			else
			{
				responseConfirm = "升级已经及完成。您当前的版本是："+Version.getValue();
			}
		}
		return "upgrade";
	}

	/**
	 * 
	 * @return
	 */
	public String pdf()
	{
		OutputStream out = null;
//    	HttpServletRequest request = ServletActionContext.getRequest();
    	HttpServletResponse response = super.getResponse();
        int offset = 0;
		try
		{
			File file = new File(PathFactory.getWebappPath(), filename);
			response.setContentType("application/pdf");
			if( file.exists() )
			{
				response.setContentLength((int)file.length());
				response.setHeader("Content-disposition", "inline; filename="+filename);
				response.setHeader("Access-Control-Allow-Origin", "*");
				response.setHeader("Access-Control-Allow-Methods", "*");
				out = response.getOutputStream();
				FileInputStream fis = null;
                fis = new FileInputStream( file );
                byte[] buffer = new byte[ 64*1024 ];
                int len = 0;
                while( (len=fis.read(buffer)) != -1 )
                {
                	out.write(buffer, 0, len);
                	offset += len;
        			out.flush();
                }
                fis.close();
                log.info("Succeed to download "+offset);
			}
			else
			{
				response.setContentType("text/html");
				response.setCharacterEncoding("UTF-8");
				response.getOutputStream().write("<script type='text/javascript' language='javascript'>if(top&&top.skit_alert){top.skit_alert('您要浏览的文件不存在。');}else{alert('您要下载的文件不存在。');}</script>".getBytes("UTF-8"));
			}
		}
		catch(Exception e)
		{
			log.error("Failed to pdf(offset="+offset+") "+filename+", "+e);
			response.setStatus(500);
		}
		finally
		{
			if( out != null )
				try
				{
					out.close();
				}
				catch(Exception e)
				{
					log.error("Failed to close response.", e);
				}
		}
		return null;
	}

	/**
	 * 显示所有的图标
	 * @return
	 */
	public String fa()
	{
    	URL url = HelperAction.class.getClassLoader().getResource("/");
    	if( url != null )
    	{
    		File file = new File(url.getFile(), "../../skin/defone/css/font-awesome.css");
            BufferedReader reader = null;  
            try 
            {  
            	ArrayList<String> list = new ArrayList<String>();
    	        // 定义BufferedReader输入流来读取URL的响应  
    	        reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));  
    	        String line = null;  
    	        while ((line = reader.readLine()) != null) 
    	        {  
    	        	int i = line.indexOf(":before");
    	        	if( line.startsWith(".fa-") && i != -1 )
    	        	{
    	        		list.add(line.substring(1, i));
    	        	}
    	        } 
    	        this.listData = list;
    	        QuickSort sorter = new QuickSort(){

					@Override
					public boolean compareTo(Object sortSrc, Object pivot)
					{
						String l = (String)sortSrc;
						String r = (String)pivot;
						return l.compareTo(r)<0;
					}
    	        	
    	        };
    	        sorter.sort(list);
    	    }
            catch (IOException e) 
            { 
            	log.error("Failed to list fa", e);
            } 
            finally
            {  
    	        if(reader!=null)
    	        {  
    	        	try
					{
						reader.close();
					}
					catch (IOException e)
					{
					}  
    	        }  
            }  
    	}
		return "fa";
	}

	/**
	 * 显示所有的图标
	 * @return
	 */
	public String icon()
	{
    	URL url = HelperAction.class.getClassLoader().getResource("/");
    	if( url != null )
    	{
    		File file = new File(url.getFile(), "../../skit/css/font-awesome.css");
            BufferedReader reader = null;  
            try 
            {  
            	ArrayList<String> list = new ArrayList<String>();
    	        // 定义BufferedReader输入流来读取URL的响应  
    	        reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));  
    	        String line = null;  
    	        while ((line = reader.readLine()) != null) 
    	        {  
    	        	int i = line.indexOf(":before");
    	        	if( line.startsWith(".icon-") && i != -1 )
    	        	{
    	        		list.add(line.substring(1, i));
    	        	}
    	        } 
    	        this.listData = list;
    	        QuickSort sorter = new QuickSort(){

					@Override
					public boolean compareTo(Object sortSrc, Object pivot)
					{
						String l = (String)sortSrc;
						String r = (String)pivot;
						return l.compareTo(r)<0;
					}
    	        	
    	        };
    	        sorter.sort(list);
    	    }
            catch (IOException e) 
            { 
            	log.error("Failed to list fa", e);
            } 
            finally
            {  
    	        if(reader!=null)
    	        {  
    	        	try
					{
						reader.close();
					}
					catch (IOException e)
					{
					}  
    	        }  
            }  
    	}
		return "icon";
	}

	/**
	 * 发送网页快照给指定的邮件收件人
	 * @return
	 */
	public String emailsnapshot()
	{
		log.info("Email the snapshot of "+snapshot);
		try 
		{
			SysemailClient.send(
				emailaddr,//主送
				"",//抄送
				"数据查询网页快照["+Tools.getFormatTime("yyyy-MM-dd HH:mm", System.currentTimeMillis())+"]",//邮件标题
				"snapshot=="+snapshot, 
				"");
			this.setResponseMessage("成功提交邮件发送，请在系统邮件发件箱查询。");
		} 
		catch (Exception e)
		{
			this.setResponseMessage("提交邮件失败，原因是"+e+", 请联系您的系统管理员。");
		}
		return "alert";
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}
	public void setHelperMgr(HelperMgr helperMgr)
	{
		this.helperMgr = helperMgr;
	}
	public String getRegexpexample() {
		return regexpexample;
	}
	public String getHtml() {
		return html;
	}

	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getPrecision() {
		return precision;
	}
	public void setPrecision(String precision) {
		this.precision = precision;
	}
}
