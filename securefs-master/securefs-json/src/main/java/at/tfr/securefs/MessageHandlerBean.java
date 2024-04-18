/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs;

import at.tfr.securefs.api.json.Message;
import at.tfr.securefs.api.json.MessageSender;
import at.tfr.securefs.key.SecretKeySpecBean;
import at.tfr.securefs.xnio.MessageHandlerImpl;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.OnMessage;
import jakarta.websocket.Session;
import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class MessageHandlerBean extends MessageHandlerImpl {

	private Logger log = Logger.getLogger(getClass());

	public MessageHandlerBean() {
	}

	@Inject
	public MessageHandlerBean(Configuration configuration, ObjectMapper objectMapper, SecretKeySpecBean sksBean) {
		super(configuration, objectMapper, sksBean);
	}

	@PostConstruct
	protected void init() {
	}

	@OnMessage
	public void handleMessage(String json, Session wsSession) throws JsonParseException, JsonMappingException,
			IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, InterruptedException, ExecutionException {

		try {
			handleMessage(json, new WebSocketSender(wsSession));
		} catch (Exception e) {
			log.warn("cannot handle message=" + StringUtils.abbreviate(json, 100) + " : " + e, e);
		}
	}

	class WebSocketSender implements MessageSender {
		private Session session;

		public WebSocketSender(Session session) {
			this.session = session;
		}

		@Override
		public void send(Message message) {
			try {
				session.getBasicRemote().sendText(objectMapper.writeValueAsString(message));
			} catch (IOException e) {
				log.warn("cannot write message=" + message, e);
			}
		}
	}

}
