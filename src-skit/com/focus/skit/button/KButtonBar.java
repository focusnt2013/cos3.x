/*    */ package com.focus.skit.button;
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
/*    */ public class KButtonBar
/*    */   extends KComponent
/*    */ {
/*    */   public String toString()
/*    */   {
/* 22 */     StringBuffer sb = new StringBuffer();
/* 23 */     sb.append("\n<table id='" + getId() + 
/* 24 */       "' cellspacing='0' cellpadding='0'>");
/* 25 */     sb.append("\n<tr>");
/* 26 */     for (int i = 0; i < getComponentCount(); i++) {
/* 27 */       sb.append("\n<td>");
/* 28 */       sb.append(getComponent(i));
/* 29 */       sb.append("\n</td>");
/*    */     }
/* 31 */     sb.append("</tr></table>");
/* 32 */     return sb.toString();
/*    */   }
/*    */ }


/* Location:              D:\focusnt\cos\trunk\CODE\cos\lib\web-lib\skit-1.1.jar!\com\focus\skit\button\KButtonBar.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */