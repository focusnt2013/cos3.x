package com.focus.cos.api.email;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.htmlparser.Parser;
import org.htmlparser.util.ParserException;

import com.focus.cos.api.Assert;
import com.focus.cos.api.email.io.InputStreamResource;
import com.focus.cos.api.email.io.UrlResource;
import com.focus.cos.api.email.javamail.JavaMailSenderImpl;
import com.focus.cos.api.email.javamail.MimeMessageHelper;
import com.focus.util.Tools;

public class EmailSendProxy
{
	/**
	 * 发送文本邮件
	 * 
	 * @param host smtp主机
	 * @param userName 用户名
	 * @param password 密码
	 * @param auth 是否鉴权（true或false）
	 * @param to 发送邮件地址
	 * @param subject 标题
	 * @param content 内容
	 * @return 返回0表示发送成功
	 */
	public static void main(String[] args)
	{
//		ArrayList<String> attachList = new ArrayList<String>();
		// attachList.add("EMA://192.168.88.160:9529/laputa/log/showExcption");
//		attachList.add("/laputa/log/showExcption");
//		int i = sendTextMail("smtp.163.com", "laputa_brd@163.com", "laputa_brd", "LaputaNo1", "true", "hankthon@163.com;cuid@broadtech.com.cn", "主题",
//								"内容人人<br />人人人人\n人人人人人人", attachList);
		// sendHtmlMail("smtp.163.com", "laputa_brd@163.com",
		// "laputa_brd","LaputaNo1", "true", "hankthon@163.com", "主题",
		// "内容人人人人人人人人人人人人", new ArrayList());
//		System.out.println(i);
	}

	public static int sendTextMail(String host, String from, String userName, String password, String auth, String to, String subject, String content,
			ArrayList<String> attchMailFilename)
	{
		if (host == null || host.length() == 0)
		{
			Assert.notNull(host, "host must not be null");
		}
		if (userName == null || userName.length() == 0)
		{
			Assert.notNull(userName, "userName must not be null");
		}
		if (to == null || to.length() == 0)
		{
			Assert.notNull(to, "Mail to Address must not be null");
		}
		JavaMailSenderImpl sender = new JavaMailSenderImpl();
		sender.setHost(host);
		sender.setUsername(userName);
		sender.setPassword(password);

		Properties pos = new Properties();
		pos.put("mail.smtp.auth", auth);
		pos.put("mail.smtp.timeout", "25000");
		pos.put("mail.smtp.localhost", "localhost");
		sender.setJavaMailProperties(pos);

		// SimpleMailMessage mail = new SimpleMailMessage();
		MimeMessage msg = sender.createMimeMessage();
		MimeMessageHelper helper;
		try
		{
			helper = new MimeMessageHelper(msg, true, "utf-8");
			helper.setFrom(from);
			if (to != null && to.length() > 0 && to.indexOf(";") != -1)
			{
				String[] tos = to.split(";");
				helper.setTo(tos);
			}
			else
			{
				helper.setTo(to);
			}
			msg.setSubject(MimeUtility.encodeText(subject,"gb2312","B"));
			msg.setText(content);
			if (!attchMailFilename.isEmpty())
			{
				// 新建一个MimeMultipart对象用来存放多个BodyPart对象
				Multipart container = new MimeMultipart();
				// 新建一个存放信件内容的BodyPart对象
				BodyPart textBodyPart = new MimeBodyPart();
				// 给BodyPart对象设置内容和格式/编码方式
				textBodyPart.setContent(content, "text/plain;charset=gbk");
				// 将含有信件内容的BodyPart加入到MimeMultipart对象中
				container.addBodyPart(textBodyPart);
				Iterator<String> fileIterator = attchMailFilename.iterator();
				while (fileIterator.hasNext())
				{// 迭代所有附件
					String attachmentString = fileIterator.next();
					if (attachmentString.startsWith("EMA://"))
					{
						int startHost = attachmentString.indexOf(":") + 3;
						int endHost = attachmentString.indexOf("/", startHost);
						String host1 = attachmentString.substring(6, endHost);
						int ipEnd = host1.indexOf(":");
						String ip = host1.substring(0, ipEnd);
						int port = Integer.valueOf(host1.substring(ipEnd + 1));
						attachmentString = attachmentString.substring(endHost);
						byte[] payload = new byte[256];
						int offset = 0;
						payload[offset++] = (byte) 2;
						payload[offset++] = (byte) 1;
						byte buf1[] = attachmentString.getBytes();
						Tools.intToBytes(buf1.length, payload, offset, 2);
						offset += 2;
						offset = Tools.copyByteArray(buf1, payload, offset);
						OutputStream out = null;
						DatagramSocket datagramSocket = null;
						ServerSocket ss = null;
						Socket socket = null;
						// int port = 0;
						// SendNatRequest nat = null;

						try
						{
							datagramSocket = new DatagramSocket();
							datagramSocket.setSoTimeout(3000);
							InetAddress addr = InetAddress.getByName(ip);
							// DatagramPacket request = new DatagramPacket(new
							// byte[]{-1}, 0, 1, addr,
							// omtMgr.getControlPort(host) );
							// for(int i = 0; i < 10; i++ )
							// {
							// datagramSocket.send( request );
							// Thread.sleep(100);
							// })
							DatagramPacket request = new DatagramPacket(payload, 0, offset, addr, port);
							datagramSocket.send(request);

							port = datagramSocket.getLocalPort();
							ss = new ServerSocket(port);
							ss.setSoTimeout(10000);
							socket = ss.accept();
							InputStream is = socket.getInputStream();
							int beginIndex = attachmentString.lastIndexOf("/");
							String filename = attachmentString.substring(beginIndex + 1);
							if (is.read() == 0)// 0表示文本，1表示二进制
							{

								int beginIndex1 = filename.lastIndexOf(".");
								filename = beginIndex != -1 ? (filename.substring(0, beginIndex1) + ".txt") : (filename + ".txt");
								// msg.setContentType("text/plain;charset=utf8");
								// getResponse().setHeader("Content-disposition",
								// "inline; filename="+filename);
							}
							else
							{
								int beginIndex1 = filename.lastIndexOf(".");
								filename = beginIndex != -1 ? (filename.substring(0, beginIndex1) + ".zip") : (filename + ".zip");
								// getResponse().setContentType("application/binary;charset=ISO8859_1");
								// getResponse().setHeader("Content-disposition",
								// "attachment; filename="+filename);
							}
							// attachmentString =
							// attachmentString.substring(0,beginIndex+1)+"temp/"+filename;
							attachmentString = attachmentString.substring(0, attachmentString.indexOf("/", 2) + 1) + "log/temp/" + filename;
							File localFile = new File(attachmentString);
							if (!localFile.getParentFile().exists())
							{
								localFile.getParentFile().mkdirs();
							}
							localFile.createNewFile();
							out = new BufferedOutputStream(new FileOutputStream(localFile));

							int ch;
							while ((ch = is.read()) != -1)
							{
								out.write(ch);
							}
							out.flush();
							out.close();
							is.close();

						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
						finally
						{
							try
							{

								if (datagramSocket != null)
									datagramSocket.close();
								if (socket != null)
									socket.close();
								if (ss != null)
									ss.close();
								// if( out != null ) out.close();
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
						}

					}
					// 新建一个存放信件附件的BodyPart对象
					BodyPart fileBodyPart = new MimeBodyPart();
					// 将本地文件作为附件
					FileDataSource fds = new FileDataSource(attachmentString);
					fileBodyPart.setDataHandler(new DataHandler(fds));
					// 处理邮件中附件文件名的中文问题
					String attachName = fds.getName();
					attachName = MimeUtility.encodeText(attachName);
					// 设定附件文件名
					fileBodyPart.setFileName(attachName);
					// 将附件的BodyPart对象加入到container中

					container.addBodyPart(fileBodyPart);
				}
				// 将container作为消息对象的内容
				msg.setContent(container);
			}
			else
			{// 没有附件的情况
				msg.setContent(content, "text/html;charset=gbk");
			}

			try
			{
				sender.send(msg);
			}
			catch (Exception ex)
			{
				System.out.println("&&&&&&&&&&&&&&&&&&&&&Error Happened&&&&&&&&&&&&&&&&&&&&&&&");
				ex.printStackTrace();
			}
		}
		catch (MessagingException e)
		{
			e.printStackTrace();
			return -1;
		}

		catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	/**
	 * 发送HTML邮件
	 * 
	 * @param host smtp主机
	 * @param userName 用户名
	 * @param password 密码
	 * @param auth 是否鉴权
	 * @param to 邮件地址
	 * @param subject 标题
	 * @param content 内容(HTML链接)
	 * @return 返回0表示发送成功
	 */
	public static int sendHtmlMail(String host, String from, String userName, String password, String auth, String to, String subject, String content,
			ArrayList<String> attchMailFilename)
	{

		if (host == null || host.length() == 0)
		{
			Assert.notNull(host, "host must not be null");
		}
		if (userName == null || userName.length() == 0)
		{
			Assert.notNull(userName, "userName must not be null");
		}
		if (to == null || to.length() == 0)
		{
			Assert.notNull(to, "Mail to Address must not be null");
		}
		JavaMailSenderImpl sender = new JavaMailSenderImpl();
		sender.setHost(host);
		sender.setUsername(userName);
		sender.setPassword(password);

		Properties pos = new Properties();
		pos.put("mail.smtp.auth", auth);
		pos.put("mail.smtp.timeout", "25000");
		sender.setJavaMailProperties(pos);

		MimeMessage msg = sender.createMimeMessage();
		MimeMessageHelper helper;
		try
		{
			helper = new MimeMessageHelper(msg, true, "utf-8");
			if (to != null && to.length() > 0 && to.indexOf(";") != -1)
			{
				String[] tos = to.split(";");
				helper.setTo(tos);
			}
			else
			{
				helper.setTo(to);
			}
			helper.setFrom(from);
			helper.setSubject(subject);

			if (content != null && content.length() > 0 && content.indexOf(";") != -1)
			{
				String[] urls = content.split(";");
				if (urls != null && urls.length > 0)
				{
					String html = "";
					String imgs = "";
					HashMap<String, String> img = new HashMap<String, String>();
					HttpClient httpclient = new HttpClient();
					for (int i = 0; i < urls.length; i++)
					{
						GetMethod getMethod = new GetMethod(urls[i]);
						httpclient.executeMethod(getMethod);

						Header ct = getMethod.getResponseHeader("Content-Type");
						if (ct != null && ct.getValue() != null)
						{
							if (ct.getValue().toLowerCase().indexOf("text") != -1)
							{
								Parser parser = new Parser(urls[i]);
								parser.setEncoding("utf-8");

								URL url = new URL(urls[i]);
								HtmlparserUrlModifier modifier = new HtmlparserUrlModifier(url);
								parser.visitAllNodesWith(modifier);

								html = modifier.getModifiedResult();

								HashMap<String, String> imgMap = modifier.getImgMap();
								img.putAll(imgMap);
							}
							else if (ct.getValue().toLowerCase().indexOf("image") != -1)
							{
								int width = 400;
								int height = 300;
								String[] params = urls[i].split("\\?");
								if (params != null && params.length > 1)
								{
									String[] param = params[1].split("&");
									if (param != null && param.length > 0)
									{
										for (int j = 0; j < param.length; j++)
										{
											String[] kv = param[j].split("=");
											if (kv.length > 0)
											{
												if (kv[0].toLowerCase().equals("width"))
												{
													if (kv.length > 1)
													{
														width = Integer.parseInt(kv[1]);
													}
												}
												else if (kv[0].toLowerCase().equals("height"))
												{
													if (kv.length > 1)
													{
														height = Integer.parseInt(kv[1]);
													}
												}
											}
										}
									}
								}
								String key = DigestUtils.md5Hex(urls[i]);
								imgs += "<img src = 'cid:" + key + "' width='" + width + "' height='" + height + "'><br/>";
								img.put(key, urls[i]);
							}
							else
							{
								String key = DigestUtils.md5Hex(urls[i]);
								imgs += "<img src = 'cid:" + key + "' width=400' height='300'><br/>";
								img.put(key, urls[i]);
							}
						}
						else
						{
							String key = DigestUtils.md5Hex(urls[i]);
							imgs += "<img src = 'cid:" + key + "' width=400' height='300'><br/>";
							img.put(key, urls[i]);
						}
					}
					if (html.equals(""))
					{
						html = "<html><head></head><body></body></html>";
					}
					int index = html.indexOf("</body>");
					StringBuffer buf = new StringBuffer(html);
					if (index > 0 && imgs.length() > 0)
					{
						buf.insert(index, imgs);
					}
					else
					{
						buf.append(imgs);
					}
					helper.setText(buf.toString(), true);
					Set<String> set = img.keySet();
					Iterator<?> it = set.iterator();
					while (it.hasNext())
					{
						String key = (String) it.next();
						helper.addInline(key, new UrlResource(img.get(key)));
					}
					if (!attchMailFilename.isEmpty())
					{

						// 新建一个MimeMultipart对象用来存放多个BodyPart对象
						Multipart container = new MimeMultipart();
						// 新建一个存放信件内容的BodyPart对象
						BodyPart textBodyPart = new MimeBodyPart();
						// 给BodyPart对象设置内容和格式/编码方式
						textBodyPart.setContent(content, "text/html;charset=gbk");
						// 将含有信件内容的BodyPart加入到MimeMultipart对象中
						container.addBodyPart(textBodyPart);
						Iterator<String> fileIterator = attchMailFilename.iterator();
						while (fileIterator.hasNext())
						{// 迭代所有附件
							String attachmentString = fileIterator.next();
							if (attachmentString.startsWith("EMA://"))
							{
								int startHost = attachmentString.indexOf(":") + 3;
								int endHost = attachmentString.indexOf("/", startHost);
								String host1 = attachmentString.substring(6, endHost);
								int ipEnd = host1.indexOf(":");
								String ip = host1.substring(0, ipEnd);
								int port = Integer.valueOf(host1.substring(ipEnd + 1));
								attachmentString = attachmentString.substring(endHost);
								byte[] payload = new byte[256];
								int offset = 0;
								payload[offset++] = (byte) 2;
								payload[offset++] = (byte) 1;
								byte buf1[] = attachmentString.getBytes();
								Tools.intToBytes(buf1.length, payload, offset, 2);
								offset += 2;
								offset = Tools.copyByteArray(buf1, payload, offset);
								OutputStream out = null;
								DatagramSocket datagramSocket = null;
								ServerSocket ss = null;
								Socket socket = null;
								// int port = 0;
								// SendNatRequest nat = null;

								try
								{
									datagramSocket = new DatagramSocket();
									datagramSocket.setSoTimeout(3000);
									InetAddress addr = InetAddress.getByName(ip);
									// DatagramPacket request = new
									// DatagramPacket(new byte[]{-1}, 0, 1,
									// addr, omtMgr.getControlPort(host) );
									// for(int i = 0; i < 10; i++ )
									// {
									// datagramSocket.send( request );
									// Thread.sleep(100);
									// })
									DatagramPacket request = new DatagramPacket(payload, 0, offset, addr, port);
									datagramSocket.send(request);

									port = datagramSocket.getLocalPort();
									ss = new ServerSocket(port);
									ss.setSoTimeout(10000);
									socket = ss.accept();
									InputStream is = socket.getInputStream();
									int beginIndex = attachmentString.lastIndexOf("/");
									String filename = attachmentString.substring(beginIndex + 1);
									if (is.read() == 0)// 0表示文本，1表示二进制
									{

										int beginIndex1 = filename.lastIndexOf(".");
										filename = beginIndex != -1 ? (filename.substring(0, beginIndex1) + ".txt") : (filename + ".txt");
										// msg.setContentType("text/plain;charset=utf8");
										// getResponse().setHeader("Content-disposition",
										// "inline; filename="+filename);
									}
									else
									{
										int beginIndex1 = filename.lastIndexOf(".");
										filename = beginIndex != -1 ? (filename.substring(0, beginIndex1) + ".zip") : (filename + ".zip");
										// getResponse().setContentType("application/binary;charset=ISO8859_1");
										// getResponse().setHeader("Content-disposition",
										// "attachment; filename="+filename);
									}
									// attachmentString =
									// attachmentString.substring(0,beginIndex+1)+"temp/"+filename;
									attachmentString = attachmentString.substring(0, attachmentString.indexOf("/", 2) + 1) + "log/temp/" + filename;
									File localFile = new File(attachmentString);
									if (!localFile.getParentFile().exists())
									{
										localFile.getParentFile().mkdirs();
									}
									localFile.createNewFile();
									out = new BufferedOutputStream(new FileOutputStream(localFile));

									int ch;
									while ((ch = is.read()) != -1)
									{
										out.write(ch);
									}

								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
								finally
								{
									try
									{

										if (datagramSocket != null)
											datagramSocket.close();
										if (socket != null)
											socket.close();
										if (ss != null)
											ss.close();
										if (out != null)
										{
											out.flush();
											out.close();
										}

										// if( out != null ) out.close();
									}
									catch (IOException e)
									{
										e.printStackTrace();
									}
								}

							}

							// 新建一个存放信件附件的BodyPart对象
							BodyPart fileBodyPart = new MimeBodyPart();
							// 将本地文件作为附件
							FileDataSource fds = new FileDataSource(attachmentString);
							fileBodyPart.setDataHandler(new DataHandler(fds));
							// 处理邮件中附件文件名的中文问题
							String attachName = fds.getName();
							attachName = MimeUtility.encodeText(attachName);
							// 设定附件文件名
							fileBodyPart.setFileName(attachName);
							// 将附件的BodyPart对象加入到container中
							container.addBodyPart(fileBodyPart);
						}
						// 将container作为消息对象的内容
						msg.setContent(container);
					}
					else
					{// 没有附件的情况
						msg.setContent(content, "text/html;charset=gbk");
					}
					sender.send(msg);
				}
			}
			else
			{
				String html = "";
				String imgs = "";
				HashMap<String, String> img = new HashMap<String, String>();
				HttpClient httpclient = new HttpClient();
				Header ct = null;
				try
				{
					GetMethod getMethod = new GetMethod(content);
					ct = getMethod.getResponseHeader("Content-Type");
					httpclient.executeMethod(getMethod);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				if (ct != null && ct.getValue() != null)
				{
					if (ct.getValue().toLowerCase().indexOf("text") != -1)
					{
						Parser parser = new Parser(content);
						parser.setEncoding("utf-8");
						URL url = new URL(content);
						HtmlparserUrlModifier modifier = new HtmlparserUrlModifier(url);
						parser.visitAllNodesWith(modifier);

						html = modifier.getModifiedResult();
						HashMap<String, String> imgMap = modifier.getImgMap();
						img.putAll(imgMap);
					}
					else if (ct.getValue().toLowerCase().indexOf("image") != -1)
					{
						int width = 400;
						int height = 300;
						String[] params = content.split("\\?");
						if (params != null && params.length > 1)
						{
							String[] param = params[1].split("&");
							if (param != null && param.length > 0)
							{
								for (int j = 0; j < param.length; j++)
								{
									String[] kv = param[j].split("=");
									if (kv.length > 0)
									{
										if (kv[0].toLowerCase().equals("width"))
										{
											if (kv.length > 1)
											{
												width = Integer.parseInt(kv[1]);
											}
										}
										else if (kv[0].toLowerCase().equals("height"))
										{
											if (kv.length > 1)
											{
												height = Integer.parseInt(kv[1]);
											}
										}
									}
								}
							}
						}
						String key = DigestUtils.md5Hex(content);
						imgs += "<img src = 'cid:" + key + "' width='" + width + "' height='" + height + "'><br/>";
						img.put(key, content);
					}
					else
					{
						String key = DigestUtils.md5Hex(content);
						imgs += "<img src = 'cid:" + key + "' width=400' height='300'><br/>";
						img.put(key, content);
					}
				}
				else
				{
					String key = DigestUtils.md5Hex(content);
					imgs += "<img src = 'cid:" + key + "' width=400' height='300'><br/>";
					img.put(key, content);
				}

				if (html.equals(""))
				{
					html = "<html><head></head><body></body></html>";
				}
				int index = html.indexOf("</body>");
				StringBuffer buf = new StringBuffer(html);
				if (index > 0 && imgs.length() > 0)
				{
					buf.insert(index, imgs);
				}
				else
				{
					buf.append(imgs);
				}
				helper.setText(buf.toString(), true);// true表示发送的是html邮件
				Set<String> set = img.keySet();
				Iterator<?> it = set.iterator();

				while (it.hasNext())
				{
					String key = (String) it.next();
					helper.addInline(key, new UrlResource(img.get(key)));
				}
				if (!attchMailFilename.isEmpty())
				{

					// 新建一个MimeMultipart对象用来存放多个BodyPart对象
					Multipart container = new MimeMultipart();
					// 新建一个存放信件内容的BodyPart对象
					BodyPart textBodyPart = new MimeBodyPart();
					// 给BodyPart对象设置内容和格式/编码方式
					textBodyPart.setContent(content, "text/html;charset=gbk");
					// 将含有信件内容的BodyPart加入到MimeMultipart对象中
					container.addBodyPart(textBodyPart);
					Iterator<String> fileIterator = attchMailFilename.iterator();
					while (fileIterator.hasNext())
					{// 迭代所有附件
						String attachmentString = fileIterator.next();
						if (attachmentString.startsWith("EMA://"))
						{
							int startHost = attachmentString.indexOf(":") + 3;
							int endHost = attachmentString.indexOf("/", startHost);
							String host1 = attachmentString.substring(6, endHost);
							int ipEnd = host1.indexOf(":");
							String ip = host1.substring(0, ipEnd);
							int port = Integer.valueOf(host1.substring(ipEnd + 1));
							attachmentString = attachmentString.substring(endHost);
							byte[] payload = new byte[256];
							int offset = 0;
							payload[offset++] = (byte) 2;
							payload[offset++] = (byte) 1;
							byte buf1[] = attachmentString.getBytes();
							Tools.intToBytes(buf1.length, payload, offset, 2);
							offset += 2;
							offset = Tools.copyByteArray(buf1, payload, offset);
							OutputStream out = null;
							DatagramSocket datagramSocket = null;
							ServerSocket ss = null;
							Socket socket = null;
							// int port = 0;
							// SendNatRequest nat = null;

							try
							{
								datagramSocket = new DatagramSocket();
								datagramSocket.setSoTimeout(3000);
								InetAddress addr = InetAddress.getByName(ip);
								// DatagramPacket request = new
								// DatagramPacket(new byte[]{-1}, 0, 1, addr,
								// omtMgr.getControlPort(host) );
								// for(int i = 0; i < 10; i++ )
								// {
								// datagramSocket.send( request );
								// Thread.sleep(100);
								// })
								DatagramPacket request = new DatagramPacket(payload, 0, offset, addr, port);
								datagramSocket.send(request);

								port = datagramSocket.getLocalPort();
								ss = new ServerSocket(port);
								ss.setSoTimeout(10000);
								socket = ss.accept();

								InputStream is = socket.getInputStream();
								int beginIndex = attachmentString.lastIndexOf("/");
								String filename = attachmentString.substring(beginIndex + 1);
								if (is.read() == 0)// 0表示文本，1表示二进制
								{

									int beginIndex1 = filename.lastIndexOf(".");
									filename = beginIndex != -1 ? (filename.substring(0, beginIndex1) + ".txt") : (filename + ".txt");
									// msg.setContentType("text/plain;charset=utf8");
									// getResponse().setHeader("Content-disposition",
									// "inline; filename="+filename);
								}
								else
								{
									int beginIndex1 = filename.lastIndexOf(".");
									filename = beginIndex != -1 ? (filename.substring(0, beginIndex1) + ".zip") : (filename + ".zip");
									// getResponse().setContentType("application/binary;charset=ISO8859_1");
									// getResponse().setHeader("Content-disposition",
									// "attachment; filename="+filename);
								}
								// attachmentString =
								// attachmentString.substring(0,beginIndex+1)+"temp/"+filename;
								attachmentString = attachmentString.substring(0, attachmentString.indexOf("/", 2) + 1) + "log/temp/" + filename;
								File localFile = new File(attachmentString);
								if (!localFile.getParentFile().exists())
								{
									localFile.getParentFile().mkdirs();
								}
								localFile.createNewFile();
								out = new BufferedOutputStream(new FileOutputStream(localFile));

								int ch;
								while ((ch = is.read()) != -1)
								{
									out.write(ch);
								}

							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
							finally
							{
								try
								{

									if (datagramSocket != null)
										datagramSocket.close();
									if (socket != null)
										socket.close();
									if (ss != null)
										ss.close();
									if (out != null)
									{
										out.flush();
										out.close();
									}

									// if( out != null ) out.close();
								}
								catch (IOException e)
								{
									e.printStackTrace();
								}
							}

						}

						// 新建一个存放信件附件的BodyPart对象
						BodyPart fileBodyPart = new MimeBodyPart();
						// 将本地文件作为附件
						FileDataSource fds = new FileDataSource(attachmentString);
						fileBodyPart.setDataHandler(new DataHandler(fds));
						// 处理邮件中附件文件名的中文问题
						String attachName = fds.getName();
						attachName = MimeUtility.encodeText(attachName);
						// 设定附件文件名
						fileBodyPart.setFileName(attachName);
						// 将附件的BodyPart对象加入到container中
						container.addBodyPart(fileBodyPart);
					}
					// 将container作为消息对象的内容
					msg.setContent(container);
				}
				else
				{// 没有附件的情况
					msg.setContent(content, "text/html;charset=gbk");
				}
				sender.send(msg);

			}

		}
		catch (MessagingException e)
		{
			e.printStackTrace();
			return -1;
		}
		catch (ParserException e)
		{
			e.printStackTrace();
			return -1;
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
			return -1;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}
		return 0;
	}
/*
	public static void sendMixMail(String host, String from, String userName, String password, String auth, String to, String subject, String content,
			byte[] data,String attachments)
	{
		if (host == null || host.length() == 0)
		{
			Assert.notNull(host, "host must not be null");
		}
		if (userName == null || userName.length() == 0)
		{
			Assert.notNull(userName, "userName must not be null");
		}
		if (to == null || to.length() == 0)
		{
			Assert.notNull(to, "Mail to Address must not be null");
		}
		JavaMailSenderImpl sender = new JavaMailSenderImpl();
		sender.setHost(host);
		sender.setUsername(userName);
		sender.setPassword(password);

		Properties pos = new Properties();
		pos.put("mail.smtp.auth", auth);
		pos.put("mail.smtp.timeout", "25000");
		pos.put("mail.smtp.localhost", "localhost");
		sender.setJavaMailProperties(pos);

		MimeMessage msg = sender.createMimeMessage();
		MimeMessageHelper helper;
		try
		{
			helper = new MimeMessageHelper(msg, true, "utf-8");
			if (to != null && to.length() > 0 && to.indexOf(";") != -1)
			{
				String[] tos = to.split(";");
				helper.setTo(tos);
			}
			else
			{
				helper.setTo(to);
			}
			helper.setFrom(from);
			helper.setSubject(MimeUtility.encodeText(subject,"gb2312","B"));
			Multipart mp = new MimeMultipart("related");
			if (content != null)
			{
				BodyPart bodyPart = new MimeBodyPart();
				bodyPart.setDataHandler(new DataHandler(content, "text/html;charset=GBK"));
				mp.addBodyPart(bodyPart);
			}
			if (data != null)
			{
				MultiPartData multiPartData = new MultiPartData(data);
				Enumeration<?> e = multiPartData.elements();
				int count = 0;
				while (e.hasMoreElements())
				{
					count++;
					MultiPartEntry multiPartEntry = (MultiPartEntry) e.nextElement();
					String contentType = multiPartEntry.getContentType();
					byte payload[] = multiPartEntry.getPayload();
					if (contentType.indexOf(EmailHelper.CT_IMAGE_GIF) != -1 || contentType.indexOf(EmailHelper.CT_IMAGE_JPEG) != -1
							|| contentType.indexOf(EmailHelper.CT_IMAGE_JPG) != -1 || contentType.indexOf(EmailHelper.CT_IMAGE_PNG) != -1)
					{
						BodyPart imgPart = new MimeBodyPart();
						ByteArrayDataSource fileds = new ByteArrayDataSource(payload, "application/octet-stream");
						imgPart.setDataHandler(new DataHandler(fileds));
						String cid = multiPartEntry.getHeader("Content-ID");
						imgPart.setFileName(cid);
						imgPart.setHeader("Content-ID", cid);
						mp.addBodyPart(imgPart);
					}
					else
					{						
						String[] filenames = attachments.split(";");
						HashMap<String,String> map = new HashMap<String,String>();
						for(String name:filenames)
						{
							String[] kv = name.split(":");
							if(kv.length == 2)
							{
								map.put(kv[0], kv[1]);
							}
						}
						String cid = multiPartEntry.getHeader("Content-ID");
						String filename = map.get(cid);
						filename = filename == null?cid:filename;
						File file = new File(filename);
						FileOutputStream fileOutStream = new FileOutputStream(file);
						fileOutStream.write(payload);
						fileOutStream.close();
						FileDataSource att = new FileDataSource(file);
						BodyPart attachBodyPart = new MimeBodyPart();
						attachBodyPart.setDataHandler(new DataHandler(att));						
//						attachBodyPart.setFileName("=?GBK?B?" + new sun.misc.BASE64Encoder().encode(filename.getBytes()) + "?=");// 解决附件名中文乱码
						attachBodyPart.setFileName(MimeUtility.encodeText(filename));
						mp.addBodyPart(attachBodyPart);
					}
				}
			}
			msg.setContent(mp);
			sender.send(msg);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
*/
	public static int sendMailAttachMent(String host, String from, String userName, String password, String auth, String to, String subject, String content,
			String attachmentFilename, InputStream in)
	{
		if (host == null || host.length() == 0)
		{
			Assert.notNull(host, "host must not be null");
		}
		if (userName == null || userName.length() == 0)
		{
			Assert.notNull(userName, "userName must not be null");
		}
		if (to == null || to.length() == 0)
		{
			Assert.notNull(to, "Mail to Address must not be null");
		}
		JavaMailSenderImpl sender = new JavaMailSenderImpl();
		sender.setHost(host);
		sender.setUsername(userName);
		sender.setPassword(password);

		Properties pos = new Properties();
		pos.put("mail.smtp.auth", auth);
		pos.put("mail.smtp.timeout", "25000");
		sender.setJavaMailProperties(pos);

		MimeMessage msg = sender.createMimeMessage();
		MimeMessageHelper helper;
		try
		{
			helper = new MimeMessageHelper(msg, true, "utf-8");
			if (to != null && to.length() > 0 && to.indexOf(";") != -1)
			{
				String[] tos = to.split(";");
				helper.setTo(tos);
			}
			else
			{
				helper.setTo(to);
			}
			helper.setFrom(from);
			helper.setSubject(subject);

			Parser parser = new Parser(content);
			parser.setEncoding("utf-8");

			if (content != null && content.startsWith("http://"))
			{
				URL url = new URL(content);
				HtmlparserUrlModifier modifier = new HtmlparserUrlModifier(url);
				parser.visitAllNodesWith(modifier);

				String html = modifier.getModifiedResult();
				helper.setText(html, true);// 第二个参数代表发送的是正文是html
				HashMap<String, String> imgMap = modifier.getImgMap();
				Set<String> set = imgMap.keySet();
				Iterator<?> it = set.iterator();
				while (it.hasNext())
				{
					String key = (String) it.next();
					helper.addInline(key, new UrlResource(imgMap.get(key)));
				}
			}
			else
			{
				helper.setText(content, false);
			}
			helper.addAttachment(attachmentFilename, new InputStreamResource(in));
			sender.send(msg);
		}
		catch (MessagingException e)
		{
			return -1;
		}
		catch (ParserException e)
		{
			return -1;
		}
		catch (MalformedURLException e)
		{
			return -1;
		}
		return 0;
	}

	static class ByteArrayDataSource implements DataSource
	{

		private final String contentType;
		private final byte[] buf;
		private final int len;

		public ByteArrayDataSource(byte[] buf, String contentType)
		{
			this(buf, buf.length, contentType);
		}

		public ByteArrayDataSource(byte[] buf, int length, String contentType)
		{
			this.buf = buf;
			this.len = length;
			this.contentType = contentType;
		}

		public String getContentType()
		{
			if (contentType == null)
				return "application/octet-stream";
			return contentType;
		}

		public InputStream getInputStream()
		{
			return new ByteArrayInputStream(buf, 0, len);
		}

		public String getName()
		{
			return null;
		}

		public OutputStream getOutputStream()
		{
			throw new UnsupportedOperationException();
		}

	}
}
