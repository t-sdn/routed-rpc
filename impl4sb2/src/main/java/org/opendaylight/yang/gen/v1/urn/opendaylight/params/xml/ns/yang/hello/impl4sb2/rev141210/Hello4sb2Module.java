package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.impl4sb2.rev141210;

import org.etri.impl4sb2.Hello4sb2Provider;

public class Hello4sb2Module extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.impl4sb2.rev141210.AbstractHello4sb2Module {
    public Hello4sb2Module(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public Hello4sb2Module(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.impl4sb2.rev141210.Hello4sb2Module oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
    	Hello4sb2Provider provider = new Hello4sb2Provider();
    	getBrokerDependency().registerProvider(provider);
    	return provider;
    }

}
