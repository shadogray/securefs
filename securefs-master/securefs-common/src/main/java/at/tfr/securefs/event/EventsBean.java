/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.event;

import at.tfr.securefs.Role;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.*;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

@Stateless
@PermitAll
@RunAs(Role.OPERATOR)
@TransactionManagement(TransactionManagementType.BEAN)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class EventsBean implements Events {

    @Inject
    private Event<SecureFsEvent> secfsEvent;

    @Override
	@Asynchronous
    public void sendEvent(SecureFsEvent event) {
    	secfsEvent.fire(event);
    }
}
