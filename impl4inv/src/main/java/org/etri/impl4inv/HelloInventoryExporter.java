package org.etri.impl4inv;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.general.types.rev150105.ResponseType;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.provider.rev150105.TestNotificationBetweenModulesInput;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.provider.rev150105.TestNotificationBetweenModulesOutput;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.provider.rev150105.TestNotificationBetweenModulesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.provider.rev150105.TestRoutedRpcInput;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.provider.rev150105.TestRoutedRpcOutput;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.provider.rev150105.TestRoutedRpcOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.provider.rev150105.TsdnInventoryProviderService;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.AccessIfRemoved;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.AccessIfUpdated;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.NodeConnectorRemoved;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.NodeRemoved;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.NodeUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.Nodes;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.NodesBuilder;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.SetTunnelInput;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.SetTunnelInputBuilder;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.SetTunnelOutput;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.SetTunnelOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.TsdnInventoryListener;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.TsdnInventoryService;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.TunnelRemoved;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.TunnelUpdated;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.TunnelXcRemoved;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.TunnelXcUpdated;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.node.rev150105.NodeId;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.node.rev150105.NodeRef;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.node.rev150105.NodeType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldOutputBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

//TsdnInventoryListener: to hear notification
//TsdnInventoryProviderService: to implement rpc method
class HelloInventoryExporter <T extends DataObject> implements TsdnInventoryListener, TsdnInventoryProviderService {

	private static final Logger LOG = LoggerFactory.getLogger(HelloInventoryExporter.class);
	
	private final HelloInventoryOperationProcessor processor;
	private final NotificationProviderService notiService;
	private final ProviderContext session;
	
	public static final InstanceIdentifier<Nodes> NODES_IID = 
			InstanceIdentifier.builder(Nodes.class).build();
	
	HelloInventoryExporter (HelloInventoryOperationProcessor processor, NotificationProviderService notiService, ProviderContext session) {
		this.processor = Preconditions.checkNotNull(processor);
		this.notiService = notiService;
		this.session = session;
	}
	
	//test routed rpc
	/*void inserNodes4RoutedRPC() {
		LOG.info("testRoutedRPC - HelloInventoryExporter");
		
		//adding test nodes into operational db
		List<Node> lNodes = new LinkedList<Node>();
		Node node1 = new NodeBuilder().
				setNodeId(new NodeId("labry")).
				setKey(new NodeKey(new NodeId("labry"))).
				setNodeType(NodeType.Ptn).build();
		Node node2 = new NodeBuilder().
				setNodeId(new NodeId("jshin")).
				setKey(new NodeKey(new NodeId("jshin"))).
				setNodeType(NodeType.Otn).build();
		lNodes.add(node1);
		lNodes.add(node2);
		
		DataBroker db = session.getSALService(DataBroker.class);
		final ReadWriteTransaction tx = db.newReadWriteTransaction();
		tx.put(LogicalDatastoreType.OPERATIONAL, NODES_IID, new NodesBuilder().setNode(lNodes).build());
		tx.submit();
	}*/
	
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
		LOG.info("HelloInventoryExporter onNodeUpdated: " + notification.getName());
		
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

	/*
	 * TsdnInventoryProviderService
	 * rpc input: {"input":{"strin":"***"}}
	 */
	@Override
	public Future<RpcResult<TestNotificationBetweenModulesOutput>> testNotificationBetweenModules(
			TestNotificationBetweenModulesInput input) {
		LOG.info("HelloInventoryExporter - testNotificationBetweenModules");
		
		//tree path
		org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.nodes.NodeKey key = 
				new org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.nodes.NodeKey(new NodeId(input.getStrin()));
		InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.nodes.Node> path = 
				InstanceIdentifier.create(org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.Nodes.class)
				.child(org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.nodes.Node.class, key);

		NodeUpdated noti = new NodeUpdatedBuilder().
				setName(input.getStrin()).setNodeRef(new NodeRef(path)).build();
		this.notiService.publish(noti);

		//return
		TestNotificationBetweenModulesOutputBuilder builder = new TestNotificationBetweenModulesOutputBuilder();
		builder.setStrout("Notification acomplished." + input.getStrin());
		
		return RpcResultBuilder.success(builder.build()).buildFuture();
	}

	@Override
	public Future<RpcResult<TestRoutedRpcOutput>> testRoutedRpc(TestRoutedRpcInput input) {
		LOG.info("HelloInventoryExporter - testRoutedRpc");
		
		TsdnInventoryService routedRpc = session.getRpcService(TsdnInventoryService.class);
		
		NodeKey nodeKey = null;
		if (input.getStrin().equals("labry")) {
			nodeKey = new NodeKey(new NodeId("labry"));
		} else {
			nodeKey = new NodeKey(new NodeId("jshin"));
		}
		InstanceIdentifier<Node> path = InstanceIdentifier.create(Nodes.class).child(Node.class, nodeKey);
		NodeRef nodeRef = new NodeRef(path);
		
		SetTunnelInput tunnelInput = new SetTunnelInputBuilder().setNodeRef(nodeRef).build();
		Future<RpcResult<SetTunnelOutput>> result = routedRpc.setTunnel(tunnelInput);
		
		String strResult = null;
		try {
			strResult = result.get().toString();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return RpcResultBuilder.success(new TestRoutedRpcOutputBuilder().setStrout(strResult)).buildFuture();
	}
}
