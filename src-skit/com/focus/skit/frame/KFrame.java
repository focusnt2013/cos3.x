/*    */ package com.focus.skit.frame;
/*    */ 
/*    */ import com.focus.skit.BorderLayout;
/*    */ import com.focus.skit.Container;
/*    */ import com.focus.skit.Layout;
/*    */ import java.io.PrintStream;
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
/*    */ public class KFrame
/*    */   extends Container
/*    */ {
/*    */   public KFrame(Layout layout)
/*    */   {
/* 23 */     super(layout);
/*    */   }
/*    */   
/*    */   public String toString()
/*    */   {
/* 28 */     StringBuffer sb = new StringBuffer();
/*    */     
/* 30 */     if (getLayout() != null) {
/* 31 */       sb.append(getLayout().layoutContainer(this));
/*    */     }
/*    */     else {
/* 34 */       for (int i = 0; i < getComponentCount(); i++) {
/* 35 */         sb.append("\n");
/* 36 */         sb.append(getComponent(i));
/*    */       }
/*    */     }
/* 39 */     return sb.toString();
/*    */   }
/*    */   
/*    */   public static void main(String[] args)
/*    */   {
/* 44 */     KFrame f = new KFrame(new BorderLayout(1));
/*    */     
/* 46 */     System.out.println(f);
/*    */   }
/*    */ }


/* Location:              D:\focusnt\cos\trunk\CODE\cos\lib\web-lib\skit-1.1.jar!\com\focus\skit\frame\KFrame.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */