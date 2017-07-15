/**
 * Copyright (c) 1999, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.ldap;

import javax.naming.NamingException;

public interface ExtendedRequest extends java.io.Serializable{
    public String getID();

    public byte[] getEncodedValue();

    public ExtendedResponse createExtendedResponse(String id,
                                                   byte[] berValue,int offset,int length) throws NamingException;
    // static final long serialVersionUID = -7560110759229059814L;
}
