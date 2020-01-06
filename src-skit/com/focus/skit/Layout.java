package com.focus.skit;

public abstract interface Layout
{
  public abstract String layoutContainer(Container paramContainer);
  
  public abstract int getWidth();
  
  public abstract int getHeight();
  
  public abstract void setWidth(int paramInt);
  
  public abstract void setHeight(int paramInt);
  
  public abstract void setTabToWidth(boolean paramBoolean);
  
  public abstract int getType();
}


/* Location:              D:\focusnt\cos\trunk\CODE\cos\lib\web-lib\skit-1.1.jar!\com\focus\skit\Layout.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */