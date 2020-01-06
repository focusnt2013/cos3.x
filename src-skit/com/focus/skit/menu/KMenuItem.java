package com.focus.skit.menu;

import com.focus.skit.KComponent;
import com.focus.skit.tree.KAction;

public class KMenuItem extends KComponent
{
	private static final long serialVersionUID = -4841258400796456010L;
	private String href = "#";
	private String icon = null;
	private KAction action = null;
	private String module = null;
	private boolean hidden = false;
	private String funcName;
	private String target;
	
	public KMenuItem(String name)
	{
		setLabel(name);
		this.funcName = "alert('待实现');";
	}
	public KMenuItem(String name, String funcName)
	{
		setLabel(name);
		if (funcName.equals("delete"))
		{
			funcName = "deleteA";
		}
		this.funcName = funcName;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<tr><td class='skit_menu_item_left'/>");
		sb.append("<td class='skit_menu_item_middle' ");
		sb.append("id='" + getId() + "' ");
		sb.append("onmouseout='SKIT_MENU_ITEM_OUT(this)' ");
		sb.append("onmouseover='SKIT_MENU_ITEM_OVER(this)' ");
		sb.append("onclick='" + this.funcName + "()'>");
		sb.append(getLabel());
		sb.append("</td>");
		sb.append("<td class='skit_menu_item_right'/></tr>");

		return sb.toString();
	}

	public String getScriptTag()
	{
		StringBuffer sb = new StringBuffer();
		String remark = getLabel();
		if ((this.funcName != null) && (this.funcName.length() > 0))
		{
			if (remark != null)
			{
				remark = this.funcName;
			}

			String alert = "请实现函数--" + remark + "！";
			if (this.funcName.equals("delete"))
			{
				alert = "请不要使用" + this.funcName + "作为函数名称，因为与系统的关键字冲突!";
				this.funcName += "A";
			}
			sb.append("\n/*函数:" + remark + "*/");
			sb.append("\nfunction " + this.funcName + "()");
			sb.append("\n{");
			sb.append("\n    alert('" + alert + "');");
			sb.append("\n    SKIT_HIDE_POPMENU();");
			sb.append("\n}");
		}
		return sb.toString();
	}
	public String getHref()
	{
		return href;
	}
	public void setHref(String href)
	{
		this.href = href;
	}
	public KAction getAction()
	{
		return action;
	}
	public void setAction(KAction action)
	{
		this.action = action;
	}
	public String getModule()
	{
		return module;
	}
	public void setModule(String module)
	{
		this.module = module;
	}
	public boolean isHidden()
	{
		return hidden;
	}
	public void setHidden(boolean hidden)
	{
		this.hidden = hidden;
	}
	public String getIcon()
	{
		return icon;
	}
	public void setIcon(String icon)
	{
		this.icon = icon;
	}
	public void setFuncName(String funcName)
	{
		this.funcName = funcName;
	}

	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
}