/**
 * Copyright (c) 1999, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.directory;

import javax.naming.NamingEnumeration;

public interface Attributes extends Cloneable, java.io.Serializable{
    boolean isCaseIgnored();

    int size();

    Attribute get(String attrID);

    NamingEnumeration<? extends Attribute> getAll();

    NamingEnumeration<String> getIDs();

    Attribute put(String attrID,Object val);

    Attribute put(Attribute attr);

    Attribute remove(String attrID);

    Object clone();
    /**
     * Use serialVersionUID from JNDI 1.1.1 for interoperability
     */
    // static final long serialVersionUID = -7247874645443605347L;
}
