/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
package com.focus.net;

public class BufferExhaustedException extends java.io.IOException
{
	private static final long serialVersionUID = 1084909263059304903L;

	public BufferExhaustedException()
    {
        super("Buffer exhausted!");
    }
}
