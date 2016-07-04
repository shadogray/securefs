package at.tfr.securefs;

import javax.ejb.Asynchronous;

import at.tfr.securefs.event.SecureFs;

public interface SecureFSEvent {

	void sendEvent(SecureFs event);

}