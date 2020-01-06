package com.focus.cos.api.email.javamail;

import java.io.File;
import java.io.IOException;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;

import com.focus.cos.api.email.io.ClassPathResource;
import com.focus.cos.api.email.io.Resource;

public class ConfigurableMimeFileTypeMap extends FileTypeMap
{
	private Resource mappingLocation = new ClassPathResource("mime.types", getClass());

	private String[] mappings;

	private FileTypeMap fileTypeMap;

	public void setMappingLocation(Resource mappingLocation)
	{
		this.mappingLocation = mappingLocation;
	}

	public void setMappings(String[] mappings)
	{
		this.mappings = mappings;
	}

	public void afterPropertiesSet()
	{
		getFileTypeMap();
	}

	protected final FileTypeMap getFileTypeMap()
	{
		if (this.fileTypeMap == null)
		{
			try
			{
				this.fileTypeMap = createFileTypeMap(this.mappingLocation, this.mappings);
			}
			catch (IOException ex)
			{
				throw new IllegalStateException("Could not load specified MIME type mapping file: " + this.mappingLocation);
			}
		}
		return fileTypeMap;
	}

	protected FileTypeMap createFileTypeMap(Resource mappingLocation, String[] mappings) throws IOException
	{
		MimetypesFileTypeMap fileTypeMap = (mappingLocation != null) ? new MimetypesFileTypeMap(mappingLocation.getInputStream()) : new MimetypesFileTypeMap();
		if (mappings != null)
		{
			for (int i = 0; i < mappings.length; i++)
			{
				fileTypeMap.addMimeTypes(mappings[i]);
			}
		}
		return fileTypeMap;
	}

	public String getContentType(File file)
	{
		return getFileTypeMap().getContentType(file);
	}

	public String getContentType(String fileName)
	{
		return getFileTypeMap().getContentType(fileName);
	}
}
