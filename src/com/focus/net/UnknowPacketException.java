/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
package com.focus.net;

public class UnknowPacketException extends java.io.IOException
{
	private static final long serialVersionUID = 4983343352355527477L;

	public UnknowPacketException()
    {
        super("Invalid input packet!");
    }
}
