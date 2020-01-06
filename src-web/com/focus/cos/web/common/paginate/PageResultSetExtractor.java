package com.focus.cos.web.common.paginate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;


public class PageResultSetExtractor implements ResultSetExtractor
{
	private final int start;// 起始行号 
	private final int length;// 结果集合的长度 
	private final RowMapper rowMapper;// 行包装器 
	public PageResultSetExtractor(RowMapper rowMapper, int start, int length) 
	{ 
		Assert.notNull(rowMapper, "RowMapper is required"); 
		this.rowMapper = rowMapper; 
		this.start = start; 
		this.length = length; 
	} 
	
	public Object extractData(ResultSet rs) throws SQLException, DataAccessException 
	{ 
		List<Object> result = new ArrayList<Object>(); 
		int rowNum = 0; 
		int end = start + length; 
		point: while (rs.next()) 
		{ 
			++rowNum; 
			if (rowNum < start) 
			{ 
				continue point; 
			} 
			else if (rowNum >= end) 
			{ 
				break point; 
			} 
			else 
			{ 
				result.add(this.rowMapper.mapRow(rs, rowNum)); 
			} 
		} 
		return result; 
	} 
}
