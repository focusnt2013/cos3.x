package com.focus.cos.ops.dao;

import java.util.List;

import com.focus.cos.api.AlarmSeverity;
import com.focus.cos.api.AlarmType;
import com.focus.cos.api.Sysalarm;
import com.focus.sql.ConnectionPool;
import com.focus.sql.Dao;

public class AlarmDaoTest extends Dao
{
    public static void main(String args[])
    {
		try
		{
//			H2.initialize();
			Thread.sleep(1000);

	    	System.setProperty("cos.jdbc.url", "jdbc:h2:tcp://localhost:"+9092+",127.0.0.1:"+9192+"/./h2/cos");
	        System.setProperty("cos.jdbc.driver", "org.h2.Driver");
	        System.setProperty("cos.jdbc.user", "sa");
	        System.setProperty("cos.jdbc.password", "");
	        
            String dbDriver = System.getProperty("cos.jdbc.driver", "");
            String dbUser = System.getProperty("cos.jdbc.user", "");
            String dbPassword = System.getProperty("cos.jdbc.password", "");
            String dbUrl = System.getProperty("cos.jdbc.url", "");
        	ConnectionPool.connect(dbUrl, dbDriver, dbUser, dbPassword);
            AlarmDao dao = new AlarmDao();
            List<Sysalarm> list = dao.query(null, null, null, null,null, null, null);
            System.out.println(list.size());
            dao.prepareInsert();
            Sysalarm alarm = new Sysalarm();
            alarm.setSysid("test");
            alarm.setText("读取告警文件失败！");
            alarm.setTitle("读取告警错误");
            alarm.setCause("");
            alarm.setSeverity(AlarmSeverity.BLACK.getValue());
            alarm.setType(AlarmType.B.getValue());
            alarm.setId("通信告警");
            dao.save(alarm);
            System.out.println(dao.execute());
            System.out.println(Dao.getMaxId("TB_SYSALARM", "ALARMID"));
            list = dao.query(null, null, null, null,null, null, null);
            System.out.println(list.size());
            StringBuffer log = new StringBuffer("::");
			for(Sysalarm a : list)
			{
				log.append("\r\n\t["+a.getAlarmid()+"] "+a.getSysid()+" "+a.getTitle());
			}
			System.err.println(log.toString());
            dao.close();
            ConnectionPool.disconnect();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
    }
}
