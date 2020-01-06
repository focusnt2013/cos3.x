package com.focus.cos.web.common.filter;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Description:漏洞类型： URL重定向 弱点描述： Struts2在2.3.15.1之前版本对redirect或redirectAction后的参数过滤不严，存在多个开放重定向漏洞，攻击者通过构建特制的URL并诱使用户点击，利用这些漏洞将用户重定向到攻击者控制的
 * Create Date:Dec 13, 2012
 * @author Focus
 *
 * @since 1.0
 */
public class RedirectFilter implements Filter
{
	private static final Log log = LogFactory.getLog(RedirectFilter.class);

	public void init(FilterConfig filterConfig) throws ServletException
	{
	}
	
	public void destroy()
	{
	}
	/**
	 * 
	 */
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
			FilterChain filterChain) throws IOException, ServletException
	{
		HttpServletRequest request = (HttpServletRequest)servletRequest;
		HttpServletResponse response = (HttpServletResponse)servletResponse;
		Enumeration<String> e1 = request.getParameterNames();
//        StringBuffer test = new StringBuffer();
        while (e1.hasMoreElements()) {
            String key = e1.nextElement();
            if( key != null && key.startsWith("redirect") ){
            	log.warn("Filter "+key);
//				response.sendRedirect(Kit.URL_PATH(request)+ "403");
            	response.setStatus(403);
            	ServletOutputStream out = null;
            	try{
            		out = response.getOutputStream();
					out.write("[403] Forbind to redirect.".getBytes("UTF-8"));
            	}
        		catch (Exception e)
        		{
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
                }
            	return;
            }
//            test.append("\r\n\t");
//            test.append(key);
//            test.append(" = ");
//            test.append(request.getParameter(key));
        }
//        test.insert(0, request.getRequestURI());
//        System.err.println(test.toString());
		filterChain.doFilter(servletRequest, servletResponse);
	}
}
