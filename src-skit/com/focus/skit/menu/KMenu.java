package com.focus.skit.menu;

import com.focus.skit.KComponent;

public class KMenu extends KComponent
{
	private static final long serialVersionUID = -3115774783954805488L;

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<div id='" + getId() + "' class='skit_popmenu'>");
		sb.append("<table id='" + getTbId() + "' cellspacing='0' cellpadding='0'>");
		sb.append("<tr><td class='skit_menu_box_top_left'/><td class='skit_menu_box_top_middle'/><td class='skit_menu_box_top_right'/></tr>");
		for (int i = 0; i < getComponentCount(); i++)
		{
			if (i > 0)
			{
				sb.append("<tr><td class='skit_menu_line_left'/><td class='skit_menu_line_middle'/><td class='skit_menu_line_right'/></tr>");
			}
			sb.append(getComponent(i));
		}
		sb.append("<tr><td class='skit_menu_box_bottom_left'/><td class='skit_menu_box_bottom_middle'/><td class='skit_menu_box_bottom_right'/></tr>");
		sb.append("</table></div>");

		return sb.toString();
	}

	public String getTbId()
	{
		return getId() + "_tb";
	}
}