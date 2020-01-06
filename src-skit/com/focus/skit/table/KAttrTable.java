/*    */ package com.focus.skit.table;
/*    */ 
/*    */ import com.focus.skit.KComponent;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class KAttrTable
/*    */   extends KComponent
/*    */ {
/*    */   public KAttrTable(String title)
/*    */   {
/* 22 */     setLabel(title);
/*    */   }
/*    */   
/*    */   public String toString()
/*    */   {
/* 27 */     StringBuffer sb = new StringBuffer();
/* 28 */     sb.append("\n<TABLE id='" + getId() + 
/* 29 */       "' cellspacing='0' cellpadding='0' width='100%'>");
/* 30 */     sb.append("\n<TR><TD>");
/* 31 */     sb.append("\n<Table cellspacing='0' cellpadding='0' width='100%' class='skit_table_head' >");
/* 32 */     sb.append("\n<Tr><Td>" + getLabel() + "</td></tr>");
/* 33 */     sb.append("\n</Table>");
/* 34 */     sb.append("\n</TD></TR>");
/* 35 */     sb.append(
/* 36 */       "\n<TR><TD style='padding-left:1px;padding-right:1px;' valign='top'>");
/* 37 */     sb.append("\n<Table id='" + getId() + 
/* 38 */       "body' cellspacing='0' cellpadding='0' width='100%' class='skit_attrtab_body' >");
/* 39 */     for (int i = 0; i < getComponentCount(); i++) {
/* 40 */       if (i == getComponentCount() - 1) {
/* 41 */         ((KAttrItem)getComponent(i)).setLastRow(true);
/*    */       }
/* 43 */       sb.append(getComponent(i));
/*    */     }
/* 45 */     sb.append("\n</Table>");
/* 46 */     sb.append("\n</TD></TR>");
/* 47 */     sb.append("\n</TABLE>");
/* 48 */     return sb.toString();
/*    */   }
/*    */ }


/* Location:              D:\focusnt\cos\trunk\CODE\cos\lib\web-lib\skit-1.1.jar!\com\focus\skit\table\KAttrTable.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */