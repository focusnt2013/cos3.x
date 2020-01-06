<%@page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response); %>
<html>
<head>
<style type='text/css'>
body {overflow:auto; margin-top:0px; margin-left:0px; margin-bottom:0px; margin-right:0px }
<jsp:include page='<%=Kit.getSkinCssPath("skit_frame.css", "../")%>'/>
<jsp:include page='<%=Kit.getSkinCssPath("skit_view.css", "../")%>'/>
<jsp:include page='<%=Kit.getSkinCssPath("skit_table.css", "../")%>'/>
<jsp:include page='<%=Kit.getSkinCssPath("skit_button.css", "../")%>'/>
<jsp:include page='<%=Kit.getSkinCssPath("skit_about.css", "../")%>'/>

</style>
<%=Kit.getJSTag(request, "skit_button.js")%>
</head>
<body>
<TABLE id='win' width='100%' height="100%" cellspacing='0' cellpadding='0' border='0'>
<TR><TD align='center' valign='middle'>

	<table border='0' cellspacing='0' cellpadding='0'>
	<tr><td class='skit_dialog_top_left'/>
		<td class='skit_dialog_top_middle'>
		<!-- interal frame 标题区 -->
		  <table border='0' cellspacing='0' cellpadding='0'>
			<tr><td class='pager_title' id='dlgTitle' >&nbsp;</td></tr>
		  </table>
		<!-- interal frame 标题区 -->
		</td>
		<td class='skit_dialog_top_right'/></tr>
	<tr><td class='skit_dialog_middle_left'/>
		<td class='skit_dialog_middle_middle'>
		  <table cellspacing='0' cellpadding='0'>
			<tr><td class='skit_dialog_label' id='dlg002' style='text-align:center;padding-left:22px;padding-top:10px;padding-bottom:10px;padding-right:10px'>
				<table width='100%' border='0' cellspacing='0' cellpadding='0'>
					<tr><td class='skit_title_logo' style='text-align:center;padding-top:5px'><ww:property value='profile.getString("About")'/></td></tr>
					<tr><td class='skit_text' colspan='2' style='text-align:center'><ww:property value='profile.getString("Copyright")'/></td></tr>
					<tr><td class='skit_table_cell' colspan='2'>&nbsp;</td></tr>
				</table>			
			</td></tr>
			<tr><td align='center'>
				<button type='button' class='skit_btn80' onclick="top.closeView();"><ww:text name="label.ema.button.sure"/></button>
				</td>
			</tr>
		  </table>
		</td>
		<td class='skit_dialog_middle_right'/>
	</tr>
	<tr><td class='skit_dialog_bottom_left'/>
		<td class='skit_dialog_bottom_middle'/>
		<td class='skit_dialog_bottom_right'/>
	</tr>
	</table>

</TD></TR>
</TABLE>
</body>
</html>