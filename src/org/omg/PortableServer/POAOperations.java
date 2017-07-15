package org.omg.PortableServer;

public interface POAOperations{
    POA create_POA(String adapter_name,POAManager a_POAManager,org.omg.CORBA.Policy[] policies) throws org.omg.PortableServer.POAPackage.AdapterAlreadyExists, org.omg.PortableServer.POAPackage.InvalidPolicy;

    POA find_POA(String adapter_name,boolean activate_it) throws org.omg.PortableServer.POAPackage.AdapterNonExistent;

    void destroy(boolean etherealize_objects,boolean wait_for_completion);

    ThreadPolicy create_thread_policy(ThreadPolicyValue value);

    LifespanPolicy create_lifespan_policy(LifespanPolicyValue value);

    IdUniquenessPolicy create_id_uniqueness_policy(IdUniquenessPolicyValue value);

    IdAssignmentPolicy create_id_assignment_policy(IdAssignmentPolicyValue value);

    ImplicitActivationPolicy create_implicit_activation_policy(ImplicitActivationPolicyValue value);

    ServantRetentionPolicy create_servant_retention_policy(ServantRetentionPolicyValue value);

    RequestProcessingPolicy create_request_processing_policy(RequestProcessingPolicyValue value);

    String the_name();

    POA the_parent();

    POA[] the_children();

    POAManager the_POAManager();

    AdapterActivator the_activator();

    void the_activator(AdapterActivator newThe_activator);

    ServantManager get_servant_manager() throws org.omg.PortableServer.POAPackage.WrongPolicy;

    void set_servant_manager(ServantManager imgr) throws org.omg.PortableServer.POAPackage.WrongPolicy;

    Servant get_servant() throws org.omg.PortableServer.POAPackage.NoServant, org.omg.PortableServer.POAPackage.WrongPolicy;

    void set_servant(Servant p_servant) throws org.omg.PortableServer.POAPackage.WrongPolicy;

    byte[] activate_object(Servant p_servant) throws org.omg.PortableServer.POAPackage.ServantAlreadyActive, org.omg.PortableServer.POAPackage.WrongPolicy;

    void activate_object_with_id(byte[] id,Servant p_servant) throws org.omg.PortableServer.POAPackage.ServantAlreadyActive, org.omg.PortableServer.POAPackage.ObjectAlreadyActive, org.omg.PortableServer.POAPackage.WrongPolicy;

    void deactivate_object(byte[] oid) throws org.omg.PortableServer.POAPackage.ObjectNotActive, org.omg.PortableServer.POAPackage.WrongPolicy;

    org.omg.CORBA.Object create_reference(String intf) throws org.omg.PortableServer.POAPackage.WrongPolicy;

    org.omg.CORBA.Object create_reference_with_id(byte[] oid,String intf);

    byte[] servant_to_id(Servant p_servant) throws org.omg.PortableServer.POAPackage.ServantNotActive, org.omg.PortableServer.POAPackage.WrongPolicy;

    org.omg.CORBA.Object servant_to_reference(Servant p_servant) throws org.omg.PortableServer.POAPackage.ServantNotActive, org.omg.PortableServer.POAPackage.WrongPolicy;

    Servant reference_to_servant(org.omg.CORBA.Object reference) throws org.omg.PortableServer.POAPackage.ObjectNotActive, org.omg.PortableServer.POAPackage.WrongPolicy, org.omg.PortableServer.POAPackage.WrongAdapter;

    byte[] reference_to_id(org.omg.CORBA.Object reference) throws org.omg.PortableServer.POAPackage.WrongAdapter, org.omg.PortableServer.POAPackage.WrongPolicy;

    Servant id_to_servant(byte[] oid) throws org.omg.PortableServer.POAPackage.ObjectNotActive, org.omg.PortableServer.POAPackage.WrongPolicy;

    org.omg.CORBA.Object id_to_reference(byte[] oid) throws org.omg.PortableServer.POAPackage.ObjectNotActive, org.omg.PortableServer.POAPackage.WrongPolicy;

    byte[] id();
} // interface POAOperations
