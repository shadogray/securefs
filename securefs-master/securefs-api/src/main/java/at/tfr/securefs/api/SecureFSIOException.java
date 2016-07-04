package at.tfr.securefs.api;

import java.io.IOException;

import javax.ejb.ApplicationException;

@ApplicationException(inherited=true, rollback=true)
public class SecureFSIOException extends IOException {

	public SecureFSIOException() {
		super();
	}

	public SecureFSIOException(String message, Throwable cause) {
		super(message, cause);
	}

	public SecureFSIOException(String message) {
		super(message);
	}

	public SecureFSIOException(Throwable cause) {
		super(cause);
	}
}
