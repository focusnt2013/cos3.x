<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%Kit.noCache(request,response);%>
<html>
<ww:if test="responseConfirm!=null">
<SCRIPT TYPE="text/javascript">
if( top && top.skit_alert )
top.skit_confirm('<ww:property value="responseMessage"/>', function(yes){
	if( yes )
	{
		top.openView('<ww:property value="viewTitle"/>', '<ww:property value="responseConfirm"/>');
	}
});
</SCRIPT>
</ww:if>
<ww:elseif test="responseMessage!=null">
<SCRIPT type="text/javascript">
if(window.top && window.top.skit_alert )
{
	window.top.skit_alert('<ww:property value="%{responseMessage}"/>', "后台提示", function(){
	});
}
</SCRIPT>
</ww:elseif>
<ww:if test="responseException!=null">
<SCRIPT type="text/javascript">
if(window.top && window.top.skit_alert )
{
	window.top.skit_alert('<ww:property value="%{responseException}"/>', "异常提示", function(){
	});
}
</SCRIPT>
</ww:if>
<SCRIPT type="text/javascript">
var redirectUrl = "<ww:property value='responseRedirect'/>";
if( redirectUrl )
{
	window.location.href = redirectUrl;
}
</SCRIPT>
</html>