<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%Kit.noCache(request,response);%>
<html>
<body>
</body>
<ww:if test="responseMessage!=null">
<SCRIPT type="text/javascript">
if(window.top && window.top.skit_alert )
{
	window.top.skit_alert('<ww:property value="%{responseMessage}"/>', "后台提示", function(){
		window.top.closeReloadView();
	});
}
</SCRIPT>
</ww:if>
<ww:if test="responseException!=null">
<SCRIPT type="text/javascript">
if(window.top && window.top.skit_alert )
{
	window.top.skit_alert('<ww:property value="%{responseException}"/>', "异常提示", function(){
		window.top.closeReloadView();
	});
}
</SCRIPT>
</ww:if>
</html>