package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.impl4sb.rev141210;

import org.etri.impl4sb.Hello4sbProvider;

public class Hello4sbModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.impl4sb.rev141210.AbstractHello4sbModule {
    public Hello4sbModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public Hello4sbModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.impl4sb.rev141210.Hello4sbModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        Hello4sbProvider provider = new Hello4sbProvider();
        getBrokerDependency().registerProvider(provider);
        
        //JMX
        Hello4sbRuntimeRegistration runtimeReg = getRootRuntimeBeanRegistratorWrapper().register(provider);
        
        return provider;
    }

}
