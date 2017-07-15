/**
 * Copyright (c) 1999, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.ldap;

public interface ExtendedResponse extends java.io.Serializable{
    public String getID();

    public byte[] getEncodedValue();
    //static final long serialVersionUID = -3320509678029180273L;
}
