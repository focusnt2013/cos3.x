<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<style type='text/css'>
<jsp:include page='<%=Kit.getSkinCssPath("skit_frame.css", "../../")%>'/>
<jsp:include page='<%=Kit.getSkinCssPath("skit_view.css", "../../")%>'/>
<jsp:include page='<%=Kit.getSkinCssPath("skit_table.css", "../../")%>'/>
<jsp:include page='<%=Kit.getSkinCssPath("skit_button.css", "../../")%>'/>
</style>
<%=Kit.getJSTag(request, "skit_table.js")%>

<script type="text/javascript">
	function keyWordFocus(obj)
	{
		if(obj.value=='请输入关键字')obj.value='';
	}
	function keyWordBlur(obj)
	{
		if(obj.value=='')obj.value='请输入关键字';
	}
	
	function preAdd()
	{
		document.forms[0].action="notice!preSet.action";
		document.forms[0].submit();
	}
	
	//查询
	function doQuery()
	{
		if(document.getElementById('queryMeta.keyword').value=="请输入关键字")
		{
			document.getElementById('queryMeta.keyword').value = "";
		}
    	document.forms[0].submit();
	}
	
	function preview(id)
	{
		var content = document.getElementById("content"+id).value;
		skit_message(content, "预览公告");
	}
	
	function edit(id)
	{
		document.forms[0].action="notice!preSet.action?notice.id="+id;
		document.forms[0].submit();
	}
	
	//下载
	function download(id)
	{
		document.forms[0].action="notice!downloadNotice.action?notice.id="+id;
		document.forms[0].submit();
	}
	
	//删除
	function doDelete()
    {
        var ids = getSelectedIds("ids");
        if(ids=="")
        {
            alert("至少选择一条记录删除！");
            return;
        }
        if(confirm("确定要删除所选择记录？"))
        {
            document.forms[0].action="notice!doDelete.action";
            document.forms[0].submit();
        }
    }
    
    //发布
    function doRelease()
    {
    	var ids = getSelectedIds("ids");
        if(ids=="")
        {
            alert("至少选择一条记录！");
            return;
        }
        if(confirm("确定要发布所选择公告？"))
        {
            document.forms[0].action="notice!doRelease.action";
            document.forms[0].submit();
        }
    }
</script>
</head>
<body onResize='resizeWindow()'>
<form action="notice!doQuery.action" method="post" name="form1" id="form1">
<table width="100%" border="0" cellspacing="0" cellpadding="3" id='tableFeview'>
  <tr><td>
	<table width='100%' cellspacing="0" cellpadding="0" border='0' class='skit_table_frame'>
		<tr>
		  <td class="skit_table_navig" align="right">
			<table cellpadding="0" cellspacing="0" border="0"><tr>
				<td style='padding-right:5px;width:220'><input type="text" id="queryMeta.keyword" name="queryMeta.keyword" class="skit_search_input" value='<ww:property value="queryMeta.keyword"/>' onfocus="keyWordFocus(this);" onblur="keyWordBlur(this);"></td>
				<td><button type='button' class='skit_input_submit' onClick="doQuery();"><i class='skit_fa_btn fa fa-search'></i>查询</button></td>
				</tr>
			</table>
		</tr>
	</table>
  </td></tr>

  <tr>
	<td valign='top'>
        <table width='100%' border="0" cellpadding="0" cellspacing="1" class="skit_table_frame">
			<tr><td valign='top'>
				<table width='100%' border="0" cellpadding=0 cellspacing=0 onClick='selectRow(this)'>
					<tr> 
						<td class="skit_table_head" width='22' align='center'><input type="checkbox" onclick="selectAll('ids',this.checked)"/></td>
						<td class="skit_table_head" width='120'>时间</td>
						<td class="skit_table_head">标题</td>
						<td class="skit_table_head" width='50'>状态</td>
						<td class="skit_table_head" width='36'>编辑</td>
						<td class="skit_table_head" width='36'>预览</td>
						<td class="skit_table_head" width='36'>附件</td>
					</tr>
					<ww:iterator value="listData" status="loop">				
					<tr class="<ww:property value="%{(#loop.index)%2!=0?'td_one_left':'td_two_left'}"/>"> 
						<td class="skit_table_cell" align='center'><input type="checkbox" name="ids" value="<ww:property value="%{id}"/>"/></td>
						<td class="skit_table_cell"><ww:date name="%{addTime}" format="yyyy-MM-dd HH:mm:ss"/> </td>
						<td class="skit_table_cell"><ww:property value="title"/></td>
						<td class="skit_table_cell"><ww:if test="state==0">待发布</ww:if>
													<ww:elseif test="state==1">已发布</ww:elseif></td>
						<td class="skit_table_cell"><a href='javascript:edit(<ww:property value="id"/>);'>编辑</a></td>
						<td class="skit_table_cell"><a href='javascript:preview(<ww:property value="id"/>);'>预览</a></td>
						<td class="skit_table_cell">
							<ww:if test="attachUri==null || attachUri.equals(\"\")">&nbsp;</ww:if><ww:else>
						<a href='javascript:download(<ww:property value="id"/>);'>下载</a></ww:else></td>
						<textarea id='content<ww:property value="id"/>' style='display:none'><ww:property value="content"/></textarea>
					</tr>
					</ww:iterator>
					
					<%-- 循环输出空行 --%>
					<ww:bean id="counter2" name="com.opensymphony.webwork.util.Counter">   
		     			<ww:param name="last" value="(pageBean.getPageSize()>listData.size)?(pageBean.getPageSize()-listData.size):0" />   
					</ww:bean>
					<ww:iterator value="#counter2" status="loop">
						<tr class="<ww:property value="%{(listData.size+#loop.index)%2!=0?'td_one_left':'td_two_left'}"/>">  
							<td class="skit_table_cell">&nbsp;</td>
							<td class="skit_table_cell">&nbsp;</td>
							<td class="skit_table_cell">&nbsp;</td>
							<td class="skit_table_cell">&nbsp;</td>
							<td class="skit_table_cell">&nbsp;</td>
							<td class="skit_table_cell">&nbsp;</td>
							<td class="skit_table_cell">&nbsp;</td>
						</tr>
					</ww:iterator>
					
				</table>
				<table width='100%' border="0" cellpadding=0 cellspacing=0>
				<tr>
					<td style="background-color:#A9BAA5;">
						<button type='button' class='skit_btn60' onclick='preAdd();'><img src='<%=Kit.URL_IMAGEPATH(request)%>icons/useradd.gif' ALIGN="ABSMIDDLE">新增</button>&nbsp;
						<button type='button' class='skit_btn60' onclick='doRelease();'><i class='skit_fa_btn fa fa-download'></i>发布</button>&nbsp;
						<button type='button' class='skit_btn60' onclick='doDelete();'><i class='skit_fa_btn fa fa-remove'></i>删除</button>&nbsp;
					</td>
					<td class='skit_table_page'><ww:property value="%{pageMenu}" escape="false"/></td>
				</tr>
				</table>
			</td>
			</tr>
		</table>
	</td>
  </tr>  
</table>
</form>
</body>

<SCRIPT LANGUAGE="JavaScript">
<!--
/*实现窗口对齐*/
function resizeWindow()
{
}
resizeWindow();
-->
</SCRIPT>
<%@ include file="../../skit/inc/skit_cos.inc"%>
</html>