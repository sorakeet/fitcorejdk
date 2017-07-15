/**
 * Copyright (c) 1999, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.ldap;

public interface Control extends java.io.Serializable{
    public static final boolean CRITICAL=true;
    public static final boolean NONCRITICAL=false;

    public String getID();

    public boolean isCritical();

    public byte[] getEncodedValue();
    // static final long serialVersionUID = -591027748900004825L;
}
