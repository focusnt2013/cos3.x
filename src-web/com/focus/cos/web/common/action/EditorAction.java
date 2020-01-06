package com.focus.cos.web.common.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.focus.cos.web.action.CosBaseAction;
import com.focus.cos.web.common.Kit;

public class EditorAction extends CosBaseAction {

	private static final long serialVersionUID = -1340717608764968981L;
	private static final Log log = LogFactory.getLog(EditorAction.class);
	/*编辑文本脚本*/
	public String text()
	{
		log.info("Open the editor of text.");
		messageCode = "txt";
		return "text";
	}
	/*编辑JS脚本*/
	public String js()
	{
		log.info("Open the editor of javascript.");
		messageCode = "js";
		return "xml_json_css_sql";
	}
	/*编辑XML脚本*/
	public String xml()
	{
		log.info("Open the editor of xml.");
		messageCode = "xml";
		return "xml_json_css_sql";
	}
	/**
	 * 编辑json对象
	 * @return
	 */
	public String json()
	{
		messageCode = "json";
		return "xml_json_css_sql";
	}
	/**
	 * 编辑样式
	 * @return
	 */
	public String css()
	{
		messageCode = "css";
		return "xml_json_css_sql";
	}
	/**
	 * 编辑SQL语句
	 * @return
	 */
	public String sql()
	{
		messageCode = "sql";
		return "xml_json_css_sql";
	}
	/**
	 * 编辑json对象
	 * @return
	 */
	public String javascript()
	{
		messageCode = "javascript";
		return "javascript";
	}

	/**
	 * 编辑json对象
	 * @return
	 */
	public String compare()
	{
//		messageCode = "javascript";
		return "mergely";
	}
	private String leftContent;
	private String rightContent;
	private String titleLeft;
	private String titleRight;
	public String getCompareLeftTitle() {
		return titleLeft;
	}
	public void setCompareLeftTitle(String titleLeft) {
		this.titleLeft = titleLeft;
	}
	public String getCompareRightTitle() {
		return titleRight;
	}
	public void setCompareRightTitle(String titleRight) {
		this.titleRight = titleRight;
	}
	public void setCompareLeft(String left) 
	{
		this.leftContent = left;
	}
	public void setCompareRight(String right) 
	{
		this.rightContent = right;
	}
	public String getCompareLeft() 
	{
		return leftContent!=null&&!leftContent.isEmpty()?Kit.chr2Unicode(leftContent):"";
	}
	public String getCompareRight() 
	{
		return rightContent!=null&&!rightContent.isEmpty()?Kit.chr2Unicode(rightContent):"";
	}
}
