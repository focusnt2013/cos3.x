package com.focus.cos.web.common.paginate;

import java.util.List;

import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Description:分页实现类 Create Date:Oct 17, 2008
 * 
 * @author Focus
 * @since 1.0
 */

public class PaginateImp extends HibernateDaoSupport implements Paginate
{
	/**
	 * 生成分页脚本
					<div class='pq-grid-footer pq-pager'>&nbsp;
						<button
							class='ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-button-disabled ui-state-disabled'
							type='button' title='第一页'>
							<span class='ui-button-icon-primary ui-icon ui-icon-seek-first'></span>
							<span class='ui-button-text'></span>
						</button>
						<button
							class='ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-button-disabled ui-state-disabled'
							type='button' title='上一页'>
								<span class='ui-button-icon-primary ui-icon ui-icon-seek-prev'></span>
								<span class='ui-button-text'></span>
						</button>
						<span class='pq-separator'></span>
						<span class='pq-page-placeholder' style='font-size:6pt;'>
							<span>第</span>
							<input tabindex='0' class='ui-corner-all' type='text'>
							<span>页（共</span><span class='total'>27</span><span>页</span><span class='total'>27</span><span>记录）</span>
						</span>
						<button
							class='ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only'
							type='button' title='下一页'>
							<span class='ui-button-icon-primary ui-icon ui-icon-seek-next'></span>
							<span class='ui-button-text'></span>
						</button>
						<button
							class='ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only' 
							type='button' title='尾页'>
							<span class='ui-button-icon-primary ui-icon ui-icon-seek-end'></span>
							<span class='ui-button-text'></span>
						</button>
						<span class='pq-separator'></span>
						<span class='pq-page-placeholder'>
							<span></span>
							<select class='ui-corner-all' style='width:36px'>
								<option value='1'>1</option>
								<option value='10'>10</option>
								<option value='20'>20</option>
								<option value='30'>30</option>
								<option value='40'>40</option>
								<option value='50'>50</option>
								<option value='100'>100</option>
								<option value='500'>500</option>
								<option value='1000'>1000</option>
							</select>
							<span class='pq-separator'></span>
						</span>
					</div>
	 */
	public String getPageMenu(PageBean page)
	{
		StringBuffer str = new StringBuffer("<div class='pq-grid-footer pq-pager'>&nbsp;");
		String form = "document.forms[0]";
		if (page.getForm() != null)
		{
			form = "document.forms['" + page.getForm() + "']";
		}
		String btnFormat = "<button class='ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only%s' "+
			"style='width:32px;height:16px;' type='button' title='%s' onclick=\"%s\">"+
			"<span class='ui-button-icon-primary ui-icon %s'></span><span class='ui-button-text'></span></button>";

		int prev, next;
		prev = page.getPage() - 1;
		next = page.getPage() + 1;
		if (page.getPage() > 1)
		{
			str.append(String.format(btnFormat, "", "第一页", ("" + form + ".elements['pageBean.page'].value=1;doQuery();"), "ui-icon-seek-first"));
			str.append(String.format(btnFormat, "", "上一页", ("" + form + ".elements['pageBean.page'].value=" + prev + ";doQuery();"), "ui-icon-seek-prev"));
		}
		else
		{
			str.append(String.format(btnFormat, " ui-button-disabled ui-state-disabled", "第一页", "", "ui-icon-seek-first"));
			str.append(String.format(btnFormat, " ui-button-disabled ui-state-disabled", "上一页", "", "ui-icon-seek-prev"));
		}

		str.append("<span class='pq-page-placeholder' style='font-size:7pt;'>");
		str.append("<span>第</span>");
		str.append("<input type='input' name='jump' style='width:35px' value='"
					+ page.getPage()
					+ "' class='ui-corner-all' type='text'onKeyUp=\"this.value=this.value.replace(/\\D/g,'')\" onafterpaste=\"this.value=this.value.replace(/\\D/g,'')\" onKeydown=\"if(event.keyCode==13){event.returnValue=false;event.cancel=true;"
					+ form + ".elements['pageBean.page'].value=" + form + ".jump.value;doQuery();}\">");
		str.append("<span>页（共</span><span class='total'>" + page.getPageCount() + "</span><span>页</span><span class='total'>" + page.getCount()
				+ "</span><span>记录）</span>");
		str.append("</span>");

		if (page.getPage() < page.getPageCount())
		{
			str.append(String.format(btnFormat, "", "下一页", ("" + form + ".elements['pageBean.page'].value=" + next + ";doQuery();"), "ui-icon-seek-next"));
		}
		else
		{
			str.append(String.format(btnFormat, " ui-state-focus ui-button-disabled ui-state-disabled", "下一页", "", "ui-icon-seek-next"));
		}

		if (page.getPageCount() > 1 && page.getPage() != page.getPageCount())
		{
			str.append(String.format(btnFormat, "", "尾页", ("" + form + ".elements['pageBean.page'].value=" + page.getPageCount() + ";doQuery();"),
										"ui-icon-seek-end"));
		}
		else
		{
			str.append(String.format(btnFormat, " ui-state-focus ui-button-disabled ui-state-disabled", "尾页", "", "ui-icon-seek-end"));
		}

		str.append("<span class='pq-page-placeholder'>");
//		str.append("<span></span>");
		str.append("<select name='pageBean.pageSize' onchange='doQuery();' class='ui-corner-all' style='width:47px'>");
		final int pagesizes[] = {1, 10, 15, 20, 30, 40, 50, 100, 500, 1000};
		for(int pagesize : pagesizes)
		{
			str.append("<option value='"+pagesize+"'");
			if( page.getPageSize() == pagesize )
				str.append("selected");
			str.append(">"+pagesize+"</option>");
		}
		str.append("</select>");
		str.append("</span>");
		str.append("<INPUT type='hidden' value=" + page.getPage() + " name=\"pageBean.page\" > ");
		return str.toString();
	}

	/**
	 * 根据pageBean取记录 (non-Javadoc)
	 * @author Focus
	 */
	public List<?> getList(PageBean pageBean)
	{
		Session session = super.getSession();
		try
		{
			Query q = session.createQuery(pageBean.getListSQL());
			q.setFirstResult((pageBean.getPage() - 1) * pageBean.getPageSize());
			q.setMaxResults(pageBean.getPageSize());
			List<?> list = q.list();
			// session.flush();
			return list;
		}
		catch (Exception e)
		{
			return null;
		}
		finally
		{
			session.setFlushMode(FlushMode.AUTO);
			this.releaseSession(session);
		}
	}

	/**
	 * 根据pageBean取记录 (non-Javadoc)
	 * @author Focus
	 */
	public List<?> getList(PageBean pageBean, List<?> listParameter)
	{
		Session session = super.getSession();
		try
		{
			Query query = session.createQuery(pageBean.getListSQL());
			for (int i = 0; listParameter != null && i < listParameter.size(); i++)
			{
				query.setParameter(i, listParameter.get(i));
			}
			query.setFirstResult((pageBean.getPage() - 1) * pageBean.getPageSize());
			query.setMaxResults(pageBean.getPageSize());
			List<?> list = query.list();
			return list;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		finally
		{
			session.setFlushMode(FlushMode.AUTO);
			this.releaseSession(session);
		}
	}

	/**
	 * 根据pageBean和参数取记录 (non-Javadoc)
	 * @author Focus
	 */
	public List<?> getList(PageBean pageBean, String[] names, Object[] values) throws Exception
	{
		Session session = super.getSession();
		try
		{
			Query query = session.createQuery(pageBean.getListSQL());
			for (int i = 0; i < names.length; i++)
			{
				query.setParameter(names[i], values[i]);
			}
			query.setFirstResult((pageBean.getPage() - 1) * pageBean.getPageSize());
			query.setMaxResults(pageBean.getPageSize());

			List<?> list = query.list();
			// releaseSession(session);
			return list;
		}
		catch (Exception e)
		{
			return null;
		}
		finally
		{
			session.setFlushMode(FlushMode.AUTO);
			this.releaseSession(session);
		}
	}

	/**
	 * 重载方法,取总记录条数
	 */
	public int getTotalCount(PageBean pageBean, List<?> listParameter) throws Exception
	{
		Session session = super.getSession();
		try
		{
			Query query = session.createQuery(pageBean.getTotalCountSQL());
			for (int i = 0; listParameter != null && i < listParameter.size(); i++)
			{
				query.setParameter(i, listParameter.get(i));
			}
			// Long count = (Long)query.uniqueResult();
			long count = 0;
			List<?> list = query.list();
			if (list != null)
			{
				for (Long ret : (List<Long>) list)
				{
					if (ret != null)
					{
						count += ret;
					}
				}
			}
			return (int) count;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return 0;
		}
		finally
		{
			session.setFlushMode(FlushMode.AUTO);
			this.releaseSession(session);
		}
	}

	/**
	 * 重载方法,取总记录条数 (non-Javadoc)
	 * 
	 */
	public int getTotalCount(PageBean pageBean, String[] names, Object[] values) throws Exception
	{
		List<?> list = getHibernateTemplate().findByNamedParam(pageBean.getTotalCountSQL(), names, values);
		long count = 0;
		// if (list.size() > 0)
		// {
		// count = ((Integer) list.get(0)).intValue();
		// }
		for (int i = 0; i < list.size(); i++)
		{
			Integer ret = (Integer) list.get(0);
			if (ret != null)
			{
				count += ret;
			}
		}

		return (int) count;
	}

	/**
	 * 取总记录条数 (non-Javadoc)
	 */
	public int getTotalCount(PageBean pageBean) throws Exception
	{
		List<?> list = getHibernateTemplate().find(pageBean.getTotalCountSQL());
		int count = 0;
		// if (list.size() > 0)
		// {
		// count = ((Long) list.get(0)).intValue();
		// }
		for (int i = 0; i < list.size(); i++)
		{
			Integer ret = ((Long) list.get(0)).intValue();
			if (ret != null)
			{
				count += ret;
			}
		}
		return (int) count;
	}

	// jdbcTelplate
	public List<?> getList(final JdbcTemplate jt, final PageBean pageBean, final Object[] values, final ParameterizedRowMapper<Object> rowMapper)
	{
		// final int rowCount = jt.queryForInt(pageBean.getTotalCountSQL(),
		// values);
		int rowCount = 0;
		if (values != null && values.length > 0)
		{
			rowCount = jt.queryForInt(pageBean.getTotalCountSQL(), values);
		}
		else
		{
			rowCount = jt.queryForInt(pageBean.getTotalCountSQL());
		}

		// List<?> retList = null;
		// if(values != null && values.length > 0)
		// {
		// retList = jt.queryForList(pageBean.getTotalCountSQL(), values);
		// }
		// else
		// {
		// retList = jt.queryForList(pageBean.getTotalCountSQL());
		// }
		// if(retList != null)
		// {
		// for(ListOrderedMap loMap:(List<ListOrderedMap>)retList)
		// {
		// if(loMap != null)
		// {
		// Object countObj = loMap.get("COUNT(*)");
		// if(countObj != null)
		// {
		// if(countObj instanceof java.lang.Long)
		// {
		// rowCount += (Long)countObj;
		// }
		// else
		// {
		// java.math.BigDecimal value = (java.math.BigDecimal)countObj;
		// if(value != null)
		// {
		// rowCount += value.longValue();
		// }
		// }
		// }
		//    				
		// }
		// }
		// }
		pageBean.setCount(rowCount);
		// fetch a single page of results
		final int startRow = (pageBean.getPage() - 1) * pageBean.getPageSize() + 1;
		List<Object> queryList = null;
		if (values != null && values.length > 0)
		{
			queryList = (List<Object>) jt.query(pageBean.getListSQL(), values, new PageResultSetExtractor(rowMapper, startRow, pageBean.getPageSize()));
		}
		else
		{
			queryList = (List<Object>) jt.query(pageBean.getListSQL(), new PageResultSetExtractor(rowMapper, startRow, pageBean.getPageSize()));
		}
		return queryList;
	}
}
