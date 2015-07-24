/*
 * Copyright (c) 2015 ETRI and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.etri.impl4inv;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
//import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.provider.rev150105.TsdnInventoryProviderService;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.network.topology.rev150105.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.network.topology.rev150105.TopologyId;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.network.topology.rev150105.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.network.topology.rev150105.network.topology.TopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.network.topology.rev150105.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
//import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hello4invProvider implements BindingAwareProvider, AutoCloseable {

	private static final Logger LOG = LoggerFactory.getLogger(Hello4invProvider.class);
	
	private Thread thread;

	@Override
	public void onSessionInitiated(ProviderContext session) {
		LOG.info("Hello4invProvider Session Initiated");

		//wiring databroker
		final DataBroker db = session.getSALService(DataBroker.class);

		final HelloInventoryOperationProcessor processor = new HelloInventoryOperationProcessor(db);
		
		final NotificationProviderService notiService = session.getSALService(NotificationProviderService.class);
		
//		final TopologyKey key = new TopologyKey(new TopologyId(TOPOLOGY_ID));
//		final InstanceIdentifier<Topology> path = InstanceIdentifier
//				.create(NetworkTopology.class)
//				.child(Topology.class, key);

		HelloInventoryExporter invExporter = new HelloInventoryExporter(processor, notiService, session);

		//rpc for testing notification
		session.addRpcImplementation(TsdnInventoryProviderService.class, invExporter);

		//register notification listener with HelloProvider
		ListenerRegistration<NotificationListener> notiRegistration = notiService.registerNotificationListener(invExporter);

//		final ListenerRegistration<DataChangeListener> dataChangeListenerRegistration2 = 
//				db.registerDataChangeListener(LogicalDatastoreType.CONFIGURATION, 
//						path, topoExporter, DataChangeScope.SUBTREE);
		
		//creating default network
//		final ReadWriteTransaction tx = db.newReadWriteTransaction();
//		tx.put(LogicalDatastoreType.OPERATIONAL, path, new TopologyBuilder().setKey(key).build(), true);
//		tx.put(LogicalDatastoreType.CONFIGURATION, path, new TopologyBuilder().setKey(key).build(), true);
//		tx.submit();
		
		thread = new Thread(processor);
		thread.setDaemon(true);
		thread.setName("HelloInventoryExporter");
		thread.start();
	}

	@Override
	public void close() throws Exception {
		LOG.info("Hello4invProvider Closed");
		
		if (thread != null) {
			thread.interrupt();
			thread.join();
			thread = null;
		}
	}
}
