var skit_status_nopick = true;
function SKIT_PICKED_MOUSE_OVER(button_item)
{
	if( skit_status_nopick )
	{
		button_item.cells[0].className = 'skit_picked_over_left';
		button_item.cells[1].className = 'skit_picked_over_middle';
		button_item.cells[2].className = 'skit_picked_over_right';
		button_item.cells[1].childNodes[0].style.visibility = "visible";
	}
}

function SKIT_PICKED_MOUSE_OUT(button_item)
{
	if( skit_status_nopick )
	{
		button_item.cells[0].className = 'skit_picked_left';
		button_item.cells[1].className = 'skit_picked_middle';
		button_item.cells[2].className = 'skit_picked_right';
		button_item.cells[1].childNodes[0].style.visibility = "hidden";
	}
}

function SKIT_PICKED_MOUSE_CLK(button_item)
{
	if( skit_status_nopick )
	{
		button_item.cells[0].className = 'skit_picked_clicked_left';
		button_item.cells[1].className = 'skit_picked_clicked_middle';
		button_item.cells[2].className = 'skit_picked_clicked_right';
		button_item.cells[1].childNodes[0].style.visibility = "visible";
		skit_status_nopick = false;//表示被点击
	}
	else
	{
		button_item.cells[0].className = 'skit_picked_over_left';
		button_item.cells[1].className = 'skit_picked_over_middle';
		button_item.cells[2].className = 'skit_picked_over_right';
		skit_status_nopick = true;//表示被点击
	}
}

function SKIT_BLUEBTN_MOUSE_OVER(button_item)
{
	button_item.cells[0].className = 'skit_blue_button_over_left';
	button_item.cells[1].className = 'skit_blue_button_over_middle';
	button_item.cells[2].className = 'skit_blue_button_over_right';
}

function SKIT_BLUEBTN_MOUSE_OUT(button_item)
{
	button_item.cells[0].className = 'skit_blue_button_left';
	button_item.cells[1].className = 'skit_blue_button_middle';
	button_item.cells[2].className = 'skit_blue_button_right';
	button_item.cells[1].firstChild.className = 'skit_button_font';
}

function onBlueBtnMouseClicked(button_item)
{
	button_item.cells[0].className = 'skit_blue_button_clicked_left';
	button_item.cells[1].className = 'skit_blue_button_clicked_middle';
	button_item.cells[2].className = 'skit_blue_button_clicked_right';
	button_item.cells[1].firstChild.className = 'skit_button_font';
}


function SKIT_BTN_MOUSE_OVER()
{
	var args = SKIT_BTN_MOUSE_OVER.arguments;
	var button_item = args[0];
	button_item.cells[0].className = 'skit_button_over_left';
	button_item.cells[1].className = 'skit_button_over_middle';
	button_item.cells[2].className = 'skit_button_over_right';
}

function SKIT_BTN_MOUSE_OUT()
{
	var args = SKIT_BTN_MOUSE_OUT.arguments;
	var button_item = args[0];
	button_item.cells[0].className = 'skit_button_left';
	button_item.cells[1].className = 'skit_button_middle';
	button_item.cells[2].className = 'skit_button_right';
	button_item.cells[1].firstChild.className = 'skit_button_font';
}

function SKIT_BTN_MOUSE_CLK()
{
	var args = SKIT_BTN_MOUSE_CLK.arguments;
	var button_item = args[0];
	button_item.cells[0].className = 'skit_button_clicked_left';
	button_item.cells[1].className = 'skit_button_clicked_middle';
	button_item.cells[2].className = 'skit_button_clicked_right';
	button_item.cells[1].firstChild.className = 'skit_button_font';
}

function SKIT_SELECTION_MINUS_OVER(button_item)
{
	button_item.className = 'skit_minus_over';
}

function SKIT_SELECTION_MINUS_OUT(button_item)
{
	button_item.className = 'skit_minus';
}

function SKIT_SELECTION_MINUS_CLK(button_item)
{
	button_item.className = 'skit_minus_clicked';
}

function SKIT_SELECTION_MINUS_DISABLED(button_item)
{
	button_item.className = 'skit_minus_disabled';
}


//获取元素的纵坐标
function skit_getTop(e)
{
	var offset = e.offsetTop;
	if( e.offsetParent !== null )
	{
		offset += skit_getTop(e.offsetParent);
	}
	return offset;
}
//获取元素的横坐标
function skit_getLeft(e)
{
	var offset = e.offsetLeft;
	if(e.offsetParent!==null) 
	{
		offset += skit_getLeft(e.offsetParent);
	}
	return offset;
} 

function skit_mousePosition(ev)
{
	if(ev.pageX || ev.pageY)
	{
		return {x:ev.pageX, y:ev.pageY};
	}
	return {
		x:ev.clientX + document.body.scrollLeft - document.body.clientLeft,
		y:ev.clientY + document.body.scrollTop - document.body.clientTop
	};
} 

/*新增弹出窗口*/
function skit_new_popmenu(id, width, args)
{
	if( args.length == 0 )
	{
		return;
	}
	var div = document.createElement("div");
	div.id = id;
	div.className = "skit_popmenu";
	div.style.width = width+"px";
	div.style.zIndex = 200;
	div.onmouseover = new Function('document.getElementById("'+id+'").style.visibility = "visible";');
	div.onmouseout = new Function('document.getElementById("'+id+'").style.visibility = "hidden";');
	var table = document.createElement("table");
	table.width = "100%";
	table.cellspacing = 0;
	table.cellpadding = 0;
	var i = 0;
	for(; i < args.length && i < 100;)
	{
		var newRow = table.insertRow(i);
		var method = args[i][0];
		var trId = id + ".row."+ i;
		if( args[i].length > 3 && args[i][3] != "" )
		{
			trId = args[i][3];
		}
		newRow.id = trId;
		method = 'document.getElementById("'+id+'").style.visibility = "hidden";'+method
		newRow.onclick = new Function(method);
		newRow.onmouseover = new Function('document.getElementById("'+newRow.id+'").style.backgroundColor = "#CED9CC";');
		newRow.onmouseout = new Function('document.getElementById("'+newRow.id+'").style.backgroundColor = "";');
		var cell = newRow.insertCell(0);
		if( i == (args.length - 1) )
			cell.className = "skit_table_cell_end";
		else
			cell.className = "skit_table_cell";
		cell.style.paddingLeft = "1px";
		cell.style.paddingRight = "1px";
		var title = args[i][1];
		if( args[i].length > 2 && args[i][2] != "" )
		{
			var iconname = args[i][2];
			if( iconname.indexOf("fa-") != -1 ) cell.innerHTML = "<i class='"+iconname+"'></i>"+title;
			else cell.innerHTML = "<img src='skit/images/icons/"+iconname+".gif' ALIGN='ABSMIDDLE' />&nbsp;"+title;
		}
		else
		{
			cell.innerHTML = title;
		}
		i += 1;
	}
	div.appendChild(table);
	document.body.appendChild(div);
}

//得到DIV层的位置
function skit_getDivScrollTop(ctrl, depth)
{
	if (ctrl.tagName == "HTML") {
		return 0;
	}
	if( ctrl.tagName == "DIV" )
	{
	//alert(ctrl.scrollTop+"->"+depth);
		return ctrl.scrollTop;
	}
	depth = depth?(depth+1):1;
	if( depth < 100 )
	{
		return skit_getDivScrollTop(ctrl.parentNode, depth);
	}
	return 0;
}
//打开菜单位置(link表示有关联
function skit_popmenu(tag, btn, valign)
{
	var div = document.getElementById(tag);
	if( div )
	{
		div.style.visibility = "visible";
		var top = skit_getTop(btn);
		var left = skit_getLeft(btn);
		if( valign && valign == "up" )
		{
			top = top - div.clientHeight;
			if( top - div.clientHeight < 0 )
			{//如果向上超过边界就向下
				top = skit_getTop(btn) + btn.parentNode.clientHeight;
			}
		}
		else
		{
			top = top + btn.parentNode.clientHeight;
			if( top + div.clientHeight > window.document.body.clientHeight )
			{//如果向下超过边界就向上
				top = skit_getTop(btn) - div.clientHeight;
			}
		}
		var top1 = top - skit_getDivScrollTop(btn);
		div.style.top = top1<0?0:top1;
		if( left + div.clientWidth > window.document.body.clientWidth )
		{//如果left的位置加上
			left = window.document.body.clientWidth - div.clientWidth - 3;
		}
		div.style.left = left;
	}
}

//新建select控件
var skitSelectOptions = new Object();//数据映射表
function skit_new_append(args, id, divWidth, divHeight)
{
	_skit_new_select(args, id, "skit_append", divWidth, divHeight);
}
function skit_new_select(args, id, divWidth, divHeight)
{
	_skit_new_select(args, id, "skit_select", divWidth, divHeight);
}
//新建
function skit_new_selectex(args, id, divWidth, divHeight)
{
	_skit_new_select(args, id, "skit_selectex", divWidth, divHeight);
}
//新建关联空间
function skit_new_select_link(args, id, divWidth, divHeight)
{
	_skit_new_select(args, id, "skit_select_link", divWidth, divHeight);
}

//新建自带输入框控件的select
function skit_new_select_input(args, id, divWidth, divHeight)
{
	_skit_new_select(args, id, "skit_select", divWidth, divHeight, true);
}
//新建带checkbox的控件
function skit_new_select_checkbox(args, id, divWidth, divHeight)
{
	_skit_new_select(args, id, "skit_check", divWidth, divHeight, false, true);
}
/*
 * 构建select控件
 */
function _skit_new_select()
{
	var args = _skit_new_select.arguments;
	var options = args[0];
	var id = args[1];
	var method = args[2];
	var divWidth = args[3];
	var divHeight = args[4];
	var keyupInput = args.length>5?args[5]:null;
	var checkbox = args.length>6?args[6]:false; 
	if( options.length == 0 )
	{
		return;
	}
	var divOld = document.getElementById(id);
	if( divOld )
	{
		document.body.removeChild(divOld);
	}
	var div = document.createElement("div");
	div.id = id;
	div.className = "skit_select";
	var table1 = document.createElement("table");
	//table1.width = "100%";
	table1.cellspacing = 0;
	table1.cellpadding = 0;
	var table = skit_create_select_options(options,id,method,null,checkbox);
	table.onmouseover = new Function('document.getElementById("'+id+'").style.visibility = "visible";');
	table.onmouseout = new Function('document.getElementById("'+id+'").style.visibility = "hidden";');
	table1.insertRow(0).insertCell(0).appendChild(table);
	var input = null;
	if( keyupInput )
	{
		input = document.createElement("input");
		input.type = "text";
		input.id = "input"+id;
		if( divHeight )
		{
			input.onkeyup = new Function("skit_keyup_select(this,'"+id+"','up',"+divHeight+");");
		}
		else
		{
			input.onkeyup = new Function("skit_keyup_select(this,'"+id+"','down');");
		}
		input.className = "skit_input_text";
		//input.style.width = divWidth;
		table1.insertRow(1).insertCell(0).appendChild(input);
	}	
	div.appendChild(table1)
	div.style.zIndex = 10000;
//	if( dwr ){alert("table:"+table);}
	document.body.appendChild(div);
	if( table.clientWidth > divWidth )
	{
		divWidth = table.clientWidth;
	}
	else
	{
		table.style.width = divWidth;
		if( input ) input.style.width = divWidth - 5;
	}

	if( divHeight && table1.clientHeight > divHeight )
	{
		div.style.overflowX = "hidden";
		div.style.overflowY = "auto";
	}
	else
	{
		div.style.overflowX = "hidden";
		div.style.overflowY = "hidden";
		divHeight = table1.clientHeight;
	}
	div.style.width = divWidth;
	div.style.height = divHeight;
	skitSelectOptions[id] = options;
}
//创建option表格
function skit_create_select_options()
{
	var args = skit_create_select_options.arguments;
	var options = args[0];
	var id = args[1];
	var method = args[2];
	var inputValue = args.length>3?args[3]:null;
	var checkbox = args.length>4?args[4]:false; 
	if( !method || method == "")
	{
		method = "skit_select";
	}
	var table = document.createElement("table");
	table.width = "100%";
	table.cellspacing = 0;
	table.cellpadding = 0;
	var i = 0, k = 0;
	for(; i < options.length && k < 100; i+=1 )
	{
		var value = (options[i].length&&options[i].length>1)?options[i][0]:options[i];//label标签
		var label = (options[i].length&&options[i].length>1)?options[i][1]:value;
		value = ""+value;
		label = ""+label;
		if( inputValue != null && inputValue != "" )
		{
			//if( value.indexOf(inputValue)==-1 && label.indexOf(inputValue)==-1 )
			if( value.toUpperCase().indexOf(inputValue.toUpperCase())==-1 && label.toUpperCase().indexOf(inputValue.toUpperCase())==-1 )
			{
				continue;
			}
		}
		var params = "";
		for(var j = 0; j < options[i].length; j++ )
		{
			if( j > 0 )
			{
				params += ",";
			}
			var str = ""+options[i][j];
			if( str.indexOf("'") != -1 )
			{
				str = str.replace("'", " ");
			}
			params += "'"+str+"'";
		}
		var newRow = table.insertRow(k);
		newRow.id = "tr."+ id + value;
		if( !checkbox )
		{
			newRow.onclick = new Function("document.getElementById('"+id+"').style.visibility = 'hidden';"+method+"("+params+");");
		}
		newRow.onmouseover = new Function('document.getElementById("'+newRow.id+'").style.backgroundColor = "#CED9CC";');
		newRow.onmouseout = new Function('document.getElementById("'+newRow.id+'").style.backgroundColor = "";');
		var cell = newRow.insertCell(0);
		if( i == (options.length - 1) )
			cell.className = "skit_table_cell_end";
		else
			cell.className = "skit_table_cell";
		cell.style.paddingLeft = "1px";
		cell.style.paddingRight = "1px";
		cell.style.whiteSpace = "nowrap";
		if( checkbox )
		{
			cell.innerHTML = "<input type='checkbox' onclick='"+method+"(this);' name='checkbox"+id+"' value='"+value+"'/>&nbsp;"+label;//label标签
		}
		else
		{
			cell.innerHTML = label;
		}
		if( label == value )
		{
			cell.title = label;
		}
		else
		{
			cell.title = label+"("+value+")";
		}
		k += 1;
	}
	
	if( k < options.length )
	{
		var newRow = table.insertRow(k);
		var cell = newRow.insertCell(0);
		cell.className = "skit_table_cell_end";
		cell.style.paddingLeft = "1px";
		cell.style.paddingRight = "1px";
		cell.innerHTML = "..."+options.length;
	}
	return table;
}

var skit_open_select_tag;//打开空间的id
var skit_focus_select;
var skit_focus_select_link = "";//扩展的弹出太多标签
//打开select控件位置(link表示有关联
function skit_open_select(btn, tag, valign)
{
	skit_focus_select = btn;
	var div = document.getElementById(tag);
	if( div )
	{
		//var id = "input"+tag; 
		//var input = document.getElementById(id);
		var top = skit_getTop(btn);
		var left = skit_getLeft(btn);
/*		if( input == null || input.id == btn.id )
		{
			if( skit_open_select_tag )
			{
				document.getElementById(skit_open_select_tag).style.visibility = "hidden";
				var selectInput = document.getElementById("input"+skit_open_select_tag);
				if(selectInput) 
				{
					selectInput.style.visibility = "hidden";
				}
			}*/
			skit_open_select_tag = tag;
			if( valign && valign == "up" )
			{
				top = top - div.clientHeight - 2;
			}
			else
			{
				top = top + btn.clientHeight + 2;
			}
			div.style.top = top - skit_getDivScrollTop(btn);
			if( left + div.clientWidth + 3 > window.document.body.clientWidth )
			{//如果left的位置加上
				left = window.document.body.clientWidth - div.clientWidth - 3;
			}
			div.style.left = left;
			div.style.visibility = "visible";
			div.style.zIndex = 999999999;
/*		}
		else
		{
			if( valign && valign == "up" )
			{
				top = top - btn.clientHeight - 2;
			}
			else
			{
				top = top + btn.parentNode.clientHeight + 2;
			}
			input.style.top = top;
			input.style.left = left;
			input.style.visibility = "visible";
			input.focus();
			input.onkeyup = new Function("skit_keyup_select(this,'"+tag+"','"+valign+"');");
			skit_open_select(input, tag, valign);
		}*/
	}
	return div;
}

//打开关联菜单
function skit_open_select_link(btn, tag, valign)
{
	skit_focus_select = btn;
	tag = tag + skit_focus_select_link;
	if( !skit_open_select_dwr(btn, tag, skit_focus_select_link, valign) )
	{
		skit_open_select(btn, tag, valign);
	}
}
var skit_select_sub_value = "";//选择框的子参数值
//选择数据项,value在中表单中，label在title中
function skit_select()
{
	var args = skit_select.arguments;
	skit_focus_select.value = args[0];
	if( args.length > 1 )
	{
		skit_focus_select.title = args[1];
	}
	skit_select_sub_value = "";
	if( args.length > 2 )
	{
		for(var j = 2; j < args.length; j++ )
		{
			if( j > 2 )
			{
				skit_select_sub_value += ",";
			}
			skit_select_sub_value += args[j];
		}
	}
	
	skit_focus_select.focus();
	var textRange = skit_focus_select.createTextRange();
	textRange.move("character",skit_focus_select.value.length);
	textRange.select();
}

//下拉列表中的数据以append的模式叠加
function skit_append()
{
	var args = skit_append.arguments;
	var text = skit_focus_select.value;
	if( text != "" )
	{
		text = text + '+' + args[0]; 
	}
	else
	{
		text = args[0];
	}
	skit_focus_select.value = text;
	if( args.length > 1 )
	{
		text = skit_focus_select.title;
		if( text != "" )
		{
			text = text + '+' + args[1]; 
		}
		else
		{
			text = args[1];
		}
		skit_focus_select.title = text;
	}
	
	skit_focus_select.focus();
	var textRange = skit_focus_select.createTextRange();
	textRange.move("character",skit_focus_select.value.length);
	textRange.select();
}

//勾选多个条件，以OR的方式添加
function skit_check(chk)
{
	var sperator = " | ";
	var cbg = document.getElementsByName(chk.name);
	if( !cbg ) return;
	var ids = "";
	var descript = "";
	if( cbg.length )
	{ 
		var n = 0;
		for(var i=0; i<cbg.length; i++)
		{
			if(cbg[i].checked )
			{
				if( n > 0 )
				{
					ids += sperator; 
					descript += sperator;
				}
				ids += cbg[i].value;
				descript += cbg[i].parentNode.innerText.trim();
				n += 1;
			}				
		}
	}
	else if( cbg.value )
	{
		if(cbg.checked)
		{
			ids +=  cbg.value;
			descript += cbg.parentNode.innerText.trim();
		}
	}
	
	skit_focus_select.value = ids;
	skit_focus_select.title = descript;
	skit_focus_select.focus();
	var textRange = skit_focus_select.createTextRange();
	textRange.move("character",skit_focus_select.value.length);
	textRange.select();
}

//选择数据项,value在中表单中，label在title中
function skit_selectex()
{
	var args = skit_selectex.arguments;
	skit_focus_select.value = args[1];
	skit_focus_select.title = args[0];
	skit_select_sub_value = "";
	if( args.length > 2 )
	{
		for(var j = 2; j < args.length; j++ )
		{
			if( j > 2 )
			{
				skit_select_sub_value += ",";
			}
			skit_select_sub_value += args[j];
		}
	}
	
	skit_focus_select.focus();
	var textRange = skit_focus_select.createTextRange();
	textRange.move("character",skit_focus_select.value.length);
	textRange.select();
}

//选择关联菜单
function skit_select_link()
{
	var args = skit_select_link.arguments;
	skit_focus_select_link = args[0];
	skit_focus_select.value = args[1];
	skit_focus_select.title = args[0];
	
	skit_focus_select.focus();
	var textRange = skit_focus_select.createTextRange();
	textRange.move("character",skit_focus_select.value.length);
	textRange.select();
}

var mLastInputText;//最后输入的文本
//输入链接选择
function skit_keyup_select_link(input,id,valign,divHeight,dwr)
{
	id = id + skit_focus_select_link;
	if( dwr )
	{//通过dwr获取数据
		skit_open_select_dwr(input, id, skit_focus_select_link, valign)
	}
	else
	{
		skit_open_select_filter(input,id,valign,divHeight,"skit_select");
	}
}
//在输入表单中输入字符串，根据字符串过滤，产生新的列表框
function skit_keyup_select(input,id,valign,divHeight,func)
{
    var e = event.srcElement;
    var k = event.keyCode;
    if( k == 38 || k == 40 )
    {//向上下移动光标
    	var div = document.getElementById(id);
    	if( div )
    	{
    		var table = div.childNodes[0];
    		var trFirst = null;
    		for( var i = 0; i < table.rows.length; i++ )
    		{
    			var tr = table.rows[i];
    			if( tr.cells[0].innerText.indexOf("...") == 0 ) break;
   				trFirst = trFirst?trFirst:tr;
    			if( table.rows.length > i+1 && 
    				table.rows[i+1].cells[0].innerText.indexOf("...") == 0 )
    				break;
    			if( tr.style.backgroundColor != "" )
    			{
    				if( k == 40 && table.rows.length > i+1  ) 
    				{
   						tr.style.backgroundColor = "";
    					table.rows[i+1].style.backgroundColor = "#CED9CC";
    					return;
    				}
    				else if( k == 38 && i > 0 )
    				{
    					tr.style.backgroundColor = "";
    					table.rows[i-1].style.backgroundColor = "#CED9CC";
    					return;
    				}
    				return;
    			}
    		} 
   			if( trFirst )
   			{
   				trFirst.style.backgroundColor = "#CED9CC";
   			}
    	}
    	return;
    }
    else if( k == 13 || k == 32 )
    {//回车 空格选择但不触发事件
    	var div = document.getElementById(id);
    	if( div )
    	{
    		var table = div.childNodes[0];
    		var isTrTotal = false;//是否有汇总列
    		for( var i = 0; i < table.rows.length; i++ )
    		{
    			var tr = table.rows[i];
    			if( tr.cells[0].innerText.indexOf("...") == 0 )
    			{
    				isTrTotal = true;
    				continue;
    			}
    			if( tr.style.backgroundColor != "" )
    			{
    				tr.onclick();
    			}
    		}
    		if( table.rows.length == isTrTotal?2:1 )
    		{
		    	if( k == 13 && func )
		    	{
		    		func(input);
		    	}
    		}
    	}
    	else if( k == 13 && func )
    	{
    		func(input);
    	}    	
    	return;
    }
	if( !skit_open_select_dwr(input, id, "", valign) )
	{//通过dwr获取数据
		skit_open_select_filter(input,id,valign,divHeight,"skit_select")
	}
}
/*
	input 输入框控件对象
	id 选择列表框的ID
	valign 向上或者向下打开列表选择框
	divHeight 列表选择框的高度
	method 点击选择列表框中的字符串时候触发什么样的事件（函数）
 */
function skit_open_select_filter(input,id,valign,divHeight,method)
{
	var e = event.srcElement;
    var k = event.keyCode;
    if( k != 27 )
    {
    	if( input.value != mLastInputText )
    	{
			mLastInputText = input.value;
			var div = document.getElementById(id);
			if( div )
			{
				var options = skitSelectOptions[id];
				var table = skit_create_select_options(options,id,method,input.value);
				if( table )
				{
					var table1 = div.childNodes[0];
					table1.deleteRow(0);
					div.style.visibility = "hidden";
					table1.insertRow(0).insertCell(0).appendChild(table);
					if( table1.clientHeight > divHeight )
					{
						div.style.overflowX = "hidden";
						div.style.overflowY = "auto";
						div.style.height = divHeight;
					}
					else
					{
						div.style.overflowX = "hidden";
						div.style.overflowY = "hidden";
						div.style.height = table1.clientHeight+2;
					}
					div.style.width = table1.clientWidth;
					skit_open_select(input, id, valign);
				}
			}
    	}
    }
}
//通过dwr方式打开选择对话框
function skit_open_select_dwr(btn, tag, link, valign)
{
	return false;
}
/*                                   coolbuttons.js                                         */
document.onmouseover = doOver;
document.onmouseout  = doOut;
document.onmousedown = doDown;
document.onmouseup   = doUp;

var m_bgcolor = "#18bc9c";//"buttonface"; //#A9BAA5
var m_border_top = "#8FA68A";//"buttonhighlight";//#8FA68A;
var m_border_left = "#8FA68A";//"buttonhighlight";//#8FA68A;
var m_border_right = "#566B52";//"buttonhighlight";//#566B52;
var m_border_bottom = "#566B52";//"buttonhighlight";//#566B52;
var m_init_border_top = "#8FA68A";//"buttonhighlight";//#8FA68A;
var m_init_border_left = "#8FA68A";//"buttonhighlight";//#8FA68A;
var m_init_border_right = "#566B52";//"buttonhighlight";//#566B52;
var m_init_border_bottom = "#566B52";//"buttonhighlight";//#566B52;

function doOver() 
{
	var toEl = getReal(window.event.toElement, "className", "coolButton");
	var fromEl = getReal(window.event.fromElement, "className", "coolButton");
	if (toEl == fromEl) return;
	var el = toEl;
	
	var cDisabled = el.cDisabled;

	cDisabled = (cDisabled != null);
	
	if (el.className == "coolButton")
	{
		el.onselectstart = new Function("return false");
	}

	if ((el.className == "coolButton") && !cDisabled) 
	{
		if(el.havearrow==1)
		{
			makeRaised(el);
			makeRaised(eval(el.aname));
		}
		else
		{
			makeRaised(el);
		}
	}
}

function doOut() 
{
	var toEl = getReal(window.event.toElement, "className", "coolButton");
	var fromEl = getReal(window.event.fromElement, "className", "coolButton");
	if (toEl == fromEl) return;
	var el = fromEl;

	var cDisabled = el.cDisabled;
	cDisabled = (cDisabled != null);

	var cToggle = el.cToggle;
	toggle_disabled = (cToggle != null);

	if (cToggle && el.value) 
	{
		makePressed(el);
	}
	else if ((el.className == "coolButton") && !cDisabled) 
	{
		if(el.havearrow==1)
		{
			makeFlat(el);
			makeFlat(eval(el.aname));
		}
		else{makeFlat(el);}
		makeFlat(el);
		
	}

}

function doDown()
{
	el = getReal(window.event.srcElement, "className", "coolButton");
	
	var cDisabled = el.cDisabled;
	cDisabled = (cDisabled != null); // If CDISABLED atribute is present
	
	if ((el.className == "coolButton") && !cDisabled) 
	{
		makePressed(el)
	}
}

function doUp() 
{
	el = getReal(window.event.srcElement, "className", "coolButton");
	
	var cDisabled = el.cDisabled;
	cDisabled = (cDisabled != null);
	
	if ((el.className == "coolButton") && !cDisabled) 
	{
		makeRaised(el);
	}
}


function getReal(el, type, value) 
{
	temp = el;
	while ((temp != null) && (temp.tagName != "BODY")) 
	{
		if (eval("temp." + type) == value) 
		{
			el = temp;
			return el;
		}
		temp = temp.parentElement;
	}
	return el;
}

function disable(el) 
{
	if (document.readyState != "complete") 
	{
		window.setTimeout("disable(" + el.id + ")", 100);
		return;
	}
	var cDisabled = el.cDisabled;
	
	cDisabled = (cDisabled != null); 
	if (!cDisabled) {
		el.cDisabled = true;
		
		if (document.getElementsByTagName) {
			el.innerHTML =	"<span style='background: buttonshadow; filter: chroma(color=red) dropshadow(color=buttonhighlight, offx=1, offy=1); width: 100%; height: 100%; text-align: center;'>" +
							"<span style='filter: mask(color=red); width: 100%; height: 100%; text-align: center; padding-top:3px;'>" +
							el.innerHTML +
							"</span>" +
							"</span>";
		}
		else { // IE4
			el.innerHTML =	'<span style="background: buttonshadow; width: 100%; height: 100%; text-align: center;">' +
							'<span style="padding-top:1px;filter:Mask(Color=buttonface) DropShadow(Color=buttonhighlight, OffX=1, OffY=1, Positive=0); height: 100%; width: 100%%; text-align: center;">' +
							el.innerHTML +
							'</span>' +
							'</span>';
		}
		if (el.onclick != null) {
			el.cDisabled_onclick = el.onclick;
			el.onclick = null;
		}
	}
}

function enable(el) {
	var cDisabled = el.cDisabled;
	
	cDisabled = (cDisabled != null);
	
	if (cDisabled) {
		el.cDisabled = null;
		el.innerHTML = el.children[0].children[0].innerHTML;

		if (el.cDisabled_onclick != null) {
			el.onclick = el.cDisabled_onclick;
			el.cDisabled_onclick = null;
		}
	}
}

function makeFlat(el) {
	with (el.style) {
		background = "";
		border = "1px solid "+m_bgcolor;
	}
}

function makeRaised(el) {
	with (el.style) {
		borderLeft   = "1px solid "+m_border_left;
		borderRight  = "1px solid "+m_border_right;
		borderTop    = "1px solid "+m_border_top;
		borderBottom = "1px solid "+m_border_bottom;
		paddingTop    = "1px";
		paddingLeft   = "1px";
		paddingBottom = "1px";
		paddingRight  = "1px";
	}
}

function makePressed(el) {
	with (el.style) {
		borderLeft   = "1px solid "+m_border_left;
		borderRight  = "1px solid "+m_border_right;
		borderTop    = "1px solid "+m_border_top;
		borderBottom = "1px solid "+m_border_bottom;
		paddingTop    = "1px";
		paddingLeft   = "1px";
		paddingBottom = "1px";
		paddingRight  = "1px";
	}
}

document.write("<style>");
document.write(".coolBar	{background: "+m_bgcolor+";border-top: 1px solid "+m_init_border_top+";	border-left: 1px solid "+m_init_border_left+";	border-bottom: 1px solid "+m_init_border_bottom+"; border-right: 1px solid "+m_init_border_right+"; font: menu;}");
document.write(".coolButton {border: 1px solid "+m_bgcolor+"; padding:1px; text-align: center; cursor: default;font-size:12px;font-family:ms shell dlg;line-height:22px}");
document.write(".coolButton IMG	{filter:;}");
document.write("</style>");