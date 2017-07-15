package org.omg.PortableInterceptor;

public interface RequestInfoOperations{
    int request_id();

    String operation();

    org.omg.Dynamic.Parameter[] arguments();

    org.omg.CORBA.TypeCode[] exceptions();

    String[] contexts();

    String[] operation_context();

    org.omg.CORBA.Any result();

    boolean response_expected();

    short sync_scope();

    short reply_status();

    org.omg.CORBA.Object forward_reference();

    org.omg.CORBA.Any get_slot(int id) throws InvalidSlot;

    org.omg.IOP.ServiceContext get_request_service_context(int id);

    org.omg.IOP.ServiceContext get_reply_service_context(int id);
} // interface RequestInfoOperations
