/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.api;

import java.nio.charset.Charset;

public class Constants {

	public static Charset UTF8 = Charset.forName("UTF-8");
	
	public static final int BUFFER_SIZE = 10240;

	public static enum Property { ignoreFileNameRegex, ignoreFileContentRegex };
}
