/* ZeroBPMFInput for Android Platform Version 0.1
 * 
 * Copyright (c) 2008 Zero, Chien-An Cho
 * (MIT License)
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 * Contact:: itszero at gmail dot com
 * 
 * FileDownload: Download file from specified URL and save to local file
 */
package idv.Zero.ZeroBPMFInput;

import java.io.*;
import java.net.*;

public class FileDownload {
	public static boolean download(String remoteUrl, String saveTo)
	{
		try {
			URL url = new URL(remoteUrl);
			OutputStream out = new BufferedOutputStream(new FileOutputStream(saveTo));
			InputStream in = url.openConnection().getInputStream();
			byte[] buffer = new byte[1024];
			int blockSize;
			while ((blockSize = in.read(buffer)) != -1) {
				out.write(buffer, 0, blockSize);
			}
			
			in.close();
			out.close();
		}
		catch (Exception exception) {
			return false;
		}
		
		return true;
	}
}