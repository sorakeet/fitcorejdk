package org.omg.PortableInterceptor;

public interface IORInfoOperations{
    org.omg.CORBA.Policy get_effective_policy(int type);

    void add_ior_component(org.omg.IOP.TaggedComponent tagged_component);

    void add_ior_component_to_profile(org.omg.IOP.TaggedComponent tagged_component,int profile_id);

    int manager_id();

    short state();

    ObjectReferenceTemplate adapter_template();

    ObjectReferenceFactory current_factory();

    void current_factory(ObjectReferenceFactory newCurrent_factory);
} // interface IORInfoOperations
