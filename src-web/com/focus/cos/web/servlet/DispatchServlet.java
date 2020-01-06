package com.focus.cos.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Deprecated
public class DispatchServlet extends HttpServlet
{
	private static final long serialVersionUID = 6129127676036516030L;

	protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException
	{
		doPost(httpServletRequest,httpServletResponse);
	}
	
	protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException
	{
		String uri = httpServletRequest.getRequestURI();
		uri = uri.substring(uri.lastIndexOf('/')+1);
//		String dispatch = WebWorkConfigLoader.getProperty(uri);
//		if(dispatch != null && !dispatch.isEmpty())
//		{
//			httpServletRequest.getRequestDispatcher(dispatch).forward(httpServletRequest, httpServletResponse);
//		}
	}
}
