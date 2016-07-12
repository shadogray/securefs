/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.websocket.EndpointConfig;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.jboss.logging.Logger;

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
