<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response); %>
<html>
<head>
<!--JQUERY UI files-->
<link href="<%=Kit.URL_PATH(request)%>skit/css/bootstrap.min.css" rel="stylesheet"/>
<link href="<%=Kit.URL_PATH(request)%>skit/css/fileinput.min.css" rel="stylesheet"/>
<link href="<%=Kit.URL_PATH(request)%>skit/css/font-awesome.min.css" rel="stylesheet"/>
<link href="<%=Kit.URL_PATH(request)%>skit/css/jquery-ui.css" rel="stylesheet"/>
<!--PQ Grid files-->
<link rel="stylesheet" href="<%=Kit.URL_PATH(request)%>skit/grid/pqgrid.min.css?v=2.0.4" />
<link rel="stylesheet" href="<%=Kit.URL_PATH(request)%>skit/grid/pqselect.min.css" />
<!--PQ Grid Office theme-->
<link rel="stylesheet" href="<%=Kit.URL_PATH(request)%>skit/grid/themes/office/pqgrid.css?v=2" />
<!--PQ Grid Skit define-->
<link href="<%=Kit.URL_PATH(request)%>skin/defone/css/simplemodal.css" rel="stylesheet"/>
<script src="<%=Kit.URL_PATH(request)%>skit/grid/jquery.min.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/grid/jquery-ui.min.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/grid/pqgrid.min.js?v=2.0.4"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/grid/pqselect.min.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/grid/pq-localize-zh.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/js/fileinput.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/js/fileinput_locale_zh.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skin/defone/js/jquery.nicescroll.min.js"></script> 
<script src="<%=Kit.URL_PATH(request)%>skin/defone/js/mootools-core-1.3.1.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skin/defone/js/simple-modal.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/js/jsrsasign-rsa-min.js"></script>
<%=Kit.getDwrJsTag(request,"interface/DiggMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
<%@ include file="pqgrid-skit.inc"%>
<style type='text/css'>
<ww:property value='pq_cellcls' escape='false'/>
</style>
<SCRIPT TYPE="text/javascript" id='beforeGridView'>
function callbackBeforeGridView(){
	try{
	    <ww:property value="beforeGridView" escape="false"/>
	}
	catch(e){
   		if( top && top.skit_alert) top.skit_alert("执行表格初始化回调出现异常"+e.message+", 行数"+e.lineNumber);
   		else alert("执行表格初始化回调出现异常"+e.message+", 行数"+e.lineNumber);
	}
}
</SCRIPT>
</head>
<body>
<div id="grid_query" style="margin-top:0px auto;"></div>
<form target='downloadframe'>
<input type='hidden' name='id' id='id' value="<ww:property value='id'/>">
<input type='hidden' name='gridxml' id='gridxml' value="<ww:property value='gridxml'/>">
<input type='hidden' name='snapshot' id='snapshot'>
<input type='hidden' name='pq_filter' id='pq_filter'>
</form>
<iframe name="downloadframe" id="downloadframe" style="display:none;border:1px solid red"></iframe>
<div class="panel panel-primary" id='divUploadfile' style='z-index: 9527; top: 100; left: 100; width:600px; display:none; position: absolute; '>
    <div class="panel-heading">
    	<span id='spanUploadtitle'>从本地磁盘选择上传文件</span>
        <div class="panel-menu">
            <button type="button" onclick='closeuploadfile()' data-action="close" class="btn btn-warning btn-action btn-xs"><i class="fa fa-times"></i></button>
        </div>
    </div>
    <div class="panel-body" style="display: block;">
		<input type="file" name="uploadfile" id="uploadfile" class="file-loading"/>
    </div>
</div>
<input type='hidden' id='uploadfilename'>
<ww:iterator value="toolbars" status="loop">
<ww:if test='popup'>
<div id="popup-dialog-crud-<ww:property value='id'/>" style="display:none;" style='padding-bottom:20px;'>
<form id="crud-form-<ww:property value='id'/>">
<!-- 遍历所有弹出对话框属性，生成对话框内界面 -->
<ww:iterator value="popups" status="loop">
	<div class="form-group" style='margin-top:15px;margin-bottom:0px;'>
		<div class="input-group">
			<span class="input-group-addon input-group-addon-font"><ww:property value='label'/><span class='fa fa-info' style='margin-left:6px'></span></span>
			<input class="form-control form-control-font" id="<ww:property value='name'/>" type="text" name="<ww:property value='name'/>"
           	data-title="<ww:property value="placeholder"/>"
			data-toggle="tooltip" 
			data-placement="bottom"
			data-trigger='manual'
			onkeydown='$("#<ww:property value='name'/>").tooltip("hide")'
          	placeholder="<ww:property value="placeholder"/>"
			>
		</div>
	</div>
</ww:iterator>
</form>
</div>
</ww:if>
</ww:iterator>
<div id='divDialogMask' onclick='closeDialogMask(this)' class='skit_mask' style='cursor:pointer;' title='点击关闭弹窗'></div>
</body>
<SCRIPT TYPE="text/javascript">
var grid_type = "grid_query";
<%@ include file="inc/init.inc"%>
<%@ include file="inc/common.inc"%>
<%@ include file="inc/toolbar.inc"%>
<%@ include file="inc/details.inc"%>
$(function () {
	try{
		<%@ include file="inc/query.inc"%>
		<%@ include file="inc/filter.inc"%>
	}
	catch(e)
	{
		if( top && top.skit_alert) top.skit_alert("初始化表格视图出现异常"+e.message+", 行数"+e.lineNumber);
		else alert("初始化表格视图出现异常"+e.message+", 行数"+e.lineNumber);
	}
});
function callbackBeforeEdit(dataIndx, rowData)
{
	return true;
}
function showDetail(rowData, detailId)
{
	return true;
}
</SCRIPT>
<SCRIPT TYPE="text/javascript">
window.onresize = function(){
	resizeWindow();
}
document.onkeydown = function(event) { 
	//alert(event);
	var e = event ? event :(window.event ? window.event : null); 
	if(e.keyCode==122){ 
		resizeWindow();
	}
}
var abortResize = false;
function resizeWindow()
{
	if( abortResize )
	{
		return;
	}
	w = window.innerWidth || document.documentElement.clientWidth || window.document.body.clientWidth; 
	h = window.innerHeight || document.documentElement.clientHeight || window.document.body.clientHeight;
	var h1 = 2;
	h -= h1;
	w -= 2;
	$("#grid_query").pqGrid( "option", "flexWidth", false );
	$("#grid_query").pqGrid( "option", "flexHeight", false );
	$("#grid_query").pqGrid( "option", "filterModel", { on: <ww:property value="filterModel.on"/>, mode: "AND", header: <ww:property value="filterModel.on"/> } );
	$("#grid_query").pqGrid( "option", "width", w );
	$("#grid_query").pqGrid( "option", "height", h );
	$("#grid_query").pqGrid("refresh");
}
</SCRIPT>
<%@ include file="inc/globalscript.inc"%>
<SCRIPT TYPE="text/javascript" id='globalscript'>
<ww:property value='javascript' escape='false'/>
</SCRIPT>
</html>