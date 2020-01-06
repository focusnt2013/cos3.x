package com.efida.clc.clean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.util.IOHelper;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

/**
 * 清洗从国家新闻出版广总局网站获取的isbn数据。
 * @author think
 *
 */
public class GamesFromCgigc {
	public static void main(String[] args) {
		File baseDir = new File(args.length>0?args[0]:"D:/focusnt/clc/trunk/DESIGN/采集数据/国家新闻出版广电总局游戏版号数据/游戏行业网/");
		File crwaledDir = new File(baseDir, "crwaled");
		File cleanedDir = new File(baseDir, "cleaned");
		HashMap<String, PrintWriter> csvPrintWriters = new HashMap<String, PrintWriter>();
		try{
			System.out.println("清洗目录的删除...");
			if( cleanedDir.exists() ){
				IOHelper.deleteDir(cleanedDir);
			}
			System.out.println("完成清洗目录的删除");
			Thread.sleep(1000);
			cleanedDir.mkdirs();
			clear(crwaledDir);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			Iterator<PrintWriter> iterator = csvPrintWriters.values().iterator();
			while(iterator.hasNext()){
				iterator.next().close();
			}
		}
	}
	
	public static void clear(File dataDir) throws Exception{
		File[] files = dataDir.listFiles();
		for(File file : files){
			if(file.isDirectory()){
				System.out.println(file.getName());
				clear(file);
				continue;
			}
			else if( !file.getName().endsWith(".json")){
				continue;
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			String line = null;
			while( (line=reader.readLine())!= null){
				if( line.isEmpty() ){
					continue;
				}
				BasicDBObject data = (BasicDBObject)JSON.parse(line);
				String isbn = data.getString("isbn_no");
				String approval = data.getString("approval_no");
				String creator = data.getString("copyrighter_name");
				String publisher = data.getString("unit_name");
				String name = data.getString("game_name");
				String type = data.getString("game_type");
				
				JSONObject game = new JSONObject();
				JSONObject work = new JSONObject();
				work = new JSONObject();
				work.put("标题", name);
				work.put("分类", "软件");
				work.put("类型", "游戏");
				work.put("创作者", creator);
				work.put("标签", setTags(type));
				String subtitle = creator+"开发的"+type;
				work.put("副标题", subtitle);
				work.put("简介", "暂无");
				setAuthorship(creator, "http://www.cgigc.com.cn", game);
				game.put("作品信息", work);
//				setRegister(isbn, "游戏文号", publisher, publishDate, "http://www.cgigc.com.cn", game);
//				setRegister(isbn, "游戏版号", publisher, publishDate, "http://www.cgigc.com.cn", game);
				
				System.err.println(data);
			}
			reader.close();
		}
	}
	
	public static JSONArray setTags(String str){
		return null;
	}
	/**
	 * 添加标签，去重
	 * @param tags
	 * @param tag
	 */
	public static void addTag(JSONArray tags, String val){
		if( val == null || val.isEmpty() ){
			return;
		}
		String[] args = null;
		if( (args=val.split("；")) != null && args.length>1 ){
			for(String arg:args){
				addTag(tags, arg);
			}
		}
		else if( (args=val.split("：")) != null && args.length>1 ){
			for(String arg:args){
				addTag(tags, arg);
			}
		}
		else{
			for(int i = 0;i < tags.length(); i++){
				String arg = tags.getString(i);
				if( val.equals(arg)){
					return;
				}
			}
			tags.put(val);
		}
	}
	/**
	 * 设置标签
	 * @param c
	 * @param Tags
	 * @param tags
	 */
	public static boolean setTag(String code, int off, String[] Tags, JSONArray tags){
		if(code.length() <= off){
			return false;
		}
		char c = code.charAt(off);		
		if( c < '0' || c > '9') return false;
		int i = Integer.parseInt(String.valueOf(c));
		if( i >= Tags.length ) {
			return false;
		}
		if( Tags[i].isEmpty() ){
			return false;
		}
		addTag(tags, Tags[i]);
		return true;
	}
	/**
	 * 覆盖
	 * @param c
	 * @param Tags
	 * @param tags
	 * @param index
	 */
	public static boolean coverTag(String code, int off, String[] Tags, JSONArray tags, int index){
		if(code.length() < off){
			return false;
		}
		char c = code.charAt(off);		
		if( c < '0' || c > '9') return false;
		int i = Integer.parseInt(String.valueOf(c));
		if( i >= Tags.length ) {
			return false;
		}
		if( Tags[i].isEmpty() ){
			return false;
		}
		tags.put(index, Tags[i]);
		return true;
	}
	
	/**
	 * 
	 * @param tags
	 * @return
	 */
	public static String getTagsLabel(JSONArray tags){
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < tags.length(); i++){
			if( i > 0 ) sb.append(";");
			sb.append(tags.getString(i));
		}
		return sb.toString();
	}
	
	/**
	 * 设置官方登记数据
	 * @param registerCode
	 * @param publisher
	 * @param publishdate
	 * @param referer
	 * @param data
	 * @return
	 */
	public static JSONObject setRegister(
			String code, 
			String codeType, 
			String publisher, 
			String publishDate,
			String refererUrl, JSONObject data){
		JSONArray registers = null;
		JSONObject copyright = null;
		if( data.has("版权信息") ){
			copyright = data.getJSONObject("版权信息");
		}
		else{
			copyright = new JSONObject();
			data.put("版权信息", copyright);
		}
		if( copyright.has("官方登记") ){
			registers = copyright.getJSONArray("官方登记");
		}
		else{
			registers = new JSONArray();
			copyright.put("官方登记", registers);
		}

		JSONObject e = new JSONObject();
		e.put("登记类别", codeType);
		e.put("登记编号", code);
		e.put("官方机构", "国家新闻出版广电总局");
		e.put("主体类型", "出版单位");
		e.put("主体名称", publisher);
		if( publishDate != null ){
			e.put("登记日期", publishDate);
		}
		e.put("官方地址", refererUrl);
		registers.put(e);
		//材料引用
		JSONObject referer = new JSONObject();
		e.put("引用资料", referer);
		referer.put("说明", "数据来自于国家新闻出版广电总局网站公开资料");
		JSONArray links = new JSONArray();
		referer.put("链接", links);
		JSONObject link = null;
		link = new JSONObject();
		link.put("类型", "url");
		link.put("URL地址", refererUrl);
		link.put("是否公开", true);
		links.put(link);
		return e;
		
	}
	/**
	 * 设置财产权
	 * @param author
	 * @param publisher
	 * @param publishDate
	 * @param data
	 * @return
	 */
	public static JSONObject setProperty(
		String author, 
		String publisher, 
		String publishDate, 
		String refererUrl,
		JSONObject data){
		JSONArray properties = null;
		JSONObject copyright = null;
		if( data.has("版权信息") ){
			copyright = data.getJSONObject("版权信息");
		}
		else{
			copyright = new JSONObject();
			data.put("版权信息", copyright);
		}
		if( copyright.has("财产权") ){
			properties = copyright.getJSONArray("财产权");
		}
		else{
			properties = new JSONArray();
			copyright.put("财产权", properties);
		}
		JSONObject property = null;
		for(int i = 0; i < properties.length(); i++){
			property = properties.getJSONObject(i);
			if( property.getString("使用方").equals(publisher) ){
				//比较出版时间，选择修改出版时间
				if( publishDate != null && property.has("开始日期") ){
					if( publishDate.compareTo(property.getString("开始日期")) > 0 ){
						property.put("开始日期", publishDate);
					}
				}
				break;
			}
			property = null;
		}
		if( property == null ){
			property = new JSONObject();
			property.put("类别", "许可使用");
			property.put("许可方", author);
			property.put("使用方", publisher);
			if( publishDate != null ){
				property.put("开始日期", publishDate);
			}
			JSONArray rightRanges = new JSONArray();
			rightRanges.put("复制权");
			rightRanges.put("发行权");
			rightRanges.put("信息网络传播权");
			property.put("权利范围", rightRanges);
			properties.put(property);
			//材料引用
			JSONObject referer = new JSONObject();
			property.put("引用资料", referer);
			referer.put("说明", "数据来自于国家新闻出版广电总局网站公开资料");
			JSONArray links = new JSONArray();
			referer.put("链接", links);
			JSONObject link = null;
			link = new JSONObject();
			link.put("类型", "url");
			link.put("URL地址", refererUrl);
			link.put("是否公开", true);
			links.put(link);
		}
		return property;
	}
	
	public static void setAuthorship(String author, String refererUrl, JSONObject data){
		JSONArray persionals = null;
		JSONObject copyright = null;
		if( data.has("版权信息") ){
			copyright = data.getJSONObject("版权信息");
		}
		else{
			copyright = new JSONObject();
			data.put("版权信息", copyright);
		}
		if( copyright.has("人身权") ){
			persionals = copyright.getJSONArray("人身权");
		}
		else{
			persionals = new JSONArray();
			copyright.put("人身权", persionals);
		}
		
		String[] args = author.split("，");
		for(String arg : args){
			JSONObject e = new JSONObject();
			if( arg.endsWith("等") ){
				arg = arg.substring(0, arg.length()-1);
				e.put("等", true);
			}
			else if( arg.endsWith("[等]") ){
				arg = arg.substring(0, arg.length()-3);
				e.put("等", true);
			}
			e.put("作者", arg);
			//材料引用
			JSONObject referer = new JSONObject();
			e.put("引用资料", referer);
			referer.put("说明", "数据来自于国家新闻出版广电总局网站公开资料");
			JSONArray links = new JSONArray();
			referer.put("链接", links);
			JSONObject link = null;
			link = new JSONObject();
			link.put("类型", "url");
			link.put("URL地址", refererUrl);
			link.put("是否公开", true);
			links.put(link);
			persionals.put(e);
		}
	}
	
	/**
	 * 格式化日期
	 * @param str
	 * @return
	 */
	public static String formatDateStr(String str){
		String[] args = split(str, ".");
		if( args.length == 2 ){
			String year = args[0];
			String moth = args[1];
			moth = moth.length()==1?("0"+moth):moth;
			return year+"-"+moth+"-01";
		}
		return null;
	}
	
    public static String[] split(String str, String s)
    {
        if( str == null || str.length() == 0 )
        {
            return new String[0];
        }
        int count = 0;
        int i = 0, bi = 0, ei = 0;
        while(ei != -1)
        {

            ei = str.indexOf(s, bi);
            bi = ei + s.length();
            count++;
        }

        ei = 0;
        bi = 0;
        String[] splits = new String[count];
        while(ei != -1)
        {
            ei = str.indexOf(s, bi);
            if(ei == -1)
            {
                splits[i++] = str.substring(bi).trim();
            }
            else
            {
                splits[i++] = str.substring(bi, ei).trim();
            }
            bi = ei + s.length();
        }

        return splits;
    }
    

    public static String md5(String origin)
    {
        try
        {
        	final MessageDigest md = MessageDigest.getInstance("MD5");
            return byteArrayToString(md.digest(origin.getBytes()));
        }
        catch(Exception ex)
        {
        	return null;
        }
    }
    /**
     * 转换字节数组为16进制字串
     * @param b 字节数组
     * @return 16进制字串
     */
    public static String byteArrayToString(byte[] b)
    {
        StringBuffer resultSb = new StringBuffer();
        for(int i = 0; i < b.length; i++)
        {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }
    private final static String[] hexDigits = {
	    "0", "1", "2", "3", "4", "5", "6", "7",
	    "8", "9", "a", "b", "c", "d", "e", "f"
    };
    private static String byteToHexString(byte b)
    {
        int n = b;
        if(n < 0)
        {
            n = 256 + n;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }
}
