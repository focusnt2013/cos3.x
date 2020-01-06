package com.focus.skit.tree;

import java.util.ArrayList;

import com.focus.skit.KComponent;

public class KTree extends KComponent
{
	private static final long serialVersionUID = 8240244674995541190L;
	private ArrayList<KAction> listActions = new ArrayList<KAction>();
	private String icon = null;
	private String defaultView = null;

	public void addAction(KAction action)
	{
		this.listActions.add(action);
	}
	
	/**
	 * 用户操作数据
	 * @return
	 */
	public ArrayList<KAction> getActions()
	{
		return this.listActions;
	}

	public String getScriptTag()
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < getComponentCount(); i++)
		{
			sb.append(getComponent(i).getScriptTag());
		}
		return sb.toString();
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<table class='skit_tree' id='tbTree' cellspacing='0' cellpadding='0'>\n");
		sb.append("<tr><td height='1'/></tr>\n");
		sb.append("<tr><td class='skit_tree_top'/></tr>\n");
		sb.append("<tr><td class='skit_tree_middle' valign='top'>");

		sb.append("\n<div id='divNavigation' class='skit_navigation'>");
//		sb.append("<table id='tbMenu' cellspacing='0' cellpadding='0'>");
//		for (int i = 0; i < getComponentCount(); i++)
//		{
//			sb.append(getComponent(i));
//		}
//		sb.append("</table></div>\n");
		sb.append("<ul id='treeCategory' class='tree'></ul>");
		sb.append("</div>\n");
		sb.append("</td></tr>\n");
		sb.append("<tr><td class='skit_tree_bottom'/></tr>\n");

		sb.append("<tr style='display:none' id='trActions'>");
		sb.append("<td class='skit_treelower_middle'>");
		sb.append("<table cellspacing='0' cellpadding='0'>");
		sb.append("<tr><td class='skit_actions_box_top'/></tr>");
		sb.append("<tr><td class='skit_actions_box_middle'>");

		for (int i = 0; i < this.listActions.size(); i++)
		{
			KAction action = (KAction) this.listActions.get(i);
			sb.append(action.toString());
		}
		sb.append("</td></tr><tr><td class='skit_actions_box_bottom'/></tr></table></td></tr>");

		sb.append("\n<tr><td class='skit_treelower_bottom'/></tr>");
		sb.append("\n</table>\n");
		for (int i = 0; i < this.listActions.size(); i++)
		{
			KAction action = (KAction) this.listActions.get(i);
			sb.append(action.getPopMenuTag());
		}
		return sb.toString();
	}

	public String getIcon()
	{
		return this.icon;
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
	}

	public String getDefaultView()
	{
		return defaultView;
	}

	public void setDefaultView(String defaultView)
	{
		this.defaultView = defaultView;
	}
}