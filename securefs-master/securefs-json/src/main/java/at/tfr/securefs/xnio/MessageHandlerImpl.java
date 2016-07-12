/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.xnio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.NullCipher;
import javax.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;
import org.xnio.ChannelListener;
import org.xnio.ChannelPipe;
import org.xnio.IoUtils;
import org.xnio.OptionMap;
import org.xnio.Xnio;
import org.xnio.XnioWorker;
import org.xnio.channels.Channels;
import org.xnio.channels.StreamSinkChannel;
import org.xnio.channels.StreamSourceChannel;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.tfr.securefs.Configuration;
import at.tfr.securefs.api.Constants;
import at.tfr.securefs.key.SecretKeySpecBean;
import at.tfr.securefs.xnio.ActiveStreams.StreamInfo;
import at.tfr.securefs.api.json.Message;
import static at.tfr.securefs.api.json.Message.MessageSubType.READ;
import at.tfr.securefs.api.json.Message.MessageType;
import static at.tfr.securefs.api.json.Message.MessageType.CLOSE;
import static at.tfr.securefs.api.json.Message.MessageType.DATA;
import static at.tfr.securefs.api.json.Message.MessageType.ERROR;
import static at.tfr.securefs.api.json.Message.MessageType.OPEN;
import at.tfr.securefs.api.json.MessageHandler;
import at.tfr.securefs.api.json.MessageSender;

@ApplicationScoped
public class MessageHandlerImpl implements MessageHandler {

	private Logger log = Logger.getLogger(getClass());
	private Charset utf8 = Charset.forName("utf-8");
	protected ActiveStreams activeStreams = new ActiveStreams();
	protected XnioWorker xnioWorker;
	protected ObjectMapper objectMapper;
	protected Configuration configuration;
	protected SecretKeySpecBean sksBean;

	public MessageHandlerImpl() {
	}

	public MessageHandlerImpl(Configuration configuration, ObjectMapper objectMapper, SecretKeySpecBean sksBean) {
		this.configuration = configuration;
		this.objectMapper = objectMapper;
		this.sksBean = sksBean;
		try {
			xnioWorker = Xnio.getInstance().createWorker(OptionMap.EMPTY);
		} catch (Exception e) {
			throw new RuntimeException("cannot create XnioWorker", e);
		}
	}

	protected void internalOnOpen() {

	}

	@Override
	public void handleMessage(String json, MessageSender messageSender) throws IOException {

		log.debug("handleMessage: " + json);
		final Message message = objectMapper.readValue(json, Message.class);

		Path path = configuration.getBasePath().resolve(message.getPath());
		if (!path.relativize(configuration.getBasePath()).toString().equals("..")) {
			throw new SecurityException("invalid path spec: " + message.getPath());
		}

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
			case ERROR:
				log.info("error from Client: " + json);
			case CLOSE: {
				if (info != null) {
					IoUtils.safeClose(info.getStream().getRightSide());
				}
			}
				break;

			case OPEN: {
				switch (message.getSubType()) {
				case READ: {
					final InputStream is = Files.newInputStream(path, StandardOpenOption.READ);
					final InputStream cis = new CipherInputStream(is, getCipher(message, Cipher.DECRYPT_MODE));

					final ChannelPipe<StreamSourceChannel, StreamSinkChannel> pipe = xnioWorker.createHalfDuplexPipe();
					pipe.getLeftSide().getReadSetter().set(new SecureChannelWriterBase(message) {
						@Override
						protected void write(Message message) {
							try {
								messageSender.send(message);
							} catch (Exception e) {
								log.warn("cannot write message="+message+" : "+e, e);
							}
						}
					});
					pipe.getLeftSide().getCloseSetter().set(new ChannelListener<StreamSourceChannel>() {

						@Override
						public void handleEvent(StreamSourceChannel channel) {
							activeStreams.getStreams().remove(uniqueKey);
							messageSender.send(new Message(MessageType.CLOSE, message.getPath()).key(uniqueKey));
						}
					});
					pipe.getRightSide().getWriteSetter().set(new ChannelListener<StreamSinkChannel>() {
						private byte[] bytes = new byte[Constants.BUFFER_SIZE];

						@Override
						public void handleEvent(StreamSinkChannel channel) {
							try {
								int count = 0;
								while ((count = cis.read(bytes, 0, bytes.length)) > 0) {
									if (count > 0) {
										Channels.writeBlocking(pipe.getRightSide(), ByteBuffer.wrap(bytes, 0, count));
									}
									if (count < 0) {
										pipe.getRightSide().close();
									} else {
										channel.resumeWrites();
									}
								}
							} catch (Exception e) {
								log.warn("cannot read from cypher: " + e, e);
								IoUtils.safeClose(channel);
							}
						}
					});

					activeStreams.getStreams().put(uniqueKey,
							new StreamInfo<ChannelPipe<StreamSourceChannel, StreamSinkChannel>>(pipe,
									message.getPath()));

					// start sending data:
					pipe.getLeftSide().resumeReads();
					pipe.getRightSide().resumeWrites();
				}
					break;

				case WRITE: {
					Files.createDirectories(path.getParent());
					OutputStream os = Files.newOutputStream(path, StandardOpenOption.CREATE,
							StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
					OutputStream cos = new CipherOutputStream(os, getCipher(message, Cipher.ENCRYPT_MODE));

					ChannelPipe<StreamSourceChannel, StreamSinkChannel> pipe = xnioWorker.createHalfDuplexPipe();

					pipe.getLeftSide().getReadSetter().set(new SecureChannelReaderBase() {

						@Override
						public void handleEvent(StreamSourceChannel channel) {
							readChannel(message, cos, pipe, channel);
						}
					});

					pipe.getLeftSide().getCloseSetter().set(new SecureChannelReaderBase() {
						@Override
						public void handleEvent(StreamSourceChannel channel) {
							try {
								cos.close();
								activeStreams.getStreams().remove(pipe.toString());
								messageSender.send(new Message(MessageType.CLOSE, message.getPath()).key(uniqueKey));
								log.info("closed channel: " + pipe.toString());
							} catch (IOException e) {
								log.warn("cannot close stream: message=" + message + " : " + e, e);
							}
						}
					});
					activeStreams.getStreams().put(uniqueKey,
							new StreamInfo<ChannelPipe<StreamSourceChannel, StreamSinkChannel>>(pipe,
									message.getPath()));

					// start receiving data:
					pipe.getLeftSide().resumeReads();
				}
					break;

				default:
					messageSender.send(new Message(MessageType.ERROR, message.getPath()).key(uniqueKey));
					break;

				}
			}
				break;

			case DATA: {
				if (info != null) {
					Channels.writeBlocking(info.getStream().getRightSide(), ByteBuffer.wrap(message.getBytes()));
				} else {
					messageSender.send(new Message(MessageType.ERROR, message.getPath()).key(uniqueKey));
				}
			}
				break;
			}

		} catch (IOException e) {
			log.warn("cannot handle message: " + message + " : " + e, e);
			throw e;
		} catch (Exception e) {
			log.warn("cannot handle message: " + message + " : " + e, e);
			throw new IOException("cannot handle message: " + message + " : " + e, e);
		}
	}

	private Cipher getCipher(Message message, int mode) throws UnsupportedEncodingException, NoSuchAlgorithmException,
			InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		if (sksBean == null) {
			return new NullCipher();
		}
		return sksBean.getCipher(message.getPath(), mode);
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

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public SecretKeySpecBean getSksBean() {
		return sksBean;
	}

	public void setSksBean(SecretKeySpecBean sksBean) {
		this.sksBean = sksBean;
	}

	public XnioWorker getXnioWorker() {
		return xnioWorker;
	}

	public void setXnioWorker(XnioWorker xnioWorker) {
		this.xnioWorker = xnioWorker;
	}
}
