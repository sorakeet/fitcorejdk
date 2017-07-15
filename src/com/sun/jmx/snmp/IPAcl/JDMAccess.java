/**
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Generated By:JJTree: Do not edit this line. JDMAccess.java
 */
/** Generated By:JJTree: Do not edit this line. JDMAccess.java */
package com.sun.jmx.snmp.IPAcl;

class JDMAccess extends SimpleNode{
    protected int access=-1;

    JDMAccess(int id){
        super(id);
    }

    JDMAccess(Parser p,int id){
        super(p,id);
    }

    public static Node jjtCreate(int id){
        return new JDMAccess(id);
    }

    public static Node jjtCreate(Parser p,int id){
        return new JDMAccess(p,id);
    }

    protected void putPermission(AclEntryImpl entry){
        if(access==ParserConstants.RO){
            // We have a read-only access.
            //
            entry.addPermission(SnmpAcl.getREAD());
        }
        if(access==ParserConstants.RW){
            // We have a read-write access.
            //
            entry.addPermission(SnmpAcl.getREAD());
            entry.addPermission(SnmpAcl.getWRITE());
        }
    }
}
