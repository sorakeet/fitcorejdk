/**
 * Copyright (c) 1996, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi;

@Deprecated
public class RMISecurityException extends SecurityException{
    private static final long serialVersionUID=-8433406075740433514L;

    @Deprecated
    public RMISecurityException(String name,String arg){
        this(name);
    }

    @Deprecated
    public RMISecurityException(String name){
        super(name);
    }
}
