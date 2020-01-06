/*当鼠标移动到视图菜单项上的时候触发*/
function SKIT_TREE_MOUSEOVER(tree_item)
{
	tree_item.className = "skit_tree_item_over";

}

/*当鼠标移出视图菜单项上的时候触发*/
function SKIT_TREE_MOUSEOUT(tree_item)
{	
	tree_item.className = "";
}

/*被选择的视图菜单项*/
var _SKIT_MENU_SELECTED;
/*被选择的视图菜单项*/
var _SKIT_ACTION_SHOW;

/*当鼠标移出视图菜单项上的字体的时候触发*/
function SKIT_MENU_OUT()
{
	var args = SKIT_MENU_OUT.arguments;
	if( _SKIT_MENU_SELECTED )
	{
		if( _SKIT_MENU_SELECTED != args[0] )
		{
			args[0].className = "skit_tree_item";
		}
	}
	else
	{
		args[0].className = "skit_tree_item";
	}
}
/*当鼠标移到视图菜单项上的字体的时候触发*/
function SKIT_MENU_OVER()
{
	var args = SKIT_MENU_OVER.arguments;
	if( _SKIT_MENU_SELECTED )
	{
		if( _SKIT_MENU_SELECTED != args[0] )
		{
			args[0].className = "skit_tree_item_click";
		}
	}
	else
	{
		args[0].className = "skit_tree_item_click";
	}
}


function SKIT_ACTION_ITEM_OVER()
{
	var args = SKIT_ACTION_ITEM_OVER.arguments;
	var item = args[0];
	if( item.firstChild )
	{
		if( item.firstChild.style )
		{
			item.firstChild.className = "skit_actions_font_click";
		}
	}
	item.className = 'skit_actions_item_over';
}

function SKIT_ACTION_ITEM_OUT()
{
	var args = SKIT_ACTION_ITEM_OUT.arguments;
	var item = args[0];
	if( item.firstChild )
	{
		if( item.firstChild.style )
		{
			item.firstChild.className = "skit_actions_font";
		}
	}
	item.className = 'skit_actions_item';
	if( _SKIT_CURRENT_POPMENU_PICED )
	{
		var p = item.firstChild;
		if( p && p != _SKIT_CURRENT_POPMENU_PICED )
		{
			SKIT_HIDE_POPMENU();
		}
	}
}
/*菜单项鼠标事件函数*/
function SKIT_MENU_ITEM_OUT()
{
	var item = SKIT_MENU_ITEM_OUT.arguments[0];
	var par = item.parentElement;
	if( par.cells.length == 3 )
	{
		par.cells[0].className = 'skit_menu_item_left';
		par.cells[1].className = 'skit_menu_item_middle';
		par.cells[2].className = 'skit_menu_item_right';
	}
}
function SKIT_MENU_ITEM_OVER()
{
	var item = SKIT_MENU_ITEM_OVER.arguments[0];
	var par = item.parentElement;
	if( par.cells.length == 3 )
	{
		par.cells[0].className = 'skit_menu_item_over_left';
		par.cells[1].className = 'skit_menu_item_over_middle';
		par.cells[2].className = 'skit_menu_item_over_right';
	}
}