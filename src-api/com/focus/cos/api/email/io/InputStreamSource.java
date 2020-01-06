package com.focus.cos.api.email.io;

import java.io.IOException;
import java.io.InputStream;

public interface InputStreamSource
{
	InputStream getInputStream() throws IOException;
}
