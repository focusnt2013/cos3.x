package com.focus.cos.web.action;

import com.focus.cos.web.common.QueryMeta;
import com.focus.cos.web.common.paginate.PageBean;
import com.opensymphony.xwork.ModelDriven;

public class QueryAction extends CosBaseAction implements ModelDriven
{
	private static final long serialVersionUID = 1L;
	/*日期类型*/
	protected String dateType;
	//分页实体
	protected PageBean pageBean = new PageBean();
	//分页菜单
	protected String pageMenu;
	//查询条件
	protected QueryMeta queryMeta = new QueryMeta();

	public Object getModel()
	{
		return pageBean;
	}

	public String getDateType()
	{
		return dateType;
	}

	public void setDateType(String dateType)
	{
		this.dateType = dateType;
	}

	public PageBean getPageBean()
	{
		return pageBean;
	}

	public void setPageBean(PageBean pageBean)
	{
		this.pageBean = pageBean;
	}

	public String getPageMenu()
	{
		return pageMenu;
	}

	public void setPageMenu(String pageMenu)
	{
		this.pageMenu = pageMenu;
	}

	public QueryMeta getQueryMeta()
	{
		return queryMeta;
	}

	public void setQueryMeta(QueryMeta queryMeta)
	{
		this.queryMeta = queryMeta;
	}
}
