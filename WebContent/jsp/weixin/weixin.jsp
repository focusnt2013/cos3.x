<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<meta http-equiv="Pragma" content="no-cache" /> 
<meta http-equiv="Cache-Control" content="no-cache" /> 
<meta http-equiv="Expires" content="0" /> 
<style type='text/css'>
<jsp:include page='<%=Kit.getSkinCssPath("skit_view.css", "../../")%>'/>
<jsp:include page='<%=Kit.getSkinCssPath("skit_table.css", "../../")%>'/>
<jsp:include page='<%=Kit.getSkinCssPath("skit_button.css", "../../")%>'/>
</style>
<script type="text/javascript">
function doSave()
{
	var ip = document.getElementById("dbcfg.ip").value;
	var port = document.getElementById("dbcfg.port").value;
	var username = document.getElementById("dbcfg.username").value;
	var password = document.getElementById("dbcfg.password").value;
	var database = document.getElementById("dbcfg.database").value;
	if(ip=="")
	{
		skit_alert("请输入连接芒果数据库的IP地址");
		return;
	}
	if(port=="")
	{
		skit_alert("请输入连接芒果数据库的端口");
		return;
	}
	if(username=="")
	{
		skit_alert("请输入数据库连接用户名");
		return;
	}
	if(password=="")
	{
		skit_alert("请输入数据库连接鉴权密码");
		return;
	}
	if(database=="")
	{
		skit_alert("请输入数据库名称");
		return;
	}
	document.forms[0].action="weixin!setdatabase.action";
	document.forms[0].submit();
}
</script>
</head>
<body>
<form>
<table width="100%" border="0">
  <tr><td height='3'></td></tr>
  <tr>
	<td valign='top'>
		<table width="100%" border="0" cellpadding="0" cellspacing="1" class="skit_table_frame">
		  <tr><td>
			  <table width="100%" border="0" cellpadding="0" cellspacing="0">
				<tr><td class="skit_table_head" colspan='2'><ww:text name="微信公众号系统数据库配置管理"/></td></tr>
				<tr>
					<td class="skit_table_cell" width='200' style='color:#4F4F4F'>数据库类型</td>
					<td class="skit_table_cell">mongodb(目前只支持芒果数据库)[<a href='javascript:openView("芒果数据库(NoSQL对象数据库)百度百科", "http://baike.baidu.com/link?url=hdgLfdp2SDlom4WuFZ5v1klHZ8dwBX0XVbJxG7zqzcTnV2PKU8AtRYHKxFsK7PHhZbrQ5INc__l2C4Ah79G8n_")'>介绍</a>]</td>
				</tr>
				<tr>
					<td class="skit_table_cell" style='color:#4F4F4F'>数据库连接IP</td>
					<td class="skit_table_cell">
						<input type='text' class='skit_input_text' placeholder='输入连接芒果数据库的IP地址' 
							   id='dbcfg.ip' name='dbcfg.ip' style='width:160px'
							   value='<ww:property value="dbcfg.ip"/>'></td>
				</tr>
				<tr>
					<td class="skit_table_cell" style='color:#4F4F4F'>数据库连接端口</td>
					<td class="skit_table_cell">
						<input type='text' class='skit_input_text' placeholder='输入连接芒果数据库的端口' 
							   id='dbcfg.port' name='dbcfg.port' style='width:80px'
							   value='<ww:property value="dbcfg.port"/>'></td>
				</tr>
				<tr>
					<td class="skit_table_cell" style='color:#4F4F4F'>数据库连接用户名</td>
					<td class="skit_table_cell">
						<input type='text' class='skit_input_text' placeholder='输入数据库连接用户名' 
							   id='dbcfg.username' name='dbcfg.username' style='width:160px'
							   value='<ww:property value="dbcfg.username"/>'></td>
				</tr>
				<tr>
					<td class="skit_table_cell" style='color:#4F4F4F'>数据库连接鉴权密码</td>
					<td class="skit_table_cell">
						<input type='password' class='skit_input_text' placeholder='输入数据库连接鉴权密码' 
							   id='dbcfg.password' name='dbcfg.password' style='width:160px'
							   value='<ww:property value="dbcfg.password"/>'></td>
				</tr>
				<tr>
					<td class="skit_table_cell" style='color:#4F4F4F'>数据库名称</td>
					<td class="skit_table_cell">
						<input type='text' class='skit_input_text' placeholder='输入数据库名称' 
							   id='dbcfg.database' name='dbcfg.database' style='width:160px'
							   value='<ww:property value="dbcfg.database"/>'></td>
				</tr>
			  </table>
		  </td></tr>
		  <tr><td align='center'><button type='button' class='skit_btn80' onclick='doSave();'>保存</button></td></tr>
		</table>
	</td>
  </tr>
</table>
</form>
</body>
<%@ include file="../../skit/inc/skit_cos.inc"%>
</html>