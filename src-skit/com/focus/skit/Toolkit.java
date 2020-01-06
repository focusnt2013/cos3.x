/*     */ package com.focus.skit;
/*     */ 
/*     */ import com.focus.skit.frame.KViewFrame;
/*     */ import com.focus.skit.menu.KMenu;
/*     */ import com.focus.skit.table.KTable;
/*     */ import com.focus.skit.tree.KTree;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.PrintWriter;
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
/*     */ public class Toolkit
/*     */ {
/*  24 */   private static int idCount = 0;
/*     */   
/*     */   public static Object getComponentId()
/*     */   {
/*  28 */     return Integer.toHexString(idCount++);
/*     */   }
/*     */   
/*     */   public static String insertJsFunction(String funcName)
/*     */   {
/*  33 */     return insertJsFunction(funcName, "");
/*     */   }
/*     */   
/*     */   public static String insertJsFunction(String funcName, String remark)
/*     */   {
/*  38 */     StringBuffer sb = new StringBuffer();
/*  39 */     if ((funcName != null) && (funcName.length() > 0)) {
/*  40 */       if (remark != null) {
/*  41 */         remark = funcName;
/*     */       }
/*     */       
/*  44 */       String alert = "请实现函数--" + remark + "！";
/*  45 */       if (funcName.equals("delete")) {
/*  46 */         funcName = funcName + "A";
/*  47 */         alert = "请不要使用" + funcName + "作为函数名称，因为与系统的关键字冲突!";
/*     */       }
/*  49 */       sb.append("\n/*函数:" + remark + "*/");
/*  50 */       sb.append("\nfunction " + funcName + "()");
/*  51 */       sb.append("\n{");
/*  52 */       sb.append("\n    alert('" + alert + "');");
/*  53 */       sb.append("\n}");
/*     */     }
/*  55 */     return sb.toString();
/*     */   }
/*     */   
/*     */   public static String insertButtonPicked(String func)
/*     */   {
/*  60 */     StringBuffer sb = new StringBuffer();
/*  61 */     sb.append("<img src='skin/metallic/picked/picked_right.gif' ");
/*  62 */     sb.append("onclick='" + func + "()' ");
/*  63 */     sb.append(
/*  64 */       "onmouseover='this.src=\"skin/metallic/picked/picked_disabled_right.gif\";'");
/*  65 */     sb.append(
/*  66 */       "onmouseout='this.src=\"skin/metallic/picked/picked_right.gif\";'>");
/*  67 */     return sb.toString();
/*     */   }
/*     */   
/*     */   public static String insertButtonPicked(KMenu menu)
/*     */   {
/*  72 */     StringBuffer sb = new StringBuffer();
/*  73 */     sb.append("<img src='skin/metallic/picked/picked_right.gif' ");
/*  74 */     sb.append("onclick='SKIT_SHOW_POPMENU(\"" + menu.getId() + 
/*  75 */       "\", \"show\", this);'");
/*  76 */     sb.append(
/*  77 */       "onmouseover='this.src=\"skin/metallic/picked/picked_disabled_right.gif\";'");
/*  78 */     sb.append(
/*  79 */       "onmouseout='this.src=\"skin/metallic/picked/picked_right.gif\";'>");
/*  80 */     return sb.toString();
/*     */   }
/*     */   
/*     */ 
/*     */   public static String insertCellLink(String href, String name, String style, int width)
/*     */   {
/*  86 */     if ((style != null) && (style.length() > 0)) {
/*  87 */       return 
/*  88 */         "<a style='" + style + "width='" + width + "'" + "' href='" + href + "' >" + name + "</a>";
/*     */     }
/*     */     
/*  91 */     return "<a href='" + href + "' >" + name + "</a>";
/*     */   }
/*     */   
/*     */ 
/*     */   public static String insertCellInput(Object value, String style, int width)
/*     */   {
/*  97 */     if (width < 120) {
/*  98 */       return "<p style='" + style + "'>" + value + "</p>";
/*     */     }
/*     */     
/* 101 */     if ((style == null) || (style.length() == 0)) {
/* 102 */       style = "width:" + (width - 20) + "px";
/*     */     }
/*     */     else {
/* 105 */       style = style + ";width:" + (width - 20) + "px";
/*     */     }
/* 107 */     return 
/* 108 */       "<input type='text' class='skit_cell_input' style='" + style + "' readOnly='true' value=\"" + value + "\">";
/*     */   }
/*     */   
/*     */   public static void createFrameJspFile(KViewFrame view, String path, String name)
/*     */   {
/*     */     try
/*     */     {
/* 115 */       String filename = path + name + ".jsp";
/* 116 */       PrintWriter writer = new PrintWriter(new FileOutputStream(filename));
/* 117 */       writer.println("<%@page contentType='text/html;charset=gb2312'%>");
/* 118 */       writer.println("<html>");
/* 119 */       writer.println("<head>");
/* 120 */       writer.println(
/* 121 */         "<meta http-equiv='Content-Type' content='text/html; charset=gb2312'>");
/* 122 */       writer.println("<style type='text/css'>");
/* 123 */       writer.println("body {overflow:hidden; margin-top:0px; margin-left:0px; margin-bottom:0px; margin-right:0px }");
/* 124 */       writer.println("<%@ include file=\"css/skit_table.css\"%>");
/* 125 */       writer.println("<%@ include file=\"css/skit_menu.css\"%>");
/* 126 */       writer.println("</style>");
/* 127 */       writer.println(
/* 128 */         "<script language='javascript' src='script/skit_table.js'></script>");
/* 129 */       writer.println(
/* 130 */         "<script language='javascript' src='script/skit_menu.js'></script>");
/* 131 */       writer.println("</head>");
/* 132 */       writer.println("<body>");
/* 133 */       writer.println(view);
/* 134 */       writer.println("</body>");
/* 135 */       writer.println("</html>");
/*     */       
/* 137 */       writer.println("<SCRIPT LANGUAGE='JavaScript'>");
/* 138 */       writer.println("<!--");
/* 139 */       writer.println("SKIT_RESIZE_TREE();");
/* 140 */       writer.println(view.getScriptTag());
/* 141 */       writer.println("//-->");
/* 142 */       writer.println("</SCRIPT>");
/* 143 */       writer.close();
/*     */     }
/*     */     catch (Exception e) {
/* 146 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */   
/*     */   public static void createTableJspFile(KTable table, String path, String name)
/*     */   {
/*     */     try
/*     */     {
/* 154 */       String filename = path + name + ".jsp";
/* 155 */       PrintWriter writer = new PrintWriter(new FileOutputStream(filename));
/* 156 */       writer.println("<%@page contentType='text/html;charset=gb2312'%>");
/* 157 */       writer.println("<html>");
/* 158 */       writer.println("<head>");
/* 159 */       writer.println(
/* 160 */         "<meta http-equiv='Content-Type' content='text/html; charset=gb2312'>");
/* 161 */       writer.println("<style type='text/css'>");
/* 162 */       writer.println("body {overflow:hidden; margin-top:0px; margin-left:0px; margin-bottom:0px; margin-right:0px }");
/* 163 */       writer.println("<%@ include file=\"css/skit_table.css\"%>");
/* 164 */       writer.println("<%@ include file=\"css/skit_menu.css\"%>");
/* 165 */       writer.println("</style>");
/* 166 */       writer.println(
/* 167 */         "<script language='javascript' src='script/skit_table.js'></script>");
/* 168 */       writer.println(
/* 169 */         "<script language='javascript' src='script/skit_menu.js'></script>");
/* 170 */       writer.println("</head>");
/* 171 */       writer.println("<body>");
/* 172 */       writer.println("<div style='height:200px;border:1px solid blue'>");
/* 173 */       writer.println(table);
/* 174 */       writer.println("</div>");
/* 175 */       writer.println("</body>");
/* 176 */       writer.println("</html>");
/*     */       
/* 178 */       writer.println("<SCRIPT LANGUAGE='JavaScript'>");
/* 179 */       writer.println("<!--");
/*     */       
/* 181 */       writer.println("function SKIT_TABLE_VIEW_INIT()");
/* 182 */       writer.println("{");
/* 183 */       writer.println(
/* 184 */         "    _SKIT_TABLE_HEAD_ARRAY.push(document.getElementById('" + 
/* 185 */         table.getId() + "tb'));");
/* 186 */       writer.println(
/* 187 */         "    _SKIT_TABLE_VIEW_ARRAY.push(document.getElementById('" + 
/* 188 */         table.getId() + "body'));");
/* 189 */       writer.println("}");
/* 190 */       writer.println("//-->");
/* 191 */       writer.println("</SCRIPT>");
/* 192 */       writer.close();
/*     */     }
/*     */     catch (Exception e) {
/* 195 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */   
/*     */   public static void createTreeJspFile(KTree tree, String path, String name)
/*     */   {
/*     */     try {
/* 202 */       String filename = path + name + ".jsp";
/* 203 */       PrintWriter writer = new PrintWriter(new FileOutputStream(filename));
/* 204 */       writer.println("<%@page contentType='text/html;charset=gb2312'%>");
/* 205 */       writer.println("<html>");
/* 206 */       writer.println("<head>");
/* 207 */       writer.println(
/* 208 */         "<meta http-equiv='Content-Type' content='text/html; charset=gb2312'>");
/* 209 */       writer.println("<style type='text/css'>");
/* 210 */       writer.println("body {overflow:hidden; margin-top:0px; margin-left:0px; margin-bottom:0px; margin-right:0px }");
/* 211 */       writer.println("<%@ include file=\"css/skit_tree.css\"%>");
/* 212 */       writer.println("<%@ include file=\"css/skit_actions.css\"%>");
/* 213 */       writer.println("<%@ include file=\"css/skit_menu.css\"%>");
/* 214 */       writer.println("</style>");
/* 215 */       writer.println(
/* 216 */         "<script language='javascript' src='script/skit_button.js'></script>");
/* 217 */       writer.println(
/* 218 */         "<script language='javascript' src='script/skit_tree.js'></script>");
/* 219 */       writer.println("</head>");
/* 220 */       writer.println("<body onResize='SKIT_RESIZE_TREE()'>");
/* 221 */       writer.println(tree);
/* 222 */       writer.println("</body>");
/* 223 */       writer.println("</html>");
/*     */       
/* 225 */       writer.println("<SCRIPT LANGUAGE='JavaScript'>");
/* 226 */       writer.println("<!--");
/* 227 */       writer.println("SKIT_RESIZE_TREE();");
/* 228 */       writer.println(tree.getScriptTag());
/* 229 */       writer.println("//-->");
/* 230 */       writer.println("</SCRIPT>");
/* 231 */       writer.close();
/*     */     }
/*     */     catch (Exception e) {
/* 234 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */   
/*     */   public static void testCreateTree() {}
/*     */   
/*     */   public static void testCreateTable() {}
/*     */   
/*     */   public static void main(String[] args) {}
/*     */ }


/* Location:              D:\focusnt\cos\trunk\CODE\cos\lib\web-lib\skit-1.1.jar!\com\focus\skit\Toolkit.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */