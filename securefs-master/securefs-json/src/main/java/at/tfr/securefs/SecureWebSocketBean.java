/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.jboss.logging.Logger;

import java.io.IOException;

@ServerEndpoint(value = "/socket")
public class SecureWebSocketBean {

	private Logger log = Logger.getLogger(getClass());
	@Inject
	private MessageHandlerBean messageHandler;

	@PostConstruct
	private void init() {
	}

	@OnMessage(maxMessageSize = 11 * 1024)
	public void receive(String json, Session session) throws IOException {

		log.debug("onMessage: " + json);
		try {
			
			messageHandler.handleMessage(json, session);
			
		} catch (Exception e) {
			log.info("onMessage: " + e, e);
			throw new IOException("onMessage: " + e, e);
		}
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig endpointConfig) {

	}

}
