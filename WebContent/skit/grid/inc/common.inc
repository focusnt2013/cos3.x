<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
function pqDatePicker(ui, callback) {
	var $this = $(this);
    var dataIndx = ui?ui.dataIndx:"";
    var field = dataIndx?filterFields[dataIndx]:null;
    var dateFormat = field&&field.format?field.format:"yy-mm-dd";
    var c = $this.css({ zIndex: 3, position: "relative" });
    c.datepicker({
		dateFormat: dateFormat,
	    yearRange: "-100:+100", //20 years prior to present.
	    changeYear: true,
	    changeMonth: true,//dateFormat.indexOf("mm")!=-1,
	    currentText: '今天',
	    nextText: '下月>',  
	    prevText: '<上月',  
	    closeText: '关闭',  
	    weekHeader: '周',  
	    monthNamesShort: ['一月','二月','三月','四月','五月','六月','七月','八月','九月','十月','十一月','十二月'],  //且让月份显示为中文 
	    dayNames: ['周日','周一','周二','周三','周四','周五','周六'], 
	    dayNamesShort: ['日','一','二','三','四','五','六'],
	    //showButtonPanel: true,,
	    beforeShow: function (input) {;
	    	if( input.value ){
	    		try{
	    			var _date;
	            	if( dateFormat == "yy-mm" ){
	            		_date = input.value+"-01";
		        	}
		        	else if( dateFormat == "yy" ){
		        		_date = input.value+"-01-01";
		        	}
		        	else {
	            		_date = input.value;
		        	}
	        		$(this).datepicker('option', "defaultDate", new Date(_date));
	        		$(this).datepicker('setDate', new Date(_date));
	    		}
	    		catch(e){
	    		}
	    	}
	    },
	    onClose: function (evt, ui) {
			$(this).focus();
			if( callback ){
				callback();
			}
	    },
	    onChangeMonthYear: function (year, month, ui) {
	        var _date;
	    	if( dateFormat == "yy-mm" ){
	    		_date = year+"-"+(month>9?month:("0"+month))+"-01";
	    		$(this).datepicker('setDate', new Date(_date));
	    		//$(this).datepicker('hide');
	    	}
	    	else if( dateFormat == "yy" ){
	    		_date = year+"-01-01";
	    		$(this).datepicker('setDate', new Date(_date));
	    	//	$(this).datepicker('hide');
	    	}
	    }
	});
}

function pqCheckboxPicker(ui){
	$(this).pqSelect({ checkbox: true, radio: true, width: '100%' });
}
//收到数据
var remoteDataReceived = <ww:property value="opendigg" escape="false"/>;
function callbackAfterReceiveRemoteData(remoteData)
{
	remoteDataReceived = true;
	if(remoteData.hasException){
		if(document.referrer && dodebug ){
			window.top.skit_confirm(remoteData.message+"<br/>您是否查看DIGG报告",function(yes){
				if( yes ){
					var gridxml = document.getElementById("gridxml").value;
					window.top.removeView("diggcfg!debuglog.action");
					window.top.openView("查看元数据模板数据驱动引擎调试日志: "+gridxml, "diggcfg!debuglog.action?id=digg")
				}
			});
		}
		else
		{
			if( top && top.skit_alert )
				top.skit_alert(remoteData.message, "异常提示");
			else  alert(remoteData.message, "异常提示");
		}
		if( dlg_progress ){
			dlg_progress.hide();
			$grid.pqGrid( "hideLoading" );
		}
	}
	else
	{
		if( dodebug && top && top.skit_confirm ){
			window.top.skit_confirm("您是否查看DIGG报告",function(yes){
				if( yes ){
					var gridxml = document.getElementById("gridxml").value;
					window.top.removeView("diggcfg!debuglog.action");
					window.top.openView("查看元数据模板数据驱动引擎调试日志: "+gridxml, "diggcfg!debuglog.action?id=digg")
				}
			});
		}
    	//alert("remoteData.pq_filter="+remoteData.pq_filter);
		if( remoteData.pq_filter ) document.getElementById("pq_filter").value = remoteData.pq_filter;
    	//alert("filterLights="+filterLights);
		var data = remoteData.data;
		data = filterRows(data);
	}
	return { curPage: remoteData.curPage, totalRecords: remoteData.totalRecords, data: remoteData.data };
}

function calculateSummary(data)
{
	var val;
	if( summary )
	{
		for(var key in summary)
		{
			var type = summary["type_"+key] == "avg"?1:0;
			type = summary["type_"+key] == "sum"?2:type;
			if( type )
			{
				summary[key] = 0;
				for(var i = 0; i < data.length; i++)
		   		{
		   			var row = data[i];
		   			val = row[key];
		   			if( val ){
	        			if( typeof val == "string" )
	        			{
	        				val = parseFloat(val);
	        			}
		   				summary[key] += val;
		   			}
		   		}
				if( type == 1 )
				{
					summary[key] /= data.length;
					summary[key] = summary[key].toFixed(2);
				}
			}
		}
	}
}
//处理过滤数据
function filterRows(data)
{
	if( data )
	{
		calculateSummary(data);
		for(var i = 0; i < data.length; i++)
   		{
   			var row = data[i];
   			filterRow(row);
   		}
	}
	else if( !stylepreview )
	{
		top.skit_alert("未能正确获取列表数据请检查您的程序是否正常。");
	}
	return data;
}
/*选择行记录唯一值*/
function selectRowId()
{
    var recIndx = $grid.pqGrid("option", "dataModel.recIndx");
	if( !recIndx )
	{
		top.skit_alert("未配置recIndx");
		return null;
	}
    var select_row= $grid.pqGrid( "selection",{ type:'row', method:'getSelection' } );
	var pqrowdata;
	for(var i = 0; i < select_row.length; i++ )
	{
		pqrowdata = select_row[i]["rowData"];
		break;
	}
	if( !pqrowdata )
	{
		return null;
	}
	return pqrowdata[recIndx];
}

var tempString;//临时字符串
function filterRow(row)
{
	if( row && colModel ){}else{
		return;
	}
	var pq_cellcls = {};
	try
	{
		var cls, i;
		if( filterCellcls ) {
			for(var name in filterCellcls) {
				cls  = filterCellcls[name];
				if( cls && cls.color ) {
					pq_cellcls[name] = cls.color;
				}
			}
		}
		for(i = 0; i < colModel.length; i++ )
		{
			var col = colModel[i];
			if( col.dataIndx == i ) continue;
			if( col.colModel )
			{
				var cm = col.colModel;
				if( cm )
		    		for(var j = 0; j < cm.length; j++ )
		    		{
		    			filterData(row, cm[j], pq_cellcls);
		    		}
			}
			else
			{
				filterData(row, col, pq_cellcls);
			}
		}
		if( valueLabels )
		{
			for(var name in valueLabels)
			{
				var value = row[name];
				if( !value )
				{
					var args = name.split(".");
					if( args )
					{
						value = getObjectValue(row, args, 0);
					}
				}
				var labels = valueLabels[name];
				if( value == null ) value = "";
				var label = labels[""+value];
				if( label ){
					row[name+".src"] = row[name];
					row[name] = label
				}
			}
		}
		if( !jQuery.isEmptyObject(pq_cellcls) ) row["pq_cellcls"] = pq_cellcls;
	}
	catch(e)
	{
		if( top && top.skit_alert) top.skit_alert("执行行数据("+tempString+")过滤出现异常"+e.message+", 行数"+e.lineNumber);
		else alert("过滤行数据("+tempString+")出现异常"+e.message+", 行数"+e.lineNumber);
	}
}

/*根据数据类型进行数据过滤转义，根据样式配置修改单元格或行格式配置*/
function filterData(row, col, pq_cellcls)
{
	var light, value, operator, color, i;
	var val = getDataValue(row, col);
	if( val == "N/A" || val == "n/a" )
	{
		return null;
	}
	if( filterCellcls && filterCellcls[col.dataIndx] ){
		var lights = filterCellcls[col.dataIndx]["lights"];
		if(lights){
			for(i = 0; i < lights.length; i++){
				light = lights[i];
				operator = light.operator;
				value = light.value;
				color = light.color;
				if( operator == "lt" && val < value ){
					pq_cellcls[col.dataIndx] = color;
					break;
				}
				if( operator == "lte" && val <= value ){
					pq_cellcls[col.dataIndx] = color;
					break;
				}
				if( operator == "gt" && val > value ){
					pq_cellcls[col.dataIndx] = color;
					break;
				}
				if( operator == "gte" && val >= value ){
					pq_cellcls[col.dataIndx] = color;
					break;
				}
				if( operator == "equal" && val == value ){
					pq_cellcls[col.dataIndx] = color;
					break;
				}
				if( operator == "noequal" && val != value ){
					pq_cellcls[col.dataIndx] = color;
					break;
				}
			}
		}
	}
	if( filterRowcls ){
		lights = filterRowcls[col.dataIndx];
		if(lights){
			for(i = 0; i < lights.length; i++){
				light = lights[i];
				operator = light.operator;
				value = light.value;
				color = light.color;
				if( operator == "lt" && val < value ){
					row["pq_rowcls"] = color;
					break;
				}
				if( operator == "lte" && val <= value ){
					row["pq_rowcls"] = color;
					break;
				}
				if( operator == "gt" && val > value ){
					row["pq_rowcls"] = color;
					break;
				}
				if( operator == "gte" && val >= value ){
					row["pq_rowcls"] = color;
					break;
				}
				if( operator == "equal" && val == value ){
					row["pq_rowcls"] = color;
					break;
				}
				if( operator == "noequal" && val != value ){
					row["pq_rowcls"] = color;
					break;
				}
			}
		}
	}
	if( col.tag )
	{
		//alert(col.dataIndx+":"+col.dataType)
		var tags = val;
		if( tags )
		{
			var showSize = col.tag;
			var html = "<ul>";
			if( typeof tags == 'object' )
			{
				var json = JSON.stringify(tags);
				row[col.dataIndx+".src"] = tags;
				if( json.charAt(0) == '[' )
				{
					html = "<ul onclick='top.skit_message(\""+tags+"\");'>";
					for(i = 0; i < tags.length && i < showSize; i++)
					{
						html += "<li><span>"+tags[i]+"</span></li>";
					}
					if( showSize < tags.length  ){
						html += "<li><span>更多("+(tags.length-showSize)+")...</span></li>";
					}
				}
				else
				{
					for(var name in tags)
					{
						html += "<li><span>"+tags[name]+"</span></li>";
					}
				}
				html += "</ul>";
			}
			else if( tags && typeof tags == 'string' )
			{
				if( tags.indexOf("<ul") == 0 ){
					tags = row[col.dataIndx+".src"];
				}
				i = tags.indexOf(",");
				var s;
				if( i != -1 ){
					s = ",";
				}
				else{
					i = tags.indexOf("\n");
					if( i != -1 ){
						s = "\n";
					}
					else {
						i = tags.indexOf("\r\n");
						if( i != -1 ){
							s = "\r\n";
						}
						else{
							i = tags.indexOf(";");
							if( i != -1 ){
								s = ";";
							}
						}
					}
				}
				html = "<ul onclick='top.skit_message(\""+tags+"\");'>";
				//alert("-"+s+"["+tags+"]");
				var args = [tags];
				if( s ) {
					args = tags.split(s);
				}
				row[col.dataIndx+".src"] = tags;
				for(i = 0; i < args.length && i < showSize; i++)
				{
					if( args[i] ){
						html += "<li><span>"+args[i]+"</span></li>";
					}
				}
				if( showSize < args.length  ){
					html += "<li><span>更多("+(args.length-showSize)+")...</span></li>";
				}
				html += "</ul>";
			}
			else{
				html = tags;
			}
			row[col.dataIndx+".src"] = tags;
			row[col.dataIndx] = html;
		}
		else
			row[col.dataIndx] = '';
	}
	else if( (col.dataType == 'bool' || col.dataType == 'boolean') && col.type != "checkBoxSelection" )
	{
		if( row[col.dataIndx] )
		{
			var left = 36;
			if( col.width ) left = col.width/2 - 8;
			row[col.dataIndx] = '<span class="ui-widget-content ui-icon ui-icon-check" style="margin-left:'+left+'px;"></span>';
		}
		else
			row[col.dataIndx] = '';
	}
	else if( col.dataType == 'datasize' )
	{
		var size = showDatasize(val);
		row[col.dataIndx] = size;
	}
	else if( col.dataType == 'datalength' )
	{
		var size = showDatalength(val);
		row[col.dataIndx] = size;
	}
	else if( col.dataType == 'password' )
	{
		setDataValue(row, col.dataIndx, '******');
		row[col.dataIndx] = '******';
	}
	else
	{
		row[col.dataIndx] = val;
	}
	return null;
}
function getObjectValue(data, args, i)
{
	if( data )
	{
		if( i + 1 == args.length )
		{
			return data[args[i]];
		}
		return getObjectValue(data[args[i++]], args, i);
	}
	return null;
}

function getRowObject(row, dataIndx)
{
	var val = row[dataIndx];
	if( val ) return val;
	var args = dataIndx.split(".");
	if( args )
	{
		val = getObjectValue(row, args, 0);
		if( val ) return val;
	}
	return val?val:"N/A";
}

function getDataValue(row, col)
{
	if( row[col.dataIndx] || col.dataType == 'bool' || col.dataType == 'boolean' ) 
	{
		if( typeof col.dataIndx == "string" )
		{
			var val = row[col.dataIndx];
			if( typeof val == "string" ){
				return trimString(row[col.dataIndx]);
			}
		}
		return row[col.dataIndx];
	}
	else
	{
		var val;
		tempString = col.dataIndx;
		if( tempString && typeof tempString == "string" )
		{
			var args = tempString.split(".");
			if( args && args.length > 1 )
			{
				val = getObjectValue(row, args, 0);
	    		if( val || typeof val != "string" ){
	    			return val;
	    		}
			}
			else{
				val = row[col.dataIndx];
	    		if( val || typeof val != "string" ){
	    			return val;
	    		}
			}
		}
	}
	return "N/A";
}

function getObjectData(row, dataIndx)
{
	var args = dataIndx.split(".");
	if( args )
	{
		return getObjectValue(row, args, 0);
	}
	return null;
}

function trimString(val)
{
	var str = ""+val;
	str = str.trim();
	while(str.length>0&&str.charAt(0)=="　")
	{
		str = str.substring(1);
	}
	return str;
}

function setDataValue(row, dataIndx, val)
{
	var str = dataIndx;
	if( str && typeof str == "string" )
	{
		var args = str.split(".");
		if( args && args.length > 1 )
		{
			setObjectValue(row, args, 0, val);
		}
		else
		{
			row[dataIndx] = val;
		}
	}	
}

function setObjectValue(data, args, i, val)
{
	if( !data )
	{
		data = new Object();
	}
	if( i + 1 == args.length )
	{
		data[args[i]] = val;
		return;
	}
	setObjectValue(data[args[i++]], args, i, val);
}

//initialize the editor of date
var dateEditor = function (ui) {
	try
	{
		var dateFormat = "yy-mm-dd"
        var $cell = ui.$cell,
        rowData = ui.rowData,
        dataIndx = ui.dataIndx,
        width = ui.column.width,
        cls = ui.cls;
        var dc = $.trim(rowData[dataIndx]);
        var $inp = $("<input type='text' name='" + dataIndx + "' class='" + cls + " pq-ac-editor' />")
        .width(width - 6)
        .appendTo($cell)
        .val(dc);
        var $grid = $(this),
        validate = function (that) {
            var valid = $grid.pqGrid("isValid", { dataIndx: ui.dataIndx, value: $inp.val() }).valid;
            if (!valid) {
                that.firstOpen = false;
            }
        };
        //initialize the editor
        $inp.on("input", function (evt) {
            validate(this);
        }).datepicker({
        	dateFormat: dateFormat,
    	    yearRange: "-100:+100", //20 years prior to present.
        	changeYear: true,
            changeMonth: true,
    	    currentText: '今天',
    	    nextText: '下月>',  
    	    prevText: '<上月',  
    	    closeText: '关闭',  
    	    weekHeader: '周',  
    	    monthNamesShort: ['一月','二月','三月','四月','五月','六月','七月','八月','九月','十月','十一月','十二月'],  //且让月份显示为中文 
    	    dayNames: ['周日','周一','周二','周三','周四','周五','周六'], 
    	    dayNamesShort: ['日','一','二','三','四','五','六'],
            showAnim: '',
            onSelect: function () {
                this.firstOpen = true;
                validate(this);
            },
            beforeShow: function (input, inst) {
                return !this.firstOpen;
            },
            onClose: function () {
                this.focus();
            }
        });
	}
	catch(e)
	{
		if( top && top.skit_alert) top.skit_alert("初始化日期编辑器出现异常"+e.message+", 行数"+e.lineNumber);
		else  alert("初始化日期编辑器出现异常"+e.message+", 行数"+e.lineNumber);
	}
};
//处理远程返回的数据
var handleRemoteData = function(remoteData){
	$grid.pqGrid("hideLoading");
	if(remoteData.hasException){//如果返回数据有异常的情况下直接回调不做数据预处理
		return callbackAfterReceiveRemoteData(remoteData);//执行接收到远程数据后的回调
	}
	dataRemote = remoteData.data;//将远程返回的数据对象赋值给本地全局变量
	try{
	/*用户自定义脚本区域below*/
    <ww:property value="handleRemoteData" escape="false"/>
	/*用户自定义脚本区域above*/
		var response = callbackAfterReceiveRemoteData(remoteData);//执行接收到远程数据后的回调
		return response;
	}
	catch(e){
		alert('脚本执行异常'+e.message+', 行数'+e.lineNumber);
	}
	return { curPage: 1, totalRecords: 0, data: [] };
};