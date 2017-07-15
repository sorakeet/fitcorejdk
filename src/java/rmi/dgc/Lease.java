/**
 * Copyright (c) 1996, 1998, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.dgc;

public final class Lease implements java.io.Serializable{
    private static final long serialVersionUID=-5713411624328831948L;
    private VMID vmid;
    private long value;

    public Lease(VMID id,long duration){
        vmid=id;
        value=duration;
    }

    public VMID getVMID(){
        return vmid;
    }

    public long getValue(){
        return value;
    }
}
