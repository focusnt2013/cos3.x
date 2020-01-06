package com.focus.cos.web.common.paginate;

/**
 * Description: 用于静态分页（页面不刷新实现分页）
 * Create Date:Oct 17, 2008
 * @author Focus Lau
 *
 * @since 1.0
 */
public class PageDetail
{
	/*分页上下文信息，保存HTML列表*/
	private String pageContext;

	/*分页菜单信息，保存分页菜单*/
	private String pageMenu;
	/*响应异常*/
	private String responseException = "";

	public String getPageContext()
	{
		return pageContext;
	}

	public void setPageContext(String pageContext)
	{
		this.pageContext = pageContext;
	}

	public String getPageMenu()
	{
		return pageMenu;
	}

	public void setPageMenu(String pageMenu)
	{
		this.pageMenu = pageMenu;
	}

	public String getResponseException()
	{
		return responseException;
	}

	public void setResponseException(String responseException)
	{
		this.responseException = responseException;
	}
}
