function queryServers()
{
	top.openView("查询微信公众号回调服务运行情况", "weixin!query.action");
}

function queryUsers()
{
	var select_row = $grid.pqGrid( "selection",{ type:'row', method:'getSelection' } );
	if( select_row.length != 1 ){
		top.skit_alert("请选择您要查询的公众账号配置");
		return;
	}
	var rowData = select_row[0]["rowData"];
	top.openView("查询微信公众号用户数据", "weixin!users.action?id="+rowData["weixinno"]);
}

function queryCallback()
{
	var select_row = $grid.pqGrid( "selection",{ type:'row', method:'getSelection' } );
	if( select_row.length != 1 ){
		top.skit_alert("请选择您要查询的公众账号配置");
		return;
	}
	var rowData = select_row[0]["rowData"];
	top.openView("查询微信公众号回调记录", "weixin!callbackquery.action?id="+rowData["weixinno"]);
}


function clearCallback()
{
	var select_row = $grid.pqGrid( "selection",{ type:'row', method:'getSelection' } );
	if( select_row.length != 1 ){
		top.skit_alert("请选择您要清除回调记录的公众账号配置");
		return;
	}
	var rowData = select_row[0]["rowData"];
	document.getElementById("id").value = rowData["weixinno"];
	document.forms[0].action = 'weixin!callbackclear.action';
	document.forms[0].target = "downloadframe";
	document.forms[0].method = "POST";
	document.forms[0].submit();
}


function configAppMenu()
{
	var select_row = $grid.pqGrid( "selection",{ type:'row', method:'getSelection' } );
	if( select_row.length != 1 ){
		top.skit_alert("请选择您要配置菜单的公众账号");
		return;
	}
	var rowData = select_row[0]["rowData"];
	top.openView("配置微信公众号自定义菜单", "weixin!presetmenu.action?id="+rowData["weixinno"]);
}