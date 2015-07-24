package org.etri.impl4sb2;

import java.util.concurrent.Future;

import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RoutedRpcRegistration;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.general.types.rev150105.ResponseType;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.DeleteAccessIfInput;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.DeleteAccessIfOutput;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.DeleteDelegatedServiceInput;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.DeleteDelegatedServiceOutput;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.DeleteTunnelInput;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.DeleteTunnelOutput;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.DeleteTunnelXcInput;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.DeleteTunnelXcOutput;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.Nodes;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.SetAccessIfInput;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.SetAccessIfOutput;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.SetDelegatedServiceInput;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.SetDelegatedServiceOutput;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.SetTunnelInput;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.SetTunnelOutput;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.SetTunnelOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.SetTunnelXcInput;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.SetTunnelXcOutput;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.TsdnInventoryService;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.inventory.rev150105.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.node.rev150105.NodeContext;
import org.opendaylight.yang.gen.v1.urn.etri.params.xml.ns.yang.tsdn.node.rev150105.NodeId;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;

//TsdnInventoryListener: to hear notification
//TsdnInventoryProviderService: to implement rpc method
class HelloSouthbound2Exporter <T extends DataObject> implements TsdnInventoryService {

	private static final Logger LOG = LoggerFactory.getLogger(HelloSouthbound2Exporter.class);
	
	private final HelloSouthbound2OperationProcessor processor;
	private final NotificationProviderService notiService;
	
	HelloSouthbound2Exporter (ProviderContext session, HelloSouthbound2OperationProcessor processor, NotificationProviderService notiService) {
		this.processor = Preconditions.checkNotNull(processor);
		this.notiService = notiService;

		//register routed rpc
		NodeKey nodeKey = new NodeKey(new NodeId("jshin"));
		InstanceIdentifier<Node> path = 
				InstanceIdentifier.
				create(Nodes.class).
				child(Node.class, nodeKey);
		
		RoutedRpcRegistration<TsdnInventoryService> routedRpcReg = 
				session.addRoutedRpcImplementation(TsdnInventoryService.class, this);
		routedRpcReg.registerPath(NodeContext.class, path);
	}

	/*
	 * TsdnInventoryService
	 */
	@Override
	public Future<RpcResult<DeleteAccessIfOutput>> deleteAccessIf(
			DeleteAccessIfInput input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<RpcResult<DeleteDelegatedServiceOutput>> deleteDelegatedService(
			DeleteDelegatedServiceInput input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<RpcResult<DeleteTunnelOutput>> deleteTunnel(
			DeleteTunnelInput input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<RpcResult<DeleteTunnelXcOutput>> deleteTunnelXc(
			DeleteTunnelXcInput input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<RpcResult<SetAccessIfOutput>> setAccessIf(
			SetAccessIfInput input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<RpcResult<SetDelegatedServiceOutput>> setDelegatedService(
			SetDelegatedServiceInput input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<RpcResult<SetTunnelOutput>> setTunnel(SetTunnelInput input) {

		LOG.info("HelloSB2Exporter is called");

		return RpcResultBuilder.success(new SetTunnelOutputBuilder().setResponse(ResponseType.Ok).build()).buildFuture();
	}

	@Override
	public Future<RpcResult<SetTunnelXcOutput>> setTunnelXc(
			SetTunnelXcInput input) {
		// TODO Auto-generated method stub
		return null;
	}
}
