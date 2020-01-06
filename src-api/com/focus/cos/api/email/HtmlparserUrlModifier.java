package com.focus.cos.api.email;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.visitors.UrlModifyingVisitor;

import com.focus.util.Tools;

public class HtmlparserUrlModifier extends UrlModifyingVisitor
{
	private URL base = null;
	private HashMap<String,String> imgMap = new HashMap<String,String>();

	public HtmlparserUrlModifier(URL base)
	{
		super("");
		this.setBaseUrl(base);
	}

	public void setBaseUrl(URL base)
	{
		if (!isHttpLikeProtocolUrl(base))
			throw new IllegalArgumentException(String.format("Base url argument '%s' is not http like protocol. " + "They are not prefix with '%s' or '%s'",
			this.base.toString(), "http", "https"));

		this.base = base;
	}

	public void visitStringNode(Text stringNode)
	{
	}

	public void visitTag(Tag tag)
	{
		try
		{
			if (tag instanceof ImageTag)
			{
				ImageTag img = (ImageTag) tag;
				URL url = new URL(base, img.getImageURL());
				String imgUrl = img.getImageURL();
				String key = Tools.encodeMD5(imgUrl);
				if (isHttpLikeProtocolUrl(url))
				{
					img.setImageURL("cid:"+key);
					imgMap.put(key, imgUrl);
				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		super.visitTag(tag);
	}

	protected String modifying(URL url) throws MalformedURLException
	{
		return null;
	}

	private boolean isHttpLikeProtocolUrl(URL url)
	{
		if(url == null) return false;
		String protocol = url.getProtocol().toLowerCase();
		return protocol.equals("http") || protocol.equals("https");
	}

	public HashMap<String, String> getImgMap()
	{
		return imgMap;
	}
}
