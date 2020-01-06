package com.focus.cos.web.common.paginate;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * Description:分页接口
 * Create Date:Oct 17, 2008
 * @author Nixin
 *
 * @since 1.0
 */
public interface Paginate
{
	/*实现静态翻页的菜单，只有翻页图标，不显示总条数*/
    //public String getSinglePageMenuAjax(PageBean pageBean);
	/*实现静态翻页的菜单，只有翻页图标*/
    //public String getSimplePageMenuAjax(PageBean pageBean);
	/*实现静态翻页的菜单，完整翻页*/
    //public String getPageMenuAjax(PageBean pageBean);
    /*实现翻页的菜单，只有翻页图标*/
    //public String getSimplePageMenu(PageBean pageBean);
    /*实现翻页的菜单，完整翻页*/
    public String getPageMenu(PageBean pageBean);

    public int getTotalCount(PageBean pageBean) throws Exception;
    
    public int getTotalCount(PageBean pageBean, List<?> listParameter) throws Exception;

    public int getTotalCount(PageBean pageBean, String names[], Object values[])throws Exception;

    public List<?> getList(PageBean pageBean);

    public List<?> getList(PageBean pageBean, List<?> listParameter);
    
    public List<?> getList(PageBean pageBean, String names[], Object values[])throws Exception;
    //jdbcTemplate
    public List<?> getList(final JdbcTemplate jt,final PageBean pageBean,final Object[] values,final ParameterizedRowMapper<Object> rowMapper);
}
