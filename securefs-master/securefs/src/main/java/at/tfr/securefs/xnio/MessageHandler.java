package at.tfr.securefs.xnio;

import java.io.IOException;

public interface MessageHandler {

	public void handleMessage(String json, MessageSender messageSender) throws IOException;

}
