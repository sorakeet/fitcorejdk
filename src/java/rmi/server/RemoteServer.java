/**
 * Copyright (c) 1996, 2002, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.server;

import sun.rmi.server.UnicastServerRef;

public abstract class RemoteServer extends RemoteObject{
    private static final long serialVersionUID=-4100238210092549637L;
    // initialize log status
    private static boolean logNull=!UnicastServerRef.logCalls;

    protected RemoteServer(){
        super();
    }

    protected RemoteServer(RemoteRef ref){
        super(ref);
    }

    public static String getClientHost() throws ServerNotActiveException{
        return sun.rmi.transport.tcp.TCPTransport.getClientHost();
    }

    public static java.io.PrintStream getLog(){
        return (logNull?null:UnicastServerRef.callLog.getPrintStream());
    }

    public static void setLog(java.io.OutputStream out){
        logNull=(out==null);
        UnicastServerRef.callLog.setOutputStream(out);
    }
}
