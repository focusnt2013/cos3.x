package com.focus.cos.web.dev.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.data.Stat;
import org.directwebremoting.WebContextFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import redis.clients.jedis.Jedis;

import com.focus.cos.api.LogSeverity;
import com.focus.cos.web.common.AjaxResult;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.service.SvrMgr;
import com.focus.util.Base64X;
import com.focus.util.IOHelper;
import com.focus.util.QuickSort;
import com.focus.util.Tools;
import com.focus.util.XMLParser;
import com.focus.util.Zookeeper;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class DiggConfigMgr extends SvrMgr
{
	private static final Log log = LogFactory.getLog(DiggConfigMgr.class);
	/**
	 * 设置模板的API配置
	 * @param id
	 * @param security
	 * @param remark
	 * @param status 设置的接口状态，开通关闭接口也调用该方法
	 * @return
	 */
	public synchronized AjaxResult<String> setTemplateApi(
			String gridxml,
			String apiremark,
			String security,
			int status)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			Zookeeper zookeeper = getZookeeper();
			JSONObject token = null;
			if( !security.isEmpty() ){
				token = zookeeper.getJSONObject("/cos/config/security/"+security);
				if( token == null ){
					throw new Exception("未知的GRID-DIGG安全令牌配置["+security+"]");
				}
			}
			String logtips = "";
			String zkpath = gridxml+"/api.json";
    		if( gridxml.startsWith("/grid/local") )
			{
				zkpath = "/cos/config"+gridxml+"/api.json";
			}
			JSONObject api = zookeeper.getJSONObject(zkpath);
			org.directwebremoting.WebContext web = WebContextFactory.get(); 
			HttpServletRequest request = web.getHttpServletRequest();
			String apiurl = Kit.URL_PATH(request)+"diggapi?gridxml="+gridxml;
			if( api == null ){
				api = new JSONObject();
				api.put("setsecurity", security==null?"":security);//0表示不要安全配置，1表示要安全令牌
				api.put("remark", apiremark);
				logtips = "申请开通模板["+gridxml+"]的数据接口，需要通过系统管理员审批开通生效。";
				api.put("tips", String.format("%s申请开通接口", super.getAccountName()));
				api.put("status", 0);//待开通接口
				if( token != null ) api.put("settoken", token);
				zookeeper.create(zkpath, api.toString().getBytes("UTF-8"));
				super.logoper(LogSeverity.INFO, "申请开通模板["+gridxml+"]接口", "DIGG接口管理");
			}
			else {
				int _status = api.getInt("status");
				switch(status){
				case 0:
					api.put("tips", String.format("%s申请开通接口", super.getAccountName()));
					logtips = "在待开通状态修改模板["+gridxml+"]的数据接口的参数，需要通过系统管理员审批开通生效。";
					break;
				case 1:
					api.put("tips", "接口被系统管理员开通");
					if( status == _status ){
						logtips = "在开通状态修改了模板["+gridxml+"]的数据接口的参数";
					}
					else{
						logtips = String.format("开通了模板[%s]的数据接口，模板数据已经可以通过URL地址[%s]获取。", gridxml, apiurl);
					}
					break;
				case 2:
					api.put("tips", "接口被系统管理员关闭");
					if( status == _status ){
						logtips = "在关闭状态修改了模板["+gridxml+"]接口的参数";
					}
					else{
						logtips = "关闭了模板["+gridxml+"]的数据接口访问，模板数据";
						logtips = String.format("关闭了模板[%s]的数据接口，模板数据通过URL地址[%s]获取已经被禁止。", gridxml, apiurl);
					}
					break;
				}
				if( super.getAccountRole() == 1 ) {
					api.put("status", status);//如果是系统管理员直接生效
					api.put("security", security==null?"":security);//0表示不要安全配置，1表示要安全令牌
					if( api.has("setsecurity") ){
						api.remove("setsecurity");
					}
					if( api.has("settoken") ){
						api.remove("settoken");
					}
					if( status == _status ){
						logtips += "，参数配置已生效。";
					}
				}
				else {
					if( status > 0 ){
						api.put("tips", String.format("%s申请修改接口参数", super.getAccountName()));
						logtips += "，需要通过系统管理员审批处理。";
					}
					api.put("setsecurity", security==null?"":security);//0表示不要安全配置，1表示要安全令牌
					if( token != null ) api.put("settoken", token);
				}
				api.put("remark", apiremark);
				if( token != null ) api.put("token", token);

        		if( api.has("security") ){
        			security = api.getString("security");
            		if( !security.isEmpty() ){
            			token = getZookeeper().getJSONObject("/cos/config/security/"+security);
            			if( token != null ){
            				api.put("token", token);
            			}
            		}
        		}
				zookeeper.setJSONObject(zkpath, api);
			}
			rsp.setMessage(logtips);
			super.logoper(logtips, "DIGG接口管理", api.toString(4), "diggcfg!datastyle.action?gridxml="+gridxml);
			rsp.setSucceed(true);
			rsp.setResult(api.toString(4));
		}
		catch(Exception e)
		{
			log.error("Failed to config the digg-api of "+gridxml, e);
			String s = "配置模板["+gridxml+"]数据接口异常"+e;
			super.logoper(s, "DIGG接口管理", e);
			rsp.setMessage(s);
		}
		return rsp;		
	}
	/**
	 * 改变模板的目录
	 * @param type
	 * @param srcId
	 * @param targetId
	 * @param sysid
	 * @return
	 */
	public synchronized AjaxResult<String> dragDropTemplate(
			String type,
			String srcId,
			String targetId,
			String sysid)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			if( targetId.startsWith(srcId) )
			{
				rsp.setSucceed(true);
			}
			else if( "inner".equals(type) )
			{
				String zkpath = srcId;
				byte[] payload = getZookeeper().getData(zkpath);
				int i = srcId.lastIndexOf("/");
				String filename = srcId.substring(i+1);
				String newzkpath = targetId+"/"+filename;
				if( getZookeeper().exists(newzkpath) != null )
				{
					rsp.setMessage("在相同根目录下不允许有相同名称("+filename+")配置的模板");
				}
				else
				{
					getZookeeper().createNode(newzkpath, payload);
					List<String> list = getZookeeper().getChildren(zkpath);
					for(String timeline : list )
					{
						JSONObject version = getZookeeper().getJSONObject(zkpath+"/"+timeline);
						timeline = newzkpath +"/"+ timeline;
						getZookeeper().createObject(timeline, version);
					}
					rsp.setSucceed(true);
					rsp.setResult(newzkpath);
					getZookeeper().deleteNode(srcId);
				}
			}
			else
			{
				rsp.setMessage("不能拖拽移动模板到除了目录之外的其它未知");
			}
		}
		catch(Exception e)
		{
			String s = "拖拽移动模板["+srcId+"]出现异常"+e;
			logoper(s, "开发管理", e);
			rsp.setMessage(s);
			log.error("Failed to drag&drop the template of digg for exception", e);
		}
		return rsp;		
	}
	
	public String getDiggType(String src) throws Exception
	{
		if( src.startsWith("/cos/config/modules/") )
    	{
    		JSONObject datasource = getZookeeper().getJSONObject(src, true);
    		String dbtype = datasource.getString("dbtype");
    		if( "mongo".equals(dbtype) )
    		{
    			return dbtype;
    		}
    		else
    		{
    			return "sql";
    		}
    	}
		return "?";
	}

	/**
	 * 创建元数据基础模板 
	 * @param json
	 * @return
	 */
	public AjaxResult<String> createQueryTemplate(String json)
	{
		AjaxResult<String> result = new AjaxResult<String>();
		try
		{
			JSONObject template = new JSONObject(json);
			JSONArray columns = template.has("grid")?template.getJSONArray("grid"):new JSONArray();
			StringBuffer xml = new StringBuffer("<?xml version='1.0' encoding='UTF-8'?>");
			String recIndx = template.has("recIndx")?template.getString("recIndx"):"%recIndx%";
			xml.append("\r\n<x type='"+template.getString("type")+"' title='"+template.getString("title")+"' version='0.0.0.0' remark='' developer='"+super.getAccountName()+"'><!-- 注意设置表格中的主键字段recIndx -->");
			xml.append("\r\n\t<grid freezeCols='0' snapshot='false' recIndx='"+recIndx+"' showTitle='false'>");
			int firstColumnType = template.has("firstColumnType")?template.getInt("firstColumnType"):0;
			if( firstColumnType == 1 ) xml.append("\r\n\t\t<cell type='detail' editable='false' width='27' minWidth='27' resizable='false'/>");
			else if( firstColumnType == 2 ) xml.append("\r\n\t\t<cell type='checkBoxSelection' dataIndx='rowCheck' width='30' minWidth='30' align='center' cls='ui-state-default' resizable='false'/>");
			this.setGridTemplate(xml, columns);
			xml.append("\r\n\t</grid>");
			String datamodel = template.has("datamodel")?template.getString("datamodel"):"";
			if( "digg".equalsIgnoreCase(datamodel) )
			{
				String src = template.getString("src");
				xml.append("\r\n\t<datamodel type='"+datamodel+"' pagesize='"+template.getString("pagesize")+"'><!--数据模型定义: 如果类型是digg那么location和sorting默认就是remote，数据根据digg配置而来-->");
	    		JSONObject datasource = getZookeeper().getJSONObject(src, true);
	    		String dbtype = datasource.getString("dbtype");
	    		if( "mongo".equals(dbtype) )
	    		{
					xml.append("\r\n\t\t<digg type='mongo' src='"+src+"' database='"+datasource.getString("dbname")+"' from='"+template.getString("from")+"'>");
	    		}
	    		else
	    		{
					xml.append("\r\n\t\t<digg type='sql' src='"+src+"' from='"+template.getString("from")+"'>");
	    		}
				xml.append("\r\n\t\t</digg>");
			}
			else if( "json".equalsIgnoreCase(datamodel) )
			{
				xml.append("\r\n\t<datamodel type='json' value='"+template.getString("jsonurl")+"' pagesize='"+template.getString("pagesize")+"'><!--数据模型定义: 如果类型是digg那么location和sorting默认就是remote，数据根据digg配置而来-->");
			}
			else if( "zookeeper".equalsIgnoreCase(datamodel) )
			{
				xml.append("\r\n\t<datamodel type='zookeeper' value='"+template.getString("zkpath")+"' mode='"+template.getString("zkmode")+"' encrypte='"+template.getString("encrypte")+"'><!--数据模型定义: 如果类型是digg那么location和sorting默认就是remote，数据根据digg配置而来-->");
			}
			xml.append("\r\n\t</datamodel>");
			xml.append("\r\n\t<selection type='row' mode='range'/>");
			if( firstColumnType == 1 )
			{
				xml.append("\r\n\t<details><!-- 表格中首列字段类型type=detail必须定义该节点 -->");
				xml.append("\r\n\t</details>");
			}
			this.setQueryTemplate(xml);
			xml.append("\r\n</x>");
			result.setSucceed(true);
			result.setResult(xml.toString());
		}
		catch(Exception e)
		{
			result.setMessage("创建元数据查询模板出现异常:"+e.getMessage());
		}
		return result;
	}
	
	private void setDetailsTemplate(StringBuffer xml)
	{
		xml.append("\r\n\t<details><!-- 表格中首列字段类型type=detail必须定义该节点 -->");
		xml.append("\r\n\t</details>");
	}

	/**
	 * 
	 * @param xml
	 * @param firstColumnType
	 * @param columns
	 */
	private void setGridTemplate(StringBuffer xml, JSONArray columns)
	{
		for(int i = 0; i < columns.length(); i++)
		{
			JSONObject e = columns.getJSONObject(i);
			String dataType = e.has("dataType")?("dataType='"+e.getString("dataType")+"'"):"";
			String title = e.has("title")?("title='"+Tools.getJSONValue(e.getString("title"))+"'"):"";
			String dataIndx = e.has("dataIndx")?("dataIndx='"+Tools.getJSONValue(e.getString("dataIndx"))+"'"):"";
			String sort = e.has("sort")?("sort='"+e.getString("sort")+"'"):"";
			String width = e.has("width")?("width='"+Tools.getJSONValue(e.getString("width"))+"'"):"";
			String align = e.has("align")?e.getString("align"):"";
			if( "true".equals(align) )
			{
				align = "align='center'";
			}
			else align = "";
			String hidden = e.has("hidden")?e.getString("hidden"):"";
			if( "true".equals(hidden) )
			{
				hidden = "hidden='true'";
			}
			else hidden = "";
			String editable = e.has("editable")?e.getString("editable"):"";
			int _editable = 0;
			if(editable.equals("true")) editable = "editable='"+editable+"'";
			else if( "function".equals(editable) )
			{
				_editable = 1;
				editable = "";
			}
			else if( "javascript".equals(editable) )
			{
				_editable = 2;
				editable = "";
			}
			String filter = e.has("filter")?e.getString("filter"):"";
			String render = e.has("render")?e.getString("render"):"";
			String editor = e.has("editor")?e.getString("editor"):"";
			
			xml.append("\r\n\t\t<cell "+dataType+" "+title+" "+dataIndx+" "+sort+" "+editable+" "+width+" "+align+" "+hidden+">");
			if( _editable > 0 )
			{
				if( _editable == 1 ) xml.append("\r\n\t\t\t<editable function='%函数名称在javascript区域实现%'/>");
				else{
					xml.append("\r\n\t\t\t<editable><![CDATA[");
					xml.append("\r\n\t\t\t\treturn false;");
					xml.append("\r\n\t\t\t]]></editable>");
				}
			}
			if( "textbox".equalsIgnoreCase(filter) )
			{
				xml.append("\r\n\t\t\t<filter type='textbox' condition='contain' listeners='change'/>");
			}
			else if( "select".equalsIgnoreCase(filter) )
			{
				xml.append("\r\n\t\t\t<filter type='select' condition='equal' listeners='change' style='height:20px;width:72px;'>");
				xml.append("\r\n\t\t\t\t<option value='0'></option>");
				xml.append("\r\n\t\t\t\t<option value='1'>值1</option>");
				xml.append("\r\n\t\t\t\t<option value='2'>值2</option>");
				xml.append("\r\n\t\t\t</filter>");
			}
			else if( "checkbox".equalsIgnoreCase(filter) )
			{
				xml.append("\r\n\t\t\t<filter type='select' init='pqCheckboxPicker' condition='range' listeners='change' attr='multiple' style='height:20px;width:72px;'>");
				xml.append("\r\n\t\t\t\t<option value='0'></option>");
				xml.append("\r\n\t\t\t\t<option value='1'>值1</option>");
				xml.append("\r\n\t\t\t\t<option value='2'>值2</option>");
				xml.append("\r\n\t\t\t</filter>");
			}
			else if( "datebetween".equalsIgnoreCase(filter) )
			{
				xml.append("\r\n\t\t\t<filter type='textbox' init='pqDatePicker' condition='between' listeners='change'/>");
			}
			else if( "date".equalsIgnoreCase(filter) )
			{
				xml.append("\r\n\t\t\t<filter type='textbox' init='pqDatePicker' condition='equal' listeners='change'/>");
			}
			if( "javascript".equalsIgnoreCase(render) )
			{
				xml.append("\r\n\t\t\t<render><![CDATA[");
				xml.append("\r\n\t\t\tfunction(ui){");
				xml.append("\r\n\t\t\t\tvar rowData = ui.rowData;");
				xml.append("\r\n\t\t\t\treturn \"<img src='images/icons/label_todo.png'/>\";");
				xml.append("\r\n\t\t\t}");
				xml.append("\r\n\t\t\t]]></render>");
			}
			else if( "function".equalsIgnoreCase(render) )
			{
				xml.append("\r\n\t\t\t<render type='"+render+"'/>");
			}
			
			if( "select".equalsIgnoreCase(editor) )
			{
				xml.append("\r\n\t\t\t<editor type='select'>");
				xml.append("\r\n\t\t\t\t<option value='0'></option>");
				xml.append("\r\n\t\t\t\t<option value='1'>值1</option>");
				xml.append("\r\n\t\t\t\t<option value='2'>值2</option>");
				xml.append("\r\n\t\t\t</editor>");
			}
			else if( "checkbox".equalsIgnoreCase(editor) )
			{
				xml.append("\r\n\t\t\t<editor type='checkbox' style='margin:6px 5px;'/>");
			}
			else if( "dateEditor".equalsIgnoreCase(editor) )
			{
				xml.append("\r\n\t\t\t<editor type='dateEditor'/>");
			}
			else if( "textbox".equalsIgnoreCase(editor) )
			{
				xml.append("\r\n\t\t\t<editor type='textbox'/>");
			}
			xml.append("\r\n\t\t</cell>");
		}
	}
	
	private void setGridTemplate(StringBuffer xml, int firstColumnType)
	{
		xml.append("\r\n\t<grid freezeCols='0' snapshot='false' recIndx='%recIndx%' showTitle='false'>");
		if( firstColumnType == 1 ) xml.append("\r\n\t\t<cell type='detail' editable='false' width='27' minWidth='27' resizable='false'/>");
		else if( firstColumnType == 2 ) xml.append("\r\n\t\t<cell type='checkBoxSelection' dataIndx='rowCheck' width='30' minWidth='30' align='center' cls='ui-state-default' resizable='false'/>");
		
		xml.append("\r\n\t\t<cell dataType='string' title='时间' dataIndx='%dataIndx1%' sort='down' editable='false' width='168'>");
		xml.append("\r\n\t\t\t<filter type='textbox' init='pqDatePicker' condition='between' listeners='change'/>");
		xml.append("\r\n\t\t</cell>");

		xml.append("\r\n\t\t<cell dataType='string' title='%title2%' dataIndx='%dataIndx2%' editable='false' width='100' align='center'>");
		xml.append("\r\n\t\t\t<filter type='select' init='pqCheckboxPicker' condition='range' listeners='change' attr='multiple' style='height:20px;width:72px;'>");
		xml.append("\r\n\t\t\t\t<option value='0'></option>");
		xml.append("\r\n\t\t\t\t<option value='1'>值1</option>");
		xml.append("\r\n\t\t\t\t<option value='2'>值2</option>");
		xml.append("\r\n\t\t\t</filter>");
		xml.append("\r\n\t\t</cell>");
		
		xml.append("\r\n\t\t<cell dataType='string' title='%title3%' dataIndx='%dataIndx3%' editable='false' width='100' align='center'>");
		xml.append("\r\n\t\t\t<filter type='select' condition='equal' listeners='change' style='height:20px;width:72px;'/>");
		xml.append("\r\n\t\t\t</label>");
		xml.append("\r\n\t\t\t\t<option value='0'></option>");
		xml.append("\r\n\t\t\t\t<option value='1'>值1</option>");
		xml.append("\r\n\t\t\t\t<option value='2'>值2</option>");
		xml.append("\r\n\t\t\t</label>");
		xml.append("\r\n\t\t</cell>");

		xml.append("\r\n\t\t<cell dataType='string' title='%title4%' dataIndx='%dataIndx4%' editable='false' width='100' align='center'>");
		xml.append("\r\n\t\t\t<filter type='select' condition='equal' listeners='change' style='height:20px;width:72px;'/>");
		xml.append("\r\n\t\t\t</label>");
		xml.append("\r\n\t\t\t\t<option value='0'></option>");
		xml.append("\r\n\t\t\t\t<option value='1'>值1</option>");
		xml.append("\r\n\t\t\t\t<option value='2'>值2</option>");
		xml.append("\r\n\t\t\t</label>");
		xml.append("\r\n\t\t</cell>");

		xml.append("\r\n\t\t<cell dataType='string' title='%title3%' dataIndx='%dataIndx3%' editable='false' width='64' align='center'>");
		xml.append("\r\n\t\t\t<render><![CDATA[");
		xml.append("\r\n\t\t\tfunction(ui){");
		xml.append("\r\n\t\t\t\tvar rowData = ui.rowData;");
		xml.append("\r\n\t\t\t\treturn \"<img src='images/icons/label_todo.png'/>\";");
		xml.append("\r\n\t\t\t}");
		xml.append("\r\n\t\t\t]]></render>");
		xml.append("\r\n\t\t</cell>");
		
		xml.append("\r\n\t\t<cell title='分组标题' align='center'>");
		xml.append("\r\n\t\t\t<cell dataType='string' title='国家' dataIndx='country' minWidth='80'/>");
		xml.append("\r\n\t\t\t<cell dataType='string' title='省' dataIndx='province' minWidth='80'/>");
		xml.append("\r\n\t\t\t<cell dataType='string' title='市' dataIndx='city' minWidth='80'/>");
		xml.append("\r\n\t\t</cell>");
		
		xml.append("\r\n\t</grid>");
	}
	
	private void setQueryTemplate(StringBuffer xml)
	{
		xml.append("\r\n\t<toolbar>");
		xml.append("\r\n\t\t<!--<button icon='ui-icon-plus' subject='功能按钮名'><![CDATA[//功能按钮自定义的点击事件脚本，可对表格中的行数据方法调用");
		xml.append("\r\n\t\t]]></button>-->");
		xml.append("\r\n\t</toolbar>");
		xml.append("\r\n\t<javascript><![CDATA[//用户自定义脚本，可实现TOP栏的功能按钮点击事件的功能方法 ");
		xml.append("\r\n\t]]></javascript>");
		xml.append("\r\n\t<globalscript><![CDATA[//全局的自定义脚本，可实现对表格单位中链接或按钮等函数调用 ");
		xml.append("\r\n\t]]></globalscript>");
	}
	/**
	 * 得到模板数据
	 * @param id
	 * @return
	 */
	public AjaxResult<String> getTemplateXml(String path)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			InputStream is;
			if( path.startsWith("/grid/local") )
    		{
				is = this.getClass().getResourceAsStream(path);
    		}
			else
			{
				Stat stat = getZookeeper().exists(path);
				is = new ByteArrayInputStream(getZookeeper().getData(path, false,stat));
				rsp.setTimestamp(stat.getMtime());
			}
			rsp.setResult(new String(IOHelper.readAsByteArray(is), "UTF-8"));
			rsp.setSucceed(true);
		}
		catch(Exception e){
			log.error("Failed to get the template from "+path, e);
			rsp.setMessage("从配置路径【"+path+"】读取元数据模板出现异常"+e.getMessage());
		}
    	return rsp;
	}
	/**
	 * 创建元数据查询通过数据源
	 * @param title
	 * @param firstColumnType
	 * @param src
	 * @param from
	 * @param pagesize 分页单页大小 
	 * @param json
	 * @return
	 */
	public AjaxResult<String> createQueryTemplateByDatasource(String title, int firstColumnType, String src, String from, int pagesize, String json)
	{
		AjaxResult<String> result = new AjaxResult<String>();
		try
		{
			JSONArray columns = new JSONArray(json);
			if( columns.length() == 0 )
			{
				result.setMessage("没有选择数据源字段无法创建元数据查询");
				return result;
			}
			StringBuffer xml = new StringBuffer("<?xml version='1.0' encoding='UTF-8'?>");
			xml.append("\r\n<x type='query' title='' version='0.0.0.0' remark='' developer='"+super.getAccountName()+"'>");
			xml.append("\r\n\t<grid freezeCols='0' snapshot='false' recIndx='%recIndx%' showTitle='false'>");
			String groupby = "";
			JSONArray detials = new JSONArray();
			for(int i = 0; i < columns.length(); i++)
			{
				JSONObject e = columns.getJSONObject(i);
				if( e.has("detial") )
				{
					detials.put(e);
				}
				if( e.has("groupby") )
				{
					String dataIndx = e.getString("COLUMN_NAME");
					if( !groupby.isEmpty() ) groupby += ",";
					groupby += dataIndx;
				}
			}
			if( detials.length() > 0 ) xml.append("\r\n\t\t<cell type='detail' editable='false' width='27' minWidth='27' resizable='false'/>");
			else if( firstColumnType == 2 ) xml.append("\r\n\t\t<cell type='checkBoxSelection' dataIndx='rowCheck' width='30' minWidth='30' align='center' cls='ui-state-default' resizable='false'/>");
			JSONArray digg = new JSONArray();
			for(int i = 0; i < columns.length(); i++)
			{
				JSONObject e = columns.getJSONObject(i);
				String dataIndx = e.getString("COLUMN_NAME");
				String dataType = "";//e.getString("TYPE_NAME");
				int type = e.getInt("DATA_TYPE");
    			JSONObject obj = new JSONObject();
    			obj.put("name", dataIndx);
    			digg.put(obj);
		    	switch( type )
		    	{
		    		case java.sql.Types.CLOB:
		    		case java.sql.Types.BLOB:
		    			break;
		    		case java.sql.Types.TIMESTAMP:
		    		case java.sql.Types.DATE:
		    			obj.put("format", "yyyy-MM-dd HH:mm:ss");
		    			dataType = "time";
		    			break;
		    		case java.sql.Types.VARCHAR:
		    			dataType = "string";
		    			break;
		    		case java.sql.Types.BOOLEAN:
		    			dataType = "boolean";
		    			break;
		    		case java.sql.Types.BIGINT:
		    			dataType = "long";
		    			break;
		    		case java.sql.Types.CHAR:
		    		case java.sql.Types.INTEGER:
		    			dataType = "int";
		    			break;
		    		case java.sql.Types.DECIMAL:
		    		case java.sql.Types.DOUBLE:
		    		case java.sql.Types.FLOAT:
		    		case java.sql.Types.NUMERIC:
		    		case java.sql.Types.NULL:
		    			dataType = "number";
		    			break;
		    		default:// java.sql.Types.VARCHAR:
		    			dataType = "string";
		    			break;
		    	}
				xml.append("\r\n\t\t<cell dataType='"+dataType+"' title='"+dataIndx+"' dataIndx='"+dataIndx+"' editable='false' width='100' minWidth='100'>");
				if( "string".equals(dataType) )
				{
					xml.append("\r\n\t\t\t<filter type='textbox' condition='contain' listeners='change'/>");
				}
				else if( "int".equals(dataType) )
				{
					xml.append("\r\n\t\t\t<filter type='select' init='pqCheckboxPicker' condition='range' listeners='change' attr='multiple' style='height:20px;width:72px;'/>");
					xml.append("\r\n\t\t\t<label>\r\n\t\t\t\t<option value='0'>?</option>\r\n\t\t\t</label>");
				}
				else if( "time".equals(dataType) )
				{
					xml.append("\r\n\t\t\t<filter type='textbox' init='pqDatePicker' condition='between' listeners='change'/>");
				}
				xml.append("\r\n\t\t</cell>");
			}
			xml.append("\r\n\t</grid>");
			xml.append("\r\n\t<datamodel sorting='remote' location='remote' pagesize='50' type='digg'><!--数据模型定义: 如果类型是digg那么location和sorting默认就是remote，数据根据digg配置而来-->");
			if( groupby.isEmpty() )	xml.append("\r\n\t\t<digg type='sql' src='"+src+"' from='"+from+"'>");
			else xml.append("\r\n\t\t<digg type='sql' src='"+src+"' from='"+from+"' groupby='"+groupby+"'>");
			for(int i = 0; i < digg.length(); i++)
			{
				JSONObject e = digg.getJSONObject(i);
				String dataIndx = e.getString("name");
				String format = e.has("format")?e.getString("format"):"";
				format = format.isEmpty()?format:("format='"+format+"'");
				xml.append("\r\n\t\t\t<"+dataIndx+" column='"+dataIndx+"' "+format+"/>");
				xml.append("\r\n\t\t\t</"+dataIndx+">");
			}
			xml.append("\r\n\t\t</digg>");
			xml.append("\r\n\t</datamodel>");
			xml.append("\r\n\t<selection type='row' mode='range'/>");
			if( detials.length() > 0 )
			{
				xml.append("\r\n\t<details><!-- 表格中首列字段类型type=detail必须定义该节点 -->");
				xml.append("\r\n\t</details>");
			}
			this.setQueryTemplate(xml);
			xml.append("\r\n</x>");
			result.setSucceed(true);
			result.setResult(xml.toString());
		}
		catch(Exception e)
		{
			result.setMessage("创建元数据查询出现异常:"+e.getMessage());
		}
		return result;
	}
	/**
	 * 创建针对
	 * @param title
	 * @param firstColumnType
	 * @param zkpath
	 * @param encrypte 是否加密
	 * @param mode 数据模式 0表示zkpath下单独的配置节点。 1表示zkpath本身是一个jsonarray，新增修改删除需要完整的考虑
	 * @return
	 */
	public AjaxResult<String> createQueryTemplateByZookeeper(String title, String zkpath, boolean encrypte, int mode)
	{
		AjaxResult<String> result = new AjaxResult<String>();
		try
		{
			StringBuffer xml = new StringBuffer("<?xml version='1.0' encoding='UTF-8'?>");
			xml.append("\r\n<x type='query' title='"+title+"' version='0.0.0.0' developer='"+super.getAccountName()+"' remark=''>");
			xml.append("\r\n\t<datamodel type='zookeeper' sorting='local' value='"+zkpath+"' encrypte='"+encrypte+"' mode='"+mode+"'><!--数据模型定义: zookeeper类型必须提供zookeeper的节点地址，动态地址按照%参数%通配设置，调用模板时候通过url传入通配参数替换即可-->");
			xml.append("\r\n\t<![CDATA[//可以添加当数据完成显示后回调的javascript脚本 ");
			xml.append("\r\n\t]]>");
			xml.append("\r\n\t</datamodel>");
			xml.append("\r\n\t<selection type='row' mode='range'/>");
			this.setQueryTemplate(xml);
			xml.append("\r\n</x>");
			result.setSucceed(true);
			result.setResult(xml.toString());
		}
		catch(Exception e)
		{
			result.setMessage("创建元数据查询出现异常:"+e.getMessage());
		}
		return result;
	}
	/**
	 * 创建json元数据查询（查询模板也可通过脚本实现对数据的修改)
	 * @param title
	 * @param firstColumnType
	 * @param url
	 * @param pagesize
	 * @return
	 */
	public AjaxResult<String> createQueryTemplateByJson(String title, int firstColumnType, String url, int pagesize)
	{
		AjaxResult<String> result = new AjaxResult<String>();
		try
		{
			StringBuffer xml = new StringBuffer("<?xml version='1.0' encoding='UTF-8'?>");
			xml.append("\r\n<x type='query' title='"+title+"' version='0.0.0.0' developer='"+super.getAccountName()+"' remark=''>");
			this.setGridTemplate(xml, firstColumnType);
			String sorting = pagesize==0?"local":"remote";
			xml.append("\r\n\t<datamodel type='json' sorting='"+sorting+"' value='"+url+"' pagesize='"+pagesize+"'><!--数据模型定义: json类型必须提供JSON的URL地址，pagesize如果为0表示不翻页本地排序-->");
			xml.append("\r\n\t<![CDATA[//可以添加当数据完成显示后回调的javascript脚本 ");
			xml.append("\r\n\t]]>");
			xml.append("\r\n\t</datamodel>");
			xml.append("\r\n\t<selection type='row' mode='range'/>");
			if( firstColumnType == 1 )
			{
				setDetailsTemplate(xml);
			}
			this.setQueryTemplate(xml);
			xml.append("\r\n</x>");
			result.setSucceed(true);
			result.setResult(xml.toString());
		}
		catch(Exception e)
		{
			result.setMessage("创建元数据查询出现异常:"+e.getMessage());
		}
		return result;
	}
	/**
	 * 创建元数据配置模板
	 * @param title
	 * @param zkpath
	 * @param encrypte 是否加密
	 * @param mode 数据模式 0表示zkpath下单独的配置节点。 1表示zkpath本身是一个jsonarray，新增修改删除需要完整的考虑
	 * @return
	 */
	public AjaxResult<String> createConfigTemplateByZookeeper(String title, String zkpath, boolean encrypte, int mode)
	{
		AjaxResult<String> result = new AjaxResult<String>();
		try
		{
			StringBuffer xml = new StringBuffer("<?xml version='1.0' encoding='UTF-8'?>");
			xml.append("\r\n<x type='edit' title='"+title+"' version='0.0.0.0' remark='' developer='"+super.getAccountName()+"'>");
			xml.append("\r\n\t<grid freezeCols='0' snapshot='false' recIndx='%recIndx%' showTitle='false'>");
			xml.append("\r\n\t\t<cell title='操作' width='144' vaglin='middle'>");
			xml.append("\r\n\t\t\t<button type='editRow' style='height: 22px;' subject='修改'/>");
			xml.append("\r\n\t\t\t<button type='deleteRow' style='height: 22px;' subject='删除'/>");
			xml.append("\r\n\t\t</cell>");
			
			xml.append("\r\n\t\t<cell dataType='string' title='%title1%' dataIndx='%dataIndx1%' editable='true' minWidth='100' nullable='false'>");
			xml.append("\r\n\t\t\t<validations>");
			xml.append("\r\n\t\t\t<valid type='minLen' value='2'/>");
			xml.append("\r\n\t\t\t<valid type='maxLen' value='64'/>");
			xml.append("\r\n\t\t</validations>");
			xml.append("\r\n\t\t</cell>");

			xml.append("\r\n\t\t<cell dataType='string' title='%title2%' dataIndx='%dataIndx2%' editable='true' minWidth='100' nullable='false'>");
			xml.append("\r\n\t\t\t<validations>");
			xml.append("\r\n\t\t\t<valid type='regexp' msg='必须是字母数字以及符号'><![CDATA[");
			xml.append("\r\n\t\t\t^([A-Za-z])|([a-zA-Z0-9])|([a-zA-Z0-9])|([a-zA-Z0-9_])+$");
			xml.append("\r\n\t\t\t]]></valid>");
			xml.append("\r\n\t\t\t<valid type='minLen' value='2'/>");
			xml.append("\r\n\t\t\t<valid type='maxLen' value='64'/>");
			xml.append("\r\n\t\t</validations>");
			xml.append("\r\n\t\t</cell>");

			xml.append("\r\n\t\t<cell dataType='string' title='%title3%' dataIndx='%dataIndx3%' editable='true' minWidth='100' nullable='false'>");
			xml.append("\r\n\t\t\t<validations>");
			xml.append("\r\n\t\t\t<valid type='host' msg=''/><!-- 校验类型包括host ip url subject等 -->");
			xml.append("\r\n\t\t</validations>");
			xml.append("\r\n\t\t</cell>");
			
			xml.append("\r\n\t\t<cell dataType='int' title='%title4%' dataIndx='%title4%' editable='true' width='100' nullable='false'>");
			xml.append("\r\n\t\t\t<editor type='select'>");
			xml.append("\r\n\t\t\t\t<option value='0'></option>");
			xml.append("\r\n\t\t\t\t<option value='1'>值1</option>");
			xml.append("\r\n\t\t\t\t<option value='2'>值2</option>");
			xml.append("\r\n\t\t\t</editor>");
			xml.append("\r\n\t\t</cell>");

			xml.append("\r\n\t\t<cell dataType='string' title='%title5%' dataIndx='%dataIndx5%' editable='false' minWidth='22' nullable='false'>");
			xml.append("\r\n\t\t\t<render><![CDATA[");
			xml.append("\r\n\t\t\tfunction(ui){");
			xml.append("\r\n\t\t\t\tvar rowData = ui.rowData;");
			xml.append("\r\n\t\t\t\treturn \"<img src='images/icons/label_todo.png'/>\";");
			xml.append("\r\n\t\t\t}");
			xml.append("\r\n\t\t\t]]></render>");
			xml.append("\r\n\t\t</cell>");

			xml.append("\r\n\t\t<cell title='%title6%' dataIndx='%dataIndx6%' editable='false' minWidth='22'>");
			xml.append("\r\n\t\t\t<button class='query_btn' type='inner' icon='ui-icon-link'><![CDATA[//rowData对象为当前行的数据，以下直接实现按钮事件的脚本");
			xml.append("\r\n\t\t\t]]></button>");
			xml.append("\r\n\t\t</cell>");

			xml.append("\r\n\t\t<cell dataType='int' title='%title7%' dataIndx='%title7%' editable='true' width='100' nullable='false'>");
			xml.append("\r\n\t\t\t<editor type='select'>");
			xml.append("\r\n\t\t\t\t<zookeeper key='%key%' value='%value%' path='%zkpath%' decrypte='false' mode='2' returnpath='false'/><!--decrypte时间是否需要解密， mode=2表示zkpath对应一个json对象，returnpath表示返回路径结果 -->");
			xml.append("\r\n\t\t\t</editor>");
			xml.append("\r\n\t\t</cell>");

			xml.append("\r\n\t\t<cell dataType='int' title='%title8%' dataIndx='%title8%' editable='true' width='100' nullable='false'>");
			xml.append("\r\n\t\t\t<valid type='function(ui){ return checkPort(ui);}' msg='' icon='ui-icon-info'/>");
			xml.append("\r\n\t\t</cell>");
			
			xml.append("\r\n\t</grid>");
			xml.append("\r\n\t<datamodel  type='zookeeper' value='"+zkpath+"' encrypte='"+encrypte+"' mode='"+mode+"'/><!--编辑模式只开放zookeeper-->");
			xml.append("\r\n\t<selection type='row' mode='single'/>");
			xml.append("\r\n\t<toolbar>");
			xml.append("\r\n\t\t<button icon='ui-icon-plus' type='addRow' subject='新增'/>");
			xml.append("\r\n\t\t<button icon='ui-icon-plus' subject='功能按钮名'><![CDATA[//功能按钮自定义的点击事件脚本，可对表格中的行数据方法调用");
			xml.append("\r\n\t\t]]></button>");
			xml.append("\r\n\t</toolbar>");
			xml.append("\r\n\t<javascript><![CDATA[//用户自定义脚本，可实现TOP栏的功能按钮点击事件的功能方法 ");
			xml.append("\r\n\tfunction callbackBeforeCommit(rowData){//提交之前被被回调");
			xml.append("\r\n\t}");
			xml.append("\r\n\tfunction callbackSave(rowData){//配置保存后被回调");
			xml.append("\r\n\t}");
			xml.append("\r\n\t]]></javascript>");
			xml.append("\r\n\t<globalscript><![CDATA[//全局的自定义脚本，可实现对表格单位中链接或按钮等函数调用 ");
			xml.append("\r\n\t]]></globalscript>");
			xml.append("\r\n</x>");
			result.setSucceed(true);
			result.setResult(xml.toString());
		}
		catch(Exception e)
		{
			result.setMessage("创建元数据配置模板出现异常:"+e.getMessage());
		}
		return result;
	}

	/**
	 * 创建基于数据源的配置模板
	 * @param title
	 * @param src
	 * @param from
	 * @return
	 */
	public AjaxResult<String> createConfigTemplateByDatasource(String title, String src, String from)
	{
		return null;
	}
	
	/**
	 * 标注数据源指定字段
	 * @param id
	 * @param ds
	 * @param tablename
	 * @param column
	 * @param remark
	 * @return
	 */
	public boolean remarkDatasourceCell(String sysid, String ds, String tablename, String column, String remark)
	{
		String zkpath = "/cos/config/modules/"+sysid+"/datasource/"+ds+"/"+tablename;
		try
		{
			Zookeeper zookeeper = getZookeeper();
			JSONObject remarks = zookeeper.getJSONObject(zkpath);
			if( remarks != null )
			{
				remarks.put(column, remark);
				zookeeper.setString(zkpath, remarks.toString(4));
			}
			else
			{
				remarks = new JSONObject();
				remarks.put(column, remark);
				zookeeper.create(zkpath, remarks.toString(4).getBytes("UTF-8"));
			}
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
	/**
	 * 得到指定子系统的所有数据源配置
	 * @param id
	 * @return 以json字符串的格式返回
	 */
	public AjaxResult<String> getModuleDatasources(String sysid)
	{
		AjaxResult<String> result = new AjaxResult<String>();
		try
		{
			JSONArray localDataArray = getZookeeper().getJSONArray("/cos/config/modules/"+sysid+"/datasource", true);
			if( localDataArray.length() > 0 )
			{
//				for(int i = 0; i < localDataArray.length(); i++)
//				{
//					JSONObject e = localDataArray.getJSONObject(i);
//					String datatype = e.getString("dbtype");
//					String dbaddr = e.getString("dbaddr");
//					String dbname = e.getString("dbname");
//					String username = e.getString("dbusername");
//					String password = e.has("dbpassword")?e.getString("dbpassword"):"";
//					password = !password.isEmpty()?new String(Base64X.decode(password)):"";
//					if("mongo".equalsIgnoreCase(datatype))
//	    			{
//	    				this.setMongoTables(dbaddr, dbname, username, password, e);
//	    				continue;
//	    			}
//					else
//					{
//						String jdbcUrl = "";
//						String driverClass = "";
//						String jdbcUsername = username;
//						String jdbcUserpswd = password;
//		    			if("h2".equalsIgnoreCase(datatype))
//		    			{
//		    				driverClass = "org.h2.Driver";
//		    				jdbcUrl = "jdbc:h2:tcp://"+dbaddr+"/../h2/"+dbname;
//		    			}
//		    			else if("mysql".equalsIgnoreCase(datatype))
//		    			{
//		    				driverClass = "com.mysql.jdbc.Driver";
//		    				jdbcUrl = "jdbc:mysql://"+dbaddr+"/"+dbname+"?lastUpdateConnt=true&amp;useUnicode=true&amp;characterEncoding=UTF-8";
//		    			}
//		    			else if("oracle".equalsIgnoreCase(datatype))
//		    			{
//		    				driverClass = "oracle.jdbc.driver.OracleDriver";
//		    				jdbcUrl = "jdbc:oracle:thin:@"+dbaddr+":"+dbname;
//		    			}
//		    			else
//		    			{
//				        	continue;
//		    			}
//						this.setJdbcTables(driverClass, jdbcUrl, jdbcUsername, jdbcUserpswd, e);
//					}
//				}
			}
			result.setSucceed(true);
			result.setResult(localDataArray.toString());
		}
		catch(Exception e)
		{
			result.setMessage("获取系统["+sysid+"]数据源出现异常:"+e.getMessage());
		}
		return result;
	}
	
	/**
	 * 是否目录下有相同名称的目录
	 * @param zkpath
	 * @param name
	 * @return
	 * @throws Exception 
	 */
	private String homonymy(String zkpath, String name) throws Exception
	{
		if( getZookeeper().exists(zkpath) == null )
		{
			return null;
		}
		List<String> list = getZookeeper().getChildren(zkpath);
		for(String nodepath : list )
		{
			String path = zkpath + "/" + nodepath;
			String name0 = getZookeeper().getUTF8(path);
			if( name.equals(name0) ){
				return nodepath;
			}
		}
		return null;
	}

	/**
	 * 根据xml配置文件生成digg数据
	 * @param zkpath ZK路径
	 * @param xmlname XML文件名称
	 * @return
	 * @throws Exception
	 */
	public JSONObject getTemplate(String zkpath, String xmlname) throws Exception
	{
		Stat stat = getZookeeper().exists(zkpath);
		InputStream is = new ByteArrayInputStream(getZookeeper().getData(zkpath, false, stat));
		return this.getTemplate(is, zkpath, xmlname, stat.getCtime(), stat.getMtime());
	}
	/**
	 * 通过文件加载模版
	 * @param file
	 * @param path
	 * @return
	 * @throws Exception
	 */
	private JSONObject getTemplate(File file, String path) throws Exception
	{
		return this.getTemplate(new FileInputStream(file), getPath(path, file), file.getName(), file.lastModified(), file.lastModified());
	}
	/**
	 * 通过输入流加载模版
	 * @param is
	 * @param path
	 * @param xmlname
	 * @param timestamp
	 * @return
	 * @throws Exception
	 */
	public JSONObject getTemplate(InputStream is, String path, String xmlname, long ctime, long mtime) throws Exception
	{
		XMLParser xml = new XMLParser(is);
		String type = XMLParser.getElementAttr(xml.getRootNode(), "type");
		JSONObject e = new JSONObject();
		e.put("title", xmlname);
		e.put("template", true);
		e.put("name", xmlname);
		e.put("ename", xmlname);
		e.put("cname", XMLParser.getElementAttr(xml.getRootNode(), "title"));
		e.put("version", XMLParser.getElementAttr(xml.getRootNode(), "version"));
		e.put("developer", XMLParser.getElementAttr(xml.getRootNode(), "developer"));
		e.put("createtime", Tools.getFormatTime("yyyy-MM-dd HH:mm", ctime));
		e.put("modifytime", Tools.getFormatTime("yyyy-MM-dd HH:mm", mtime));
		e.put("timestamp", mtime);
		e.put("remark", XMLParser.getElementAttr(xml.getRootNode(), "remark"));
		e.put("id", path);
		Element dataNode = XMLParser.getChildElementByTag(xml.getRootNode(), "datamodel");
		if( dataNode == null )
		{
			throw new Exception("Unavailable digg("+path+") for not found datamodel.");
		}
		
		Element gridNode = XMLParser.getChildElementByTag(xml.getRootNode(), "grid");
		JSONObject grid = new JSONObject();
		this.loadTemplateGrid(gridNode, grid);
		JSONArray filters = new JSONArray();
		JSONArray pq_sort = new JSONArray();
		if( grid.has("children") ){
			JSONArray children = grid.getJSONArray("children");
			for(int i = 0; i < children.length(); i++){
				JSONObject c = children.getJSONObject(i);
				if(  c.has("groupname") && c.has("children") ){
					JSONArray subchild = c.getJSONArray("children");
					for(int j = 0; j < subchild.length(); j++){
						JSONObject c1 = subchild.getJSONObject(j);
						if( c1.has("filter") ){
							filters.put(this.getPqFilter(c1));
						}
						if( c1.has("sort") ){
							JSONObject s = new JSONObject();
							s.put("dataIndx", c1.getString("dataIndx"));
							s.put("dir", c1.getString("sort"));
							pq_sort.put(s);
						}
					}
				}
				else {
					if( c.has("filter") ){
						filters.put(this.getPqFilter(c));
					}
					if( c.has("sort") ){
						JSONObject s = new JSONObject();
						s.put("dataIndx", c.getString("dataIndx"));
						s.put("dir", c.getString("sort"));
						pq_sort.put(s);
					}
				}
			}
		}
		if( filters.length() > 0 ){
			JSONObject pq_filter = new JSONObject();
			pq_filter.put("data", filters);
			pq_filter.put("mode", "AND");
			e.put("pq_filter", pq_filter);
		}
		if( pq_sort.length() > 0 ){
			e.put("pq_sort", pq_sort);
		}
		e.put("datamodel", XMLParser.getElementAttr(dataNode, "type"));
		e.put("type", type);
		if( "edit".equalsIgnoreCase(type) )
		{
			e.put("icon", "images/ico/edit.png");
		}
		else if( "report".equalsIgnoreCase(type) )
		{
			e.put("icon", "images/ico/report.png");
		}
		else if( "form".equalsIgnoreCase(type) )
		{
			e.put("icon", "images/ico/templates.png");
		}
		else
		{
			e.put("icon", "images/ico/query.png");
		}
		return e;
	}
	/**
	 * 得到过滤对象
	 * @param c
	 * @return
	 */
	private JSONObject getPqFilter(JSONObject c){
		JSONObject f = new JSONObject();
		String dataType = c.getString("dataType");
		String condition = c.getJSONObject("filter").getString("condition");
		if( "range".equals(condition) ){
			f.put("value", new JSONArray());
		}
		else if( "between".equals(condition) ){
			f.put("value", "");
			f.put("value2", "");
		}
		else{
	    	if( "int".equalsIgnoreCase(dataType) ||
				"integer".equalsIgnoreCase(dataType) )
	    	{
	    		dataType = "integer";
				f.put("value", 0L);
	    	}
	    	else if( "long".equalsIgnoreCase(dataType) )
	    	{
	    		dataType = "long";
				f.put("value", 0L);
	    	}
	    	else if( "number".equalsIgnoreCase(dataType) ||
	    			 "float".equalsIgnoreCase(dataType) ||
	    			 "double".equalsIgnoreCase(dataType) )
	    	{
	    		dataType = "float";
				f.put("value", 0.0);
	    	}
	    	else {
				f.put("value", "");
	    	}
		}
		f.put("condition", condition);
		if( c.has("dataIndx") ){
			f.put("dataIndx", c.getString("dataIndx"));
		}
		f.put("dataType", dataType);
		return f;
	}
	/**
	 * 得到所有模板的路径
	 * @param dir
	 * @param path
	 * @param gridxmls
	 * @throws Exception 
	 */
	public void getAllTemplatePath(String zkpath, ArrayList<String> gridxmls) throws Exception
	{
		List<String> list = getZookeeper().getChildren(zkpath);
		for(String xml : list )
		{
			String subzkpath = zkpath+"/"+xml;
			if( xml.endsWith(".xml") )
			{
				gridxmls.add(subzkpath);
			}
			else
			{
				getAllTemplatePath(subzkpath, gridxmls); 
			}
		}
	}
	
	/**
	 * 
	 * @param zookeeper
	 * @param path
	 * @param zipOS
	 * @throws Exception 
	 */
	public void makeTemplateZip(Zookeeper zookeeper, String path, ZipOutputStream zipOS, String dirpath) throws Exception
	{
		Stat stat = zookeeper.exists(path);
		if(stat == null ) return;
		if( path.endsWith(".xml") )
		{
			int i = path.lastIndexOf("/");
			if( i == -1 ) return;
			//得到XML模板文件目录
			dirpath += path.substring(i+1, path.length()-4);
			ZipEntry entry = new ZipEntry(dirpath+"/template.xml");
			entry.setTime(stat.getMtime());
			zipOS.putNextEntry(entry);
			byte[] data = zookeeper.getData(path);
			zipOS.write(data); 
    		zipOS.flush();
    		zipOS.closeEntry();

			List<String> list = zookeeper.getChildren(path);
			for(String name : list){
				stat = zookeeper.exists(path+"/"+name);
				if(stat == null ) continue;
				entry = new ZipEntry(dirpath+"/version/"+name);
				entry.setTime(stat.getMtime());
				zipOS.putNextEntry(entry);
				data = zookeeper.getData(path+"/"+name);
				zipOS.write(data); 
	    		zipOS.flush();
        		zipOS.closeEntry();
			}
		}
		else
		{
			List<String> list = zookeeper.getChildren(path);
			String dirname = getZookeeper().getUTF8(path);
			ZipEntry entry = new ZipEntry(dirpath+dirname+"/name.txt");
			zipOS.putNextEntry(entry);
			String namekey = path.substring(path.lastIndexOf("/")+1);
			zipOS.write(namekey.getBytes()); 
    		zipOS.flush();
    		zipOS.closeEntry();
			dirpath += dirname+"/";
			for(String name : list){
				this.makeTemplateZip(zookeeper, path+"/"+name, zipOS, dirpath);
			}
		}
	}
	/**
	 * 查询模板获取
	 * @param zkpath
	 * @return
	 * @throws Exception
	 */
	public JSONObject getApiTemplates(String zkpath)
		throws Exception
	{
	    return getTemplates(zkpath, true, true);
	}
	public JSONObject getTemplates(String zkpath, boolean loadxml)
		throws Exception
	{
	    return getTemplates(zkpath, loadxml, false);
	}
	/**
	 * 根据系统id和路径得到指定的元数据配置
	 * @param zkpath
	 * @param loadxml
	 * @return
	 * @throws Exception 
	 */
	public JSONObject getTemplates(String zkpath, boolean loadxml, boolean apiable) throws Exception
	{
		JSONObject diggdir = new JSONObject();
		diggdir.put("id", zkpath);
		diggdir.put("type", "dir");
		if( zkpath.endsWith("/digg") ){
			diggdir.put("rootdir", true);
			diggdir.put("name", "我的模板");
			diggdir.put("cname", "我的模板");
//			diggdir.put("icon", "images/ico/templates.png");
			diggdir.put("icon", "images/icons/wand.png");
			diggdir.put("count", 0);
			diggdir.put("countopen", 0);
		}
		else {
			String name = getZookeeper().getUTF8(zkpath);
			diggdir.put("name", name);
			diggdir.put("cname", name);
			diggdir.put("iconClose", "images/icons/folder_closed.png");
			diggdir.put("iconOpen",  "images/icons/folder_opened.png");
			diggdir.put("count", 0);
			diggdir.put("countopen", 0);
		}
		diggdir.put("isParent", true);
		JSONArray children = new JSONArray();
		diggdir.put("children", children);
		
		List<String> list = getZookeeper().getChildren(zkpath);
		QuickSort sorter = new QuickSort() {
			public boolean compareTo(Object sortSrc, Object pivot) {
				return sortSrc.toString().compareTo(pivot.toString()) > 0;
			}
		};
		sorter.sort(list);
		for(String xml : list )
		{
			String subzkpath = zkpath+"/"+xml;
			if( xml.endsWith(".xml") ){
				if( loadxml )
				try{
		            JSONObject template = getTemplate(subzkpath, xml);
		            if( apiable && !this.loadApiTemplate(subzkpath, template, diggdir) ) {
		            	continue;
		            }
		            children.put(template);
				}
				catch(Exception e)
				{
					log.error("Failed to load the template from "+xml+" for "+e);
				}
			}
			else
			{
				JSONObject subdiggdir = this.getTemplates(subzkpath, loadxml, apiable); 
				if( subdiggdir != null ){
					children.put(subdiggdir);
					if(subdiggdir.has("count")){
		            	diggdir.put("count", diggdir.getInt("count")+subdiggdir.getInt("count"));
		            	diggdir.put("countopen", diggdir.getInt("countopen")+subdiggdir.getInt("countopen"));
					}
				}
			}
		}
    	return diggdir;
	}
	
	/**
	 * 加载支持API的模板
	 * @param template
	 * @return
	 * @throws Exception 
	 */
	private boolean loadApiTemplate(String subzkpath, JSONObject template, JSONObject diggdir) throws Exception{
		String type = template.getString("type");
    	if( type.equalsIgnoreCase("form") || type.equalsIgnoreCase("edit") ) {
    		return false;
    	}
    	if( diggdir != null ){
    		if(!diggdir.has("count") ){
    			diggdir.put("count", 0);
    			diggdir.put("countopen", 0);
    		}
    		diggdir.put("count", diggdir.getInt("count")+1);
    	}
    	template.put("icon", "images/icons/unselected.png");
    	JSONObject api = getZookeeper().getJSONObject(subzkpath+"/api.json");
    	if( api != null ){
    		String security = "";
    		String setsecurity = "";
    		JSONObject apitoken = null;
    		if( api.has("setsecurity") ){
    			setsecurity = api.getString("setsecurity");
    		}
    		if( api.has("security") ){
    			security = api.getString("security");
    		}
    		if( !security.isEmpty() ){
    			apitoken = getZookeeper().getJSONObject("/cos/config/security/"+security);
    			if( apitoken != null ){
    				api.put("token", apitoken);
    			}
    		}
    		if( !setsecurity.isEmpty() ){
    			JSONObject token = getZookeeper().getJSONObject("/cos/config/security/"+setsecurity);
    			if( token != null ){
    				apitoken = token;
    			}
    		}
    		if( apitoken != null ){
    			template.put("apitoken", apitoken);
    		}
    		template.put("api", api);
    		switch(api.getInt("status")){
    		case 0:
    	    	template.put("icon", "images/icons/unselected.png");
    			break;
    		case 1:
    	    	template.put("icon", "images/icons/selected.png");
    			break;
    		case 2:
    	    	template.put("icon", "images/icons/abort.png");
    			break;
    		}
        	if( diggdir != null && api.getInt("status") == 1){
            	diggdir.put("countopen", diggdir.getInt("countopen")+1);
        	}
    		//System.err.print(template.toString(4));
    	}
    	return true;
	}
	
	/**
	 * 
	 * @param zookeeper
	 * @param zkpath
	 */
	public void buildDiggApiPrepublish(Zookeeper zookeeper, String zkpath, JSONArray prebulish)
		 throws Exception
	{

		List<String> list = zookeeper.getChildren(zkpath);
		for(String xml : list )
		{
			String subzkpath = zkpath+"/"+xml;
			if( xml.endsWith(".xml") )
			{
				try
				{
		            JSONObject template = getTemplate(subzkpath, xml);
	            	if(template.getString("type").equalsIgnoreCase("edit"))
	            	{
	            		continue;
	            	}
	            	JSONObject api = getZookeeper().getJSONObject(subzkpath+"/api.json");
	            	if( api == null ){
	            		continue;
	            	}
	            	if( api.getInt("status") == 0 || //申请开通
	            		api.getInt("status") == 2 || //调整配置
	            		api.getInt("status") == 3 ){ //申请关闭
	            		String setsecurity = "";
	            		if( api.has("setsecurity") ){
	            			setsecurity = api.getString("setsecurity");
	            			if( !setsecurity.isEmpty() ){
	            				JSONObject token = getZookeeper().getJSONObject("/cos/config/security/"+setsecurity);
	            				if( token != null ){
	            					api.put("settoken", token);
	            				}
	            			}
	            			api.put("template", template);
	            			prebulish.put(api);
	            		}
	            	}
				}
				catch(Exception e)
				{
					log.error("", e);
				}
			}
			else
			{
				this.buildDiggApiPrepublish(zookeeper, subzkpath, prebulish); 
			}
		}
	}
	/**
	 * 设置目录名称
	 * @param sysid
	 * @param path
	 * @param name
	 * @param isAdd
	 * @return
	 */
	public AjaxResult<String> setTemplateDir(String sysid, String zkpath, String name, boolean isAdd)
	{
		AjaxResult<String> result = new AjaxResult<String>();
		try
		{
			int i = zkpath.lastIndexOf("digg");
			String path = zkpath.substring(i+4);
			if( isAdd && Tools.countChar(path, '/') > 1 )
			{
				result.setMessage("目录层级不允许超过2级");
				return result;
			}
			if( isAdd )
			{
				String homonymy = this.homonymy(zkpath, name);
				if( homonymy != null )
				{
					result.setMessage("目录名【"+name+"】已经存在: "+homonymy);
				}
				else
				{
					homonymy = Tools.getUniqueValue();
					zkpath += "/" + homonymy;
					getZookeeper().createNode(zkpath, name.getBytes("UTF-8"));
					result.setResult(zkpath);
					result.setSucceed(true);
				}
			}
			else
			{
				getZookeeper().setString(zkpath, name);
				result.setResult(zkpath);
				result.setSucceed(true);
			}
		}
		catch(Exception e)
		{
			result.setMessage("设置系统["+sysid+"]元数据查询配置目录出现异常:"+e.getMessage());
		}
		return result;
	}

	/**
	 * 删除模板或目录
	 * @param sysid
	 * @param path
	 * @return
	 */
	public AjaxResult<String> delTemplate(String sysid, String zkpath)
	{
		AjaxResult<String> result = new AjaxResult<String>();
		try
		{
			if( zkpath.endsWith("digg") )
			{
				result.setMessage("根目录不允许删除");
				return result;
			}
			getZookeeper().deleteNode(zkpath);
			result.setSucceed(true);
		}
		catch(Exception e)
		{
			log.error("", e);
			result.setMessage("删除系统["+sysid+"]元数据模板出现异常:"+e.getMessage());
		}
		return result;
	}

	/**
	 * 复制目录，同时复制目录下的配置
	 * @param sysid
	 * @param path
	 * @return
	 */
	public AjaxResult<String> copyTemplateDir(String sysid, String path)
	{
		AjaxResult<String> result = new AjaxResult<String>();
		try
		{
			String zkpath = "/cos/config/modules/"+sysid+"/digg";
			if( !path.endsWith("/") ) path += "/";
			zkpath += path;
			getZookeeper().deleteNode(zkpath);
			result.setSucceed(true);
		}
		catch(Exception e)
		{
			result.setMessage("复制系统["+sysid+"]元数据查询配置目录出现异常:"+e.getMessage());
		}
		return result;
	}
	/**
	 * 
	 * @param host
	 * @param username
	 * @param password
	 * @param tables
	private void setMongoTables(String dbaddr, String dbname, String username, String password, JSONObject e)
	{
		MongoClient mongo = null;
		try
		{
			String args[] = Tools.split(dbaddr, ":");
			List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
			MongoCredential credential = MongoCredential.createScramSha1Credential(username, dbname, password.toCharArray());
			credentialsList.add(credential);
			ServerAddress serverAddress = new ServerAddress(args[0], Integer.parseInt(args[1])); 
			mongo = new MongoClient(serverAddress, credentialsList);
			DB db = mongo.getDB(dbname);
    		JSONArray tables = new JSONArray();
        	Iterator<String> iterator = db.getCollectionNames().iterator();
        	while( iterator.hasNext() )
        	{
            	JSONObject row = new JSONObject();
        		String tablename = iterator.next();
				row.put("TABLE_NAME", tablename);
				row.put("name", tablename);
				row.put("id", tablename);
				row.put("type", "table");
				tables.put(row);
        	}
			e.put("children", tables);
//			mongo = new MongoClient(args[0], Integer.parseInt(args[1]));
//			DB db = mongo.getDB(dbname);
//	        if(db.authenticate(username, password.toCharArray()))
//	        {
//	    		JSONArray tables = new JSONArray();
//	        	Iterator<String> iterator = db.getCollectionNames().iterator();
//	        	while( iterator.hasNext() )
//	        	{
//	            	JSONObject row = new JSONObject();
//	        		String tablename = iterator.next();
//					row.put("TABLE_NAME", tablename);
//					row.put("name", tablename);
//					row.put("id", tablename);
//					row.put("type", "table");
//					tables.put(row);
//	        	}
//				e.put("children", tables);
//	        }
//	        else
//	        {
//	        }
		}
		catch(Exception e1)
		{
			e1.printStackTrace();
		}
		finally
		{
			if( mongo != null ) mongo.close();
		}
	}
	 */
	/**
	 * 设置数据源表信息
	 * @param tables
	private void setJdbcTables(String driverClass, String jdbcUrl, String jdbcUsername, String jdbcUserpswd, JSONObject e)
	{
		Connection connection = null;
		ResultSet rs = null;
		Statement statement = null;
		JSONArray tables = new JSONArray();
		try
		{
            Class.forName(driverClass); 
            connection = DriverManager.getConnection(jdbcUrl ,jdbcUsername, jdbcUserpswd);  
            rs =  connection.getMetaData().getTables(null, null, null, new String[]{"TABLE"});
            statement = connection.createStatement();
            while( rs.next() )
            {
            	JSONObject row = new JSONObject();
				row.put("TABLE_NAME", rs.getObject("TABLE_NAME"));
				row.put("TABLE_TYPE", rs.getObject("TABLE_TYPE"));
				row.put("TABLE_CAT", rs.getObject("TABLE_CAT"));
				row.put("TABLE_SCHEM", rs.getObject("TABLE_SCHEM"));
				row.put("REMARKS", rs.getObject("REMARKS"));
				row.put("name", rs.getObject("TABLE_NAME"));
				row.put("id", rs.getObject("TABLE_NAME"));
				row.put("type", "table");
				tables.put(row);
            }
			e.put("children", tables);
		}
		catch(Exception e1)
		{
			e.put("error", e1.getMessage());
		}
        finally
        {
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
	 */
	
	private String getPath(String path, File file)
	{
		if( path.isEmpty() ) return file.getName();
		return path +"/"+file.getName();
	}

	/**
	 * 只加载元数据查询配置
	 * @param dir
	 * @param path
	 * @param data
	 * @param logs
	 * @param depth
	 * @throws Exception 
	 */
	public void loadTemplates(String zkpath, JSONArray data, boolean apiable) throws Exception
	{
		List<String> list = getZookeeper().getChildren(zkpath);
		for(String xml : list )
		{
			String subzkpath = zkpath+"/"+xml;
			if( xml.endsWith(".xml") )
			{
				try
				{
					JSONObject template = getTemplate(subzkpath, xml);
		            if( apiable && !this.loadApiTemplate(subzkpath, template, null) ) 
		            {
		            	continue;
		            }
					data.put(template);
				}
				catch(Exception e)
				{
					log.error("Failed to load the template from "+xml+" for "+e);
				}
			}
			else
			{
				this.loadTemplates(subzkpath, data, apiable);
			}
		}
	}
	/**
	 * 构建本地模板
	 * @param id
	 * @param name
	 * @param root
	 * @return
	 */
	private JSONArray setLocalTemplate(String id, String name, JSONArray root){
		JSONObject dir = new JSONObject();
		JSONArray dirs = new JSONArray();
		dir.put("name", name);
		dir.put("cname", name);
		dir.put("id", id);
		dir.put("type", "dir");
		dir.put("isParent", true);
		dir.put("local", true);
		dir.put("children", dirs);
		dir.put("iconClose", "images/icons/folder_closed.png");
		dir.put("iconOpen",  "images/icons/folder_opened.png");
		if( root != null ) root.put(dir);
		return dirs;
	}
	/**
	 * 得到本地的能够对外开放的API模版
	 * @return
	 */
	public JSONObject getLocalApiTemplates(){
		JSONArray localdirs = new JSONArray();
		this.loadLocalTemplates("/grid/local", localdirs, true, true);
		JSONObject rootdir = new JSONObject();
		rootdir.put("rootdir", true);
		rootdir.put("name", "系统模板");
		rootdir.put("cname", "【主界面框架内置模板】");
		rootdir.put("icon", "images/icons/wand_disabled.png");
		rootdir.put("count", 0);
		rootdir.put("countopen", 0);
		rootdir.put("children", localdirs);
		return rootdir;
	}
	/**
	 * 从当前系统中加载ｇｒｉｄ用于作为例程
	 * @param localdirs 
	 * @param category 
	 */
	public void loadLocalTemplates(String path, JSONArray localdirs, boolean category, boolean isapi)
	{
		JSONArray userTemplats = this.setLocalTemplate("user", "用户管理", category?localdirs:null);
		JSONArray sysTemplats = this.setLocalTemplate("sys", "系统管理", category?localdirs:null);
		JSONArray controlTemplats = this.setLocalTemplate("control", "主控管理", category?localdirs:null);
		JSONArray moduleTemplats = this.setLocalTemplate("module", "模块开发管理", category?localdirs:null);
		JSONArray diggTemplats = this.setLocalTemplate("digg", "元数据模板开发管理", category?localdirs:null);
		JSONArray weixinTemplats = this.setLocalTemplate("weixin", "微信公众号开发", category?localdirs:null);

    	URL url = DiggConfigMgr.class.getClassLoader().getResource("/");
    	File dir = new File(url.getFile(), "grid/local");
    	File[] files = dir.listFiles();
    	StringBuffer sb = new StringBuffer("Load demo-digg from "+dir.getAbsolutePath()+"("+dir.exists()+")");
    	boolean hasException = false;
    	for(File f : files)
    	{
    		sb.append("\r\n\t["+f.getPath()+"]\t");
    		if( f.isDirectory() || !f.getName().endsWith(".xml")){
    			sb.append("\r\n\t\tdiscard.");
    			continue;
    		}
            try 
            {
        		JSONObject template = this.getTemplate(f, "/grid/local");
        		template.put("local", true);
        		if( !isapi && !"local".equals(path) && !f.getName().startsWith(path) ){
        			sb.append("\r\n\t\tdiscard for not local from "+path);
        			continue;
        		}
    	    	if(template.getString("type").equalsIgnoreCase("edit"))
    	    	{
        			sb.append("\r\n\t\tdiscard for not query.");
    	    		continue;
    	    	}
    	    	if( template.getString("datamodel").equalsIgnoreCase("")||
	    			template.getString("datamodel").equalsIgnoreCase("local"))
    	    	{
        			sb.append("\r\n\t\tdiscard for not digg.");
    	    		continue;
    	    	}
        		if( isapi ){
        	    	template.put("icon", "images/icons/unselected.png");
        	    	JSONObject api = getZookeeper().getJSONObject("/cos/config"+template.getString("id")+"/api.json");
        	    	if( api != null ){
//        	    		System.err.println(api.toString(4));
        	    		String security = "";
        	    		String setsecurity = "";
        	    		JSONObject apitoken = null;
        	    		if( api.has("setsecurity") ){
        	    			setsecurity = api.getString("setsecurity");
        	    		}
        	    		if( api.has("security") ){
        	    			security = api.getString("security");
        	    		}
        	    		if( !security.isEmpty() ){
        	    			apitoken = getZookeeper().getJSONObject("/cos/config/security/"+security);
        	    			if( apitoken != null ){
        	    				api.put("token", apitoken);
        	    			}
        	    		}
        	    		if( !setsecurity.isEmpty() ){
        	    			JSONObject token = getZookeeper().getJSONObject("/cos/config/security/"+setsecurity);
        	    			if( token != null ){
        	    				apitoken = token;
        	    			}
        	    		}
        	    		if( apitoken != null ){
        	    			template.put("apitoken", apitoken);
        	    		}
        	    		template.put("api", api);
        	    		switch(api.getInt("status")){
        	    		case 0:
        	    	    	template.put("icon", "images/icons/unselected.png");
        	    			break;
        	    		case 1:
        	    	    	template.put("icon", "images/icons/selected.png");
        	    			break;
        	    		case 2:
        	    	    	template.put("icon", "images/icons/abort.png");
        	    			break;
        	    		}
//        	        	if( diggdir != null && api.getInt("status") == 1){
//        	            	diggdir.put("countopen", diggdir.getInt("countopen")+1);
//        	        	}
        	    		//System.err.print(template.toString(4));
        	    	}
        		}
    			if( f.getName().startsWith("weixin")){
    				weixinTemplats.put(template);
    			}
    			else if( f.getName().startsWith("user")){
    				userTemplats.put(template);
    			}
    			else if( f.getName().startsWith("sys")){
    				sysTemplats.put(template);
    			}
    			else if( f.getName().startsWith("module")){
    				moduleTemplats.put(template);
    			}
    			else if( f.getName().startsWith("digg")){
    				diggTemplats.put(template);
    			}
    			else if( f.getName().startsWith("control")){
    				controlTemplats.put(template);
    			}
            }
            catch(Exception e)
            {
            	hasException = true;
    			sb.append(e.getMessage());
            }
    	}
//    	System.err.println(sb.toString());
    	if( hasException ) log.error(sb.toString());
    	if(!category){
    		for(int i = 0; i < userTemplats.length(); i++ ){
    			localdirs.put(userTemplats.getJSONObject(i));
    		}
    		for(int i = 0; i < sysTemplats.length(); i++ ){
    			localdirs.put(sysTemplats.getJSONObject(i));
    		}
    		for(int i = 0; i < controlTemplats.length(); i++ ){
    			localdirs.put(controlTemplats.getJSONObject(i));
    		}
    		for(int i = 0; i < moduleTemplats.length(); i++ ){
    			localdirs.put(moduleTemplats.getJSONObject(i));
    		}
    		for(int i = 0; i < diggTemplats.length(); i++ ){
    			localdirs.put(diggTemplats.getJSONObject(i));
    		}
    		for(int i = 0; i < weixinTemplats.length(); i++ ){
    			localdirs.put(weixinTemplats.getJSONObject(i));
    		}
    	}
	}
	

	/**
	 * 加载模板的GridCell配置
	 * @param gridNode
	 * @param grid
	 */
	public void loadTemplateGrid(Element gridNode, JSONObject grid)
	{
		if( gridNode == null ) return;
		String freezeCols = XMLParser.getElementAttr(gridNode, "freezeCols");
		int fc = 0;
		if( Tools.isNumeric(freezeCols)) fc = Integer.parseInt(freezeCols);
		grid.put("freezeCols", fc);
		grid.put("open", true);
		grid.put("showTitle", "true".equals(XMLParser.getElementAttr( gridNode, "showTitle" )));
		grid.put("snapshot", "true".equals(XMLParser.getElementAttr( gridNode, "snapshot" )));
		String recIndx = XMLParser.getElementAttr(gridNode, "recIndx");
		grid.put("recIndx", recIndx);
		Element cellNode = XMLParser.getChildElementByTag( gridNode, "cell" );
		JSONArray cells = new JSONArray();
		grid.put("children", cells);
		int i = 0;
        for( ; cellNode != null; cellNode = XMLParser.nextSibling(cellNode))
        {
            String type = XMLParser.getElementAttr( cellNode, "type" );
            if( "checkBoxSelection".equals(type) ||
            	"detail".equals(type))
            {
            	grid.put("firstColumnType", type);
            	continue;
            }
            Element childcellNode = XMLParser.getChildElementByTag(cellNode, "cell");
            if( childcellNode != null )
            {
                String title = XMLParser.getElementAttr( cellNode, "title" );
                for( ; childcellNode != null; childcellNode = XMLParser.nextSibling(childcellNode))
                {
                    JSONObject cell = new JSONObject();
                	this.loadTemplateCell(childcellNode, cell);
                    cell.put("groupname", title);
                    cells.put(cell);
                    cell.put("order", ++i);
                }
            }
            else
            {
                JSONObject cell = new JSONObject();
            	this.loadTemplateCell(cellNode, cell);
                cells.put(cell);
                cell.put("order", ++i);
                if( cell.has("dataIndx") && recIndx.indexOf(cell.getString("dataIndx")) != -1 )
                {
                	cell.put("recIndx", true);
                }
            }
        }
        if(grid.has("name")){
        	grid.put("name", grid.getString("name")+"("+i+")");
        }
	}
	
	/**
	 * 加载Cell
	 * @param cellNode
	 * @param cell
	 */
	private void loadTemplateCell(Element cellNode, JSONObject cell)
	{
        String title = XMLParser.getElementAttr( cellNode, "title" );
        String align = XMLParser.getElementAttr( cellNode, "align" );
        cell.put("id", "cell");
        cell.put("icon", "images/icons/properties-selected.png");
        cell.put("title", title);
        cell.put("align", align);
        String editable = XMLParser.getElementAttr( cellNode, "editable" );
        if( !editable.isEmpty() )  cell.put("editable", "true".equalsIgnoreCase(editable));
        String dataType = XMLParser.getElementAttr( cellNode, "dataType" );
        if( !dataType.isEmpty() ) cell.put("dataType", dataType);
        String dataIndx = XMLParser.getElementAttr( cellNode, "dataIndx" );
        if( !dataIndx.isEmpty() ) cell.put("dataIndx", dataIndx);
        String resizable = XMLParser.getElementAttr( cellNode, "resizable" );
        if( !resizable.isEmpty() ) cell.put("resizable", "true".equalsIgnoreCase(resizable));
        String sort = XMLParser.getElementAttr( cellNode, "sort" );
        if( !sort.isEmpty() ) cell.put("sort", sort);
        String sortable = XMLParser.getElementAttr( cellNode, "sortable" );
        if( !sortable.isEmpty() )  cell.put("sortable", "true".equalsIgnoreCase(sortable));
        cell.put("name", Tools.getJSONValue(title)+"["+dataIndx+"]");
        if( cell.has("resizable") && cell.getBoolean("resizable") )
        {
            String minWidth = XMLParser.getElementAttr( cellNode, "minWidth" );
            if( !minWidth.isEmpty() && Tools.isNumeric(minWidth) ) cell.put("minWidth", minWidth);
            String maxWidth = XMLParser.getElementAttr( cellNode, "maxWidth" );
            if( !maxWidth.isEmpty() && Tools.isNumeric(maxWidth) ) cell.put("maxWidth", maxWidth);
            cell.put("width", minWidth);
        }
        else
        {
            String width = XMLParser.getElementAttr( cellNode, "width" );
            if( !width.isEmpty() && Tools.isNumeric(width) ) cell.put("width", width);
            if( width.isEmpty() )
            {
                String minWidth = XMLParser.getElementAttr( cellNode, "minWidth" );
                if( !minWidth.isEmpty() && Tools.isNumeric(minWidth) ) cell.put("minWidth", minWidth);
                String maxWidth = XMLParser.getElementAttr( cellNode, "maxWidth" );
                if( !maxWidth.isEmpty() && Tools.isNumeric(maxWidth) ) cell.put("maxWidth", maxWidth);
                cell.put("width", minWidth);
            }
        }
        String hidden = XMLParser.getElementAttr( cellNode, "hidden" );
        if( !hidden.isEmpty() ) cell.put("hidden", "true".equalsIgnoreCase(hidden));
        String type = XMLParser.getElementAttr( cellNode, "type" );
        if( !type.isEmpty() ) cell.put("type", type);
        String cls = XMLParser.getElementAttr( cellNode, "cls" );
        if( !cls.isEmpty() ) cell.put("cls", cls);

        JSONArray children = new JSONArray();
		cell.put("children", children);
        Node filterNode = XMLParser.getChildElementByTag(cellNode, "filter");
        if( filterNode != null )
        {
        	JSONObject filter = new JSONObject();
        	type = XMLParser.getElementAttr( filterNode, "type" );
        	String init = XMLParser.getElementAttr( filterNode, "init" );
        	String condition = XMLParser.getElementAttr( filterNode, "condition" );
        	if( !condition.isEmpty() ){
            	if( "textbox".equals(type) && "pqDatePicker".equalsIgnoreCase(init) )
            	{
        			type = "date";
            		if( "between".equals(condition) )
            		{
            			type = "datebetween";
            		}
            	}
            	filter.put("id", "filter");
            	filter.put("name", "[filter]查询过滤器("+type+")");
            	filter.put("icon", "images/icons/search.png");
        		filter.put("type", type);
        		filter.put("condition", condition);
        		filter.put("init", XMLParser.getElementAttr( filterNode, "init" ));
        		filter.put("attr", XMLParser.getElementAttr( filterNode, "attr" ));
        		filter.put("style", XMLParser.getElementAttr( filterNode, "style" ));
        		cell.put("filter", filter);
        	}
    		children.put(filter);
        }

        Node labelNode = XMLParser.getChildElementByTag(cellNode, "label");
        if( labelNode != null )
        {
        	JSONObject label = new JSONObject();
        	label.put("id", "label");
        	type = XMLParser.getElementAttr( labelNode, "type" );
        	label.put("icon", "images/icons/undelete.png");
        	label.put("type", type);
        	String cdata = XMLParser.getCData(labelNode);
    		Element optionNode = XMLParser.getChildElementByTag( labelNode, "option" );
    		if( optionNode != null )
    		{
    			type = "options";
    			JSONObject option = new JSONObject();
	            for( ; optionNode != null; optionNode = XMLParser.nextSibling(optionNode))
	            {
	            	option.put(XMLParser.getElementAttr( optionNode, "value" ), XMLParser.getElementValue(optionNode));
	            }
	            label.put("option", option);
    		}
    		else if( "zookeeper".equals(label.getString("type")) )
    		{
    			type = "zookeeper";
        		Element zookeeperNode = XMLParser.getChildElementByTag( labelNode, "zookeeper" );
    			if( zookeeperNode != null )
    			{
    	        	label.put("key", XMLParser.getElementAttr( zookeeperNode, "key" ));
    	        	label.put("value", XMLParser.getElementAttr( zookeeperNode, "value" ));
    	        	label.put("path", XMLParser.getElementAttr( zookeeperNode, "path" ));
    	        	label.put("mode", XMLParser.getElementAttr( zookeeperNode, "mode" ));
    	        	label.put("decrypte", "true".equalsIgnoreCase(XMLParser.getElementAttr( zookeeperNode, "decrypte" )));
    			}
    			else
    			{
    				label.put("error", "没有配置ZooKeeper参数");
    			}
    		}
    		else if( !cdata.isEmpty() )
    		{
    			type = "script";
    			label.put("cdata", Kit.chr2Unicode(cdata));
    		}
    		else
    		{
				label.put("error", "没有正确配置列数据显示转换器参数");
    		}
        	label.put("name", "列数据显示转换器("+type+")");
    		children.put(label);
        }
        
        Node pq_rowclsNode = XMLParser.getChildElementByTag(cellNode, "pq_rowcls");
        if( pq_rowclsNode != null )
        {
        	JSONObject pq_rowcls = new JSONObject();
        	pq_rowcls.put("id", "pq_rowcls");
        	pq_rowcls.put("name", "行单元高亮显示策略");
        	pq_rowcls.put("icon", "images/icons/flat.png");
        	JSONObject light = new JSONObject();
    		Element lightNode = XMLParser.getChildElementByTag( pq_rowclsNode, "light" );
            for( ; lightNode != null; lightNode = XMLParser.nextSibling(lightNode))
            {
            	light.put(XMLParser.getElementAttr( lightNode, "name" ), XMLParser.getElementAttr( lightNode, "value" ));
            }
        	pq_rowcls.put("light", light);
    		children.put(pq_rowcls);
        }

        Node pq_cellclsNode = XMLParser.getChildElementByTag(cellNode, "pq_cellcls");
        if( pq_cellclsNode != null )
        {
        	JSONObject pq_cellcls = new JSONObject();
        	pq_cellcls.put("id", "pq_cellcls");
        	pq_cellcls.put("name", "单元格高亮显示策略");
        	pq_cellcls.put("icon", "images/icons/mail_status_connected.png");
        	JSONObject light = new JSONObject();
    		Element lightNode = XMLParser.getChildElementByTag( pq_cellclsNode, "light" );
            for( ; lightNode != null; lightNode = XMLParser.nextSibling(lightNode))
            {
            	light.put(XMLParser.getElementAttr( lightNode, "name" ), XMLParser.getElementAttr( lightNode, "value" ));
            }
            pq_cellcls.put("light", light);
    		children.put(pq_cellcls);
        }

        Node editorNode = XMLParser.getChildElementByTag(cellNode, "editor");
        if( editorNode != null )
        {
        	type = XMLParser.getElementAttr( editorNode, "type" );
        	JSONObject editor = new JSONObject();
        	editor.put("id", "editor");
        	editor.put("name", "[editor]单元格编辑器("+type+")");
        	editor.put("icon", "images/icons/drafts.png");
        	editor.put("type", type);
        	editor.put("style", XMLParser.getElementAttr( editorNode, "style" ));
        	editor.put("attr", XMLParser.getElementAttr( editorNode, "attr" ));
    		Element optionNode = XMLParser.getChildElementByTag( editorNode, "option" );
    		if( optionNode != null )
    		{
    			JSONObject option = new JSONObject();
	            for( ; optionNode != null; optionNode = XMLParser.nextSibling(optionNode))
	            {
	            	option.put(XMLParser.getElementAttr( optionNode, "value" ), XMLParser.getElementValue(optionNode));
	            }
	            editor.put("option", option);
    		}
    		else editor.put("cdata", Kit.chr2Unicode(XMLParser.getCData(editorNode)));
    		children.put(editor);
    		cell.put("editor", type);
        }

        Node renderNode = XMLParser.getChildElementByTag(cellNode, "render");
        if( renderNode != null )
        {
        	String function = XMLParser.getElementAttr( renderNode, "function" );
        	String script = XMLParser.getCData(renderNode);
        	if( !function.isEmpty() )
        	{
        		cell.put("render", "function");
        	}
        	else
        	{
        		cell.put("render", "javascript");
        	}
        	JSONObject render = new JSONObject();
        	render.put("id", "render");
        	render.put("name", "[render]单元格渲染器");
        	render.put("icon", "images/icons/wand.png");
        	render.put("function", function);
        	render.put("cdata", Kit.chr2Unicode(script));
    		children.put(render);
        }

        //校验
        Node validationsNode = XMLParser.getChildElementByTag(cellNode, "validations");
        if( validationsNode != null )
        {
    		Element validNode = XMLParser.getChildElementByTag( validationsNode, "valid" );
    		if( validNode != null )
    		{
    			JSONObject validations = new JSONObject();
    			validations.put("id", "validations");
    			validations.put("name", "列数据编辑校验器");
    			validations.put("icon", "images/icons/check_send.png");
        		children.put(validations);
        		JSONArray valids = new JSONArray();
	            for( ; validNode != null; validNode = XMLParser.nextSibling(validNode))
	            {
	            	JSONObject valid = new JSONObject();
	            	valid.put("type", XMLParser.getElementAttr( validNode, "type" ));
	            	valid.put("value", XMLParser.getElementAttr( validNode, "value" ));
	            	valid.put("msg", XMLParser.getElementAttr( validNode, "msg" ));
	            	valid.put("icon", XMLParser.getElementAttr( validNode, "icon" ));
	            	valid.put("cdata", Kit.chr2Unicode(XMLParser.getCData(validNode)));
	            	valids.put(valid);
	            }
	            validations.put("valids", valids);
    		}
        }

        //按钮
        Node buttonNode = XMLParser.getChildElementByTag(cellNode, "button");
        if( buttonNode != null )
        {
            for( ; buttonNode != null; buttonNode = XMLParser.nextSibling(buttonNode))
            {
            	JSONObject button = new JSONObject();
    			button.put("id", "button");
            	button.put("icon", "images/icons/notes-selected.png");
            	button.put("type", XMLParser.getElementAttr( buttonNode, "type" ));//edit update inner
            	button.put("subject", XMLParser.getElementAttr( buttonNode, "subject" ));
            	button.put("name", "行单元功能按钮: "+button.getString("subject"));
            	button.put("icon0", XMLParser.getElementAttr( buttonNode, "icon" ));
            	button.put("class", XMLParser.getElementAttr( buttonNode, "class" ));
            	button.put("style", XMLParser.getElementAttr( buttonNode, "style" ));
            	button.put("cdata", Kit.chr2Unicode(XMLParser.getCData(buttonNode)));
        		children.put(button);
            }
        }
	}
	
	/**
	 * 加载数据模型模板
	 * @param datamodelNode
	 * @param datamodel
	 */
	private void loadTemplateDatamodel(Element datamodelNode, JSONObject datamodel, JSONObject grid)
	{
		String type = XMLParser.getElementAttr( datamodelNode, "type" );
		datamodel.put("type", type);
		String sorting = XMLParser.getElementAttr( datamodelNode, "sorting" );
		datamodel.put("sorting", sorting.isEmpty()?"local":sorting);
		String pagesize = XMLParser.getElementAttr( datamodelNode, "pagesize" );
		datamodel.put("pagesize", pagesize);
		String value = XMLParser.getElementAttr( datamodelNode, "value" );
		datamodel.put("value", value);
		datamodel.put("cdata", Kit.chr2Unicode(XMLParser.getCData(datamodelNode)));
		if( type.isEmpty() )
		{
			datamodel.put("error", "没有选择获取数据的方式类型");
		}
		else if( "zookeeper".equalsIgnoreCase(type) )
		{
			datamodel.put("sorting", "local");
			datamodel.put("location", "local");
			String mode = XMLParser.getElementAttr( datamodelNode, "mode" );
			String encrypte = XMLParser.getElementAttr( datamodelNode, "encrypte" );
			datamodel.put("mode", Tools.isNumeric(mode)?Integer.parseInt(mode):0);
			datamodel.put("encrypte", "true".equalsIgnoreCase(encrypte));
		}
		else if( "popup".equalsIgnoreCase(type) )
		{
		}
		else if( "digg".equals(type) )
		{
			datamodel.put("location", "remote");
			if( pagesize.isEmpty() ) pagesize = "50";
	        Element diggNode = XMLParser.getChildElementByTag(datamodelNode, "digg");
	        if( diggNode != null )
	        {
	            JSONArray children = new JSONArray();
	            datamodel.put("children", children);
	            for( ;diggNode != null; diggNode = XMLParser.nextSibling(diggNode) )
	            {
	            	JSONObject digg = new JSONObject();
	            	type = XMLParser.getElementAttr( diggNode, "type" );
	            	digg.put("type", type);
	            	digg.put("icon", "images/icons/notes-selected.png");
	            	if( "sql".equalsIgnoreCase(type) )
	            	{
	            		String from = XMLParser.getElementAttr( diggNode, "from" );
	            		String join = XMLParser.getElementAttr( diggNode, "join" );
	            		digg.put("id", "sql");
	            		digg.put("name", "关系库映射("+from+")");
//	            		this.loadTemplateQuery(diggNode, digg, grid);
		            	digg.put("from", from);
		            	digg.put("join", join);
	            	}
	            	else if( "object".equalsIgnoreCase(type) )
	            	{
	            		digg.put("id", "mongo");
	            		digg.put("name", "对象库映射()");
	            		String from = XMLParser.getElementAttr( diggNode, "from" );
//	            		this.loadTemplateQuery(diggNode, digg, grid);
	            	}
	            	else if( "oltp".equalsIgnoreCase(type) )
	            	{
	            		digg.put("id", "oltp");
	            		digg.put("name", "联机事务分析器");
	            		String classz = XMLParser.getElementAttr( diggNode, "class" );
	            		if( classz.isEmpty() && datamodel.has("") )
	            		{
	            			datamodel.put("error", "未配置针对DIGG的数据处理程序");
	            		}
		            	digg.put("class", classz);
	            	}
	            	children.put(digg);
	            }
	        }
		}
		
		/*;//url digg zookeeper
		String location = XMLParser.getElementAttr( datamodelNode, "location" );
		datamodel.put("type", type);
		datamodel.put("value", value);
		if( type.isEmpty() )
		{
			datamodel.put("error", "没有选择获取数据的方式类型");
		}
		else if( "zookeeper".equalsIgnoreCase(type) )
		{
			location = "local";
			sorting = "local";
			String mode = XMLParser.getElementAttr( datamodelNode, "mode" );
			String encrypte = XMLParser.getElementAttr( datamodelNode, "encrypte" );
			datamodel.put("mode", Tools.isNumeric(mode)?Integer.parseInt(mode):0);
			datamodel.put("encrypte", "true".equalsIgnoreCase(encrypte));
		}
		else if( "popup".equalsIgnoreCase(type) )
		{
			location = "local";
			sorting = "local";
		}
		else if( "digg".equals(type) )
		{
			location = "remote";
			if( pagesize.isEmpty() ) pagesize = "50";
	        Element diggNode = XMLParser.getChildElementByTag(datamodelNode, "digg");
	        if( diggNode != null )
	        {
	            for( ;diggNode != null; diggNode = XMLParser.nextSibling(diggNode) )
	            {
	            	JSONObject digg = new JSONObject();
	            	type = XMLParser.getElementAttr( diggNode, "type" );
	            	digg.put("type", type);
	            	if( "sql".equalsIgnoreCase(type) )
	            	{
	            		digg.put("id", "sql");
	            		digg.put("name", "关系数据库映射");
	            		this.loadTemplateQuery(diggNode, digg, grid);
	            		String from = XMLParser.getElementAttr( diggNode, "from" );
	            		String join = XMLParser.getElementAttr( diggNode, "join" );
		            	digg.put("from", from);
		            	digg.put("join", join);
	            	}
	            	else if( "object".equalsIgnoreCase(type) )
	            	{
	            		digg.put("id", "oltp");
	            		digg.put("name", "对象数据库映射");
	            		this.loadTemplateQuery(diggNode, digg, grid);
	            	}
	            	else if( "oltp".equalsIgnoreCase(type) )
	            	{
	            		digg.put("id", "oltp");
	            		digg.put("name", "联机事务分析器");
	            		String classz = XMLParser.getElementAttr( diggNode, "class" );
	            		if( classz.isEmpty() && datamodel.has("") )
	            		{
	            			datamodel.put("error", "未配置针对DIGG的数据处理程序");
	            		}
		            	digg.put("class", classz);
	            	}
	            }
	        }
		}
		else
		{
			if( "local".equalsIgnoreCase(location) )
			{
			}
			else
			{
				if( pagesize.isEmpty() ) pagesize = "50";
			}
		}
		if( Tools.isNumeric(pagesize) ) datamodel.put("pagesize", Integer.parseInt(pagesize));
		datamodel.put("location", location);
		if( sorting.isEmpty() ) sorting = "local";
		datamodel.put("sorting", sorting);*/
		
	}
	
	/**
	 * 加载数据模型模板
	 * @param diggNode
	 * @param digg
	 * @throws Exception
	 */
	private void loadTemplateQuery(Element diggNode, JSONObject digg, JSONObject grid) throws Exception
	{
		String src = XMLParser.getElementAttr( diggNode, "src" );
    	if( src.isEmpty() )
    	{
    		digg.put("error", "数据库映射器数据源未配置");
    	}
    	else if( !src.equalsIgnoreCase("local") )
    	{
    		JSONObject ds = getZookeeper().getJSONObject("/cos/config/modules/"+grid.getString("sysid")+"/datasource/"+src, true);
    		if( ds == null )
    		{
    			digg.put("error", "模块子系统数据源【"+src+"】配置无效");
    		}
    	}
    	digg.put("src", src);
    	ArrayList<JSONObject> list = new ArrayList<JSONObject>();
    	String recIndx = grid.getString("recIndx");
    	if( !grid.has("children") )
    	{
    		grid.put("error", "查询列表字段为配置");
    	}

		JSONArray children = grid.getJSONArray("children");
		for( int i = 0; i < children.length(); i++ )
		{
			JSONObject cell = children.getJSONObject(i);
			if( "cellgroup".equals(cell.getString("id")) )
			{
				JSONArray children1 = cell.getJSONArray("children");
	    		for( int j = 0; i < children1.length(); j++ )
	    		{
	    			JSONObject subcell = children1.getJSONObject(j);
					String dataIndx = subcell.getString("dataIndx");
					if( dataIndx.isEmpty() ) continue;
	            	JSONObject column = new JSONObject();
	            	column.put("id", "column");
	            	column.put("dataIndx", dataIndx);
	            	column.put("name", subcell.getString("title")+"("+dataIndx+")");
	            	column.put("icon", "images/icons/wand.png");
	            	list.add(column);
	            	if( recIndx.equals(subcell.getString("dataIndx")))
	            	{
	            		recIndx = null;
	            	}
	    		}
			}
			else
			{
				String dataIndx = cell.getString("dataIndx");
				if( dataIndx.isEmpty() ) continue;
	        	JSONObject column = new JSONObject();
            	column.put("id", "column");
            	column.put("dataIndx", dataIndx);
            	column.put("name", cell.getString("title")+"("+dataIndx+")");
            	list.add(column);
            	if( recIndx != null && cell.has("dataIndx") && recIndx.equals(cell.getString("dataIndx")))
            	{
            		recIndx = null;
            	}
			}
		}
		
		if( recIndx != null )
		{
        	JSONObject column = new JSONObject();
        	column.put("id", "column");
        	column.put("name", "列表数据唯一值字段");
        	column.put("dataIndx", recIndx);
        	column.put("icon", "images/icons/wand.png");
        	list.add(0, column);
		}
    	
    	HashMap<String, Element> map = new HashMap<String, Element>();
        Element columnNode = XMLParser.getFirstChildElement(diggNode);
        for( ;columnNode != null; columnNode = XMLParser.getNextSibling(columnNode) )
        {
        	map.put(columnNode.getNodeName(), columnNode);
        }

    	JSONArray columns = new JSONArray();
    	digg.put("children", columns);
        for(JSONObject column : list )
        {
			String dataIndx = column.getString("dataIndx");
			columnNode = map.remove(dataIndx);
			if( columnNode != null )
			{
	        	column.put("column", XMLParser.getElementAttr( columnNode, "column" ));
	        	column.put("format", XMLParser.getElementAttr( columnNode, "format" ));
	        	column.put("method", XMLParser.getElementAttr( columnNode, "method" ));
	        	column.put("value", XMLParser.getElementAttr( columnNode, "value" ));
	            Element conditionNode = XMLParser.getFirstChildElement(columnNode);
	            if( conditionNode != null )
	            {
	            	JSONArray conditions = new JSONArray();
	            	this.loadTemplateQueryCondition(conditionNode, conditions);
	            	column.put("conditions", conditions);
	            }
			}
			else
			{
				column.put("error", "该字段还未做数据查询映射");
			}
			columns.put(column);
        }
        
        if( !map.isEmpty() )
        {
        	Iterator<Element> iterator = map.values().iterator();
        	while( iterator.hasNext() )
        	{
        		columnNode = iterator.next();
	        	JSONObject column = new JSONObject();
	        	column.put("id", "column");
	        	column.put("name", "其它字段("+columnNode.getNodeName()+")");
            	column.put("dataIndx", columnNode.getNodeName());
            	column.put("icon", "images/icons/wand_disabled.png");
	        	column.put("column", XMLParser.getElementAttr( columnNode, "column" ));
	        	column.put("format", XMLParser.getElementAttr( columnNode, "format" ));
	        	column.put("method", XMLParser.getElementAttr( columnNode, "method" ));
	        	column.put("value", XMLParser.getElementAttr( columnNode, "value" ));
				columns.put(column);
	            Element conditionNode = XMLParser.getFirstChildElement(columnNode);
	            if( conditionNode != null )
	            {
	            	JSONArray conditions = new JSONArray();
	            	this.loadTemplateQueryCondition(conditionNode, conditions);
	            	column.put("conditions", conditions);
	            }
        	}
        }
	}
	
	/**
	 * 加载查询条件的模板 
	 * @param columnNode
	 * @param column
	 */
	private void loadTemplateQueryCondition(Element conditionNode, JSONArray conditions)
	{
        for( ;conditionNode != null; conditionNode = XMLParser.nextSibling(conditionNode) )
        {
        	JSONObject condition = new JSONObject();
    		condition.put("compound", XMLParser.getElementAttr( conditionNode, "compound" ));
    		condition.put("method", XMLParser.getElementAttr( conditionNode, "method" ));
    		condition.put("value", XMLParser.getElementAttr( conditionNode, "value" ));
    		conditions.put(condition);

            Element conditionNode1 = XMLParser.getFirstChildElement(conditionNode);
            if( conditionNode1 != null )
            {
            	JSONArray conditions1 = new JSONArray();
            	this.loadTemplateQueryCondition(conditionNode1, conditions1);
            	condition.put("conditions", conditions1);
            }
        }
	}

	/**
	 * 加载工具栏
	 * @param toolbarNode
	 * @param toolbar
	 */
	private void loadTemplateToolbar(Element toolbarNode, JSONObject toolbar)
	{
		if( toolbarNode == null ) return;
        //按钮
        Node buttonNode = XMLParser.getChildElementByTag(toolbarNode, "button");
        if( buttonNode != null )
        {
        	JSONArray children = new JSONArray();
            for( ; buttonNode != null; buttonNode = XMLParser.nextSibling(buttonNode))
            {
            	JSONObject button = new JSONObject();
            	button.put("id", "button");
            	button.put("icon", "images/icons/notes-selected.png");
            	button.put("type", XMLParser.getElementAttr( buttonNode, "type" ));
            	button.put("name", XMLParser.getElementAttr( buttonNode, "subject" ));
            	button.put("icon0", XMLParser.getElementAttr( buttonNode, "icon" ));
            	button.put("class", XMLParser.getElementAttr( buttonNode, "class" ));
            	button.put("style", XMLParser.getElementAttr( buttonNode, "style" ));
            	button.put("cdata", Kit.chr2Unicode(XMLParser.getCData(buttonNode)));
            	children.put(button);
            }
            toolbar.put("children", children);
        }
	}

	/**
	 * 加载选择器
	 * @param selectionNode
	 * @param selection
	 */
	private void loadTemplateSelection(Element selectionNode, JSONObject selection)
	{
    	selection.put("type", XMLParser.getElementAttr( selectionNode, "type" ));
    	selection.put("mode", XMLParser.getElementAttr( selectionNode, "mode" ));
    	selection.put("subtype", XMLParser.getElementAttr( selectionNode, "subtype" ));
    	selection.put("cbHeader", "true".equalsIgnoreCase(XMLParser.getElementAttr( selectionNode, "type" )));
    	selection.put("cbAll", "true".equalsIgnoreCase(XMLParser.getElementAttr( selectionNode, "type" )));
	}

	/**
	 * 加载详情
	 * @param detailsNode
	 * @param details
	 */
	private void loadTemplateDetails(Element detailsNode, JSONObject details)
	{
		if( detailsNode == null ) return;
        Node detailNode = XMLParser.getFirstChildElement(detailsNode);
        if( detailNode != null )
        {
        	JSONArray children = new JSONArray();
            for( ;detailNode != null;detailNode = XMLParser.nextSibling(detailNode))
            {
            	JSONObject detail = new JSONObject();
            	detail.put("id", "detail");
            	detail.put("subject", XMLParser.getElementAttr( detailNode, "subject" ));
            	detail.put("name", detail.getString("subject"));
            	detail.put("icon", "images/icons/dom_console.png");
            	String type = XMLParser.getElementAttr( detailNode, "type" );
            	if( "0".equals(type) )
            	{
                	String data = XMLParser.getElementAttr( detailNode, "data" );
                	detail.put("data", data);
                	detail.put("type", Integer.parseInt(type));
            	}
            	else if( "1".equals(type) )
            	{
                    Node metadataNode = XMLParser.getChildElementByTag(detailNode, "metadata");
                    if( metadataNode != null )
                    {
                    	JSONObject metadata = new JSONObject();
                        for( ; metadataNode != null; metadataNode = XMLParser.nextSibling(metadataNode))
                        {
                        	metadata.put(XMLParser.getElementAttr( metadataNode, "name" ), XMLParser.getElementAttr( metadataNode, "value" ));
                        }
                    	detail.put("metadata", metadata);
                    	detail.put("type", Integer.parseInt(type));
                    }
            	}
            	else if( "2".equals(type) )
            	{
            	}
            	else if( "3".equals(type) )
            	{
            	}
            	children.put(detail);
            }
            details.put("children", children);
        }
	}
	
	/**
	 * 加载模板配置
	 * @return
	 */
	public JSONArray loadTemplateTree(XMLParser xml)
	{
		JSONArray templates = new JSONArray();
		//############################################
		JSONObject grid = new JSONObject();
		grid.put("id", "grid");
		grid.put("name", "[grid]列表字段");
		grid.put("icon", "images/icons/new_item.png");
		templates.put(grid);
		Element gridNode = XMLParser.getChildElementByTag( xml.getRootNode(), "grid" );
		this.loadTemplateGrid(gridNode, grid);
		//############################################
		JSONObject selection = new JSONObject();
		selection.put("id", "selection");
		selection.put("name", "[selection]列表选择方式");
		selection.put("icon", "images/icons/selected.png");
		templates.put(selection);
		Element selectionNode = XMLParser.getChildElementByTag( xml.getRootNode(), "selection" );
		this.loadTemplateSelection(selectionNode, selection);
		//############################################
		JSONObject toolbar = new JSONObject();
		toolbar.put("id", "toolbar");
		toolbar.put("name", "[toolbar]工具栏");
		toolbar.put("icon", "images/icons/note_web.png");
		templates.put(toolbar);
		Element toolbarNode = XMLParser.getChildElementByTag( xml.getRootNode(), "toolbar" );
		this.loadTemplateToolbar(toolbarNode, toolbar);
		//############################################
		JSONObject details = new JSONObject();
		details.put("id", "details");
		details.put("name", "[details]扩展详情");
		details.put("icon", "images/icons/links.png");
		templates.put(details);
		Element detailsNode = XMLParser.getChildElementByTag( xml.getRootNode(), "details" );
		this.loadTemplateDetails(detailsNode, details);
		//############################################
		JSONObject datamodel = new JSONObject();
		datamodel.put("id", "datamodel");
		datamodel.put("name", "[datamodel]数据模型");
		datamodel.put("icon", "images/icons/tile.png");
		datamodel.put("open", true);
		templates.put(datamodel);
		Element datamodelNode = XMLParser.getChildElementByTag( xml.getRootNode(), "datamodel" );
		this.loadTemplateDatamodel(datamodelNode, datamodel, grid);
		return templates;
	}
	/**
	 * 测试数据源连接
	 * @param sysid 系统ID
	 * @param name 数据源配置ID
	 * @return
	 */
	public AjaxResult<String> testDatasource(String sysid, String name)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		MongoClient mongo = null;
		Jedis jedis = null;
		Connection connection = null;
		ResultSet rs = null;
		Statement statement = null;
		try
		{
			String path = "/cos/config/modules/"+sysid+"/datasource/"+name;
			Stat stat = getZookeeper().exists(path);
			if( stat == null )
			{
				rsp.setMessage("在子系统【"+sysid+"】未发现存在数据源配置【"+name+"】");
				return rsp;
			}
            int c = 0;
			JSONObject ds = getZookeeper().getJSONObject(path, true);
			String datatype = ds.getString("dbtype");
			String dbaddr = ds.getString("dbaddr");
			String dbname = ds.getString("dbname");
			String username = ds.getString("dbusername");
			String password = ds.has("dbpassword")?ds.getString("dbpassword"):"";
			password = !password.isEmpty()?new String(Base64X.decode(password)):"";
			if( "mongo".equals(datatype) )
			{
    			String args[] = Tools.split(dbaddr, ":");
//					log.info(ds.toString(4));
    			List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
    			MongoCredential credential = MongoCredential.createScramSha1Credential(username, dbname, password.toCharArray());
    			credentialsList.add(credential);
    			ServerAddress serverAddress = new ServerAddress(args[0], Integer.parseInt(args[1])); 
    			mongo = new MongoClient(serverAddress, credentialsList);
    			MongoDatabase database = mongo.getDatabase(dbname);
    			MongoCursor<String> iterator = database.listCollectionNames().iterator();
    			StringBuffer sb = new StringBuffer();
    			while(iterator.hasNext())
    			{
    				c += 1;
    				sb.append("\r\n"+iterator.next());
    			}
			}
    		else if( "redis".equals(datatype) )
    		{
    			String args[] = Tools.split(dbaddr, ":");
        		String host = args[0];
        		int port = Integer.parseInt(args[1]);
        		jedis = new Jedis(host, port);
        		jedis.connect();
        		if( !password.isEmpty() ){
        			jedis.auth(password);
        		}
    		}
			else
			{
				String jdbcUrl;
				String jdbcUsername;
				String jdbcUserpswd;
				String driverClass;
    			if("h2".equalsIgnoreCase(datatype))
    			{
    				driverClass = "org.h2.Driver";
    				jdbcUrl = "jdbc:h2:tcp://"+dbaddr+"/../h2/"+dbname;
    			}
    			else if("mysql".equalsIgnoreCase(datatype))
    			{
    				driverClass = "com.mysql.jdbc.Driver";
    				jdbcUrl = "jdbc:mysql://"+dbaddr+"/"+dbname+"?lastUpdateConnt=true&amp;useUnicode=true&amp;characterEncoding=UTF-8";
    			}
    			else if("oracle".equalsIgnoreCase(datatype))
    			{
    				driverClass = "oracle.jdbc.driver.OracleDriver";
    				jdbcUrl = "jdbc:oracle:thin:@"+dbaddr+":"+dbname;
    			}
    			else
    			{
					rsp.setMessage("不支持的数据源类型"+datatype);
					return rsp;
    			}
    			jdbcUsername = username;
    			jdbcUserpswd = password;
                Class.forName(driverClass); 
                connection = DriverManager.getConnection(jdbcUrl ,jdbcUsername, jdbcUserpswd);
                rs =  connection.getMetaData().getTables(null, null, null, new String[]{"TABLE"});
                while( rs.next() )
                {
                	c += 1;
                }
			}
			rsp.setSucceed(true);
			rsp.setMessage("测试数据源连接成功，该数据库有"+c+"个表.");
		}
		catch(Exception e)
		{
			rsp.setMessage("测试数据源连接失败因为异常"+e);
		}
		finally
		{
			if( jedis != null ) jedis.close();
			if( mongo != null ) mongo.close();
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
		return rsp;	
	}
	/**
	 * 构建元数据配置，如果配置文件存在则同步到ZK中
	 * @param configs
	 * @throws Exception
	public void buildMetadataConfigs(JSONArray configs) throws Exception
	{
		File path = new File(PathFactory.getDataPath(), "report/meta-data/");
		ArrayList<MetaData> listMetaData = new ArrayList<MetaData>();
		HashMap<String, MetaData> mapMetaData = new HashMap<String, MetaData>();
		File files[] = path.listFiles();
		if( files != null )
		{
			for(File file1 : files)
			{
				Object object = IOHelper.readSerializableNoException(file1);
				if( object != null && object instanceof MetaData )
				{
					MetaData metaData = (MetaData)object;
					String name = metaData.getAttributeValue("name");
					if( name.length() == 0 )
					{
						metaData.setAttribute("name", metaData.getName());
					}
					listMetaData.add(metaData);
					mapMetaData.put(metaData.getName(), metaData);
				}
			}
		}

		for(int i = 0; i < configs.length(); i++ )
		{
			JSONObject config = configs.getJSONObject(i);
			if( mapMetaData.containsKey(config.getString("name")) )
			{
				mapMetaData.remove(config.getString("name"));
			}
		}
		
		StringBuffer sb = new StringBuffer();
		for(MetaData data : listMetaData )
		{
			JSONObject e = new JSONObject();
			e.put("name", data.getName());
			e.put("title", data.getAttributeValue("name"));
			e.put("dbtype", data.getDbtype());
			e.put("host", data.getHost());
			e.put("port", data.getPort());
			e.put("username", data.getUsername());
			e.put("password", data.getPassword());
			e.put("dbname", data.getDbname());
			if( mapMetaData.containsKey(e.getString("name")) )
			{//发现配置没有同步
				String zkpath = "/cos/config/datasource/"+e.getString("name");
				Stat stat = ZKMgr.getZookeeper().exists(zkpath); 
				if( stat == null)
				{
					sb.append("\r\n\tFound the datasources of "+e.getString("name")+" to save to "+zkpath);
					ZKMgr.getZookeeper().setJSONObject(zkpath, e, true);
				}
			}
		}
		if( sb.length() > 0 ) log.info("Succeed to set the datasource from report:"+sb.toString());
	}
	 */
	/**
	 * 得到数据源配置表
	 * @return
	public JSONArray getDatasourceConfigs()
	{
		JSONArray data = new JSONArray();
		String path = "/cos";
		try
		{
			ZooKeeper zookeeper = ZKMgr.getZooKeeper();
			Stat stat = zookeeper.exists(path, false); 
			if( stat == null)
			{
				zookeeper.create(path, "企业级数据挖掘工具".getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			path = "/cos/config";
			stat = zookeeper.exists(path, false); 
			if( stat == null)
			{
				zookeeper.create(path, "产品配置".getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			path = "/xdig/config/datasource";
			stat = zookeeper.exists(path, false); 
			if( stat == null)
			{
				zookeeper.create(path, "数据源配置".getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			else
			{
				List<String> list = zookeeper.getChildren(path, false);
				if( list != null )
				{
					for( String metadata : list )
					{
						String nodepath = path+"/"+metadata;
						Stat stat_ = zookeeper.exists(nodepath, false); 
						if( stat_ == null ) continue;
						String json = new String(zookeeper.getData(nodepath, false, stat_), "UTF-8");
						data.put(new JSONObject(json));
					}
				}
			}
		}
		catch(Exception e )
		{
			log.error("Failed to load the nodes of metadata from zookeeper("+path+")", e);
		}
		return data;
	}
	 */
	private Zookeeper zookeeper;
	private Zookeeper getZookeeper() throws Exception{
		if( zookeeper != null ){
			if( zookeeper.isConnected() ){
				return zookeeper;
			}
		}
		zookeeper = ZKMgr.getZookeeper();
		return zookeeper;
	}
}
