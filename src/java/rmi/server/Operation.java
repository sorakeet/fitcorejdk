/**
 * Copyright (c) 1996, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.server;

@Deprecated
public class Operation{
    private String operation;

    @Deprecated
    public Operation(String op){
        operation=op;
    }

    @Deprecated
    public String getOperation(){
        return operation;
    }

    @Deprecated
    public String toString(){
        return operation;
    }
}
