package com.focus.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * <p>Title: 监控终端</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: kehaoinfo</p>
 * @author 刘学
 * @version 1.0
 */

public class XMLParser
{
    public Document document = null;

    public XMLParser( String path ) throws Exception
    {
        File file = new File( path );
        if( file.exists() )
        {
            buildDocument( new FileInputStream( file ) );
        }
        else
        {
            throw new Exception("Unknown xml file:"+file.getPath());
        }
    }

    public XMLParser( File file ) throws Exception
    {
        if( file.exists() )
        {
            buildDocument( new FileInputStream( file ) );
        }
        else
        {
            throw new Exception("Unknown xml file:"+file.getPath());
        }
    }

    public XMLParser( InputStream inputStream ) throws Exception
    {
        buildDocument( inputStream );
    }

    private Document buildDocument( InputStream inputStream ) throws Exception
    {
    	try
    	{
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        this.document = builder.parse( inputStream );
	        return document;
    	}
    	catch(Exception e)
    	{
    		throw e;
    	}
    	finally
    	{
    		inputStream.close();
    	}
    }

    /**
     * 写xml数据到输出流
     * @param out
     */
    public void write(OutputStream out)
    	throws Exception
    {
    	try
    	{
    		javax.xml.transform.TransformerFactory transFactory = javax.xml.transform.TransformerFactory.newInstance();
	    	javax.xml.transform.Transformer transFormer = transFactory.newTransformer();
	    	DOMSource domSource = new DOMSource(document);
	    	//设置输入源
	    	StreamResult xmlResult = new StreamResult(out);
	    	transFormer.transform(domSource, xmlResult);
    	}
    	catch(Exception e)
    	{
    		throw e;
    	}
    	finally
    	{
    		out.close();
    	}
    }
    /**
     * 得到根节点
     * @return 根节点
     */
    public Element getRootElement()
    {
        if( document == null )
        {
            return null;
        }

        for( Node node = document.getFirstChild(); node != null; node = node.getNextSibling() )
        {
            if( node.getNodeType() == Node.ELEMENT_NODE )
            {
                return (Element)node;
            }
        }
        return null;
    }
    /**
     * 得到根节点
     * @return 根节点
     */
    public Node getRootNode()
    {
        if( document == null )
        {
            return null;
        }

        for( Node node = document.getFirstChild(); node != null; node = node.getNextSibling() )
        {
            if( node.getNodeType() == Node.ELEMENT_NODE )
            {
                return node;
            }
        }
        return null;
    }

    /**
     * 通过标签名称得到对应的节点(包括当前节点)
     * @param element
     * @param tag
     * @return
     */
    public static Element getElementByTag( Node element, String tag )
    {
        if( element.getNodeType() != Node.ELEMENT_NODE )
        {
            return null;
        }
        //判断节点名称是否和输入的标签名称相同,如果相同就返回该节点
        if( element.getNodeName().equalsIgnoreCase( tag ) )
        {
            return (Element)element;
        }

        for( Node node = element.getFirstChild(); node != null; node = node.getNextSibling() )
        {
            Element e = getElementByTag( node, tag );
            if( e != null )
            {
                return e;
            }
        }
        return null;
    }
    
    /**
     * 
     * @param element
     * @return
     */
    public static String getCData(Node element)
    {
    	if( element == null ) return "";
    	String cdata = element.getTextContent().trim();
//    	cdata = Tools.replaceStr(cdata, "\r", "");
//    	cdata = Tools.replaceStr(cdata, "\t", "");
    	return cdata;
    }

    public static Node getLastChildElement( Node element )
    {
        Node e = null;
        for( Node node = element.getFirstChild();
                         node != null; node = node.getNextSibling() )
        {
            if( node.getNodeType() == Node.ELEMENT_NODE )
            {
                e = node;
            }
        }

        return e;
    }

    public static int countChildElement( Node element )
    {
        int i = 0;
        for( Node node = element.getFirstChild();
                         node != null; node = node.getNextSibling() )
        {
            if( node.getNodeType() == Node.ELEMENT_NODE )
            {
                i += 1;
            }
        }

        return i;
    }
    /**
     * 返回第一个子节点元素
     * @param element Node
     * @return Node
     */
    public static Element getFirstChildElement( Node element )
    {
        for( Node node = element.getFirstChild();
                         node != null; node = node.getNextSibling() )
        {
            if( node.getNodeType() == Node.ELEMENT_NODE )
            {
                return (Element)node;
            }
        }

        return null;
    }
    
    /**
     * 创建节点
     * @param tag
     * @return
     */
    public Element createElement(String tag)
    {
    	return this.document.createElement(tag);
    }

    /**
     * 通过路径搜索
     * @param element
     * @param path
     * @param depth
     * @return
     */
    public static Element getChildElementByPath( Node element, int path[] )
    {
    	return getChildElementByPath(element, path, 0);
    }
    
    public static Node insertBefore(Node newChild, Node refChild)
    {
    	Node parent = refChild.getParentNode();
    	Node node = getNextSibling(refChild);
    	if( node != null )
    	{
    		return parent.insertBefore(newChild, node);
    	}
    	else
    	{
    		return parent.appendChild(newChild);
    	}
    }
    
    public static Element getChildElementByPath( Node element, int path[], int depth )
    {
    	if( path == null ) return null;
    	if( depth < 0 || depth >= path.length ) return null;
    	int i = 0;
        for( Node node = element.getFirstChild(); node != null; node = node.getNextSibling() )
        {
            if( node.getNodeType() == Node.ELEMENT_NODE )
            {
            	if( i == path[depth] )
            	{
            		depth += 1;
            		if( depth == path.length ) return (Element)node;
            		else return getChildElementByPath(node, path, depth);
            	}
            	i += 1;
            }
        }

        return null;
    }
    /**
     * 通过节点标签和属性标签与值查找节点对象
     * @param element
     * @param tag
     * @param attr
     * @param value
     * @return 匹配的节点对象
     */
    public static Element findChildElementByTagAndAttr( Node element, String tag, String attr, String value )
    {
        for( Node node = element.getFirstChild(); node != null; node = node.getNextSibling() )
        {
            if( node.getNodeType() == Node.ELEMENT_NODE )
            {
                //判断节点名称是否和输入的标签名称相同,如果相同就返回该节点
                if( node.getNodeName().equalsIgnoreCase( tag ) )
                {
                    Element e = (Element)node;
                    if( e.hasAttribute(attr) && e.getAttribute(attr).equals(value) )
                    	return e;
                }
            }
        }

        return null;
    }
    /**
     * 查找所有满足条件的节点
     * @param element
     * @param tag
     * @param attr
     * @param value
     * @param buffer
     */
    public static void findAllChildElementByTagAndAttr( Node element, String tag, String attr, String value, ArrayList<Element> buffer )
    {
        for( Node node = element.getFirstChild(); node != null; node = node.getNextSibling() )
        {
            if( node.getNodeType() == Node.ELEMENT_NODE )
            {
                //判断节点名称是否和输入的标签名称相同,如果相同就返回该节点
                if( node.getNodeName().equalsIgnoreCase( tag ) )
                {
                    Element e = (Element)node;
                    if( e.hasAttribute(attr) && e.getAttribute(attr).equals(value) ){
                    	buffer.add(e);
                    }
                    findAllChildElementByTagAndAttr(e, tag, attr, value, buffer);
                }
            }
        }
    }
    /**
     * 通过标签名称得到对应的节点(不包括包括当前节点)
     * @param element
     * @param tag
     * @return
     */
    public static Element getChildElementByTag( Node element, String tag )
    {
        for( Node node = element.getFirstChild(); node != null; node = node.getNextSibling() )
        {
        	short type = node.getNodeType();
        	String name = node.getNodeName();
            if( type == Node.ELEMENT_NODE )
            {
                //判断节点名称是否和输入的标签名称相同,如果相同就返回该节点
                if( name.equalsIgnoreCase( tag ) )
                {
                    return (Element)node;
                }
            }
        }

        return null;
    }

    public static final String getElementValue( Node element )
    {
    	if( element == null ) return "";
        for( Node node = element.getFirstChild(); node != null; node = node.getNextSibling() )
        {
            if( node.getNodeType() == Node.TEXT_NODE )
            {
                return node.getNodeValue();
            }
        }
        return "";
    }

    /**
     * 根据属性名称得到指定节点的属性值
     * @param element
     * @param id
     * @return
     */
    public static final String getElementAttr( Node element, String attrName )
    {
    	return getElementAttr(element, attrName, "");
    }
    
    public static final String getElementAttr( Node element, String attrName, String def )
    {
    	if( element == null ) return def;
        NamedNodeMap map = element.getAttributes();
        int i = 0;
        for( Node n = map.item( i ); n != null; n = map.item( i ) )
        {
            if( n.getNodeType() == Node.ATTRIBUTE_NODE &&
                n.getNodeName().equalsIgnoreCase( attrName ) )
            {
                return n.getNodeValue().trim();
            }
            i++;
        }
        return def;
    }

    public static final Item[] getElementAttrs( Node element )
    {
        ArrayList list = new ArrayList();
        NamedNodeMap map = element.getAttributes();
        int i = 0;
        for( Node n = map.item( i ); n != null; n = map.item( i ) )
        {
            if( n.getNodeType() == Node.ATTRIBUTE_NODE )
            {
                Item item = new Item( n.getNodeName(), n.getNodeValue() );
                list.add( item );
            }
            i++;
        }

        Item items[] = new Item[list.size()];
        list.toArray(items);
        return items;
    }

    public static final Element getNextSibling( Node element )
    {
        for( Node node = element.getNextSibling(); node != null; node = node.getNextSibling() )
        {
            if( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE )
            {
                return (Element)node;
            }
        }
        return null;
    }

    public static final Element nextSibling( Node e )
    {
        for( Node n = e.getNextSibling(); n != null; n = n.getNextSibling() )
        {
            if( n.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE )
            {
                continue;
            }
            //如果得到的下一个节点名称和输入的节点名称相同那么就返回新的节点
            if( e.getNodeName().equalsIgnoreCase( n.getNodeName() ) )
            {
                return( Element ) n;
            }
        }
        return null;
    }

    public static final void removeChildren(Element e)
    {
    	Node child = e.getFirstChild();
    	while(child != null)
    	{
    		Node sibling = child.getNextSibling();
    		e.removeChild(child);
    		child = sibling;
    	}
    }

    /**
     * 返回所有节点名称（静态信息的那种结构）
     * @return
    public String[] getAttributes()
    {
        Vector temp = new Vector();
        for( int i = 0; i < document.getFirstChild().getChildNodes().getLength();
                     i++ )
        {
            if( document.getFirstChild().getChildNodes().item( i ).getNodeType() ==
                Node.ELEMENT_NODE )
            {
                temp.add( document.getFirstChild().getChildNodes().item( i ).
                          getNodeName() );
            }
        }
        Object[] tempObject = temp.toArray();
        String[] attributes = new String[tempObject.length];
        for( int i = 0; i < tempObject.length; i++ )
        {
            attributes[i] = tempObject[i].toString();
        }
        return attributes;
    }
     */

    /**
     * 返回所有节点值（静态信息的那种结构）
     * @return
     */
    public String[] getValues()
    {

        Vector temp = new Vector();
        for( int i = 0; i < document.getFirstChild().getChildNodes().getLength();
                     i++ )
        {
            if( document.getFirstChild().getChildNodes().item( i ).getNodeType() ==
                Node.ELEMENT_NODE )
            {
                temp.add( document.getFirstChild().getChildNodes().item( i ).
                          getFirstChild() );
            }
        }
        Object[] tempObject = temp.toArray();
        String[] values = new String[tempObject.length];
        for( int i = 0; i < tempObject.length; i++ )
        {
            values[i] = tempObject[i].toString();
        }
        return values;
    }

    /**    public static String getAttribute(Node element, String attrName)
        {
            NamedNodeMap map = element.getAttributes();
            map.
            for(
        }*/
    public static void main( String[] args )
    {
//        XMLParser handle = new XMLParser(
        //      "E:/hfc/src/resources/config/testdata/静态信息.xml");
        //    if(handle.document != null )
        //  {
        //    System.out.println( "解析XML文档成功!" );
        /*            System.out.println( handle.document.getChildNodes().item(0).getNodeType() );
         System.out.println( handle.document.getChildNodes().getLength() );
                    System.out.println( handle.getChildElementByTag(
                        handle.getRootNode(),"input-params"));
                    System.out.println( handle.getRootNode())
                    System.out.println(handle.getChildElementByTag(handle.getRootNode(),"menubar").getAttributes().getLength());
             NamedNodeMap map = handle.getChildElementByTag(handle.getRootNode(),"menubar").getAttributes();
                    System.out.println(map.item(0).getNodeValue());;*/
        //  System.out.println(handle.getAttributes().length);
//	    System.out.println(handle.getValues().length);
        //      }
        //    else
        //  {
        //    System.out.println( "解析XML文档失败!" );
//        }
    }

    public String toString()
    {
        return this.getRootNode().toString();
    }

    /**
     * 创建解析好的XML的document对象
     * @param filename XML文件的文件名
     * @return 解析好的XML对象
     */
    public static final XMLParser createXMLParser( String path )
    {
        try
        {
            return new XMLParser( path );
        }
        catch( Exception eee )
        {
            return null;
        }
    }

    public static final XMLParser createXMLParser( byte[] buffer )
    {
        try
        {
            return new XMLParser( new ByteArrayInputStream( buffer ) );
        }
        catch( Exception eee )
        {
            return null;
        }
    }
    
    public static final void copyElement(Element src, Element target, XMLParser xml)
	{
		Item[] attrs = XMLParser.getElementAttrs(src);
		for( Item attr : attrs )
		{
			target.setAttribute(attr.getName(), attr.getValue());
		}

		Element src1 = XMLParser.getFirstChildElement( src );
        for( ; src1 != null; src1 = XMLParser.getNextSibling(src1) )
        {
        	Element target1 = xml.createElement(src1.getNodeName());
        	target.appendChild(target1);
        	copyElement(src1, target1, xml);
        }
	}
	
}
