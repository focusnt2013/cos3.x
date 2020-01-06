package com.focus.cos.web.common.paginate;

import java.util.ArrayList;
import java.util.List;


/**
 * Description: 分页实体
 * Create Date:Oct 17, 2008
 * @author Nixin
 *
 * @since 1.0
 */
public class PageBean
{
    //count property 记录总数
    private int count = 0; 

    // pageSize property 每页显示记录数
    private int pageSize = 100;

    // pageCount property 总页数
    private int pageCount = 0; 

    // page property 当前页数
    private int page = 1;

    // totalCountSQL property 得到总记录数sql语句
    private String totalCountSQL;

    // listSQL property 得到查询记录sql语句
    private String listSQL;
    
    // 分页的链接
    private String href;
    
    //每页显示分页链接数  Add by JinHua
    private int pageSegment = 5;
    
    // 分页方法
    private String method = "skitPageMove";
    
    //分页所属form
    private String form;
    
    // 分页大小表单控件
    private String pageSizeId = "pageSize";

    //全部页码，从1开始add by nixin for Query.scroll()
    private int prePage = 1;
    private int nextPage = 1;
    private int[] pageNumbers;
    
    private List<Object> list = new ArrayList<Object>();
    
    //查询参数的Value
    private Object[] values;
    
	public int getPageSegment()
	{
		return pageSegment;
	}

	public void setPageSegment(int pageSegment)
	{
		this.pageSegment = pageSegment;
	}

	public int getCount()
    {
        return count;
    }

    /**
     * 设总记录条数     * @param count
     * @author nixin
     */
    public void setCount(int count)
    {
        if (pageSize != 0)
        {
            pageCount = count / pageSize;
            if (count % pageSize != 0)
            {
                pageCount++;
            }
        }
        this.count = count;
    }
    
    /**
     * 图书章节内容设总记录条数
     * @param count
     * @author JinHua
     */
    public void setContentCount(int count)
    {
        if (pageSize != 0)
        {
            pageCount = count / pageSize;
        }
        this.count = count;
    }

    public String getListSQL()
    {
        return listSQL;
    }

    public void setListSQL(String listSQL)
    {
        this.listSQL = listSQL;
    }

    public int getPage()
    {
        return page;
    }

    public void setPage(int page)
    {
        this.page = page;
    }

    public int getPageCount()
    {
        return pageCount;
    }

    public void setPageCount(int pageCount)
    {
        this.pageCount = pageCount;
    }

    public int getPageSize()
    {
        return pageSize;
    }

    public void setPageSize(int pageSize)
    {
        this.pageSize = pageSize;
    }

    public String getTotalCountSQL()
    {
        return totalCountSQL;
    }

    public void setTotalCountSQL(String totalCountSQL)
    {
        this.totalCountSQL = totalCountSQL;
    }

	public String getHref()
	{
		return href;
	}

	public void setHref(String href)
	{
		this.href = href;
	}

	public String getMethod()
	{
		return method;
	}

	public void setMethod(String method)
	{
		this.method = method;
	}

	public String getPageSizeId()
	{
		return pageSizeId;
	}

	public void setPageSizeId(String pageSizeId)
	{
		this.pageSizeId = pageSizeId;
	}

	public int getPrePage()
	{
		return prePage;
	}

	public void setPrePage(int prePage)
	{
		this.prePage = prePage;
	}

	public int getNextPage()
	{
		return nextPage;
	}

	public void setNextPage(int nextPage)
	{
		this.nextPage = nextPage;
	}

	public int[] getPageNumbers()
	{
		return pageNumbers;
	}

	public void setPageNumbers(int[] pageNumbers)
	{
		this.pageNumbers = pageNumbers;
	}

	public List<Object> getList()
	{
		return list;
	}

	public void setList(List<Object> list)
	{
		this.list = list;
	}

	public Object[] getValues()
	{
		return values;
	}

	public void setValues(Object[] values)
	{
		this.values = values;
	}
	
	public void setValues(ArrayList<Object> values)
	{
		if(values != null && values.size() > 0)
		{
			this.values = new Object[values.size()];
			values.toArray(this.values);
		}
	}

	public String getForm()
	{
		return form;
	}

	public void setForm(String form)
	{
		this.form = form;
	}
	
	/**
	 * 
	 * @param index
	 * @return
	 */
	public boolean range(int itemCount)
	{
		int offsetStart = (getPage()-1)*getPageSize();
		int offsetEnd = offsetStart + getPageSize();
		return itemCount > offsetStart && itemCount <= offsetEnd;
	}
}
