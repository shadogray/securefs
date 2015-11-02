/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs;

import at.tfr.securefs.Configuration;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jboss.logging.Logger;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.tfr.securefs.client.ClientMessageHandlerImpl;
import at.tfr.securefs.xnio.Message;
import at.tfr.securefs.xnio.MessageSender;
import at.tfr.securefs.xnio.Message.MessageType;

public class TestWebSocket {

	String localhost = "ws://localhost:8080/securefile/socket";

	@Test
	public void testSendFile() throws Exception {

		Path path = Paths.get(getClass().getResource("/test.txt").toURI());
		Message m = new Message(MessageType.OPEN, "test.txt");

		WebsocketHandler wh = new WebsocketHandler(new URI(localhost), m, path.getParent());
		wh.connectBlocking();
		synchronized (wh) {
			wh.wait(100000L);
		}

	}

	class WebsocketHandler extends WebSocketClient {

		private Logger log = Logger.getLogger(getClass());
		private Path basePath;
		private Message m;
		private ObjectMapper om = new ObjectMapper();
		private Configuration configuration = new Configuration();
		private MessageSender messageSender = new TestMessageSender();

		private ClientMessageHandlerImpl messageHandler = new ClientMessageHandlerImpl(configuration, om, messageSender);
		private boolean fileWrite = false;
		private boolean fileRead = false;
		private String path = "test.txt";

		public WebsocketHandler(URI uri, Message m, Path basePath) {
			super(uri);
			this.m = m;
			this.basePath = basePath;
			configuration.setBasePath(basePath);
		}

		@Override
		public void onOpen(ServerHandshake handshakedata) {
			System.out.println("onOpen: handshake=" + handshakedata);
			try {
				InputStream is = Files.newInputStream(configuration.getBasePath().resolve(path));
				messageHandler.write(is, path);

				Thread.sleep(2000);

				String outPath = path + ".out";
				OutputStream os = Files.newOutputStream(configuration.getBasePath().resolve(outPath));
				messageHandler.read(os, path);

			} catch (Exception e) {
				e.printStackTrace();
				try {
					send(om.writeValueAsString(new Message(MessageType.CLOSE, m.getPath())));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
//			synchronized (this) {
//				this.notifyAll();
//			}
		}

		@Override
		public void onMessage(String message) {
			System.out.println("onMessage: message=" + message);

			try {
				messageHandler.handleMessage(message, messageSender);
			} catch (Exception e) {
				e.printStackTrace();
			}

//			synchronized (WebsocketHandler.this) {
//				WebsocketHandler.this.notifyAll();
//			}
		}

		@Override
		public void onClose(int code, String reason, boolean remote) {
			System.out.println("onClose: code=" + code + ", reason=" + reason);
		}

		@Override
		public void onError(Exception ex) {
			System.out.println("onError: exc=" + ex);
		}

		class TestMessageSender implements MessageSender {
			@Override
			public void send(Message message) {
				try {
					WebsocketHandler.this.send(om.writeValueAsString(message));
				} catch (Exception e) {
					log.warn("cannot send message="+message, e);
				}
			}
		}

	}

}
