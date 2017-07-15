/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.ldap;

public class BasicControl implements Control{
    private static final long serialVersionUID=-4233907508771791687L;
    protected String id;
    protected boolean criticality=false; // default
    protected byte[] value=null;

    public BasicControl(String id){
        this.id=id;
    }

    public BasicControl(String id,boolean criticality,byte[] value){
        this.id=id;
        this.criticality=criticality;
        this.value=value;
    }

    public String getID(){
        return id;
    }

    public boolean isCritical(){
        return criticality;
    }

    public byte[] getEncodedValue(){
        return value;
    }
}
