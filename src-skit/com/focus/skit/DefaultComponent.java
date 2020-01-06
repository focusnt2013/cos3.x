/*    */ package com.focus.skit;
/*    */ 
/*    */ import java.io.File;
/*    */ import java.io.FileInputStream;
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
/*    */ public class DefaultComponent
/*    */   extends KComponent
/*    */ {
/*    */   private File file;
/*    */   
/*    */   public DefaultComponent(String file)
/*    */   {
/* 24 */     this.file = new File(file);
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   public String toString()
/*    */   {
/* 33 */     if (this.file.exists()) {
/*    */       try {
/* 35 */         FileInputStream in = new FileInputStream(this.file);
/* 36 */         byte[] buf = new byte[in.available()];
/* 37 */         in.read(buf);
/* 38 */         in.close();
/* 39 */         return new String(buf);
/*    */       }
/*    */       catch (Exception e) {
/* 42 */         e.printStackTrace();
/*    */       }
/*    */     }
/*    */     
/*    */ 
/* 47 */     return "";
/*    */   }
/*    */   
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   public String getScriptTag()
/*    */   {
/* 57 */     return "";
/*    */   }
/*    */ }


/* Location:              D:\focusnt\cos\trunk\CODE\cos\lib\web-lib\skit-1.1.jar!\com\focus\skit\DefaultComponent.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */