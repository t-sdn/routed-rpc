package org.etri.impl;

import java.util.Collections;
import java.util.Set;

import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.AccessIfRemoved;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.AccessIfUpdated;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.NodeConnectorRemoved;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.NodeRemoved;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.TsdnInventoryListener;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.TunnelRemoved;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.TunnelUpdated;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.TunnelXcRemoved;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.TunnelXcUpdated;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.network.topology.rev150105.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.network.topology.rev150105.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.network.topology.rev150105.network.topology.topology.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.network.topology.rev150105.network.topology.topology.NodeKey;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.node.rev150105.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorld;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
//import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.Nodes;

//DataChangeListener: to hear changes of data tree
//TsdnInventoryListener: to hear notification
class HelloTopologyExporter <T extends DataObject> implements DataChangeListener, TsdnInventoryListener {

	private static final Logger LOG = LoggerFactory.getLogger(HelloTopologyExporter.class);
	
	private final InstanceIdentifier<Topology> topologyIID;
	private final HelloOperationProcessor processor;

	HelloTopologyExporter (final HelloOperationProcessor processor,
			final InstanceIdentifier<Topology> topology) {
		this.processor = Preconditions.checkNotNull(processor);
		this.topologyIID = Preconditions.checkNotNull(topology);
	}
	
	//test for writing to operational data tree
	public void testNodeAdded() {
		LOG.info("testNodeAdded - HelloTopologyExporter");
		
		processor.enqueueOperation(new HelloTopologyOperation() {
			@Override
			public void applyOperation(ReadWriteTransaction transaction) {
				NodeKey nodeKey = new NodeKey(new NodeId("jshinnode1"));
				InstanceIdentifier<Node> path = topologyIID.child(Node.class, nodeKey);
				Node topoNode = new NodeBuilder().setKey(nodeKey).build();
				transaction.merge(LogicalDatastoreType.OPERATIONAL, path, topoNode, true);
			}
		});
	}

	
	private void doDataCopy ( AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changeEvent ) {

		//Extracts creation or deletion events from changeEvent.
		Set<InstanceIdentifier<?>> createdData = changeEvent.getCreatedData() != null ?
				changeEvent.getCreatedData().keySet() : Collections.<InstanceIdentifier<?>>emptySet();
		Set<InstanceIdentifier<?>> removedData = changeEvent.getRemovedPaths() != null ?
				changeEvent.getRemovedPaths() : Collections.<InstanceIdentifier<?>>emptySet();

		//If configurational data is changed, copy it into operational data.
		for (InstanceIdentifier<?> entryKey : removedData) {
			final NodeKey nodeKey = 
					entryKey.firstIdentifierOf(Node.class).firstKeyOf(Node.class, NodeKey.class);
			processor.enqueueOperation(new HelloTopologyOperation() {
				@Override
				public void applyOperation(ReadWriteTransaction transaction) {
					InstanceIdentifier<Node> path = topologyIID.child(Node.class, nodeKey);
					Node topoNode = new NodeBuilder().setKey(nodeKey).build();
					transaction.delete(LogicalDatastoreType.OPERATIONAL, path);
//					LOG.info("onDataChanged - oper node is deleted: " + path.toString());
					LOG.info("onDataChanged - oper node is deleted");
				}
			});
		}
		
		for (InstanceIdentifier<?> entryKey : createdData) {
			final NodeKey nodeKey = 
					entryKey.firstIdentifierOf(Node.class).firstKeyOf(Node.class, NodeKey.class);
			processor.enqueueOperation(new HelloTopologyOperation() {
				@Override
				public void applyOperation(ReadWriteTransaction transaction) {
					InstanceIdentifier<Node> path = topologyIID.child(Node.class, nodeKey);
					Node topoNode = new NodeBuilder().setKey(nodeKey).build();
					transaction.merge(LogicalDatastoreType.OPERATIONAL, path, topoNode, true);
//					LOG.info("onDataChanged - oper node is added: " + path.toString());
					LOG.info("onDataChanged - oper node is added");
				}
			});
		}
	}
	
	/*
	 * DataChangeListener
	 */
	@Override
	public void onDataChanged( AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changeEvent ) {
		
		for ( InstanceIdentifier<?> key : changeEvent.getUpdatedData().keySet() ) {
			Object obj = changeEvent.getUpdatedData().get(key);
			LOG.info(obj.toString());
			
			//test how to divide actions against 'changeEvent' in 'onDataChanged' method
			if ( obj instanceof Topology ) {
				doDataCopy (changeEvent);
				LOG.info("onDataChanged - TopologyExporter - received HelloTopologyExporter object");
			} else if ( obj instanceof HelloWorld ){
				LOG.info("onDataChanged - TopologyExporter - received HelloWorldImpl object");
			} else {
				LOG.info("onDataChanged - TopologyExporter - unmatch");
			}
		}
	}
	
	/*
	 * TsdnInventoryListener
	 */
	@Override
	public void onAccessIfRemoved(AccessIfRemoved notification) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAccessIfUpdated(AccessIfUpdated notification) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNodeConnectorRemoved(NodeConnectorRemoved notification) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNodeConnectorUpdated(NodeConnectorUpdated notification) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNodeRemoved(NodeRemoved notification) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNodeUpdated(NodeUpdated notification) {
		//notification from HelloInventoryExporter
		LOG.info("HelloTopologyExporter onNodeUpdated: " + notification.getName());
	}

	@Override
	public void onTunnelRemoved(TunnelRemoved notification) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTunnelUpdated(TunnelUpdated notification) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTunnelXcRemoved(TunnelXcRemoved notification) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTunnelXcUpdated(TunnelXcUpdated notification) {
		// TODO Auto-generated method stub
		
	}

}