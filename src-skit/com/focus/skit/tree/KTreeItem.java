package com.focus.skit.tree;

import com.focus.skit.KComponent;

public class KTreeItem extends KComponent
{
	private static final long serialVersionUID = -1120148226633819360L;
	private int paddingLeft = 0;
	private String viewHref = "#";
	private KAction action = null;
	private boolean hidden = false;
	private String icon = null;
	private int paddingLeftSeed = 12;
	private String funcName;
	private String target;
	
	public KTreeItem(String name, String view, KAction action, String icon)
	{
		setLabel(name);
		this.viewHref = view;
		this.action = action;
		this.icon = icon;
	}

	public KTreeItem(String name, String view, KAction action)
	{
		this(name, view, action, null);
	}

	public KTreeItem(String name, String view, String icon)
	{
		this(name, view, null, icon);
	}

	public KTreeItem(String name, String view)
	{
		this(name, view, null, null);
	}

	public KTreeItem(String name)
	{
		this(name, "#", null, null);
	}

	public String getPopMenuTag()
	{
		if (this.action != null)
		{
			return this.action.getPopMenuTag();
		}

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < getComponentCount(); i++)
		{
			KTreeItem item = (KTreeItem) getComponent(i);
			sb.append(item.getPopMenuTag());
		}
		return sb.toString();
	}

	public String getScriptTag()
	{
		if (this.action != null)
		{
			StringBuffer sb = new StringBuffer();
			sb.append("\n/*" + getLabel() + "*/\n");
			sb.append(this.action.getScriptTag());
			return sb.toString();
		}
		return "";
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		if (getComponentCount() == 0)
		{
			sb.append("\n<tr onmouseout='SKIT_TREE_MOUSEOUT(this)' ");
			sb.append(this.hidden ? "style='display:none' " : "");
			sb.append("onmouseover='SKIT_TREE_MOUSEOVER(this)'><td");
			if (this.paddingLeft > 0)
			{
				sb.append(" style='padding-left:" + this.paddingLeft + "px' ");
			}
			sb.append("><table cellspacing='0' cellpadding='0'><tr>");
			if (this.icon == null)
			{
				sb.append("<td class='skit_tree_block'>");
			}
			else
			{
				sb.append("<td class='skit_tree_icon'>");
				sb.append("<i class='skit_fa_menu fa " + this.icon + "'></i>");
			}
			sb.append("</td>");
			sb.append("<td class='skit_tree_item' width='100%' ");
			sb.append("id='" + this.viewHref + "' ");
			sb.append("title='" + this.label + "' ");
			sb.append("onmouseout='SKIT_MENU_OUT(this)' ");
			sb.append("onmouseover='SKIT_MENU_OVER(this)' ");
			if (this.action != null)
			{
				sb.append("onclick='SKIT_OPEN_VIEW(\"" + this.viewHref + "\", this, \"" + this.action.getId() + "\")'>");
			}
			else
			{
				sb.append("onclick='SKIT_OPEN_VIEW(\"" + this.viewHref + "\", this)'>");
			}
			int offset = 0;
			for (int i = 0; i < this.label.length(); i++)
			{
				if (offset + 2 > 14)
				{
					this.label = (this.label.substring(0, i) + "*");
					break;
				}
				int c = this.label.charAt(i);
				offset += (c > 128 ? 2 : 1);
			}
			sb.append(this.label);
			sb.append("</td></tr></table></td></tr>");
			sb.append("\n<tr ");
			sb.append(this.hidden ? "style='display:none' " : "");
			sb.append("><td class='skit_tree_line'/></tr>\n");
		}
		else
		{
			sb.append("\n<tr onmouseout='SKIT_TREE_MOUSEOUT(this)' ");
			sb.append(this.hidden ? "style='display:none' " : "");
			sb.append("onmouseover='SKIT_TREE_MOUSEOVER(this)' ");
			sb.append("onclick='SKIT_TREE_EXPAND(this)'><td");
			if (this.paddingLeft > 0)
			{
				sb.append(" style='padding-left:" + this.paddingLeft + "px' ");
			}
			sb.append("><table cellspacing='0' cellpadding='0'><tr>");

			sb.append("<td class='skit_tree_exp'/>");

			if (this.icon == null)
			{
				sb.append("<td class='skit_tree_block'>");
			}
			else
			{
				sb.append("<td class='skit_tree_icon'>");
				sb.append("<i class='skit_fa_menu fa " + this.icon + "'></i>");
			}
			sb.append("</td>");

			sb.append("<td class='skit_tree_item' width='100%'");
			sb.append("title='" + this.label + "' ");
			sb.append("onmouseout='SKIT_MENU_OUT(this)' ");
			sb.append("onmouseover='SKIT_MENU_OVER(this)'>");
			int offset = 0;
			for (int i = 0; i < this.label.length(); i++)
			{
				if (offset + 2 > 14)
				{
					this.label = (this.label.substring(0, i) + "*");
					break;
				}
				int c = this.label.charAt(i);
				offset += (c > 128 ? 2 : 1);
			}
			sb.append(this.label);
			sb.append("</td></tr></table></td></tr>");
			sb.append("\n<tr ");
			sb.append(this.hidden ? "style='display:none' " : "");
			sb.append("><td class='skit_tree_line'/></tr>\n");

			for (int i = 0; i < getComponentCount(); i++)
			{
				KTreeItem item = (KTreeItem) getComponent(i);
				item.setPaddingLeft(this.paddingLeft + this.paddingLeftSeed);
				item.setHidden(true);
				sb.append(item);
			}
		}

		return sb.toString();
	}

	public boolean isHidden()
	{
		return this.hidden;
	}

	public int getPaddingLeft()
	{
		return this.paddingLeft;
	}

	public String getViewHref()
	{
		return this.viewHref;
	}

	public int getPaddingLeftSeed()
	{
		return this.paddingLeftSeed;
	}

	public KAction getAction()
	{
		return this.action;
	}

	public void setHidden(boolean hidden)
	{
		this.hidden = hidden;
	}

	public void setPaddingLeft(int paddingLeft)
	{
		this.paddingLeft = paddingLeft;
	}

	public void setViewHref(String viewHref)
	{
		this.viewHref = viewHref;
	}

	public void setPaddingLeftSeed(int paddingLeftSeed)
	{
		this.paddingLeftSeed = paddingLeftSeed;
	}

	public String getIcon()
	{
		return this.icon;
	}

	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
}