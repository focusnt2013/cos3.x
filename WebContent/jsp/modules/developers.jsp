<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/ztree/css/zTreeStyle/zTreeStyle.css" rel="stylesheet"/>
<link type="text/css" href="skit/css/bootstrap-tour.min.css" rel="stylesheet">
<style type='text/css'>
</style>
<%=Kit.getDwrJsTag(request,"interface/UserMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
</head>
<body onResize='resizeWindow()' style='overflow-y:hidden;'>
    <div class="panel panel-default" style='border: 0px solid #aaaaaa' id='divZtree'>
		<div class="panel-body" style='padding: 0px;'>
			<div class="input-group custom-search-form" style='padding:3px;'>
                <input class="form-control" placeholder="输入关键字找用户" id='searchBar' onkeyup='return searchUser(this);' type="text" style='font-size: 12px;height:28px;'>
                <span class="input-group-btn">
                    <button class="btn btn-inverse" type="button" onclick='searchUser()'>
                        <i class="fa fa-search"></i>
                    </button>
                </span>
            </div>
            <div id='divUsers'><ul id='myZtree' class='ztree'></ul></div>
		</div>
	</div>		
</body>
<SCRIPT type="text/javascript">
function resizeWindow()
{
	var div = document.getElementById( 'divUsers' );
	div.style.height = windowHeight - 42;
}
</SCRIPT>

<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<%@ include file="../../skit/inc/skit_ztree.inc"%>
<SCRIPT type="text/javascript">
$( '#divUsers' ).niceScroll({
	cursorcolor: '<%=(String)session.getAttribute("System-Theme")%>',
	railalign: 'right',
	cursorborder: "none",
	horizrailenabled: true, 
	zindex: 2001,
	left: '245px',
	cursoropacitymax: 0.6,
	cursorborderradius: "0px",
	spacebarenabled: false 
});
//########################################################################
	var setting = {
		check: {
			enable: true,
			chkStyle: "checkbox",
			radioType: "level"
		},
		callback: {
			onClick: onSelectDeveloper,
			onCheck: onSelectDeveloper
		},
		view: {
			addDiyDom: addDiyDom
		}
	};
	function addDiyDom(treeId, treeNode) {
		var editStr = "";
		if( treeNode.abort ) treeNode["ico"] = "skit_fa_icon_gray fa-minus-circle";
		if( treeNode.ico )
		{
			editStr = "<i class='fa "+treeNode.ico+"' id='ico_fa_role_" + treeNode.tId + "'></i>";
			var aObj = $("#" + treeNode.tId + "_a");
			if( aObj ) aObj.after(editStr);
		}
	}
	
	function onSelectDeveloper(event, treeId, treeNode)
	{
		myZtree.expandNode(treeNode, true);//, null, null, null, true);
	}

	$(document).ready(function(){
		var json = '<ww:property value="jsonData" escape="false"/>';
		try
		{
			var zNodes = jQuery.parseJSON(json);
			$.fn.zTree.init($("#myZtree"), setting, zNodes);
			myZtree = $.fn.zTree.getZTreeObj("myZtree");
		}
		catch(e)
		{
			alert(e);
		}
	});
	//#########################################################################
</SCRIPT>
<link href="skin/defone/css/simplemodal.css" rel="stylesheet">
<script src="skin/defone/js/mootools-core-1.3.1.js"></script>
<script src="skin/defone/js/simple-modal.js?v=3"></script>
<script src="skit/js/bootstrap-tour.min.js"></script>
<SCRIPT type="text/javascript">
</SCRIPT>
</html>