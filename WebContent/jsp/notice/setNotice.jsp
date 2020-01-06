<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<style type='text/css'>
<jsp:include page='<%=Kit.getSkinCssPath("skit_view.css", "../../")%>'/>
<jsp:include page='<%=Kit.getSkinCssPath("skit_table.css", "../../")%>'/>
<jsp:include page='<%=Kit.getSkinCssPath("skit_button.css", "../../")%>'/>
</style>
<%=Kit.getJSTag(request, "global.js")%>
<%=Kit.getJSTag(request, "skit_table.js")%>
<%=Kit.getDwrJsTag(request,"interface/UserMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>

<script type="text/javascript">
	//校验并提交新增		 
     function doAdd()
     {
	    //用户名
	    var objTitle = document.getElementById("notice.title");
	    objTitle.value = objTitle.value.trim();
	    if(objTitle.value.trim()=="")
	    {
		   skit_alert("请输入标题！");
		   objTitle.focus();
		   return false;
	    }
	    //用户名
	    var objContent = document.getElementById("notice.content");
	    objContent.value = objContent.value.trim();
	    if(objContent.value.trim()=="")
	    {
		   skit_alert("请输入内容！");
		   objContent.focus();
		   return false;
	    }
	    
	    document.forms[0].action="notice!doSetNotice.action";
	    document.forms[0].submit();
     }		

     
     //取消
     function doCancel()
     {
     	document.forms[0].action="notice!doQuery.action";
     	document.forms[0].submit();
     }
</script>
</head>
<body onResize='resizeWindow()'>
<form name="form1" method="post" action="notice!doAdd.action" enctype="multipart/form-data">
<input type="hidden" id="notice.id" name="notice.id" value="<ww:property value="notice.id"/>">
<TABLE width='100%' border='0' cellspacing='0' cellpadding='0' id='tableFeview'>
  <TR style='height:3px;'><TD/></TR>
  <TR><TD>
	<table width='100%' cellspacing='1' cellpadding='0' class='skit_table_frame' id='tbFrame1'>
		<tr><td class="skit_table_head">公告信息</td></tr>
		<tr><td>
			<table width='100%' border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td class='skit_table_cell' width="60">标题</td>
				<td class='skit_table_cell'><input tye='text' name="notice.title" maxlength="32" class='skit_input_text' value="<ww:property value="notice.title"/>"></td>
			</tr>
			<tr>
				<td class='skit_table_cell'>内容</td>
				<td class='skit_table_cell'><textarea id='notice.content' name="notice.content" rows=6 class='skit_input_textarea'><ww:property value="notice.content"/></textarea></td>
			</tr>
			<tr>
				<td class='skit_table_cell'>附件</td>
				<td class='skit_table_cell'><input type='file' name="attachmentFile" id="attachmentFile" class='skit_input_text' style="width:50%" onKeyPress="return false;" onPaste="return false;"></td>
			</tr>
			</table>
		</td>
		</tr>
		<tr><td style='height:32px' align='center' valign='middle'>
			<button type='button' class='skit_btn60' onClick="doAdd();"><i class='skit_fa_btn fa fa-save'></i>保存</button>&nbsp;
			<button type='button' class='skit_btn60' onClick="doCancel();"><i class='skit_fa_btn fa fa-sign-out'></i>取消</button>
		</td></tr>
	</table>
  </TD></TR>
</TABLE>
</form>
</body>
<SCRIPT>
/*实现窗口对齐*/
function resizeWindow()
{
	var feview = window.document.body;
	var tableFeview = document.getElementById( "tableFeview" );
	var tbFrame1 = document.getElementById( "tbFrame1" ); 
	
	var div;
	var h = 0;
	var h1 = 0;
	var h2 = 0;
	
	h += tableFeview.rows[0].clientHeight;
	h += 2;
	h = feview.clientHeight - h;

}
resizeWindow();
var _curPropbar = document.getElementById('div_public_contacts');
function changeContactsType( div )
{
	div.className = 'propbutton';
	if( _curPropbar != null && div.uniqueID != _curPropbar.uniqueID )
	{
		_curPropbar.className = 'propbutton_unchk';
	}
	_curPropbar = div;
}
</SCRIPT>
<%@ include file="../../skit/inc/skit_cos.inc"%>
</html>