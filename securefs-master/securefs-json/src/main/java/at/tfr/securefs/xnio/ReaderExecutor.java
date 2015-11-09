package at.tfr.securefs.xnio;

import java.io.InputStream;

import javax.websocket.Session;

public interface ReaderExecutor {

	void execute(Session session, String path, InputStream sios);

}