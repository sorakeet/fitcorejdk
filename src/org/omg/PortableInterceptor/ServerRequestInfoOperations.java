package org.omg.PortableInterceptor;

public interface ServerRequestInfoOperations extends RequestInfoOperations{
    org.omg.CORBA.Any sending_exception();

    byte[] object_id();

    byte[] adapter_id();

    String server_id();

    String orb_id();

    String[] adapter_name();

    String target_most_derived_interface();

    org.omg.CORBA.Policy get_server_policy(int type);

    void set_slot(int id,org.omg.CORBA.Any data) throws InvalidSlot;

    boolean target_is_a(String id);

    void add_reply_service_context(org.omg.IOP.ServiceContext service_context,boolean replace);
} // interface ServerRequestInfoOperations
