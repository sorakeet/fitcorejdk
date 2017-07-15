package org.omg.PortableInterceptor;

public interface IORInterceptor_3_0Operations extends IORInterceptorOperations{
    void components_established(IORInfo info);

    void adapter_manager_state_changed(int id,short state);

    void adapter_state_changed(ObjectReferenceTemplate[] templates,short state);
} // interface IORInterceptor_3_0Operations
