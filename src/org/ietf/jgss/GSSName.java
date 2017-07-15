/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.ietf.jgss;

public interface GSSName{
    public static final Oid NT_HOSTBASED_SERVICE
            =Oid.getInstance("1.2.840.113554.1.2.1.4");
    public static final Oid NT_USER_NAME
            =Oid.getInstance("1.2.840.113554.1.2.1.1");
    public static final Oid NT_MACHINE_UID_NAME
            =Oid.getInstance("1.2.840.113554.1.2.1.2");
    public static final Oid NT_STRING_UID_NAME
            =Oid.getInstance("1.2.840.113554.1.2.1.3");
    public static final Oid NT_ANONYMOUS
            =Oid.getInstance("1.3.6.1.5.6.3");
    public static final Oid NT_EXPORT_NAME
            =Oid.getInstance("1.3.6.1.5.6.4");

    public boolean equals(GSSName another) throws GSSException;

    public int hashCode();

    public boolean equals(Object another);

    public String toString();

    public GSSName canonicalize(Oid mech) throws GSSException;

    public byte[] export() throws GSSException;

    public Oid getStringNameType() throws GSSException;

    public boolean isAnonymous();

    public boolean isMN();
}
