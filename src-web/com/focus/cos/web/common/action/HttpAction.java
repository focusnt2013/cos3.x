package com.focus.cos.web.common.action;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.select.Elements;

import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.util.HttpUtils;
import com.focus.util.IOHelper;
import com.focus.util.Tools;
import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.ActionSupport;

public class HttpAction extends ActionSupport
{
	private static final Log log = LogFactory.getLog(HttpAction.class);
	private static final long serialVersionUID = 3343534055887470048L;
	/* 链接地址 */
	private String link;
	/*适配宽度*/
	private int width = 0;
	/*高度适配*/
	private int height = 0;
	/*取最小适配*/
	private int scale = 0;
	/*数据输出流*/
	private InputStream dataStream = null;
	/*内容内心*/
	private static MimeTypes AllTypes = MimeTypes.getDefaultMimeTypes();
	/**
	 * 预览图片
	 * @return
	 */
	public String image32()
	{
		this.width = 32;
		return image();
	}
	
	public String image22()
	{
		this.width = 22;
		return image();
	}
	
	public String image80()
	{
		this.width = 80;
		return image();
	}
	
	/**
	 * 调整图片尺寸
	 * @return
	 */
	public String imagezoom()
	{
		width = 16;
		height = 16;
		if( link == null || !link.startsWith("http:/") )
		{
			//尝试用Unicode解码
			try
			{
				if( link.endsWith(".js") || link.endsWith(".css") ){
					int i = link.lastIndexOf(".");
					link = link.substring(0, i);
				}
				link = Tools.decodeUnicode(this.link);//编码后的链接地址
			}
			catch(Exception e)
			{
			}
			if( link == null || !link.startsWith("http:/") )
				return null;
		}
		return image();
	}
	
	/**
	 * 从链接得到图片数据，然后自动压缩
	 * @return
	 */
	public String image()
	{
		long ts = System.currentTimeMillis();
		if( link == null )
		{
			return this.defaultImage();
		}
		if( !link.startsWith("http://") && !link.startsWith("https://") )
		{
			link = Kit.URL(ServletActionContext.getRequest())+link;
		}
		int i = link.lastIndexOf("http");
		link = link.substring(i);
		boolean cachable = false;
		byte buffer[] = null;
		String type = "jpg";
		OutputStream out = null;
		FSDirectory directory = null;
		//根据链接去下载图片
		HttpURLConnection connection = null;
		try
		{
			if( scale == 0 && width == 0 && height == 0 )//缺省情况下才取缓存
			{
				File indexPath = new File(PathFactory.getDataPath(), "cachimg/");
				if( indexPath.exists() )
				{//从索引中取缓存文件
					IndexSearcher searcher = null;
					IndexReader reader = null;
					try
					{
						directory = FSDirectory.open(indexPath);
						reader = IndexReader.open(directory);
						searcher = new IndexSearcher(reader);
						TermQuery query = new TermQuery(new Term("url", link));
						ScoreDoc[] docs = searcher.search(query, 1).scoreDocs;
						if( docs != null && docs.length > 0 )
						{
							log.info("<"+(System.currentTimeMillis()-ts)+">Found cach image for "+link);
							Document e = searcher.doc(docs[0].doc);
							buffer = e.getBinaryValue("payload");
							if( buffer != null )
							{
								type = e.get("type");
						        HttpServletResponse response = null;
						        response = ServletActionContext.getResponse();
								response.setContentType("image/"+type);
								response.setHeader("Content-disposition", "inline; filename="+java.util.UUID.randomUUID()+"."+type);
								out = response.getOutputStream();
								out.write(buffer);
								out.flush();
								out.close();
								return null;
							}
						}
					}
					catch(Exception e)
					{
						log.error("Failed to get cach from "+link, e);
					}
					finally
					{
						if( searcher != null ) searcher.close();
						if( reader != null ) reader.clone();
					}
					if( System.currentTimeMillis()-ts > 1000 )
						log.warn("<"+(System.currentTimeMillis()-ts)+">Too long time to search cach from "+indexPath);
				}
				else
				{
					directory = FSDirectory.open(indexPath);
				}
				cachable = true;
			}
			URLConnection theConnection = new URL(link).openConnection();
	        if( !(theConnection instanceof HttpURLConnection) )
	        {
	    		return defaultImage();
	        }
	        int responseCode = 0;
	        if( link.startsWith("https") )
	        {
	        	HttpsURLConnection connection1 = (HttpsURLConnection) theConnection;
	        	connection1.setDoInput(true);  
	        	connection1.setDoOutput(true);  
	        	connection1.setRequestMethod( "GET" );
	        	connection1.setConnectTimeout( 7000 );
	        	connection1.setReadTimeout( 15000 );
	        	//connection1.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.8.0.2) Gecko/20060308 Firefox/1.5.0.2" );
	        	connection1.setRequestProperty("User-Agent", "Opera/9.23 (Windows NT 5.1; U; en)" );
	        	connection1.connect();
	        	responseCode = connection1.getResponseCode();
	        	connection = connection1;
	        }
	        else
	        {
		        connection = (HttpURLConnection) theConnection;
		        connection.setRequestMethod( "GET" );
		        connection.setConnectTimeout( 7000 );
		        connection.setReadTimeout( 15000 );
//		        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.8.0.2) Gecko/20060308 Firefox/1.5.0.2" );
		        connection.setRequestProperty("User-Agent", "Opera/9.23 (Windows NT 5.1; U; en)" );
		        connection.connect();
		        responseCode = connection.getResponseCode();
	        }
	        if(responseCode!=200)
	        {
	        	String err = responseCode+"\t"+connection.getContentType()+"\t"+connection.getResponseMessage();//+"\t"+connection.getContent();
	        	log.error("Failed to get the image from "+link+" for "+err);
//	        	System.out.println(err);
	    		return errorImage(responseCode);
	        }
//			System.out.println("ContentType"+connection.getContentType());
	        String contentType = connection.getContentType();
	        if( contentType.indexOf("image") ==-1 )
	        {
	    		return defaultImage();
	        }
	        type = contentType.substring(contentType.indexOf("/")+1);
	        int contentLength = connection.getContentLength();
	        if( contentLength <= 0 )
	        {
	        	contentLength = 1024*1024;
	        }
	        buffer = new byte[contentLength];
	        int len = -1, off = 0;
	        InputStream inputStream = connection.getInputStream();
	        while( ( len = inputStream.read(buffer, off, contentLength) ) != -1 )
	        {
	        	contentLength -= len;
	        	off += len;
	        	if( contentLength == 0 ) break;
	        }
	        if( off == 0 )
	        {
	        	log.warn("<"+(System.currentTimeMillis()-ts)+">Failed to catch images for error inputstream "+link);
	    		return defaultImage();
	        }
//        	System.out.println(off);
	        inputStream.close();
			inputStream = new ByteArrayInputStream(buffer, 0, off);
	        Image srcImage = ImageIO.read(inputStream);
	        int oldWidth = srcImage.getWidth( null );
	        int oldHeight = srcImage.getHeight( null );
	        int scaleSize = scale>0?scale:(this.width>this.height?this.width:this.height);
	        int scaleType = scale>0?(oldWidth<oldHeight?1:0):(this.width>this.height?0:1);
	        if( scaleSize == 0 )
	        {
	        	scaleSize = 1600;
	        }
	        
	        //如果原始图片的宽度小于请求的宽度那么，按原始图片大小预览
	        if( scaleType == 0 && oldWidth < scaleSize )
	        {
	        	scaleSize = 0;
	        }
	        if( scaleType == 1 && oldHeight < scaleSize )
	        {
	        	scaleSize = 0;
	        }
	        HttpServletResponse response = null;
	        response = ServletActionContext.getResponse();
	        if( scaleSize > 0 )
	        {
		        double rate = scaleSize;
	            rate /= scaleType==0?oldWidth:oldHeight;
		        int sacledWidth = ( int ) ( oldWidth * rate );
		        int sacledHeight = ( int ) ( oldHeight * rate );
		        Image scaledImage = srcImage.getScaledInstance(
		            sacledWidth, sacledHeight, Image.SCALE_DEFAULT );
		        //写文件
		        BufferedImage bi = new BufferedImage(
		            sacledWidth, sacledHeight, BufferedImage.TYPE_INT_RGB );
		        bi.getGraphics().drawImage(scaledImage, 0, 0, sacledWidth, sacledHeight, null );
		        type = "jpg";
		        ByteArrayOutputStream baos = new ByteArrayOutputStream();
		        ImageIO.write(bi, "JPEG", baos);
		        buffer = baos.toByteArray();
	        }
			response.setContentType("image/"+type);
			response.setHeader("Content-disposition", "inline; filename="+java.util.UUID.randomUUID()+"."+type);
			out = response.getOutputStream();
			out.write(buffer);
			out.flush();
			inputStream.close();
			if( cachable )
			{
				Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
				IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_36,  analyzer);
				IndexWriter indexWriter = new IndexWriter(directory,conf);
				org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
				doc.add(new Field("url", link, Store.YES, Index.NOT_ANALYZED));
				doc.add(new Field("payload", buffer, 0, buffer.length));
				doc.add(new Field("type", type, Store.YES, Index.NO));
				doc.add(new Field("time", Tools.getFormatTime("yyyy-MM-dd HH:mm", System.currentTimeMillis()), Store.YES, Index.NOT_ANALYZED));
				indexWriter.addDocument(doc);
				indexWriter.close();
				log.info("<"+(System.currentTimeMillis()-ts)+">Cach image for "+link);
			}
			return null;
		}
		catch(SocketTimeoutException e)
		{
			log.error("<"+(System.currentTimeMillis()-ts)+">Failed(SocketTimeoutException) to get image from "+link +".");
		}
		catch(Exception e)
		{
			log.error("<"+(System.currentTimeMillis()-ts)+">Failed to get image from "+link, e);
		}
		finally
		{
			if( out != null )
				try
				{
					out.close();
				}
				catch (IOException e)
				{
				}
			if( connection != null )
				connection.disconnect();
		}
		
		return defaultImage(type);
	}
	
	/**
	 * 根据内容错误码
	 * @return
	 */
	public String errorImage(int errorCode)
	{
		HttpServletResponse response = null;
		OutputStream out = null;
		byte[] payload = null;
		try
		{
			File file = new File(ServletActionContext.getServletContext().getRealPath(""),"images/"+errorCode+".png");
			if( !file.exists() )
			{
				file = new File(ServletActionContext.getServletContext().getRealPath(""),"images/error.png");
			}
			payload = IOHelper.readAsByteArray(file);
			response = ServletActionContext.getResponse();
			response.setContentType("image/png");
			response.setHeader("Content-disposition", "inline; filename=focusnt_inc.png");
        	out = response.getOutputStream();
        	out.write(payload);
			out.flush();
			out.close();
		}
		catch( Exception e )
		{
			log.error("Failed to get default image", e);
		}
		return null;
	}
	
	/**
	 * 显示缺省图片
	 * @return
	 */
	public String defaultImage()
	{
//		try
//		{
//			File file = new File(ServletActionContext.getServletContext().getRealPath(""),"images/icon/delete.gif");
//			FileInputStream fileInputStream = new FileInputStream(file);
//			byte[] payload = new byte[fileInputStream.available()];
//			fileInputStream.read(payload);
//			dataStream = new ByteArrayInputStream(payload);
//		}
//		catch( Exception e )
//		{
//			e.printStackTrace();
//		}
//		return "gif";
		return this.defaultImage("jpg");
	}
	/* 缓存缺省图片 */
	private HashMap<String,byte[]> mapPayload = new HashMap<String, byte[]>();	
	public String defaultImage(String type)
	{
		HttpServletResponse response = null;
		OutputStream out = null;
		byte[] payload = null;
		try
		{
			type = type.toLowerCase();
			if( !mapPayload.containsKey(type) )
			{
				File file = new File(ServletActionContext.getServletContext().getRealPath(""),"images/filetype/"+type+".png");
				if( !file.exists() )
				{
					file = new File(ServletActionContext.getServletContext().getRealPath(""),"images/filetype/bmp.png");
					type = "bmp";
				}
				payload = IOHelper.readAsByteArray(file);
				mapPayload.put(type, payload);
			}
			else
			{
				payload = mapPayload.get(type);
			}
			response = ServletActionContext.getResponse();
			response.setContentType("image/"+type);
			response.setHeader("Content-disposition", "inline; filename=focusnt_inc."+type);
        	out = response.getOutputStream();
        	out.write(payload);
			out.flush();
			out.close();
		}
		catch( Exception e )
		{
			log.error("Failed to get default image", e);
		}
		return null;
	}
	/**
	 * 预览WAP网页
	 * @return
	 */
	private String fontSize = null; 
	public String doGetWml8pt() throws Exception
	{
		fontSize = "8pt"; 
		return doGetWml();
	}

	public String doGetWml9pt() throws Exception
	{
		fontSize = "9pt"; 
		return doGetWml();
	}
	
	public String doGetWml() throws Exception
	{

		String url = "http!get.action?link=";
		if( "9pt".equals(fontSize) )
		{
			url = "http!get9pt.action?link=";
		}
		else if( "8pt".equals(fontSize) )
		{
			url = "http!get8pt.action?link=";
		}
		else
		{
			fontSize = "9pt";
		}
		link = link.replaceAll(",,", "&");
		if( !link.startsWith("http://") )
		{
			link = Kit.URL_PATH(ServletActionContext.getRequest())+link;
		}
		if( link.indexOf("%") != -1 )
		{
			link = link.replaceAll("%", "%25");
		}
		URLConnection theConnection = new URL(link).openConnection();
        if( !(theConnection instanceof HttpURLConnection) )
        {
            throw new Exception("打开WAP链接错误:" + link);
        }
        HttpURLConnection connection = (HttpURLConnection) theConnection;
        connection.setRequestMethod( "GET" );
        connection.setConnectTimeout( 7000 );
        connection.setReadTimeout( 15000 );
        connection.setRequestProperty("User-Agent", "DOPOD-S505/6900_FULL_EVDO OPERA/8.65 CTC/1.0" );
        connection.connect();
        int responseCode = connection.getResponseCode();
        if(responseCode!=200)
        {
            throw new Exception( "不能打开链接地址：" + link + "("+responseCode+")" );
        }
        int contentLength = connection.getContentLength();
        if( contentLength == -1 )
        {
        	contentLength = 1024*1024;
        }
        if( connection.getContentType().toLowerCase().indexOf("wml") != -1 )
        {
            byte buffer[] = new byte[contentLength];
            int ch = -1, i = 0;
            while( ( ch = connection.getInputStream().read() ) != -1 )
            {
                if( i >= buffer.length )
                {
                    break;
                }

                buffer[ i++ ] = ( byte )ch;
            }
	        String charset = "UTF-8";
	        int k = connection.getContentType().indexOf("charest=");
	        if( k != -1 )
	        {
	        	charset = connection.getContentType().substring(k+"charest=".length());
	        }
	        String data = new String(buffer, charset);
	        StringBuffer out = new StringBuffer();
	        String line = null;
	        InputStream inputStream = new ByteArrayInputStream(data.getBytes());
	        java.io.BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
	        boolean begin = false;
	        String previewHref = Kit.URL_PATH(ServletActionContext.getRequest()) + url;
	        String defaultHref = Kit.URL(ServletActionContext.getRequest());
	        while ((line = reader.readLine()) != null)
	        {
	        	//line = line.toLowerCase();
	        	if( !begin )
	        	{
	            	begin = line.indexOf("<wml>") != -1;
	            	line = line + "<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset="+charset+"\" /></head>";
	        	}
	        	
	        	if( begin )
	        	{
		        	if( line.indexOf("wml") != -1 )
		        	{
		        		begin = true;
		        		line = line.replaceAll("wml", "html");
		        	}
		        	if( line.indexOf("card") != -1 )
		        	{
		        		line = line.replaceAll("card", "div");
		        		
		        		k = line.indexOf("title=");
		        		if( k != -1 )
		        		{
		        			k += "title=".length();
		        			char c =  line.charAt(k);
		        			int j = line.indexOf(c, k+1);
		        			if( j != -1 )
		        			{
		        				String title = line.substring(k+1, j);
		        				line = line + "<H4>"+title+"</H4>";
		        			}
		        		}
		        	}
	
		        	StringBuffer buf = new StringBuffer(line.trim());
		        	i = 0;
		        	do
		        	{
		        		i = buf.indexOf("href=", i);
		        		if( i != -1 )
		        		{
			        		char c = buf.charAt(i+5);
			        		i += 6;
			        		k = buf.indexOf(String.valueOf(c), i);
			        		if( k != -1 )
			        		{
			        			String href = buf.substring(i, k);
			        			if( href.indexOf( "http://" ) == -1 )
			        			{
			        				href = defaultHref+href;
			        			}
			        			buf.replace(i, k, previewHref+href);
			        		}
		        		}
		        	}
		        	while( i != -1 );
		        	
		        	i = buf.indexOf("<anchor>返回上一页<prev/></anchor>");
		        	if( i != -1 )
		        	{
		        		k = i + "<anchor>返回上一页<prev/></anchor>".length();
		        		link = previewHref + link;
		        		buf.replace(i, k, "<a href='"+link+"'>返回上一页</a>");
		        	}
	
		        	out.append(buf);
	        	}
	        }
	
	        dataStream = new ByteArrayInputStream(out.toString().getBytes(charset));
        }
        else if( connection.getContentType().toLowerCase().indexOf("html") != -1 )
        {//html的情况
            byte buffer[] = new byte[contentLength];
            int ch = -1, i = 0;
            while( ( ch = connection.getInputStream().read() ) != -1 )
            {
                if( i >= buffer.length )
                {
                    break;
                }

                buffer[ i++ ] = ( byte )ch;
            }
	        String charset = "UTF-8";
	        int k = connection.getContentType().indexOf("charest=");
	        if( k != -1 )
	        {
	        	charset = connection.getContentType().substring(k+"charest=".length());
	        }
	        String data = new String(buffer, charset);
	        StringBuffer out = new StringBuffer();
	        String line = null;
	        InputStream inputStream = new ByteArrayInputStream(data.getBytes());
	        java.io.BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
	        String previewHref = "http!get.action?link=";
	        String defaultHref = Kit.URL(ServletActionContext.getRequest());
			if( link.startsWith("http://") )
			{
				int n = link.indexOf('/', 7);
				n = n!=-1?n:link.length();
				defaultHref = link.substring(0, n);
			}
	        while ((line = reader.readLine()) != null)
	        {
	        	StringBuffer buf = new StringBuffer(line.trim());
	        	if( fontSize != null )
	        	{
		        	i = buf.indexOf("</head>");
		        	if( i != -1 )
		        	{
	        			 buf.insert(i, "<style type='text/css'>td{font-size:"+fontSize+";padding:0px};th{font-size:"+fontSize+";padding:0px}</style>");
		        	}
		        	i = buf.indexOf("<body");
		        	if( i != -1 )
		        	{
	        			 buf.insert(i+5, " style='overflow:auto; font-size:"+fontSize+";margin-top:0px; margin-left:0px; margin-bottom:0px; margin-right:0px'");
		        	}
	        	}
	        	i = 0;
	        	do
	        	{
	        		i = buf.indexOf("href=", i);
	        		if( i != -1 )
	        		{
		        		char c = buf.charAt(i+5);
		        		if( c=='\''||c=='"' )
		        		{
		        			i += 6;
		        			k = buf.indexOf(String.valueOf(c), i);
		        		}
		        		else
		        		{
		        			i += 5;
		        			int k1 = buf.indexOf(String.valueOf('>'), i);
		        			int k2 = buf.indexOf(String.valueOf(' '), i);
		        			k = k1<k2?k1:k2;
		        		}
		        		if( k != -1 )
		        		{
		        			String href = buf.substring(i, k);
		        			if( !href.startsWith("#") )
		        			{
		        				href = Tools.replaceStr(href, "&", ",,");
			        			if( href.indexOf( "http://" ) == -1 )
			        			{
			        				if( !href.startsWith("/") )
			        				{
			        					href = "/"+href;
			        				}
			        				href = defaultHref+href;
			        			}
			        			buf.replace(i, k, previewHref+href);
		        			}
		        		}
	        		}
	        	}
	        	while( i != -1 );
	        	out.append(buf);
	        }
	
	        dataStream = new ByteArrayInputStream(out.toString().getBytes(charset));
        }
		return "preview";
	}
	
	/**
	 * 模拟浏览器访问指定链接，包括执行js脚本
	 * @return
	public String doGetx()
	{
		log.info("Do getx "+link);
		OutputStream out = null;
    	HttpServletRequest request = ServletActionContext.getRequest();
    	HttpServletResponse response = ServletActionContext.getResponse();
		if( link == null || !link.startsWith("http:/") )
		{
			//尝试用Unicode解码
			if( link.indexOf(".action") != -1 )
			{
				link = Common.URL_PATH(request)+link;
			}
			else
			{
				try
				{
					link = Tools.decodeUnicode(link);
				}
				catch(Exception e)
				{
				}
			}
			if( link == null || !link.startsWith("http:/") )
				return null;
		}
		if( !link.startsWith("http://") )
		{
			StringBuffer links = new StringBuffer(link);
			links.insert("http:/".length(), '/');
			link = links.toString();
		}
		StringBuffer sb = new StringBuffer();
    	WebClient webClient = null;
		try
		{
			Enumeration<String> names = request.getHeaderNames();
	        String ip = request.getRemoteAddr();
			sb.append("Proxy "+link);
			sb.append("\r\n\tReceive the request-do from "+ip);
			while( names.hasMoreElements() )
			{
				String key = names.nextElement();
				String value = request.getHeader(key);
				if( "connection".equalsIgnoreCase(key) )
					value = "keep-alive";
				sb.append("\r\n\t\t"+key+" = "+value);
			}
			log.debug(sb.toString());
			//模拟一个浏览器
			webClient = new WebClient();
			CookieManager cookieManager = webClient.getCookieManager();
//			String domain = HttpUtils.getBaseAndHost(link)[0];
			for( javax.servlet.http.Cookie c : request.getCookies() )
			{
				String name = c.getName();// get the cookie name
				String value = c.getValue(); // get the cookie value
				Cookie cookie = new Cookie("", name, value);
				cookieManager.addCookie(cookie);
			}
			cookieManager.setCookiesEnabled(true);
//			webClient.setCookieManager(cookieManager);
			//模拟浏览器打开一个目标网址
			webClient.getPage(link);
			webClient.close();
		}
		catch(Exception e)
		{
			log.error("Failed to get link for exception:"+ e);
			log.error(sb.toString());
			try
			{
				out = response.getOutputStream();
				response.reset();
				response.setContentType("text/html");
				response.setCharacterEncoding("UTF-8");
				response.getOutputStream().write(("<script type='text/javascript' language='javascript'>"+
				                                 "if(top&&top.skit_alert){top.skit_alert('代理访问网站失败，原因是"+e+"。请稍后继续尝试');}"+
				                                 "else{alert('代理访问网站失败，原因是"+e+"。请稍后继续尝试');}</script>").getBytes("UTF-8"));
			}
			catch(Exception e1)
			{
				log.error("Failed to exception-response."+ e);
			}
		}
		finally
		{
			if( webClient != null ) webClient.close();
			if( out != null )
				try
				{
					out.flush();
					out.close();
				}
				catch(Exception e)
				{
					log.error("Failed to close response."+e);
				}
		}
		return null;
	}
	 */
	/**
	 * 解码链接
	 * @param link
	 * @return
	 */
	private String decodeLink(String link){
		try{
			/*s=0表示http，1表示https,默认表示带了完整链接路径*/
			String prefix = "";
			switch( link.charAt(0) ){
			case '0':
				prefix = String.format("%s", "http:");
				link = URLDecoder.decode(link.substring(1), "UTF-8");
				break;
			case '1':
				prefix = String.format("%s", "https:");
				link = URLDecoder.decode(link.substring(1), "UTF-8");
				break;
			default:
				link = URLDecoder.decode(link, "UTF-8");
				break;
			}
			return String.format("%s%s", prefix, link);
		}
		catch(Exception e){
			log.error("Failed to decode link "+link, e);
			return null;
		}
	}
	/**
	 * 请求指定页面
	 * @param request
	 * @param response
	 * @param link
	 * @param depth
	 * @return
	 */
	private File proxyDir = null;
	private String doGetOrPost(String method, HttpServletRequest request, HttpServletResponse response, String link, int depth) throws Exception{
		logInfo(depth, "Do proxy %s by %s", link, request.getRequestURI());
		HttpURLConnection connection = null;
		OutputStream out = null;
		InputStream is = null;
		String baseUri = "";
		String host = "";
		String pathUri = "";
		boolean isHttps = false;
		try
		{
			String[] params = HttpUtils.getBaseAndHost(link);
			pathUri = params[2];
			baseUri = params[0];
			host = params[1];
			if(link.startsWith("https"))
			{
				isHttps = true;
			}
			URLConnection theConnection = new URL(link).openConnection();
	        if( !(theConnection instanceof HttpURLConnection) )
	        {
		    	response.setContentType("text/html");
		    	response.setStatus(500);
		    	return null;
	        }
	        connection = (HttpURLConnection) theConnection;
	        connection.setRequestMethod( method );
	        connection.setConnectTimeout( 7000 );
	        connection.setReadTimeout( 15000 );
	        connection.setDoOutput(true);
	        connection.setDoInput(true);
			Enumeration<String> names = request.getHeaderNames();
	        String ip = request.getRemoteAddr();
	        logInfo(depth+1, "Host is %s, the url of base is %s", host, baseUri);
	        logInfo(depth+1, "Receive the request from %s", ip);
	        String referer = null;
			while( names.hasMoreElements() )
			{
				String key = names.nextElement();
				String value = request.getHeader(key);
				if( "host".equalsIgnoreCase(key) ){
					value = host;
				}
				else if( "referer".equalsIgnoreCase(key) ){
					logInfo(depth+2, "%s.old=%s", key, value);
					int i = value.indexOf("link=");
	    			referer = decodeLink(value.substring(i+5));
	    			value = referer;
				}
				else if( "if-modified-since".equals(key) ){
					continue;
				}
				else if( "if-none-match".equals(key) ){
					continue;
				}
				else if( "cache-control".equals(key) ){
					value = "no-cache";
				}
				logInfo(depth+2, "%s=%s", key, value);
				connection.setRequestProperty(key, value);
//				else if( "connection".equalsIgnoreCase(key) )
//					value = "keep-alive";
			}
			connection.setRequestProperty("accept-encoding", "gzip");
			if( "GET".equalsIgnoreCase(method) ){
				connection.setRequestProperty("Pragam", "no-cache");
			}
	        connection.connect();
	        if( "POST".equalsIgnoreCase(method) ){
	        	names = request.getParameterNames();
	        	logInfo(depth+1, "Write the data of post about parameters");
	        	StringBuffer parameters = new StringBuffer();
	        	int i = 0;
	        	while( names.hasMoreElements() )
				{
					String name = names.nextElement();
					String value = request.getParameter(name);
					if( name.equalsIgnoreCase("link") ) {
						i = value.lastIndexOf("?");
						if( i != -1 ){
							value = value.substring(i+1);
							parameters.append(value);
							logInfo(depth+2, "%s", value);
						}
						else{
							i = 0;
						}
						continue;
					}
					logInfo(depth+2, "%s=%s", name, value);
					if( i > 0 ){
						parameters.append("&");
						i += 1;
					}
					parameters.append(String.format("%s=%s", name, value));
				}
	        	int len = 0, size = 0;
	        	out = connection.getOutputStream();
	        	if( parameters.length() > 0 ){
	        		out.write(parameters.toString().getBytes());
	        		out.flush();
	        		size += parameters.length();
	        	}
	        	is = request.getInputStream();
		    	byte[] buff = new byte[1024*64];
	            while( ( len = is.read(buff) ) != -1 )
	            {
	            	size += len;
	            	out.write(buff, 0, len);
	            }
	            is.close();
	            out.flush();
	            out.close();
	    		this.logInfo(depth+1, "Succeed to write the data of post, total %s bytes", size);
	        }
	        String ct = connection.getContentType();
//			System.err.println(String.format("[%s][%s] %s", connection.getContentType(), connection.getResponseCode(), link));
	    	if( connection.getResponseCode() == 200 && ct.startsWith("text/html") ){
				if( proxyDir == null ){
					String path = String.format("log/CosProxy/%s", Tools.encodeMD5(link));
					proxyDir = new File(PathFactory.getAppPath(), path);
					if( proxyDir.exists() ){
						IOHelper.deleteDir(proxyDir);
					}
					proxyDir.mkdirs();
				}
				String chartset = null;
				int i = ct.indexOf("charset=") ;
				if( i != -1 ) chartset=ct.substring(i+"charset=".length());
				if(chartset!= null && chartset.endsWith(";"))
					chartset = chartset.substring(0, chartset.length()-1);
				if( chartset == null ) chartset = "UTF-8";
				is = connection.getInputStream();
	    		if( "gzip".equalsIgnoreCase(connection.getContentEncoding()) )
	    		{
	    			is = new GZIPInputStream(is);
	    		}
				org.jsoup.nodes.Document doc = Jsoup.parse(is, chartset, baseUri);
				StringBuffer parser = new StringBuffer("以下内容是HTML解析日志");
				parse(isHttps, pathUri, doc.head(), 0, parser);
				parse(isHttps, pathUri, doc.body(), 0, parser);		
				writeFile(new File(proxyDir, "page.parser"), parser.toString().getBytes("UTF-8"));
				byte[] payload = doc.html().toString().getBytes(chartset);
//				System.err.println(doc.html());
				this.proxy(connection, response, depth+1);
				response.setHeader("Content-Length", String.valueOf(payload.length));
				File page = new File(proxyDir, "page.html");
				IOHelper.writeFile(page, payload);
				out = response.getOutputStream();
	    		if( "gzip".equalsIgnoreCase(connection.getContentEncoding()) )
	    		{
	    			GZIPOutputStream gos = new GZIPOutputStream(out);
	    			gos.write(payload);
	    			gos.finish();
	    		}
	    		else{
					out.write(payload);
					out.flush();
	    		}
	    		this.logInfo(depth+1, "Succeed to write page to %s total %s bytes",
    				page.getAbsolutePath(), payload.length);
	    	}
	    	else{
	    		if( referer != null ){
	    			String path = String.format("log/CosProxy/%s", Tools.encodeMD5(referer));
	    			proxyDir = new File(PathFactory.getAppPath(), path);
	    			if( !proxyDir.exists() ){
	    				proxyDir.mkdirs();					
	    			}
	    		}
		    	if( connection.getResponseCode() == 302 ){
					if( proxyDir == null ){
						String path = String.format("log/CosProxy/%s", Tools.encodeMD5(link));
						proxyDir = new File(PathFactory.getAppPath(), path);
						if( proxyDir.exists() ){
							IOHelper.deleteDir(proxyDir);
						}
						proxyDir.mkdirs();
					}
		    		this.logInfo(depth+1, "Redirect %s", connection.getHeaderField("Location"));
//		    		String redirectUrl = setProxyUrl(connection.getHeaderField("Location"));
//		    		event.append("\r\n\t to "+redirectUrl);
//		    		response.setHeader("Location", redirectUrl);
		    		return doGetOrPost(method, request, response, connection.getHeaderField("Location"), depth+1);
		    	}
		    	else if( connection.getResponseCode() == 200 ){

		    		int i = link.lastIndexOf('/');
    				String filename = link.substring(i+1);
    				i = filename.indexOf("?");
    				if( i != -1 ){
    					filename = filename.substring(0, i);
    				}
    				if( filename.lastIndexOf('.') == -1 ){
    					MimeType mime = AllTypes.forName(ct);
    					filename += "."+mime.getExtension();
    				}
    				File page = new File(proxyDir, filename);
		    		FileOutputStream fos = new FileOutputStream(page);
		    		is = connection.getInputStream();
			    	int len = 0, size = 0;
    				this.proxy(connection, response, depth+1);
					out = response.getOutputStream();
		    		if( ct.startsWith("text/css") ){
			    		if( "gzip".equalsIgnoreCase(connection.getContentEncoding()) )
			    		{
			    			is = new GZIPInputStream(is);
			    		}
			    		String css = parseCss(new String(IOHelper.readAsByteArray(is)), baseUri, pathUri);
			    		byte[] payload = css.getBytes("UTF-8");
			    		fos.write(payload);
			            fos.flush();
			            fos.close();
			    		if( "gzip".equalsIgnoreCase(connection.getContentEncoding()) )
			    		{
			    			GZIPOutputStream gos = new GZIPOutputStream(out);
			    			gos.write(payload);
			    			gos.finish();
			    		}
			    		else{
							out.write(payload);
							out.flush();
			    		}
		    		}
		    		else{
				    	byte[] buff = new byte[1024*64];
			            while( ( len = is.read(buff) ) != -1 )
			            {
			            	size += len;
			            	out.write(buff, 0, len);
			            	fos.write(buff, 0, len);
			            }
			            fos.flush();
			            fos.close();
		    		}
		    		this.logInfo(depth+1, "Succeed to write data to %s, total %s bytes",
	    				page.getAbsolutePath(), size);
	    		}
		    	else{
					this.proxy(connection, response, depth+1);
		    		this.logInfo(depth+1, "Succeed to response.");
		    	}
	    	}
		}
		catch(Exception e)
		{
			throw e;
		}
		finally
		{
			if( connection != null )
				try
				{
					if( is != null ) is.close();
					connection.disconnect();
				}
				catch(Exception e)
				{
					log.error("Failed to close connection.", e);
				}
			if( out != null )
				try
				{
					out.flush();
					out.close();
				}
				catch(Exception e)
				{
					log.error("Failed to close response.", e);
				}
		}
		return null;
	}
	
	/**
	 * 解析css
	 * @param isHttps
	 * @param baseUri
	 * @param css
	 * @throws UnsupportedEncodingException 
	 */
	private String parseCss(String css, String baseUri, String pathUri) throws UnsupportedEncodingException{
		int i = 0, j = 0;
		StringBuilder buff = new StringBuilder(css);
		while((i=buff.indexOf("url(", i)) != -1 ){
			i += 4;
			while( buff.charAt(i) == ' ') {
				i += 1;
			}
			j = buff.indexOf(")", i);
			String src = buff.substring(i, j);
			buff.delete(i, j);
			if( src.startsWith("/") ){
				src = URLEncoder.encode(baseUri + src, "UTF-8");;
			}
			else if( src.startsWith("..") ){
				src = URLEncoder.encode(pathUri + src, "UTF-8");;
			}
			else {
				src = URLEncoder.encode(src, "UTF-8");
			}
			buff.insert(i, "http!get.action?link="+src);
		}
		return buff.toString();
	}
	
	/**
	 * 解析百家号
	 * @param isHttps
	 * @param baseUri
	 * @param doc
	 * @param data
	 * @throws Exception
	 */
	private void parseBaijiahao(boolean isHttps, String baseUri, org.jsoup.nodes.Document doc, JSONObject data)
		throws Exception
	{
		JSONArray images = data.getJSONArray("images");
		int i = 0, j = 0;
		data.put("plateform", "百家号");
		Elements elements = doc.getElementsByClass("authorName");
		if( elements != null && elements.size() > 0 ){
			data.put("author", elements.text());
			data.put("authorReferer", elements.get(0).parent().attr("href"));
		}
		elements = doc.getElementsByClass("avatarLink");
		if( elements != null && elements.size() > 0 ){
			data.put("avatar", elements.get(0).getElementsByTag("img").get(0).attr("src"));
		}
		elements = doc.getElementsByClass("mainContent");
		if( elements != null && elements.size() > 0 ){
			data.put("content", elements.text());
			elements = elements.get(0).getElementsByTag("img");
			for(Element e : elements ){
				images.put(e.attr("src"));
			}
			if( images.length() > 0 ) {
				data.put("cover", images.get(0));
			}
			elements = doc.getElementsByTag("script");
			for(Element e : elements ){
				String script = e.html();
				if( script.startsWith("(function (global)") ){
					i = script.indexOf("\"updatedate\":\"");
					if( i != -1 ) {
						i += "\"updatedate\":\"".length();
						j = script.indexOf('\"', i);
						String time = script.substring(i, j);
						time = time.replaceAll("T", " ");
						data.put("time", time);
					}
					break;								
				}
			}
		}
	}
	/**
	 * 解析头条
	 * @param isHttps
	 * @param baseUri
	 * @param doc
	 * @param data
	 * @throws Exception
	 */
	private void parseToutiao(boolean isHttps, String baseUri, org.jsoup.nodes.Document doc, JSONObject data)
		throws Exception
	{
		JSONArray images = data.getJSONArray("images");
		int i = 0, j = 0;
		data.put("plateform", "今日头条");
		Elements elements = doc.getElementsByTag("script");
		for(Element e : elements ){
			String script = e.html();
			if( script.startsWith("var BASE_DATA = ") ){
				i = script.lastIndexOf("}");
				script = script.substring("var BASE_DATA = ".length(), i+1);
//				System.err.println(String.format("[%s] %s", i++, script));
				i = script.indexOf("coverImg: '");
				if( i != -1 ) {
					i += "coverImg: '".length();
					j = script.indexOf('\'', i);
					data.put("cover", script.substring(i, j));
				}
				i = script.indexOf("name: '");
				if( i != -1 ) {
					i += "name: '".length();
					j = script.indexOf('\'', i);
					data.put("author", script.substring(i, j));
				}
				i = script.indexOf("avatar: '");
				if( i != -1 ) {
					i += "avatar: '".length();
					j = script.indexOf('\'', i);
					data.put("avatar", (isHttps?"https:":"http:")+script.substring(i, j));
				}
				i = script.indexOf("openUrl: '");
				if( i != -1 ) {
					i += "openUrl: '".length();
					j = script.indexOf('\'', i);
					data.put("authorReferer", baseUri+script.substring(i+1, j));
				}
				i = script.indexOf("content: '");
				if( i != -1 ) {
					i += "content: '".length();
					j = script.indexOf('\'', i);
					String content = script.substring(i, j);
					data.put("content", content);
					i = 0;
					while((i=content.indexOf("img src&#x3D;&quot;", i)) != -1){
						i += "img src&#x3D;&quot;".length();
						j = content.indexOf("&quot;", i);
						images.put(content.substring(i, j));
					}
				}
				i = script.indexOf("time: '");
				if( i != -1 ) {
					i += "time: '".length();
					j = script.indexOf('\'', i);
					data.put("time", script.substring(i, j));
				}
			}
		}
	}
	/**
	 * 得到页面的信息
	 * @return
	 */
	public String doGetinfo(){
		HttpURLConnection connection = null;
		OutputStream out = null;
		InputStream is = null;
		String baseUri = "";
		String host = "";
        JSONObject data = new JSONObject();
		boolean isHttps = false;
		try
		{
			this.link = decodeLink(this.link);
			if( link == null || (!link.startsWith("http://") && !link.startsWith("https://")) )
			{
				data.put("yes", false);
				data.put("remark", "请求的链接不正确");
				return null;
			}
			if(link.startsWith("https"))
			{
				isHttps = true;
			}
			String[] params = HttpUtils.getBaseAndHost(link);
			baseUri = params[0];
			host = params[1];
			URLConnection theConnection = new URL(link).openConnection();
	        if( !(theConnection instanceof HttpURLConnection) )
	        {
	    		data.put("remark", "请求获取信息的网页不是http协议，而是"+theConnection.getClass().getName());
		    	return null;
	        }
	        connection = (HttpURLConnection) theConnection;
	        connection.setRequestMethod( "GET" );
	        connection.setConnectTimeout( 7000 );
	        connection.setReadTimeout( 15000 );
			connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			connection.setRequestProperty("Accept-Encoding", "gzip");
			connection.setRequestProperty("Accept-Language", "zh-cn");
			connection.setRequestProperty("Host", host);
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
			connection.setRequestProperty("Pragam", "no-cache");
			connection.setRequestProperty("Connection", "close");
			connection.setRequestProperty("Cache-Control", "no-cache");
			connection.setRequestProperty("Referer", baseUri);
	        connection.connect();
	    	String ct = connection.getContentType();
			JSONArray images = new JSONArray();
			data.put("description", "");
			data.put("plateform", "");
			data.put("author", "");
			data.put("authorReferer", "");
			data.put("avatar", "");
			data.put("images", images);
			data.put("cover", "");
			data.put("time", "");
	    	if( connection.getResponseCode() == 200 ){
	    		data.put("yes", true);
	    		if( ct.startsWith("text/html") ) {
					data.put("type", "link");
					String chartset = null;
					int i = ct.indexOf("charset=") ;
					if( i != -1 ) chartset=ct.substring(i+"charset=".length());
					if(chartset!= null && chartset.endsWith(";"))
						chartset = chartset.substring(0, chartset.length()-1);
					if( chartset == null ) chartset = "UTF-8";
					is = connection.getInputStream();
		    		if( "gzip".equalsIgnoreCase(connection.getContentEncoding()) )
		    		{
		    			is = new GZIPInputStream(is);
		    		}
					org.jsoup.nodes.Document doc = Jsoup.parse(is, chartset, baseUri);
					data.put("title", doc.title());
					Elements elements = doc.getElementsByTag("meta");
					if( elements != null ) {
						for(Element e : elements ){
							String name = e.attr("name");
							if( "keywords".equalsIgnoreCase(name) ){
								data.put("keywords", e.attr("content"));
							}
							else if( "description".equalsIgnoreCase(name) ){
								data.put("description", e.attr("content"));
							}
						}
					}
//					System.err.println(String.format("[%s] %s", host, doc.html()));
					if( "ms.mbd.baidu.com".equalsIgnoreCase(host) ){
						this.parseBaijiahao(isHttps, baseUri, doc, data);
					}
					else if( "www.toutiao.com".equalsIgnoreCase(host)
						|| "m.toutiao.com".equalsIgnoreCase(host) 
						|| "m.toutiaocdn.com".equalsIgnoreCase(host) ){
						this.parseToutiao(isHttps, baseUri, doc, data);
					}
	    		}
	    		else if( ct.startsWith("image") ) {
					data.put("type", "image");
					data.put("cover", link);
					int i = ct.indexOf("/")+1;
					int j = ct.indexOf(",");
					if( j == -1 ){
						data.put("subtype", ct.substring(i));
					}
					else{
						data.put("subtype", ct.substring(i, j));
					}
	    		}
	    		else if( ct.startsWith("application") ) {
					data.put("type", "file");
					int i = ct.indexOf("/")+1;
					int j = ct.indexOf(",");
					if( j == -1 ){
						data.put("subtype", ct.substring(i));
					}
					else{
						data.put("subtype", ct.substring(i, j));
					}
	    		}
	    		else{
		    		data.put("remark", String.format("请求获取信息的网页资源类型[%s]不支持", ct));
		    		data.put("yes", false);
	    		}
	    	}
	    	else{
	    		log.error("Failed to get the info from "+link);
	    		data.put("remark", String.format("请求获取信息的网页访问返回[%s]错误", connection.getResponseCode()));
	    		data.put("yes", false);//获取页面信息失败
	    	}
		}
		catch(Exception e)
		{
			log.error("Failed to get info from "+link, e);
		}
		finally
		{
			HttpServletResponse response = ServletActionContext.getResponse();
        	response.setContentType("application/json;charset=utf8");
        	response.setHeader("Content-disposition", "inline; filename=data_"+ts+".json");
			response.setStatus(200);
			try{
				response.getOutputStream().write(data.toString().getBytes("UTF-8"));
			}
			catch(Exception e){
				log.error("Failed to write data for "+e);
			}
			if( connection != null )
				try
				{
					if( is != null ) is.close();
					connection.disconnect();
				}
				catch(Exception e)
				{
					log.error("Failed to close connection.", e);
				}
			if( out != null )
				try
				{
					out.flush();
					out.close();
				}
				catch(Exception e)
				{
					log.error("Failed to close response.", e);
				}
		}
		return null;
	}
	
	/**
	 * HTTP-POST代理接口
	 * @return
	 */
	public String doPost(){

		this.link = decodeLink(this.link);
		if( link == null || (!link.startsWith("http://") && !link.startsWith("https://")) )
		{
			return null;
		}
		logs.append(String.format("[%s] doPost %s", Tools.getFormatTime(ts), this.link));
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		try{
			return doGetOrPost("POST", request, response, this.link, 0);
		}
		catch(Exception e){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			logs.append("\r\n\t=======Exception Below=============\r\n");
			logs.append(baos.toString());
			try
			{
				response.setStatus(500);
				response.setContentType("text/html");
				response.setCharacterEncoding("UTF-8");
				response.getOutputStream().write(("代理访问网站失败，原因是"+e+"。请稍后继续尝试").getBytes("UTF-8"));
			}
			catch(Exception e1)
			{
				log.error("Failed to exception-response for "+ e);
			}
			return null;
		}
		finally {
			logs.append(String.format("\r\n[%s] Finish logto %s\r\n",
				Tools.getFormatTime(System.currentTimeMillis()), proxyDir));
			System.out.println(logs.toString());
			if( proxyDir != null )
				try {
					writeFile(new File(proxyDir, "network.log"), logs.toString().getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e1) {
			}
		}
	}
	/**
	 * HTTP-GET代理接口
	 * @return
	 * @throws Exception
	 */
	public String doGet()
	{
		this.link = decodeLink(this.link);
		if( link == null || (!link.startsWith("http://") && !link.startsWith("https://")) )
		{
			return null;
		}
		logs.append(String.format("[%s] doGet %s", Tools.getFormatTime(ts), this.link));
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		try{
			return doGetOrPost("GET", request, response, this.link, 0);
		}
		catch(Exception e){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			logs.append("\r\n\t=======Exception Below=============\r\n");
			logs.append(baos.toString());
			try
			{
				response.setStatus(500);
				response.setContentType("text/html");
				response.setCharacterEncoding("UTF-8");
				response.getOutputStream().write(("代理访问网站失败，原因是"+e+"。请稍后继续尝试").getBytes("UTF-8"));
			}
			catch(Exception e1)
			{
				log.error("Failed to exception-response for "+ e);
			}
			return null;
		}
		finally {
			logs.append(String.format("\r\n[%s] Finish logto %s\r\n",
				Tools.getFormatTime(System.currentTimeMillis()), proxyDir));
			System.out.println(logs.toString());
			if( proxyDir != null )
				try {
					writeFile(new File(proxyDir, "network.log"), logs.toString().getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e1) {
			}
		}
	}
	/**
	 * 
	 * @param connection
	 * @param response
	 * @param event
	 * @throws Exception
	 */
	private void proxy(HttpURLConnection connection, HttpServletResponse response, int depth) throws Exception {
		response.setContentType(connection.getContentType());
    	response.setContentLength(connection.getContentLength());
    	response.setStatus(connection.getResponseCode());
		response.setCharacterEncoding(connection.getContentEncoding());
		this.logInfo(depth, "Connection "+connection.getResponseCode()+
		          ", Content-Type="+connection.getContentType()+
		          ", Content-Length="+connection.getContentLength()+
		          ", Content-Encoding="+connection.getContentEncoding());
		Iterator<String> iterator = connection.getHeaderFields().keySet().iterator();
    	while( iterator.hasNext() )
    	{
    		String name = iterator.next();
    		String value = connection.getHeaderField(name);
    		this.logInfo(depth+1, "%s=%s", name, value);
    		if( "Transfer-Encoding".equalsIgnoreCase(name) && value.equals("chunked") )
    		{
    			continue;
    		}
    		response.setHeader(name, value);
    	}
	}
	
	/**
	 * 生成代理新地址
	 * @param url
	 * @return
	 */
	private String setProxyUrl(String url){
		int p = 0;
		try {
			if( url.startsWith("https:") ){
				url = url.substring(6);
				p = 1;
			}
			else{
				url = url.substring(5);
			}
			url = URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		return String.format("http!get.action?link=%s%s", p, url);
	}
	
	/**
	 * 
	 */
	private StringBuffer logs = new StringBuffer();
	private long ts = System.currentTimeMillis();
	private void logInfo(int depth, String info, Object... args){
//		log.append(String.format("\r\n\t[%s] %s", System.currentTimeMillis() - ts, String.format(info, args)));
		logs.append("\r\n");
		for(int i = 0; i < depth; i++ ) {
			logs.append("  ");
		}
		logs.append(String.format("[%s] %s", System.currentTimeMillis() - ts, String.format(info, args)));
	}
	/**
	 * 解析html内容替换内中
	 * @param e
	 * @param depth
	 */
	private void parse(boolean isHttps, String pathUri, Element e, int depth, StringBuffer parser){
		int i = 0;
		parser.append("\r\n");
		for( ; i < depth; i++ ){
			parser.append("  ");
		}
		parser.append(e.tagName());
		Elements children = e.children();
		parser.append(" "+(children!=null?children.size():-1));
		i = 0;
		if( e.tagName().equalsIgnoreCase("script") ){
			if( pathUri.indexOf("www.gapp.gov.cn") != -1 ){
				this.translateGappScript(e, depth);
			}
		}
		Attributes attrs = e.attributes();
		for(Attribute attr : attrs ){
			parser.append(" ");
			parser.append(attr.getKey());
			parser.append("='");
			String val = attr.getValue().trim();
			String oldval = val;
			String newval = val;
			boolean p = false;
			if( "href".equals(attr.getKey()) ||
				"data-original".equals(attr.getKey()) ||
				"data-actualsrc".equals(attr.getKey()) ||
				"src".equals(attr.getKey()) ){
				if( val.startsWith("//") ){
					val = (isHttps?"https:":"http:")+val;
				}
				else if( !val.startsWith("http://") && !val.startsWith("https://") && !val.startsWith("data:") ){
					if( val.startsWith("/") ){
						val = e.baseUri()+val.substring(1);
					}
					else{
						val = pathUri+val;
					}
				}
				
				if( !val.startsWith("data:") ){
					newval = setProxyUrl(val);
					p = true;
				}
			}
			if( !oldval.equals(newval)  ){
				parser.append(oldval);
				parser.append(" -> ");
				e.attr(attr.getKey(), newval);
			}
			if( p ){
				parser.append("[p*]");					
			}
			else{
				parser.append(val);
			}
			parser.append("'");
		}
		if( children == null || children.isEmpty() ){
			return;
		}
		if( !e.tagName().equalsIgnoreCase("script") ){
			for(Element child : children){
				parse(isHttps, pathUri, child, depth+1, parser);
			}
		}
	}
	
	/**
	 * 转换脚本
	 */
	private void translateGappScript(Element e, int depth){
		StringBuilder script = new StringBuilder(e.html().replaceAll("\"", "'"));
		int i = script.indexOf( "stlDynamic_ajaxElement" );
		if( i != -1 ){
			i = script.indexOf("var url = '");
			if( i != -1 ) {
				i += ("var url = '".length());
				script.deleteCharAt(i);
				script.insert(i, e.baseUri());
				i = script.indexOf("jQuery.post(", i);
				if( i != -1 ){
//					?ID=001666965CPP02
//					script.insert(i, "alert(1);\r\n");
//					i += "alert(1);\r\n".length();
					i += "jQuery.post(".length();
					script.insert(script.indexOf("url", i)+3, ")");
					script.insert(i, "'http!post.action?link='+encodeURIComponent(");
//			        logInfo(depth+1, "TranslateGappScript\r\n%s", script.toString());
				}
				i = script.indexOf("' + getQueryString();");
				if( i != -1 ){
					script.delete(i, i+"' + getQueryString();".length());
					script.insert(i, link.substring(link.lastIndexOf("?")+1)+"';");
					Entities.EscapeMode.base.getMap().clear();
					e.html(script.toString());
				}
			}
		}
	}

	public InputStream getDataStream()
	{
		return dataStream;
	}

	public void setDataStream(InputStream dataStream)
	{
		this.dataStream = dataStream;
	}

	public String getLink()
	{
		return link;
	}

	public void setLink(String link)
	{
		this.link = link;
	}
	public void setHeight(int height)
	{
		this.height = height;
	}
	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getScale()
	{
		return scale;
	}

	public void setScale(int scale)
	{
		this.scale = scale;
	}

    public static void writeFile(File file, byte[] payload)
    {
    	synchronized (log) {
    		try
    		{
    			File dir = file.getParentFile();
    			if( !dir.exists() ) dir.mkdirs();
    			FileOutputStream out = new FileOutputStream(file, true);
    			out.write(payload);
    			out.close();
    		}
    		catch( Exception e )
    		{
    			e.printStackTrace();
    		}
		}
    }

	public static void main(String[] args)
	{
		String url = "http://4-ps.googleusercontent.com/x/x/i.361lu.com/?img=http://sharenxs.com/photos/2014/01/22/52df852ca7d3e/0011.jpg";
		System.out.println(Tools.encodeUnicode(url));
//		HttpAction action = new HttpAction();
//		action.setLink(Tools.encodeUnicode(url));
//		action.imagezoom();
	}
}
