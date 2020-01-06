package com.focus.cos.user.dao;

import java.util.List;

import com.focus.cos.api.Sysuser;
import com.focus.sql.ConnectionPool;
import com.focus.sql.Dao;

public class UserDaoTest extends Dao
{
    public static void main(String args[])
    {
		try
		{
//			Thread.sleep(1000);
//			standby = new H2(9092, 0);
//			standby.start();
//			Thread.sleep(1000);
//			master = new H2(9092, 9192);
//			master.start();
//			Thread.sleep(1000);

	    	System.setProperty("cos.jdbc.url", "jdbc:h2:tcp://localhost:9292,127.0.0.1:9192/../h2/cos");//"jdbc:h2:tcp://127.0.0.1:19092,127.0.0.1:19192/./h2/cos");//"jdbc:h2:tcp://127.0.0.1:9092/./h2/cos");
	        System.setProperty("cos.jdbc.driver", "org.h2.Driver");
	        System.setProperty("cos.jdbc.user", "sa");
	        System.setProperty("cos.jdbc.password", "");

            String dbDriver = "org.h2.Driver";//System.getProperty("cos.jdbc.driver", "");
            String dbUser = "sa";//System.getProperty("cos.jdbc.user", "");
            String dbPassword = "";//System.getProperty("cos.jdbc.password", "");
            //jdbc:h2:tcp://localhost:9092,10.10.10.94:9092/../h2/cos
            String dbUrl = System.getProperty("cos.jdbc.url", "");
            Class.forName(dbDriver).newInstance();
//            con = DriverManager.getConnection(dbUrl ,dbUser, dbPassword);
//            H2.initialize(con);

        	ConnectionPool.connect(dbUrl, dbDriver, dbUser, dbPassword);
        	if( ConnectionPool.isConnect() )
        	{
//	        	UserDao dao = new UserDao();
	            List<Sysuser> list = UserDao.query(null, null, null, null, null);
	            StringBuffer log = new StringBuffer("::"+list.size());
				for(Sysuser e : list)
				{
					log.append("\r\n\t["+e.getId()+"] "+e.getUsername()+" "+e.getRealname());
				}
				System.err.println(log.toString());
				
//	            dao.prepareInsert();
//	            Sysuser user = new Sysuser();
//	            user.setUsername("test");
//	            user.setRealname("AVCC");
//	            user.setPassword(Tools.encodeMD5("123456"));
//	            user.setEmail("liu3xue@163.com");
//	            user.setSex(Sex.Male.getValue());
//	            user.setStatus(Status.Enable.getValue());
//	            user.setRoleid((byte)1);
//	            dao.save(user);
//	            user = new Sysuser();
//	            user.setUsername("test1");
//	            user.setRealname("AVCC1");
//	            user.setPassword(Tools.encodeMD5("123456"));
//	            user.setEmail("liu4xue@163.com");
//	            user.setSex(Sex.Male.getValue());
//	            user.setStatus(Status.Enable.getValue());
//	            user.setRoleid((byte)1);
//	            dao.save(user);
//	            System.out.println(dao.execute());
	            
//	            String sql = "INSERT INTO TB_USER(roleid, username, password, realname, email, status, sex)" +
//	                " VALUES(1, 'test3', 'sjdhfskdhf', '真名', 'sdfsdf', 1, 1)";
//	            con = DriverManager.getConnection(dbUrl ,dbUser, dbPassword);
//	            stmt = con.createStatement();
//	            System.out.println("update:"+stmt.executeUpdate(sql));
//	            System.out.println(Dao.getMaxId("TB_USER", "ID"));
//	            dao.close();
	            int i = 0;
	            while( i < 10 )
	            {
					Thread.sleep(3000);
	            	i += 1;
	            	list = UserDao.query(null, null, null, null, null);
		            log = new StringBuffer("::"+list.size());
					for(Sysuser e : list)
					{
						log.append("\r\n\t["+e.getId()+"] "+e.getUsername()+" "+e.getRealname());
					}
					System.err.println(log.toString());
	            }
	            ConnectionPool.disconnect();
        	}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
    }
}
