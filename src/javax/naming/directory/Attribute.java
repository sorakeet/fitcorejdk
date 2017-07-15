/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.directory;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

public interface Attribute extends Cloneable, java.io.Serializable{
    static final long serialVersionUID=8707690322213556804L;

    NamingEnumeration<?> getAll() throws NamingException;

    Object get() throws NamingException;

    int size();

    String getID();

    boolean contains(Object attrVal);

    boolean add(Object attrVal);

    boolean remove(Object attrval);

    void clear();

    DirContext getAttributeSyntaxDefinition() throws NamingException;

    DirContext getAttributeDefinition() throws NamingException;
    //----------- Methods to support ordered multivalued attributes

    Object clone();

    boolean isOrdered();

    Object get(int ix) throws NamingException;

    Object remove(int ix);

    void add(int ix,Object attrVal);

    Object set(int ix,Object attrVal);
}
