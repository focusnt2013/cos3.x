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
/*    */ public class KButtonBlue
/*    */   extends KComponent
/*    */ {
/*    */   private String funcName;
/*    */   
/*    */   public KButtonBlue(String name, String funcName)
/*    */   {
/* 23 */     setLabel(name);
/* 24 */     this.funcName = funcName;
/*    */   }
/*    */   
/*    */   public String toString()
/*    */   {
/* 29 */     StringBuffer sb = new StringBuffer();
/* 30 */     sb.append("<table cellspacing='0' cellpadding='0' id='" + getId() + 
/* 31 */       "'>");
/* 32 */     sb.append("\n<tr class='skit_button'");
/* 33 */     if ((this.funcName != null) && (this.funcName.length() > 0)) {
/* 34 */       sb.append("\nonclick='" + this.funcName + "()'");
/*    */     }
/* 36 */     sb.append("\nonmouseover='SKIT_BLUEBTN_MOUSE_OVER(this)'");
/* 37 */     sb.append("\nonmouseout='SKIT_BLUEBTN_MOUSE_OUT(this)'>");
/* 38 */     sb.append("\n<td class='skit_blue_button_left'/>");
/* 39 */     sb.append(
/* 40 */       "\n<td class='skit_blue_button_middle'><P class='skit_button_font'>");
/* 41 */     sb.append(getLabel());
/* 42 */     sb.append("</P></td>");
/* 43 */     sb.append("\n<td class='skit_blue_button_right'/></tr>");
/* 44 */     sb.append("\n</table>");
/* 45 */     return sb.toString();
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   public String getScriptTag()
/*    */   {
/* 55 */     StringBuffer sb = new StringBuffer();
/* 56 */     String remark = getLabel();
/* 57 */     if ((this.funcName != null) && (this.funcName.length() > 0)) {
/* 58 */       if (remark != null) {
/* 59 */         remark = this.funcName;
/*    */       }
/*    */       
/* 62 */       String alert = "请实现函数--" + remark + "！";
/* 63 */       if (this.funcName.equals("delete")) {
/* 64 */         alert = "请不要使用" + this.funcName + "作为函数名称，因为与系统的关键字冲突!";
/* 65 */         this.funcName += "A";
/*    */       }
/* 67 */       sb.append("\n/*函数:" + remark + "*/");
/* 68 */       sb.append("\nfunction " + this.funcName + "()");
/* 69 */       sb.append("\n{");
/* 70 */       sb.append("\n    alert('" + alert + "');");
/* 71 */       sb.append("\n}");
/*    */     }
/* 73 */     return sb.toString();
/*    */   }
/*    */ }


/* Location:              D:\focusnt\cos\trunk\CODE\cos\lib\web-lib\skit-1.1.jar!\com\focus\skit\button\KButtonBlue.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */