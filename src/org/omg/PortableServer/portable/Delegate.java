/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.PortableServer.portable;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

public interface Delegate{
    org.omg.CORBA.ORB orb(Servant Self);

    org.omg.CORBA.Object this_object(Servant Self);

    POA poa(Servant Self);

    byte[] object_id(Servant Self);

    POA default_POA(Servant Self);

    boolean is_a(Servant Self,String Repository_Id);

    boolean non_existent(Servant Self);
    //Simon And Ken Will Ask About Editorial Changes
    //In Idl To Java For The Following Signature.
// The get_interface() method has been replaced by get_interface_def()
    //org.omg.CORBA.Object get_interface(Servant Self);

    org.omg.CORBA.Object get_interface_def(Servant self);
}
