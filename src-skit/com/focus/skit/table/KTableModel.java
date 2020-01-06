/*    */ package com.focus.skit.table;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public abstract class KTableModel
/*    */ {
/*    */   private int[] tbColsWidth;
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   private String[] tbColsStyle;
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   public KTableModel(int length)
/*    */   {
/* 26 */     this.tbColsWidth = new int[length];
/* 27 */     this.tbColsStyle = new String[length];
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   public void setColStyle(int col, String style)
/*    */   {
/* 37 */     if ((col >= 0) && (col < this.tbColsStyle.length) && 
/* 38 */       (style != null) && (style.length() > 0)) {
/* 39 */       this.tbColsStyle[col] = style;
/*    */     }
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   public String getColStyle(int col)
/*    */   {
/* 50 */     if ((col >= 0) && (col < this.tbColsStyle.length)) {
/* 51 */       return this.tbColsStyle[col];
/*    */     }
/* 53 */     return null;
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   public void setColWidth(int col, int width)
/*    */   {
/* 63 */     if ((col >= 0) && (col < this.tbColsWidth.length) && (width > 0))
/*    */     {
/* 65 */       this.tbColsWidth[col] = width;
/*    */     }
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   public int getColWidth(int col)
/*    */   {
/* 79 */     if ((col >= 0) && (col < this.tbColsWidth.length)) {
/* 80 */       return this.tbColsWidth[col];
/*    */     }
/* 82 */     return 0;
/*    */   }
/*    */   
/*    */   public abstract int getRowCount();
/*    */   
/*    */   public abstract int getColumnCount();
/*    */   
/*    */   public abstract String getColumnName(int paramInt);
/*    */   
/*    */   public abstract Object getValueAt(int paramInt1, int paramInt2);
/*    */   
/*    */   public abstract String getScriptTag();
/*    */   
/*    */   public abstract String getPopMenuTag();
/*    */ }


/* Location:              D:\focusnt\cos\trunk\CODE\cos\lib\web-lib\skit-1.1.jar!\com\focus\skit\table\KTableModel.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */