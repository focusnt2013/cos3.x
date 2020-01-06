<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%> 
<style type='text/css'>
.ztree * {
    padding: 0;
    margin: 0;
    font-size: 12px;
}
.ztree li span.button.add {margin-left:2px; margin-right: -1px; background-position:-144px 0; vertical-align:top; *vertical-align:middle}
div#rMenu {
	position:absolute; visibility:hidden; top:0; background-color: #555;text-align: left;padding: 2px;
}
div#rMenu ul {
	position: absolute;
	top: 100%;
	left: 0;
	z-index: 1000;
	display: none;
	float: left;
	min-width: 160px;
	padding: 5px 0;
	margin: 2px 0 0;
	font-size: 14px;
	text-align: left;
	list-style: none;
	background-color: #fff;
	-webkit-background-clip: padding-box;
	background-clip: padding-box;
	border: 1px solid #ccc;
	border: 1px solid rgba(0,0,0,.15);
	border-radius: 4px;
	-webkit-box-shadow: 0 6px 12px rgba(0,0,0,.175);
	box-shadow: 0 6px 12px rgba(0,0,0,.175);
}
div#rMenu ul li{
	margin: 1px 0;
	padding: 5px 5px;
	cursor: pointer;
	list-style: none outside none;
	background-color: #fff;
}
div#rMenu ul li {
    display: block;
    padding: 3px 10px;
    clear: both;
    font-weight: 400;
    line-height: 1.42857143;
    color: #333;
    white-space: nowrap;
}
.divider {
	height：1px;
	border-top: 1px solid #ccc;
}
div#rMenu ul li:focus, div#rMenu ul li:hover {
    color: #262626;
    text-decoration: none;
    background-color: #f5f5f5;
}
</style>
<script type="text/javascript" src="<%=Kit.URL_PATH(request)%>skit/js/bootstrap.js"></script>
<script type="text/javascript" src="<%=Kit.URL_PATH(request)%>skit/ztree/js/jquery.ztree.core.min.js"></script>
<script type="text/javascript" src="<%=Kit.URL_PATH(request)%>skit/ztree/js/jquery.ztree.excheck.js"></script>
<script type="text/javascript" src="<%=Kit.URL_PATH(request)%>skit/ztree/js/jquery.ztree.exedit.js"></script>
<SCRIPT type="text/javascript">
var myZtree;
var myZtreeMenu;

function showRemoveBtn(treeId, treeNode) {
	return true;
}

function onRightClick(event, treeId, treeNode) {
	if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
		myZtree.cancelSelectedNode();
		showRMenu("root", event.clientX, event.clientY);
	} else if (treeNode && !treeNode.noR) {
		myZtree.selectNode(treeNode);
		showRMenu("node", event.clientX, event.clientY);
	}
}

function showRMenu(type, x, y) {
	$("#rMenu ul").show();
	if (type=="root") {
		$("#m_del").hide();
		$("#m_check").hide();
		$("#m_unCheck").hide();
	} else {
		$("#m_del").show();
		$("#m_check").show();
		$("#m_unCheck").show();
	}
	myZtreeMenu.css({"top":y+"px", "left":x+"px", "visibility":"visible"});
	$("body").bind("mousedown", onBodyMouseDown);
}

function hideRMenu() {
	if (myZtreeMenu) myZtreeMenu.css({"visibility": "hidden"});
	$("body").unbind("mousedown", onBodyMouseDown);
}

function onBodyMouseDown(event){
	if (!(event.target.id == "rMenu" || $(event.target).parents("#rMenu").length>0)) {
		myZtreeMenu.css({"visibility" : "hidden"});
	}
}

function onClick(event, treeId, treeNode) {
	myZtree.expandNode(treeNode, null, null, null, true);
}

function expandAll(expandSign) {
	myZtree.expandAll(expandSign);
}

function addHoverDom(treeId, treeNode) {
	/*if( treeNode.newMenu ) return;
	var sObj = $("#" + treeNode.tId + "_span");
	if ( $("#addBtn_"+treeNode.tId).length>0) return;
	var addStr = "<span class='button add' id='addBtn_" + treeNode.tId+ "' title='新增菜单' onfocus='this.blur();'></span>";
	sObj.after(addStr);
	var btn = $("#addBtn_"+treeNode.tId);
	if (btn) btn.bind("click", function(){
		addMenu();
	});*/
};
function removeHoverDom(treeId, treeNode) {
	//$("#addBtn_"+treeNode.tId).unbind().remove();
};

function addDiyDom(treeId, treeNode) {
	/*if( treeNode.ico )
	{
		var aObj = $("#" + treeNode.tId + "_a");
		var editStr = "<i class='fa "+treeNode.ico+"' id='ico_fa_menu_" + treeNode.tId + "'></i>";
		if( aObj ) aObj.after(editStr);
	}*/
}
var curExpandNode = null;
function beforeExpand(treeId, treeNode) {
	var pNode = curExpandNode ? curExpandNode.getParentNode():null;
	var treeNodeP = treeNode.parentTId ? treeNode.getParentNode():null;
	var zTree = myZtree;
	for(var i=0, l=!treeNodeP ? 0:treeNodeP.children.length; i<l; i++ ) {
		if (treeNode !== treeNodeP.children[i]) {
			zTree.expandNode(treeNodeP.children[i], false);
		}
	}
	while (pNode) {
		if (pNode === treeNode) {
			break;
		}
		pNode = pNode.getParentNode();
	}
	if (!pNode) {
		singlePath(treeNode);
	}

}
function singlePath(newNode) {
	if (newNode === curExpandNode) return;

    var zTree = myZtree, rootNodes, tmpRoot, tmpTId, i, j, n;

    if (!curExpandNode) {
        tmpRoot = newNode;
        while (tmpRoot) {
            tmpTId = tmpRoot.tId;
            tmpRoot = tmpRoot.getParentNode();
        }
        rootNodes = zTree.getNodes();
        for (i=0, j=rootNodes.length; i<j; i++) {
            n = rootNodes[i];
            if (n.tId != tmpTId) {
                zTree.expandNode(n, false);
            }
        }
    } else if (curExpandNode && curExpandNode.open) {
		if (newNode.parentTId === curExpandNode.parentTId) {
			zTree.expandNode(curExpandNode, false);
		} else {
			var newParents = [];
			while (newNode) {
				newNode = newNode.getParentNode();
				if (newNode === curExpandNode) {
					newParents = null;
					break;
				} else if (newNode) {
					newParents.push(newNode);
				}
			}
			if (newParents!=null) {
				var oldNode = curExpandNode;
				var oldParents = [];
				while (oldNode) {
					oldNode = oldNode.getParentNode();
					if (oldNode) {
						oldParents.push(oldNode);
					}
				}
				if (newParents.length>0) {
					zTree.expandNode(oldParents[Math.abs(oldParents.length-newParents.length)-1], false);
				} else {
					zTree.expandNode(oldParents[oldParents.length-1], false);
				}
			}
		}
	}
	curExpandNode = newNode;
}

function onExpand(event, treeId, treeNode) {
	curExpandNode = treeNode;
}

function dropPrev(treeId, nodes, targetNode) {
	var pNode = targetNode.getParentNode();
	if (pNode && pNode.dropInner === false) {
		return false;
	} else {
		for (var i=0,l=curDragNodes.length; i<l; i++) {
			var curPNode = curDragNodes[i].getParentNode();
			if (curPNode && curPNode !== targetNode.getParentNode() && curPNode.childOuter === false) {
				return false;
			}
		}
	}
	return true;
}
function dropInner(treeId, nodes, targetNode) {
	if (targetNode && targetNode.dropInner === false) {
		return false;
	} else {
		for (var i=0,l=curDragNodes.length; i<l; i++) {
			if (!targetNode && curDragNodes[i].dropRoot === false) {
				return false;
			} else if (curDragNodes[i].parentTId && curDragNodes[i].getParentNode() !== targetNode && curDragNodes[i].getParentNode().childOuter === false) {
				return false;
			}
		}
	}
	return true;
}
function dropNext(treeId, nodes, targetNode) {
	var pNode = targetNode.getParentNode();
	if (pNode && pNode.dropInner === false) {
		return false;
	} else {
		for (var i=0,l=curDragNodes.length; i<l; i++) {
			var curPNode = curDragNodes[i].getParentNode();
			if (curPNode && curPNode !== targetNode.getParentNode() && curPNode.childOuter === false) {
				return false;
			}
		}
	}
	return true;
}

//var log;
//var className = "dark";
var curDragNodes;
var autoExpandNode;
function beforeDrag(treeId, treeNodes) {
	//className = (className === "dark" ? "":"dark");
	//showLog("[ "+getTime()+" beforeDrag ]&nbsp;&nbsp;&nbsp;&nbsp; drag: " + treeNodes.length + " nodes." );
	for (var i=0,l=treeNodes.length; i<l; i++) {
		if (treeNodes[i].drag === false) {
			curDragNodes = null;
			return false;
		} else if (treeNodes[i].parentTId && treeNodes[i].getParentNode().childDrag === false) {
			curDragNodes = null;
			return false;
		}
	}
	curDragNodes = treeNodes;
	return true;
}
function beforeDragOpen(treeId, treeNode) {
	autoExpandNode = treeNode;
	return true;
}
function beforeDrop(treeId, treeNodes, targetNode, moveType, isCopy) {
	//className = (className === "dark" ? "":"dark");
	//showLog("[ "+getTime()+" beforeDrop ]&nbsp;&nbsp;&nbsp;&nbsp; moveType:" + moveType);
	//showLog("target: " + (targetNode ? targetNode.name : "root") + "  -- is "+ (isCopy==null? "cancel" : isCopy ? "copy" : "move"));
	return true;
}
function onDrag(event, treeId, treeNodes) {
	//className = (className === "dark" ? "":"dark");
	//showLog("[ "+getTime()+" onDrag ]&nbsp;&nbsp;&nbsp;&nbsp; drag: " + treeNodes.length + " nodes." );
}
function onDrop(event, treeId, treeNodes, targetNode, moveType, isCopy) {
	//className = (className === "dark" ? "":"dark");
	//showLog("[ "+getTime()+" onDrop ]&nbsp;&nbsp;&nbsp;&nbsp; moveType:" + moveType);
	//showLog("target: " + (targetNode ? targetNode.name : "root") + "  -- is "+ (isCopy==null? "cancel" : isCopy ? "copy" : "move"))
}
function onExpand(event, treeId, treeNode) {
	curExpandNode = treeNode;
	if (treeNode === autoExpandNode) {
		//className = (className === "dark" ? "":"dark");
		//showLog("[ "+getTime()+" onExpand ]&nbsp;&nbsp;&nbsp;&nbsp;" + treeNode.name);
	}
}
var expandMemory = new Object();
//保存用户操作记忆
function saveExpandMemory(id)
{
	var memory = "";
	var i = 0;
	for( var o in expandMemory )
	{
		if( expandMemory[o] )
		{
			if( i > 0 )
			{
				memory += ",";
			}
			memory += o;
			i += 1;
		}
	}
	setUserActionMemory(id, memory);
}

function setNodeMemoryOpen(nodes, opens)
{
	for( var i = 0; i < nodes.length; i++ )
	{
		var node = nodes[i];
		if( node.ip ) continue;
 		if( opens[node.id] )
		{
			node["open"] = true;
		}
 		if( node.children )
 		{
 			setNodeMemoryOpen(node.children, opens)
 		}
	}
}
</SCRIPT>