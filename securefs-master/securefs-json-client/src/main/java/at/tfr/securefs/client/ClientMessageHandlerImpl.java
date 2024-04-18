/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.client;

import at.tfr.securefs.api.Constants;
import at.tfr.securefs.api.json.Message;
import at.tfr.securefs.api.json.Message.MessageSubType;
import at.tfr.securefs.api.json.Message.MessageType;
import at.tfr.securefs.api.json.MessageHandler;
import at.tfr.securefs.api.json.MessageSender;
import at.tfr.securefs.xnio.ActiveStreams;
import at.tfr.securefs.xnio.ActiveStreams.StreamInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;
import org.xnio.*;
import org.xnio.channels.Channels;
import org.xnio.channels.StreamSinkChannel;
import org.xnio.channels.StreamSourceChannel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class ClientMessageHandlerImpl implements MessageHandler {

	private Logger log = Logger.getLogger(getClass());
	private Charset utf8 = Charset.forName("utf-8");
	protected ActiveStreams activeStreams = new ActiveStreams();
	protected XnioWorker xnioWorker;
	protected ObjectMapper objectMapper;
	protected MessageSender messageSender;

	public ClientMessageHandlerImpl() {
	}

	public ClientMessageHandlerImpl(String basePath, ObjectMapper objectMapper, MessageSender messageSender) {
		this.objectMapper = objectMapper;
		this.messageSender = messageSender;
		try {
			xnioWorker = Xnio.getInstance().createWorker(OptionMap.EMPTY);
		} catch (Exception e) {
			throw new RuntimeException("cannot create XnioWorker", e);
		}
	}

	protected void internalOnOpen() {

	}

	public void write(InputStream is, final String path) throws IOException {

		final ChannelPipe<StreamSourceChannel, StreamSinkChannel> pipe = xnioWorker.createHalfDuplexPipe();
		final String uniqueKey = pipe.toString();

		pipe.getLeftSide().getReadSetter().set(new ChannelListener<StreamSourceChannel>() {
			private final ByteBuffer buff = ByteBuffer.allocate(Constants.BUFFER_SIZE);

			public void handleEvent(StreamSourceChannel channel) {
				Message msg = null;
				buff.clear();
				try {

					int count;
					while ((count = channel.read(buff)) > 0) {
						buff.flip();
						if (buff.hasRemaining()) {
							msg = new Message(MessageType.DATA, path, buff).key(uniqueKey);
							messageSender.send(msg);
						}
					}
					if (count < 0) {
						IoUtils.safeClose(channel);
					} else {
						channel.resumeReads();
					}

				} catch (IOException e) {
					log.warn("cannot write message: " + msg, e);
					IoUtils.safeClose(channel);
				}
			}
		});

		pipe.getLeftSide().getCloseSetter().set(new ChannelListener<StreamSourceChannel>() {

			@Override
			public void handleEvent(StreamSourceChannel channel) {
				activeStreams.getStreams().remove(uniqueKey);
				messageSender.send(new Message(MessageType.CLOSE, path).key(uniqueKey));
			}
		});

		pipe.getRightSide().getWriteSetter().set(new ChannelListener<StreamSinkChannel>() {
			private byte[] bytes = new byte[Constants.BUFFER_SIZE];

			@Override
			public void handleEvent(StreamSinkChannel channel) {
				try {
					int count = 0;
					while ((count = is.read(bytes, 0, bytes.length)) > 0) {
						Channels.writeBlocking(channel, ByteBuffer.wrap(bytes, 0, count));
					}

					if (count < 0) {
						IoUtils.safeClose(channel);
					} else {
						channel.resumeWrites();
					}
				} catch (Exception e) {
					log.warn("cannot read from cypher: " + e, e);
					IoUtils.safeClose(channel);
				}
			}
		});

		try {
			messageSender.send(new Message(MessageType.OPEN, MessageSubType.WRITE, path).key(uniqueKey));
		} catch (Exception e) {
			IoUtils.safeClose(pipe.getLeftSide());
			IoUtils.safeClose(pipe.getRightSide());
			throw new IOException("cannot initiate OPEN: "+e, e);
		}

		activeStreams.getStreams().put(uniqueKey,
				new StreamInfo<ChannelPipe<StreamSourceChannel, StreamSinkChannel>>(pipe, path));

		// start sending data:
		pipe.getLeftSide().resumeReads();
		pipe.getRightSide().resumeWrites();
	}

	public void read(OutputStream os, final String path) throws IOException {

		final ChannelPipe<StreamSourceChannel, StreamSinkChannel> pipe = xnioWorker.createHalfDuplexPipe();
		final String uniqueKey = pipe.toString();

		pipe.getLeftSide().getReadSetter().set(new ChannelListener<StreamSourceChannel>() {
			private ByteBuffer buff = ByteBuffer.allocate(Constants.BUFFER_SIZE);

			@Override
			public void handleEvent(StreamSourceChannel channel) {

				buff.clear();
				try {

					int count = channel.read(buff);
					buff.flip();

					if (buff.hasRemaining()) {
						log.debug("read: buff=" + buff + " " + new String(Arrays.copyOf(buff.array(), buff.remaining())));
						os.write(buff.array(), buff.position(), buff.limit());
						os.flush();
					}

					if (count < 0) {
						IoUtils.safeClose(channel);
					} else {
						channel.resumeReads();
					}

				} catch (IOException e) {
					log.warn("cannot read stream: " + e, e);
					IoUtils.safeClose(channel);
				}
			}
		});

		pipe.getLeftSide().getCloseSetter().set(new ChannelListener<StreamSourceChannel>() {
			@Override
			public void handleEvent(StreamSourceChannel channel) {
				try {
					os.close();
					activeStreams.getStreams().remove(pipe.toString());
					log.info("closed channel: " + pipe.toString());
				} catch (IOException e) {
					log.warn("cannot close stream: " + e, e);
				}
			}
		});

		try {
			messageSender.send(new Message(MessageType.OPEN, MessageSubType.READ, path).key(uniqueKey));
		} catch (Exception e) {
			IoUtils.safeClose(pipe.getLeftSide());
			IoUtils.safeClose(pipe.getRightSide());
			throw new IOException("cannot initiate OPEN: "+e, e);
		}

		activeStreams.getStreams().put(uniqueKey,
				new StreamInfo<ChannelPipe<StreamSourceChannel, StreamSinkChannel>>(pipe, path));

		// start receiving data:
		pipe.getLeftSide().resumeReads();
	}

	@Override
	public void handleMessage(String json, MessageSender messageSender) throws IOException {

		log.debug("handleMessage: " + json);
		final Message message = objectMapper.readValue(json, Message.class);

		try {
			final String uniqueKey = message.getUniqueKey();
			// find the Channel for this data stream:
			StreamInfo<ChannelPipe<StreamSourceChannel, StreamSinkChannel>> info = activeStreams.getStreams()
					.get(uniqueKey);

			if (message.getType() == MessageType.OPEN && info != null) {
				log.warn("illegal state on Open stream: " + message);
				IoUtils.safeClose(info.getStream().getRightSide());
				messageSender.send(new Message(MessageType.ERROR, message.getPath()).key(uniqueKey));
			}

			switch (message.getType()) {
			case CLOSE: {
				if (info != null) {
					IoUtils.safeClose(info.getStream().getRightSide());
				}
			}

			case DATA: {
				if (info != null) {
					Channels.writeBlocking(info.getStream().getRightSide(), ByteBuffer.wrap(message.getBytes()));
				} else {
					messageSender.send(new Message(MessageType.ERROR, message.getPath()).key(uniqueKey));
				}
			}
				break;
			}

		} catch (Exception e) {
			log.warn("cannot handle message: " + message + " : " + e, e);
			throw new IOException("cannot handle message: " + message + " : " + e, e);
		}
	}

	public ActiveStreams getActiveStreams() {
		return activeStreams;
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public XnioWorker getXnioWorker() {
		return xnioWorker;
	}

	public void setXnioWorker(XnioWorker xnioWorker) {
		this.xnioWorker = xnioWorker;
	}
}
