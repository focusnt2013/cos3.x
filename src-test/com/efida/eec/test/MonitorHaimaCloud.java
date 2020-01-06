package com.efida.eec.test;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.zookeeper.data.Stat;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

import com.focus.cos.control.Module;
import com.focus.cos.web.util.RsaKeyTools;
import com.focus.util.HttpUtils;
import com.focus.util.IOHelper;
import com.focus.util.Log;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;


public class MonitorHaimaCloud extends Thread {
	
	private ArrayList<JSONObject> unuse = new ArrayList<JSONObject>();
	private ArrayList<JSONObject> used = new ArrayList<JSONObject>();
	private List<JSONObject> programs = null;
	private HashMap<Integer, JSONObject> reports = new HashMap<Integer, JSONObject>();
	/**
	 * 加载集群中的配置
	 * @param children
	 */
	private void loadArm(JSONArray children, JSONObject parent){
		for(int i = 0; i < children.length(); i++){
			JSONObject e = children.getJSONObject(i);
			if( e.has("ip") ){
				if( e.getInt("id") > 1000000 )
				{
					if( parent != null ){
						String server_name = parent.getString("name");
						e.put("host_name", server_name.substring(6)+"-"+(i+1));
						e.put("server_no", Tools.encodeMD5(e.getString("host_name")));
						e.put("host_info", parent.getString("host_info")+"第"+(i+1)+"块ARM芯片，型号P1024-4XT-CRK(4核)");
						e.put("descript", server_name+"第"+(i+1)+"块ARM芯片");
					}
//					if( unuse.size() < 1 ){
						e.put("ip_info", "当前ARM模块内外IP地址"+e.getString("ip"));
						unuse.add(e);
//					}
				}
			}
			else if(e.has("children")){
				String host_info = "";
				if( parent!=null ){
					host_info = parent.has("host_info")?parent.getString("host_info"):"";
					e.put("host_info", host_info+e.getString("name"));
				}
				this.loadArm(e.getJSONArray("children"), e);
			}
		}
	}
	
	/**
	 * 
	 * @param ip
	 * @param port
	 * @return
	 * @throws Exception
	 */
	private static String report(JSONArray servers)
		throws Exception
	{
		String privatekey = "-----BEGIN RSA PRIVATE KEY-----"+
			"\r\nMIIBOgIBAAJBAK/fACAuh1F2g1jMdgrQJgSQktuB1msm72fPTjfKs2zk28Ir+CHO"+
			"\r\n/PBvlr893K9/D1NEJ/xcwKPQmW2+kD2M8yECAwEAAQJBAI0OuzcUmowIFgke1H6P"+
			"\r\nvC5lFqTgWm3O6K3GY+Hzvj1TbM9nXhEw54OwxXl9pi/8LTyoHv+Iqhs7rdvSb/Yr"+
			"\r\nzcECIQDgv3zFHi/zi7tE+rOqaiZqJ3zJgUibfqS/gdC1TDQzbQIhAMhTm44kw3F6"+
			"\r\nP71Fvf951gndQ4D2vu1aIahJUdU2XnoFAiAa4QvtD0Uw3X9SKqGeOf/KJlrYRqwG"+
			"\r\nXMkbVk09wZVkqQIgS3qdEMivTzFllLYTaZAJYnwY9B2EQMAGEHdXuwMaQ9UCIBFe"+
			"\r\nuDSwhfDnavcvn/FouXzisBmSiq0ApKNdV92Wmy82"+
			"\r\n-----END RSA PRIVATE KEY-----";
		privatekey = "-----BEGIN RSA PRIVATE KEY-----"+
			"\r\nMIIBPAIBAAJBAJeLPEntNLymjTTj1tWVvQBxQGScrK3TXUNQWphz+w3HxFN1U7hp"+
			"\r\n/y/0IF5am+5HSTms8PY7E8Uw3RrmhW3CtSUCAwEAAQJAfY9jmGztMRFeFeBg8/5o"+
			"\r\n5qDvFW2qtStXLpq5NW+DJ1p/Q9dnMYF6pqFqyjTte0Dhad0BYzAhE+uhfoJ0YgYz"+
			"\r\nAQIhAOOgGIZZhPJFMoz2tfbRWqYfl5ftzstd/r05eADh4/gRAiEAqm9Aw7+r/B0l"+
			"\r\nGkfLAQbn3OOqhQc1esq+r9V+pZCXX9UCIQCSjLZ9eREhMe/z1bsdp3gnUFu3U0I6"+
			"\r\nqM+jYGakmsq9IQIhAJMoIBlkA5FZcNGyL1uhpM0aXf8ibFCrgd28Tu+RWsJJAiEA"+
			"\r\ntJccW5CtHSFG10DT4MUqNJYRgJAmDeFTp+J+D3da8zo="+
			"\r\n-----END RSA PRIVATE KEY-----";
		PEMParser parser = new PEMParser(new StringReader(privatekey));
		PEMKeyPair pari = (PEMKeyPair)parser.readObject();
        parser.close();
        byte[] pk1 = pari.getPrivateKeyInfo().getEncoded();
        String token = "sdf";
        String timestamp = Tools.getFormatTime("yyyyMMddHHmmss", System.currentTimeMillis());
        String nonce = Tools.getUniqueValue();
        
		byte[] signautreResult = RsaKeyTools.sign(token+timestamp+nonce, pk1);
		String signature = RsaKeyTools.bytes2String(signautreResult);
        String url = "http://localhost:18080/reportmonitor/l8d33i1swd8/"+timestamp+"/"+nonce+"/"+signature;
        JSONObject obj = new JSONObject();
        obj.put("servers", servers);
        Document doc = HttpUtils.post(url, null, ("data="+obj.toString()).getBytes("UTF-8"));
        return doc.getElementsByTag("body").get(0).text();
	}
	
	/**
	 * 初始化所有监控数据
	 * @throws Exception 
	 */
	public void initialze(List<JSONObject> programs) throws Exception
	{
		Random random = new Random();
		this.programs = programs;
		int mu = 100*1024*1024;
		final long Mt = 8589934592L;
		JSONArray servers = new JSONArray();
		for(JSONObject e: unuse ) {
			JSONObject report = new JSONObject();
			report.put("ip", e.getString("ip"));
			report.put("port", e.getInt("port"));
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.DAY_OF_MONTH, -60-random.nextInt(30));
			long systime = calendar.getTimeInMillis();
			report.put("system_uptime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", systime));
			report.put("descript", e.getString("descript"));
			report.put("host_name", e.getString("host_name"));
			report.put("os_name", "安卓/Liunux");
			report.put("os_version", "Android 7.0/Linux 6.5");
			report.put("server_no", e.getString("server_no"));
			report.put("host_info", e.getString("host_info"));
			report.put("ip_info", e.getString("ip_info"));
			servers.put(report);
			reports.put(e.getInt("id"), report);

			JSONObject cpu = new JSONObject();
			int usage = random.nextInt(10);
			cpu.put("usage", ((double)usage)/100);
			report.put("cpu", cpu);
			JSONObject mem = new JSONObject();
			int used = random.nextInt(mu);
			mem.put("used", used);
			double d = ((double)used)/Mt;
			mem.put("usage", d);
			report.put("mem", mem);
			JSONObject net = new JSONObject();
			int input = random.nextInt(64*1024);
			int output = random.nextInt(64*1024);
			net.put("input", input);
			net.put("output", output);
			report.put("net", net);
			JSONObject io = new JSONObject();
			io.put("write", 0);
			io.put("read", 0);
			report.put("io", io);
			JSONObject disk = new JSONObject();
			disk.put("usage", 0);
			disk.put("used", 0);
			disk.put("storages_info", "无磁盘");
			report.put("disk", disk);
			JSONArray modules = new JSONArray();
			JSONObject control = new JSONObject();
			control.put("id", "eec");
			control.put("name", "嵌入式版权云服务");
			control.put("startup_time", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", systime+10000));
			control.put("timestamp", System.currentTimeMillis());
			control.put("version", "1.3.3.2");
			control.put("log_path", "/efida/eec/logs");
			control.put("remark", "管控ARM芯片");
			control.put("programer", "超级管理员");
			control.put("programer_email", "zhangguobao@haimacloud.com");
			control.put("state", Module.STATE_STARTUP);
			control.put("run_info", "运行中");
			int min = 13412+random.nextInt(1024);
			int max = 29035+random.nextInt(1024);
			control.put("mem_usage", String.format("%sK/%sK", min, max));
			control.put("run_time", (System.currentTimeMillis()-systime)/1000);
			modules.put(control);
			report.put("modules", modules);
			if( servers.length() >= 256 ){
				Log.msg(String.format("Succeed to build %s reports.", servers.length()));
				String result = report(servers);
				Log.msg(String.format("Succeed to get the response of report:%s", result));
				servers = new JSONArray();
			}
			File file = new File("../data/haimacloud/"+e.getInt("id")+".json");
			if( file.exists() ){
				try{
					byte[] payload = IOHelper.readAsByteArray(file);
					JSONArray data = new JSONArray(new String(payload, "UTF-8"));
					for(int j = 1; j < data.length(); j++)
					{
						JSONObject a = data.getJSONObject(j);
						modules.put(a);
					}
						
				}
				catch(Exception e1){
				}
			}
		}
		if( servers != null && servers.length() > 0 ){
			Log.msg(String.format("Succeed to build %s reports.", servers.length()));
			String result = report(servers);
			Log.msg(String.format("Succeed to get the response of report:%s", result));
		}
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				synchronized(reports){
					running = false;
					reports.notify();
				}
			}
		});
	}
	private boolean running = true;
	public void run()
	{
		final long G1 = 8589934592L/8;
		final long Gt = 8589934592L;
		final int M = 1024*1024*1024;
		Random random = new Random();
		//每15秒随机更新2000个服务器
		while(running){
			synchronized (reports) {
				try {
					reports.wait(7000+random.nextInt(3000));
				} catch (InterruptedException e) {
					Log.err(e);
				}
			}
			int count = 0;
			if( !running ) {
				break;
			}
			if( unuse.isEmpty() ){
				ArrayList<JSONObject> swap = unuse;
				unuse = used;
				used = swap; 
			}
			JSONArray servers = new JSONArray();
			while( count < 256 && !unuse.isEmpty() ){
				int i = random.nextInt(unuse.size());
				JSONObject e = unuse.remove(i);
				used.add(e);
				count += 1;
				JSONObject report = reports.get(e.getInt("id"));
				if( report != null ){
					JSONArray modules = report.getJSONArray("modules");
					JSONObject program = this.programs.get(random.nextInt(this.programs.size()));
					JSONObject game = null;
					JSONObject runprogam = modules.getJSONObject(0);
					long ts = runprogam.getLong("timestamp");
					runprogam.put("run_time", (System.currentTimeMillis()-ts)/1000);
					for(int k = 1; k < modules.length(); k++ ){
						runprogam = modules.getJSONObject(k);
						runprogam.put("state", Module.STATE_SHUTDOWN);
						runprogam.put("run_info", "已停止");
						ts = runprogam.getLong("timestamp");
						runprogam.put("run_time", (System.currentTimeMillis()-ts)/1000);
						if(runprogam.getString("id").equals(program.getString("id"))){
							game = runprogam;
							break;
						}
						else if( modules.length() > 10 ) {
							modules.remove(k);
							k -= 1;
						}
					}
					try{
						File file = new File("../data/haimacloud/"+e.getInt("id")+".json");
						IOHelper.writeFile(file, modules.toString().getBytes("UTF-8"));
					}
					catch(Exception e1){
					}
					JSONObject mem = report.getJSONObject("mem");
					JSONObject net = report.getJSONObject("net");
					if( random.nextBoolean() ){
						if( game == null ){
							game = new JSONObject();
							game.put("id", program.getString("id"));
							game.put("name", program.getString("name"));
							game.put("startup_time", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
							game.put("timestamp", System.currentTimeMillis());
							game.put("version", program.getString("version"));
							game.put("log_path", "/efida/eec/logs/"+program.getString("id"));
							game.put("remark", program.getString("description"));
							game.put("programer", program.getJSONObject("maintenance").getString("programmer"));
							game.put("programer_email", program.getJSONObject("maintenance").getString("email"));
							modules.put(game);
							game.put("run_time", 10);
						}
						game.put("run_info", "运行中");
						int usage = random.nextInt(62);
						report.getJSONObject("cpu").put("usage", ((double)usage)/100);

						long used = 2L*random.nextInt(1189934592);
						long used_max = random.nextInt(1189934592)+G1*random.nextInt(7);
						game.put("mem_usage", String.format("%sK/%sK", used/1024, used_max/1024));
						game.put("state", Module.STATE_STARTUP);
					
						used += random.nextInt(1189934592);
						mem.put("used", used);
						double d = ((double)used)/Gt;
						mem.put("usage", d);
						
						int input = random.nextInt(M/100);
						int output = random.nextInt(M)+random.nextInt(M);
						net.put("input", input);
						net.put("output", output);
					}
					else{
						int usage = random.nextInt(10);
						report.getJSONObject("cpu").put("usage", ((double)usage)/100);

						final int mu = 100*1024*1024;
						int used = random.nextInt(mu);
						mem.put("used", used);
						double d = ((double)used)/Gt;
						mem.put("usage", d);
						
						int input = random.nextInt(64*1024);
						int output = random.nextInt(64*1024);
						net.put("input", input);
						net.put("output", output);
					}
					servers.put(report);
				}
			}
			try {
				String result = report(servers);
				Log.msg(String.format("Succeed to report for %s arms: %s", servers.length(), result));
			} catch (Exception e1) {
				Log.err("Failed to report:", e1);
				break;
			}
		}
		Log.msg("The monitor over.");
	}
	
	public void close(){
		synchronized(reports){
			running = false;
			reports.notify();
		}	
	}
	
	public static void main(String[] args) {
		Zookeeper local = null;
		MonitorHaimaCloud monitor = null;
		try
		{
			String host = args.length>0?args[0]:"localhost";
	        //启动日志管理器
	        Log.getInstance().setSubroot("MonitorHaimaCloud");
	        Log.getInstance().setDebug(true);
	        Log.getInstance().setLogable(true);
	        Log.getInstance().start();
			//先ZK读取程序数据
			local = Zookeeper.getInstance(host, 9088);
			Stat stat = local.exists("/cos/config/monitor/clusters");
			if( stat != null ){
				byte[] payload = local.getData("/cos/config/monitor/clusters", false, stat, true);
				JSONArray clusters = new JSONArray(new String(payload, "UTF-8"));
				monitor = new MonitorHaimaCloud();
				monitor.loadArm(clusters, null);
				Log.msg(String.format("Succeed to load %s arms.", monitor.unuse.size()));
				List<JSONObject> programs = local.getJSONObjects("/cos/config/modules/Sys/program");
				local.close();
				Log.msg(String.format("Succeed to read %s programs.", programs.size()));
				monitor.initialze(programs);
				monitor.start();
			}
		} 
		catch (Exception e) 
		{
			Log.err(e);
			if( monitor != null ){
				monitor.close();
			}
		}
	}

}
