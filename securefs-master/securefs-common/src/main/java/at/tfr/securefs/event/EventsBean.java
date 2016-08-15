/*
 * Copyright 2016 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.event;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RunAs;
import javax.ejb.Asynchronous;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import at.tfr.securefs.Role;

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
