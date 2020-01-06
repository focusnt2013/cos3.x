package com.focus.skit;

import java.util.ArrayList;

public class KComponent extends ArrayList<Object> implements Component
{
	private static final long serialVersionUID = -2865758387192540112L;
	protected String id = "component";
	protected String label = "";
	protected boolean display = true;
	protected String suid;

	protected KComponent()
	{
		this.id = Toolkit.getComponentId().toString();
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

	public String getPopMenuTag()
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < getComponentCount(); i++)
		{
			sb.append(getComponent(i).getPopMenuTag());
		}
		return sb.toString();
	}

	public String toString()
	{
		return "";
	}

	public Component addComponent(Component component)
	{
		add(component);
		return component;
	}

	public Component addComponent(Component component, int index)
	{
		add(index, component);
		return component;
	}

	public int getComponentCount()
	{
		return size();
	}
	
	public boolean hasChild()
	{
		return !this.isEmpty();
	}
	
	public Component getComponent(int index)
	{
		return (Component) get(index);
	}

	public Component[] getComponents()
	{
		Component[] arry = new Component[getComponentCount()];
		toArray(arry);
		return arry;
	}

	public String getId()
	{
		return this.id;
	}

	public String getLabel()
	{
		return this.label;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public void setDisplay(boolean display)
	{
		this.display = display;
	}

	public boolean isDisplay()
	{
		return this.display;
	}

	public String getSuid()
	{
		return suid != null ? suid : id;
	}

	public void setSuid(String suid)
	{
		this.suid = suid;
	}
}