package at.tfr.securefs;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.crypto.NoSuchPaddingException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.OnMessage;
import javax.websocket.Session;

import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.tfr.securefs.key.SecretKeySpecBean;
import at.tfr.securefs.xnio.Message;
import at.tfr.securefs.xnio.MessageHandlerImpl;
import at.tfr.securefs.xnio.MessageSender;

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
