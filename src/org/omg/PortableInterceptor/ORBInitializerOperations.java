package org.omg.PortableInterceptor;

public interface ORBInitializerOperations{
    void pre_init(ORBInitInfo info);

    void post_init(ORBInitInfo info);
} // interface ORBInitializerOperations
