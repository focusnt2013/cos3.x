/*    */ package com.focus.skit;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public abstract class Container
/*    */   extends KComponent
/*    */ {
/*    */   private Layout layout;
/*    */   
/*    */ 
/*    */   private int width;
/*    */   
/*    */ 
/*    */   private int height;
/*    */   
/*    */ 
/*    */   private Object constraints;
/*    */   
/*    */ 
/* 21 */   private String display = "";
/*    */   
/*    */   protected Container(Layout layout)
/*    */   {
/* 25 */     this.layout = layout;
/*    */   }
/*    */   
/*    */   public Container getContainer(int index)
/*    */   {
/* 30 */     return (Container)getComponent(index);
/*    */   }
/*    */   
/*    */   public Container addContainer(Container container)
/*    */   {
/* 35 */     return addContainer(container, null);
/*    */   }
/*    */   
/*    */   public Container addContainer(Container container, Object constraints)
/*    */   {
/* 40 */     container.setConstraints(constraints);
/* 41 */     return (Container)addComponent(container);
/*    */   }
/*    */   
/*    */   public Layout getLayout()
/*    */   {
/* 46 */     return this.layout;
/*    */   }
/*    */   
/*    */   public int getWidth()
/*    */   {
/* 51 */     return this.width;
/*    */   }
/*    */   
/*    */   public int getHeight()
/*    */   {
/* 56 */     return this.height;
/*    */   }
/*    */   
/*    */   public Object getConstraints()
/*    */   {
/* 61 */     return this.constraints;
/*    */   }
/*    */   
/*    */   public String getDisplay()
/*    */   {
/* 66 */     return this.display;
/*    */   }
/*    */   
/*    */   public void setLayout(Layout layout)
/*    */   {
/* 71 */     this.layout = layout;
/*    */   }
/*    */   
/*    */   public void setWidth(int width)
/*    */   {
/* 76 */     this.width = width;
/*    */   }
/*    */   
/*    */   public void setHeight(int height)
/*    */   {
/* 81 */     this.height = height;
/*    */   }
/*    */   
/*    */   public void setConstraints(Object constraints)
/*    */   {
/* 86 */     this.constraints = constraints;
/*    */   }
/*    */   
/*    */   public void setDisplay(String display)
/*    */   {
/* 91 */     this.display = display;
/*    */   }
/*    */ }


/* Location:              D:\focusnt\cos\trunk\CODE\cos\lib\web-lib\skit-1.1.jar!\com\focus\skit\Container.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */