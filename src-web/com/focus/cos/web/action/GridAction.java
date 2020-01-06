package com.focus.cos.web.action;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.service.GridSetter;
import com.focus.cos.web.service.SvrMgr;
import com.focus.util.Base64X;
import com.focus.util.IOHelper;
import com.focus.util.QuickSort;
import com.focus.util.Tools;
import com.focus.util.XMLParser;
import com.focus.util.Zookeeper;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import com.opensymphony.xwork.ModelDriven;

/**
 * Grid-Digg的框架
 * @author focus
 *
 */
public class GridAction extends CosBaseAction implements ModelDriven
{
	private static final long serialVersionUID = 5364288292570217391L;
	private static final Log log = LogFactory.getLog(GridAction.class);

	protected String datatype;//数据类型
	protected String pq_filter;//查询请求的过滤条件 
	protected String pq_sort;//查询请求的过滤条件 
//	protected String exportUrl;//导出数据的URL
	protected String filetype;//导出文件的类型
	protected String db;
	protected String sql;
	protected String colModel;
	/*数据模型*/
	protected String dataModel;
	protected String location;//当前数据模型的属于本地还是
	protected String editorModel;
	protected String pageModel;
	protected String localData;//本地数据，在本地程序开发或者ZK的时候有效
	protected String localDataPlus;//本地扩展属性数据
	protected JSONArray localDataArray;
	protected JSONObject localDataObject = new JSONObject();
	protected JSONObject dataStyle = new JSONObject();//数据样式
	protected JSONObject diggObject = null;
	protected String load = "";
	protected String beforeTableView = "";//刷新列表前出来
	protected String beforeGridView = "";//刷新表格之前出来
	protected String handleRemoteData = "";//处理远程提取的数据的脚本
	protected String rowSelect = "";
	protected String cellSelect;
	protected String cellSave;
	protected String cellBeforeSave;
	protected String cellClick;
	protected String cellDblClick;
	protected String cellRightClick;
	protected String cellKeyDown;
	protected String cellEditKeyDown;
	protected boolean thisisapi;//这是在做API调用，不加载details
	protected boolean editable;
	protected boolean exportable = true;//是否数据可导出
	protected BasicDBObject filterModel = new BasicDBObject("on", false);
	protected BasicDBObject labelsModel = new BasicDBObject();
	protected int freezeCols;
	protected String toolbarSize;//工具栏按钮规格
	protected boolean hasUeditor;//是否有富文本编辑器
	protected ArrayList<BasicDBObject> forms = new ArrayList<BasicDBObject>();
	protected ArrayList<BasicDBObject> details = new ArrayList<BasicDBObject>();
	protected ArrayList<BasicDBObject> toolbars = new ArrayList<BasicDBObject>();
	protected ArrayList<BasicDBObject> editorPopups = new ArrayList<BasicDBObject>();
	protected ArrayList<BasicDBObject> innerbuttons = new ArrayList<BasicDBObject>();
	protected ArrayList<BasicDBObject> autoCompleteEditors = new ArrayList<BasicDBObject>();
	protected HashMap<String, Boolean> hiddens = new HashMap<String, Boolean>();
	protected HashMap<String, String> values = new HashMap<String, String>();//记录映射值的数据
	protected boolean hasToolbar = true;
	protected String selectionModel = "{}";
	protected boolean scrollModelAutoFit;
	protected String javascript;//自定义脚本路径
	protected String bottomInfo;
	protected int workmode;//工作模式
	protected boolean showBottom;
	protected boolean showTitle;
	protected boolean showRemark;
	protected boolean sortable = true;
	protected boolean numberCell;
	protected JSONObject summaryObject = new JSONObject();
	protected String emailaddr;
	protected boolean snapshotable = true;
	protected String snapshot;
	protected String formmode;//表单模式，编辑模式和查询模式
	protected String bgcolor;//页面北京
	protected int snapshotWidth;
	/*grid的Action操作*/
	protected String ajaxDelete;
	protected String ajaxAdd;
	protected String ajaxUpdate;
	protected boolean asyncDigg;//异步拉取数据
	/*上传文件*/
	protected String uploadurl;
	protected File uploadfile;
	/*返回给页面的对象数据*/
	protected String jsonData;
	/*颜色表*/
	protected String pq_cellcls;
	/*开放digg*/
	protected boolean opendigg;
	/*接口调用的事物日志*/
	protected  StringBuffer trans = new StringBuffer("====================================================================================");
	
	public void setPq_cellcls(String pq_cellcls) {
		this.pq_cellcls = pq_cellcls;
	}
	public String getPq_cellcls() {
		return pq_cellcls;
	}
	public void setJsonData(String jsonData) {
		this.jsonData = jsonData;
	}
	public String getJsonData(){
		return jsonData;
	}
	public JSONArray getLocalDataArray() {
		return localDataArray;
	}
	public JSONObject getLocalDataObject() 
	{
		return localDataObject;
	}
	
	public String getLocalDataPlus() {
		return localDataPlus;
	}
	public boolean isEmailable()
	{
		return sql!=null&&!sql.isEmpty();
	}
	
	public String redirect(String url)
	{
		try
		{
			super.getResponse().sendRedirect(url);
		}
		catch (Exception e)
		{
		}
		return null;
	}

	/**
	 * 返回错误
	 * @param rsp
	 * @param status
	 * @param msg
	 * @return
	 */
	public String response(HttpServletResponse rsp, int status, String msg)
	{
		rsp.setStatus(500);
		ServletOutputStream sos = null;
		try
		{
			sos = rsp.getOutputStream();
    		sos.write(msg.getBytes("UTF-8"));
		}
		catch(Exception e){
			log.error("Failed to response "+status, e);
		}
        finally
        {
        	if( sos != null )
	    		try
				{
	    			sos.flush();
	    			sos.close();
				}
				catch (IOException e)
				{
					log.error("", e);
				}
        }
		return null;
	}
	
	/**
	 * 返回json数据
	 * @param rsp
	 * @param json
	 * @return
	 */
	public String response(HttpServletResponse rsp, String json)
	{
		ServletOutputStream sos = null;
		try
		{
			rsp.setContentType("application/json;charset=utf8");
    		rsp.setHeader("Content-disposition", "inline; filename="+System.currentTimeMillis()+".json");
    		rsp.setStatus(200);
    		sos = rsp.getOutputStream();
    		sos.write(json.getBytes("UTF-8"));
		}
		catch (IOException e)
		{
			rsp.setStatus(500);
			log.error("Failed to resonse json", e);
		}
        finally
        {
        	if( sos != null )
	    		try
				{
	    			sos.flush();
	    			sos.close();
				}
				catch (IOException e)
				{
					log.error("", e);
				}
        }
		return null;
	}

	public void debugJSONObject(String json)
	{
		try
		{
			new JSONObject(json);
		}
		catch (Exception e)
		{
			StringBuffer sb = new StringBuffer();
			String msg = e.toString();
			int i = msg.indexOf("character ");
			if( i != -1 )
			{
				sb.append("Failed to build the json for exception("+e+"):");
				i += "character ".length();
				int j = msg.indexOf("of", i);
				if( j != -1 )
				{
					String str = msg.substring(i, j).trim();
					if( Tools.isNumeric(str) )
					{
						i = Integer.parseInt(str);
						sb.append("\r\n...\r\n");
						sb.append(json.substring(i));
						log.debug(sb.toString());
					}
				}
			}
			if( sb.length() == 0 ) log.debug("", e);
		}
	}
	
	public void debugJSONArray(String json)
	{
		try
		{
			new JSONArray(json);
		}
		catch (Exception e)
		{
			StringBuffer sb = new StringBuffer();
			String msg = e.toString();
			int i = msg.indexOf("character ");
			if( i != -1 )
			{
				sb.append("Failed to build the json for exception("+e+"):");
				i += "character ".length();
				int j = msg.indexOf("of", i);
				if( j != -1 )
				{
					String str = msg.substring(i, j).trim();
					if( Tools.isNumeric(str) )
					{
						i = Integer.parseInt(str);
						sb.append("\r\n...\r\n");
						sb.append(json.substring(i));
						log.debug(sb.toString());
					}
				}
			}
			if( sb.length() == 0 ) log.debug("", e);
		}
	}
	
	/**
	 * 根据标签模型创建过滤模型
	 * options 用于字段编辑时下拉选择
	 * field.options 用于列过滤下来选择
	 * labels 用于字段数据转换
	 */
	public void createFilterModel()
	{
		//构造labels数据
		BasicDBObject labelsFilter = new BasicDBObject();
		BasicDBObject optionsFilter = new BasicDBObject();
		JSONObject _labels = new JSONObject();
		Iterator<String> iterator = labelsModel.keySet().iterator();
		while(iterator.hasNext())
		{
			String name = iterator.next();
			BasicDBList labels = (BasicDBList)labelsModel.get(name);
			BasicDBList fieldOptions = new BasicDBList();
			BasicDBObject fieldLabels = new BasicDBObject();
			JSONObject _label = new JSONObject();
			for(int i = 0; i < labels.size(); i++)
			{
				Object o = labels.get(i);
				if (o instanceof BasicDBObject)
				{
					BasicDBObject e = (BasicDBObject)o;
					fieldLabels.put(e.getString("value"), e.getString("label"));
					fieldOptions.add(new BasicDBObject(e.getString("value"), e.getString("label")));
					_label.put(e.getString("value"), e.getString("label"));
				}
			}
			if( !labels.isEmpty() )	{
				labelsFilter.put(name, fieldLabels);
				_labels.put(name, _label);
				optionsFilter.put(name, fieldOptions);
			}
		}
		dataStyle.put("label", _labels);
		filterModel.put("labels", labelsFilter);
		filterModel.put("options", optionsFilter);
		String json = optionsFilter.toString();
		test(1, "根据数据值映射的配置(fieldModel.options)生成options，可用于表格下拉选择: %s", new JSONObject(json).toString(4));
        BasicDBList fields = (BasicDBList)filterModel.get("fields");
		test(1, "根据条件过滤字段的配置(fieldModel.fields)生成options项，用于条件查询的时候下来选择");
        if( fields != null )
        {
        	for( int i = 0; i < fields.size(); i++ )
        	{
        		BasicDBObject field = (BasicDBObject)fields.get(i);
        		String name = field.getString("name");//过滤字段
        		if( !labelsModel.containsField(name) )
        		{
        			continue;
        		}
        		BasicDBList labels = (BasicDBList)labelsModel.get(name);
        		BasicDBList options = new BasicDBList();
	        	if( "local".equalsIgnoreCase(this.location) )
	        	{
	        		test(2, "[%s]条件过滤字段的值映射因为是本地模式所以采用单列模式", name);
            		for(int j = 0; j < labels.size(); j++ )
            		{
            			Object e = labels.get(j);
            			if( e instanceof BasicDBObject )
            			{
            				BasicDBObject label = (BasicDBObject)labels.get(j);
            				options.add(label.getString("label"));
            			}
            			else options.add(e);
            		}
	        	}
	        	else
	        	{
	        		test(2, "[%s]条件过滤字段的值映射因为是远程模式所以采用键值对模式", name);
            		for(int j = 0; j < labels.size(); j++ )
            		{
            			Object e = labels.get(j);
            			if( e instanceof BasicDBObject )
            			{
            				BasicDBObject label = (BasicDBObject)labels.get(j);
            				options.add(new BasicDBObject(label.getString("value"), label.getString("label")));
            			}
            			else options.add(e);
            		}
	        	}
    			field.put("options", options);
        	}
    		json = fields.toString();
    		test(1, "过滤字段与值映射(fieldModel.fields): %s", new JSONArray(json).toString(4));
    	}
	}

	/**
	 * 模板测试
	 * @return
	 */
	protected TemplateChecker checker; 
	protected void test(int t, String text, Object... args)
	{
		checker.test(t, text, args);
	}

	/**
	 * 查看digg的详细日志
	 * @return
	 */
	public String debuglog()
	{
    	ServletOutputStream sos = null;
		byte[] payload = null;
		try 
		{
	        payload = (byte[])super.getSession().getAttribute(id+".log");
	        if( payload == null ){
	        	payload = ("调试日志已经不存在了.").getBytes("UTF-8");
	        }
			getResponse().setContentType("text/html");
			getResponse().setCharacterEncoding("UTF-8");
			sos = getResponse().getOutputStream();
			sos.print("<html><body style='padding: 1px;font-size:9pt;background:#000;color:#fff;word-break:keep-all;white-space:pre;'>");
			sos.write(("<p id='aaa' style='padding-top:5px;padding-bottom:5px;padding-left:5px;font-size:14px;color:#33ffff;background:'>模板【"+gridxml+"】执行调测...</p>").getBytes("UTF-8"));
			sos.flush();
		}
		catch (Exception e) 
		{
			try {
				payload = ("打开元数据模板调试日志出现异常"+e.toString()).getBytes("UTF-8");
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
	    			sos.write(("</body>\r\n\r\n<script type='text/javascript'>"+javascript+"</script></html>").getBytes("UTF-8"));
	    			sos.flush();
	        	} catch (IOException e1) {
				}
        	}
        }
		return null;
	}
	/**
	 * 检查参数
	 * @param value
	 * @return
	 * @throws Exception 
	 */
	public String checkParamter(String value) throws Exception{
		return checker.parameter(value, super.getRequest());
	}
	/**
	 * 执行参数过滤
	 * @param text
	 * @return
	 * @throws Exception
	public String parameter(String text) throws Exception{
		return checker.parameter(text, super.getRequest());
	}
	 */
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public InputStream getGridXmlInputStream()
		throws Exception
	{
		if( gridxml == null ){
			return null;
		}
		if( gridxml.startsWith("/grid/local") )
		{
			return  this.getClass().getResourceAsStream(gridxml);
		}
		else
		{
			byte[] payload = ZKMgr.getZookeeper().getData(gridxml);
			if( payload == null ){
				throw new Exception(String.format("模板[%s]可能还没有保存，不能获取模板配置", gridxml));
			}
			return new ByteArrayInputStream(payload);
		}
	}
	
	/**
	 * 根据XML配置输出grid界面
	 * @return
	 */
	protected String grid(String xmlpath)
	{
		checker = new TemplateChecker(workmode, this.values);
		test(-1, "检测元数据查询配置模板[%s]", xmlpath);
		this.gridxml = xmlpath;
		InputStream is;
		try 
		{
			is = getGridXmlInputStream();
			if( is != null ){
	        	this.parse(is);
				if( workmode == TemplateChecker.DEBUG && checker.hasDynamicParameter() )
				{
					this.setResponseMessage("该模板不能预览出数据，因为模板配置了动态参数，通过HTTP-Request或System.properties获取对应的系统参数:"+checker.printDynamicParameter());
				}
//				log.info(checker.toString());
				log.info("Parse the grid("+xmlpath+") and the result is "+this.checker.errcount()+", "+this.checker.warcount());
				return "grid"+this.filetype;
			}
			setResponseException("您要打开的元数据配置查询模板(" + this.gridxml + ")根本不存在，请联系您的系统管理员。");
		}
		catch (Exception e) 
		{
			this.checker.err();
			test(1, "解析元数据配置查询模板发生异常: %s", e.getMessage());
			this.checker.write(e);
			if( workmode == TemplateChecker.WORK ){
	    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"元数据模板解析",
					"执行元数据模板解析["+gridxml+"]出现异常",
					"[<i class='fa fa-digg fa-fw'></i>] 异常描述"+e.getMessage()+"，堆栈如下所示：\r\n"+this.checker.toString(),
					"helper!previewxml.action?path="+gridxml,
                    "情况确认", "#feedback?to="+super.getUserAccount()
                );
			}
			setResponseException("解析元数据配置查询模板(" + this.gridxml + ")发生异常: " + e + ", 已经将异常消息发送给了系统管理员，请联系系统管理员解决。"); 
		}
		return "alert";
	}
	
	/**
	 * 解析XML配置模板
	 * @param is
	 * @return
	 * @throws Exception
	 */
	protected JSONObject labelColumns;
	protected JSONArray titleColumns;
	protected JSONObject dataColumns;
	public String getTitleColumns() {
		return titleColumns!=null?titleColumns.toString():"[]";
	}
	public String getDataColumns() {
		return dataColumns!=null?dataColumns.toString():"{}";
	}
	private HashMap<String, String> pq_cls = new HashMap<String, String>();
	protected void parse(InputStream is) throws Exception
	{
		if( is == null )
		{
			test(1, "模板不存在, 请联系系统管理员检查问题并解决.");
			throw new Exception("配置的元数据模板["+gridxml+"]不存在, 请联系系统管理员检查问题并解决.");
		}
		filterModel.put("header", false);
		List<String> pq_cellcls = IOHelper.readLines(this.getClass().getResourceAsStream("/com/focus/cos/web/action/pq_cls.css"), null);
		StringBuilder sb = new StringBuilder();
		for(String css : pq_cellcls){
        	String args[] = Tools.split(css, ",");
        	String color = "#fff";
        	int k = Integer.parseInt(args[1].substring(1), 16);
        	if( k >= 0xdcdcdc ){
        		color = "#000";
        	}
        	pq_cls.put(args[0], args[1]);
        	sb.append(String.format("tr td.%s{background: %s;color: %s;} ", args[0], args[1], color));
        }
		this.pq_cellcls = sb.toString();
		if( checker == null ) checker = new TemplateChecker(workmode, this.values);
		test(1, "解析模板的XML脚本...");
		XMLParser parser = new XMLParser(is);
		super.viewTitle = viewTitle==null?XMLParser.getElementAttr(parser.getRootNode(), "title"):viewTitle;
		gridtitle = XMLParser.getElementAttr(parser.getRootNode(), "title");
		this.gridversion = XMLParser.getElementAttr(parser.getRootNode(), "version");
		
		this.filetype = XMLParser.getElementAttr(parser.getRootNode(), "type");
		test(1, "解析模板的XML脚本成功，模板标题是【%s】，模板类型是'%s'.", gridtitle, filetype);
		JSONArray sortColumns = new JSONArray();
		dataColumns = new JSONObject();
		titleColumns = new JSONArray();
		String recIndx = null;
		if( "form".equalsIgnoreCase(filetype) ){
			Node formNode = XMLParser.getChildElementByTag( parser.getRootNode(), "form" );
			if( formNode == null )
			{
				test(1, "[严重错误] 模板没有配置form节点.");
				throw new Exception("配置的元数据模板["+gridxml+"]没有配置form节点, 请联系系统管理员检查问题并解决.");
			}
			recIndx = XMLParser.getElementAttr(formNode, "recIndx");
			this.id = recIndx;
			this.buildForm(formNode, titleColumns, dataColumns);
		}
		else{
			Node gridNode = XMLParser.getChildElementByTag( parser.getRootNode(), "grid" );
			if( gridNode == null )
			{
				test(1, "[严重错误] 模板没有配置grid节点.");
				throw new Exception("配置的元数据模板["+gridxml+"]没有配置grid节点, 请联系系统管理员检查问题并解决.");
			}
			String snapshot = XMLParser.getElementAttr(gridNode, "snapshot");
			snapshotable = "true".equals(snapshot);
			recIndx = XMLParser.getElementAttr( gridNode, "recIndx" );
	        test(1, "表格唯一字段(解析recIndx属性)是%s,快照开关(解析snapshot属性)是%s", recIndx, snapshot);
			this.buildGrid(gridNode, titleColumns, dataColumns, sortColumns);
			if( showRemark ){
				super.viewTitle = XMLParser.getElementAttr(parser.getRootNode(), "remark");
			}
			test(1, "一共%s/%s个【标题与数据字段】(根据grid表格的配置title和dataIndx属性决定)，在导出数据时会只导出这些字段", titleColumns.length(), dataColumns.length());
	        test(1, "表格排序字段是: %s", sortColumns.toString(4));
		}
		Node javascriptNode = XMLParser.getChildElementByTag(parser.getRootNode(), "javascript");
		test(0, "开始构建内置JAVA脚本(解析javascript节点，实现诸如事件函数，因为模板中所有渲染脚本可能要用到全局函数，所以优先校验)...");
		javascript = null;
		if( javascriptNode != null ){
			javascript = XMLParser.getCData(javascriptNode);
			javascript = checker.javascript(javascript, super.getRequest());
			checker.js(javascript, 1);
		}
		else
		{
			test(1, "没有内置JAVA脚本.");
		}

		test(0, "开始构建工具栏(解析toolbar节点)...");
		if( javascriptNode != null ){
			this.javascript = XMLParser.getCData(javascriptNode);
		}
		Node toolbarNode = XMLParser.getChildElementByTag( parser.getRootNode(), "toolbar" );
		this.buildToolbars(toolbarNode);
		test(0, "开始构建选择模型(解析selection节点)...");
		if( javascriptNode != null ){
			this.javascript = XMLParser.getCData(javascriptNode);
		}
		
		test(0, "开始构建显示表格(解析grid节点)...");
        if( javascriptNode != null ){
        	this.javascript = XMLParser.getCData(javascriptNode);
        }
        
        Node selectionNode = XMLParser.getChildElementByTag(parser.getRootNode(), "selection");
		this.selectionModel = this.getSelectionModel(selectionNode);
		test(0, "开始构建数据模型(解析datamodel节点)...");
		if( javascriptNode != null ){
			this.javascript = XMLParser.getCData(javascriptNode);
		}
		Node datamodelNode = XMLParser.getChildElementByTag( parser.getRootNode(), "datamodel" );
		if( datamodelNode == null )
		{
			test(1, "[%s] 模板没有配置datamodel节点.", "严重错误");
			throw new Exception("配置的元数据模板["+gridxml+"]没有配置datamodel节点, 请联系系统管理员检查问题并解决.");
		}
		StringBuilder beforeTableView = new StringBuilder();
		DataModel dataModel = this.createDataModel(recIndx, datamodelNode, titleColumns, dataColumns, sortColumns, beforeTableView, null, 1);
		if( colModel != null && this.colModel.indexOf("<type='details'") != -1 && "zookeeper".equalsIgnoreCase(dataModel.getType()) ){
			checker.err();
			checker.test(1, "[%s] 配置了[扩展详情]的模板[数据模型]类型不支持[%s].", "严重错误", dataModel.getType());
		}
		if( "form".equalsIgnoreCase(filetype) ){
			javascript = XMLParser.getCData(datamodelNode);
			if( !javascript.isEmpty() ){
				javascript = checker.javascript(javascript, super.getRequest(), "datamodel");
				this.beforeGridView = javascript;	
			}
		}
		
		this.location = dataModel.getLocation();
		this.dataModel = dataModel.toString();
		JSONArray data = new JSONArray();
		data.put(dataStyle);
		dataStyle = new JSONObject();
		dataStyle.put("data", data);
		dataStyle.put("totalRecords", 0);
		dataStyle.put("curPage", 1);
		dataStyle.put("curPageRecords", 0);
		dataStyle.put("pageSize", 0);
		dataStyle.put("hasException", false);
		dataStyle.put("message", "");
		test(1, "数据列对象: %s", dataColumns.toString(4));
		if( !thisisapi ){
			if( beforeTableView.length() > 0 )
			{
				this.beforeTableView = beforeTableView.toString();
			}
			Node beforeGridViewNode = XMLParser.getChildElementByTag( parser.getRootNode(), "beforeGridView" );
			if( beforeGridViewNode != null ){
				String javascript = XMLParser.getCData(beforeGridViewNode);
				this.beforeGridView = javascript;
			}
			Element loadNode = XMLParser.getChildElementByTag(datamodelNode, "load");
			if( loadNode != null ){
				load = XMLParser.getCData(loadNode);
				test(1, "数据模型加载脚本(解析datamode节点lload子节点): "+load);
			}
			test(0, "开始构建表格扩展详情模板...");
			Node detailsNode = XMLParser.getChildElementByTag( parser.getRootNode(), "details" ); 
			if( detailsNode != null ){
				String type = XMLParser.getElementAttr( datamodelNode, "type" );
				if( type == null || "zookeeper".equals(type) || type.isEmpty() )
				{
					test(1, "[%s] 模板的主数据模型类型是[%s], 不允许配置配置[details]详情视图.", "严重错误", type);
					throw new Exception("配置的元数据模板["+gridxml+"]错误, 请联系系统管理员检查问题并解决.");
				}
				this.buildDetails(detailsNode);
			}
			
			javascript = null;
			if( javascriptNode != null ){
				javascript = XMLParser.getCData(javascriptNode);
				javascript = checker.javascript(javascript, super.getRequest());
			}
			test(0, "开始构建元数据查询的全局JAVA脚本(解析globalscript节点，实现数据表格中点击事件函数要定义在这里)[globalscript定义弃用兼容元globalscript]...");
			Node globalscriptNode = XMLParser.getChildElementByTag(parser.getRootNode(), "globalscript");
			if( globalscriptNode != null  ){
				String globalscript = XMLParser.getCData(globalscriptNode);
				globalscript = checker.javascript(globalscript, super.getRequest());
				checker.js(globalscript, 1);
				if( javascript == null) javascript = "";
				javascript += globalscript;//将全局脚本和单点脚本合并到一起
			}
			else
			{
				test(1, "没有全局JAVA脚本.");
			}
			
			if(exportable){
				String referer = super.getRequest().getHeader("referer");
				if( referer != null ){
					exportable = referer.indexOf("notify!messenger.action") == -1;
				}
			}
		}
		test(0, "开始构建元数据查询的过滤模型(将之前生成的labelsModel,filterModel对象进行重构，生成过滤字段和过滤模型)...");
		createFilterModel();
		test(0, "模板中配置动态参数情况: %s", checker.dynamicParameter.toString(4));
		test(0, "模板中产生的动态参数情况: %s", values.toString());
		
	}
	
	/**
	 * 
	 * @param detailsNode
	 * @throws Exception
	 */
	protected void buildDetails(Node detailsNode) throws Exception
	{
		Node detailNode = XMLParser.getChildElementByTag(detailsNode, "detail");
        for( ; detailNode != null; detailNode = XMLParser.nextSibling(detailNode) )
        {
            if( !detailNode.getNodeName().equalsIgnoreCase( "detail" ) ) continue;

            String subject = XMLParser.getElementAttr( detailNode, "subject" );
            String innertype = XMLParser.getElementAttr( detailNode, "type" );
            if( "textarea".equals(innertype) ) innertype = "0";
            else if( "grid".equals(innertype) ) innertype = "2";
            else if( "metadata".equals(innertype) ) innertype = "1";
            else if( "baidumap".equals(innertype) ) innertype = "3";
            else if( "link".equals(innertype) ) innertype = "4";
            else if( "html".equals(innertype) ) innertype = "5";
            else if( "comparison".equals(innertype) ) innertype = "6";
            int type = Integer.parseInt(innertype);
            BasicDBObject detail = new BasicDBObject();
    		detail.put("type", type);
    		detail.put("subject", subject);
            String data = XMLParser.getElementAttr( detailNode, "data" );
            String indx = XMLParser.getElementAttr( detailNode, "indx" );
            if( !data.isEmpty() ){
            	detail.put("data", data);
            	detail.put("tab_key", Tools.encodeMD5(data));
            }
            if( type == 1 )
            {
            	BasicDBList metadata = new BasicDBList();
        		Node metadataNode = XMLParser.getChildElementByTag(detailNode, "metadata");
                for( ; metadataNode != null; metadataNode = XMLParser.nextSibling(metadataNode))
                {
                    if( !metadataNode.getNodeName().equalsIgnoreCase( "metadata" ) ) continue;
                    String name = XMLParser.getElementAttr( metadataNode, "name" );
                    String value = XMLParser.getElementAttr( metadataNode, "value" );
                    boolean nullable = !XMLParser.getElementAttr( metadataNode, "nullable" ).equals("false");
            		metadata.add(new BasicDBObject("name", name).append("value", value).append("nullable", nullable));
                }
        		Node zookeeperNode = XMLParser.getChildElementByTag(detailNode, "zookeeper");
        		if( zookeeperNode != null )
        		{
        			String path = XMLParser.getElementAttr(zookeeperNode, "path");
        			path = checker.parameter(path, super.getRequest(), "detail.zookeeper", "path");
        			String mode = XMLParser.getElementAttr(zookeeperNode, "mode");
        			String key = XMLParser.getElementAttr(zookeeperNode, "key");
        			boolean decrypte = "true".equalsIgnoreCase(XMLParser.getElementAttr(zookeeperNode, "decrypte"));
        			if( "1".equals(mode) )
        			{
        			}
        			else
        			{
        				String value = XMLParser.getElementAttr(zookeeperNode, "value");
        				List<JSONObject> nodes = ZKMgr.getZookeeper().getJSONObjects(path, decrypte); 
        				for( JSONObject e: nodes )
        				{
        					String label = e.getString(value);
        					String val = e.getString(key);
                            boolean nullable = !XMLParser.getElementAttr( metadataNode, "nullable" ).equals("false");
                    		metadata.add(new BasicDBObject("name", label).append("value", val).append("nullable", nullable));
        				}
        			}
        		}
                detail.put("metadata", metadata);
            }
            else if( type == 6 )
            {
                String left = XMLParser.getElementAttr( detailNode, "left" );
                String right = XMLParser.getElementAttr( detailNode, "right" );
        		detail.put("left", left);
        		detail.put("right", right);
            }
            else if( type == 2 )
            {
            	detail.put("indx", !indx.isEmpty()?indx:data);//映射的字段，如果为空则标识主表字段
        		Node gridNode = XMLParser.getChildElementByTag(detailNode, "grid");
                if( gridNode == null ) continue;
                Node cellNode = XMLParser.getChildElementByTag( gridNode, "cell" );
        		if( cellNode == null ) continue;
        		JSONArray sortColumns = new JSONArray();
        		JSONObject dataColumns = new JSONObject();
        		JSONArray titleColumns = new JSONArray();
        		StringBuffer colM = new StringBuffer();
        		this.buildGridForCells(cellNode, colM, 0, titleColumns, dataColumns, sortColumns);
        		detail.put("colModel", colM.toString());
        		test(1, "生成的列模型配置如下：%s", colM.toString());

        		Node datamodelNode = XMLParser.getChildElementByTag( detailNode, "datamodel" );
    	        String recIndx = XMLParser.getElementAttr( gridNode, "recIndx" );
    	        StringBuilder subPageModel = new StringBuilder();
    	        DataModel dataModel = createDataModel(recIndx, datamodelNode, titleColumns, dataColumns, sortColumns, subPageModel, data, 2);
        		detail.put("dataModel", dataModel.toString());
//	            String str = subPageModel.toString();//"{ type: \"local\", rPP: 5, strRpp: \"\"}";//subPageModel.toString();
        		detail.put("remote", dataModel.getLocation().equals("remote")?1:0);
//        		System.err.println(detail.toString());
        		if( subPageModel.length() == 0 ){
        			String pagesize = XMLParser.getElementAttr( datamodelNode, "pagesize" );
        			if( !pagesize.isEmpty() && Tools.isNumeric(pagesize) ){
        				subPageModel.append(",pageModel: { type: \"local\", rPP: "+pagesize+", strRpp: \"\"}");
        			}
        		}
        		detail.put("subPageModel", subPageModel);
//	            System.err.println(str.toString());
//            { type: 'remote', rPP: 5, strRpp: '{0}', rPPOptions: [1, 10, 20, 30, 40, 50, 100, 500, 1000] }
            }
    		details.add(detail);
        }
	}

	public boolean isOpendigg() {
		return opendigg;
	}
	/**
	 * 构建编辑器的弹出对话框
	 * @param data
	 * @param popupNode
	 * @throws Exception
	 */
	protected void buildEditorPopup(String dataIndx, Node popupNode, int depth) throws Exception
	{
        Node gridNode = XMLParser.getChildElementByTag(popupNode, "grid");
        if( gridNode == null ) return;
		Node cellNode = XMLParser.getChildElementByTag( gridNode, "cell" );
		StringBuffer colM = new StringBuffer();
		JSONArray sortColumns = new JSONArray();
		JSONObject dataColumns = new JSONObject();
		JSONArray titleColumns = new JSONArray();

        String recIndx = XMLParser.getElementAttr( gridNode, "recIndx" );
        test(depth, "解析弹窗表格的配置，表格唯一字段(解析recIndx属性)是%s", recIndx);
		this.buildGridForCells(cellNode, colM, depth, titleColumns, dataColumns, sortColumns);
        test(depth, "一共%s/%s个【标题与数据字段】(根据grid表格的配置title和dataIndx属性决定)", titleColumns.length(), dataColumns.length());
        test(depth, "表格排序字段是: %s", sortColumns.toString(4));
        
        StringBuilder popupObj = new StringBuilder();
		popupObj.append("{");
        String title = XMLParser.getElementAttr( popupNode, "title" );
        if( title.isEmpty() ) title = "["+dataIndx+"]弹窗";
        String type = XMLParser.getElementAttr( popupNode, "type" );
        String editable = XMLParser.getElementAttr( popupNode, "editable" );
        String width = XMLParser.getElementAttr( popupNode, "width" );
        String showTop = XMLParser.getElementAttr( popupNode, "showTop" );
        this.editable = "true".equalsIgnoreCase(editable);
        popupObj.append("\r\n\ttitle: '"+title+"'");
        if( !type.isEmpty() ) popupObj.append("\r\n\t,type: '"+type+"'");
        if( !width.isEmpty() ) popupObj.append("\r\n\t,width: "+width);

        test(depth, "解析弹窗选择模型...");
        Node selectionNode = XMLParser.getChildElementByTag(popupNode, "selection");
        Node scrollNode = XMLParser.getChildElementByTag(popupNode, "scroll");
        popupObj.append("\r\n\t,selectionModel: "+this.getSelectionModel(selectionNode));
		popupObj.append("\r\n\t,scrollModel: "+this.getScrollModel(scrollNode));
        if( !editable.isEmpty() ) popupObj.append("\r\n\t,editable: "+editable);
        if( !showTop.isEmpty() ) popupObj.append("\r\n\t,showTop: "+showTop);
        String showBottom = XMLParser.getElementAttr( popupNode, "showBottom" );
        if( !showBottom.isEmpty() ) popupObj.append("\r\n\t,showBottom: "+showBottom);
		popupObj.append("\r\n\t,refresh: function () {}");
		popupObj.append("\r\n\t,colModel: popupColModel['"+dataIndx+"']");
		Node datamodelNode = XMLParser.getChildElementByTag( popupNode, "datamodel" );
		popupObj.append("\r\n\t,dataModel: popupDataModel['"+dataIndx+"']");
//		String load = "";
//		Element loadNode = XMLParser.getChildElementByTag(datamodelNode, "load");
//		if( loadNode != null ) load = XMLParser.getCData(loadNode);
//		popup.append("\r\n\tload: function( event, ui ){"+load+"}");
		BasicDBObject editor = new BasicDBObject("name", dataIndx);
		editor.put("colModel", colM.toString());
		
		Element opencallbackNode = XMLParser.getChildElementByTag(popupNode, "opencallback");
		if( opencallbackNode != null ) editor.put("opencallback", XMLParser.getCData(opencallbackNode));
		else editor.put("opencallback", "function(){}");
		
		Element closeNode = XMLParser.getChildElementByTag(popupNode, "closecallback");
		String callback = XMLParser.getCData(closeNode);
		if( closeNode != null ) editor.put("closecallback", XMLParser.getCData(closeNode));
		else editor.put("closecallback", "function(){}");
        test(depth+1, "弹窗关闭事件回调函数: %s", callback);
		checker.js(callback, depth);

        test(depth, "解析弹窗数据模型: ", dataIndx);
		editor.put("dataModel", this.createDataModel(recIndx, datamodelNode, titleColumns, dataColumns, sortColumns, popupObj, dataIndx, depth+1));
		popupObj.append("\r\n}");
        editor.put("popupObj", popupObj.toString());
        editorPopups.add(editor);
	}
	
	/**
	 * 
	 * @param inputNode
	 * @param titleColumns
	 * @param dataColumns
	 * @param forms
	 * @throws Exception
	 */
	private BasicDBObject buildFormInput(Node inputNode, JSONArray titleColumns, JSONObject dataColumns) throws Exception{
    	BasicDBObject input = new BasicDBObject();
        JSONObject column = new JSONObject();
        String title = XMLParser.getElementAttr(inputNode, "title");
    	input.put("hidden", XMLParser.getElementAttr(inputNode, "hidden").equals("true")||XMLParser.getElementAttr(inputNode, "hidden").equals("1"));
    	input.put("title", title);
    	column.put("title", title);
		String type = XMLParser.getElementAttr(inputNode, "type");
    	String dataIndx = XMLParser.getElementAttr(inputNode, "dataIndx");
		input.put("dataIndx", dataIndx);
    	column.put("dataIndx", dataIndx);
		input.put("dataIndxMd5", Tools.encodeMD5(dataIndx));
    	column.put("dataIndxMd5", Tools.encodeMD5(dataIndx));
    	String dataType = XMLParser.getElementAttr(inputNode, "dataType");
		input.put("dataType", dataType);
    	column.put("dataType", dataType);
		input.put("icon", XMLParser.getElementAttr(inputNode, "icon"));
    	column.put("icon", XMLParser.getElementAttr(inputNode, "icon"));
    	String label = XMLParser.getElementAttr(inputNode, "label");
		input.put("label", label);
    	column.put("label", label);
		input.put("nullable", "true".equals(XMLParser.getElementAttr(inputNode, "nullable")));
    	column.put("nullable", "true".equals(XMLParser.getElementAttr(inputNode, "nullable")));
		Element labelNode = XMLParser.getChildElementByTag( inputNode, "label" );
		if( labelNode != null ){
        	String labeltype = XMLParser.getElementAttr(labelNode, "type");
        	if( !labeltype.isEmpty() ) {
        		labelNode = XMLParser.getChildElementByTag( labelNode, labeltype );
        		if( "zookeeper".equalsIgnoreCase(labeltype) ) {
        			this.buildGridZookeeperLabel(labelNode, dataIndx);
        		}
        		if( "mongo".equalsIgnoreCase(labeltype) ) {
	        		this.buildGridMongoLabel(labelNode, dataIndx);
        		}
        		if( "redis".equalsIgnoreCase(labeltype) ) {
        		}
        		if( "sql".equalsIgnoreCase(labeltype) ) {
	        		this.buildGridSqlLabel(labelNode, dataIndx);
        		}
        	}
        	Node optionNode = XMLParser.getChildElementByTag( labelNode, "option" );
        	this.buildGridCellLabel(optionNode, dataIndx);
        	Object options = labelsModel.get(dataIndx);
        	input.put("options", options);
        	column.put("options", new JSONArray(options.toString()));
		}
		if( "text".equalsIgnoreCase(type) || "password".equalsIgnoreCase(dataType) || "textarea".equalsIgnoreCase(type) || "date".equalsIgnoreCase(type) ){
			type = "password".equalsIgnoreCase(dataType)?dataType:type;
    		javascript = "function(input){}";
			if( "date".equalsIgnoreCase(type) ){
	    		input.put("format", XMLParser.getElementAttr(inputNode, "format", "Y-m-d"));
	    		column.put("format", input.getString("format") );
	    		type = "text";
	    		input.put("plugin", "datetimepicker" );
	    		column.put("plugin", "datetimepicker" );
	    		Element onselectNode = XMLParser.getChildElementByTag( inputNode, "onselect" );
	    		if( onselectNode != null ){
	    			this.javascript = XMLParser.getCData(onselectNode);
	    			this.javascript = this.checker.javascript(this.javascript, super.getRequest());
	    		}
			}
    		input.put("onselect", javascript);
			boolean readonly = XMLParser.getElementAttr(inputNode, "editable").equals("false")?true:false;
			boolean onlyshow = XMLParser.getElementAttr(inputNode, "onlyshow").equals("true")?true:false;
			input.put("readonly", readonly||onlyshow?"readonly":"");
			column.put("readonly", readonly||onlyshow);
			if( onlyshow ){
				input.put("nullable", onlyshow);
		    	column.put("nullable", onlyshow);
			}
			input.put("placeholder", XMLParser.getElementAttr(inputNode, "placeholder"));
        	column.put("placeholder", XMLParser.getElementAttr(inputNode, "placeholder"));
			input.put("style", XMLParser.getElementAttr(inputNode, "style"));
        	column.put("style", XMLParser.getElementAttr(inputNode, "style"));
        	
        	String mask = XMLParser.getElementAttr(inputNode, "mask");
        	if( !mask.isEmpty() ){
        		input.put("mask", mask);
        		column.put("mask", mask);
        	}
        	String rows = XMLParser.getElementAttr(inputNode, "rows");
        	if( !rows.isEmpty() ){
    			input.put("rows", rows);
            	column.put("rows", rows);
        	}

    		Element onfocusNode = XMLParser.getChildElementByTag( inputNode, "onfocus" );
    		javascript = "";
    		if( onfocusNode != null ){
    			this.javascript = XMLParser.getCData(onfocusNode);
    			this.javascript = this.checker.javascript(this.javascript, super.getRequest());
    		}
    		if( javascript.isEmpty() ){
    			javascript = "function(input){}";
    		}
    		input.put("onfocus", javascript);
    		
    		Element onblurNode = XMLParser.getChildElementByTag( inputNode, "onblur" );
    		javascript = "";
    		if( onblurNode != null ){
    			this.javascript = XMLParser.getCData(onblurNode);
    			this.javascript = this.checker.javascript(this.javascript, super.getRequest());
    		}
    		if( javascript.isEmpty() ){
    			javascript = "function(input){}";
    		}
    		input.put("onblur", javascript);
		}
		else if( "select".equalsIgnoreCase(type) || "radio".equalsIgnoreCase(type) || "checkbox".equalsIgnoreCase(type) ){
			input.put("style", XMLParser.getElementAttr(inputNode, "style"));
        	column.put("style", XMLParser.getElementAttr(inputNode, "style"));
			input.put("filter", XMLParser.getElementAttr(inputNode, "filter"));
        	column.put("filter", XMLParser.getElementAttr(inputNode, "filter"));
			boolean readonly = XMLParser.getElementAttr(inputNode, "editable").equals("false")?true:false;
			boolean onlyshow = XMLParser.getElementAttr(inputNode, "onlyshow").equals("true")?true:false;
			input.put("readonly", readonly||onlyshow?"readonly":"");
			column.put("readonly", readonly||onlyshow);
    		Element onchangeNode = XMLParser.getChildElementByTag( inputNode, "onchange" );
    		javascript = "";
    		boolean init = false;
    		if( onchangeNode != null ){
    			init = XMLParser.getElementAttr(onchangeNode, "init").equals("true")?true:false;
    			this.javascript = XMLParser.getCData(onchangeNode);
    			this.javascript = this.checker.javascript(this.javascript, super.getRequest());
    		}
    		if( javascript.isEmpty() ){
    			javascript = "function(input){}";
    		}
    		input.put("onchange", javascript);
			input.put("init", init);
        	column.put("init", init);
        	
    		String width = XMLParser.getElementAttr(inputNode, "width");
			if( !width.isEmpty() ){
				input.put("width", width);
			}
			String height = XMLParser.getElementAttr(inputNode, "height");
			if( !height.isEmpty() ){
				input.put("height", height);
			}
		}
		else if( "ueditor".equalsIgnoreCase(type) ){
			input.put("dataType", "string");
	    	column.put("dataType", "string");
			String height = XMLParser.getElementAttr(inputNode, "height");
			if( !height.isEmpty() ){
				input.put("height", height);
			}
		}
		else if( "file".equalsIgnoreCase(type) ){
			input.put("dataType", "string");
	    	column.put("dataType", "string");
			input.put("maxFileSize", XMLParser.getElementAttr(inputNode, "maxFileSize", "0"));
        	column.put("maxFileSize", XMLParser.getElementAttr(inputNode, "maxFileSize", "0"));
        	int maxFileCount = Integer.parseInt(XMLParser.getElementAttr(inputNode, "maxFileCount", "1"));
			input.put("maxFileCount", maxFileCount);
        	column.put("maxFileCount", maxFileCount);
        	input.put("multiple", maxFileCount>1?"multiple":"");
        	//控制上传文件夹的参数
        	String webkitdirectory = XMLParser.getElementAttr(inputNode, "webkitdirectory");
    		column.put("webkitdirectory", webkitdirectory);
        	boolean _webkitdirectory = "true".equalsIgnoreCase(webkitdirectory) || "1".equalsIgnoreCase(webkitdirectory);
    		input.put("webkitdirectory", _webkitdirectory?"webkitdirectory":"");

        	//控制上传多个文件的参数
        	String multiFileUpload = XMLParser.getElementAttr(inputNode, "multiFileUpload");
    		column.put("multiFileUpload", multiFileUpload);
        	boolean _multiFileUpload = "true".equalsIgnoreCase(multiFileUpload) || "1".equalsIgnoreCase(multiFileUpload);
    		input.put("multiFileUpload", _multiFileUpload);
    		input.put("multiple", _multiFileUpload?"multiple":input.getString("multiple"));
    		
        	String allowedFileExtensions = XMLParser.getElementAttr(inputNode, "allowedFileExtensions");
        	JSONArray array = new JSONArray();
        	if( !allowedFileExtensions.isEmpty() ){
        		String[] args = Tools.split(allowedFileExtensions, ",");
        		for(String arg: args){
        			array.put(arg);
        		}
        	}
        	input.put("allowedFileExtensions", array.toString());
			
    		Element uploadUrlNode = XMLParser.getChildElementByTag( inputNode, "uploadUrl" );
    		javascript = "";
    		if( uploadUrlNode != null ){
    			this.javascript = XMLParser.getCData(uploadUrlNode);
    			this.javascript = this.checker.javascript(this.javascript, super.getRequest());
    		}
    		if( javascript.isEmpty() ){
            	String uploadUrl = XMLParser.getElementAttr(inputNode, "uploadUrl");
    			if( !uploadUrl.isEmpty() ){
    				javascript = "function(){return '"+uploadUrl+"';}";
    			}
    			else{
        			javascript = "function(){return false;}";
    			}
    		}
			input.put("uploadUrl", javascript);

    		Element deleteUrlNode = XMLParser.getChildElementByTag( inputNode, "deleteUrl" );
    		javascript = "";
    		if( deleteUrlNode != null ){
    			this.javascript = XMLParser.getCData(deleteUrlNode);
    			this.javascript = this.checker.javascript(this.javascript, super.getRequest());
    		}
    		if( javascript.isEmpty() ){
            	String deleteUrl = XMLParser.getElementAttr(inputNode, "deleteUrl");
    			if( !deleteUrl.isEmpty() ){
    				javascript = "function(){return '"+deleteUrl+"';}";
    			}
    			else{
        			javascript = "function(){return false;}";
    			}
    		}
    		input.put("deleteUrl", javascript);
        	
    		Element uploadExtraDataNode = XMLParser.getChildElementByTag( inputNode, "uploadExtraData" );
    		javascript = "";
    		if( uploadExtraDataNode != null ){
    			this.javascript = XMLParser.getCData(uploadExtraDataNode);
    			this.javascript = this.checker.javascript(this.javascript, super.getRequest());
    		}
    		if( javascript.isEmpty() ){
    			javascript = "function(){return {};}";
    		}
    		input.put("uploadExtraData", javascript);
    		Element deleteExtraDataNode = XMLParser.getChildElementByTag( inputNode, "deleteExtraData" );
    		javascript = "";
    		if( deleteExtraDataNode != null ){
    			this.javascript = XMLParser.getCData(deleteExtraDataNode);
    			this.javascript = this.checker.javascript(this.javascript, super.getRequest());
    		}
    		if( javascript.isEmpty() ){
    			javascript = "function(){return {};}";
    		}
    		input.put("deleteExtraData", javascript);
    		Element fileloadedNode = XMLParser.getChildElementByTag( inputNode, "fileloaded" );
    		javascript = "";
    		if( fileloadedNode != null ){
    			this.javascript = XMLParser.getCData(fileloadedNode);
    			this.javascript = this.checker.javascript(this.javascript, super.getRequest());
    		}
    		if( javascript.isEmpty() ){
    			javascript = "function(event, file, previewId, index, reader){}";
    		}
    		input.put("fileloaded", javascript);
    		
    		Element filebeforeloadNode = XMLParser.getChildElementByTag( inputNode, "filebeforeload" );
    		javascript = "";
    		if( filebeforeloadNode != null ){
    			this.javascript = XMLParser.getCData(filebeforeloadNode);
    			this.javascript = this.checker.javascript(this.javascript, super.getRequest());
    		}
    		if( javascript.isEmpty() ){
    			javascript = "function(event, file, index, reader){}";
    		}
    		input.put("filebeforeload", javascript);
    		
    		Element filebatchpreuploadNode = XMLParser.getChildElementByTag( inputNode, "filebatchpreupload" );
    		javascript = "";
    		if( filebatchpreuploadNode != null ){
    			this.javascript = XMLParser.getCData(filebatchpreuploadNode);
    			this.javascript = this.checker.javascript(this.javascript, super.getRequest());
    		}
    		if( javascript.isEmpty() ){
    			javascript = "function(event, data){}";
    		}
    		input.put("filebatchpreupload", javascript);
    		
    		Element filepreuploadNode = XMLParser.getChildElementByTag( inputNode, "filepreupload" );
    		javascript = "";
    		if( filepreuploadNode != null ){
    			this.javascript = XMLParser.getCData(filepreuploadNode);
    			this.javascript = this.checker.javascript(this.javascript, super.getRequest());
    		}
    		if( javascript.isEmpty() ){
    			javascript = "function(event, data, previewId, index, jqXHR){}";
    		}
    		input.put("filepreupload", javascript);
    		
    		Element fileuploadedNode = XMLParser.getChildElementByTag( inputNode, "fileuploaded" );
    		javascript = "";
    		if( fileuploadedNode != null ){
    			this.javascript = XMLParser.getCData(fileuploadedNode);
    			this.javascript = this.checker.javascript(this.javascript, super.getRequest());
    		}
    		if( javascript.isEmpty() ){
    			javascript = "function(event, previewId, index, fileId){}";
        		input.put("skipfileuploaded", "yes");
    		}
    		input.put("fileuploaded", javascript);

    		Element filebeforedeleteNode = XMLParser.getChildElementByTag( inputNode, "filebeforedelete" );
    		javascript = "";
    		if( filebeforedeleteNode != null ){
    			this.javascript = XMLParser.getCData(filebeforedeleteNode);
    			this.javascript = this.checker.javascript(this.javascript, super.getRequest());
    		}
    		if( javascript.isEmpty() ){
    			javascript = "function(event, key, data){return true;}";
    		}
    		input.put("filebeforedelete", javascript);

    		Element filedeletedNode = XMLParser.getChildElementByTag( inputNode, "filedeleted" );
    		javascript = "";
    		if( filedeletedNode != null ){
    			this.javascript = XMLParser.getCData(filedeletedNode);
    			this.javascript = this.checker.javascript(this.javascript, super.getRequest());
    		}
    		if( javascript.isEmpty() ){
    			javascript = "function(event, key, jqXHR, data){}";
    		}
    		input.put("filedeleted", javascript);
    		
    		Element initialPreviewNode = XMLParser.getChildElementByTag( inputNode, "initialPreview" );
    		javascript = "";
    		if( initialPreviewNode != null ){
    			this.javascript = XMLParser.getCData(initialPreviewNode);
    			this.javascript = this.checker.javascript(this.javascript, super.getRequest());
    		}
    		if( javascript.isEmpty() ){
    			javascript = "function(data, initialPreview){initialPreview.push(data);return data;}";
    		}
    		input.put("initialPreview", javascript);

    		Element filesortedNode = XMLParser.getChildElementByTag( inputNode, "filesorted" );
    		javascript = "";
    		if( filesortedNode != null ){
    			this.javascript = XMLParser.getCData(filesortedNode);
    			this.javascript = this.checker.javascript(this.javascript, super.getRequest());
    		}
    		input.put("reversePreviewOrder", javascript.isEmpty()?false:true);
    		if( javascript.isEmpty() ){
    			javascript = "function(event, param){}";
    		}
    		input.put("filesorted", javascript);
    		/*
			String width = XMLParser.getElementAttr(inputNode, "width", "360");
			String height = XMLParser.getElementAttr(inputNode, "height", "0");
    		input.put("width", width);
    		input.put("height", height);
			StringBuffer style = new StringBuffer();
			if( !width.isEmpty() && Tools.isNumeric(width) ){
				style.append("width:"+width+"px;");
			}
//			if( !height.isEmpty() && Tools.isNumeric(height) ){
//				style.append("height:"+height+"px;");
//			}
			input.put("fileStyle", style.toString());*/
		}
		String width = XMLParser.getElementAttr(inputNode, "width");
		if( !width.isEmpty() && Tools.isNumeric(width) ){
			int w = Integer.parseInt(width);
			if( !label.isEmpty() ){
				w += label.length()*12;
			}
			input.put("groupStyle", "width:"+w+"px;margin-bottom:5px;");
		}
    	input.put("type", type);
    	column.put("type", type);
		dataColumns.put(column.getString("dataIndx"), column);
		titleColumns.put(column);
		test(3, "表单: %s", input.toString());

        Node validationsNode = XMLParser.getChildElementByTag(inputNode, "validations");
        if( validationsNode != null )
        {
        	JSONArray validations = new JSONArray();
        	column.put("validations", validations);
        	Node validNode = validationsNode!=null?XMLParser.getChildElementByTag( validationsNode, "valid" ):null;
	        for( ; validNode != null; validNode = XMLParser.nextSibling(validNode) )
	        {
	            if( !validNode.getNodeName().equalsIgnoreCase( "valid" ) ) continue;
	            String validType = XMLParser.getElementAttr( validNode, "type" );
	            String validMsg = XMLParser.getElementAttr( validNode, "msg" );
	            String validValue = XMLParser.getElementAttr( validNode, "value" );
	            JSONObject valid = new JSONObject();
	            validations.put(valid);
	            if( "ip".equalsIgnoreCase(validType) )
	            {
		            if( validMsg.isEmpty() ) 
		            {
		            	validMsg = "请按照IP地址格式(x.x.x.x)输入";
		            }
	            }
	            else if( "domain".equalsIgnoreCase(validType) )
	            {
		            if( validMsg.isEmpty() ) 
		            {
		            	validMsg = "请按照域名格式(weixin.com)输入";
		            }
	            }	            
	            else if( "port".equalsIgnoreCase(validType) )
	            {
		            if( validMsg.isEmpty() ) 
		            {
		            	validMsg = "端口请输入0到65535的整数";
		            }
	            }
	            else if( "host".equalsIgnoreCase(validType) )
	            {
	            }
	            else if( "version".equalsIgnoreCase(validType) )
	            {
	            }
	            else if( "url".equalsIgnoreCase(validType) )
	            {
	            }
	            else if( "subject".equalsIgnoreCase(validType) )
	            {
	            }
	            else if( "email".equalsIgnoreCase(validType) )
	            {
	            }
	            else if( "number".equalsIgnoreCase(validType) )
	            {
		            if( validMsg.isEmpty() ) 
	            	{
		            	if( validValue.isEmpty() )
		            	{
		            		validMsg = title+"需要输入数字";
		            	}
		            	else
		            	{
		            		validMsg = title+"需要输入数字且数值不超过"+validValue;
		            	}
	            	}
	            }
	            else if( validType.isEmpty() )
	            {
	            	validValue = XMLParser.getCData(validNode);
	            	if( !validValue.isEmpty() ){
	            		validType = "function";
	            	}
	            }
	            else
	            {
		            if( "regexp".equalsIgnoreCase(validType) )
		            {
		            	validValue = XMLParser.getCData(validNode);
		            }
	            }
	            if( validMsg.isEmpty() ) 
	            {
	            	if( "minLen".equals(validType) )
	            	{
		            	validMsg = title+"至少输入"+validValue+"个字符";
	            	}
	            	else if( "maxLen".equals(validType) )
	            	{
		            	validMsg = title+"输入不超过"+validValue+"个字符";
	            	}
	            	else if( "ip".equals(validType) )
	            	{
		            	validMsg = "请按IP地址格式输入";
	            	}
	            }
	            valid.put("msg", validMsg);
	            valid.put("type", validType);
	            valid.put("value", validValue);
	        }
        }
		return input;
	}
	/**
	 * 构建form的数据
	 * @param cellNode
	 * @throws Exception
	 */
	protected void buildForm(Node formNode, JSONArray titleColumns, JSONObject dataColumns) throws Exception
	{
		this.bgcolor = XMLParser.getElementAttr(formNode, "bgcolor", "#f8f8f8");
		Element node = XMLParser.getFirstChildElement(formNode);//, "panel" );
    	BasicDBList _forms_ = new BasicDBList();
    	BasicDBObject _forms = new BasicDBObject();
    	_forms.put("forms", _forms_);
    	this.forms.add(_forms);
		while( node != null ){
			if( "panel".equalsIgnoreCase(node.getTagName()) ){
				Element panelNode = node;
	        	BasicDBObject panel = new BasicDBObject();
	        	BasicDBList forms = new BasicDBList();
	        	panel.put("forms", forms);
	    		String title = XMLParser.getElementAttr(panelNode, "title");
				title = checker.parameter(title, super.getRequest(), false);
	    		test(2, "%s", title);
	    		panel.put("title", title);
	    		String src = XMLParser.getElementAttr(panelNode, "src");
	    		if( src.isEmpty() ){
	    			Element inputNode = XMLParser.getChildElementByTag( panelNode, "input" );
	    			for( ; inputNode != null; inputNode = XMLParser.nextSibling(inputNode)){
	    				forms.add(this.buildFormInput(inputNode, titleColumns, dataColumns));
	    			}
	    		}
	    		else{
	    			src = checker.parameter(src, super.getRequest(), false);
	    			panel.put("src", src);
					String height = XMLParser.getElementAttr(panelNode, "height");
					if( !Tools.isNumeric(height) ){
						height = "256";
					}
					panel.put("height", height);
	    		}
	            this.forms.add(panel);
			}
			else if( "input".equalsIgnoreCase(node.getTagName()) ){
				BasicDBObject input = this.buildFormInput(node, titleColumns, dataColumns);
				if( "file".equalsIgnoreCase(input.getString("type")) ){
		        	BasicDBObject panel = new BasicDBObject();
		        	BasicDBList forms = new BasicDBList();
		        	panel.put("forms", forms);
		    		String title = XMLParser.getElementAttr(node, "title");
					title = checker.parameter(title, super.getRequest(), false);
		    		panel.put("title", title);
		    		panel.put("label", XMLParser.getElementAttr(node, "label"));
		        	forms.add(input);
		            this.forms.add(panel);
				}
				else if( "ueditor".equalsIgnoreCase(input.getString("type")) ){
		        	this.hasUeditor = true;
					BasicDBObject panel = new BasicDBObject();
		        	BasicDBList forms = new BasicDBList();
		        	panel.put("forms", forms);
		    		String title = XMLParser.getElementAttr(node, "title");
					title = checker.parameter(title, super.getRequest(), false);
		    		panel.put("title", title);
		    		panel.put("label", XMLParser.getElementAttr(node, "label"));
		    		input.put("dataType", "string");
		        	forms.add(input);
		            this.forms.add(panel);
				}
				else{
					_forms_.add(input);
				}
			}
			node = XMLParser.getNextSibling(node);
		}
		test(1, "生成的表单模型(背景 %s)配置如下：%s", bgcolor, this.forms.size());
	}
	/**
	 * 构建grid的数据
	 * @param cellNode
	 * @throws Exception
	 */
	protected void buildGrid(Node gridNode, JSONArray titleColumns, JSONObject dataColumns, JSONArray sortColumns) throws Exception
	{
		String freezeCols = XMLParser.getElementAttr(gridNode, "freezeCols");
		if( Tools.isNumeric(freezeCols)) this.freezeCols = Integer.parseInt(freezeCols);
		showTitle = "true".equals(XMLParser.getElementAttr( gridNode, "showTitle" ));
		if(!showTitle ){
			showRemark = "true".equals(XMLParser.getElementAttr( gridNode, "showRemark" ));
			showTitle = showRemark;
		}
		Node cellNode = XMLParser.getChildElementByTag( gridNode, "cell" );
		StringBuffer colM = new StringBuffer();
        test(1, "表格冻结列%s,是否显示表格标题%s(某些提示信息可以协助表格标题上)", freezeCols, showTitle);
		this.buildGridForCells(cellNode, colM, 0, titleColumns, dataColumns, sortColumns);
		colModel = colM.toString();
		test(1, "生成的列模型配置如下：%s", colModel.toString());
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
		String start0 = start;
    	String end0 = end;
		String startFormat = XMLParser.getElementAttr(startNode, "format");
    	if( startFormat.isEmpty() ){
    		startFormat = "yyyy-MM-dd";
    	}
		String endFormat = XMLParser.getElementAttr(endNode, "format" );
    	if( endFormat.isEmpty() ){
    		endFormat = "yyyy-MM-dd";
    	}
    	start = checker.parameter(start, super.getRequest(), false);
    	end = checker.parameter(end, super.getRequest(), false);
		Calendar c = Calendar.getInstance();
    	if( start.isEmpty() || end.isEmpty() ){
    		c.add(Calendar.DAY_OF_MONTH, -6);//半年内的数据
    		start = Tools.getFormatTime(startFormat, c.getTimeInMillis());
    		end = Tools.getFormatTime(endFormat, System.currentTimeMillis());
    		values.put(start0.replaceAll("%", "#"), String.valueOf(c.getTimeInMillis()));
    		values.put(end0.replaceAll("%", "#"), String.valueOf(System.currentTimeMillis()));
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
    	start = checker.parameter(start, super.getRequest(), false);
    	end = checker.parameter(end, super.getRequest(), false);
		Calendar c = Calendar.getInstance();
    	if( start.isEmpty() || end.isEmpty() ){
    		c.add(Calendar.MONTH, -5);//半年内的数据
    		String start0 = XMLParser.getElementAttr(startNode, "column");
        	String end0 = XMLParser.getElementAttr(endNode, "column" );
    		start = Tools.getFormatTime(startFormat, c.getTimeInMillis());
    		end = Tools.getFormatTime(endFormat, System.currentTimeMillis());
    		values.put(start0.replaceAll("%", "#"), String.valueOf(c.getTimeInMillis()));
    		values.put(end0.replaceAll("%", "#"), String.valueOf(System.currentTimeMillis()));
    	}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		Date startDate = sdf.parse(start);
		Date endDate = sdf.parse(end);
		c.setTime(endDate);
		for( int i = 0; c.getTimeInMillis() > startDate.getTime(); i++ ){
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
    	presetDatabase(src, cfg);
    	MongoClient mongo = null;
    	try
    	{
    		DataModel datamodel = new DataModel("", super.getRequest(), values, checker) {
				@Override
				public void finish() {
				}
				
				@Override
				public void build(Node datamodelNode, JSONObject dataColumns, JSONArray titleColumns, String gridxml, StringBuilder gridObj)
						throws Exception {
					
				}
			};
    		mongo = DataModel.getMongoClient(
				this.mongoHost,
				this.mongoPort,
				this.mongoUsername,
				this.mongoUserpswd,
				this.mongoDatabase
			);
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
    		this.logError("Failed to loadGridMongoFor: %s", cfg.toString(4));
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
	public static void main(String[] args){
		JSONArray values = new JSONArray();
		values.put("静谧也哉");
		values.put("skldjfsdf");
		try{
			System.err.println(JSON.parse(values.toString()).getClass().getName());
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * 递归构建表格的列字段
	 * @param cellNode
	 * @param colM
	 * @param depth
	 * @param dataColumns
	 * @param sortColumns
	 * @throws Exception
	 */
	private String _title;
	private String _dataIndx;
	private void buildGridForCells(
			Node cellNode,
			StringBuffer colM,
			int depth,
			JSONArray titleColumns,
			JSONObject dataColumns,
			JSONArray sortColumns) throws Exception
	{
		colM.append("[");
        for( int i = 0; cellNode != null; cellNode = XMLParser.getNextSibling(cellNode))
        {
        	if( cellNode.getNodeName().equalsIgnoreCase("gd:for") ){
        		//支持for循环产生grid.cell节点
        		String type = XMLParser.getElementAttr( cellNode, "type" );
                Node node = XMLParser.getChildElementByTag(cellNode, type);
                if( node == null ){
                	continue;
                }
            	List<JSONObject> list = null;
        		if( type.equalsIgnoreCase("mongo") ) {
                	list = this.loadGridMongoFor(node);
        		}
	    		else if( type.equalsIgnoreCase("date") ) {
                	list = this.loadGridDateFor(node);
	    		}
	    		else if( type.equalsIgnoreCase("month") ) {
                	list = this.loadGridMonthFor(node);
	    		}
        		
            	Element cellElement = XMLParser.getChildElementByTag(cellNode, "cell");
            	_title = XMLParser.getElementAttr( cellElement, "title" );
            	_dataIndx = XMLParser.getElementAttr( cellElement, "dataIndx" );
            	for(JSONObject e : list){
            		cellElement.setAttribute("title", DataModel.getVariable(_title, e));
//                		cellElement.setAttribute("dataIndx", DataModel.getVariable(_dataIndx, e));
        			if( i > 0 ) colM.append(",");
        			i += 1;
        			colM.append("\r\n\t");
        	        for( int j = 0; j < depth; j++ ) colM.append("\t");
                    String title = DataModel.getVariable(_title, e);
                    colM.append("{ title: '"+title+"'");
                    String align = XMLParser.getElementAttr( cellElement, "align" );
                    if( !align.isEmpty() ) colM.append(", align: '"+align+"'");
                    Element cellElementChild = XMLParser.getChildElementByTag(cellElement, "cell");
                    if( cellElementChild != null )
                    {
                        if( align.isEmpty() ) colM.append(", align: 'center'");
            			colM.append(", colModel: [");
                        for( int j = 0 ; cellElementChild != null; cellElementChild = XMLParser.nextSibling(cellElementChild))
                        {
                        	if( j++ > 0 ) colM.append(",");
                			colM.append("\r\n\t");
                	        for( int k = 0; k <= depth; k++ ) colM.append("\t");
                            colM.append("{ title: '"+XMLParser.getElementAttr( cellElementChild, "title" )+"'");
                            align = XMLParser.getElementAttr( cellElementChild, "align" );
                            if( !align.isEmpty() ) colM.append(", align: '"+align+"'");                                	
                        	_dataIndx = XMLParser.getElementAttr( cellElementChild, "dataIndx" );
                        	cellElementChild.setAttribute("dataIndx", DataModel.getVariable(_dataIndx, e));
                        	this.createGridCell(cellElementChild, colM, depth+2, titleColumns, dataColumns, sortColumns);
                        	cellElementChild.setAttribute("dataIndx", _dataIndx);
                            colM.append(" }");
                        }
                        colM.append("\r\n");
                        for( int j = 0; j <= depth; j++ ) colM.append("\t");
                		colM.append("]");
            	        test(depth+2, "添加分组列【%s】(将多个列归类在一起), 标题默认居中)", title, align);
                    }
                    else
                    {
                    	cellElement.setAttribute("dataIndx", DataModel.getVariable(_dataIndx, e));
                    	this.createGridCell(cellElement, colM, depth+1, titleColumns, dataColumns, sortColumns);
                    }
                	colM.append(" }");
            	}
            	_title = null;
            	_dataIndx = null;
        	}
            if( !cellNode.getNodeName().equalsIgnoreCase( "cell" ) ) continue;
			if( i > 0 ) colM.append(",");
			i += 1;
			colM.append("\r\n\t");
	        for( int j = 0; j < depth; j++ ) colM.append("\t");
            String title = XMLParser.getElementAttr( cellNode, "title" );
            colM.append("{ title: '"+title+"'");
            String align = XMLParser.getElementAttr( cellNode, "align" );
            if( !align.isEmpty() ) colM.append(", align: '"+align+"'");
            Node childcellNode = XMLParser.getChildElementByTag(cellNode, "cell");
            if( childcellNode != null )
            {
                if( align.isEmpty() ) colM.append(", align: 'center'");
    			colM.append(", colModel: ");
    	        test(depth+2, "添加分组列【%s】(将多个列归类在一起), 标题默认居中)", title, align);
            	this.buildGridForCells(childcellNode, colM, depth+1, titleColumns, dataColumns, sortColumns);
            }
            else
            {
            	this.createGridCell(cellNode, colM, depth+1, titleColumns, dataColumns, sortColumns);
            }
            colM.append(" }");
        }
        for (BasicDBObject filter : this.toolbars) {
        	if ((filter.containsField("filter")) && (!dataColumns.has(filter.getString("dataIndx")))) {
        		colM.append("\r\n\t");
        		for (int j = 0; j < depth; j++) colM.append("\t");
        		colM.append(",{ title: '" + (filter.containsField("placeholder") ? filter.getString("placeholder") : filter.containsField("label") ? filter.getString("label") : "") + "'");
        		colM.append(", dataIndx: '" + filter.getString("dataIndx") + "'");
        		colM.append(", dataType: '" + (filter.containsField("dataType") ? filter.getString("dataType") : "string") + "'");
        		colM.append(", hidden: 'true'");
        		colM.append(", exportable: 'false'}");
        	}
        }        
        colM.append("\r\n");
        for( int i = 0; i < depth; i++ ) colM.append("\t");
		colM.append("]");
	}
	
	/**
	 * 构建单项表格
	 * @param cellNode
	 * @param colM
	 * @throws Exception
	 */
	private void createGridCell(
			Node cellNode, 
			StringBuffer colM, 
			int depth, 
			JSONArray titleColumns,
			JSONObject dataColumns,
			JSONArray sortColumns) throws Exception
	{
        JSONObject column = new JSONObject();
        String dataType = XMLParser.getElementAttr( cellNode, "dataType" );
        String dataIndx = XMLParser.getElementAttr( cellNode, "dataIndx" );
        if( !dataType.isEmpty() ){
        	if( "int".equalsIgnoreCase(dataType) ||
    			"integer".equalsIgnoreCase(dataType) )
        	{
        		dataType = "integer";
            	dataStyle.put(dataIndx, 0);
        	}
        	else if( "long".equalsIgnoreCase(dataType) )
        	{
        		dataType = "long";
            	dataStyle.put(dataIndx, 0L);
        	}
        	else if( "number".equalsIgnoreCase(dataType) ||
        			 "float".equalsIgnoreCase(dataType) ||
        			 "double".equalsIgnoreCase(dataType) )
        	{
        		dataType = "float";
            	dataStyle.put(dataIndx, 0.0);
        	}
        	else if( "string".equalsIgnoreCase(dataType) )
        	{
            	dataStyle.put(dataIndx, dataType);
                String tag = XMLParser.getElementAttr( cellNode, "tag" );
        		if( !tag.isEmpty() )
            	{
                	colM.append(", tag: "+tag);
            	}
        	}
        	else if( "tag".equalsIgnoreCase(dataType) )
        	{
        		dataType = "string";
            	dataStyle.put(dataIndx, dataType);
            	colM.append(", tag: 99");
        	}
        	else if( dataType.startsWith("tag") )
        	{
        		colM.append(", tag: "+dataType.substring(3));
        		dataType = "string";
            	dataStyle.put(dataIndx, dataType);
        	}
        	else if( "ip".equalsIgnoreCase(dataType) )
        	{
        		dataType = "string";
            	dataStyle.put(dataIndx, dataType);
                column.put("translate", "ip");
        	}
        	else if( "location".equalsIgnoreCase(dataType) )
        	{
        		dataType = "string";
            	dataStyle.put(dataIndx, dataType);
                column.put("translate", "location");
        	}
        	else {
            	dataStyle.put(dataIndx, dataType);
        	}
//			<option value='string'>字符串</option>
//			<option value='int'>整数</option>
//			<option value='long'>长整数</option>
//			<option value='bool'>布尔</option>
//			<option value='number'>数字</option>
//			<option value='button'>按钮</option>
//			<option value='ip'>IP地址</option>
//			<option value='time'>时间</option>
//			<option value='null'>空</option>
        	colM.append(", dataType: '"+dataType+"'");
        }
        String editable = XMLParser.getElementAttr( cellNode, "editable" );
//    	if( dataIndx.startsWith("count_table") ){
//    		System.err.println(dataIndx+":"+editable);
//    	}
        String title = Tools.getJSONValue(XMLParser.getElementAttr(cellNode, "title"));
        String type = XMLParser.getElementAttr( cellNode, "type" );
        String sortable = XMLParser.getElementAttr( cellNode, "sortable" );
        String exportable = XMLParser.getElementAttr( cellNode, "exportable" );
        String cls = XMLParser.getElementAttr( cellNode, "cls" );
        String resizable = XMLParser.getElementAttr( cellNode, "resizable" );
        String hidden = XMLParser.getElementAttr( cellNode, "hidden" );
        if( "1".equals(hidden) || "true".equalsIgnoreCase(hidden) ){
        	hidden = "true";
        }
        else{
        	if( !hidden.isEmpty() ){
        		String[] args = Tools.split(hidden, "=");
        		String hide = super.getRequest().getParameter(args[0]) ;
        		hidden = "false";
        		if( args.length > 1 ){
        			if( hide != null && args[1].equals(hide) ){
        				hidden = "true";
        			}
        		}
        		else{
        			if( hide != null && ( "1".equals(hide) || "true".equalsIgnoreCase(hide) ) ){
        				hidden = "true";
        			}
        		}
        	}
        }
        String display = XMLParser.getElementAttr( cellNode, "display" );
        if( !display.isEmpty() ){
    		String[] args = Tools.split(display, "=");
    		String hide = super.getRequest().getParameter(args[0]) ;
    		hidden = "true";
    		if( args.length > 1 ){
    			if( hide != null && args[1].equals(hide) ){
    				hidden = "false";
    			}
    		}
    		else{
    			if( hide != null && ( "1".equals(hide) || "true".equalsIgnoreCase(hide) ) ){
    				hidden = "false";
    			}
    		}
        }
        String dir = XMLParser.getElementAttr( cellNode, "sort" );
        String width = XMLParser.getElementAttr( cellNode, "width" );
        String minWidth = XMLParser.getElementAttr( cellNode, "minWidth" );
        String maxWidth = XMLParser.getElementAttr( cellNode, "maxWidth" );
        String translate = XMLParser.getElementAttr( cellNode, "translate" );
        if( !dataIndx.isEmpty() ){
        	colM.append(", dataIndx: '"+dataIndx+"'");
            column.put("title", title);
            column.put("dataIndx", dataIndx);
            column.put("dataType", dataType);
        	titleColumns.put(column);
        	dataColumns.put(dataIndx, column);
        	if( cellNode.getParentNode() != null && "cell".equals(cellNode.getParentNode().getNodeName()) ){
        		column.put("parent_title", Tools.getJSONValue(XMLParser.getElementAttr(cellNode.getParentNode(), "title")));
        	}
            test(depth+2, "添加列【%s】 字段(dataIndx:%s), 类型(dataType:%s), 是否可编辑(editable:%s), 排序(dir:%s), 三种列宽(width:%s,minWidth:%s,maxWidth:%s), 样式(cls:%s)", title, dataType, dataIndx, editable, dir, width, minWidth, maxWidth, cls);
        }
        else
        {
            if( !dir.isEmpty() )
            {
                test(depth+3, "<span style='color:#ffcc99'>非显示列不需要设置排序字段(dir:"+dir+")</span>");
            }
            if(hidden.equals("true"))
            {
                test(depth+2, "添加隐藏列 , 类型(type:%s)", type, resizable);
            }
            else
            {
                test(depth+2, "添加非显示列 , 类型(type:%s), 三种列宽(width:%s,minWidth:%s,maxWidth:%s), 样式(cls:%s)", type, width, minWidth, maxWidth, cls);
            }
        }
        if( !translate.isEmpty() ){
            column.put("translate", translate);
            if( "location".equalsIgnoreCase(translate) ) {
                String location = XMLParser.getElementAttr( cellNode, "location" );
                String query = XMLParser.getElementAttr( cellNode, "query" );
                column.put("location", location);
                column.put("query", query);
            }
        }
        
        if( hidden.equals("true") && (!editable.isEmpty() ) )
        {
            test(depth+3, "<span style='color:#ffcc99'>隐藏列不需要设置编辑开关(editable)，但也许您只是让模板列暂时隐藏</span>");
        }
        if( hidden.equals("true") && (!width.isEmpty() || !minWidth.isEmpty() || !maxWidth.isEmpty() ) )
        {
            test(depth+3, "<span style='color:#ffcc99'>隐藏列不需要设置列宽字段(width)，但也许您只是让模板列暂时隐藏</span>");
        }
        if( hidden.equals("true") && (!resizable.isEmpty() ) )
        {
            test(depth+3, "<span style='color:#ffcc99'>隐藏列不需要设置调整大小字段(resizable)，但也许您只是让模板列暂时隐藏</span>");
        }
        if( hidden.equals("true") && (!cls.isEmpty() ) )
        {
            test(depth+3, "<span style='color:#ffcc99'>隐藏列不需要设置调整样式(cls)，但也许您只是让模板列暂时隐藏</span>");
        }
        if( !width.isEmpty() ){
        	if( Tools.isNumeric(width))
        	{
        		colM.append(", width: "+width);
        		column.put("width", width);
        	}
        	else
        	{
        		colM.append(", width: '"+width+"'");
        	}
        }
        if( !minWidth.isEmpty() ){
        	colM.append(", minWidth: "+minWidth);
        	column.put("minWidth", minWidth);
        }
        if( !exportable.isEmpty() && "false".equalsIgnoreCase(exportable) ){
        	column.put("exportable", false);
        }
        String align = XMLParser.getElementAttr( cellNode, "align" );
        if( !align.isEmpty()){
        	column.put("align", align);
        }
        if( !maxWidth.isEmpty() ){
        	colM.append(", maxWidth: "+maxWidth);
        	column.put("maxWidth", maxWidth);
        }
        boolean hide = hiddens.containsKey(dataIndx)?hiddens.get(dataIndx):false;
        if( hide ) hidden = "true";
        if( !hidden.isEmpty() ) colM.append(", hidden: "+hidden);
        if( !resizable.isEmpty() ) colM.append(", resizable: "+resizable);
        if( !type.isEmpty() ) colM.append(", type: '"+type+"'");
        if( !cls.isEmpty() ) colM.append(", cls: '"+cls+"'");
        if( !dir.isEmpty() ){
        	colM.append(", sortable: true");
        	JSONObject e = new JSONObject();
        	e.put("dataIndx", dataIndx);
        	e.put("dir", dir);
        	sortColumns.put(e);
        }
        else
        {
        	if( "true".equalsIgnoreCase(sortable) ){
            	colM.append(", sortable: true");
        	}
        	else{
            	colM.append(", sortable: false");
        	}
        }
        if( editable.isEmpty() ) editable = "false";

        Node renderNode = XMLParser.getChildElementByTag(cellNode, "render");
        //按钮
        Node buttonNode = XMLParser.getChildElementByTag(cellNode, "button");
        if( buttonNode != null )
        {
        	colM.append(", render: function (ui) { return \"");
	        for( int i = 0; buttonNode != null; buttonNode = XMLParser.nextSibling(buttonNode) )
	        {
	            String buttonClass = XMLParser.getElementAttr( buttonNode, "class" );
	            String buttonStyle = XMLParser.getElementAttr( buttonNode, "style" );
	            String buttonType = XMLParser.getElementAttr( buttonNode, "type" );
	            String subject = XMLParser.getElementAttr( buttonNode, "subject" );
	            if( i++ > 0 ) colM.append(" ");
	            if( buttonType.startsWith("edit") )
	            {
	            	buttonClass = "edit_btn";
	            	this.ajaxUpdate = XMLParser.getElementAttr( buttonNode, "ajax" );
	            	if( ajaxUpdate.isEmpty() ) ajaxUpdate = "dev!update.action";
//	            	log.info("["+this.gridxml+"] Set url of update to "+this.ajaxUpdate);
		            test(depth+3, "<span style='color:#0099cc'>根据类型(type:%s)在表格中添加修改按钮</span>", buttonType);
	            }
	            else if( buttonType.startsWith("delete") )
	            {
	            	buttonClass = "delete_btn";
	            	this.ajaxDelete = XMLParser.getElementAttr( buttonNode, "ajax" );
	            	if( ajaxDelete.isEmpty() ) ajaxDelete = "dev!delete.action";
//	            	log.info("["+this.gridxml+"] Set url of delete to "+this.ajaxDelete);
		            test(depth+3, "<span style='color:#0099cc'>根据类型(type:%s)在表格中添加删除按钮</span>", buttonType);
	            }
	            else
	            {
	            	String function = XMLParser.getCData(buttonNode);
	            	function = checker.javascript(function, super.getRequest(), "cell.button");
		            BasicDBObject innerbutton = new BasicDBObject();
		            String buttonIcon = XMLParser.getElementAttr( buttonNode, "icon" );
		            innerbutton.put("class", buttonClass);
		            innerbutton.put("icon", buttonIcon);
		            innerbutton.put("function", function);
		            this.innerbuttons.add(innerbutton);
		            function = "\t\t\t\t"+function;
		            function = function.replaceAll("\t", "    ");
		            test(depth+3, "在表格中添加按钮【%s】, 类型(type:%s), 图标(icon:%s), 样式(class:%s), 扩展样式(style:%s), 按钮事件: \r\n%s", subject, buttonType, buttonIcon, buttonClass, buttonStyle, function);
		            checker.js(function, depth+3);
	            }
	            colM.append("<button type='button' class='"+buttonClass+"' style='"+buttonStyle+"'>"+subject+"</button>");
	        }
        	colM.append("\";}");
        }
        else if( renderNode == null )
        {
            String numeric = XMLParser.getElementAttr( cellNode, "numeric" );
            if( !numeric.isEmpty() )
            {
            	editable = "true";
        		colM.append(", render: function (ui) { var val = getObjectData(ui.rowData, '"+dataIndx+"');return showNumeric('"+numeric+"', val);}");
            }
//            if( "length".equalsIgnoreCase(numeric) )
//            {
//            	           }
//            if( "size".equalsIgnoreCase(numeric) )
//            {
//            	editable = "true";
//        		colM.append(", render: function (ui) { var val = getObjectData(ui.rowData, '"+dataIndx+"');return showDatasize(val);}");
//            }
        }

    	boolean nullable = "true".equalsIgnoreCase(XMLParser.getElementAttr( cellNode, "nullable" ));
        Node summaryNode = XMLParser.getChildElementByTag(cellNode, "summary");
        if( summaryNode != null )
        {
        	String summaryType = XMLParser.getElementAttr(summaryNode, "type");
    		summaryObject.put(dataIndx, summaryType);
        }
        Node validationsNode = XMLParser.getChildElementByTag(cellNode, "validations");
        Node editorNode = XMLParser.getChildElementByTag(cellNode, "editor");
        if( validationsNode != null || editorNode != null )
        {
        	colM.append(", validations: [ ");
        	Node validNode = validationsNode!=null?XMLParser.getChildElementByTag( validationsNode, "valid" ):null;
        	int i = 0;
	        for( ; validNode != null; validNode = XMLParser.nextSibling(validNode), i++ )
	        {
	            if( !validNode.getNodeName().equalsIgnoreCase( "valid" ) ) continue;
	            if( i > 0 ) colM.append(", ");
	        	colM.append("{ ");
	            String validType = XMLParser.getElementAttr( validNode, "type" );
	            String validMsg = XMLParser.getElementAttr( validNode, "msg" );
	            String validValue = XMLParser.getElementAttr( validNode, "value" );
	            if( "ip".equalsIgnoreCase(validType) )
	            {
//	            	colM.append("type: 'regexp'");
		            if( validMsg.isEmpty() ) 
		            {
		            	validMsg = "请按照IP地址格式(x.x.x.x)输入";
		            }
//		            colM.append(", value: '^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$'");
//		            colM.append(", msg: '"+validMsg+"'");
	            	colM.append("type: function(ui){ return checkHost(ui, "+nullable+");}");
	            }
	            else if( "domain".equalsIgnoreCase(validType) )
	            {
		            if( validMsg.isEmpty() ) 
		            {
		            	validMsg = "请按照域名格式(weixin.com)输入";
		            }
	            	colM.append("type: function(ui){ return checkHost(ui, "+nullable+");}");
	            }	            
	            else if( "port".equalsIgnoreCase(validType) )
	            {
//	            	colM.append("type: 'regexp'");
//		            if( validMsg.isEmpty() ) 
//		            {
//		            	validMsg = "端口请输入0到65535的整数";
//		            }
//		            colM.append(", value: '^[1-9]$|(^[1-9][0-9]$)|(^[1-9][0-9][0-9]$)|(^[1-9][0-9][0-9][0-9]$)|(^[1-6][0-5][0-5][0-3][0-5]$)'");
//		            colM.append(", msg: '"+validMsg+"'");
	            	colM.append("type: function(ui){ return checkPort(ui, "+nullable+");}");
	            }
	            else if( "version".equalsIgnoreCase(validType) )
	            {
	            	colM.append("type: function(ui){ return checkVersion(ui, "+nullable+");}");
	            }
	            else if( "url".equalsIgnoreCase(validType) )
	            {
	            	colM.append("type: function(ui){ return checkUrl(ui, "+nullable+");}");
	            }
	            else if( "subject".equalsIgnoreCase(validType) )
	            {
	            	colM.append("type: function(ui){ return checkSubject(ui, "+nullable+");}");
	            }
	            else if( "email".equalsIgnoreCase(validType) )
	            {
	            	colM.append("type: function(ui){ return checkEmail(ui, "+nullable+");}");
	            }
	            else if( "host".equalsIgnoreCase(validType) )
	            {
	            	colM.append("type: function(ui){ return checkHost(ui, "+nullable+");}");
	            }
	            else if( "number".equalsIgnoreCase(validType) )
	            {
		            if( validMsg.isEmpty() ) 
	            	{
		            	if( validValue.isEmpty() )
		            	{
		            		validMsg = title+"需要输入数字";
			            	colM.append("type: function(ui){ return checkNumber(ui, "+nullable+");}");
		            	}
		            	else
		            	{
		            		validMsg = title+"需要输入数字且数值不超过"+validValue;
			            	colM.append("type: function(ui){ return checkNumber(ui, "+nullable+", "+validValue+");}");
		            	}
	            	}
		            else colM.append("type: function(ui){ return checkNumber(ui, "+nullable+");}");
	            }
	            else if( validType.startsWith("function") )
	            {
	            	colM.append("type: "+validType);
	            }
	            else if( validType.isEmpty() )
	            {
	            	validType = XMLParser.getCData(validNode);
	            	if( !validType.isEmpty() ){
	        			test(depth+3, "添加校验器(valid)回调脚本:");
	        			checker.js(validType, depth+3);
	            		colM.append("type: "+validType);
	            	}
	            }
	            else
	            {
	            	colM.append("type: '"+validType+"'");
		            if( "regexp".equalsIgnoreCase(validType) )
		            {
		            	validValue = XMLParser.getCData(validNode);
		            	if( !validValue.isEmpty() ) colM.append(", value: '"+validValue+"'");
		            }
		            else if( !validValue.isEmpty() ) colM.append(", value: "+validValue);
	            }
	            if( validMsg.isEmpty() ) 
	            {
	            	if( "minLen".equals(validType) )
	            	{
		            	validMsg = title+"至少输入"+validValue+"个字符";
	            	}
	            	else if( "maxLen".equals(validType) )
	            	{
		            	validMsg = title+"输入不超过"+validValue+"个字符";
	            	}
	            	else if( "ip".equals(validType) )
	            	{
		            	validMsg = "请按IP地址格式输入";
	            	}
	            }
	            //System.err.println("validMsg='"+validMsg+"'");
	            colM.append(", msg: '"+validMsg+"'");
	            String validIcon = XMLParser.getElementAttr( validNode, "icon" );
	            if( !validIcon.isEmpty() ) colM.append(", icon: '"+validIcon+"'");
	        	colM.append(" }");
	        }
	        if( editorNode != null && !nullable )
	        {
	            if( i > 0 ) colM.append(", ");
	        	colM.append("{ ");
            	colM.append("type: function(ui){ return ui.value!='';}");
	            colM.append(", msg: '该配置项不允许为空'");
	            colM.append(", icon: 'ui-icon-info'");
	        	colM.append(" }");
	        }
	        colM.append("]");
        }

        Node labelNode = XMLParser.getChildElementByTag(cellNode, "label");
        if( labelNode != null )
        {
        	String labeltype = XMLParser.getElementAttr(labelNode, "type");
        	if( !labeltype.isEmpty() )
        	{
        		labelNode = XMLParser.getChildElementByTag( labelNode, labeltype );
        		if( "zookeeper".equalsIgnoreCase(labeltype) )
        		{
        			this.buildGridZookeeperLabel(labelNode, dataIndx);
        		}
        		if( "mongo".equalsIgnoreCase(labeltype) )
        		{
	        		this.buildGridMongoLabel(labelNode, dataIndx);
        		}
        		if( "redis".equalsIgnoreCase(labeltype) )
        		{
        		}
        		if( "sql".equalsIgnoreCase(labeltype) )
        		{
	        		this.buildGridSqlLabel(labelNode, dataIndx);
        		}
        	}
        	
        	Node optionNode = XMLParser.getChildElementByTag( labelNode, "option" );
        	this.buildGridCellLabel(optionNode, dataIndx);
        }
        
        Node filterNode = XMLParser.getChildElementByTag(cellNode, "filter");
        if( filterNode != null )
        {
        	filterModel.put("header", true);
        	colM.append(", filter: { ");
//        	String filterOnchange = XMLParser.getElementAttr( filterNode, "onchange" );
        	String filterType = XMLParser.getElementAttr( filterNode, "type" );
        	colM.append("type: '"+filterType+"'");
        	String filterCondition = XMLParser.getElementAttr( filterNode, "condition" );
            if( !filterCondition.isEmpty() ) colM.append(", condition: '"+filterCondition+"'");
        	String init = XMLParser.getElementAttr( filterNode, "init" );
            if( !init.isEmpty() ) colM.append(", init: "+init);
            StringBuffer listeners = new StringBuffer();
            String args[] = Tools.split(XMLParser.getElementAttr( filterNode, "listeners" ), ",");
            for( int i = 0; i < args.length; i++ )
            {
            	if( i > 0 )listeners.append(",");
            	listeners.append("'"+args[i]+"'");
            }
        	String labelIndx = XMLParser.getElementAttr( filterNode, "labelIndx" );
            if( !labelIndx.isEmpty() ) colM.append(", labelIndx: '"+labelIndx+"'");
            colM.append(", valueIndx: '"+dataIndx+"'");
	        
        	if( listeners.length() > 0 ) colM.append(", listeners: ["+listeners+"]");
        	else colM.append(", listeners: [change]");
        	Node optionNode = XMLParser.getChildElementByTag( filterNode, "option" );
        	this.buildGridCellLabel(optionNode, dataIndx);
        	colM.append("}");

            BasicDBList fields = (BasicDBList)filterModel.get("fields");
            if( fields == null )
            {
            	fields = new BasicDBList();
            	filterModel.put("fields", fields);
            	filterModel.put("on", true);
            }
    		BasicDBObject obj = new BasicDBObject("name", dataIndx);
    		String attr = XMLParser.getElementAttr( filterNode, "attr" );
    		if( !attr.isEmpty() ) obj.append("attr", attr);
    		String format = XMLParser.getElementAttr( filterNode, "format" );
    		if( !format.isEmpty() ) obj.append("format", format);
    		obj.append("condition", filterCondition);
    		String style = XMLParser.getElementAttr( filterNode, "style" );
    		if( !attr.isEmpty() ) obj.append("attr", attr);
    		obj.append("style", style);
    		fields.add(obj);
        }

        Node pq_rowclsNode = XMLParser.getChildElementByTag(cellNode, "pq_rowcls");
        if( pq_rowclsNode != null )
        {
        	String color = "";
        	Node lightNode = XMLParser.getChildElementByTag( pq_rowclsNode, "light" );
        	BasicDBList lights = new BasicDBList();
	        for( ; lightNode != null; lightNode = XMLParser.nextSibling(lightNode) )
	        {
	            if( !lightNode.getNodeName().equalsIgnoreCase( "light" ) ) continue;
	            String value = XMLParser.getElementAttr( lightNode, "name" );
	            BasicDBObject light = new BasicDBObject();
	            if( !value.isEmpty() ){
	            	color = XMLParser.getElementAttr( lightNode, "value" );
	            	if( color.isEmpty() ){
	            		continue;
	            	}
		            light.put("operator", "equal");
		            light.put("value", DataModel.getDataValue(dataType, value, null));
		            light.put("color", color);
	            }
	            else
	            {
	            	String operator = XMLParser.getElementAttr( lightNode, "operator" );
	            	value = XMLParser.getElementAttr( lightNode, "value" );
	            	color = XMLParser.getElementAttr( lightNode, "color" );
		            if( operator.isEmpty() || value.isEmpty() || value.isEmpty() ){
	            		continue;
		            }
		            light.put("operator", operator);
		            light.put("value", DataModel.getDataValue(dataType, value, null));
		            light.put("color", color);
	            }
	            lights.add(light);
	        }
        	BasicDBObject pq_rowcls = null;
        	if( filterModel.containsField("pq_rowcls") )
        	{
        		pq_rowcls = (BasicDBObject)filterModel.get("pq_rowcls");
        	}
        	else
        	{
        		pq_rowcls = new BasicDBObject();
	    		filterModel.put("pq_rowcls", pq_rowcls);
        	}
        	pq_rowcls.put(dataIndx, lights);
        }
        
        Node pq_cellclsNode = XMLParser.getChildElementByTag(cellNode, "pq_cellcls");
        if( pq_cellclsNode != null )
        {
        	String color = XMLParser.getElementAttr( pq_cellclsNode, "value" );
        	color = color.isEmpty()?XMLParser.getElementAttr( pq_cellclsNode, "color" ):color;
            if( !color.isEmpty()){
            	if( pq_cls.containsKey(color) ){
            		column.put("pq_cellcls", pq_cls.get(color));
            	}
            }
        	Node lightNode = XMLParser.getChildElementByTag( pq_cellclsNode, "light" );
        	BasicDBList lights = new BasicDBList();
        	BasicDBObject cellcls = new BasicDBObject("lights", lights);
        	if( !color.isEmpty() ){
        		cellcls.put("color", color);//基础单元格颜色配置
        	}
	        for( ; lightNode != null; lightNode = XMLParser.nextSibling(lightNode) )
	        {
	            if( !lightNode.getNodeName().equalsIgnoreCase( "light" ) ) continue;
	            String value = XMLParser.getElementAttr( lightNode, "name" );
	            BasicDBObject light = new BasicDBObject();
	            if( !value.isEmpty() ){
	            	color = XMLParser.getElementAttr( lightNode, "value" );
	            	if( color.isEmpty() ){
	            		continue;
	            	}
		            light.put("operator", "equal");
		            light.put("value", DataModel.getDataValue(dataType, value, null));
		            light.put("color", color);
	            }
	            else
	            {
	            	String operator = XMLParser.getElementAttr( lightNode, "operator" );
	            	value = XMLParser.getElementAttr( lightNode, "value" );
	            	color = XMLParser.getElementAttr( lightNode, "color" );
		            if( operator.isEmpty() || value.isEmpty() || value.isEmpty() ){
	            		continue;
		            }
		            light.put("operator", operator);
		            light.put("value", DataModel.getDataValue(dataType, value, null));
		            light.put("color", color);
	            }
	            lights.add(light);
	        }
        	BasicDBObject pq_cellcls = null;
        	if( filterModel.containsField("pq_cellcls") )
        	{
        		pq_cellcls = (BasicDBObject)filterModel.get("pq_cellcls");
        	}
        	else
        	{
        		pq_cellcls = new BasicDBObject();
	    		filterModel.put("pq_cellcls", pq_cellcls);
        	}
        	pq_cellcls.put(dataIndx, cellcls);
        }
        if( editorNode != null )
        {
        	editable = "true";
        	colM.append(", editor: { ");
        	String editorType = XMLParser.getElementAttr( editorNode, "type" );
        	if( editorType.isEmpty() )
        	{
        		editorType = "textbox";
        		test(depth + 3, "【%s】设置编辑器(editor)但是类型[%s]为空，默认设置为[%s]", new Object[] { "警告提醒", "type", "textbox" });
        		this.checker.war();
        	}
        	test(depth + 3, "添加编辑器(editor)类型为%s(type)", new Object[] { editorType });
        	if ("autoCompleteEditor".equals(editorType)) {
        		Node autoCompleteEditorNode = XMLParser.getChildElementByTag(editorNode, "autoCompleteEditor");
        		if (autoCompleteEditorNode != null) {
        			this.javascript = XMLParser.getCData(autoCompleteEditorNode);
        			this.javascript = this.checker.javascript(this.javascript, super.getRequest());
        			test(depth + 3, "设置自定义编辑器(autoCompleteEditor)脚本:%s", this.javascript);
        			this.checker.js(this.javascript, depth + 3);
        			colM.append("type: " + createEditorScript(dataIndx, this.javascript, depth, title));
        		}
        		else
        		{
        			this.checker.err();
        			test(depth + 3, "【%s】设置自定义编辑器(autoCompleteEditor)但是没有发现脚本定义", new Object[] { "严重错误" });
        		}
        	}
        	else if( "dateEditor".equals(editorType) ){
    			colM.append("type: dateEditor");
        	}
        	else
        	{
        		Node popupNode = XMLParser.getChildElementByTag(editorNode, "popup");
        		if (popupNode != null) {
        			test(depth + 3, "编辑器点击事件为打开弹窗编辑器(%s)", dataIndx);
        			buildEditorPopup(dataIndx, popupNode, depth + 4);
        			colM.append("type: openPopupEditor");
        		}
        		else
        		{
                	Node initNode = XMLParser.getChildElementByTag(editorNode, "init");
                	if( initNode != null ){
                		this.javascript = XMLParser.getCData(editorNode);
                	}
                	else{
                		this.javascript = "";
                	}
                	if (!javascript.isEmpty()) {
            			this.javascript = this.checker.javascript(this.javascript, super.getRequest());
            			test(depth + 3, "设置编辑器(init)初始化脚本:%s", javascript);
            			this.checker.js(this.javascript, depth + 3);
            			colM.append("type: " + createEditorScript(dataIndx, this.javascript, depth, title));
            		}
            		else{
            			colM.append("type: '" + editorType + "'");
            		}
        		}
        	}
    		String subtype = XMLParser.getElementAttr( editorNode, "subtype" );
    		if( !subtype.isEmpty() ) colM.append(", subtype: '"+subtype+"'");
        		
    		String style = XMLParser.getElementAttr( editorNode, "style" );
    		if( !style.isEmpty() ) colM.append(", style: '"+style+"'");

        	Node optionNode = XMLParser.getChildElementByTag( editorNode, "option" );
        	if( optionNode != null )
        	{
        		this.buildGridCellLabel(optionNode, dataIndx);
        		if ("select".equalsIgnoreCase(editorType)) colM.append(", options: fieldOptions['" + dataIndx + "']");
        		filterModel.put("options", labelsModel);
        	}
        	Node zookeeperNode = XMLParser.getChildElementByTag( editorNode, "zookeeper" );
        	if( zookeeperNode != null )
        	{
        		this.buildGridZookeeperLabel(zookeeperNode, dataIndx);
        		if ("select".equalsIgnoreCase(editorType)) colM.append(", options: fieldOptions['"+dataIndx+"']");
        		filterModel.put("options", labelsModel);
        	}
        	Node mongoNode = XMLParser.getChildElementByTag( editorNode, "mongo" );
        	if( mongoNode != null )
        	{
        		this.buildGridMongoLabel(mongoNode, dataIndx);
        		if ("select".equalsIgnoreCase(editorType)) colM.append(", options: fieldOptions['"+dataIndx+"']");
        		filterModel.put("options", labelsModel);
        	}
        	Node sqlNode = XMLParser.getChildElementByTag( editorNode, "sql" );
        	if( sqlNode != null )
        	{
        		this.buildGridSqlLabel(sqlNode, dataIndx);
        		if ("select".equalsIgnoreCase(editorType)) colM.append(", options: fieldOptions['"+dataIndx+"']");
        		filterModel.put("options", labelsModel);
        	}
    		String attr = XMLParser.getElementAttr( editorNode, "attr" );
    		if( !attr.isEmpty() ) colM.append(", attr: '"+attr+"'");
        	colM.append("}");
        }
        
        if( renderNode != null )
        {
        	String function = XMLParser.getElementAttr( renderNode, "function" );
        	if( !function.isEmpty() )
        	{
        		colM.append(", render: function (ui) { return "+function+"(ui);}");
        	}
        	else
        	{
        		javascript = XMLParser.getCData(renderNode);
        		if( !javascript.isEmpty() ){
        			javascript = checker.javascript(javascript, super.getRequest());
        			if( _dataIndx != null ){
        				Node node = cellNode.getParentNode();
        				if( "cell".equalsIgnoreCase(node.getNodeName()) ){
        					javascript = Tools.replaceStr(javascript, _title, XMLParser.getElementAttr(node, "title"));
        				}
        				else{
        					javascript = Tools.replaceStr(javascript, _title, title);
        				}
        				javascript = Tools.replaceStr(javascript, _dataIndx, dataIndx);
        			}
        			else{
        				test(depth+3, "添加渲染器器(render)回调脚本:%s", javascript);
        				checker.js(javascript, depth+3);
        			}
        			int i = javascript.indexOf("function");
        			if( i != -1 ){
        				i = javascript.indexOf("{");
        				javascript = javascript.substring(i+1);
        				i = javascript.lastIndexOf("}");
        				javascript = javascript.substring(0, i);
        			}
        			StringBuffer sb = new StringBuffer();
        			sb.append("\r\ntry{\r\n");
        			sb.append("/*用户自定义脚本区域below*/");
        			sb.append(javascript);
        			sb.append("/*用户自定义脚本区域above*/\r\n}");
        			sb.append("\r\ncatch(e){");
        			sb.append("\r\nif( window.top && window.top.skit_alert) window.top.skit_alert('执行单元格【"+title+"/"+dataIndx+"】渲染器脚本异常'+e.message+', 行数'+e.lineNumber);");
        			sb.append("\r\nelse alert('执行单元格【"+title+"/"+dataIndx+"】渲染器脚本异常'+e.message+', 行数'+e.lineNumber);");
        			sb.append("\r\nreturn \"\";");
        			sb.append("\r\n}");
            		colM.append(", render: function (ui) {\r\n"+sb.toString()+"\r\n}");
        		}
        	}
        }

        Node editableNode = XMLParser.getChildElementByTag(cellNode, "editable");
        if( editableNode != null )
        {
        	String function = XMLParser.getElementAttr( editableNode, "function" );
        	if( !function.isEmpty() )
        	{
        		colM.append(", editable: function (ui) { return "+function+"(ui);}");
        	}
        	else
        	{
        		javascript = XMLParser.getCData(editableNode);
        		if( !javascript.isEmpty() ){
        			javascript = checker.javascript(javascript, super.getRequest());
        			test(depth+3, "添加编辑器是否可编辑判断(editable)回调脚本:%s", javascript);
        			checker.js(javascript, depth+3);
        			int i = javascript.indexOf("function");
        			if( i != -1 ){
        				i = javascript.indexOf("{");
        				javascript = javascript.substring(i+1);
        				i = javascript.lastIndexOf("}");
        				javascript = javascript.substring(0, i);
        			}
        			StringBuffer sb = new StringBuffer();
        			sb.append("\r\ntry{\r\n");
        			sb.append("/*用户自定义脚本区域below*/");
        			sb.append(javascript);
        			sb.append("/*用户自定义脚本区域above*/\r\n}");
        			sb.append("\r\ncatch(e){");
        			sb.append("\r\nif( window.top && window.top.skit_alert) window.top.skit_alert('执行单元格【"+title+"/"+dataIndx+"】编辑器是否可编辑判断脚本异常'+e.message+', 行数'+e.lineNumber);");
        			sb.append("\r\nelse alert('执行单元格【"+title+"/"+dataIndx+"】编辑器是否可编辑判断脚本异常'+e.message+', 行数'+e.lineNumber);");
        			sb.append("\r\nreturn false;");
        			sb.append("\r\n}");
        			colM.append(", editable: function (ui) {" + sb.toString() + "\r\n}");
        		}
        		else{
        			colM.append(", editable: " + "true".equalsIgnoreCase(editable));
        		}
        	}
        }
        else
        {
        	colM.append(", editable: "+"true".equalsIgnoreCase(editable));
        }
	}
	/**
	 * 创建编辑器脚本
	 * @param dataIndx
	 * @param javascript
	 * @param depth
	 * @param cellTitle
	 * @return
	 * @throws Exception
	 */
	private String createEditorScript(String dataIndx, String javascript, int depth, String cellTitle)
		throws Exception
	{
	     int i = javascript.indexOf("function");
	     if (i != -1) {
	       i = javascript.indexOf("{");
	       javascript = javascript.substring(i + 1);
	       i = javascript.lastIndexOf("}");
	       javascript = javascript.substring(0, i);
	     }
	     String editorFunction = "editor_" + dataIndx;
	     StringBuffer sb = new StringBuffer();
	     sb.append("\r\ntry{\r\n");
	     sb.append("/*用户自定义脚本区域below*/");
	     sb.append(javascript);
	     sb.append("/*用户自定义脚本区域above*/\r\n}");
	     sb.append("\r\ncatch(e){");
	     
	     sb.append("\r\nif( window.top && window.top.skit_alert) window.top.skit_alert('执行单元格【" + cellTitle + "/" + dataIndx + "】编辑器(editor)脚本异常'+e.message+', 行数'+e.lineNumber);");
	     sb.append("\r\nelse alert('执行单元格【" + cellTitle + "/" + dataIndx + "】编辑器(editor)脚本异常'+e.message+', 行数'+e.lineNumber);");
	     sb.append("\r\n}");
	     this.autoCompleteEditors.add(new BasicDBObject("script", sb.toString()).append("function", editorFunction));
	     return editorFunction;
	}
	/**
	 * 构建SQL的查询
	 * @param e
	 * @param dataIndx
	 * @throws Exception
	 */
	public void buildGridSqlLabel(Node e, String dataIndx) throws Exception{
    	BasicDBList labels = new BasicDBList();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		StringBuffer sql = new StringBuffer();
		try
		{
			String src = XMLParser.getElementAttr( e, "src" );
			src = checker.parameter(src, super.getRequest(), false);
			String from = XMLParser.getElementAttr( e, "from" );
	    	from = checker.parameter(from, super.getRequest(), false);
			JSONObject digg = new JSONObject();
	    	presetDatabase(src, digg);
			if( from.startsWith("%") ){
				return;
			}
    		DataModel datamodel = new DataModel("", super.getRequest(), values, checker) {
				@Override
				public void finish() {
				}
				
				@Override
				public void build(Node datamodelNode, JSONObject dataColumns, JSONArray titleColumns, String gridxml, StringBuilder gridObj)
						throws Exception {
					
				}
			};
            StringBuilder value = new StringBuilder();
    		Node defaultNode = XMLParser.getChildElementByTag(e, "default");
    		if( defaultNode != null )
    		{
    			String val = XMLParser.getElementAttr(defaultNode, "value");
    			val = checker.parameter(val, super.getRequest());
    			if( !val.startsWith("%") ){
    				value.append(val);
    				BasicDBObject label = new BasicDBObject("value", val);
    				label.put("label", XMLParser.getElementAttr(defaultNode, "label"));
    				labels.add(label);
    			}
    		}
			Node keyNode = XMLParser.getChildElementByTag(e, "key");
			String keyColumn = XMLParser.getElementAttr(keyNode, "column");
			Node valueNode = XMLParser.getChildElementByTag(e, "value");
			String valueColumn = XMLParser.getElementAttr(valueNode, "column");
    		Node dependNode = XMLParser.getChildElementByTag(e, "depend");
    		String dependColumn = XMLParser.getElementAttr(dependNode, "column");
			sql.append("select ");
			sql.append(keyColumn);
			sql.append(",");
			sql.append(valueColumn);
			if( !dependColumn.isEmpty() ){
				sql.append(",");
				sql.append(dependColumn);
			}
			sql.append(" from ");
			sql.append(from);
			Node conditionNode = XMLParser.getChildElementByTag(e, "condition");
			datamodel.setQuerySqlWhere(conditionNode, null, sql, 0);
			Class.forName(driverClass); 
            connection = DriverManager.getConnection(jdbcUrl ,jdbcUsername, jdbcUserpswd);  
            statement = connection.createStatement();
            rs = statement.executeQuery(sql.toString());
            while( rs.next() )
            {
            	if( value.length() > 0 ){
            		value.append(",");
            	}
    			Object key = rs.getString(1);
    			if( key == null ) continue;
    			value.append(key);
    			Object val = rs.getString(2);
    			if( val == null ) continue;
    			BasicDBObject label = new BasicDBObject("value", key.toString());
    			label.put("label", val.toString());
    			if( !dependColumn.isEmpty() ){
    				Object depend = rs.getObject(3);
    				if( depend != null ){
    					label.put("depend", depend);
    				}
    			}
    			labels.add(label);
	        }
    		labelsModel.put(dataIndx, labels);
    		values.put("#"+dataIndx+"#", value.toString());
		}
		catch(Exception e1)
		{
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
	/**
	 * 根据芒果数据库构建label
	 * @param e
	 * @param dataIndx
	 * @throws Exception
	 */
	private void buildGridMongoLabel(Node e, String dataIndx) throws Exception
	{
    	BasicDBList labels = new BasicDBList();
		String src = XMLParser.getElementAttr(e, "src");
    	String from = XMLParser.getElementAttr( e, "from" );
    	String database = XMLParser.getElementAttr( e, "database" );
    	JSONObject cfg = new JSONObject();
    	cfg.put("type", "mongo");
    	cfg.put("mongo.database1", database);//第一数据库
    	cfg.put("mongo.tablename", from);
    	presetDatabase(src, cfg);
    	MongoClient mongo = null;
    	try
    	{
    		DataModel datamodel = new DataModel("", super.getRequest(), values, checker) {
				@Override
				public void finish() {
				}
				
				@Override
				public void build(Node datamodelNode, JSONObject dataColumns, JSONArray titleColumns, String gridxml, StringBuilder gridObj)
						throws Exception {
					
				}
			};
    		mongo = DataModel.getMongoClient(
				mongoHost, 
				mongoPort,
				mongoUsername,
				mongoUserpswd,
				mongoDatabase);
    		MongoDatabase db = mongo.getDatabase(database);
    		MongoCollection<Document> col = db.getCollection(from);

    		Node keyNode = XMLParser.getChildElementByTag(e, "key");
    		String keyColumn = XMLParser.getElementAttr(keyNode, "column");
    		Node valueNode = XMLParser.getChildElementByTag(e, "value");
    		String valueColumn = XMLParser.getElementAttr(valueNode, "column");
    		String quote = XMLParser.getElementAttr(valueNode, "quote");
    		String bracket = XMLParser.getElementAttr(valueNode, "bracket");
    		Node dependNode = XMLParser.getChildElementByTag(e, "depend");
    		String dependColumn = XMLParser.getElementAttr(dependNode, "column");
    		Node conditionNode = XMLParser.getChildElementByTag(e, "condition");
    		BasicDBObject condition = new BasicDBObject();
    		datamodel.setQueryMongoWhere(conditionNode, null, null, condition, 0);
    		col.find(condition);

            test(4, "构建值映射表[Mongo] from%s", from);
			FindIterable<Document> find = col.find(condition);
			test(5, "键%s值%s条件%s", keyColumn, valueColumn, condition.toString());
    		Node sortNode = XMLParser.getChildElementByTag(e, "sort");
    		if( sortNode != null ){
    			String sortColumn = XMLParser.getElementAttr(sortNode, "column");
    			int dir = 1;
				if( "down".equalsIgnoreCase(XMLParser.getElementAttr(sortNode, "dir")) || "u".equalsIgnoreCase(XMLParser.getElementAttr(sortNode, "dir")) ) {
					dir = -1;
				}
    			find = find.sort(new Document(sortColumn, dir));
                test(5, "排序%s %s", sortColumn, dir);
    		}
    		MongoCursor<Document> cursor = find.iterator();
    		Node defaultNode = XMLParser.getChildElementByTag(e, "default");
            StringBuilder value = new StringBuilder();
    		if( defaultNode != null ){
    			String val = XMLParser.getElementAttr(defaultNode, "value");
    			val = checker.parameter(val, super.getRequest(), false);
    			if( !val.startsWith("%") ){
	    			value.append(val);
	    			BasicDBObject label = new BasicDBObject("value", val);
	    			label.put("label", XMLParser.getElementAttr(defaultNode, "label"));
	    			labels.add(label);
    			}
    		}
    		while( cursor.hasNext() ){
    			Document doc = cursor.next();
    			Object key = doc.containsKey(keyColumn)?doc.get(keyColumn):"";
    			Object val = doc.containsKey(valueColumn)?doc.get(valueColumn):"";
            	if( value.length() > 0 ){
            		value.append(",");
            	}
            	value.append(key.toString());
    			BasicDBObject label = new BasicDBObject("value", key.toString());
    			label.put("label", val.toString());
    			if( !bracket.isEmpty() ){
    				if( doc.containsKey(bracket) ){
    					key = doc.get(bracket);
    					label.put("label", val+"("+key+")");
    					if( !quote.isEmpty() && labelsModel.containsField(quote) ){
    						BasicDBList quotes = (BasicDBList)labelsModel.get(quote);
    						for(int j = 0; j < quotes.size(); j++){
    							BasicDBObject refer = (BasicDBObject)quotes.get(j);
    							if( key.toString().equals(refer.get("value").toString()) ){
    		    					label.put("label", val+"("+refer.getString("label")+")");
    								break;
    							}
    						}
    					}
    				}
    			}
    			if( !dependColumn.isEmpty() && doc.containsKey(dependColumn) ){
    				label.put("depend", doc.get(dependColumn));
    			}
    			labels.add(label);
    		}
    		labelsModel.put(dataIndx, labels);
    		values.put("#"+dataIndx+"#", value.toString());
            test(4, "动态变量: %s", value);
    	}
    	catch(Exception e1){
    		throw e1;
    	}
    	finally{
    		if(mongo != null)
    		{
    			mongo.close();
    		}
    	}
	}
	/**
	 * 构建Grid的ZK标签
	 * @param e
	 * @param dataIndx
	 * @throws Exception
	 */
	private void buildGridZookeeperLabel(Node e, String dataIndx) throws Exception
	{
    	BasicDBList labels = new BasicDBList();
		String path = XMLParser.getElementAttr(e, "path");
		path = checker.parameter(path, super.getRequest(), "cell.zookeeper", "path");
		String mode = XMLParser.getElementAttr(e, "mode");
		String key = XMLParser.getElementAttr(e, "key");
		Node keyNode = XMLParser.getChildElementByTag(e, "key");
		if(keyNode != null){
			key = XMLParser.getElementAttr(keyNode, "column");
		}
		
		String src = XMLParser.getElementAttr(e, "src");
		boolean decrypte = "true".equalsIgnoreCase(XMLParser.getElementAttr(e, "decrypte"));
		Node conditionNode = XMLParser.getChildElementByTag(e, "condition");
		JSONArray conditions = new JSONArray(); 
		for( ; conditionNode != null; conditionNode = XMLParser.nextSibling(conditionNode) )
        {
			JSONObject condition = new JSONObject();
			condition.put("column", XMLParser.getElementAttr(conditionNode, "column"));
			condition.put("method", XMLParser.getElementAttr(conditionNode, "method"));
			condition.put("value", checker.parameter(XMLParser.getElementAttr(conditionNode, "value"), super.getRequest(), false));
			conditions.put(condition);
        }

        StringBuilder valueSb = new StringBuilder();
		if( "2".equals(mode) )
		{
			JSONObject config = null;
			if( src.isEmpty() ){
				config = ZKMgr.getZookeeper().getJSONObject(path, decrypte);
			}
			else{
				Zookeeper zookeeper = Zookeeper.getInstance(src);
				config = zookeeper.getJSONObject(path, decrypte);
				zookeeper.close();
			}
			if( config == null )
			{
    			test(4, "没有从ZooKeeper找到路径[%s]对应的配置", path);
			}
			else if( config.has(key) )
			{
				try
				{
					JSONObject val = config.getJSONObject(key);
					Iterator<?> iterator = val.keys();
					while( iterator.hasNext() )
					{
						key = iterator.next().toString();
						BasicDBObject label = new BasicDBObject("value", key);
						label.put("label", val.get(key));
						labels.add(label);
			            if( valueSb.length() > 0 ){
			        		valueSb.append(",");
			        	}
			        	valueSb.append(label.getString("value"));
					}
				}
				catch(Exception e1)
				{
					throw new Exception("从ZooKeeper路径["+path+"]的配置中找到的["+key+"]不是数组对象");
				}
			}
//			throw new Exception("没有从ZooKeeper路径["+path+"]的配置中找到["+key+"]");
		}
		else
		{
			String value = XMLParser.getElementAttr(e, "value");
			Node valueNode = XMLParser.getChildElementByTag(e, "value");
			if(valueNode != null){
				value = XMLParser.getElementAttr(valueNode, "column");
			}
			boolean returnpath = "true".equalsIgnoreCase(XMLParser.getElementAttr(e, "returnpath"));
	        List<JSONObject> list = null;
			if( src.isEmpty() ){
				list = ZKMgr.getZookeeper().getJSONObjects(path, decrypte);
			}
			else{
				Zookeeper zookeeper = Zookeeper.getInstance(src);
				list = zookeeper.getJSONObjects(path, decrypte);
				zookeeper.close();
			}
	        path += "/";
			Node sortNode = XMLParser.getChildElementByTag(e, "sort");
			if(sortNode != null){
				String sort = XMLParser.getElementAttr(sortNode, "column");
				int dir = 1;
//				"down".equalsIgnoreCase(XMLParser.getElementAttr(sortNode, "dir")) ||
				if( "u".equalsIgnoreCase(XMLParser.getElementAttr(sortNode, "dir")) ||
					"up".equalsIgnoreCase(XMLParser.getElementAttr(sortNode, "dir")) )
				{
					dir = -1;
				}
				QuickSort sorter = new QuickSort(sort, dir) {
					@Override
					public boolean compareTo(Object sortSrc, Object pivot) {
						JSONObject l = (JSONObject)sortSrc;
						JSONObject r = (JSONObject)pivot;
						Object l0 = null, r0 = null;
						if( l.has(this.getOrderby()) ){
							l0 = l.get(this.getOrderby());
						}
						if( r.has(this.getOrderby()) ){
							r0 = r.get(this.getOrderby());
						}
						if( l0 instanceof Integer && r0 instanceof Integer ){
							int l1 = (Integer)l0;
							int r1 = (Integer)r0;
							if( this.getSc() < 0 ){
								return r1 > l1;
							}
							else{
								return r1 < l1;
							}
						}
						else if( l0 instanceof Long && r0 instanceof Long ){
							long l1 = (Long)l0;
							long r1 = (Long)r0;
							if( this.getSc() < 0 ){
								return r1 > l1;
							}
							else{
								return r1 < l1;
							}
						}
						else {
							String l1 = l0.toString();
							String r1 = r0.toString();
							if( this.getSc() < 0 ){
								return r1.compareTo(l1) > 0;
							}
							else{
								return r1.compareTo(l1) < 0;
							}
						}
					}
				};
				sorter.sort(list);
			}

    		Node dependNode = XMLParser.getChildElementByTag(e, "depend");
    		String dependColumn = XMLParser.getElementAttr(dependNode, "column");
			for(JSONObject p : list)
			{
				boolean filter = false;
				for(int i = 0; i < conditions.length(); i++ )
				{
					JSONObject c = conditions.getJSONObject(i);
					String val = c.getString("value");
					String method = c.getString("method");
					String col = c.getString("column");
					if( p.has(col) )
					{
						String val1 = p.get(col).toString();
						if( method.equals("=") && !val.equals(val1) ){
							filter = true;
							break;
						}
						if( method.equals("<>") && val.equals(val1) ){
							filter = true;
							break;
						}
					}
				}
				if( conditions.length() > 0 && filter ) continue;
				if( p.has(key) && p.has(value))
				{
					BasicDBObject label = new BasicDBObject("value", (returnpath?path:"")+p.get(key));
					label.put("label", String.valueOf(p.get(value)));
					if( !dependColumn.isEmpty() && p.has(dependColumn) ){
						label.put("depend", p.get(dependColumn));
					}
					labels.add(label);
		            if( valueSb.length() > 0 ){
		        		valueSb.append(",");
		        	}
		        	valueSb.append(label.getString("value"));
				}
			}
		}
		labelsModel.put(dataIndx, labels);
		values.put("#"+dataIndx+"#", valueSb.toString());
	}
	
	/**
	 * 
	 * @param optionNode
	 * @param colM
	 */
	private void buildGridCellLabel(Node optionNode, String dataIndx) throws Exception
	{
		if( optionNode == null ) return;
		StringBuilder value = new StringBuilder();
		BasicDBList labels = new BasicDBList();
        for( ; optionNode != null; optionNode = XMLParser.nextSibling(optionNode) )
        {
            if( !optionNode.getNodeName().equalsIgnoreCase( "option" ) ) continue;
            String optionValue = XMLParser.getElementAttr( optionNode, "value" );
            String optionTips = XMLParser.getElementAttr( optionNode, "tips" );
            String optionLabel = XMLParser.getElementValue(optionNode);
            String optionDepend = XMLParser.getElementAttr( optionNode, "depend" );
            if( value.length() > 0 ){
        		value.append(",");
        	}
        	value.append(optionValue);
        	
			BasicDBObject label = new BasicDBObject("value", optionValue);
			label.put("label", optionLabel);
			if( !optionTips.isEmpty() ){
				label.put("tips", optionTips);
			}
			if( !optionDepend.isEmpty() ){
				label.put("depend", optionDepend);
			}
			labels.add(label);
        }
        values.put("#"+dataIndx+"#", value.toString());
        labelsModel.put(dataIndx, labels);
	}
	/**
	 * 
	 * @param diggObject
	 * @param pageSize
	 * @param plusDataIndx
	 */
	private void setDataModelDiggFinish(int pageSize, String plusDataIndx){

        if( !labelsModel.isEmpty() )
        {//构建起值映射模型
        	labelColumns = new JSONObject();
    		Iterator<String> iterator = labelsModel.keySet().iterator();
    		while(iterator.hasNext())
    		{
    			String name = iterator.next();
    			BasicDBList labels = (BasicDBList)labelsModel.get(name);
				JSONObject labelColumn = new JSONObject();
    			for(int i = 0; i < labels.size(); i++)
    			{
    				Object o = labels.get(i);
    				if (o instanceof BasicDBObject)
    				{
    					BasicDBObject e = (BasicDBObject)o;
    					labelColumn.put(e.getString("value"), e.getString("label"));
    				}
    			}
				labelColumns.put(name, labelColumn);
    		}
        	diggObject.put("labelColumns", labelColumns);
        }
        diggObject.put("gridxml", gridxml);
        diggObject.put("gridtitle", gridtitle);
        diggObject.put("gridversion", this.gridversion);
        if( plusDataIndx == null )
		{
			if( pageSize > 0 )
			{//大于0的时候设置分页模型
				pageModel = "{ type: dataModel.location, rPP: "+pageSize+", strRpp: '{0}', rPPOptions: [1, 10, 20, 30, 40, 50, 100, 500, 1000,2000,5000,10000,20000] }";
			}
//			location = "remote";
			if( ww != 4121113 ){//如果是表单编辑，会给这个变量赋特殊的值，那么不将diggObject写入到会话中。
				if( opendigg ){
					String uri = Kit.URLPATH(super.getRequest());
					String key = Tools.replaceStr(uri, "digg!open.action", "digg/open");
					log.info("Set the config of digg to session "+key+" from "+uri);
					getSession().setAttribute(key, diggObject);
				}
				else{
					getSession().setAttribute(Kit.URLPATH(super.getRequest()), diggObject);
				}
			}
		}
		else
		{
			getSession().setAttribute(Kit.URLPATH(super.getRequest())+"#"+plusDataIndx, diggObject);
		}
	}
	/**
	 * 构建数据模型
	 * @param recIndx
	 * @param datamodelNode
	 * @param titleColumns
	 * @param dataColumns
	 * @param sortColumns
	 * @param beforeTableView
	 * @param plusDataIndx null 表示主表  有值表示弹出窗口 或者详情
	 * @return
	 * @throws Exception
	 */
	private HashMap<String, String> labels = new HashMap<String, String>();
	protected DataModel createDataModel(
			String recIndx,
			Node datamodelNode, 
			JSONArray titleColumns,
			JSONObject dataColumns, 
			JSONArray sortColumns,
			StringBuilder gridObj,//local模式才有这个
			String plusDataIndx,
			int depth) throws Exception
	{
        if( datamodelNode == null ){
        	return null;
        }
        if( recIndx == null || recIndx.isEmpty() ){
        	recIndx = XMLParser.getElementAttr( datamodelNode, "recIndx" ); 
        	this.id = recIndx;
        }
		String type = XMLParser.getElementAttr( datamodelNode, "type" );
		if( type.isEmpty() && plusDataIndx == null ){
			type = "report".equalsIgnoreCase(filetype)?filetype:type;
		}
		DataModel datamodel = null;
		if( "dashborad".equalsIgnoreCase(type) ){
			this.localDataArray = new JSONArray();
			datamodel = new DataModelDashboard(type, super.getRequest(), values, checker){
				public void finish(){
					localDataArray = localArray;
					localData = localArray.toString();
				}
			};
		}
        //判断模板是不是报表模板
		else if( "report".equalsIgnoreCase(type) ){
        	labels.clear();
			BasicDBList list = (BasicDBList)labelsModel.get(recIndx);
        	if( list != null ){
    			for(int i = 0; i < list.size(); i++)
    			{
    				Object o = list.get(i);
    				if (o instanceof BasicDBObject)
    				{
    					BasicDBObject e = (BasicDBObject)o;
    					labels.put(e.getString("value"), e.getString("label"));
    				}
    			}
        	}
			datamodel = new DataModelReport(filetype, super.getRequest(), values, checker){
				public void finish(){
			        setDiggObject(getDiggObject());
					setDataModelDiggFinish(pageSize, plusDataIndx);
				}
				public void setDimension(
						ArrayList<JSONObject> dimension,
						HashMap<String, JSONObject> dimensionMap,
						Object val,
						HashMap<String, String> merge){
					String key = val.toString();
					JSONObject e = null;
					
					if( merge.containsKey(key) ){
						String key1 = merge.get(key);
						if( labels.containsKey(key1) ){
							key1 = labels.get(key1);
						}
						if( dimensionMap.containsKey(key1) ){
							e = dimensionMap.get(key1);
							e.getJSONArray(recIndx+"_val").put(val);
						}
						else{
							e = new JSONObject();
							e.put(recIndx, key1);
							e.put(recIndx+"_val", new JSONArray().put(val));
							dimensionMap.put(key1, e);
							dimension.add(e);
						}
					}
					else {
						if( labels.containsKey(key) ){
							key = labels.get(key);
						}
						e = new JSONObject();
						e.put(recIndx, key);
						e.put(recIndx+"_val", new JSONArray().put(val));
						dimension.add(e);
						dimensionMap.put(key, e);
					}
				}
			};
        }
        else if( "zookeeper".equals(type) ) {
			datamodel = new DataModelZookeeper(filetype, super.getRequest(), values, checker){
				public void finish(){
					if( plusDataIndx == null ){
						location = "local";
						if( localArray != null ){
							localDataArray = localArray;
							localData = localDataArray!=null?localDataArray.toString():"";
						}
						else if( localObject != null ){
							localDataObject = localObject;
							localData = localObject.toString();
						}
					}
					else{
						localDataObject.put(plusDataIndx, localArray);
						localDataPlus = localDataObject.toString();
					}
				}
			};
		}
		//通过数据库获取数据
		else if( "digg".equals(type) ) {
			datamodel = new DataModelDigg(type, super.getRequest(), values, checker) {
				@Override
				public void finish() {
			        if( "local".equalsIgnoreCase(location) ){
			        	asyncDigg = true;//异步拉取置为true
			        }
			        if( this.plusDataIndx == null ){
			        	handleRemoteData = this.javascript;
			        }
			        setDiggObject(getDiggObject());
					setDataModelDiggFinish(pageSize, plusDataIndx);
				}

				@Override
				public String getDiggDataUrl() {
					return opendigg?"opendata":"digg!data.action";
				}
			};
		}
		//其他情况
		else{
			datamodel = new DataModel(type, super.getRequest(), values, checker) {
				@Override
				public void build(Node datamodelNode, JSONObject dataColumns,
						JSONArray titleColumns, String gridxml,StringBuilder gridObj)
						throws Exception {
					//如果脚本对象还有效，需要设置表格刷新前时间
					if( javascript != null && javascript.length() > 0 )
					{
						test(depth, "Local数据模型配置的视图刷新前回调脚本 : %s", javascript);
						checker.js(javascript.toString(), depth);
						gridObj.append(",beforeTableView: ");
						gridObj.append(this.setLocalCallbackScript(javascript));
					}
					dataM.append(",\r\n\tlocation: 'local'");
					dataM.append(",\r\n\tsorting: 'local'");
					if( plusDataIndx == null)
					{
						location = "local";
						localData = localDataArray!=null?localDataArray.toString():"[]";
						dataM.append(",\r\n\tdata: filterRows(dataLocal)");
					}
					else
					{
						localDataPlus = localDataObject!=null?localDataObject.toString():"{}";
						dataM.append(",\r\n\tdata: filterRows(dataLocalPlus['"+plusDataIndx+"'])");
					}
				}
				@Override
				public void finish() {
				}
			};
		}
		String javascript = XMLParser.getCData(datamodelNode);
		datamodel.initialize(recIndx, sortColumns, plusDataIndx, javascript);
		datamodel.build(datamodelNode, dataColumns, titleColumns, this.gridxml, gridObj);
		test(depth, "生成的数据模型配置如下：%s", datamodel.toString());
		return datamodel;
	}

	/**
	 * 构建选择模型
	 * @param modelNode
	 */
	protected String getSelectionModel(Node selectionNode) throws Exception
	{
        if( selectionNode == null ) return null;
//		type: 'none', subtype:'incr', cbHeader:true, cbAll:true
        StringBuffer selectionModel = new StringBuffer();//"{ type: '"+type+"', mode: '"+mode+"' }";
        selectionModel.append("{");
        String type = XMLParser.getElementAttr( selectionNode, "type" );
        if( type.isEmpty() ) type = "none";
    	selectionModel.append("type: '"+type+"'");
        
        String mode = XMLParser.getElementAttr( selectionNode, "mode" );
        if( !mode.isEmpty() ) selectionModel.append(", mode: '"+mode+"'");
        String subtype = XMLParser.getElementAttr( selectionNode, "subtype" );
        if( !subtype.isEmpty() ) selectionModel.append(", subtype: '"+subtype+"'");
        String cbHeader = XMLParser.getElementAttr( selectionNode, "cbHeader" );
        if( !cbHeader.isEmpty() ) selectionModel.append(", cbHeader: "+cbHeader);
        String cbAll = XMLParser.getElementAttr( selectionNode, "cbAll" );
        if( !cbAll.isEmpty() ) selectionModel.append(", cbAll: "+cbAll);
        selectionModel.append("}");
        
        Node cellNode = XMLParser.getChildElementByTag(selectionNode, "cellSave");
        if( cellNode != null )
        {
        	this.cellSave = XMLParser.getCData(cellNode);
        }
        cellNode = XMLParser.getChildElementByTag(selectionNode, "cellBeforeSave");
        if( cellNode != null )
        {
        	this.cellBeforeSave = XMLParser.getCData(cellNode);
        }
        cellNode = XMLParser.getChildElementByTag(selectionNode, "cellSelect");
        if( cellNode != null )
        {
        	this.cellSelect = XMLParser.getCData(cellNode);
        }
        cellNode = XMLParser.getChildElementByTag(selectionNode, "cellClick");
        if( cellNode != null )
        {
        	this.cellClick = XMLParser.getCData(cellNode);
        }
        cellNode = XMLParser.getChildElementByTag(selectionNode, "cellDblClick");
        if( cellNode != null )
        {
        	this.cellDblClick = XMLParser.getCData(cellNode);
        }
        cellNode = XMLParser.getChildElementByTag(selectionNode, "cellKeyDown");
        if( cellNode != null )
        {
        	this.cellKeyDown = XMLParser.getCData(cellNode);
        }
        cellNode = XMLParser.getChildElementByTag(selectionNode, "cellEditKeyDown");
        if( cellNode != null )
        {
        	this.cellEditKeyDown = XMLParser.getCData(cellNode);
        }
        cellNode = XMLParser.getChildElementByTag(selectionNode, "cellRightClick");
        if( cellNode != null )
        {
        	this.cellRightClick = XMLParser.getCData(cellNode);
        }
        return selectionModel.toString();
	}
	
	/**
	 * 
	 * @param modelNode
	 * @return
	 * @throws Exception
	 */
	protected String getScrollModel(Node scrollNode) throws Exception
	{
        if( scrollNode == null ) return null;
        StringBuffer scrollModel = new StringBuffer();
        scrollModel.append("{");
        String autoFit = XMLParser.getElementAttr( scrollNode, "autoFit" );
        if( autoFit.isEmpty() ) autoFit = "false";
        scrollModel.append("autoFit: "+autoFit);
        scrollModel.append("}");
        return scrollModel.toString();
	}
	/**
	 * 构建工具栏
	 * @param buttonNode
	 */
	protected void buildToolbars(Node toolbarNode)
		throws Exception
	{
		if( toolbarNode == null ){
			test(1, "模板没有配置toolbar节点，没有用户自定义工具栏，引擎将自动配置缺省导出按钮.");
			return;
		}
        this.toolbarSize = XMLParser.getElementAttr( toolbarNode, "size", "lg" );
        this.exportable = !"false".equalsIgnoreCase(XMLParser.getElementAttr(toolbarNode, "exportable"));
		Node itemNode = XMLParser.getFirstChildElement(toolbarNode);
        for( ; itemNode != null; itemNode = XMLParser.getNextSibling(itemNode) )
        {
        	if( "button".equalsIgnoreCase(itemNode.getNodeName()) )
            {
        		this.buildToolbarButton(itemNode, toolbarNode);
            }
        	else if( "filter".equalsIgnoreCase(itemNode.getNodeName()) )
            {
        		this.buildToolbarFilter(itemNode, toolbarNode);
            }

        }
        if( toolbars.isEmpty() )
        {
			test(1, "模板没有配置toolbar节点，没有用户自定义工具栏，引擎将自动配置缺省导出按钮.");
        }
	}
	
	/**
	 * 構建工具欄文本輸入
	 * @param textboxNode
	 * @param toolbarNode
	 * @throws Exception 
	 */
	private void buildToolbarFilter(Node filterNode, Node toolbarNode) throws Exception
	{
		//{ type: 'textbox', attr: 'placeholder="Enter your keyword"', cls: "filterValue", listeners: [{ 'change': filterhandler}] },
		//<filter type='textbox' init='pqDatePicker' condition='between' listeners='change'/>
    	String subject = XMLParser.getElementAttr( filterNode, "subject" );
    	String dataIndx = XMLParser.getElementAttr( filterNode, "dataIndx" );
        String type = XMLParser.getElementAttr( filterNode, "type" );
        String placeholder = XMLParser.getElementAttr( filterNode, "placeholder" );
        String condition = XMLParser.getElementAttr( filterNode, "condition" );
		BasicDBObject filter = new BasicDBObject("dataIndx", dataIndx);
		filter.put("filter", true);
        if( !subject.isEmpty() ){
        	filter.put("label", subject);
        }
        if( condition.isEmpty() ){
        	condition = "equal";
        }
        filter.put("condition", condition);
        if( !placeholder.isEmpty() ){
        	filter.put("placeholder", placeholder);
        }
        filter.put("change", "toolbarfilterhandler");
    	if ("calendar".equalsIgnoreCase(type)) {
    		type = "textbox";
    		filter.put("calendar", Boolean.valueOf(true));
    	}
    	filter.put("type", type);

        BasicDBList fields = (BasicDBList)filterModel.get("fields");
        if( fields == null )
        {
        	fields = new BasicDBList();
        	filterModel.put("fields", fields);
        	filterModel.put("on", true);
        }
		String attr = XMLParser.getElementAttr( filterNode, "attr" );
		if( !attr.isEmpty() ) filter.append("attr", attr);
		String format = XMLParser.getElementAttr( filterNode, "format" );
		if( !format.isEmpty() ) filter.append("format", format);
		String style = XMLParser.getElementAttr( filterNode, "style" );
		filter.append("style", style);
		fields.add(filter);
    	
        test(1, "添加工具栏过滤器[%s]%s，类型[%s]，提示[%s].", dataIndx, subject, type, placeholder);
        if("select".equalsIgnoreCase(type)){
        	Node optionsNode = XMLParser.getChildElementByTag(filterNode, "options");
            if( optionsNode != null )
            {//通过其他数据源关联选项数据
            	type = XMLParser.getElementAttr(optionsNode, "type");
            	if( !type.isEmpty() )
            	{
            		if( "zookeeper".equalsIgnoreCase(type) )
            		{
            			this.buildGridZookeeperLabel(optionsNode, dataIndx);
            		}
            		if( "mongo".equalsIgnoreCase(type) )
            		{
    	        		this.buildGridMongoLabel(optionsNode, dataIndx);
            		}
            		if( "sql".equalsIgnoreCase(type) )
            		{
    	        		this.buildGridSqlLabel(optionsNode, dataIndx);
            		}
            	}
            }
            else{//模板中配置选项数据
            	Node optionNode = XMLParser.getChildElementByTag( filterNode, "option" );
        		BasicDBList labels = new BasicDBList();
                for( ; optionNode != null; optionNode = XMLParser.nextSibling(optionNode) )
                {
                    if( !optionNode.getNodeName().equalsIgnoreCase( "option" ) ) continue;
                    String optionValue = XMLParser.getElementAttr( optionNode, "value" );
                    String optionLabel = XMLParser.getElementValue(optionNode);

        			BasicDBObject label = new BasicDBObject("value", optionValue);
        			label.put("label", optionLabel);
        			labels.add(label);
                }
                if( !labels.isEmpty() ){
                	labelsModel.put(dataIndx, labels);
                }
            }
        }
		toolbars.add(filter);
		test(2, "%s", new Object[] { filter.toString() });
//        { "begin": "Begins With" },
//        { "contain": "Contains" },
//        { "end": "Ends With" },
//        { "notcontain": "Does not contain" },
//        { "equal": "Equal To" },
//        { "notequal": "Not Equal To" },
//        { "empty": "Empty" },
//        { "notempty": "Not Empty" },
//        { "less": "Less Than" },
//        { "great": "Great Than" }
	}
	/**
	 * 構建工具欄按鈕
	 * @param buttonNode
	 * @param toolbarNode
	 * @throws Exception
	 */
	private void buildToolbarButton(Node buttonNode, Node toolbarNode) throws Exception{
    	String subject = XMLParser.getElementAttr( buttonNode, "subject" );
        String icon = XMLParser.getElementAttr( buttonNode, "icon" );
        String type = XMLParser.getElementAttr( buttonNode, "type" );
        String btn = XMLParser.getElementAttr( buttonNode, "btn" );
        String confirm = XMLParser.getElementAttr( buttonNode, "confirm" );
        test(1, "添加工具栏按钮[%s]，类型[%s]，图标[%s].", subject, type, icon);
        String javascript = XMLParser.getCData(buttonNode);
        javascript = checker.javascript(javascript, super.getRequest(), "toolbar.button");
        BasicDBObject button = new BasicDBObject("label", subject);
        button.put("button", true);
        button.put("id", subject);
        button.put("icon", icon);
        button.put("type", type);
        button.put("btn", btn.isEmpty()?"default":btn);
        button.put("confirm", confirm);
		if( "popup".equalsIgnoreCase(type) )
		{
			Node popupNode = XMLParser.getChildElementByTag(buttonNode, "popup");
			if (popupNode != null) {
				button.put("popup", Boolean.valueOf(true));
				String id = XMLParser.getElementAttr(popupNode, "id");
				button.put("id", id);
				String title = XMLParser.getElementAttr(popupNode, "title");
				if (title.isEmpty()) {
					title = subject;
				}
				String width = XMLParser.getElementAttr(popupNode, "width");
				if ((width.isEmpty()) || (!Tools.isNumeric(width))){
					width = "512";
				}
				String height = XMLParser.getElementAttr(popupNode, "height");
				if ((height.isEmpty()) || (!Tools.isNumeric(height))) { 
					height = "384";
				}
				Node formNode = XMLParser.getChildElementByTag(popupNode, "form");
				if (formNode == null) {
					test(2, "添加工具栏弹窗[%s][%s]失败，因为没有配置%s表单.", new Object[] { id, title, "form" });
					return;
				}
				BasicDBList popups = new BasicDBList();
				for (Node intputNode = XMLParser.getFirstChildElement(formNode); intputNode != null; intputNode = XMLParser.getNextSibling(intputNode)){
					String name = intputNode.getNodeName();
					String style = XMLParser.getElementAttr(intputNode, "style");
					String label = XMLParser.getElementAttr(intputNode, "label");
					String placeholder = XMLParser.getElementAttr(intputNode, "placeholder");
					BasicDBObject input = new BasicDBObject();
					input.put("name", name);
					input.put("style", style);
					input.put("label", label);
					input.put("placeholder", placeholder);
					popups.add(input);
				}
				button.put("popups", popups);
				Node openNode = XMLParser.getChildElementByTag(popupNode, "opencallback");
				Node closeNode = XMLParser.getChildElementByTag(popupNode, "closecallback");
				Node actionNode = XMLParser.getChildElementByTag(popupNode, "action");
				StringBuffer sb = new StringBuffer();
				if (openNode != null) {
					String open = XMLParser.getCData(openNode);
				    sb.append(open);
				}
				sb.append("\r\nvar d = { title: '" + title + "', width: " + width + ", height: " + height);
				sb.append("\r\n\t,buttons: {");
				if (actionNode != null) {
					String name = XMLParser.getElementAttr(actionNode, "name");
					String action = XMLParser.getCData(actionNode);
					sb.append("\r\n\t\t'" + name + "': " + action);
					sb.append(",");
				}
				if (closeNode != null) {
				  String name = XMLParser.getElementAttr(closeNode, "name");
				  if (name.isEmpty()) {
				    name = "取消";
				  }
				  String close = XMLParser.getCData(closeNode);
				  sb.append("\r\n\t\t'" + name + "': " + close);
				}
				else {
				  sb.append("\r\n\t\t'取消': function() {");
				  sb.append("\r\n\t\t\t$(this).dialog('close');//关闭对话框弹出");
				  sb.append("\r\n\t\t}");
				}
				sb.append("\r\n\t}");
				sb.append("\r\n}");
				sb.append("\r\n$('#popup-dialog-crud-" + id + "').dialog(d).dialog('open');");
				javascript = sb.toString();
				test(2, "添加工具栏按钮弹窗事件[%s]，弹窗标题[%s]，窗口大小[%sx%s].", new Object[] { id, title, width, height });
			}
		}
		else if( "addRow".equalsIgnoreCase(type) )
		{
			javascript = "addRow($grid);";
		}
        test(2, "按钮事件脚本: %s", javascript);
        checker.js(this.javascript, javascript, 2);
        button.put("javascript", javascript);
		toolbars.add(button);
	}

	/*grid的xml路径*/
	protected String gridxml;
	/*Grid的标题*/
	protected String gridtitle;
    /*GRID的版本*/
	protected String gridversion;

	/**
	 * 递归构建表格的列字段
	 * @param gridNode
	 */
	protected void setGridParameters(HttpServletRequest req, Node cellNode, JSONObject data) throws Exception
	{
        for( ; cellNode != null; cellNode = XMLParser.nextSibling(cellNode))
        {
            if( !cellNode.getNodeName().equalsIgnoreCase( "cell" ) ) continue;
            Node childcellNode = XMLParser.getChildElementByTag(cellNode, "cell");
            if( childcellNode != null )
            {
            	this.setGridParameters(req, childcellNode, data);
            }
            else
            {
            	String title = XMLParser.getElementAttr( cellNode, "title" );
            	String dataIndx = XMLParser.getElementAttr( cellNode, "dataIndx" );
            	if( dataIndx.isEmpty() ) continue;
            	String value = Tools.delHTMLTag(req.getParameter(dataIndx));//Tools.getJSONValue(req.getParameter(dataIndx));
            	value = Tools.replaceStr(value, "&amp;", "&");
            	value = Tools.replaceStr(value, "<div>", "");
				value = Tools.replaceStr(value, "</div>", "");
            	value = value.replaceAll("<br>", "");
            	String dataType = XMLParser.getElementAttr( cellNode, "dataType" );
            	if( dataType.isEmpty() ) dataType = "string";
            	else if( dataType.equalsIgnoreCase("datalength") ){
            		dataType = "long";
            	}
            	String tag = XMLParser.getElementAttr( cellNode, "tag" );
            	Object val = value;
            	boolean nullable = "true".equalsIgnoreCase(XMLParser.getElementAttr( cellNode, "nullable" ));
            	if( !nullable ){
            		nullable = "true".equalsIgnoreCase(XMLParser.getElementAttr( cellNode, "onlyshow" ));
            	}
            	if( !nullable ){
            		nullable = "true".equalsIgnoreCase(XMLParser.getElementAttr( cellNode, "hidden" ));
            	}
            	boolean isTag = dataType.equalsIgnoreCase("tag") || (!tag.isEmpty() && Tools.isNumeric(tag));
            	if( dataType.startsWith("int") )
            	{
            		if( !Tools.isNumeric(value) )
            		{
            			if( nullable || skipnull) continue;
                		throw new Exception("【"+title+"】必须是数字，您输入的是"+value+"。");
            		}
            		val = Integer.parseInt(value);
            	}
            	else if( dataType.equalsIgnoreCase("long") )
            	{
            		if( !Tools.isNumeric(value) )
            		{
            			if( nullable || skipnull) continue;
                		throw new Exception("【"+title+"】必须是长整数字，您输入的是"+value+"。");
            		}
            		val = Long.parseLong(value);
            	}
            	else if( dataType.equalsIgnoreCase("number") )
            	{
            		if( !Tools.isNumeric(value) )
            		{
            			if( nullable || skipnull ) continue;
                		throw new Exception("【"+title+"】必须是Float数字，您输入的是"+value+"。");
            		}
            		val = Float.parseFloat(value);
            	}
//            	else if( ("zookeeper".equalsIgnoreCase(type) || mongoHost != null ) &&  (isTag || dataType.equalsIgnoreCase("object") ) )
            	else if( (isTag || dataType.equalsIgnoreCase("object") ) )
            	{//配置数据源是ZK和芒果才将数据处理成对象
            		if( !value.isEmpty() && value.startsWith("{") )
            		{
	            		val = new JSONObject(value);
//	            		response.put(dataIndx, val);
            		}
            		else if( !value.isEmpty() && value.startsWith("[") )
            		{
	            		val = new JSONArray(value);
//	            		response.put(dataIndx, val);
            		}
            	}
            	else if( dataType.equalsIgnoreCase("password") )
            	{
            		if( !value.isEmpty() && !value.equals("******") )
            		{
            			val = Base64X.encode(value.getBytes());
            		}
            		else{
            			continue;//密码，不保存
            		}
            	}
            	else if( dataType.equalsIgnoreCase("ip") )
            	{
            		if( Tools.countChar(value, '.') != 3 )
            		{
            			if( nullable || skipnull ) continue;
                		throw new Exception("【"+title+"】必须是IP地址格式，您输入的是"+value+"。");
            		}
            	}
            	else if( dataType.startsWith("bool") || dataType.equalsIgnoreCase("boolean") )
            	{
        			if( value.indexOf("ui-icon-check") != -1 ) value = "true";
        			val = Boolean.parseBoolean(value);
            	}
            	else if( dataType.equalsIgnoreCase("md5") )
            	{
                	String encode = XMLParser.getElementAttr( cellNode, "encode" );
            		if( !encode.isEmpty() )
            		{
            			encode = req.getParameter(encode); 
            			if( encode != null && !encode.isEmpty() )
            			{
            				val = Tools.encodeMD5(encode);
            			}
            		}
            	}
            	if( val.toString().isEmpty()  )
            	{
            		if( skipnull ){
            			continue;
            		}
            		if( !nullable )	throw new Exception("字段【"+title+"/"+dataIndx+"】不允许为空。");
            	}
				data.put(dataIndx, val);
            }
        }
	}

	/**
	 * 递归设置Grid的密码
	 * @param gridNode
	 */
	protected void setGridTag(Node cellNode, JSONObject old, JSONObject data) throws Exception
	{
		if( old == null ) return;
        for( ; cellNode != null; cellNode = XMLParser.nextSibling(cellNode))
        {
            if( !cellNode.getNodeName().equalsIgnoreCase( "cell" ) ) continue;
            Node childcellNode = XMLParser.getChildElementByTag(cellNode, "cell");
            if( childcellNode != null )
            {
            	this.setGridTag(childcellNode, old, data);
            }
            else
            {
            	String dataType = XMLParser.getElementAttr( cellNode, "dataType" );
            	if( dataType.isEmpty() ){
            		dataType = "string";
            	}
            	String tag = XMLParser.getElementAttr( cellNode, "tag" );
            	boolean isTag = !tag.isEmpty() && Tools.isNumeric(tag);
            	if( dataType.equalsIgnoreCase("tag") ){
            		dataType = "string";
            		isTag = true;
            	}
            	if( !isTag ) continue;
            	String dataIndx = XMLParser.getElementAttr( cellNode, "dataIndx" );
            	if( old.has( dataIndx ) )
            	{
            		Object obj = old.get(dataIndx);
            		if( !data.has(dataIndx) )
            		{
            			data.put(dataIndx, obj);
            		}
            	}
            }
        }
	}
	/**
	 * 递归设置Grid的密码
	 * @param gridNode
	 */
	protected void setGridPassword(Node cellNode, JSONObject old, JSONObject data) throws Exception
	{
		if( old == null ) return;
        for( ; cellNode != null; cellNode = XMLParser.nextSibling(cellNode))
        {
            if( !cellNode.getNodeName().equalsIgnoreCase( "cell" ) ) continue;
            Node childcellNode = XMLParser.getChildElementByTag(cellNode, "cell");
            if( childcellNode != null )
            {
            	this.setGridPassword(childcellNode, old, data);
            }
            else
            {
            	String dataType = XMLParser.getElementAttr( cellNode, "dataType" );
            	if( dataType.isEmpty() ) dataType = "string";
            	if( !"password".equalsIgnoreCase(dataType) ) continue;
            	String dataIndx = XMLParser.getElementAttr( cellNode, "dataIndx" );
            	if( old.has( dataIndx ) )
            	{
            		if( !data.has(dataIndx) || data.getString(dataIndx).equals("******") )
            		{
            			data.put(dataIndx, old.getString(dataIndx));
            		}
            	}
            }
        }
	}
	/**
	 * 执行数据设置，跳过空与空字符串
	 * @return
	 */
	private boolean skipnull;
	public boolean isSkipnull() {
		return skipnull;
	}
	public String doSetdata()
	{
		skipnull = true;
		return doUpdate();
	}
	/**
	 * 执行更新数据
	 * @param autoadd
	 * @param saver
	 * @return
	 */
	public String doUpdate()
	{
		checker = new TemplateChecker(workmode, this.values);
		StringBuffer sb = new StringBuffer();
		sb.append(""
				+ " "+gridxml+", the parameters of below ");
		Iterator<Map.Entry<String, String[]>> iterator = super.getRequest().getParameterMap().entrySet().iterator();
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

		try
		{
			GridSetter setter = new GridSetter(this){
				@Override
				public JSONObject set() throws Exception
				{
					JSONObject data = new JSONObject();
					Node cellnode = XMLParser.getChildElementByTag( filednode, "cell" );
					if( cellnode == null )
					{
						throw new Exception("不正确的grid("+gridtitle+")格式");
					}
					setGridParameters(getRequest(), cellnode, data);
					String recIndx = getRecIndx();
					myid = getRequest().getParameter(recIndx);
					if( myid != null && !myid.isEmpty() ){
						this.method = GridSetter.UPDATE;
						myid = Tools.replaceStr(myid, "<br>", "");
						myid = Tools.replaceStr(myid, "<div>", "");
						myid = Tools.replaceStr(myid, "</div>", "");
					}
					else{
						this.method = GridSetter.ADD;
					}
					log.debug("Todo set data("+myid+") of update:"+data.toString(4));
					return data;
				}
				@Override
				public void update(JSONObject old, JSONObject set) throws Exception {
					Node cellnode = XMLParser.getChildElementByTag( filednode, "cell" );
					String editmode = XMLParser.getElementAttr(filednode, "editmode");
					if( old != null && "1".equals(editmode) )
					{
						Iterator<?> iterator = old.keys();
						while(iterator.hasNext())
						{
							String key = iterator.next().toString();
							Object value = old.get(key);
							if( !set.has(key) )	set.put(key, value);
						}
					}
					setGridPassword(cellnode, old, set);
					setGridTag(cellnode, old, set);
				}

			};
//			setter.setAutoadd(true);
			JSONObject response = setter.execute((JSONObject)getSession().getAttribute("account"));
//			System.err.println(response.toString(4));
			return response(this.getResponse(), response.toString());
		}
		catch (Exception e1)
		{
			sb = new StringBuffer();
			sb.append("Failed to execute doUpdate by "+gridxml+", watch the parameters of below ");
			Iterator<Map.Entry<String, String[]>> iterator1 = super.getRequest().getParameterMap().entrySet().iterator();
			while(iterator1.hasNext())
			{
				Map.Entry<String, String[]> e = iterator1.next();
				sb.append("\r\n\t");
				sb.append(e.getKey());
				sb.append("=");
				for(String value : e.getValue())
					sb.append(value+"\t");
			}
			log.error(sb.toString(), e1);
			JSONObject response = new JSONObject();
			response.put("hasException", true);
			response.put("message", "保存配置出现异常: "+e1.getMessage());
			return response(this.getResponse(), response.toString());
		}
	}

	/**
	 * 删除
	 * @return
	 */
	public String doDelete() {
		checker = new TemplateChecker(workmode, this.values);
		StringBuffer sb = new StringBuffer();
		sb.append("Receive the request of delete by "+gridxml+", the parameters of below ");
		Iterator<Map.Entry<String, String[]>> iterator = super.getRequest().getParameterMap().entrySet().iterator();
		while(iterator.hasNext()) {
			Map.Entry<String, String[]> e = iterator.next();
			sb.append("\r\n\t");
			sb.append(e.getKey());
			sb.append("=");
			for(String value : e.getValue())
				sb.append(value+"\t");
		}
		log.info(sb.toString());
		try
		{
			GridSetter setter = new GridSetter(this){
				@Override
				public JSONObject set() throws Exception
				{
					String recIndx = getRecIndx();
					myid = getRequest().getParameter(recIndx);
					this.method = GridSetter.DELETE;
					log.info("Todo delete the data("+myid+") of grid from "+path);
					return null;
				}
				@Override
				public void update(JSONObject old, JSONObject set) throws Exception {
				}
			};
//			setter.setSaver(saver);
			return response(this.getResponse(), setter.execute((JSONObject)getSession().getAttribute("account")).toString());
		}
		catch (Exception e)
		{
			JSONObject result = new JSONObject();
			result.put("hasException", true);
			result.put("message", e.getMessage());
			return response(this.getResponse(), result.toString());
		}
	}
	
	/**
	 * 数据类型是数字
	 * @param dataType
	 * @return
	 */
	public boolean dataTypeIsNumber(String dataType)
	{
		if( "number".equalsIgnoreCase(dataType) ) return true;
		if( "int".equalsIgnoreCase(dataType) ) return true;
		if( "long".equalsIgnoreCase(dataType) ) return true;
		return false;
	}
	/**
	 * 
	 * @param gridxml
	 */
	public void setGridxml(String gridxml) 
	{
		this.gridxml = gridxml;
	}

	public String getGridxml() {
		return gridxml;
	}

	public void setGridtitle(String gridtitle) {
		this.gridtitle = gridtitle;
	}
	public void setUploadfile(File uploadfile) {
		this.uploadfile = uploadfile;
	}

	public String getUploadurl() {
		return this.uploadurl;
	}

	public String getPageModel() {
		return pageModel;
	}
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
	/**
	 * 设置JDBC连接参数
	 * @param src
	 * @param sObject
	 * @throws Exception
	 */
	public void presetDatabase(String src) throws Exception
	{
		if(src != null && !src.isEmpty() )
    	{
			this.presetDatabase(src, new JSONObject());
    	}
	}
	
	public void presetDatabase(String src, JSONObject sObject) throws Exception
	{
		if( src == null || src.isEmpty() ) src = "local";
		StringBuffer sb = new StringBuffer();
		sb.append("Receive the request, the parameters of below ");
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
//		log.info(sb.toString());
    	if( "local".equalsIgnoreCase(src) )
    	{
			Properties jdbcConfig = new Properties();
			db = "h2";
	    	jdbcConfig.load(new FileInputStream(new File(PathFactory.getWebappPath(), "WEB-INF/classes/config/jdbc.properties")));
	    	jdbcUrl = jdbcConfig.getProperty("jdbc.url");
	    	jdbcUsername = jdbcConfig.getProperty("jdbc.username");
	    	jdbcUserpswd = jdbcConfig.getProperty("jdbc.password");
	    	driverClass = jdbcConfig.getProperty("jdbc.driverClass");
    		if( jdbcUserpswd == null ) jdbcUserpswd = "";
    		if( jdbcUrl == null || jdbcUrl.isEmpty() || jdbcUsername == null || jdbcUsername.isEmpty() || driverClass == "" || driverClass.isEmpty() )
    		{
    			throw new Exception("因为jdbc配置连接参数("+db+")无效执行数据库加载");
    		}
    		sObject.put("dbtype", db);
    		sObject.put("jdbc.driverClass", driverClass);
    		sObject.put("jdbc.username", jdbcUsername);
    		sObject.put("jdbc.password", new String(Base64X.encode(jdbcUserpswd.getBytes())));
    		sObject.put("jdbc.url", jdbcUrl);
    	}
		else if( src.startsWith("/cos/config/monitor/") )
		{
			JSONObject jdbcConfig = ZKMgr.getZookeeper().getJSONObject(db);
			if( jdbcConfig != null )
			{
		    	jdbcUrl = jdbcConfig.getString("jdbc.url");
		    	jdbcUsername = jdbcConfig.getString("jdbc.username");
		    	jdbcUserpswd = jdbcConfig.getString("jdbc.password");
		    	driverClass = jdbcConfig.getString("jdbc.driver");
        		sObject.put("jdbc.driverClass", driverClass);
        		sObject.put("jdbc.username", jdbcUsername);
        		sObject.put("jdbc.password", jdbcUserpswd);
        		sObject.put("jdbc.url", jdbcUrl);
		    	jdbcUserpswd = new String(Base64X.decode(jdbcUserpswd));
			}
    		if( jdbcUrl == null || jdbcUrl.isEmpty() || jdbcUsername == null || jdbcUsername.isEmpty() || driverClass == "" || driverClass.isEmpty() )
    		{
    			throw new Exception("因为jdbc配置连接参数("+src+")无效执行数据库加载");
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
//				!datasource.has("dbpassword") 
				)
    		{
    			throw new Exception("未发现正确数据源配置("+src+","+datasource+")无法进行数据查询");
    		}
    		db = datasource.getString("dbtype");
    		sObject.put("dbtype", db);
			String dbaddr = datasource.getString("dbaddr");
			String dbname = datasource.getString("dbname");
			String username = datasource.getString("dbusername");
			String password = datasource.has("dbpassword")?datasource.getString("dbpassword"):"";
    		if( "mongo".equals(db) )
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
    		else if( "redis".equals(db) )
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
    			if("h2".equalsIgnoreCase(db))
    			{
    				driverClass = "org.h2.Driver";
    				jdbcUrl = "jdbc:h2:tcp://"+dbaddr+"/../h2/"+dbname;
    			}
    			else if("mysql".equalsIgnoreCase(db))
    			{
    				driverClass = "com.mysql.jdbc.Driver";
    				jdbcUrl = "jdbc:mysql://"+dbaddr+"/"+dbname+"?lastUpdateConnt=true&amp;useUnicode=true&amp;characterEncoding=UTF-8";
    			}
    			else if("oracle".equalsIgnoreCase(db))
    			{
    				driverClass = "oracle.jdbc.driver.OracleDriver";
    				jdbcUrl = "jdbc:oracle:thin:@"+dbaddr+":"+dbname;
    			}
    			else
    			{
        			throw new Exception("不支持的数据源类型"+db);
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
	 * 
	 * @param data
	 * @return
	 */
	protected String querydata(JSONArray data, Exception e)
	{
		return this.querydata(data!=null?data.length():0, 1, data, e);
	}
	protected String querydata(int totalRecords, int curPage, JSONArray data, Exception e)
	{
    	ServletOutputStream out = null;
		JSONObject remoteData = new JSONObject();
		try
		{
			HttpServletResponse response = super.getResponse();
            out = response.getOutputStream();
			response.setContentType("text/json;charset=utf8");
    		response.setHeader("Content-disposition", "inline; filename="+System.currentTimeMillis()+".json");

    		remoteData.put("totalRecords", totalRecords);
			remoteData.put("curPage", 1);
			remoteData.put("data", data!=null?data:new JSONArray());
			remoteData.put("hasException", e!=null);
			if( e != null ){
				remoteData.put("message", "获取数据出现异常("+e.getMessage()+")");
				log.error("", e);
			}
		}
		catch (Exception e1)
		{
			log.error("", e1);
		}
        finally
        {
        	if( out != null )
	    		try
				{
	    			String json = remoteData.toString();
					out.write(json.getBytes("UTF-8"));
	            	out.close();
				}
				catch (IOException e1)
				{
					log.error("", e1);
				}
        }
		return null;
	}
	/**
	 * 输出Gid的各项参数
	 */
	public void printGrid()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("Grid{");
		sb.append("\r\ncolModel: "+colModel);
		sb.append("\r\ndataModel: "+dataModel);
		sb.append("\r\nfilterModel: "+filterModel);
		sb.append("\r\nselectionModel: "+selectionModel);
		sb.append("\r\nlocalData: "+this.localData);
		log.debug(sb.toString());
	}
	@Override
	public Object getModel() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int getFreezeCols()
	{
		return freezeCols;
	}

	public String getColModel()
	{
		return colModel;
	}

	public String getDataModel()
	{
		return dataModel;
	}

	public BasicDBObject getFilterModel()
	{
		return filterModel;
	}

	public ArrayList<BasicDBObject> getDetails()
	{
		return details;
	}

	public ArrayList<BasicDBObject> getForms()
	{
		return forms;
	}
	public boolean isHasUeditor() {
		return hasUeditor;
	}
	public String getToolbarSize(){
		return toolbarSize;
	}
	public String getDatatype()
	{
		return datatype;
	}
	
	public void setDb(String db)
	{
		this.db = db;
	}

	public String getDb()
	{
		return db;
	}

	public String getSql()
	{
		return sql;
	}

	public String getRowSelect()
	{
		return rowSelect;
	}

	public String getLoad() {
		return load;
	}
	
	public String getBeforeTableView() {
		return beforeTableView;
	}
	public String getCellSelect()
	{
		return cellSelect;
	}

	public String getCellSave() {
		return cellSave;
	}
	public String getCellBeforeSave() {
		return cellBeforeSave;
	}
	public String getCellClick() {
		return cellClick;
	}
	public String getCellDblClick() {
		return cellDblClick;
	}
	public String getCellRightClick() {
		return cellRightClick;
	}
	public String getCellKeyDown() {
		return cellKeyDown;
	}
	public String getCellEditKeyDown() {
		return cellEditKeyDown;
	}
	
	public String getEditorModel() {
		return editorModel;
	}
	
	public ArrayList<BasicDBObject> getToolbars()
	{
		return toolbars;
	}

	public boolean isHasToolbar()
	{
		if( !hasToolbar ){
			return false;
		}
		return exportable||!toolbars.isEmpty();
	}
	
	public void setHidetoolbar(boolean b) {
		this.hasToolbar = !b;
	}
	
	public String getSelectionModel()
	{
		return selectionModel;
	}

	public String getJavascript()
	{
		return javascript;
	}

	public void setDatatype(String datatype)
	{
		this.datatype = datatype;
	}

	public String getPq_filter()
	{
		return pq_filter;
	}

	protected JSONObject getPqFilter()
	{
		JSONObject filter = null;
        if( pq_filter == null || pq_filter.isEmpty() || !pq_filter.startsWith("{"))
        {
        	filter = new JSONObject();
        }
        else
        {
        	filter = new JSONObject(pq_filter);
        }
        return filter;
	}
	
	public void setPq_filter(String pq_filter)
	{
		this.pq_filter = pq_filter;
	}

	public String getPq_sort() 
	{
		return pq_sort;
	}
	
	public void setPq_sort(String pq_sort) 
	{
		this.pq_sort = pq_sort;
	}
	
	public String getFiletype()
	{
		return filetype;
	}

	public void setFiletype(String filetype)
	{
		this.filetype = filetype;
	}

	public boolean isSnapshotable() 
	{
		return snapshotable;
	}
	
	public String getBottomInfo()
	{
		return bottomInfo;
	}

	public void setSql(String sql)
	{
		this.sql = sql;
	}

	public String getSnapshot()
	{
		return snapshot;
	}

	public int getSnapshotWidth()
	{
		return snapshotWidth;
	}

	public void setSnapshot(String snapshot)
	{
		this.snapshot = snapshot;
	}

	public void setEmailaddr(String emailaddr)
	{
		this.emailaddr = emailaddr;
	}

	public String getEmailaddr()
	{
		return emailaddr;
	}

	public String getLocalData()
	{
		if( localData != null )
		{
			localData = localData.replace("\\", "\\\\");
			localData = localData.replace("'", "");
		}
		return localData;
	}

	
	public boolean isAsyncDigg() {
		return asyncDigg;
	}
	
	public String getAjaxDelete()
	{
		return ajaxDelete;
	}

	public String getAjaxAdd()
	{
		return ajaxAdd;
	}

	public String getAjaxUpdate()
	{
		return ajaxUpdate;
	}

	public ArrayList<BasicDBObject> getEditorPopups()
	{
		return editorPopups;
	}

	public ArrayList<BasicDBObject> getInnerbuttons() {
		return innerbuttons;
	}
	
	public ArrayList<BasicDBObject> getAutoCompleteEditors() {
		return autoCompleteEditors;
	}
	
	public boolean isScrollModelAutoFit()
	{
		return scrollModelAutoFit;
	}

	public boolean isShowBottom()
	{
		return this.pageModel!=null?true:this.summaryObject.length()>0?true:showBottom;
	}

	public boolean isNumberCell() {
		return numberCell;
	}
	
	public boolean isShowTitle() {
		return showTitle;
	}

	public boolean isSortable()
	{
		return sortable;
	}

	public String getSummary()
	{
//		System.err.println(summaryObject.toString(4));
		return summaryObject.length()==0?"":summaryObject.toString();
	}

	public boolean isEditable()
	{
		return editable;
	}

	public boolean isExportable() {
		return exportable;
	}

	public String getBeforeGridView() {
		return beforeGridView;
	}
	public String getJdbcUrl()
	{
		return jdbcUrl!=null?jdbcUrl:"";
	}
	public void setJdbcUrl(String jdbcUrl)
	{
		this.jdbcUrl = jdbcUrl;
	}
	public String getJdbcUsername()
	{
		return jdbcUsername!=null?jdbcUsername:"";
	}
	public void setJdbcUsername(String jdbcUsername)
	{
		this.jdbcUsername = jdbcUsername;
	}
	public String getJdbcUserpswd()
	{
		return jdbcUserpswd!=null?jdbcUserpswd:"";
	}
	public void setJdbcUserpswd(String jdbcUserpswd)
	{
		this.jdbcUserpswd = jdbcUserpswd;
	}
	public String getDriverClass()
	{
		return driverClass!=null?driverClass:"";
	}
	public void setDriverClass(String driverClass)
	{
		this.driverClass = driverClass;
	}
	public int getWorkmode() {
		return workmode;
	}
	public void setWorkmode(int workmode) {
		this.workmode = workmode;
	}
	public boolean isDodebug(){
		return this.workmode>TemplateChecker.WORK;
	}
	public boolean isStylepreview(){
		return this.workmode==TemplateChecker.DEMO;
	}
	public String getHandleRemoteData() {
		return handleRemoteData;
	}
	private void setDiggObject(JSONObject diggObject) {
		this.diggObject = diggObject;
	}

	/**
	 * 写日志
	 * @param info
	 * @param args
	 */

	protected void logError(String info, Object... args){
		this.logError(null, info, args);
	}
	

	protected void logError(Exception e, String info, Object... args){
		String str = String.format(info, args);
		str = String.format("[%s]ERROR %s", Tools.getFormatTime("HH:mm:ss SSS", System.currentTimeMillis()), str);
		trans.append("\r\n");
		trans.append(str);
		
		if( e != null ){
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			PrintWriter writer = new PrintWriter(out);
			e.printStackTrace(writer);
			trans.append("\r\n");
			trans.append(new String(out.toByteArray()));
			try {
				out.close();
			} catch (IOException e1) {
			}
		}
	}
	
	protected void logInfo(String info, Object... args){
		if( args.length > 0 ){
			String str = String.format(info, args);
			str = String.format("[%s]INFO %s", Tools.getFormatTime("HH:mm:ss SSS", System.currentTimeMillis()), str);
			trans.append("\r\n");
			trans.append(str);
		}
		else {
			trans.append("\r\n");
			trans.append(info);
		}
	}

	public String getBgcolor() {
		return bgcolor;
	}
	public void setFormmode(String formmode) {
		this.formmode = formmode;
	}
}
