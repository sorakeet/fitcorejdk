package org.omg.PortableInterceptor;

public interface ClientRequestInfoOperations extends RequestInfoOperations{
    org.omg.CORBA.Object target();

    org.omg.CORBA.Object effective_target();

    org.omg.IOP.TaggedProfile effective_profile();

    org.omg.CORBA.Any received_exception();

    String received_exception_id();

    org.omg.IOP.TaggedComponent get_effective_component(int id);

    org.omg.IOP.TaggedComponent[] get_effective_components(int id);

    org.omg.CORBA.Policy get_request_policy(int type);

    void add_request_service_context(org.omg.IOP.ServiceContext service_context,boolean replace);
} // interface ClientRequestInfoOperations
