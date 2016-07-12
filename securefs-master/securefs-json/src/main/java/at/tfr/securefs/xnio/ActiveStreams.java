/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.xnio;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.xnio.ChannelPipe;
import org.xnio.channels.StreamSinkChannel;
import org.xnio.channels.StreamSourceChannel;

public class ActiveStreams implements Serializable {

	private Map<String, StreamInfo<ChannelPipe<StreamSourceChannel, StreamSinkChannel>>> streams = new HashMap<>();

	public Map<String, StreamInfo<ChannelPipe<StreamSourceChannel, StreamSinkChannel>>> getStreams() {
		return streams;
	}

	public static class StreamInfo<S> implements Serializable {

		private transient S stream;
		private String path;

		public StreamInfo() {
		}

		public StreamInfo(S stream, String path) {
			this.stream = stream;
			this.path = path;
		}

		public S getStream() {
			return stream;
		}

		public void setStream(S stream) {
			this.stream = stream;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}
	}
}
