package org.omg.PortableServer;

public interface POAManagerOperations{
    void activate() throws org.omg.PortableServer.POAManagerPackage.AdapterInactive;

    void hold_requests(boolean wait_for_completion) throws org.omg.PortableServer.POAManagerPackage.AdapterInactive;

    void discard_requests(boolean wait_for_completion) throws org.omg.PortableServer.POAManagerPackage.AdapterInactive;

    void deactivate(boolean etherealize_objects,boolean wait_for_completion) throws org.omg.PortableServer.POAManagerPackage.AdapterInactive;

    org.omg.PortableServer.POAManagerPackage.State get_state();
} // interface POAManagerOperations
