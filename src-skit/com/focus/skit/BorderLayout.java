/*     */ package com.focus.skit;
/*     */ 
/*     */ 
/*     */ 
/*     */ public class BorderLayout
/*     */   implements Layout
/*     */ {
/*     */   public static final int BORDER_TYPE_VERTICAL = 0;
/*     */   
/*     */ 
/*     */   public static final int BORDER_TYPE_HORIZONTAL = 1;
/*     */   
/*     */ 
/*     */   public static final String BORDER_ALIGN_RIGHT = "right";
/*     */   
/*     */ 
/*     */   public static final String BORDER_ALIGN_CENTER = "center";
/*     */   
/*     */ 
/*     */   public static final String BORDER_ALIGN_LEFT = "left";
/*     */   
/*     */ 
/*     */   public static final String BORDER_VALIGN_TOP = "top";
/*     */   
/*     */   public static final String BORDER_VALIGN_MIDDLE = "middle";
/*     */   
/*     */   public static final String BORDER_VALIGN_BOTTOM = "bottom";
/*     */   
/*  29 */   private String valign = "top";
/*  30 */   private String align = "left";
/*     */   private int type;
/*  32 */   private int width = -1;
/*  33 */   private int height = -1;
/*  34 */   private boolean tabToWidth = false;
/*     */   
/*     */   public BorderLayout(int type)
/*     */   {
/*  38 */     setType(type);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public String layoutContainer(Container parent)
/*     */   {
/*  50 */     if (parent.getComponentCount() == 0) {
/*  51 */       return "";
/*     */     }
/*     */     
/*  54 */     StringBuffer sb = new StringBuffer();
/*  55 */     String extendStyle = this.tabToWidth ? "width='100%' " : "";
/*  56 */     if (getType() == 0) {
/*  57 */       sb.append("\n<Table " + extendStyle + 
/*  58 */         "cellspacing=0 cellpadding=0 class='skit_view_base'>");
/*  59 */       for (int i = 0; i < parent.getComponentCount(); i++) {
/*  60 */         if (i > 0) {
/*  61 */           sb.append("\n<Tr height='1' ");
/*  62 */           sb.append(parent.getComponent(i).isDisplay() ? ">" : 
/*  63 */             "style='display:none'>");
/*  64 */           sb.append("<Td/></Tr>");
/*     */         }
/*  66 */         sb.append("\n<Tr id='" + parent.getComponent(i).getId() + 
/*  67 */           "layout' ");
/*  68 */         sb.append(parent.getComponent(i).isDisplay() ? "" : 
/*  69 */           "style='display:none'");
/*  70 */         sb.append("><Td>");
/*  71 */         sb.append(parent.getComponent(i));
/*  72 */         sb.append("\n</Td></Tr>");
/*     */       }
/*  74 */       sb.append("\n</Table>");
/*     */     }
/*     */     else {
/*  77 */       sb.append("\n<Table " + extendStyle + 
/*  78 */         " cellspacing=0 cellpadding=0 class='skit_view_base'><TR>");
/*  79 */       for (int i = 0; i < parent.getComponentCount(); i++) {
/*  80 */         if (i > 0) {
/*  81 */           sb.append("\n<Td width='2' ");
/*  82 */           sb.append(parent.getComponent(i).isDisplay() ? ">" : 
/*  83 */             "style='display:none'>");
/*     */         }
/*  85 */         sb.append("\n<Td id='" + parent.getComponent(i).getId() + 
/*  86 */           "layout' ");
/*  87 */         sb.append(parent.getComponent(i).isDisplay() ? ">" : 
/*  88 */           "style='display:none'>");
/*  89 */         sb.append(parent.getComponent(i));
/*  90 */         sb.append("\n</Td>");
/*     */       }
/*  92 */       sb.append("\n</Tr></Table>");
/*     */     }
/*  94 */     return sb.toString();
/*     */   }
/*     */   
/*     */ 
/*     */   public int getType()
/*     */   {
/* 100 */     return this.type;
/*     */   }
/*     */   
/*     */   public String getAlign()
/*     */   {
/* 105 */     return this.align;
/*     */   }
/*     */   
/*     */   public String getValign()
/*     */   {
/* 110 */     return this.valign;
/*     */   }
/*     */   
/*     */   public int getHeight()
/*     */   {
/* 115 */     return this.height;
/*     */   }
/*     */   
/*     */   public void setAlign(String align)
/*     */   {
/* 120 */     this.align = align;
/*     */   }
/*     */   
/*     */   public void setType(int type)
/*     */   {
/* 125 */     this.type = type;
/*     */   }
/*     */   
/*     */   public void setValign(String valign)
/*     */   {
/* 130 */     this.valign = valign;
/*     */   }
/*     */   
/*     */   public void setWidth(int width)
/*     */   {
/* 135 */     this.width = width;
/*     */   }
/*     */   
/*     */   public void setHeight(int height)
/*     */   {
/* 140 */     this.height = height;
/*     */   }
/*     */   
/*     */   public void setTabToWidth(boolean tabToWidth)
/*     */   {
/* 145 */     this.tabToWidth = tabToWidth;
/*     */   }
/*     */   
/*     */   public int getWidth()
/*     */   {
/* 150 */     return this.width;
/*     */   }
/*     */   
/*     */   public boolean isTabToWidth()
/*     */   {
/* 155 */     return this.tabToWidth;
/*     */   }
/*     */ }


/* Location:              D:\focusnt\cos\trunk\CODE\cos\lib\web-lib\skit-1.1.jar!\com\focus\skit\BorderLayout.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */