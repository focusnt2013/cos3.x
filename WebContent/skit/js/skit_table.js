var _SKIT_TABLE_VIEW_ARRAY = new Array();


var _cur_sort_td;
var _show_col  = true;
var _cur_bgc = "#E7E7E7";
var _cur_fc = "#FFF000";
var _charMode  = 0;

var _suspendSort = false;
var _SKIT_TABLE_VIEW_ARRAY = new Array();
var _SKIT_TABLE_HEAD_ARRAY = new Array();
var _SKIT_TABLE_ROW_SELECTED;

function SKIT_TABLE_VIEW_INIT()
{
}


window.onload=function()
{
	SKIT_TABLE_VIEW_INIT();
	for( var i = 0; i < _SKIT_TABLE_HEAD_ARRAY.length; i++ )
	{
		_SKIT_TABLE_HEAD_ARRAY[i].onclick  = SKIT_TABLE_SORT;
		_SKIT_TABLE_HEAD_ARRAY[i].onmouseover  = SKIT_HEAD_OVER;
		_SKIT_TABLE_HEAD_ARRAY[i].onmouseout  = SKIT_HEAD_OUT;

		_SKIT_TABLE_VIEW_ARRAY[i].onclick = SKIT_VIEW_ROWS_CLK;
	}
	_SKIT_TABLE_ROW_SELECTED = new Array(_SKIT_TABLE_HEAD_ARRAY.length);
}

function SKIT_VIEW_ROWS_CLK()
{
    var the_obj = event.srcElement;
	var the_tr = SKIT_GET_ELEMENT(the_obj,"tr");
	if(the_tr)
	{
		for(var i = 0; i < _SKIT_TABLE_VIEW_ARRAY.length; i++ )
		{
			var row = _SKIT_TABLE_VIEW_ARRAY[i].rows[the_tr.rowIndex];
			if( _SKIT_TABLE_ROW_SELECTED[i] != null )
			{
				_SKIT_TABLE_ROW_SELECTED[i].style.backgroundColor = '';
				//_SKIT_TABLE_ROW_SELECTED.style.color = '';
				for( var j = 0; j < _SKIT_TABLE_ROW_SELECTED[i].cells.length; j++ )
				{
					var cell = _SKIT_TABLE_ROW_SELECTED[i].cells[j];
					if( cell.firstChild.style )
					{
						cell.firstChild.style.backgroundColor = '';
					}
				}
			}
			SKIT_TABLE_ROWS_CLK(row);
			_SKIT_TABLE_ROW_SELECTED[i] = row;
		}
	}
}

function SKIT_TABLE_ROWS_CLK()
{
	var i = 0;
	var args = SKIT_TABLE_ROWS_CLK.arguments;

	args[0].style.backgroundColor = '#C1CFBE';//#DBE1D9
	//args[0].style.color = '#58626A';
	for( i = 0; i < args[0].cells.length; i++ )
	{
		var cell = args[0].cells[i];
		if( cell.firstChild.style )
		{
			cell.firstChild.style.backgroundColor = '#C1CFBE';
		}
	}
}

function SKIT_GET_ELEMENT(the_ele,the_tag)
{
	the_tag = the_tag.toLowerCase();
	if(the_ele.tagName.toLowerCase()==the_tag)return the_ele;

	//while(the_ele=the_ele.offsetParent)
	while(the_ele=the_ele.parentElement)
	{
		if(the_ele.tagName.toLowerCase()==the_tag)return the_ele;
	}
	return(null);
}

function SKIT_HEAD_OVER()
{
	if( _suspendSort ) return;
    var the_obj = event.srcElement;
	if( the_obj.tagName.toLowerCase() != "table" &&
	    the_obj.tagName.toLowerCase() != "tbody" &&
		the_obj.tagName.toLowerCase() != "tr")
	{
		var the_td  = SKIT_GET_ELEMENT(the_obj,"td");
		if(the_td != null && the_td.innerText != " " )
		{
			the_td.className="skit_table_over";
		}
	}
}
function SKIT_HEAD_OUT()
{
	if( _suspendSort ) return;
    var the_obj = event.srcElement;
	if( the_obj.tagName.toLowerCase() != "table" &&
	    the_obj.tagName.toLowerCase() != "tbody" &&
		the_obj.tagName.toLowerCase() != "tr")
	{
		var the_td = SKIT_GET_ELEMENT(the_obj,"td");
		if(the_td != null && the_td.innerText != " "  )
		{
			the_td.className="skit_head_cell";
		}
	}
}

function getTableIndex(the_obj)
{
	for( var i = 0; i < _SKIT_TABLE_HEAD_ARRAY.length; i++ )
	{
		if( _SKIT_TABLE_HEAD_ARRAY[i].uniqueID == the_obj.uniqueID )
		{
			return i;
		}
	}
	return -1;
}

function SKIT_TABLE_SORT()
{
	if( _suspendSort ) return;
	_suspendSort = true;
    var the_obj = event.srcElement;
	if( the_obj.tagName.toLowerCase() != "table" &&
	    the_obj.tagName.toLowerCase() != "tbody" &&
		the_obj.tagName.toLowerCase() != "tr")
	{
		var the_td = SKIT_GET_ELEMENT(the_obj,"td");

		if(the_td != null )
		{
			var the_tr  = the_td.parentElement;
			var the_table  = SKIT_GET_ELEMENT(the_td, "table");
			var col = the_td.cellIndex;
			var tab = getTableIndex(the_table);
			if( tab != -1 )
			{
				the_td.mode = !the_td.mode;
				sort_tab(tab, col, the_td.mode);
				if( _cur_sort_td && _cur_sort_td.uniqueID !=  the_td.uniqueID && _cur_sort_td.firstChild.style )
					_cur_sort_td.firstChild.style.display = 'none';
				_cur_sort_td = the_td;
			}
		}
	}
	_suspendSort = false;
}

function sort_tab(){
  var tab = arguments[0];
  var col = arguments[1];
  var mode = arguments[2];

  var tab_arr = new Array();
  var start = new Date;
  var sort_tab = _SKIT_TABLE_VIEW_ARRAY[tab];
  for( var i = 0; i < sort_tab.rows.length; i++ )
  {
	  var sort_arr = new Array();
	  for( var j = 0; j < _SKIT_TABLE_VIEW_ARRAY.length; j++ )
	  {
		  sort_arr.push(_SKIT_TABLE_VIEW_ARRAY[j].rows[i]);
	  }
	  var cell = sort_tab.rows[i].cells[col];

	  if( !cell.innerText && !cell.firstChild.value && !cell.firstChild.innerText )
	  {
			return;
	  }

	  if( cell.firstChild.value )
	  {
	      tab_arr.push(new Array(cell.firstChild.value.toLowerCase(), sort_arr));
	  }
	  else if( cell.firstChild.innerText )
	  {
	      tab_arr.push(new Array(cell.firstChild.innerText.toLowerCase(), sort_arr));
	  }
	  else
	  {
	      tab_arr.push(new Array(sort_tab.rows[i].cells[col].innerText.toLowerCase(), sort_arr));
	  }
  }

  function SortArr(mode) {
    return function (arr1, arr2){
      var flag;
      var a,b;
      a = arr1[0];
      b = arr2[0];	
	  if((a.charAt(a.length-1)=="%" && b.charAt(b.length-1)=="%") || (a.charAt(a.length-1)=="??" && b.charAt(b.length-1)=="??"))
	  {
		a = a.substr(0,a.length-1);
		b = b.substr(0,b.length-1);
	  }		
      if(/^(\+|-)?\d+($|\.\d+$)/.test(a) && /^(\+|-)?\d+($|\.\d+$)/.test(b)){		  
        a=eval(a);
        b=eval(b);
        flag=mode?(a>b?1:(a<b?-1:0)):(a<b?1:(a>b?-1:0));
      }else{
        a=a.toString();
        b=b.toString();
        if(a.charCodeAt(0)>=19968 && b.charCodeAt(0)>=19968){
          flag = judge_CN(a,b,mode);
        }else{
          flag=mode?(a>b?1:(a<b?-1:0)):(a<b?1:(a>b?-1:0));
        }
      }
      return flag;
    };
  }
  tab_arr.sort(SortArr(mode));
  for( var j = 0; j < _SKIT_TABLE_VIEW_ARRAY.length; j++ )
  {
	  for( var i = 0; i < tab_arr.length; i++)
	  {
	  	  _SKIT_TABLE_VIEW_ARRAY[j].lastChild.appendChild(tab_arr[i][1][j]);
	  }
  }  

  //window.status = " (Time spent: " + (new Date - start) + "ms)";
}
function SKIT_PANELTITLE_SET()
{
	if( parent )
	{
		var paneltitle = document.getElementById( "paneltitle" );
		if( parent.frames.navFrame.window._SKIT_MENU_SELECTED && paneltitle)
		{
			paneltitle.innerText = parent.frames.navFrame.window._SKIT_MENU_SELECTED.innerText;
		}
	}
}
function SKIT_TREE_RESET()
{
	if( parent )
	{
		var paneltitle = document.getElementById( "paneltitle" );
		var treemenu = null;
		if( parent.frames.navFrame.window._SKIT_MENU_SELECTED )
		{
			treemenu = parent.frames.navFrame.window._SKIT_MENU_SELECTED;
		}
		if( treemenu && paneltitle)
		{
			var win = parent.frames.navFrame.window;
			if( treemenu.innerText != paneltitle.innerText )
			{
				var tb_tree = win.document.getElementById('tbMenu');
				for( var i = 0 ;i < tb_tree.rows.length; i++ )
				{
					if( tb_tree.rows[i].cells[0].childNodes.length > 0 )
					{
						var table = tb_tree.rows[i].cells[0].childNodes[0];
						if( table.rows[0].cells[1].innerText == paneltitle.innerText )
						{
							if( win._SKIT_MENU_SELECTED )
							{
								if( win._SKIT_MENU_SELECTED.uniqueID == 
									table.rows[0].cells[1].uniqueID )
								{
									return;
								}
								win._SKIT_MENU_SELECTED.style.color="#FFFFFF";
							}

							win._SKIT_MENU_SELECTED = table.rows[0].cells[1];
							win._SKIT_MENU_SELECTED.style.color = "#FFF000";
							return;
						}
					}
				}		
			}
		}		
	}
}

var _w = 0;
var _h = 0;
function SKIT_DIV_RESIZE()
{
	if( parent )
	{
		var h = parent.window.document.body.clientHeight;
		var w = parent.window.document.body.clientWidth;
		var frame = document.getElementById('fe');
		var div = document.getElementById('feview');
		if( parent.frames.navFrame )
			w -= 172;
		if( parent.frames.topFrame )
			h -= 91;
		if( _w == 0 && _h == 0 )
		{
			for(var i = 0; i < frame.rows.length; i++)
			{
				if( i != 2 )
				{
					_h += frame.rows[i].clientHeight;
				}
				else
				{
					_w += frame.rows[i].cells[0].clientWidth;
					_w += frame.rows[i].cells[2].clientWidth;
				}
			}
		}
		div.style.width = w - _w;
		div.style.height = h - _h;
	}
}

function SKIT_WIN_COLLAPSE()
{
	var args = SKIT_WIN_COLLAPSE.arguments;
	var img = args[0];
	var td = img.parentNode.parentNode.parentNode.rows[1];
	var imgurl = '';
	if( args.length == 2 )
	{
		imgurl = args[1];
	}
	
	if( td != null )
	{
		if( td.style.display == '' )
		{
			td.style.display='none';
			img.src = imgurl+'icons/expand.gif';
			img.alt = "";
		}
		else
		{
			td.style.display='';
			img.src = imgurl+'icons/collapse.gif';
			img.alt = "";
		}
	}
}

function skitPageMove()
{
	var args = skitPageMove.arguments;
	skit_alert("");
}

function clickRow()
{
}
/*
*/
function selectRow()
{
    var args = selectRow.arguments;
    var tbl = args[0];
    var callback_click = args.length>1?args[1]:null;
    var rowObj = null;
    var curevent = getEvent();
    if( curevent )
    {
    	var eventObj = curevent.srcElement?curevent.srcElement:curevent.target;
	    rowObj = _getMyElement(eventObj, "tr");
	}
	var rowClick;
    if (rowObj)
    {
        var len = tbl.rows.length;
        for(var i = 1; i < len; i++ )
        {
		    if(tbl.rows[i]==rowObj)
            {
            	rowClick = tbl.rows[i];
                if( callback_click )
                {//点击行之后回调
                	callback_click(rowClick);
                }            	
                for (var j=0; j<tbl.rows[i].cells.length; j++)
                {
                    var cell = tbl.rows[i].cells[j];
                    var k = cell.className.indexOf("skit_table_row_selected");
                    if( k != -1 )
                    {
                    	cell.className = cell.className.substring(0, k - 1);
                    }
                    else
                    {
						cell.className += " skit_table_row_selected";
					}
					if( cell.childNodes &&
						cell.childNodes.length > 0 &&
						cell.childNodes[0].style && 
						cell.innerHTML.indexOf("skit_input_label") != -1 )
					{
						k = cell.childNodes[0].className.indexOf("skit_table_row_selected");
	                    if( k != -1 )
	                    {
	                    	cell.childNodes[0].className = cell.className.substring(0, k - 1);
	                    }
	                    else
	                    {
							cell.childNodes[0].className += " skit_table_row_selected";
						}
					}
                }
		    }
            else            
            {
                for (var j=0; j<tbl.rows[i].cells.length; j++)
                {
                	//ȡ��td�Ķ���
                    var cell = tbl.rows[i].cells[j];
					var k = cell.className.indexOf(' ');
					if( k != -1 )
					{
						cell.className = cell.className.substring(0, k);
					}
					if( cell.childNodes && 
						cell.childNodes.length > 0 && 
						cell.childNodes[0].style && 
						cell.innerHTML.indexOf("skit_input_label") != -1 )
					{
						k = cell.childNodes[0].className.indexOf(' ');
						if( k != -1 )
						{
							cell.childNodes[0].className = cell.childNodes[0].className.substring(0, k);
						}
					}
                }
            }
	    }
	    if( rowClick )
	    {
            clickRow(rowClick);
        }
    }
}

/*选择表的行，回调触发*/
function selectRowEx()
{
    var args = selectRowEx.arguments;
    var tbl = args[0];
    var callback_click = args.length>1?args[1]:null;
    var rowObj = null;
    var curevent = getEvent();
    if( curevent )
    {
    	var eventObj = curevent.srcElement?curevent.srcElement:curevent.target;
	    rowObj = _getMyElement(eventObj, "tr");
	}
    if (rowObj)
    {
        var len = tbl.rows.length;
        for(var i=0;i<len;i++)
        {
		    if(tbl.rows[i]==rowObj)
            {
                var k = rowObj.className.indexOf("skit_table_row_selected");
                if( k != -1 )
                {
                	rowObj.className = rowObj.className.substring(0, k - 1);
                }
                else
                {
					rowObj.className += " skit_table_row_selected";
				}
                if( callback_click )
                {//点击行之后回调
                	callback_click(tbl.rows[i]);
	            }
            }
            else
            {
                var k = tbl.rows[i].className.indexOf("skit_table_row_selected");
                if( k != -1 )
                {
                	tbl.rows[i].className = tbl.rows[i].className.substring(0, k - 1);
                }
            }
	    }
    }
}

function clickCell()
{
}
function selectCell()
{
    var args = selectCell.arguments;
    var activeBgColor = '#ced9cc';
    var tbl = args[0];
    var beginRow = args.length>1?args[1]:1;
    var eventObj = event.srcElement;
    var cellObj = _getMyElement(eventObj, "td");
	var cellClick;
    if (cellObj)
    {
        var len = tbl.rows.length;
        for(var i = beginRow; i < len; i++ )
        {
        	var row = tbl.rows[i];
            for(var j = 0; j < row.cells.length; j++ )
        	{
				var cell = row.cells[j];
			    if(cell==cellObj)
	            {
	            	cellClick = cell;
	            	if( cell.style.backgroundColor != activeBgColor )
	            	{
		                cell.style.backgroundColor = activeBgColor;
			    	}
			    	else
			    	{
			    		cell.style.backgroundColor = "";
	                }
			    }
        	}
	    }
	    if( cellClick )
	    {
            clickRow(cellClick);
        }
    }
}
function _getMyElement(ele,tag)
{
	tag = tag.toLowerCase();
	if(ele.tagName.toLowerCase()==tag)return ele;

	while(ele=ele.parentElement)
	{
		if(ele.tagName.toLowerCase()==tag)return ele;
	}
	return(null);
}

function mousePosition(ev)
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
function getTop(e)
{
	var offset = e.offsetTop;
	if( e.offsetParent != null)
	{
		offset += getTop(e.offsetParent);
	}
	return offset;
}
function getLeft(e)
{
	var offset = e.offsetLeft;
	if(e.offsetParent!=null) 
	{
		offset += getLeft(e.offsetParent);
	}
	return offset;
} 

/**
	 * 取得所选预案id列,ex: 1, 3, 10
	*/
	function getCheckSelectedIds(checkName)
	{
		var ids = ""; 
		var cbg = document.all[checkName];
		if( !cbg )
		{
			return ids;
		}
		if(cbg.length)
		{ 
			//数组,有多于一个预案
			for(var i=0; i<cbg.length; i++)
			{
				if(cbg[i].checked )
				{
					ids += "," + cbg[i].value;
				}				
			}
		}
		else
		{
			if(cbg.checked)
			{
				ids +=  "," + cbg.value;
			}
		}
		if( ids!="" )
		{
			ids = ids.substring(1);
		}
		return ids;
	}
/**
 * 将复选框的全选或全不选
 */	
function skit_checkall(checkall,subchecked)
{ 
  	if(checkall)
  	{ 
    	var OnOff = checkall.checked;
    	var theItem = document.getElementsByName(subchecked); 
    	if (theItem!=null)
    	{
      		if (typeof(theItem.value)=='undefined')
      		{ 
        		for (var i=0;i<theItem.length; i++)
        		{
          			var SubItem = theItem[i];
          			if(SubItem.disabled==false)
          			{ 
          				SubItem.checked = OnOff; 
          			}
        		} 
            }
            else
            { 
      			if(theItem.disabled==false)
      			{
        			theItem.checked = OnOff; 
        		}
      		} 
    	} 
  	} 
} 
function checkAll(checkall,subchecked)
{ 
  	var OnOffItem = document.getElementsByName(checkall)[0];
  	if (OnOffItem!=null)
  	{ 
    	var OnOff = OnOffItem.checked;
    	var theItem = document.getElementsByName(subchecked); 
    	if (theItem!=null)
    	{ 
      		if (typeof(theItem.value)=='undefined')
      		{ 
        		for (var i=0;i<theItem.length; i++)
        		{
          			var SubItem = theItem[i];
          			if(SubItem.disabled==false)
          			{ 
          				SubItem.checked = OnOff; 
          			}
        		} 
            }
            else
            { 
      			if(theItem.disabled==false)
      			{
        			theItem.checked = OnOff; 
        		}
      		} 
    	} 
  	} 
} 
/**
 * 将复选框的全选或全不选
 */	
function checkOne(checkall,subchecked)
{ 
  	var OnOffItem = document.getElementsByName(checkall)[0];
  	if (OnOffItem!=null)
  	{ 
    	var i_checked = 0;
    	var theItem = document.getElementsByName(subchecked); 
    	if (theItem!=null)
    	{
      		if (typeof(theItem.value)=='undefined')
      		{ 
        		for (var i=0;i < theItem.length; i++)
        		{ 
          			var SubItem = theItem[i]; 
          			if(SubItem.checked)
          			{
          				i_checked++;
          			}
        		} 
        		if(i_checked==theItem.length)
        		{
        			 OnOffItem.checked = true;
        		}else
        		{
        			OnOffItem.checked = false;
        		}
      		}
      		else
      		{ 
      			var o = theItem.checked;
        		OnOffItem.checked = o; 
      		} 
    	} 
  	} 
}
/**
 * 复选框:选择或取消选择所有
 * 设定已有:
 * <input type="checkbox" name="CbGroup" value="1" />
 * <input type="checkbox" name="CbGroup" value="2" />
 * ...
 * 使用 <input type="checkbox" name="CbSelectAll" id="CbSelectAll" onclick="selectAll('CbGroup', this.checked)" />
 * 即可设定选择或取消选择所有
 */
function selectAll(checkboxGroup, isChecked)
{
	var cbg = document.getElementsByName(checkboxGroup);
	if( cbg.length )
	{ 
		for(var i=0; i<cbg.length; i++)
		{
			if(cbg[i].disabled==false)
			{
				cbg[i].checked = isChecked;
			}
		}
	}
	else
	{
		if(cbg.disabled==false)
		{
			cbg.checked = isChecked;
		}
	}
}

/**
 * 复选框:更改全选选框状态
 * 设定已有:
 * <input type="checkbox" name="CbSelectAll" id="CbSelectAll" onclick="selectAll('CbGroup', this.checked)" />
 * 使用 
 * <input type="checkbox" name="CbGroup" value="1" onclick="selectOne('CbSelectAll', this)" />
 * <input type="checkbox" name="CbGroup" value="2" onclick="selectOne('CbSelectAll', this)" />
 * ...
 * 即可设定CbSelectAll的选择状态
 */
function selectOne(CbSelectAll, CbOne)
{
	var cbx = document.getElementById(CbSelectAll) || document.getElementsByName(CbSelectAll)[0];
	if ( !cbx ) return;

	if( !CbOne.checked )
	{
		cbx.checked = false;
	}
	else
	{
		var cbg = document.getElementsByName(CbOne.name);
		for(var i=0; i<cbg.length; i++)
		{
			if( !cbg[i].checked )
			{
				cbx.checked = false;
				return;
			}				
		}
		cbx.checked = true;
	}
}

/**
 * 复选框:取得所选复选框的id列,形如: (1, 3, 10)
 * 设定已有:
 * <input type="checkbox" name="CbGroup" value="1" />
 * <input type="checkbox" name="CbGroup" value="2" />
 * 被选择，则 getSelectedIds("CbGroup") 返回字符串"(1,2)"
 */
function getSelectedIds(checkboxGroup)
{
	var ids = ""; 
	var cbg = document.getElementsByName(checkboxGroup);
	if( !cbg ) return ids;

	if( cbg.length )
	{ 
		for(var i=0; i<cbg.length; i++)
		{
			if(cbg[i].checked )
			{
				ids += "," + cbg[i].value;
			}				
		}
	}
	else
	{
		if(cbg.checked )
		{
			ids +=  "," + cbg.value;
		}
	}
	if( ids!="" )
	{
		ids = ids.substring(1) ;
	}
	return ids;
}

function getSelectedTitle()
{
	var args = getSelectedTitle.arguments;
	var ids = ""; 
	var checkboxGroup = args.length>0?args[0]:"";
	var sperator = args.length>1?args[1]:",";
	var cbg = document.getElementsByName(checkboxGroup);
	if( !cbg ) return ids;
	if( cbg.length )
	{ 
		for(var i=0; i<cbg.length; i++)
		{
			if(cbg[i].checked )
			{
				ids += sperator + cbg[i].title;
			}				
		}
	}
	else if( cbg.value )
	{
		if(cbg.checked)
		{
			ids +=  sperator + cbg.title;
		}
	}
	if( ids != "" )
	{
		ids = ids.substring(1) ;
	}
	return ids;
}

/**
 * 得到所有复选框的值
 * 设定已有:
 * <input type="checkbox" name="CbGroup" value="1" />
 * <input type="checkbox" name="CbGroup" value="2" />
 * ，则 getAllIds("CbGroup") 返回字符串"(1,2)"
 */
function getAllIds(checkboxGroup)
{
	var ids = ""; 
	var cbg = document.getElementsByName(checkboxGroup);
	if( cbg )
	{
		if( cbg.length )
		{ 
			for(var i = 0; i < cbg.length; )
			{
				ids += "," + cbg[i].value;
				i += 1;
			}
		}
		else if( cbg.value )
		{
			ids +=  "," + cbg.value;
		}
		
		if( ids!="" )
		{
			ids = ids.substring(1) ;
		}
	}
	return ids;
}

function setSelectOption(options, value)
{
	options[0].selected = true;
	for(var i = 0; i < options.length; i++)
	{
		if( !options[i].selected && options[i].value == value )
		{
			options[i].selected = true;
			return true;
		}
	}
	return false;
}

function getSelectOption(options)
{
	for(var i = 0; i < options.length; i++)
	{
		if( options[i].selected )
		{
			return options[i].text;
		}
	}
	return "";
}
/**
 * 根据输入的数据字符串（,号分隔的 ）设置复选框
 */
function setCheckboxChecked(checkboxGroup, data)
{
	var cbg = document.getElementsByName(checkboxGroup);
	if( cbg )
	{
		if( cbg.length )
		{ 
			for(var i = 0; i < cbg.length; i++)
			{
				if(data.indexOf(cbg[i].value)!=-1)
				{
					cbg[i].checked = true;
				}
				else
				{
					cbg[i].checked = false;
				}
			}
		}
		else if(data.indexOf(cbg.value) != -1)
		{
			cbg.checked = true;
		}
		else
		{
			cbg.checked = false;
		}
	}
}
/**
 * 根据输入的数据字符串（,号分隔的 ）设置复选框
 */
function getRadioChecked(radios)
{
	var cbg = document.getElementsByName(radios);
	if( cbg )
	{
		if( cbg.length )
		{ 
			for(var i = 0; i < cbg.length; i++)
			{
				if( cbg[i].checked )
				{
					return cbg[i];
				}
			}
		}
		else if( cbg.checked )
		{
			return cbg;
		}
	}
	return null;
}

function setRadioChecked(checkboxGroup, data)
{
	var cbg = document.getElementsByName(checkboxGroup);
	if( cbg )
	{
		if( cbg.length )
		{
			for(var i = 0; i < cbg.length; i++)
			{
				if(data == cbg[i].value )
				{
					cbg[i].checked = true;
				}
			}
		}
		else if(data == cbg.value )
		{
			cbg.checked = true;
		}
	}
}

function setCheckboxDisabled(checkboxGroup, isDisabled)
{
	var cbg = document.getElementsByName(checkboxGroup);
	if( cbg )
	{
		if( cbg.length )
		{ 
			for(var i = 0; i < cbg.length; i++)
			{
				cbg[i].disabled = isDisabled;
			}
		}
		else if(data.indexOf(cbg.value) != -1)
		{
			cbg.disabled = isDisabled;
		}
		else
		{
			cbg.disabled = isDisabled;
		}
	}
}

/**
 * 根据输入的数据字符串（,号分隔的 ）设置复选框
 */
function skit_setCheckboxChecked(checkboxes, value)
{
	if( checkboxes )
	{
		if( checkboxes.length )
		{ 
			for(var i = 0; i < checkboxes.length; i++)
			{
				if(value.indexOf(checkboxes[i].value)!=-1)
				{
					checkboxes[i].checked = true;
				}
				else
				{
					checkboxes[i].checked = false;
				}
			}
		}
		else if(value.indexOf(checkboxes.value) != -1)
		{
			checkboxes.checked = true;
		}
		else
		{
			checkboxes.checked = false;
		}
	}
}
/**
 * 根据输入的数据字符串（,号分隔的 ）设置下拉框
 */
function skit_setSelectSelected(select, value)
{
	for( var i = 0; i < select.options.length; i++ )
	{
		if( select.options[i].value == value )
		{
			select.options[i].selected = true;
		} 
	}
}

function getEvent()
{
	if(document.all)
	{
		return window.event;//如果是ie
	}
	func = getEvent.caller;
	while(func!=null)
	{
		var arg0 = func.arguments[0];
		if(arg0)
		{
			if((arg0.constructor==Event || arg0.constructor ==MouseEvent)||
			  (typeof(arg0)=="object" && arg0.preventDefault && arg0.stopPropagation))
            {
				return arg0;
			}
		}
		func=func.caller;
	}
	return null;
}

//创建信息盒子
function createBoxHtml(innerHTML)
{
	return "<table width='100%' cellspacing='0' cellpadding='0' onclick='copyText(this)'>"+
		"<tr><td class='t_top_left'/>"+
		"<td class='t_top_middle'></td>"+
		"<td class='t_top_right'/></tr>"+
		"<tr><td class='t_middle_left'/>"+
		"<td class='t_middle_middle'>"+innerHTML+"</td>"+
		"<td class='t_middle_right'/>"+
		"</tr>"+
		"<tr><td class='t_bottom_left'/>"+
		"<td class='t_bottom_middle'/>"+
		"<td class='t_bottom_right'/>"+
		"</tr></table>";
}