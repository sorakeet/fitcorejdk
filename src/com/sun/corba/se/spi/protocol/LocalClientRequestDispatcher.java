/**
 * Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.protocol;

import org.omg.CORBA.portable.ServantObject;

public interface LocalClientRequestDispatcher{
    public boolean useLocalInvocation(org.omg.CORBA.Object self);

    public boolean is_local(org.omg.CORBA.Object self);

    public ServantObject servant_preinvoke(org.omg.CORBA.Object self,
                                           String operation,
                                           Class expectedType);

    public void servant_postinvoke(org.omg.CORBA.Object self,
                                   ServantObject servant);
}
// End of file.
