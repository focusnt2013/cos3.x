/*    */ package com.focus.skit;
/*    */ 
/*    */ import com.focus.skit.button.KButtonBar;
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
/*    */ 
/*    */ 
/*    */ public class KDialog
/*    */   extends KComponent
/*    */ {
/*    */   private DefaultComponent ctrl;
/*    */   private KButtonBar btnBar;
/*    */   
/*    */   public KDialog(String title, DefaultComponent ctrl, KButtonBar btnBar)
/*    */   {
/* 27 */     setLabel(title);
/* 28 */     this.ctrl = ctrl;
/* 29 */     this.btnBar = btnBar;
/* 30 */     this.id = Toolkit.getComponentId().toString();
/*    */   }
/*    */   
/*    */   public String toString()
/*    */   {
/* 35 */     StringBuffer sb = new StringBuffer();
/* 36 */     sb.append("\n<div id='" + getId() + "' class='skit_dialog' >");
/* 37 */     sb.append("\n<table cellspacing='0' cellpadding='0' id='" + getId() + 
/* 38 */       "tb' style='display:'>");
/* 39 */     sb.append("\n<tr><td class='skit_dialog_top_left'/>");
/* 40 */     sb.append("\n<td class='skit_dialog_top_middle'>");
/* 41 */     sb.append("\n<table cellspacing='0' cellpadding='0'>");
/* 42 */     sb.append("\n<tr><td class='skit_dialog_title'>" + getLabel() + 
/* 43 */       "</td></tr>");
/* 44 */     sb.append("\n</table></td>");
/* 45 */     sb.append("\n<td class='skit_dialog_top_right'/></tr>");
/* 46 */     sb.append("\n<tr><td class='skit_dialog_middle_left'/>");
/* 47 */     sb.append("\n<td class='skit_dialog_middle_middle'>");
/*    */     
/* 49 */     sb.append(
/* 50 */       "\n<table cellspacing='0' cellpadding='0' class='skit_dialog_content'>");
/* 51 */     sb.append("\n<tr><td>");
/* 52 */     if (this.ctrl != null) {
/* 53 */       sb.append(this.ctrl);
/*    */     }
/* 55 */     sb.append("\n</td></tr>");
/* 56 */     sb.append("\n<tr><td align='right'>");
/* 57 */     if (this.btnBar != null) {
/* 58 */       sb.append(this.btnBar);
/*    */     }
/* 60 */     sb.append("\n</td></tr></table>");
/*    */     
/* 62 */     sb.append("\n</td>");
/* 63 */     sb.append("\n<td class='skit_dialog_middle_right'/></tr>");
/* 64 */     sb.append("\n<tr><td class='skit_dialog_bottom_left'/>");
/* 65 */     sb.append("\n<td class='skit_dialog_bottom_middle'/>");
/* 66 */     sb.append("\n<td class='skit_dialog_bottom_right'/></tr>");
/* 67 */     sb.append("\n</table>");
/* 68 */     sb.append("\n</div>");
/*    */     
/* 70 */     return sb.toString();
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   public String getScriptTag()
/*    */   {
/* 80 */     StringBuffer sb = new StringBuffer();
/*    */     
/* 82 */     if (this.ctrl != null) {
/* 83 */       sb.append(this.ctrl.getScriptTag());
/*    */     }
/* 85 */     if (this.btnBar != null) {
/* 86 */       sb.append(this.btnBar.getScriptTag());
/*    */     }
/* 88 */     return sb.toString();
/*    */   }
/*    */ }


/* Location:              D:\focusnt\cos\trunk\CODE\cos\lib\web-lib\skit-1.1.jar!\com\focus\skit\KDialog.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */