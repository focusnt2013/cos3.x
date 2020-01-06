/*     */ package com.focus.skit.frame;
/*     */ 
/*     */ import com.focus.skit.Component;
/*     */ import com.focus.skit.Container;
/*     */ import com.focus.skit.Layout;
/*     */ import com.focus.skit.button.KButtonBar;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class KViewFrame
/*     */   extends Container
/*     */ {
/*     */   private KButtonBar btnBar;
/*  22 */   private String style = "";
/*     */   
/*     */   public KViewFrame(Layout layout, String title)
/*     */   {
/*  26 */     super(layout);
/*  27 */     setLabel(title);
/*     */   }
/*     */   
/*     */   public KViewFrame(String title)
/*     */   {
/*  32 */     super(null);
/*  33 */     setLabel(title);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public String getScrollId()
/*     */   {
/*  42 */     return getId() + "view";
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public String toString()
/*     */   {
/*  51 */     StringBuffer sb = new StringBuffer();
/*  52 */     sb.append("\n<Table id='" + getId() + 
/*  53 */       "' cellspacing='0' cellpadding='0' class='skit_view_base'>");
/*  54 */     sb.append("\n<tr><td colspan='3' height='2'/></tr>");
/*  55 */     sb.append("\n<tr>");
/*  56 */     sb.append("\n<td class='skit_view_top_left'/>");
/*  57 */     sb.append("\n<td class='skit_view_top_middle'>");
/*  58 */     sb.append("\n<!-- interal frame 标题区 -->");
/*  59 */     sb.append("\n<table cellspacing='0' cellpadding='0' width='100%'>");
/*  60 */     sb.append("\n<tr>");
/*  61 */     sb.append("\n<td class='skit_view_title' id='" + getId() + 
/*  62 */       "title'>");
/*  63 */     sb.append(getLabel() + "</td>");
/*  64 */     sb.append("\n<td align='right' id='" + getId() + 
/*  65 */       "button' class='pager_button'>");
/*  66 */     if (this.btnBar != null) {
/*  67 */       sb.append(this.btnBar);
/*     */     }
/*  69 */     sb.append("\n</td>");
/*  70 */     sb.append("\n</tr>");
/*  71 */     sb.append("\n</table>");
/*  72 */     sb.append("\n<!-- interal frame 标题区 -->");
/*  73 */     sb.append("\n</td>");
/*  74 */     sb.append("\n<td class='skit_view_top_right'/>");
/*  75 */     sb.append("\n</tr>");
/*     */     
/*  77 */     sb.append("\n<tr>");
/*  78 */     sb.append("\n<td class='skit_view_middle_left'/>");
/*  79 */     sb.append("\n<td class='skit_view_middle_middle'>");
/*     */     
/*  81 */     sb.append("\n<div class='skit_view_div' id='" + getId() + 
/*  82 */       "view' style='" + getStyle() + "'");
/*  83 */     sb.append(" style='width:" + getWidth());
/*  84 */     sb.append("px;height:" + getHeight() + "px'>");
/*     */     
/*  86 */     if (getLayout() != null) {
/*  87 */       sb.append(getLayout().layoutContainer(this));
/*     */     }
/*     */     else {
/*  90 */       for (int i = 0; i < getComponentCount(); i++) {
/*  91 */         sb.append("\n");
/*  92 */         sb.append(getComponent(i));
/*     */       }
/*     */     }
/*  95 */     sb.append("\n</div>");
/*  96 */     sb.append("\n</td>");
/*  97 */     sb.append("\n<td class='skit_view_middle_right'/>");
/*  98 */     sb.append("\n</tr>");
/*  99 */     sb.append("\n<tr><td class='skit_view_bottom_left'/><td class='skit_view_bottom_middle'/><td class='skit_view_bottom_right'/></tr>");
/* 100 */     sb.append("\n</table>");
/*     */     
/* 102 */     return sb.toString();
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public String getPopMenuTag()
/*     */   {
/* 111 */     StringBuffer sb = new StringBuffer();
/* 112 */     if (this.btnBar != null) {
/* 113 */       sb.append("\n");
/* 114 */       sb.append(this.btnBar.getPopMenuTag());
/*     */     }
/*     */     
/* 117 */     for (int i = 0; i < getComponentCount(); i++) {
/* 118 */       sb.append("\n");
/* 119 */       sb.append(getComponent(i).getPopMenuTag());
/*     */     }
/* 121 */     return sb.toString();
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public String getScriptTag()
/*     */   {
/* 132 */     StringBuffer sb = new StringBuffer();
/* 133 */     for (int i = 0; i < getComponentCount(); i++) {
/* 134 */       sb.append(getComponent(i).getScriptTag());
/*     */     }
/*     */     
/* 137 */     for (int i = 0; 
/* 138 */         (this.btnBar != null) && (i < this.btnBar.getComponentCount()); 
/* 139 */         i++) {
/* 140 */       sb.append(this.btnBar.getComponent(i).getScriptTag());
/*     */     }
/*     */     
/* 143 */     return sb.toString();
/*     */   }
/*     */   
/*     */   public KButtonBar getBtnBar()
/*     */   {
/* 148 */     return this.btnBar;
/*     */   }
/*     */   
/*     */   public String getStyle()
/*     */   {
/* 153 */     return this.style;
/*     */   }
/*     */   
/*     */   public void setBtnBar(KButtonBar btnBar)
/*     */   {
/* 158 */     this.btnBar = btnBar;
/*     */   }
/*     */   
/*     */   public void setStyle(String style)
/*     */   {
/* 163 */     this.style = style;
/*     */   }
/*     */ }


/* Location:              D:\focusnt\cos\trunk\CODE\cos\lib\web-lib\skit-1.1.jar!\com\focus\skit\frame\KViewFrame.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */