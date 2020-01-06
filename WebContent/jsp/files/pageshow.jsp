<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<!-- Core CSS - Include with every page -->
<link href="skit/css/bootstrap.min.css" rel="stylesheet">
<!-- Font Awesome Icons -->
<link href="skit/css/font-awesome.min.css" rel="stylesheet">
<!-- /#wrapper -->
<!-- Core Scripts - Include with every page -->
<script src="skit/js/jquery-2.1.4.min.js"></script>
<!-- Bootstrap minimal -->
<script src="skit/js/bootstrap.min.js"></script>
<!-- BlockUI for reloading panels and widgets -->
<script src="skin/defone/js/jquery.blockui.js"></script>
<script src="skin/defone/js/jquery-ui.custom.min.js"></script>
<!-- Page-Level Plugin Scripts - Dashboard -->
<script src="skit/js/jquery.nicescroll.min.js"></script> 
   <!-- Init Scripts - Include with every page -->
<style type='text/css'>
htm {
    bakcground:#fff;
}
body {
    font-size: 12px;
    line-height: 1.42857143;
    color: #333;
    overflow-x:hidden;
    overflow-y:hidden;
    background:#fff;
}
td, th {
    padding: 0;
    font-size: 12px;
}
</style>
</head>
<body style='background:#fff;'>
<form method="post">
<input type='hidden' name='path' value='<ww:property value="path"/>'>
<input type='hidden' name='zip' value='1'/>
<input type='hidden' name='ip' value='<ww:property value="ip"/>'>
<input type='hidden' name='port' value='<ww:property value="port"/>'/>
<input type='hidden' name='length' value='<ww:property value="length"/>'>
<input type='hidden' name='pageBean.pageSize' value='<ww:property value="pageBean.pageSize"/>'>
<input type='hidden' name='pageBean.page' value='0' id='pageBean.page'>
<input type='hidden' name='pageBean.pageCount' value='<ww:property value="pageBean.pageCount"/>'>
<table style='width:100%'>
	<tr><td colspan='2'><iframe id='downloadFrame' name='downloadFrame' style='width:100%;border:0px solid red;' class='nonicescroll'></iframe></td></tr>
	<tr><td align='center'>
		<ul class="pagination" id="pagination" style='margin-top:1px;margin-bottom:1px;'>
             <li><a href="javascript:stepPage(-<ww:property value="pageBean.pageCount"/>)"><i class='fa fa-fast-backward'></i>&nbsp;</a></li>
             <li><a href="javascript:stepPage(-10);"><i class='fa fa-step-backward'></i>&nbsp;</a></li>
		  <ww:bean id="pages" name="com.opensymphony.webwork.util.Counter">   
    			<ww:param name="last" value="pageBean.pageCount" />   
		  </ww:bean>
             <li id='li_start'><a href="javascript:openStep();">...</a></li>
		  <ww:iterator value="#pages" status="loop">
			<ww:if test='pageBean.pageCount>10&&(#loop.index<(pageBean.pageCount-10))'>
            	<li id='li_<ww:property value="#loop.index+1"/>' style='display:none'>
            		<a href="javascript:goPage(<ww:property value="#loop.index+1"/>);"><ww:property value="#loop.index+1"/></a></li>
		  	</ww:if>
		  	<ww:else>
            	<li id='li_<ww:property value="#loop.index+1"/>'>
            		<a href="javascript:goPage(<ww:property value="#loop.index+1"/>);"><ww:property value="#loop.index+1"/></a></li>
		  	</ww:else>
		  </ww:iterator>
            	<li id='li_end'><a href="javascript:openStep();">...</a></li>
             <li><a href="javascript:stepPage(10);"><i class='fa fa-step-forward'></i>&nbsp;</a></li>
             <li><a href="javascript:stepPage(<ww:property value="pageBean.pageCount"/>);"><i class='fa fa-fast-forward'></i>&nbsp;</a></li>
           </ul></td>
           <td width='40' align='right'><select name='previewCharset' onchange='changeCharset()' style='width:38;font-size:8pt;'>
           	<option value=''></option>
           	<option value='UTF-8'>UTF-8</option>
           	<option value='GBK'>GBK</option>
           	<option value='GBK'>GB2312</option>
           </select></td>
	</tr>		
</table>
</form>
<div style='position:absolute;top:28px;left:28px;' id='toolbar'>
   <button type="button" class="btn btn-warning btn-circle" id="btnReload" onclick="top.reloadView();" title='重新刷新文件'>
   	<i class="fa fa-repeat fa-spin"></i></button><br/><br/>
   <button type="button" class="btn btn-info btn-circle" id="btnDownload" onclick="downloadFile();" title='下载该文件'>
   	<i class="fa fa-download"></i></button><br/><br/>
   <button type="button" class="btn btn-success btn-circle" id="btnScrollBottom" onclick="setScrollBottom();" title='滚动到底部'>
   	<i class="fa fa-angle-double-down"></i></button><br/><br/>
   <button type="button" class="btn btn-success btn-circle" id="btnScrollTop" onclick="setScrollTop();" title='滚动到顶部'>
   	<i class="fa fa-angle-double-up"></i></button>
</div>
</body>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<script type="text/javascript">
function resizeWindow()
{
	//发布参数设置面板位置自适应
	var div = document.getElementById( 'downloadFrame' );
	div.style.height = windowHeight - 36;
	
	var toolbar = document.getElementById( 'toolbar' );
	toolbar.style.top = windowHeight - toolbar.clientHeight - 32;
	toolbar.style.left = windowWidth - toolbar.clientWidth - 15;
}
resizeWindow();
function downloadFile()
{
	skit_confirm("您确定获取该文件 "+document.forms[0].name.value+"?", function(yes){
		if( yes )
		{
			document.forms[0].action = "files!download.action";
			document.forms[0].method = "POST";
			document.forms[0].target = "downloadFrame";
			document.forms[0].submit();
		}
	});
}

function setScrollBottom()
{
	var iframe = frames['downloadFrame'];
	iframe.window.document.body.scrollTop = iframe.window.document.body.scrollHeight;
}

function setScrollTop()
{
	var iframe = frames['downloadFrame'];
	iframe.window.document.body.scrollTop = 0+"px";
}
var active_li;
var pageCount = <ww:property value="pageBean.pageCount"/>;
var startPage = <ww:property value="pageBean.pageCount"/> - 9;
if( startPage <= 0 ) startPage = 1;
var endPage = pageCount;
function goPage(page)
{
	var mypage = Number(document.getElementById( 'pageBean.page' ).value);
	if( endPage == pageCount )
	{
		document.getElementById( 'li_end' ).style.display = "none";
	}
	else
	{
		document.getElementById( 'li_end' ).style.display = "";
	}
	if( startPage == 1 )
	{
		document.getElementById( 'li_start' ).style.display = "none";
	}
	else
	{
		document.getElementById( 'li_start' ).style.display = "";
	}
	if( page == pageCount && mypage == pageCount )
	{
		skit_alert("这里已经是最有一页了。");
		return;
	}
	if( page == 1 && mypage == 1)
	{
		skit_alert("这里已经是第一页了。");
		return;
	}
	var li = document.getElementById( 'li_'+page );
	if( active_li ) active_li.className = '';
	active_li = li;
	active_li.className = 'active';
	document.getElementById( 'pageBean.page' ).value = page;
	document.forms[0].action = "files!pageshow.action";
	document.forms[0].method = "POST";
	document.forms[0].target = "downloadFrame";
	//if( top && top.skit_showLoading ) top.skit_showLoading(3000);
	document.forms[0].submit();
}

function changeCharset()
{
	document.forms[0].action = "files!pageshow.action";
	document.forms[0].method = "POST";
	document.forms[0].target = "downloadFrame";
	//if( top && top.skit_showLoading ) top.skit_showLoading(1000);
	document.forms[0].submit();
}

function stepPage(page)
{
//alert(startPage+","+endPage);
	var mypage = Number(document.getElementById( 'pageBean.page' ).value);
	var step = mypage + page;
	if( step > pageCount )
	{
		step = pageCount;
	}
	if( step <= 0 )	step = 1;
	if( step == pageCount && mypage == pageCount )
	{
		skit_alert("这里已经是最有一页了。");
		return;
	}
	for( var i = startPage; i <= endPage; i++ )
		document.getElementById( 'li_'+i ).style.display = "none";
	startPage = step - 9;
	if( startPage <= 0 ) startPage = 1;
	endPage = step; 
	if( endPage == 1 )
		if( pageCount > 10 ) endPage = 10;
		else endPage = pageCount;
	for( var i = startPage; i <= endPage; i++ )
		document.getElementById( 'li_'+i ).style.display = "";
	goPage(step);
}
function openStep()
{
}
goPage(<ww:property value='pageBean.pageCount'/>);
</script>
</html>