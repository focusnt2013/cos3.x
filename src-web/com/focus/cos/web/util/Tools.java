package com.focus.cos.web.util;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.lang.time.DateFormatUtils;
import org.doomdark.uuid.UUIDGenerator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

import com.focus.util.HttpUtils;

public class Tools
{
	public static java.text.DecimalFormat DFORMAT = new java.text.DecimalFormat("0.00K");
	public static java.text.DecimalFormat DF = new java.text.DecimalFormat("0.00");

	public static final String getKBStr(long size)
	{
		double d = size;
		return DFORMAT.format(d / 1024);
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
                splits[i++] = str.substring(bi);
            }
            else
            {
                splits[i++] = str.substring(bi, ei);
            }
            bi = ei + s.length();
        }

        return splits;
    }
	public static final String getDecimalStr(int d)
	{
		double d1 = d;
		return DF.format(d1 / 100);
	}
	/**
	 * 还原Unicode编码的字符串
	 * 
	 * @param src
	 * @return String
	 * @author Focus
	 */
	public static String unicode2Chr(String src)
	{
		if( src == null || src.isEmpty() )
		{
			return src;
		}
		String tempStr = "";
		String returnStr = "";

		// 将编码过的字符串进行重排
		for (int i = 0; i < src.length() / 4; i++)
		{
			if (0 == i)
			{
				tempStr = src.substring(4 * i + 2, 4 * i + 4);
				tempStr += src.substring(4 * i, 4 * i + 2);
			}
			else
			{
				tempStr += src.substring(4 * i + 2, 4 * i + 4);
				tempStr += src.substring(4 * i, 4 * i + 2);
			}
		}

		byte[] b = new byte[tempStr.length() / 2];


		try
		{
			// 将重排过的字符串放入byte数组，用于进行转码
			for (int j = 0; j < tempStr.length() / 2; j++)
			{
				String subStr = tempStr.substring(j * 2, j * 2 + 2);
				int b1 = Integer.decode("0x" + subStr).intValue();
				b[j] = (byte) b1;
			}
			// 转码
			returnStr = new String(b, "utf-16");
		}
		catch (UnsupportedEncodingException ex)
		{
			ex.printStackTrace();
		}
		catch(Exception e)
		{
			return src;
		}
		return returnStr;
	}

	public static String chr2Unicode(String src)
	{
		StringBuffer s = new StringBuffer();
		if (src != null && !"".equals(src))
		{
			String hex = "";

			for (int i = 0; i < src.length(); i++)
			{
				hex = Integer.toHexString((int) src.charAt(i));
				if (hex.length() < 4)
				{
					while (hex.length() < 4)
					{
						hex = "0".concat(hex);
					}
				}
				hex = hex.substring(2, 4).concat(hex.substring(0, 2));
				s.append(hex.toUpperCase());
			}
		}
		return s.toString();
	}

	/**
	 * 关键字过滤条件不能包含全角或半角的单引号、双引号和百分号等
	 * 
	 * @param String
	 * @return String
	 * @author Focus
	 */
	public static String replaceStr(String str)
	{
		// ===========SQL Server 里特殊字符转换==========/
		// String replaceStr = str.replace("[", "[[]");// 此句一定要在最前面
		// replaceStr = replaceStr.replace("_", "[_]");
		// replaceStr = replaceStr.replace("%", "[%]");
		// replaceStr = replaceStr.replace("'", "''");
		// replaceStr = replaceStr.replace("‘", "\\‘");
		// replaceStr = replaceStr.replace("’", "\\’");
		/* ========================================== */

		// ============MySQL 里特殊字符转换=============/
		String replaceStr = str.replace("_", "\\_");
		replaceStr = replaceStr.replace("%", "\\%");
		replaceStr = replaceStr.replace("'", "''");
		replaceStr = replaceStr.replace("‘", "\\‘");
		replaceStr = replaceStr.replace("’", "\\’");
		/* =========================================== */
		return replaceStr;
	}

	public static final String getValidStr(String param_str)
	{
		return (getValidStr(param_str, ""));
	}

	public static final String getValidStr(String param_str, String param_default)
	{
		return (param_str != null ? (!param_str.trim().equals("") ? param_str.trim() : param_default) : param_default);
	}

	public static final String toHtml(String source)
	{
		String target = Tools.getValidStr(source);
		target = target.replaceAll("&", "&amp;");
		target = target.replaceAll("\"", "&quot;");
		target = target.replaceAll("<", "&lt;");
		target = target.replaceAll(">", "&gt;");
		target = target.replaceAll("'", "&lsquo;");

		return target;
	}

	public static final String generateUUID()
	{
		String uuid = UUIDGenerator.getInstance().generateRandomBasedUUID().toString();
		uuid = uuid.replaceAll("-", "");
		return uuid;
	}

	public static final String uri2String(URI[] uri)
	{
		if (uri == null || uri.length == 0)
			return "";
		StringBuffer uris = new StringBuffer();
		int i = 0;
		for (URI u : uri)
		{
			if (i == 0)
				uris.append(u.toString());
			else
				uris.append(";" + u.toString());
			i++;
		}
		return uris.toString();
	}

	public static boolean isAscii(char c)
	{
		int k = 0x80;
		return c / k == 0 ? true : false;
	}

	/**
	 * 计算字符串长度,汉字两字节
	 * 
	 * @param s
	 * @return
	 */
	public static int length(String s)
	{
		if (s == null)
			return 0;
		char[] c = s.toCharArray();
		int len = 0;
		for (int i = 0; i < c.length; i++)
		{
			len++;
			if (!isAscii(c[i]))
			{
				len++;
			}
		}
		return len;
	}

	/**
	 * 把字节数组转换成16进制字符串
	 * 
	 * @param bArray
	 * @return
	 */
	public static final String byte2HexString(byte[] bArray)
	{
		String hs = "";
		String tmp = "";
		for (int i = 0; i < bArray.length; i++)
		{
			// 整数转成十六进制表示
			tmp = (Integer.toHexString(bArray[i] & 0XFF));
			if (tmp.length() == 1)
			{
				hs += "0" + tmp;
			}
			else
			{
				hs += tmp;
			}
		}
		return hs.toUpperCase(); // 转成大写
	}

	public static Date Str2Date(String date, String pattern)
	{
		if (date == null)
			return null;
		SimpleDateFormat sdf = null;
		if (pattern == null)
		{
			sdf = new SimpleDateFormat("yyyyMMddHHmmss");

		}
		else
		{
			sdf = new SimpleDateFormat(pattern);
		}

		Date d = null;
		try
		{
			d = sdf.parse(date);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return d;
	}

	public static String long2Date(long millis,String pattern)
	{
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(millis);
		
		if(pattern == null || "".equals(pattern))
		{
			return DateFormatUtils.format(now.getTime(), "yyyy-MM-dd HH:mm:ss");
		}
		else
		{
			return DateFormatUtils.format(now.getTime(), pattern);
		}
	}

	/**
	 * 正则表达式匹配
	 * 
	 * @return boolean
	 * @author hezhenbo
	 */
	public static boolean patternFind(String src, String regEx)
	{
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(src);
		return m.find();
	}
  public static String getTxtCharset(InputStream fis)
  {
	  String charset = "GBK";
		byte[] first3Bytes = new byte[3];
		try
		{
			boolean checked = false;
			BufferedInputStream bis = new BufferedInputStream(fis);
			bis.mark(0);
			int read = bis.read(first3Bytes, 0, 3);
			if (read == -1)
				return charset;
			if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE)
			{
				charset = "UTF-16LE";
				checked = true;
			}
			else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF)
			{
				charset = "UTF-16BE";
				checked = true;
			}
			else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB && first3Bytes[2] == (byte) 0xBF)
			{
				charset = "UTF-8";
				checked = true;
			}
			bis.reset();
			if (!checked)
			{
				int loc = 0;

				while ((read = bis.read()) != -1)
				{
					loc++;
					if (read >= 0xF0)
						break;
					if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
						break;
					if (0xC0 <= read && read <= 0xDF)
					{
						read = bis.read();
						if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
							// (0x80
							// -
							// 0xBF),也可能在GB编码内
							continue;
						else
							break;
					}
					else if (0xE0 <= read && read <= 0xEF)
					{// 也有可能出错，但是几率较小
						read = bis.read();
						if (0x80 <= read && read <= 0xBF)
						{
							read = bis.read();
							if (0x80 <= read && read <= 0xBF)
							{
								charset = "UTF-8";
								break;
							}
							else
								break;
						}
						else
							break;
					}
				}
			}

			bis.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return charset;
  }
	/**
	 * 判断文本文件编码格式 ANSI： 无格式定义； Unicode： 前两个字节为FFFE； Unicode big endian：
	 * 前两字节为FEFF； UTF-8： 前两字节为EFBB；
	 * 
	 * @param file
	 * @return
	 */
	public static String getTxtCharset(File file)
	{
		String charset = "GBK";
		try
		{
			FileInputStream  fis= new FileInputStream(file);
			charset = getTxtCharset(fis);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}	
		return charset;
	}

	/**
	 * 电信手机号码
	 * 
	 * @param mobile
	 * @return boolean
	 */
	public static boolean isMobile(String mobile)
	{
		return mobile.matches("^(((\\+{0,1}86){0,1})1\\d{10})$");
	}

	/**
	 * 电信手机号码
	 * 
	 * @param mobile
	 * @return boolean
	 */
	public static boolean isCtMobile(String mobile)
	{
		return mobile
				.matches("^(((\\+{0,1}86){0,1})133\\d{8})|(((\\+{0,1}86){0,1})153\\d{8})|(((\\+{0,1}86){0,1})189\\d{8})$");
	}

	public static boolean isEmail(String mail)
	{
		boolean b = mail.matches("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
		// System.out.println(b);
		return b;
	}

	// public static BufferedImage readImage(File file){
	//		
	// }
	//	 
	// extract metadata
	public static BufferedImage readImage(File file) throws IOException
	{

		// Get an ImageReader.
		ImageInputStream input = ImageIO.createImageInputStream(file);
		Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
		if (readers == null || !readers.hasNext())
		{
			throw new RuntimeException("No ImageReaders found");
		}
		ImageReader reader = readers.next();
		// input.reset();
		reader.setInput(input);
		System.out.println("Can Read Raster?" + reader.canReadRaster());
		System.out.println(reader.getWidth(0) + "x" + reader.getHeight(0));
		// reader.readRaster(imageIndex, param)
		String format = reader.getFormatName();
		if (!"JPEG".equalsIgnoreCase(format) && !"JPG".equalsIgnoreCase(format))
		{
			throw new RuntimeException("No ImageReaders found");
		}
		// IIOMetadata metadata = reader.getImageMetadata(0);
		// String metadataFormat = metadata.getNativeMetadataFormatName();
		// IIOMetadataNode iioNode = (IIOMetadataNode)
		// metadata.getAsTree(metadataFormat);
		// NodeList children = iioNode.getElementsByTagName("app14Adobe");
		// if (children.getLength() > 0) {
		// iioNode = (IIOMetadataNode) children.item(0);
		// int transform = Integer.parseInt(iioNode.getAttribute("transform"));
		Raster raster = reader.readRaster(0, reader.getDefaultReadParam());

		if (input != null)
		{
			input.close();
		}
		reader.dispose();
		// return createJPEG4(raster, transform);
		// return createJPEG4(raster, 0);
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		ColorModel cm = new ComponentColorModel(cs, false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
		return new BufferedImage(cm, (WritableRaster) raster, true, null);
		// }
		// else
		// {
		// throw new RuntimeException("No ImageReaders found");
		// }
	}

	/**
	 * Java's ImageIO can't process 4-component images <p/> and Java2D can't
	 * apply AffineTransformOp either, <p/> so convert raster data to RGB. <p/>
	 * Technique due to MArk Stephens. <p/> Free for any use.
	 */
	private static BufferedImage createJPEG4(Raster raster, int xform)
	{
		int w = raster.getWidth();
		int h = raster.getHeight();
		byte[] rgb = new byte[w * h * 3];

		// if (Adobe_APP14 and transform==2) then YCCK else CMYK
		if (xform == 2)
		{ // YCCK -- Adobe

			float[] Y = raster.getSamples(0, 0, w, h, 0, (float[]) null);
			float[] Cb = raster.getSamples(0, 0, w, h, 1, (float[]) null);
			float[] Cr = raster.getSamples(0, 0, w, h, 2, (float[]) null);
			float[] K = raster.getSamples(0, 0, w, h, 3, (float[]) null);

			for (int i = 0, imax = Y.length, base = 0; i < imax; i++, base += 3)
			{
				float k = 220 - K[i], y = 255 - Y[i], cb = 255 - Cb[i], cr = 255 - Cr[i];

				double val = y + 1.402 * (cr - 128) - k;
				val = (val - 128) * .65f + 128;
				rgb[base] = val < 0.0 ? (byte) 0 : val > 255.0 ? (byte) 0xff : (byte) (val + 0.5);

				val = y - 0.34414 * (cb - 128) - 0.71414 * (cr - 128) - k;
				val = (val - 128) * .65f + 128;
				rgb[base + 1] = val < 0.0 ? (byte) 0 : val > 255.0 ? (byte) 0xff : (byte) (val + 0.5);

				val = y + 1.772 * (cb - 128) - k;
				val = (val - 128) * .65f + 128;
				rgb[base + 2] = val < 0.0 ? (byte) 0 : val > 255.0 ? (byte) 0xff : (byte) (val + 0.5);
			}

		}
		else
		{
			// assert xform==0: xform;
			// CMYK

			int[] C = raster.getSamples(0, 0, w, h, 0, (int[]) null);
			int[] M = raster.getSamples(0, 0, w, h, 1, (int[]) null);
			int[] Y = raster.getSamples(0, 0, w, h, 2, (int[]) null);
			int[] K = raster.getSamples(0, 0, w, h, 3, (int[]) null);

			for (int i = 0, imax = C.length, base = 0; i < imax; i++, base += 3)
			{
				int c = 255 - C[i];
				int m = 255 - M[i];
				int y = 255 - Y[i];
				int k = 255 - K[i];
				float kk = k / 255f;

				rgb[base] = (byte) (255 - Math.min(255f, c * kk + k));
				rgb[base + 1] = (byte) (255 - Math.min(255f, m * kk + k));
				rgb[base + 2] = (byte) (255 - Math.min(255f, y * kk + k));
			}
		}

		// from other image types we know InterleavedRaster's can be
		// manipulated by AffineTransformOp, so create one of
		// those.
		raster = Raster.createInterleavedRaster(new DataBufferByte(rgb, rgb.length), w, h, w * 3, 3, new int[] { 0, 1,
				2 }, null);

		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		ColorModel cm = new ComponentColorModel(cs, false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
		return new BufferedImage(cm, (WritableRaster) raster, true, null);
	}

	/**
	 * 文件是否是UTF-8格式
	 * 
	 * @return
	 */
	public static boolean isUtf8(File file)
	{
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(file);
			int ch = -1;
			int i = 0;
			while ((ch = fis.read()) != -1)
			{
				if (ch < 0x80)
					continue;
				if (ch < 0xc0 || ch > 0xfd)
					return false;
				int count = ch > 0xfc ? 5 : ch > 0xf8 ? 4 : ch > 0xf0 ? 3 : ch > 0xe0 ? 2 : 1;
				if (i + count > file.length())
					return false;
				// for( int j = 0; j = 0xc0)
				// {
				// return false;
				// }
			}
		}
		catch (Exception e)
		{
		}
		finally
		{
			if (fis != null)
			{
				try
				{
					fis.close();
				}
				catch (IOException e)
				{
				}
			}
		}
		return true;
	}

	public static String charset(File file)
	{
		BufferedInputStream bis = null;
		try
		{
			bis = new BufferedInputStream(new FileInputStream(file));
			return charset(bis);
		}
		catch (FileNotFoundException e)
		{
		}
		finally
		{
			if (bis != null)
			{
				try
				{
					bis.close();
				}
				catch (IOException e)
				{
				}
			}
		}
		return "GBK";
	}

	public static String charset(InputStream bis)
	{
		String charset = "GBK";
		try
		{
			boolean checked = false;
			int read = 0;
			if (!checked)
			{
				while ((read = bis.read()) != -1)
				{
					if (read >= 0xF0)
						break;
					if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
						break;
					if (0xC0 <= read && read <= 0xDF)
					{
						read = bis.read();
						if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
							// (0x80
							// - 0xBF),也可能在GB编码内
							continue;
						else
							break;
					}
					else if (0xE0 <= read && read <= 0xEF)
					{// 也有可能出错，但是几率较小
						read = bis.read();
						if (0x80 <= read && read <= 0xBF)
						{
							read = bis.read();
							if (0x80 <= read && read <= 0xBF)
							{
								charset = "UTF-8";
								break;
							}
							else
								break;
						}
						else
							break;
					}
				}
			}
		}
		catch (Exception e)
		{
		}
		finally
		{
			try
			{
				bis.close();
			}
			catch (IOException e)
			{
			}
		}

		return charset;
	}
	
	public static String charset(byte[] payload, int length)
	{
		String charset = "GBK";
		try
		{
			boolean checked = false;
			int read = 0;
			if (!checked)
			{
				for( int i = 0; i < length; i++ )
				{
					read = payload[i];
					if (read >= 0xF0)
						break;
					if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
						break;
					if (0xC0 <= read && read <= 0xDF)
					{
						read = payload[i++];
						if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
							// (0x80
							// - 0xBF),也可能在GB编码内
							continue;
						else
							break;
					}
					else if (0xE0 <= read && read <= 0xEF)
					{// 也有可能出错，但是几率较小
						read = payload[i++];
						if (0x80 <= read && read <= 0xBF)
						{
							read = payload[i++];
							if (0x80 <= read && read <= 0xBF)
							{
								charset = "UTF-8";
								break;
							}
							else
								break;
						}
						else
							break;
					}
				}
			}
		}
		catch (Exception e)
		{
		}
		return charset;
	}

	public static String escape(String src)
	{
		int i;
		char j;
		StringBuffer tmp = new StringBuffer();
		tmp.ensureCapacity(src.length() * 6);

		for (i = 0; i < src.length(); i++)
		{

			j = src.charAt(i);

			if (Character.isDigit(j) || Character.isLowerCase(j) || Character.isUpperCase(j))
				tmp.append(j);
			else if (j < 256)
			{
				tmp.append("%");
				if (j < 16)
					tmp.append("0");
				tmp.append(Integer.toString(j, 16));
			}
			else
			{
				tmp.append("%u");
				tmp.append(Integer.toString(j, 16));
			}
		}
		return tmp.toString();
	}

	public static String unescape(String src)
	{
		StringBuffer tmp = new StringBuffer();
		tmp.ensureCapacity(src.length());
		int lastPos = 0, pos = 0;
		char ch;
		while (lastPos < src.length())
		{
			pos = src.indexOf("%", lastPos);
			if (pos == lastPos)
			{
				if (src.charAt(pos + 1) == 'u')
				{
					ch = (char) Integer.parseInt(src.substring(pos + 2, pos + 6), 16);
					tmp.append(ch);
					lastPos = pos + 6;
				}
				else
				{
					ch = (char) Integer.parseInt(src.substring(pos + 1, pos + 3), 16);
					tmp.append(ch);
					lastPos = pos + 3;
				}
			}
			else
			{
				if (pos == -1)
				{
					tmp.append(src.substring(lastPos));
					lastPos = src.length();
				}
				else
				{
					tmp.append(src.substring(lastPos, pos));
					lastPos = pos;
				}
			}
		}
		return tmp.toString();
	}

	/**
	 * 得到任意位长的随机数
	 * @param len
	 * @return
	 */
	public static String getRandom(int len)
	{
		Random r = new Random();
		if (len == 0)
			return "";
		String random = "";
		for (int k = 0; k < len; k++)
		{
			random += r.nextInt(9);
		}
		return random;
	}
	
	/**
	 * 将毫秒转化为HH:mm:ss.S
	 * @param ms
	 * @return
	 */
	public static String format(long ms)
	{
		int ss = 1000;
		int mi = ss * 60;
		int hh = mi * 60;
		int dd = hh * 24;
		
		long day = ms / dd;
		long hour = (ms - day * dd)/hh;
		long minute = (ms - day * dd - hour * hh)/mi;
		long second = (ms - day * dd - hour * hh -minute *mi)/ss;
		long milliSecond = ms - day * dd - hour * hh -minute *mi - second * ss;
		
		//String strDay = day < 10 ? "0" + day : ""+day;
		String strHour = hour < 10 ? "0" + hour : ""+hour;
		String strMinute = minute < 10 ? "0" + minute : ""+minute;
		String strSecond = second < 10 ? "0" + second : ""+second;
		String strMilliSecond = milliSecond < 10 ? "0" + milliSecond : ""+milliSecond;
		strMilliSecond = milliSecond < 100 ? "0" + strMilliSecond : ""+strMilliSecond;
		return strHour+":"+strMinute+":"+strSecond+"."+strMilliSecond;
	}
	/**
	 * 电信手机号码
	 * @param mobile
	 * @return boolean
	 */
	public static boolean isMobile4Ctm(String mobile)
	{
		return mobile.matches("^(133|153|189){1}([0-9]){8}$");
	}
	
	/**
	 * 合法手机号
	 * @param number
	 * @return
	 */
	public static boolean isMobileNumber(String number)
	{
		return number.matches("^((13[0-9])|(15[^4])|(18[0|5-9])){1}([0-9]){8}$");
	}
	public static String generateToken()
	{
		String token = UUIDGenerator.getInstance().generateRandomBasedUUID().toString();
		token = token.substring(token.lastIndexOf("-")+1);
		return token;
	}
	
	/**
	 * 点码格式转换
	 * @param codeclass 点码格式，14位或者24位
	 * @param spcode 待转换的点码，十进制
	 * @return transSpcode 转换后的，3-8-3 or 8-8-8
	 */
	public static String getTransSpcode(String spcode,int codeclass)
	{
		StringBuffer transSpcode = new StringBuffer();
		
        String binaryNum = Integer.toBinaryString(Integer.parseInt(spcode));
		
		int numLength = binaryNum.length();
		StringBuffer zeros = new StringBuffer();
		
		//转换为2进制后，位数不够前面补0
		if (numLength < codeclass)
		{
			for (int zero = 0;zero < codeclass - numLength;zero++)
			{
				zeros.append("0");
			}
			binaryNum = zeros.toString() + binaryNum;
		}
		
		//14位的按3-8-3格式显示
		if (codeclass == 14)
		{
			//第一次截取前三位
			transSpcode.append(Integer.toHexString(Integer.valueOf(binaryNum.substring(0,3),2)));
			transSpcode.append("-");
			
			//第二次截取中间八位
			transSpcode.append(Integer.toHexString(Integer.valueOf(binaryNum.substring(3,11),2)));
			transSpcode.append("-");
			
			//第三次截取最后三位
			transSpcode.append(Integer.toHexString(Integer.valueOf(binaryNum.substring(11,14),2)));
		}
		
		//24位的按8-8-8格式显示
		if (codeclass == 24)
		{
			//第一次截取前八位
			transSpcode.append(Integer.toHexString(Integer.valueOf(binaryNum.substring(0,8),2)));
			transSpcode.append("-");
			
			//第二次截取中间八位
			transSpcode.append(Integer.toHexString(Integer.valueOf(binaryNum.substring(8,16),2)));
			transSpcode.append("-");
			
			//第三次截取最后八位
			transSpcode.append(Integer.toHexString(Integer.valueOf(binaryNum.substring(16,24),2)));
		}
		return transSpcode.toString();
		
	}
    /**
     * 将标准的长整形的时间转换成指定格式的时间表现
     * @param TimeFormat 可配的时间表现的格式（可以是yyyy年MM月dd日 HH:mm:ss）
     * @param millis
     * @return
     */
    public static final String getFormatTime(String TimeFormat, long millis)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(TimeFormat);
        java.util.Calendar time = java.util.Calendar.getInstance();
        time.setTimeInMillis(millis);

        return sdf.format(time.getTime());
    }

    /**
     * 将标准的整形的时间转换成指定格式的时间表现
     * @param TimeFormat 可配的时间表现的格式（可以是yyyy年MM月dd日 HH:mm:ss）
     * @param seconds
     * @return
     */
    public static final String getFormatTime(String TimeFormat, int seconds)
    {
        return getFormatTime(TimeFormat, ( (long) seconds) * 1000);
    }

	public static void main(String[] args) throws Exception
	{
//		System.err.println(location("118.113.1.185"));
//		System.out.println(unicode2Chr("%CC%DA%D1%B6"));
//		System.out.println(chr2Unicode("腾讯").toUpperCase());
//		System.out.println(getTimeFormat("1912-12-13 32:44"));
//		System.out.println(getTimeFormat("13 32:44:44"));
//		System.out.println(getTimeFormat("12-13 32:44:44"));
//		System.out.println(getTimeFormat("1912-12-13 32:44:44"));
//		System.out.println(getTimeFormat("1912-12-13"));
//		System.out.println(getTimeFormat("19121213234422"));
//		System.out.println(getTimeFormat("12"));
	}
}
