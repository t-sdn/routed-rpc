/*
 * Copyright (c) 2015 ETRI and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.etri.impl;

import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
//import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.provider.rev150105.TestNotificationBetweenModulesInput;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.provider.rev150105.TestNotificationBetweenModulesOutput;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.provider.rev150105.TsdnInventoryProviderService;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.NodeUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.network.topology.rev150105.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.network.topology.rev150105.TopologyId;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.network.topology.rev150105.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.network.topology.rev150105.network.topology.TopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.network.topology.rev150105.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.node.rev150105.NodeId;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.node.rev150105.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.opendaylight.yangtools.yang.common.RpcResult;
//import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloProvider implements BindingAwareProvider, AutoCloseable {

	private static final Logger LOG = LoggerFactory.getLogger(HelloProvider.class);
	static final String TOPOLOGY_ID = "jshinnetwork1";
	private NotificationProviderService notiService;
	
	private Thread thread;
	
//    private RpcRegistration<HelloService> helloService;

//    private ListenerRegistration<NotificationListener> listenerRegistration;

	@Override
	public void onSessionInitiated(ProviderContext session) {
		LOG.info("HelloProvider Session Initiated");

		HelloWorldImpl helloWorldImpl = new HelloWorldImpl();

		//wiring databroker
		final DataBroker db = session.getSALService(DataBroker.class);
		helloWorldImpl.setDB(db);
		final HelloOperationProcessor processor = new HelloOperationProcessor(db);
		
		notiService = session.getSALService(NotificationProviderService.class);

		//generate tree path
		final TopologyKey key = new TopologyKey(new TopologyId(TOPOLOGY_ID));
		final InstanceIdentifier<Topology> path = InstanceIdentifier
				.create(NetworkTopology.class)
				.child(Topology.class, key);
		
		//
		HelloTopologyExporter topoExporter = new HelloTopologyExporter(processor, path);
		
		//register notification listener of Hello4invProvider
		ListenerRegistration<NotificationListener> notiRegistration = notiService.registerNotificationListener(topoExporter);
		
		

		//wiring RPC
		session.addRpcImplementation(HelloService.class, helloWorldImpl);

		//wiring notification
		helloWorldImpl.setNotificationProvider(notiService);
		notiService.registerNotificationListener(helloWorldImpl);

		//register listener for the configurational tree
		final ListenerRegistration<DataChangeListener> dataChangeListenerRegistration = 
				db.registerDataChangeListener(LogicalDatastoreType.CONFIGURATION, 
						helloWorldImpl.HELLO_IID, helloWorldImpl, DataChangeScope.SUBTREE);
		
		//test how to divide actions against 'changeEvent' in 'onDataChanged' method
		/*final ListenerRegistration<DataChangeListener> dataChangeListenerRegistration = 
				db.registerDataChangeListener(LogicalDatastoreType.CONFIGURATION, 
						helloWorldImpl.HELLO_IID, topoExporter, DataChangeScope.SUBTREE);*/
		
		final ListenerRegistration<DataChangeListener> dataChangeListenerRegistration2 = 
				db.registerDataChangeListener(LogicalDatastoreType.CONFIGURATION, 
						path, topoExporter, DataChangeScope.SUBTREE);
		
		//creating default network
		final ReadWriteTransaction tx = db.newReadWriteTransaction();
		tx.put(LogicalDatastoreType.OPERATIONAL, path, new TopologyBuilder().setKey(key).build(), true);
		tx.put(LogicalDatastoreType.CONFIGURATION, path, new TopologyBuilder().setKey(key).build(), true);
		tx.submit();
		
		//threading
		thread = new Thread(processor);
		thread.setDaemon(true);
		thread.setName("HelloTopologyExporter - " + this.TOPOLOGY_ID);
		thread.start();
		
		//test for inserting node
//		topoExporter.testNodeAdded();
	}

	@Override
	public void close() throws Exception {
		LOG.info("HelloProvider Closed");
		
		if (thread != null) {
			thread.interrupt();
			thread.join();
			thread = null;
		}
	}
}
