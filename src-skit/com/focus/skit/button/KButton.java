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
/*    */ public class KButton
/*    */   extends KComponent
/*    */ {
/*    */   private String funcName;
/*    */   
/*    */   public KButton(String name, String funcName)
/*    */   {
/* 24 */     setLabel(name);
/* 25 */     this.funcName = funcName;
/*    */   }
/*    */   
/*    */   public String toString()
/*    */   {
/* 30 */     StringBuffer sb = new StringBuffer();
/* 31 */     sb.append("<table cellspacing='0' cellpadding='0' id='" + getId() + 
/* 32 */       "'>");
/* 33 */     sb.append("\n<tr class='skit_button'");
/* 34 */     if ((this.funcName != null) && (this.funcName.length() > 0)) {
/* 35 */       sb.append("\nonclick='" + this.funcName + "()'");
/*    */     }
/* 37 */     sb.append("\nonmouseover='SKIT_BTN_MOUSE_OVER(this)'");
/* 38 */     sb.append("\nonmouseout='SKIT_BTN_MOUSE_OUT(this)'>");
/* 39 */     sb.append("\n<td class='skit_button_left'/>");
/* 40 */     sb.append(
/* 41 */       "\n<td class='skit_button_middle'><P class='skit_button_font'>");
/* 42 */     sb.append(getLabel());
/* 43 */     sb.append("</P></td>");
/* 44 */     sb.append("\n<td class='skit_button_right'/></tr>");
/* 45 */     sb.append("\n</table>");
/* 46 */     return sb.toString();
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   public String getScriptTag()
/*    */   {
/* 56 */     StringBuffer sb = new StringBuffer();
/* 57 */     String remark = getLabel();
/* 58 */     if ((this.funcName != null) && (this.funcName.length() > 0)) {
/* 59 */       if (remark != null) {
/* 60 */         remark = this.funcName;
/*    */       }
/*    */       
/* 63 */       String alert = "请实现函数--" + remark + "！";
/* 64 */       if (this.funcName.equals("delete")) {
/* 65 */         alert = "请不要使用" + this.funcName + "作为函数名称，因为与系统的关键字冲突!";
/* 66 */         this.funcName += "A";
/*    */       }
/* 68 */       sb.append("\n/*函数:" + remark + "*/");
/* 69 */       sb.append("\nfunction " + this.funcName + "()");
/* 70 */       sb.append("\n{");
/* 71 */       sb.append("\n    alert('" + alert + "');");
/* 72 */       sb.append("\n}");
/*    */     }
/* 74 */     return sb.toString();
/*    */   }
/*    */ }


/* Location:              D:\focusnt\cos\trunk\CODE\cos\lib\web-lib\skit-1.1.jar!\com\focus\skit\button\KButton.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */