<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/css/cosinput.css" rel="stylesheet">
<style type='text/css'>
</style>
<%=Kit.getDwrJsTag(request,"interface/SftCfgMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
<script type="text/javascript">
var focusValue = "";
function onModifyFocus()
{
	var args = onModifyFocus.arguments;
	focusValue = args[0].value;
}

function onModifyKeyup()
{
	var args = onModifyKeyup.arguments;
	var e = event.srcElement;
    var k = event.keyCode;
    if( k == 27 )
    {
    	args[0].parentNode.focus();
    }
}

function onModify()
{
	var args = onModify.arguments;
    if(args[0].value != focusValue)
    {
    	SftCfgMgr.modifyProperty(args[0].name, args[0].value, args[0].title,
		{
			callback:function(ret) {
				skit_alert(ret);
			},
			timeout:30000,
			errorHandler:function(message) { skit_alert("<ww:text name='label.ema.config.exception'/>"+message+"<ww:text name='label.ema.config.exception.end'/>"); }
		});
		focusValue = "";
	}
}	
</script>
</head>
<body>
<div class="panel panel-default" style='margin:10px'>
	<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='fa fa-check-square-o'></i> <ww:text name="label.ema.sersoft.param"/></div>
 	<div class="panel-body" style='padding: 0px;'>
	  <table>
		<tr>
			<td class="skit_table_cell" width='200'>软件名称</td>
			<td class="skit_table_cell">
				<input type='text' title='软件名称'
					   class='skit_input_text' autocomplete='off' 
					   name='SoftwareName'
				       value='<ww:property value="profile.getString('SoftwareName')"/>'
					   onfocus="return onModifyFocus(this);"
					   onblur='return onModify(this);'
					   onkeyup="return onModifyKeyup(this);">
			</td>
		</tr>
		<tr>
			<td class="skit_table_cell">软件版本</td>
			<td class="skit_table_cell">
				<input type='text' title='软件版本'
					   class='skit_input_text' autocomplete='off' 
					   name='SoftwareVersion'
				       value='<ww:property value="profile.getString('SoftwareVersion')"/>'
					   onfocus="return onModifyFocus(this);"
					   onblur='return onModify(this);'
					   onkeyup="return onModifyKeyup(this);">
			</td>
		</tr>
		<tr>
			<td class="skit_table_cell">开发商名称</td>
			<td class="skit_table_cell">
				<input type='text' title='开发商名称'
					   class='skit_input_text' autocomplete='off' 
					   name='SoftwareVendor'
				       value='<ww:property value="profile.getString('SoftwareVendor')"/>'
					   onfocus="return onModifyFocus(this);"
					   onblur='return onModify(this);'
					   onkeyup="return onModifyKeyup(this);">
			</td>
		</tr>
		<tr>
			<td class="skit_table_cell">关于</td>
			<td class="skit_table_cell">
				<input type='text' title='关于'
					   class='skit_input_text' autocomplete='off' 
					   name='About'
				       value='<ww:property value="profile.getString('About')"/>'
					   onfocus="return onModifyFocus(this);"
					   onblur='return onModify(this);'
					   onkeyup="return onModifyKeyup(this);">
			</td>
		</tr>
		<tr>
			<td class="skit_table_cell">软件版权描述</td>
			<td class="skit_table_cell">
				<input type='text' title='软件版权描述'
					   class='skit_input_text' autocomplete='off' 
					   name='Copyright'
				       value='<ww:property value="profile.getString('Copyright')"/>'
					   onfocus="return onModifyFocus(this);"
					   onblur='return onModify(this);'
					   onkeyup="return onModifyKeyup(this);">
			</td>
		</tr>
	  </table>
	</div>
</div>
</body>
<%@ include file="../../skit/inc/skit_cos.inc"%>
</html>