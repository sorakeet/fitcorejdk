/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.PortableServer;

abstract public class DynamicImplementation extends Servant{
    abstract public void invoke(org.omg.CORBA.ServerRequest request);
}
