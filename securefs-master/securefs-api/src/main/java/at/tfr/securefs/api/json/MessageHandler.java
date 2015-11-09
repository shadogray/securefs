package at.tfr.securefs.api.json;

import java.io.IOException;

public interface MessageHandler {

	public void handleMessage(String json, MessageSender messageSender) throws IOException;

}
