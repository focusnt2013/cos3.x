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

//########################################################################
	var setting = {
		//editable: false,
		//fontCss : {color:"red"},
		check: {
			enable: false
		},
		callback: {
			onRightClick: onRightClick,
			onClick: onClick,
			onDblClick: onDblClick,
			beforeRename: beforeRename,
			onRename: onRename,
			beforeDrag: beforeDrag,
			beforeDrop: beforeDrop,
			beforeDragOpen: beforeDragOpen,
			onDrag: onDrag,
			onDrop: onDrop,
		},
		view: {
			addDiyDom: addDiyDom
		},
		edit: {
			drag: {
				autoExpandTrigger: true,
				prev: dropPrev,
				inner: dropInner,
				next: dropNext
			},
			enable: true,
			showRemoveBtn: false,
			showRenameBtn: sysid!="local"
		},
		data: {
			key: {
				name: developer?"name":"cname",
				title: "id"
			}
		}
	};
	
	function onRightClick(event, treeId, treeNode) 
	{
		document.getElementById( 'liCheckAll' ).style.display = treeNode.rootdir?"":"none";
		document.getElementById( 'liOpenVersions' ).style.display = "none";
		document.getElementById( 'liQuerySql' ).style.display = "none";
		document.getElementById( 'liAddConfigTemplate' ).style.display = "none";
		document.getElementById( 'liAddQueryTemplate' ).style.display = "none";
		document.getElementById( 'liCopyDiggConfig' ).style.display = "none";
		document.getElementById( 'liDeleteTemplate' ).style.display = "none";
		document.getElementById( 'liUploadTemplate' ).style.display = "none";
		document.getElementById( 'liDownloadTemplate' ).style.display = "none";
		document.getElementById( 'liUploadTemplates' ).style.display = "none";
		document.getElementById( 'liDownloadTemplates' ).style.display = "none";
		document.getElementById( 'liDivider0' ).style.display = "none";
		document.getElementById( 'liAddTemplateDir' ).style.display = "none";
		document.getElementById( 'liPublishMenus' ).style.display = "none";
		if( treeNode.rootdir ){
			if( !developer ){
				document.getElementById( 'liUploadTemplates' ).style.display = "";
				document.getElementById( 'liDownloadTemplates' ).style.display = "";
			}
		}
		else{
			if( sysid == "local" ){
				return;
			}
		}
		
		if( sysid != "local" ){
			if( treeNode.type == "dir" )
			{
				if( developer ){
					document.getElementById( 'liAddQueryTemplate' ).style.display = "";
					document.getElementById( 'liAddConfigTemplate' ).style.display = "";
					document.getElementById( 'liUploadTemplate' ).style.display = "";
					document.getElementById( 'liDivider0' ).style.display = "";
					document.getElementById( 'liAddTemplateDir' ).style.display = "";
					document.getElementById( 'liDeleteTemplate' ).style.display = treeNode.rootdir?"none":"";
				}
			}
			else
			{
				if( !treeNode.type || treeNode.type == "datasource" ) return;
				if( treeNode.type == "table"  )
				{
					document.getElementById( 'liQuerySql' ).style.display = "";
				}
				else
				{
					if( treeNode.demo )
					{
						if( developer ){
							document.getElementById( 'liCopyDiggConfig' ).style.display = "";
						}
						document.getElementById( 'liDownloadTemplate' ).style.display = "";
					}
					else 
					{
						document.getElementById( 'liOpenVersions' ).style.display = "";
						if( developer ){
							document.getElementById( 'liDeleteTemplate' ).style.display = "";
							document.getElementById( 'liCopyDiggConfig' ).style.display = "";
						}
						document.getElementById( 'liDownloadTemplate' ).style.display = "";
					}
				}
			}
		
		}
		if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
			myZtree.cancelSelectedNode();
			showRMenu("root", event.clientX, event.clientY);
			//alert("0:"+document.body.clientTop);
		} else if (treeNode && !treeNode.noR) {
			myZtree.selectNode(treeNode);
			showRMenu("node", event.clientX, event.clientY);
		}
	}

	function onClick(event, treeId, treeNode)
	{
		//myZtree.expandNode(treeNode, null, null, null, true);
		//myZtree.expandNode(treeNode, true);
		open(treeNode);
		//setUserActionMemory("role!set.tree", treeNode.id);
	}
	
	function onDblClick(event, treeId, treeNode){
		if( treeNode.type == "edit" && !treeNode.demo ){
			doEdit();
		}
	}

	function showRenameBtn(treeId, treeNode) {
		return !treeNode.demo&&treeNode.type=="dir"&&treeNode.id!="";
	}
	
	var _oldName;
	function beforeRename(treeId, treeNode, newName, isCancel) {
		if( sysid == "local" ){
			myZtree.cancelEditName();
			skit_alert("主界面框架系统的模板目录不能修改.");
			return false;
		}
		var parent = treeNode.getParentNode();
		if( !parent ){
			myZtree.cancelEditName();
			skit_alert("模板管理根目录不能修改.");
			return false;
		}
		if (newName.length == 0) {
			myZtree.cancelEditName();
			skit_alert("目录名称不能为空.");
			return false;
		}
		else if (newName.length > 10) {
			myZtree.cancelEditName();
			skit_alert("目录名称不能超过10个字.");
			return false;
		}
		else if (newName.indexOf('/') != -1 ||
				newName.indexOf(':') != -1 ||
				newName.indexOf('\\') != -1 ||
				newName.indexOf('<') != -1 ||
				newName.indexOf('>') != -1) {
			myZtree.cancelEditName();
			skit_alert("目录名称不能出现特殊字符.");
			return false;
		}
		var children = parent.children;
		if( children && children.length > 1 )
			for(var i = 0; i < children.length; i++)
			{
				var child = children[i];
				if( child == treeNode ) continue;
				if( child.name == newName )
				{
					skit_alert("同级目录名称不能相同.");
					if( treeNode.id )
					{
						myZtree.cancelEditName();
					}
					else
					{
						myZtree.removeNode(treeNode);
						myZtree.selectNode(parent, false, false);
						open(parent);
					}
					return false;
				}
			}
		_oldName = treeNode.name;
		return true;
	}
	
	function onRename(e, treeId, treeNode, isCancel) 
	{
		if( isCancel )
		{
			return;
		}
		if (_oldName == treeNode.name && treeNode.id != "" ) {
			skit_alert("目录名称【"+_oldName+"】没有变化。");
			return;
		}
		var tips = treeNode.id?("您确定要将目录名称更改为"+treeNode.name+"吗？"):("您确定要将目录名称设置为"+treeNode.name+"吗？");
		skit_confirm(tips, function(yes){
			if( yes )
			{
				var parent = treeNode.getParentNode();
				var isAdd = treeNode.id?false:true;
				var zkpath = isAdd?parent.id:treeNode.id;
				DiggConfigMgr.setTemplateDir(sysid, zkpath, treeNode.name, isAdd, {
					callback:function(rsp) {
						if( rsp.succeed ){
							if( isAdd )	treeNode.id = rsp.result;
							open(treeNode);
						}
						else{
							skit_error(rsp.message);
							var parent = treeNode.getParentNode();
							myZtree.removeNode(treeNode);
							myZtree.selectNode(parent, false, false);
							open(parent);
						}
					},
					timeout:30000,
					errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
				});
			}
			else
			{
				treeNode.name = _oldName;
				myZtree.updateNode (treeNode);
			}
		});
	}

	function addDiyDom(treeId, treeNode)
	{
		setTemplateCheck(treeNode);
	}
	
	function setCheckResult(json)
	{
		try{
			var result = jQuery.parseJSON(json);
			var i, treeNode, check;
			for(i=0; i < result.length; i++)
			{
				check = result[i];
				treeNode = myZtree.getNodeByParam("id", check.path);
				if( treeNode )
				{
					treeNode.check = check;
					setParentCheckResult(treeNode, check.err, check.war);
					setTemplateCheck(treeNode);
				}
			}			
		}
		catch(e){
			skit_alert("执行模板检测结果回调方法出现异常:"+e);
		}
	}

	function setParentCheckResult(treeNode, err, war)
	{
		var parent = treeNode.getParentNode();
		if( parent )
		{
			if( !parent.check )
			{
				parent.check = {};
				parent.check.err = 0;
				parent.check.war = 0;
			}
			parent.check.err += err;
			parent.check.war += war;
			setTemplateCheck(parent);
			setParentCheckResult(parent, err, war);
		}
	}
	
	function setTemplateCheck(treeNode)
	{
		var title, ico = "skit_fa_icon_gray fa fa-question-circle", check;
		title = "待检测";
		check = treeNode["check"];
		var ic = document.getElementById( 'ico_abc_'+$.md5(treeNode.id) );
		if( check ){

			if( check.war > 0 )
			{
				title += check.war+"个改进提示.<br/>";
				ico = "skit_fa_icon_yellow fa fa-exclamation-circle";
			}
			if( check.err > 0 )
			{
				title += check.err+"个严重错误.";
				ico = "skit_fa_icon_red fa fa-minus-circle";
			}
			if( check.war == 0 && check.err == 0 )
			{
				title = "模板OK";
				ico = "skit_fa_icon_green fa fa-check-circle-o";
			}
		}
		if( ic ){
			ic.className = ico;
			ic.title = title;
			if( !developer ){
				document.getElementById( 'iDiggState' ).className = ic.className;
				document.getElementById( 'pDiggState' ).innerHTML = ic.title;
			}
			return;
		}
		var treeItem;
		if( treeNode.type == 'dir')
		{
			treeNode.check = {};
			treeNode.check.err = 0;
			treeNode.check.war = 0;
			treeItem = $("#" + treeNode.tId + "_a");
			treeItem.before("<i id='ico_abc_"+$.md5(treeNode.id)+"' class='"+ico+"' title='"+title+"' style='font-size:14px;'></i>");
			return;
		}
		if( !treeNode.template ) return;
		treeItem = $("#" + treeNode.tId + "_a");
		treeItem.before("<i id='ico_abc_"+$.md5(treeNode.id)+"' class='"+ico+"' title='"+title+"' style='font-size:14px;'></i>");
	}

	function beforeDrop(treeId, treeNodes, targetNode, moveType, isCopy) {
		var srcNode = treeNodes[0];
		if( srcNode.demo || targetNode.demo  ){
			skit_alert("DEMO模板或目录不能被移动");
			return false;
		}
		if( targetNode.type != "dir"  ){
			skit_alert("模板只能选择移动到模板目录");
			return false;
		}
		if( !srcNode.template  ){
			skit_alert("只允许用户自定模板移动");
			return false;
		}
		if( srcNode.editmode  ){
			skit_alert("编辑中的模板不能拖拽移动");
			return false;
		}
		return true;
	}

	function onDrop(event, treeId, treeNodes, targetNode, moveType, isCopy)
	{
		if( moveType )
		{
			try
			{
				var node = treeNodes[0];
				var parentSource = treeNodes[0].getParentNode();
				var parentTarget = targetNode.getParentNode();
				if( !parentTarget ) parentTarget == targetNode;

				tIdOper = node.tId;
				//alert(moveType+" source["+parentSource.path+":"+treeNodes[0].path+"]   target["+parentTarget.path+":"+targetNode.path+"]");
				DiggConfigMgr.dragDropTemplate(moveType, node.id, targetNode.id, sysid, {
					callback:function(rsp) {
						skit_hiddenLoading();
						try
						{
							if( rsp.succeed ){
								node.id = rsp.result;
								open(node);
							}
							else{
								skit_error(rsp.message);
								parent.open();
							}
						}
						catch(e)
						{
							skit_alert("拖拽移动模板操作出现异常"+e);
						}
					},
					timeout:30000,
					errorHandler:function(message) {skit_hiddenLoading(); skit_alert(message); }
				});			
			}
			catch(e)
			{
				skit_alert(e);
			}
		}
	}
	$(document).ready(function(){
		try
		{
			var jsonData = '<ww:property value="jsonData" escape="false"/>';
			var zNodes = jQuery.parseJSON(jsonData);
			//var zNodes = json.children;
			myZtree = $.fn.zTree.init($("#myZtree"), setting, zNodes);
			//myZtree = $.fn.zTree.getZTreeObj("myZtree");
			myZtreeMenu = $("#rMenu");
			var id = getUserActionMemory("diggcfg!manager."+sysid);
			if( developer ){
				id = getUserActionMemory("diggcfg!explorer."+sysid);
			}
			var opened = false;
			if( id )
			{
				var node = myZtree.getNodeByParam("id", id);
				if( node )
				{
					myZtree.selectNode(node);
					myZtree.expandNode(node, true);
					open(node);
					opened = true;
				}
			}
			if( !opened )
			{
				var node = myZtree.getNodeByParam("id", "/cos/config/modules/"+sysid+"/digg");
				if( node )
				{
					myZtree.selectNode(node);
					myZtree.expandNode(node, true);
					open(node);
				}
			}
			checkAllTemplates(false);
	        
			$(document).keydown(function(e){
				//var ev = window.event||e;
				if( e.ctrlKey ){
					if(e.keyCode == 83){
						doSave();
			            e.preventDefault();  
			            window.event.returnValue= false;
					}
					if(e.keyCode == 87){
			            e.preventDefault();  
			            window.event.returnValue= false;
					}
				}
			});
		}
		catch(e)
		{
			skit_alert("初始化模板导航树异常"+e.message+", 行数"+e.lineNumber);
		}
	});
//#########################################################################
function checkAllTemplates(again){
	hideRMenu();
	document.forms[0].method = "POST";
	document.forms[0].action = "diggcfg!batchtest.action"+(again?"?again=1":"");
	document.forms[0].target = "iDownload";
	document.forms[0].submit();
}
function doFullscreen(btn)
{
	var iDigg = document.getElementById( 'iDigg' );
	iDigg.style.display = "none";
	document.getElementById( 'tdTree' ).style.display = "none";
	fullscreen = true;
	if( btn )
	{
		if( btn.className.indexOf("warning") != -1  ){
			btn.className = "btn btn-outline btn-default btn-xs";
			fullscreen = false;
			document.getElementById( 'tdTree' ).style.display = "";
			resizeWindow();
			return;
		}
		btn.className = "btn btn-outline btn-warning btn-xs";
		resizeWindow();
	}
	else
	{
		btn = document.getElementById( 'btnFullscreen' );
		btn.className = "btn btn-outline btn-warning btn-xs";
	}
}
function closeVersions(template){
	if(!template){
		template = getNode();
	}
	if( template.viewversions ){
		delete template.viewversions;
	}
	document.getElementById( 'divVersions' ).style.display = "none";
	var iDigg = document.getElementById( 'iDigg' );
	iDigg.style.display = "none";
	resizeWindow();
}

function openVersions(template){
	hideRMenu();
	if(!template){
		template = getNode();
	}
	template.viewversions = true;
	var iDigg = document.getElementById( 'iDigg' );
	iDigg.style.display = "";
	document.getElementById( 'divVersions' ).style.display = "";
	resizeWindow();
	document.forms[0].method = "POST";
	document.getElementById("gridtext").value = "";
	document.getElementById("id").value = template.id;
	document.forms[0].action = "diggcfg!version.action";
	document.forms[0].target = "iDigg";
	document.forms[0].submit();
}
//预览编辑中的模板
function previewTemplateEdit(flag)
{
	var node = getNode();
	if( !node.check )
	{
		skit_alert("该模板还未完成检测，不能被预览");return;
	}
	if( node.check && node.check.err > 0 )
	{
		skit_alert("该模板检测发现有不可忽略的错误，请完成修改后再预览");return;
	}
	
	try
	{
		var div = document.createElement("div");
		div.id = "div-editdebug";
		div.className = "panel panel-primary";
		div.style.marginTop = 3;
		div.style.marginLeft = 3;
		div.style.position = "absolute";
		div.style.width = windowWidth - 10;
		var div1 = document.createElement("div");
		div1.className = "panel-heading";
		div1.style.height = 28;
		div.appendChild(div1);
		var div10 = document.createElement("div");
		div10.style.float = "left";
		div1.appendChild(div10);
		var span10 = document.createElement("span");
		span10.className = "panel-title";
		span10.innerHTML = "<i class='fa fa-eye'></i> 模板预览";
		span10.style.width = 512;
		div10.appendChild(span10);
		var div11 = document.createElement("div");
		div11.className = "panel-menu";
		div11.innerHTML = 
			"<button type='button' onclick='closeTemplateEdit()' data-action='close' class='btn btn-warning btn-action btn-xs'><i class='fa fa-times'></i></button>";
		div1.appendChild(div11);
		var div2 = document.createElement("div");
		div2.className = "panel-body";
		div2.innerHTML = "<iframe name='i-editdebug' id='i-editdebug' class='nonicescroll' style='width:100%;border:0px solid red;'></iframe>";
		div.appendChild(div2);
		document.forms[0].appendChild(div);
		div.style.left = 3;
		div.style.top = 32;
		div.style.zIndex = zIndex++;
		document.getElementById("divMask").style.visibility = "visible";
		document.getElementById("divMask").style.width = windowWidth;
		document.getElementById("divMask").style.height = windowHeight;
		document.getElementById("divMask").onclick = new Function("closeTemplateEdit();");
		document.getElementById("i-editdebug").style.height = windowHeight - 100;
		document.getElementById("gridtext").value = getDiggXmlEditorValue();
		
		document.getElementById("liDigglog").style.display = "none";
		document.getElementById("liDiggexport").style.display = "none";
		document.getElementById("divDebugObjectScript").style.display = "";
		document.getElementById("divDebugObjectScript").style.zIndex = zIndex*2;
		tour();	
		if( "form" == node.type ){
			document.forms[0].action = "form!debug.action?a="+(flag?"1":"0");
		}
		else{
			document.forms[0].action = "diggcfg!debug.action";
		}
		document.forms[0].method = "POST";
		document.forms[0].target = "i-editdebug";
		document.forms[0].submit();
	}
	catch(e)
	{
		skit_alert(e);
	}
}
//设置模板调试器uiMenuDebugSubgrid
function addSubgridDebugMenu(key, label, subgridobj)
{
	try{
		var id = "liSubgridDebugMenu"+key;
		if(document.getElementById(id)){
			return;
		}
		document.getElementById("liMenuDebugSubgrid").style.display = "";
		document.getElementById("liDividerMenuDebugInnerGrid").style.display = "";
		var uiMenuDebugSubgrid = document.getElementById("uiMenuDebugSubgrid");
		var colModel = subgridobj.colModel;
		var li = document.createElement("li");
		li.id = id;
		li.className = "dropdown-submenu";
		var html = "<a title='' href='javascript:;'><i class='skit_fa_icon fa fa-external-link fa-fw'></i>"+label+"</a>"+
			"<ul class='dropdown-menu'>"+
	    	"<li><a title='' onclick='editJavascript(11, this, \""+key+"\", \""+label+"\")'><i class='skit_fa_icon fa fa-edit fa-fw'></i>编辑数据预处理脚本</a></li>";
		var rhtml = "";
	   	if( colModel )
	   	{
	   		var r, i;
	   		//将模板的单元格渲染器添加到菜单
	   		for(i = 0; i < colModel.length; i++){
	   			var col = colModel[i];
   				r = col.render;
   				if( r ){
   					var title = label+"["+col.title+"]";
   					rhtml += "<li><a title='' onclick='editJavascript(12, this, \""+key+"\", "+i+");'><i class='skit_fa_icon fa fa-magic fa-fw'></i>"+title+"</a></li>";
   				}
	   		}
	   	}
	   	if( rhtml ){
	   		html += "<li class='dropdown-submenu'>"+
	   			"<a title='' href='javascript:;'><i class='skit_fa_icon fa fa-edit fa-fw'></i>编辑单元格渲染器脚本</a>"+
				"<ul class='dropdown-menu'>"+rhtml+"</ul></li>";
	   	}
	   	html += "<li class='divider'></li>";
	   	html += "<li><a title='' onclick='viewObject(10, this, \""+key+"\", \""+label+"\")'><i class='skit_fa_icon fa fa-info fa-fw'></i>查看配置对象</a></li>";
	   	html += "<li><a title='' onclick='viewObject(11, this, \""+key+"\", \""+label+"\")'><i class='skit_fa_icon fa fa-code fa-fw'></i>查看数据对象</a></li>";
	   	html += "</ul>";
		li.innerHTML = html;
		uiMenuDebugSubgrid.appendChild(li);
	}
	catch(e){
		alert(e);
	}
}

function addRenderDebugMenu(title, i, j)
{
	document.getElementById("liMenuDebugRender").style.display = "";
	var id = "liMenuDebugRender"+i+"_"+j;
	if( document.getElementById(id) ){
		return;
	}
	var ulMenuDebugRender = document.getElementById("ulMenuDebugRender");
	var li = document.createElement("li");
	li.id = id;
	li.innerHTML = "<a onclick='editJavascript(2,this, "+i+","+j+");'><i class='skit_fa_icon fa fa-code fa-fw'></i>"+title+"</a>";
	ulMenuDebugRender.appendChild(li);
}

function addEditorDebugMenu(title, i, j)
{
	document.getElementById("liMenuDebugEditor").style.display = "";
	var id = "liMenuDebugEditor"+i+"_"+j;
	if( document.getElementById(id) ){
		return;
	}
	var ulMenuDebugEditor = document.getElementById("ulMenuDebugEditor");
	var li = document.createElement("li");
	li.id = id;
	li.innerHTML = "<a onclick='editJavascript(4, this, "+i+","+j+");'><i class='skit_fa_icon fa fa-code fa-fw'></i>"+title+"</a>";
	ulMenuDebugEditor.appendChild(li);
}

function addEditorInitDebugMenu(title, i, j)
{
	document.getElementById("liMenuDebugEditorInit").style.display = "";
	var id = "liMenuDebugEditorInit"+i+"_"+j;
	if( document.getElementById(id) ){
		return;
	}
	var ulMenuDebugEditorInit = document.getElementById("ulMenuDebugEditorInit");
	var li = document.createElement("li");
	li.id = id;
	li.innerHTML = "<a onclick='editJavascript(4, this, "+i+","+j+");'><i class='skit_fa_icon fa fa-code fa-fw'></i>"+title+"</a>";
	ulMenuDebugEditorInit.appendChild(li);
}

function addEditableDebugMenu(title, i, j)
{
	document.getElementById("liMenuDebugEditable").style.display = "";
	var id = "liMenuDebugEditable"+i+"_"+j;
	if( document.getElementById(id) ){
		return;
	}
	var ulMenuDebugEditable = document.getElementById("ulMenuDebugEditable");
	var li = document.createElement("li");
	li.id = id;
	li.innerHTML = "<a onclick='editJavascript(5,this, "+i+","+j+");'><i class='skit_fa_icon fa fa-code fa-fw'></i>"+title+"</a>";
	ulMenuDebugEditable.appendChild(li);
}
function viewObject(type, li, i, j){
	var ifr = window.frames["i-editdebug"];
	if( ifr && ifr.viewObject )
	{
		ifr.viewObject(type, li.innerHTML, i, j);
	}
}

function editJavascript(type, li, i, j){
	var ifr = window.frames["i-editdebug"];
	if( ifr && ifr.editJavascript )
	{
		ifr.editJavascript(type, li.innerHTML, i, j);
	}
}
function showDebuglog(all){
	document.getElementById("liDigglog").style.display = all?"":"none";
}
function showDebugexport(all){
	document.getElementById("liDiggexport").style.display = all?"":"none";
}

function closeTemplateEdit()
{
	try
	{
		document.getElementById("liMenuDebugEditorInit").style.display = "none";
		document.getElementById("liMenuDebugEditable").style.display = "none";
		document.getElementById("liMenuDebugEditor").style.display = "none";
		document.getElementById("liMenuDebugRender").style.display = "none";
		
		document.getElementById("liMenuDebugSubgrid").style.display = "none";
		document.getElementById("liDividerMenuDebugInnerGrid").style.display = "none";
		document.getElementById("liDigglog").style.display = "none";
		document.getElementById("liDiggexport").style.display = "none";
		document.getElementById("divDebugObjectScript").style.display = "none";
		var ulMenuDebugRender = document.getElementById("ulMenuDebugRender");
		ulMenuDebugRender.innerHTML = "";
		var uiMenuDebugSubgrid = document.getElementById("uiMenuDebugSubgrid");
		uiMenuDebugSubgrid.innerHTML = "";
		var panel = document.getElementById("div-editdebug");
		document.forms[0].removeChild(panel);
		document.getElementById("divMask").style.visibility = "hidden";
	}
	catch(e)
	{
		alert("关闭窗口出现异常"+e.message+", 行数"+e.lineNumber);
	}
}

function removeFunction(javascript, name){
	var i, j;
	try{
		i = javascript.indexOf(name);
		if( i != -1 ){
			i = javascript.lastIndexOf("function", i);
			if( i == -1 ){
				timeout_alert("模板脚本格式错误，脚本区发现‘"+name+"’函数未正确定义", "错误提示");
				return null;
			}
			j = javascript.indexOf("function", i+1);
			if( j == -1 ){
				j = javascript.lastIndexOf("}");
			}
			else{
				j = javascript.lastIndexOf("}", j);
			}
			if( j == -1 ){
				timeout_alert("模板脚本格式错误，脚本区发现‘"+name+"’函数未正确闭合", "错误提示");
				return null;
			}
			//删掉了原来定义的callbackBeforeCommit函数，这样可以由新函数替换
			javascript = javascript.substring(0,i)+javascript.substring(j+1);
			javascript = javascript.replace(/(^\s*)|(\s*$)/g, "");
		}
		return javascript;
	}
	catch(e){
		timeout_alert("从脚本中删除函数("+name+")出现异常"+e);
		return null;
	}
}

//设置参数提交回调脚本
function setJavascriptEditCallback(js){
	var xml = getDiggXmlEditorValue();
	var xml0, xml1;
	var javascript = "";
	var i = xml.indexOf("<javascript>");
	var j = xml.indexOf("</javascript>");
	if( i != -1 && j != -1 ){
		try{
			i = xml.indexOf("<![CDATA[", i);
			if( i == -1 ){
				timeout_alert("模板脚本格式错误，全局脚本区未发现‘<![CDATA[’标签", "错误提示");
				return;
			}
			var k = xml.indexOf("]]>", i);
			if( k == -1 || k > j ){
				timeout_alert("模板脚本格式错误，全局脚本区未发现‘]]>’标签", "错误提示");
				return;
			}
			i += "<![CDATA[".length;
			xml0 = xml.substring(0, i);
			javascript = xml.substring(i, k);
			xml1 = xml.substring(k);
			javascript = removeFunction(javascript, "callbackBeforeCommit");
			if( javascript == null ){
				return;
			}
			javascript = removeFunction(javascript, "callbackSave");
			if( javascript == null ){
				return;
			}
			xml = xml0 + "\r\n"+javascript +"\r\n"+xml1;
			var i = xml.indexOf("</javascript>");
			j = xml.lastIndexOf("]]>", i);
			if( xml.charAt(j-1) == '\t' ){
				j -= 1;
			}
			i = j;
			javascript = js;
		}
		catch(e){
			timeout_alert("解析模板脚本异常:"+e, "错误提示");
			return;
		}
	}
	else{
		javascript= "\t<javascript>"+
        	"\r\n\t<![CDATA[\r\n"+
        	js+
        	"\r\n\t]]>"+
        	"\r\n\t</javascript>";
		i = xml.lastIndexOf("</x>");
		j = i;
	}
	xml0 = xml.substring(0, i);
	xml1 = xml.substring(j);
	xml = xml0 + "\r\n"+javascript +"\r\n"+ xml1;
	setDiggXmlEditorValue(xml);
	timeout_alert("正确设置编辑模板callbackBeforeCommit, callbackSave回调脚本，调测界面已刷新。");
	closeTemplateEdit();
	previewTemplateEdit();
}
//设置数据模型处理回调脚本
function setJavascriptDatamodel(js){
	var xml = getDiggXmlEditorValue();
	var xml0, xml1;
	var javascript = "";
	var i = xml.indexOf("<javascript>");
	var j = xml.indexOf("</javascript>");
	if( i != -1 && j != -1 ){
		try{
			i = xml.indexOf("<![CDATA[", i);
			if( i == -1 ){
				timeout_alert("模板脚本格式错误，主表数据预处理用户自定义脚本区未发现‘<![CDATA[’标签", "错误提示");
				return;
			}
			var k = xml.indexOf("]]>", i);
			if( k == -1 || k > j ){
				timeout_alert("模板脚本格式错误，主表数据预处理用户自定义脚本区未发现‘]]>’标签", "错误提示");
				return;
			}
			i += "<![CDATA[".length;
			j = k;
			javascript = js;
		}
		catch(e){
			timeout_alert("解析模板脚本异常:"+e, "错误提示");
			return;
		}
	}
	else{
		javascript= "\t<javascript>"+
        	"\r\n\t<![CDATA[\r\n"+
        	js+
        	"\r\n\t]]>"+
        	"\r\n\t</javascript>";
		i = xml.lastIndexOf("</x>");
		j = i;
	}
	xml0 = xml.substring(0, i);
	xml1 = xml.substring(j);
	xml = xml0 + "\r\n"+javascript +"\r\n"+ xml1;
	setDiggXmlEditorValue(xml);
	closeTemplateEdit();
	previewTemplateEdit();
}
//设置表格显示之前回调脚本
function setJavascriptBeforeGridView(js){
	var xml = getDiggXmlEditorValue();
	var xml0, xml1;
	var javascript = "";
	var i = xml.indexOf("<beforeGridView>");
	var j = xml.indexOf("</beforeGridView>");
	if( i != -1 && j != -1 ){
		try{
			i = xml.indexOf("<![CDATA[", i);
			if( i == -1 ){
				timeout_alert("模板脚本格式错误，表格初始化前回调脚本区未发现‘<![CDATA[’标签", "错误提示");
				return;
			}
			var k = xml.indexOf("]]>", i);
			if( k == -1 || k > j ){
				timeout_alert("模板脚本格式错误，表格初始化前回调脚本区未发现‘]]>’标签", "错误提示");
				return;
			}
			i += "<![CDATA[".length;
			j = k;
			javascript = js;
		}
		catch(e){
			timeout_alert("解析模板脚本异常:"+e, "错误提示");
			return;
		}
	}
	else{
		javascript= "\t<beforeGridView>"+
        	"\r\n\t<![CDATA[\r\n"+
        	js+
        	"\r\n\t]]>"+
        	"\r\n\t</beforeGridView>";
		i = xml.lastIndexOf("</x>");
		j = i;
	}
	xml0 = xml.substring(0, i);
	xml1 = xml.substring(j);
	xml = xml0 + "\r\n"+javascript +"\r\n"+ xml1;
	setDiggXmlEditorValue(xml);
	closeTemplateEdit();
	previewTemplateEdit();
}
//设置工具栏按钮脚本
function setJavascriptToolbar(js, subject){
	var xml = getDiggXmlEditorValue();
	var i = xml.indexOf("<toolbar>");
	var j = xml.indexOf("</toolbar>");
	var xml0, xml1;
	if( i == -1 || j == -1 ){
		timeout_alert("模板配置错误，没有定义toolbar", "错误提示");
		return;
	}
	i = xml.indexOf("subject='"+subject+"'");
	if( i == -1 ){
		timeout_alert("模板配置错误，没有找到您要配置的按钮【"+subject+"】", "错误提示");
		return;
	}
	i = xml.indexOf(">");
	j = xml.indexOf("</button>");
	xml0 = xml.substring(0, i);
	xml1 = xml.substring(j);
	xml = xml0 + "\r\n\t\t<![CDATA[\r\n"+js +"\r\n\t\t]]>\r\n"+ xml1;
	setDiggXmlEditorValue(xml);
	closeTemplateEdit();
	previewTemplateEdit();
}
//设置渲染器脚本
function setJavascriptRender(js, title, dataIndx){
	alert(title+","+dataIndx+":"+js);
}

function previewTemplateData()
{
	var node = getNode();
	if( !node.demo )
	{
		if( !node.check )
		{
			skit_alert("该模板还未完成检测，不能被预览");return;
		}
		if( node.check && node.check.err > 0 )
		{
			skit_alert("该模板检测发现有不可忽略的错误，请完成修改后再预览");return;
		}
	}
	var tips = "元数据查询模板真实数据测试预览(数据不可修改，数据不可下载，记录操作日志)";
	if( "edit" == node.type ) tips = "元数据配置模板真实数据测试预览(数据不可修改，数据不可下载，记录操作日志)";
	openView(tips+":【"+node.name+"】"+node.title, "diggcfg!debug.action?gridxml="+node.id);
}

function previewTemplateConfig()
{
	skit_alert("该功能暂不开放");return;
}

var newTemplateJson;
function addQueryTemplate(json)
{
	var table = getNode();
	var parent = table.getParentNode();
	newTemplateJson = json;
	document.getElementById( 'db' ).value = parent.id;
	document.getElementById( 'datatype' ).value = table.name;
	openPrecreate("query");
}
//编辑
function doEdit(visualize)
{
	var node = getNode();
	if( visualize )
	{
		skit_alert("可视化编辑暂不开放。");
//		var title = "";
//		if( node.type == "edit" )
//		{
//			title = "元数据配置模板编辑器";
//		}
//		else
//		{
//			title = "元数据查询模板编辑器";
//		}
//		title += ": "+node.name+"("+node.id+")";
//		openView(title, "diggcfg!preset.action?sysid="+sysid+"&gridxml="+node.id);
	}
	else
	{
		var id = parent.openNewView();
		window.setTimeout("delayOpenTemplate('"+id+"','"+node.id+"')",500);
		//openView("打开元数据模板开发", "diggcfg!explorer.action?mode=1&sysid="+sysid);
	}
}

function delayOpenTemplate(id, templateid){
	window.top.executeByView(id, function(ifr){
		if(ifr && ifr.openTemplate ){
			ifr.openTemplate(templateid);
		}
		else{
			window.setTimeout("delayOpenTemplate('"+id+"','"+templateid+"')",500);
		}
	});
}
//创建元数据查询模板配置
function presetQueryTemplate()
{
	hideRMenu();
	document.getElementById( 'db' ).value = "";
	openPrecreate("query");
}

function presetConfigTemplate()
{
	hideRMenu();
	skit_alert("暂不开放创建配置模板");return;
	document.getElementById( 'db' ).value = "";
	openPrecreate("edit");
}

var zIndex = 10000;
function openPrecreate(type)
{
	document.getElementById( 'filetype' ).value = type;
	try
	{
		var div = document.createElement("div");
		div.id = "div-precreate";
		div.className = "panel panel-primary";
		div.style.marginTop = 3;
		div.style.marginLeft = 3;
		div.style.position = "absolute";
		div.style.width = windowWidth - 10;
		var div1 = document.createElement("div");
		div1.className = "panel-heading";
		div1.style.height = 28;
		div.appendChild(div1);
		var div10 = document.createElement("div");
		div10.style.float = "left";
		div1.appendChild(div10);
		var span10 = document.createElement("span");
		span10.className = "panel-title";
		span10.innerHTML = "<i class='fa fa-copy'></i> 创建元数据查询模板";
		if( "edit" == type )
			span10.innerHTML = "<i class='fa fa-copy'></i> 创建元数据配置模板";
		span10.style.width = 512;
		div10.appendChild(span10);
		var div11 = document.createElement("div");
		div11.className = "panel-menu";
		div11.innerHTML = 
			"<button type='button' onclick='savePrecreate()' class='btn btn-success btn-action btn-xs' style='height:20px;margin-right:4px;'><i class='fa fa-save'></i>保存</button>"+
			"<button type='button' onclick='closePrecreate()' data-action='close' class='btn btn-warning btn-action btn-xs'><i class='fa fa-times'></i></button>";
		div1.appendChild(div11);
		var div2 = document.createElement("div");
		div2.className = "panel-body";
		div2.innerHTML = "<iframe name='i-precreate' id='i-precreate' class='nonicescroll' style='width:100%;border:0px solid red;'></iframe>";
		div.appendChild(div2);
		document.forms[0].appendChild(div);
		div.style.left = 3;
		div.style.top = 32;
		div.style.zIndex = zIndex++;
		document.getElementById("divMask").style.visibility = "visible";
		document.getElementById("divMask").style.width = windowWidth;
		document.getElementById("divMask").style.height = windowHeight;
		document.getElementById("i-precreate").style.height = windowHeight - 100;

		document.forms[0].action = "diggcfg!precreate.action";
		document.forms[0].method = "POST";
		document.forms[0].target = "i-precreate";
		document.forms[0].submit();
	}
	catch(e)
	{
		skit_alert(e);
	}	
}

function createTemplate(template)
{
	if( template == null ) return;
	//alert(template.path);
	var parent = myZtree.getNodeByParam("id", template.path); 
	/*var node = getNode();
	if( node.type == "dir" && !node.demo )
	{
		parent = node;
	}
	else if( node.template && !node.demo )
	{
		parent = node.getParentNode();
	}
	if( !parent )
	{
		parent = myZtree.getNodeByParam("id", "/cos/config/moduels/"+sysid+"/digg"); 
	}*/
	try
	{

	    var title = template.title;
		var id = parent.id + "/"+template.id+".xml";
		var children = parent.children;
		if( children && children.length > 1 )
			for(var i = 0; i < children.length; i++)
			{
				var child = children[i];
				if( child.type == "dir" ) continue;
				if( child.title == id )
				{
					skit_alert("同级目录元数据查询配置模板唯一标识("+id+")不能相同.");
					return;
				}
				if( child.name == title )
				{
					skit_alert("同级目录元数据查询配置模板名称("+title+")不能相同.");
					return;
				}
			}
		var json = JSON.stringify(template);
		DiggConfigMgr.createQueryTemplate(json, {
			callback:function(rsp) {
				if( rsp.succeed ) {
					var newTeplate = new Object();
					newTeplate["id"] = id;
					//alert(id);
					newTeplate["name"] = title;
					newTeplate["cname"] = title;
					newTeplate["title"] = template.id+".xml";
					newTeplate["type"] = template.type;
					newTeplate["newTemplate"] = true;
					newTeplate["editmode"] = "xml";
					newTeplate["template"] = true;
					newTeplate["xml"] = rsp.result;
					newTeplate["icon"] = "images/icons/new_item.png";
					addNewTemplate(newTeplate, rsp.result, parent);
					open(newTeplate);
					closePrecreate();
				}
				else skit_error(rsp.message);
			},
			timeout:30000,
			errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
		});
	}
	catch(e)
	{
		skit_alert("创建新编辑的模板出现异常"+e.message+", 行数"+e.lineNumber);
	}
}

function savePrecreate()
{
	var ifr = window.frames["i-precreate"];
	if( ifr && ifr.createTemplate )
	{
		template = ifr.createTemplate();
	}
}

function closePrecreate()
{
	try
	{
		var panel = document.getElementById("div-precreate");
		document.forms[0].removeChild(panel);
		document.getElementById("divMask").style.visibility = "hidden";
	}
	catch(e)
	{
		alert("关闭窗口出现异常"+e.message+", 行数"+e.lineNumber);
	}
}

function getDiggXmlEditorValue(){
	var template = getNode();
	var ifr = window.frames[template.id];
    if( ifr && ifr.getValue )
    {
    	return ifr.getValue();
    }
    return "";
}

function setDiggXmlEditorValue(xml){
	var template = getNode();
	var ifr = window.frames[template.id];
    if( ifr && ifr.setValue )
    {
    	ifr.setValue(xml);
    }
}

function remarkCell(column, remark)
{
	var ds = document.getElementById( 'db' ).value;
	var tablename = document.getElementById( 'datatype' ).value;
//	alert(sysid+"."+ds+"."+tablename+"."+column+"."+remark);
	DiggConfigMgr.remarkDatasourceCell(sysid, ds, tablename, column, remark, {
		callback:function(rsp) {
			skit_hiddenLoading();
			if( !rsp ) skit_alert("备注数据源元数据失败");
		},
		timeout:30000,
		errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
	});
}

function getNode()
{
	var nodes = myZtree.getSelectedNodes();
	return nodes[0];
}

function addTemplateDir()
{
	var node = getNode();
	//var path = node.id;
	hideRMenu();
	var newNode = new Object();
	newNode["id"] = "";
	newNode["name"] = node.name+"子目录";
	newNode["cname"] = node.name+"子目录";
	newNode["type"] = "dir";
	newNode["isParent"] = true;
	newNode["iconClose"] = "images/icons/folder_closed.png";
	newNode["iconOpen"] = "images/icons/folder_opened.png";
	var nodes = myZtree.addNodes(node, 0, newNode);
	myZtree.editName(nodes[0]);
}

function delTemplate()
{
	var node = getNode();
	hideRMenu();
	var tips = node.type=="dir"?("您确定要删除元数据模板【"+node.name+"】目录吗？"):("您确定要删除元数据模板【"+node.name+"】吗？");
	skit_confirm(tips, function(yes){
		if( yes )
		{
			DiggConfigMgr.delTemplate(sysid, node.id, {
				callback:function(rsp) {
					if( rsp.succeed ){
						var parent = node.getParentNode();
						if(node.template){
							if(removeTab(node.id)){
								myZtree.removeNode(node);
								return;
							}
						}
						myZtree.removeNode(node);
						myZtree.selectNode(parent, false, false);
						open(parent);
					}
					else skit_error(rsp.message);
				},
				timeout:30000,
				errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
			});
		}
	});	
}

function copyTemplateDir()
{
	var node = getNode();
	var path = node.path?node.path:"";
	hideRMenu();
//	alert(sysid+"."+ds+"."+tablename+"."+column+"."+remark);
	DiggConfigMgr.copyTemplateDir(sysid, path, {
		callback:function(rsp) {
			if( rsp.succeed ){
				var newNode = jQuery.parseJSON(rsp.result);
				var nodes = myZtree.addNodes(node, newNode);
				myZtree.selectNode(nodes[0], false, false);
				open(nodes[0]);
			}
			else skit_error(rsp.message);
		},
		timeout:30000,
		errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
	});
}

function querySql()
{
	hideRMenu();
	var node = getNode();
	var parent = node.getParentNode();
	var src = "/cos/config/modules/"+sysid+"/datasource/"+parent.id;
	var sql = 'select * from '+node.name;
	openView("SQL查询分析器:【"+node.name+"】"+src, "helper!sqlquery.action?db="+src+"&sql="+sql);
}

function testTemplate()
{
	presaveTemplate(true);
}

function cancelSaveTemplate()
{
	document.getElementById( 'tdTree' ).style.display = "";
	var node = getNode();
	removeTab(node.id);
}

function doTest(mode)
{
	var node = getNode();
	DiggConfigMgr.getTemplateXml(node.id, {
		callback:function(rsp) {
			if( rsp.succeed ) {
				if( rsp.message ) skit_alert(rsp.message);
				document.getElementById( 'content' ).value = rsp.result;
				var iDigg = document.getElementById( 'iDigg' );
				var divDiggProfile = document.getElementById( 'divDiggProfile' );
				divDiggProfile.style.display = "none";
				iDigg.style.display = "";
//				document.getElementById( 'divAddQuery' ).style.display = "none";
//				document.getElementById( 'divAddConfig' ).style.display = "none";
//				document.getElementById( 'divSave' ).style.display = "none";
//				document.getElementById( 'divCancel' ).style.display = "none";
//				document.getElementById( 'divDebug' ).style.display = "none";
//				document.getElementById( 'divPreview' ).style.display = "none";
				document.forms[0].method = "POST";
				document.forms[0].action = "diggcfg!test.action";
				document.forms[0].target = "iDigg";
				yaoSave = false;
				document.forms[0].submit();
				resizeWindow();
			}
			else skit_error(rsp.message);
		},
		timeout:30000,
		errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
	});
	
}

var yaoSave = false;
function presaveTemplate(showEdit)
{
	try
	{
		var div = document.createElement("div");
		div.id = "div-presavetest";
		div.className = "panel panel-primary";
		div.style.marginTop = 3;
		div.style.marginLeft = 3;
		div.style.position = "absolute";
		div.style.width = windowWidth - 10;
		var div1 = document.createElement("div");
		div1.className = "panel-heading";
		div1.style.height = 28;
		div.appendChild(div1);
		var div10 = document.createElement("div");
		div10.style.float = "left";
		div1.appendChild(div10);
		var span10 = document.createElement("span");
		span10.className = "panel-title";
		span10.innerHTML = "<i class='fa fa-eye'></i> 模板检测";
		span10.style.width = 512;
		div10.appendChild(span10);
		var div11 = document.createElement("div");
		div11.className = "panel-menu";
		div11.innerHTML = 
			"<button type='button' onclick='closePresaveTemplate()' data-action='close' class='btn btn-warning btn-action btn-xs'><i class='fa fa-times'></i></button>";
		div1.appendChild(div11);
		var div2 = document.createElement("div");
		div2.className = "panel-body";
		div2.innerHTML = "<iframe name='i-presavetest' id='i-presavetest' class='nonicescroll' style='width:100%;border:0px solid red;'></iframe>";
		div.appendChild(div2);
		document.forms[0].appendChild(div);
		div.style.left = 3;
		div.style.top = 32;
		div.style.zIndex = zIndex++;
		document.getElementById("divMask").style.visibility = "visible";
		document.getElementById("divMask").style.width = windowWidth;
		document.getElementById("divMask").style.height = windowHeight;
		document.getElementById("divMask").onclick = new Function("closePresaveTemplate();");
		document.getElementById("i-presavetest").style.height = windowHeight - 100;
		document.getElementById("gridtext").value = getDiggXmlEditorValue();

		yaoSave = showEdit?false:true;
		document.forms[0].method = "POST";
		document.forms[0].action = "diggcfg!pretest.action";
		document.forms[0].target = "i-presavetest";
		document.forms[0].submit();
	}
	catch(e)
	{
		skit_alert(e);
	}
}

function closePresaveTemplate()
{
	try
	{
		var panel = document.getElementById("div-presavetest");
		if( panel ){
			document.forms[0].removeChild(panel);
			document.getElementById("divMask").style.visibility = "hidden";
		}
	}
	catch(e)
	{
		alert("关闭窗口出现异常"+e.message+", 行数"+e.lineNumber);
	}
}

var templateComparing;
function compareTemplate(title, leftTitle, leftContent, rightTitle, rightContent, callback)
{
	if( templateComparing ) return;
	try
	{
		templateComparing = true;
		var div = document.createElement("div");
		div.id = "div-compare";
		div.className = "panel panel-primary";
		div.style.marginTop = 3;
		div.style.marginLeft = 3;
		div.style.position = "absolute";
		div.style.width = windowWidth - 10;
		var div1 = document.createElement("div");
		div1.className = "panel-heading";
		div1.style.height = 28;
		div.appendChild(div1);
		var div10 = document.createElement("div");
		div10.style.float = "left";
		div1.appendChild(div10);
		var span10 = document.createElement("span");
		span10.className = "panel-title";
		span10.innerHTML = "<i class='fa fa-eye'></i> "+title;
		span10.style.width = 512;
		div10.appendChild(span10);
		var div11 = document.createElement("div");
		div11.className = "panel-menu";
		div11.innerHTML = 
			"<button type='button' onclick='closeCompareTemplate()' data-action='close' class='btn btn-warning btn-action btn-xs'><i class='fa fa-times'></i></button>";
		div1.appendChild(div11);
		var div2 = document.createElement("div");
		div2.className = "panel-body";
		div2.innerHTML = "<iframe name='i-compare' id='i-compare' class='nonicescroll' style='width:100%;border:0px solid red;'></iframe>";
		div.appendChild(div2);
		var h = 100;
		if( callback ){
			h += 48;
			
			var div3 = document.createElement("div");
			div3.style.paddingLeft = windowWidth/2 - 96;
			div3.style.marginBottom = 10;
			div.appendChild(div3);
			var btn = document.createElement("button");
			btn.className = "btn btn-success btn-action btn-xs";
			btn.innerHTML = "<i class='fa fa-sign-in'></i> 确定";
			div3.appendChild(btn);
			btn.type = "button";
			btn.onclick = callback;
			
			btn = document.createElement("button");
			btn.type = "button";
			btn.style.marginLeft = 10;
			btn.className = "btn btn-default btn-action btn-xs";
			btn.innerHTML = "<i class='fa fa-sign-out'></i> 取消";
			div3.appendChild(btn);
			btn.onclick = closeCompareTemplate;
		}
		document.forms[0].appendChild(div);
		div.style.left = 3;
		div.style.top = 32;
		div.style.zIndex = zIndex++;
		document.getElementById("divMask").style.visibility = "visible";
		document.getElementById("divMask").style.width = windowWidth;
		document.getElementById("divMask").style.height = windowHeight;
		document.getElementById("divMask").onclick = new Function("closeCompareTemplate();");
		document.getElementById("i-compare").style.height = windowHeight - h;
		document.getElementById("compareLeftTitle").value = leftTitle;
		document.getElementById("compareRightTitle").value = rightTitle;
		document.getElementById("compareLeft").value = leftContent;
		document.getElementById("compareRight").value = rightContent;
		document.forms[0].method = "POST";
		document.forms[0].action = "editor!compare.action";
		document.forms[0].target = "i-compare";
		document.forms[0].submit();
	}
	catch(e)
	{
		skit_alert(e);
	}
}

function closeCompareTemplate()
{
	templateComparing = false;
	try
	{
		var panel = document.getElementById("div-compare");
		if( panel ){
			document.forms[0].removeChild(panel);
			document.getElementById("divMask").style.visibility = "hidden";
			document.getElementById("compareLeftTitle").value = "";
			document.getElementById("compareRightTitle").value = "";
			document.getElementById("compareLeft").value = "";
			document.getElementById("compareRight").value = "";
		}
	}
	catch(e)
	{
		alert("关闭窗口出现异常"+e.message+", 行数"+e.lineNumber);
	}
}

function finishTest(tips, err, war)
{
	var node = getNode();
	var old_err = 0, old_war = 0;
	if( !node.check ) node.check = {};
	else {
		old_err = node.check.err;
		old_war = node.check.war;
	}
	node.check.err = err;
	node.check.war = war;
	err -= old_err;
	war -= old_war;
	//alert("old:("+old_err+","+old_war+"), "+err+","+war);
	setParentCheckResult(node, err, war);
	setTemplateCheck(node);
	if( yaoSave )
	{
		if( err > 0 )
		{
			skit_confirm(tips+"您要继续完善编辑吗？", function(yes){
				if( yes )
				{
					closePresaveTemplate();
				}
				else
				{
					cancelSaveTemplate();
				}
			});
		}
		else
		{
			doSave();
		}
	}
}

var iVersion;
var iRemark;
function doSave()
{
	try
	{
		var node = getNode();
		if( !node.template ) return;
		var lastversion = node.version;
		var template = document.getElementById("divSaveTemplate").innerHTML;
		template = template.replace("#version#", "iVersion");
		template = template.replace("#lastversion#", "模板当前版本: "+lastversion);
		template = template.replace("#templateTitle#", "iTemplateTitle");
		template = template.replace("#versionremark#", "iRemark");
		var SM = new SimpleModal({"btn_ok":"确定","btn_cancel":"取消","width":440});
	    var content = "<div style='height:224px;width:400px;border:0px solid red;overflow-y:hidden;'>"+template+"</div>";
	    SM.show({
	    	"title":"<span class='panel-title' style='width:400px;'><i class='skit_fa_icon_blue fa fa-save'></i> 保存元数据模板［"+node.id+"］</span>",
	        "model":"confirm",
	        "callback": function(){
	        	var version = iVersion.value;
	            if( version == "" )
	            {
	        		$("#iVersion").tooltip("show");
	        		document.getElementById( "iVersion" ).focus();
	            	return false;
	            }
	            var regexp2 = "^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\."+
	            			   "(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\."+
	            			   "(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\."+
	            			   "(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$";
	            var m2 = version.match(new RegExp(regexp2));
	            if( !m2 )
	            {
	        		$("#iVersion").tooltip("show");
	        		iVersion.focus();
			        return false;
	            }
	        	var args1 = version.split(".");
	        	//var args0 = lastversion.split(".");
	        	var v1 = Number(args1[0])*1000000+Number(args1[1])*10000+Number(args1[2])*100+Number(args1[3]);
	        	//var v0 = Number(args0[0])*1000000+Number(args0[1])*10000+Number(args0[2])*100+Number(args0[3]);
	            if( v1 == 0 )
	            {
	        		$("#iVersion").tooltip("show");
	        		iVersion.focus();
	            	return false;
	            }

	            var remark = iRemark.value;
	            if( remark.length < 0 )
	            {
	        		$("#iRemark").tooltip("show");
	        		iRemark.focus();
	            	return false;
	            }
	        	var title = iTemplateTitle.value;
	            if( title == "" )
	            {
	        		$("#iTemplateTitle").tooltip("show");
	        		iTemplateTitle.focus();
	            	return false;
	            }
				try
				{
					var text = getDiggXmlEditorValue();
					var developer = document.getElementById( 'account' ).value;
					document.getElementById( 'version' ).value = version;
					document.getElementById( 'remark' ).value = remark;//特性描述
					document.getElementById( 'gridxml' ).value = node.id;
					document.getElementById( 'timestamp' ).value = node.timestamp;
					node.name = title;
					node.cname = title;
					node.developer = title;
					text = setXml(text, "title", title);
					text = setXml(text, "developer", developer);
					text = setXml(text, "version", version);
					document.getElementById("gridtext").value = text;
					document.forms[0].method = "POST";
					document.forms[0].action = "diggcfg!save.action";
					document.forms[0].target = "iDownload";
					document.forms[0].submit();
				}
				catch(e)
				{
					alert(e);
				}
	        	return true;
	        },
	        "cancelback": function(){
				closePresaveTemplate();
	        },
	    	"contents": content
	    });
	    var h = windowHeight;
	    var w = windowWidth;
	    var div = document.getElementById('simple-modal');
	    var top = h/2 - div.scrollHeight/2;
	    SM.options.offsetTop = top;
	    SM._display();
    	iRemark = document.getElementById("iRemark");
    	iRemark.value = node.versionremark?node.versionremark:"";
    	iRemark.onkeydown=function(event){ 
    		$("#iRemark").tooltip("hide");
    	}
    	iVersion = document.getElementById("iVersion");
    	iVersion.value = node.version?node.version:"";
    	document.getElementById("oldversion").value = node.version?node.version:"";
    	iVersion.focus();
    	iVersion.onkeydown=function(event){ 
    		$("#iVersion").tooltip("hide");
    	}

    	var userAgent = navigator.userAgent; //取得浏览器的userAgent字符串
    	var isSafari = userAgent.indexOf("Safari") > -1;
    	if( !isSafari ){
        	$("#iVersion").inputmask("mask", {"mask": "9.9.9.0"});
    	}
	    iTemplateTitle = document.getElementById("iTemplateTitle");
	    iTemplateTitle.value = node.name;
	    iTemplateTitle.onkeydown=function(event){ 
    		$("#iTemplateTitle").tooltip("hide");
    	}
	}
	catch(e)
	{
		alert(e);
	}
}

function finishSave(succeed, id, tips, ts, version)
{
	closePresaveTemplate();
	var template = getNode();
	var xml = document.getElementById("gridtext").value;
	if( succeed )
	{
	    template.timestamp = ts;
	    template.version = document.getElementById( 'version' ).value;
	    template.remark = document.getElementById( 'remark' ).value;	
		template.xmlmd5 = $.md5(xml);
		document.getElementById( 'tdTree' ).style.display = "";
		template.template = true;
		if( template.newTemplate ) delete template.newTemplate;
		document.getElementById( 'divCancel' ).style.display = "none";
		if( template.type == "edit" ) template.icon = "images/icons/drafts.png";
		else template.icon = "images/icons/search.png";
		myZtree.updateNode(template);
		var icofa = document.getElementById("icofa-"+template.id);
		if(icofa){
			icofa.className = "fa fa-file-code-o";
		}
		setDiggXmlEditorValue(xml);
		setTemplateEditorStatus(template);
		fullscreen = false;
		openVersions(template);
	}
	else
	{
		if( ts != template.timestamp && !template.newTemplate ){
			DiggConfigMgr.getTemplateXml(template.id, {
				callback:function(rsp) {
					if( rsp.succeed ) {
					    var ifr = window.frames[template.id];
						if( rsp.message ) skit_alert(rsp.message);
						compareTemplate(tips+" 您可以对版本进行合并",
							"<i class='fa fa-cloud-upload'></i> 已被其他用户修改的版本", rsp.result, 
							"<i class='fa fa-edit'></i> 编辑中的版本", xml,
						function(){
						    var icompare = window.frames["i-compare"];
						    var txt1 = icompare.getRight();
						    var txt0 = ifr.getValue();
							skit_confirm("您已经完成内容合并，确定保存吗？", function(yes){
								if( yes && ifr ) {
									template.timestamp = rsp.timestamp;//时间戳设为一致
									template.version = version;
									var m1 = $.md5(txt1);
									var m0 = $.md5(txt0);
									if( m1 != m0 ){
										ifr.setValue(txt1);
									}
									else{
										setTimeout("skit_alert('合并内容与编辑内容一致，或者您没有做合并操作')", 500);										
									}
									closeCompareTemplate();
							    }
							});
							return;
						});
					}
					else skit_error(rsp.message);
				},
				timeout:30000,
				errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
			});
		}
		else{
			skit_alert(tips, "保存模板失败");
		}
	}
}

function setXml(text, name, value)
{
	var start, end, i, j, c, str0, str1;
	start = text.indexOf("<x");
	end = text.indexOf(">", start);
	var tag = name+"=";
	i = text.lastIndexOf(tag, end);
	var ff = true;
	//alert("start:"+start+",end:"+end+", i="+i);
	if( i > start )
	{
		i += tag.length;
		c = text.charAt(i);
		j = text.indexOf(c, i+1);
		if( i <= j )
		{
			ff = false;
			str0 = text.substring(0, i+1);
			str1 = text.substring(j);
			text = str0+value+str1;
		}
	}
	if( ff )
	{
		attr = " "+name+"='"+value+"'";
		str0 =  text.substring(0, end);
		str1 = text.substring(end);
		text = str0+attr+str1;
	}
	return text;
}
/*
var aaa = "<x type='query' title='版权百科用户搜索日报' developer='admin'>";
aaa = setXml(aaa, "title", "aaa");
alert(aaa);
aaa = setXml(aaa, "remark", "bbb");
alert(aaa);
aaa = setXml(aaa, "version", "1.0.0.0");
alert(aaa);
*/

function doPublish()
{
	var node = getNode();
	if( node.newTemplate )
	{
		skit_alert("新建模板还未不能发布");
		return;
	}
	if( !node.check )
	{
		skit_alert("该元数据查询配置模板还没有测试，请先测试。");
		return;
	}
	if( node.check.err )
	{
		skit_alert("该元数据查询配置模板有"+node.check.err+"个严重错误，请按照测试结果的提示先解决模板中的问题后再发布。");
		return;
	}
	skit_confirm("将该元数据查询配置模板配置到系统后台菜单管理，通过权限控制提供给其它用户使用。您是否打开后台菜单管理", function(yes){
		if( yes )
		{
			var url = "digg!query.action?gridxml="+node.id;
			parent.publishMenu(node.name, url);
		}
	});		
}

function copyUrl()
{
	var node = getNode();
	var url = "digg!query.action?gridxml="+node.id;
	skit_input("模板链接地址，点击确定直接跳转到后台菜单管理进行菜单设置", url, function(yes, url){
		if( yes ){
			window.setTimeout("doPublish()",500);
		}
	});
}

var filename;
function preuploadTemplate()
{
	var node = getNode();
	hideRMenu();
	var divUploadfile = document.getElementById( 'divUploadfile' );
	divUploadfile.style.display = "";
	$("#uploadfile").fileinput({
			language: 'zh', //设置语言
			uploadUrl: "diggcfg!upload.action", //上传的地址
			allowedFileExtensions: ["xml"],//接收的文件后缀
			showUpload: true, //是否显示上传按钮
			showCaption: false,//是否显示标题
			showClose: false,
			showPreview: true,
			browseClass: "btn btn-primary", //按钮样式     
			previewSettings: { image: {width: "auto", height: "260"} },
			//dropZoneEnabled: false,//是否显示拖拽区域
			//maxFileSize: 0,//单位为kb，如果为0表示不限制文件大小
			//minFileCount: 0,
			maxFileCount: 1, //表示允许同时上传的最大文件个数
			enctype: 'multipart/form-data',
			validateInitialCount:true,
			previewFileIcon: "<i class='glyphicon glyphicon-king'></i>",
			msgFilesTooMany: "选择上传的文件数量({n}) 超过允许的最大数值{m}！",
            uploadExtraData: function(previewId, index) {   //额外参数的关键点
                var obj = {};
                obj.path = node.id;
                obj.gridxml = filename;
                return obj;
            }
	});
	document.getElementById( 'uploadtitle' ).innerHTML = "从本地磁盘选择上传元数据查询配置模板文件";
	divUploadfile.style.top = 64;//windowHeight/2 - divUploadfile.clientHeight*2/3;
	divUploadfile.style.left = windowWidth/2 - divUploadfile.clientWidth/2;
	$("#uploadfile").on("fileloaded", function (data, previewId, index) {
		var tips = "您确定要上传元数据查询配置模板["+previewId.name+"]吗？";
		document.getElementById( 'uploadtitle' ).innerHTML = tips;
		filename = previewId.name;
	});
	//导入文件上传完成之后的事件
	$("#uploadfile").on("fileuploaded", function (event, data, previewId, index) {
		if( data.response.alt )
		{
			skit_alert(data.response.alt);
		}
		finishUploadTemplate(node, data);
		document.getElementById( 'divUploadfile' ).style.display = "none";
		$("#uploadfile").fileinput("destroy");
	});
}

function finishUploadTemplate(node, data){
	if( data.response.succeed ) {
		var template = data.response.template;
		var oldNode = myZtree.getNodeByParam("id", template.id);
		if( oldNode == null ){
			var parent = node;
			addNewTemplate(template, data.response.xml, parent);
			open(template);
		}
		else {
			document.getElementById("content").value = data.response.xml;
			if(!openComparer(template.name, template.id)){
				skit_alert("您上传了模板【"+template.name+"】的新脚本配置，请通过模板脚本比较器与原版本比较，如果发现冲突，请合并后再开始您的新编辑。");
				open(oldNode, function(){
					document.getElementById("content").value = data.response.xml;
					window.setTimeout("openComparer('"+template.name+"', '"+template.id+"');", 2000);
				});
		    }
		}
	}
}

function openComparer(name, id){
	var xml = document.getElementById("content").value;
    var ifr = window.frames[id];
    if( ifr && ifr.getValue ) {
		compareTemplate("新上传的模板【"+name+"】内容与原内容比较，确认后更新编辑内容",
			"<i class='fa fa-cloud-upload'></i>上传的模板内容", xml, 
			"<i class='fa fa-edit'></i>编辑中的模板内容", ifr.getValue(),
		function(){
		    var icompare = window.frames["i-compare"];
		    var txt1 = icompare.getRight();
		    var txt0 = ifr.getValue();
			skit_confirm("您已经完成内容合并，确定复制到编辑器中开始您的编辑吗？", function(yes){
				if( yes && ifr ) {
					var m1 = $.md5(txt1);
					var m0 = $.md5(txt0);
					if( m1 != m0 ){
						ifr.setValue(txt1);
					}
					else{
						window.setTimeout("skit_alert('上传模板内容与编辑内容一致，或者您没有做合并操作')", 500);										
					}
					closeCompareTemplate();
			    }
			});
			return;
		});
		return true;
    }
    return false;
}
//对比
function doCompareTemplate(){
	var template = getNode();
	DiggConfigMgr.getTemplateXml(template.id, {
		callback:function(rsp) {
			if( rsp.succeed ) {
			    var ifr = window.frames[template.id];
				if( rsp.message ) skit_alert(rsp.message);
		    	compareTemplate("模板【"+template.name+"】编辑前后对比", 
		    		"<i class='fa fa-save'></i>正式的模板内容", rsp.result, 
		    		"<i class='fa fa-edit'></i>编辑中的模板内容", ifr.getValue());
			}
			else skit_error(rsp.message);
		},
		timeout:30000,
		errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
	});
}

function closeuploadfile()
{
	document.getElementById( 'divUploadfile' ).style.display = "none";
	$("#uploadfile").fileinput("destroy");
}

function downloadTemplate()
{
	hideRMenu();
	var node = getNode();
	if( node.newTemplate )
	{
		skit_alert("新建模板还未保存不能下载");
		return;
	}
	document.getElementById( 'gridxml' ).value = node.id;
	document.forms[0].method = "POST";
	document.forms[0].action = "diggcfg!download.action";
	document.forms[0].target = "iDigg";
	document.forms[0].submit();
}

function downloadTemplates()
{
	hideRMenu();
	document.forms[0].method = "POST";
	document.forms[0].action = "diggcfg!exporttemplates.action";
	document.forms[0].target = "iDigg";
	document.forms[0].submit();
}

//上传模板
function preuploadTemplates()
{
	hideRMenu();
	skit_confirm("执行模板导入会直接覆盖现有模板，为了确保不出错，请考虑先执行导出模板备份操作。<B>请确认是否执行导入？</B>", function(yes){
		if( yes )
		{
			var divUploadfile = document.getElementById( 'divUploadfile' );
			divUploadfile.style.display = "";
			$("#uploadfile").fileinput({
					language: 'zh', //设置语言
					uploadUrl: "diggcfg!importtemplates.action", //上传的地址
					allowedFileExtensions: ["zip"],//接收的文件后缀
					showUpload: true, //是否显示上传按钮
					showCaption: false,//是否显示标题
					showClose: false,
					showPreview: true,
					browseClass: "btn btn-primary", //按钮样式     
					previewSettings: { image: {width: "auto", height: "260"} },
					//dropZoneEnabled: false,//是否显示拖拽区域
					//maxFileSize: 0,//单位为kb，如果为0表示不限制文件大小
					//minFileCount: 0,
					maxFileCount: 1, //表示允许同时上传的最大文件个数
					enctype: 'multipart/form-data',
					validateInitialCount:true,
					previewFileIcon: "<i class='glyphicon glyphicon-king'></i>",
					msgFilesTooMany: "选择上传的文件数量({n}) 超过允许的最大数值{m}！",
		            uploadExtraData: function(previewId, index) {   //额外参数的关键点
		                var obj = {};
		                obj.sysid = sysid;
		                return obj;
		            }
			});
			document.getElementById( 'uploadtitle' ).innerHTML = "从本地磁盘选择导入元数据模板文件包(*.zip)";
			divUploadfile.style.top = 64;//windowHeight/2 - divUploadfile.clientHeight*2/3;
			divUploadfile.style.left = windowWidth/2 - divUploadfile.clientWidth/2;
			$("#uploadfile").on("fileloaded", function (data, previewId, index) {
				var tips = "您确定要导入元数据模板吗？";
				document.getElementById( 'uploadtitle' ).innerHTML = tips;
				filename = previewId.name;
				//if( node.id.indexOf(".xml") != -1 )	document.getElementById( 'gridxml' ).value = node.id;
				//else document.getElementById( 'gridxml' ).value = node.id+"/"+previewId.name;
			});
			//导入文件上传完成之后的事件
			$("#uploadfile").on("fileuploaded", function (event, data, previewId, index) {
				if( data.response.alt )
				{
					skit_alert(data.response.alt);
				}
				if( data.response.succeed )
				{
					if( parent && parent.open ){
						parent.open();
					}
					else {
						window.location.reload();
					}
				}
				document.getElementById( 'divUploadfile' ).style.display = "none";
				$("#uploadfile").fileinput("destroy");
			});
		}
	});
}

//添加新模板
var k = 10;
function addNewTemplate(template, xml, parent){
	template.newTemplate = true;
	template.template = true;
	template.name = template.cname;
	template.icon = "images/icons/new_item.png";
	template.xmlmd5 = $.md5(xml);
	var nodes = myZtree.addNodes(parent, 0, template);
	template = nodes[0];
	myZtree.selectNode(template, false, false);
	addTab(template, k++);
	var ic = document.getElementById( 'ico_abc_'+$.md5(template.id) );
	if( ic )
	{
		ic.className = "skit_fa_icon fa fa-edit";
		ic.title = "模板编辑中...";
	}
	$("#tabL").tabs('select', '#'+template.id);
	document.getElementById("content").value = xml;
	document.forms[0].action = "editor!xml.action";
	document.forms[0].method = "POST";
	document.forms[0].target = template.id;
	document.forms[0].submit();	
	return template;
}

function addTab(node, n){
	var id = node.id;
	var ulTab = document.getElementById( "ulTab" );
	var li = document.createElement("li");
	var title = getTabTile(node);
	li.id = "li-"+node.id;
	var ico = node.type != "dir" && "datasourcecfgs" != node.id ? "file-code-o" : "folder-open-o";
	if( node.type=="datasource" ){
		ico = "database";
	}
	else if( node.type=="help" ){
		ico = "question";
	}
	if( node.newTemplate ){
		ico = "<i class='fa fa-file' id='icofa-"+id+"'></i> ";
	}
	else{
		ico = "<i class='fa fa-"+ico+"'></i> ";
	}
	title = "<span id='a-"+id+"'>"+title+"</span>";
	if( n ){
		title = "<span id='a-"+id+"'>"+title+"("+n+")</span>";
	}
	li.innerHTML = "<a href='#"+id+"' title='"+id+"'>"+ico+title+" <i class='fa fa-remove' style='cursor:pointer' title='关闭选项卡' onclick='removeTab(\""+node.id+"\")'></i></a>";
	ulTab.appendChild(li);
	var i = document.createElement("iframe");
	i.name = id;
	i.id = id;
	i.className = "nonicescroll";
	i.style.width = "100%";
	i.style.border = "0px";
	i.style.display = "none";
	document.getElementById( "tabL" ).appendChild(i);
	$("#tabL").tabs("refresh");	
}

var iTemplateId;
var iTemplateTitle;
function copyTemplate()
{
	hideRMenu();
	try
	{
		var node = getNode();
		var parent = node.getParentNode();
		var template = document.getElementById("divCopyTemplate").innerHTML;
		template = template.replace("#templateId#", "iTemplateId");
		template = template.replace("#templateTitle#", "iTemplateTitle");
		var SM = new SimpleModal({"btn_ok":"确定","btn_cancel":"取消","width":520});
	    var content = "<div style='height:144px;width:480px;border:0px solid red;overflow-y:auto;'>"+template+"</div>"
	    SM.show({
	    	"title":"复制元数据模板［"+node.name+"］",
	        "model":"confirm",
	        "callback": function(){
	            var id = iTemplateId.value;
	            if( id == "" )
	            {
	        		$("#iTemplateId").tooltip("show");
	        		document.getElementById( "iTemplateId" ).focus();
	            	return false;
	            }
	    		var regexp1 = "^([A-Za-z])|([a-zA-Z0-9])|([a-zA-Z0-9])|([a-zA-Z0-9_])+$";
	            var m2 = id.match(new RegExp(regexp1));
	            if( !m2 )
	            {
	        		$("#iTemplateId").tooltip("show");
	        		document.getElementById( "iTemplateId" ).focus();
			        return false;
	            }
	        	var title = iTemplateTitle.value;
	            if( title == "" )
	            {
	        		$("#iTemplateTitle").tooltip("show");
	        		document.getElementById( "iTemplateTitle" ).focus();
	            	return false;
	            }
	            id = id + ".xml";
	            if( parent.children )
	            {
	            	var children = parent.children;
	            	for(var i = 0; i < children.length; i++)
	            	{
	            		var child = children[i];
	            		if( child.title == id )
	            		{
	    	        		$("#iTemplateId").tooltip("show");
	    	        		document.getElementById( "iTemplateId" ).focus();
			            	return false;
	            		}
	        			if( child.name == title )
	        			{
	    	        		$("#iTemplateTitle").tooltip("show");
	    	        		document.getElementById( "iTemplateTitle" ).focus();
			            	return false;
	        			}
	            	}
	            }
				try
				{
					DiggConfigMgr.getTemplateXml(node.id, {
						callback:function(rsp) {
							if( rsp.succeed ) {
								if( rsp.message ) skit_alert(rsp.message);
								var newNode = {};
								newNode["id"] = parent.id+"/"+id;
								newNode["title"] = id;
								newNode["name"] = title;
								newNode["ename"] = id;
								newNode["cname"] = title;
								newNode["type"] = node.type;
								newNode["newTemplate"] = true;
								newNode["datamodel"] = node.datamodel;
								newNode["icon"] = "images/icons/new_item.png";
								newNode["xml"] = rsp.result;
								addNewTemplate(newNode, rsp.result, parent);
							}
							else skit_error(rsp.message);
						},
						timeout:30000,
						errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
					});
				}
				catch(e)
				{
					alert(e);
				}
	        	return true;
	        },
	        "cancelback": function(){
	        },
	    	"contents": content
	    });
	    var h = windowHeight;
	    var w = windowWidth;
	    var div = document.getElementById('simple-modal');
	    var top = h/2 - div.scrollHeight/2;
	    SM.options.offsetTop = top;
	    SM._display();
	    iTemplateTitle = document.getElementById("iTemplateTitle");
	    iTemplateTitle.value = node.name;
	    iTemplateTitle.onkeydown=function(event){ 
    		$("#iTemplateTitle").tooltip("hide");
    	}
	    iTemplateId = document.getElementById("iTemplateId");
	    iTemplateId.value = node.title.substring(0, node.title.length-4);
	    iTemplateId.onkeydown=function(event){ 
    		$("#iTemplateId").tooltip("hide");
    	}
	    iTemplateId.focus();
	}
	catch(e)
	{
		alert(e);
	}
}
//预览版本的对比内容
function previewCompare(id, version, xml){
	if(!id){
		var template = getNode();
		id = template.id;
	}
	DiggConfigMgr.getTemplateXml(id, {
		callback:function(rsp) {
			if( rsp.succeed ) {
				if( rsp.message ) skit_alert(rsp.message);
		    	compareTemplate("版本【"+version+"】与当前最新版本的对比", 
		    		"<i class='fa fa-save'></i> 版本"+version, unicode2Chr(xml), 
		    		"<i class='fa fa-edit'></i> 最新版本", rsp.result);
			}
			else skit_error(rsp.message);
		},
		timeout:30000,
		errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
	});
}

function previewBackup(version, xml, name, id){
	try
	{
		var div = document.createElement("div");
		div.id = "div-previewbackup";
		div.className = "panel panel-primary";
		div.style.marginTop = 3;
		div.style.marginLeft = 3;
		div.style.position = "absolute";
		div.style.width = windowWidth - 10;
		var div1 = document.createElement("div");
		div1.className = "panel-heading";
		div1.style.height = 28;
		div.appendChild(div1);
		var div10 = document.createElement("div");
		div10.style.float = "left";
		div1.appendChild(div10);
		var span10 = document.createElement("span");
		span10.className = "panel-title";
		if( !name ){
			var template = getNode();
			name = template.name;
			id = template.id;
		}
		span10.innerHTML = "<i class='fa fa-eye'></i> 模板【"+name+"】备份【"+version+"】 "+id;
		span10.style.width = windowWidth - 128;
		div10.appendChild(span10);
		var div11 = document.createElement("div");
		div11.className = "panel-menu";
		div11.innerHTML = 
			"<button type='button' onclick='closePreviewBackup()' data-action='close' class='btn btn-warning btn-action btn-xs'><i class='fa fa-times'></i></button>";
		div1.appendChild(div11);
		var div2 = document.createElement("div");
		div2.className = "panel-body";
		div2.innerHTML = "<iframe name='i-previewbackup' id='i-previewbackup' class='nonicescroll' style='width:100%;border:0px solid red;'></iframe>";
		div.appendChild(div2);
		document.forms[0].appendChild(div);
		div.style.left = 3;
		div.style.top = 32;
		div.style.zIndex = zIndex++;
		document.getElementById("divMask").style.visibility = "visible";
		document.getElementById("divMask").style.width = windowWidth;
		document.getElementById("divMask").style.height = windowHeight;
		document.getElementById("divMask").onclick = new Function("closePreviewBackup();");
		document.getElementById("i-previewbackup").style.height = windowHeight - 100;
		document.getElementById("content").value = unicode2Chr(xml);
		document.forms[0].action = "editor!xml.action?t="+zIndex;
		document.forms[0].method = "POST";
		document.forms[0].target = "i-previewbackup";
		document.forms[0].submit();
	}
	catch(e)
	{
		skit_alert(e);
	}
}

function closePreviewBackup()
{
	try
	{
		var panel = document.getElementById("div-previewbackup");
		if( panel ){
			document.forms[0].removeChild(panel);
			document.getElementById("divMask").style.visibility = "hidden";
		}
	}
	catch(e)
	{
		alert("关闭窗口出现异常"+e.message+", 行数"+e.lineNumber);
	}
}

function viewVersions(timeline)
{
	var iDigg = document.getElementById( 'iDigg' );
	iDigg.style.display = "";
	resizeWindow();
	if( timeline ){

		var dataurl = "diggcfg!versiontimeline.action?id="+getNode().id;
		dataurl = chr2Unicode(dataurl);
		document.getElementById("iDigg").src = "helper!timeline.action?dataurl="+dataurl;
		return;
	}
	document.forms[0].method = "POST";
	document.getElementById("id").value = getNode().id;
	document.forms[0].action = "diggcfg!version.action";
	document.forms[0].target = "iDigg";
	document.forms[0].submit();
}
function tour()
{
	var showTour = getUserActionMemory("tourDiggMgr");
	if( showTour != "1" )
	{
		setUserActionMemory("tourDiggMgr", "1");
		var divTour = document.getElementById("divTour");
		divTour.style.display = "";
		divTour.style.zIndex = zIndex*3;
		divTour.style.left = 1;
		divTour.style.top = 28;
	}
}