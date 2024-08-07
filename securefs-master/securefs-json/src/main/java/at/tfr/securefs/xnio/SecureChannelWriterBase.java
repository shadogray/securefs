/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.xnio;

import at.tfr.securefs.api.Constants;
import at.tfr.securefs.api.json.Message;
import at.tfr.securefs.api.json.Message.MessageType;
import org.jboss.logging.Logger;
import org.xnio.ChannelListener;
import org.xnio.IoUtils;
import org.xnio.channels.StreamSourceChannel;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class SecureChannelWriterBase implements ChannelListener<StreamSourceChannel> {

	private Logger log = Logger.getLogger(getClass());
	private ByteBuffer buff = ByteBuffer.allocate(Constants.BUFFER_SIZE);
	private Message context;
	private boolean sentOK;

	public SecureChannelWriterBase(Message context) {
		this.context = context;
	}

	public void handleEvent(StreamSourceChannel source) {

//		if (!sentOK) {
//			Message ok = null;
//			try {
//				ok = context.reply(MessageSubType.OK);
//				write(ok);
//			} catch (Exception e) {
//				log.warn("cannot write message: "+ok, e);
//			}
//			sentOK = true;
//		}

		Message msg = null;
		buff.clear();
		try {

			int count = source.read(buff);
			buff.flip();
			if (buff.hasRemaining()) {
				msg = new Message(MessageType.DATA, context.getPath(), buff).key(context.getUniqueKey());
				write(msg);
			}
			if (count < 0) {
				IoUtils.safeClose(source);
			}

		} catch (IOException e) {
			log.warn("cannot write message: "+msg, e);
		}
	}

	protected abstract void write(Message message);

}
