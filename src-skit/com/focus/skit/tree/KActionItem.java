package com.focus.skit.tree;

import com.focus.skit.KComponent;
import com.focus.skit.menu.KMenu;

public class KActionItem extends KComponent
{
	private static final long serialVersionUID = -810105013205980923L;
	private String icon;
	private String action;
	private String viewHref;

	public KActionItem(String name, String action, String icon)
	{
		setLabel(name);
		this.action = action;
		this.viewHref = action;
		this.icon = icon;
	}

	public KActionItem(String name, String action)
	{
		setLabel(name);
		this.action = action;
	}

	public KActionItem(String name, KMenu popMenu, String icon)
	{
		setLabel(name);
		add(popMenu);
		this.icon = icon;
	}

	public KActionItem(String name, KMenu popMenu)
	{
		setLabel(name);
		add(popMenu);
	}

	public String getPopMenuTag()
	{
		StringBuffer sb = new StringBuffer();
		if (getComponentCount() == 1)
		{
			sb.append(getComponent(0));
		}
		return sb.toString();
	}

	public String getScriptTag()
	{
		return "";
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		if (getComponentCount() == 0)
		{
			sb.append("<tr><td id='" + getId() + "' class='skit_actions_item' ");
			sb.append("onmouseout='SKIT_ACTION_ITEM_OUT(this)' ");
			sb.append("onmouseover='SKIT_ACTION_ITEM_OVER(this)' ");
			sb.append("onclick='SKIT_OPEN_VIEW(\"" + this.action + "\")'>");
			sb.append("<table>");
			if (this.icon == null)
			{
				sb.append("<td class='skit_tree_block'>");
			}
			else
			{
				sb.append("<td class='skit_tree_icon'>");
				sb.append("<i class='skit_fa_menu fa " + this.icon + "'></i>");
			}
			sb.append("</td><td class='skit_actions_font'>");
			sb.append(getLabel());
			sb.append("</td></table>");
			sb.append("</td></tr>");
		}
		else
		{
			sb.append("<tr><td id='" + getId() + "' class='skit_actions_item' ");
			sb.append("onmouseout='SKIT_ACTION_ITEM_OUT(this)' ");
			sb.append("onmouseover='SKIT_ACTION_ITEM_OVER(this, \"" + getComponent(0).getId() + "\")'>");
			sb.append("<table width='100%' ");
			sb.append("onmouseover='SKIT_SHOW_POPMENU(\"" + getComponent(0).getId() + "\", \"show\", this)' ");
			sb.append("cellspacing='0' cellpadding='0' class='skit_action_popmenu' >");
			sb.append("<tr><td>" + getLabel() + "</td>");
			sb.append("<td width='20'><i class='skit_fa_menu fa fa-list'></i></td></tr>");
			sb.append("</table></td></tr>");
		}
		return sb.toString();
	}

	public String getAction()
	{
		return this.action;
	}

	public String getIcon()
	{
		return this.icon;
	}

	public String getViewHref()
	{
		return viewHref;
	}

	public void setViewHref(String viewHref)
	{
		this.viewHref = viewHref;
	}
}