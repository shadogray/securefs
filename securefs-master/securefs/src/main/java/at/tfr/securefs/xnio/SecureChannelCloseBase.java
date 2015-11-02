package at.tfr.securefs.xnio;

import java.nio.ByteBuffer;

import org.xnio.ChannelListener;
import org.xnio.channels.StreamSourceChannel;

import at.tfr.securefs.xnio.Message.MessageType;

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
