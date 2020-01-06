<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<%=Kit.getDwrJsTag(request,"interface/RpcMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
<SCRIPT type="text/javascript">
var hrefOpen = "rpc!open.action";
var sysadmin = <ww:property value='sysadmin'/>;
var inputTerminalIP = document.createElement("input");
inputTerminalIP.id = "inputTerminalIP";
var inputTerminalPort = document.createElement("input");
inputTerminalPort.id = "inputTerminalPort";
var inputTerminalUser = document.createElement("input");
inputTerminalUser.id = "inputTerminalUser";
var inputTerminalPassword = document.createElement("input");
inputTerminalPassword.id = "inputTerminalPassword";
var selectTerminalServers = document.createElement("select");
selectTerminalServers.id = "selectTerminalServers";
var memServerId;
var memIP = "";
var memPort = "";
var memUser = "";
var memPassword = "";
var memServerKey = "";
function addTerminal()
{
	var nodes = myZtree.getSelectedNodes();
	var parentNode = null;
	if( nodes && nodes.length > 0 ){
		parentNode = nodes[0];
		memServerId = nodes[0].id;
		if( nodes[0].ip ){
			memIP = nodes[0].ip;
		}
		if( nodes[0].port ){
			memPort = nodes[0].port;
		}
	}
	if( !parentNode || !parentNode.gateone ){
		skit_alert("请选择导航树中堡垒机伺服器，添加新增【远程控制终端】");
		return;
	}
	try
	{
		var div0 = createPanelDiv();
		inputTerminalIP.readOnly = false;
		inputTerminalPort.readOnly = false;
		inputTerminalUser.readOnly = false;
		
		var SM = new SimpleModal({"btn_ok":"确定","btn_cancel":"取消","width":520});
	    var content = "<div style='height:240px;width:480px;border:0px solid red;overflow-y:auto;'>"+div0.outerHTML+"</div>"
	    SM.show({
	    	"title":"新增SSH终端(通过堡垒机伺服器【"+parentNode.ip+"】)",
	        "model":"confirm",
	        "callback": function(){
	        	inputTerminalIP = document.getElementById(inputTerminalIP.id);
	        	var ip = inputTerminalIP.value;
	            var regexp2 = "^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$";
	            var m2 = ip.match(new RegExp(regexp2));
	            if( !m2 )
	            {
		        	skit_errtip("请输入正确的IP地址", inputTerminalIP);
			        return false;
	            }
	            inputTerminalPort = document.getElementById(inputTerminalPort.id);
	            var port = Number(inputTerminalPort.value);
	            if( port<1 || port>65535 )
	            {
		        	skit_errtip("请输入正确的端口号", inputTerminalPort);
	            	return false;
	            }
	        	inputTerminalUser = document.getElementById(inputTerminalUser.id);
	        	if( inputTerminalUser.value == "" ){
		        	skit_errtip("请输入终端访问账号", inputTerminalUser);
	        		return false;
	        	}
	        	inputTerminalPassword = document.getElementById(inputTerminalPassword.id);
	            memIP = ip;
	            memPort = port;
	            memUser = inputTerminalUser.value;
	            memPassword = inputTerminalPassword.value;
				if( myZtree.getNodeByParam("id", memUser+"@"+ip+":"+port ) )
				{
		        	skit_errtip("账号["+memUser+"]的ssh连接已经存在", inputTerminalUser);
					return false;
				}
				RpcMgr.getTerminalServerKey(ip,{
					callback:function(rsp) {
						skit_hiddenLoading();
						try
						{
							if( rsp.succeed )
							{
								setTerminal(parentNode, jQuery.parseJSON(rsp.result));
							}
							else
							{
								skit_alert(rsp.message);
							}
						}
						catch(e)
						{
							alert(e);
						}
					},
					timeout:30000,
					errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
				});
	        	return true;
	        },
	        "cancelback": function(){
	        	
	        },
	    	"contents": content
	    });
	    $('#'+inputTerminalIP.id).inputmask('ip');
	    document.getElementById(inputTerminalIP.id).value = memIP;
	    document.getElementById(inputTerminalPort.id).value = memPort;
	    document.getElementById(inputTerminalUser.id).value = memUser;
	}
	catch(e)
	{
		alert(e);
	}
}

/*设置SSH重大*/
function setTerminal(parentNode, servers)
{
	try
	{
		var div0 = document.createElement("div");
		div0.className = "panel panel-default";
		var div1 = document.createElement("div");
		div1.className = "panel-body";
		div1.style.paddingBottom = 1;
		div0.appendChild(div1);
		
		var div2 = document.createElement("div");
		div2.className = "form-group";
		div1.appendChild(div2);
		var div3 = document.createElement("div");
		div3.className = "input-group";
		div2.appendChild(div3);
		span = document.createElement("span");
		span.className = "input-group-addon";
		span.innerHTML = "<i class='fa fa-key'></i>终端服务器";
		div3.appendChild(span);
		selectTerminalServers = document.createElement("select");
		selectTerminalServers.id = "selectTerminalServers";
		selectTerminalServers.className = "form-control";
		div3.appendChild(selectTerminalServers);
		for(var i = 0; i < servers.length; i++){
			var e = servers[i];
			var option = document.createElement("OPTION");
			option.text= e["server-name"];
			option.value = e["security-key"];
			selectTerminalServers.options.add(option);
			
		}
		var SM = new SimpleModal({"btn_ok":"确定","btn_cancel":"取消","width":520});
	    var content = "<div style='height:96px;width:480px;border:0px solid red;overflow-y:auto;'>"+div0.outerHTML+"</div>"
	    SM.show({
	    	"title":"选择终端所属伺服器【"+memIP+"】",
	        "model":"confirm",
	        "callback": function(){
				selectTerminalServers = document.getElementById(selectTerminalServers.id);
	        	if( selectTerminalServers.value == "" ){
		        	skit_errtip("请选择终端伺服器", selectTerminalServers);
	        		return false;
	        	}
				RpcMgr.addTerminal(memServerId, memIP, memPort, memUser, memPassword, selectTerminalServers.value,{
					callback:function(rsp) {
						skit_hiddenLoading();
						try
						{
							if( rsp.succeed )
							{
								var newNode = jQuery.parseJSON(rsp.result);
								var newNodeParent = myZtree.getNodeByParam("pid", newNode.ip+"@"+newNode.pid );
								if( !newNodeParent )
								{
									newNodeParent = {};
									newNodeParent.gateone = true;
									newNodeParent.name = newNode.ip;
									newNodeParent.ip = newNode.ip;
									newNodeParent.id = parentNode.id;
									newNodeParent.pid = newNode.ip+"@"+newNode.pid;
									newNodeParent.iconClose = "images/icons/folder_closed.png";
									newNodeParent.iconOpen = "images/icons/folder_opened.png";
									newNodeParent.title = "SSH端口"+newNode.port+", 伺服器代码"+newNode.serverkey;
									newNodeParent = myZtree.addNodes(parentNode, -1, newNodeParent)[0];
								}
								newNode.ssh = newNode.user+"@"+newNode.ip;
								newNode = myZtree.addNodes(newNodeParent, -1, newNode)[0];
								myZtree.selectNode(newNode);
								open(newNode);
							}
							else
							{
								skit_alert(rsp.message);
								if( rsp.result )
								{
									var existNode = jQuery.parseJSON(rsp.result);
									var node = myZtree.getNodeByParam("id", existNode.id );
									if( node )
									{
										myZtree.selectNode(node);
									}
								}
							}
						}
						catch(e)
						{
							alert(e);
						}
					},
					timeout:30000,
					errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
				});
	        	return true;
	        },
	        "cancelback": function(){
	        	
	        },
	    	"contents": content
	    });
	}
	catch(e)
	{
		alert(e);
	}
}

function createPanelDiv(serverNode){
	var div0 = document.createElement("div");
	div0.className = "panel panel-default";
	var div1 = document.createElement("div");
	div1.className = "panel-body";
	div1.style.paddingBottom = 1;
	div0.appendChild(div1);
	
	var div2 = document.createElement("div");
	div2.className = "form-group";
	div1.appendChild(div2);
	var div3 = document.createElement("div");
	div3.className = "input-group";
	div2.appendChild(div3);
	var span = document.createElement("span");
	span.className = "input-group-addon";
	span.innerHTML = "<i class='fa fa-key'></i>终端ＩＰ地址";
	div3.appendChild(span);
	inputTerminalIP.className = "form-control";
	inputTerminalIP.type = "text";
	div3.appendChild(inputTerminalIP);
	
	div2 = document.createElement("div");
	div2.className = "form-group";
	div1.appendChild(div2);
	div3 = document.createElement("div");
	div3.className = "input-group";
	div2.appendChild(div3);
	span = document.createElement("span");
	span.className = "input-group-addon";
	span.innerHTML = "<i class='fa fa-key'></i>终端SSH端口";
	div3.appendChild(span);
	inputTerminalPort.className = "form-control";
	inputTerminalPort.type = "text";
	inputTerminalPort.style.width = 80;
	div3.appendChild(inputTerminalPort);

	div2 = document.createElement("div");
	div2.className = "form-group";
	div1.appendChild(div2);
	div3 = document.createElement("div");
	div3.className = "input-group";
	div2.appendChild(div3);
	span = document.createElement("span");
	span.className = "input-group-addon";
	span.innerHTML = "<i class='fa fa-key'></i>终端访问账号";
	div3.appendChild(span);
	inputTerminalUser.className = "form-control";
	inputTerminalUser.type = "text";
	div3.appendChild(inputTerminalUser);

	div2 = document.createElement("div");
	div2.className = "form-group";
	div1.appendChild(div2);
	div3 = document.createElement("div");
	div3.className = "input-group";
	div2.appendChild(div3);
	span = document.createElement("span");
	span.className = "input-group-addon";
	span.innerHTML = "<i class='fa fa-key'></i>终端访问密码";
	div3.appendChild(span);
	inputTerminalPassword.className = "form-control";
	inputTerminalPassword.type = "password";
	div3.appendChild(inputTerminalPassword);
	
	if( serverNode ){
		div2 = document.createElement("div");
		div2.className = "form-group";
		div1.appendChild(div2);
		div3 = document.createElement("div");
		div3.className = "input-group";
		div2.appendChild(div3);
		span = document.createElement("span");
		span.className = "input-group-addon";
		span.innerHTML = "<i class='fa fa-key'></i>终端伺服器表";
		div3.appendChild(span);
		selectTerminalServers = document.createElement("select");
		selectTerminalServers.id = "selectTerminalServers";
		selectTerminalServers.className = "form-control";
		div3.appendChild(selectTerminalServers);
		for(var i = 0; i < serverNode.servers.length; i++){
			var e = serverNode.servers[i];
			var option = document.createElement("OPTION");
			option.text= e["server-name"];
			option.value = e["security-key"];
			selectTerminalServers.options.add(option);
		}
		//	alert(serverKey+" = "+option.value +": "+(serverKey == option.value));
	}
	return div0;
}

function setTerminalPassword()
{
	var nodes = myZtree.getSelectedNodes();
	var parentId = null;
	if( nodes && nodes.length > 0 ){
		parentId = nodes[0].getParentNode().id;
	}
	try
	{
		var div0 = createPanelDiv(nodes[0].getParentNode());
		//top.showJson(nodes[0]);
		inputTerminalIP.readOnly = true;
		inputTerminalPort.readOnly = true;
		inputTerminalUser.readOnly = true;
		//selectTerminalServers.readOnly = true;
		
		var SM = new SimpleModal({"btn_ok":"确定","btn_cancel":"取消","width":520});
	    var content = "<div style='height:284px;width:480px;border:0px solid red;overflow-y:auto;'>"+div0.outerHTML+"</div>"
	    SM.show({
	    	"title":"设置SSH终端【"+nodes[0].name+"】的登录密码与连接伺服器",
	        "model":"confirm",
	        "callback": function(){
	        	try{
		        	inputTerminalIP = document.getElementById(inputTerminalIP.id);
		            inputTerminalPort = document.getElementById(inputTerminalPort.id);
		        	inputTerminalUser = document.getElementById(inputTerminalUser.id);
		        	inputTerminalPassword = document.getElementById(inputTerminalPassword.id);
		            memIP = inputTerminalIP.value;
		            memPort = Number(inputTerminalPort.value);
		            memUser = inputTerminalUser.value;
		            memPassword = inputTerminalPassword.value;
					selectTerminalServers = document.getElementById(selectTerminalServers.id);
		        	if( selectTerminalServers.value == "" ){
			        	skit_errtip("请选择终端伺服器", selectTerminalServers);
		        		return false;
		        	}
		            //alert(parentId+"\r\n"+memIP+"\r\n"+memPort+"\r\n"+memUser+"\r\n"+memPassword);
		            RpcMgr.setTerminal(parentId, memIP, memPort, memUser, memPassword, selectTerminalServers.value, {
						callback:function(rsp) {
							skit_hiddenLoading();
							skit_alert(rsp.message);
							if( rsp.succeed ){
								nodes[0].password = memPassword;
							}
						},
						timeout:30000,
						errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
					});
	        	}
	        	catch(e){
	        		alert(e);
	        	}
	        	return true;
	        },
	        "cancelback": function(){
	        	
	        },
	    	"contents": content
	    });
	    document.getElementById(inputTerminalIP.id).value = nodes[0].ip;
	    document.getElementById(inputTerminalPort.id).value = nodes[0].port;
	    document.getElementById(inputTerminalUser.id).value = nodes[0].user;
	    document.getElementById(inputTerminalPassword.id).value = nodes[0].password;
	    selectTerminalServers = document.getElementById(selectTerminalServers.id);
		for(var i = 0; i < selectTerminalServers.options.length; i++){
			if( nodes[0]["security-key"] == selectTerminalServers.options[i].value ){
				selectTerminalServers.options[i].selected = true;
				break;
			}
		}
	}
	catch(e)
	{
		alert(e);
	}
}

function delTerminal()
{
	hideRMenu();
	var nodes = myZtree.getSelectedNodes();
	if( nodes == null || nodes.length == 0 ) return;
	var node = nodes[0];
	var parentNode = node.getParentNode();
	var rootNode = parentNode.getParentNode();
	
	var tips = "你确定要从【"+rootNode.title+"】移除远程控制终端【"+node.id+"】？";
	skit_confirm(tips, function(yes){
		if( yes )
		{
			RpcMgr.delTerminal(rootNode["security-key"], rootNode.title, node.id,{
				callback:function(response){
					if( response.succeed )
					{
						myZtree.removeNode(node);
						removeTab(node.id);
						if( !parentNode.children || parentNode.children.length == 0 ){
							myZtree.removeNode(parentNode);
						}
					}
					rootNode.isParent = true;
					skit_alert(response.message);
				},
				timeout:1000000,
				errorHandler:function(message) {
					skit_hiddenLoading();
				}
			});	
		}
	});
}

</SCRIPT>
<html>
<head>
<link type="text/css" href="skit/ztree/css/zTreeStyle/zTreeStyle.css" rel="stylesheet"/>
<link type="text/css" href="skit/css/bootstrap.css" rel="stylesheet">
<link type="text/css" href="skit/css/bootstrap-tour.min.css" rel="stylesheet">
</head>
<body>
<form>
<input type='hidden' name='id' id='id'>
<input type='hidden' name='db' id='db'>
<input type='hidden' name='sshid' id='sshid'>
<TABLE style='width:100%;height:100%;margin-top:3px'>
<TR><TD width='250'>
        <div class="panel panel-default" style='border: 1px solid #aaaaaa'>
   			<div class="panel-heading"><i class='skit_fa_btn fa fa-server'></i> SSH远程控制管理
   				<ww:if test='sysadmin'>
                <div style='float:right;right:4px;top:0px;' id='divAdd'>
 				<button type="button" class='btn btn-outline btn-primary btn-xs' onclick='addTerminal()'>
 					<i class='fa fa-plus-circle'></i> 新增远程终端</button>
                </div>
                </ww:if>
   			</div>
   			<div class="panel-body" style='padding: 0px;'>
   				<div class="input-group custom-search-form" style='padding:3px;'>
                    <input class="form-control" placeholder="输入关键字找伺服器" id='searchBar' onkeyup='return searchServer(this);' type="text" style='font-size: 12px;height:28px;'>
                    <span class="input-group-btn">
                        <button class="btn btn-inverse" type="button" onclick='searchServer()'>
                            <i class="fa fa-search"></i>
                        </button>
                    </span>
                </div>
   				<div id='divTree'>
					<ul id='myZtree' class='ztree'></ul>
					<div id="rMenu">
						<ul>
							<li onclick="addTerminal();" id='liAddTerminal'><i class='skit_fa_icon fa fa-plus-circle'></i> 新增SSH终端</li>
							<li onclick="setTerminalPassword();" id='liTerminalPassword'><i class='skit_fa_icon_war fa fa-user-secret'></i> 设置SSH密码</li>
							<li onclick="delTerminal();" id='liDelTerminal'><i class='skit_fa_icon_red fa fa-remove'></i> 移除SSH终端</li>
						</ul>
					</div>
				</div>
   			</div>
   		</div>	
	</TD>
	<TD style='width:3px;'/>
	<TD valign='top'>
		<div id="tabL" style="position:relative;display:none">
			<ul id='ulTab'>
				<li><a href="#tabL-about">关于GateOne</a></li>
			</ul>
			<div id="tabL-about">
				<iframe id='i-about' name='i-about' src='http://liftoff.github.io/GateOne/About/index.html' frameborder='0' style='width:100%;height:100%;'></iframe>
			</div>
		</div>
	</TD>
</TR>
</TABLE>
</form>
</body>
<SCRIPT type="text/javascript">
var ifrid = "i-about";// = "i-1";
function resizeWindow()
{
	var div = document.getElementById( 'divTree' );
	div.style.height = windowHeight - 78;
	if( ifrid )
	{
		div = document.getElementById( ifrid );
		var h = document.getElementById("ulTab").clientHeight;
		if( h == 0 ) h = 33;
		div.style.height = windowHeight - h - 15;
	}
}

function searchServer()
{
	var text = document.getElementById( "searchBar" ).value;
	text = text.trim();
	if( text == "" )
	{
		return false;
	}
	
	var nodes = myZtree.getNodesByParamFuzzy("ip", text, null);
	for(var i = 0; i < nodes.length; i++)
	{
		if( nodes[i].isParent ) continue;
		myZtree.selectNode(nodes[i]);
		open(nodes[i]);
		break;
	}
}
</SCRIPT>
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<%@ include file="../../skit/inc/skit_ztree.inc"%>
<style type='text/css'>
.ui-tabs .ui-tabs-panel {
    display: block;
    border-width: 0;
    padding: 0px;
    background: none;
}
</style>
<link href="skin/defone/css/simplemodal.css" rel="stylesheet">
<script src="skin/defone/js/mootools-core-1.3.1.js"></script>
<script src="skin/defone/js/simple-modal.js?v=3"></script>
<script src="skit/js/jquery.inputmask.bundle.min.js"></script>
<SCRIPT type="text/javascript">
$( '#divTree' ).niceScroll({
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

$('#tabL').tabs({
/*    add: function(event, ui) {
        alert(ui.panel.id);
        alert($tabs);
        $tabs.tabs('select', '#' + ui.panel.id);
    },*/
    remove: function(event, ui) {
		$("#tabL").tabs('select', '#i-about');
    },
    select: function(event, ui) {
		ifrid = ui.panel.id;
		if( resizeWindow ) resizeWindow();
    }
});
document.getElementById( 'tabL' ).style.display = "";
var timestamp = <ww:property value='timestamp'/>;
//########################################################################
	var setting = {
		//editable: false,
		//fontCss : {color:"red"},
		check: {
			enable: false
		},
		callback: {
			onClick: onClick,
			onRightClick: onRightClick,
			onCollapse: onCollapse,
			onExpand: onExpand
		},
		data: {
			key: {
				title: "title"
			}
		}
	};

	function onCollapse(event, treeId, treeNode)
	{
	}
	
	function onExpand(event, treeId, treeNode) 
	{
	}
	
	function onClick(event, treeId, treeNode)
	{
		myZtree.expandNode(treeNode, true);
		open(treeNode);
	}

	function onRightClick(event, treeId, treeNode)
	{
		if( !sysadmin || treeNode.id == "localhost" ){
			return;
		}
		if( treeNode.isParent ){
			document.getElementById("liTerminalPassword").style.display = "none";
			document.getElementById("liAddTerminal").style.display = "";
			document.getElementById("liDelTerminal").style.display = "none";
		}
		else {
			document.getElementById("liTerminalPassword").style.display = "";
			document.getElementById("liAddTerminal").style.display = "none";
			document.getElementById("liDelTerminal").style.display = "";
		}
		if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
			myZtree.cancelSelectedNode();
			showRMenu("root", event.clientX, event.clientY);
		} else if (treeNode && !treeNode.noR) {
			myZtree.selectNode(treeNode);
			showRMenu("node", event.clientX, event.clientY);
		}
	}
	
	$(document).ready(function(){
		try
		{
			var zNodes = <ww:property value="jsonData" escape="false"/>;
			$.fn.zTree.init($("#myZtree"), setting, zNodes);
			myZtree = $.fn.zTree.getZTreeObj("myZtree");
			myZtreeMenu = $("#rMenu");
			/*var serverid = getUserActionMemory(memoryServer);
			var node = null;
			if( serverid )
			{
				var node = myZtree.getNodeByParam("id", serverid);
				if( node )
				{
					myZtree.selectNode(node);
					//open(node);
					return;
				}
			}*/
			//serverid = -1;
			var nodes = myZtree.getNodesByParam("server", true, null);
			if( !nodes || nodes.length == 0 )
			{
				window.top.skit_alert('集群未配置任何伺服器作为堡垒机，不能使用集群SSH远程管理器', "后台提示", function(){
					window.top.closeView();
				});
			}
			/*else
			{
				var node = nodes[0];
				myZtree.selectNode(node);
				open(node);
			}*/
		}
		catch(e)
		{
			alert(e);
		}
	});
//#########################################################################
function removeTab(id)
{
	if( !id ){
		var node = myZtree.getSelectedNodes()[0];
		id = node.id;
	}
	var node = myZtree.getNodeByParam("id", -1);
	if( node )
	{
		myZtree.selectNode(node);
		open(node);
	}
	$("#tabL").tabs('remove', '#i'+id);
}

function open(node)
{
	var parent = node.getParentNode();
	if( !sysadmin && !parent ){
		$("#tabL").tabs('select', '#i-about');
		return;
	}
	if( node.children && parent ){
		$("#tabL").tabs('select', '#i-about');
		return;		
	}
	var i = document.getElementById( "i"+node.id );
	if( i ){}else
	{
		var id = "i"+node.id;
		var ulTab = document.getElementById( "ulTab" );
		var li = document.createElement("li");
		li.innerHTML = "<a href='#i"+node.id+"'>"+(parent?node.ssh:node.name)+" <i class='fa fa-remove' style='cursor:pointer' title='关闭选项卡' onclick='removeTab(\""+node.id+"\")'></i></a>";
		ulTab.appendChild(li);
		i = document.createElement("iframe");
		i.name = id;
		i.id = id;
		i.className = "nonicescroll";
		i.style.width = "100%";
		i.style.border = "0px";
		i.style.display = "none";
		document.getElementById( "tabL" ).appendChild(i);
		$("#tabL").tabs("refresh");
	}
	$("#tabL").tabs('select', '#i'+node.id);
	/*if( parent ){
		document.getElementById( "id" ).value = parent.id?parent.id:"";
		document.getElementById( 'sshid' ).value = node.id;
	}
	else{
		document.getElementById( "id" ).value = node.id?node.id:"";
		document.getElementById( 'sshid' ).value = "";
	}*/
	if( parent ){
		document.getElementById( "id" ).value = parent.id?parent.id:"";
		document.getElementById( 'sshid' ).value = node.id;
	}
	else{
		document.getElementById( "id" ).value = node.id?node.id:"";
		document.getElementById( 'sshid' ).value = "";
	}
	var tId = node.tId;
	tId = tId.substring(tId.indexOf("_")+1);
	document.getElementById( "db" ).value = tId;
	document.forms[0].action = hrefOpen;
	document.forms[0].method = "POST";
	document.forms[0].target = "i"+node.id;
	document.forms[0].submit();
}
function view()
{
	var node = myZtree.getSelectedNodes()[0];
	var title = titleView+" - "+node.ip;
	if( node.name ) title += node.name;
	openView(title, hrefView+"?id="+node.id+"&ip="+node.ip+"&port="+node.port);
	hideRMenu();
}

function rmServer(nodes, filter)
{
	for(var i = 0; i < nodes.length; i++)
	{
		var node = nodes[i];
		if( node.children )
		{
			rmServer(node.children, filter);
		}
		else if( !node.isParent )
		{
			if( filter[node.id] ){}else
			{
				myZtree.removeNode(node);
			}
		}
	}
}
</SCRIPT>
</html>