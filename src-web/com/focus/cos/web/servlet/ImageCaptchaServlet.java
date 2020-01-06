package com.focus.cos.web.servlet;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;

import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.focus.cos.web.login.service.CaptchaServiceSingleton;
import com.octo.captcha.service.CaptchaServiceException;
import com.octo.captcha.service.image.ImageCaptchaService;

/**
 * Description:登录页面加入图形验证码，此类生成图形验证码
 * Create Date:Oct 18, 2008
 * @author Nixin
 *
 * @since 1.0
 */
public class ImageCaptchaServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	
	private static final Log log = LogFactory.getLog(ImageCaptchaServlet.class);

	public void init(ServletConfig servletConfig) throws ServletException
	{
		super.init(servletConfig);
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException,
			IOException
	{
		
		try
		{
			Object timestamp = request.getSession().getAttribute("Timestamp-ImageCaptcha");
			if( timestamp != null )
			{
				long ts = (Long)timestamp;
				ts = System.currentTimeMillis() - ts;
				if( ts < 1000 )
				{
					StringBuffer sb = new StringBuffer();
			        sb.append("Found the request of submit repeated\r\n"+ "http://" + request.getHeader("Host") + request.getContextPath()+request.getServletPath());
			        String ip = request.getRemoteAddr();
			        String encode = System.getProperty("service.config.encode");//获取服务配置编码
			        sb.append("\r\n\tip=" + ip);
			        sb.append("\r\n\tencode=" + encode);
			        Enumeration<String> e1 = request.getHeaderNames();
			        while (e1.hasMoreElements()) {
			            String key = e1.nextElement();
			            sb.append("\r\n\t");
			            sb.append(key);
			            sb.append("=");
			            sb.append(request.getHeader(key) + "\t");
			        }
			        e1 = request.getParameterNames();
			        sb.append("\r\n\t====================");
			        while (e1.hasMoreElements()) {
			            String key = e1.nextElement();
			            sb.append("\r\n\t");
			            sb.append(key);
			            sb.append("=");
			            sb.append(request.getParameter(key) + "\t");
			        }
			        String ip_ = request.getHeader("x-forwarded-for");
			        ip_ = ip_ == null ? request.getHeader("x-real-ip") : ip_;
			        if (ip_ != null) {
			            ip = ip_;
			        }
			        sb.append("\r\n\tip=" + ip);
			        log.debug(sb.toString());
					return;
				}
			}
	        request.getSession().setAttribute("Timestamp-ImageCaptcha", System.currentTimeMillis());        
			//验证码储存在当前session中
			String captchaId = request.getSession().getId();
			ImageCaptchaService ics = CaptchaServiceSingleton.getInstance();
			//生成图片验证码
			BufferedImage challenge = ics.getImageChallengeForID(captchaId,request.getLocale());
			log.debug("ImageCaptchaServlet(captchaId="+captchaId+",).");
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        ImageIO.write(challenge, "JPEG", baos);
			//图片字节流写到页面
			response.setHeader("Cache-Control", "no-store");
			response.setHeader("Pragma", "no-cache");
			response.setDateHeader("Expires", 0);
			response.setContentType("image/jpeg");
			ServletOutputStream responseOutputStream = response.getOutputStream();
			responseOutputStream.write(baos.toByteArray());
			responseOutputStream.flush();
			responseOutputStream.close();
		}
		catch (IllegalArgumentException e)
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		catch (CaptchaServiceException e)
		{
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
	}
}
