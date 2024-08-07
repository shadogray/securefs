/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.xnio;

import at.tfr.securefs.api.Constants;
import at.tfr.securefs.api.json.Message;
import org.jboss.logging.Logger;
import org.xnio.ChannelListener;
import org.xnio.ChannelPipe;
import org.xnio.IoUtils;
import org.xnio.channels.StreamSinkChannel;
import org.xnio.channels.StreamSourceChannel;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public abstract class SecureChannelReaderBase implements ChannelListener<StreamSourceChannel> {

	private Logger log = Logger.getLogger(getClass());
	private ByteBuffer buff = ByteBuffer.allocate(Constants.BUFFER_SIZE);

	protected void readChannel(Message message, OutputStream cos,
			ChannelPipe<StreamSourceChannel, StreamSinkChannel> pipe, StreamSourceChannel channel) {
		buff.clear();
		try {

			int count = 0;
			while ((count = channel.read(buff)) > 0) {

				buff.flip();

				if (buff.hasRemaining()) {
					log.debug("read: buff="+buff+" "+ new String(Arrays.copyOf(buff.array(), buff.remaining())));
					cos.write(buff.array(), buff.position(), buff.limit());
					cos.flush();
				}
			}

			if (count < 0) {
				cos.close();
				IoUtils.safeClose(channel);
			} else {
				channel.resumeReads();
			}

		} catch (IOException e) {
			log.warn("cannot read stream: message=" + message + " : " + e, e);
			IoUtils.safeClose(channel);
		}
	}

}
