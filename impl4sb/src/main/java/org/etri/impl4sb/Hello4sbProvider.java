/*
 * Copyright (c) 2015 ETRI and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.etri.impl4sb;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
//import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.impl4sb.rev141210.Hello4sbRuntimeMXBean;
//import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hello4sbProvider implements BindingAwareProvider, AutoCloseable, Hello4sbRuntimeMXBean {

	private static final Logger LOG = LoggerFactory.getLogger(Hello4sbProvider.class);
	
	private Thread thread;

	@Override
	public void onSessionInitiated(ProviderContext session) {
		LOG.info("Hello4invProvider Session Initiated");

		//wiring databroker
		final DataBroker db = session.getSALService(DataBroker.class);

		final HelloSouthboundOperationProcessor processor = new HelloSouthboundOperationProcessor(db);
		
		final NotificationProviderService notiService = session.getSALService(NotificationProviderService.class);
		
		HelloSouthboundExporter sbExporter = new HelloSouthboundExporter(session, processor, notiService);

		thread = new Thread(processor);
		thread.setDaemon(true);
		thread.setName("HelloInventoryExporter");
		thread.start();
	}

	/*
	 * AutoCloseable
	 */
	@Override
	public void close() throws Exception {
		LOG.info("Hello4invProvider Closed");
		
		if (thread != null) {
			thread.interrupt();
			thread.join();
			thread = null;
		}
	}

	/*
	 * Hello4sbRuntimeMXBean
	 */
	@Override
	public void updateSbNode(String id) {
		LOG.info("Hello4invProvider - updateSbNode: " + id);
		
	}
}
