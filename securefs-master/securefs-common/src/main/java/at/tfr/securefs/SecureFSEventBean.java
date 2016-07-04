package at.tfr.securefs;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Asynchronous;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.DependsOn;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import at.tfr.securefs.event.SecureFs;

@Stateless
@PermitAll
@TransactionManagement(TransactionManagementType.BEAN)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class SecureFSEventBean implements SecureFSEvent {

    @Inject
    private Event<SecureFs> secfsEvent;

    @Override
	@Asynchronous
    public void sendEvent(SecureFs event) {
    	secfsEvent.fire(event);
    }
}
