/*
 * Copyright 2015 Thomas Frühbeck, fruehbeck(at)aon(dot)at.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package at.tfr.securefs.cache;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RunAs;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachemanagerlistener.annotation.ViewChanged;
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;
import org.infinispan.remoting.transport.Address;
import org.jboss.logging.Logger;

import at.tfr.securefs.Role;
import at.tfr.securefs.event.SecureFsMonitor;

@Listener(clustered=true)
@Asynchronous
@Stateless
@PermitAll
@RunAs(Role.ADMIN)
public class SecureFsToplogyListener {

	private Logger log = Logger.getLogger(getClass());

	@Inject
	private Event<SecureFsMonitor> monitorEvent;

	@ViewChanged
	public void viewChanged(ViewChangedEvent viewChangedEvent) {
		SecureFsMonitor mevt = new SecureFsMonitor();
		log.info("new view: "+viewChangedEvent.getNewMembers());
		for (Address address : viewChangedEvent.getNewMembers()) {
			mevt.add(address.toString(), address.getClass().getSimpleName(), address.toString());
		}
		monitorEvent.fire(mevt);
	}
}
