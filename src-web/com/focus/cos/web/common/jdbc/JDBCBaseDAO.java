package com.focus.cos.web.common.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.focus.cos.web.common.paginate.PageBean;
import com.focus.cos.web.common.paginate.Paginate;
import com.focus.util.Item;


public class JDBCBaseDAO extends JdbcDaoSupport
{
	private Paginate paginate;
	private PageBean pageBean;
	/**
	 * 清除所有操作日志
	 */
	public void clearLog()
	{
		String sql = "TRUNCATE TABLE TB_SYSLOG";
		this.getJdbcTemplate().execute(sql);
	}
	
	public List<?> queryNotifyFilter()
	{
		String sql = "SELECT DISTINCT FILTER FROM TB_NOTIFIES";
		return this.getJdbcTemplate().query(sql, new FilterMapper());
	}
	
	public List<?> getLogType()
	{
		String sql = "SELECT DISTINCT LOGTYPE FROM TB_SYSLOG";
		return this.getJdbcTemplate().query(sql, new FilterMapper());
	}

	public List<?> getLogSeverity()
	{
		String sql = "SELECT DISTINCT LOGSEVERITY FROM TB_SYSLOG";
		return this.getJdbcTemplate().query(sql, new FilterMapper());
	}

	public List<?> getAlarmType()
	{
		String sql = "SELECT DISTINCT ORGTYPE FROM TB_SYSALARM";
		return this.getJdbcTemplate().query(sql, new FilterMapper());
	}

	public List<?> getAlarmSeverity()
	{
		String sql = "SELECT DISTINCT ORGSEVERITY FROM TB_SYSALARM";
		return this.getJdbcTemplate().query(sql, new FilterMapper());
	}

	public List<?> getNotifyConfig()
	{
		String sql = "SELECT * FROM TB_NOTIFY_CONFIG";
		return this.getJdbcTemplate().query(sql, new FilterMapper());
	}
	
	protected class FilterMapper implements RowMapper 
	{
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException 
		{
			Item item = new Item(rs.getString(1),"");
			return item;
		}
	}

	public Paginate getPaginate()
	{
		return paginate;
	}

	public void setPaginate(Paginate paginate)
	{
		this.paginate = paginate;
	}

	public PageBean getPageBean()
	{
		return pageBean;
	}

	public void setPageBean(PageBean pageBean)
	{
		this.pageBean = pageBean;
	}
}
