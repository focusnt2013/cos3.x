<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<div id="menuUser" class="menuContent" style="display:none; position: absolute; z-index: 3;">
	<ul id="myZtreeUser" class="ztree" style="margin-top:0; border: 1px solid #617775; background: #eee; color: #fff; width: 220px; height: 360px;"></ul>
</div>
<SCRIPT type="text/javascript">
var settingUser = {
	view: {
		dblClickExpand: false
	},
	check: {
		enable: true,
		chkStyle: "radio",
		radioType: "level"
	},
	callback: {
		onClick: onSelectUser,
		onCheck: onSelectUser
	}
};
/*
function onSelectUser(e, treeId, treeNode) {
	hideSelector();
}
*/

function showSelector(id, width) 
{
	var json = '<ww:property value="localData" escape="false"/>';
	$.fn.zTree.init($("#myZtreeUser"), settingUser, jQuery.parseJSON(json));
	var div = $("#"+id);
	var offset = $("#"+id).offset();
	if( width ){}else{
		width = 300;
	}
	document.getElementById("myZtreeUser").style.width = width;
	$("#menuUser").css({left:offset.left + "px", top:offset.top + div.outerHeight() + "px"}).slideDown("fast");
	$("body").bind("mousedown", onBodyDown);
}

function hideSelector() {
	$("#menuUser").fadeOut("fast");
	$("body").unbind("mousedown", onBodyDown);
	$.fn.zTree.destroy("myZtreeUser");
}
function onBodyDown(event) {
	if (!(event.target.id == "menuBtn" || event.target.id == "menuContent" || $(event.target).parents("#menuUser").length>0)) {
		hideSelector();
	}
}
</SCRIPT>
