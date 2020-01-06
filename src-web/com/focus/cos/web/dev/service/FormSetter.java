package com.focus.cos.web.dev.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Node;

import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.service.GridSetter;
import com.focus.util.Base64X;
import com.focus.util.Tools;
import com.focus.util.XMLParser;

/**
 * 修改Grid表格的数据
 * @author focus
 *
 */
public abstract class FormSetter extends GridSetter
{
	protected JSONObject response = new JSONObject();
	protected JSONObject formData = new JSONObject();
	protected JSONObject columns = new JSONObject();
	protected String gridtitle;
	/**
	 * 构建表格保存
	 * @param gridxml
	 * @throws Exception
	 */
	public FormSetter(int mode, String gridxml, JSONObject formData, JSONObject columns) throws Exception
	{
		InputStream is = null;
		this.formData = formData;
		this.columns = columns;
		if( gridxml.startsWith("/grid/local") )
		{
			is =  this.getClass().getResourceAsStream(gridxml);
		}
		else
		{
			byte[] payload = ZKMgr.getZookeeper().getData(gridxml);
			if( payload == null ){
				throw new Exception(String.format("模板[%s]可能还没有保存，不能获取模板配置", gridxml));
			}
			is = new ByteArrayInputStream(payload);
		}
		method = mode;
		filedtag = "input";
		load(is);
	}
	
	/**
	 * 递归构建表格的列字段
	 * @param gridNode
	 */
	protected void setFormParameters(JSONObject formData, Node formNode) throws Exception
	{
        for( ; formNode != null; formNode = XMLParser.getNextSibling(formNode))
        {
            Node childNode = XMLParser.getChildElementByTag(formNode, filedtag);
            if( childNode != null )
            {
            	this.setFormParameters(formData, childNode);
            }
            else
            {
            	String title = XMLParser.getElementAttr( formNode, "title" );
            	String dataIndx = XMLParser.getElementAttr( formNode, "dataIndx" );
            	if( dataIndx.isEmpty() || !formData.has(dataIndx) ) continue;
            	String value = formData.get(dataIndx).toString();
            	String dataType = XMLParser.getElementAttr( formNode, "dataType" );
            	if( dataType.isEmpty() ) dataType = "string";
            	else if( dataType.equalsIgnoreCase("datalength") ){
            		dataType = "long";
            	}
            	String tag = XMLParser.getElementAttr( formNode, "tag" );
            	Object val = value;
            	boolean nullable = "true".equalsIgnoreCase(XMLParser.getElementAttr( formNode, "nullable" ));
            	if( !nullable ){
            		nullable = "hidden".equalsIgnoreCase(XMLParser.getElementAttr( formNode, "type" ));
            	}
            	if( !nullable ){
            		nullable = "true".equalsIgnoreCase(XMLParser.getElementAttr( formNode, "onlyshow" ));
            	}
            	if( !nullable ){
            		nullable = "true".equalsIgnoreCase(XMLParser.getElementAttr( formNode, "hidden" ));
            	}
            	boolean isTag = dataType.equalsIgnoreCase("tag") || (!tag.isEmpty() && Tools.isNumeric(tag));
            	if( dataType.startsWith("int") )
            	{
            		if( !Tools.isNumeric(value) )
            		{
            			if( nullable ) continue;
                		throw new Exception("【"+title+"】必须是数字，您输入的是"+value+"。");
            		}
            		val = Integer.parseInt(value);
            	}
            	else if( dataType.equalsIgnoreCase("long") )
            	{
            		if( !Tools.isNumeric(value) )
            		{
            			if( nullable ) continue;
                		throw new Exception("【"+title+"】必须是长整数字，您输入的是"+value+"。");
            		}
            		val = Long.parseLong(value);
            	}
            	else if( dataType.equalsIgnoreCase("number") )
            	{
            		if( !Tools.isNumeric(value) )
            		{
            			if( nullable ) continue;
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
            			formData.remove(dataIndx);
            			continue;//密码，不保存
            		}
            	}
            	else if( dataType.equalsIgnoreCase("ip") )
            	{
            		if( Tools.countChar(value, '.') != 3 )
            		{
            			if( nullable ) continue;
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
                	String encode = XMLParser.getElementAttr( formNode, "encode" );
            		if( !encode.isEmpty() )
            		{
            			encode = formData.getString(encode); 
            			if( encode != null && !encode.isEmpty() )
            			{
            				val = Tools.encodeMD5(encode);
            			}
            		}
            	}
            	if( val.toString().isEmpty()  )
            	{
            		if( !nullable )	throw new Exception("字段【"+title+"/"+dataIndx+"】不允许为空。");
            	}
				formData.put(dataIndx, val);
            }
        }
	}
}
