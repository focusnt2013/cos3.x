package com.focus.cos.web.common.hibernate;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.type.Type;

/**
 * Description:自定义一个主键生成器 Create Date:Oct 19, 2008
 * @ 使用Long作为主键类型,自动增,支持集群
 * @author Nixin
 * @since 1.0
 */
public class IncrementGenerator implements IdentifierGenerator, Configurable
{
	private static final Log log = LogFactory.getLog(IncrementGenerator.class);
	private Long next;
	private String sql;

	public Serializable generate(SessionImplementor session, Object object)throws HibernateException
	{
		if (sql != null)
		{
			getNext(session.connection());
		}
		return next;
	}

	public void configure(Type type, Properties params, Dialect d)throws MappingException
	{
		String table = params.getProperty("table");
		if (table == null)
			table = params.getProperty(PersistentIdentifierGenerator.TABLE);
		String column = params.getProperty("column");
		if (column == null)
			column = params.getProperty(PersistentIdentifierGenerator.PK);
		String schema = params.getProperty(PersistentIdentifierGenerator.SCHEMA);
		sql = "select max(" + column + ") from " + (schema == null ? table : schema + '.' + table);
		log.info(sql);
	}

	private void getNext(Connection conn) throws HibernateException
	{
		try
		{
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();
			if (rs.next())
			{
				next = rs.getLong(1) + 1;
			}
			else
			{
				next = 1l;
			}
		}
		catch (SQLException e)
		{
			throw new HibernateException(e);
		}
		finally
		{
			try
			{
				conn.close();
			}
			catch (SQLException e)
			{
				throw new HibernateException(e);
			}
		}
	}
}
