/**
 * Copyright (c) 1999, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;
// java import

import java.io.Serializable;

public abstract class QueryEval implements Serializable{
    private static final long serialVersionUID=2675899265640874796L;
    private static ThreadLocal<MBeanServer> server=
            new InheritableThreadLocal<MBeanServer>();

    public static MBeanServer getMBeanServer(){
        return server.get();
    }

    public void setMBeanServer(MBeanServer s){
        server.set(s);
    }
}
