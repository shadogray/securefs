/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.xnio;

import at.tfr.securefs.api.json.Message;
import at.tfr.securefs.api.json.Message.MessageType;

import org.xnio.ChannelListener;
import org.xnio.channels.StreamSourceChannel;


public abstract class SecureChannelCloseBase implements ChannelListener<StreamSourceChannel> {

	private Message context;

	public SecureChannelCloseBase(Message context) {
		this.context = context;
	}

	public void handleEvent(StreamSourceChannel channel) {
		write(new Message(MessageType.CLOSE, context.getPath(), (byte[])null).key(context.getUniqueKey()));
	}

	public abstract void write(Message message);

}
