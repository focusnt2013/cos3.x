/*     */ package com.focus.skit.table;
/*     */ 
/*     */ import com.focus.skit.KComponent;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ public class KTable
/*     */   extends KComponent
/*     */ {
/*     */   private KTableModel model;
/*     */   private String dblClickFunc;
/*  24 */   private int height = 22;
/*     */   
/*     */ 
/*     */   public KTable() {}
/*     */   
/*     */ 
/*     */   public KTable(KTableModel model)
/*     */   {
/*  32 */     this.model = model;
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public String getTableWidth()
/*     */   {
/*  41 */     return getTableWidth(0);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public String getTableWidth(int ex)
/*     */   {
/*  51 */     if (this.model != null) {
/*  52 */       int w = 0;
/*  53 */       for (int i = 0; i < this.model.getColumnCount(); i++) {
/*  54 */         if (this.model.getColWidth(i) == 0)
/*     */         {
/*  56 */           w = 0;
/*  57 */           break;
/*     */         }
/*  59 */         w += this.model.getColWidth(i);
/*     */       }
/*     */       
/*  62 */       if (w > 0) {
/*  63 */         return String.valueOf(w + ex);
/*     */       }
/*     */     }
/*  66 */     return "100%";
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   private void setTableHeader(StringBuffer sb)
/*     */   {
/*  75 */     if (getTableWidth().equals("100%")) {
/*  76 */       for (int i = 0; i < this.model.getColumnCount(); i++) {
/*  77 */         sb.append("\n<td class='skit_head_cell' ");
/*  78 */         if (i < this.model.getColumnCount() - 1) {
/*  79 */           if (this.model.getColWidth(i) <= 0) {
/*  80 */             this.model.setColWidth(i, 120);
/*     */           }
/*  82 */           sb.append("width='" + this.model.getColWidth(i) + "'");
/*     */         }
/*     */         else {
/*  85 */           this.model.setColWidth(i, 0);
/*     */         }
/*  87 */         sb.append(">");
/*  88 */         if ((this.model.getColumnName(i) == null) || 
/*  89 */           (this.model.getColumnName(i).length() == 0)) {
/*  90 */           sb.append("&nbsp;");
/*     */         }
/*     */         else {
/*  93 */           sb.append(this.model.getColumnName(i));
/*     */         }
/*  95 */         sb.append("</td>");
/*     */       }
/*     */       
/*     */     } else {
/*  99 */       for (int i = 0; i < this.model.getColumnCount(); i++) {
/* 100 */         sb.append("\n<td class='skit_head_cell' ");
/* 101 */         sb.append("width='" + this.model.getColWidth(i) + "'");
/* 102 */         sb.append(">");
/* 103 */         if ((this.model.getColumnName(i) == null) || 
/* 104 */           (this.model.getColumnName(i).length() == 0)) {
/* 105 */           sb.append("&nbsp;");
/*     */         }
/*     */         else {
/* 108 */           sb.append(this.model.getColumnName(i));
/*     */         }
/* 110 */         sb.append("</td>");
/*     */       }
/*     */     }
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public String getScrollId()
/*     */   {
/* 121 */     return getId() + "view";
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public String toString()
/*     */   {
/* 130 */     if (this.model == null) {
/* 131 */       return "";
/*     */     }
/*     */     
/* 134 */     StringBuffer sb = new StringBuffer();
/* 135 */     sb.append("\n<table cellspacing='0' cellpadding='0' id='" + getId() + 
/* 136 */       "'");
/* 137 */     if (getTableWidth().equals("100%")) {
/* 138 */       sb.append("width='" + getTableWidth() + "'><tr><td>\n<td>");
/*     */     }
/*     */     else {
/* 141 */       sb.append("width='" + getTableWidth(20) + "'><tr>\n<td width='" + 
/* 142 */         getTableWidth() + "'>");
/*     */     }
/*     */     
/* 145 */     sb.append("\n<table id='" + getId() + "tb' ");
/* 146 */     sb.append("class='skit_table_head' cellspacing='0' cellpadding='0' ");
/* 147 */     sb.append("width='" + getTableWidth() + "'><tr>");
/* 148 */     setTableHeader(sb);
/* 149 */     sb.append("\n</tr></table></td>");
/* 150 */     sb.append("\n<td width='20' class='skit_table_head_ext'>&nbsp;</td>");
/* 151 */     sb.append("\n</tr>\n<tr>");
/*     */     
/*     */ 
/* 154 */     sb.append("\n<td colspan='2'>");
/* 155 */     sb.append("<div id='" + getId() + 
/* 156 */       "view' class='skit_table_body' style='height:" + (this.height - 22) + 
/* 157 */       "px'>");
/* 158 */     if (this.model != null) {
/* 159 */       sb.append("\n<table cellspacing='0' cellpadding='0' ");
/* 160 */       sb.append("id='" + getId() + "body' ");
/* 161 */       if ((this.dblClickFunc != null) && (this.dblClickFunc.length() > 0)) {
/* 162 */         sb.append("ondblclick='" + this.dblClickFunc + "()' ");
/*     */       }
/* 164 */       sb.append("width='" + getTableWidth() + "'>");
/* 165 */       for (int i = 0; i < this.model.getRowCount(); i++) {
/* 166 */         sb.append("\n<tr>");
/* 167 */         for (int j = 0; j < this.model.getColumnCount(); j++) {
/* 168 */           Object o = this.model.getValueAt(i, j);
/* 169 */           sb.append("\n<td class='skit_table_cell' ");
/* 170 */           if ((i == 0) && (this.model.getColWidth(j) > 0)) {
/* 171 */             sb.append("width='" + this.model.getColWidth(j) + "'");
/*     */           }
/* 173 */           sb.append(">");
/* 174 */           sb.append(o);
/* 175 */           sb.append("</td>");
/*     */         }
/* 177 */         sb.append("</tr>");
/*     */       }
/* 179 */       sb.append("\n</table>");
/*     */     }
/* 181 */     sb.append("\n</div>");
/*     */     
/*     */ 
/* 184 */     sb.append("\n</td></tr>\n</table>");
/*     */     
/* 186 */     return sb.toString();
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public String getPopMenuTag()
/*     */   {
/* 195 */     return this.model.getPopMenuTag();
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   public String getScriptTag()
/*     */   {
/* 205 */     StringBuffer sb = new StringBuffer();
/* 206 */     if ((this.dblClickFunc != null) && (this.dblClickFunc.length() > 0)) {
/* 207 */       sb.append(
/* 208 */         Toolkit.insertJsFunction(this.dblClickFunc, "Table" + getId() + 
/* 209 */         "双击事件函数"));
/*     */     }
/* 211 */     sb.append(this.model.getScriptTag());
/* 212 */     return sb.toString();
/*     */   }
/*     */   
/*     */   public KTableModel getModel()
/*     */   {
/* 217 */     return this.model;
/*     */   }
/*     */   
/*     */   public String getDblClickFunc()
/*     */   {
/* 222 */     return this.dblClickFunc;
/*     */   }
/*     */   
/*     */   public int getHeight()
/*     */   {
/* 227 */     return this.height;
/*     */   }
/*     */   
/*     */   public void setModel(KTableModel model)
/*     */   {
/* 232 */     this.model = model;
/*     */   }
/*     */   
/*     */   public void setDblClickFunc(String dblClickFunc)
/*     */   {
/* 237 */     this.dblClickFunc = dblClickFunc;
/*     */   }
/*     */   
/*     */   public void setHeight(int height)
/*     */   {
/* 242 */     this.height = height;
/*     */   }
/*     */ }


/* Location:              D:\focusnt\cos\trunk\CODE\cos\lib\web-lib\skit-1.1.jar!\com\focus\skit\table\KTable.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */