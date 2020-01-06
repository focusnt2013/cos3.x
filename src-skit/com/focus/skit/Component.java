package com.focus.skit;

public abstract interface Component
{
	public abstract Component addComponent(Component paramComponent);

	public abstract Component addComponent(Component paramComponent, int paramInt);

	public abstract int getComponentCount();

	public abstract Component getComponent(int paramInt);

	public abstract Component[] getComponents();

	public abstract String getId();

	public abstract String getLabel();

	public abstract void setId(String paramString);

	public abstract void setLabel(String paramString);

	public abstract boolean isDisplay();

	public abstract String getScriptTag();

	public abstract String getPopMenuTag();
}
