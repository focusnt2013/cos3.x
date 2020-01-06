package com.focus.skit.tree;

import com.focus.skit.KComponent;

public class KAction extends KComponent
{
	private static final long serialVersionUID = 2769247979952090492L;

	public String getScriptTag()
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < getComponentCount(); i++)
		{
			sb.append(getComponent(i).getScriptTag());
		}
		return sb.toString();
	}

	public String getPopMenuTag()
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < getComponentCount(); i++)
		{
			KActionItem item = (KActionItem) getComponent(i);

			sb.append(item.getPopMenuTag());
		}
		return sb.toString();
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<table id='" + getId() + "' style='cursor:hand;display:none' cellspacing='0' cellpadding='0'>");
		for (int i = 0; i < getComponentCount(); i++)
		{
			if (i > 0)
			{
				sb.append("<tr><td class='skit_actions_line'/></tr>");
			}
			sb.append(getComponent(i));
		}
		sb.append("</table>");
		return sb.toString();
	}
}