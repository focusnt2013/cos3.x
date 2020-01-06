/*     */ package com.focus.skit.table;
/*     */ 
/*     */ import com.focus.skit.Component;
/*     */ import com.focus.skit.DefaultComponent;
/*     */ import com.focus.skit.Toolkit;
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
/*     */ public class KAttrItem
/*     */   implements Component
/*     */ {
/*     */   private String id;
/*     */   private String name;
/*     */   private String value;
/*     */   private String nameStyle;
/*     */   private String valueStyle;
/*     */   private DefaultComponent valueCtrl;
/*  26 */   private boolean lastRow = false;
/*  27 */   private boolean display = true;
/*     */   
/*     */   public KAttrItem(String name, String value)
/*     */   {
/*  31 */     this.name = name;
/*  32 */     this.value = value;
/*  33 */     this.id = Toolkit.getComponentId().toString();
/*     */   }
/*     */   
/*     */   public KAttrItem(String value)
/*     */   {
/*  38 */     this.value = value;
/*  39 */     this.id = Toolkit.getComponentId().toString();
/*     */   }
/*     */   
/*     */   public KAttrItem(String name, DefaultComponent valueCtrl)
/*     */   {
/*  44 */     this.name = name;
/*  45 */     this.valueCtrl = valueCtrl;
/*  46 */     this.id = Toolkit.getComponentId().toString();
/*     */   }
/*     */   
/*     */   public KAttrItem(DefaultComponent valueCtrl)
/*     */   {
/*  51 */     this.valueCtrl = valueCtrl;
/*  52 */     this.id = Toolkit.getComponentId().toString();
/*     */   }
/*     */   
/*     */   public String toString()
/*     */   {
/*  57 */     if ((getValue() == null) && (this.valueCtrl == null)) {
/*  58 */       return "";
/*     */     }
/*     */     
/*  61 */     int colspan = 1;
/*  62 */     StringBuffer sb = new StringBuffer();
/*  63 */     sb.append("\n<tr><td style='width:6px'/>");
/*  64 */     if (getName() != null) {
/*  65 */       if (isLastRow()) {
/*  66 */         sb.append("\n<td class='skit_attrtab_cell_ext' ");
/*     */       }
/*     */       else {
/*  69 */         sb.append("\n<td class='skit_attrtab_cell' ");
/*     */       }
/*     */       
/*  72 */       if (this.nameStyle != null) {
/*  73 */         sb.append("style='" + this.nameStyle + "'");
/*     */       }
/*  75 */       sb.append(">");
/*  76 */       sb.append(this.name);
/*  77 */       sb.append("</td>");
/*     */     }
/*     */     else {
/*  80 */       colspan = 2;
/*     */     }
/*  82 */     if (isLastRow()) {
/*  83 */       sb.append("\n<td class='skit_attrtab_cell_ext' ");
/*     */     }
/*     */     else {
/*  86 */       sb.append("\n<td class='skit_attrtab_cell' ");
/*     */     }
/*     */     
/*  89 */     if (colspan > 1) {
/*  90 */       sb.append("colspan='" + colspan + "' ");
/*     */     }
/*  92 */     if (this.valueStyle != null) {
/*  93 */       sb.append("style='" + this.valueStyle + "'");
/*     */     }
/*  95 */     sb.append(">");
/*  96 */     sb.append(this.value != null ? this.value : this.valueCtrl.toString());
/*  97 */     sb.append("</td>");
/*  98 */     sb.append("\n<td style='width:6px'/></tr>");
/*  99 */     return sb.toString();
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public Component addComponent(Component component, int index)
/*     */   {
/* 112 */     return null;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public Component addComponent(Component component)
/*     */   {
/* 124 */     return null;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public Component getComponent(int index)
/*     */   {
/* 136 */     return null;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public int getComponentCount()
/*     */   {
/* 147 */     return 0;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public Component[] getComponents()
/*     */   {
/* 158 */     return null;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public String getId()
/*     */   {
/* 169 */     return this.id;
/*     */   }
/*     */   
/*     */   public String getName()
/*     */   {
/* 174 */     return this.name;
/*     */   }
/*     */   
/*     */   public String getValue()
/*     */   {
/* 179 */     return this.value;
/*     */   }
/*     */   
/*     */   public String getValueStyle()
/*     */   {
/* 184 */     return this.valueStyle;
/*     */   }
/*     */   
/*     */   public String getNameStyle()
/*     */   {
/* 189 */     return this.nameStyle;
/*     */   }
/*     */   
/*     */   public boolean isLastRow()
/*     */   {
/* 194 */     return this.lastRow;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public String getLabel()
/*     */   {
/* 205 */     return this.name;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public String getPopMenuTag()
/*     */   {
/* 216 */     return "";
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
/* 227 */     return "";
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public void setId(String id)
/*     */   {
/* 238 */     this.id = id;
/*     */   }
/*     */   
/*     */   public void setValueStyle(String valueStyle)
/*     */   {
/* 243 */     this.valueStyle = valueStyle;
/*     */   }
/*     */   
/*     */   public void setNameStyle(String nameStyle)
/*     */   {
/* 248 */     this.nameStyle = nameStyle;
/*     */   }
/*     */   
/*     */   public void setLastRow(boolean lastRow)
/*     */   {
/* 253 */     this.lastRow = lastRow;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public void setLabel(String label)
/*     */   {
/* 264 */     this.name = label;
/*     */   }
/*     */   
/*     */   public void setDisplay(boolean display)
/*     */   {
/* 269 */     this.display = display;
/*     */   }
/*     */   
/*     */ 
/*     */   public boolean isDisplay()
/*     */   {
/* 275 */     return this.display;
/*     */   }
/*     */ }


/* Location:              D:\focusnt\cos\trunk\CODE\cos\lib\web-lib\skit-1.1.jar!\com\focus\skit\table\KAttrItem.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */