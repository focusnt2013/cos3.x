package com.focus.cos.api.email.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.focus.cos.api.StringUtils;

public abstract class AbstractResource implements Resource
{

	public boolean exists()
	{
		try
		{
			return getFile().exists();
		}
		catch (IOException ex)
		{
			// Fall back to stream existence: can we open the stream?
			try
			{
				InputStream is = getInputStream();
				is.close();
				return true;
			}
			catch (Throwable isEx)
			{
				return false;
			}
		}
	}

	public boolean isReadable()
	{
		return true;
	}

	public boolean isOpen()
	{
		return false;
	}

	public URL getURL() throws IOException
	{
		throw new FileNotFoundException(getDescription() + " cannot be resolved to URL");
	}

	public URI getURI() throws IOException
	{
		URL url = getURL();
		try
		{
			String location = url.toString();
			return new URI(StringUtils.replace(location, " ", "%20"));
		}
		catch (URISyntaxException ex)
		{
			throw new IOException("Invalid URI [" + url + "]", ex);
		}
	}

	public File getFile() throws IOException
	{
		throw new FileNotFoundException(getDescription() + " cannot be resolved to absolute file path");
	}

	public long lastModified() throws IOException
	{
		long lastModified = getFileForLastModifiedCheck().lastModified();
		if (lastModified == 0L)
		{
			throw new FileNotFoundException(getDescription() + " cannot be resolved in the file system for resolving its last-modified timestamp");
		}
		return lastModified;
	}

	protected File getFileForLastModifiedCheck() throws IOException
	{
		return getFile();
	}

	public Resource createRelative(String relativePath) throws IOException
	{
		throw new FileNotFoundException("Cannot create a relative resource for " + getDescription());
	}

	public String getFilename() throws IllegalStateException
	{
		throw new IllegalStateException(getDescription() + " does not carry a filename");
	}

	public String toString()
	{
		return getDescription();
	}

	public boolean equals(Object obj)
	{
		return (obj == this || (obj instanceof Resource && ((Resource) obj).getDescription().equals(getDescription())));
	}

	public int hashCode()
	{
		return getDescription().hashCode();
	}
}
