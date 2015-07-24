package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.impl4inv.rev141210;

import org.etri.impl4inv.Hello4invProvider;

public class Hello4invModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.impl4inv.rev141210.AbstractHello4invModule {
    public Hello4invModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public Hello4invModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.impl4inv.rev141210.Hello4invModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        Hello4invProvider provider = new Hello4invProvider();
        getBrokerDependency().registerProvider(provider);
        return provider;
    }

}
