//删除
function doDelete()
{
	var select_row = $(".pq-row-select");
	var val_sel = "";
	for(var i = 0; i < select_row.length; i++ )
	{
		var rowIndx = select_row[i].rowIndex - 1;
		var rowData = $grid.pqGrid("getRowData", {rowIndx: rowIndx});
		if(val_sel.length > 0 ) val_sel += ",";
		val_sel += rowData["_pk"];
	}
	if( val_sel == "" )
	{
		top.skit_alert("请选择您要删除的日志，日志被删除后将不可恢复。");
		return;
	}
	if(top&&top.skit_text)
	{
		top.skit_confirm('你确定删除选择的日志信息？',function(yes){
			if( yes ){
				var input = document.createElement("input");
				input.name = "logs";
				input.value = val_sel;
				input.type = "hidden";
				document.forms[0].appendChild(input);
				document.forms[0].target = "";
		        document.forms[0].action="syslog!doDelete.action";
		        document.forms[0].submit();
			}
		});
	}
}
//清除
function doClear()
{
	if(top&&top.skit_text)
	{
		top.skit_confirm('你确定清除所有的日志信息，清除后日志将不可再恢复？',function(yes){
			if( yes ){
            	document.forms[0].action="syslog!doClear.action";
				document.forms[0].target = "";
           		document.forms[0].submit();
			}
		});
	}
}