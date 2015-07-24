package org.etri.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorld;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldReadInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldReadOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldReadOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldWriteInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldWriteOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.HelloWorldWriteOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.MultipleOfFive;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev150105.MultipleOfFiveBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public class HelloWorldImpl implements HelloService, DataChangeListener, HelloListener {

	private static final Logger LOG = LoggerFactory.getLogger(HelloProvider.class);
	
	/**
	 * for DB
	 */
	private DataBroker db;
	public static final InstanceIdentifier<HelloWorld> HELLO_IID = 
			InstanceIdentifier.builder(HelloWorld.class).build();
	
	public void setDB (final DataBroker db) {
		this.db = db;
	}
	
	/**
	 * global variable
	 */
	private long helloCounter;
	
	/**
	 * for notification
	 */
	private NotificationProviderService notificationProvider;
	public void setNotificationProvider (final NotificationProviderService noti) {
		this.notificationProvider = noti;
	}
	
	private void sendNotificationOrNot () {
		if (((helloCounter % 5) == 0) && (helloCounter != 0)) {
			MultipleOfFive noti = new MultipleOfFiveBuilder().build();
			notificationProvider.publish(noti);
		}
	}
	
	/**
	 * RPC methods
	 */
	@Override
	public Future<RpcResult<HelloWorldOutput>> helloWorld(HelloWorldInput input) {
		//DB
		final ReadWriteTransaction tx = db.newReadWriteTransaction();
		
		//counter
		tx.put(LogicalDatastoreType.CONFIGURATION, HELLO_IID, 
				new HelloWorldBuilder().setCounter(helloCounter++).build());
		tx.submit();
		LOG.info("[jshin]helloCount(hello): " + helloCounter);

		//notification
		sendNotificationOrNot();
		
		//rpc return
		HelloWorldOutputBuilder helloBuilder = new HelloWorldOutputBuilder();
		helloBuilder.setStrout("This is greeting " + input.getStrin());
		
		return RpcResultBuilder.success(helloBuilder.build()).buildFuture();
	}

	@Override
	public Future<RpcResult<HelloWorldReadOutput>> helloWorldRead(HelloWorldReadInput input) {
		//DB
		final ReadWriteTransaction tx = db.newReadWriteTransaction();

		Future<Optional<HelloWorld>> readFuture = 
				tx.read(LogicalDatastoreType.OPERATIONAL, HELLO_IID);
		
		//counter
		tx.put(LogicalDatastoreType.CONFIGURATION, HELLO_IID, 
				new HelloWorldBuilder().setCounter(helloCounter++).build());

		try {
			tx.submit().get();
		} catch (InterruptedException | ExecutionException e) {
			LOG.warn("[jshin]db exception: ", e);
			e.printStackTrace();
		}
		LOG.info("[jshin]helloCount(read): " + helloCounter);

		//notification
		sendNotificationOrNot();
		
		//rpc return
		HelloWorldReadOutputBuilder helloReadBuilder = new HelloWorldReadOutputBuilder();
		try {
			helloReadBuilder.setStrout(input.getStrin() + ", " + readFuture.get().get().getCounter());
		} catch (InterruptedException | ExecutionException e) {
			LOG.warn("[jshin]db exception: ", e);
			e.printStackTrace();
		}

		return RpcResultBuilder.success(helloReadBuilder.build()).buildFuture();
	}

	@Override
	public Future<RpcResult<HelloWorldWriteOutput>> helloWorldWrite(HelloWorldWriteInput input) {
		//DB
		final ReadWriteTransaction tx = db.newReadWriteTransaction();

		tx.put(LogicalDatastoreType.OPERATIONAL, HELLO_IID, 
				new HelloWorldBuilder().setValue(input.getStrin()).build());

		//counter
		tx.put(LogicalDatastoreType.CONFIGURATION, HELLO_IID, 
				new HelloWorldBuilder().setCounter(helloCounter++).build());

		try {
			tx.submit().get();
		} catch (InterruptedException | ExecutionException e) {
			LOG.warn("[jshin]db exception: ", e);
			e.printStackTrace();
		}
		LOG.info("[jshin]helloCount(write): " + helloCounter);

		//notification
		sendNotificationOrNot();
		
		//rpc return
		HelloWorldWriteOutputBuilder helloWriteBuilder = new HelloWorldWriteOutputBuilder();
		helloWriteBuilder.setStrout(input.getStrin());
		
		return RpcResultBuilder.success(helloWriteBuilder.build()).buildFuture();
	}

	/**
	 * HelloListener method
	 */
	@Override
	public void onMultipleOfFive(MultipleOfFive notification) {
		LOG.info("[jshin]notification received: onMultipleOfFive.");
	}

	/**
	 * DataChangeListener method
	 */
	@Override
	public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
		DataObject data = change.getUpdatedSubtree();
		if (data instanceof HelloWorld) {
			LOG.info("[jshin]onDataChanged by listener: " + helloCounter);
		}
	}

}
