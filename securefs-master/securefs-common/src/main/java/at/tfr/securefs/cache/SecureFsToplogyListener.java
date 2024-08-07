/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.cache;

import at.tfr.securefs.Role;
import at.tfr.securefs.beans.Logging;
import at.tfr.securefs.event.Events;
import at.tfr.securefs.event.SecureFsMonitor;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachemanagerlistener.annotation.ViewChanged;
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;
import org.infinispan.remoting.transport.Address;
import org.jboss.logging.Logger;

@Listener(clustered=true)
@Asynchronous
@Stateless
@PermitAll
@RunAs(Role.ADMIN)
@Logging
public class SecureFsToplogyListener {

	private Logger log = Logger.getLogger(getClass());

	private Events events;
	
	public SecureFsToplogyListener() {
	}
	
	@Inject
	public SecureFsToplogyListener(Events events) {
		this.events = events;
	}

	@ViewChanged
	public void viewChanged(ViewChangedEvent viewChangedEvent) {
		SecureFsMonitor mevt = new SecureFsMonitor();
		log.info("new view: "+viewChangedEvent.getNewMembers());
		for (Address address : viewChangedEvent.getNewMembers()) {
			mevt.add(address.toString(), address.getClass().getSimpleName(), address.toString());
		}
		events.sendEvent(mevt);
	}
}
